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

package org.yawlfoundation.yawl.editor.ui.properties.dialog.component;

import org.yawlfoundation.yawl.editor.ui.properties.data.validation.BindingTypeValidator;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.FlowPredicateDialog;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Michael Adams
 * @date 8/08/12
 */
public class FlowConditionEditor extends AbstractCellEditor
        implements TableCellEditor, ActionListener {

    private final ValueField valuePanel;
    private FlowConditionTablePanel tablePanel;
    private BindingTypeValidator _validator;


    public FlowConditionEditor() {
        valuePanel = new ValueField(this, null);
    }

    public FlowConditionEditor(FlowConditionTablePanel panel,
                               BindingTypeValidator validator) {
        this();
        _validator = validator;
        setTablePanel(panel);
    }


    public void setTablePanel(FlowConditionTablePanel panel) { tablePanel = panel; }


    public Object getCellEditorValue() {
        return valuePanel.getText();
    }


    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row,
                                                 int column) {
        valuePanel.setText((String) value);
        return valuePanel;
    }


    public boolean stopCellEditing() {
        isValid();
        return super.stopCellEditing();
    }


    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getActionCommand().equals("ShowDialog")) {
            showValueDialog((String) getCellEditorValue());
        }
        if (isValid()) {
            fireEditingStopped();
        }
    }



    private void showValueDialog(String value) {
        FlowConditionTable table = tablePanel.getTable();
        FlowPredicateDialog dialog = new FlowPredicateDialog(tablePanel.getParentDialog(),
                table.getSelectedFlow());
        dialog.setTypeValidator(_validator);
        dialog.setText(value);
        dialog.setVisible(true);
        String text = dialog.getText();
        if (text != null) {
            valuePanel.setText(text);
        }
    }


    private boolean isValid() {
        if (validate((String) getCellEditorValue())) {
            tablePanel.showOKStatus();
            return true;
        }
        return false;
    }


    private boolean validate(String value) {
        if (StringUtil.isNullOrEmpty(value)) {
            tablePanel.showErrorStatus("Predicate Required", null);
            return false;
        }

        java.util.List<String> errors = _validator.validate("boolean(" + value + ")");
        if (! errors.isEmpty()) {
            tablePanel.showErrorStatus("Invalid boolean value", errors);
        }
        return (errors.isEmpty());
    }

}
