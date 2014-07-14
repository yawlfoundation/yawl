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
import org.yawlfoundation.yawl.editor.ui.properties.data.binding.OutputBindings;
import org.yawlfoundation.yawl.editor.ui.swing.JSingleSelectTable;

import javax.swing.table.TableModel;
import java.util.List;

/**
* @author Michael Adams
* @date 3/08/12
*/
class BindingViewTable extends JSingleSelectTable {

    private boolean input;

    public BindingViewTable(TableModel model) {
        super();
        consumeEnterKeyWraps();
        setModel(model);
        setRowHeight(getRowHeight() + 5);
        setRowSelectionAllowed(true);
        setFillsViewportHeight(true);
        getColumnModel().getColumn(0).setPreferredWidth(330);
    }


    public void setRows(List<VariableRow> rows) {
        input = true;
        ((InputBindingViewTableModel) getModel()).setVariableRows(rows);
        updateAfterSet();
    }


    public void setRows(OutputBindings bindings) {
        input = false;
        ((OutputBindingViewTableModel) getModel()).setBindings(bindings);
        updateAfterSet();
    }


    public String getSelectedTaskVarName() {
        int row = getSelectedRow();
        return input ?
                ((InputBindingViewTableModel) getModel()).getSelectedTaskVarName(row) :
                ((OutputBindingViewTableModel) getModel()).getSelectedTaskVarName(row);
    }


    public void refresh() {
        if (! input) ((OutputBindingViewTableModel) getModel()).refresh();
    }

    private void updateAfterSet() {
        setPreferredScrollableViewportSize(getPreferredSize());
        updateUI();
    }
}
