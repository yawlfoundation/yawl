package org.yawlfoundation.yawl.resourcing.datastore.orgdata;


import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Role;
import org.yawlfoundation.yawl.resourcing.util.Docket;
import org.yawlfoundation.yawl.util.PasswordEncryptor;

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
    private HashMap<String, Role> _roles = null;

    public LDAPSource() {
        loadProperties();
        _roles = new HashMap<String, Role>();
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

    private String getProperty(String key) {
        return _props.getProperty(key);
    }

    private Hashtable<String, String> getAttributeMap() {
        if (_attributeMap == null) {
            _attributeMap = new Hashtable<String, String>();
            if (_props != null) {
                _attributeMap.put("userid", getProperty("userid"));
                _attributeMap.put("firstname", getProperty("firstname"));
                _attributeMap.put("lastname", getProperty("lastname"));
                _attributeMap.put("password", getProperty("password"));

                // these are optional
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


    private HashMap<String, Participant> loadParticipants() {
        HashMap<String, Participant> map = new HashMap<String, Participant>();
        String[] attrIDs = getAttributeIDNames();
        try {
            DirContext ctx = new InitialDirContext(getEnvironment());
            List<String> nameList = getNameList();
            String binding = getProperty("binding");
            for (String name : nameList) {
                Attributes attributes = ctx.getAttributes(name + "," + binding, attrIDs);
                Participant p = createParticipant(attributes);
                map.put(p.getID(), p);
            }

            ctx.close();
        }
        catch (NamingException e) {
            System.out.println("List Bindings failed: " + e);
        }
        return map;
    }

    private Participant createParticipant(Attributes attributes) throws NamingException {
        String lastname = getStringValue(attributes, "lastname");
        String firstname = getStringValue(attributes, "firstname");
        String userid = getStringValue(attributes, "userid");
        Participant p = new Participant(lastname, firstname, userid);
        p.setID("U_" + userid);

        // password needs a little work
        String password = new String(getByteValue(attributes, "password"));
        try {
            password = PasswordEncryptor.encrypt(password);
        }
        catch (Exception e) {
            // do nothing - just accept plaintext password
        }
        p.setPassword(password);
        setRoles(p, getStringValue(attributes, "roles"));
        return p;
    }

    private void setRoles(Participant p, String roles) {
        if (isNotNullOrEmpty(roles)) {
            String[] roleArray = roles.split("\\s*,\\s*");
            for (String roleName : roleArray) {
                Role r = _roles.get(roleName);
                if (r == null) {
                    r = new Role(roleName);
                    r.setID(roleName);
                    _roles.put(roleName, r);
                }
                r.addResource(p);
                p.addRole(r);
            }
        }
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



    // BASE CLASS IMPLEMENTATIONS //

    public ResourceDataSet loadResources() {
        ResourceDataSet rds = new ResourceDataSet(this);
        if (_props != null) {
            rds.setParticipants(loadParticipants(), this);
            if (! _roles.isEmpty()) {
                rds.setRoles(_roles, this);
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
}
