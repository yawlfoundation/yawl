package au.edu.qut.yawl.editor.resourcing;

import java.io.Serializable;

import au.edu.qut.yawl.editor.data.DataVariable;

public class DataVariableContent implements Serializable, Cloneable {

  private static final long serialVersionUID = 1L;

  public static final int DATA_CONTENT_TYPE = 0;
  public static final int USERS_CONTENT_TYPE = 1;
  public static final int ROLES_CONTENT_TYPE = 2;
  
  private static final String DATA_STRING = "Data";
  private static final String USERS_STRING = "Users";
  private static final String ROLES_STRING = "Roles";
  
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
           variable.getDataType().equals(DataVariable.XML_SCHEMA_STRING_TYPE);
  }
  
  public String getContentTypeAsString() {
    return getContentTypeAsString(contentType);
  }
  
  public static String getContentTypeAsString(int contentType) {
    switch(contentType) {
      case DATA_CONTENT_TYPE: {
        return DATA_STRING;
      }
      case USERS_CONTENT_TYPE: {
        return USERS_STRING;
      }
      case ROLES_CONTENT_TYPE: {
        return ROLES_STRING;
      }
    }
    return DATA_STRING;
  }
  
  public static int getContentTypeForString(String contentTypeString) {
    if (contentTypeString.equals(DATA_STRING)) {
      return DATA_CONTENT_TYPE;
    }
    if (contentTypeString.equals(USERS_STRING)) {
      return USERS_CONTENT_TYPE;
    }
    if (contentTypeString.equals(ROLES_STRING)) {
      return ROLES_CONTENT_TYPE;
    }
    return DATA_CONTENT_TYPE;
  }
}