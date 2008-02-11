package org.yawlfoundation.yawl.editor.swing.resourcing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import org.yawlfoundation.yawl.editor.resourcing.AllocationMechanism;
import org.yawlfoundation.yawl.editor.resourcing.ResourceMapping;
import org.yawlfoundation.yawl.editor.swing.resourcing.ResourcingWizardPanel;
import org.yawlfoundation.yawl.editor.thirdparty.resourcing.ResourcingServiceProxy;

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
        "<html><body>The allocation process involves choossing a single user, from those " +
        "offered a work item, to actually undertake that work.  As you have asked that the " +
        "system automatically do this, you must now select the mechanism for doing so. " +
        "Choose from one of the mechanisms, supplied by a running engine, below.</body></html>"
    );
    
    add(discussion,gbc);
    
    gbc.gridy++;
    gbc.insets = new Insets(0,0,0,5);
    gbc.fill = GridBagConstraints.NONE;
    gbc.gridwidth = 1;
    gbc.weightx = 0.5;
    gbc.anchor = GridBagConstraints.EAST;
    
    JLabel mechanismLabel = new JLabel("Choose the runtime allocation mechanism:");
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
    return "Specify System Behaviour when Allocating a Work Item";
  }
  
  protected void initialise() {
    // TODO: Initialise widgets
  }
  
  public void doBack() {}

  public void doNext() {}

  public void refresh() {
    mechanismComboBox.reset();
    
    // JIC the option is currently not delivered (say the resourcing service is down)
    // but the setting was previously allowed.   

    if (!ResourcingServiceProxy.getInstance().getRegisteredAllocationMechanisms().contains(
            getResourceMapping().getAllocationMechanism())) {

      ResourcingServiceProxy.getInstance().getRegisteredAllocationMechanisms().add(
          getResourceMapping().getAllocationMechanism()    
      );
    }
    
    mechanismComboBox.setAllocationMechanisms(
        ResourcingServiceProxy.getInstance().getRegisteredAllocationMechanisms()
    );
    
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