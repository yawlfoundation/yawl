package org.yawlfoundation.yawl.editor.ui.properties.data;

import org.yawlfoundation.yawl.editor.core.data.YInternalType;
import org.yawlfoundation.yawl.editor.ui.data.DataSchemaValidator;
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
public class NetVariableRowStringEditor extends AbstractCellEditor
        implements TableCellEditor, ActionListener {

    private JTextField nameField;
    private JComboBox dataTypeCombo;
    private JTextField valueField;

    private JPanel valuePanel;

    private VariableTablePanel tablePanel;

    private int editingColumn;
    private int editingRow;

    private DataSchemaValidator dataValidator;


    public NetVariableRowStringEditor() {
        nameField = new JTextField();
        dataTypeCombo = new JComboBox(new Vector<String>(
                SpecificationModel.getSpec().getAllDataTypeNames()));
        valuePanel = createValueField();
        dataValidator = new DataSchemaValidator();

    }

    public NetVariableRowStringEditor(VariableTablePanel table) {
        this();
        setTablePanel(table);
    }


    public void setTablePanel(VariableTablePanel table) { tablePanel = table; }


    public Object getCellEditorValue() {
        switch (editingColumn) {
            case NetVarTableModel.TYPE_COLUMN: return dataTypeCombo.getSelectedItem();
            case NetVarTableModel.VALUE_COLUMN: return valueField.getText();
            default: return nameField.getText();
        }
    }


    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row,
                                                 int column) {
        editingColumn = column;
        editingRow = row;

        if (column == NetVarTableModel.TYPE_COLUMN) {
            dataTypeCombo.setSelectedItem(value);
            return dataTypeCombo;
        }
        else if (column == NetVarTableModel.VALUE_COLUMN) {
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
        if (editingColumn == NetVarTableModel.NAME_COLUMN && ! validateName(value)) {
            return false;
        }
        if (editingColumn == NetVarTableModel.VALUE_COLUMN && ! validateValue(value)) {
            return false;
        }
        tablePanel.clearStatus();
        return true;
    }


    private boolean validateName(String name) {
        String invalidChars = "<>&'\"";
        String errMsg = null;
        if (name.length() == 0) {
            errMsg = "Name can't be empty";
        }
        for (char c : name.toCharArray()) {
            if (Character.isWhitespace(c)) {
                errMsg = "Spaces not allowed in name";
                break;
            }
            else if (invalidChars.indexOf(c) > -1) {
                errMsg = "'" + c + "' character not allowed in name";
                break;
            }
        }
        if (errMsg != null) tablePanel.showErrorStatus(errMsg);
        return errMsg == null;
    }


    private boolean validateValue(String value) {
        String errors = "";
        String instance = "<dummy>" + value + "</dummy>";
        String dataType = tablePanel.getVariableAtRow(editingRow).getDataType();
        if (XSDType.getInstance().isBuiltInType(dataType)) {
            String typeDef = "<element name=\"dummy\" type=\"" + dataType + "\"/>";
            errors = dataValidator.validateBaseDataTypeInstance(typeDef, instance);
        }
        else if (YInternalType.isName(dataType)) {
            YInternalType internalType = YInternalType.valueOf(dataType);
            String typeDef = internalType.getValidationSchema("dummy");
            errors = dataValidator.validateBaseDataTypeInstance(typeDef, instance);
        }
        else {            // user defined complex type

        }
        if (errors.length() > 0) {
            tablePanel.showErrorStatus("Invalid value for data type");
        }
        return (errors.length() == 0);
    }

}
