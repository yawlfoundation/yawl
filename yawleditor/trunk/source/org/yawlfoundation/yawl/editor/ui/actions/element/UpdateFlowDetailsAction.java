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

package org.yawlfoundation.yawl.editor.ui.actions.element;


import org.yawlfoundation.yawl.editor.ui.actions.net.YAWLSelectedNetAction;
import org.yawlfoundation.yawl.editor.ui.elements.model.Decorator;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLFlowRelation;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.swing.AbstractOrderedTablePanel;
import org.yawlfoundation.yawl.editor.ui.swing.JUtilities;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;
import org.yawlfoundation.yawl.editor.ui.swing.data.FlowPriorityTable;
import org.yawlfoundation.yawl.editor.ui.swing.element.AbstractTaskDoneDialog;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

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
    putValue(Action.LONG_DESCRIPTION, "Update flow detail for this task.");
    putValue(Action.SMALL_ICON, getPNGIcon("arrow_divide"));
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
  private JLabel defaultLabel;

  private static final long serialVersionUID = 1L;
  
  public FlowPriorityDialog() {
    super(null, true, false);
    setResizable(false);
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

    panel.add(createDefaultLabel(), gbc);
    
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
    setDefaultLabelForSplitType(task);
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
  
  private JLabel createDefaultLabel() {
    defaultLabel = new JLabel("The bottom-most flow will be used as the default.");
    defaultLabel.setHorizontalAlignment(JLabel.CENTER);
    return defaultLabel;
  }

  public void setDefaultLabelForSplitType(YAWLTask task) {
      String text = "The bottom-most flow will be used as the default.";
      if (task.hasSplitDecorator() &&
         (task.getSplitDecorator().getType() == Decorator.XOR_TYPE)) {
          text = text.replaceFirst("be", "be set to 'true()' and");
      }
      defaultLabel.setText(text);
  }
}

class FlowDetailTablePanel extends AbstractOrderedTablePanel {

  private AbstractTaskDoneDialog parent;
  
  private FlowPriorityTable flowTable = new FlowPriorityTable();

  private NetGraph netOfTask;
  private Map<YAWLFlowRelation, Color> flowColours;             // previous flow colour

  private static final long serialVersionUID = 1L;
  
  public FlowDetailTablePanel(AbstractTaskDoneDialog parent) {
    super();
    this.parent = parent;
    setOrderedTable(flowTable);
    flowTable.setParentWindow(parent);
    setPreferredSize(new Dimension(450, 112));
  }
  
  public void setTaskAndNet(YAWLTask task, NetGraph net) {
    this.netOfTask = net;
    flowTable.setTaskAndNet(task, net);

    // don't show alternate row colours if there's less than 5 rows
    if (task.getSplitDecorator().getFlowCount() < 5)
        getOrderedTable().setOddRowColor(Color.WHITE);
    else
        getOrderedTable().setOddRowColor(Color.decode("0xFAEBD7"));  //default odd row colour
      
    rememberOriginalFlowColours(task);
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

  public void setFlowColours(Map<YAWLFlowRelation, Color> colours) {
      flowColours = colours;
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
        colorFlow(flow, flowColours.get(flow));
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

  private void rememberOriginalFlowColours(YAWLTask task) {
      SortedSet flows = task.getSplitDecorator().getFlowsInPriorityOrder();
      flowColours = new HashMap<YAWLFlowRelation, Color>();
      for (Object obj: flows) {
          YAWLFlowRelation flow = (YAWLFlowRelation) obj;
          Color origColor = netOfTask.getCellForeground(flow);
          if (origColor == null) origColor = Color.BLACK;
          flowColours.put(flow, origColor);
      }
  }
    
}

