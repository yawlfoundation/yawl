/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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
import org.hibernate.Transaction;
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
 * This class is a thin client of HibernateEngine that implements methods for 
 * Organisational Data CRUD.
 *
 *  @author Michael Adams
 *  v0.1, 03/08/2007
 */

public final class Persister implements Serializable {

    private HibernateEngine _db ;
    private static Persister _me;


    private Persister() {
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
       commit();
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

    public int execUpdate(String statement, boolean commit) {
        return _db.execUpdate(statement, commit);
    }

    public Query createQuery(String query) {
        return _db.createQuery(query);
    }

    public Transaction beginTransaction() { return _db.beginTransaction(); }

    public Transaction getOrBeginTransaction() { return _db.getOrBeginTransaction(); }

    public Object load(Class claz, Serializable key) { return _db.load(claz, key); }

    public Object get(Class claz, Serializable key) { return _db.get(claz, key); }

    public void commit() { _db.commit(); }

    public void rollback() { _db.rollback(); }

    public void closeDB() { _db.closeFactory(); }


    public Object selectScalar(String className, String id) {
       Object retObj ;
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

    public boolean update(Object obj) { return _db.exec(obj, HibernateEngine.DB_UPDATE); }

    public boolean delete(Object obj) { return _db.exec(obj, HibernateEngine.DB_DELETE); }

    public boolean insert(Object obj) { return _db.exec(obj, HibernateEngine.DB_INSERT); }

    public boolean update(Object obj, Transaction tx) {
        return (tx != null) ? _db.exec(obj, HibernateEngine.DB_UPDATE, tx) : update(obj);
    }

    public boolean delete(Object obj, Transaction tx) {
        return (tx != null) ? _db.exec(obj, HibernateEngine.DB_DELETE, tx) : delete(obj);
    }

    public boolean insert(Object obj, Transaction tx) {
        return (tx != null) ? _db.exec(obj, HibernateEngine.DB_INSERT, tx): insert(obj);
    }

    public boolean update(Object obj, boolean commit) {
        return _db.exec(obj, HibernateEngine.DB_UPDATE, commit);
    }

    public boolean delete(Object obj, boolean commit) {
        return _db.exec(obj, HibernateEngine.DB_DELETE, commit);
    }

    public boolean insert(Object obj, boolean commit) {
        return _db.exec(obj, HibernateEngine.DB_INSERT, commit);
    }

}
