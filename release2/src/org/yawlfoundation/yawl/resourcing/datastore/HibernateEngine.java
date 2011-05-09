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

package org.yawlfoundation.yawl.resourcing.datastore;

import org.apache.log4j.Logger;
import org.hibernate.*;
import org.hibernate.cfg.Configuration;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.ResourceMap;
import org.yawlfoundation.yawl.resourcing.WorkQueue;
import org.yawlfoundation.yawl.resourcing.calendar.CalendarEntry;
import org.yawlfoundation.yawl.resourcing.calendar.CalendarLogEntry;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.AuditEvent;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.ResourceEvent;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.SpecLog;
import org.yawlfoundation.yawl.resourcing.resource.*;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanCategory;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanSubCategory;

import java.io.Serializable;
import java.util.List;


/**
 *  This singleton class provides db & persistence support via Hibernate.
 *
 *  @author Michael Adams
 *  @date 03/08/2007
 *
 *  last update: 26/08/2010 (for v2.2)
 */

public class HibernateEngine {

    // persistence actions
    public static final int DB_UPDATE = 0;
    public static final int DB_DELETE = 1;
    public static final int DB_INSERT = 2;

    // reference to Hibernate
    private static SessionFactory _factory = null;

    // instance reference
    private static HibernateEngine _me;

    // class references for config
    private static Class[] persistedClasses = {
            Participant.class, Role.class, Capability.class, Position.class,
            OrgGroup.class, UserPrivileges.class, NonHumanResource.class,
            WorkQueue.class, ResourceMap.class, PersistedAutoTask.class,
            CalendarEntry.class, WorkItemRecord.class, ResourceEvent.class,
            AuditEvent.class, SpecLog.class, CalendarLogEntry.class,
            NonHumanCategory.class, NonHumanSubCategory.class
    };


    private static boolean _persistOn = false;
    private static final Logger _log = Logger.getLogger(HibernateEngine.class);


    /*********************************************************************************/

    // Constructors and Initialisation //
    /***********************************/

    /** The constuctor - called from getInstance() */
    private HibernateEngine(boolean persistenceOn) throws HibernateException {
        _persistOn = persistenceOn;
        initialise();
    }


    /** returns the current HibernateEngine instance */
    public static HibernateEngine getInstance(boolean persistenceOn) {
        if (_me == null) {
            try {
                _me = new HibernateEngine(persistenceOn);
            }
            catch (HibernateException he) {
                _persistOn = false ;
                _log.error("Could not initialise database connection.", he);
            }
        }
        return _me;
    }


    /** initialises hibernate and the required tables */
    private void initialise() throws HibernateException {
        try {
            Configuration _cfg = new Configuration();

            // add each persisted class to config
            for (Class persistedClass : persistedClasses) {
                _cfg.addClass(persistedClass);
            }

           // get a session context
            _factory = _cfg.buildSessionFactory();

            // check tables exist and are of a matching format to the persisted objects
            new SchemaUpdate(_cfg).execute(false, true);

        }
        catch (MappingException me) {
            _log.error("Could not initialise database connection.", me);
        }
    }


    /** @return true if a table of 'tableName' currently exists and has at least one row */
    public boolean isAvailable(String tableName) {
        try {
            Session session = _factory.getCurrentSession();
            getOrBeginTransaction();
            Query query = session.createQuery("from " + tableName).setMaxResults(1);
            boolean hasTable = query.list().size() > 0;
            commit();
            return hasTable;
        }
        catch (Exception e) {
            return false;
        }
     }



    /** @return true if this instance is persisting */
    public boolean isPersisting() {
        return _persistOn;
    }


    /******************************************************************************/

    // Persistence Methods //
    /***********************/

    public boolean exec(Object obj, int action) {
        return exec(obj, action, true);
    }

    /**
     * persists the object instance passed
     * @param obj - an instance of the object to persist
     * @param action - type type of action performed
     * @return true if persist was successful, false if otherwise
     */
    public boolean exec(Object obj, int action, boolean commit) {

        Transaction tx = null;
        try {
            Session session = _factory.getCurrentSession();
            tx = getOrBeginTransaction();

            if (action == DB_INSERT) session.save(obj);
            else if (action == DB_UPDATE) updateOrMerge(session, obj);
            else if (action == DB_DELETE) session.delete(obj);

            if (commit) tx.commit();
            return true;
        }
        catch (HibernateException he) {
            _log.error("Handled Exception: Error persisting object (" + actionToString(action) +
                    "): " + obj.toString(), he);
            if (tx != null) tx.rollback();
            return false;
        }
    }


    /**
     * persists the object instance passed
     * @param obj - an instance of the object to persist
     * @param action - type type of action performed
     * @params tx - an active Transaction object. NOTE: Any objects persisted via this
     * method will not be permanently actioned until the transaction is committed via
     * a call to 'commit()'.
     * @return true if persist was successful, false if otherwise
     */
    public boolean exec(Object obj, int action, Transaction tx) {

        try {
            Session session = _factory.getCurrentSession();
            if (action == DB_INSERT) session.save(obj);
            else if (action == DB_UPDATE) updateOrMerge(session, obj);
            else if (action == DB_DELETE) session.delete(obj);

            return true;
        }
        catch (HibernateException he) {
            _log.error("Handled Exception: Error persisting object (" + actionToString(action) +
                    "): " + obj.toString(), he);
            if (tx != null) tx.rollback();
            return false;
        }
    }



    /* a workaround for a hibernate 'feature' */
    private void updateOrMerge(Session session, Object obj) {
        try {
            session.saveOrUpdate(obj);
        }
        catch (Exception e) {
              session.merge(obj);
       }
    }


    public Transaction getOrBeginTransaction() {
        try {
           Transaction tx = _factory.getCurrentSession().getTransaction();
           return ((tx != null) && tx.isActive()) ? tx : beginTransaction();
        }
        catch (HibernateException he) {
            _log.error("Caught Exception: Error creating or getting transaction", he);
            return null;
        }        
    }


    /**
     * executes a Query object based on the sql string passed
     * @param queryString - the sql query to execute
     * @return the List of objects returned, or null if the query has some problem
     */
    public List execQuery(String queryString) {

        List result = null;
        Transaction tx = null;
        try {
            Session session = _factory.getCurrentSession();
            tx = getOrBeginTransaction();
            Query query = session.createQuery(queryString);
            if (query != null) result = query.list();
        }
        catch (JDBCConnectionException jce) {
            _log.error("Caught Exception: Couldn't connect to datasource - " +
                    "starting with an empty dataset");
        }
        catch (HibernateException he) {
            _log.error("Caught Exception: Error executing query: " + queryString, he);
            if (tx != null) tx.rollback();
        }

        return result;
     }


    public int execUpdate(String queryString) {
        return execUpdate(queryString, true);
    }


    public int execUpdate(String queryString, boolean commit) {
        int result = -1;
        Transaction tx = null;
        try {
            Session session = _factory.getCurrentSession();
            tx = getOrBeginTransaction();
            result = session.createQuery(queryString).executeUpdate();
            if (commit) tx.commit();
        }
        catch (JDBCConnectionException jce) {
            _log.error("Caught Exception: Couldn't connect to datasource - " +
                    "starting with an empty dataset");
        }
        catch (HibernateException he) {
            _log.error("Caught Exception: Error executing query: " + queryString, he);
            if (tx != null) tx.rollback();
        }

        return result;
    }


    public Query createQuery(String queryString) {
        Transaction tx = null;
        try {
            Session session = _factory.getCurrentSession();
            tx = getOrBeginTransaction();
            return session.createQuery(queryString);
        }
        catch (HibernateException he) {
            _log.error("Caught Exception: Error creating query: " + queryString, he);
            if (tx != null) tx.rollback();
        }
        return null;
    }


    public Transaction beginTransaction() {
        return _factory.getCurrentSession().beginTransaction();
    }


    public Object load(Class claz, Serializable key) {
        getOrBeginTransaction();
        return _factory.getCurrentSession().load(claz, key);
    }


    public Object get(Class claz, Serializable key) {
        getOrBeginTransaction();
        return _factory.getCurrentSession().get(claz, key);
    }

    
    public void commit() {
        try {
           Transaction tx = _factory.getCurrentSession().getTransaction();
           if ((tx != null) && tx.isActive()) tx.commit();
        }
        catch (HibernateException he) {
            _log.error("Caught Exception: Error committing transaction", he);
        }
    }


    public void rollback() {
        try {
           Transaction tx = _factory.getCurrentSession().getTransaction();
           if ((tx != null) && tx.isActive()) tx.rollback();
        }
        catch (HibernateException he) {
            _log.error("Caught Exception: Error rolling back transaction", he);
        }
    }


    public void closeFactory() {
        if (_factory != null) _factory.close();
    }


    /**
     * executes a join query. For example, passing ("car", "part", "pid") will
     * return a list of 'car' objects that have in their 'part' property (a Set) a
     * part with a key id="pid"
     *
     * @param table the parent table - returned objects are of this class
     * @param field the property name of the [Set] column in the parent table
     * @param value the id of the child object in the set to match
     * @return a List of objects of class 'table' that have, in their [Set] property
     *         called 'field', an object with an key field value of 'value'
     */
    public List execJoinQuery(String table, String field, String value) {
        String qry = String.format("from %s parent where '%s' in elements(parent.%s)",
                                    table, value, field) ;
        return execQuery(qry) ;
    }


    /**
     * gets a scalar value (as an object) based on the sql string passed
     * @param className - the type of object to select
     * @param field - the column name which contains the queried value
     * @param value - the value to find in the 'field' column
     * @return the first (or only) object matching 'where [field] = [value]'
     */
    public Object selectScalar(String className, String field, String value) {
        String qry = String.format("from %s as tbl where tbl.%s = '%s'",
                                    className, field, value);
        List result = execQuery(qry) ;
        if (result != null) {
            if (! result.isEmpty()) return result.iterator().next();
        }
        return null ;
    }


    /**
     * returns all the instances currently persisted for the class passed
     * @param className - the name of the class to retrieve instances of
     * @return a List of the instances retrieved
     */
    public List getObjectsForClass(String className) {
        return execQuery("from " + className);
    }


    /**
     * returns all the instances currently persisted for the class passed that
     * match the condition specified in the where clause
     * @param className the name of the class to retrieve instances of
     * @param whereClause the condition (without the 'where' part) e.g. "age=21"
     * @return a List of the instances retrieved
     */
    public List getObjectsForClassWhere(String className, String whereClause) {
        List result = null;
        try {
            String qry = String.format("from %s as tbl where tbl.%s",
                                        className, whereClause) ;
            result = execQuery(qry);
        }
        catch (HibernateException he) {
            _log.error("Error reading data for class: " + className, he);
        }
        return result ;
    }


    /**
     * returns a String representation of the action passed
     * @param action
     * @return the string equivalent
     */
    private String actionToString(int action) {
        String result = null ;
        switch (action) {
            case DB_UPDATE: result = "update"; break;
            case DB_DELETE: result = "delete"; break;
            case DB_INSERT: result = "insert"; break;
        }
        return result ;
    }

    /****************************************************************************/

}
