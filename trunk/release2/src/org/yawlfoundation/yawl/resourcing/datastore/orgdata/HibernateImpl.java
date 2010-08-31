/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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

import org.yawlfoundation.yawl.resourcing.datastore.HibernateEngine;
import org.yawlfoundation.yawl.resourcing.resource.*;
import org.yawlfoundation.yawl.exceptions.YAuthenticationException;

import java.util.HashMap;
import java.util.List;

/**
 *  This class implements methods for Organisational Data CRUD.
 *
 *  @author Michael Adams
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
    private final String _nonHumanResource = NonHumanResource.class.getName();
    
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
        else if (obj instanceof NonHumanResource) prefix = "NH";

        return getNextID(prefix);
    }


    /** these 5 methods load resource entity sets individually */

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

    public HashMap<String,NonHumanResource> loadNonHumanResources() {
        HashMap<String,NonHumanResource> nhMap = new HashMap<String,NonHumanResource>();
        List<NonHumanResource> nhList = _db.getObjectsForClass(_nonHumanResource) ;
        for (NonHumanResource r : nhList) nhMap.put(r.getID(), r) ;
        return nhMap ;
    }


   /*******************************************************************************/

    // IMPLEMENTED METHODS FROM BASE DATASOURCE CLASS //
    // (see DataSource for details about the purpose of each method) //

    public ResourceDataSet loadResources() {

       ResourceDataSet ds = new ResourceDataSet(this) ;

       List<Capability> cList = _db.getObjectsForClass(_capability) ;
       for (Capability c : cList) ds.putCapability(c) ;

       List<OrgGroup> ogList = _db.getObjectsForClass(_orgGroup) ;
       for (OrgGroup o : ogList) ds.putOrgGroup(o) ;

       List<Position> posList = _db.getObjectsForClass(_position) ;
       for (Position p : posList) ds.putPosition(p) ;

       List<Role> roleList = _db.getObjectsForClass(_role) ;
       for (Role r : roleList) ds.putRole(r) ;

       List<Participant> pList = _db.getObjectsForClass(_participant) ;
       for (Participant par : pList) ds.putParticipant(par) ;

       List<NonHumanResource> resList = _db.getObjectsForClass(_nonHumanResource) ;
       for (NonHumanResource res : resList) ds.putNonHumanResource(res) ;

       return ds ;
    }


    public void update(Object obj) { _db.exec(obj, _UPDATE); }


    public void delete(Object obj) { _db.exec(obj, _DELETE); }


    public String insert(Object obj) {
        String id = getNextID(obj);

        // set the newly generated id
        if (obj instanceof AbstractResource) {
            ((AbstractResource) obj).setID(id);

            // if a Participant, pre-insert the user privileges
            if (obj instanceof Participant) {
                _db.exec(((Participant) obj).getUserPrivileges(), _INSERT);
            }
        }
        else ((AbstractResourceAttribute) obj).setID(id);

        _db.exec(obj, _INSERT);

        return id ;
    }

    public void importObj(Object obj) {
        if (obj instanceof Participant) {
            Participant p = (Participant) obj ;

            // pre-insert the participant's user privileges
            _db.exec(p.getUserPrivileges(), _INSERT);
        }
        _db.exec(obj, _INSERT);
    }


    public int execUpdate(String query) {
        return _db.execUpdate(query);
    }

    public boolean authenticate(String userid, String password) throws
            YAuthenticationException {
        return false;
    }


}



