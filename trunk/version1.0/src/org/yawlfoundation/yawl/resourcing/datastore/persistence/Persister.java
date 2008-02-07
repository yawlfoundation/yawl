package org.yawlfoundation.yawl.resourcing.datastore.persistence;

import org.yawlfoundation.yawl.resourcing.datastore.HibernateEngine;
import org.yawlfoundation.yawl.resourcing.resource.*;
import org.yawlfoundation.yawl.resourcing.WorkQueue;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;

import java.util.List;
import java.util.HashMap;
import java.io.Serializable;

/**
 *  This class implements methods for Organisational Data CRUD.
 *
 *  @author Michael Adams
 *  v0.1, 03/08/2007
 */

public class Persister implements Serializable {

    private HibernateEngine _db ;
    private static Persister _me;

    // persistence actions
    public final int _UPDATE = HibernateEngine.DB_UPDATE;
    public final int _DELETE = HibernateEngine.DB_DELETE;
    public final int _INSERT = HibernateEngine.DB_INSERT;


    public Persister() {
        _db = HibernateEngine.getInstance(true) ;
    }

    // only want one persister instance at runtime
    public static Persister getInstance() {
        if (_me == null) _me = new Persister();
        return _me ;

    }

   /*******************************************************************************/

    // IMPLEMENTED METHODS FROM BASE DATASOURCE CLASS //
    // (see DataSource for details about the purpose of each method) //

//    public ResourceDataSet loadResources() {
//
//       ResourceDataSet ds = new ResourceDataSet() ;
//
//       List<Capability> cList = selectAllCapabilities() ;
//       for (Capability c : cList) ds.capabilityMap.put(c.getID(), c) ;
//
//       List<OrgGroup> ogList = selectAllOrgGroups() ;
//       for (OrgGroup o : ogList) ds.orgGroupMap.put(o.getID(), o) ;
//
//       List<Position> posList = selectAllPositions() ;
//       for (Position p : posList) ds.positionMap.put(p.getID(), p) ;
//
//       List<Role> roleList = selectAllRoles() ;
//       for (Role r : roleList) ds.roleMap.put(r.getID(), r) ;
//
//       List<Participant> pList = selectAllParticipants() ;
//       for (Participant par : pList) ds.participantMap.put(par.getID(), par) ;
//
//       return ds ;
//    }
//
//
//    // Participants //
//
//    public List selectAllParticipants() {
//        return _db.getObjectsForClass(Participant.class.getName());
//    }
//
//
//    public Participant selectParticipant(String pid) {
//        return (Participant) _db.selectScalar(Participant.class.getName(),
//                                              "_resourceID", pid);
//    }
//
//    public void updateParticipant(Participant p) { _db.exec(p, _UPDATE); }
//
//    public String insertParticipant(Participant p) {
//        String id = getNextID() ;
//        p.setID(id);
//        _db.exec(p, _INSERT);
//        return id ;
//    }
//
//    public void deleteParticipant(Participant p) { _db.exec(p, _DELETE); }
//
//
//    // Roles //
//
//    public List selectAllRoles() {
//        return _db.getObjectsForClass(Role.class.getName());
//    }
//
//    public Role selectRole(String rid) {
//        return (Role) _db.selectScalar(Role.class.getName(), "_id", rid);
//    }
//
//    public void updateRole(Role r) { _db.exec(r, _UPDATE); }
//
//    public String insertRole(Role r) {
//        String id = getNextID() ;
//        r.setID(id);
//        _db.exec(r, _INSERT);
//        return id ;
//    }
//
//    public void deleteRole(Role r) { _db.exec(r, _DELETE); }
//
//
//    // User Privileges //
//
//    public List selectAllUserPrivileges()  {
//        return _db.getObjectsForClass(UserPrivileges.class.getName());
//    }
//
//    public UserPrivileges selectUserPrivileges(String pid) {
//        return (UserPrivileges) _db.selectScalar(UserPrivileges.class.getName(),
//                                                  "_participantID", pid);
//    }
//
//    public void updateUserPrivileges(UserPrivileges up) { _db.exec(up, _UPDATE); }
//
//    public void insertUserPrivileges(UserPrivileges up) { _db.exec(up, _INSERT); }
//
//    public void deleteUserPrivileges(UserPrivileges up) { _db.exec(up, _DELETE); }
//
//
//    // Capabilities //
//
//    public List selectAllCapabilities() {
//        return _db.getObjectsForClass(Capability.class.getName());
//    }
//
//    public Capability selectCapability(String cid) {
//        return (Capability) _db.selectScalar(Capability.class.getName(), "_id", cid);
//
//    }
//
//    public void updateCapability(Capability c) { _db.exec(c, _UPDATE); }
//
//    public String insertCapability(Capability c) {
//        String id = getNextID() ;
//        c.setID(id);
//        _db.exec(c, _INSERT);
//        return id ;
//    }
//
//    public void deleteCapability(Capability c) { _db.exec(c, _DELETE); }
//
//
//    // Positions //
//
//    public List selectAllPositions() {
//        return _db.getObjectsForClass(Position.class.getName());
//    }
//
//    public Position selectPosition(String pid) {
//        return (Position) _db.selectScalar(Position.class.getName(), "_id", pid);
//    }
//
//    public void updatePosition(Position p) { _db.exec(p, _UPDATE); }
//
//    public String insertPosition(Position p) {
//        String id = getNextID() ;
//        p.setID(id);
//        _db.exec(p, _INSERT);
//        return id ;
//    }
//
//    public void deletePosition(Position p) { _db.exec(p, _DELETE); }
//
//
//    // OrgGroups //
//
//    public List selectAllOrgGroups() {
//        return _db.getObjectsForClass(OrgGroup.class.getName());
//    }
//
//    public OrgGroup selectOrgGroup(String oid) {
//        return (OrgGroup) _db.selectScalar(OrgGroup.class.getName(), "_id", oid);
//    }
//
//    public void updateOrgGroup(OrgGroup o) { _db.exec(o, _UPDATE); }
//
//    public String insertOrgGroup(OrgGroup o) {
//        String id = getNextID() ;
//        o.setID(id);
//        _db.exec(o, _INSERT);
//        return id ;
//    }
//
//    public void deleteOrgGroup(OrgGroup o) { _db.exec(o, _DELETE); }
//
//
//    // Joins //
//
//    public List selectRoleParticipants(String rid) {
//        return _db.execJoinQuery(_db.tblParticipant, "_roles", rid) ;
//    }
//
//    public List selectCapabilityParticipants(String cid) {
//        return _db.execJoinQuery(_db.tblParticipant, "_capabilities", cid) ;
//    }
//
//    public List selectPositionParticipants(String pid) {
//        return _db.execJoinQuery(_db.tblParticipant, "_positions", pid) ;
//    }
//
//
//

   public HashMap selectMap(String className) {
       HashMap result = new HashMap() ;
       if (className.endsWith("UserPrivileges")) {
           List<UserPrivileges> upList = _db.getObjectsForClass(className) ;
           for (UserPrivileges up : upList) result.put(up.getID(), up) ;
       }
       else if (className.endsWith("WorkItemRecord")) {
           List<WorkItemRecord> qsList = _db.getObjectsForClass(className) ;
           for (WorkItemRecord wir : qsList) result.put(wir.getID(), wir) ;
       }
       else if (className.endsWith("WorkQueue")) {
           List<WorkQueue> wqList = _db.getObjectsForClass(className) ;
           for (WorkQueue wq : wqList) result.put(wq.getID(), wq) ;
       }
       return result ;
   }


    public List select(Object obj) {
        return select(obj.getClass().getName());
    }

    public List select(String className) {
        return _db.getObjectsForClass(className);
    }

    public List selectWhere(String className, String whereClause) {
       return _db.getObjectsForClassWhere(className, whereClause) ;
    }


    public Object selectScalar(String className, String id) {
       Object retObj = null ;
       if (className.endsWith("Participant"))
           retObj = _db.selectScalar(className,"_resourceID", id);
       else if (className.endsWith("UserPrivileges"))
           retObj = _db.selectScalar(className,"_participantID", id);
       else if ((className.endsWith("QueueSet")) ||
                (className.endsWith("WorkQueue")))
           retObj = _db.selectScalar(className,"_ownerID", id);
       else
           retObj = _db.selectScalar(className,"_id", id);

       return retObj ;
    }

    public void update(Object obj) { _db.exec(obj, _UPDATE); }

    public void delete(Object obj) { _db.exec(obj, _DELETE); }

    public void insert(Object obj) { _db.exec(obj, _INSERT); }

}
