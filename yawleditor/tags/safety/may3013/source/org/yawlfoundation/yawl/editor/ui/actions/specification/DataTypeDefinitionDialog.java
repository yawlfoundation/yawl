package org.yawlfoundation.yawl.editor.ui.actions.specification;

import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.ui.swing.AbstractDoneDialog;
import org.yawlfoundation.yawl.editor.ui.swing.data.JXMLSchemaEditorPane;
import org.yawlfoundation.yawl.editor.ui.swing.menu.DataTypeDialogToolBarMenu;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 */
public class DataTypeDefinitionDialog extends AbstractDoneDialog {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  protected JXMLSchemaEditorPane dataTypeDefinitionEditor;

    public DataTypeDefinitionDialog() {
    super("Update Data Type Definitions", true);
    setContentPanel(getVariablePanel());
    getContentPane().add(getToolbarMenuPanel(), BorderLayout.NORTH) ;
    getDoneButton().addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          SpecificationModel.getInstance().setDataTypeDefinition(
              dataTypeDefinitionEditor.getText()
          );
          SpecificationUndoManager.getInstance().setDirty(true);
        }
      }
    );
    getRootPane().setDefaultButton(getCancelButton());
  }

  protected void makeLastAdjustments() {
    setSize(900,800);
  }

  private JPanel getVariablePanel() {

    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(12,12,0,11));

    panel.add(getDataTypeDefinitionEditor(),BorderLayout.CENTER);

    return panel;
  }

  public void setVisible(boolean state) {
    if (state) {
      dataTypeDefinitionEditor.setText(
          SpecificationModel.getInstance().getDataTypeDefinition()
      );
      dataTypeDefinitionEditor.getEditor().setCaretPosition(1);
    }
    super.setVisible(state);
  }

  private JXMLSchemaEditorPane getDataTypeDefinitionEditor() {
    dataTypeDefinitionEditor = new JXMLSchemaEditorPane(true);
    return dataTypeDefinitionEditor;
  }

    private JPanel getToolbarMenuPanel() {
      JPanel toolbarMenuPanel = new JPanel();
      toolbarMenuPanel.setLayout(new BoxLayout(toolbarMenuPanel, BoxLayout.X_AXIS));
      toolbarMenuPanel.add(new DataTypeDialogToolBarMenu(dataTypeDefinitionEditor));
      toolbarMenuPanel.add(Box.createVerticalGlue());
      return toolbarMenuPanel;
    }


}
