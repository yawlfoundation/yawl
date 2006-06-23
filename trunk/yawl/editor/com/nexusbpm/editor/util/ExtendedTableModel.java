package com.nexusbpm.editor.util;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.exceptions.YAWLException;

/**
 * Extension to the <tt>TableModel</tt> interface which provides extra
 * functionality, such as determining whether a column contains numeric
 * data, determining the datatype in a column based on the column name
 * or the column index, retrieving a list of distinct values within a
 * specific column, sorting on a specific column, retrieving the index
 * of a column based on the column name, cloning the model, and data
 * filtering.
 *
 * @author Daniel Gredler
 * @version $Revision: 1.21 $
 * @created Sept 10, 2004
 */
public interface ExtendedTableModel extends TableModel {

	/**
	 * Returns the tabular data contained in this table model, as a <tt>List</tt>
	 * of <tt>List</tt>s.
	 *
	 * @return the tabular data contained in this table model.
	 */
	public List getData();

	/**
	 * Adds the specified row data to the table.
	 *
	 * @param rowData a row of data as a <tt>List</tt>
	 */
	public void addRow( List<Object> rowData );

	/**
	 * Retrieves the classes of the columns in this table model.
	 *
	 * @return the classes of the columns in this table model.
	 */
	public List<Class> getColumnClasses();

	/**
	 * Retrieves the <tt>Class</tt> of the column at the specified index.
	 *
	 * @param column the index of the column
	 * @return the <tt>Class</tt> of the specified column
	 */
	public Class<?> getColumnClass( int column );

	/**
	 * Retrieves the class of the column with the specified name.
	 *
	 * @param columnName the name of the column
	 * @return the <tt>Class</tt> of the specified column
	 */
	public Class getColumnClass( String columnName );

	/**
	 * Returns the names of the columns in this table model.
	 *
	 * @return a <tt>List</tt> of the names of the columns
	 */
	public List<String> getColumnNames();

	/**
	 * Retrieves the name of the column at the specified index.
	 *
	 * @param column the index of the column
	 * @return the name of the column
	 */
	public String getColumnName( int column );

	/**
	 * Returns <tt>true</tt> if the column at the specified index contains
	 * numeric data.
	 *
	 * @param column the index of the column
	 * @return whether the column contains numeric data.
	 */
	public boolean isNumeric( int column );

	/**
	 * Returns <tt>true</tt> if the column with the specified name contains
	 * numeric data.
	 *
	 * @param columnName the name of the column
	 * @return whether the column contains numeric data
	 */
	public boolean isNumeric( String columnName );

	/**
	 * Trims all string data in the model.
	 *
	 * @see String#trim()
	 */
	public void trimStrings();

	/**
	 * Returns the row data for the given row.
	 *
	 * @param row the index of the row
	 * @return a <tt>List</tt> of the row data
	 */
	public List<Object> getRow( int row );

	/**
	 * Retrieves the set of all the distinct values in the column at the specified
	 * index. The returned set's iterator will iterate over the values in the order
	 * of their rows within the table model.
	 *
	 * @param column the index of the column
	 * @return the set of distinct values in the column
	 */
	public Set getDistinctValues( int column );

	/**
	 * Retrieves the set of the first n of the distinct values in the column at the
	 * specified index. Clears the contents of the passed set and then adds the
	 * values to it instead of creating a new set. The set's iterator will iterate
	 * over the values in the order of their rows within the table model. Returns
	 * all distinct values if maxDistinctValues is -1.
	 *
	 * @param column            the index of the column
	 * @param maxDistinctValues the maximum number of distinct values to return, or -1 for all.
	 * @param distinctValues    the set to store the distinct values in
	 * @return the set of up to n distinct values
	 */
	public Set getDistinctValues( int column, int maxDistinctValues, Set<Object> distinctValues );

	/**
	 * Retrieves the set of all the distinct values in the column with the specified
	 * name. The returned set's iterator will iterate over the values in the order
	 * of their rows within the table model.
	 *
	 * @param columnName the name of the column
	 * @return the set of distinct values in the column
	 */
	public Set getDistinctValues( String columnName );

	/**
	 * Retrieves the set of the first n of the distinct values in the column with
	 * the specified name. Clears the contents of the passed set and then adds the
	 * values to it instead of creating a new set. The set's iterator will iterate
	 * over the values in the order of their rows within the table model. Returns
	 * all distinct values if maxDistinctValues is -1.
	 *
	 * @param columnName        the name of the column
	 * @param maxDistinctValues the maximum number of distinct values to return, or -1 for all.
	 * @param distinctValues    the set to store the distinct values in
	 * @return the set of up to n distinct values
	 */
	public Set getDistinctValues( String columnName, int maxDistinctValues, Set<Object> distinctValues );

	/**
	 * Retrieves a filtered subset of the data in this table model, optionally cloning
	 * the data values such that the returned table model's data may be modified without
	 * altering the original data. If <tt>filter</tt> is <tt>null</tt>, no filter is
	 * applied.
	 *
	 * @param filter    the filter to apply
	 * @param deepClone whether to do a deep copy (clone the data values)
	 * @return an <tt>ExtendedTableModel</tt> of the filtered data
	 * @throws YAWLException if any of the data cannot be handled
	 */
	public ExtendedTableModel getFilteredSubset( Filter filter, boolean deepClone ) throws YAWLException;

	/**
	 * Adds a column with the specified name and with the specified value in the new
	 * cells created.
	 *
	 * @param columnName the name of the column to add
	 * @param value      the value to add in the specified column for all existing rows
	 */
	public void addColumnWithValue( String columnName, Object value );

	/**
	 * Adds a column with the specified name and of the specified type to this table.
	 *
	 * @param columnName the name of the column to add
	 * @param columnType the <tt>Class</tt> of the data that will be in the column
	 */
	public void addColumn( String columnName, Class columnType );

	/**
	 * Adds a column with the specified name and of the specified type to this table at
	 * the specified column index.
	 *
	 * @param columnName the name of the column
	 * @param columnType the <tt>Class</tt> of the data that will be in the column
	 * @param index      the index where the column should be inserted
	 */
	public void addColumn( String columnName, Class columnType, int index );

	/**
	 * Sorts the rows in the table model in either ascending or descending order
	 * according to the data in the column at the specified index.
	 *
	 * @param column    the index of the column that data should be sorted by
	 * @param ascending whether the data should be sorted in ascending order
	 *                  (<tt>true</tt>) or descending order (<tt>false</tt>).
	 */
	public void sortColumn( int column, boolean ascending );

	/**
	 * Sorts the rows in the table model in either ascending or descending order
	 * according to the data in the column with the specified name.
	 *
	 * @param columnName the name of the column that data should be sorted by
	 * @param ascending  whether the data should be sorted in ascending order
	 *                   (<tt>true</tt>) or descending order (<tt>false</tt>).
	 */
	public void sortColumn( String columnName, boolean ascending );

	/**
	 * Retrieves the index of the column with the specified name. If there is no
	 * column with the specified name, <tt>-1</tt> is returned.
	 *
	 * @param columnName the name of the column whose index is desired
	 * @return the index of the column with the given name or <tt>-1</tt> if none exists with that name
	 */
	public int getColumnIndex( String columnName );

	/**
	 * Retrieves a clone of this table model, optionally cloning the data values such
	 * that the returned table model's data may be modified without altering the original
	 * data.
	 *
	 * @param deep whether to make a deep copy (clone the data in the table) or not
	 * @return an <tt>ExtendedTableModel</tt> that is a clone of this table model
	 * @throws YAWLException if the table contains data that it cannot handle.
	 */
	public ExtendedTableModel getClone( boolean deep ) throws YAWLException;

	/**
	 * Appends all the rows in the specified table to this table's rows. The column names
	 * and column types of both tables must match, or this method will throw an exception,
	 * unless the table passed in has zero columns, in which case this method just returns.
	 * If the specified table is <tt>null</tt> this method also returns immediately.
	 *
	 * @param table the table containing the rows to add to this table
	 * @throws YAWLException if the columns don't match
	 */
	public void appendRows( ExtendedTableModel table ) throws YAWLException;

	public void appendToSubset( ExtendedTableModel table ) throws YAWLException;

	public ExtendedTableModel initializeSubset( String[] columnNames ) throws YAWLException;

	public boolean checkSubset( String[] columnNames );

	/**
	 * Does a cross join between this table and the specified table and returns the
	 * resultant table. A cross join returns the cross product (or Cartesian product)
	 * of the two tables, so if this table contains 5 rows and the specified table
	 * contains 10 rows, the resultant table will contain 50 rows.
	 *
	 * @param table the table to cross join with this table
	 * @return the table that is a result of cross-joining two tables
	 * @throws YAWLException if there is an error in cross-joining the two tables
	 */
	public ExtendedTableModel crossJoin( ExtendedTableModel table ) throws YAWLException;

	/**
	 * If this table contains some rows with values in only one column, and other rows with
	 * values in all columns except said column, then most likely the rows with only one value
	 * are headers. The header values are moved into the other rows, and the header rows are
	 * deleted.
	 *
	 * <pre>
	 * -----------------------------          -----------------------------
	 * |  h1  | NULL | NULL | NULL |          |          DELETED          |
	 * -----------------------------          -----------------------------
	 * | NULL |  ab  |  xy  |  hj  |          |  h1  |  ab  |  xy  |  hj  |
	 * -----------------------------          -----------------------------
	 * | NULL |  cd  |  rt  |  yu  |    -->   |  h1  |  cd  |  rt  |  yu  |
	 * -----------------------------          -----------------------------
	 * |  h2  | NULL | NULL | NULL |          |          DELETED          |
	 * -----------------------------          -----------------------------
	 * | NULL |  ie  |  vb  |  qw  |          |  h2  |  ie  |  vb  |  qw  |
	 * -----------------------------          -----------------------------
	 * </pre>
	 */
	public void normalizeHeaders();

	/**
	 * Retrieves the tabular data as a string, with columns delimited using the specified
	 * column delimeter and optionally prepending column headers at the top. The row delimiter
	 * defaults to "\n".
	 *
	 * @param columnDelimiter the <tt>String</tt> to use as a column delimiter
	 * @param headers         whether to prepend column headers at the top
	 * @return the tabular data as a <tt>String</tt>
	 */
	public String getCSV( String columnDelimiter, boolean headers );

	/**
	 * Retrieves the tabular data as a string, with rows delimited using the specified row
	 * delimiter, columns delimited using the specified column delimeter and optionally
	 * prepending column headers at the top.
	 *
	 * @param rowDelimiter    the <tt>String</tt> to use as a row delimiter
	 * @param columnDelimiter the <tt>String</tt> to use as a column delimiter
	 * @param headers         whether to prepend column headers at the top
	 * @return the tabular data as a <tt>String</tt>
	 */
	public String getCSV( String rowDelimiter, String columnDelimiter, boolean headers );

	/**
	 * Gets total sum of values in column
	 *
	 * @param col
	 * @return column sum value
	 */
	public double columnTotal( int col );

	/**
	 * Gets variance of values in column
	 *
	 * @param col
	 * @return column variance
	 */
	public double variance( int col );

	/**
	 * Filters are used to filter <tt>ExtendedTableModel</tt>s into data subsets.
	 *
	 * @see ExtendedTableModel#getFilteredSubset(Filter, boolean)
	 */
	public abstract class Filter implements Serializable {

		/**
		 * The log for the filter.
		 */
		protected static final transient Log LOG = LogFactory.getLog( Filter.class );

		/**
		 * Returns whether this filter accepts or rejects the given row of the given
		 * <tt>ExtendedTableModel</tt>.
		 *
		 * @param model the model to check
		 * @param row   the row to check
		 * @return whether the given row of the given model is acceptable to this filter
		 * @throws YAWLException if the filter encounters an error
		 */
		public abstract boolean accepts( ExtendedTableModel model, int row ) throws YAWLException;

		/**
		 * Returns a <tt>Filter</tt> that performs a logical OR with this filter and
		 * the given filter. The returned filter will accept any row that either this
		 * filter or the given filter will accept.
		 *
		 * @param filter the filter to perform a logical OR with
		 * @return a <tt>Filter</tt> that performs a logical OR of this filter and the given filter
		 */
		public Filter or( Filter filter ) {
			return new OrFilter( this, filter );
		}

		/**
		 * Returns a <tt>Filter</tt> that performs a logical AND with this filter and
		 * the given filter. The returned filter will accept only rows that both this
		 * filter and the given filter will accept.
		 *
		 * @param filter the filter to perform a logical AND with
		 * @return a <tt>Filter</tt> that performs a logical AND of this filter and the given filter
		 */
		public Filter and( Filter filter ) {
			return new AndFilter( this, filter );
		}

		/**
		 * Appends the comparison this filter makes to the end of the given SQL query
		 * as a "where" clause.
		 *
		 * @param sql the SQL query to have this filter appended to
		 * @return the new SQL query with the "where" clause appended
		 */
		public String appendWhereClause( String sql ) {
			StringBuffer s = new StringBuffer( sql );
			int whereIndex = sql.toLowerCase().indexOf( " where " );
			if( whereIndex == -1 ) {
				s.append( " where " );
			}
			else {
				s.append( " and " );
			}
			s.append( this.toString() );
			return s.toString();
		}

		protected String stringHelper( String filter1, String key, String filter2 ) {

			StringBuffer buf = new StringBuffer();

			if( null == filter1 || filter1.length() == 0 ) {
				if( null == filter2 || filter2.length() == 0 ) {
					buf.append( "" );
				}
				else {
					buf.append( filter2 );
				}
			}
			else if( null == filter2 || filter2.length() == 0 ) {
				buf.append( filter1 );
			}
			else if( filter1.equals( filter2 ) ) {
				buf.append( filter1 );
			}
			else {
				buf.append( "(" );
				buf.append( filter1 );
				buf.append( " " );
				buf.append( key );
				buf.append( " " );
				buf.append( filter2 );
				buf.append( ")" );
			}

			return buf.toString();
		}

	}

	/**
	 * A basic filter that defines a column to filter on, a filter operation, and
	 * a value to filter against.
	 */
	public class BasicFilter extends Filter implements Serializable {

		private int column;

		private String columnName;

		private Operator comparison;

		private Object comparable; // Boolean is not Comparable - weird huh?

		private static final ComparatorHelper comparatorHelper = new ComparatorHelper();

		/**
		 * Creates a basic filter for a column of data. Compares the item in that
		 * column for any given row with <tt>comparable</tt> using <tt>comparison</tt>
		 * as in operator.
		 *
		 * @param column     the index of the column to use to retrive data for comparison
		 * @param comparison the operator of the comparison
		 * @param comparable the data that the table data is compared to
		 */
		public BasicFilter( int column, Operator comparison, Object comparable ) {
			this.column = column;
			this.comparison = comparison;
			this.comparable = comparable;
		}

		/**
		 * Creates a basic filter for a column of data. Compares the item in that
		 * column for any given row with <tt>comparable</tt> using <tt>comparison</tt>
		 * as in operator.
		 *
		 * @param columnName the name of the column to use to retrive data for comparison
		 * @param comparison the operator of the comparison
		 * @param comparable the data that the table data is compared to
		 */
		public BasicFilter( String columnName, Operator comparison, Object comparable ) {
			this.columnName = columnName;
			this.comparison = comparison;
			this.comparable = comparable;
		}

		/**
		 * Returns whether this filter accepts or rejects the given row of the given
		 * <tt>ExtendedTableModel</tt>. Data from the table in the column specified
		 * in the constructor at the row specified here will be compared to data
		 * given to the constructor using the operator given to the constructor to
		 * determine whether the given row is accepted.
		 *
		 * @param model the model to check
		 * @param row   the row to check
		 * @return whether the given row of the given model is acceptable to this filter
		 * @throws YAWLException if the filter encounters an error
		 */
		public boolean accepts( ExtendedTableModel model, int row ) throws YAWLException {
			if( columnName != null ) {
				column = model.getColumnIndex( columnName );
				columnName = null;
			}

			Object valueAt = model.getValueAt( row, column );
			return comparatorHelper.compare( valueAt, comparison, comparable );
		}

		/**
		 * converts the filter to a string that can be used as a "where" clause
		 * in a SQL query.
		 *
		 * @return a string that can be used in a SQL query
		 */
		public String toString() {

			if( null == comparison ) return "";

			String op = comparison.toString();

			String val;
			if( comparable instanceof String ) {
				val = "'" + comparable + "'";
			}
			else {
				val = String.valueOf( comparable );
			}

			// wrapping parenthethis are redundant with and/or
			return columnName + " " + op + " " + val;

			//return "(" + this.columnName + " " + op + " " + val + ")";
		}
	}

	/**
	 * A filter with two sub-filters that accepts a row if said row is accepted
	 * by either of the two sub-filters.
	 */
	public class OrFilter extends Filter implements Serializable {

		private Filter filter1;

		private Filter filter2;

		/**
		 * Creates an <tt>OrFilter</tt> that performs a logical OR of the two given
		 * filters. This filter will accept any row that either of the given
		 * filters will accept.
		 *
		 * @param filter1 one of the filters to perform a logical OR with
		 * @param filter2 the other filter to perform a logical OR with
		 */
		public OrFilter( Filter filter1, Filter filter2 ) {
			this.filter1 = filter1;
			this.filter2 = filter2;
		}

		/**
		 * Returns whether this filter accepts or rejects the given row of the given
		 * <tt>ExtendedTableModel</tt>. This filter will check if either of the
		 * filters given to it in the constructor accepts this row, and if either
		 * one accepts this row than this filter accepts this row.
		 *
		 * @param model the model to check
		 * @param row   the row to check
		 * @return whether the given row of the given model is acceptable to this filter
		 * @throws YAWLException if the filter encounters an error
		 */
		public boolean accepts( ExtendedTableModel model, int row ) throws YAWLException {
			boolean ret;

			if( this.filter1.accepts( model, row ) ) {
				ret = true;
			}
			else if( this.filter2.accepts( model, row ) ) {
				ret = true;
			}
			else {
				ret = false;
			}

			return ret;
		}

		/**
		 * converts the filter to a string that can be used as a "where" clause
		 * in a SQL query.
		 *
		 * @return a string that can be used in a SQL query
		 */
		public String toString() {
			return stringHelper( this.filter1.toString(), "or", this.filter2.toString() );
		}
	}

	/**
	 * A filter with two sub-filters that accepts a row if said row is accepted
	 * by both of the two sub-filters.
	 */
	public class AndFilter extends Filter implements Serializable {

		private Filter filter1;

		private Filter filter2;

		/**
		 * Creates an <tt>AndFilter</tt> that performs a logical AND of the two given
		 * filters. This filter will accept only rows that both of the given
		 * filters will accept.
		 *
		 * @param filter1 one of the filters to perform a logical AND with
		 * @param filter2 the other filter to perform a logical AND with
		 */
		public AndFilter( Filter filter1, Filter filter2 ) {
			this.filter1 = filter1;
			this.filter2 = filter2;
		}

		/**
		 * Returns whether this filter accepts or rejects the given row of the given
		 * <tt>ExtendedTableModel</tt>. This filter will check if both of the
		 * filters given to it in the constructor accept this row, and if both of
		 * the filters accept this row than this filter will accept the row.
		 *
		 * @param model the model to check
		 * @param row   the row to check
		 * @return whether the given row of the given model is acceptable to this filter
		 * @throws YAWLException if the filter encounters an error
		 */
		public boolean accepts( ExtendedTableModel model, int row ) throws YAWLException {
			boolean ret;
			if( !this.filter1.accepts( model, row ) ) {
				ret = false;
			}
			else if( !this.filter2.accepts( model, row ) ) {
				ret = false;
			}
			else {
				ret = true;
			}

			return ret;
		}

		/**
		 * converts the filter to a string that can be used as a "where" clause
		 * in a SQL query.
		 *
		 * @return a string that can be used in a SQL query
		 */
		public String toString() {
			return stringHelper( this.filter1.toString(), "and", this.filter2.toString() );
		}
	}

	/**
	 * A filter that accepts all rows.
	 */
	public class InclusiveFilter extends Filter implements Serializable {

		/**
		 * Returns whether this filter accepts or rejects the given row of the given
		 * <tt>ExtendedTableModel</tt>. This filter does not check the row of the
		 * table, but instead accepts all rows.
		 *
		 * @param model the model to check
		 * @param row   the row to check
		 * @return whether the given row of the given model is acceptable to this filter
		 */
		public boolean accepts( ExtendedTableModel model, int row ) {
			return true;
		}

		/**
		 * converts the filter to a string that can be used as a "where" clause
		 * in a SQL query.
		 *
		 * @return a string that can be used in a SQL query
		 */
		public String toString() {
			return "(1 = 1)";
		}
	}

	/**
	 * A filter that doesn't accept any rows.
	 */
	public class ExclusiveFilter extends Filter implements Serializable {

		/**
		 * Returns whether this filter accepts or rejects the given row of the given
		 * <tt>ExtendedTableModel</tt>. This filter does not check the row of the
		 * table, but instead rejects all rows.
		 *
		 * @param model the model to check
		 * @param row   the row to check
		 * @return whether the given row of the given model is acceptable to this filter
		 */
		public boolean accepts( ExtendedTableModel model, int row ) {
			return false;
		}

		/**
		 * converts the filter to a string that can be used as a "where" clause
		 * in a SQL query.
		 *
		 * @return a string that can be used in a SQL query
		 */
		public String toString() {
			return "(1 = 0)";
		}
	}
}
