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
import org.yawlfoundation.yawl.editor.ui.properties.data.validation.VariableValueDialog;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
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
        implements TableCellEditor, ActionListener {

    private JTextField nameField;
    private JComboBox dataTypeCombo;
    private JTextField valueField;
    private JCheckBox checkBox;

    private JPanel valuePanel;
    private VariableTablePanel tablePanel;
    private String editingColumnName;
    private int editingRow;

    private static final String INVALID_CHARS = "<>&'\"";


    public VariableRowStringEditor() {
        nameField = new JTextField();
        dataTypeCombo = new JComboBox(new Vector<String>(
                SpecificationModel.getHandler().getDataHandler().getDataTypeNames()));
        dataTypeCombo.addActionListener(this);
        valuePanel = createValueField();
    }

    public VariableRowStringEditor(VariableTablePanel table) {
        this();
        setTablePanel(table);
    }


    public void setTablePanel(VariableTablePanel table) { tablePanel = table; }


    public Object getCellEditorValue() {
        if (editingColumnName.equals("Type")) return dataTypeCombo.getSelectedItem();
        if (editingColumnName.equals("Value")) {
            return checkBox != null ? String.valueOf(checkBox.isSelected()) : valueField.getText();
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
        else if (editingColumnName.equals("Value")) {
            VariableRow varRow = tablePanel.getVariableAtRow(row);
            if (varRow.getDataType().equals("boolean")) {
                checkBox = new JCheckBox();
                checkBox.setSelected(Boolean.valueOf((String) value));
                return checkBox;
            }
            valueField.setText((String) value);
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


    private JPanel createValueField() {
        valueField = new JTextField();
        JButton btnExpand = new JButton("...");
        btnExpand.setPreferredSize(new Dimension(20, 20));
        btnExpand.addActionListener(this);
        btnExpand.setActionCommand("ShowDialog");
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(btnExpand, BorderLayout.EAST);
        panel.add(valueField, BorderLayout.CENTER);
        return panel;
    }


    private void showValueDialog(String value) {
        String dataType = tablePanel.getVariableAtRow(editingRow).getDataType();
        VariableValueDialog dialog = new VariableValueDialog(
                tablePanel.getVariableDialog(), nameField.getText(), dataType, value);
        String text = dialog.showDialog();
        if (text != null) {
            valueField.setText(text);
        }
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
            row.setValidValue(validateType(value));
            if (! row.isValidValue()) return false;
        }
        else if (editingColumnName.equals("Value")) {
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
            errMsg = "There is already a variable with that name";
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

    private boolean validateType(String dataType) {
        return validate(dataType, tablePanel.getVariableAtRow(editingRow).getValue());
    }


    private boolean validate(String dataType, String value) {
        if (StringUtil.isNullOrEmpty(value)) return true;

        java.util.List<String> errors = SpecificationModel.getHandler().getDataHandler()
                    .validate(dataType, value);
        if (! errors.isEmpty()) {
            tablePanel.showErrorStatus("Invalid value for data type", errors);
        }
        return (errors.isEmpty());
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

}
