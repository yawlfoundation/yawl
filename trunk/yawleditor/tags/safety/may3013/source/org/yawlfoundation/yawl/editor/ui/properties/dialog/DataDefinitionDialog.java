package org.yawlfoundation.yawl.editor.ui.properties.dialog;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.swing.AbstractDoneDialog;
import org.yawlfoundation.yawl.editor.ui.swing.data.JXMLSchemaEditorPane;
import org.yawlfoundation.yawl.editor.ui.swing.menu.DataTypeDialogToolBarMenu;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DataDefinitionDialog extends AbstractDoneDialog {

    protected JXMLSchemaEditorPane editorPane;

    public DataDefinitionDialog() {
        super("Update Data Type Definitions", true);
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
    }

    protected void makeLastAdjustments() {
        setSize(900, 800);
    }

    private JPanel getVariablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(12,12,0,11));
        editorPane = new JXMLSchemaEditorPane(true);
        panel.add(editorPane, BorderLayout.CENTER);
        return panel;
    }


    private JPanel getToolbarMenuPanel() {
        JPanel toolbarMenuPanel = new JPanel();
        toolbarMenuPanel.setLayout(new BoxLayout(toolbarMenuPanel, BoxLayout.X_AXIS));
        toolbarMenuPanel.add(new DataTypeDialogToolBarMenu(editorPane));
        toolbarMenuPanel.add(Box.createVerticalGlue());
        return toolbarMenuPanel;
    }

}
