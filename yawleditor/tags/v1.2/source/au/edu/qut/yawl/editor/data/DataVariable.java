/*
 * Created on 23/05/2004
 * YAWLEditor v1.01 
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2003 Queensland University of Technology
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package au.edu.qut.yawl.editor.data;

import java.io.Serializable;
import au.edu.qut.yawl.editor.foundations.XMLUtilities;

public class DataVariable implements Serializable, Cloneable {
  private String name;
  private String dataType;
  private String initialValue;
  private int usage;
  
  public static final int USAGE_INPUT_AND_OUTPUT = 0;
  public static final int USAGE_INPUT_ONLY       = 1;
  public static final int USAGE_OUTPUT_ONLY      = 2;
  public static final int USAGE_LOCAL            = 3;
  
  public static final int SCOPE_NET = 0;
  public static final int SCOPE_TASK = 1;
  
  private transient DataVariableSet scope;
  
  private static final String[] SIMPLE_TYPES = {
    "boolean",
    "date",
    "double",
    "long",
    "string",
    "time"    
  };
  
  private static final int DEFAULT_TYPE = 4; // String
  
  public DataVariable() {
    this.name = "";
    this.dataType = SIMPLE_TYPES[DEFAULT_TYPE];
    this.initialValue = "";
    this.usage = USAGE_INPUT_AND_OUTPUT;
  }    

  public DataVariable(String name, String dataType, String initialValue, int usage) {
    setName(name);
    setDataType(dataType);
    setInitialValue(initialValue); 
    setUsage(usage);
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getName() {
    return this.name;
  }
  
  public void setDataType(String dataType) {
    this.dataType = dataType;
  }
  
  public String getDataType() {
    return this.dataType;
  }
  
  public void setInitialValue(String initialValue) {
    this.initialValue = initialValue;
  }
  
  public String getInitialValue() {
    return this.initialValue;
  }

  public void setUsage(int usage) {
    this.usage = usage;
  }
  
  public int getUsage() {
    return this.usage;
  }
  
  public boolean isSimpleDataType() {
    return isSimpleDataType(getDataType());
  }
  
  public boolean isInputVariable() {
    return getUsage() == USAGE_INPUT_AND_OUTPUT || getUsage() == USAGE_INPUT_ONLY;
  }

  public boolean isOutputVariable() {
    return getUsage() == USAGE_INPUT_AND_OUTPUT || getUsage() == USAGE_OUTPUT_ONLY;
  }
  
  public boolean isLocalVariable() {
    return getUsage() == USAGE_LOCAL;
  }
  
  public boolean isNumberType() {
    return getDataType().equals("double") || getDataType().equals("long");
  }
  
  public static boolean isSimpleDataType(String type) {
    boolean isSimpleType = false;
    for(int i = 0; i < SIMPLE_TYPES.length; i++) {
      if (type.equals(SIMPLE_TYPES[i])) {
        return true;
      }
    }
    return isSimpleType;
  }
  
  public static String[] getSimpleDataTypes() {
    return SIMPLE_TYPES;
  }
  
  public void quoteXMLcontent() {
    setInitialValue(XMLUtilities.quoteXML(getInitialValue())); 
  }
  
  public void unquoteXMLcontent() {
    setInitialValue(XMLUtilities.unquoteXML(getInitialValue())); 
  }
  
  public void setScope(DataVariableSet scope) {
    this.scope = scope;
  }
  
  public DataVariableSet getScope() {
    return this.scope;
  }
  
  public static String usageToString(int usage) {
    switch(usage) {
      case USAGE_INPUT_AND_OUTPUT: {
        return "Input & Output";
      }
      case DataVariable.USAGE_INPUT_ONLY: {
        return "Input Only";
      }
      case DataVariable.USAGE_OUTPUT_ONLY: {
        return "Output Only";
      }
      case DataVariable.USAGE_LOCAL: {
        return "Local";
      }
    }
    return "--error--";
  }

  public static String scopeToString(int scope) {
    switch(scope) {
      case SCOPE_TASK: {
        return "Task";
      }
      case SCOPE_NET: {
        return "Net";
      }
    }
    return "--error--";
  }
  
  public Object clone() {
    DataVariable variable = new DataVariable();

    variable.setDataType(this.getDataType());
    variable.setInitialValue(this.getInitialValue());
    variable.setName(this.getName());
    variable.setUsage(this.getUsage());
    
    return variable;
  }
}
