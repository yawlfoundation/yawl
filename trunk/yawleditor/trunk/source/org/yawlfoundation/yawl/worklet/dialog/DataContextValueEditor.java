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

import org.yawlfoundation.yawl.editor.core.data.YDataHandlerException;
import org.yawlfoundation.yawl.editor.ui.properties.data.VariableRow;
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

/**
 * @author Michael Adams
 * @date 30/09/2014
 */
public class DataContextValueEditor extends AbstractCellEditor
        implements TableCellEditor, ActionListener, CaretListener {

    private DataContextTable _table;
    private final ValueField _valuePanel;


    public DataContextValueEditor() {
        super();
        _valuePanel = new ValueField(this, this);
    }

    public Object getCellEditorValue() {
        return _valuePanel.getText();
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row,
                                                 int column) {
        _table = (DataContextTable) table;
        setVisuals(_table.getSelectedVariable());
        _valuePanel.setText((String) value);
        return _valuePanel;
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


    public void caretUpdate(CaretEvent caretEvent) {
        setVisuals(_table.getSelectedVariable());
     }


    private void setVisuals(VariableRow row) {
        if (row == null) return;
        JTextField textField = _valuePanel.getTextField();
        if (row.isValidValue()) {
            textField.setBackground(Color.WHITE);
            textField.setToolTipText(null);
        }
        else {
            textField.setBackground(Color.PINK);
            textField.setToolTipText("Invalid value for data type");
        }
    }


    private void showValueDialog(String value) {
         VariableRow row = _table.getSelectedVariable();
         VariableValueDialog dialog = new VariableValueDialog(null, row, value);
         String text = dialog.showDialog();
         if (text != null) {
             _valuePanel.setText(text);
         }
    }


    private boolean isValid() {
        VariableRow row = _table.getSelectedVariable();
        row.setValidValue(validate(row.getDataType(), (String) getCellEditorValue()));
        return row.isValidValue();
    }


    private boolean validate(String dataType, String value) {
        if (StringUtil.isNullOrEmpty(value)) return true;
        try {
            java.util.List<String> errors = SpecificationModel.getHandler()
                    .getDataHandler().validate(dataType, value);
            return (errors.isEmpty());
        }
        catch (YDataHandlerException ydhe) {
            return false;
        }
    }


}
