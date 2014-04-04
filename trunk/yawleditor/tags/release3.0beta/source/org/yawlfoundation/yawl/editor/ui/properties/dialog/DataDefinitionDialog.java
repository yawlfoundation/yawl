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

package org.yawlfoundation.yawl.editor.ui.properties.dialog;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.swing.AbstractDoneDialog;
import org.yawlfoundation.yawl.editor.ui.data.editorpane.XMLSchemaEditorPane;
import org.yawlfoundation.yawl.editor.ui.swing.menu.DataTypeDialogToolBarMenu;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;

public class DataDefinitionDialog extends AbstractDoneDialog implements CaretListener {

    private XMLSchemaEditorPane editorPane;

    public DataDefinitionDialog() {
        super("Update Data Type Definitions");
        setContentPanel(getVariablePanel());
        getContentPane().add(getToolbarMenuPanel(), BorderLayout.NORTH) ;
        getRootPane().setDefaultButton(getCancelButton());
        setLocationRelativeTo(YAWLEditor.getInstance());
    }


    public String getContent() {
        return cancelButtonSelected() ? null : editorPane.getText();
    }

    public void setContent(String content) {
        editorPane.setText(content);
        editorPane.getEditor().setCaretPosition(1);
        editorPane.getEditor().addCaretListener(this);
    }


    public void caretUpdate(CaretEvent caretEvent) {
        editorPane.getEditor().validateContent();
        getDoneButton().setEnabled(editorPane.isContentValid());
    }

    protected void makeLastAdjustments() {
        setSize(900, 800);
    }

    private JPanel getVariablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(12,12,0,11));
        editorPane = new XMLSchemaEditorPane(true);
        panel.add(editorPane, BorderLayout.CENTER);
        return panel;
    }


    private JPanel getToolbarMenuPanel() {
        JPanel toolbarMenuPanel = new JPanel();
        toolbarMenuPanel.setLayout(new BoxLayout(toolbarMenuPanel, BoxLayout.X_AXIS));
        toolbarMenuPanel.add(new DataTypeDialogToolBarMenu(this, editorPane));
        toolbarMenuPanel.add(Box.createVerticalGlue());
        return toolbarMenuPanel;
    }

}
