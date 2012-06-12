/*
 * Created on 13/09/2004
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

package org.yawlfoundation.yawl.editor.ui.swing.data;

import org.yawlfoundation.yawl.editor.ui.data.DataVariableSet;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLFlowRelation;
import org.yawlfoundation.yawl.editor.ui.foundations.XMLUtilities;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.swing.AbstractDoneDialog;
import org.yawlfoundation.yawl.editor.ui.swing.JUtilities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class FlowPredicateUpdateDialog extends AbstractDoneDialog {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private AbstractDoneDialog parent;
  
  private boolean firstAppearance = true;
  
  private YAWLFlowRelation flow;
  private NetGraph net;
  
  private JXQueryEditorPane xQueryEditor;
  
  private JButton inputVariableQueryButton;
  protected DataVariableComboBox inputVariableComboBox;
  
  public FlowPredicateUpdateDialog(AbstractDoneDialog parent) {
    super("Update Flow Predicate", true);
    this.parent = parent;
    setContentPanel(getPredicatePanel());
    
    getDoneButton().addActionListener( 
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          flow.setPredicate(xQueryEditor.getText());
        }        
      }
    );
  }
  
  public void setFlow(YAWLFlowRelation flow, NetGraph net) {
    this.flow = flow;
    this.net = net;
    populateInputVariableComboBox();
    xQueryEditor.setText(flow.getPredicate());
  }

  protected void makeLastAdjustments() {
    pack();
    setSize(430,240);
    JUtilities.setMinSizeToCurrent(this);
  }
  
  public void setVisible(boolean isVisible) {
    if (isVisible) {
      if (firstAppearance) {
        this.setLocationRelativeTo(parent);
        firstAppearance = false;
      }
    }
    super.setVisible(isVisible);
  }
  
  private JPanel getPredicatePanel() {

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    JPanel panel = new JPanel(gbl);
    panel.setBorder(new EmptyBorder(12,12,0,11));

    // TODO: make variable query widget set a package for this and ParameterUpdateDialog
    
    gbc.gridy = 0;
    gbc.gridx = 0;
    gbc.weighty = 0;
    gbc.weightx = 0.333;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.EAST;

    gbc.insets = new Insets(0,0,5,5);
    JLabel inputVariableLabel = new JLabel("Net variable:");
    inputVariableLabel.setHorizontalAlignment(JLabel.RIGHT);
    inputVariableLabel.setDisplayedMnemonic('v');

    panel.add(inputVariableLabel, gbc);

    gbc.gridx++;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.insets = new Insets(0,5,5,5);
    panel.add(getInputVariableComboBox(),gbc);
    inputVariableLabel.setLabelFor(inputVariableComboBox);
    
    gbc.gridx++;
    gbc.anchor = GridBagConstraints.CENTER;
    panel.add(getNewInputVariableQueryButton(),gbc);
    
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridwidth = 3;
    gbc.weighty = 1;
    gbc.weightx = 1;
    gbc.fill = GridBagConstraints.BOTH;

    panel.add(getXQueryEditor(),gbc);
  
    return panel;
  }
  
  private void populateInputVariableComboBox() {
    inputVariableComboBox.setEnabled(false);

    inputVariableComboBox.setDecomposition(
        net.getNetModel().getDecomposition()
    );
    
    if (inputVariableComboBox.getItemCount() > 0) {
      inputVariableComboBox.setEnabled(true);
      inputVariableQueryButton.setEnabled(true);
    } else {
      inputVariableComboBox.setEnabled(false);
      inputVariableQueryButton.setEnabled(false);
    }
  }  
  
  private JComboBox getInputVariableComboBox() {
    inputVariableComboBox = new DataVariableComboBox(DataVariableSet.VALID_USAGE_ENTIRE_NET);
    return inputVariableComboBox;
  }

  private JButton getNewInputVariableQueryButton() {
    inputVariableQueryButton = new JButton("XPath Expression");
    inputVariableQueryButton.setToolTipText("Generates an XPath expression returning this variable");
    inputVariableQueryButton.setMnemonic(KeyEvent.VK_X);
    inputVariableQueryButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        try {
          xQueryEditor.getDocument().insertString(
              xQueryEditor.getCaretPosition(),
              XMLUtilities.getXPathPredicateExpression(inputVariableComboBox.getSelectedVariable()),
              null
          );        
        } catch (Exception e) {
          xQueryEditor.setText(
              xQueryEditor.getText() + 
              XMLUtilities.getXPathPredicateExpression(inputVariableComboBox.getSelectedVariable())
          );
        }
      }
    });

    return inputVariableQueryButton; 
  }
  
  private JXQueryEditorPane getXQueryEditor() {
    xQueryEditor = new JXQueryEditorPane(" = 'true'");
    xQueryEditor.setMinimumSize(new Dimension(400,400));
    return xQueryEditor;
  }
}
