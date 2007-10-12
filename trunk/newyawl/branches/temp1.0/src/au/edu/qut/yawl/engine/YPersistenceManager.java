/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;


import au.edu.qut.yawl.authentication.User;
import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.admintool.model.HumanResourceRole;
import au.edu.qut.yawl.logging.YWorkItemEvent;
import au.edu.qut.yawl.logging.YCaseEvent;
import au.edu.qut.yawl.logging.YWorkItemDataEvent;
import net.sf.hibernate.*;
import net.sf.hibernate.cfg.Configuration;
import net.sf.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;


/**
 * 
 *
 * @author Andrew Hastie (M2 Investments)
 *         Date: 21/06/2005
 *         Time: 13:46:54
 *         
 *         <p/>
 *         This class acts as a simple manager for handling transactional persistence within the engine.<P>
 */
public class YPersistenceManager {
    /**
     * Flag indicating if we batch up all the peristence requests and action them at the point of commital, or
     * whether we preserve the Beta5 engine image style of performing the updates atomically.
     *
     * AJH: This currently does not work correctly as I've not fully completed the work on lazy caching.
     */
    private static final boolean LAZY_UPDATES = false;

    /**
     * Flag indicating if we attempt to optimise the caching of objects within the object_to_be_persisted cache.
     *
     * AJH: This currently does not work correctly as not all objects passed into persistence have the nested oobjects
     * present. Need to do more work on this.
     */
    private static final boolean OPTIMIZED_CACHING = false;

    // Persistable type constants
    private static int PT_IDENT = 1;
    private static int PT_CASE_DATA = 2;
    private static int PT_LOG_DATA = 3;
    private static int PT_LOG_IDENT = 4;
    private static int PT_NET_RUNNER = 5;
    private static int PT_SPEC_FILE = 6;
    private static int PT_WORK_ITEM = 7;
    private static int PT_WORK_ITEM_EVENT = 8;
    private static int PT_SERVICE_REF = 9;
    private static int PT_USER = 10;


    // Note: We use Hashtables rather than Hashmaps or anything from collections framework as ideally all of
    // persistence needs to be thread safe.
    private Hashtable objectsToStore;
    private Hashtable objectsToUpdate;
    private Hashtable objectsToDelete;

    private static SessionFactory factory = null;
    private Session session = null;
    private Transaction transaction = null;
    private Connection con = null;

    private static Logger logger = null;
    private boolean restoring = false;

    /**
     * Constructor
     */
    protected YPersistenceManager(SessionFactory factory) {
        logger = Logger.getLogger(this.getClass());

        objectsToStore = new Hashtable(5);        // Arbitary initial size
        objectsToUpdate = new Hashtable(5);
        objectsToDelete = new Hashtable(5);

        setFactory(factory);
    }

    protected Session getSession() {
        return session;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    private void closeSession(Session session) {
        try {
            if ((session != null) && (session.isOpen())) {
                session.close();
            }
        } catch (HibernateException e) {
            logger.error("Failure to close Hibernate session", e);
        }
    }

    /**
     * Start a new Hibernate session.<P>
     *
     * @throws YPersistenceException
     */
    protected void startTransactionalSession() throws YPersistenceException {
        try {
            session = getFactory().openSession();
            transaction = session.beginTransaction();
        } catch (HibernateException e) {
            logger.fatal("Failure to start transactional session", e);
            throw new YPersistenceException("Failure to start transactional session", e);
        }
    }

    /**
     * Adds an object to be stored into the lazy storage cache.<P>
     * <p/>
     * Note: We use Hashtable rather than Hashmap or anything from collections framework as ideally all of
     * persistence needs to be thread safe.
     *
     * @param obj The object to be persisted
     */
    protected void storeObject(Object obj) throws YPersistenceException {
        if (!restoring) {
            Object objToStore = obj;

            if (obj instanceof YIdentifier) {
                P_YIdentifier py = createPY(obj);
                objToStore = py;
            } else if (obj instanceof YNetRunner) {
                YNetRunner runner = (YNetRunner) obj;
                P_YIdentifier py = createPY(runner.get_caseIDForNet());
                runner.set_standin_caseIDForNet(py);
            }

            if (logger.isDebugEnabled()) {
                String key = getHibernateIdentifier(objToStore);
                logger.debug("Adding to insert cache: Type=" + objToStore.getClass().getName() + " ID=" + key);
            }

            if (LAZY_UPDATES) {
                objectsToStore.put(getHibernateIdentifier(objToStore), objToStore);
            } else {
                doPersistAction(objToStore, false);
            }
        }
    }

    /**
     * Causes the supplied object to be persisted when the current transaction is commited.<P>
     *
     * @param obj The object to be persisted
     */
    protected void updateObject(Object obj) throws YPersistenceException {
        Object objToStore = obj;

        if (!restoring) {
            if (obj instanceof YIdentifier) {
                P_YIdentifier py = createPY(obj);
                objToStore = py;
            } else if (obj instanceof YNetRunner) {
                YNetRunner runner = (YNetRunner) obj;
                P_YIdentifier py = createPY(runner.get_caseIDForNet());
                runner.set_standin_caseIDForNet(py);
            }

            if (logger.isDebugEnabled()) {
                String key = getHibernateIdentifier(objToStore);
                logger.debug("Adding to update cache: Type=" + objToStore.getClass().getName() + " ID=" + key);
            }

            if (LAZY_UPDATES) {
                objectsToUpdate.put(getHibernateIdentifier(objToStore), objToStore);
            } else {
                doPersistAction(objToStore, true);
            }
        }
    }

    /**
     * Causes the supplied object to be persisted when the current transaction is commited.<P>
     *
     * This method simply calls {@link this.updateObject(Object)} but is public in scope. Ideally we need to dump this
     * method assuming we can refector all persistence dependant classes to be within the engine package.

     *
     * @param obj
     */
    public void storeObjectFromExternal(Object obj) throws YPersistenceException {
        storeObject(obj);
    }

    /**
     * Causes the supplied object to be updated within the persistence cache when the current transaction is
     * commited.<P>
     *
     * This method simply calls {@link this.updateObject(Object)} but is public in scope. Ideally we need to dump this
     * method assuming we can refector all persistence dependant classes to be within the engine package.
     *
     * @param obj
     */
    public void updateObjectExternal(Object obj) throws YPersistenceException {
        updateObject(obj);
    }


    /**
     * Causes the supplied object to be removed from the persistence cache when the current transaction is commited.<P>
     *
     * This method simply calls {@link this.updateObject(Object)} but is public in scope. Ideally we need to dump this
     * method assuming we can refector all persistence dependant classes to be within the engine package.

     * @param obj
     * @throws YPersistenceException
     */
    protected void deleteObject(Object obj) throws YPersistenceException {

        if (logger.isDebugEnabled()) {
            String key = getHibernateIdentifier(obj);
            logger.debug("Adding to delete cache: Type=" + obj.getClass().getName() + " ID=" + key);
        }
        try {
            if (LAZY_UPDATES) {
                objectsToDelete.put(getHibernateIdentifier(obj), obj);
            } else {
                getSession().delete(obj);
                getSession().flush();
            }
        } catch (HibernateException e) {
             logger.error("Failed to delete - " + e.getMessage());
        }

        try
        {
            getSession().evict(obj);
        }
        catch (HibernateException e)
        {
            // ignore !!!
        }


    }

    protected void commit() throws YPersistenceException {
        logger.debug("--> commit");

        if (logger.isDebugEnabled() && OPTIMIZED_CACHING) {
            dump();
        }

        if (OPTIMIZED_CACHING) {
            /**
             * Pre-process the cache prior to committing via Hibernate.
             *
             * Deletions:
             * If the same object+key exists in either the insert or update cache, thow an exception.
             *
             * Inserts:
             * If the same object+key exists within the update cache :-
             *  a] Take the version from the update cache and copy over the version in the insert cache
             *  b] Remove from the update cache
             */

            // Check for deletions+insert/updates
            {
                Enumeration enumeration = objectsToDelete.keys();

                while (enumeration.hasMoreElements()) {
                    String key = (String) enumeration.nextElement();
                    if (objectsToStore.containsKey(key)) {
                        throw new YPersistenceException("Object with key [" + key + "] present in both Delete and Insert cache");
                    }
                    if (objectsToUpdate.containsKey(key)) {
                        throw new YPersistenceException("Object with key [" + key + "] present in both Delete and Update cache");
                    }
                }
            }

            // Check for insert and updates
            {
                Enumeration enumeration = objectsToUpdate.keys();

                while (enumeration.hasMoreElements()) {
                    String key = (String) enumeration.nextElement();
                    if (objectsToStore.containsKey(key)) {
                        objectsToStore.put(key, objectsToUpdate.get(key));
                        objectsToUpdate.remove(key);
                    }
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("POST DUPLICATE CHECK");
                dump();
            }
        }

        try
        {
            if (LAZY_UPDATES)
            {
                // Action object deletions
                {
                    Enumeration enumeration = objectsToDelete.elements();
                    while (enumeration.hasMoreElements()) {
                        removeData(enumeration.nextElement());
                    }
                }

                // Action object insertions - Note that we MUST insert the identifers first to cater for database RI rules
                {
                    {
                        Enumeration enumeration = objectsToStore.elements();
                        while (enumeration.hasMoreElements()) {
                            Object obj = enumeration.nextElement();
                            if (obj instanceof P_YIdentifier) {
                                storeData(obj);
                            }
                        }
                    }
                    {
                        Enumeration enumeration = objectsToStore.elements();
                        while (enumeration.hasMoreElements()) {
                            Object obj = enumeration.nextElement();
                            if (obj instanceof P_YIdentifier) {
                                // Null action
                            } else {
                                storeData(obj);
                            }
                        }

                    }
                }

                // Action object updates
                {
                    Enumeration enumeration = objectsToUpdate.elements();
                    while (enumeration.hasMoreElements()) {
                        updateData(enumeration.nextElement());
                    }
                }
            }

            // Commit
            getTransaction().commit();
            
        } catch (Exception e1) {
            logger.fatal("Failure to commit transactional session - Rolling Back Transaction", e1);

            try {
                getTransaction().rollback();
            } catch (Exception e2) {
                throw new YPersistenceException("Failure to rollback transactional session", e2);
            }
            throw new YPersistenceException("Failure to commit transactional session", e1);
        } finally {
            transaction = null;
            if (getSession() != null) {
                try {
                    getSession().close();
                } catch (HibernateException e) {
                    logger.warn("Failure to tidy close Hibernate session", e);
                }
            }
            session = null;
        }

        logger.debug("<-- commit");
    }

    /**
     * Forces a rollback of the current transaction,<P>
     */
    protected void rollbackTransaction() throws YPersistenceException {
        logger.debug("--> rollback Transaction");

        if (getTransaction() == null) {
            // Nothing to do ???
        } else {
            try {
                getTransaction().rollback();
            } catch (HibernateException e) {
                throw new YPersistenceException("Failure to rollback transaction", e);
            } finally {
                transaction = null;
                if (getSession() != null) {
                    try {
                        getSession().close();
                    } catch (HibernateException e) {
                        logger.warn("Failure to tidy close Hibernate session", e);
                    }
                }
                session = null;
            }
        }

        logger.debug("<-- rollback Transaction");
    }

    /**
     * Causes the supplied object to be removed from the persistence cache when the current transaction is
     * commited.<P>
     *
     * This method simply calls {@link this.removeData(Object)} but is public in scope. Ideally we need to dump this
     * method assuming we can refector all persistence dependant classes to be within the engine package.
     *
     * @param obj
     */
    public void removeDataFromExternal(Object obj) throws YPersistenceException {
        removeData(obj);
    }


    private void removeData(Object obj) throws YPersistenceException {
        logger.debug("--> removeData: " + obj);

        try {
            getSession().delete(obj);
        } catch (HibernateException e) {
            logger.error("Failure whilst removing persisted data", e);
            throw new YPersistenceException("Failure whilst removing persisted data", e);
        }
        logger.debug("<-- removeData");
    }

    private void storeData(Object obj) throws YPersistenceException {
        doPersistAction(obj, false);
    }

    private void updateData(Object obj) throws YPersistenceException {
        doPersistAction(obj, true);
    }

    public Query createQuery(String queryString) throws YPersistenceException {
        Query query = null;

        try {
            query = getSession().createQuery(queryString);
        } catch (HibernateException e) {
            throw new YPersistenceException("Failure to create Hibernate query object", e);
        }

        return query;
    }

//    public QueryResults executeQuery(String queryString) throws YPersistenceException
//    {
//        QueryResults results = new QueryResults();
//        Query query = null;
//        Session session = null;
//        Iterator iter = null;
//
//        try
//        {
//            session = getFactory().openSession();
//            session.setFlushMode(FlushMode.NEVER);
//            query = session.createQuery(queryString);
//            results.setQuery(query);
//            results.setSession(session);
//            return results;
//
//        }
//        catch (HibernateException e)
//        {
//            throw new YPersistenceException("Failure to create Hibernate query object", e);
//        }
//    }


    private void doPersistAction(Object obj, boolean update) throws YPersistenceException {
        if (logger.isDebugEnabled()) {
            logger.debug("--> doPersistAction: Object=" + obj.getClass().getName() + " Key=" + getHibernateIdentifier(obj) + (update ? " Mode=Update" : " Mode=Create"));
        }
        try {
            if (update) {
                getSession().update(obj);
                getSession().flush();
            } else {
                getSession().save(obj);
                getSession().flush();
            }
        } catch (Exception e) {
            logger.error("Failure detected whilst persisting instance of " + obj.getClass().getName(), e);
        }

        try
        {
            getSession().evict(obj);
        }
        catch (HibernateException e)
        {
            logger.warn("Failure whilst evicting object from Hibernate session cache", e);
        }
    }

    protected P_YIdentifier createPY(Object obj) {
        YIdentifier yid = (YIdentifier) obj;
        P_YIdentifier py = new P_YIdentifier();
        py.set_idString(yid.get_idString());
        List list = yid.get_children();
        List plist = new Vector();

        py.setLocationNames(yid.getLocationNames());

        /*
          The children must be p_yidentifiers instead of normal yidentifiers
        */
        for (int i = 0; i < list.size(); i++) {
            YIdentifier child = (YIdentifier) list.get(i);
            plist.add(createPY(child));
        }
        py.set_children(plist);

        return py;
    }

    protected static SessionFactory initialise(boolean journalising) throws YPersistenceException {
        String loggerName = "au.edu.qut.yawl.engine.YPersistenceManager";
        SessionFactory factory = null;
        Connection con = null;
        Transaction tx = null;
        Configuration cfg = null;

        // Create the Hibernate config, check and create database if required, and generally set things up .....
        if (journalising) {
            try {
                cfg = new Configuration();
                cfg.addClass(YSpecFile.class);
                cfg.addClass(YNetRunner.class);
                cfg.addClass(YWorkItem.class);
                cfg.addClass(P_YIdentifier.class);
                cfg.addClass(YCaseData.class);
                cfg.addClass(au.edu.qut.yawl.logging.YWorkItemDataEvent.class);
                cfg.addClass(au.edu.qut.yawl.logging.YWorkItemEvent.class);
                cfg.addClass(au.edu.qut.yawl.logging.YCaseEvent.class);
                cfg.addClass(YAWLServiceReference.class);

                cfg.addClass(au.edu.qut.yawl.admintool.model.Resource.class);
                cfg.addClass(au.edu.qut.yawl.admintool.model.Role.class);
                cfg.addClass(au.edu.qut.yawl.admintool.model.HumanResourceRole.class);
                cfg.addClass(au.edu.qut.yawl.admintool.model.Capability.class);
                cfg.addClass(au.edu.qut.yawl.admintool.model.ResourceCapability.class);
                cfg.addClass(au.edu.qut.yawl.admintool.model.OrgGroup.class);
                cfg.addClass(au.edu.qut.yawl.admintool.model.Position.class);
                cfg.addClass(au.edu.qut.yawl.admintool.model.HResOccupiesPosition.class);
                cfg.addClass(au.edu.qut.yawl.exceptions.Problem.class);

                factory = cfg.buildSessionFactory();

                Logger.getLogger(loggerName).debug("Validating existance of database for persistence");

                new SchemaUpdate(cfg).execute(false, true);

//                boolean createtables = false;
//
//                /*
//                  Execute a select statement to see if tables are there
//                 */
//                Session session = factory.openSession();
//
//                //AJH: See if we can establish a connection
//                try {
//                    con = session.connection();
//                } catch (Exception e) {
//                    String msg = "Failure to establish connection to persistance database";
//                    Logger.getLogger(loggerName).fatal(msg, e);
//                    throw new YPersistenceException(msg, e);
//                }
//
//                //AJH: Validate database connection by Selecting some data
//                //todo Need a better database validation mechanism here
//                Statement st = null;
//                ResultSet rs = null;
//                try {
//                    tx = session.beginTransaction();
//                    st = session.connection().createStatement();
//                    rs = st.executeQuery("select * from specs");
//                    tx.commit();
//                } catch (Exception e) {
//                    if (tx != null) {
//                        tx.rollback();
//                    }
//                    Logger.getLogger(loggerName).warn("Database does not appear to exist - Attempting to create new database ...");
//                    new SchemaUpdate(cfg).execute(false, true);
//                    HumanResourceRole.addIntegrityEnforcements(session);
//
//                } finally {
//                    session.close();
//                }

            } catch (Exception e) {
                e.printStackTrace();
                Logger.getLogger(loggerName).fatal("Failure initialising persistence layer", e);
                throw new YPersistenceException("Failure initialising persistence layer", e);
            }
        }
        // Finally, return the session factory
        return factory;
    }



    public int getNextCaseNbr() {
        Session session = null;
        int lastCaseNbr = 0 ;                                 // default starting case
        try {
            session = factory.openSession();

            Query query = session.createQuery(
                                "select from YCaseEvent order by eventtime desc");
            if (query != null) {
                if (! query.list().isEmpty()) {
                    YCaseEvent caseEvent = (YCaseEvent) query.iterate().next();
                    lastCaseNbr = Integer.parseInt(caseEvent.get_caseID());
                }    
            }
            session.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ++lastCaseNbr ;
    }

    public SessionFactory getFactory() {
        return factory;
    }

    private void setFactory(SessionFactory factory) {
        this.factory = factory;
    }

    /**
     * Derives a storage key for persistable objects. This key is used as a hashtable key and
     * is based upon the object type being persisted together with its Hibernate identifier.
     *
     * @param obj
     * @return
     * @throws YPersistenceException
     */
    private String getHibernateIdentifier(Object obj) throws YPersistenceException {
        String key = null;
        int objectType = 0;

        if (obj instanceof P_YIdentifier) {
            objectType = PT_IDENT;
            P_YIdentifier obj2 = (P_YIdentifier) obj;
            key = objectType + "/" + obj2.get_idString();
        } else if (obj instanceof YCaseData) {
            objectType = PT_CASE_DATA;
            YCaseData obj2 = (YCaseData) obj;
            key = obj2.getId();
        } else if (obj instanceof YWorkItemDataEvent) {
            objectType = PT_LOG_DATA;
            key = objectType + "/" + new Date().getTime();
        } else if (obj instanceof YCaseEvent) {
            objectType = PT_LOG_IDENT;
            YCaseEvent obj2 = (YCaseEvent) obj;
            key = obj2.get_caseID();
        } else if (obj instanceof YNetRunner) {
            objectType = PT_NET_RUNNER;
            YNetRunner obj2 = (YNetRunner) obj;
            key = objectType + "/" + obj2.get_caseID();
        } else if (obj instanceof YSpecFile) {
            objectType = PT_SPEC_FILE;
            YSpecFile obj2 = (YSpecFile) obj;
            key = objectType + "/" + obj2.getId();
        } else if (obj instanceof YWorkItem) {
            objectType = PT_WORK_ITEM;
            YWorkItem obj2 = (YWorkItem) obj;
            //todo (by LJA) shouldn't the workitem use getUniqueID() ??
            key = objectType + "/" + obj2.get_thisID();
        } else if (obj instanceof YWorkItemEvent) {
            objectType = PT_WORK_ITEM_EVENT;
            YWorkItemEvent obj2 = (YWorkItemEvent) obj;
            key = obj2.get_workItemEventID();
        } else if (obj instanceof YAWLServiceReference) {
            objectType = PT_SERVICE_REF;
            YAWLServiceReference obj2 = (YAWLServiceReference) obj;
            key = objectType + "/" + obj2.get_yawlServiceID();
        } else if (obj instanceof User) {
            objectType = PT_USER;
            User obj2 = (User) obj;
            key = obj2.getUserID();
        } else {
            logger.error("Unknown object type [" + obj.getClass().getName() + "]");
            throw new YPersistenceException("Unknown object type [" + obj.getClass().getName() + "]");
        }

        return key;
    }

    public boolean isRestoring() {
        return restoring;
    }

    protected void setRestoring(boolean restoring) {
        this.restoring = restoring;
    }

    private void dump() {
        logger.debug("*** DUMP OF PERSISTENCE_MANAGER CACHES STARTS ***");

        if (objectsToStore.size() > 0) {
            logger.debug("*** INSERTIONS ***");
            {
                Enumeration enumeration = objectsToStore.keys();
                while (enumeration.hasMoreElements()) {
                    String key = (String) enumeration.nextElement();
                    logger.debug(objectsToStore.get(key).getClass().getName() + " " + key);
                }
            }
        }

        if (objectsToUpdate.size() > 0) {
            logger.debug("*** UPDATES ***");
            {
                Enumeration enumeration = objectsToUpdate.keys();
                while (enumeration.hasMoreElements()) {
                    String key = (String) enumeration.nextElement();
                    logger.debug(objectsToUpdate.get(key).getClass().getName() + " " + key);
                }
            }
        }

        if (objectsToDelete.size() > 0) {
            logger.debug("*** DELETIONS ***");
            {
                Enumeration enumeration = objectsToDelete.keys();
                while (enumeration.hasMoreElements()) {
                    String key = (String) enumeration.nextElement();
                    logger.debug(objectsToDelete.get(key).getClass().getName() + " " + key);

                }
            }
        }
        logger.debug("*** DUMP OF PERSISTENCE_MANAGER CACHES ENDS ***");
    }
}

