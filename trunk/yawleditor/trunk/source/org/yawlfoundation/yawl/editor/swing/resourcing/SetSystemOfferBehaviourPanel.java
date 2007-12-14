package org.yawlfoundation.yawl.editor.swing.resourcing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;

import org.yawlfoundation.yawl.editor.elements.model.YAWLAtomicTask;
import org.yawlfoundation.yawl.editor.resourcing.ResourceMapping;

public class SetSystemOfferBehaviourPanel extends ResourcingWizardPanel {

  private static final long serialVersionUID = 1L;
  
  private JRadioButton taskRoutingDetailButton;
  private JRadioButton retainFamiliarButton;
  
  private FamiliarTaskComboBox familiarTaskComboBox;

  public SetSystemOfferBehaviourPanel(ManageResourcingDialog dialog) {
    super(dialog);
  }
  
  protected void buildInterface() {
    setBorder(new EmptyBorder(12,12,0,11));

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    
    setLayout(gbl);
    
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 2;
    gbc.weightx = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.insets = new Insets(0,0,20,0);

    JLabel discussion = new JLabel(
        "<html><body>The offer process involves choossing a number of users that should be " +
        "informed of the existance of the work item, one of whom should eventually do this " +
        "work. As you have asked that the system automatically do this, you must now specify " +
        "how the system should automatically go about offering the work item. Initially, this " +
        "requires you to choose between the follow two alternatives: </body></html>"
    );
    
    add(discussion,gbc);
    
    taskRoutingDetailButton = buildTaskRoutingDetailButton();
    
    gbc.gridy++;
    gbc.insets = new Insets(0,0,5,0);
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.WEST;
    
    add(taskRoutingDetailButton, gbc);

    gbc.gridy++;
    gbc.gridwidth = 1;
    gbc.weightx = 0;
    gbc.insets = new Insets(0,0,0,5);

    retainFamiliarButton = buildRetainFamiliarButton();

    add(retainFamiliarButton, gbc);
    
    gbc.gridx++;

    familiarTaskComboBox = buildFamiliarTaskComboBox();
    
    add(familiarTaskComboBox, gbc);
    
    ButtonGroup buttons = new ButtonGroup();

    buttons.add(taskRoutingDetailButton);
    buttons.add(retainFamiliarButton);

    buttons.setSelected(taskRoutingDetailButton.getModel(), true);
  }
  
  private JRadioButton buildTaskRoutingDetailButton() {
    final JRadioButton button = new JRadioButton("Specify Distribution of Work Item Offers.");
    button.setMnemonic(KeyEvent.VK_D);
    button.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (button.isSelected()) {
              getResourceMapping().setRetainFamiliarTask(null);
              familiarTaskComboBox.setEnabled(false);
            }
          }
        }
    );
    return button;
  }

  private JRadioButton buildRetainFamiliarButton() {
    final JRadioButton button = new JRadioButton("Retain user from a familiar task: ");
    button.setMnemonic(KeyEvent.VK_R);
    button.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (button.isSelected()) {
            familiarTaskComboBox.setEnabled(true);
            getResourceMapping().setRetainFamiliarTask(
                familiarTaskComboBox.getSelectedFamiliarTask()    
            );
          } 
        }
      }
    );
    return button;
  }

  private FamiliarTaskComboBox buildFamiliarTaskComboBox() {
    final FamiliarTaskComboBox box = new FamiliarTaskComboBox();
    box.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if (retainFamiliarButton.isSelected() && box.isEnabled()) {
              getResourceMapping().setRetainFamiliarTask(
                  box.getSelectedFamiliarTask()    
              );
            }
          }
        }
    );
    
    return box;
  }

  public String getWizardStepTitle() {
    return "Specify System Behaviour when Offering a Work Item";
  }
  
  protected void initialise() {
    // TODO: Initialise widgets
  }
  
  public void doBack() {}

  public void doNext() {}

  public void refresh() {
    familiarTaskComboBox.setTask(
        (YAWLAtomicTask) getTask()
    );

    if (familiarTaskComboBox.getFamiliarTaskNumber() == 0) {
      retainFamiliarButton.setEnabled(false);
      familiarTaskComboBox.setEnabled(false);
      taskRoutingDetailButton.setSelected(true);
      return;
    } 
    
    if (getResourceMapping().getRetainFamiliarTask() == null) {
      taskRoutingDetailButton.setSelected(true);
      retainFamiliarButton.setEnabled(true);
      familiarTaskComboBox.setEnabled(false);
    } else {
      familiarTaskComboBox.setEnabled(true);
      familiarTaskComboBox.setSelectedFamiliarTask(
          getResourceMapping().getRetainFamiliarTask()    
      );
      retainFamiliarButton.setEnabled(true);
      retainFamiliarButton.setSelected(true);
    }
  }
  
  public boolean shouldDoThisStep() {
    return getResourceMapping().getOfferInteractionPoint() == 
      ResourceMapping.SYSTEM_INTERACTION_POINT;
  }
}