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

import org.yawlfoundation.yawl.worklet.exception.ExletAction;
import org.yawlfoundation.yawl.worklet.exception.ExletTarget;
import org.yawlfoundation.yawl.worklet.rdr.RdrConclusion;
import org.yawlfoundation.yawl.worklet.rdr.RdrPrimitive;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 30/09/2014
 */
public class ConclusionTableModel extends AbstractTableModel {

    private List<RdrPrimitive> _primitives;


    private static final String[] COLUMN_LABELS = { "", "Action", "Target" };


    public ConclusionTableModel() {
        super();
        _primitives = new ArrayList<RdrPrimitive>();
    }


    public void setConclusion(List<RdrPrimitive> primitives) {
        _primitives = primitives;
        fireTableDataChanged();
    }


    public int getRowCount() {
        return (_primitives != null) ? _primitives.size() : 0;
    }


    public int getColumnCount() { return COLUMN_LABELS.length; }


    public String getColumnName(int column) { return COLUMN_LABELS[column]; }


    public Class<?> getColumnClass(int columnIndex) { return String.class; }


    public boolean isCellEditable(int row, int column) {
        return column > 0;
    }


    public Object getValueAt(int row, int col) {
        if (row < getRowCount()) {
            switch (col) {
                case 0: return String.valueOf(row + 1);
                case 1: return _primitives.get(row).getAction();
                case 2: {
                    String target = _primitives.get(row).getTarget();
                    return target.equals("invalid") ? "" : target;
                }
            }
        }
        return null;
    }


    public void setValueAt(Object value, int row, int col) {
        if (row < getRowCount() && col > 0) {
            switch (col) {
                case 1: _primitives.get(row).setAction(value.toString()); break;
                case 2: _primitives.get(row).setTarget(value.toString()); break;
            }
            fireTableRowsUpdated(row, row);
        }
    }


    public RdrConclusion getConclusion() {
        RdrConclusion conclusion = new RdrConclusion();
        if (getRowCount() > 0) {                              // not null or empty
            for (RdrPrimitive primitive : _primitives) {
                String action = primitive.getAction();
                String target = primitive.getTarget();
                if (action.equals(ExletAction.Select.toString())) {
                    conclusion.setSelectionPrimitive(target);
                    break;
                }
                else if (! action.equals(ExletAction.Invalid.toString())) {
                    conclusion.addPrimitive(action, target);
                }
            }
        }
        return conclusion;
    }


    public void addRow() {
        _primitives.add(new RdrPrimitive(getRowCount(),
                ExletAction.Invalid, ExletTarget.Invalid));
        int newRowIndex = getRowCount() - 1;
        fireTableRowsInserted(newRowIndex, newRowIndex);
    }


    public void removeRow(int row) {
        if (getRowCount() == 0 || row >= getRowCount()) return;
        _primitives.remove(row);
        fireTableDataChanged();
    }

}
