package com.nexusbpm.editor.logger;

import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JScrollBar;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Table model listener for the log tables. This listener scrolls
 * the log tables down automatically when a new log item is added
 * to the table.
 * 
 * @author     Dean Mao
 * @created    September 8, 2003
 */
public class LogTableModelListener implements TableModelListener, AdjustmentListener {

  private final static Log LOG = LogFactory.getLog( LogTableModelListener.class );

  /**
   *  TODO: This is a hack that works
   */
//  protected static int blah = 0;

  private JTable table;
  //private JScrollBar bar;
  //  Worker worker;

  /**
   * Creates a new log table model listener that will scroll the table down when
   * a new log is added. Note that this constructor does not add this object
   * as a listener to the given table; the caller is responsible for adding the
   * listener as a listener to the table.
   * @param table the table that is being listened to.
   * @param b the scrollbar that controls vertical scrolling for the table.
   */
  public LogTableModelListener(JTable table, JScrollBar b) {
    this.table = table;
    //bar = b;
    b.addAdjustmentListener(this);
    //    worker = new Worker();
  }

  /**
   * Empty implementation.
   * @see AdjustmentListener#adjustmentValueChanged(AdjustmentEvent)
   */
//  int lastV = -1;
//  boolean goDown = true;
  public void adjustmentValueChanged(AdjustmentEvent e) {
//    int v = e.getValue();
//    if (v + bar.getVisibleAmount() >= bar.getMaximum())
//      goDown = true;
//    else if (v < lastV)
//      goDown = false;
//    lastV = v;
  }

  /**
   * Called when the table model has changed.
   * @param e the event indicating how the table model has changed.
   */
  public void tableChanged(TableModelEvent e) {

	    if (e.getType() == TableModelEvent.INSERT /*&& goDown*/) {
	      SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	          try {
	            Rectangle cellRec = table.getCellRect(table.getRowCount(), 0, false);
	            Rectangle tableRec = table.getVisibleRect();
	            if (!tableRec.contains(cellRec)) {
	              table.scrollRectToVisible(cellRec);
	            }
	          } catch (Exception e) {
	          	LOG.error(e);
	          }
	        }
	      });
	    }
	  }

}