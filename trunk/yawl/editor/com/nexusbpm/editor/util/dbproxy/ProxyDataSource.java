/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.util.dbproxy;

import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.sql.DataSource;

import au.edu.qut.yawl.exceptions.YAWLException;

/**
 * Used when database connection information is supplied instead instead of
 * the JNDI name of a <code>DataSource</code>.
 * Supports only a single <code>Connection</code> per JVM for Redbrick.
 */
public class ProxyDataSource implements DataSource, Serializable {
	/**
	 * @param driverClassName
	 * @param connectionURL
	 * @param userName
	 * @param password
	 */
	public ProxyDataSource( String driverClassName, String connectionURL, 
		String userName, String password ) throws YAWLException {
		if( driverClassName == null ) throw new YAWLException( "driverClassName == null" );
		try {
			Class.forName( driverClassName );
		}//try
		catch( ClassNotFoundException e ) {
			throw new YAWLException( e );
		}//catch 
		_driverClassName = driverClassName;
		_connectionURL = connectionURL;
		_userName = userName;
		_password = password;
		if( DatabaseProxyBean.REDBRICK_DRIVER.equals( _driverClassName ) ) {
			_redbrickReferenceCount++;
		}//if
	}//ProxyDataSource()
	private String _driverClassName;
	private String _connectionURL;
	private String _userName;
	private String _password;
	/**
	 * @see javax.sql.DataSource#getLoginTimeout()
	 */
	public int getLoginTimeout() throws SQLException {
		return DriverManager.getLoginTimeout();
	}//getLoginTimeout()
	/**
	 * @see javax.sql.DataSource#setLoginTimeout(int)
	 */
	public void setLoginTimeout( int seconds ) throws SQLException {
		DriverManager.setLoginTimeout( seconds );
	}//setLoginTimeout()
	/**
	 * @see javax.sql.DataSource#getLogWriter()
	 */
	public PrintWriter getLogWriter() throws SQLException {
		return DriverManager.getLogWriter();
	}//getLogWriter()
	/**
	 * @see javax.sql.DataSource#setLogWriter(java.io.PrintWriter)
	 */
	public void setLogWriter( PrintWriter out ) throws SQLException {
		DriverManager.setLogWriter( out );
	}//setLogWriter()
	/**
	 * @see javax.sql.DataSource#getConnection()
	 */
	public Connection getConnection() throws SQLException {
		try {
			Class.forName( _driverClassName );
		}//try
		catch( ClassNotFoundException e ) {
			throw new RuntimeException( e );
		}//catch 
		Connection connection = null;
		if( DatabaseProxyBean.REDBRICK_DRIVER.equals( _driverClassName ) ) {
			synchronized( DatabaseProxyBean.REDBRICK_DRIVER ) {
				if( _redbrickConnection == null ) {
					_redbrickConnection = DriverManager.getConnection(
						_connectionURL, _userName, _password );
				}//if
			}//synchronized
			connection = _redbrickConnection;
		}//if
		else {
			if( _connection == null ) {
				for( Enumeration e = DriverManager.getDrivers(); e.hasMoreElements(); ) {
					DatabaseProxyBean.LOG.debug( e.nextElement() );
				}//for
				_connection = DriverManager.getConnection( _connectionURL, _userName, _password );
			}//if
			connection = _connection;
		}//else
		return new ProxyConnection( connection );
	}//getConnection()
	/**
	 * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
	 */
	public Connection getConnection( String userName, String password ) 
	throws SQLException {
		throw new UnsupportedOperationException("Only getConnection() is supported.");
	}//getConnection()
	/**
	 * The single <code>Connection</code> managed by this <code>DataSource</code>.
	 */
	private Connection _connection = null;
	/**
	 * The single RedBrick <code>Connection</code> per JVM.
	 */
	static private Connection _redbrickConnection = null;
	static private int _redbrickReferenceCount = 0;
	/**
	 * Use this instead of <tt>Connection.close()</tt> to allow proper 
	 * <tt>Connection</tt> management for Redbrick. 
	 * @throws SQLException
	 */
	public void close() throws SQLException {
		if( DatabaseProxyBean.REDBRICK_DRIVER.equals( _driverClassName ) ) {
			synchronized( DatabaseProxyBean.REDBRICK_DRIVER ) {
				_redbrickReferenceCount--;
				if( _redbrickReferenceCount == 0 && _redbrickConnection != null ) {
					DatabaseProxyBean.LOG.info("Shutting down the Redbrick connection.");
					_redbrickConnection.close();
					_redbrickConnection = null;
				}//if
			}//synchronized
		}//if
		else if( _connection != null ) {
			_connection.close();
		}//else if
	}//close()
}