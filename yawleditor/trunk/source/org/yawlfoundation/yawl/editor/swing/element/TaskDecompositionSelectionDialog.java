/*
 * Created on 21/02/2006
 * YAWLEditor v1.4
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

package org.yawlfoundation.yawl.editor.swing.element;

import org.yawlfoundation.yawl.editor.data.Decomposition;
import org.yawlfoundation.yawl.editor.data.WebServiceDecomposition;
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.swing.ActionAndFocusListener;
import org.yawlfoundation.yawl.editor.swing.JUtilities;
import org.yawlfoundation.yawl.editor.swing.data.TaskDecompositionUpdateDialog;
import org.yawlfoundation.yawl.editor.swing.data.WebServiceDecompositionComboBox;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class TaskDecompositionSelectionDialog extends AbstractTaskDoneDialog {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  protected String labelText;
  
  protected WebServiceDecompositionComboBox decompositionBox;
  
  private Decomposition chosenDecomposition = null;
  
  public TaskDecompositionSelectionDialog() {
    super("Select Decomposition for Task", true, true);
    setContentPanel(buildPanel());
    
    getDoneButton().addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        getGraph().setTaskDecomposition(
            getTask(),
            chosenDecomposition
          );          
          SpecificationUndoManager.getInstance().setDirty(true);
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
  
  private WebServiceDecompositionComboBox getDecompositionComboBox() {
    decompositionBox = new WebServiceDecompositionComboBox();
    decompositionBox.setEnabled(false); 
    
    new ActionAndFocusListener(decompositionBox) {
      protected void process(Object eventSource) {
        WebServiceDecompositionComboBox thisBox = (WebServiceDecompositionComboBox) eventSource;
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
        
        WebServiceDecomposition decomposition = new WebServiceDecomposition();

        getGraph().stopUndoableEdits();

        TaskDecompositionUpdateDialog updateDialog = 
          new TaskDecompositionUpdateDialog(decomposition, getGraph());

        // We have to wait on whether they pressed cancel or not to decide whether to keep the
        // decomposition
        
        updateDialog.setModal(true);
        
        JUtilities.centreWindowUnderVertex(getGraph(), updateDialog, getTask(), 10);
        updateDialog.setVisible(true);
        
        getGraph().startUndoableEdits();

        if (!updateDialog.cancelButtonSelected()) {

          SpecificationModel.getInstance().addWebServiceDecomposition(decomposition);

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
