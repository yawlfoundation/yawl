package au.edu.qut.yawl.editor.resourcing;

import java.io.Serializable;
import java.util.HashMap;

public class ResourcingFilter implements Serializable {

  /* ALL yawl-specific attributes of this object and its descendants 
   * are to be stored in serializationProofAttributeMap, meaning we 
   * won't get problems with incompatible XML serializations as we add 
   * new attributes in the future. 
   */
  
  private static final long serialVersionUID = 1L;
  
  protected HashMap serializationProofAttributeMap = new HashMap();
  
  public ResourcingFilter() {
    setParameters(new HashMap<String, String>());
  }
  
  public ResourcingFilter(String name, String displayName) {
    setName(name);
    setDisplayName(displayName);
    setParameters(new HashMap<String, String>());
  }
  
  public ResourcingFilter(String name, String displayName, HashMap<String, String> parameters) {
    setName(name);
    setDisplayName(displayName);
    setParameters(parameters);
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
  
  public void setParameters(HashMap<String, String> parameters) {
    serializationProofAttributeMap.put("parameters", parameters);
  }
  
  public HashMap<String, String> getParameters() {
    return (HashMap<String, String>) serializationProofAttributeMap.get("parameters");
  }
  
  public boolean equals(Object otherObject) {
    if (!(otherObject instanceof ResourcingFilter)) {
      return false;
    }

    ResourcingFilter otherMechanism = (ResourcingFilter) otherObject;
    
    if (!getName().equals(otherMechanism.getName())) {
      return false;
    }

    if (!getDisplayName().equals(otherMechanism.getDisplayName())) {
      return false;
    }
    
    return true;
  }
}
