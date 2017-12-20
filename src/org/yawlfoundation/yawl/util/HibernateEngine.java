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

package org.yawlfoundation.yawl.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.*;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.c3p0.internal.C3P0ConnectionProvider;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Criterion;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;

import java.io.Serializable;
import java.util.List;
import java.util.Properties;
import java.util.Set;


/**
 *  This singleton class provides db & persistence support via Hibernate.
 *
 *  @author Michael Adams
 *  @date 03/08/2007
 *
 *  last update: 18/09/2014 (for v3.0+)
 */

public class HibernateEngine {

    // persistence actions
    public static final int DB_UPDATE = 0;
    public static final int DB_DELETE = 1;
    public static final int DB_INSERT = 2;

    // reference to Hibernate
    private SessionFactory _factory = null;
    private boolean _persistOn = false;

    private static final Logger _log = LogManager.getLogger(HibernateEngine.class);


    /*********************************************************************************/

    // Constructors and Initialisation //
    /***********************************/

    /** The constuctor - called from getInstance() */
    public HibernateEngine(boolean persistenceOn, Set<Class> classes, Properties props)
            throws HibernateException {
        _persistOn = persistenceOn;
        initialise(classes, props);
    }


    public HibernateEngine(boolean persistenceOn, Set<Class> classes) {
        this(persistenceOn, classes, null);
    }


    /** initialises hibernate and the required tables */
    private void initialise(Set<Class> classes, Properties props) throws HibernateException {
        try {
            Configuration _cfg = new Configuration();

            // if props supplied, use them instead of hibernate.properties
            if (props != null) {
                _cfg.setProperties(props);
            }

            // add each persisted class to config
            for (Class persistedClass : classes) {
                _cfg.addClass(persistedClass);
            }

            // get a session context
            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(_cfg.getProperties()).build();
            _factory = _cfg.buildSessionFactory(serviceRegistry);

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
            getOrBeginTransaction();
            Query query = getSession().createQuery("from " + tableName).setMaxResults(1);
            boolean hasTable = ! query.list().isEmpty();
            commit();
            return hasTable;
        }
        catch (Exception e) {
            return false;
        }
     }


    public void setPersisting(boolean persist) { _persistOn = persist; }


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
            tx = getOrBeginTransaction();
            boolean success = exec(obj, action, tx);
            if (success && commit) {
                commit();
            }
            return success;
        }
        catch (HibernateException he) {
            _log.error("Handled Exception: Error accessing transaction (" +
                    actionToString(action) + "): " + obj.toString(), he);
            if (tx != null) tx.rollback();
            return false;
        }
    }


    /**
     * persists the object instance passed
     * @param obj - an instance of the object to persist
     * @param action - type type of action performed
     * @param tx - an active Transaction object. NOTE: Any objects persisted via this
     * method will not be permanently actioned until the transaction is committed via
     * a call to 'commit()'.
     * @return true if persist was successful, false if otherwise
     */
    public boolean exec(Object obj, int action, Transaction tx) {
        try {
            Session session = getSession();
            if (action == DB_INSERT) session.save(obj);
            else if (action == DB_UPDATE) updateOrMerge(session, obj);
            else if (action == DB_DELETE) session.delete(obj);

            return true;
        }
        catch (HibernateException he) {
            _log.error("Handled Exception: Error persisting object (" +
                    actionToString(action) + "): " + obj.toString(), he);
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
            Transaction tx = getSession().getTransaction();
            _log.debug("Transaction GET tx = {}; isActive = {}",
                    tx, (tx != null && tx.isActive()));
            return ((tx != null) && tx.isActive()) ? tx : beginTransaction();
        }
        catch (HibernateException he) {
            _log.error("Caught Exception: Error creating or getting transaction", he);
            return null;
        }        
    }


    /**
     * executes a Query object based on the hql string passed
     * @param queryString - the hibernate query to execute
     * @return the List of objects returned, or null if the query has some problem
     */
    public List execQuery(String queryString) {
        List result = null;
        Transaction tx = null;
        try {
            tx = getOrBeginTransaction();
            Query query = getSession().createQuery(queryString);
            if (query != null) result = query.list();
        }
        catch (JDBCConnectionException jce) {
            _log.error("Caught Exception: Couldn't connect to datasource - " +
                    "continuing with an empty dataset");
            if (tx != null) tx.rollback();
        }
        catch (HibernateException he) {
            _log.error("Caught Exception: Error executing query: " + queryString, he);
            if (tx != null) tx.rollback();
        }

        return result;
    }


    /**
     * executes a plain SQL Query
     * @param queryString - the SQL query to execute
     * @return the List of objects returned, or null if the query has some problem
     */
    public List execSQLQuery(String queryString) {
        List result = null;
        Transaction tx = null;
        try {
            tx = getOrBeginTransaction();
            Query query = getSession().createSQLQuery(queryString);
            if (query != null) result = query.list();
            commit();
        }
        catch (JDBCConnectionException jce) {
            _log.error("Caught Exception: Couldn't connect to datasource - " +
                    "starting with an empty dataset");
            if (tx != null) tx.rollback();
        }
        catch (HibernateException he) {
            _log.error("Caught Exception: Error executing query: " + queryString, he);
            rollback();
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
            tx = getOrBeginTransaction();
            result = getSession().createQuery(queryString).executeUpdate();
            if (commit) commit();
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
            tx = getOrBeginTransaction();
            return getSession().createQuery(queryString);
        }
        catch (HibernateException he) {
            _log.error("Caught Exception: Error creating query: " + queryString, he);
            if (tx != null) tx.rollback();
        }
        return null;
    }


    public Transaction beginTransaction() {
        return getSession().beginTransaction();
    }


    public Object load(Class claz, Serializable key) {
        return load(claz, key, true);
    }

    public Object load(Class claz, Serializable key, boolean doCommit) {
        getOrBeginTransaction();
        Object result = getSession().load(claz, key);
        Hibernate.initialize(result);
        if (doCommit) commit();
        return result;
    }


    public Object get(Class claz, Serializable key) {
        return get(claz, key, true);
    }

    public Object get(Class claz, Serializable key, boolean doCommit) {
        getOrBeginTransaction();
        Object result = getSession().get(claz, key);
        Hibernate.initialize(result);
        if (doCommit) commit();
        return result;
    }


    public List getByCriteria(Class claz, Criterion... criteria) {
        return getByCriteria(claz, true, criteria);
    }

    public List getByCriteria(Class claz, boolean commit, Criterion... criteria) {
        getOrBeginTransaction();
        Criteria c = getSession().createCriteria(claz);
        for (Criterion criterion : criteria) {
            c.add(criterion);
        }
        List result = c.list();
        if (commit) commit();
        return result;
    }


    public void commit() {
        try {
            Transaction tx = getSession().getTransaction();
            if ((tx != null) && tx.isActive()) {
                _log.debug("Transaction COMMIT tx: " + tx);
                tx.commit();
            }
        }
        catch (HibernateException he) {
            _log.error("Caught Exception: Error committing transaction", he);
        }
    }


    public void rollback() {
        try {
            Transaction tx = getSession().getTransaction();
            if ((tx != null) && tx.isActive()) tx.rollback();
        }
        catch (HibernateException he) {
            _log.error("Caught Exception: Error rolling back transaction", he);
        }
    }


    public void closeFactory() {
        if (_factory != null) {
            if (_factory instanceof SessionFactoryImpl) {
               SessionFactoryImpl sf = (SessionFactoryImpl) _factory;
               ConnectionProvider conn = sf.getConnectionProvider();
               if (conn instanceof C3P0ConnectionProvider) {
                 ((C3P0ConnectionProvider)conn).stop();
               }
            }

            _factory.close();
        }
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
    
    
    private Session getSession() {
        return _factory.getCurrentSession();
    }

    /****************************************************************************/

}
