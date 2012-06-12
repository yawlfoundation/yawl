package org.yawlfoundation.yawl.editor.ui.swing.resourcing;

import org.yawlfoundation.yawl.editor.ui.client.YConnector;
import org.yawlfoundation.yawl.editor.ui.resourcing.AllocationMechanism;
import org.yawlfoundation.yawl.editor.ui.resourcing.ResourceMapping;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

public class SetSystemAllocateBehaviourPanel extends ResourcingWizardPanel {
  
  private static final long serialVersionUID = 1L;

  private AllocationMechanismComboBox mechanismComboBox;

  public SetSystemAllocateBehaviourPanel(ManageResourcingDialog dialog) {
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
        "<html><body>An allocation strategy is a way of dynamically selecting one " +
                "participant from those resources that have been offered a task, and " +
                "have the task allocated, or assigned to, the chosen participant. " +
                "Each allocation strategy uses a different method to choose a " +
                "participant. Please choose the preferred strategy from those listed " +
                "below.</body></html>"
    );
    
    add(discussion,gbc);
    
    gbc.gridy++;
    gbc.insets = new Insets(0,0,0,5);
    gbc.fill = GridBagConstraints.NONE;
    gbc.gridwidth = 1;
    gbc.weightx = 0.5;
    gbc.anchor = GridBagConstraints.EAST;
    
    JLabel mechanismLabel = new JLabel("Allocation strategy:");
    mechanismLabel.setDisplayedMnemonic(KeyEvent.VK_A);
    
    add(mechanismLabel, gbc);
    
    gbc.gridx++;
    gbc.insets = new Insets(0,5,0,0);
    gbc.anchor = GridBagConstraints.WEST;
    
    // TODO: source mechanisms from engine api
    // TODO: tie into resourcing model
    
    mechanismComboBox = getMechanismComboBox();
    
    add(mechanismComboBox, gbc);
    mechanismLabel.setLabelFor(mechanismComboBox);
  }
  
  private AllocationMechanismComboBox getMechanismComboBox() {
    final AllocationMechanismComboBox  mechanismBox = new AllocationMechanismComboBox ();
    mechanismBox.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if (!mechanismBox.hasFocus()) {
            return;
          }
          getResourceMapping().setAllocationMechanism(
              mechanismBox.getSelectedMechanism()
          );
        }
      }
    );
    
    return mechanismBox;
  }

  public String getWizardStepTitle() {
    return "System Allocation";
  }
  
  protected void initialise() {
    // TODO: Initialise widgets
  }
  
  public void doBack() {}

  public boolean doNext() {  return true; }

    public void refresh() {
        mechanismComboBox.reset();
        mechanismComboBox.setAllocationMechanisms(YConnector.getAllocationMechanisms());
        mechanismComboBox.setSelectedAllocationMechanism(
                getResourceMapping().getAllocationMechanism()
        );
    }

  public boolean shouldDoThisStep() {
    return getResourceMapping().getAllocateInteractionPoint() == 
      ResourceMapping.SYSTEM_INTERACTION_POINT;
  }
}

class AllocationMechanismComboBox extends JComboBox {
  
  private static final long serialVersionUID = 1L;

  private List<AllocationMechanism> mechanisms;
  
  public void setAllocationMechanisms(List<AllocationMechanism> mechanisms) {
    this.mechanisms = mechanisms;
    for(AllocationMechanism mechanism : mechanisms) {
      addItem(
          mechanism.getDisplayName()
       );
    }
  }
  
  public void reset() {
    setSelectedIndex(-1);
    removeAllItems();
    mechanisms = null;
  }
  
  public void addAllocationMechanism(AllocationMechanism mechanism) {
    if (!mechanisms.contains(mechanism)) {
      mechanisms.add(mechanism);
      addItem(mechanism.getDisplayName());
    }
  }
  
  public AllocationMechanism getSelectedMechanism() {
    if (getSelectedIndex()== -1) {
      return null;
    }
    return mechanisms.get(
        getSelectedIndex()
    );
  }
  
  public void setSelectedAllocationMechanism(AllocationMechanism selectedMechanism) {
    if (selectedMechanism == null) {
      return;
    }
    for(AllocationMechanism mechanism : mechanisms) {
      if(selectedMechanism.equals(mechanism)) {
        setSelectedItem(mechanism.getDisplayName());
        return;
      }
    }
  }
}