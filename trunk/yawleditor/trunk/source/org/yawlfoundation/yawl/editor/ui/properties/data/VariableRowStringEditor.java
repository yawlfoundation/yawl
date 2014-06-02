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

package org.yawlfoundation.yawl.editor.ui.properties.data;

import org.apache.xerces.util.XMLChar;
import org.yawlfoundation.yawl.editor.core.data.YDataHandlerException;
import org.yawlfoundation.yawl.editor.ui.properties.data.validation.VariableValueDialog;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.component.ValueField;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * @author Michael Adams
 * @date 8/08/12
 */
public class VariableRowStringEditor extends AbstractCellEditor
        implements TableCellEditor, ActionListener, CaretListener {

    private final JTextField nameField;
    private final JComboBox dataTypeCombo;
    private JCheckBox checkBox;

    private final ValueField valuePanel;
    private VariableTablePanel tablePanel;
    private String editingColumnName;
    private int editingRow;


    public VariableRowStringEditor() {
        nameField = new JTextField();
        nameField.addCaretListener(this);
        dataTypeCombo = new JComboBox(getDataTypeNames());
        dataTypeCombo.addActionListener(this);
        valuePanel = new ValueField(this, this);
        checkBox = new JCheckBox();
        checkBox.addActionListener(this);
    }

    public VariableRowStringEditor(VariableTablePanel panel) {
        this();
        setTablePanel(panel);
    }


    public void setTablePanel(VariableTablePanel panel) { tablePanel = panel; }


    public Object getCellEditorValue() {
        if (editingColumnName.equals("Type")) return dataTypeCombo.getSelectedItem();
        if (editingColumnName.endsWith("Value")) {
            return isBooleanValueRow() ? String.valueOf(checkBox.isSelected())
                    : valuePanel.getText();
        }
        if (editingColumnName.equals("Name")) return nameField.getText();
        return null;
    }


    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row,
                                                 int column) {
        tablePanel.setEditMode(true);
        editingColumnName = table.getColumnName(column);
        editingRow = row;

        if (editingColumnName.equals("Type")) {
            dataTypeCombo.setSelectedItem(value);
            return dataTypeCombo;
        }
        else if (editingColumnName.endsWith("Value")) {
            if (isBooleanValueRow()) {
                checkBox.setSelected(Boolean.valueOf((String) value));
                return checkBox;
            }
            valuePanel.setText((String) value);
            return valuePanel;
        }
        else {
            nameField.setText((String) value);
            return nameField;
        }
    }


    public boolean stopCellEditing() {
        isValid();
        tablePanel.setEditMode(false);
        return super.stopCellEditing();
    }


    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getActionCommand().equals("ShowDialog")) {
            showValueDialog((String) getCellEditorValue());
        }
        if (isValid()) {
            tablePanel.setEditMode(false);
            fireEditingStopped();
        }
    }


    public void caretUpdate(CaretEvent caretEvent) {
        tablePanel.setTableChanged();
    }


    private void showValueDialog(String value) {
        String dataType = tablePanel.getVariableAtRow(editingRow).getDataType();
        VariableValueDialog dialog = new VariableValueDialog(
                tablePanel.getVariableDialog(), nameField.getText(), dataType, value);
        String text = dialog.showDialog();
        if (text != null) {
            valuePanel.setText(text);
        }
    }


    private boolean isBooleanValueRow() {
        VariableRow varRow = tablePanel.getVariableAtRow(editingRow);
        return varRow.getDataType().equals("boolean");
    }

    private boolean isValid() {
        String value = (String) getCellEditorValue();
        VariableRow row = tablePanel.getVariableAtRow(editingRow);
        if (editingColumnName.equals("Name")) {
            row.setValidName(validateName(value));
            if (! row.isValidName()) return false;
            tablePanel.getVariableDialog().updateMappingsOnVarNameChange(row, value);
        }
        else if (editingColumnName.equals("Type")) {
            row.setValidValue(true);                 // varrow will re-initialise value
        }
        else if (editingColumnName.endsWith("Value")) {
            row.setValidValue(validateValue(value));
            if (! row.isValidValue()) return false;
        }

        tablePanel.clearStatus();
        return true;
    }


    private boolean validateName(String name) {
        String errMsg = null;
        if (name.length() == 0) {
            errMsg = "Name can't be empty";
        }
        else if (! isUniqueName(name)) {
            errMsg = "Duplicate variable name";
        }
        else if (! XMLChar.isValidName(name)) {
            errMsg = "Invalid XML name";
        }
        if (errMsg != null) {
            tablePanel.showErrorStatus(errMsg, null);
        }
        return errMsg == null;
    }


    private boolean validateValue(String value) {
        return validate(tablePanel.getVariableAtRow(editingRow).getDataType(), value);
    }


    private boolean validate(String dataType, String value) {
        if (StringUtil.isNullOrEmpty(value)) return true;

        try {
            java.util.List<String> errors = SpecificationModel.getHandler()
                    .getDataHandler().validate(dataType, value);
            if (!errors.isEmpty()) {
                tablePanel.showErrorStatus("Invalid value for data type", errors);
            }
            return (errors.isEmpty());
        }
        catch (YDataHandlerException ydhe) {
            tablePanel.showErrorStatus(ydhe.getMessage(), null);
            return false;
        }
    }


    private boolean isUniqueName(String name) {
        VariableTableModel model = tablePanel.getTable().getTableModel();
        for (int i=0; i< model.getRowCount(); i++) {
            if (i != editingRow && name.equals(model.getVariableAtRow(i).getName())) {
                return false;
            }
        }
        return true;
    }


    private Vector<String> getDataTypeNames() {
        try {
            return new Vector<String>(
                    SpecificationModel.getHandler().getDataHandler().getDataTypeNames());
        }
        catch (YDataHandlerException ydhe) {
            return new Vector<String>();
        }
    }

}
