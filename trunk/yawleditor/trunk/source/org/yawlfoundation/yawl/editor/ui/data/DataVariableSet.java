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

import org.yawlfoundation.yawl.editor.ui.foundations.XMLUtilities;

import java.io.Serializable;
import java.util.*;

public class DataVariableSet implements Serializable, Cloneable {
  
  private static final long serialVersionUID = 1L;

  public static final int VALID_USAGE_INPUT_FROM_NET   = 0;
  public static final int VALID_USAGE_INPUT_TO_TASK    = 1;
  public static final int VALID_USAGE_OUTPUT_FROM_TASK = 2;
  public static final int VALID_USAGE_OUTPUT_TO_NET    = 3;
  public static final int VALID_USAGE_ENTIRE_NET       = 4;
  
  private static final int[][] VALID_USAGE_MAP = {
    {  // VALID_USAGE_INPUT_FROM_NET
       DataVariable.USAGE_INPUT_AND_OUTPUT,
       DataVariable.USAGE_INPUT_ONLY,
       DataVariable.USAGE_LOCAL,
       DataVariable.USAGE_OUTPUT_ONLY
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
    setVariableSet(new LinkedList<DataVariable>());
  }
  
  public DataVariableSet(List<DataVariable> variables) {
    if (variables == null) {
      variables = new LinkedList<DataVariable>();
    }
    setVariableSet(variables);
  }

  public List<DataVariable> getVariableSet() {
    return (List<DataVariable>) serializationProofAttributeMap.get("variableSet");
  }
  
  public void setVariableSet(List<DataVariable> variableSet) {
    serializationProofAttributeMap.put("variableSet",variableSet);
    
    if (variableSet == null) {
      return;
    }
    
    for(DataVariable variable : variableSet) {
      variable.setScope(this);
    }
  }
  
  public void setDecomposition(Decomposition decomposition) {
    serializationProofAttributeMap.put("decomposition",decomposition);
  }
  
  public Decomposition getDecomposition() {
    return (Decomposition) serializationProofAttributeMap.get("decomposition");
  }
  
  public LinkedList getAllVariables() {
    return (LinkedList) getVariableSet();
  }
  
  public List<DataVariable> getInputVariables() {
    LinkedList<DataVariable> inputVariables = new LinkedList<DataVariable>();
    
    for(DataVariable variable : getVariableSet()) {
      if (variable.isInputVariable()) {
        variable.setIndex(getVariableSet().indexOf(variable));
        inputVariables.add(variable);
      }
    }
    return inputVariables;
  }
  
  public List<DataVariable> getOutputVariables() {
    LinkedList<DataVariable> outputVariables = new LinkedList<DataVariable>();
  
    for(DataVariable variable : getVariableSet()) {
      if (variable.isOutputVariable()) {
        variable.setIndex(getVariableSet().indexOf(variable));
        outputVariables.add(variable);
      }
    }
    return outputVariables;
  }
  
  public List<DataVariable> getLocalVariables() {
    LinkedList<DataVariable> localVariables = new LinkedList<DataVariable>();
    
    for(DataVariable variable : getVariableSet()) {
      if (variable.isLocalVariable()) {
        variable.setIndex(getVariableSet().indexOf(variable));
        localVariables.add(variable);
      }
    }
    return localVariables;
  }
  
  public List<DataVariable> getInputAndLocalVariables() {
    List<DataVariable> variables = getInputVariables();
    variables.addAll(getLocalVariables());
    return variables;
  }
  
  public List<DataVariable> getOutputAndLocalVariables() {
    List<DataVariable> variables = getOutputVariables();
    variables.addAll(getLocalVariables());
    return variables;
  }
  
  public List<DataVariable> getVariablesWithValidUsage(int validUsageType) {
    int[] validUsages = this.getValidUsageSet(validUsageType);

    LinkedList<DataVariable> validVariables = new LinkedList<DataVariable>();
    
    for(DataVariable variable : getVariableSet()) {
      for(int validUsage : validUsages) {
        if (variable.getUsage() == validUsage) {
          validVariables.add(variable);
        }
      }
    }
    return validVariables;
  }

  public List<DataVariable> getUserDefinedVariables() {
    LinkedList<DataVariable> userDefinedVariables = new LinkedList<DataVariable>();

    for (DataVariable variable : getVariableSet()) {
      if (variable.getUserDefined()) {
        userDefinedVariables.add(variable);
      }
    }
    return userDefinedVariables;
  }
  
  public void addVariables(List<DataVariable> newVariables) {
    if (newVariables == null) {
      return;
    }

    for (DataVariable variable: newVariables) {
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
    
    for(DataVariable variable : getVariableSet()) {
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
    for(DataVariable variable: getVariableSet()) {
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
    for (DataVariable variable: getVariableSet()) {
      variable.quoteXMLcontent();
    }
  }
  
  public void unquoteXMLcontent() {
    for(DataVariable variable: getVariableSet()) {
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
  
  
  /**
   * A utility function designed to take input parameters and output
   * parameters of this set of the same name and datatype, and replace them
   * with an equivalent input and output parameter. This is used for engine
   * import activities, where the engine parameters come in separately
   * and must be fused for correct behaviour.
   */
  public void consolidateInputAndOutputVariables() {
      List<DataVariable> varSet = getVariableSet();
      if (! varSet.isEmpty()) {
          List<DataVariable> inputList = new ArrayList<DataVariable>();
          List<DataVariable> outputList = new ArrayList<DataVariable>();
          for (DataVariable variable : varSet) {
              if (variable.getUsage() == DataVariable.USAGE_INPUT_ONLY) {
                  inputList.add(variable);
              }
              else {
                  outputList.add(variable);
              }
          }
          for (DataVariable inputVar : inputList) {
              for (DataVariable outputVar : outputList) {
                   if (inputVar.equalsIgnoreUsage(outputVar)) {
                       inputVar.setUsage(DataVariable.USAGE_INPUT_AND_OUTPUT);
                       varSet.remove(outputVar);
                   }
              }
          }
          Collections.sort(varSet);  // put them in index order
      }
  }
}