package com.nexusbpm.editor.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.exceptions.YAWLException;

/**
 * Default implementation of the <tt>ExtendedTableModel</tt> interface, providing
 * more functionality than <tt>DefaultTableModel</tt>.
 *
 * @author Daniel Gredler
 * @version $Revision: 1.23 $
 * @created Sept 10, 2004
 * @see ExtendedTableModel
 */
public class DefaultExtendedTableModel extends AbstractTableModel implements ExtendedTableModel {

	private final static Log LOG = LogFactory.getLog( DefaultExtendedTableModel.class );

	private List<List<Object>> data;
	private List<String> columnNames;
	private List<Class> columnClasses;

	/**
	 * Creates an empty table model.
	 *
	 * @throws YAWLException when DefaultExtendedTableModel(List, List, List) does
	 */
	public DefaultExtendedTableModel() throws YAWLException {
		this( new ArrayList<List<Object>>(), new ArrayList<String>(), new ArrayList<Class>() );
	}

	/**
	 * Creates a table model with the given data and columns.
	 *
	 * @param data        the rows of data
	 * @param columnNames the names of the columns
	 * @param columnTypes the <tt>Class</tt>s of the data stored in the columns
	 * @throws YAWLException if the data, number of column names, and number of column types don't match
	 */
	public DefaultExtendedTableModel( List<List<Object>> data, List<String> columnNames, List<Class> columnTypes )
			throws YAWLException {
		if( columnNames.size() != columnTypes.size() ) {
			throw new YAWLException( "Specified " + columnNames.size() + " column names but " + columnTypes.size() + " types!" );
		}
		for( Iterator i = data.iterator(); i.hasNext(); ) {
			List row = (List) i.next();
			if( row.size() != columnNames.size() ) {
				throw new YAWLException( "Specified " + columnNames.size() + " columns, but one of the data rows contains " + row.size() + " columns!" );
			}
		}
		this.data = data;
		this.columnNames = columnNames;
		this.columnClasses = columnTypes;
	}

	/**
	 * Sets the value in the cell at <code>columnIndex</code> and
	 * <code>rowIndex</code> to <code>value</code>.
	 *
	 * @param value       the new value.
	 * @param rowIndex    the row whose value is to be changed
	 * @param columnIndex the column whose value is to be changed
	 * @see #getValueAt(int,int)
	 * @see #isCellEditable(int,int)
	 * @see AbstractTableModel#setValueAt(Object, int, int)
	 */
	public void setValueAt( Object value, int rowIndex, int columnIndex ) {
		List<Object> row;
		while( rowIndex >= data.size() ) {
			row = new Vector<Object>();
			addRow( row );
		}

		row = data.get( rowIndex );

		// MJF - 2005-04-21 This used to be doing an add not a set - which would push the whole array over if there was already a value here.
		// This essentially made trimStrings blow DecisionTree up.

		while( columnIndex >= row.size() ) {
			row.add( null );
		}

		row.set( columnIndex, value );
	}

	/**
	 * Returns the tabular data contained in this table model, as a
	 * <tt>List</tt> of <tt>List</tt>s.
	 *
	 * @return the tabular data contained in this table model.
	 * @see ExtendedTableModel#getData()
	 */
	public List getData() {
		return Collections.unmodifiableList( this.data );
	}

	/**
	 * Returns the value for the cell at <code>columnIndex</code> and
	 * <code>rowIndex</code>.
	 *
	 * @param rowIndex    the row whose value is to be queried
	 * @param columnIndex the column whose value is to be queried
	 * @return the value at the specified cell
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt( int rowIndex, int columnIndex ) {
		if( rowIndex < 0 || rowIndex >= this.data.size() ) return null;
		List row = (List) this.data.get( rowIndex );
		if( columnIndex < 0 || columnIndex >= row.size() ) return null;
		Object val = row.get( columnIndex );
		return val;
	}

	/**
	 * Adds the specified row data to the table.
	 *
	 * @param rowData a row of data as a <tt>List</tt>
	 * @see ExtendedTableModel#addRow(List)
	 */
	public void addRow( List<Object> rowData ) {
		this.data.add( rowData );
	}

	/**
	 * Gets the number of rows in the model.
	 *
	 * @return the number of rows in the model.
	 * @see #getColumnCount()
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return this.data.size();
	}

	/**
	 * Gets the number of columns in the model.
	 *
	 * @return the number of columns in the model.
	 * @see #getRowCount()
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return this.columnNames.size();
	}

	/**
	 * Adds a column with the specified name and sets the value in that column
	 * to the specified value for all existing rows.
	 *
	 * @param columnName the name of the column to add.
	 * @param value      the value to add in the specified column for existing rows
	 * @see ExtendedTableModel#addColumnWithValue(String, Object)
	 */
	public void addColumnWithValue( String columnName, Object value ) {
		Class clazz = value.getClass();
		this.addColumn( columnName, clazz );
		int rows = this.getRowCount();
		int col = this.getColumnCount() - 1;
		for( int row = 0; row < rows; row++ ) {
			this.setValueAt( value, row, col );
		}
	}

	/**
	 * Adds a column with the specified name and of the specified type to this
	 * table.
	 *
	 * @param columnName the name of the column to add.
	 * @param columnType the <tt>Class</tt> of the data that will be in the
	 *                   column.
	 * @see ExtendedTableModel#addColumn(String, Class)
	 */
	public void addColumn( String columnName, Class columnType ) {
		this.addColumn( columnName, columnType, this.columnNames.size() );
	}

	/**
	 * Adds a column with the specified name and of the specified type to this
	 * table at the specified column index.
	 *
	 * @param columnName the name of the column.
	 * @param columnType the <tt>Class</tt> of the data that will be in the
	 *                   column.
	 * @param index      the index where the column should be inserted.
	 * @see ExtendedTableModel#addColumn(String, Class, int)
	 */
	public void addColumn( String columnName, Class columnType, int index ) {
		this.columnNames.add( index, columnName );
		this.columnClasses.add( index, columnType );
		for( Iterator<List<Object>> i = this.data.iterator(); i.hasNext(); ) {
			List<Object> row = i.next();
			row.add( index, null );
		}
	}

	/**
	 * Retrieves a list of the classes of the columns in this table model.
	 *
	 * @return the classes of the columns in this table model.
	 * @see ExtendedTableModel#getColumnClasses()
	 */
	public List<Class> getColumnClasses() {
		return Collections.unmodifiableList( this.columnClasses );
	}

	/**
	 * Returns the most specific superclass for all the cell values in the
	 * column.
	 *
	 * @param column the index of the column.
	 * @return the common ancestor class of the object values in the model.
	 * @see ExtendedTableModel#getColumnClass(int)
	 */
	public Class<?> getColumnClass( int column ) {
		return this.columnClasses.get( column );
	}

	/**
	 * Retrieves the class of the column with the specified name.
	 *
	 * @param columnName the name of the column
	 * @return the <tt>Class</tt> of the specified column
	 * @see ExtendedTableModel#getColumnClass(String)
	 */
	public Class getColumnClass( String columnName ) {
		return getColumnClass( getColumnIndex( columnName ) );
	}

	/**
	 * Returns the names of the columns in this table model.
	 *
	 * @return a <tt>List</tt> of the names of the columns.
	 * @see ExtendedTableModel#getColumnNames()
	 */
	public List<String> getColumnNames() {
		return Collections.unmodifiableList( this.columnNames );
	}

	/**
	 * Returns the name of the column at <code>columnIndex</code>.
	 *
	 * @param column the index of the column.
	 * @return the name of the column.
	 * @see ExtendedTableModel#getColumnName(int)
	 */
	public String getColumnName( int column ) {
		return (String) this.columnNames.get( column );
	}

	/**
	 * Returns <tt>true</tt> if the column at the specified index contains
	 * numeric data.
	 *
	 * @param column the index of the column.
	 * @return whether the column contains numeric data.
	 * @see ExtendedTableModel#isNumeric(int)
	 */
	public boolean isNumeric( int column ) {
		return Number.class.isAssignableFrom( getColumnClass( column ) );
	}

	/**
	 * Returns <tt>true</tt> if the column with the specified name contains
	 * numeric data.
	 *
	 * @param columnName the name of the column.
	 * @return whether the column contains numeric data.
	 * @see ExtendedTableModel#isNumeric(java.lang.String)
	 */
	public boolean isNumeric( String columnName ) {
		Class columnClass = getColumnClass( columnName );
		return Number.class.isAssignableFrom( columnClass );
	}

	/**
	 * Trims all string data in the model.
	 *
	 * @see String#trim()
	 * @see ExtendedTableModel#trimStrings()
	 */
	public void trimStrings() {
		int rows = this.getRowCount();
		int colSize = this.columnClasses.size();
		LOG.info( "DefaultExtendedTableModel - Trimming rows=" + rows + ";cols=" + colSize );
		int numTrim = 0;
		int numValues = 0;
		int numNulls = 0;
		int numNotString = 0;
		for( int col = 0; col < colSize; col++ ) {
			Class clazz = this.columnClasses.get( col );
			if( String.class.isAssignableFrom( clazz ) ) {
				for( int row = 0; row < rows; row++ ) {
					numValues++;
					Object valueAt = this.getValueAt( row, col );
					String value;

					if( null == valueAt ) {
						value = "<<<NULL>>>";
						LOG.debug( "Found null value at (" + row + "," + col + ") while trimming, setting to " + value );
						numNulls++;
					}
					else {
						try {
							value = (String) valueAt;
						}
						catch( ClassCastException e ) {
							value = "";
							LOG.debug( "Could not trim row=" + row + ";col=" + col + ";class=" + valueAt.getClass().getName() );
							numNotString++;
						}

					}

					// optimization - only do setValueAt if value and trim have different lengths
					int valueLength;
					if( null != valueAt && null != value )
						valueLength = value.length();
					else
						valueLength = -1;

					String trimmed = value.trim();
					int trimLength;
					if( null != trimmed )
						trimLength = trimmed.length();
					else
						trimLength = -1;

					if( valueLength != trimLength ) {
						setValueAt( trimmed, row, col );
						numTrim++;
					}
				}
			}
		}

		LOG.info( "DefaultExtendedTableModel - Trimmed rows=" + rows + ";cols=" + colSize
				+ ";numValues=" + numValues + ";numTrimmed=" + numTrim
				+ ";numNull=" + numNulls + ";numNotString=" + numNotString );
	}

	/**
	 * Returns the row data for the given row.
	 *
	 * @param row the index of the row.
	 * @return a <tt>List</tt> of the row data.
	 * @see ExtendedTableModel#getRow(int)
	 */
	public List<Object> getRow( int row ) {
		return this.data.get( row );
	}

	/**
	 * Retrieves the set of the first n of the distinct values in the column at
	 * the specified index. Clears the contents of the passed set and then adds
	 * the values to it instead of creating a new set. The set's iterator will
	 * iterate over the values in the order of their rows within the table
	 * model. Returns all distinct values if maxDistinctValues is -1.<br>
	 *
	 * @param column            the index of the column
	 * @param maxDistinctValues the maximum number of distinct values to return,
	 *                          or -1 for all.
	 * @param distinctValues    the set to store the distinct values in.
	 * @return the set of the first n distinct values
	 * @see ExtendedTableModel#getDistinctValues(int, int, Set)
	 */
	public Set getDistinctValues( int column, int maxDistinctValues, Set<Object> distinctValues ) {
		if( null == distinctValues )
			distinctValues = new LinkedHashSet<Object>();
		else {
			distinctValues.clear();
		}

		int numValues = 0;
		for( Iterator i = this.data.iterator(); i.hasNext(); ) {
			List row = (List) i.next();
			Object value = row.get( column );
			if( null == value )
				value = "<<<NULL>>>";

			if( distinctValues.add( value ) ) {
				numValues++;
				if( maxDistinctValues >= 0 && numValues > maxDistinctValues ) {
					break;
				}
			}
		}

		return distinctValues;
	}

	/**
	 * Retrieves the set of all the distinct values in the column at the
	 * specified index. The returned set's iterator will iterate over the
	 * values in the order of their rows within the table model.
	 *
	 * @param column the index of the column.
	 * @return the set of distinct values in the column.
	 * @see ExtendedTableModel#getDistinctValues(int)
	 */
	public Set getDistinctValues( int column ) {
		return getDistinctValues( column, -1, new LinkedHashSet<Object>() );
	}

	/**
	 * Retrieves the set of the first n of the distinct values in the column
	 * with the specified name. Clears the contents of the passed set and then
	 * adds the values to it instead of creating a new set. The set's iterator
	 * will iterate over the values in the order of their rows within the table
	 * model. Returns all distinct values if maxDistinctValues is -1.
	 *
	 * @param columnName        the name of the column.
	 * @param maxDistinctValues the maximum number of distinct values to return,
	 *                          or -1 for all.
	 * @param distinctValues    the set to store the distinct values in.
	 * @return the set of the first n distinct values
	 * @see ExtendedTableModel#getDistinctValues(String, int, Set)
	 */
	public Set getDistinctValues( String columnName, int maxDistinctValues, Set<Object> distinctValues ) {
		return getDistinctValues( getColumnIndex( columnName ), maxDistinctValues, distinctValues );
	}

	/**
	 * Retrieves the set of all the distinct values in the column with the
	 * specified name. The returned set's iterator will iterate over the values
	 * in the order of their rows within the table model.
	 *
	 * @param columnName the name of the column.
	 * @return the set of distinct values in the column.
	 * @see ExtendedTableModel#getDistinctValues(String)
	 */
	public Set getDistinctValues( String columnName ) {
		return getDistinctValues( columnName, -1, new LinkedHashSet<Object>() );
	}

	/**
	 * Retrieves a filtered subset of the data in this table model, optionally
	 * cloning the data values such that the returned table model's data may be
	 * modified without altering the original data. If <tt>filter</tt> is
	 * <tt>null</tt>, no filter is applied.
	 *
	 * @param filter    the filter to apply.
	 * @param deepClone whether to do a deep copy (clone the data values).
	 * @return an <tt>ExtendedTableModel</tt> of the filtered data.
	 * @throws YAWLException if any of the data cannot be handled.
	 * @see ExtendedTableModel#getFilteredSubset(Filter, boolean)
	 */
	public ExtendedTableModel getFilteredSubset( Filter filter, boolean deepClone )
			throws YAWLException {

		int columnNamesSize = columnNames.size();

		List<List<Object>> filteredData = new ArrayList<List<Object>>();
		for( int i = 0; i < this.data.size(); i++ ) {
			List row = (List) this.data.get( i );
			if( filter != null ) {
				if( !filter.accepts( this, i ) ) {
					continue;
				}
			}
			List<Object> rowClone = new ArrayList<Object>( columnNamesSize );
			int jj = 0;
			for( Iterator j = row.iterator(); j.hasNext() && jj < columnNamesSize; jj++ ) {
				Object val = j.next();
				Object valClone;
				if( null == val ) {
					valClone = new String( "" );
				}
				else if( val instanceof Long ) {
					valClone = new Long( ((Long) val).longValue() );
				}
				else if( val instanceof Integer ) {
					valClone = new Integer( ((Integer) val).intValue() );
				}
				else if( val instanceof Double ) {
					valClone = new Double( ((Double) val).doubleValue() );
				}
				else if( val instanceof Short ) {
					valClone = Short.valueOf( ((Short) val).shortValue() );
				}
				else if( val instanceof String ) {
					valClone = new String( (String) val );
				}
				else if( val instanceof Date ) {
					valClone = new Date( ((Date) val).getTime() );
				}
				else if( val instanceof Boolean ) {
					valClone = Boolean.valueOf( ((Boolean) val).booleanValue() );
				}
				else if( val instanceof BigDecimal ) {
					valClone = new BigDecimal( ((BigDecimal) val).doubleValue() );
				}
				else if( val instanceof BigInteger ) {
					valClone = BigInteger.valueOf( ((BigDecimal) val).longValue() );
				}
				else if( val instanceof Byte ) {
					valClone = new Byte( ((Byte) val).byteValue() );
				}
				else {
					String clazz = val.getClass().getName();
					String msg = "Cannot handle type '" + clazz + "' row=" + i + ", col=" + jj
							+ "(" + columnNames.get( jj ) + ", " + columnClasses.get( jj ) + ").";
					throw new YAWLException( msg );
				}
				rowClone.add( valClone );
			}

			if( rowClone.size() != columnNamesSize ) {
				LOG.info( "getFilteredSubset mismatch i=" + i + ";names=" + columnNamesSize
						+ ";types=" + columnClasses.size() + ";row=" + rowClone.size() );
			}

			filteredData.add( rowClone );
		}

		List<String> names = new ArrayList<String>( columnNames );
		List<Class> types = new ArrayList<Class>( columnClasses );
		DefaultExtendedTableModel clone = new DefaultExtendedTableModel( filteredData, names, types );

		return clone;
	}

	/**
	 * Sorts the rows in the table model in either ascending or descending order
	 * according to the data in the column at the specified index.
	 *
	 * @param column    the index of the column that the rows should be sorted by.
	 * @param ascending whether the data should be sorted in ascending order
	 *                  (<tt>true</tt>) or descending order (<tt>false</tt>).
	 * @see ExtendedTableModel#sortColumn(int, boolean)
	 */
	public void sortColumn( int column, boolean ascending ) {
		Collections.sort( this.data, new ColumnComparator( column, ascending ) );
	}

	/**
	 * Sorts the rows in the table model in either ascending or descending order
	 * according to the data in the column with the specified name.
	 *
	 * @param columnName the name of the column that data should be sorted by
	 * @param ascending  whether the data should be sorted in ascending order
	 *                   (<tt>true</tt>) or descending order (<tt>false</tt>).
	 * @see ExtendedTableModel#sortColumn(String, boolean)
	 */
	public void sortColumn( String columnName, boolean ascending ) {
		sortColumn( getColumnIndex( columnName ), ascending );
	}

	/**
	 * Retrieves the index of the column with the specified name. If there is no
	 * column with the specified name, <tt>-1</tt> is returned.
	 *
	 * @param columnName the name of the column whose index is desired.
	 * @return the index of the column with the given name or <tt>-1</tt> if
	 *         none exists with that name.
	 * @see ExtendedTableModel#getColumnIndex(String)
	 */
	public int getColumnIndex( String columnName ) {
		for( int i = 0; i < this.columnNames.size(); i++ ) {
			if( this.columnNames.get( i ).equals( columnName ) ) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Retrieves a clone of this table model, optionally cloning the data values
	 * such that the returned table model's data may be modified without
	 * altering the original data.
	 *
	 * @param deep whether to make a deep copy (clone the data in the table) or
	 *             not.
	 * @return an <tt>ExtendedTableModel</tt> that is a clone of this table
	 *         model.
	 * @throws YAWLException if any of the data cannot be handled.
	 * @see ExtendedTableModel#getClone(boolean)
	 */
	public ExtendedTableModel getClone( boolean deep ) throws YAWLException {
		return this.getFilteredSubset( null, deep );
	}

	public boolean checkSubset( String[] columnNames ) {
		boolean ret = true;

		try {
			for( int col = 0; col < columnNames.length; col++ ) {
				if( getColumnIndex( columnNames[ col ] ) < 0 ) {
					ret = false;
					break;
				}
			}
		}
		catch( Exception e ) {
			ret = false;
		}

		return ret;
	}

	public ExtendedTableModel initializeSubset( String[] columnNames ) throws YAWLException {
		LOG.debug( "initializeSubset" );
		Vector<String> newColumnNames = new Vector<String>();
		Vector<Class> newColumnTypes = new Vector<Class>();

		String colName;
		int colIndex;
		for( int col = 0; col < columnNames.length; col++ ) {
			colName = columnNames[ col ];
			colIndex = getColumnIndex( colName );
			if( colIndex >= 0 ) {
				newColumnNames.add( colName );
				newColumnTypes.add( getColumnClass( colIndex ) );
			}
		}

		return new DefaultExtendedTableModel( new Vector<List<Object>>(), newColumnNames, newColumnTypes );
	}

	/**
	 * @see ExtendedTableModel#appendRows(ExtendedTableModel)
	 */
	public void appendToSubset( ExtendedTableModel table ) throws YAWLException {
		LOG.debug( "appendToSubset" );
		if( table == null ) return;
		if( table.getColumnCount() == 0 ) return;

		int[] colXref = new int[ getColumnCount() ];
		for( int col = 0; col < colXref.length; col++ ) {
			colXref[ col ] = table.getColumnIndex( getColumnName( col ) );
		}

		// Go ahead and append the rows from the other table.
		for( int i = 0; i < table.getRowCount(); i++ ) {
			List<Object> newRow = new Vector<Object>();
			for( int col = 0; col < colXref.length; col++ ) {
				if( colXref[ col ] < 0 ) {
					newRow.add( null );
				}
				else {
					// LOG.info(i + "," + col + " = " + i + "," + colXref[col] + " = " + table.getValueAt(i, colXref[col]));
					newRow.add( table.getValueAt( i, colXref[ col ] ) );
				}
			}

			addRow( newRow );
		}

		// LOG.info("appendToSubset rowCount=" + table.getRowCount() + " table:\n" + toString());
	}

	/**
	 * Appends all the rows in the specified table to this table's rows. The
	 * column names and column types of both tables must match, or this method
	 * will throw an exception, unless the table passed in has zero columns,
	 * in which case this method just returns. If the specified table is
	 * <tt>null</tt> this method also returns immediately. If both tables have
	 * matching columns except one of them has extra columns, those columns
	 * will be added to the other table.
	 *
	 * @param table the table containing the rows to add to this table.
	 * @throws YAWLException if the columns don't match.
	 * @see ExtendedTableModel#appendRows(ExtendedTableModel)
	 */
	public void appendRows( ExtendedTableModel table ) throws YAWLException {
		if( table == null ) return;
		if( table.getColumnCount() == 0 ) return;
		int count1 = this.getColumnCount();
		int count2 = table.getColumnCount();
		if( count1 < count2 ) {
			// This table is missing columns that the other table contains. Add the missing columns to this table.
			for( int i = 0; i < count2; i++ ) {
				String name1 = (i < this.getColumnCount() ? this.getColumnName( i ) : null);
				String name2 = table.getColumnName( i );
				if( !name2.equals( name1 ) ) {
					this.addColumn( table.getColumnName( i ), table.getColumnClass( i ), i );
				}
			}
		}
		else if( count2 < count1 ) {
			// The other table is missing columns that this table contains. Add the missing columns to the other table.
			for( int i = 0; i < count1; i++ ) {
				String name1 = this.getColumnName( i );
				String name2 = (i < table.getColumnCount() ? table.getColumnName( i ) : null);
				if( !name1.equals( name2 ) ) {
					table.addColumn( this.getColumnName( i ), this.getColumnClass( i ), i );
				}
			}
		}
		// Make sure all the column names match.
		for( int i = 0; i < count1; i++ ) {
			String name1 = this.getColumnName( i );
			String name2 = table.getColumnName( i );
			if( !name1.equals( name2 ) ) {
				throw new YAWLException( "This table has column '" + name1 + "' at index " + i + ", but the specified table has column '" + name2 + "'." );
			}
		}
		// Make sure all the column types match.
		for( int i = 0; i < count1; i++ ) {
			String name1 = this.getColumnClass( i ).getName();
			String name2 = table.getColumnClass( i ).getName();
			if( !name1.equals( name2 ) ) {
				throw new YAWLException( "This table has a column of type '" + name1 + "' at index " + i + ", but the specified table has a column of type '" + name2 + "'." );
			}
		}
		// Go ahead and append the rows from the other table.
		for( int i = 0; i < table.getRowCount(); i++ ) {
			List<Object> row = table.getRow( i );
			this.addRow( row );
		}
	}

	/**
	 * Does a cross join between this table and the specified table and returns
	 * the resultant table. A cross join returns the cross product (or Cartesian
	 * product) of the two tables, so if this table contains 5 rows and the
	 * specified table contains 10 rows, the resultant table will contain 50
	 * rows.
	 *
	 * @param table the table to cross join with this table.
	 * @return the table that is a result of cross-joining two tables.
	 * @throws YAWLException if there is an error in cross-joining the two
	 *                          tables.
	 * @see ExtendedTableModel#crossJoin(ExtendedTableModel)
	 */
	public ExtendedTableModel crossJoin( ExtendedTableModel table )
			throws YAWLException {

		List<List<Object>> newData = new ArrayList<List<Object>>();
		for( Iterator<List<Object>> iter = this.data.iterator(); iter.hasNext(); ) {
			List<Object> row = iter.next();
			for( int j = 0; j < table.getRowCount(); j++ ) {
				List<Object> newRow = new ArrayList<Object>( row );
				List<Object> otherRow = table.getRow( j );
				newRow.addAll( otherRow );
				newData.add( newRow );
			}
		}

		List<String> newNames = new ArrayList<String>();
		newNames.addAll( this.getColumnNames() );
		newNames.addAll( table.getColumnNames() );

		List<Class> newTypes = new ArrayList<Class>();
		newTypes.addAll( this.getColumnClasses() );
		newTypes.addAll( table.getColumnClasses() );

		ExtendedTableModel newETM = new DefaultExtendedTableModel( newData, newNames, newTypes );
		return newETM;
	}

	/**
	 * @see ExtendedTableModel#normalizeHeaders()
	 */
	public void normalizeHeaders() {
		if( this.getRowCount() < 2 ) return;
		if( this.getColumnCount() < 2 ) return;
		Object lastHeaderVal = null;
		int lastHeaderIndex = -1;
		for( Iterator<List<Object>> i = this.data.iterator(); i.hasNext(); ) {
			List<Object> row = i.next();
			int[] notNullIndices = this.getIndices( row, false );
			int[] nullIndices = this.getIndices( row, true );
			if( notNullIndices.length == 1 && (lastHeaderIndex == -1 || lastHeaderIndex == notNullIndices[ 0 ]) ) {
				// This is a header row.
				for( int j = 0; j < row.size(); j++ ) {
					Object obj = row.get( j );
					if( obj != null ) {
						lastHeaderVal = obj;
						lastHeaderIndex = j;
						break;
					}
				}
				i.remove();
			}
			else if( nullIndices.length == 1 && nullIndices[ 0 ] == lastHeaderIndex ) {
				// This is a subrow for the last header.
				row.set( nullIndices[ 0 ], lastHeaderVal );
			}
		}
	}
	/**
	 * Retrieves the indices of the items in the specified <tt>List</tt> that are
	 * <tt>null</tt> if <tt>isNull</tt> is <tt>true</tt>, or the indices of the items
	 * that are not <tt>null</tt> if <tt>isNull</tt> is <tt>false</tt>.
	 */
	private int[] getIndices( List v, boolean isNull ) {
		int count = 0;
		int[] indices = new int[ v.size() ];
		for( int i = 0; i < v.size(); i++ ) {
			Object o = v.get( i );
			if( (o == null) == isNull ) {
				indices[ count ] = i;
				count++;
			}
		}
		int[] indices2 = new int[ count ];
		for( int i = 0; i < count; i++ ) {
			indices2[ i ] = indices[ i ];
		}
		return indices2;
	}

	/**
	 * Retrieves the tabular data as a string, with columns delimited using the
	 * specified column delimeter and optionally prepending column headers at
	 * the top. The row delimiter defaults to "\n".
	 *
	 * @param columnDelimiter the <tt>String</tt> to use as a column delimiter.
	 * @param headers         whether to prepend column headers at the top.
	 * @return the tabular data as a <tt>String</tt>.
	 * @see ExtendedTableModel#getCSV(String, boolean)
	 */
	public String getCSV( String columnDelimiter, boolean headers ) {
		return this.getCSV( "\n", columnDelimiter, headers );
	}

	/**
	 * Retrieves the tabular data as a string, with rows delimited using the
	 * specified row delimiter, columns delimited using the specified column
	 * delimeter and optionally prepending column headers at the top.
	 *
	 * @param rowDelimiter    the <tt>String</tt> to use as a row delimiter.
	 * @param columnDelimiter the <tt>String</tt> to use as a column delimiter.
	 * @param headers         whether to prepend column headers at the top.
	 * @return the tabular data as a <tt>String</tt>.
	 * @see ExtendedTableModel#getCSV(String, String, boolean)
	 */
	public String getCSV( String rowDelimiter, String columnDelimiter, boolean headers ) {
		StringBuffer s = new StringBuffer();
		if( headers ) {
			for( Iterator i = this.columnNames.iterator(); i.hasNext(); ) {
				Object name = i.next();
				s.append( name );
				if( i.hasNext() ) s.append( columnDelimiter );
			}
			s.append( rowDelimiter );
		}
		for( Iterator i = this.data.iterator(); i.hasNext(); ) {
			List row = (List) i.next();
			for( Iterator j = row.iterator(); j.hasNext(); ) {
				Object val = j.next();
				s.append( val );
				if( j.hasNext() ) s.append( columnDelimiter );
			}
			s.append( rowDelimiter );
		}
		return s.toString();
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "DefaultExtendedTableModel(rows=" + this.data.size() + ", data=" + this.data.toString() + ")";
	}

	/**
	 * Private comparator class used in <tt>sortColumn()</tt>.
	 */
	private class ColumnComparator implements Comparator<List<Object>> {
		private int index;
		private boolean ascending;
		private ComparatorHelper comparatorHelper = new ComparatorHelper();
		/**
		 * Creates a new <tt>ColumnComparator</tt> on the given column.
		 *
		 * @param index     the index of the column
		 * @param ascending whether to compare in ascending order
		 */
		public ColumnComparator( int index, boolean ascending ) {
			this.index = index;
			this.ascending = ascending;
		}
		/**
		 * Compares two rows.
		 *
		 * @param rowOne one of the rows to compare
		 * @param rowTwo the other row to compare
		 * @return the comparison result of the two rows.
		 */
		public int compare( List<Object> rowOne, List<Object> rowTwo ) {
			// get the data from the appropriate column
			Object oOne = rowOne.get( index );
			Object oTwo = rowTwo.get( index );

			// compare the data from the two rows
			int ret = comparatorHelper.compare( oOne, oTwo );
/*
			// if both are null or both are not Comparable then they are equal enough
			int ret;
			if (null == oOne) {
				if ( null == oTwo)
					ret = 0;
				else
					ret = -1;
			}
			else if (null == oTwo)
				ret = 1;
			else {
				if (!(oOne instanceof Comparable)) {
					if (!(oTwo instanceof Comparable))
						ret = 0;
					else
						ret = -1;
				}
				else if (!(oTwo instanceof Comparable))
					ret = 1;
				else {
					Comparable cOne = (Comparable) oOne;
					Comparable cTwo = (Comparable) oTwo;
					ret = cOne.compareTo( cTwo );
				}
			}
*/
			if( !ascending )
				ret = -ret;

			return ret;
		}
	}

	/**
	 * Returns the sum of all numeric data in the specified column.
	 *
	 * @param col the index of the column.
	 * @return the sum of all values in the specified column.
	 */
	public double columnTotal( int col ) {
		double ret = 0.0;
		int rowCount = getRowCount();
		Object value;
		for( int i = 0; i < rowCount; i++ ) {
			value = getValueAt( i, col );
			try {
				ret += Double.parseDouble( value.toString() );
			}
			catch( NumberFormatException e ) {
			}
		}
		return ret;
	}

	/**
	 * Returns the mathematical variance of the values in the specified column.
	 *
	 * @param col the index of the column.
	 * @return the mathematical variance of the values in the specified column.
	 */
	public double variance( int col ) {
		double sum = 0.0;
		int rowCount = getRowCount();
		double colTotal = columnTotal( col );
		double mean = colTotal / rowCount;
		Object value;
		for( int i = 0; i < rowCount; i++ ) {
			value = getValueAt( i, col );
			try {
				sum += Math.pow( Double.parseDouble( value.toString() ) - mean, 2 );
			}
			catch( NumberFormatException e ) {
			}
		}

		return sum / rowCount;
	}

	/**
	 * Main test method.
	 *
	 * @param args unused
	 * @throws YAWLException when table operations fail
	 */
	public static void main( String[] args ) throws YAWLException {

		System.out.println( "Starting tests..." );
		AssertionError ae = null;
		try {
			assert true == false;
		}
		catch( AssertionError e ) {
			ae = e;
		}
		if( ae == null ) {
			System.out.println( "Assertions are NOT enabled! Use -enableassertions to enable them." );
			System.exit( 1 );
		}

		List<List<Object>> data = new ArrayList<List<Object>>();

		List<Object> row1 = new ArrayList<Object>();
		List<Object> row2 = new ArrayList<Object>();
		List<Object> row3 = new ArrayList<Object>();
		List<Object> row4 = new ArrayList<Object>();

		row1.add( "a" );
		row1.add( new Integer( 4 ) );
		row1.add( "z" );
		row2.add( "b" );
		row2.add( new Integer( 1 ) );
		row2.add( "a" );
		row3.add( "c" );
		row3.add( new Integer( 2 ) );
		row3.add( "z" );
		row4.add( "a" );
		row4.add( new Integer( 3 ) );
		row4.add( "z" );

		data.add( row1 );
		data.add( row2 );
		data.add( row3 );
		data.add( row4 );

		List<String> columnNames = new ArrayList<String>();
		columnNames.add( "Column 1" );
		columnNames.add( "Column 2" );
		columnNames.add( "Column 3" );

		List<Class> columnTypes = new ArrayList<Class>();
		columnTypes.add( String.class );
		columnTypes.add( Integer.class );
		columnTypes.add( String.class );

		DefaultExtendedTableModel model = new DefaultExtendedTableModel( data, columnNames, columnTypes );

		assert model.isNumeric( 0 ) == false;
		assert model.isNumeric( 1 ) == true;
		assert model.isNumeric( 2 ) == false;
		assert model.isNumeric( "Column 1" ) == false;
		assert model.isNumeric( "Column 2" ) == true;
		assert model.isNumeric( "Column 3" ) == false;

		List names = model.getColumnNames();
		assert names.get( 0 ).equals( "Column 1" );
		assert names.get( 1 ).equals( "Column 2" );
		assert names.get( 2 ).equals( "Column 3" );

		assert model.getDistinctValues( 0 ).size() == 3;
		assert model.getDistinctValues( 1 ).size() == 4;
		assert model.getDistinctValues( 2 ).size() == 2;
		assert model.getDistinctValues( "Column 1" ).size() == 3;
		assert model.getDistinctValues( "Column 2" ).size() == 4;
		assert model.getDistinctValues( "Column 3" ).size() == 2;

		assert model.getColumnIndex( "Column 1" ) == 0;
		assert model.getColumnIndex( "Column 2" ) == 1;
		assert model.getColumnIndex( "Column 3" ) == 2;

		model.sortColumn( 0, true );
		assert model.getValueAt( 0, 0 ).equals( "a" );
		assert model.getValueAt( 1, 0 ).equals( "a" );
		assert model.getValueAt( 2, 0 ).equals( "b" );
		assert model.getValueAt( 3, 0 ).equals( "c" );
		assert model.getValueAt( 0, 1 ).equals( new Integer( 4 ) );
		assert model.getValueAt( 1, 1 ).equals( new Integer( 3 ) );
		assert model.getValueAt( 2, 1 ).equals( new Integer( 1 ) );
		assert model.getValueAt( 3, 1 ).equals( new Integer( 2 ) );
		assert model.getValueAt( 0, 2 ).equals( "z" );
		assert model.getValueAt( 1, 2 ).equals( "z" );
		assert model.getValueAt( 2, 2 ).equals( "a" );
		assert model.getValueAt( 3, 2 ).equals( "z" );

		model.sortColumn( "Column 2", true );
		assert model.getValueAt( 0, 0 ).equals( "b" );
		assert model.getValueAt( 1, 0 ).equals( "c" );
		assert model.getValueAt( 2, 0 ).equals( "a" );
		assert model.getValueAt( 3, 0 ).equals( "a" );
		assert model.getValueAt( 0, 1 ).equals( new Integer( 1 ) );
		assert model.getValueAt( 1, 1 ).equals( new Integer( 2 ) );
		assert model.getValueAt( 2, 1 ).equals( new Integer( 3 ) );
		assert model.getValueAt( 3, 1 ).equals( new Integer( 4 ) );
		assert model.getValueAt( 0, 2 ).equals( "a" );
		assert model.getValueAt( 1, 2 ).equals( "z" );
		assert model.getValueAt( 2, 2 ).equals( "z" );
		assert model.getValueAt( 3, 2 ).equals( "z" );

		model.sortColumn( "Column 3", true );
		assert model.getValueAt( 0, 0 ).equals( "b" );
		assert model.getValueAt( 1, 0 ).equals( "c" );
		assert model.getValueAt( 2, 0 ).equals( "a" );
		assert model.getValueAt( 3, 0 ).equals( "a" );
		assert model.getValueAt( 0, 1 ).equals( new Integer( 1 ) );
		assert model.getValueAt( 1, 1 ).equals( new Integer( 2 ) );
		assert model.getValueAt( 2, 1 ).equals( new Integer( 3 ) );
		assert model.getValueAt( 3, 1 ).equals( new Integer( 4 ) );
		assert model.getValueAt( 0, 2 ).equals( "a" );
		assert model.getValueAt( 1, 2 ).equals( "z" );
		assert model.getValueAt( 2, 2 ).equals( "z" );
		assert model.getValueAt( 3, 2 ).equals( "z" );

		Filter filter1 = new BasicFilter( "Column 3", Operator.EQUAL_TO, "z" );
		ExtendedTableModel subset = model.getFilteredSubset( filter1, true );
		assert subset.getRowCount() == 3;

		filter1 = new BasicFilter( "Column 3", Operator.EQUAL_TO, "a" );
		subset = model.getFilteredSubset( filter1, false );
		assert subset.getRowCount() == 1;

		filter1 = new BasicFilter( "Column 3", Operator.EQUAL_TO, "q" );
		subset = model.getFilteredSubset( filter1, false );
		assert subset.getRowCount() == 0;

		filter1 = new BasicFilter( "Column 2", Operator.LESS_THAN_OR_EQUAL_TO, new Integer( 2 ) );
		subset = model.getFilteredSubset( filter1, true );
		assert subset.getRowCount() == 2;

		filter1 = new BasicFilter( "Column 2", Operator.GREATER_THAN_OR_EQUAL_TO, new Integer( 2 ) );
		subset = model.getFilteredSubset( filter1, false );
		assert subset.getRowCount() == 3;

		filter1 = new BasicFilter( "Column 2", Operator.GREATER_THAN_OR_EQUAL_TO, new Integer( 2 ) );
		Filter filter2 = new BasicFilter( "Column 3", Operator.GREATER_THAN_OR_EQUAL_TO, "a" );
		Filter orFilter = new OrFilter( filter1, filter2 );
		subset = model.getFilteredSubset( orFilter, true );
		assert subset.getRowCount() == 4;

		filter1 = new BasicFilter( "Column 2", Operator.EQUAL_TO, new Integer( 2 ) );
		filter2 = new BasicFilter( "Column 3", Operator.EQUAL_TO, "a" );
		orFilter = new OrFilter( filter1, filter2 );
		subset = model.getFilteredSubset( orFilter, false );
		assert subset.getRowCount() == 2;

		filter1 = new BasicFilter( "Column 1", Operator.EQUAL_TO, "a" );
		filter2 = new BasicFilter( "Column 3", Operator.EQUAL_TO, "z" );
		Filter andFilter = new AndFilter( filter1, filter2 );
		subset = model.getFilteredSubset( andFilter, true );
		assert subset.getRowCount() == 2;

		filter1 = new BasicFilter( "Column 1", Operator.EQUAL_TO, "c" );
		filter2 = new BasicFilter( "Column 2", Operator.EQUAL_TO, new Integer( 3 ) );
		andFilter = new AndFilter( filter1, filter2 );
		subset = model.getFilteredSubset( andFilter, false );
		assert subset.getRowCount() == 0;

		System.out.println( "Tests passed!" );
	}

}