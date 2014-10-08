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

package org.yawlfoundation.yawl.worklet.dialog;

import org.yawlfoundation.yawl.editor.ui.swing.JSingleSelectTable;
import org.yawlfoundation.yawl.worklet.rdr.RdrConclusion;
import org.yawlfoundation.yawl.worklet.rdr.RdrPrimitive;

import java.util.List;

/**
 * @author Michael Adams
 * @date 30/09/2014
 */
public class ConclusionTable extends JSingleSelectTable {

    public ConclusionTable() {
        super();
        setModel(new ConclusionTableModel());
        setRowHeight(getRowHeight() + 5);
        setCellSelectionEnabled(true);
        setRowSelectionAllowed(true);
        setColumnSelectionAllowed(true);
        setFillsViewportHeight(true);            // to allow drops on empty table
        getColumnModel().getColumn(0).setCellEditor(new ExletActionCellEditor());
        getColumnModel().getColumn(1).setCellEditor(new ExletTargetCellEditor());
    }


    public RdrConclusion getConclusion() { return getTableModel().getConclusion(); }


    public RdrPrimitive getSelectedPrimitive() {
        return getConclusion().getPrimitive(getSelectedRow() + 1);
    }


    public void setConclusion(List<RdrPrimitive> primitives) {
        getTableModel().setConclusion(primitives);
        updateUI();
    }


    public ConclusionTableModel getTableModel() {
        return (ConclusionTableModel) getModel();
    }


    public void addRow() {
        getTableModel().addRow();
        int row = getRowCount() - 1;
        selectRow(row);
        editCellAt(row, 0);
        requestFocusInWindow();
    }

    public void removeRow() {
        int row = getSelectedRow();
        getTableModel().removeRow(row);
        if (getRowCount() > 0) {
            selectRow((row < getRowCount() - 1) ? row : getRowCount() - 1);
        }
    }

}
