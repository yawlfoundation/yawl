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

import org.yawlfoundation.yawl.editor.ui.properties.dialog.component.ValueField;
import org.yawlfoundation.yawl.editor.ui.resourcing.subdialog.ListDialog;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.worklet.exception.ExletAction;
import org.yawlfoundation.yawl.worklet.exception.ExletTarget;
import org.yawlfoundation.yawl.worklet.model.WorkletListModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Vector;

/**
 * @author Michael Adams
 * @date 30/09/2014
 */
public class ExletTargetCellEditor extends ExletCellEditor {

    private ValueField _fldWorklet;
    private final JLabel _lblInvalid = new JLabel("invalid");
    private ExletAction _currentAction;

    public ExletTargetCellEditor() { super(); }


    public Object getCellEditorValue() {
        switch (_currentAction) {
            case Invalid: return "invalid";
            case Compensate:
            case Select: return _fldWorklet.getText();
            default: return _combo.getSelectedItem();
        }
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row,
                                                 int column) {
        super.getTableCellEditorComponent(table, value, isSelected, row, column);
        _currentAction = getSelectedAction(table);
        if (_currentAction.isInvalidAction()) {
            return _lblInvalid;     // no editing of target without valid action first
        }
        if (_currentAction.isWorkletAction()) {
            _fldWorklet = new ValueField(this, null);
            _fldWorklet.setText((String) value);
            return _fldWorklet;
        }
        else {
            return newComboInstance(table, value);
        }
    }


    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getActionCommand().equals("ShowDialog")) {
            showListDialog();
        }
        fireEditingStopped();
    }


    protected Vector<ExletTarget> getItemsForContext(ConclusionTable table) {
        Vector<ExletTarget> targets = new Vector<ExletTarget>();
        if (! _currentAction.isItemOnlyAction()) {
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


    private void showListDialog() {
        ListDialog dialog = new ListDialog(null, new WorkletListModel(), "Worklets");
        dialog.setVisible(true);
        Vector<String> selections = new Vector<String>();
        for (Object o : dialog.getSelections()) {
             selections.add((String) o);
        }
        _fldWorklet.setText(StringUtil.join(selections, ';'));
    }

}
