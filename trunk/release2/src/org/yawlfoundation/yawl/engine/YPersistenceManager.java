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

package org.yawlfoundation.yawl.engine;


import org.apache.log4j.Logger;
import org.hibernate.*;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.yawlfoundation.yawl.authentication.YExternalClient;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.time.YWorkItemTimer;
import org.yawlfoundation.yawl.exceptions.Problem;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.logging.table.*;
import org.yawlfoundation.yawl.util.HibernateStatistics;

import java.util.Iterator;
import java.util.List;


/**
 * 
 * This class acts as a handler for transactional persistence within the engine.
 *
 * @author Andrew Hastie (M2 Investments)
 *         Date: 21/06/2005
 *         Time: 13:46:54
 *
 * @author Michael Adams - updated for v2.1 11/2009
 *
 */
public class YPersistenceManager {

    // persistence actions
    public static final int DB_UPDATE = 0;
    public static final int DB_DELETE = 1;
    public static final int DB_INSERT = 2;
    
    private static Class[] persistedClasses = {
            YSpecification.class, YNetRunner.class, YWorkItem.class, YIdentifier.class,
            YNetData.class, YAWLServiceReference.class, YExternalClient.class,
            YWorkItemTimer.class, YCaseNbrStore.class, Problem.class,
            YLogSpecification.class, YLogNet.class, YLogTask.class, YLogNetInstance.class,
            YLogTaskInstance.class, YLogEvent.class, YLogDataItemInstance.class,
            YLogDataType.class, YLogService.class, YAuditEvent.class
    } ;

    private static final boolean INSERT = false;
    private static final boolean UPDATE = true;
    private static Logger logger = null;

    private static SessionFactory factory = null;
    private boolean restoring = false;
    private boolean enabled = false;

    /**
     * Constructor
     */
    public YPersistenceManager() {
        logger = Logger.getLogger(YPersistenceManager.class);
    }


    protected SessionFactory initialise(boolean journalising) throws YPersistenceException {
        Configuration cfg;

        // Create the Hibernate config, check and create database if required,
        // and generally set things up .....
        if (journalising) {
            try {
                cfg = new Configuration();
                for (Class persistedClass : persistedClasses) {
                    cfg.addClass(persistedClass);
                }

                factory = cfg.buildSessionFactory();
                new SchemaUpdate(cfg).execute(false, true);
                setEnabled(true);
            }
            catch (Exception e) {
                e.printStackTrace();
                logger.fatal("Failure initialising persistence layer", e);
                throw new YPersistenceException("Failure initialising persistence layer", e);
            }
        }
        return factory;
    }


    public void setEnabled(boolean enable) { enabled = enable; }

    public boolean isEnabled() { return enabled; }


    public SessionFactory getFactory() {
        return factory;
    }


    public boolean isRestoring() {
        return restoring;
    }

    protected void setRestoring(boolean restoring) {
        this.restoring = restoring;
    }

    public Session getSession() {
        return factory.getCurrentSession();
    }

    public Transaction getTransaction() {
        return getSession().getTransaction();
    }

    public void closeSession() {
        if (isEnabled()) {
            try {
                Session session = getSession();
                if ((session != null) && (session.isOpen())) {
                    session.close();
                }
            } catch (HibernateException e) {
                logger.error("Failure to close Hibernate session", e);
            }
        }
    }


    public void closeFactory() {                    // shutdown persistence engine
        if (factory != null) factory.close();
    }


    public String getStatistics() {
        HibernateStatistics stats = new HibernateStatistics(factory);
        return stats.toXML();
    }

    public void setStatisticsEnabled(boolean enabled) {
        factory.getStatistics().setStatisticsEnabled(enabled);
    }

    public boolean isStatisticsEnabled() {
        return factory.getStatistics().isStatisticsEnabled();
    }
    

    /**
     * Start a new Hibernate transaction.
     * @return true if a transaction is started successfully, false if the session
     * already has an active transaction
     * @throws YPersistenceException if there's a problem starting a transaction 
     */
    public boolean startTransaction() throws YPersistenceException {
        if ((! isEnabled()) || isActiveTransaction()) return false;
        logger.debug("---> start Transaction");
        try {
            getSession().beginTransaction();
        } catch (HibernateException e) {
            logger.fatal("Failure to start transactional session", e);
            throw new YPersistenceException("Failure to start transactional session", e);
        }
        logger.debug("<--- start Transaction");
        return true;
    }

    /**
     * Persists an object.
     *
     * @param obj The object to be persisted
     */
    protected void storeObject(Object obj) throws YPersistenceException {
        if ((!restoring) && isEnabled()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Adding to insert cache: Type=" + obj.getClass().getName());
            }
            doPersistAction(obj, INSERT);
        }
    }



    /**
     * Causes the supplied object to be updated when the current transaction is committed.
     *
     * @param obj The object to be persisted
     */
    protected void updateObject(Object obj) throws YPersistenceException {
        if ((!restoring) && isEnabled()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Adding to update cache: Type=" + obj.getClass().getName());
            }
            doPersistAction(obj, UPDATE);
        }
    }


    /**
      * Causes the supplied object to be removed from the persistence cache when the
      * current transaction is committed.

      * @param obj The object to be persisted
      * @throws YPersistenceException
      */
     protected void deleteObject(Object obj) throws YPersistenceException {
         if (! isEnabled()) return;

         if (logger.isDebugEnabled()) {
             logger.debug("Adding to delete cache: Type=" + obj.getClass().getName());
         }
         try {
             getSession().delete(obj);
             getSession().flush();
         }
         catch (HibernateException e) {
             logger.error("Failed to delete - " + e.getMessage());
         }
         try {
             getSession().evict(obj);
         }
         catch (HibernateException he) {
             // nothing to do
         }
     }


    private void updateOrMerge(Object obj) {
        try {
            getSession().saveOrUpdate(obj);
        }
        catch (Exception e) {
            getSession().merge(obj);
       }
    }


    private boolean isActiveTransaction() {
        Transaction transaction = getTransaction();
        return (transaction != null) && transaction.isActive();
    }

    /**
     * Causes the supplied object to be persisted when the current transaction is committed.
     * This method simply calls {@link #storeObject(Object)} but is public in scope.
     *
     * @param obj The object to be persisted
     */
    public void storeObjectFromExternal(Object obj) throws YPersistenceException {
        storeObject(obj);
    }

    /**
     * Causes the supplied object to be updated within the persistence cache when the
     * current transaction is committed.
     * This method simply calls {@link #updateObject(Object)} but is public in scope.
     *
     * @param obj The object to be persisted
     */
    public void updateObjectExternal(Object obj) throws YPersistenceException {
        updateObject(obj);
    }


    /**
      * Causes the supplied object to be unpersisted when the current transaction is committed.
      * This method simply calls {@link #deleteObject(Object)} but is public in scope.
      *
      * @param obj The object to be unpersisted
      */
    public void deleteObjectFromExternal(Object obj) throws YPersistenceException {
        deleteObject(obj);
    }


    private synchronized void doPersistAction(Object obj, boolean update)
            throws YPersistenceException {
        if (logger.isDebugEnabled()) {
            logger.debug("--> doPersistAction: Object=" + obj.getClass().getName() +
                        (update ? " Mode=Update" : " Mode=Create"));
        }
        try {
            if (update) {
                updateOrMerge(obj);
            }
            else {
                getSession().save(obj);
            }
            getSession().flush();
        }
        catch (Exception e) {
            logger.error("Failure detected whilst persisting instance of " +
                    obj.getClass().getName(), e);
            try {
                getTransaction().rollback();
            }
            catch (Exception e2) {
                throw new YPersistenceException("Failure to rollback transactional session", e2);
            }
            throw new YPersistenceException("Failure detected whilst persisting instance of " +
                    obj.getClass().getName(), e);
        }

        try {
            getSession().evict(obj);
        }
        catch (HibernateException e) {
            logger.warn("Failure whilst evicting object from Hibernate session cache", e);
        }
        logger.debug("<-- doPersistAction");
    }


     public void commit() throws YPersistenceException {
        logger.debug("--> commit");
        try {
            if (isEnabled() && isActiveTransaction()) getTransaction().commit();
        }
        catch (Exception e1) {
            logger.fatal("Failure to commit transactional session - Rolling Back Transaction", e1);
            rollbackTransaction();
            throw new YPersistenceException("Failure to commit transactional session", e1);
        }
        logger.debug("<-- commit");
    }


    /**
     * Forces a rollback of the current transaction,<P>
     */
    protected void rollbackTransaction() throws YPersistenceException {
        logger.debug("--> rollback Transaction");
        if (isEnabled() && isActiveTransaction()) {
            try {
                getTransaction().rollback();
            }
            catch (HibernateException e) {
                throw new YPersistenceException("Failure to rollback transaction", e);
            }
            finally {
                closeSession();
            }
        }
        logger.debug("<-- rollback Transaction");
    }


    public Query createQuery(String queryString) throws YPersistenceException {
        try {
            return getSession().createQuery(queryString);
        }
        catch (HibernateException e) {
            throw new YPersistenceException("Failure to create Hibernate query object", e);
        }
    }


     /**
      * executes a Query object based on the sql string passed
      * @param queryString - the sql query to execute
      * @throws YPersistenceException if there's a problem reading the db
      * @return the List of objects returned
      */
     public List execQuery(String queryString) throws YPersistenceException {
         try {
              Query query = createQuery(queryString);
              return (query != null) ? query.list() : null;
         }
         catch (HibernateException he) {
              throw new YPersistenceException("Error executing query: " + queryString, he);
         }
     }


    /**
     * returns all the instances currently persisted for the class passed
     * @param className - the name of the class to retrieve instances of
     * @throws YPersistenceException if there's a problem reading the db
     * @return a List of the instances retrieved
     */
    public List getObjectsForClass(String className) throws YPersistenceException {
        return execQuery("from " + className);
    }


    /**
     * returns all the instances currently persisted for the class passed that
     * match the condition specified in the where clause
     * @param className the name of the class to retrieve instances of
     * @param whereClause the condition (without the 'where' part) e.g. "age=21"
     * @throws YPersistenceException if there's a problem reading the db
     * @return a List of the instances retrieved
     */
    public List getObjectsForClassWhere(String className, String whereClause)
                                                      throws YPersistenceException {
        try {
            String qry = String.format("from %s as tbl where tbl.%s",
                                        className, whereClause) ;
            Query query = createQuery(qry);
            return (query != null) ? query.list() : null;
        }
        catch (HibernateException he) {
            throw new YPersistenceException("Error reading data for class: " + className, he);
        }
    }


    /**
     * gets a scalar value (as an object) based on the values passed
     * @param className - the type of object to select
     * @param field - the column name which contains the queried value
     * @param value - the value to find in the 'field' column
     * @return the first (or only) object matching 'where [field] = [value]'
     */
    public Object selectScalar(String className, String field, String value)
            throws YPersistenceException {
        String qryStr = String.format("from %s as tbl where tbl.%s=%s",
                                       className, field, value);
        Iterator itr = createQuery(qryStr).iterate();
        if (itr.hasNext()) return itr.next();
        else return null ;
    }


    /** same as above but takes a long value instead */
    public Object selectScalar(String className, String field, long value)
            throws YPersistenceException {
        return selectScalar(className, field, String.valueOf(value));
    }

}
