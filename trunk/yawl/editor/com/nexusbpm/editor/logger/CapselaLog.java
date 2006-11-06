/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.logger;

import java.awt.Color;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.apache.log4j.Level;

/**
 * The CapselaLog keeps lists of log records.
 * Each list contains logs up to a certain level.
 * @author     HoY
 * @author     Dean Mao
 * @created    September 8, 2003
 * @see com.ichg.capsela.framework.logging.LogRecordI
 */
public class CapselaLog implements ListModel, TableModel {

	private final static Color COLOR_INFO = new Color( 106, 204, 95 );
	private final static Color COLOR_WARN = Color.ORANGE;
	private final static Color COLOR_ERROR = Color.RED;

	private final static Integer SOURCE = new Integer( 0 );
	private final static Integer DATE = new Integer( 1 );
	private final static Integer LEVEL = new Integer( 2 );
	private final static Integer MESSAGE = new Integer( 3 );
	//private final static Integer[] columns = { SOURCE, DATE, LEVEL, MESSAGE };
	/** Constant denoting the name of the <tt>source</tt> column. */
	public final static String COLUMN_SOURCE = "Source";
	/** Constant denoting the name of the <tt>date</tt> column. */
	public final static String COLUMN_DATE = "Date";
	/** Constant denoting the name of the <tt>level</tt> column. */
	public final static String COLUMN_LEVEL = "Level";
	/** Constant denoting the name of the <tt>message</tt> column. */
	public final static String COLUMN_MESSAGE = "Message";
	private final static String[] columnNames = { COLUMN_SOURCE, COLUMN_DATE, COLUMN_LEVEL, COLUMN_MESSAGE };
	//private final static Class[] columnClasses = { Integer.class, Date.class, Level.class, String.class };

	/**
	 * The collection of <tt>TableModelListener</tt>s for this table.
	 */
	protected Vector listeners = new Vector();
	// list of ListDataListeners or TableModelListeners

	/**
	 * List containing logs at the debug level and above. Currently, debug log
	 * messages are not ever added to the list.
	 */
	protected Vector fineList = new Vector();
	// LogRecords

	/**
	 * List containing logs at the info level and above.
	 */
	protected Vector infoList = new Vector();
	// LogRecords

	/**
	 * List containing logs at the warning level and above.
	 */
	protected Vector warningList = new Vector();
	// LogRecords

	/**
	 * List of column names that are visible.
	 */
	protected Vector visibleColumns = new Vector();
	// list of strings representing what is visible

	/**
	 * The level of logs to display.
	 */
	protected static Level mode = Level.INFO;

	/**
	 * Log of messages coming from the server.
	 */
	protected static CapselaLog serverLog = new CapselaLog( false );

	/**
	 * Log of messages coming from an engine.
	 */
	protected static CapselaLog engineLog = new CapselaLog( false );

	/**
	 * Log of messages coming from the client.
	 */
	protected static CapselaLog clientLog = new CapselaLog( false );

	/**
	 * Log of messages from all sources.
	 */
	protected static CapselaLog allLog = new CapselaLog( true );

	/**
	 * The list selection model.
	 */
	protected ListSelectionModel selectionModel = new DefaultListSelectionModel();

	/**
	 * Creates a new CapselaLog. This function does not need to be called
	 * directly outside the CapselaLog class; there are logs defined inside the
	 * class that can be accessed by calling static functions in CapselaLog.
	 * @param showSource whether the source of log messages should be displayed.
	 * @see #getAllLog()
	 * @see #getClientLog()
	 * @see #getEngineLog()
	 * @see #getServerLog()
	 * @see #logForRecord(LogRecordI)
	 */
	public CapselaLog( boolean showSource ) {
		// Determine what columns to show.
		if( showSource ) {
			visibleColumns.add( SOURCE );
		}
		visibleColumns.add( DATE );
		visibleColumns.add( MESSAGE );
		// Set the selection mode.
		this.selectionModel.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
	}

	/**
	 * Returns the log that the given record would be added to if passed to the
	 * function {@link #log(LogRecordI)}.
	 * @param record a record whose source indicates which log should be
	 *               returned.
	 * @return the log that displays log messages from the source that the given
	 *         log message was from.
	 */
	public static CapselaLog logForRecord( LogRecordI record ) {
		if( record.getSourceType() == LogRecordI.SOURCE_SERVER ) {
			return serverLog;
		}
		else if( record.getSourceType() == LogRecordI.SOURCE_ENGINE ) {
			return engineLog;
		}
		else if( record.getSourceType() == LogRecordI.SOURCE_CLIENT ) {
			return clientLog;
		}
		else {
			return null;
		}
	}

	/**
	 * Returns HTML code with the given session ID between "session" tags and
	 * the given message appended to the end.
	 * @param sessionId the sesion ID to put in the "session" tag.
	 * @param message the message to append after the "session" tag.
	 * @return an HTML string.
	 */
	public static String sessionTag( long sessionId, String message ) {
		return "<session>" + sessionId + "</session>" + message;
	}

	/**
	 * Logs the given log message. The message will be added to the log that
	 * contains all log messages and the log that contains only messages with
	 * the same source.
	 * @param record the log message to be recorded.
	 */
	public static void log( LogRecordI record ) {
		logForRecord( record ).add( record );
		allLog.add( record );
	}

	/**
	 * Records the given log message. Currently, log messages with a level of
	 * "debug" are never added.
	 * @param record the log message to record.
	 */
	protected synchronized void add( LogRecordI record ) {
		if( record.getLevel() >= Level.WARN_INT ) {
			warningList.add( record );
		}
		if( record.getLevel() >= Level.INFO_INT ) {
			infoList.add( record );
		}

		if( getSize() > 0 ) {
			int ind = getSize() - 1;

			fireAdd( ind, ind );
		}
	}

	/**
	 * @return the list containing log messages at the displayed level.
	 */
	protected java.util.List currentList() {
		if( getMode().equals( Level.DEBUG ) ) {
			return fineList;
		}
		else if( getMode().equals( Level.INFO ) ) {
			return infoList;
		}
		else if( getMode().equals( Level.WARN ) ) {
			return warningList;
		}
		else {
			return null;
		}
	}

	/**
	 * Gets the list displaying log messages at the level of the given log
	 * message.
	 * @param record the log message whose level is the level of the list that
	 *               should be returned.
	 * @return the list that contains log messages of the same level as the
	 *         given log message.
	 */
	protected List listForRecord( LogRecordI record ) {
		if( record.getLevel() >= Level.WARN_INT ) {
			return warningList;
		}
		else if( record.getLevel() >= Level.INFO_INT ) {
			return infoList;
		}
		else if( record.getLevel() >= Level.DEBUG_INT ) {
			return fineList;
		}
		else {
			return null;
		}
	}

	/**
	 * @return the number of log messages in the log at the displayed level.
	 */
	public int getSize() {
		return currentList().size();
	}

	/**
	 * Gets the log record that is at the given index.
	 * @param index the index of the log record to return.
	 * @return the log record at the given index.
	 */
	public Object getElementAt( int index ) {
		return currentList().get( index );
	}

	/**
	 * @return a string containing the messages of all the log records separated
	 *         by the newline character.
	 */
	public String toString() {
		StringWriter sw = new StringWriter();
		PrintWriter writer = new PrintWriter( sw );
		for( int i = 0; i < this.getSize(); i++ ) {
			LogRecordI record = (LogRecordI) this.getElementAt( i );
			String text = record.getMessage();
			writer.println( text );
		}

		return sw.toString();
	}

	/**
	 * Adds a listener to the log that's notified each time a change to the
	 * data model occurs.
	 * @param l the listener to be added.
	 */
	public void addListDataListener( ListDataListener l ) {
		listeners.add( l );
	}

	/**
	 * Removes a listener from the log that's notified each time a change to
	 * the data model occurs.
	 * @param l the listener to remove.
	 */
	public void removeListDataListener( ListDataListener l ) {
		listeners.remove( l );
	}

	/**
	 * Removes all listeners (TableModelListener and ListDataListener) from the
	 * log. 
	 */
	public void removeAllListeners() {
		for( Iterator i = listeners.iterator(); i.hasNext(); ) {
			i.next();
			i.remove();
		}
	}

	/**
	 * Tells all the listeners of this log that the entire log has changed.
	 */
	public void fireAll() {
		for( int i = 0; i < listeners.size(); i++ ) {
			Object listener = listeners.get( i );
			if( listener instanceof ListDataListener ) {
				( (ListDataListener) listeners.get( i ) ).contentsChanged( new ListDataEvent( this, ListDataEvent.CONTENTS_CHANGED, 0, currentList().size() ) );
			}
			else if( listener instanceof TableModelListener ) {
				TableModelEvent evt = new TableModelEvent( this );
				( (TableModelListener) listener ).tableChanged( evt );
			}
		}
	}

	/**
	 * Tells all the listeners of all the logs (that are defined in this class
	 * as static) that the entire log has changed.
	 */
	public static void globalFireAll() {
		getServerLog().fireAll();
		getEngineLog().fireAll();
		getClientLog().fireAll();
		getAllLog().fireAll();
	}

	/**
	 * Tells all the listeners of this log that the given interval (inclusive)
	 * has been added.
	 * @param start the start of the interval.
	 * @param end the end of the interval.
	 */
	public void fireAdd( int start, int end ) {
		for( int i = 0; i < listeners.size(); i++ ) {
			Object listener = listeners.get( i );
			if( listener instanceof ListDataListener ) {
				( (ListDataListener) listeners.get( i ) ).intervalAdded( new ListDataEvent( this, ListDataEvent.INTERVAL_ADDED, start, end ) );
			}
			else if( listener instanceof TableModelListener ) {
				TableModelEvent evt = new TableModelEvent( this, start, end, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT );
				( (TableModelListener) listener ).tableChanged( evt );
			}
		}
	}

	/**
	 * Sets the level of log messages displayed.
	 * @param m the level of log messages to be displayed.
	 */
	public static synchronized void setMode( Level m ) {
		if( mode != m ) {
			mode = m;
			globalFireAll();
		}
	}

	/**
	 * @return the level of log messages displayed.
	 */
	public static Level getMode() {
		return mode;
	}

	/**
	 * @return the log that contains only server messages.
	 */
	public static CapselaLog getServerLog() {
		return serverLog;
	}

	/**
	 * Sets the log that will have only log messages from the server recorded
	 * in it.
	 * @param serverLogParameter the log that will have only log messages from
	 *                           the server recorded in it.
	 */
	public static void setServerLog( CapselaLog serverLogParameter ) {
		serverLog = serverLogParameter;
	}

	/**
	 * @return the log that contains only engine messages.
	 */
	public static CapselaLog getEngineLog() {
		return engineLog;
	}

	/**
	 * Sets the log that will have only log messages from an engine recorded
	 * in it.
	 * @param engineLogParameter the log that will have only log messages from
	 *                           an engine recorded in it.
	 */
	public static void setEngineLog( CapselaLog engineLogParameter ) {
		engineLog = engineLogParameter;
	}

	/**
	 * @return the log that contains only client messages.
	 */
	public static CapselaLog getClientLog() {
		return clientLog;
	}

	/**
	 * Sets the log that will have only log messages from the client recorded
	 * in it.
	 * @param clientLogParameter the log that will have only log messages from
	 *                           the client recorded in it.
	 */
	public static void setClientLog( CapselaLog clientLogParameter ) {
		clientLog = clientLogParameter;
	}

	/**
	 * @return the log that contains messages from all sources (client, server,
	 *         and engine).
	 */
	public static CapselaLog getAllLog() {
		return allLog;
	}

	/**
	 * Sets the log that will have all log messages recorded in it.
	 * @param allLogParameter the log that will have all log messages recorded
	 *                        in it.
	 */
	public static void setAllLog( CapselaLog allLogParameter ) {
		allLog = allLogParameter;
	}

	/**
	 * @return the list selection model.
	 */
	public ListSelectionModel getSelectionModel() {
		return selectionModel;
	}

	/**
	 * Sets the list selection model.
	 * @param model the list selection model to be used.
	 */
	public void setSelectionModel( ListSelectionModel model ) {
		selectionModel = model;
	}

	/**
	 * Clears all of the lists on all of the logs. The change will be reported
	 * to listeners.
	 */
	public static void clearAll() {
		getServerLog().clear();
		getEngineLog().clear();
		getClientLog().clear();
		getAllLog().clear();
	}

	/**
	 * Clears all of the lists for this log and reports the change to listeners.
	 */
	public void clear() {
		fineList.clear();
		infoList.clear();
		warningList.clear();
		this.fireAll();
	}

	/*
	 *  ============TableModel Methods===========
	 */
	/**
	 * Adds a listener to the list that is notified each time a change to the
	 * data model occurs.
	 * @param l the listener to add.
	 */
	public void addTableModelListener( TableModelListener l ) {
		listeners.add( l );
	}

	/**
	 * Returns the most specific superclass for all the cell values in the
	 * column. This is used by the JTable to set up a default renderer and
	 * editor for the column.
	 * @param columnIndex the column in question.
	 * @return the common ancestor class of the object values in the model.
	 */
	public Class getColumnClass( int columnIndex ) {
		return CellModel.class;
		//columnClasses[((Integer)visibleColumns.get(columnIndex)).intValue()];
	}

	/**
	 * Returns the number of columns in the model. A JTable uses this method to
	 * determine how many columns it should create and display by default.
	 * @return the number of columns in the model.
	 */
	public int getColumnCount() {
		return visibleColumns.size();
	}

	/**
	 * Returns the name of the column at columnIndex. This is used to initialize
	 * the table's column header name.
	 * @param columnIndex the column in question.
	 * @return the name of the column.
	 */
	public String getColumnName( int columnIndex ) {
		return columnNames[ ( (Integer) visibleColumns.get( columnIndex ) ).intValue() ];
	}

	/**
	 * Returns the number of rows in the model. A JTable uses this method to
	 * determine how many rows it should display. This method should be quick,
	 * as it is called frequently during rendering.
	 * @return the number of rows in the model.
	 */
	public int getRowCount() {
		return getSize();
	}

	/**
	 * Returns the value for the cell at columnIndex and rowIndex.
	 * @param rowIndex the row of the cell in question.
	 * @param columnIndex the column of the cell in question.
	 * @return the value Object at the specified cell.
	 */
	public Object getValueAt( int rowIndex, int columnIndex ) {

		LogRecordI record = (LogRecordI) currentList().get( rowIndex );
		Integer column = (Integer) visibleColumns.get( columnIndex );

		Color color;
		if( record.getLevel() <= Level.INFO_INT ) {
			color = COLOR_INFO;
		}
		else if( record.getLevel() <= Level.WARN_INT ) {
			color = COLOR_WARN;
		}
		else {
			color = COLOR_ERROR;
		}

		if( column.equals( SOURCE ) ) {
			return new CellModel( Color.DARK_GRAY, new Integer( record.getSourceType() ) );
		}
		else if( column.equals( DATE ) ) {
			return new CellModel( color, new Date( record.getMilliseconds() ) );
		}
		else if( column.equals( LEVEL ) ) {
			return new CellModel( color, new Integer( record.getLevel() ) );
		}
		else if( column.equals( MESSAGE ) ) {
			return new CellModel( Color.BLACK, record.getMessage() );
		}
		else {
			return null;
		}

	}

	/**
	 * Returns true if the cell at rowIndex and columnIndex is editable; always
	 * returns false for this implementation.
	 * @param rowIndex the row of the cell in question.
	 * @param columnIndex the column of the cell in question.
	 * @return <tt>false</tt>.
	 */
	public boolean isCellEditable( int rowIndex, int columnIndex ) {
		return false;
	}

	/**
	 * Removes a listener from the list that is notified each time a change to
	 * the data model occurs.
	 * @param l the listener to remove.
	 */
	public void removeTableModelListener( TableModelListener l ) {
		listeners.remove( l );
	}

	/**
	 * This should never be called in this implementation; throws an
	 * <tt>AssertionError</tt>.
	 * Sets the value in the cell at columnIndex and rowIndex to aValue.
	 * @param aValue the new value.
	 * @param rowIndex the row whose value is to be changed.
	 * @param columnIndex the column whose value is to be changed.
	 */
	public void setValueAt( Object aValue, int rowIndex, int columnIndex ) {
		// this shouldn't ever be called
		throw new AssertionError( "this shouldn't ever be called" );
	}

	/**
	 * @return statistics for the log as a string.
	 */
	public String getStatistics() {
		StringBuffer buf = new StringBuffer();
		buf.append( "mode=" );
		buf.append( getMode() );
		buf.append( ";listeners=" );
		buf.append( listeners.size() );
		buf.append( ";fine=" );
		buf.append( fineList.size() );
		buf.append( ";info=" );
		buf.append( infoList.size() );
		buf.append( ";warn=" );
		buf.append( warningList.size() );
		buf.append( ";col=" );
		buf.append( getColumnCount() );
		return buf.toString();
	}

}
