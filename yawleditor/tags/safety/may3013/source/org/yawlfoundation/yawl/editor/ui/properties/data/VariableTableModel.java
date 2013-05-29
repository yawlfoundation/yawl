package org.yawlfoundation.yawl.editor.ui.properties.data;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
* @author Michael Adams
* @date 3/08/12
*/
abstract class VariableTableModel extends AbstractTableModel {

    protected List<VariableRow> variables;
    protected List<VariableRow> removed;
    protected boolean editable;
    protected boolean tableChanged;

    public VariableTableModel() {
        super();
    }

    public List<VariableRow> getVariables() {
        return variables;
    }

    public List<VariableRow> getRemovedVariables() {
        return removed != null ? removed : Collections.<VariableRow>emptyList();
    }

    public VariableRow getVariableAtRow(int row) {
        return variables.get(row);
    }

    public void insertRow(int row, VariableRow variableRow) {
        variables.add(row, variableRow);
        fireTableRowsInserted(row, row);
    }

    public void removeRow(int row) {
        if (getRowCount() == 0 || row >= getRowCount()) return;
        getVariableAtRow(row).setMultiInstance(false);
        if (removed == null) removed = new ArrayList<VariableRow>();
        removed.add(variables.remove(row));
        fireTableDataChanged();
    }

    public void swapRows(int first, int second) {
        Collections.swap(variables, first, second);
        fireTableRowsUpdated(first, second);
    }


    public void setVariables(List<VariableRow> varList) {
        variables = varList;
        fireTableRowsUpdated(0, getRowCount() - 1);
    }


    public void setEditable(boolean canEdit) {
        editable = canEdit;
    }


    public void updatesApplied() {
        if (removed != null) removed.clear();
        for (VariableRow row : variables) {
            row.updatesApplied();
        }
        tableChanged = false;
    }


    public boolean isTableChanged() { return tableChanged; }

    public void setTableChanged(boolean changed) { tableChanged = changed; }


    public int getRowCount() {
        return (getVariables() != null) ? getVariables().size() : 0;
    }


    public void setMultiInstanceRow(int row) {
        for (VariableRow varRow : variables) varRow.setMultiInstance(false);
        getVariableAtRow(row).setMultiInstance(true);
        fireTableDataChanged();
    }


    public void addRow(int scope) {
        variables.add(new VariableRow(scope));
        int newRowIndex = getRowCount() - 1;
        fireTableRowsInserted(newRowIndex, newRowIndex);
    }


    public abstract void addRow();

}
