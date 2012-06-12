package org.yawlfoundation.yawl.editor.ui.elements.model;

import org.yawlfoundation.yawl.editor.ui.data.DataVariable;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

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

  public void setTimeoutVariable(DataVariable variable) {
    serializationProofAttributeMap.put("timeoutValue", null);
    serializationProofAttributeMap.put("timeoutDate", null);
    serializationProofAttributeMap.put("timeoutVariable", variable);
  }

  public DataVariable getTimeoutVariable() {
    return (DataVariable) serializationProofAttributeMap.get("timeoutVariable");
  }
  
  public void setTimeoutValue(String timeoutValue) {
    serializationProofAttributeMap.put("timeoutValue", timeoutValue);
    serializationProofAttributeMap.put("timeoutDate", null);
    serializationProofAttributeMap.put("timeoutVariable", null);
    
  }
  
  public String getTimeoutValue() {
    return (String) serializationProofAttributeMap.get("timeoutValue");
  }

  public void setTimeoutDate(Date timeoutDate) {
    serializationProofAttributeMap.put("timeoutDate", timeoutDate);
    serializationProofAttributeMap.put("timeoutValue", null);
    serializationProofAttributeMap.put("timeoutVariable", null);
  }
  
  public Date getTimeoutDate() {
    return (Date) serializationProofAttributeMap.get("timeoutDate");
  }
  
  public void setTrigger(int trigger) {
    serializationProofAttributeMap.put("trigger", new Integer(trigger));
  }
  
  public int getTrigger() {
    return (Integer) serializationProofAttributeMap.get("trigger");
  }

}
