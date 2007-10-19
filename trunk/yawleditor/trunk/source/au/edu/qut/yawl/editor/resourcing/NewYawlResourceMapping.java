package au.edu.qut.yawl.editor.resourcing;

import java.util.LinkedList;
import java.util.List;
import java.util.HashSet;

import au.edu.qut.yawl.editor.data.DataVariable;
import au.edu.qut.yawl.editor.data.DataVariableUtilities;
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
  
  private String[] baseUserDistributionList;
  private String[] baseRoleDistributionList;
  
  private HashSet<RuntimeUserPrivilege> enabledPrivileges = new HashSet<RuntimeUserPrivilege>();
  
  private List<DataVariableContent> baseVariableContentList;
  
  public static enum RuntimeUserPrivilege {
    CAN_SUSPEND,
    CAN_REALLOCATE_STATELESS,
    CAN_REALLOCATE_STATEFUL,
    CAN_DEALLOCATE,
    CAN_DELEGATE,
    CAN_SKIP,
    CAN_PILE
  };
  
  private YAWLAtomicTask resourceRequiringTask;
  
  public NewYawlResourceMapping(YAWLAtomicTask resourceRequiringTask) {
    this.resourceRequiringTask = resourceRequiringTask;
    this.baseVariableContentList = buildDefaultBaseVariableContentList();
  }
  
  private List<DataVariableContent> buildDefaultBaseVariableContentList() {
    LinkedList<DataVariableContent> list = new LinkedList<DataVariableContent>();
    
    List<DataVariable> validPossibleVariables = 
      DataVariableUtilities.getVariablesOfType(
          ((YAWLTask) resourceRequiringTask).getVariables().getInputVariables(),
          DataVariable.XML_SCHEMA_STRING_TYPE
    );
    
    for(DataVariable variable : validPossibleVariables) {
      
      list.add(new DataVariableContent(variable));
    }

    return list;
  }
  

  /**
   * Resynchronises the variable content map with changes that may have been
   * applied to the task's data perspective definitions. All new variables that
   * could be used to store resourcing data default to storing plain data initially.
   */
  
  public void syncWithDataPerspective() {
    LinkedList<DataVariableContent> variablesToRemove = new LinkedList<DataVariableContent>();
    for(DataVariableContent variableContent : baseVariableContentList) {
      if (!variableContent.isValidForResourceContainment()) {
        variablesToRemove.add(variableContent);
      }
    }
    
    for(DataVariableContent variableContent : variablesToRemove) {
      baseVariableContentList.remove(variableContent);
    }
    
    LinkedList<DataVariable> variablesToAdd = new LinkedList<DataVariable>();
    for(DataVariable variable : ((YAWLTask) resourceRequiringTask).getVariables().getInputVariables()) {
      boolean variableFound = false;
      for(DataVariableContent variableContent : baseVariableContentList) {
        if (variableContent.getVariable() == variable) {
          variableFound = true;
          break;
        }
      }
      if (!variableFound && DataVariableContent.isValidForResourceContainment(variable)) {
        variablesToAdd.add(variable);
      }
    }
    
    for(DataVariable variable: variablesToAdd) {
      baseVariableContentList.add(
          new DataVariableContent(variable)
      );
    }
  }
  
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

  public void setBaseUserDistributionList(String[] userList) {
    baseUserDistributionList = userList;
  }
  
  public String[] getBaseUserDistributionList() {
    return baseUserDistributionList;
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

  //TODO: Stubbed up until we source the list from a running engine.

  public void setBaseRoleDistributionList(String[] roles) {
    this.baseRoleDistributionList = roles;
  }
  
  public String[] getBaseRoleDistributionList() {
    return baseRoleDistributionList;
  }
  
  public void setBaseVariableContentList(List<DataVariableContent> list) {
    this.baseVariableContentList = list;
  }
  
  public List<DataVariableContent>  getBaseVariableContentList() {
    return this.baseVariableContentList;
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
    
    StringBuffer baseUserDistribitionListString = new StringBuffer("");
    if (baseUserDistributionList != null && baseUserDistributionList.length > 0) {
      baseUserDistribitionListString.append(
          "Base User Distribution List:\n" + 
          "---------------------------\n"
      );
      for(String user : baseUserDistributionList) {
        baseUserDistribitionListString.append("  " + user + "\n");
      }
    }

    StringBuffer baseRoleDistribitionListString = new StringBuffer("");
    if (baseRoleDistributionList != null && baseRoleDistributionList.length > 0) {
      baseRoleDistribitionListString.append(
          "Base RoleDistribution List:\n" + 
          "---------------------------\n"
      );
      for(String role : baseRoleDistributionList) {
        baseRoleDistribitionListString.append("  " + role + "\n");
      }
    }

    StringBuffer baseVariableContentListString = new StringBuffer("");
    if (baseVariableContentList != null && baseVariableContentList.size()> 0) {
      baseVariableContentListString.append(
          "Variable Content List:\n" + 
          "----------------------\n"
      );
      for(DataVariableContent content: baseVariableContentList) {
        baseVariableContentListString.append(
            "  " + content.getVariable().getName() + 
            " contains " + 
            content.getContentTypeAsString() + "\n");
      }
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
        baseUserDistribitionListString +
        baseRoleDistribitionListString +
        baseVariableContentListString +
        enabledPrivilegesString;
  }
}