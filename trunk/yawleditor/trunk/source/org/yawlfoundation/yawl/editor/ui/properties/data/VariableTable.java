package org.yawlfoundation.yawl.editor.ui.properties.data;

import org.yawlfoundation.yawl.editor.ui.swing.JSingleSelectTable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
* @author Michael Adams
* @date 3/08/12
*/
class VariableTable extends JSingleSelectTable {

    private boolean orderChanged;
    private String netElementName; // the name of the net or task the variables belong to

    public VariableTable(VariableTableModel model) {
        super();
        consumeEnterKeyWraps();
        setModel(model);
        setRowHeight(getRowHeight() + 5);
        setEditable(true);
        setRowSelectionAllowed(true);
        setFillsViewportHeight(true);            // to allow drops on empty table
    }


    public VariableTableModel getTableModel() {
        return (VariableTableModel) getModel();
    }


    public String getNetElementName() { return netElementName; }

    public void setNetElementName(String name) { netElementName = name; }


    public void setVariables(List<VariableRow> variables) {
        getTableModel().setVariables(variables);
        setPreferredScrollableViewportSize(getPreferredSize());
        updateUI();
    }


    public List<VariableRow> getVariables() {
        return getTableModel().getVariables();
    }

    public List<VariableRow> getRemovedVariables() {
        return getTableModel().getRemovedVariables();
    }


    public VariableRow getSelectedVariable() {
        return getTableModel().getVariableAtRow(getSelectedRow());
    }

    public boolean allRowsValid() {
        return getTableModel().allRowsValid();
    }


    public void addRow() {
        getTableModel().addRow();
        int row = getRowCount() - 1;
        selectRow(row);
        editCellAt(row, 0);
        requestFocusInWindow();
        getTableModel().getVariableAtRow(row).setDecompositionID(netElementName);
        orderChanged = true;
    }

    public void removeRow() {
        int row = getSelectedRow();
        getTableModel().removeRow(row);
        orderChanged = true;
        if (getRowCount() > 0) {
            selectRow((row < getRowCount() - 1) ? row : getRowCount() - 1);
        }
    }


    public void insertRow(int row, VariableRow variableRow) {
        getTableModel().insertRow(row, variableRow);
        orderChanged = true;
    }

    public void moveSelectedRowUp() {
        int selectedRow = getSelectedRow();
        if (selectedRow > 0) {
            getTableModel().swapRows(selectedRow, selectedRow - 1);
            selectRow(selectedRow - 1);
            orderChanged = true;
        }
    }

    public void moveSelectedRowDown() {
        int selectedRow = getSelectedRow();
        if (selectedRow < getRowCount() - 1) {
            getTableModel().swapRows(selectedRow, selectedRow + 1);
            selectRow(selectedRow + 1);
            orderChanged = true;
        }
    }

    public void setMultiInstanceRow() {
        getTableModel().setMultiInstanceRow(getSelectedRow());
    }


    public boolean hasChangedRowOrder() { return orderChanged; }

    public boolean isChanged() { return getTableModel().isTableChanged(); }


    public void setEditable(boolean editable) {
        setColumnSelectionAllowed(editable);
        setCellSelectionEnabled(editable);
        getTableModel().setEditable(editable);
        if (editable) {
            requestFocusInWindow();
            editCellAt(getSelectedRow(), 0);
        }
        else if (isEditing()) {
            getCellEditor().stopCellEditing();
        }
    }


    public void updatesApplied() {
        getTableModel().updatesApplied();
    }


    /**
     * This method adds a custom action to prevent wrapping to the first table row
     * when the enter key is pressed while on the last table row - that is, it
     * overrides default enter key behaviour and stays on the last row.
     *
     * Based on code sourced from stackoverflow.com
     */
    private void consumeEnterKeyWraps() {
        Object key = getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .get(KeyStroke.getKeyStroke("ENTER"));
        final Action action = getActionMap().get(key);

        Action custom = new AbstractAction("wrap") {
            public void actionPerformed(ActionEvent e) {
                int row = getSelectionModel().getLeadSelectionIndex();
                if (row == getRowCount() - 1) {
                    if (isEditing()) getCellEditor().stopCellEditing();
                    return;     // stop wrapping to top of table
                }
                action.actionPerformed(e);
            }

        };
        getActionMap().put(key, custom);
    }

}
