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

import org.yawlfoundation.yawl.editor.data.DataVariable;
import org.yawlfoundation.yawl.editor.data.Decomposition;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.swing.AbstractDoneDialog;
import org.yawlfoundation.yawl.editor.swing.JUtilities;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class NetDecompositionUpdateDialog extends AbstractDoneDialog {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private DecompositionLabelField labelField;
  private DataVariableTablePanel dataVariablePanel;
  
  private Decomposition decomposition;
  
  private JLabel decompositionLabel;
  private TitledBorder titledBorder;
  
  private String oldLabel;

  protected LogPredicatesPanel logPredicatesPanel;
  
  public NetDecompositionUpdateDialog(Decomposition decomposition) {
    super("Update Decomposition", false);
    setContentPanel(getDecompositionPanel(DataVariable.SCOPE_NET));
    setDecomposition(decomposition);
    
    setTitle(DataVariable.SCOPE_NET);
    
    getDoneButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        oldLabel = getDecomposition().getLabel();
        
        if (!oldLabel.equals(labelField.getText())) {
          getDecomposition().setLabel(labelField.getText());
          SpecificationModel.getInstance().propogateDecompositionLabelChange(getDecomposition(), oldLabel);
        }

        if (logPredicatesPanel != null) {
            getDecomposition().setLogPredicateStarted(
                    logPredicatesPanel.getStartedPredicate());
            getDecomposition().setLogPredicateCompletion(
                    logPredicatesPanel.getCompletionPredicate());
        }
          
        SpecificationUndoManager.getInstance().setDirty(true);
      }
    });
    
    getRootPane().setDefaultButton(getDoneButton());
  }

  protected void makeLastAdjustments() {
    pack();
    JUtilities.setMinSizeToCurrent(this);
  }
  
  public void setVisible(boolean visible) {
    if (visible) {
      setContent();
      makeLastAdjustments();
    }
    super.setVisible(visible);
  }
  
  protected void setDecomposition(Decomposition decomposition) {
    this.decomposition = decomposition;
    if (logPredicatesPanel != null) {    // only for net level dialog
        logPredicatesPanel.setStartedPredicate(decomposition.getLogPredicateStarted());
        logPredicatesPanel.setCompletionPredicate(decomposition.getLogPredicateCompletion());
    }    
  }
  
  protected void setTitle(int scope) {
    setTitle("Update " + 
              DataVariable.scopeToString(scope) + 
              " Decomposition " + 
              (decomposition.getLabel().equals("") ? 
                  "" : " \"" + decomposition.getLabel() +
                   "\"")
    );
    decompositionLabel.setText(
        DataVariable.scopeToString(scope) + " Decomposition Label:"
    );
    titledBorder.setTitle(
        DataVariable.scopeToString(scope) + " Decomposition Variables"
    );
  }
  
  public Decomposition getDecomposition() {
    return this.decomposition;
  }
  
  protected JPanel getDecompositionPanel(int scope) {
    JPanel panel = new JPanel(new BorderLayout());
    if (scope == DataVariable.SCOPE_NET) {
        panel.add(buildBasePanel(), BorderLayout.CENTER);
    }
    else {
        panel.add(getGenericDecompositionPanel(), BorderLayout.CENTER);
    }
    return panel;
  }

    protected JTabbedPane buildBasePanel() {
      JTabbedPane pane = new JTabbedPane();
      pane.setFocusable(false);
      pane.setBorder(new EmptyBorder(5,5,5,5));

      pane.addTab("Standard", getGenericDecompositionPanel());

      logPredicatesPanel = new LogPredicatesPanel(25, 4, LogPredicatesPanel.Parent.Decomposition);
      pane.addTab("Log Predicates", logPredicatesPanel);

      return pane;
    }
    
  
  private JPanel getGenericDecompositionPanel() {

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    JPanel panel = new JPanel(gbl);
    panel.setBorder(new EmptyBorder(12,10,0,10));

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 0;
    gbc.weighty = 0;
    gbc.insets = new Insets(0,20,10,5);
    gbc.anchor = GridBagConstraints.EAST;
    
    decompositionLabel = new JLabel("Decomposition Label:");
    decompositionLabel.setDisplayedMnemonic('L');
    
    panel.add(decompositionLabel, gbc);

    gbc.gridx++;
    gbc.insets = new Insets(0,0,10,20);
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    panel.add(buildLabelField(),gbc);
    decompositionLabel.setLabelFor(labelField);
    
    gbc.gridy++;
    gbc.gridx = 0;
    gbc.gridwidth = 2;
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.gridy = 1;
    gbc.insets = new Insets(0,0,5,5);

    gbc.fill = GridBagConstraints.BOTH;
    panel.add(buildDataVariableTablePanel(),gbc);

    return panel;
  }

  private JTextField buildLabelField() {
    labelField = new DecompositionLabelField(10);
    labelField.addKeyListener(new LabelFieldDocumentListener());
    return labelField;
  }
  
  private DataVariableTablePanel buildDataVariableTablePanel() {
     dataVariablePanel = new DataVariableTablePanel(this);
     dataVariablePanel.setScope(DataVariable.SCOPE_NET);
     
     titledBorder = new TitledBorder("Decomposition Variables");
     
     dataVariablePanel.setBorder(
         new CompoundBorder(
             titledBorder,
             new EmptyBorder(0,5,5,6)
         )
     );
    return dataVariablePanel;
  }
  
  protected DataVariableTablePanel getDataVariablePanel() {
    return dataVariablePanel;
  }
  
  protected void setContent() {
    setTitle(DataVariable.SCOPE_NET);
    labelField.setDecomposition(decomposition);
    oldLabel = decomposition.getLabel();
    dataVariablePanel.setVariables(decomposition.getVariables());
    getDoneButton().setEnabled(
            (labelField.getText() != null) && (labelField.getText().length() > 0));  
  }
  
  class LabelFieldDocumentListener implements KeyListener {
    
    public void keyPressed(KeyEvent e) {
      // deliberately does nothing
    }
    
    public void keyTyped(KeyEvent e) {
      // deliberately does nothing
    }

    public void keyReleased(KeyEvent e) {
      getDoneButton().setEnabled(nameFieldValid());
    }

    private boolean nameFieldValid() {
      return labelField.getInputVerifier().verify(labelField);
    }
  }
  
  public void setDataVariablePanelEnabled(boolean enabled) {
    dataVariablePanel.setEnabled(enabled);
  }
}