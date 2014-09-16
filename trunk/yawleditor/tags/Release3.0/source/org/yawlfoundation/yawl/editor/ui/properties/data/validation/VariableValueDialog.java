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

package org.yawlfoundation.yawl.editor.ui.properties.data.validation;

import org.yawlfoundation.yawl.editor.ui.properties.data.VariableRow;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.component.ButtonBar;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.component.MiniToolBar;
import org.yawlfoundation.yawl.editor.ui.util.XMLUtilities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Author: Michael Adams
 * Creation Date: 16/08/2013
 */
public class VariableValueDialog extends JDialog implements ActionListener {

    private InstanceEditorPane _editorPane;
    private final VariableRow _row;
    private String _value;
    private ButtonBar _buttonBar;

    public VariableValueDialog(Window parent, VariableRow row, String value) {
        super(parent);
        _row = row;
        _value = format(value);
        setModal(true);
        setTitle("Edit Value for Variable " + row.getName());
        setResizable(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        add(getContent());
        setMinimumSize(new Dimension(420, 270));
        pack();
    }


    public String showDialog() {
        setVisible(true);
        return _value;
    }


    public String getText() { return _value; }


    public void actionPerformed(ActionEvent event) {
        String action = event.getActionCommand();
        if (action.equals("OK")) {
            setValueAndClose(_editorPane.getText());
        }
        else if (action.equals("generate")) {
            generateDefaultValue();
        }
        else if (action.equals("reset")) {
            _editorPane.getEditor().setText(_value);
        }
        else if (action.equals("format")) {
            _editorPane.getEditor().setText(format(_editorPane.getText()));
        }
        else {                                               // cancel
            setValueAndClose(null);
        }
    }


    private void setValueAndClose(String value) {
        _value = value;
        setVisible(false);
    }


    private JPanel getContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(5, 10, 5, 10));
        content.add(createToolBar(), BorderLayout.NORTH);
        _editorPane = new InstanceEditorPane(_row.getDataType(), _value);
        content.add(_editorPane, BorderLayout.CENTER);
        content.add(createButtonBar(), BorderLayout.SOUTH);
        return content;
    }


    private JPanel createToolBar() {
        MiniToolBar toolBar = new MiniToolBar(this);
        toolBar.addButton("generate", "generate", " Generate sample value ");
        toolBar.addButton("reset", "reset", " Reset to original value ");
        toolBar.addButton("format", "format", " Format text ");
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(toolBar, BorderLayout.EAST);
        panel.add(new JLabel(), BorderLayout.CENTER);
        return panel;
    }


    private JPanel createButtonBar() {
        _buttonBar = new ButtonBar(this);
        _buttonBar.setOKEnabled(true);
        _editorPane.setParentOKButton(_buttonBar.getOK());
        return _buttonBar;
    }


    private void generateDefaultValue() {
        String value = new SampleValueGenerator().generate(_row);
        if (value != null) _editorPane.getEditor().setText(format(value));
    }


    private String format(String text) {
        return XMLUtilities.formatXML(text, true, true);
    }

}
