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

import org.yawlfoundation.yawl.editor.ui.properties.data.DataVariableDialog;
import org.yawlfoundation.yawl.editor.ui.properties.data.VariableRow;
import org.yawlfoundation.yawl.editor.ui.swing.JSingleSelectTable;

import javax.swing.table.TableModel;
import java.util.List;

/**
* @author Michael Adams
* @date 3/08/12
*/
abstract class BindingViewTable extends JSingleSelectTable {


    public BindingViewTable(TableModel model) {
        super();
        consumeEnterKeyWraps();
        setModel(model);
        setRowHeight(getRowHeight() + 5);
        setRowSelectionAllowed(true);
        setFillsViewportHeight(true);
        getColumnModel().getColumn(0).setPreferredWidth(330);
        setDefaultRenderer(String.class, new RowRenderer());
    }


    public abstract BindingViewTableModel getTableModel();

    public void setRows(List<VariableRow> rows) { }        // input table

    public void setRows(DataVariableDialog dataDialog) { } // output table


    public String getSelectedTaskVarName() {
        return getTableModel().getSelectedTaskVarName(getSelectedRow());
    }


    public boolean isMIRow(int row) {
        return getTableModel().isMIRow(row);
    }


    public boolean removeSelectedBinding() {
        return getTableModel().removeBinding(getSelectedRow());
    }


    public boolean isMIRowSelected() { return isMIRow(getSelectedRow()); }


    protected void updateAfterSet() {
        setPreferredScrollableViewportSize(getPreferredSize());
        updateUI();
    }
}
