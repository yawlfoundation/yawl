package com.nexusbpm.editor.util.dbproxy;

import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Custom implementation of <tt>ResultSet</tt> used by <tt>DatabaseProxyBean</tt>.
 */
class ProxyResultSet implements ResultSet, List, Serializable {
	/**
	 * @param contents
	 * @param columnNames
	 */
	public ProxyResultSet( List contents, String[] columnNames ) {
		_rows = contents;
		for( int i = 0; i < columnNames.length; i++ ) {
			_columnIndex.put( columnNames[i], new Integer( i + 1 ) );
		}//for
	}//ProxyResultSet
	private List _rows;
	private int _current = -1;
	private Map _columnIndex = new HashMap();
	public List currentRow() {
		return (List) _rows.get( _current );
	}//currentRow
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append( "ProxyResultSet(rows=" ).append( _rows.size() );
		s.append( ", current row=" ).append( _current ).append( " columns=[" );
		for( Iterator i = _columnIndex.keySet().iterator(); i.hasNext(); ) {
			s.append( i.next() );
			if( i.hasNext() ) {
				s.append( ", " );
			}//if
		}//for
		s.append( "])" );
		return s.toString();
	}//toString()
	/**
	 * Moves the cursor down one row from its current position.
	 * A <code>ResultSet</code> cursor is initially positioned
	 * before the first row; the first call to the method
	 * <code>next</code> makes the first row the current row; the
	 * second call makes the second row the current row, and so on. 
	 *
	 * <P>If an input stream is open for the current row, a call
	 * to the method <code>next</code> will
	 * implicitly close it. A <code>ResultSet</code> object's
	 * warning chain is cleared when a new row is read.
	 *
	 * @return <code>true</code> if the new current row is valid; 
	 * <code>false</code> if there are no more rows 
	 * @exception SQLException if a database access error occurs
	 * @see java.sql.ResultSet#next()
	 */
	public boolean next() throws SQLException {
		if( _current == Integer.MAX_VALUE ) return false;
		if( _current == _rows.size() - 1 ) return false;
		_current++;
		return true;
	}//next()
	/**
	 * Moves the cursor to the previous row in this <code>ResultSet</code> object.
	 * @return <code>true</code> if the cursor is on a valid row; 
	 * <code>false</code> if it is off the result set
	 * @exception SQLException if a database access error
	 * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
	 * @see java.sql.ResultSet#previous()
	 */
	public boolean previous() throws SQLException {
		if( _current == -1 ) return false;
		if( _current == 0 ) return false;
		_current--;
		return true;
	}//previous()
	/**
	 * Retrieves the current row number. The first row is number 1, the
	 * second number 2, and so on.
	 * @return the current row number; <code>0</code> if there is no current row
	 * @exception SQLException if a database access error occurs
	 * @see java.sql.ResultSet#getRow()
	 */
	public int getRow() throws SQLException {
		if( _current == Integer.MAX_VALUE ) return 0;
		return _current + 1;
	}//getRow()
	/**
	 * Moves the cursor to the end of this <code>ResultSet</code> object, 
	 * just after the last row. This method has no effect if the result set 
	 * contains no rows.
	 * @exception SQLException if a database access error
	 * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
	 * @see java.sql.ResultSet#afterLast()
	 */
	public void afterLast() throws SQLException {
		_current = Integer.MAX_VALUE;
	}//afterLast()
	/**
	 * Moves the cursor to the front of this <code>ResultSet</code> object, 
	 * just before the first row. This method has no effect if the result set 
	 * contains no rows.
	 * @exception SQLException if a database access error
	 * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
	 * @see java.sql.ResultSet#beforeFirst()
	 */
	public void beforeFirst() throws SQLException {
		_current = -1;
	}//beforeFirst()
	/**
	 * Moves the cursor to the first row in this <code>ResultSet</code> object.
	 * @return <code>true</code> if the cursor is on a valid row;
	 * <code>false</code> if there are no rows in the result set
	 * @exception SQLException if a database access error
	 * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
	 * @see java.sql.ResultSet#first()
	 */
	public boolean first() throws SQLException {
		if( ! _rows.isEmpty() ) {
			_current = 0;
		}//if
		else {
			_current = -1;
		}//else
		return ! _rows.isEmpty();
	}//first()
	/**
	 * Moves the cursor to the last row in this <code>ResultSet</code> object.
	 * @return <code>true</code> if the cursor is on a valid row;
	 * <code>false</code> if there are no rows in the result set
	 * @exception SQLException if a database access error
	 * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
	 * @see java.sql.ResultSet#last()
	 */
	public boolean last() throws SQLException {
		if( ! _rows.isEmpty() ) {
			_current = _rows.size() - 1;
		}//if
		else {
			_current = Integer.MAX_VALUE;
		}//else
		return ! _rows.isEmpty();
	}//last()
	/**
	 * Retrieves whether the cursor is before the first row in 
	 * this <code>ResultSet</code> object.
	 * @return <code>true</code> if the cursor is before the first row;
	 * <code>false</code> if the cursor is at any other position or the
	 * result set contains no rows
	 * @exception SQLException if a database access error occurs
	 * @see java.sql.ResultSet#isBeforeFirst()
	 */
	public boolean isBeforeFirst() throws SQLException {
		return _current == -1;
	}//isBeforeFirst()
	/**
	 * Retrieves whether the cursor is after the last row in 
	 * this <code>ResultSet</code> object.
	 * @return <code>true</code> if the cursor is after the last row;
	 * <code>false</code> if the cursor is at any other position or the
	 * result set contains no rows
	 * @exception SQLException if a database access error occurs
	 * @see java.sql.ResultSet#isAfterLast()
	 */
	public boolean isAfterLast() throws SQLException {
		return _current == Integer.MAX_VALUE;
	}//isAfterLast()
	/**
	 * Retrieves whether the cursor is on the first row of
	 * this <code>ResultSet</code> object.
	 * @return <code>true</code> if the cursor is on the first row;
	 * <code>false</code> otherwise
	 * @exception SQLException if a database access error occurs
	 * @see java.sql.ResultSet#isFirst()
	 */
	public boolean isFirst() throws SQLException {
		return _current == 0;
	}//isFirst()
	/**
	 * Retrieves whether the cursor is on the last row of 
	 * this <code>ResultSet</code> object.
	 * Note: Calling the method <code>isLast</code> may be expensive
	 * because the JDBC driver might need to fetch ahead one row in order to 
	 * determine whether the current row is the last row in the result set.
	 * @return <code>true</code> if the cursor is on the last row;
	 * <code>false</code> otherwise
	 * @exception SQLException if a database access error occurs
	 * @see java.sql.ResultSet#isLast()
	 */
	public boolean isLast() throws SQLException {
		return _current == _rows.size() - 1;
	}//isLast()
	/**
	 * Moves the cursor to the given row number in this <code>ResultSet</code> 
	 * object.
	 * <p>If the row number is positive, the cursor moves to the given row 
	 * number with respect to the beginning of the result set. The first row 
	 * is row 1, the second is row 2, and so on. 
	 * <p>If the given row number is negative, the cursor moves to an absolute 
	 * row position with respect to the end of the result set. For example, 
	 * calling the method <code>absolute(-1)</code> positions the cursor on 
	 * the last row; calling the method <code>absolute(-2)</code>
	 * moves the cursor to the next-to-last row, and so on.
	 * <p>An attempt to position the cursor beyond the first/last row in
	 * the result set leaves the cursor before the first row or after 
	 * the last row.
	 * <p><b>Note:</b> Calling <code>absolute(1)</code> is the same
	 * as calling <code>first()</code>. Calling <code>absolute(-1)</code> 
	 * is the same as calling <code>last()</code>.
	 * @param row the number of the row to which the cursor should move.
	 *        A positive number indicates the row number counting from the
	 *        beginning of the result set; a negative number indicates the
	 *        row number counting from the end of the result set
	 * @return <code>true</code> if the cursor is on the result set;
	 * <code>false</code> otherwise
	 * @exception SQLException if a database access error occurs, 
	 *            or the result set type is <code>TYPE_FORWARD_ONLY</code>
	 * @see java.sql.ResultSet#absolute(int)
	 */
	public boolean absolute( int row ) throws SQLException {
		if( row >= 0 ) {
			_current = row - 1;
		}//if
		else {
			_current = _rows.size() + row;
		}//else
		if( _current < 0 ) {
			_current = -1;
			return false;
		}//if
		if( _current > _rows.size() - 1 ) {
			_current = Integer.MAX_VALUE;
			return false;
		}//if
		return true;
	}//absolute()
	/**
	 * Moves the cursor a relative number of rows, either positive or negative.
	 * Attempting to move beyond the first/last row in the result set positions 
	 * the cursor before/after the the first/last row. Calling <code>relative(0)</code> 
	 * is valid, but does not change the cursor position.
	 * <p>Note: Calling the method <code>relative(1)</code>
	 * is identical to calling the method <code>next()</code> and 
	 * calling the method <code>relative(-1)</code> is identical
	 * to calling the method <code>previous()</code>.
	 * @param rows an <code>int</code> specifying the number of rows to
	 *        move from the current row; a positive number moves the cursor
	 *        forward; a negative number moves the cursor backward
	 * @return <code>true</code> if the cursor is on a row;
	 *         <code>false</code> otherwise
	 * @exception SQLException if a database access error occurs, 
	 *            there is no current row, or the result set type is 
	 *            <code>TYPE_FORWARD_ONLY</code>
	 * @see java.sql.ResultSet#relative(int)
	 */
	public boolean relative( int rows ) throws SQLException {
		_current += rows;
		if( _current < 0 ) {
			_current = -1;
			return false;
		}//if
		if( _current > _rows.size() - 1 ) {
			_current = Integer.MAX_VALUE;
			return false;
		}//if
		return true;
	}//relative()
	/**
	 * @see java.sql.ResultSet#findColumn(java.lang.String)
	 */
	public int findColumn( String columnName ) throws SQLException {
		return ((Integer) _columnIndex.get( columnName )).intValue();
	}//findColumn()
	
	
	/**
	 * @see java.sql.ResultSet#getString(int)
	 */
	public String getString( int columnIndex ) throws SQLException {
		return (String) currentRow().get( columnIndex - 1 );
	}//getString()
	/**
	 * @see java.sql.ResultSet#getString(java.lang.String)
	 */
	public String getString( String columnName ) throws SQLException {
		return (String) currentRow().get( findColumn( columnName ) );
	}//getString()
	/**
	 * @see java.sql.ResultSet#getByte(int)
	 */
	public byte getByte( int columnIndex ) throws SQLException {
		return ((Byte) currentRow().get( columnIndex - 1 )).byteValue();
	}//getByte()
	/**
	 * @see java.sql.ResultSet#getByte(java.lang.String)
	 */
	public byte getByte( String columnName ) throws SQLException {
		return getByte( findColumn( columnName ) );
	}//getByte()
	/**
	 * @see java.sql.ResultSet#getDouble(int)
	 */
	public double getDouble( int columnIndex ) throws SQLException {
		return ((Double) currentRow().get( columnIndex - 1 )).doubleValue();
	}//getDouble()
	/**
	 * @see java.sql.ResultSet#getDouble(java.lang.String)
	 */
	public double getDouble( String columnName ) throws SQLException {
		return getDouble( findColumn( columnName ) );
	}//getDouble()
	/**
	 * @see java.sql.ResultSet#getFloat(int)
	 */
	public float getFloat( int columnIndex ) throws SQLException {
		return ((Float) currentRow().get( columnIndex - 1 )).floatValue();
	}//getFloat()
	/**
	 * @see java.sql.ResultSet#getFloat(java.lang.String)
	 */
	public float getFloat( String columnName ) throws SQLException {
		return getFloat( findColumn( columnName ) );
	}//getFloat()
	/**
	 * @see java.sql.ResultSet#getInt(int)
	 */
	public int getInt( int columnIndex ) throws SQLException {
		return ((Integer) currentRow().get( columnIndex - 1 )).intValue();
	}//getInt()
	/**
	 * @see java.sql.ResultSet#getInt(java.lang.String)
	 */
	public int getInt( String columnName ) throws SQLException {
		return getInt( findColumn( columnName ) );
	}//getInt()
	/**
	 * @see java.sql.ResultSet#getLong(int)
	 */
	public long getLong( int columnIndex ) throws SQLException {
		return ((Long) currentRow().get( columnIndex - 1 )).longValue();
	}//getLong()
	/**
	 * @see java.sql.ResultSet#getLong(java.lang.String)
	 */
	public long getLong( String columnName ) throws SQLException {
		return getLong( findColumn( columnName ) );
	}//getLong()
	/**
	 * @see java.sql.ResultSet#getShort(int)
	 */
	public short getShort( int columnIndex ) throws SQLException {
		return ((Short) currentRow().get( columnIndex - 1 )).shortValue();
	}//getShort()
	/**
	 * @see java.sql.ResultSet#getShort(java.lang.String)
	 */
	public short getShort( String columnName ) throws SQLException {
		return getShort( findColumn( columnName ) );
	}//getShort()
	/**
	 * @see java.sql.ResultSet#getBoolean(int)
	 */
	public boolean getBoolean( int columnIndex ) throws SQLException {
		return ((Boolean) currentRow().get( columnIndex - 1 )).booleanValue();
	}//getBoolean()
	/**
	 * @see java.sql.ResultSet#getBoolean(java.lang.String)
	 */
	public boolean getBoolean( String columnName ) throws SQLException {
		return getBoolean( findColumn( columnName ) );
	}//getBoolean()
	/**
	 * @see java.sql.ResultSet#getBytes(int)
	 */
	public byte[] getBytes( int columnIndex ) throws SQLException {
		return (byte[]) currentRow().get( columnIndex - 1 );
	}//getBytes()
	/**
	 * @see java.sql.ResultSet#getBytes(java.lang.String)
	 */
	public byte[] getBytes( String columnName ) throws SQLException {
		return getBytes( findColumn( columnName ) );
	}//getBytes()
	/**
	 * @see java.sql.ResultSet#getObject(int)
	 */
	public Object getObject( int columnIndex ) throws SQLException {
		return currentRow().get( columnIndex - 1 );
	}//getObject()
	/**
	 * Retrieves the value of the designated column in the current row of this 
	 * <code>ResultSet</code> object as an <code>Object</code> in the Java 
	 * programming language. If the value is an SQL <code>NULL</code>, the 
	 * driver returns a Java <code>null</code>. This method uses the specified 
	 * <code>Map</code> object for custom mapping if appropriate.
	 *
	 * @param colName the name of the column from which to retrieve the value
	 * @param map a <code>java.util.Map</code> object that contains the mapping 
	 *          from SQL type names to classes in the Java programming language
	 * @return an <code>Object</code> representing the SQL value in the 
	 *         specified column
	 * @exception SQLException if a database access error occurs
	 * @see java.sql.ResultSet#getObject(int, java.util.Map)
	 */
	public Object getObject( int columnIndex, Map map ) throws SQLException {
		throw new UnsupportedOperationException();
	}//getObject()
	/**
	 * @see java.sql.ResultSet#getObject(java.lang.String)
	 */
	public Object getObject( String columnName ) throws SQLException {
		return getObject( findColumn( columnName ) );
	}//getObject()
	/**
	 * @see java.sql.ResultSet#getObject(java.lang.String, java.util.Map)
	 */
	public Object getObject( String columnName, Map map ) throws SQLException {
		return getObject( findColumn( columnName ), map );
	}//getObject()
	/**
	 * @see java.sql.ResultSet#getBigDecimal(int)
	 */
	public BigDecimal getBigDecimal( int columnIndex ) throws SQLException {
		return (BigDecimal) currentRow().get( columnIndex - 1 );
	}//getBigDecimal()
	/**
	 * @see java.sql.ResultSet#getBigDecimal(java.lang.String)
	 */
	public BigDecimal getBigDecimal( String columnName ) throws SQLException {
		return getBigDecimal( findColumn( columnName ) );
	}//getBigDecimal()
	/**
	 * @see java.sql.ResultSet#getBigDecimal(int, int)
	 */
	public BigDecimal getBigDecimal( int columnIndex, int scale ) throws SQLException {
		return (BigDecimal) currentRow().get( columnIndex - 1 );
	}//getBigDecimal()
	/**
	 * @see java.sql.ResultSet#getBigDecimal(java.lang.String, int)
	 */
	public BigDecimal getBigDecimal( String columnName, int scale ) throws SQLException {
		return getBigDecimal( findColumn( columnName ), scale );
	}//getBigDecimal()
	/**
	 * @see java.sql.ResultSet#getURL(int)
	 */
	public URL getURL( int columnIndex ) throws SQLException {
		return (URL) currentRow().get( columnIndex - 1 );
	}//getURL()
	/**
	 * @see java.sql.ResultSet#getURL(java.lang.String)
	 */
	public URL getURL( String columnName ) throws SQLException {
		return getURL( findColumn( columnName ) );
	}//getURL()
	/**
	 * @see java.sql.ResultSet#getArray(int)
	 */
	public Array getArray( int columnIndex ) throws SQLException {
		return (Array) currentRow().get( columnIndex - 1 );
	}//getArray()
	/**
	 * @see java.sql.ResultSet#getArray(java.lang.String)
	 */
	public Array getArray( String columnName ) throws SQLException {
		return getArray( findColumn( columnName ) );
	}//getArray()
	/**
	 * @see java.sql.ResultSet#getBlob(int)
	 */
	public Blob getBlob( int columnIndex ) throws SQLException {
		return (Blob) currentRow().get( columnIndex - 1 );
	}//getBlob()
	/**
	 * @see java.sql.ResultSet#getBlob(java.lang.String)
	 */
	public Blob getBlob( String columnName ) throws SQLException {
		return getBlob( findColumn( columnName ) );
	}//getBlob()
	/**
	 * @see java.sql.ResultSet#getClob(int)
	 */
	public Clob getClob( int columnIndex ) throws SQLException {
		return (Clob) currentRow().get( columnIndex - 1 );
	}//getClob()
	/**
	 * @see java.sql.ResultSet#getClob(java.lang.String)
	 */
	public Clob getClob( String columnName ) throws SQLException {
		return getClob( findColumn( columnName ) );
	}//getClob()
	/**
	 * @see java.sql.ResultSet#getDate(int)
	 */
	public Date getDate( int columnIndex ) throws SQLException {
		return (Date) currentRow().get( columnIndex - 1 );
	}//getDate()
	/**
	 * @see java.sql.ResultSet#getDate(int, java.util.Calendar)
	 */
	public Date getDate( int columnIndex, Calendar cal ) throws SQLException {
		throw new UnsupportedOperationException();
	}//getDate()
	/**
	 * @see java.sql.ResultSet#getDate(java.lang.String)
	 */
	public Date getDate( String columnName ) throws SQLException {
		return getDate( findColumn( columnName ) );
	}//getDate()
	/**
	 * @see java.sql.ResultSet#getDate(java.lang.String, java.util.Calendar)
	 */
	public Date getDate(String columnName, Calendar cal) throws SQLException {
		return getDate( findColumn( columnName ), cal );
	}//getDate()
	/**
	 * @see java.sql.ResultSet#getRef(int)
	 */
	public Ref getRef( int columnIndex ) throws SQLException {
		return (Ref) currentRow().get( columnIndex - 1 );
	}//getRef()
	/**
	 * @see java.sql.ResultSet#getRef(java.lang.String)
	 */
	public Ref getRef( String columnName ) throws SQLException {
		return (Ref) currentRow().get( findColumn( columnName ) );
	}//getRef()
	/**
	 * @see java.sql.ResultSet#getTime(int)
	 */
	public Time getTime( int columnIndex ) throws SQLException {
		return (Time) currentRow().get( columnIndex - 1 );
	}//getTime()
	/**
	 * @see java.sql.ResultSet#getTime(int, java.util.Calendar)
	 */
	public Time getTime( int columnIndex, Calendar cal ) throws SQLException {
		throw new UnsupportedOperationException();
	}//getTime()
	/**
	 * @see java.sql.ResultSet#getTime(java.lang.String)
	 */
	public Time getTime( String columnName ) throws SQLException {
		return getTime( findColumn( columnName ) );
	}//getTime()
	/**
	 * @see java.sql.ResultSet#getTime(java.lang.String, java.util.Calendar)
	 */
	public Time getTime( String columnName, Calendar cal ) throws SQLException {
		return getTime( findColumn( columnName ), cal );
	}//getTime()
	/**
	 * @see java.sql.ResultSet#getTimestamp(int)
	 */
	public Timestamp getTimestamp( int columnIndex ) throws SQLException {
		return (Timestamp) currentRow().get( columnIndex - 1 );
	}//getTimestamp()
	/**
	 * @see java.sql.ResultSet#getTimestamp(int, java.util.Calendar)
	 */
	public Timestamp getTimestamp( int columnIndex, Calendar cal ) throws SQLException {
		throw new UnsupportedOperationException();
	}//getTimestamp()
	/**
	 * @see java.sql.ResultSet#getTimestamp(java.lang.String)
	 */
	public Timestamp getTimestamp( String columnName ) throws SQLException {
		return getTimestamp( findColumn( columnName ) );
	}//getTimestamp()
	/**
	 * @see java.sql.ResultSet#getTimestamp(java.lang.String, java.util.Calendar)
	 */
	public Timestamp getTimestamp( String columnName, Calendar cal ) throws SQLException {
		return getTimestamp( findColumn( columnName ), cal );
	}//getTimestamp()


	/**
	 * @see java.sql.ResultSet#getConcurrency()
	 */
	public int getConcurrency() throws SQLException {
		throw new UnsupportedOperationException();
	}//getConcurrency()
	/**
	 * @see java.sql.ResultSet#getCursorName()
	 */
	public String getCursorName() throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#getMetaData()
	 */
	public ResultSetMetaData getMetaData() throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#getWarnings()
	 */
	public SQLWarning getWarnings() throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#getStatement()
	 */
	public Statement getStatement() throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#getFetchDirection()
	 */
	public int getFetchDirection() throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#getFetchSize()
	 */
	public int getFetchSize() throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#setFetchDirection(int)
	 */
	public void setFetchDirection(int direction) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#setFetchSize(int)
	 */
	public void setFetchSize(int rows) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#wasNull()
	 */
	public boolean wasNull() throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#getType()
	 */
	public int getType() throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#cancelRowUpdates()
	 */
	public void cancelRowUpdates() throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#clearWarnings()
	 */
	public void clearWarnings() throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#close()
	 */
	public void close() throws SQLException {
		// We don't really have anything to close, so there's nothing to do.
		// Do NOT throw an unsupported exception, because there is no reason to.
	}
	/**
	 * @see java.sql.ResultSet#deleteRow()
	 */
	public void deleteRow() throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#insertRow()
	 */
	public void insertRow() throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#rowDeleted()
	 */
	public boolean rowDeleted() throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#rowInserted()
	 */
	public boolean rowInserted() throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#rowUpdated()
	 */
	public boolean rowUpdated() throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#moveToCurrentRow()
	 */
	public void moveToCurrentRow() throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#moveToInsertRow()
	 */
	public void moveToInsertRow() throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#refreshRow()
	 */
	public void refreshRow() throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateRow()
	 */
	public void updateRow() throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#getAsciiStream(int)
	 */
	public InputStream getAsciiStream( int columnIndex ) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#getBinaryStream(int)
	 */
	public InputStream getBinaryStream( int columnIndex ) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#getUnicodeStream(int)
	 */
	public InputStream getUnicodeStream( int columnIndex ) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#getCharacterStream(int)
	 */
	public Reader getCharacterStream( int columnIndex ) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#getAsciiStream(java.lang.String)
	 */
	public InputStream getAsciiStream( String columnName ) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#getBinaryStream(java.lang.String)
	 */
	public InputStream getBinaryStream( String columnName ) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#getUnicodeStream(java.lang.String)
	 */
	public InputStream getUnicodeStream( String columnName ) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#getCharacterStream(java.lang.String)
	 */
	public Reader getCharacterStream( String columnName ) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateNull(int)
	 */
	public void updateNull( int columnIndex ) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateByte(int, byte)
	 */
	public void updateByte(int columnIndex, byte x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateDouble(int, double)
	 */
	public void updateDouble(int columnIndex, double x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateFloat(int, float)
	 */
	public void updateFloat(int columnIndex, float x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateInt(int, int)
	 */
	public void updateInt(int columnIndex, int x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateLong(int, long)
	 */
	public void updateLong(int columnIndex, long x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateShort(int, short)
	 */
	public void updateShort(int columnIndex, short x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateBoolean(int, boolean)
	 */
	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateBytes(int, byte[])
	 */
	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateAsciiStream(int, java.io.InputStream, int)
	 */
	public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateBinaryStream(int, java.io.InputStream, int)
	 */
	public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateCharacterStream(int, java.io.Reader, int)
	 */
	public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateString(int, java.lang.String)
	 */
	public void updateString(int columnIndex, String x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateNull(java.lang.String)
	 */
	public void updateNull( String columnName ) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateByte(java.lang.String, byte)
	 */
	public void updateByte(String columnName, byte x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateDouble(java.lang.String, double)
	 */
	public void updateDouble(String columnName, double x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateFloat(java.lang.String, float)
	 */
	public void updateFloat(String columnName, float x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateInt(java.lang.String, int)
	 */
	public void updateInt(String columnName, int x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateLong(java.lang.String, long)
	 */
	public void updateLong(String columnName, long x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateShort(java.lang.String, short)
	 */
	public void updateShort(String columnName, short x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateBoolean(java.lang.String, boolean)
	 */
	public void updateBoolean(String columnName, boolean x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateBytes(java.lang.String, byte[])
	 */
	public void updateBytes(String columnName, byte[] x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateBigDecimal(int, java.math.BigDecimal)
	 */
	public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateArray(int, java.sql.Array)
	 */
	public void updateArray(int columnIndex, Array x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateBlob(int, java.sql.Blob)
	 */
	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateClob(int, java.sql.Clob)
	 */
	public void updateClob(int columnIndex, Clob x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateDate(int, java.sql.Date)
	 */
	public void updateDate(int columnIndex, Date x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateRef(int, java.sql.Ref)
	 */
	public void updateRef(int columnIndex, Ref x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateTime(int, java.sql.Time)
	 */
	public void updateTime(int columnIndex, Time x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateTimestamp(int, java.sql.Timestamp)
	 */
	public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateAsciiStream(java.lang.String, java.io.InputStream, int)
	 */
	public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateBinaryStream(java.lang.String, java.io.InputStream, int)
	 */
	public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateCharacterStream(java.lang.String, java.io.Reader, int)
	 */
	public void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateObject(int, java.lang.Object)
	 */
	public void updateObject(int columnIndex, Object x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateObject(int, java.lang.Object, int)
	 */
	public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateObject(java.lang.String, java.lang.Object)
	 */
	public void updateObject(String columnName, Object x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateObject(java.lang.String, java.lang.Object, int)
	 */
	public void updateObject(String columnName, Object x, int scale) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateString(java.lang.String, java.lang.String)
	 */
	public void updateString(String columnName, String x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateBigDecimal(java.lang.String, java.math.BigDecimal)
	 */
	public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateArray(java.lang.String, java.sql.Array)
	 */
	public void updateArray(String columnName, Array x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateBlob(java.lang.String, java.sql.Blob)
	 */
	public void updateBlob(String columnName, Blob x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateClob(java.lang.String, java.sql.Clob)
	 */
	public void updateClob(String columnName, Clob x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateDate(java.lang.String, java.sql.Date)
	 */
	public void updateDate(String columnName, Date x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateRef(java.lang.String, java.sql.Ref)
	 */
	public void updateRef(String columnName, Ref x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateTime(java.lang.String, java.sql.Time)
	 */
	public void updateTime(String columnName, Time x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.sql.ResultSet#updateTimestamp(java.lang.String, java.sql.Timestamp)
	 */
	public void updateTimestamp(String columnName, Timestamp x) throws SQLException {
		throw new UnsupportedOperationException();
	}
	
	
	/**
	 * @see java.util.List#size()
	 */
	public int size() {
		return _rows.size();
	}//size()
	/**
	 * @see java.util.List#clear()
	 */
	public void clear() {
		throw new UnsupportedOperationException();
	}//clear()
	/**
	 * @see java.util.List#isEmpty()
	 */
	public boolean isEmpty() {
		return _rows.isEmpty();
	}//isEmpty()
	/**
	 * @see java.util.List#toArray()
	 */
	public Object[] toArray() {
		return _rows.toArray();
	}//toArray()
	/**
	 * @see java.util.List#get(int)
	 */
	public Object get(int index) {
		return _rows.get( index );
	}//get()
	/**
	 * @see java.util.List#remove(int)
	 */
	public Object remove(int index) {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	public void add(int index, Object element) {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	public int indexOf(Object o) {
		return _rows.indexOf( o );
	}
	/**
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(Object o) {
		return _rows.lastIndexOf( o );
	}
	/**
	 * @see java.util.List#add(java.lang.Object)
	 */
	public boolean add(Object o) {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.util.List#contains(java.lang.Object)
	 */
	public boolean contains(Object o) {
		return _rows.contains( o );
	}
	/**
	 * @see java.util.List#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	public boolean addAll(int index, Collection c) {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection c) {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.util.List#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection c) {
		return _rows.containsAll( c );
	}
	/**
	 * @see java.util.List#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection c) {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.util.List#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection c) {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.util.List#iterator()
	 */
	public Iterator iterator() {
		return _rows.iterator();
	}
	/**
	 * @see java.util.List#subList(int, int)
	 */
	public List subList(int fromIndex, int toIndex) {
		return _rows.subList( fromIndex, toIndex );
	}
	/**
	 * @see java.util.List#listIterator()
	 */
	public ListIterator listIterator() {
		return _rows.listIterator();
	}
	/**
	 * @see java.util.List#listIterator(int)
	 */
	public ListIterator listIterator(int index) {
		return _rows.listIterator( index );
	}
	/**
	 * @see java.util.List#set(int, java.lang.Object)
	 */
	public Object set(int index, Object element) {
		throw new UnsupportedOperationException();
	}
	/**
	 * @see java.util.List#toArray(java.lang.Object[])
	 */
	public Object[] toArray(Object[] a) {
		return _rows.toArray( a );
	}
}