/*
 * Created on 20/09/2004
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

package org.yawlfoundation.yawl.editor.swing.element;

import org.yawlfoundation.yawl.editor.elements.model.YAWLMultipleInstanceTask;
import org.yawlfoundation.yawl.editor.swing.JFormattedNumberField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class MultipleInstanceBoundsPanel extends JPanel {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private YAWLMultipleInstanceTask task;
  
  private JRadioButton maxInfinityButton;
  private JRadioButton maxBoundButton;

  private ButtonGroup maxButtonGroup = new ButtonGroup();

  protected JRadioButton thresholdInfinityButton;
  private JRadioButton thresholdBoundButton;

  private ButtonGroup thresholdButtonGroup = new ButtonGroup();

  private JRadioButton staticCreationButton;
  private JRadioButton dynamicCreationButton;

  private ButtonGroup creationButtonGroup = new ButtonGroup();
  
  protected JFormattedNumberField minInstancesField;
  protected JFormattedNumberField maxInstancesField;
  protected JFormattedNumberField thresholdField;
  
  private static final Insets BUTTON_INSETS = new Insets(0,0,0,0);

  public MultipleInstanceBoundsPanel() {
    super();
    buildContent();
  }
  
  private void buildContent() {
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    setLayout(gbl);
    setBorder(new EmptyBorder(12,12,0,11));

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(0,0,5,5);
    gbc.anchor = GridBagConstraints.EAST;

    JLabel minimumLabel = new JLabel("Minimum Instances:");
    minimumLabel.setDisplayedMnemonic('M');
    add(minimumLabel, gbc);
    
    gbc.gridy++;
    gbc.insets = new Insets(0,5,2,0);

    JLabel maximumLabel = new JLabel("Maximum Instances:");
    add(maximumLabel, gbc);

    gbc.gridy += 2;

    JLabel thresholdLabel = new JLabel("Continuation Threshold:");
    add(thresholdLabel, gbc);

    gbc.gridy += 2;

    JLabel instanceCreationLabel = new JLabel("Instance Creation:");
    add(instanceCreationLabel, gbc);
    
    gbc.gridy = 0;
    gbc.gridx = 1;
    gbc.insets = new Insets(0,5,5,0);
    gbc.anchor = GridBagConstraints.WEST;

    add(getMinInstancesField(),gbc);
    minimumLabel.setLabelFor(minInstancesField);

    gbc.gridy++;
    gbc.insets = new Insets(0,5,2,0);
    add(getMaxInfinityButton(),gbc);

    gbc.gridy++;
    gbc.insets = new Insets(0,5,5,0);
    add(getMaxBoundButton(),gbc);

    maxButtonGroup.add(maxInfinityButton);
    maxButtonGroup.add(maxBoundButton);
    
    gbc.gridy++;
    gbc.insets = new Insets(0,5,2,0);
    add(getThresholdInfinityButton(),gbc);

    gbc.gridy++;
    gbc.insets = new Insets(0,5,5,0);
    add(getThresholdBoundButton(),gbc);

    thresholdButtonGroup.add(thresholdInfinityButton);
    thresholdButtonGroup.add(thresholdBoundButton);

    gbc.gridy++;
    gbc.insets = new Insets(0,5,2,0);
    add(getStaticCreationButton(),gbc);

    gbc.gridy++;
    gbc.insets = new Insets(0,5,10,0);
    add(getDynamicCreationButton(),gbc);

    creationButtonGroup.add(staticCreationButton);
    creationButtonGroup.add(dynamicCreationButton);

    gbc.gridx = 2;
    gbc.gridy = 2;
    gbc.insets = new Insets(0,5,5,0);

    add(getMaxInstancesField(),gbc);

    gbc.gridy += 2;

    add(getThresholdField(),gbc);
  }
  
  private void addFieldListener(JFormattedNumberField field) {
    ActionFocusFieldListener fieldListener = new ActionFocusFieldListener();
    
    field.addActionListener(fieldListener);
    field.addFocusListener(fieldListener);
  }

  private JFormattedNumberField getMinInstancesField() {
    minInstancesField = new JFormattedNumberField("###,###,##0",1,12);
    minInstancesField.setLowerBound(1);
    
    addFieldListener(minInstancesField);
 
    return minInstancesField;
  }
  
  private JFormattedNumberField getMaxInstancesField() {
    maxInstancesField = new JFormattedNumberField("###,###,##0",1,12);
    maxInstancesField.setLowerBound(1);
    addFieldListener(maxInstancesField);
    return maxInstancesField;
  }

  private JFormattedNumberField getThresholdField() {
    thresholdField = new JFormattedNumberField("###,###,##0",1,12);
    thresholdField.setLowerBound(1);
    addFieldListener(thresholdField);
    return thresholdField;
  }
  
  private JRadioButton getMaxInfinityButton() {
    maxInfinityButton = new JRadioButton("is infinite");
    maxInfinityButton.setMargin(BUTTON_INSETS);
    maxInfinityButton.setMnemonic(KeyEvent.VK_I);
    maxInfinityButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        maxInstancesField.setEnabled(false); 
        task.setMaximumInstances(YAWLMultipleInstanceTask.INFINITY);           
      }
    });
    return maxInfinityButton; 
  }

  private JRadioButton getMaxBoundButton() {
    maxBoundButton = new JRadioButton("is equal to");
    maxBoundButton.setMargin(BUTTON_INSETS);
    maxBoundButton.setMnemonic(KeyEvent.VK_E);
    maxBoundButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        maxInstancesField.setEnabled(true);  
        maxInstancesField.requestFocus();            
        task.setMaximumInstances((long) maxInstancesField.getDouble());           
      }
    });
    maxBoundButton.setSelected(true);
    return maxBoundButton; 
  }

  private JRadioButton getThresholdInfinityButton() {
    thresholdInfinityButton = new JRadioButton("is infinite");
    thresholdInfinityButton.setMargin(BUTTON_INSETS);
    thresholdInfinityButton.setMnemonic(KeyEvent.VK_N);
    thresholdInfinityButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        thresholdField.setEnabled(false);              
        task.setContinuationThreshold(YAWLMultipleInstanceTask.INFINITY);           
      }
    });
    return thresholdInfinityButton; 
  }

  private JRadioButton getThresholdBoundButton() {
    thresholdBoundButton = new JRadioButton("is equal to");
    thresholdBoundButton.setMnemonic(KeyEvent.VK_T);
    thresholdBoundButton.setMargin(BUTTON_INSETS);
    thresholdBoundButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        thresholdField.setEnabled(true);              
        thresholdField.requestFocus(); 
        task.setContinuationThreshold((long) thresholdField.getDouble());           
      }
    });
    thresholdBoundButton.setSelected(true);
    return thresholdBoundButton; 
  }

  private JRadioButton getStaticCreationButton() {
    staticCreationButton = new JRadioButton("Static");
    staticCreationButton.setMargin(BUTTON_INSETS);
    staticCreationButton.setMnemonic(KeyEvent.VK_S);
    staticCreationButton.setSelected(true);
    staticCreationButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        task.setInstanceCreationType(YAWLMultipleInstanceTask.STATIC_INSTANCE_CREATION);           
      }
    });
    return staticCreationButton; 
  }

  private JRadioButton getDynamicCreationButton() {
    dynamicCreationButton = new JRadioButton("Dynamic");
    dynamicCreationButton.setMnemonic(KeyEvent.VK_Y);
    dynamicCreationButton.setMargin(BUTTON_INSETS);
    dynamicCreationButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        task.setInstanceCreationType(YAWLMultipleInstanceTask.DYNAMIC_INSTANCE_CREATION);           
      }
    });
    return dynamicCreationButton; 
  }
  
  public void setTask(YAWLMultipleInstanceTask task) {
    this.task  = task;
    
    minInstancesField.setDouble(task.getMinimumInstances());
    maxInstancesField.setLowerBound(task.getMinimumInstances());
    thresholdField.setLowerBound(task.getMinimumInstances());

    if (task.getMaximumInstances() == YAWLMultipleInstanceTask.INFINITY) {
      maxInstancesField.setText("");
      if (!maxInfinityButton.isSelected()) {
        maxInfinityButton.doClick();

      }
    } else {
      maxInstancesField.setDouble(task.getMaximumInstances());
      if (!maxBoundButton.isSelected()) {
        maxBoundButton.doClick();
      }
      thresholdField.setUpperBound(task.getMaximumInstances());
    } 

    if (task.getContinuationThreshold() == YAWLMultipleInstanceTask.INFINITY) {
      thresholdField.setText("");
      if (!thresholdInfinityButton.isSelected()) {
        thresholdInfinityButton.doClick();
      }
    } else {
      thresholdField.setDouble(task.getContinuationThreshold());
      if (!thresholdBoundButton.isSelected()) {
        thresholdBoundButton.doClick();
      }
    }
    
    if (task.getInstanceCreationType() == YAWLMultipleInstanceTask.STATIC_INSTANCE_CREATION) {
      if (!staticCreationButton.isSelected()) {
        staticCreationButton.doClick();
      }
    } else {
      if (!dynamicCreationButton.isSelected()) {
        dynamicCreationButton.doClick();
      }
    }
  }
  
  class ActionFocusFieldListener implements ActionListener, FocusListener {

    public void actionPerformed(ActionEvent event) {
      processField((JFormattedNumberField) event.getSource());
    }
    
    public void focusGained(FocusEvent event) {
      if (fieldHasNoContent((JFormattedNumberField) event.getSource())) {
//        getDoneButton().setEnabled(false);
      }
    }
    
    private boolean fieldHasNoContent(JFormattedNumberField field) {
      return field.getText().equals("");
    }

    public void focusLost(FocusEvent event) {
      processField((JFormattedNumberField) event.getSource());
    }

    private void processField(JFormattedNumberField field) {
      if (!shouldYieldFocus(field)) {
        return;
      }
      
      if (field == minInstancesField) {
        maxInstancesField.setLowerBound(minInstancesField.getDouble());
        thresholdField.setLowerBound(minInstancesField.getDouble());
      }
      if (field == maxInstancesField) {
        thresholdField.setUpperBound(maxInstancesField.getDouble());
      }

      task.setMinimumInstances((long) minInstancesField.getDouble());

        /*
        * SPR: Do *not* set task values using getDouble() when they are infinite
        * (which is synonymous with them being disabled). We do not have to set them
        * at all in this case, since this is already done as soon as an infinite
        * radio button is clicked
        */
       if (maxInstancesField.isEnabled()) {
           task.setMaximumInstances((long) maxInstancesField.getDouble());
       }
       if (thresholdField.isEnabled()) {
           task.setContinuationThreshold((long) thresholdField.getDouble());
       }
    }
    
    private boolean shouldYieldFocus(JFormattedNumberField field) {
      InputVerifier verifier = field.getInputVerifier();

      assert verifier != null : "No InputVerifier tied to this FormattedTextField";

      return verifier.shouldYieldFocus(field);
    }
  }

}
