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

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

/**
 * @author Michael Adams
 * @date 30/09/2014
 */
public class ExletTargetCellEditor extends ExletCellEditor {

    private JTextField _txtWorklet;

    public ExletTargetCellEditor() { super(); }


    public Object getCellEditorValue() {
        return _txtWorklet != null ? _txtWorklet.getText() :
                _combo.getSelectedItem();
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row,
                                                 int column) {
        super.getTableCellEditorComponent(table, value, isSelected, row, column);
        if (isWorkletAction(table)) {
            _txtWorklet = new JTextField((String) value);
            return _txtWorklet;
        }
        else {
            _txtWorklet = null;
            return newComboInstance(table, value);
        }
    }


    protected Vector<ExletTarget> getItemsForContext(ConclusionTable table) {
        Vector<ExletTarget> targets = new Vector<ExletTarget>();
        if (! isItemOnlyAction(table)) {
            targets.add(ExletTarget.AllCases);
            targets.add(ExletTarget.AncestorCases);
            targets.add(ExletTarget.Case);
        }
        if (table.getSelectedRuleType().isItemLevelType()) {
            targets.add(ExletTarget.Workitem);
        }
        return targets;
    }


    private ExletAction getSelectedAction(JTable table) {
        return ((ConclusionTable) table).getSelectedPrimitive().getExletAction();
    }


    private boolean isWorkletAction(JTable table) {
        ExletAction action = getSelectedAction(table);
        return action == ExletAction.Compensate || action == ExletAction.Select;
    }

    private boolean isItemOnlyAction(JTable table) {
        ExletAction action = getSelectedAction(table);
        return action == ExletAction.Fail || action == ExletAction.Complete ||
                action == ExletAction.Restart;
    }
}
