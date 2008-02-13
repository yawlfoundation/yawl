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

import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.actions.net.YAWLSelectedNetAction;
import org.yawlfoundation.yawl.editor.elements.model.AtomicTask;
import org.yawlfoundation.yawl.editor.elements.model.TaskTimeoutDetail;
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;

import org.yawlfoundation.yawl.editor.data.DataVariable;
import org.yawlfoundation.yawl.editor.data.DataVariableSet;
import org.yawlfoundation.yawl.editor.data.Decomposition;
import org.yawlfoundation.yawl.editor.data.WebServiceDecomposition;
import org.yawlfoundation.yawl.editor.swing.JUtilities;
import org.yawlfoundation.yawl.editor.swing.TooltipTogglingWidget;
import org.yawlfoundation.yawl.editor.swing.data.DurationDataVariableComboBox;
import org.yawlfoundation.yawl.editor.swing.data.JXMLSchemaDurationEditorPane;
import org.yawlfoundation.yawl.editor.swing.data.TaskDecompositionUpdateDialog;
import org.yawlfoundation.yawl.editor.swing.data.NetDecompositionUpdateDialog;
import org.yawlfoundation.yawl.editor.swing.element.AbstractTaskDoneDialog;
import org.yawlfoundation.yawl.editor.swing.resourcing.ManageResourcingDialog;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

public class TaskTimeoutDetailAction extends YAWLSelectedNetAction 
                                           implements TooltipTogglingWidget {

  private static final long serialVersionUID = 1L;

  private TaskTimeoutDialog dialog = new TaskTimeoutDialog();
  
  private NetGraph graph;
  private YAWLTask task;
  
  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Task Timeout Detail...");
    putValue(Action.LONG_DESCRIPTION, "Manage the timeout behaviour of this task. ");
    putValue(Action.SMALL_ICON, getIconByName("TaskTimeout"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_T));
  }
  
  public TaskTimeoutDetailAction(YAWLTask task, NetGraph graph) {
    super();
    this.task = task;
    this.graph = graph;
  }  
  
  public String getEnabledTooltipText() {
    return " Manage the timeout behaviour of this task ";
  }
  
  public String getDisabledTooltipText() {
    return " Only atomic tasks may have timeout information set for them ";
  }

  public void actionPerformed(ActionEvent event) {
    dialog.setTask(task, graph);
    dialog.setVisible(true);

    graph.clearSelection();
  }
  
  public boolean shouldBeEnabled() {
    return (task instanceof AtomicTask);
  }
}


class TaskTimeoutDialog extends AbstractTaskDoneDialog {

  private static final long serialVersionUID = 1L;
  
  private JCheckBox timeoutNeededCheckBox;
  
  private JRadioButton onEnablementRadioButton;
  private JRadioButton onStartingRadioButton;

  private JRadioButton viaNetVariableRadioButton;
  private JRadioButton viaStaticDurationRadioButton;
  
  private DurationDataVariableComboBox durationVariableComboBox;
  private JXMLSchemaDurationEditorPane durationValueEditor;
  
  public TaskTimeoutDialog() {
    super(null, true, true);
    setContentPanel(buildPanel());
    
    getDoneButton().addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        
        if (!timeoutNeededCheckBox.isSelected()) {
          if (getAtomicTask().getTimeoutDetail() != null) {
            getAtomicTask().setTimeoutDetail(null);
          }
          return;
        }
        
        // From this point on, we need timeout detail, so we build
        // a new encoding of it from the current state of the widgets.
        
        TaskTimeoutDetail detail = new TaskTimeoutDetail();
        
        if (onEnablementRadioButton.isSelected()) {
          detail.setTrigger(TaskTimeoutDetail.TRIGGER_ON_ENABLEMENT);
        }
        if (onStartingRadioButton.isSelected()) {
          detail.setTrigger(TaskTimeoutDetail.TRIGGER_ON_STARTING);
        }
        
        if (viaNetVariableRadioButton.isSelected()) {
          detail.setTimeoutVariable(
            durationVariableComboBox.getSelectedVariable()    
          );
        }
        if (viaStaticDurationRadioButton.isSelected()) {
          detail.setTimeoutValue(
            durationValueEditor.getText()    
          );
        }
        
        getAtomicTask().setTimeoutDetail(
            detail
        );
      }
    });
  }

  protected void makeLastAdjustments() {
    setSize(500,300);
//    pack();
    JUtilities.setMinSizeToCurrent(this); 
    setResizable(true);
  } 
  
  
  public AtomicTask getAtomicTask() {
    return (AtomicTask) getTask();
  }
  
  public String getTitlePrefix() {
    return "Set Timeout Detail for ";
  }

  public void setTask(YAWLTask task, NetGraph graph) {
    super.setTask(task, graph);
    
    durationVariableComboBox.setDecomposition(
      getGraph().getNetModel().getDecomposition()    
    );
    
    if (getAtomicTask().getTimeoutDetail() == null) {
      resetToDefault();
    } else {
      // Populate widget settings from TaskTimeoutDetail already available.

      timeoutNeededCheckBox.setSelected(true);
      if (getAtomicTask().getTimeoutDetail().getTrigger() == TaskTimeoutDetail.TRIGGER_ON_ENABLEMENT) {
        onEnablementRadioButton.setSelected(true);
      }
      if (getAtomicTask().getTimeoutDetail().getTrigger() == TaskTimeoutDetail.TRIGGER_ON_STARTING) {
        onStartingRadioButton.setSelected(true);
      }
      if (getAtomicTask().getTimeoutDetail().getTimeoutValue() != null) {
        this.viaStaticDurationRadioButton.setSelected(true);
        this.durationValueEditor.setText(
            getAtomicTask().getTimeoutDetail().getTimeoutValue()
        );
      }
      if (getAtomicTask().getTimeoutDetail().getTimeoutVariable() != null) {
        this.viaNetVariableRadioButton.setSelected(true);
        this.durationVariableComboBox.setSelectedItem(
            getAtomicTask().getTimeoutDetail().getTimeoutVariable()
        );
      }
    }
    
    enableWidgetsAsRequired();
  }
  
  private void resetToDefault() {
    timeoutNeededCheckBox.setSelected(false);
    onEnablementRadioButton.setSelected(true);
    viaStaticDurationRadioButton.setSelected(true);
  }
  
  private JPanel buildPanel() {
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    
    JPanel panel = new JPanel(gbl);

    panel.setBorder(new EmptyBorder(12,12,0,11));

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1;
    gbc.gridwidth = 3;
    gbc.insets = new Insets(0,0,5,0);
    gbc.anchor = GridBagConstraints.CENTER;
    
    panel.add(buildTimeoutNeededCheckBox(), gbc);
    
    
    gbc.gridx++;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1;
    panel.add(new JLabel(), gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.weightx = 0;
    gbc.gridwidth = 3;
    
    gbc.insets = new Insets(5,12,5,12);

    panel.add(new JSeparator(),gbc);  
    
    gbc.gridy++;
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.EAST;
    gbc.insets = new Insets(5,10,0,0);
    
    panel.add(new JLabel("Timer begins: "), gbc);

    gbc.insets = new Insets(5,2,0,0);
    gbc.anchor = GridBagConstraints.WEST;
    gbc.gridx++;

    panel.add(buildOnEnablementRadioButton(), gbc);

    gbc.insets = new Insets(0,2,10,0);
    gbc.gridy++;

    panel.add(buildOnStartingRadioButton(), gbc);
    
    ButtonGroup timerStartsGroup = new ButtonGroup();
    timerStartsGroup.add(onEnablementRadioButton);
    timerStartsGroup.add(onStartingRadioButton);

    onEnablementRadioButton.setSelected(true);
    
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.anchor = GridBagConstraints.EAST;
    gbc.insets = new Insets(5,0,0,0);   
    
    panel.add(new JLabel("Timeout: "), gbc);

    gbc.insets = new Insets(5,2,0,0);
    gbc.anchor = GridBagConstraints.WEST;
    gbc.gridx++;

    panel.add(buildViaNetVariableRadioButton(), gbc);
    
    gbc.gridx++;
    panel.add(buildDurationDataVariableComboBox(), gbc);

    gbc.insets = new Insets(2,2,0,0);
    gbc.gridy++;
    gbc.gridx--;
    gbc.anchor = GridBagConstraints.NORTHWEST;

    panel.add(buildViaStaticDurationRadioButton(), gbc);
    
    ButtonGroup timesViaGroup = new ButtonGroup();
    timesViaGroup.add(viaNetVariableRadioButton);
    timesViaGroup.add(viaStaticDurationRadioButton);
    
    viaStaticDurationRadioButton.setSelected(true);

    gbc.gridx++;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weighty = 1;

    panel.add(buildDurationValueEditor(), gbc);

    return panel;
  }
  
  private JCheckBox buildTimeoutNeededCheckBox() {
    timeoutNeededCheckBox = new JCheckBox();
    
    timeoutNeededCheckBox.setText("Task is required to timeout");
    timeoutNeededCheckBox.setMnemonic(KeyEvent.VK_R);
    
    timeoutNeededCheckBox.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          enableWidgetsAsRequired();
        }
      }
    );
    
    return timeoutNeededCheckBox;
  }
  
  private JRadioButton buildOnEnablementRadioButton() {
    onEnablementRadioButton = new JRadioButton();
    
    onEnablementRadioButton.setText("upon work item enablement");
    onEnablementRadioButton.setMnemonic(KeyEvent.VK_E);
    onEnablementRadioButton.setDisplayedMnemonicIndex(15);
    
    return onEnablementRadioButton;
  }
  
  private JRadioButton buildOnStartingRadioButton() {
    onStartingRadioButton = new JRadioButton();

    onStartingRadioButton.setText("upon work item starting");
    onStartingRadioButton.setMnemonic(KeyEvent.VK_S);
    
    return onStartingRadioButton;
  }
  
  private JRadioButton buildViaNetVariableRadioButton() {
    viaNetVariableRadioButton = new JRadioButton();
    
    viaNetVariableRadioButton.setText("dynamically via net variable");
    viaNetVariableRadioButton.setMnemonic(KeyEvent.VK_V);
    
    viaNetVariableRadioButton.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          durationVariableComboBox.setEnabled(
              viaNetVariableRadioButton.isSelected()
          );
          durationValueEditor.setEnabled(
              viaStaticDurationRadioButton.isSelected()
          );
        }
      }
    );
    
    return viaNetVariableRadioButton;
  }

  private JRadioButton buildViaStaticDurationRadioButton() {
    viaStaticDurationRadioButton = new JRadioButton();
    
    viaStaticDurationRadioButton.setText("statically via duration of");
    viaStaticDurationRadioButton.setMnemonic(KeyEvent.VK_U);
    viaStaticDurationRadioButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            durationVariableComboBox.setEnabled(
                viaNetVariableRadioButton.isSelected()
            );
            durationValueEditor.setEnabled(
                viaStaticDurationRadioButton.isSelected()
            );
            if (viaStaticDurationRadioButton.isSelected()) {
              durationValueEditor.requestFocus();
            }
          }
        }
      );
    
    return viaStaticDurationRadioButton;
  }
  
  private DurationDataVariableComboBox buildDurationDataVariableComboBox() {
    durationVariableComboBox = new DurationDataVariableComboBox();
    
    durationVariableComboBox.setValidUsageType(
        DataVariableSet.VALID_USAGE_INPUT_FROM_NET    
    );
    
    return durationVariableComboBox;
  }
  
  private JXMLSchemaDurationEditorPane buildDurationValueEditor() {
    durationValueEditor = new JXMLSchemaDurationEditorPane();
    
    durationValueEditor.hideProblemTable();
    
    return durationValueEditor;
  }
  
  private void enableWidgetsAsRequired() {
    boolean timeoutNeeded = timeoutNeededCheckBox.isSelected();
    
    onEnablementRadioButton.setEnabled(timeoutNeeded);
    onStartingRadioButton.setEnabled(timeoutNeeded);
      
    viaNetVariableRadioButton.setEnabled(timeoutNeeded);

    durationVariableComboBox.setEnabled(
        viaNetVariableRadioButton.isEnabled() &&
        viaNetVariableRadioButton.isSelected()
    );

    viaStaticDurationRadioButton.setEnabled(timeoutNeeded);
    
    durationValueEditor.setEnabled(
        viaStaticDurationRadioButton.isEnabled() &&
        viaStaticDurationRadioButton.isSelected()
    );
  }
}