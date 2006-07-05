/*
 * Created on 09/10/2003
 * YAWLEditor v1.0 
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
 *
 */

package au.edu.qut.yawl.editor.actions.element;

import au.edu.qut.yawl.editor.net.NetGraph;
import au.edu.qut.yawl.editor.actions.net.YAWLSelectedNetAction;
import au.edu.qut.yawl.editor.elements.model.YAWLTask;
import au.edu.qut.yawl.editor.specification.SpecificationModel;

import au.edu.qut.yawl.editor.data.Decomposition;
import au.edu.qut.yawl.editor.data.WebServiceDecomposition;

import au.edu.qut.yawl.editor.swing.ActionAndFocusListener;
import au.edu.qut.yawl.editor.swing.JUtilities;
import au.edu.qut.yawl.editor.swing.element.AbstractTaskDoneDialog;
import au.edu.qut.yawl.editor.swing.data.DecompositionComboBox;
import au.edu.qut.yawl.editor.swing.data.TaskDecompositionUpdateDialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

public class SelectTaskDecompositionAction extends YAWLSelectedNetAction {

  private static final DecompositionDialog dialog = new DecompositionDialog();

  private NetGraph graph;
  private YAWLTask task;
  
  {
    putValue(Action.SHORT_DESCRIPTION, " Select which decomposition this task uses");
    putValue(Action.NAME, "Select Task Decomposition...");
    putValue(Action.LONG_DESCRIPTION, "Select which decomposition this task uses.");
    putValue(Action.SMALL_ICON, getIconByName("LabelElement"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_S));
  }
  
  public SelectTaskDecompositionAction(YAWLTask task, NetGraph graph) {
    super();
    this.task = task;
    this.graph = graph;
  }  

  public void actionPerformed(ActionEvent event) {
    dialog.setTask(task, graph);
    dialog.setVisible(true);
    graph.clearSelection();
  }
}

class DecompositionDialog extends AbstractTaskDoneDialog {
  protected String labelText;
  
  protected DecompositionComboBox decompositionBox;
  
  private Decomposition chosenDecomposition = null;
  
  public DecompositionDialog() {
    super("Select Decomposition for Task", true, true);
    setContentPanel(buildPanel());
    
    getDoneButton().addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        getGraph().setTaskDecomposition(
            getTask(),
            chosenDecomposition
          );
      }
    });
  }
  
  private JPanel buildPanel() {
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    
    JPanel panel = new JPanel(gbl);

    panel.setBorder(new EmptyBorder(12,12,0,11));

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(0,0,0,5);
    gbc.anchor = GridBagConstraints.EAST;

    JLabel label = new JLabel("Set decomposition to:");
    label.setDisplayedMnemonic('S');
    panel.add(label, gbc);
    
    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;

    decompositionBox = getDecompositionComboBox();

    label.setLabelFor(decompositionBox);
    
    panel.add(decompositionBox, gbc);

    gbc.gridx++;
    gbc.anchor = GridBagConstraints.CENTER;
    
    panel.add(getCreateButton(), gbc);

    return panel;
  }
  
  private DecompositionComboBox getDecompositionComboBox() {
    decompositionBox = new DecompositionComboBox();
    decompositionBox.setEnabled(false); 
    
    new ActionAndFocusListener(decompositionBox) {
      protected void process(Object eventSource) {
        DecompositionComboBox thisBox = (DecompositionComboBox) eventSource;
        if (thisBox.isEnabled()) {
          
          chosenDecomposition =  
            SpecificationModel.getInstance().getDecompositionFromLabel(
              (String)thisBox.getSelectedItem()
            );    
        }
      }
    };
    return decompositionBox;
  }
  
  private JButton getCreateButton() {
    JButton button = new JButton("Create...");
    button.setMnemonic(KeyEvent.VK_C);
    
    final AbstractTaskDoneDialog dialog = this;
    
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        
        Decomposition decomposition = new WebServiceDecomposition();

        getGraph().stopUndoableEdits();

        TaskDecompositionUpdateDialog updateDialog = new TaskDecompositionUpdateDialog(decomposition);

        // We have to wait on whether they pressed cancel or not to decide whether to keep the
        // decomposition
        
        updateDialog.setModal(true);
        
        JUtilities.centreWindowUnderVertex(getGraph(), updateDialog, getTask(), 10);
        updateDialog.setVisible(true);
        
        getGraph().startUndoableEdits();

        if (!updateDialog.cancelButtonSelected()) {

          SpecificationModel.getInstance().addDecomposition(decomposition);

          getGraph().setTaskDecomposition(
              getTask(),
              decomposition
          );
        } 
        
        dialog.setVisible(false);
      }
    });
    
    return button;
  }

  public void setTask(YAWLTask task, NetGraph graph) {
    super.setTask(task,graph);
    chosenDecomposition = task.getDecomposition();
    refresh();
  }
  
  private void refresh() {
    decompositionBox.setEnabled(false);
    decompositionBox.refresh();

    if (chosenDecomposition != null) {
      decompositionBox.setSelectedItem(chosenDecomposition.getLabel());
    }
    
    if (decompositionBox.getItemCount() > 0) {
      if (chosenDecomposition == null) {
        chosenDecomposition =  
          SpecificationModel.getInstance().getDecompositionFromLabel(
            (String)decompositionBox.getSelectedItem()
          );    
      }
      decompositionBox.setEnabled(true);
    }
    
    pack();
  }
}
