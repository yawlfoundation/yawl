/*
 * Created on 05/12/2003
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
 *
 */

package org.yawlfoundation.yawl.editor.ui.actions.element;

import java.awt.event.ActionEvent;
import javax.swing.Action;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.event.KeyEvent;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.ButtonGroup;

import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;

import org.yawlfoundation.yawl.editor.ui.actions.YAWLBaseAction;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.elements.model.Decorator;

import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;
import org.yawlfoundation.yawl.editor.ui.swing.element.AbstractTaskDoneDialog;

public class DecorateTaskAction extends YAWLBaseAction implements TooltipTogglingWidget {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  protected static final DecorationDialog dialog = new DecorationDialog();
  
  private YAWLTask task;
  private NetGraph graph;

  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Decorate...");
    putValue(Action.LONG_DESCRIPTION, "Decorate this task with a split and/or join.");
    putValue(Action.SMALL_ICON, getIconByName("Decorate"));
    putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_E));
  }

  public DecorateTaskAction(YAWLTask task, NetGraph graph) {
    super();
    this.task = task;
    this.graph = graph;
  }

  public void actionPerformed(ActionEvent event) {
    dialog.setTask(task, graph);
    dialog.setVisible(true);
  }
  
  public String getEnabledTooltipText() {
    return " Decorate this task with a split and/or join ";
  }
  
  public String getDisabledTooltipText() {
    return " You must have a task selected" + 
           " to specify any decorators for it ";
  }
}

class DecorationDialog extends AbstractTaskDoneDialog {

  // TODO: Make this a non-edit generating dialog until the "Done" button is hit.
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private SplitDecoratorPanel splitPanel;
  private JoinDecoratorPanel  joinPanel;

  public DecorationDialog() {
    super(null, true, false);
    setContentPanel(getJoinSplitPanel());
  }
  
  public void setTask(YAWLTask task, NetGraph graph) {
    super.setTask(task,graph);
    splitPanel.setTask(task, graph);
    joinPanel.setTask(task, graph);
    validityChanged(); 
  }
  
  public String getTitlePrefix() {
    return "Decorate ";
  }
    
  private JPanel getJoinSplitPanel() {
    JPanel panel = new JPanel();
    
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setBorder(new EmptyBorder(12,12,0,11));

    panel.add(getJoinPanel());
    panel.add(Box.createRigidArea(new Dimension(10,0)));
    panel.add(getSplitPanel());

    return panel;    
  }
  
  private JPanel getJoinPanel() {
    JPanel panel = new JPanel();

    joinPanel = new JoinDecoratorPanel(this, graph, getTask());

    panel.setBorder(new TitledBorder("Join Decorator"));
    panel.add(joinPanel);

    return panel;    
  }

  private JPanel getSplitPanel() {
    JPanel panel = new JPanel();

    splitPanel = new SplitDecoratorPanel(this, graph, getTask());

    panel.setBorder(new TitledBorder("Split Decorator"));
    panel.add(splitPanel);
    return panel;    
  }
  
  public void validityChanged() {
    if (splitPanel.inValidState() && joinPanel.inValidState()) {
      graph.clearSelection();
      setDefaultCloseOperation(AbstractTaskDoneDialog.HIDE_ON_CLOSE);
      getDoneButton().setEnabled(true);
    } else {
      setDefaultCloseOperation(AbstractTaskDoneDialog.DO_NOTHING_ON_CLOSE);
      getDoneButton().setEnabled(false);
    }     
    joinPanel.setPositionDisabled(getTask().getSplitDecoratorPos());
    splitPanel.setPositionDisabled(getTask().getJoinDecoratorPos());
  }
}

class SplitDecoratorPanel extends DecoratorPanel {

   /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public SplitDecoratorPanel(DecorationDialog dialog, 
                              NetGraph graph, 
                              YAWLTask task) {
     super(dialog, graph, task); 
   }

  protected String getLabelFor() {
    return "Split";
  }
  
  protected void setDecorator(int type, int position) {
    graph.setSplitDecorator(task, type, position);
  }

  protected int hasDecoratorAtPosition() {
    return task.hasSplitDecoratorAt();
  }

  protected int decoratorTypeAtPosition(int position) {
    return task.decoratorTypeAtPosition(position);
  }
  
  protected boolean decoratorIsOptional() {
    return (task.getOutgoingFlowCount() <= 1 && task.hasNoSelfReferencingFlows());
  }

  protected void assignMnemonics() {
    typeNoneButton.setMnemonic(KeyEvent.VK_P);
    typeAndButton.setMnemonic(KeyEvent.VK_D);
    typeOrButton.setMnemonic(KeyEvent.VK_L);
    typeXorButton.setMnemonic(KeyEvent.VK_I);

    positionTopEdgeButton.setMnemonic(KeyEvent.VK_H);
    positionBottomEdgeButton.setMnemonic(KeyEvent.VK_U);
    positionLeftEdgeButton.setMnemonic(KeyEvent.VK_S);
    positionRightEdgeButton.setMnemonic(KeyEvent.VK_T);
  }
}

class JoinDecoratorPanel extends DecoratorPanel {
   /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public JoinDecoratorPanel(DecorationDialog dialog, 
                             NetGraph graph,
                             YAWLTask task) {
     super(dialog, graph, task);
   }
   
  protected String getLabelFor() {
    return "Join";
  }

  protected void setDecorator(int type, int position) {
    graph.setJoinDecorator(task, type, position);
  }
  
  protected int hasDecoratorAtPosition() {
    return task.hasJoinDecoratorAt();
  }

  protected int decoratorTypeAtPosition(int position) {
    return task.decoratorTypeAtPosition(position);
  }

  protected boolean decoratorIsOptional() {
    return (task.getIncomingFlowCount() <= 1 && task.hasNoSelfReferencingFlows());
  }
  
  protected void assignMnemonics() {
    typeNoneButton.setMnemonic(KeyEvent.VK_J);
    typeAndButton.setMnemonic(KeyEvent.VK_A);
    typeOrButton.setMnemonic(KeyEvent.VK_R);
    typeXorButton.setMnemonic(KeyEvent.VK_X);

    positionTopEdgeButton.setMnemonic(KeyEvent.VK_N);
    positionBottomEdgeButton.setMnemonic(KeyEvent.VK_O);
    positionLeftEdgeButton.setMnemonic(KeyEvent.VK_W);
    positionRightEdgeButton.setMnemonic(KeyEvent.VK_E);
  }
}

abstract class DecoratorPanel extends JPanel implements ActionListener {

  protected JRadioButton typeNoneButton;
  protected JRadioButton typeAndButton;
  protected JRadioButton typeOrButton;
  protected JRadioButton typeXorButton;
  
  private ButtonGroup typeButtonGroup = new ButtonGroup();

  protected JRadioButton positionTopEdgeButton;
  protected JRadioButton positionBottomEdgeButton;
  protected JRadioButton positionLeftEdgeButton;
  protected JRadioButton positionRightEdgeButton;
  protected JRadioButton positionNothingButton = new JRadioButton();

  private ButtonGroup positionButtonGroup = new ButtonGroup();

  protected DecorationDialog dialog;
  
  protected YAWLTask task;
  protected NetGraph graph;

  protected int type = Decorator.NO_TYPE;
  protected int position = YAWLTask.NOWHERE;
  
  public DecoratorPanel(DecorationDialog dialog, 
                        NetGraph graph,
                        YAWLTask task) {
    super();
    this.task = task;
    this.graph = graph;
    this.dialog = dialog;    
    buildContent();
    assignMnemonics();
    setPositionGroupEnabled(false);
  }
  
  private void buildContent() {
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    setLayout(gbl);

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(0,12,5,12);
    gbc.anchor = GridBagConstraints.WEST;

    add(getTypeNoneButton(), gbc);
    
    gbc.gridy++;
    add(getTypeAndButton(), gbc);
        
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.insets = new Insets(0,0,5,11);

    add(getTypeOrButton(), gbc);

    gbc.gridy++;

    add(getTypeXorButton(), gbc);

    typeButtonGroup.add(typeNoneButton);
    typeButtonGroup.add(typeAndButton);
    typeButtonGroup.add(typeOrButton);
    typeButtonGroup.add(typeXorButton);

    gbc.gridx = 0;
    gbc.gridwidth = 2;
    gbc.gridy++;
    gbc.insets = new Insets(5,12,5,12);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    add(new JSeparator(),gbc);  

    gbc.gridy++;
    gbc.insets = new Insets(5,0,5,0);
    gbc.fill = GridBagConstraints.NONE;

    gbc.anchor = GridBagConstraints.CENTER;

    add(getPositionTopEdgeButton(), gbc);

    gbc.insets = new Insets(0,0,5,0);
    gbc.anchor = GridBagConstraints.CENTER;

    gbc.gridx = 0;
    gbc.gridwidth = 1;
    gbc.gridy++;

    add(getPositionLeftEdgeButton(), gbc);

    gbc.gridx = 1;
    gbc.anchor = GridBagConstraints.CENTER;

    add(getPositionRightEdgeButton(), gbc);

    gbc.gridx = 0;
    gbc.gridwidth = 2;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.gridy++;

    add(getPositionBottomEdgeButton(), gbc);

    positionButtonGroup.add(positionLeftEdgeButton);
    positionButtonGroup.add(positionRightEdgeButton);
    positionButtonGroup.add(positionTopEdgeButton);
    positionButtonGroup.add(positionBottomEdgeButton);
    positionButtonGroup.add(positionNothingButton);

  }
  
  private JRadioButton getTypeNoneButton() {
    final String label = new String("No " + getLabelFor());
    typeNoneButton = new JRadioButton(label);
    typeNoneButton.setSelected(true);
    typeNoneButton.setMargin(new Insets(0,0,0,0));
    typeNoneButton.setActionCommand(label);
    typeNoneButton.addActionListener(this);
    return typeNoneButton; 
  }

  private JRadioButton getTypeAndButton() {
    typeAndButton = new JRadioButton("And " + getLabelFor());
    typeAndButton.setMargin(new Insets(0,0,0,0));
    typeAndButton.addActionListener(this);
    return typeAndButton; 
  }

  private JRadioButton getTypeOrButton() {
    typeOrButton = new JRadioButton("Or " + getLabelFor());
    typeOrButton.setMargin(new Insets(0,0,0,0));
    typeOrButton.addActionListener(this);
    return typeOrButton; 
  }

  private JRadioButton getTypeXorButton() {
    typeXorButton = new JRadioButton("Xor " + getLabelFor());
    typeXorButton.setMargin(new Insets(0,0,0,0));
    typeXorButton.addActionListener(this);
    return typeXorButton; 
  }

  private JRadioButton getPositionTopEdgeButton() {
    positionTopEdgeButton = new JRadioButton("North");
    positionTopEdgeButton.setMargin(new Insets(0,0,0,0));
    positionTopEdgeButton.addActionListener(this);
    return positionTopEdgeButton; 
  }

  private JRadioButton getPositionBottomEdgeButton() {
    positionBottomEdgeButton = new JRadioButton("South");
    positionBottomEdgeButton.setMargin(new Insets(0,0,0,0));
    positionBottomEdgeButton.addActionListener(this);
    return positionBottomEdgeButton; 
  }

  private JRadioButton getPositionLeftEdgeButton() {
    positionLeftEdgeButton = new JRadioButton("West");
    positionLeftEdgeButton.setMargin(new Insets(0,0,0,0));
    positionLeftEdgeButton.addActionListener(this);
    return positionLeftEdgeButton; 
  }

  private JRadioButton getPositionRightEdgeButton() {
    positionRightEdgeButton = new JRadioButton("East");
    positionRightEdgeButton.setMargin(new Insets(0,0,0,0));
    positionRightEdgeButton.addActionListener(this);
    return positionRightEdgeButton; 
  }
  
  public void actionPerformed(ActionEvent event) {
    if (event.getActionCommand().startsWith("No ")) {
      type     = Decorator.NO_TYPE;
      position = YAWLTask.NOWHERE;
    }
    if (event.getActionCommand().startsWith("And ")) {
      type     = Decorator.AND_TYPE;
    }
    if (event.getActionCommand().startsWith("Or ")) {
      type     = Decorator.OR_TYPE;
    }
    if (event.getActionCommand().startsWith("Xor ")) {
      type     = Decorator.XOR_TYPE;
    }
    if (event.getActionCommand().startsWith("North")) {
      position = YAWLTask.TOP;
    }
    if (event.getActionCommand().startsWith("South")) {
      position = YAWLTask.BOTTOM;
    }
    if (event.getActionCommand().startsWith("West")) {
      position = YAWLTask.LEFT;
    }
    if (event.getActionCommand().startsWith("East")) {
      position = YAWLTask.RIGHT;
    }
    setDecorator(type, position);
    validityChanged();
  }
  
  private void setPositionGroupEnabled(boolean state) {
    if (!state) {
      positionNothingButton.setSelected(true);
    }

    positionTopEdgeButton.setEnabled(state);
    positionBottomEdgeButton.setEnabled(state);
    positionLeftEdgeButton.setEnabled(state);
    positionRightEdgeButton.setEnabled(state);
  }
  
  public boolean inValidState() {
    return (typeNoneButton.isSelected() || 
             (!typeNoneButton.isSelected() &&
               somePositionButtonSelected()
             )
           );
  }
  
  private boolean somePositionButtonSelected() {
    return (!positionNothingButton.isSelected());
  }
  
  public void setPositionDisabled(int position) {
    if (typeNoneButton.isSelected()) {
      return;
    }
    switch (position) {
      case YAWLTask.TOP: {
        positionTopEdgeButton.setEnabled(false);
        positionBottomEdgeButton.setEnabled(true);
        positionLeftEdgeButton.setEnabled(true);
        positionRightEdgeButton.setEnabled(true);
        break;
      }
      case YAWLTask.BOTTOM: {
        positionTopEdgeButton.setEnabled(true);
        positionBottomEdgeButton.setEnabled(false);
        positionLeftEdgeButton.setEnabled(true);
        positionRightEdgeButton.setEnabled(true);
        break;
      }
      case YAWLTask.LEFT: {
        positionTopEdgeButton.setEnabled(true);
        positionBottomEdgeButton.setEnabled(true);
        positionLeftEdgeButton.setEnabled(false);
        positionRightEdgeButton.setEnabled(true);
        break;
      }
      case YAWLTask.RIGHT: {
        positionTopEdgeButton.setEnabled(true);
        positionBottomEdgeButton.setEnabled(true);
        positionLeftEdgeButton.setEnabled(true);
        positionRightEdgeButton.setEnabled(false);
        break;
      }
      default: {
        positionTopEdgeButton.setEnabled(true);
        positionBottomEdgeButton.setEnabled(true);
        positionLeftEdgeButton.setEnabled(true);
        positionRightEdgeButton.setEnabled(true);
        break;
      }
    }
  }

  public YAWLTask getTask() {
    return task;
  }
  
  public void setTask(YAWLTask task, NetGraph graph) {
    this.task = task;
    this.graph = graph;
    refreshPanel();
  }
  
  protected void refreshPanel() {
    
    position = hasDecoratorAtPosition();
    type = decoratorTypeAtPosition(position);

    switch(type) {
      case Decorator.NO_TYPE: {
        typeNoneButton.setSelected(true);
        break;
      }
      case Decorator.AND_TYPE: {
        typeAndButton.setSelected(true);
        break;
      }
      case Decorator.OR_TYPE: {
        typeOrButton.setSelected(true);
        break;
      }
      case Decorator.XOR_TYPE: {
        typeXorButton.setSelected(true);
        break;
      }
    }
    switch(position) {
      case YAWLTask.NOWHERE: {
        positionNothingButton.setSelected(true);
        break;
      }
      case YAWLTask.TOP: {
        positionTopEdgeButton.setSelected(true);
        break;
      }
      case YAWLTask.BOTTOM: {
        positionBottomEdgeButton.setSelected(true);
        break;
      }
      case YAWLTask.LEFT: {
        positionLeftEdgeButton.setSelected(true);
        break;
      }
      case YAWLTask.RIGHT: {
        positionRightEdgeButton.setSelected(true);
        break;
      }
    }
    validityChanged();
    typeNoneButton.setEnabled(decoratorIsOptional());
  }

  protected void validityChanged() {
    if (type == Decorator.NO_TYPE) {
      setPositionGroupEnabled(false);
    } else {
      setPositionGroupEnabled(true);
    }
    dialog.validityChanged();
  }
  

  abstract protected String getLabelFor();
  
  abstract protected void setDecorator(int type, int position);
  
  abstract protected int decoratorTypeAtPosition(int position);

  abstract protected boolean decoratorIsOptional();

  abstract protected int hasDecoratorAtPosition();

  abstract protected void assignMnemonics();
}


