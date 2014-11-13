/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.editor.ui.properties.data;

import org.yawlfoundation.yawl.editor.ui.swing.JSingleSelectTable;

import java.util.List;

/**
* @author Michael Adams
* @date 3/08/12
*/
public class VariableTable extends JSingleSelectTable {

    private boolean orderChanged;
    private TableType tableType;
    private String decompositionID; // the id of the decomposition the variables belong to

    public VariableTable(TableType type) {
        super();
        consumeEnterKeyWraps();
        tableType = type;
        setModel(type.getModel());
        setRowHeight(getRowHeight() + 5);
        setEditable(true);
        setRowSelectionAllowed(true);
        setFillsViewportHeight(true);            // to allow drops on empty table
    }


    public VariableTableModel getTableModel() {
        return (VariableTableModel) getModel();
    }

    public TableType getTableType() { return tableType; }

    public String getDecompositionID() { return decompositionID; }

    public void setDecompositionID(String name) { decompositionID = name; }


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
        int selectedRow = getSelectedRow();
        return selectedRow > -1 ? getTableModel().getVariableAtRow(selectedRow) : null;
    }

    public boolean allRowsValid() {
        return getTableModel().allRowsValid();
    }


    public void addRow() {
        getTableModel().addRow();
        int row = getRowCount() - 1;
        selectRow(row);
        editCellAt(row, 1);
        requestFocusInWindow();
        getTableModel().getVariableAtRow(row).setDecompositionID(decompositionID);
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
        selectRow(row);
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


    public void setMultiInstanceRow(VariableRow row) {
        getTableModel().setMultiInstanceRow(row);
    }

    public boolean hasMultiInstanceRow() {
        return getTableModel().hasMultiInstanceRow();
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


    public void refresh() {
        int selectedRow = getSelectedRow();
        getTableModel().fireTableDataChanged();
        selectRow(Math.min(selectedRow, getRowCount() - 1));
    }


    public void updatesApplied() {
        getTableModel().updatesApplied();
    }

}
