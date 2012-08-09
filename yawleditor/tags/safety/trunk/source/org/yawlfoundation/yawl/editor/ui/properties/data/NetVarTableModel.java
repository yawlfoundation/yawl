package org.yawlfoundation.yawl.editor.ui.properties.data;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
* @author Michael Adams
* @date 3/08/12
*/
class NetVarTableModel extends AbstractTableModel {

    private List<NetVariableRow> variables;
    private List<NetVariableRow> removed;
    private boolean editable;

    private static final String[] COLUMN_LABELS = {"Name", "Type", "Scope", "Value"};

    public static final int NAME_COLUMN  = 0;
    public static final int TYPE_COLUMN  = 1;
    public static final int SCOPE_COLUMN = 2;
    public static final int VALUE_COLUMN = 3;

    public NetVarTableModel() {
        super();
    }

    public List<NetVariableRow> getVariables() {
        return variables;
    }

    public List<NetVariableRow> getRemovedVariables() {
        return removed != null ? removed : Collections.<NetVariableRow>emptyList();
    }

    public NetVariableRow getVariableAtRow(int row) {
        return variables.get(row);
    }

    public void addRow() {
        variables.add(new NetVariableRow());
        int newRowIndex = getRowCount() - 1;
        fireTableRowsInserted(newRowIndex, newRowIndex);
    }

    public void removeRow(int row) {
        if (getRowCount() == 0) return;
        if (removed == null) removed = new ArrayList<NetVariableRow>();
        removed.add(variables.remove(row));
        fireTableDataChanged();
    }

    public void swapRows(int first, int second) {
        Collections.swap(variables, first, second);
        fireTableRowsUpdated(first, second);
    }


    public void setVariables(List<NetVariableRow> varList) {
        variables = varList;
        fireTableRowsUpdated(0, getRowCount() - 1);
    }

    public int getColumnCount() {
        return COLUMN_LABELS.length;
    }

    public String getColumnName(int column) {
        return COLUMN_LABELS[column];
    }

    public Class<?> getColumnClass(int columnIndex) {
        return (columnIndex == SCOPE_COLUMN) ? NetVariableRow.Usage.class: String.class;
    }

    public boolean isCellEditable(int row, int column) {
        return editable && (column != VALUE_COLUMN || getVariableAtRow(row).mayUpdateValue());
    }

    public void setEditable(boolean canEdit) {
        editable = canEdit;
    }


    public int getRowCount() {
        return (getVariables() != null) ? getVariables().size() : 0;
    }

    public Object getValueAt(int row, int col) {
        NetVariableRow selected = getVariableAtRow(row);
        switch (col) {
            case NAME_COLUMN:  {
                return selected.getName();
            }
            case TYPE_COLUMN:  {
                return selected.getDataType();
            }
            case SCOPE_COLUMN:  {
                return selected.getUsage();
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
        NetVariableRow selected = getVariableAtRow(row);
        switch (col) {
            case NAME_COLUMN:  {
                selected.setName((String) value); break;
            }
            case TYPE_COLUMN:  {
                selected.setDataType((String) value); break;
            }
            case SCOPE_COLUMN:  {
                selected.setUsage((NetVariableRow.Usage) value); break;
            }
            case VALUE_COLUMN:  {
                selected.setValue((String) value); break;
            }
        }
        fireTableRowsUpdated(row, row);
    }

}
