/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

import org.yawlfoundation.yawl.resourcing.datastore.eventlog.EventLogger;
import org.yawlfoundation.yawl.resourcing.jsf.comparator.ParticipantNameComparator;
import org.yawlfoundation.yawl.resourcing.resource.*;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanCategory;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanSubCategory;
import org.yawlfoundation.yawl.util.XNode;

import java.util.*;

/**
 * Handles the loading, mapping and runtime management of resources from data source.
 *
 * @author Michael Adams
 * @date 4/11/2009 (orginally extracted from ResourceManager class)
 */

public class ResourceDataSet {

    public enum ResUnit { Participant, Role, Capability, OrgGroup,
                          Position, NonHumanResource, NonHumanCategory }

    public enum Identifier { FullName, ReverseFullName, LastName, Userid }

    // Data maps [id, object] for each of the seven resource entities
    private HashMap<String, Participant> participantMap ;
    private HashMap<String, Role> roleMap ;
    private HashMap<String, Capability> capabilityMap;
    private HashMap<String, Position> positionMap;
    private HashMap<String, OrgGroup> orgGroupMap;
    private HashMap<String, NonHumanResource> nonHumanMap;
    private HashMap<String, NonHumanCategory> nonHumanCategoryMap;

    // if true, overrides read-only setting of external data sources (set from web.xml)
    private boolean _allowExternalOrgDataMods = true;

    // if true, delegates user authentication to external data source (set from web.xml)
    private boolean _externalUserAuthentication = false;

    // maps the data source for each org data entity
    private Map<ResUnit, DataSource> _sources = new Hashtable<ResUnit, DataSource>();

    // stores a timestamp of each entity's last change
    private Map<ResUnit, Long> _changeStamp = new Hashtable<ResUnit, Long>();


    public ResourceDataSet(DataSource source) {
        initUnitMaps(source);
        participantMap = new HashMap<String, Participant>();
        roleMap = new HashMap<String, Role>();
        capabilityMap = new HashMap<String, Capability>();
        positionMap = new HashMap<String, Position>();
        orgGroupMap = new HashMap<String, OrgGroup>();
        nonHumanMap = new HashMap<String, NonHumanResource>();
        nonHumanCategoryMap = new HashMap<String, NonHumanCategory>();
    }

    /*************************************************************************/

    // PRIVATE METHODS //

    private void initUnitMaps(DataSource source) {
        long now = System.currentTimeMillis();
        for (ResUnit unit : ResUnit.values()) {
            _sources.put(unit, source);
            _changeStamp.put(unit, now);
        }
    }


    private void setChangeStamp(ResUnit unit) {
        _changeStamp.put(unit, System.currentTimeMillis());        
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
    // web.xml on startup, allows that default to be overridden 
    public void setAllowExternalOrgDataMods(boolean allow) {
        _allowExternalOrgDataMods = allow;
    }

    public boolean isDataEditable(ResUnit resource) {
        return _allowExternalOrgDataMods || hasDefaultDataSource(resource) ;
    }

    public boolean isDataEditable(String resName) {
        return isDataEditable(getResUnit(resName));
    }


    // This value is set from web.xml, and if true allows the delegation of user
    // authentication to a currently active external data source
    public void setExternalUserAuthentication(boolean externalAuth) {
        _externalUserAuthentication = externalAuth;
    }

    public boolean isUserAuthenticationExternal() {
        return _externalUserAuthentication && ! hasDefaultDataSource(ResUnit.Participant) ;
    }


    public void setDataSource(ResUnit resource, DataSource source) {
        _sources.put(resource, source);
    }

    public DataSource getDataSource(ResUnit resource) {
        return _sources.get(resource);
    }

    // HibernateImpl is the default Resource Service Data Source
    public boolean hasDefaultDataSource(ResUnit resource) {
        return (getDataSource(resource) instanceof HibernateImpl);
    }

    public ResUnit getResUnit(String name) {
        return ResUnit.valueOf(name);
    }


    public long getChangeStamp(ResUnit unit) {
        return _changeStamp.get(unit);
    }

    public long getLastChangeStamp() {
        long lastChange = 0;
        for (Long stamp : _changeStamp.values()) {
            if (stamp > lastChange) lastChange = stamp;
        }
        return lastChange;
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

    public void setNonHumanResources(HashMap<String, NonHumanResource> resources,
                                     DataSource source) {
        nonHumanMap = resources;
        setDataSource(ResUnit.NonHumanResource, source);
    }

    public void setNonHumanCategories(HashMap<String, NonHumanCategory> resources,
                                      DataSource source) {
        nonHumanCategoryMap = resources;
        setDataSource(ResUnit.NonHumanCategory, source);
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

        if (getNonHumanResources().isEmpty())
            setNonHumanResources(defaultSource.loadNonHumanResources(), defaultSource);

        if (getNonHumanCategories().isEmpty())
            setNonHumanCategories(defaultSource.loadNonHumanCategories(), defaultSource);
    }

    /************************************/

    public void putParticipant(Participant p) {
        participantMap.put(p.getID(), p);
        setChangeStamp(ResUnit.Participant);
    }

    public void putCapability(Capability c) {
        capabilityMap.put(c.getID(), c);
        setChangeStamp(ResUnit.Capability);
    }

    public void putRole(Role r) {
        roleMap.put(r.getID(), r);
        setChangeStamp(ResUnit.Role);
    }

    public void putPosition(Position p) {
        positionMap.put(p.getID(), p);
        setChangeStamp(ResUnit.Position);
    }

    public void putOrgGroup(OrgGroup o) {
        orgGroupMap.put(o.getID(), o);
        setChangeStamp(ResUnit.OrgGroup);
    }

    public void putNonHumanResource(NonHumanResource r) {
        nonHumanMap.put(r.getID(), r);
        setChangeStamp(ResUnit.NonHumanResource);
    }

    public void putNonHumanCategory(NonHumanCategory r) {
        nonHumanCategoryMap.put(r.getID(), r);
        setChangeStamp(ResUnit.NonHumanCategory);
    }

    /************************************/

    public void delParticipant(Participant p) {
        participantMap.remove(p.getID());
        setChangeStamp(ResUnit.Participant);
    }

    public void delRole(Role r) {
        roleMap.remove(r.getID());
        setChangeStamp(ResUnit.Role);
    }

    public void delCapability(Capability c) {
        capabilityMap.remove(c.getID());
        setChangeStamp(ResUnit.Capability);
    }

    public void delPosition(Position p) {
        positionMap.remove(p.getID());
        setChangeStamp(ResUnit.Position);
    }

    public void delOrgGroup(OrgGroup o) {
        orgGroupMap.remove(o.getID());
        setChangeStamp(ResUnit.OrgGroup);
    }

    public void delNonHumanResource(NonHumanResource r) {
        nonHumanMap.remove(r.getID());
        setChangeStamp(ResUnit.NonHumanResource);
    }

    public void delNonHumanCategory(NonHumanCategory r) {
        nonHumanCategoryMap.remove(r.getID());
        setChangeStamp(ResUnit.NonHumanCategory);
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

    public String addNonHumanResource(NonHumanResource r) {
        if (isDataEditable(ResUnit.NonHumanResource)) {
            String newID = getDataSource(ResUnit.NonHumanResource).insert(r) ; // persist it
            if (! hasDefaultDataSource(ResUnit.NonHumanResource)) r.setID(newID);
            putNonHumanResource(r) ;                 // ...and add it to the data set
            return newID;
        }
        else return fail("External NonHumanResource dataset is read-only");
    }

    public String addNonHumanCategory(NonHumanCategory c) {
        if (isDataEditable(ResUnit.NonHumanCategory)) {
            String newID = getDataSource(ResUnit.NonHumanCategory).insert(c) ;
            if (! hasDefaultDataSource(ResUnit.NonHumanCategory)) c.setID(newID);
            putNonHumanCategory(c) ;
            return newID;
        }
        else return fail("External NonHumanCategory dataset is read-only");
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

    public void importNonHumanResource(NonHumanResource r) {
        if (isDataEditable(ResUnit.NonHumanResource)) {
            getDataSource(ResUnit.NonHumanResource).importObj(r) ;
            putNonHumanResource(r) ;
        }
    }

    public void importNonHumanCategory(NonHumanCategory r) {
        if (isDataEditable(ResUnit.NonHumanCategory)) {
            getDataSource(ResUnit.NonHumanCategory).importObj(r) ;
            putNonHumanCategory(r) ;
        }
    }

    /************************************/

    public void updateResource(AbstractResource r) {
        if (r instanceof Participant) updateParticipant((Participant) r);
        else updateNonHumanResource((NonHumanResource) r);
    }


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

    public void updateNonHumanResource(NonHumanResource r) {
        if (isDataEditable(ResUnit.NonHumanResource)) {
            getDataSource(ResUnit.NonHumanResource).update(r) ;            // persist it
            putNonHumanResource(r) ;                      // ... and update the data set
        }
    }

    public void updateNonHumanCategory(NonHumanCategory r) {
        if (isDataEditable(ResUnit.NonHumanCategory)) {
            getDataSource(ResUnit.NonHumanCategory).update(r) ;    // persist it
            putNonHumanCategory(r) ;              // ... and update the data set
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

    public synchronized void removeNonHumanResource(NonHumanResource r) {
        if (isDataEditable(ResUnit.NonHumanResource)) {
            r.detachSubCategory();
            if (getDataSource(ResUnit.NonHumanResource).delete(r)) {
                delNonHumanResource(r);
            }    
        }
    }

    public synchronized void removeNonHumanCategory(NonHumanCategory r) {
        if (isDataEditable(ResUnit.NonHumanCategory)) {
            if (getDataSource(ResUnit.NonHumanCategory).delete(r)) {
                delNonHumanCategory(r);
            }    
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

    public boolean removeNonHumanResource(String rid) {
        if (rid != null) {
            NonHumanResource resource = getNonHumanResource(rid);
            if (resource != null) {
                removeNonHumanResource(resource);
                return true;
            }
        }
        return false;
    }

    public boolean removeNonHumanCategory(String rid) {
        if (rid != null) {
            NonHumanCategory category = getNonHumanCategory(rid);
            if (category != null) {
                removeNonHumanCategory(category);
                return true;
            }
        }
        return false;
    }


    /************************************/

    public AbstractResource getResource(String id) {
        AbstractResource resource = getParticipant(id);
        if (resource == null) {
            resource = getNonHumanResource(id);   // may also be null - means unknown id
        }
        return resource;
    }

    public Participant getParticipant(String pid) {
        return (pid != null) ? participantMap.get(pid) : null ;
    }

    public Role getRole(String rid) {
        return (rid != null) ? roleMap.get(rid) : null;
    }

    public Capability getCapability(String cid) {
        return (cid != null) ? capabilityMap.get(cid) : null;
    }

    public Position getPosition(String pid) {
        return (pid != null) ? positionMap.get(pid) : null;
    }

    public OrgGroup getOrgGroup(String oid) {
        return (oid != null) ? orgGroupMap.get(oid) : null;
    }

    public NonHumanResource getNonHumanResource(String rid) {
        return (rid != null) ? nonHumanMap.get(rid) : null;
    }

    public NonHumanCategory getNonHumanCategory(String cid) {
        return (cid != null) ? nonHumanCategoryMap.get(cid) : null;
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

    public HashSet<NonHumanResource> getNonHumanResources() {
        return new HashSet<NonHumanResource>(nonHumanMap.values()) ;
    }

    public HashSet<NonHumanCategory> getNonHumanCategories() {
        return new HashSet<NonHumanCategory>(nonHumanCategoryMap.values()) ;
    }

    public Set<NonHumanSubCategory> getNonHumanSubCategories() {
        Set<NonHumanSubCategory> set = new HashSet<NonHumanSubCategory>();
        for (NonHumanCategory c : nonHumanCategoryMap.values()) {
            set.addAll(c.getSubCategories());
        }
        return set;
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

    public HashMap<String, NonHumanResource> getNonHumanResourceMap() {
        return nonHumanMap ;
    }

    public HashMap<String, NonHumanCategory> getNonHumanCategoryMap() {
        return nonHumanCategoryMap ;
    }

    public Map<String, String> getParticipantIdentifiers() {
        return getParticipantIdentifiers(Identifier.FullName);
    }

    // idStr is a integer string 0..3
    public Map<String, String> getParticipantIdentifiers(String idStr) {
        Identifier identifier = Identifier.values()[0];              // default
        if (idStr.equals("1")) identifier = Identifier.values()[1];
        else if (idStr.equals("2")) identifier = Identifier.values()[2];
        else if (idStr.equals("3")) identifier = Identifier.values()[3];
        return getParticipantIdentifiers(identifier);
    }


    public Map<String, String> getParticipantIdentifiers(Identifier idType) {
        Map<String, String> idMap = new Hashtable<String, String>();
        for (Participant p : getParticipants()) {
            String nameValue ;
            switch (idType) {
                case FullName : nameValue = p.getFullName(); break;
                case ReverseFullName :
                    nameValue = p.getLastName() + ", " + p.getFirstName(); break;
                case LastName : nameValue = p.getLastName(); break;
                default : nameValue = p.getUserID();
            }
            idMap.put(p.getID(), nameValue);
        }
        return idMap;
    }

    public Map<String, String> getRoleIdentifiers() {
        Map<String, String> idMap = new Hashtable<String, String>();
        for (Role r : getRoles()) {
            idMap.put(r.getID(), r.getName());
        }
        return idMap;
    }

    public Map<String, String> getPositionIdentifiers() {
        Map<String, String> idMap = new Hashtable<String, String>();
        for (Position p : getPositions()) {
            idMap.put(p.getID(), p.getTitle());
        }
        return idMap;
    }

    public Map<String, String> getCapabilityIdentifiers() {
        Map<String, String> idMap = new Hashtable<String, String>();
        for (Capability c : getCapabilities()) {
            idMap.put(c.getID(), c.getCapability());
        }
        return idMap;
    }

    public Map<String, String> getOrgGroupIdentifiers() {
        Map<String, String> idMap = new Hashtable<String, String>();
        for (OrgGroup o : getOrgGroups()) {
            idMap.put(o.getID(), o.getGroupName());
        }
        return idMap;
    }

    public Map<String, String> getNonHumanResourceIdentifiers() {
        Map<String, String> idMap = new Hashtable<String, String>();
        for (NonHumanResource r : getNonHumanResources()) {
            idMap.put(r.getID(), r.getName());
        }
        return idMap;
    }

    public Map<String, String> getNonHumanCategoryIdentifiers() {
        Map<String, String> idMap = new Hashtable<String, String>();
        for (NonHumanCategory r : getNonHumanCategories()) {
            idMap.put(r.getID(), r.getName());
        }
        return idMap;
    }

    public String getNonHumanSubCategoriesAsXML(String categoryID) {
        XNode node = new XNode("nonHumanSubCategories");
        NonHumanCategory nhCategory = getNonHumanCategory(categoryID);
        if (nhCategory != null) {
            node.addAttribute("category", categoryID);
            for (String subcategory : nhCategory.getSubCategoryNames()) {
                node.addChild("subcategory", subcategory);
            }
        }    
        return node.toString();
    }

    public String getNonHumanCategorySet() {
        XNode node = new XNode("nonHumanCategorySet");
        for (NonHumanCategory category : nonHumanCategoryMap.values()) {
            XNode categoryNode = node.addChild("category");
            categoryNode.addAttribute("name", category.getName());
            for (String subcategory : category.getSubCategoryNames()) {
                categoryNode.addChild("subcategory", subcategory);
            }
        }
        return node.toString();
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

    public NonHumanResource getNonHumanResourceByName(String name) {
        for (NonHumanResource r : nonHumanMap.values()) {
            if (r.getName().equalsIgnoreCase(name))
                return r ;
        }
        return null ;                    // no match
    }

    public NonHumanCategory getNonHumanCategoryByName(String name) {
        for (NonHumanCategory r : nonHumanCategoryMap.values()) {
            if (r.getName().equalsIgnoreCase(name))
                return r ;
        }
        return null ;                    // no match
    }

    public Set<NonHumanResource> getNonHumanResources(NonHumanCategory category,
                                                      String subcategory) {
        Set<NonHumanResource> resources = new HashSet<NonHumanResource>();
        if (category != null) {
            resources = category.getSubCategoryResources(subcategory);
        }
        return resources ;
    }

    public Set<NonHumanResource> getNonHumanResources(String id, String subcategory) {
        return getNonHumanResources(getNonHumanCategory(id), subcategory);
    }

    public Set<NonHumanResource> getNonHumanResourcesByName(String category, String subcategory) {
        return getNonHumanResources(getNonHumanCategoryByName(category), subcategory);
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

    public boolean isKnownNonHumanResourceName(String name) {
        return getNonHumanResourceByName(name) != null;
    }

    public boolean isKnownNonHumanCategoryName(String name) {
        return getNonHumanCategoryByName(name) != null;
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

    public boolean isKnownNonHumanResource(String rid) {
        return nonHumanMap.containsKey(rid);
    }

    public boolean isKnownNonHumanCategory(String rid) {
        return nonHumanCategoryMap.containsKey(rid);
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

    public String getNonHumanResourceNames() {
        XNode node = new XNode("nonHumanResourceNames") ;
        for (NonHumanResource r : nonHumanMap.values()) {
            node.addChild("name", r.getName());
        }
        return node.toString();
    }

    public String getNonHumanCategoryNames() {
        XNode node = new XNode("nonHumanCategoryNames") ;
        for (NonHumanCategory r : nonHumanCategoryMap.values()) {
            node.addChild("name", r.getName());
        }
        return node.toString();
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


    public Set<AbstractResource> getRoleParticipantsWithCapability(String rid, String cid) {
        Set<AbstractResource> resourceSet = new HashSet<AbstractResource>();
        Role role = getRole(rid);
        if (role != null) {

            // filter role members by capability
            if (cid != null) {
                Capability cap = getCapability(cid);
                if (cap != null) {
                    for (AbstractResource member : role.getResources()) {
                        if (((Participant) member).getCapabilities().contains(cap)) {
                           resourceSet.add(member);
                        }
                    }
                }
            }
            else resourceSet = role.getResources();         // no cid means don't filter
        }
        return resourceSet;
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


    /**
     * Gets the immediate supervisor of a participant. If the participant holds multiple
     * positions, and therefore has multiple supervisors, one is returned as a
     * random selection.
     * @param pid the id of the participant to get the supervisor of
     * @return the Participant who is the supervisor of the pid passed, or null if there
     * is no supervisor.
     */
    public Participant getImmediateSupervisor(String pid) {
        return getImmediateSupervisor(getParticipant(pid));
    }


    /**
     * Gets the immediate supervisor of a participant. If the participant holds multiple
     * positions, and therefore has multiple supervisors, one is returned as a
     * random selection.
     * @param p the participant to get the supervisor of
     * @return the Participant who is the supervisor of the pid passed, or null if there
     * is no supervisor.
     */
    public Participant getImmediateSupervisor(Participant p) {
        if (p != null) {
            for (Position position : p.getPositions()) {
                Position superPosition = position.getReportsTo();
                if (superPosition != null) {
                    Set<AbstractResource> resources = superPosition.getResources();
                    if (! resources.isEmpty()) {
                        return (Participant) resources.iterator().next();
                    }
                }
            }
        }
        return null;
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

    public String getNonHumanResourcesAsXML() {
        Set<NonHumanResource> rList = new TreeSet<NonHumanResource>(nonHumanMap.values());

        StringBuilder xml = new StringBuilder("<nonhumanresources>") ;
        for (NonHumanResource r : rList) xml.append(r.toXML()) ;
        xml.append("</nonhumanresources>");
        return xml.toString() ;
    }


    public String getNonHumanSubCategoriesAsXML() {
        StringBuilder xml = new StringBuilder("<nonhumansubcategories>") ;
        for (NonHumanSubCategory n : getNonHumanSubCategories()) xml.append(n.toXML()) ;
        xml.append("</nonhumansubcategories>");
        return xml.toString() ;
    }


    public String getNonHumanCategoriesAsXML() {
        Set<NonHumanCategory> rList = new TreeSet<NonHumanCategory>(
                nonHumanCategoryMap.values());

        StringBuilder xml = new StringBuilder("<nonhumancategories>") ;
        for (NonHumanCategory r : rList) xml.append(r.toXML()) ;
        xml.append("</nonhumancategories>");
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

    /********************************/

    public boolean setResourceAvailability(String caseID, String id, boolean available) {
        AbstractResource resource = getResource(id);
        if (resource != null) {
            resource.setAvailable(available);
            updateResource(resource);        // persist the change
            EventLogger.log(null, caseID, id,
                    (available ? EventLogger.event.released : EventLogger.event.in_use));
        }
        return (resource != null);
    }


    public void freeResource(String caseID, String id) {
        setResourceAvailability(caseID, id, true);
    }
    
}
