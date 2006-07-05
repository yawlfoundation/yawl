/*
 * Created on 6/08/2004
 * YAWLEditor v1.01 
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2003 Queensland University of Technology
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

package au.edu.qut.yawl.editor.swing.data;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import au.edu.qut.yawl.editor.data.DataVariable;
import au.edu.qut.yawl.editor.swing.AbstractDoneDialog;
import au.edu.qut.yawl.editor.swing.ActionAndFocusListener;
import au.edu.qut.yawl.editor.swing.JUtilities;
import au.edu.qut.yawl.editor.swing.JXMLSchemaInstanceEditorPane;

public class NetDataVariableUpdateDialog extends TaskDataVariableUpdateDialog {
  
  protected JXMLSchemaInstanceEditorPane initialValueEditor;
  
  private JLabel initialValueLabel;
  private JScrollPane initialValueScrollPane;
  
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
    setSize(400,200);
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
    getUsageComboBox().setScope(DataVariable.SCOPE_NET);
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
    gbc.weighty = 0.5;
    gbc.fill = GridBagConstraints.BOTH;
    
    initialValueScrollPane = new JScrollPane(buildInitialValueEditor());
    panel.add(initialValueScrollPane,gbc);
    initialValueLabel.setLabelFor(initialValueEditor);
    
    return panel;
  }
  
  private JEditorPane buildInitialValueEditor() {
    initialValueEditor = new JXMLSchemaInstanceEditorPane();
    initialValueEditor.setMinimumSize(new Dimension(400,400));
    return initialValueEditor;
  }
  
  protected void setContent() {
    super.setContent();
    
    initialValueLabel.setVisible(true);
    initialValueScrollPane.setVisible(true);
    initialValueEditor.setText(getVariable().getInitialValue());
    initialValueEditor.setVariableType(getVariable().getDataType());  
    initialValueEditor.setVariableName(getVariable().getName());  
  }
}
