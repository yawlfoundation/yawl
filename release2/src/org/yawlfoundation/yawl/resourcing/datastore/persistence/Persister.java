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
       else if (className.endsWith("AutoTask"))
           retObj = _db.selectScalar(className,"_wirID", id);
       else
           retObj = _db.selectScalar(className,"_id", id);

       return retObj ;
    }

    public void update(Object obj) { _db.exec(obj, _UPDATE); }

    public void delete(Object obj) { _db.exec(obj, _DELETE); }

    public void insert(Object obj) { _db.exec(obj, _INSERT); }

}
