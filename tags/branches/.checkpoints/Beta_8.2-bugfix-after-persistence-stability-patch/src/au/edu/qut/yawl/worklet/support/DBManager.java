/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.worklet.support;

import net.sf.hibernate.cfg.Configuration;
import net.sf.hibernate.*;
import net.sf.hibernate.mapping.PersistentClass;
import net.sf.hibernate.tool.hbm2ddl.SchemaUpdate;

import java.util.*;
import java.sql.Statement;

import org.apache.log4j.Logger;


/**
 *  The DBManager class provides persistence support for the Worklet Service.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.8, 04-09/2006
 */

public class DBManager {

    // persistence actions
    public static final int DB_UPDATE = 0;
    public static final int DB_DELETE = 1;
    public static final int DB_INSERT = 2;

    private static boolean _persistOn = false;
    private static Logger _log = null;
    private Configuration _cfg = null;
    private static SessionFactory _factory = null;
    private static DBManager _me;


    /** The constuctor - called from getInstance() */
    private DBManager(boolean persistenceOn) throws HibernateException {
         _persistOn = persistenceOn;
        _log = Logger.getLogger(this.getClass());
        initialise();
    }

    /** initialises the worklet tables in the database */
     public void initialise() throws HibernateException {
        if (_persistOn) {
            try {
                _cfg = new Configuration();

                // add each persisted worklet class to config
                _cfg.addClass(au.edu.qut.yawl.worklet.selection.CheckedOutItem.class);
                _cfg.addClass(au.edu.qut.yawl.worklet.selection.CheckedOutChildItem.class);
                _cfg.addClass(au.edu.qut.yawl.worklet.admin.AdministrationTask.class);
                _cfg.addClass(au.edu.qut.yawl.worklet.exception.CaseMonitor.class);
                _cfg.addClass(au.edu.qut.yawl.worklet.exception.HandlerRunner.class);
                _cfg.addClass(au.edu.qut.yawl.worklet.support.WorkletEvent.class);

                _factory = _cfg.buildSessionFactory();

                // build tables if necessary
                if (! allTablesExist())
                    new SchemaUpdate(_cfg).execute(false, true);
            }
            catch (MappingException me) {
                _log.error("Could not initialise database connection.", me);
            }
        }
    }


    /** returns the current DBManager instance */
    public static DBManager getInstance(boolean persistenceOn) {
        if (_me == null) {
            try {
                _me = new DBManager(persistenceOn);
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
        return _persistOn ;
    }


    /**
     * executes a query against the database
     * @param query  - the sql query to execute
     * @param action - the type of action performed
     */
    public void persist(String query, int action) {
        if (_persistOn) {
            try {
                Session session = _factory.openSession();
                Transaction tx = session.beginTransaction();
                switch (action) {
                    case DB_UPDATE : session.update(query); break;
                    case DB_DELETE : session.delete(query); break;
                    case DB_INSERT : session.save(query);   break;
                }
                session.flush();
                tx.commit();
                session.close();
            } catch (HibernateException he) {
                _log.error("Error persisting query: " + query, he);
            }
        }
    }

    /**
     * persists the object instance passed
     * @param obj - an instance of the object to persist
     * @param action - type type of action performed
     */
    public void persist(Object obj, int action) {
        if (_persistOn) {
            try {
                Session session = _factory.openSession();
                Transaction tx = session.beginTransaction();
                switch (action) {
                    case DB_UPDATE : session.update(obj); break;
                    case DB_DELETE : session.delete(obj); break;
                    case DB_INSERT : session.save(obj);   break;
                }
                session.flush();
                tx.commit();
                session.close();
            } catch (HibernateException he) {
                _log.error("Error persisting object (" + actionToString(action) +
                           "): " + obj.toString(), he);
            }
        }
    }


    /**
     * creates a Query object based on the sql string passed
     * @param queryString - the sql query to execute
     * @return the created Query
     */
    private Query createQuery(String queryString) {
        Query query = null;
        if (_persistOn) {
            try {
                query = _factory.openSession().createQuery(queryString);
            } catch (HibernateException he) {
                _log.error("Error creating query: " + queryString, he);
            }
        }
        return query;
    }


    /**
     * returns all the instances currently persisted for the class passed
     * @param className - the name of the class to retrieve instances of
     * @return a List of the instances retrieved
     */
    public List getObjectsForClass(String className) {
        List result = null;
        try {
            Query query = createQuery("from " + className);
            if (query != null) result = query.list();
        }
        catch (HibernateException he) {
            _log.error("Error restoring data for class: " + className, he);
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
            case DB_UPDATE : result = "update"; break;
            case DB_DELETE : result = "delete"; break;
            case DB_INSERT : result = "insert"; break;
        }
        return result ;
    }

}  // end DBManager.
