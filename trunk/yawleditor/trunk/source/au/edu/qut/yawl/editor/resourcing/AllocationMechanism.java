package au.edu.qut.yawl.editor.resourcing;

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
  
  public AllocationMechanism() {}
  
  public AllocationMechanism(String id, String name, String description) {
    setName(id);
    setDisplayName(name);
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
