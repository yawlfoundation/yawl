package au.edu.qut.yawl.editor.resourcing;

import java.util.HashSet;

import au.edu.qut.yawl.editor.elements.model.YAWLAtomicTask;
import au.edu.qut.yawl.editor.elements.model.YAWLTask;

public class NewYawlResourceMapping {
  
  public static enum InteractionPointSetting {
    SYSTEM,
    USER
  };
  
  private InteractionPointSetting offerInteractionPoint = InteractionPointSetting.USER;
  private InteractionPointSetting allocateInteractionPoint = InteractionPointSetting.USER;
  private InteractionPointSetting startInteractionPoint = InteractionPointSetting.USER;
  
  private String allocationMechanism;
  
  private YAWLAtomicTask retainFamiliarTask = null;
  
  private HashSet<RuntimeUserPrivilege> enabledPrivileges = new HashSet<RuntimeUserPrivilege>();

  public static enum RuntimeUserPrivilege {
    CAN_SUSPEND,
    CAN_REALLOCATE_STATELESS,
    CAN_REALLOCATE_STATEFUL,
    CAN_DEALLOCATE,
    CAN_DELEGATE,
    CAN_SKIP,
    CAN_PILE
  };
  
  public void setOfferInteractioPoint(InteractionPointSetting setting) {
    offerInteractionPoint = setting;
  }
  
  public InteractionPointSetting getOfferInteractionPoint() {
    return offerInteractionPoint;
  }

  public void setAllocateInteractioPoint(InteractionPointSetting setting) {
    allocateInteractionPoint = setting;
  }
  
  public InteractionPointSetting getAllocateInteractionPoint() {
    return allocateInteractionPoint;
  }

  public void setStartInteractioPoint(InteractionPointSetting setting) {
    startInteractionPoint = setting;
  }
  
  public InteractionPointSetting getStartInteractionPoint() {
    return startInteractionPoint;
  }
  
  //TODO: Stubbed up until we source the list from a running engine.
  
  public String[] getRegisteredAllocationMechanisms() {
    return new String[] {
        "Round-Robin", 
        "Shortest-Queue", 
        "Random"  
    };
  }

  //TODO: Stubbed up until we source the list from a running engine.

  public String[] getUserList() {
    return new String[] {
        "Lindsay Bradford",
        "John Perkins",
        "Peter Poulos",
        "Scott Hariss",
        "David Reynolds"
    };
  }

  //TODO: Stubbed up until we source the list from a running engine.

  public String[] getBaseUserDistributionList() {
    return new String[] {
        "Lindsay Bradford",
        "David Reynolds"
    };
  }

  //TODO: Stubbed up until we source the list from a running engine.

  public String[] getRoles() {
    return new String[] {
        "CEO",
        "CIO",
        "Consultant",
        "Accountant",
        "Personal Assistant"
    };
  }
  
  public String getAllocationMechanism() {
    if (allocateInteractionPoint == InteractionPointSetting.SYSTEM) {
      return allocationMechanism;
    }
    return null;
  }
  
  public void setAllocationMechanism(String allocationMechanism) {
    this.allocationMechanism = allocationMechanism;
  }
  
  public YAWLAtomicTask getRetainFamiliarTask() {
    return this.retainFamiliarTask;
  }

  public void setRetainFamiliarTask(YAWLAtomicTask task) {
    this.retainFamiliarTask = task;
  }

  public void setEnabledPrivileges(HashSet<RuntimeUserPrivilege> privileges) {
    this.enabledPrivileges = privileges;
  }
  
  public HashSet<RuntimeUserPrivilege> getEnabledPrivileges() {
    return enabledPrivileges;
  }
  
  public void enablePrivilege(RuntimeUserPrivilege privilege, boolean enabled) {
    if (enabled) {
      enabledPrivileges.add(privilege);
    } else {
      enabledPrivileges.remove(privilege);
    }
  }
  
  public boolean isPrivilegeEnabled(RuntimeUserPrivilege privilege) {
    if (enabledPrivileges.contains(privilege)) {
      return true;
    }
    return false;
  }
  
  public String toString() {
    
    StringBuffer systemAllocationMechanism = new StringBuffer("");
    
    if (allocateInteractionPoint == InteractionPointSetting.SYSTEM) {
      systemAllocationMechanism.append(
          "System Allocation Mechanism\n" + 
          "---------------------------\n" + 
          "  " + getAllocationMechanism() + "\n"
      );
    }
    
    StringBuffer retainFamiliarTaskString = new StringBuffer("");
    if(retainFamiliarTask != null) {
      retainFamiliarTaskString.append("Retain Familiar Task = (" 
          + ((YAWLTask) retainFamiliarTask).getEngineId()
          + ").\n"
      );
    }
    
    StringBuffer enabledPrivilegesString = new StringBuffer("");
    
    if (enabledPrivileges.size() > 0) {
      enabledPrivilegesString.append(
          "Enabled Runtime Privileges:\n" + 
          "---------------------------\n"
      );
      for(RuntimeUserPrivilege privilege: enabledPrivileges) {
        enabledPrivilegesString.append("  " + privilege + "\n");
      }
    }
    
    return 
        "Interaction Points:\n-------------------\n  Offer: " + offerInteractionPoint + 
        ", Allocate: " + allocateInteractionPoint + 
        ", Start: " + startInteractionPoint + "\n" +
        systemAllocationMechanism +
        retainFamiliarTaskString +
        enabledPrivilegesString;
  }
}
