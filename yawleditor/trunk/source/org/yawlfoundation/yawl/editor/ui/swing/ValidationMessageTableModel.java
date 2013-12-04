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

import org.yawlfoundation.yawl.editor.ui.specification.validation.ValidationMessage;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

public class ValidationMessageTableModel extends AbstractTableModel {

    private java.util.List<ValidationMessage> messages = new ArrayList<ValidationMessage>();

    private static final String[] COLUMN_LABELS = { "Problem" };
    public static final int PROBLEM_COLUMN = 0;


    public int getColumnCount() {
        return COLUMN_LABELS.length;
    }


    public void reset() {
        messages.clear();
    }


    public String getColumnName(int columnIndex) {
        return null;
    }

    public int getRowCount() {
        return (messages != null) ? messages.size() : 0;
    }

    public Object getValueAt(int row, int col) {
        return messages.get(row).getTableRowForm();
    }

    public String getLongMessage(int row) {
        return (row > -1 && row < getRowCount()) ? messages.get(row).getLongForm() : null;
    }

    public void addMessages(java.util.List<ValidationMessage> msgList) {
        messages.clear();
        messages.addAll(msgList);
        fireTableRowsInserted(0, getRowCount() - 1);
    }
}
