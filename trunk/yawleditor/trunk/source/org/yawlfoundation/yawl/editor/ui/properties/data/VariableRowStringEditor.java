package org.yawlfoundation.yawl.editor.ui.properties.data;

import org.yawlfoundation.yawl.editor.core.data.YInternalType;
import org.yawlfoundation.yawl.editor.core.data.DataSchemaValidator;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.TextAreaDialog;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.schema.XSDType;

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
    private DataSchemaValidator dataValidator;

    private static final String INVALID_CHARS = "<>&'\"";
    private static final String SCHEMA_HEADER =
            "<schema xmlns=\"http://www.w3.org/2001/XMLSchema\">";
    private static final String SCHEMA_CLOSER = "</schema>";

    public VariableRowStringEditor() {
        nameField = new JTextField();
        dataTypeCombo = new JComboBox(new Vector<String>(
                SpecificationModel.getHandler().getDataHandler().getDataTypeNames()));
        valuePanel = createValueField();
        dataValidator = new DataSchemaValidator();

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
        editingColumnName = table.getColumnName(column);
        editingRow = row;

        if (editingColumnName.equals("Type")) {
            dataTypeCombo.setSelectedItem(value);
            return dataTypeCombo;
        }
        else if (editingColumnName.equals("Value")) {
            VariableRow varRow = ((VariableTable) table).getTableModel().getVariableAtRow(row);
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
        return (isValid()) && super.stopCellEditing();
    }


    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getActionCommand().equals("ShowDialog")) {
        //    showXQueryDialog();       // todo
            showTextDialog((String) getCellEditorValue());
        }
        if (isValid()) fireEditingStopped();
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


    private void showTextDialog(String value) {
        TextAreaDialog dialog = new TextAreaDialog(null, null, value);
        String text = dialog.showDialog();
        if (text != null) {
            valueField.setText(text);
        }
    }


    private boolean isValid() {
        String value = (String) getCellEditorValue();
        if (editingColumnName.equals("Name") && ! validateName(value)) {
            return false;
        }
        if (editingColumnName.equals("Value") && ! validateValue(value)) {
            return false;
        }
        tablePanel.clearStatus();
        return true;
    }


    private boolean validateName(String name) {
        String errMsg = null;
        if (name.length() == 0) {
            errMsg = "Name can't be empty";
        }
        for (char c : name.toCharArray()) {
            if (Character.isWhitespace(c)) {
                errMsg = "Spaces not allowed in name";
                break;
            }
            else if (INVALID_CHARS.indexOf(c) > -1) {
                errMsg = "'" + c + "' character not allowed in name";
                break;
            }
        }
        if (errMsg != null) tablePanel.showErrorStatus(errMsg);
        return errMsg == null;
    }


    private boolean validateValue(String value) {
        if (value.length() == 0) return true;
        String errors = "";
        String instance = "<dummy>" + value + "</dummy>";
        String dataType = tablePanel.getVariableAtRow(editingRow).getDataType();
        if (XSDType.getInstance().isBuiltInType(dataType)) {
            dataValidator.setDataTypeSchema(makeSchema(
                    "<element name=\"dummy\" type=\"" + dataType + "\"/>"));
            errors = dataValidator.validate(instance);
        }
        else if (YInternalType.isName(dataType)) {
            YInternalType internalType = YInternalType.valueOf(dataType);
            dataValidator.setDataTypeSchema(makeSchema(
                    internalType.getValidationSchema("dummy")));
            errors = dataValidator.validate(instance);
        }
        else {            // user defined complex type
                 //todo
        }
        if (errors.length() > 0) {
            tablePanel.showErrorStatus("Invalid value for data type");
        }
        return (errors.length() == 0);
    }

    private String makeSchema(String innards) {
        return SCHEMA_HEADER + innards + SCHEMA_CLOSER;
    }

}
