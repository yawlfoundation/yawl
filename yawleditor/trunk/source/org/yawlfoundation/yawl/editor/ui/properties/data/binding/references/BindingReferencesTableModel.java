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

package org.yawlfoundation.yawl.editor.ui.properties.data.binding.references;

import org.yawlfoundation.yawl.editor.core.data.BindingReference;
import org.yawlfoundation.yawl.editor.ui.properties.data.DataUtils;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
* @author Michael Adams
* @date 11/07/14
*/
class BindingReferencesTableModel extends AbstractTableModel {

    private List<BindingReference> rows;

    private static final String[] COLUMN_LABELS = { "Net", "Task", "Variable", "Scope",
                                                   "Binding" };


    public BindingReferencesTableModel() {
        super();
    }


    public int getRowCount() {
        return (getRows() != null) ? getRows().size() : 0;
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
        BindingReference selected = getReferenceAtRow(row);
        switch (col) {
            case 0: return selected.getNetID();
            case 1: return selected.getTaskID();
            case 2: return selected.getVariableName();
            case 3: return selected.getScope();
            case 4: return DataUtils.unwrapBinding(selected.getBinding());
            default: return null;
        }
    }


    public boolean isCellEditable(int row, int col) {
        return col == 4;
    }

    public BindingReference getReferenceAtRow(int row) {
        if (getRowCount() == 0 || row < 0 || row >= getRowCount()) {
            return null;
        }
        return getRows().get(row);
    }


    public void setRows(List<BindingReference> list) {
        rows = list;
        fireTableDataChanged();
    }


    public List<BindingReference> getRows() {
        return rows;
    }

}
