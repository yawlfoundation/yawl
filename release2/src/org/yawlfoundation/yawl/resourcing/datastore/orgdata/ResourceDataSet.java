package org.yawlfoundation.yawl.resourcing.datastore.orgdata;

import org.yawlfoundation.yawl.resourcing.resource.*;
import org.yawlfoundation.yawl.resourcing.jsf.comparator.ParticipantNameComparator;

import java.util.*;

/**
 * Author: Michael Adams
 * Creation Date: 4/11/2009 (extracted from ResourceManager class)
 */

public class ResourceDataSet {

    public enum ResUnit {Participant, Role, Capability, OrgGroup, Position}

    // Data maps for each of the five resource entities
    private HashMap<String, Participant> participantMap ;
    private HashMap<String, Role> roleMap ;
    private HashMap<String, Capability> capabilityMap;
    private HashMap<String, Position> positionMap;
    private HashMap<String, OrgGroup> orgGroupMap;

    // if true, overrides read-only setting of external data sources (set from web.xml)
    private boolean _allowExternalOrgDataMods = true;

    // maps the data source for each org data entity
    private Map<ResUnit, DataSource> sources = new Hashtable<ResUnit, DataSource>();


    public ResourceDataSet(DataSource source) {
        initSourcesTable(source);            
        participantMap = new HashMap<String,Participant>();
        roleMap = new HashMap<String,Role>();
        capabilityMap = new HashMap<String,Capability>();
        positionMap = new HashMap<String,Position>();
        orgGroupMap = new HashMap<String,OrgGroup>();
    }

    /*************************************************************************/

    // PRIVATE METHODS //

    private void initSourcesTable(DataSource source) {
        for (ResUnit unit : ResUnit.values()) {
            sources.put(unit, source);
        }
    }


    private synchronized void disconnectResources(AbstractResourceAttribute attrib) {
        Set<AbstractResource> resources = attrib.getResources();

        // get ids to avoid ConcurrentModificationException
        List<String> ids = new ArrayList<String>();
        for (AbstractResource resource : resources)
            ids.add(resource.getID());

        for (String id : ids) {
            Participant p = getParticipant(id);
            if (attrib instanceof Role) p.removeRole((Role) attrib);
            else if (attrib instanceof Capability) p.removeCapability((Capability) attrib);
            else if (attrib instanceof Position) p.removePosition((Position) attrib);
            updateParticipant(p);
        }
    }


    private ArrayList<Participant> sortFullParticipantListByName() {
        ArrayList<Participant> pList = new ArrayList<Participant>(participantMap.values());
        Collections.sort(pList, new ParticipantNameComparator());
        return pList ;
    }


    private String participantSetToXML(Set<Participant> pSet, String header) {
        StringBuilder xml = new StringBuilder(header) ;
        for (Participant p : pSet) xml.append(p.toXML()) ;
        xml.append("</participants>");
        return xml.toString() ;
    }


    private String fail(String msg) {
        return "<failure>" + msg + "</failure>";
    }


    /**************************************************************************/

    // PUBLIC METHODS //

    // By default, data loaded from external sources is read-only. This value, set from
    //web.xml on startup, allows that default to be overridden 
    public void setAllowExternalOrgDataMods(boolean allow) {
        _allowExternalOrgDataMods = allow;
    }
    

    public boolean isDataEditable(ResUnit resource) {
        return _allowExternalOrgDataMods || hasDefaultDataSource(resource) ;
    }

    public boolean isDataEditable(String resName) {
        return isDataEditable(getResUnit(resName));
    }

    public void setDataSource(ResUnit resource, DataSource source) {
        sources.put(resource, source);
    }

    public DataSource getDataSource(ResUnit resource) {
        return sources.get(resource);
    }

    // HibernateImpl is the default Resource Service Data Source
    public boolean hasDefaultDataSource(ResUnit resource) {
        return (getDataSource(resource) instanceof HibernateImpl);
    }

    public ResUnit getResUnit(String name) {
        return ResUnit.valueOf(name);
    }

    /************************************/

    public void setParticipants(HashMap<String, Participant> participants, DataSource source) {
        participantMap = participants;
        setDataSource(ResUnit.Participant, source);
    }

    public void setRoles(HashMap<String, Role> roles, DataSource source) {
        roleMap = roles;
        setDataSource(ResUnit.Role, source);
    }

    public void setCapabilities(HashMap<String, Capability> capabilities, DataSource source) {
        capabilityMap = capabilities;
        setDataSource(ResUnit.Capability, source);
    }

    public void setPositions(HashMap<String, Position> positions, DataSource source) {
        positionMap = positions;
        setDataSource(ResUnit.Position, source);
    }

    public void setOrgGroups(HashMap<String, OrgGroup> groups, DataSource source) {
        orgGroupMap = groups;
        setDataSource(ResUnit.OrgGroup, source);
    }


    public void augmentDataSourceAsRequired() {
        HibernateImpl defaultSource = new HibernateImpl();

        if (getRoles().isEmpty())
            setRoles(defaultSource.loadRoles(), defaultSource);

        if (getCapabilities().isEmpty())
            setCapabilities(defaultSource.loadCapabilities(), defaultSource);

        if (getOrgGroups().isEmpty())
            setOrgGroups(defaultSource.loadOrgGroups(), defaultSource);

        if (getPositions().isEmpty())
            setPositions(defaultSource.loadPositions(), defaultSource);
    }

    /************************************/

    public void putParticipant(Participant p) {
        participantMap.put(p.getID(), p);
    }

    public void putCapability(Capability c) {
        capabilityMap.put(c.getID(), c);
    }

    public void putRole(Role r) {
        roleMap.put(r.getID(), r);
    }

    public void putPosition(Position p) {
        positionMap.put(p.getID(), p);
    }

    public void putOrgGroup(OrgGroup o) {
        orgGroupMap.put(o.getID(), o) ;
    }

    /************************************/

    public void delParticipant(Participant p) {
        participantMap.remove(p.getID()) ;
    }

    public void delRole(Role r) {
        roleMap.remove(r.getID());
    }

    public void delCapability(Capability c) {
        capabilityMap.remove(c.getID());
    }

    public void delPosition(Position p) {
        positionMap.remove(p.getID());
    }

    public void delOrgGroup(OrgGroup o) {
        orgGroupMap.remove(o.getID());
    }

    /************************************/

    public String addParticipant(Participant p) {
        if (isDataEditable(ResUnit.Participant)) {
            String newID = getDataSource(ResUnit.Participant).insert(p) ;
            if (! hasDefaultDataSource(ResUnit.Participant)) p.setID(newID);
            putParticipant(p) ;                      // add it to the data set
            return newID;
        }
        else return fail("External Participant dataset is read-only");
    }

    public String addRole(Role r) {
        if (isDataEditable(ResUnit.Role)) {
            String newID = getDataSource(ResUnit.Role).insert(r) ;          // persist it
            if (! hasDefaultDataSource(ResUnit.Role)) r.setID(newID);
            putRole(r);                                  // ...and add it to the data set
            return newID;
        }
        else return fail("External Role dataset is read-only");
    }

    public String addCapability(Capability c) {
        if (isDataEditable(ResUnit.Capability)) {
            String newID = getDataSource(ResUnit.Capability).insert(c) ;    // persist it
            if (! hasDefaultDataSource(ResUnit.Capability)) c.setID(newID);
            putCapability(c) ;                           // ...and add it to the data set
            return newID;
        }
        else return fail("External Capability dataset is read-only");
    }

    public String addPosition(Position p) {
        if (isDataEditable(ResUnit.Position)) {
            String newID = getDataSource(ResUnit.Position).insert(p) ;      // persist it
            if (! hasDefaultDataSource(ResUnit.Position)) p.setID(newID);
            putPosition(p) ;                             // ...and add it to the data set
            return newID;
        }
        else return fail("External Position dataset is read-only");
    }

    public String addOrgGroup(OrgGroup o) {
        if (isDataEditable(ResUnit.OrgGroup)) {
            String newID = getDataSource(ResUnit.OrgGroup).insert(o) ;      // persist it
            if (! hasDefaultDataSource(ResUnit.OrgGroup)) o.setID(newID);
            putOrgGroup(o) ;                 // ...and add it to the data set
            return newID;
        }
        else return fail("External OrgGroup dataset is read-only");
    }

    /************************************/

    public boolean importParticipant(Participant p) {
        boolean editable = isDataEditable(ResUnit.Participant);
        if (editable) {
             getDataSource(ResUnit.Participant).importObj(p) ;
             putParticipant(p) ;
        }
        return editable;
    }

    public void importRole(Role r) {
        if (isDataEditable(ResUnit.Role)) {
            getDataSource(ResUnit.Role).importObj(r) ;
            putRole(r) ;
        }
    }

    public void importCapability(Capability c) {
        if (isDataEditable(ResUnit.Capability)) {
            getDataSource(ResUnit.Capability).importObj(c) ;
            putCapability(c) ;
        }
    }

    public void importPosition(Position p) {
        if (isDataEditable(ResUnit.Position)) {
            getDataSource(ResUnit.Position).importObj(p) ;
            putPosition(p) ;
        }
    }

    public void importOrgGroup(OrgGroup o) {
        if (isDataEditable(ResUnit.OrgGroup)) {
            getDataSource(ResUnit.OrgGroup).importObj(o) ;
            putOrgGroup(o) ;
        }
    }

    /************************************/

    public boolean updateParticipant(Participant p) {
        boolean editable = isDataEditable(ResUnit.Participant);
        if (editable) {
            getDataSource(ResUnit.Participant).update(p);                  // persist it
            putParticipant(p) ;                           // ... and update the data set
        }
        return editable;
    }

    public void updateResourceAttribute(Object obj) {
        if (obj instanceof Role) updateRole((Role) obj);
        else if (obj instanceof Capability) updateCapability((Capability) obj);
        else if (obj instanceof Position) updatePosition((Position) obj);
        else if (obj instanceof OrgGroup) updateOrgGroup((OrgGroup) obj);
    }

    public void updateRole(Role r) {
        if (isDataEditable(ResUnit.Role)) {
            getDataSource(ResUnit.Role).update(r) ;                        // persist it
            putRole(r) ;                                  // ... and update the data set
        }
    }

    public void updateCapability(Capability c) {
        if (isDataEditable(ResUnit.Capability)) {
            getDataSource(ResUnit.Capability).update(c) ;                  // persist it
            putCapability(c) ;                            // ... and update the data set
        }
    }

    public void updatePosition(Position p) {
        if (isDataEditable(ResUnit.Position)) {
            getDataSource(ResUnit.Position).update(p) ;                   // persist it
            putPosition(p) ;                             // ... and update the data set
        }
    }

    public void updateOrgGroup(OrgGroup o) {
        if (isDataEditable(ResUnit.OrgGroup)) {
            getDataSource(ResUnit.OrgGroup).update(o) ;                    // persist it
            putOrgGroup(o) ;                              // ... and update the data set
        }
    }

    /************************************/

    public synchronized boolean removeParticipant(Participant p) {
        boolean editable = isDataEditable(ResUnit.Participant);
        if (editable) {
            p.removeAttributeReferences() ;
            getDataSource(ResUnit.Participant).delete(p);
            delParticipant(p) ;
        }
        return editable;
    }

    public synchronized void removeRole(Role r) {
        if (isDataEditable(ResUnit.Role)) {
            disconnectResources(r);
            for (Role role : getRoles()) {
                Role owner = role.getOwnerRole() ;
                if ((owner != null) && owner.getID().equals(r.getID())) {
                    role.setOwnerRole((Role) null);
                    getDataSource(ResUnit.Role).update(role);
                }
            }
            delRole(r);
            getDataSource(ResUnit.Role).delete(r);
        }
    }

    public synchronized void removeCapability(Capability c) {
        if (isDataEditable(ResUnit.Capability)) {
            disconnectResources(c);
            delCapability(c);
            getDataSource(ResUnit.Capability).delete(c);
        }
    }

    public synchronized void removePosition(Position p) {
        if (isDataEditable(ResUnit.Position)) {
            disconnectResources(p);
            for (Position position : getPositions()) {
                Position boss = position.getReportsTo();
                if ((boss != null) && boss.getID().equals(p.getID())) {
                    position.setReportsTo((Position) null);
                    getDataSource(ResUnit.Position).update(position);
                }
            }
            delPosition(p);
            getDataSource(ResUnit.Position).delete(p);
        }
    }

    public synchronized void removeOrgGroup(OrgGroup o) {
        if (isDataEditable(ResUnit.OrgGroup)) {
            for (Position position : getPositions()) {
                OrgGroup group = position.getOrgGroup();
                if ((group != null) && group.getID().equals(o.getID())) {
                    position.setOrgGroup((OrgGroup) null);
                    getDataSource(ResUnit.Position).update(position);
                }
            }

            for (OrgGroup group : getOrgGroups()) {
                OrgGroup owner = group.getBelongsTo();
                if ((owner != null) && owner.getID().equals(o.getID())) {
                    group.setBelongsTo((OrgGroup) null);
                    getDataSource(ResUnit.OrgGroup).update(group);
                }
            }
            delOrgGroup(o);
            getDataSource(ResUnit.OrgGroup).delete(o);
        }
    }

    public boolean removeRole(String rid) {
        if (rid != null) {
            Role role = getRole(rid);
            if (role != null) {
                removeRole(role);
                return true;
            }
        }
        return false;
    }

    public boolean removeCapability(String cid) {
        if (cid != null) {
            Capability capability = getCapability(cid);
            if (capability != null) {
                removeCapability(capability);
                return true;
            }
        }
        return false;
    }

    public boolean removePosition(String pid) {
        if (pid != null) {
            Position position = getPosition(pid);
            if (position != null) {
                removePosition(position);
                return true;
            }
        }
        return false;
    }

    public boolean removeOrgGroup(String oid) {
        if (oid != null) {
            OrgGroup orgGroup = getOrgGroup(oid);
            if (orgGroup != null) {
                removeOrgGroup(orgGroup);
                return true;
            }
        }
        return false;
    }

    /************************************/

    public Participant getParticipant(String pid) {
       return participantMap.get(pid) ;
    }

    public Role getRole(String rid) {
        return roleMap.get(rid);
    }

    public Capability getCapability(String cid) {
        return capabilityMap.get(cid);
    }

    public Position getPosition(String pid) {
        return positionMap.get(pid);
    }

    public OrgGroup getOrgGroup(String oid) {
        return orgGroupMap.get(oid);
    }

    public HashSet<Participant> getParticipants() {
        return new HashSet<Participant>(participantMap.values()) ;
    }

    public HashSet<Role> getRoles() {
        return new HashSet<Role>(roleMap.values()) ;
    }

    public HashSet<Position> getPositions() {
        return new HashSet<Position>(positionMap.values()) ;
    }

    public HashSet<Capability> getCapabilities() {
        return new HashSet<Capability>(capabilityMap.values()) ;
    }

    public HashSet<OrgGroup> getOrgGroups() {
        return new HashSet<OrgGroup>(orgGroupMap.values()) ;
    }

    public HashMap<String, Participant> getParticipantMap() {
        return participantMap ;
    }

    public HashMap<String, Role> getRoleMap() {
        return roleMap ;
    }

    public HashMap<String, Position> getPositionMap() {
        return positionMap ;
    }

    public HashMap<String, Capability> getCapabilityMap() {
        return capabilityMap ;
    }

    public HashMap<String, OrgGroup> getOrgGroupMap() {
        return orgGroupMap ;
    }

    public int getParticipantCount() {
        return participantMap.size();
    }


    public Role getRoleByName(String roleName) {
        for (Role r : roleMap.values()) {
            if (r.getName().equalsIgnoreCase(roleName))
                return r ;
        }
        return null ;                    // no match
    }

    public Position getPositionByLabel(String label) {
        for (Position p : positionMap.values()) {
            if (p.getTitle().equals(label)) {
                return p;
            }
        }
        return null;
    }

    public OrgGroup getOrgGroupByLabel(String label) {
        for (OrgGroup o : orgGroupMap.values()) {
            if (o.getGroupName().equals(label)) {
                return o;
            }
        }
        return null;
    }

    public Capability getCapabilityByLabel(String label) {
        for (Capability c : capabilityMap.values()) {
            if (c.getCapability().equals(label)) {
                return c;
            }
        }
        return null;
    }


    public boolean isKnownRoleName(String name) {
        return getRoleByName(name) != null;
    }

    public boolean isKnownCapabilityName(String name) {
        return getCapabilityByLabel(name) != null;
    }

    public boolean isKnownPositionName(String name) {
        return getPositionByLabel(name) != null;
    }

    public boolean isKnownOrgGroupName(String name) {
        return this.getOrgGroupByLabel(name) != null;
    }

    public boolean isKnownParticipant(Participant p) {
        return isKnownParticipant(p.getID());
    }

    public boolean isKnownParticipant(String pid) {
        return participantMap.containsKey(pid);
    }

    public boolean isKnownRole(Role r) {
        return isKnownRole(r.getID());
    }

    public boolean isKnownRole(String rid) {
        return roleMap.containsKey(rid);
    }

    public boolean isKnownCapability(String cid) {
        return capabilityMap.containsKey(cid);
    }

    public boolean isKnownPosition(String pid) {
        return positionMap.containsKey(pid);
    }

    public boolean isKnownOrgGroup(String oid) {
        return orgGroupMap.containsKey(oid);
    }

    // @return a csv listing of the full name of each participant
    public String getParticipantNames() {
        ArrayList<Participant> pList = sortFullParticipantListByName();
        StringBuilder csvList = new StringBuilder() ;
        for  (Participant p : pList) {
            if (csvList.length() > 0) csvList.append(",");
            csvList.append(p.getFullName()) ;
        }
        return csvList.toString();
    }


    // @return a csv listing of the full name of each role
    public String getRoleNames() {
        StringBuilder csvList = new StringBuilder() ;
        for (Role r : roleMap.values()) {
            if (csvList.length() > 0) csvList.append(",");
            csvList.append(r.getName()) ;
        }
        return csvList.toString();
    }

    public Set<Role> getParticipantRoles(String pid) {
        Participant p = participantMap.get(pid);
        return (p != null) ? p.getRoles() : null ;
    }


    public Set<Position> getParticipantPositions(String pid) {
        Participant p = participantMap.get(pid);
        return (p != null) ? p.getPositions() : null ;
    }


    public Set<Capability> getParticipantCapabilities(String pid) {
        Participant p = participantMap.get(pid);
        return (p != null) ? p.getCapabilities() : null ;
    }


    public Set<Participant> getRoleParticipants(String rid) {
        Role r = roleMap.get(rid);
        return (r != null) ? castToParticipantSet(r.getResources()) : null ;
    }


    public Set<Participant> getCapabilityParticipants(String cid) {
        Capability c = capabilityMap.get(cid);
        return (c != null) ? castToParticipantSet(c.getResources()) : null ;
    }


    public Set<Participant> getPositionParticipants(String pid) {
        Position p = positionMap.get(pid);
        return (p != null) ? castToParticipantSet(p.getResources()) : null ;
    }


    public Set<Participant> getParticipantsWithRole(String roleName) {
        Set<Participant> result = null;
        if (roleName != null) {
            Role r = getRoleByName(roleName);
            if (r != null) {
                result = getRoleParticipants(r.getID());
            }
        }
        return result;
    }


    public Set<Participant> getParticipantsWithPosition(String positionName) {
        Set<Participant> result = null;
        if (positionName != null) {
            Position p = getPositionByLabel(positionName);
            if (p != null) {
                result = getPositionParticipants(p.getID());
            }
        }
        return result;
    }

    public Set<Participant> getParticipantsWithCapability(String capabilityName) {
        Set<Participant> result = null;
        if (capabilityName != null) {
            Capability c = getCapabilityByLabel(capabilityName);
            if (c != null) {
                result = getCapabilityParticipants(c.getID());
            }
        }
        return result;
    }


    public Set<Participant> getOrgGroupMembers(OrgGroup o) {
        Set<Participant> result = new HashSet<Participant>();
        for (Participant p : participantMap.values()) {
            if (p.isOrgGroupMember(o)) result.add(p);
        }
        return result;
    }


    /**
     * Gets the complete set of Participants that ultimately report to the
     * position(s) held by a Participant
     * @param pid the id of the 'manager' Participant
     * @return the set of Particpants 'managed' by this Participant
     */
    public Set<Participant> getParticipantsReportingTo(String pid) {
        Set<Participant> result = new HashSet<Participant>() ;
        Set<Position> posSet = getParticipantPositions(pid) ;
        for (Position pos : posSet) {
            result.addAll(getParticipantsReportingToPosition(pos)) ;
        }
        if (result.isEmpty()) result = null ;
        return result ;
    }


    /**
     * Gets the set of Participants the ultimately report to the Position passed
     * @param manager the 'manager' Position
     * @return the set of Particpants 'managed' by this Position
     */
    public Set<Participant> getParticipantsReportingToPosition(Position manager) {
        Set<Participant> result = new HashSet<Participant>() ;
        Set<Position> posSet = getPositions();
        for (Position pos : posSet) {
            if (pos.ultimatelyReportsTo(manager)) {
                result.addAll(castToParticipantSet(pos.getResources()));
            }
        }
        return result ;
    }


    public Set<Participant> getParticipantsInDescendantRoles(Role owner) {
        Set<Participant> result = new HashSet<Participant>();
        Set<Role> roleSet = getRoles();
        for (Role role : roleSet) {
            if (role.ultimatelyBelongsTo(owner)) {
                result.addAll(castToParticipantSet(role.getResources()));
            }
        }
        return result;
    }


    public String getParticipantsAsXML() {
        ArrayList<Participant> pList = sortFullParticipantListByName();

        StringBuilder xml = new StringBuilder("<participants>") ;
        for (Participant p : pList) xml.append(p.toXML()) ;
        xml.append("</participants>");
        return xml.toString() ;
    }


    public String getRolesAsXML() {
        ArrayList<Role> rList = new ArrayList<Role>(roleMap.values());
        Collections.sort(rList);

        StringBuilder xml = new StringBuilder("<roles>") ;
        for (Role r : rList) xml.append(r.toXML()) ;
        xml.append("</roles>");
        return xml.toString() ;
    }


    public String getCapabilitiesAsXML() {
        ArrayList<Capability> cList = new ArrayList<Capability>(capabilityMap.values());
        Collections.sort(cList);

        StringBuilder xml = new StringBuilder("<capabilities>") ;
        for (Capability c : cList) xml.append(c.toXML()) ;
        xml.append("</capabilities>");
        return xml.toString() ;
    }

    public String getPositionsAsXML() {
        ArrayList<Position> pList = new ArrayList<Position>(positionMap.values());
        Collections.sort(pList);

        StringBuilder xml = new StringBuilder("<positions>") ;
        for (Position p : pList) xml.append(p.toXML()) ;
        xml.append("</positions>");
        return xml.toString() ;
    }

    public String getOrgGroupsAsXML() {
        ArrayList<OrgGroup> oList = new ArrayList<OrgGroup>(orgGroupMap.values());
        Collections.sort(oList);

        StringBuilder xml = new StringBuilder("<orggroups>") ;
        for (OrgGroup o : oList) xml.append(o.toXML()) ;
        xml.append("</orggroups>");
        return xml.toString() ;
    }


    public String getParticipantRolesAsXML(String pid) {
        Set<Role> roles = getParticipantRoles(pid);
        if (roles != null) {
            String header = String.format("<roles participantid=\"%s\">", pid);
            StringBuilder xml = new StringBuilder(header) ;
            for (Role r : roles) xml.append(r.toXML()) ;
            xml.append("</roles>");
            return xml.toString() ;
        }
        else return("<roles/>") ;
    }


    public String getParticipantPositionsAsXML(String pid) {
        Set<Position> posSet = getParticipantPositions(pid);
        if (posSet != null) {
            String header = String.format("<positions participantid=\"%s\">", pid);
            StringBuilder xml = new StringBuilder(header) ;
            for (Position p : posSet) xml.append(p.toXML()) ;
            xml.append("</positions>");
            return xml.toString() ;
        }
        else return("<positions/>") ;
    }


    public String getParticipantCapabilitiesAsXML(String pid) {
        Set<Capability> capSet = getParticipantCapabilities(pid);
        if (capSet != null) {
            String header = String.format("<capabilities participantid=\"%s\">", pid);
            StringBuilder xml = new StringBuilder(header) ;
            for (Capability c : capSet) xml.append(c.toXML()) ;
            xml.append("</capabilities>");
            return xml.toString() ;
        }
        else return("<capabilities/>") ;
    }


    public String getParticpantsWithRoleAsXML(String roleName) {
        String result = "<participants/>";
        if (roleName != null) {
            Role r = getRoleByName(roleName);
            if (r != null) {
                result = getRoleParticipantsAsXML(r.getID());
            }
        }
        return result;
    }


    public String getRoleParticipantsAsXML(String rid) {
        Set<Participant> pSet = getRoleParticipants(rid);
        if (pSet != null) {
            String header = String.format("<participants roleid=\"%s\">", rid);
            return participantSetToXML(pSet, header);
        }
        else return "<participants/>" ;
    }


    public String getParticpantsWithPositionAsXML(String positionName) {
        String result = "<participants/>";
        if (positionName != null) {
            Position p = getPositionByLabel(positionName);
            if (p != null) {
                result = getPositionParticipantsAsXML(p.getID());
            }
        }
        return result;
    }


    public String getPositionParticipantsAsXML(String posid) {
        Set<Participant> pSet = getPositionParticipants(posid);
        if (pSet != null) {
            String header = String.format("<participants positionid=\"%s\">", posid);
            return participantSetToXML(pSet, header);
        }
        else return "<participants/>" ;
    }


    public String getParticpantsWithCapabilityAsXML(String capabilityName) {
        String result = "<participants/>";
        if (capabilityName != null) {
            Capability c = getCapabilityByLabel(capabilityName);
            if (c != null) {
                result = getCapabilityParticipantsAsXML(c.getID());
            }
        }
        return result;
    }


    public String getCapabilityParticipantsAsXML(String cid) {
        Set<Participant> pSet = getCapabilityParticipants(cid);
        if (pSet != null) {
            String header = String.format("<participants positionid=\"%s\">", cid);
            return participantSetToXML(pSet, header);
        }
        else return "<participants/>" ;
    }

    
    public Set<Participant> castToParticipantSet(Set<AbstractResource> resources) {
        Set<Participant> result = null;
        if (resources != null) {
            result = new HashSet<Participant>();
            for (AbstractResource resource : resources)
                result.add((Participant) resource);
        }
        return result;
    }

    /***************************************/

    public String checkCyclicAttributeReference(AbstractResourceAttribute resource,
                                                String parentID) {
        if (resource instanceof Role)
            return checkCyclicRoleReference((Role) resource, parentID);
        else if (resource instanceof Position)
            return checkCyclicPositionReference((Position) resource, parentID);
        else if (resource instanceof OrgGroup)
            return checkCyclicOrgGroupReference((OrgGroup) resource, parentID);

        return null;       // should be unreachable
    }


    public String checkCyclicRoleReference(Role role, String refID) {
        String result = null;
        List<String> hierarchy = new ArrayList<String>();
        hierarchy.add(role.getName());
        Role owner = getRole(refID);
        String refName = owner.getName();            // name of role attempting to add to
        while (owner != null) {
            hierarchy.add(owner.getName());
            if (owner.equals(role)) {
                result = constructCyclicAttributeErrorMessage(hierarchy, "role", refName);
                break;
            }
            owner = owner.getOwnerRole();
        }
        return result;
    }


    public String checkCyclicPositionReference(Position position, String refID) {
        String result = null;
        List<String> hierarchy = new ArrayList<String>();
        hierarchy.add(position.getTitle());
        Position owner = getPosition(refID);
        String refName = owner.getTitle();          // title of posn attempting to add to
        while (owner != null) {
            hierarchy.add(owner.getTitle());
            if (owner.equals(position)) {
                result = constructCyclicAttributeErrorMessage(hierarchy, "position", refName);
                break;
            }
            owner = owner.getReportsTo();
        }
        return result;
    }


    public String checkCyclicOrgGroupReference(OrgGroup orgGroup, String refID) {
        String result = null;
        List<String> hierarchy = new ArrayList<String>();
        hierarchy.add(orgGroup.getGroupName());
        OrgGroup owner = getOrgGroup(refID);
        String refName = owner.getGroupName();     // name of group attempting to add to
        while (owner != null) {
            hierarchy.add(owner.getGroupName());
            if (owner.equals(orgGroup)) {
                result = constructCyclicAttributeErrorMessage(hierarchy, "org group", refName);
                break;
            }
            owner = owner.getBelongsTo();
        }
        return result;
    }


    private String constructCyclicAttributeErrorMessage(List<String> chain, String type,
                                                        String refName) {
        String templateMsg = "Cyclic Reference Error: The selected %s cannot %s to %s " +
                             "'%s' because it references itself in the hierarchy '%s'." ;
        String refType = (type.equals("position")) ? "report" : "belong";

        StringBuilder chainStr = new StringBuilder(chain.get(0));
        for (int i=1; i<chain.size(); i++) {
            chainStr.append(" --> ").append(chain.get(i));
        }
        return String.format(templateMsg, type, refType, type, refName, chainStr.toString());
    }
    
}
