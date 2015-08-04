/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine.interfce;

import au.edu.qut.yawl.exceptions.YQueryException;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 
 * @author Lachlan Aldred
 * Date: 20/05/2005
 * Time: 19:02:27
 */
public class DBConnector {
    private Connection _conn;

    public DBConnector() throws ClassNotFoundException, SQLException {
        _conn = getConnection();
    }

    //todo put this in another class - if we ever decide to use it.
    public static synchronized String encrypt(String plaintext) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = null;

        md = MessageDigest.getInstance("SHA"); //step 2
        md.update(plaintext.getBytes("UTF-8")); //step 3

        byte raw[] = md.digest(); //step 4
        String hash = (new BASE64Encoder()).encode(raw); //step 5
        return hash; //step 6
    }



    public Connection getConnection() throws ClassNotFoundException, SQLException {

        Class.forName("org.postgresql.Driver");
        //todo load properties from file
        String dbName = "yawl";
        String dbUserName = "postgres";
        String dbPassword = "admin";

        String url = "jdbc:postgresql:" + dbName;
        Properties props = new Properties();
        props.setProperty("user", dbUserName);
        props.setProperty("password", dbPassword);

        return DriverManager.getConnection(url, props);
    }


    /**
     * Executes a query over the organisation model of the YAWL system.
     * @param query
     * @return
     */
    public List whichUsersForThisQuery(String query) throws SQLException, YQueryException {
        List users = new ArrayList();

        ResultSet rs = executeQuery(query);
        while (rs.next()) {
            String user = rs.getString("hresid");
            if (null == user) {
                throw new YQueryException("" +
                        "Something Wrong with a query inside the YAWL Process Specification:\n" +
                        "The worklist executed query [" + query + "] over the " +
                        "organisational model and this yielded a improperly typed " +
                        "query result.");
            }
            users.add(user);
        }

        return users;
    }

    public ResultSet executeQuery(String query) throws SQLException {
        Statement stmnt = _conn.createStatement();
        ResultSet rs = stmnt.executeQuery(query);
        return rs;
    }

    public int executeUpdate(String sql) throws SQLException {
        Statement statement = _conn.createStatement();
        return statement.executeUpdate(sql);
    }

}
