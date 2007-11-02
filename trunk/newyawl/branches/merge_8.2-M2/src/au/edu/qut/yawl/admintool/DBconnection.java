/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */




//package au.edu.qut.yawl.adminTool;
package au.edu.qut.yawl.admintool;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author heijens
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 * @deprecated
 */
public class DBconnection {

	private static Connection connection = null;
	private static final String dbDriver = "org.postgresql.Driver";
	private static final String dbUrl = "jdbc:postgresql:yawl";
	private static final String dbName = "yawl";
	private static final String dbUser = "postgres";
	private static final String dbPassword = "admin";
	
	
	/**
	 * method to open the connection
	 */
	public static boolean getConnection() {
	   boolean isClosed = false;
	   if ( connection == null ){
		   try {
		       connection = DriverManager.getConnection(
								dbUrl, dbUser, dbPassword );
		   } catch( SQLException e ) {
		      System.err.println(
		         "Cannot connect to database: "
		            + "check that postgres database is running and  "
		            + "the YAWLdb exists.");
		   }
	   }
		try {
	    	isClosed = connection.isClosed();
	   } catch (SQLException e) {
		    //	   		 TODO Auto-generated catch block
			e.printStackTrace();
	   }

	   return !isClosed;
	   //	   returns false when the connection isClosed
	}
	
	
	/**
	 * @param driver
	 */
	public static void loadDriver(String driver){
		   try {
		   	Class.forName( driver );
		   } catch( ClassNotFoundException e ) {
		      System.err.println(
		         "Cannot load database driver: for postgres, "
		            + "your classpath must include "
		            + "the postgres driver" );
		   }
	}
	
	
	/**
	 * method to kill the connection to the database
	 */
	public static void killConnection(){
		try {
			connection.close();
		} catch (SQLException e) {
		    //			 TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	
	/**
	 * @return returns a statement object that can be 
	 * used for the SQL-query
	 */
	public static Statement createStatement(){
		Statement statement = null;
		try {
			statement = connection.createStatement();
		} catch (SQLException e) {
		    //			 TODO Auto-generated catch block
			e.printStackTrace();
		}
		return statement;
	}
	
	public static ResultSet getResultSet(Statement statement, String sql){
		ResultSet rs = null;
		try {
			rs = statement.executeQuery(sql);
		} catch (SQLException e) {
		    //			 TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rs;
	}
	
	/**
	 * method to test whether the database connection is working
	 */
	public static void printMetaData(){

	    //		 print info from the driver metadata
		DatabaseMetaData md;

		try {
			md = connection.getMetaData();

			System.out.println("Product name: "
		     + md.getDatabaseProductName() );
		  System.out.println( "Driver name: "
		 + md.getDriverName() );
		} catch (SQLException e) {
		    //			 TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}