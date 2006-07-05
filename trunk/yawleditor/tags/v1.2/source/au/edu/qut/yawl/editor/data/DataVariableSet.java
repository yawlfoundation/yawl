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
import java.util.Vector;
import java.util.Iterator;

public class DataVariableSet implements Serializable, Cloneable {
  private Vector variableSet;
  private Decomposition decomposition;
  
  public DataVariableSet() {
    variableSet = new Vector();
  }
  
  public void setVariableSet(Vector variableSet) {
    this.variableSet = variableSet;
    Iterator i = variableSet.iterator();
    while (i.hasNext()) {
      DataVariable variable = (DataVariable) i.next();
      variable.setScope(this);
    }
  }
  
  public Vector getVariableSet() {
    return this.variableSet;
  }
  
  public Vector getAllVariables() {
    return getVariableSet();
  }
  
  public Vector getInputVariables() {
    Vector inputVariables = new Vector();
  
    Iterator i = variableSet.iterator();
    while (i.hasNext()) {
      DataVariable variable = (DataVariable) i.next();
      if (variable.isInputVariable()) {
        inputVariables.add(variable);
      }
    }
    return inputVariables;
  }
  
  public Vector getOutputVariables() {
    Vector outputVariables = new Vector();
  
    Iterator i = variableSet.iterator();
    while (i.hasNext()) {
      DataVariable variable = (DataVariable) i.next();
      if (variable.isOutputVariable()) {
        outputVariables.add(variable);
      }
    }
    return outputVariables;
  }
  
  public Vector getLocalVariables() {
    Vector localVariables = new Vector();
  
    Iterator i = variableSet.iterator();
    while (i.hasNext()) {
      DataVariable variable = (DataVariable) i.next();
      if (variable.isLocalVariable()) {
        localVariables.add(variable);
      }
    }
    return localVariables;
  }

  public Vector getInputAndLocalVariables() {
    Vector variables = getInputVariables();
    variables.addAll(getLocalVariables());
    return variables;
  }
  
  public Vector getOutputAndLocalVariables() {
    Vector variables = getOutputVariables();
    variables.addAll(getLocalVariables());
    return variables;
  }

  public int size() {
    return variableSet.size();
  }
  
  public DataVariable getVariableAt(int position) {
    return (DataVariable) variableSet.get(position);
  }
  
  public void add(int position, DataVariable variable) {
    variable.setScope(this);
    variableSet.add(position, variable);
  }

  public void add(DataVariable variable) {
    variable.setScope(this);
    variableSet.add(variable);
  }
  
  public void remove(int position) {
    variableSet.remove(position);
  }
  
  public void remove(DataVariable variable) {
    variableSet.remove(variable);
  }
  
  public void remove(String variableName) {
    Iterator i = variableSet.iterator();
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
  
  public boolean isValidName(String name) {
    Iterator i = variableSet.iterator();
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
    Iterator i = variableSet.iterator();
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
    return variableSet.toString();
  }
  
  public void quoteXMLcontent() {
    Iterator i = variableSet.iterator();
    while (i.hasNext()) {
      DataVariable variable = (DataVariable) i.next();
      variable.quoteXMLcontent();
    }
  }
  
  public void unquoteXMLcontent() {
    Iterator i = variableSet.iterator();
    while (i.hasNext()) {
      DataVariable variable = (DataVariable) i.next();
      variable.unquoteXMLcontent();
    }
  }
  
  public void setDecomposition(Decomposition decomposition) {
    this.decomposition = decomposition; 
  }
  
  public Decomposition getDecomposition() {
    return decomposition;
  }
  
  public Object clone() {
    DataVariableSet newSet = new DataVariableSet();
    Iterator i = variableSet.iterator();
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