/*
 * Created on 6/08/2004
 * YAWLEditor v1.01 
 *
 * @author Lindsay Bradford
 * 
 * 
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

package org.yawlfoundation.yawl.editor.swing.data;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.yawlfoundation.yawl.editor.data.DataVariable;
import org.yawlfoundation.yawl.editor.swing.AbstractDoneDialog;
import org.yawlfoundation.yawl.editor.swing.ActionAndFocusListener;
import org.yawlfoundation.yawl.editor.swing.JUtilities;

public class NetDataVariableUpdateDialog extends TaskDataVariableUpdateDialog {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  protected JXMLSchemaInstanceEditorPane initialValueEditor;
  
  private JLabel initialValueLabel;
  
  public NetDataVariableUpdateDialog(AbstractDoneDialog parent) {
    super(parent);

    getDoneButton().addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          getVariable().setInitialValue(initialValueEditor.getText());
        }
      }
    );
    
    addExtraEventListeners();
  }

  protected void makeLastAdjustments() {
    setSize(400,300);
    JUtilities.setMinSizeToCurrent(this);
  }
  
  public void setVariable(DataVariable variable) {
    super.setVariable(variable);
    setTitle(DataVariable.SCOPE_NET);
  }
  
  private void addExtraEventListeners() {
    new ActionAndFocusListener(getTypeComboBox()) {
      protected void process(Object eventSource) {
        DataTypeComboBox thisBox = (DataTypeComboBox) eventSource;
        if (thisBox.isEnabled()) {
          initialValueEditor.setVariableType((String) thisBox.getSelectedItem());   
        }
      }
    };
    new ActionAndFocusListener(getNameField()) {
      protected void process(Object eventSource) {
        JTextField thisField = (JTextField) eventSource;
        initialValueEditor.setVariableName(thisField.getText()); 
      }
    };
    new ActionAndFocusListener(getUsageComboBox()) {
      protected void process(Object eventSource) {
        enableInitialValueEditorIfAppropriate();
      }
    };
    getUsageComboBox().setScope(DataVariable.SCOPE_NET);
  }
  
  private void enableInitialValueEditorIfAppropriate() {
    if (getUsageComboBox().isEnabled() && getUsageComboBox().getSelectedItem() != null) {
      if (((String)getUsageComboBox().getSelectedItem()).equals("Local")) {
        initialValueEditor.setEnabled(true);
      } else {
        initialValueEditor.setEnabled(false);
      }
    }
  }
  
  protected JPanel getVariablePanel() {
    JPanel panel = super.getVariablePanel();
    
    panel.add(getInitialValuePanel(), BorderLayout.CENTER);

    return panel;
  }
  
  protected JPanel getInitialValuePanel() {

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    JPanel panel = new JPanel(gbl);
    panel.setBorder(new EmptyBorder(12,12,0,11));

    gbc.insets = new Insets(0,0,5,5);
    gbc.anchor = GridBagConstraints.NORTHEAST;

    initialValueLabel = new JLabel("Initial Value:");
    initialValueLabel.setDisplayedMnemonic('V');
    
    panel.add(initialValueLabel, gbc);

    gbc.gridwidth = 5;
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.fill = GridBagConstraints.BOTH;
    
    panel.add(buildInitialValueEditor(),gbc);
    initialValueLabel.setLabelFor(initialValueEditor);
    
    return panel;
  }
  
  private JXMLSchemaInstanceEditorPane buildInitialValueEditor() {
    initialValueEditor = new JXMLSchemaInstanceEditorPane();
    return initialValueEditor;
  }
  
  protected void setContent() {
    super.setContent();
    
    initialValueLabel.setVisible(true);
    initialValueEditor.setVisible(true);
    initialValueEditor.setText(getVariable().getInitialValue());
    initialValueEditor.setVariableName(getVariable().getName());  
    initialValueEditor.setVariableType(getVariable().getDataType());  
    enableInitialValueEditorIfAppropriate();
  }
  
  //BEGIN MLF
  protected JComponent createExtendedAttributePanel() {
      //do nothing -> extended attributes are meaningless at net level
      return null;
  }
  //END MLF
}
