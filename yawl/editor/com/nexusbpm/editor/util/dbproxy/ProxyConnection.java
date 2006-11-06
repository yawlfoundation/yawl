/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.util.dbproxy;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Map;

/**
 * The <code>Connection</code> returned by a <code>ProxyDataSource</code>.
 * It is closed in <code>ejbRemove()</code>, not its <code>close()</code>
 * method. 
 */
class ProxyConnection implements Connection {
	public ProxyConnection( Connection connection ) {
		_connection = connection;
	}//ProxyConnection()
	private Connection _connection; 
	/**
	 * @see java.sql.Connection#getHoldability()
	 */
	public int getHoldability() throws SQLException {
		return _connection.getHoldability();
	}//getHoldability()
	/**
	 * @see java.sql.Connection#getTransactionIsolation()
	 */
	public int getTransactionIsolation() throws SQLException {
		return _connection.getTransactionIsolation();
	}//getTransactionIsolation()
	/**
	 * @see java.sql.Connection#clearWarnings()
	 */
	public void clearWarnings() throws SQLException {
		_connection.clearWarnings();
	}//clearWarnings()
	/**
	 * @see java.sql.Connection#close()
	 */
	public void close() throws SQLException {
		// Close the connection in ejbRemove().
		// _connection.close();
	}//close()
	/**
	 * @see java.sql.Connection#commit()
	 */
	public void commit() throws SQLException {
		_connection.commit();
	}//commit()
	/**
	 * @see java.sql.Connection#rollback()
	 */
	public void rollback() throws SQLException {
		_connection.rollback();
	}//rollback()
	/**
	 * @see java.sql.Connection#getAutoCommit()
	 */
	public boolean getAutoCommit() throws SQLException {
		return _connection.getAutoCommit();
	}//getAutoCommit()
	/**
	 * @see java.sql.Connection#isClosed()
	 */
	public boolean isClosed() throws SQLException {
		return _connection.isClosed();
	}//isClosed()
	/**
	 * @see java.sql.Connection#isReadOnly()
	 */
	public boolean isReadOnly() throws SQLException {
		return _connection.isReadOnly();
	}//isReadOnly()
	/**
	 * @see java.sql.Connection#setHoldability(int)
	 */
	public void setHoldability( int holdability ) throws SQLException {
		_connection.setHoldability( holdability );
	}//setHoldability()
	/**
	 * @see java.sql.Connection#setTransactionIsolation(int)
	 */
	public void setTransactionIsolation( int level ) throws SQLException {
		_connection.setTransactionIsolation( level );
	}//setTransactionIsolation( ()
	/**
	 * @see java.sql.Connection#setAutoCommit(boolean)
	 */
	public void setAutoCommit( boolean autoCommit ) throws SQLException {
		_connection.setAutoCommit( autoCommit );
	}//setAutoCommit()
	/**
	 * @see java.sql.Connection#setReadOnly(boolean)
	 */
	public void setReadOnly( boolean readOnly ) throws SQLException {
		_connection.setReadOnly( readOnly );
	}//setReadOnly()
	/**
	 * @see java.sql.Connection#getCatalog()
	 */
	public String getCatalog() throws SQLException {
		return _connection.getCatalog();
	}//getCatalog()
	/**
	 * @see java.sql.Connection#setCatalog(java.lang.String)
	 */
	public void setCatalog( String catalog ) throws SQLException {
		_connection.setCatalog( catalog );
	}//setCatalog()
	/**
	 * @see java.sql.Connection#getMetaData()
	 */
	public DatabaseMetaData getMetaData() throws SQLException {
		return _connection.getMetaData();
	}//getMetaData()
	/**
	 * @see java.sql.Connection#getWarnings()
	 */
	public SQLWarning getWarnings() throws SQLException {
		return _connection.getWarnings();
	}//getWarnings()
	/**
	 * @see java.sql.Connection#setSavepoint()
	 */
	public Savepoint setSavepoint() throws SQLException {
		return _connection.setSavepoint();
	}//setSavepoint()
	/**
	 * @see java.sql.Connection#releaseSavepoint(java.sql.Savepoint)
	 */
	public void releaseSavepoint( Savepoint savepoint ) throws SQLException {
		_connection.releaseSavepoint( savepoint );
	}//releaseSavepoint()
	/**
	 * @see java.sql.Connection#rollback(java.sql.Savepoint)
	 */
	public void rollback( Savepoint savepoint ) throws SQLException {
		_connection.rollback( savepoint );
	}//rollback()
	/**
	 * @see java.sql.Connection#createStatement()
	 */
	public Statement createStatement() throws SQLException {
		return _connection.createStatement();
	}//createStatement()
	/**
	 * @see java.sql.Connection#createStatement(int, int)
	 */
	public Statement createStatement( int resultSetType, int resultSetConcurrency )
	throws SQLException {
		return _connection.createStatement( resultSetType, resultSetConcurrency );
	}//createStatement()
	/**
	 * @see java.sql.Connection#createStatement(int, int, int)
	 */
	public Statement createStatement( 
		int resultSetType, int resultSetConcurrency, int resultSetHoldability ) 
	throws SQLException {
		return _connection.createStatement( 
			resultSetType, resultSetConcurrency, resultSetHoldability );
	}//createStatement()
	/**
	 * @see java.sql.Connection#getTypeMap()
	 */
	public Map getTypeMap() throws SQLException {
		return _connection.getTypeMap();
	}//getTypeMap()
	/**
	 * @see java.sql.Connection#setTypeMap(java.util.Map)
	 */
	public void setTypeMap( Map map ) throws SQLException {
		_connection.setTypeMap( map );
	}//setTypeMap()
	/**
	 * @see java.sql.Connection#nativeSQL(java.lang.String)
	 */
	public String nativeSQL( String sql ) throws SQLException {
		return _connection.nativeSQL( sql );
	}//nativeSQL()
	/**
	 * @see java.sql.Connection#prepareCall(java.lang.String)
	 */
	public CallableStatement prepareCall( String sql ) throws SQLException {
		return _connection.prepareCall( sql );
	}//prepareCall()
	/**
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int)
	 */
	public CallableStatement prepareCall( 
		String sql, int resultSetType, int resultSetConcurrency ) 
	throws SQLException {
		return _connection.prepareCall( sql, resultSetType, resultSetConcurrency );
	}//prepareCall()
	/**
	 * @see java.sql.Connection#prepareCall(java.lang.String, int, int, int)
	 */
	public CallableStatement prepareCall( 
		String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability ) 
	throws SQLException {
		return _connection.prepareCall( 
			sql, resultSetType, resultSetConcurrency, resultSetHoldability );
	}//prepareCall()
	/**
	 * @see java.sql.Connection#prepareStatement(java.lang.String)
	 */
	public PreparedStatement prepareStatement( String sql ) throws SQLException {
		return _connection.prepareStatement( sql );
	}//prepareStatement()
	/**
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int)
	 */
	public PreparedStatement prepareStatement( String sql, int autoGeneratedKeys )
	throws SQLException {
		return _connection.prepareStatement( sql, autoGeneratedKeys );
	}//prepareStatement()
	/**
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int)
	 */
	public PreparedStatement prepareStatement( 
		String sql, int resultSetType, int resultSetConcurrency ) 
	throws SQLException {
		return _connection.prepareStatement( sql, resultSetType, resultSetConcurrency );
	}//prepareStatement()
	/**
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int, int, int)
	 */
	public PreparedStatement prepareStatement( 
		String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability )
	throws SQLException {
		return _connection.prepareStatement( 
			sql, resultSetType, resultSetConcurrency, resultSetHoldability );
	}//prepareStatement()
	/**
	 * @see java.sql.Connection#prepareStatement(java.lang.String, int[])
	 */
	public PreparedStatement prepareStatement( String sql, int[] columnIndexes ) 
	throws SQLException {
		return _connection.prepareStatement( sql, columnIndexes );
	}//prepareStatement()
	/**
	 * @see java.sql.Connection#setSavepoint(java.lang.String)
	 */
	public Savepoint setSavepoint( String name ) throws SQLException {
		return _connection.setSavepoint( name );
	}//setSavepoint()
	/**
	 * @see java.sql.Connection#prepareStatement(java.lang.String, java.lang.String[])
	 */
	public PreparedStatement prepareStatement( String sql, String[] columnNames )
	throws SQLException {
		return _connection.prepareStatement( sql, columnNames );
	}//prepareStatement()
}