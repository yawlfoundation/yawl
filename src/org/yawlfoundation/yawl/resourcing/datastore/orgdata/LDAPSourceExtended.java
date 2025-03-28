/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.resourcing.datastore.orgdata;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yawlfoundation.yawl.exceptions.YAuthenticationException;
import org.yawlfoundation.yawl.resourcing.resource.*;

import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.LdapName;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.yawlfoundation.yawl.resourcing.datastore.orgdata.LDAPConstants.*;


/**
 * openLDAP DataSource connecting to an openLDAP server using the YAWL LDAP schema.
 * 
 * This class requires the YAWL openLDAP schema to be active in the openLDAP 
 * server to be used. 
 * 
 * @author florian.quadt@rheni.de
 * @author felix.mannhardt@rheni.de
 */
public class LDAPSourceExtended extends DataSource {

    private final Logger _log;
    private Properties _props = null;
    private Hashtable<String, Object> _environment = null;
    
    // Input map from LDAP
    private Map<LdapName, Attributes> _inputMap;
    
    // output maps containing participants, roles etc.
    private Map<String, Participant> _participantsWithDNasKey;
    private Map<String, Participant> _participantsWithIDasKey;
    private Map<String, Role> _rolesWithDNasKey;
    private Map<String, Role> _rolesWithIDasKey;
    private Map<String, OrgGroup> _orgGroupsWithDNasKey;
    private Map<String, OrgGroup> _orgGroupsWithIDasKey;
    private Map<String, Position> _positionsWithDNasKey;
    private Map<String, Position> _positionsWithIDasKey;
    private Map<String, Capability> _capabilitiesWithDNasKey;
    private Map<String, Capability> _capabilitiesWithIDasKey;
    private Map<String, String> _uid2dnMap;
    
    /** 
     * Default constructor loading properties and initialising internal maps.
     */
    public LDAPSourceExtended() {
        _log = LogManager.getLogger(this.getClass());
        loadProperties();
        initMaps();
    }

    private void initMaps() {
        // init maps
        // this map contains <UID, DN> pairs to resolve a uid to a DN used  
        _uid2dnMap = new HashMap<String, String>();
        _inputMap = new HashMap<LdapName, Attributes>();
        _participantsWithDNasKey = new HashMap<String, Participant>();
        _participantsWithIDasKey = new HashMap<String, Participant>();
        _rolesWithDNasKey = new HashMap<String, Role>();
        _rolesWithIDasKey = new HashMap<String, Role>();
        _orgGroupsWithDNasKey = new HashMap<String, OrgGroup>();
        _orgGroupsWithIDasKey = new HashMap<String, OrgGroup>();
        _capabilitiesWithDNasKey = new HashMap<String, Capability>();
        _capabilitiesWithIDasKey = new HashMap<String, Capability>();
        _positionsWithDNasKey = new HashMap<String, Position>();
        _positionsWithIDasKey = new HashMap<String, Position>();
    }
    
    /**
     * Search LDAP recursively for all relevant object class DNs starting from 
     * binding(s) as root. Then store the DNs and the attribute set for the 
     * according node in the _inputMap. 
     */
    private void fillInputMapFromLDAP() {
        for (String binding : getProperty("binding").split(";")) {
            if (isNotNullOrEmpty(binding)) {                
                DirContext dctx = getDirContext();
                try {
                    NamingEnumeration<SearchResult> searchResult = dctx.search(binding, LDAPConstants.OC_FILTER, LDAPConstants.SUBTREE_SCOPE);
       
                    if (searchResult.hasMore()) {
                        while (searchResult.hasMore()) {
                            NameClassPair nc = searchResult.next();
                            _inputMap.put(new LdapName(nc.getNameInNamespace()), dctx.getAttributes(nc.getNameInNamespace()));
                        }
                    }

                } catch (NamingException ex) {
                    _log.error("Error while reading LDAP.", ex);
                } finally {
                    try {
                        dctx.close();
                    } catch (NamingException ex) {
                        _log.error("DirContext could not be closed.", ex);
                    }
                }
            }
        }
    }
    
    /**
     * In the first pass a loop iterates over the _inputMap and creates object
     * instances like Participant, OrgGroup etc. 
     * No relations (belongsTo, reportsTo) are set here.
     * @throws NamingException 
     */
    private void firstPass() throws NamingException {
        for (Map.Entry<LdapName, Attributes> node : _inputMap.entrySet()) {
            Attribute objectClasses = node.getValue().get("objectClass");
            
            if (objectClasses.contains(OC_PARTICIPANT)) {
                Participant participant = createParticipant(node.getKey(), node.getValue());
                _participantsWithDNasKey.put(node.getKey().toString(), participant);
                _participantsWithIDasKey.put(participant.getID(), participant);
            } else if (objectClasses.contains(OC_ORG_GROUP)) {
                OrgGroup orgGroup = createOrgGroup(node.getKey(), node.getValue());
                _orgGroupsWithDNasKey.put(node.getKey().toString(), orgGroup);
                _orgGroupsWithIDasKey.put(orgGroup.getID(), orgGroup);
            } else if (objectClasses.contains(OC_CAPABILITY_UNIQUE_NAMES)) {
                Capability capability = createCapability(node.getKey(), node.getValue());
                _capabilitiesWithDNasKey.put(node.getKey().toString(), capability);
                _capabilitiesWithIDasKey.put(capability.getID(), capability);
            } else if (objectClasses.contains(OC_POSITION_UNIQUE_NAMES)) {
                Position position = createPosition(node.getKey(), node.getValue());
                _positionsWithDNasKey.put(node.getKey().toString(), position);
                _positionsWithIDasKey.put(position.getID(), position);
            } else if (objectClasses.contains(OC_ROLE_UNIQUE_NAMES)) {
                Role role = createRole(node.getKey(), node.getValue());
                _rolesWithDNasKey.put(node.getKey().toString(),role) ;
                _rolesWithIDasKey.put(role.getID(), role);
            }
        }
    }
    
    /**
     * In the second pass the relations are set (roles, capabilities, positions,
     * belongsTo, reportsTo etc.)
     * @throws InvalidNameException
     * @throws NamingException 
     */
    private void secondPass() throws InvalidNameException, NamingException {
        // Set role members
        for (Map.Entry<String, Role> roleNode : _rolesWithDNasKey.entrySet()) {
            Role role = roleNode.getValue();
            LdapName roleDN = new LdapName(roleNode.getKey());
            
            Attributes roleAttributes = _inputMap.get(roleDN);
            Attribute memberAttributes = roleAttributes.get(ATTR_UNIQUE_MEMBER);
            if (memberAttributes != null) {
                NamingEnumeration members = memberAttributes.getAll();
                while (members.hasMore()) {
                    Object member = members.next();
                    if (member != null && member.getClass().equals(String.class)) {
                        String dn = (String) member;
                        if (_participantsWithDNasKey.containsKey(dn)) {
                            _participantsWithDNasKey.get(dn).addRole(role);
                        } else {
                            _log.error("Role (" + role.getName() + ") member " + dn + " does not exist.");
                        }
                    } else {
                        _log.info("Role " + role.getName() + " has an invalid member ");
                    }
                }
            }
        }
        
        // Set capability members
        for (Map.Entry<String, Capability> capabilityNode : _capabilitiesWithDNasKey.entrySet()) {
            Capability capability = capabilityNode.getValue();
            LdapName roleDN = new LdapName(capabilityNode.getKey());
            
            Attributes capabilityAttributes = _inputMap.get(roleDN);
            Attribute memberAttributes = capabilityAttributes.get(ATTR_UNIQUE_MEMBER);
            if (memberAttributes != null) {
                NamingEnumeration members = memberAttributes.getAll();
                while (members.hasMore()) {
                    Object member = members.next();
                    if (member != null && member.getClass().equals(String.class)) {
                        String dn = (String) member;
                        if (_participantsWithDNasKey.containsKey(dn)) {
                            _participantsWithDNasKey.get(dn).addCapability(capability);
                        } else {
                            _log.error("Capability (" + capability.getName() + ") member " + dn + " does not exist.");
                        }
                    } else {
                        _log.info("Role " + capability.getName() + " has an invalid member ");
                    }
                }
            }
        }
        
        // Set position members
        for (Map.Entry<String, Position> positionNode : _positionsWithDNasKey.entrySet()) {
            Position position = positionNode.getValue();
            LdapName positionDN = new LdapName(positionNode.getKey());
            
            Attributes positionAttributes = _inputMap.get(positionDN);
            Attribute memberAttributes = positionAttributes.get(ATTR_UNIQUE_MEMBER);
            if (memberAttributes != null) {
                NamingEnumeration members = memberAttributes.getAll();
                while (members.hasMore()) {
                    Object member = members.next();
                    if (member != null && member.getClass().equals(String.class)) {
                        String dn = (String) member;
                        if (_participantsWithDNasKey.containsKey(dn)) {
                            _participantsWithDNasKey.get(dn).addPosition(position);
                        } else {
                            _log.error("Position (" + position.getName() + ") member " + dn + " does not exist.");
                        }
                    } else {
                        _log.info("Position " + position.getName() + " has an invalid member ");
                    }
                }
            }
        }
    
        
        // Set OrgGroups belongsTo
        for (Map.Entry<String, OrgGroup> orgGroupNode : _orgGroupsWithDNasKey.entrySet()) {
            LdapName orgGroupDN = new LdapName(orgGroupNode.getKey());
            String parent = orgGroupDN.getPrefix(orgGroupDN.size()-1).toString();
            
            if (_orgGroupsWithDNasKey.containsKey(parent)) {
                OrgGroup parentOrgGroup = _orgGroupsWithDNasKey.get(parent);
                orgGroupNode.getValue().setBelongsTo(parentOrgGroup);
            }
        }
        
        // Set Position belongsTo
        for (Map.Entry<String, Position> positionNode : _positionsWithDNasKey.entrySet()) {
            Position currentPosition = positionNode.getValue();
            LdapName positionDN = new LdapName(positionNode.getKey());
            String parent = positionDN.getPrefix(positionDN.size()-1).toString();
            
            if (_orgGroupsWithDNasKey.containsKey(parent)) {
                // position is child of orgGroup
                OrgGroup parentOrgGroup = _orgGroupsWithDNasKey.get(parent);
                currentPosition.setOrgGroup(parentOrgGroup);
                currentPosition.setLabel(currentPosition.getName() + " (" + parentOrgGroup.getName() + ")");
            } 
            
            // Set belongsTo relation by attribute (prio 1) and reportsTo relation
            Attributes positionAttributes = _inputMap.get(positionDN);
            String orgGroupDN = getAttributeAsString(positionAttributes, ATTR_POSITION_ORG_GROUP);
            String reportsToDN = getAttributeAsString(positionAttributes, ATTR_POSITION_REPORTS_TO);
            
            if (isNotNullOrEmpty(orgGroupDN)) {
                if (_orgGroupsWithDNasKey.containsKey(orgGroupDN)) {
                    currentPosition.setOrgGroup(_orgGroupsWithDNasKey.get(orgGroupDN));
                } else {
                    _log.error("The orggroup '" + orgGroupDN + "' for position " 
                        + positionNode.getKey() + " could not be found.");
                }
            }
            
            if (isNotNullOrEmpty(reportsToDN)) {
                if (_positionsWithDNasKey.containsKey(reportsToDN)) {
                    currentPosition.setReportsTo(_positionsWithDNasKey.get(reportsToDN));
                } else {
                    _log.error("The position '" + reportsToDN + "' for position "
                            + positionNode.getKey() + " could not be found or is not a position.");
                }
            }
        }
        
        // Role belongsTo
        for (Map.Entry<String, Role> roleNode : _rolesWithDNasKey.entrySet()) {
            LdapName roleDN = new LdapName(roleNode.getKey());
            String parent = roleDN.getPrefix(roleDN.size()-1).toString();
            
            if (_rolesWithDNasKey.containsKey(parent)) {
                Role parentRole = _rolesWithDNasKey.get(parent);
                roleNode.getValue().setOwnerRole(parentRole);
            }
        }
    }


    private Participant createParticipant(LdapName dn, Attributes attr) {
        // Must attributes
        String firstName = getAttributeAsString(attr, ATTR_GIVENNAME);
        String lastName = getAttributeAsString(attr, ATTR_SURNAME);
        String uid = getAttributeAsString(attr, ATTR_UID);
        
        // May attributes
        String description = getAttributeAsString(attr, ATTR_DESCRIPTION);
        String notes = getAttributeAsString(attr, ATTR_NOTES); 
        String yawlInternalId = getAttributeAsString(attr, ATTR_YAWL_INTERNAL_ID);
        String email = getAttributeAsString(attr, ATTR_MAIL);
        
        Boolean isAdministrator = getAttributeAsBoolean(attr, ATTR_PRIVILEGE_ADMINISTRATOR);
        Boolean canChooseItemToStart = getAttributeAsBoolean(attr, ATTR_PRIVILEGE_CAN_CHOOSE_ITEM_TO_START);
        Boolean canStartConcurrent = getAttributeAsBoolean(attr, ATTR_PRIVILEGE_CAN_START_CONCURRENT);
        Boolean canReorder = getAttributeAsBoolean(attr, ATTR_PRIVILEGE_CAN_REORDER);
        Boolean canViewTeamItems = getAttributeAsBoolean(attr, ATTR_PRIVILEGE_CAN_VIEW_TEAM_ITEMS);
        Boolean canViewOrgGroupItems = getAttributeAsBoolean(attr, ATTR_PRIVILEGE_CAN_VIEW_ORG_GROUP_ITEMS) ;
        Boolean canChainExecution = getAttributeAsBoolean(attr, ATTR_PRIVILEGE_CAN_CHAIN_EXECUTION);
        Boolean canManageCases = getAttributeAsBoolean(attr, ATTR_PRIVILEGE_CAN_MANAGE_CASES);
        
        Participant participant = new Participant(lastName, firstName, uid);
        if (isNotNullOrEmpty(yawlInternalId)) {
            participant.setID(yawlInternalId);
        } else {
            participant.setID(UUID.nameUUIDFromBytes(dn.toString().getBytes()).toString());
        }
        UserPrivileges userPrivileges = 
                new UserPrivileges(participant.getID(), canChooseItemToStart, canStartConcurrent, canReorder,
                false, false, false, canViewTeamItems, canViewOrgGroupItems, canChainExecution, canManageCases);
        participant.setUserPrivileges(userPrivileges);
        participant.setAdministrator(isAdministrator);

        participant.setEmail(email);
        participant.setDescription(description);
        participant.setNotes(notes);
        
        _uid2dnMap.put(uid, dn.toString());
        
        return participant;
    }
    
    private OrgGroup createOrgGroup(LdapName dn, Attributes attr) throws NamingException {
        // Must attributes
        String cn = getAttributeAsString(attr, ATTR_COMMON_NAME);
        String typeUnvalidated = getAttributeAsString(attr, ATTR_ORGGROUP_TYPE).trim().toUpperCase();
        OrgGroup.GroupType type = OrgGroup.GroupType.GROUP;
        if (ORG_GROUP_TYPES.contains(typeUnvalidated)) {
            type = OrgGroup.GroupType.valueOf(typeUnvalidated);
            _log.debug("Set type to " + type);
        } else {
            _log.error("Group type '" + typeUnvalidated + "' of group " + cn + " is not valid. Seting value to default 'GROUP'.");
        }

        // May attributes
        String description = getAttributeAsString(attr, ATTR_DESCRIPTION);
        String notes = getAttributeAsString(attr, ATTR_NOTES);
        String displayName = getAttributeAsString(attr, ATTR_DISPLAYNAME);
        String yawlInternalId = getAttributeAsString(attr, ATTR_YAWL_INTERNAL_ID);
        
        OrgGroup orgGroup = new OrgGroup(cn, type, null, description);
        if (isNotNullOrEmpty(yawlInternalId)) {
            orgGroup.setID(yawlInternalId);
        } else {
            orgGroup.setID(UUID.nameUUIDFromBytes(dn.toString().getBytes()).toString());
        }
        orgGroup.setNotes(notes);
        if (isNotNullOrEmpty(displayName)) {
            orgGroup.setLabel(displayName);
        }
        return orgGroup;
    }
    
    private Capability createCapability(LdapName dn, Attributes attr) throws NamingException {
        // Must attributes
        String cn = getAttributeAsString(attr, ATTR_COMMON_NAME);
        
        // May attributes
        String description = getAttributeAsString(attr, ATTR_DESCRIPTION);
        String notes = getAttributeAsString(attr, ATTR_NOTES);
        String displayName = getAttributeAsString(attr, ATTR_DISPLAYNAME);      
        String yawlInternalId = getAttributeAsString(attr, ATTR_YAWL_INTERNAL_ID);
        
        Capability capability = new Capability(cn, description);
        if (isNotNullOrEmpty(yawlInternalId)) {
            capability.setID(yawlInternalId);
        } else {
            capability.setID(UUID.nameUUIDFromBytes(dn.toString().getBytes()).toString());
        }
        capability.setNotes(notes);
        if (isNotNullOrEmpty(displayName)) {
            capability.setLabel(displayName);
        }
        return capability;
    }   

    private Position createPosition(LdapName dn, Attributes attr) throws NamingException {
        // Must attributes
        String cn = getAttributeAsString(attr, ATTR_COMMON_NAME);
        
        // May attributes
        String description = getAttributeAsString(attr, ATTR_DESCRIPTION);
        String notes = getAttributeAsString(attr, ATTR_NOTES);
        String displayName = getAttributeAsString(attr, ATTR_DISPLAYNAME);        
        String yawlInternalId = getAttributeAsString(attr, ATTR_YAWL_INTERNAL_ID);
        
        Position position = new Position(cn);
        if (isNotNullOrEmpty(yawlInternalId)) {
            position.setID(yawlInternalId);
        } else {
            position.setID(UUID.nameUUIDFromBytes(dn.toString().getBytes()).toString());
        }
        position.setDescription(description);
        position.setNotes(notes);
        if (isNotNullOrEmpty(displayName)) {
            position.setLabel(displayName);
        }
        return position;
    }
    
    private Role createRole(LdapName dn, Attributes attr) throws NamingException {
        // Must attributes
        String cn = getAttributeAsString(attr, ATTR_COMMON_NAME);
        
        // May attributes
        String description = getAttributeAsString(attr, ATTR_DESCRIPTION);
        String notes = getAttributeAsString(attr, ATTR_NOTES);
        String displayName = getAttributeAsString(attr, ATTR_DISPLAYNAME);
        String yawlInternalId = getAttributeAsString(attr, ATTR_YAWL_INTERNAL_ID);
        
        Role role = new Role(cn);
        role.setDescription(description);
        role.setNotes(notes);
        if (isNotNullOrEmpty(yawlInternalId)) {
            role.setID(yawlInternalId);
        } else {
            role.setID(UUID.nameUUIDFromBytes(dn.toString().getBytes()).toString());
        }
        if (isNotNullOrEmpty(displayName)) {
            role.setLabel(displayName);
        }
        return role;
    }
    
    private Boolean getAttributeAsBoolean(Attributes attr, String attributeName) {
        if (attr.get(attributeName) != null) {
            try {
                return Boolean.parseBoolean((String) attr.get(attributeName).get());
            } catch (NamingException ex) {
                _log.error("Error getting attribute " + attributeName, ex);
            }
        }
        return Boolean.FALSE;
    }
    
    private String getAttributeAsString(Attributes attr, String attributeName) {
        if (attr.get(attributeName) != null) {
            try {
                return (String) attr.get(attributeName).get();
            } catch (NamingException ex) {
                _log.error("Error getting attribute " + attributeName, ex);
            }
        }
        return "";
    } 
    
    private boolean isNotNullOrEmpty(String s) {
        return (s != null) && (s.length() > 0) ;
    }

    private String getProperty(String key) {
        String property = _props.getProperty(key);
        return property != null ? property : "";                 // ensure not null
    }    

    private DirContext getDirContext() {
        InitialDirContext ctx = null;
        try {
            ctx = new InitialDirContext(getEnvironment());
        } catch (NamingException ex) {
            _log.error("Could not create initial dir context.", ex);
        }
        return ctx;
    }
    
    private void loadProperties() {
        InputStream is = null;
        try {
            _props = new Properties();
            is = Thread.currentThread().getContextClassLoader()
                   .getResourceAsStream("LDAPSourceExtended.properties");
            if (is == null) throw new Exception("LDAPSourceExtended.properties not found.");
  
            _props.load(is);
            if (_log.isDebugEnabled()) {
                for (Map.Entry<Object,Object> entry:_props.entrySet()) {
                    _log.error(entry.getKey() + ":" + entry.getValue());
                }
            }
        } catch (Exception e) {
            _log.error("Exception thrown when loading LDAP properties.", e);
            _props = null;           // this will cause a controlled service disablement
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ex) {
                _log.error("Could not close properties file.", ex);
            }
        }
    }  
    
    /**
     * Gets the environment attribute values for server connections. The value for
     * each attribute is read from the properties file.
     * @return a populated map of environment values.
     */
    private Hashtable<String, Object> getEnvironment() {
        if (_environment == null) {
            _environment = new Hashtable<String, Object>();
            if (_props != null) {
                String scheme = "ldap";
                String protocol = getProperty("securityprotocol");
                if (isNotNullOrEmpty(protocol)) {
                    _environment.put(Context.SECURITY_PROTOCOL, protocol);
                    scheme += "s";
                }
                String url = String.format("%s://%s:%s", scheme, getProperty("host"),
                        getProperty("port"));

                _environment.put(Context.PROVIDER_URL, url);
                _environment.put(Context.INITIAL_CONTEXT_FACTORY, getProperty("contextfactory"));
                _environment.put(Context.SECURITY_AUTHENTICATION, getProperty("authentication"));
                _environment.put(Context.SECURITY_PRINCIPAL, getProperty("adminusername"));
                _environment.put(Context.SECURITY_CREDENTIALS, getProperty("adminpassword"));
                
                _log.debug("=== Environment ===");
                _log.debug(_environment.get(Context.PROVIDER_URL));
                _log.debug(_environment.get(Context.INITIAL_CONTEXT_FACTORY));
                _log.debug(_environment.get(Context.SECURITY_AUTHENTICATION));
                _log.debug(_environment.get(Context.SECURITY_PRINCIPAL));
                _log.debug(_environment.get(Context.SECURITY_CREDENTIALS));
            }
        }
        return _environment;
    } 
    
    @Override
    public ResourceDataSet loadResources() {
        _log.info("Updating org data from external LDAP server");
        initMaps();
        ResourceDataSet rds = new ResourceDataSet(this);
        if (_props != null) {
            try {
                fillInputMapFromLDAP(); // Load input map from LDAP
                firstPass(); // Create YAWL objects in maps 
                secondPass(); // Create relations between objects
                
                rds.setParticipants(_participantsWithIDasKey, this);
                if (!_capabilitiesWithIDasKey.isEmpty()) rds.setCapabilities(_capabilitiesWithIDasKey, this);
                if (!_orgGroupsWithIDasKey.isEmpty()) rds.setOrgGroups(_orgGroupsWithIDasKey, this);
                if (!_rolesWithIDasKey.isEmpty()) rds.setRoles(_rolesWithIDasKey, this);
                if (!_positionsWithIDasKey.isEmpty()) rds.setPositions(_positionsWithIDasKey, this);
                
                rds.setAllowExternalOrgDataMods(false);
                rds.setExternalUserAuthentication(true);
            } catch (NamingException ne) {
                _log.error(
                   "Naming Exception thrown when attempting to retrieve org data from LDAP.", ne);
            }
        } 
        return rds;
    }

    @Override
    public void update(Object obj) { }

    @Override
    public boolean delete(Object obj) { return false; }

    @Override
    public String insert(Object obj) { return null; }

    @Override
    public void importObj(Object obj) { }

    @Override
    public int execUpdate(String query) { return -1; }

    @Override
    public boolean authenticate(String userid, String password) throws
            YAuthenticationException {

        if (! _uid2dnMap.containsKey(userid)) {
            throw new YAuthenticationException("Unknown userid");
        }

        Hashtable<String,Object> env = new Hashtable<String, Object>(getEnvironment());
        String userBinding = _uid2dnMap.get(userid);
        System.out.println("get user " + userBinding);
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
    
    /* Output for debugging purposes
        private void printMaps() {
        for (Map.Entry<String, Participant> participantEntry : _participantsWithDNasKey.entrySet()) {
            printParticipant(participantEntry.getValue());
        }
        for (Map.Entry<String, OrgGroup> orgGroupEntry : _orgGroupsWithDNasKey.entrySet()) {
            printOrgGroup(orgGroupEntry.getValue());
        }
        for (Map.Entry<String, Role> roleEntry : _rolesWithDNasKey.entrySet()) {
            printRole(roleEntry.getValue());
        }
        for (Map.Entry<String, Capability> capabilityEntry : _capabilitiesWithDNasKey.entrySet()) {
            printCapability(capabilityEntry.getValue());
        }
        for (Map.Entry<String, Position> positionEntry : _positionsWithDNasKey.entrySet()) {
            printPosition(positionEntry.getValue());
        }
    }
        
    private void printParticipant(Participant participant) {
        System.out.println("");
        System.out.println("         Participant");
        System.out.println("                  ID " + participant.getID());
        System.out.println("                Name " + participant.getName());
        System.out.println("               Notes " + participant.getNotes());
        System.out.println("                Desc " + participant.getDescription());
        System.out.println("           Full Name " + participant.getFullName());
        System.out.println("          First Name " + participant.getFirstName());
        System.out.println("           Last Name " + participant.getLastName());
        System.out.println("             User ID " + participant.getUserID());
        System.out.println("            is Admin " + participant.isAdministrator());
        System.out.println("          privileges " + participant.getUserPrivileges().getPrivilegesAsBits());
    }
    
    private void printOrgGroup(OrgGroup orgGroup) {
        System.out.println("");
        System.out.println("           OrgGroup");
        printAbstractResource(orgGroup);
        System.out.println("         Group Name " + orgGroup.getGroupName());
        System.out.println("               Type " + orgGroup.getGroupType());
        System.out.println("          BelongsTo " + (orgGroup.getBelongsTo() != null ? orgGroup.getBelongsTo().getName() : ""));
    }

    private void printRole(Role role) {
        System.out.println("");
        System.out.println("               Role");
        printAbstractResource(role);
        System.out.println("          BelongsTo " + (role.getOwnerRole() != null ? role.getOwnerRole().getName() : ""));
        System.out.println("            Members " + (role.getResources()!= null ? role.getResources() : ""));
    }

    private void printCapability(Capability capability) {
        System.out.println("");
        System.out.println("          Capability");
        printAbstractResource(capability);
        System.out.println("            Members " + (capability.getResources()!= null ? capability.getResources() : ""));
    }

    private void printPosition(Position position) {
        System.out.println("");
        System.out.println("            Position");
        printAbstractResource(position);
        System.out.println("          BelongsTo " + (position.getOrgGroup() != null ? position.getOrgGroup().getName() : ""));
        System.out.println("          ReportsTo " + (position.getReportsTo() != null ? position.getReportsTo().getName() : ""));
        System.out.println("            Members " + (position.getResources()!= null ? position.getResources() : ""));
    }
    
    private void printAbstractResource(AbstractResourceAttribute asr) {
        System.out.println("                 ID " + asr.getID());
        System.out.println("               Name " + asr.getName());
        System.out.println("              Notes " + asr.getNotes());
        System.out.println("               Desc " + asr.getDescription());
    }
    */
}
