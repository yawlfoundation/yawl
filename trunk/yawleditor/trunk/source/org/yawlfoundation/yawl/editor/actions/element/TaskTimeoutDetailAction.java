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

import com.toedter.calendar.JDateChooser;
import org.yawlfoundation.yawl.editor.actions.net.YAWLSelectedNetAction;
import org.yawlfoundation.yawl.editor.data.DataVariableSet;
import org.yawlfoundation.yawl.editor.data.Decomposition;
import org.yawlfoundation.yawl.editor.elements.model.AtomicTask;
import org.yawlfoundation.yawl.editor.elements.model.TaskTimeoutDetail;
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.swing.JUtilities;
import org.yawlfoundation.yawl.editor.swing.TooltipTogglingWidget;
import org.yawlfoundation.yawl.editor.swing.data.JXMLSchemaDurationEditorPane;
import org.yawlfoundation.yawl.editor.swing.data.TimerDataVariableComboBox;
import org.yawlfoundation.yawl.editor.swing.element.AbstractTaskDoneDialog;
import org.yawlfoundation.yawl.editor.swing.resourcing.JTimeSpinner;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TaskTimeoutDetailAction extends YAWLSelectedNetAction 
                                           implements TooltipTogglingWidget {

  private static final long serialVersionUID = 1L;

  private TaskTimeoutDialog dialog = new TaskTimeoutDialog();
  
  private NetGraph graph;
  private YAWLTask task;
  
  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Set Task Timer...");
    putValue(Action.LONG_DESCRIPTION, "Manage the timer behaviour of this task. ");
    putValue(Action.SMALL_ICON, getPNGIcon("hourglass"));
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
    return (task instanceof AtomicTask) && (task.getDecomposition() != null);
  }
}


class TaskTimeoutDialog extends AbstractTaskDoneDialog {

  private static final long serialVersionUID = 1L;
  
  private JCheckBox timeoutNeededCheckBox;
  
  private TimerDataVariableComboBox timerVariableComboBox;

  private JRadioButton viaNetVariableRadioButton;
  
  private JRadioButton viaStaticDateRadioButton;
  private JPanel dateValueField;

  private JRadioButton viaStaticDurationRadioButton;
  private JXMLSchemaDurationEditorPane durationValueEditor;

  private JRadioButton onEnablementRadioButton;
  private JRadioButton onStartingRadioButton;

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
        
        
        if (viaNetVariableRadioButton.isSelected()) {
          detail.setTimeoutVariable(
            timerVariableComboBox.getSelectedVariable()
          );
        }
        if (viaStaticDurationRadioButton.isSelected()) {
          detail.setTimeoutValue(
            durationValueEditor.getText()    
          );
        }
        if (viaStaticDateRadioButton.isSelected()) {
         detail.setTimeoutDate(
            getStaticDate(dateValueField)
          );
        }

        if (viaStaticDurationRadioButton.isSelected() || 
            viaStaticDateRadioButton.isSelected()) {
          if (onEnablementRadioButton.isSelected()) {
            detail.setTrigger(TaskTimeoutDetail.TRIGGER_ON_ENABLEMENT);
          }
          if (onStartingRadioButton.isSelected()) {
            detail.setTrigger(TaskTimeoutDetail.TRIGGER_ON_STARTING);
          }
        }
        
        getAtomicTask().setTimeoutDetail(
            detail
        );

        SpecificationUndoManager.getInstance().setDirty(true);
      }
    });
  }

  protected void makeLastAdjustments() {
    setSize(550,400);
//    pack();
    JUtilities.setMinSizeToCurrent(this); 
    setResizable(true);
  } 
  
  
  public AtomicTask getAtomicTask() {
    return (AtomicTask) getTask();
  }
  
  public String getTitlePrefix() {
    return "Set Timer Detail for ";
  }

  public void setTask(YAWLTask task, NetGraph graph) {
    super.setTask(task, graph);
    
    timerVariableComboBox.setDecomposition(
      getGraph().getNetModel().getDecomposition()    
    );
    
    if (getAtomicTask().getTimeoutDetail() == null) {
      resetToDefault();
    } else {
      // Populate widget settings from TaskTimeoutDetail already available.

      timeoutNeededCheckBox.setSelected(true);

      if (getAtomicTask().getTimeoutDetail().getTimeoutVariable() != null) {
        this.viaNetVariableRadioButton.setSelected(true);
        this.timerVariableComboBox.setSelectedItem(
            getAtomicTask().getTimeoutDetail().getTimeoutVariable()
        );
      }
      else {

          if (getAtomicTask().getTimeoutDetail().getTimeoutValue() != null) {
              this.viaStaticDurationRadioButton.setSelected(true);
              this.durationValueEditor.setText(
                      getAtomicTask().getTimeoutDetail().getTimeoutValue()
              );
          }

          if (getAtomicTask().getTimeoutDetail().getTimeoutDate() != null) {
              this.viaStaticDateRadioButton.setSelected(true);
              setStaticDate(dateValueField, getAtomicTask().getTimeoutDetail().getTimeoutDate());
          }

          if (getAtomicTask().getTimeoutDetail().getTrigger() == TaskTimeoutDetail.TRIGGER_ON_ENABLEMENT) {
              onEnablementRadioButton.setSelected(true);
          }
          if (getAtomicTask().getTimeoutDetail().getTrigger() == TaskTimeoutDetail.TRIGGER_ON_STARTING) {
              onStartingRadioButton.setSelected(true);
          }
      }
    }
    
    enableWidgetsAsRequired();
  }
  
  private void resetToDefault() {
    timeoutNeededCheckBox.setSelected(false);
    onEnablementRadioButton.setSelected(true);
    viaStaticDateRadioButton.setSelected(true);
    viaStaticDurationRadioButton.setSelected(false);
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

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridwidth = 1;
    gbc.anchor = GridBagConstraints.EAST;
    gbc.insets = new Insets(5,2,20,0);   
    
    panel.add(new JLabel("Timeout: "), gbc);

    gbc.insets = new Insets(5,2,20,0);
    gbc.anchor = GridBagConstraints.WEST;
    gbc.gridx++;

    panel.add(buildViaNetVariableRadioButton(), gbc);
    
    gbc.gridx++;
    panel.add(buildDurationDataVariableComboBox(), gbc);

    gbc.gridy++;
    gbc.gridx--;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(0,2,2,0);
    
    panel.add(buildViaStaticDateRadioButton(), gbc);

    gbc.gridx++;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    panel.add(buildDateTimeValueField(), gbc);

    gbc.gridx--;
    gbc.gridy++;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    
    panel.add(buildViaStaticDurationRadioButton(), gbc);
    
    gbc.gridx++;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weighty = 1;

    panel.add(buildDurationValueEditor(), gbc);
    
    ButtonGroup timesViaGroup = new ButtonGroup();
    timesViaGroup.add(viaNetVariableRadioButton);
    timesViaGroup.add(viaStaticDurationRadioButton);
    timesViaGroup.add(viaStaticDateRadioButton);
    
    viaStaticDurationRadioButton.setSelected(true);

    gbc.gridx = 1;
    gbc.gridy++;
    gbc.gridwidth = 1;
    gbc.weighty = 0;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.EAST;
    gbc.insets = new Insets(5,10,0,0);
    
    panel.add(new JLabel("Timer begins: "), gbc);

    gbc.insets = new Insets(5,2,0,0);
    gbc.anchor = GridBagConstraints.WEST;
    gbc.gridx++;

    panel.add(buildOnEnablementRadioButton(), gbc);

    gbc.insets = new Insets(0,2,0,0);
    gbc.gridy++;

    panel.add(buildOnStartingRadioButton(), gbc);
    
    ButtonGroup timerStartsGroup = new ButtonGroup();
    timerStartsGroup.add(onEnablementRadioButton);
    timerStartsGroup.add(onStartingRadioButton);

    onEnablementRadioButton.setSelected(true);
    
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
          enableWidgetsAsRequired();
        }
      }
    );
    
    return viaNetVariableRadioButton;
  }

  private JRadioButton buildViaStaticDurationRadioButton() {
    viaStaticDurationRadioButton = new JRadioButton();
    
    viaStaticDurationRadioButton.setText("after a duration of");
    viaStaticDurationRadioButton.setMnemonic(KeyEvent.VK_U);
    viaStaticDurationRadioButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            enableWidgetsAsRequired();
          }
        }
      );
    
    return viaStaticDurationRadioButton;
  }

  private JRadioButton buildViaStaticDateRadioButton() {
    viaStaticDateRadioButton = new JRadioButton();
    
    viaStaticDateRadioButton.setText("at the time of ");
    viaStaticDateRadioButton.setMnemonic(KeyEvent.VK_T);
    viaStaticDateRadioButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            enableWidgetsAsRequired();
          }
        }
      );
    
    return viaStaticDateRadioButton;
  }


  private JPanel buildDateTimeValueField() {
      dateValueField = new JPanel();
      JDateChooser chooser = new JDateChooser();
      chooser.setMinSelectableDate(Calendar.getInstance().getTime());
      chooser.setDate(Calendar.getInstance().getTime());

      // workaround for too narrow an edit field
      chooser.setDateFormatString("dd/MM/yyyy ");
      
      dateValueField.add(chooser) ;
      dateValueField.add(new JTimeSpinner()) ;
      return dateValueField ;
    }

    private void setStaticDate(JPanel dateField, Date date) {
        Component[] widgets = dateField.getComponents();

        for (int i=0; i<widgets.length; i++) {
            Component widget = widgets[i] ;
            if (widget instanceof JDateChooser) {
                JDateChooser chooser = (JDateChooser) widget ;
                chooser.setDate(date);
            }
            else if (widget instanceof JTimeSpinner) {
                JTimeSpinner spinner = (JTimeSpinner) widget;
                spinner.setTime(date);
            }
        }
    }


    private Date getStaticDate(JPanel dateField) {
        Date date = null;
        int time = 0;
        Component[] widgets = dateField.getComponents();

        for (int i=0; i<widgets.length; i++) {
            Component widget = widgets[i] ;
            if (widget instanceof JDateChooser) {
                JDateChooser chooser = (JDateChooser) widget ;
                date = chooser.getDate();
            }
            else if (widget instanceof JTimeSpinner) {
                JTimeSpinner spinner = (JTimeSpinner) widget;
                time = spinner.getTimeAsSeconds();
            }
        }
        GregorianCalendar cal = new GregorianCalendar();
        if (date != null) {
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.add(Calendar.SECOND, time) ;
            return cal.getTime();
        }
        else return null ;
    }

  private TimerDataVariableComboBox buildDurationDataVariableComboBox() {
    timerVariableComboBox = new TimerDataVariableComboBox();

    timerVariableComboBox.setValidUsageType(
        DataVariableSet.VALID_USAGE_INPUT_FROM_NET    
    );

    
    return timerVariableComboBox;
  }
  
  private JXMLSchemaDurationEditorPane buildDurationValueEditor() {
    durationValueEditor = new JXMLSchemaDurationEditorPane();
    durationValueEditor.getBottomComponent().setVisible(false);    // hide initially
    durationValueEditor.setToolTipText(" Specify a duration in the form of an XMLSchema duration basic type ");
    return durationValueEditor;
  }
  
  private void enableWidgetsAsRequired() {
    boolean timeoutNeeded = timeoutNeededCheckBox.isSelected();

    viaNetVariableRadioButton.setEnabled(timeoutNeeded &&
                                        (timerVariableComboBox.getItemCount() > 0));

    if (viaNetVariableRadioButton.isSelected() && (! viaNetVariableRadioButton.isEnabled())) {
        viaNetVariableRadioButton.setSelected(false);    
    }

    timerVariableComboBox.setEnabled(
        viaNetVariableRadioButton.isEnabled() &&
        viaNetVariableRadioButton.isSelected()
    );

    onEnablementRadioButton.setEnabled(timeoutNeeded && (! viaNetVariableRadioButton.isSelected()));

    Decomposition decomp = getTask().getDecomposition();
    boolean autotask = (decomp != null) && (! decomp.isManualInteraction());
    onStartingRadioButton.setEnabled(timeoutNeeded &&
            (! viaNetVariableRadioButton.isSelected()) &&
            (! autotask));

    viaStaticDateRadioButton.setEnabled(timeoutNeeded);
    
    setDateValueFieldEnabled(
        viaStaticDateRadioButton.isEnabled() &&
        viaStaticDateRadioButton.isSelected()
    );

    viaStaticDurationRadioButton.setEnabled(timeoutNeeded);
    
    durationValueEditor.setEnabled(
        viaStaticDurationRadioButton.isEnabled() &&
        viaStaticDurationRadioButton.isSelected()
    );
  }

  private void setDateValueFieldEnabled(boolean enable) {

      Component[] widgets = dateValueField.getComponents();

      for (int i=0; i<widgets.length; i++) {
          Component widget = widgets[i] ;
          if (widget instanceof JDateChooser) {
              JDateChooser chooser = (JDateChooser) widget ;
              chooser.setEnabled(enable);
          }
          else if (widget instanceof JTimeSpinner) {
              JTimeSpinner spinner = (JTimeSpinner) widget;
              spinner.setEnabled(enable);
          }
      }
  }
}