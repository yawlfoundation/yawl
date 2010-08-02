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

package org.yawlfoundation.yawl.resourcing.datastore.persistence;

import org.hibernate.Query;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.WorkQueue;
import org.yawlfoundation.yawl.resourcing.datastore.HibernateEngine;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.SpecLog;
import org.yawlfoundation.yawl.resourcing.resource.UserPrivileges;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

   public Map<String, Object> selectMap(String className) {
       Map<String, Object> result = new HashMap<String, Object>() ;
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
       else if (className.endsWith("SpecLog")) {
           List<SpecLog> slList = _db.getObjectsForClass(className) ;
           for (SpecLog sl : slList) result.put(sl.getSpecID().getKey() + sl.getVersion(), sl) ;
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

    public List execQuery(String query) {
        return _db.execQuery(query);
    }

    public int execUpdate(String statement) {
        return _db.execUpdate(statement);
    }

    public Query createQuery(String query) {
        return _db.createQuery(query);
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
