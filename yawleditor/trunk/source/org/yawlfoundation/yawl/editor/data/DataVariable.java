/*
 * Created on 23/05/2004
 * YAWLEditor v1.01 
 *
 * @author Lindsay Bradford
 * 
 * 
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

package org.yawlfoundation.yawl.editor.data;

import org.yawlfoundation.yawl.editor.foundations.FileUtilities;
import org.yawlfoundation.yawl.editor.foundations.XMLUtilities;
import org.yawlfoundation.yawl.schema.XSDType;

import java.io.Serializable;
import java.util.*;

public class DataVariable implements Serializable, Cloneable {
  
  /* ALL yawl-specific attributes of this object and its descendants 
   * are to be stored in serializationProofAttributeMap, meaning we 
   * won't get problems with incompatible XML serializations as we add 
   * new attributes in the future. 
   */
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  protected HashMap serializationProofAttributeMap = new HashMap();
  
  private transient DataVariableSet scope;
  
  public static final int USAGE_INPUT_AND_OUTPUT = 0;
  public static final int USAGE_INPUT_ONLY       = 1;
  public static final int USAGE_OUTPUT_ONLY      = 2;
  public static final int USAGE_LOCAL            = 3;
  
  public static final int SCOPE_NET = 0;
  public static final int SCOPE_TASK = 1;
  
//  public static final String XML_SCHEMA_BOOLEAN_TYPE  = "boolean";
//  public static final String XML_SCHEMA_DATE_TYPE     = "date";
//  public static final String XML_SCHEMA_DOUBLE_TYPE   = "double";
//  public static final String XML_SCHEMA_DURATION_TYPE = "duration";
//  public static final String XML_SCHEMA_LONG_TYPE     = "long";
//  public static final String XML_SCHEMA_STRING_TYPE   = "string";
//  public static final String XML_SCHEMA_TIME_TYPE     = "time";
  public static final String YAWL_SCHEMA_TIMER_TYPE   = "YTimerType";

  /**
   * A string array of base XMLSchema data types that can
   * be used directly in the editor without users needing to 
   * specify their own types.
   */
  
  private static final String[] BASE_DATA_TYPES = makeBaseDataTypeArray();    
  private static final int DEFAULT_TYPE = XSDType.STRING;
  
  public static final String PROPERTY_LOCATION = FileUtilities.getVariablePropertiesExtendedAttributePath();
  
  public DataVariable() {
    setName("");
    setDataType(XSDType.getString(DEFAULT_TYPE));
    setInitialValue(""); 
    setUsage(USAGE_INPUT_AND_OUTPUT);
    setUserDefined(true);
    setAttributes(null);
  }    

  public DataVariable(String name, String dataType, String initialValue, int usage) {
    setName(name);
    setDataType(dataType);
    setInitialValue(initialValue); 
    setUsage(usage);
    setUserDefined(true);
    setAttributes(null);
  }
  
  public void setName(String name) {
    serializationProofAttributeMap.put("name",name);
  }
  
  public String getName() {
    return (String) serializationProofAttributeMap.get("name");
  }
  
  public void setDataType(String dataType) {
    serializationProofAttributeMap.put("dataType",dataType);
  }
  
  public String getDataType() {
    return (String) serializationProofAttributeMap.get("dataType");
  }
  
  public void setInitialValue(String initialValue) {
    serializationProofAttributeMap.put("initialValue",initialValue);
  }
  
  public String getInitialValue() {
    return (String) serializationProofAttributeMap.get("initialValue");
  }
  
  public void setDefaultValue(String initialValue) {
    serializationProofAttributeMap.put("defaultValue",initialValue);
  }
  
  public String getDefaultValue() {
    return (String) serializationProofAttributeMap.get("defaultValue");
  }

  public void setUsage(int usage) {
    serializationProofAttributeMap.put("usage",new Integer(usage));
  }
  
  public int getUsage() {
    return ((Integer) serializationProofAttributeMap.get("usage")).intValue();
  }

  public void setUserDefined(boolean userDefined) {
    serializationProofAttributeMap.put("userDefined",new Boolean(userDefined));
  }
  
  public boolean getUserDefined() {
    return ((Boolean) serializationProofAttributeMap.get("userDefined")).booleanValue();
  }
  
  public boolean isSimpleDataType() {
    return isBaseDataType(getDataType());
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
      return XSDType.getInstance().isNumericType(getDataType());
  }

  public boolean isYTimerType() {
      return getDataType().equals(YAWL_SCHEMA_TIMER_TYPE);
  }
  
  public static boolean isBaseDataType(String type) {
    return XSDType.getInstance().isBuiltInType(type);  
  }
  
  public static String[] getBaseDataTypes() {
    return BASE_DATA_TYPES;
  }

  private static String[] makeBaseDataTypeArray() {
      List<String> typeList = XSDType.getInstance().getBuiltInTypeList();
      typeList.add(YAWL_SCHEMA_TIMER_TYPE);
      Collections.sort(typeList, new StringIgnoreCaseComparator());
      return typeList.toArray(new String[typeList.size()]);
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
  
  //MLF: BEGIN
  //LWB: Slight mods on MLF code to make extended attributes part of the typical decomposition attribute set.
  public void setAttribute(String name, Object value) {
      getAttributes().put(name, value);
  }

  public String getAttribute(String name) {
    //todo MLF: returning empty String when null. is this right?
    return (getAttributes().get(name) == null ? 
               "" : getAttributes().get(name).toString());
  }

  public Hashtable getAttributes() {
    return (Hashtable) serializationProofAttributeMap.get("extendedAttributes");
  }

  public void setAttributes(Hashtable attributes) {
    if (attributes == null) {
      attributes = new Hashtable();
    }
    serializationProofAttributeMap.put("extendedAttributes",attributes);
  }
  //MLF: END

  
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
  
  public String toString() {
    return "name: " + getName() + ", type: " + getDataType() + 
           ", usage: " + getUsage();
  }
  
  public Object clone() {
    DataVariable variable = new DataVariable();

    variable.setDataType(getDataType());
    variable.setInitialValue(getInitialValue());
    variable.setDefaultValue(getDefaultValue());
    variable.setName(getName());
    variable.setUsage(getUsage());
    
    return variable;
  }
  
  public boolean equals(Object object) {
    return equalsIgnoreUsage(object)  &&
           (getUsage() == ((DataVariable) object).getUsage());
  }

    public boolean equalsIgnoreUsage(Object object) {
      if (!(object instanceof DataVariable)) {
        return false;
      }
      DataVariable otherVariable = (DataVariable) object;
        return getDataType().equals(otherVariable.getDataType()) &&
               getName().equals(otherVariable.getName());
    }


    static class StringIgnoreCaseComparator implements Comparator<String> {

      	public int compare(String one, String two)	{

            // if one object is null, ignore it and return the other as having precedence
            if (one == null) return -1;
            if (two == null) return 1;

            // compare strings ignoring case
            return one.compareToIgnoreCase(two);
        }
    }


}
