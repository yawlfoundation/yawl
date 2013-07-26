package org.yawlfoundation.yawl.editor.ui.properties.data;

import org.yawlfoundation.yawl.editor.core.data.YDataHandler;

/**
* @author Michael Adams
* @date 3/08/12
*/
class TaskInputVarTableModel extends VariableTableModel {

    private static final String[] COLUMN_LABELS = {"Name", "Type"};

    public static final int NAME_COLUMN  = 0;
    public static final int TYPE_COLUMN  = 1;

    public TaskInputVarTableModel() {
        super();
    }

    public int getColumnCount() {
        return COLUMN_LABELS.length;
    }

    public String getColumnName(int column) {
        return COLUMN_LABELS[column];
    }

    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    public boolean isCellEditable(int row, int column) {
        return editable;
    }

    public Object getValueAt(int row, int col) {
        VariableRow selected = getVariableAtRow(row);
        switch (col) {
            case NAME_COLUMN:  {
                return selected.getName();
            }
            case TYPE_COLUMN:  {
                return selected.getDataType();
            }
            default: {
                return null;
            }
        }
    }

    public void setValueAt(Object value, int row, int col) {
        VariableRow selected = getVariableAtRow(row);
        switch (col) {
            case NAME_COLUMN:  {
                selected.setName((String) value); break;
            }
            case TYPE_COLUMN:  {
                selected.setDataType((String) value); break;
            }
        }
        fireTableRowsUpdated(row, row);
    }


    public void addRow() { super.addRow(YDataHandler.INPUT); }

}
