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

package au.edu.qut.yawl.editor.actions.element;


import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import java.util.LinkedList;


import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import au.edu.qut.yawl.editor.foundations.ResourceLoader;
import au.edu.qut.yawl.editor.actions.net.YAWLSelectedNetAction;
import au.edu.qut.yawl.editor.elements.model.YAWLFlowRelation;
import au.edu.qut.yawl.editor.elements.model.YAWLTask;
import au.edu.qut.yawl.editor.net.NetGraph;
import au.edu.qut.yawl.editor.swing.JUtilities;
import au.edu.qut.yawl.editor.swing.TooltipTogglingWidget;
import au.edu.qut.yawl.editor.swing.element.AbstractTaskDoneDialog;
import au.edu.qut.yawl.editor.swing.data.FlowPriorityTable;

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
}

class FlowPriorityDialog extends AbstractTaskDoneDialog implements ListSelectionListener {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private YAWLFlowRelation oldSelectedFlow;

  public static final int NO_ELEMENTS = 0;
  public static final int SOME_ELEMENTS = 1;
  
  private JButton increasePriorityButton;
  private JButton decreasePriorityButton;
  private JButton updatePredicateButton;
   
  private FlowPriorityTable flowTable = new FlowPriorityTable(this);
 
  public FlowPriorityDialog() {
    super(null, true, false);
    setContentPanel(getFlowPriorityPanel());
    flowTable.getSelectionModel().addListSelectionListener(this);
  }
  
  public void valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting()) {
      return;  // The mouse button has not yet been released
    }

    int row = flowTable.getSelectedRow();

    increasePriorityButton.setEnabled(true);
    decreasePriorityButton.setEnabled(true);
    
    if (row == 0) {
      increasePriorityButton.setEnabled(false);
    } 
    if (row == (flowTable.getRowCount() - 1)) {
      decreasePriorityButton.setEnabled(false);
    } 
    colorSelectedRow();
  }
  
  private void colorSelectedRow() {
    YAWLFlowRelation selectedFlow = 
      flowTable.getFlowModel().getFlowAt(
          flowTable.getSelectedRow()
      );
    if (oldSelectedFlow != null) {
      colorFlow(oldSelectedFlow, Color.BLACK);
    }
    if (selectedFlow != null) {
     colorFlow(selectedFlow, Color.GREEN.darker());
     oldSelectedFlow = selectedFlow;
    }
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
    flowTable.setTask(task, graph);
    updateState();
    flowTable.selectRow(0);
  }
  
  private JPanel getFlowPriorityPanel() {
    JPanel panel = new JPanel();
    panel.setBorder(new EmptyBorder(12,12,0,11));

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    panel.setLayout(gbl);

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridheight = 5;
    gbc.weightx = 1;
    gbc.insets = new Insets(0,0,0,5);
    gbc.fill = GridBagConstraints.BOTH;
    
    panel.add(new JScrollPane(flowTable),gbc);

    gbc.gridx = 1;
    gbc.gridheight = 1;
    gbc.weightx = 0;
    gbc.weighty = 0.5;
    gbc.insets = new Insets(0,5,5,0);
    panel.add(Box.createVerticalGlue(),gbc);

    gbc.gridy++;
    gbc.weighty = 0;
    gbc.anchor = GridBagConstraints.CENTER;
    panel.add(getIncreasePriorityButton(), gbc);

    gbc.gridy++;
    panel.add(getDecreasePriorityButton(), gbc);

    gbc.gridy++;
    panel.add(getUpdatePredicateButton(), gbc);

    gbc.gridy++;
    gbc.weighty = 0.5;
    gbc.fill = GridBagConstraints.BOTH;
    panel.add(Box.createVerticalGlue(),gbc);

    gbc.gridx=0;
    gbc.gridy=5;
    gbc.weighty = 0;
    gbc.weightx = 1;
    gbc.gridwidth = 2;
    gbc.insets = new Insets(10,5,5,0);
    gbc.fill = GridBagConstraints.BOTH;
    panel.add(getDefaultLabel(),gbc);
    
    LinkedList buttonList = new LinkedList();
    buttonList.add(increasePriorityButton);
    buttonList.add(decreasePriorityButton);
    buttonList.add(updatePredicateButton);
    
    JUtilities.equalizeComponentSizes(buttonList);
    
    return panel;    
  }
  
  private JButton getIncreasePriorityButton() {
    increasePriorityButton = new JButton();
    increasePriorityButton.setIcon(getIconByName("Up"));
    increasePriorityButton.setMargin(new Insets(0,11,0,12));
    increasePriorityButton.setToolTipText(" Increase selected flow's priority ");
    increasePriorityButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          flowTable.increaseRowPriority();
        }
      }
    );
    return increasePriorityButton; 
   }

  private JButton getDecreasePriorityButton() {
    decreasePriorityButton = new JButton();
    decreasePriorityButton.setIcon(getIconByName("Down"));
    decreasePriorityButton.setMargin(new Insets(0,11,0,12));
    decreasePriorityButton.setToolTipText(" Decrease selected flow's priority ");
    decreasePriorityButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          flowTable.decreaseRowPriority();
        }
      }
    );
    return decreasePriorityButton; 
   }
  
  private JButton getUpdatePredicateButton() {
    updatePredicateButton = new JButton("Predicate...");
    updatePredicateButton.setMnemonic(KeyEvent.VK_P);
    updatePredicateButton.setMargin(new Insets(2,11,3,12));
    updatePredicateButton.setToolTipText(" Change predicate of selected flow ");
    updatePredicateButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          flowTable.updatePredicateOfSelectedFlow();
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
  
  public void setVisible(boolean state) {
    if (state == true) {
      JUtilities.centreWindowUnderVertex(graph, this, getTask(), 10);
    }
    if (state == false) {
      colorFlow(
        flowTable.getFlowModel().getFlowAt(flowTable.getSelectedRow()), 
        Color.BLACK
      );
    }
    super.setVisible(state);
  }
  
  private void colorFlow(YAWLFlowRelation flow, Color color) {
    graph.stopUndoableEdits();      
    graph.changeCellForeground(flow, color);
    graph.startUndoableEdits();      
  }
  
  private ImageIcon getIconByName(String iconName) {
    return ResourceLoader.getImageAsIcon(
           "/au/edu/qut/yawl/editor/resources/menuicons/" 
           + iconName + "24.gif");
  }
  
  public void updateState() {
    if (flowTable == null) {
      setState(NO_ELEMENTS);
      return;
    }
    
    if (flowTable.getRowCount() > 0) {
      setState(SOME_ELEMENTS);
    } else {
      setState(NO_ELEMENTS);
    }
  }

  public void setState(int state) {
    if (state == NO_ELEMENTS) {
      increasePriorityButton.setEnabled(false);
      decreasePriorityButton.setEnabled(false);
      updatePredicateButton.setEnabled(false);
    }
    if (state == SOME_ELEMENTS) {
      increasePriorityButton.setEnabled(true);
      decreasePriorityButton.setEnabled(true);
      updatePredicateButton.setEnabled(true);
    }
  }
}
