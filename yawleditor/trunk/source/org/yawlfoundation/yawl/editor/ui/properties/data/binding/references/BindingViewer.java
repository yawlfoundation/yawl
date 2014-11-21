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

package org.yawlfoundation.yawl.editor.ui.properties.data.binding.references;

import org.yawlfoundation.yawl.editor.core.data.BindingReference;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.TextAreaDialog;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.component.ValueField;
import org.yawlfoundation.yawl.editor.ui.util.XMLUtilities;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * @author Michael Adams
 * @date 8/08/12
 */
public class BindingViewer extends AbstractCellEditor
        implements TableCellEditor, ActionListener {

    private final ValueField valuePanel;
    private BindingReferencesDialog parent;


    public BindingViewer(BindingReferencesDialog parent) {
        valuePanel = new ValueField(this, null);
        this.parent = parent;
        setKeyListener();
    }


    public Object getCellEditorValue() {
        return valuePanel.getText();
    }


    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row,
                                                 int column) {
        valuePanel.setText((String) value);
        return valuePanel;
    }


    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getActionCommand().equals("ShowDialog")) {
            showValueDialog((String) getCellEditorValue());
        }
        fireEditingStopped();
    }


    private void showValueDialog(String value) {
        TextAreaDialog dialog = new TextAreaDialog(parent,
                XMLUtilities.formatXML(value, true, true));
        dialog.setTitle("Binding");
        dialog.setEditable(false);
        dialog.setResizable(true);
        setSelected(dialog);
        dialog.showDialog();
    }


    private void setSelected(TextAreaDialog dialog) {
        BindingReference selected = parent.getTable().getSelectedReference();
        if (selected != null) {
            String key = selected.getBindingKey();
            String binding = dialog.getText();
            int start = binding.indexOf(key);
            if (start > -1) {
                int end = start + key.length();
                char next = binding.charAt(end);
                end += (next == '*' ? 1 : 6);
                if (end < binding.length()) {
                    dialog.setSelection(start, end);
                }
            }
        }
    }


    private void setKeyListener() {
        valuePanel.getTextField().addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                e.consume();  // ignore event
            }
        });
    }

}
