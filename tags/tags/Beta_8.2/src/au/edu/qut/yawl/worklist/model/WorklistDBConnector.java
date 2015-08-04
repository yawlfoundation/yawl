/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.worklist.model;

import au.edu.qut.yawl.exceptions.YQueryException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.Problem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.cfg.Configuration;

/**
 *
 * @author Lachlan Aldred
 * Date: 20/05/2005
 * Time: 19:02:27
 */
public class WorklistDBConnector {
    private SessionFactory _factory;
    private static WorklistDBConnector _myInstance;
    public static final int UPDATE_OPERATION = 1;
    public static final int DELETE_OPERATION = 2;
    public static final int SAVE_OPERATION = 3;



    public static WorklistDBConnector getInstance() throws HibernateException {
        if (null == _myInstance) {
            _myInstance = new WorklistDBConnector();
        }
        return _myInstance;
    }

    private WorklistDBConnector() throws HibernateException {
        Configuration cfg = new Configuration();
        _factory = cfg.buildSessionFactory();
    }


    /**
     * Executes a query over the organisation model of the YAWL system.
     * @param query
     * @return a list of hresid that satisfy the query (as strings).
     */
    public List whichUsersForThisQuery(String query) throws SQLException, YQueryException, HibernateException {
        List users = new ArrayList();

        ResultSet rs = executeQuery(query);
        while (rs.next()) {
            String user = rs.getString("hresid");
            //System.out.println("\tuser = " + user);
            if (null == user) {
                throw new YQueryException(
                        "Something went wrong with the query inside the YAWL Process " +
                        "Specification:\n" +
                        "The worklist executed query [" + query + "] over the " +
                        "organisational model and this yielded a improperly typed " +
                        "query result.");
            }
            users.add(user);
        }

        return users;
    }

    public ResultSet executeQuery(String query) throws SQLException, HibernateException {
        Connection conn = getConnection();
        Statement stmnt = conn.createStatement();
        ResultSet rs = stmnt.executeQuery(query);
        conn.close();
        return rs;
    }

    private Connection getConnection() throws HibernateException {
        Session session = _factory.openSession();
        return session.connection();
    }

    public int executeUpdate(String sql) throws SQLException, HibernateException {
        Connection conn = getConnection();
        Statement statement = conn.createStatement();
        int result = statement.executeUpdate(sql);
        conn.close();
        return result;
    }

    public void saveWarning(Problem warning) throws YPersistenceException {
        doPersistAction(warning, SAVE_OPERATION);
    }

    private void doPersistAction(Object obj, int operation) throws YPersistenceException {
        try {
            Session session = _factory.openSession();
            Transaction tx = session.beginTransaction();
            if (UPDATE_OPERATION == operation) {
                session.update(obj);
            } else if (DELETE_OPERATION == operation) {
                session.delete(obj);
            } else if (SAVE_OPERATION == operation) {
                session.save(obj);
            }
            session.flush();
            session.evict(obj);
            tx.commit();
            session.close();
        } catch (HibernateException e) {
            throw new YPersistenceException("Hibernate problem: " + e.getMessage(), e);
        }
    }

}
