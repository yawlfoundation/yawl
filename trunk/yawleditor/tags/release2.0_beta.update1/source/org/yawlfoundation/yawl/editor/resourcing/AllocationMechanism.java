package org.yawlfoundation.yawl.editor.resourcing;

import java.io.Serializable;
import java.util.HashMap;

public class AllocationMechanism implements Serializable {

  /* ALL yawl-specific attributes of this object and its descendants 
   * are to be stored in serializationProofAttributeMap, meaning we 
   * won't get problems with incompatible XML serializations as we add 
   * new attributes in the future. 
   */
  
  private static final long serialVersionUID = 1L;
  
  protected HashMap serializationProofAttributeMap = new HashMap();
  
  /*  
   * I supply 3 "guaranteed-to-be-there allocation mechanisms as defaults
   * here, needed for when we have no engine connection to speak of.
   * The lables for the standard, guaranteed to be there, allocation
   * mechanisms are necessarilly redundant.  If the engine ever 
   * changes the name and/or class name of one of the standard 
   * allocation mechanisms, that detail must also be updated here.
   */
  
  public static final AllocationMechanism RANDOM_MECHANISM = new AllocationMechanism(
      "RandomChoice",
      "Random Choice",
      "Randomly allocate the workitem from the set of participants."
  );

  public static final AllocationMechanism ROUND_ROBIN_MECHANISM = new AllocationMechanism(
      "RoundRobin",
      "Round-Robin",
      "Allocated based on a round-robin sequence of the participants."
  );

  public static final AllocationMechanism SHORTEST_QUEUE_MECHANISM = new AllocationMechanism(
      "ShortestQueue",
      "Shortest-Queue",
      "Allocate based on the participant with the shortest work-list queue."
  );

  public static final AllocationMechanism DEFAULT_MECHANISM = RANDOM_MECHANISM;
  
  public AllocationMechanism() {}
  
  public AllocationMechanism(String name, String displayName, String description) {
    setName(name);
    setDisplayName(displayName);
    setDescription(description);
  }

  public void setSerializationProofAttributeMap(HashMap map) {
    this.serializationProofAttributeMap = map;
  }
  
  public HashMap getSerializationProofAttributeMap() {
    return this.serializationProofAttributeMap;
  }
  
  public void setName(String name) {
    serializationProofAttributeMap.put("name", name);
  }
  
  public String getName() {
    return (String) serializationProofAttributeMap.get("name");
  }

  public void setDisplayName(String displayName) {
    serializationProofAttributeMap.put("displayName", displayName);
  }
  
  public String getDisplayName() {
    return (String) serializationProofAttributeMap.get("displayName");
  }

  public void setDescription(String description) {
    serializationProofAttributeMap.put("description", description);
  }
  
  public String getDescritpion() {
    return (String) serializationProofAttributeMap.get("description");
  }
  
  public boolean equals(Object otherObject) {
    if (!(otherObject instanceof AllocationMechanism)) {
      return false;
    }

    AllocationMechanism otherMechanism = (AllocationMechanism) otherObject;
    
    if (!getName().equals(otherMechanism.getName())) {
      return false;
    }

    if (!getDisplayName().equals(otherMechanism.getDisplayName())) {
      return false;
    }
    
    return true;
  }
}
