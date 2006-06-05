package com.nexusbpm.editor.util.dbproxy;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.exceptions.YAWLException;

import com.nexusbpm.editor.util.DefaultExtendedTableModel;
import com.nexusbpm.editor.util.ExtendedTableModel;

/**
 * Executes a query against an arbitrary named JDBC DataSource. The DataSource
 * is looked up via JNDI. If one of the retrieveX() methods is called, the query
 * is executed and the data from the ResultSet returned.
 * @author  Felix L J Mayer
 * @version $Revision: 1.8 $
 * @created Feb 24, 2004
 * @ejb.bean name="DatabaseProxy" type="Stateful" display-name="DatabaseProxy EJB"
 *   description="EJB that acts as a proxy for various databases"
 *   view-type="remote" jndi-name="ejb/DatabaseProxy"
 */
public class DatabaseProxyBean extends AbstractSessionBean {
	
	static protected final Log LOG = LogFactory.getLog( DatabaseProxyBean.class );

	static private final int DEFAULT_CACHE_SIZE = 512;

	public DatabaseProxyBean() {
		// Do initialization in ejbCreate().
	}//DatabaseProxyBean()

	/**
	 * Initializes the DatabaseProxyBean with a <code>DataSource</code>, a SQL
	 * query string and the size of the row cache. 
	 * The row cache is only used when calling <code>retrieveRows(int,int)</code>.
	 * @param dataSourceName  the name of the data source
	 * @param query           the query SQL statement
	 * @param cacheSize       the size of the row cache
	 * @throws CreateException
	 * @throws CapselaException
	 * @ejb.create-method
	 */
	public void ejbCreate( String dataSourceName, String query, int cacheSize ) 
	throws CreateException, YAWLException {
		LOG.info( "ejbCreate(dataSourceName='" + dataSourceName 
			+ "' query='" + query + "' cacheSize=" + cacheSize + ")" );
		_dataSourceName = dataSourceName;
		// FIXME: XXX TODO need to get data source in a YAWL specific manner
//		_dataSource = (DataSource) ServiceLocator.instance().lookup( 
//			"java:/" + dataSourceName );
		_cacheSize = cacheSize;
		if( query != null ) setQuery( query );
		throw new RuntimeException( "Need to implemement for YAWL" );
	}//ejbCreate()
	/**
	 * Initializes the DatabaseProxyBean with a <code>DataSource</code>, a SQL
	 * query string and the default size of the row cache. 
	 * @param dataSourceName  the name of the data source
	 * @param query           the query SQL statement
	 * @throws CreateException
	 * @throws YAWLException
	 * @ejb.create-method
	 */
	public void ejbCreate( String dataSourceName, String query ) 
	throws CreateException, YAWLException {
		ejbCreate( dataSourceName, query, DEFAULT_CACHE_SIZE );
	}//ejbCreate()
	/**
	 * @param driverClassName
	 * @param connectionURL
	 * @param userName
	 * @param password
	 * @param query
	 * @throws CreateException
	 * @throws YAWLException
	 * @ejb.create-method
	 */
	public void ejbCreate( String driverClassName, String connectionURL, 
		String userName, String password, String query ) 
	throws CreateException, YAWLException {
		LOG.debug( "ejbCreate(driverClassName='" + driverClassName 
			+ "' connectionURL='" + connectionURL + "' userName='" + userName 
			+ "' password='" + password + "' query='" + query + "')" );
		_dataSource = new ProxyDataSource( 
			driverClassName, connectionURL, userName, password );
		_driverClassName = driverClassName;
		_query = query;
		_cacheSize = DEFAULT_CACHE_SIZE;
		if( query != null ) {
			// Determine metadata.
			try {
				// NOTE: The SAS driver returns the string
				// SAS/SHARE driver for JDBC, Version 2.5 Production
				// instead of the driver class name.
				if( REDBRICK_DRIVER.equals( driverClassName() ) ) {
					// Ensure sequential access to the Redbrick connection.
					synchronized( REDBRICK_DRIVER ) {
						determineMetadata( _query );
					}//synchronized
				}//if
				else {
					determineMetadata( _query );
				}//else
			}//try
			catch( SQLException e ) {
				throw new YAWLException( e );
			}//catch
		}//if
	}//ejbCreate()
	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 */
	public void ejbRemove() throws EJBException, RemoteException {
		LOG.info( "ejbRemove()" );
		if( _dataSource instanceof ProxyDataSource ) {
			ProxyDataSource proxyDataSource = (ProxyDataSource) _dataSource;
			try {
				LOG.debug( "ProxyDataSource.close()" );
				proxyDataSource.close();
			}//try
			catch( SQLException e ) {
				throw new EJBException( e );
			}//catch
		}//if
	}//ejbRemove()


	private String _dataSourceName;
	/**
	 * Gets the name of the DataSource.
	 * @return  the name of the DataSource
	 * @ejb.interface-method view-type="remote"
	 * @ejb.transaction type="NotSupported"
	 */
	public String dataSourceName() {
		LOG.debug( "dataSourceName()=" + _dataSourceName );
		return _dataSourceName;
	}//dataSourceName()
	
	private String _driverClassName;
	public String driverClassName() {
		return _driverClassName;
	}//driverClassName()
	
	private String _query;
	/**
	 * Gets the the SQL query.
	 * @return  the SQL query
	 * @ejb.interface-method view-type="remote"
	 * @ejb.transaction type="NotSupported"
	 */
	public String query() {
		LOG.debug( "query()=" + _query );
		return _query;
	}//query()
	/**
	 * @param query
	 * @ejb.interface-method view-type="remote"
	 * @ejb.transaction type="NotSupported"
	 */
	public void setQuery( String query ) throws YAWLException {
		LOG.debug( "setQuery(query='" + query + "')" );
		if( query == null ) throw new YAWLException( "query == null" );
		// Determine metadata.
		try {
			// NOTE: The SAS driver returns the string
			// SAS/SHARE driver for JDBC, Version 2.5 Production
			// instead of the driver class name.
			if( REDBRICK_DRIVER.equals( driverClassName() ) ) {
				// Ensure sequential access to the Redbrick connection.
				synchronized( REDBRICK_DRIVER ) {
					determineMetadata( query );
				}//synchronized
			}//if
			else {
				determineMetadata( query );
			}//else
		}//try
		catch( SQLException e ) {
			throw new YAWLException( e );
		}//catch
		_query = query;
		_cache = null;
	}//setQuery()

	private int _cacheSize;
	/**
	 * Gets the size of the row cache.
	 * @return  the size of the row cache.
	 * @ejb.interface-method view-type="remote"
	 * @ejb.transaction type="NotSupported"
	 */
	public int cacheSize() {
		LOG.trace( "cacheSize()=" + _cacheSize );
		return _cacheSize;
	}//cacheSize
	
	private int _columnCount = -1;
	/**
	 * Gets the column count of the query's result set.
	 * Returns a meaningful result only after <code>retrieveRows()</code> or 
	 * <code>retrieveSampleRows() </code> has been called.
	 * @return  the column count
	 * @ejb.interface-method view-type="remote"
	 * @ejb.transaction type="NotSupported"
	 */
	public int columnCount() {
		LOG.trace( "columnCount()=" + _columnCount );
		return _columnCount;
	}//columnNames()

	private String[] _columnNames = null;
	/**
	 * Gets the column names of the query's result set.
	 * Returns a meaningful result only after <code>retrieveRows()</code> or 
	 * <code>retrieveSampleRows() </code> has been called.
	 * @return  an array of column names
	 * @ejb.interface-method view-type="remote"
	 * @ejb.transaction type="NotSupported"
	 */
	public String[] columnNames() {
		LOG.trace( "columnNames()=" + _columnNames );
		return _columnNames;
	}//columnNames()

	private Class[] _columnClasses = null;
	/**
	 * Gets the column classes of the query's result set.
	 * Returns a meaningful result only after <code>retrieveRows()</code> or 
	 * <code>retrieveSampleRows() </code> has been called.
	 * @return  an array of column names
	 * @ejb.interface-method view-type="remote"
	 * @ejb.transaction type="NotSupported"
	 */
	public Class[] columnClasses() {
		LOG.trace( "columnClasses()=" + _columnClasses );
		return _columnClasses;
	}//columnClasses()
	
	/**
	 * Determines the number of rows that satisfy the query.
	 * @return  the number of rows that satisfy the query.
	 * @ejb.interface-method view-type="remote"
	 * @ejb.transaction type="NotSupported"
	 */
	public int rowCount() throws YAWLException {
		int rowCount = 0;
		Connection connection = null;
		Statement statement = null;
		try {
			connection = _dataSource.getConnection();
			statement = connection.createStatement();
			if( REDBRICK_DRIVER.equals( driverClassName() ) ) {
				// Ensure sequential access to the Redbrick connection.
				synchronized( REDBRICK_DRIVER ) {
					rowCount = countRows( statement ); 
				}//synchronized
			}//if
			else {
				rowCount = countRows( statement ); 
			}//else
		}//try
		catch( SQLException e ) {
			throw new YAWLException( e );
		}//catch
		finally {
			try {
				if( statement != null ) statement.close();
				if( connection != null ) connection.close();
			}//try
			catch( SQLException e ) {
				throw new YAWLException( e );
			}//catch
		}//finally
		LOG.trace( "rowCount()=" + rowCount );
		return rowCount;
	}//rowCount()
	/**
	 * @param statement
	 * @return
	 * @throws SQLException
	 */
	private int countRows( Statement statement ) throws SQLException {
		String countQuery;
		// Trim the query so that endsWith(";") actually works.
		_query = _query.trim();
		if( _query.endsWith( ";" ) ) {
			// Remove the semicolon from the subquery and put it at the end.
			String subQuery = _query.substring( 0, _query.length() - 1 );
			countQuery = "SELECT COUNT(*) FROM (" + subQuery + ") SUB;";
		}//if
		else {
			// There is no semicolon to remove from the subquery.
			countQuery = "SELECT COUNT(*) FROM (" + _query + ") SUB";
		}//else
		ResultSet resultSet = null;
		int rowCount = -1;
		try {
			resultSet = statement.executeQuery( countQuery );
			resultSet.next();
			rowCount = resultSet.getInt( 1 );
		}//try
		finally {
			if( resultSet != null ) resultSet.close();
		}//finally
		return rowCount;
	}//countRows()


	/**
	 * The name of the Redbrick driver class. Used to detect if the DataSource
	 * is Redbrick and as a lock to ensure sequential access.
	 */
	static public final String REDBRICK_DRIVER = "redbrick.jdbc.RBWDriver";
	/**
	 * The DataSource against which the query is executed.
	 */
	private transient DataSource _dataSource;

	
	/**
	 * @param sql
	 * @return
	 * @throws YAWLException
	 * @ejb.interface-method view-type="remote"
	 * @ejb.transaction type="Required"
	 */
	public int executeSQL( String sql ) throws YAWLException {
		LOG.debug( "executeSQL(sql='" + sql + "')" );
		Connection connection = null;
		Statement statement = null;
		int result = -1;
		try {
			connection = _dataSource.getConnection();
			statement = connection.createStatement();
			if( REDBRICK_DRIVER.equals( driverClassName() ) ) {
				// Ensure sequential access to the Redbrick connection.
				synchronized( REDBRICK_DRIVER ) {
					result = statement.executeUpdate( sql );
				}//synchronized
			}//if
			else {
				result = statement.executeUpdate( sql );
			}//else
		}//try
		catch( SQLException e ) {
			throw new YAWLException( e );
		}//catch
		finally {
			try {
				if( statement != null ) statement.close();
				if( connection != null ) connection.close();
			}//try
			catch( SQLException e ) {
				throw new YAWLException( e );
			}//catch
		}//finally
		return result;
	}//executeSQL()


	/**
	 * Retrieves a sample of the rows from the query's result set.
	 * The query is executed each time the method is called, no caching
	 * takes place.
	 * @param start  the index of the first row to return
	 * @param end    the index of the last row to return
	 * @param count  the count of rows to return
	 * @return  the <code>ResultSet<code>'s data as a <code>List<code> of <code>List<code>s
	 * @throws YAWLException
	 * @ejb.interface-method view-type="remote"
	 * @ejb.transaction type="NotSupported"
	 */
	public ResultSet retrieveSampleRows( int start, int end, int count ) 
	throws YAWLException {
		LOG.debug( "retrieveSampleRows(start=" + start + " end=" + end 
			+ " count=" + count + ")" );
		if( _query == null || _query.length() == 0 ) {
			throw new YAWLException("No query specified in create().");
		}//if
		if( start < 0 ) throw new YAWLException("start < 0");
		if( end < 0 ) throw new YAWLException("end < 0");
		if( start > end ) throw new YAWLException("start > end");
		if( count < 0 ) throw new YAWLException("count < 0");
		ProxyResultSet rows = readDatabase( start, end, count, 0 );
		return rows;
	}//retrieveSampleRows()
	/**
	 * Retrieves the specified rows of the query's result set.
	 * This method uses a contiguous row cache of a fixed size determined when the
	 * bean was created. The row cache is centered on the row index 
	 * <code>start</code> and enhances the performance of subsequent calls
	 * if the row indices between <code>start</code> and <code>start + count</code> 
	 * fall within the cache. If they don't, the entire cache is read from the
	 * database centered around the new <code>start</code> position.
	 * @param start  the index of the first row to return
	 * @param count  the count of rows to return
	 * @return  the <code>ResultSet<code>'s data as a <code>List</code> of 
	 *          <code>List<c/ode>s
	 * @throws YAWLException
	 * @ejb.interface-method view-type="remote"
	 * @ejb.transaction type="NotSupported"
	 */
	public ResultSet retrieveRows( int start, int count ) 
	throws YAWLException {
		if( _query == null || _query.length() == 0 ) {
			throw new YAWLException("No query specified in create().");
		}//if
		if( start < 0 ) throw new YAWLException("start < 0");
		if( count < 0 ) throw new YAWLException("count < 0");
		ProxyResultSet resultSet = null;
		// Log a user-friendly info message, because lots of these database queries
		// run for a long time and the user needs periodic feedback.
		if( count == 1 ) {
		  // Commented because it produces too much logging (bug 860)
			// LOG.info( "Retrieving row " + start + "." );
		}//if
		else {
			LOG.info( "Retrieving rows " + start + " to " + ( start + count ) + "." );
		}//else
		// If the cache should be used and there is a cache and the requested rows
		// are in the cache...
		if( _cache != null && start >= _cacheStart
			// Note that start + count may go beyond what is actually in the cache
			// if the query didn't return enough data.
			&& start + count <= _cacheStart + _cacheSize ) {
			// ... then copy the requested rows into the returned list.
			if( LOG.isDebugEnabled() ) {
				LOG.debug( "retrieveRows(start=" + start + " count=" + count 
					+ ") from cache[start=" + _cacheStart + " size=" + _cache.size() 
					+ " first=" + ((Row) _cache.get( 0 )).index() 
					+ " last=" + ((Row) _cache.get( _cache.size() - 1 )).index() 
					+ "]" );
			}//if
			List rows = new ArrayList();
			for( int i = start - _cacheStart; 
				i < Math.min( start - _cacheStart + count, _cache.size() ); i++ ) {
				rows.add( ((Row) _cache.get( i )).data() );
			}//for
			resultSet = new ProxyResultSet( rows, _columnNames );
		}//if
		// If the requested rows don't exist in the query... 
		else if( _cache != null && start >= _cacheStart + _cache.size() 
			&& start + count < _cacheStart + cacheSize() ) {
			// ... then return an empty result set.
			// The requested rows are beyond the end of the query if:
			// - the cache contains data,
			// - the start row is beyond the end of the cache,
			// - the last requested row should be in the cache but is not, i.e. the 
			// cache was not filled because the end of the result set was reached.
			LOG.debug( "retrieveRows(start=" + start + " count=" + count + ") empty result" ); 
			resultSet = new ProxyResultSet( new ArrayList(), _columnNames );
		}//else if
		else {
			// Otherwise read the rows from the DataSource.
			LOG.debug( "retrieveRows(start=" + start + " count=" + count + ") from database" ); 
			resultSet = readDatabase( start, start + count, count, _cacheSize );
		}//else
		return resultSet;
	}//retrieveRows()
	/**
	 * Retrieves the specified rows of the query's result set as an 
	 * <tt>ExtendedTableModel</tt>.
	 * @param start the index of the first row to return
	 * @param count the count of rows to return
	 * @return the specified rows of the query's result set as an <tt>ExtendedTableModel</tt>
	 * @ejb.interface-method view-type="remote"
	 * @ejb.transaction type="NotSupported"
	 */
	public ExtendedTableModel retrieveRowsAsTableModel( int start, int count ) throws YAWLException {
		try {
			// Build the data vector.
			ProxyResultSet rs = (ProxyResultSet) this.retrieveRows( start, count );
			Vector dataVector = new Vector();
			while( rs.next() ) {
				List row = rs.currentRow();
				Vector v = new Vector();
				for( Iterator i = row.iterator(); i.hasNext(); ) {
					v.addElement( i.next() );
				}//for
				dataVector.add( v );
			}//while
			// Build the column name vector.
			Vector columnNames = new Vector();
			String[] names = this.columnNames();
			for( int i = 0; i < names.length; i++ ) {
				columnNames.addElement( names[i] );
			}//for
			// Instantiate and return the table model.
			List columnClasses = Arrays.asList( this.columnClasses() );
			DefaultExtendedTableModel model = new DefaultExtendedTableModel( dataVector, columnNames, columnClasses );
			return model;
		}//try
		catch( SQLException e ) {
			throw new YAWLException( e );
		}//catch
	}//retrieveRowsAsTableModel()
	/**
	 * Reads the data from the database. A <code>Connection</code> is obtained 
	 * from the <code>DataSource</code> and a <code>Statement<code> is created 
	 * from the <code>Connection</code>. The SQL query is executed on the
	 * <code>Statement<code>, producing a <code>ResultSet<code>. If the
	 * <code>DataSource</code> uses the Redbrick driver, processing of the
	 * <code>ResultSet<code> is serialized on this JVM.
	 * @param start  the index of the first row to return
	 * @param end    the index of the last row to return
	 * @param count  the count of rows to return
	 * @param cacheSize  the size of the created row cache
	 * @return  the <code>ResultSet<code>'s data as a <code>List<code> of <code>Row<code>s
	 * @throws YAWLException
	 */
	private ProxyResultSet readDatabase( int start, int end, int count, int cacheSize )
	throws YAWLException {
		ProxyResultSet rows = null;
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			connection = _dataSource.getConnection();
			statement = connection.createStatement();
			if( REDBRICK_DRIVER.equals( driverClassName() ) ) {
				// Ensure sequential access to the Redbrick connection.
				synchronized( REDBRICK_DRIVER ) {
					resultSet = statement.executeQuery( _query );
					rows = readResultSet( resultSet, start, end, count, cacheSize ); 
				}//synchronized
			}//if
			else {
				resultSet = statement.executeQuery( _query );
				rows = readResultSet( resultSet, start, end, count, cacheSize ); 
			}//else
		}//try
		catch( SQLException e ) {
			throw new YAWLException( e );
		}//catch
		finally {
			try {
				if( resultSet != null ) resultSet.close();
				if( statement != null ) statement.close();
				// TODO Close connection after every read? Is not serializable!
				if( connection != null ) connection.close();
			}//try
			catch( SQLException e ) {
				throw new YAWLException( e );
			}//catch
		}//finally
		return rows;
	}//readDatabase()
	/**
	 * Reads the rows from the <code>ResultSet<code> and places them into the
	 * returned <code>List</code and the row cache.
	 * @param resultSet  the <code>ResultSet<code> to process
	 * @param start      the index of the first row to return
	 * @param end        the index of the last row to return
	 * @param count      the count of rows to return
	 * @param cacheSize  the size of the created row cache
	 * @return  the <code>ResultSet<code>'s data as a <code>List<code> of <code>Row<code>s
	 * @throws SQLException
	 */
	private ProxyResultSet readResultSet( ResultSet resultSet, int start, int end, 
		int count, int cacheSize ) 
	throws SQLException {
		assert cacheSize == 0 || end - start == count 
			: "cacheSize == 0 || end - start == count";
		List rows = new ArrayList();
		if( cacheSize > 0 ) {
			_cache = new ArrayList( cacheSize );
			_cacheStart = Math.max( start - cacheSize/2, 0 );
		}//if
		else {
			_cache = null;
			_cacheStart = start;
		}//else
		float sampleIncrement = 1;
		if( end - start != count ) sampleIncrement = (end - start + 1)/(float) count;
		float sampleIndex = start;
		// Redbrick does not support ResultSet.isAfterLast(), so everything needs
		// to be done in a single loop.
		for( int i = 0; resultSet.next()
			// Note that end could be greater than _cacheStart + cacheSize.
			&& i < Math.max( _cacheStart + cacheSize, end ); i++ ) {
			if( i >= _cacheStart ) {
				Row row = new Row( i, resultSet );
				if( cacheSize > 0 ) _cache.add( row );
				if( i >= start && i < end ) {
					if( sampleIncrement == 1 || Math.round( sampleIndex ) == i ) {
						rows.add( row.data() );
						sampleIndex += sampleIncrement;
					}//if
				}//if
			}//if 
		}//for
		return new ProxyResultSet( rows, _columnNames );
	}//readResultSet()
	/**
	 * Determines the metadata from the <code>ResultSet</code> for the
	 * specified query and places it into instance variables for later use.
	 * @param query the query to pull metadata from
	 * @throws SQLException
	 */
	private void determineMetadata( String query ) 
	throws SQLException {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		try {
			connection = _dataSource.getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery( query );
			ResultSetMetaData metaData = resultSet.getMetaData();
			if( _columnCount == -1 ) {
				_columnCount = metaData.getColumnCount();
			}//if
			if( _columnNames == null ) {
				_columnNames = new String[_columnCount];
				for( int i = 0; i < _columnCount; i++ ) {
					_columnNames[i] = metaData.getColumnName( i + 1 );
				}//for
			}//if
			if( _columnClasses == null ) {
				_columnClasses = new Class[_columnCount];
				for( int i = 0; i < _columnCount; i++ ) {
					String className = metaData.getColumnClassName( i + 1 );
					try {
						_columnClasses[i] = Class.forName( className );
					}//try
					catch( ClassNotFoundException e ) {
						int type = metaData.getColumnType( i + 1 );
						switch( type ) {
						case Types.BOOLEAN:
							_columnClasses[i] = Boolean.class;
							break;
						case Types.BLOB: case Types.CLOB:
							_columnClasses[i] = Object.class;
							break;
						case Types.BIGINT:
							_columnClasses[i] = Long.class;
							break;
						case Types.INTEGER:
							_columnClasses[i] = Integer.class;
							break;
						case Types.FLOAT:
							_columnClasses[i] = Float.class;
							break;
						case Types.DOUBLE: case Types.DECIMAL: case Types.REAL:
							_columnClasses[i] = Double.class;
							break;
						case Types.VARCHAR :
							_columnClasses[i] = String.class;
							break;
						case Types.DATE: case Types.TIME: case Types.TIMESTAMP:
							_columnClasses[i] = java.sql.Date.class;
							break;
						default:
							_columnClasses[i] = Object.class;
							break;
						}//switch
					}//catch
				}//for
			}//if
		}//try
		finally {
			if( resultSet != null ) resultSet.close();
			if( statement != null ) statement.close();
			if( connection != null ) connection.close();
		}//finally
	}//determineMetadata()

	private ArrayList _cache;
	private int _cacheStart;
	
}//DatabaseProxyBean
