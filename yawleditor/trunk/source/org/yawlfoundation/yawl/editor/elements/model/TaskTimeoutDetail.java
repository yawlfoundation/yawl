package org.yawlfoundation.yawl.editor.elements.model;

import java.io.Serializable;
import java.util.HashMap;

import org.yawlfoundation.yawl.editor.data.DataVariable;

public class TaskTimeoutDetail  implements Serializable {

  /* ALL yawl-specific attributes of this object and its descendants 
   * are to be stored in serializationProofAttributeMap, meaning we 
   * won't get problems with incompatible XML serializations as we add 
   * new attributes in the future. 
   */
  
  private static final long serialVersionUID = 1L;
  
  protected HashMap serializationProofAttributeMap = new HashMap();
  
  public static final int TRIGGER_ON_ENABLEMENT = 0;
  public static final int TRIGGER_ON_STARTING = 1;
  
  public void setSerializationProofAttributeMap(HashMap map) {
    this.serializationProofAttributeMap = map;
  }
  
  public HashMap getSerializationProofAttributeMap() {
    return this.serializationProofAttributeMap;
  }
  
  public void setTrigger(int trigger) {
    serializationProofAttributeMap.put("trigger", new Integer(trigger));
  }
  
  public int getTrigger() {
    return ((Integer) serializationProofAttributeMap.get("trigger")).intValue();
  }
  
  public void setTimeoutValue(String timeoutValue) {
    serializationProofAttributeMap.put("timeoutValue", timeoutValue);
    serializationProofAttributeMap.put("timeoutVariable", null);
  }
  
  public String getTimeoutValue() {
    return (String) serializationProofAttributeMap.get("timeoutValue");
  }

  public void setTimeoutVariable(DataVariable variable) {
    serializationProofAttributeMap.put("timeoutValue", null);
    serializationProofAttributeMap.put("timeoutVariable", variable);
  }

  public DataVariable getTimeoutVariable() {
    return (DataVariable) serializationProofAttributeMap.get("timeoutVariable");
  }
}
