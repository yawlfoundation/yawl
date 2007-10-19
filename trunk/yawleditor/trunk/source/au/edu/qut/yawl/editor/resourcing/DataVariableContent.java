package au.edu.qut.yawl.editor.resourcing;

import au.edu.qut.yawl.editor.data.DataVariable;

public class DataVariableContent {

  public static enum ContentType {
    DATA,
    USERS,
    ROLES
  }
  
  private static final String DATA_STRING = "Data";
  private static final String USERS_STRING = "Users";
  private static final String ROLES_STRING = "Roles";
  
  private DataVariable variable;
  private ContentType  contentType = ContentType.DATA;
  
  public DataVariableContent(DataVariable variable) {
    this.variable = variable;
    this.contentType = ContentType.DATA;
  }
  
  public DataVariableContent(DataVariable variable, ContentType contentType) {
    this.variable = variable;
    this.contentType = contentType;
  }
  
  public DataVariable getVariable() {
    return this.variable;
  }
  
  public ContentType getContentType() {
    return this.contentType;
  }
  
  public void setContentType(ContentType contentType) {
    this.contentType = contentType;
  }

  public void setContentType(String contentTypeString) {
    System.out.println("Setting content type for variable (" + variable.getName() + ") to " + contentTypeString);
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
  
  public static String getContentTypeAsString(ContentType contentType) {
    switch(contentType) {
      case DATA: {
        return DATA_STRING;
      }
      case USERS: {
        return USERS_STRING;
      }
      case ROLES: {
        return ROLES_STRING;
      }
    }
    return DATA_STRING;
  }
  
  public static ContentType getContentTypeForString(String contentTypeString) {
    if (contentTypeString.equals(DATA_STRING)) {
      return ContentType.DATA;
    }
    if (contentTypeString.equals(USERS_STRING)) {
      return ContentType.USERS;
    }
    if (contentTypeString.equals(ROLES_STRING)) {
      return ContentType.ROLES;
    }
    return ContentType.DATA;
  }
}