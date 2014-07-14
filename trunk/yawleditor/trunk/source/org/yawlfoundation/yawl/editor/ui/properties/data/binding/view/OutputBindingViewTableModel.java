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

package org.yawlfoundation.yawl.editor.ui.properties.data.binding.view;

import org.yawlfoundation.yawl.editor.ui.properties.data.DataUtils;
import org.yawlfoundation.yawl.editor.ui.properties.data.binding.OutputBindings;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
* @author Michael Adams
* @date 11/07/14
*/
class OutputBindingViewTableModel extends AbstractTableModel {

    private OutputBindings outputBindings;
    private List<Binding> bindings;

    private static final String[] COLUMN_LABELS = {"Binding", " Net Variable"};
    private static final int BINDING_COLUMN  = 0;
    private static final int VARIABLE_COLUMN  = 1;


    public OutputBindingViewTableModel() {
        super();
    }


    public int getRowCount() {
        return (bindings != null) ? bindings.size() : 0;
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
        switch (col) {
            case BINDING_COLUMN: return bindings.get(row).binding;
            case VARIABLE_COLUMN: return bindings.get(row).netVar;
            default: return null;
        }
    }


    public void setBindings(OutputBindings ob) {
        outputBindings = ob;
        refresh();
    }


    public String getSelectedTaskVarName(int row) {
        if (row < 0) return null;
        String binding = bindings.get(row).binding;

        // try external first
        int colonPos = binding.lastIndexOf(':');
        if (colonPos > -1) {
            return binding.substring(colonPos);
        }

        if (binding.startsWith("<")) {
            return binding.substring(1, binding.indexOf('>') -1);
        }
        return null;
    }


    public void refresh() {
        bindings = new ArrayList<Binding>();
        Map<String, String> summary = outputBindings.getBindingsSummary();
        for (String binding : summary.keySet()) {
            bindings.add(new Binding(binding, summary.get(binding)));
        }
        fireTableRowsUpdated(0, getRowCount() - 1);
    }


    class Binding {
        String binding;
        String netVar;

        Binding(String b, String n) {
            binding = DataUtils.unwrapBinding(b);
            netVar = n;
        }
    }

}
