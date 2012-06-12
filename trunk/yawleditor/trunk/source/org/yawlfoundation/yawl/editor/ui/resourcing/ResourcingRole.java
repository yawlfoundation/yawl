package org.yawlfoundation.yawl.editor.ui.resourcing;

import java.io.Serializable;
import java.util.HashMap;

public class ResourcingRole implements Serializable {

  /* ALL yawl-specific attributes of this object and its descendants 
   * are to be stored in serializationProofAttributeMap, meaning we 
   * won't get problems with incompatible XML serializations as we add 
   * new attributes in the future. 
   */
  
  private static final long serialVersionUID = 1L;
  
  protected HashMap serializationProofAttributeMap = new HashMap();
  
  public ResourcingRole() {}
  
  public ResourcingRole(String id, String name) {
    setId(id);
    setName(name);
  }

  public void setSerializationProofAttributeMap(HashMap map) {
    this.serializationProofAttributeMap = map;
  }
  
  public HashMap getSerializationProofAttributeMap() {
    return this.serializationProofAttributeMap;
  }
  
  public void setId(String roleId) {
    serializationProofAttributeMap.put("id", roleId);
  }
  
  public String getId() {
    return (String) serializationProofAttributeMap.get("id");
  }

  public void setName(String name) {
    serializationProofAttributeMap.put("name", name);
  }
  
  public String getName() {
    return (String) serializationProofAttributeMap.get("name");
  }
  
  public boolean equals(Object otherObject) {
    if (!(otherObject instanceof ResourcingRole)) {
      return false;
    }

    ResourcingRole otherRole = (ResourcingRole) otherObject;
    
    if (!getName().equals(otherRole.getName())) {
      return false;
    }

    if (!getId().equals(otherRole.getId())) {
      return false;
    }
    
    return true;
  }
}
