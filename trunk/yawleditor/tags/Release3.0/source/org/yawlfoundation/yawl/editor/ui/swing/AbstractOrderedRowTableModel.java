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

package org.yawlfoundation.yawl.editor.ui.swing;

import javax.swing.table.AbstractTableModel;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractOrderedRowTableModel extends AbstractTableModel {

    private List orderedRows = new LinkedList();

    public List getOrderedRows() {
        return orderedRows;
    }

    public void setOrderedRows(List orderedRows) {
        this.orderedRows = orderedRows;
    }

    public int getRowCount() {
        return getOrderedRows() != null ? getOrderedRows().size() : 0;
    }

    public void refresh() {
        fireTableRowsUpdated(0, Math.max(0, getRowCount() - 1));
    }

    public void raiseRow(int row) {
        doRaiseRow(row);
    }

    public void lowerRow(int row) {
        doRaiseRow(row + 1);
    }

    private void doRaiseRow(int rowToRaise) {

        getOrderedRows().add(
                rowToRaise - 1,
                getOrderedRows().remove(rowToRaise)
        );

        fireTableRowsUpdated(rowToRaise - 1, rowToRaise);
    }
}
