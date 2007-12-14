/*
 * Created on 09/10/2003
 * YAWLEditor v1.0 
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
 *
 */

package org.yawlfoundation.yawl.editor.actions.element;


import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import java.util.LinkedList;
import java.util.List;


import javax.swing.event.ListSelectionEvent;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;

import org.yawlfoundation.yawl.editor.actions.net.YAWLSelectedNetAction;
import org.yawlfoundation.yawl.editor.elements.model.Decorator;
import org.yawlfoundation.yawl.editor.elements.model.YAWLFlowRelation;
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.swing.AbstractOrderedTablePanel;
import org.yawlfoundation.yawl.editor.swing.JUtilities;
import org.yawlfoundation.yawl.editor.swing.TooltipTogglingWidget;
import org.yawlfoundation.yawl.editor.swing.element.AbstractTaskDoneDialog;
import org.yawlfoundation.yawl.editor.swing.data.FlowPriorityTable;

public class UpdateFlowDetailsAction extends YAWLSelectedNetAction 
                                     implements TooltipTogglingWidget {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  protected static final FlowPriorityDialog dialog = new FlowPriorityDialog();

  private NetGraph graph;
  private YAWLTask task;
  
  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Update Flow Detail...");
    putValue(Action.LONG_DESCRIPTION, "Update flow detail  for this task.");
    putValue(Action.SMALL_ICON, getIconByName("Blank"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_F));
  }
  
  public UpdateFlowDetailsAction(YAWLTask task, NetGraph graph) {
    super();
    this.task = task;
    this.graph = graph;
  } 
  
  public void actionPerformed(ActionEvent event) {
    dialog.setTask(task, graph);
    dialog.setVisible(true);
  }
  
  public String getEnabledTooltipText() {
    return " Update flow detail for this task ";
  }
  
  public String getDisabledTooltipText() {
    return " You must have a task with an XOR-Split or OR-Split decoration selected" + 
           " to update its flow detail ";
  }
  
  public boolean shouldBeVisible() {
    if (task.hasSplitDecorator() && (
        task.getSplitDecorator().getType() == Decorator.OR_TYPE || 
        task.getSplitDecorator().getType() == Decorator.XOR_TYPE )) {
       return true;
    }
    return false;
  }
}

class FlowPriorityDialog extends AbstractTaskDoneDialog {
  
  private FlowDetailTablePanel flowDetailPanel;
  private JButton updatePredicateButton;

  
  private static final long serialVersionUID = 1L;
  
  public FlowPriorityDialog() {
    super(null, true, false);
    
    buildContentPanel(
        new FlowDetailTablePanel(this)
    );
  }
  
  public void buildContentPanel(FlowDetailTablePanel flowPanel) {
    this.flowDetailPanel = flowPanel;
    
    JPanel panel = new JPanel();
    
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    panel.setLayout(gbl);

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.BOTH;

    panel.add(flowPanel, gbc);

    gbc.gridy++;
    gbc.gridwidth = 2;
    gbc.insets = new Insets(10,0,0,0);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    panel.add(getDefaultLabel(), gbc);
    
    gbc.gridy = 0;
    gbc.gridwidth = 1;
    gbc.gridx = 1;
    gbc.insets = new Insets(0,0,0,10);
    gbc.fill = GridBagConstraints.NONE;
    
    panel.add(getUpdatePredicateButton(), gbc);
    
    setContentPanel(panel);
  }
  
  public FlowDetailTablePanel getFlowDetailTablePanel() {
    return this.flowDetailPanel;  
  }
  
  protected void makeLastAdjustments() {
    pack();
    JUtilities.setMinSizeToCurrent(this);
  }
  
  public String getTitlePrefix() {
    return "Flow detail for ";
  }
  
  public void setTask(YAWLTask task, NetGraph graph) {
    super.setTask(task, graph);
    getFlowDetailTablePanel().setTaskAndNet(task, graph);
    getFlowDetailTablePanel().selectFlowAtRow(0);
    if (getFlowDetailTablePanel().hasFlows()) {
      updatePredicateButton.setEnabled(true);
    } else {
      updatePredicateButton.setEnabled(false);
    }
  }
  
  
  public void setVisible(boolean state) {
    if (state == true) {
      JUtilities.centreWindowUnderVertex(
          graph, 
          this, 
          getTask(), 
          10
      );
    }
    if (state == false) {
      getFlowDetailTablePanel().colorSelectedFlow(Color.BLACK);
    }
    super.setVisible(state);
  }

  
  private JButton getUpdatePredicateButton() {
    updatePredicateButton = new JButton("Predicate...");
    updatePredicateButton.setMnemonic(KeyEvent.VK_P);
    updatePredicateButton.setMargin(new Insets(2,11,3,12));
    updatePredicateButton.setToolTipText(" Change predicate of selected flow ");
    updatePredicateButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          getFlowDetailTablePanel().updatePredicateOfSelectedFlow();
        }
      }
    );
    return updatePredicateButton; 
   }
  
  private JLabel getDefaultLabel() {
    JLabel label = new JLabel("The bottom-most flow will be used as the default.");
    label.setHorizontalAlignment(JLabel.CENTER);
    return label;
  }
}

class FlowDetailTablePanel extends AbstractOrderedTablePanel {

  private AbstractTaskDoneDialog parent;
  
  private FlowPriorityTable flowTable = new FlowPriorityTable();

  private NetGraph netOfTask;

  private static final long serialVersionUID = 1L;
  
  public FlowDetailTablePanel(AbstractTaskDoneDialog parent) {
    super();
    this.parent = parent;
    setOrderedTable(flowTable);
    flowTable.setParentWindow(parent);
  }
  
  public void setTaskAndNet(YAWLTask task, NetGraph net) {
    this.netOfTask = net;
    flowTable.setTaskAndNet(task, net);
  }
  
  public NetGraph getNetOfTask() {
    return netOfTask;
  }
  
  public void updatePredicateOfSelectedFlow() {
    flowTable.updatePredicateOfSelectedFlow();
  }
  
  public void selectFlowAtRow(int rowNumber) {
    flowTable.selectRow(rowNumber);
  }
  
  public YAWLFlowRelation getSelectedFlow() {
    return flowTable.getSelectedFlow();
  }
  
  public boolean hasFlows() {
    return (flowTable.getRowCount() > 0);
  }
  
  public List<YAWLFlowRelation> getAllFlows() {
    return  flowTable.getFlowModel().getOrderedRows();
  }
  
  public void valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting()) {
      return;  // The mouse button has not yet been released
    }

    super.valueChanged(e);
    colorFlowOfSelectedRow();
  }

  private void colorFlowOfSelectedRow() {
    for(YAWLFlowRelation flow: getAllFlows()) {
      if (getSelectedFlow() == flow) {
        colorSelectedFlow(Color.GREEN.darker());
      } else {
        colorFlow(flow, Color.BLACK);
      }
    }
  }

  public void colorSelectedFlow(Color color) {
    colorFlow(getSelectedFlow(), color);
  }
  
  private void colorFlow(YAWLFlowRelation flow, Color color) {
    getNetOfTask().stopUndoableEdits();      
    getNetOfTask().changeCellForeground(flow, color);
    getNetOfTask().startUndoableEdits();      
  }
}

