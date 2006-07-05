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

package au.edu.qut.yawl.editor.actions.specification;

import java.awt.BorderLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import au.edu.qut.yawl.editor.YAWLEditor;
import au.edu.qut.yawl.editor.specification.SpecificationModel;
import au.edu.qut.yawl.editor.swing.AbstractDoneDialog;
import au.edu.qut.yawl.editor.swing.data.JXMLSchemaEditorPane;

public class UpdateDataTypeDefinitionsAction extends YAWLOpenSpecificationAction {
  private static final UpdateDataTypeDefinitionDialog dialog = new UpdateDataTypeDefinitionDialog();

  private boolean invokedAtLeastOnce = false;

  {
    putValue(Action.SHORT_DESCRIPTION, " Update Data Type Definitions. ");
    putValue(Action.NAME, "Update Data Type Definitions");
    putValue(Action.LONG_DESCRIPTION, "Update Data Type Definitions.");
    putValue(Action.SMALL_ICON, getIconByName("Blank"));
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
  
  protected JXMLSchemaEditorPane dataTypeDefinitionEditor;
  
  public UpdateDataTypeDefinitionDialog() {
    super("Update Data Type Definitions", true);
    setContentPanel(getVariablePanel());
    getDoneButton().addActionListener( 
      new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          SpecificationModel.getInstance().setDataTypeDefinition(
              dataTypeDefinitionEditor.getText()
          );
        }
      }
    );
  }

  protected void makeLastAdjustments() {
    setSize(600,300);
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
    }
    super.setVisible(state);
  }
  
  private JXMLSchemaEditorPane getDataTypeDefinitionEditor() {
    dataTypeDefinitionEditor = new JXMLSchemaEditorPane();
    
    return dataTypeDefinitionEditor;
  }
}