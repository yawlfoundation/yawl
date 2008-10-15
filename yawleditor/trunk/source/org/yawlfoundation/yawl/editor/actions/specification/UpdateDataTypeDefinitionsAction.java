/*
 * Created on 11/06/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.yawlfoundation.yawl.editor.actions.specification;

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.swing.AbstractDoneDialog;
import org.yawlfoundation.yawl.editor.swing.data.JXMLSchemaEditorPane;
import org.yawlfoundation.yawl.editor.swing.menu.DataTypeDialogToolBarMenu;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UpdateDataTypeDefinitionsAction extends YAWLOpenSpecificationAction {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final UpdateDataTypeDefinitionDialog dialog = new UpdateDataTypeDefinitionDialog();

  private boolean invokedAtLeastOnce = false;

  {
    putValue(Action.SHORT_DESCRIPTION, " Update Data Type Definitions. ");
    putValue(Action.NAME, "Update Data Type Definitions");
    putValue(Action.LONG_DESCRIPTION, "Update Data Type Definitions.");
    putValue(Action.SMALL_ICON, getPNGIcon("page_white_code"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_D));
  }

  public void actionPerformed(ActionEvent event) {
    if (!invokedAtLeastOnce) {
      dialog.setLocationRelativeTo(YAWLEditor.getInstance());
      invokedAtLeastOnce = true;       
    }
    dialog.setVisible(true);
  }
}

class UpdateDataTypeDefinitionDialog extends AbstractDoneDialog {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  protected JXMLSchemaEditorPane dataTypeDefinitionEditor;
  
  public UpdateDataTypeDefinitionDialog() {
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
    setSize(800,800);
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
      dataTypeDefinitionEditor.getEditor().setCaretPosition(56);
    }
    super.setVisible(state);
  }
  
  private JXMLSchemaEditorPane getDataTypeDefinitionEditor() {
    dataTypeDefinitionEditor = new JXMLSchemaEditorPane();
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