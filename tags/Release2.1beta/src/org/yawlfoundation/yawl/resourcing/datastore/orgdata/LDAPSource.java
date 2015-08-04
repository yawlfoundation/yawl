package org.yawlfoundation.yawl.resourcing.datastore.orgdata;


import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Role;
import org.yawlfoundation.yawl.resourcing.util.Docket;
import org.yawlfoundation.yawl.util.PasswordEncryptor;
import org.yawlfoundation.yawl.exceptions.YAuthenticationException;

import javax.naming.*;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.io.FileInputStream;
import java.util.*;

/**
 * Author: Michael Adams
 * Creation Date: 5/03/2010
 */
public class LDAPSource extends DataSource {

    private Properties _props = null;
    private Hashtable<String, String> _attributeMap = null;
    private Hashtable<String, Object> _environment = null;
    private Hashtable<String, String> _user2nameMap = null;
    private HashMap<String, Role> _roles = null;

    public LDAPSource() {
        loadProperties();
        initMaps();
    }

    private void loadProperties() {
        try {
            _props = new Properties();
            String path = Docket.getPackageFileDir("datastore/orgdata/");
            _props.load(new FileInputStream(path + "LDAPSource.properties"));
        }
        catch (Exception e) {
            _props = null;           // this will cause a controlled service disablement
        }
    }

    private void initMaps() {
        _roles = new HashMap<String, Role>();
        if (getProperty("delegateauthentication").equalsIgnoreCase("true")) {
            _user2nameMap = new Hashtable<String, String>();
        }
    }

    private String getProperty(String key) {
        return _props.getProperty(key);
    }

    private Hashtable<String, String> getAttributeMap() {
        if (_attributeMap == null) {
            _attributeMap = new Hashtable<String, String>();
            if (_props != null) {

                // these are mandatory
                _attributeMap.put("userid", getProperty("userid"));
                _attributeMap.put("firstname", getProperty("firstname"));
                _attributeMap.put("lastname", getProperty("lastname"));

                // these are optional
                String password = getProperty("password");
                if (isNotNullOrEmpty(password)) {
                   _attributeMap.put("password", password);
                }
                String isAdmin = getProperty("administrator");
                if (isNotNullOrEmpty(isAdmin)) {
                   _attributeMap.put("isAdmin", isAdmin);
                }
                String roles = getProperty("roles");
                if (isNotNullOrEmpty(roles)) {
                   _attributeMap.put("roles", roles); 
                }
            }
        }
        return _attributeMap;
    }

    
    private List<String> getNameList() throws NamingException {
        List<String> nameList = new ArrayList<String>();
        Context ctx = new InitialContext(getEnvironment());
        NamingEnumeration list = ctx.list(getProperty("binding"));

        while (list.hasMore()) {
            NameClassPair nc = (NameClassPair) list.next();
            nameList.add(nc.getName());
        }
        ctx.close();

        return nameList;
    }


    private String[] getAttributeIDNames() {
        return getAttributeMap().values().toArray(new String[0]);
    }


    private Hashtable<String, Object> getEnvironment() {
        if (_environment == null) {
            _environment = new Hashtable<String, Object>();
            if (_props != null) {
                String url = String.format("ldap://%s:%s", getProperty("host"), getProperty("port"));
                _environment.put(Context.PROVIDER_URL, url);
                _environment.put(Context.INITIAL_CONTEXT_FACTORY, getProperty("contextfactory"));
                _environment.put(Context.SECURITY_AUTHENTICATION, getProperty("authentication"));
                _environment.put(Context.SECURITY_PRINCIPAL, getProperty("adminusername"));
                _environment.put(Context.SECURITY_CREDENTIALS, getProperty("adminpassword"));
            }
        }
        return _environment;
    }


    private HashMap<String, Participant> loadParticipants() throws NamingException {
        HashMap<String, Participant> map = new HashMap<String, Participant>();
        String[] attrIDs = getAttributeIDNames();
        DirContext ctx = new InitialDirContext(getEnvironment());
        List<String> nameList = getNameList();
        String binding = getProperty("binding");
        for (String name : nameList) {
            Attributes attributes = ctx.getAttributes(name + "," + binding, attrIDs);
            Participant p = createParticipant(name, attributes);
            map.put(p.getID(), p);
        }
        ctx.close();
        return map;
    }

    private Participant createParticipant(String name, Attributes attributes) throws NamingException {
        String lastname = getStringValue(attributes, "lastname");
        String firstname = getStringValue(attributes, "firstname");
        String userid = getStringValue(attributes, "userid");
        Participant p = new Participant(lastname, firstname, userid);
        p.setID("U_" + userid);

        // if authentication is done via LDAP, keep the LDAP name - userid mapping
        if (_user2nameMap != null) {
            _user2nameMap.put(userid, name);
        }
        else {
            p.setPassword(loadUserPassword(attributes));
        }

        // set the roles for the particpant - may be enum or csv list
        if (hasEnumeratedRoles()) {
            setRoles(p, attributes);
        }
        else {
            setRoles(p, getStringValue(attributes, "roles"));
        }

        return p;
    }


    private void setRoles(Participant p, Attributes attributes) throws NamingException {
        Attribute roles = attributes.get(getProperty("roles"));
        if (roles != null) {
            NamingEnumeration e = roles.getAll();
            while (e.hasMoreElements()) {
                addToRole(p, String.valueOf(e.next()));
            }
        }
    }


    private void setRoles(Participant p, String rolesCSV) {
        if (isNotNullOrEmpty(rolesCSV)) {
            String[] roleArray = rolesCSV.split("\\s*,\\s*");
            for (String roleName : roleArray) {
                addToRole(p, roleName);
            }
        }
    }


    private void addToRole(Participant p, String roleName) {
        Role r = _roles.get(roleName);
        if (r == null) {
            r = new Role(roleName);
            r.setID(roleName);
            _roles.put(roleName, r);
        }
        r.addResource(p);
        p.addRole(r);
    }


    private String loadUserPassword(Attributes attributes) {
        String password = null;
        if (getAttributeMap().get("password") != null) {
            try {
                byte[] pwBytes = getByteValue(attributes, "password");
                password = PasswordEncryptor.encrypt(new String(pwBytes));
            }
            catch (Exception e) {
                // do nothing - null will be returned
            }
        }
        return password;
    }


    private String getStringValue(Attributes attributes, String attributeName)
            throws NamingException {
        Attribute attr = getAttribute(attributes, attributeName);
        return (String) attr.get();
    }

    private byte[] getByteValue(Attributes attributes, String attributeName)
            throws NamingException {
        Attribute attr = getAttribute(attributes, attributeName);
        return (byte[]) attr.get();
    }

    private Attribute getAttribute(Attributes attributes, String attributeName)
            throws NamingException {
        String attrID = getAttributeMap().get(attributeName);
        return attributes.get(attrID);       
    }

    private boolean isNotNullOrEmpty(String s) {
        return (s != null) && (s.length() > 0) ;
    }

    private boolean hasEnumeratedRoles() {
        String roleFormat = getProperty("roleformat");
        return (roleFormat != null) && roleFormat.equalsIgnoreCase("enumeration");
    }

    
    // BASE CLASS IMPLEMENTATIONS //

    public ResourceDataSet loadResources() {
        initMaps();                                   // (re)initialise data structures
        ResourceDataSet rds = new ResourceDataSet(this);
        if (_props != null) {
            try {
                rds.setParticipants(loadParticipants(), this);
                if (! _roles.isEmpty()) {
                    rds.setRoles(_roles, this);
                }
            }
            catch (NamingException ne) {
                // thrown by loadParticipants(); nothing to do, as an empty rds will
                // be returned, initialising a controlled service disablement
            }
        }    
        return rds;
    }

    public void update(Object obj) {

    }

    public void delete(Object obj) {

    }

    public String insert(Object obj) {
        return null;
    }

    public void importObj(Object obj) {

    }

    public int execUpdate(String query) {
        return -1;
    }

    public boolean authenticate(String userid, String password) throws
            YAuthenticationException {

        if (_user2nameMap == null) {
            throw new YAuthenticationException(
                    "Cannot authenticate user: LDAP Authentication disabled");
        }
        if (! _user2nameMap.contains(userid)) {
            throw new YAuthenticationException("Unknown userid");
        }

        Hashtable<String,Object> env = getEnvironment() ;
        String userBinding = _user2nameMap.get(userid) + "," + getProperty("binding");
        Object prevID = env.put(Context.SECURITY_PRINCIPAL, userBinding);
        Object prevPW = env.put(Context.SECURITY_CREDENTIALS, password);
        try {
            new InitialDirContext(env);     // will throw exception if credentials wrong
            return true;
        }
        catch (AuthenticationException ae) {
            return false;                       // bad password
        }
        catch (NamingException ne) {
            throw new YAuthenticationException(
                    "Cannot authenticate user: LDAP Authentication exception.", ne);
        }
        finally {
            env.put(Context.SECURITY_PRINCIPAL, prevID);
            env.put(Context.SECURITY_CREDENTIALS, prevPW);
        }

    }
    
}
