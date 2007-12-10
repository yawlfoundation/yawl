/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.datastore.orgdata;

import java.util.*;

import org.yawlfoundation.yawl.resourcing.resource.*;
import org.yawlfoundation.yawl.resourcing.datastore.HibernateEngine;

/**
 *  This class implements methods for Organisational Data CRUD.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@yawlfoundation.org
 *  v0.1, 03/08/2007
 */

public class HibernateImpl extends DataSource {

    private HibernateEngine _db ;

    // persistence actions
    private final int _UPDATE = HibernateEngine.DB_UPDATE;
    private final int _DELETE = HibernateEngine.DB_DELETE;
    private final int _INSERT = HibernateEngine.DB_INSERT;

    // object names
    private final String _participant = Participant.class.getName();
    private final String _role = Role.class.getName();
    private final String _capability = Capability.class.getName();
    private final String _position = Position.class.getName();
    private final String _orgGroup = OrgGroup.class.getName();

    // the constructor
    public HibernateImpl() {
        _db = HibernateEngine.getInstance(true) ;
    }


    /**
     * Override of super.getNextID() to apply an appropriate prefix to the id
     * @param obj the object to generate a unique id for
     * @return a unique identifier appropriately prefixed
     */
    private String getNextID(Object obj) {
        String prefix = "";

        if (obj instanceof OrgGroup) prefix = "OG" ;
        else if (obj instanceof Capability) prefix = "CA" ;
        else if (obj instanceof Position) prefix = "PO" ;
        else if (obj instanceof Role) prefix = "RO" ;
        else if (obj instanceof Participant) prefix = "PA";

        return getNextID(prefix);
    }


    /** these 4 methods load resource entity sets individually */

    public HashMap<String,Capability> loadCapabilities() {
        HashMap<String,Capability> capMap = new HashMap<String,Capability>();
        List<Capability> cList = _db.getObjectsForClass(_capability) ;
        for (Capability c : cList) capMap.put(c.getID(), c) ;
        return capMap ;
    }

    public HashMap<String,Role> loadRoles() {
        HashMap<String,Role> roleMap = new HashMap<String,Role>() ;
        List<Role> roleList = _db.getObjectsForClass(_role) ;
        for (Role r : roleList) roleMap.put(r.getID(), r) ;
        return roleMap ;
    }

    public HashMap<String,Position> loadPositions() {
        HashMap<String,Position> posMap = new HashMap<String,Position>();
        List<Position> posList = _db.getObjectsForClass(_position) ;
        for (Position p : posList) posMap.put(p.getID(), p) ;
        return posMap ;
    }

    public HashMap<String,OrgGroup> loadOrgGroups() {
        HashMap<String,OrgGroup> orgMap = new HashMap<String,OrgGroup>();
        List<OrgGroup> ogList = _db.getObjectsForClass(_orgGroup) ;
        for (OrgGroup o : ogList) orgMap.put(o.getID(), o) ;
        return orgMap ;
    }             


   /*******************************************************************************/

    // IMPLEMENTED METHODS FROM BASE DATASOURCE CLASS //
    // (see DataSource for details about the purpose of each method) //

    public ResourceDataSet loadResources() {

       ResourceDataSet ds = new ResourceDataSet() ;

       List<Capability> cList = _db.getObjectsForClass(_capability) ;
       for (Capability c : cList) ds.capabilityMap.put(c.getID(), c) ;

       List<OrgGroup> ogList = _db.getObjectsForClass(_orgGroup) ;
       for (OrgGroup o : ogList) ds.orgGroupMap.put(o.getID(), o) ;

       List<Position> posList = _db.getObjectsForClass(_position) ;
       for (Position p : posList) ds.positionMap.put(p.getID(), p) ;

       List<Role> roleList = _db.getObjectsForClass(_role) ;
       for (Role r : roleList) ds.roleMap.put(r.getID(), r) ;

       List<Participant> pList = _db.getObjectsForClass(_participant) ;
       for (Participant par : pList) ds.participantMap.put(par.getID(), par) ;

       return ds ;
    }


    public void update(Object obj) { _db.exec(obj, _UPDATE); }


    public void delete(Object obj) { _db.exec(obj, _DELETE); }


    public String insert(Object obj) {
        String id = getNextID(obj);

        // set the newly generated id
        if (obj instanceof Participant) {
            Participant p = (Participant) obj ;
            p.setID(id);

            // pre-insert the particpant's user privileges
            _db.exec(p.getUserPrivileges(), _INSERT);

        }
        else ((AbstractResourceAttribute) obj).setID(id);

        _db.exec(obj, _INSERT);

        return id ;
    }

}



