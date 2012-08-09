package org.yawlfoundation.yawl.editor.ui.properties.data;

import org.yawlfoundation.yawl.editor.ui.swing.JSingleSelectTable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
* @author Michael Adams
* @date 3/08/12
*/
class NetVarTable extends JSingleSelectTable {

    private boolean orderChanged;

    public NetVarTable() {
        super();
        consumeEnterKeyWraps();
        setModel(new NetVarTableModel());
        setRowHeight(getRowHeight() + 5);
        setRowSelectionAllowed(true);
    }

    public NetVarTableModel getNetVarTableModel() {
        return (NetVarTableModel) getModel();
    }

    public void setVariables(List<NetVariableRow> variables) {
        getNetVarTableModel().setVariables(variables);
        setPreferredScrollableViewportSize(getPreferredSize());
        updateUI();
    }

    public List<NetVariableRow> getVariables() {
        return getNetVarTableModel().getVariables();
    }

    public List<NetVariableRow> getRemovedVariables() {
        return getNetVarTableModel().getRemovedVariables();
    }


    public void addRow() {
        getNetVarTableModel().addRow();
        selectRow(getRowCount() - 1);
        orderChanged = true;
    }

    public void removeRow() {
        int row = getSelectedRow();
        getNetVarTableModel().removeRow(row);
        orderChanged = true;
        if (getRowCount() > 0) {
            selectRow((row < getRowCount() - 1) ? row : getRowCount() - 1);
        }
    }

    public void moveSelectedRowUp() {
        int selectedRow = getSelectedRow();
        if (selectedRow > 0) {
            getNetVarTableModel().swapRows(selectedRow, selectedRow - 1);
            selectRow(selectedRow - 1);
            orderChanged = true;
        }
    }

    public void moveSelectedRowDown() {
        int selectedRow = getSelectedRow();
        if (selectedRow < getRowCount() - 1) {
            getNetVarTableModel().swapRows(selectedRow, selectedRow + 1);
            selectRow(selectedRow + 1);
            orderChanged = true;
        }
    }

    public boolean hasChangedRowOrder() { return orderChanged; }


    public void setEditable(boolean editable) {
        setColumnSelectionAllowed(editable);
        setCellSelectionEnabled(editable);
        getNetVarTableModel().setEditable(editable);
        if (editable) {
            requestFocusInWindow();
            editCellAt(getSelectedRow(), 0);
        }
        else if (isEditing()) {
            getCellEditor().stopCellEditing();
        }
    }


    /**
     * This method adds a custom action to prevent wrapping to the first table row
     * when the enter key is pressed while on the last table row - that is , it
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
