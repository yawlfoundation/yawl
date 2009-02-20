/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.swingWorklist.util;

/**
 * In a chain of data manipulators some behaviour is common. TableMap
 * provides most of this behavour and can be subclassed by filters
 * that only need to override a handful of specific methods. TableMap
 * implements TableModel by routing all requests to its model, and
 * TableModelListener by routing all events to its listeners. Inserting
 * a TableMap which has not been subclassed into a chain of table filters
 * should have no effect.
 *
 * @version 1.4 12/17/97
 * @author Philip Milne */

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

public class TableMap extends AbstractTableModel
        implements TableModelListener {
    protected TableModel model;

    public TableModel getModel() {
        return model;
    }


    public void setModel(TableModel model) {
        this.model = model;
        model.addTableModelListener(this);
    }


    // By default, implement TableModel by forwarding all messages
    // to the model.
    public Object getValueAt(int aRow, int aColumn) {
        return model.getValueAt(aRow, aColumn);
    }


    public void setValueAt(Object aValue, int aRow, int aColumn) {
        model.setValueAt(aValue, aRow, aColumn);
    }


    public int getRowCount() {
        return (model == null) ? 0 : model.getRowCount();
    }


    public int getColumnCount() {
        return (model == null) ? 0 : model.getColumnCount();
    }


    public String getColumnName(int aColumn) {
        return model.getColumnName(aColumn);
    }


    public Class getColumnClass(int aColumn) {
        return model.getColumnClass(aColumn);
    }


    public boolean isCellEditable(int row, int column) {
        return model.isCellEditable(row, column);
    }

    // By default forward all events to all the listeners.
    public void tableChanged(TableModelEvent e) {
        fireTableChanged(e);
    }
}
