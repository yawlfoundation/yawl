package au.edu.qut.yawl.resourcing.datastore;

import au.edu.qut.yawl.resourcing.datastore.orgdata.DataSource;
import au.edu.qut.yawl.resourcing.resource.*;
import org.apache.log4j.Logger;
import net.sf.hibernate.cfg.Configuration;
import net.sf.hibernate.*;
import net.sf.hibernate.mapping.PersistentClass;
import net.sf.hibernate.tool.hbm2ddl.SchemaUpdate;

import java.util.Iterator;
import java.util.List;
import java.sql.Statement;

/**
 *  This class provides db & persistence support via Hibernate.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.1, 03/08/2007
 */

public class HibernateEngine {

    // persistence actions
    public static final int DB_UPDATE = 0;
    public static final int DB_DELETE = 1;
    public static final int DB_INSERT = 2;

    private static boolean _persistOn = false;
    private static Logger _log = null;
    private Configuration _cfg = null;
    private static SessionFactory _factory = null;
    private static HibernateEngine _me;

    // shortened table names
    private static final String _pkg = "au.edu.qut.yawl.resourcing." ;
    public static final String tblParticipant = _pkg + "resource.Participant";
    public static final String tblRole = _pkg + "resource.Role";
    public static final String tblCapability = _pkg + "resource.Capability";
    public static final String tblPosition = _pkg + "resource.Position";
    public static final String tblOrgGroup = _pkg + "resource.OrgGroup";
    public static final String tblUserPrivileges = _pkg + "resource.UserPrivileges";
    public static final String tblQueueSet = _pkg + "QueueSet";
    public static final String tblWorkQueue = _pkg + "WorkQueue";


    /** The constuctor - called from getInstance() */
    private HibernateEngine(boolean persistenceOn) throws HibernateException {
        _persistOn = persistenceOn;
        _log = Logger.getLogger(this.getClass());
        initialise();
    }

    public HibernateEngine() {
        getInstance(true) ;
    }

    /** initialises the tables in the database */
     public void initialise() throws HibernateException {
        try {
            _cfg = new Configuration();

            // add each persisted resources class to config
            _cfg.addClass(au.edu.qut.yawl.resourcing.resource.Participant.class);
            _cfg.addClass(au.edu.qut.yawl.resourcing.resource.Role.class);
            _cfg.addClass(au.edu.qut.yawl.resourcing.resource.Capability.class);
            _cfg.addClass(au.edu.qut.yawl.resourcing.resource.Position.class);
            _cfg.addClass(au.edu.qut.yawl.resourcing.resource.OrgGroup.class);
            _cfg.addClass(au.edu.qut.yawl.resourcing.resource.UserPrivileges.class);
            _cfg.addClass(au.edu.qut.yawl.resourcing.QueueSet.class);
            _cfg.addClass(au.edu.qut.yawl.resourcing.WorkQueue.class);
     //       _cfg.addClass(au.edu.qut.yawl.worklist.model.WorkItemRecord.class);


            _factory = _cfg.buildSessionFactory();

            // build tables if necessary
//            if (! allTablesExist())
                new SchemaUpdate(_cfg).execute(false, true);
        }
        catch (MappingException me) {
            _log.error("Could not initialise database connection.", me);
        }
    }


    /** returns the current DBManager instance */
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


    /** iterates through all tables in cfg, tests that they physically exist */
    private boolean allTablesExist() {

        Iterator itr = _cfg.getClassMappings() ;
        while (itr.hasNext()) {
            PersistentClass pClass = (PersistentClass) itr.next() ;
            if (! tableExists(pClass.getTable().getName())) return false ;
        }
        return true;
    }


    /** tests that a table exists by trying to select some data from it
     *  @return true if the table exists
     */
    private boolean tableExists(String tableName) {
        Statement st ;
        Transaction tx = null;
        boolean result = false ;

        try {
            Session session = _factory.openSession();

            //  Execute a select statement to see if tables are there
            try {
                tx = session.beginTransaction();
                st = session.connection().createStatement();
                st.executeQuery("select * from " + tableName);
                tx.commit();
                result = true ;
            }
            catch (Exception e) {
                if (tx != null) {
                    tx.rollback();
                }
            }
            finally {
                session.close();
            }
        }
        catch (HibernateException he) {
            _log.error("Could not create tables for persistence.", he);
        }
        return result ;
    }


    /** returns true if this instance is persisting */
    public boolean isPersisting() {
        return _persistOn;
    }



    /**
     * persists the object instance passed
     * @param obj - an instance of the object to persist
     * @param action - type type of action performed
     */
    public void exec(Object obj, int action) {
       try {
            Session session = _factory.openSession();
            Transaction tx = session.beginTransaction();
            switch (action) {
                case DB_UPDATE: session.update(obj); break;
                case DB_DELETE: session.delete(obj); break;
                case DB_INSERT: session.save(obj);   break;
            }
            session.flush();
            tx.commit();
            session.close();
        } catch (HibernateException he) {
            _log.error("Error persisting object (" + actionToString(action) +
                           "): " + obj.toString(), he);
        }
    }


    /**
     * creates a Query object based on the sql string passed
     * @param queryString - the sql query to execute
     * @return the created Query
     */
    private Query createQuery(String queryString) throws HibernateException {
        return _factory.openSession().createQuery(queryString);
    }


    /**
     * executes a Query object based on the sql string passed
     * @param queryString - the sql query to execute
     * @return the List of objects returned
     */
    private List execQuery(String queryString) {
        try {
             Query query = createQuery(queryString);
             if (query != null) return query.list();
        } catch (HibernateException he) {
             _log.error("Error executing query: " + queryString, he);
        }
        return null;
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
     *         called 'field', an obejct with an key field value of 'value'
     */
    public List execJoinQuery(String table, String field, String value) {
        String qry = String.format("from %s parent where '%s' in elements(parent.%s)",
                                    table, value, field) ;
        return execQuery(qry) ;
    }



    /**
     * creates a Query object based on the sql string passed
     * @param className - the sql query to execute
     * @return the created Query
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
            Query query = createQuery(qry);
            if (query != null) result = query.list();
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
