package org.yawlfoundation.yawl.editor.resourcing;

import org.yawlfoundation.yawl.editor.data.DataVariable;
import org.yawlfoundation.yawl.schema.XSDType;

import java.io.Serializable;

public class DataVariableContent implements Serializable, Cloneable {

  private static final long serialVersionUID = 1L;

  public static final int DATA_CONTENT_TYPE = 0;
  public static final int PARTICIPANT_CONTENT_TYPE = 1;
  public static final int ROLE_CONTENT_TYPE = 2;
  
  private static final String DATA_STRING = "Data";
  private static final String PARTICIPANT_STRING = "Participant";
  private static final String ROLE_STRING = "Role";
  
  protected DataVariable variable;
  protected int contentType = DATA_CONTENT_TYPE;
  
  public DataVariableContent() {}
  
  public DataVariableContent(DataVariable variable) {
    this.variable = variable;
    this.contentType = DATA_CONTENT_TYPE;
  }
  
  public DataVariableContent(DataVariable variable, int contentType) {
    this.variable = variable;
    this.contentType = contentType;
  }
  
  public void setVariable(DataVariable variable) {
    this.variable = variable;
  }
  
  public DataVariable getVariable() {
    return this.variable;
  }

  
  public void setContentType(int contentType) {
    this.contentType = contentType;
  }

  public int getContentType() {
    return this.contentType;
  }

  public void setContentType(String contentTypeString) {
    this.contentType = getContentTypeForString(contentTypeString);
  }

  public boolean isValidForResourceContainment() {
    return isValidForResourceContainment(variable);
  }
  
  public static boolean isValidForResourceContainment(DataVariable variable) {
    return variable.isInputVariable() &&
           (XSDType.getInstance().getOrdinal(variable.getDataType()) == XSDType.STRING);
  }
  
  public String getContentTypeAsString() {
    return getContentTypeAsString(contentType);
  }
  
  public static String getContentTypeAsString(int contentType) {
    switch(contentType) {
      case DATA_CONTENT_TYPE: {
        return DATA_STRING;
      }
      case PARTICIPANT_CONTENT_TYPE: {
        return PARTICIPANT_STRING;
      }
      case ROLE_CONTENT_TYPE: {
        return ROLE_STRING;
      }
    }
    return DATA_STRING;
  }
  
  public static int getContentTypeForString(String contentTypeString) {
    if (contentTypeString.equals(DATA_STRING)) {
      return DATA_CONTENT_TYPE;
    }
    if (contentTypeString.equals(PARTICIPANT_STRING)) {
      return PARTICIPANT_CONTENT_TYPE;
    }
    if (contentTypeString.equals(ROLE_STRING)) {
      return ROLE_CONTENT_TYPE;
    }
    return DATA_CONTENT_TYPE;
  }
}