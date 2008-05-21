/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.datastore;

import org.apache.log4j.Logger;
import org.hibernate.*;
import org.hibernate.cfg.Configuration;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;

import java.util.ArrayList;
import java.util.List;


/**
 *  This singleton class provides db & persistence support via Hibernate.
 *
 *  @author Michael Adams
 *  v0.1, 03/08/2007
 *
 *  last update: 07/04/2008 (for v2.0)
 */

public class HibernateEngine {

    // persistence actions
    public static final int DB_UPDATE = 0;
    public static final int DB_DELETE = 1;
    public static final int DB_INSERT = 2;

    // bulk transaction options
    private List bulkInserts = new ArrayList();
    private List bulkUpdates = new ArrayList();
    private List bulkDeletes = new ArrayList();
    private boolean bulkMode = false;

    // reference to Hibernate
    private static SessionFactory _factory = null;

    // instance reference
    private static HibernateEngine _me;

    private static boolean _persistOn = false;
    private final Object _mutex = new Object();
    private static final Logger _log = Logger.getLogger(HibernateEngine.class);

    // table name abbreviations
    private static final String _pkg = "org.yawlfoundation.yawl.resourcing." ;
    public static final String tblParticipant = _pkg + "resource.Participant";
    public static final String tblRole = _pkg + "resource.Role";
    public static final String tblCapability = _pkg + "resource.Capability";
    public static final String tblPosition = _pkg + "resource.Position";
    public static final String tblOrgGroup = _pkg + "resource.OrgGroup";
    public static final String tblUserPrivileges = _pkg + "resource.UserPrivileges";
    public static final String tblWorkQueue = _pkg + "WorkQueue";
    public static final String tblEventLog = _pkg + "datastore.eventlog.ResourceEvent";

    /*********************************************************************************/

    // Constructors and Initialisation //
    /***********************************/

    /** The constuctor - called from getInstance() */
    private HibernateEngine(boolean persistenceOn) throws HibernateException {
        _persistOn = persistenceOn;
        initialise();
    }


    public HibernateEngine() { getInstance(true) ; }


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
    public void initialise() throws HibernateException {
        try {
            Configuration _cfg = new Configuration();

            // add each persisted class to config
            _cfg.addClass(org.yawlfoundation.yawl.resourcing.resource.Participant.class);
            _cfg.addClass(org.yawlfoundation.yawl.resourcing.resource.Role.class);
            _cfg.addClass(org.yawlfoundation.yawl.resourcing.resource.Capability.class);
            _cfg.addClass(org.yawlfoundation.yawl.resourcing.resource.Position.class);
            _cfg.addClass(org.yawlfoundation.yawl.resourcing.resource.OrgGroup.class);
            _cfg.addClass(org.yawlfoundation.yawl.resourcing.resource.UserPrivileges.class);
            _cfg.addClass(org.yawlfoundation.yawl.resourcing.WorkQueue.class);
            _cfg.addClass(org.yawlfoundation.yawl.resourcing.ResourceMap.class);
            _cfg.addClass(org.yawlfoundation.yawl.engine.interfce.WorkItemRecord.class);
            _cfg.addClass(
                    org.yawlfoundation.yawl.resourcing.datastore.eventlog.ResourceEvent.class);

           // get a session context
            _factory = _cfg.buildSessionFactory();

            // check tables exist and are of a matching format to the persisted objects
            new SchemaUpdate(_cfg).execute(false, true);

        }
        catch (MappingException me) {
            _log.error("Could not initialise database connection.", me);
        }
    }


    /** @return true if a table of 'tableName' currently exists */
    public boolean isAvailable(String tableName) {
        return (getObjectsForClass(tableName) != null);
    }


    /** @return true if this instance is persisting */
    public boolean isPersisting() {
        return _persistOn;
    }


    /******************************************************************************/

    // Persistence Methods //
    /***********************/

    /**
     * persists the object instance passed
     * @param obj - an instance of the object to persist
     * @param action - type type of action performed
     */
    public void exec(Object obj, int action) {

        Transaction tx = null;
        try {
            Session session = _factory.getCurrentSession();
            tx = session.beginTransaction();
  //          session.flush();

            if (action == DB_INSERT) session.save(obj);
            else if (action == DB_UPDATE) updateOrMerge(session, obj);
            else if (action == DB_DELETE) session.delete(obj);

            tx.commit();
        }
        catch (HibernateException he) {
            _log.error("Error persisting object (" + actionToString(action) +
                    "): " + obj.toString(), he);
            if (tx != null) tx.rollback();
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


    /**
     * executes a Query object based on the sql string passed
     * @param queryString - the sql query to execute
     * @return the List of objects returned, or null if the query has some problem
     */
    private List execQuery(String queryString) {

        List result = null;
        Transaction tx = null;
        try {
            Session session = _factory.getCurrentSession();
            tx = session.beginTransaction();
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

        int result = -1;
        Transaction tx = null;
        try {
            Session session = _factory.getCurrentSession();
            tx = session.beginTransaction();
            result = session.createQuery(queryString).executeUpdate();
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
