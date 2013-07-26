package org.yawlfoundation.yawl.editor.ui.properties.data;

import org.yawlfoundation.yawl.editor.core.data.YDataHandler;

/**
* @author Michael Adams
* @date 3/08/12
*/
class TaskOutputVarTableModel extends VariableTableModel {

    private static final String[] COLUMN_LABELS = {"Name", "Type", "Value"};

    public static final int NAME_COLUMN  = 0;
    public static final int TYPE_COLUMN  = 1;
    public static final int VALUE_COLUMN = 2;

    public TaskOutputVarTableModel() {
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
        return editable && (column != VALUE_COLUMN || getVariableAtRow(row).isOutputOnlyTask());
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
            case VALUE_COLUMN:  {
                return selected.getValue();
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
            case VALUE_COLUMN:  {
                selected.setValue((String) value); break;
            }
        }
        fireTableRowsUpdated(row, row);
    }

    public void addRow() { super.addRow(YDataHandler.OUTPUT); }

}
