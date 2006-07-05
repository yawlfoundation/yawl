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
 
package au.edu.qut.yawl.editor.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import au.edu.qut.yawl.editor.foundations.XMLUtilities;

public class DataVariableSet implements Serializable, Cloneable {
  
  public static final int VALID_USAGE_INPUT_FROM_NET   = 0;
  public static final int VALID_USAGE_INPUT_TO_TASK    = 1;
  public static final int VALID_USAGE_OUTPUT_FROM_TASK = 2;
  public static final int VALID_USAGE_OUTPUT_TO_NET    = 3;
  public static final int VALID_USAGE_ENTIRE_NET       = 4;
  
  private static final int[][] VALID_USAGE_MAP = {
    {  // VALID_USAGE_INPUT_FROM_NET
       DataVariable.USAGE_INPUT_AND_OUTPUT,
       DataVariable.USAGE_INPUT_ONLY,
       DataVariable.USAGE_LOCAL
    },
    {  // VALID_USAGE_INPUT_TO_TASK
       DataVariable.USAGE_INPUT_AND_OUTPUT,
       DataVariable.USAGE_INPUT_ONLY,
    },
    {  // VALID_USAGE_OUTPUT_FROM_TASK
       DataVariable.USAGE_INPUT_AND_OUTPUT,
       DataVariable.USAGE_OUTPUT_ONLY,
    },
    {  // VALID_USAGE_OUTPUT_TO_NET
       DataVariable.USAGE_INPUT_AND_OUTPUT,
       DataVariable.USAGE_OUTPUT_ONLY,
       DataVariable.USAGE_LOCAL
    },
    {  // VALID_USAGE_ENTIRE_NET
       DataVariable.USAGE_INPUT_AND_OUTPUT,
       DataVariable.USAGE_INPUT_ONLY,
       DataVariable.USAGE_OUTPUT_ONLY,
       DataVariable.USAGE_LOCAL
    }
  };
  
  /* ALL yawl-specific attributes of this object and its descendants 
   * are to be stored in serializationProofAttributeMap, meaning we 
   * won't get problems with incompatible XML serializations as we add 
   * new attributes in the future. 
   */
  
  protected HashMap serializationProofAttributeMap = new HashMap();

  public DataVariableSet() {
    setVariableSet(new LinkedList());
  }
  
  public DataVariableSet(List variables) {
    if (variables == null) {
      variables = new LinkedList();
    }
    setVariableSet(variables);
  }

  public List getVariableSet() {
    return (List) serializationProofAttributeMap.get("variableSet");
  }
  
  public void setVariableSet(List variableSet) {
    serializationProofAttributeMap.put("variableSet",variableSet);
    
    if (variableSet == null) {
      return;
    }
    
    Iterator i = variableSet.iterator();
    while (i.hasNext()) {
      DataVariable variable = (DataVariable) i.next();
      variable.setScope(this);
    }
  }
  
  public void setDecomposition(Decomposition decomposition) {
    serializationProofAttributeMap.put("decomposition",decomposition);
  }
  
  public Decomposition getDecomposition() {
    return (Decomposition) serializationProofAttributeMap.get("decomposition");
  }
  
  public List getAllVariables() {
    return getVariableSet();
  }
  
  public List getInputVariables() {
    LinkedList inputVariables = new LinkedList();
  
    Iterator i = getVariableSet().iterator();
    while (i.hasNext()) {
      DataVariable variable = (DataVariable) i.next();
      if (variable.isInputVariable()) {
        inputVariables.add(variable);
      }
    }
    return inputVariables;
  }
  
  public List getOutputVariables() {
    LinkedList outputVariables = new LinkedList();
  
    Iterator i = getVariableSet().iterator();
    while (i.hasNext()) {
      DataVariable variable = (DataVariable) i.next();
      if (variable.isOutputVariable()) {
        outputVariables.add(variable);
      }
    }
    return outputVariables;
  }
  
  public List getLocalVariables() {
    LinkedList localVariables = new LinkedList();
  
    Iterator i = getVariableSet().iterator();
    while (i.hasNext()) {
      DataVariable variable = (DataVariable) i.next();
      if (variable.isLocalVariable()) {
        localVariables.add(variable);
      }
    }
    return localVariables;
  }
  
  public List getInputAndLocalVariables() {
    List variables = getInputVariables();
    variables.addAll(getLocalVariables());
    return variables;
  }
  
  public List getOutputAndLocalVariables() {
    List variables = getOutputVariables();
    variables.addAll(getLocalVariables());
    return variables;
  }
  
  public List getVariablesWithValidUsage(int validUsageType) {
    int[] validUsages = this.getValidUsageSet(validUsageType);

    LinkedList validVariables = new LinkedList();
    
    Iterator variableIterator = getVariableSet().iterator();
    while (variableIterator.hasNext()) {
      DataVariable variable = (DataVariable) variableIterator.next();
      for(int i = 0; i < validUsages.length; i++) {
        if (variable.getUsage() == validUsages[i]) {
          validVariables.add(variable);
        }
      }
    }
    return validVariables;
  }

  public List getUserDefinedVariables() {
    LinkedList userDefinedVariables = new LinkedList();
    
    Iterator i = getVariableSet().iterator();
    while (i.hasNext()) {
      DataVariable variable = (DataVariable) i.next();
      if (variable.getUserDefined() == true) {
        userDefinedVariables.add(variable);
      }
    }
    return userDefinedVariables;

  }
  
  public void addVariables(List newVariables) {
    if (newVariables == null) {
      return;
    }
    
    Iterator i = newVariables.iterator();
    while (i.hasNext()) {
      DataVariable variable = (DataVariable) i.next();
      add(variable);
    }
  }
  
  public int size() {
    return getVariableSet().size();
  }
  
  public DataVariable getVariableAt(int position) {
    return (DataVariable) getVariableSet().get(position);
  }
  
  public void add(int position, DataVariable variable) {
    variable.setScope(this);
    getVariableSet().add(position, variable);
  }

  public void add(DataVariable variable) {
    variable.setScope(this);
    getVariableSet().add(variable);
  }
  
  public void remove(int position) {
    getVariableSet().remove(position);
  }
  
  public void remove(DataVariable variable) {
    getVariableSet().remove(variable);
  }
  
  public void remove(String variableName) {
    Iterator i = getVariableSet().iterator();
    while (i.hasNext()) {
      DataVariable variable = (DataVariable) i.next();
      if (variable.getName().equals(variableName)) {
        i.remove();
      }
    }
  }
  
  public void setNameAt(int position, String name) {
    getVariableAt(position).setName(name);
  }
  
  public boolean isValidUserDefinedName(String name) {
    
   //  Only reserved variables for the engine can begin with "Yawl" 
    
    if (name.trim().startsWith("Yawl")) {  
      return false;
    }
    
    if (!XMLUtilities.isValidXMLName(name.trim())) {
      return false;
    }
    
    Iterator i = getVariableSet().iterator();
    while (i.hasNext()) {
      DataVariable variable = (DataVariable) i.next();
      if (variable.getName().equals(name)) {
        return false;
      }
    }
    return true;
  }
  
  public void setDataTypeAt(int position, String dataType) {
    getVariableAt(position).setDataType(dataType);
  }
  
  public void setInitialValueAt(int position, String initialValue) {
    getVariableAt(position).setInitialValue(initialValue);
  }

  public void setUsageAt(int position, int usage) {
    getVariableAt(position).setUsage(usage);
  }
  
  public String getNameAt(int position) {
    return getVariableAt(position).getName();
  }

  public DataVariable getVariableWithName(String name) {
    Iterator i = getVariableSet().iterator();
    while (i.hasNext()) {
      DataVariable variable = (DataVariable) i.next();
      if (variable.getName().equals(name)) {
        return variable;
      }
    }
    return null;
  }

  public String getDataTypeAt(int position) {
    return getVariableAt(position).getDataType();
  }
  
  public String getInitialValueAt(int position) {
    return getVariableAt(position).getInitialValue();
  }

  public int getUsageAt(int position) {
    return getVariableAt(position).getUsage();
  }
  
  public String toString() {
    return getVariableSet().toString();
  }
  
  public void quoteXMLcontent() {
    Iterator i = getVariableSet().iterator();
    while (i.hasNext()) {
      DataVariable variable = (DataVariable) i.next();
      variable.quoteXMLcontent();
    }
  }
  
  public void unquoteXMLcontent() {
    Iterator i = getVariableSet().iterator();
    while (i.hasNext()) {
      DataVariable variable = (DataVariable) i.next();
      variable.unquoteXMLcontent();
    }
  }
  
  public int[] getValidUsageSet(int validUsageType) {
    return VALID_USAGE_MAP[validUsageType];
  }
  
  public Object clone() {
    DataVariableSet newSet = new DataVariableSet();
    Iterator i = getVariableSet().iterator();
    while (i.hasNext()) {
      DataVariable variable = (DataVariable) i.next();
      DataVariable newVariable = (DataVariable) variable.clone();
      newSet.add(newVariable);
      newVariable.setScope(newSet);
    }
    newSet.setDecomposition(getDecomposition());
    return newSet;
  }
}