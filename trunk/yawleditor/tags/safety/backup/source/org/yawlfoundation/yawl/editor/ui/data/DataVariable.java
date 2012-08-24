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

package org.yawlfoundation.yawl.editor.ui.data;

import org.yawlfoundation.yawl.editor.core.data.YInternalType;
import org.yawlfoundation.yawl.editor.ui.util.FileUtilities;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.logging.YLogPredicate;
import org.yawlfoundation.yawl.schema.XSDType;

import java.util.*;

public class DataVariable implements Cloneable, Comparable<DataVariable> {

  private DataVariableSet scope;

    private YParameter inputVariable;
    private YParameter outputVariable;
    private YVariable localVariable;

  public static final int USAGE_INPUT_AND_OUTPUT = 0;
  public static final int USAGE_INPUT_ONLY       = 1;
  public static final int USAGE_OUTPUT_ONLY      = 2;
  public static final int USAGE_LOCAL            = 3;
  
  public static final int SCOPE_NET = 0;
  public static final int SCOPE_TASK = 1;

    protected static final String XML_SCHEMA_URI = "http://www.w3.org/2001/XMLSchema";

  /**
   * A string array of base XMLSchema data types that can
   * be used directly in the editor without users needing to 
   * specify their own types.
   */
  
  private static final String[] BASE_DATA_TYPES = makeBaseDataTypeArray();    
  private static final int DEFAULT_TYPE = XSDType.STRING;
  
  public static final String PROPERTY_LOCATION = FileUtilities.getVariablePropertiesExtendedAttributePath();
  
  public DataVariable() {
    this("", XSDType.getString(DEFAULT_TYPE), "", USAGE_INPUT_AND_OUTPUT);
  }

    public DataVariable(YVariable local) {
        localVariable = local;
    }

    public DataVariable(YParameter param) {
        if (param.getParamType() == YParameter._INPUT_PARAM_TYPE) {
            inputVariable = param;
        }
        else outputVariable = param;
    }

  public DataVariable(String name, String dataType, String initialValue, int usage) {
      if (isInputUsage(usage)) {
          inputVariable = createParameter(name, dataType, initialValue, YParameter._INPUT_PARAM_TYPE);
      }
      if (isOutputUsage(usage)) {
          outputVariable = createParameter(name, dataType, initialValue, YParameter._OUTPUT_PARAM_TYPE);
      }
      if (usage == USAGE_LOCAL) {
          createLocalVariable(name, dataType, initialValue);
      }

  }

    private YParameter createParameter(String name, String dataType,
                                 String initialValue, int usage) {
        YParameter parameter = new YParameter(null, usage);
        parameter.setDataTypeAndName(dataType, name, XML_SCHEMA_URI);
        parameter.setInitialValue(initialValue);
        return parameter;
    }

    private void createLocalVariable(String name, String dataType, String initialValue) {
        localVariable = new YVariable();
        localVariable.setDataTypeAndName(dataType, name, XML_SCHEMA_URI);
        localVariable.setInitialValue(initialValue);
    }


    public YParameter getInputVariable() { return inputVariable; }

    public void setInputVariable(YParameter var) { inputVariable = var; }


    public YParameter getOutputVariable() { return outputVariable; }

    public void setOutputVariable(YParameter var) { outputVariable = var; }


    public YVariable getLocalVariable() { return localVariable; }

    public void setLocalVariable(YVariable var) { localVariable = var; }


  
  public void setName(String name) {
      if (inputVariable != null) inputVariable.setName(name);
      if (outputVariable != null) outputVariable.setName(name);
      if (localVariable != null) localVariable.setName(name);
  }
  
  public String getName() {
      if (inputVariable != null) return inputVariable.getName();
      if (outputVariable != null) return outputVariable.getName();
      if (localVariable != null) return localVariable.getName();
      return null;
  }
  
  public void setDataType(String dataType) {
      if (inputVariable != null) inputVariable.setDataTypeAndName(dataType, getName(), XML_SCHEMA_URI);
      if (outputVariable != null) outputVariable.setDataTypeAndName(dataType, getName(), XML_SCHEMA_URI);
      if (localVariable != null) localVariable.setDataTypeAndName(dataType, getName(), XML_SCHEMA_URI);
  }
  
  public String getDataType() {
      if (inputVariable != null) return inputVariable.getDataTypeName();
      if (outputVariable != null) return outputVariable.getDataTypeName();
      if (localVariable != null) return localVariable.getDataTypeName();
      return "string";
  }
  
  public void setInitialValue(String initialValue) {
      if (inputVariable != null) inputVariable.setInitialValue(initialValue);
      if (localVariable != null) localVariable.setInitialValue(initialValue);
  }
  
  public String getInitialValue() {
      if (inputVariable != null) return inputVariable.getInitialValue();
      if (localVariable != null) return localVariable.getInitialValue();
      return "";
  }
  
  public void setDefaultValue(String defaultValue) {
      if (outputVariable != null) outputVariable.setInitialValue(defaultValue);
  }
  
  public String getDefaultValue() {
      if (outputVariable != null) return outputVariable.getInitialValue();
      return "";
  }

  public void setUsage(int usage) {
      switch (usage) {
          case USAGE_INPUT_ONLY : changeToInputOnly(); break;
          case USAGE_OUTPUT_ONLY : changeToOutputOnly(); break;
          case USAGE_INPUT_AND_OUTPUT : changeToInputOutput(); break;
          case USAGE_LOCAL : changeToLocal(); break;
      }
  }


    private void changeToInputOnly() {
        switch (getUsage()) {
            case USAGE_OUTPUT_ONLY : {
                swapTypes(outputVariable, inputVariable, YParameter._INPUT_PARAM_TYPE);
                // deliberate no break
            }
            case USAGE_INPUT_AND_OUTPUT : outputVariable = null; break;

            case USAGE_LOCAL: {
                inputVariable = createParameter(localVariable.getName(),
                        localVariable.getDataTypeName(), localVariable.getInitialValue(),
                        YParameter._INPUT_PARAM_TYPE);
                localVariable = null;
                break;
            }
        }
    }


    private void changeToOutputOnly() {
        switch (getUsage()) {
            case USAGE_INPUT_ONLY : {
                swapTypes(inputVariable, outputVariable, YParameter._OUTPUT_PARAM_TYPE);
                // deliberate no break
            }
            case USAGE_INPUT_AND_OUTPUT : inputVariable = null; break;

            case USAGE_LOCAL: {
                outputVariable = createParameter(localVariable.getName(),
                        localVariable.getDataTypeName(), localVariable.getInitialValue(),
                        YParameter._OUTPUT_PARAM_TYPE);
                localVariable = null;
                break;
            }
        }
    }


    private void changeToInputOutput() {
        switch (getUsage()) {
            case USAGE_INPUT_ONLY : {
                swapTypes(inputVariable, outputVariable, YParameter._OUTPUT_PARAM_TYPE);
                break;
            }
            case USAGE_OUTPUT_ONLY : {
                swapTypes(outputVariable, inputVariable, YParameter._INPUT_PARAM_TYPE);
                break;
            }
            case USAGE_LOCAL: {
                inputVariable = createParameter(localVariable.getName(),
                        localVariable.getDataTypeName(), localVariable.getInitialValue(),
                        YParameter._INPUT_PARAM_TYPE);
                outputVariable = createParameter(localVariable.getName(),
                        localVariable.getDataTypeName(), localVariable.getInitialValue(),
                        YParameter._OUTPUT_PARAM_TYPE);
                localVariable = null;
                break;
            }
        }
    }


    private void changeToLocal() {
        switch (getUsage()) {
            case USAGE_INPUT_ONLY :
            case USAGE_INPUT_AND_OUTPUT : {
                createLocalVariable(inputVariable.getName(),
                        inputVariable.getDataTypeName(), inputVariable.getInitialValue());
                break;
            }
            case USAGE_OUTPUT_ONLY : {
                createLocalVariable(outputVariable.getName(),
                        outputVariable.getDataTypeName(), outputVariable.getInitialValue());
                break;
            }
        }
        inputVariable = null;
        outputVariable = null;
    }



    private void swapTypes(YParameter from, YParameter to, int type) {
        to = createParameter(from.getPreferredName(), from.getDataTypeName(), "", type);
        to.setAttributes(from.getAttributes());
        to.setLogPredicate(from.getLogPredicate());
    }
  
  public int getUsage() {
      if (localVariable != null) return USAGE_LOCAL;
      if (! (inputVariable == null || outputVariable == null)) return USAGE_INPUT_AND_OUTPUT;
      if (inputVariable != null) return USAGE_INPUT_ONLY;
      return USAGE_OUTPUT_ONLY;
  }

  public void setUserDefined(boolean userDefined) {
//    serializationProofAttributeMap.put("userDefined",userDefined);
  }
  
  public boolean getUserDefined() {
    return true;
  }

    public void setIndex(int index) {
        if (inputVariable != null) inputVariable.setOrdering(index);
        if (outputVariable != null) outputVariable.setOrdering(index);
        if (localVariable != null) localVariable.setOrdering(index);
    }

    public int getIndex() {
        if (inputVariable != null) return inputVariable.getOrdering();
        if (outputVariable != null) return outputVariable.getOrdering();
        if (localVariable != null) return localVariable.getOrdering();
        return -1;
    }


    public void setLogPredicateStarted(String predicate) {
        YLogPredicate logPredicate;
        if (inputVariable != null) {
            logPredicate = getLogPredicate(inputVariable);
            logPredicate.setStartPredicate(predicate);
        }
        if (outputVariable != null) {
            logPredicate = getLogPredicate(outputVariable);
            logPredicate.setStartPredicate(predicate);
        }
        if (localVariable != null) {
            logPredicate = getLogPredicate(localVariable);
            logPredicate.setStartPredicate(predicate);
        }
    }

    public String getLogPredicateStarted() {
        YLogPredicate logPredicate;
        if (inputVariable != null) {
            logPredicate = inputVariable.getLogPredicate();
            return logPredicate != null ? logPredicate.getStartPredicate() : null;
        }
        if (outputVariable != null) {
            logPredicate = outputVariable.getLogPredicate();
            return logPredicate != null ? logPredicate.getStartPredicate() : null;
        }
        if (localVariable != null) {
            logPredicate = localVariable.getLogPredicate();
            return logPredicate != null ? logPredicate.getStartPredicate() : null;
        }
        return null;
    }

    public void setLogPredicateCompletion(String predicate) {
        YLogPredicate logPredicate;
        if (inputVariable != null) {
            logPredicate = getLogPredicate(inputVariable);
            logPredicate.setCompletionPredicate(predicate);
        }
        if (outputVariable != null) {
            logPredicate = getLogPredicate(outputVariable);
            logPredicate.setCompletionPredicate(predicate);
        }
        if (localVariable != null) {
            logPredicate = getLogPredicate(localVariable);
            logPredicate.setCompletionPredicate(predicate);
        }
    }

    public String getLogPredicateCompletion() {
        YLogPredicate logPredicate;
        if (inputVariable != null) {
            logPredicate = inputVariable.getLogPredicate();
            return logPredicate != null ? logPredicate.getCompletionPredicate() : null;
        }
        if (outputVariable != null) {
            logPredicate = outputVariable.getLogPredicate();
            return logPredicate != null ? logPredicate.getCompletionPredicate() : null;
        }
        if (localVariable != null) {
            logPredicate = localVariable.getLogPredicate();
            return logPredicate != null ? logPredicate.getCompletionPredicate() : null;
        }
        return null;
    }


  
  public boolean isSimpleDataType() {
    return isBaseDataType(getDataType());
  }
  
  public boolean isInputVariable() {
    return isInputUsage(getUsage());
  }

  public static boolean isInputUsage(int usage) {
      return usage == USAGE_INPUT_AND_OUTPUT || usage == USAGE_INPUT_ONLY;
  }

  public boolean isOutputVariable() {
      return isOutputUsage(getUsage());
  }

  public static boolean isOutputUsage(int usage) {
      return usage == USAGE_INPUT_AND_OUTPUT || usage == USAGE_OUTPUT_ONLY;
  }
  
  public boolean isLocalVariable() {
    return getUsage() == USAGE_LOCAL;
  }
  
  public boolean isNumberType() {
      return XSDType.getInstance().isNumericType(getDataType());
  }

  public boolean isYTimerType() {
      return YInternalType.YTimerType.name().equals(getDataType());
  }

  public boolean isYStringListType() {
      return YInternalType.YStringListType.name().equals(getDataType());
  }

    public boolean isYDocumentType() {
        return YInternalType.YDocumentType.name().equals(getDataType());
    }

    public boolean isYInternalType() {
        return isYTimerType() || isYStringListType() || isYDocumentType();
    }


  public static boolean isBaseDataType(String type) {
    return XSDType.getInstance().isBuiltInType(type);
  }
  
  public static String[] getBaseDataTypes() {
    return BASE_DATA_TYPES;
  }

  private static String[] makeBaseDataTypeArray() {
      List<String> typeList = new ArrayList<String>(
                                  XSDType.getInstance().getBuiltInTypeList());
      for (YInternalType internalType : YInternalType.values()) {
          typeList.add(internalType.name());
      }
      Collections.sort(typeList, new StringIgnoreCaseComparator());
      return typeList.toArray(new String[typeList.size()]);
  }
  

  public void setScope(DataVariableSet scope) {
    this.scope = scope;
  }
  
  public DataVariableSet getScope() {
    return this.scope;
  }
  
  public void setAttribute(String name, Object value) {
      if (inputVariable != null) inputVariable.addAttribute(name, String.valueOf(value));
      if (outputVariable != null) outputVariable.addAttribute(name, String.valueOf(value));
      if (localVariable != null) localVariable.addAttribute(name, String.valueOf(value));
  }

  public String getAttribute(String name) {
    //todo MLF: returning empty String when null. is this right?
    return (getAttributes().get(name) == null ? 
               "" : getAttributes().get(name).toString());
  }

  public Map<String, String> getAttributes() {
      if (inputVariable != null) return inputVariable.getAttributes();
      if (outputVariable != null) outputVariable.getAttributes();
      if (localVariable != null) localVariable.getAttributes();
      return null;
  }

  public void setAttributes(Map<String, String> attributes) {
    if (attributes != null) {
        if (inputVariable != null) inputVariable.setAttributes(attributes);
        if (outputVariable != null) outputVariable.setAttributes(attributes);
        if (localVariable != null) localVariable.setAttributes(attributes);
    }
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

    public int compareTo(DataVariable other) {
        return this.getIndex() - other.getIndex();
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

    private YLogPredicate getLogPredicate(YVariable parameter) {
        YLogPredicate logPredicate = parameter.getLogPredicate();
        if (logPredicate == null) {
            logPredicate = new YLogPredicate();
            parameter.setLogPredicate(logPredicate);
        }
        return logPredicate;
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
