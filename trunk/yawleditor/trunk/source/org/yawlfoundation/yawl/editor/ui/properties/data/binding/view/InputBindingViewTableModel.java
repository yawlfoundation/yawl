/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.properties.data.binding.view;

import org.yawlfoundation.yawl.editor.ui.properties.data.VariableRow;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
* @author Michael Adams
* @date 11/07/14
*/
class InputBindingViewTableModel extends AbstractTableModel
        implements BindingViewTableModel  {

    private List<VariableRow> variableRows;

    private static final String[] COLUMN_LABELS = {"Binding", " Task Variable"};
    private static final int BINDING_COLUMN  = 0;
    private static final int VARIABLE_COLUMN  = 1;


    public InputBindingViewTableModel() {
        super();
    }


    public int getRowCount() {
        return (getVariableRows() != null) ? getVariableRows().size() : 0;
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

    public Object getValueAt(int row, int col) {
        VariableRow selected = getVariableAtRow(row);
        switch (col) {
            case BINDING_COLUMN: return selected.getMapping();
            case VARIABLE_COLUMN: return selected.getName();
            default: return null;
        }
    }


    public VariableRow getVariableAtRow(int row) {
        if (getRowCount() == 0 || row < 0 || row >= getRowCount()) {
            return null;
        }
        return getVariableRows().get(row);
    }


    public String getSelectedTaskVarName(int row) {
        return row > -1 ? (String) getValueAt(row, 1) : null;
    }


    public void setVariableRows(List<VariableRow> list) {
        variableRows = new ArrayList<VariableRow>();
        for (VariableRow row : list) {
            if (row.isInput() || row.isInputOutput()) {
                variableRows.add(row);
            }
        }
        fireTableRowsUpdated(0, getRowCount() - 1);
    }


    public List<VariableRow> getVariableRows() {
        return variableRows;
    }


    public boolean isMIRow(int row) {
        VariableRow varRow = variableRows.get(row);
        return varRow != null && varRow.isMultiInstance();
    }


    public boolean removeBinding(int row) {
        if (row < getRowCount()) {
            variableRows.get(row).setMapping(null);
            fireTableRowsUpdated(row, row);
        }
        return row < getRowCount();
    }

}
