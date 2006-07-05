/*
 * Created on 13/08/2004
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
import java.util.Iterator;
import java.util.LinkedList;

import au.edu.qut.yawl.editor.foundations.XMLUtilities;

public class ParameterList implements Serializable, Cloneable {
  private LinkedList parameters;
  
  public ParameterList() {
    this.parameters = new LinkedList();
  }
  
  public void setParameters(LinkedList parameters) {
    this.parameters = parameters;
    Iterator i = parameters.iterator();
    while (i.hasNext()) {
      Parameter parameter = (Parameter) i.next();
      parameter.setList(this);
    }
  }
  
  public LinkedList getParameters() {
    return this.parameters;
  }
  
  public void add(int position, DataVariable variable, String query) {
    Parameter parameter = new Parameter(variable,query);
    parameters.add(position, parameter);
    parameter.setList(this);
  }
  
  public void addParameterPair(DataVariable variable, String query) {
    Parameter parameter = new Parameter(variable,query);
    parameters.add(parameter);
    parameter.setList(this);
  }
  
  public void remove(int position) {
    parameters.remove(position);
  }
  
  public void remove(DataVariable variable) {
    Iterator i = parameters.iterator();
    while (i.hasNext()) {
      Parameter parameter = (Parameter) i.next();
      if (parameter.getVariable().equals(variable)) {
        i.remove();
      }
    }
  }
  
  public int size() {
    return parameters.size();
  }
  
  public DataVariable getVariableAt(int position) {
    return ((Parameter) parameters.get(position)).getVariable();
  }

  public String  getVariableNameAt(int position) {
    return ((Parameter) parameters.get(position)).getVariable().getName();
  }

  public DataVariable getVariableWithName(String name) {
    Iterator parameterIterator = parameters.iterator();
    while(parameterIterator.hasNext()) {
      Parameter parameter = (Parameter) parameterIterator.next();
      if (parameter.getVariableName().equals(name)) {
        return parameter.getVariable();        
      }
    }
    return null;
  }

  public String getQueryAt(int position) {
    return ((Parameter) parameters.get(position)).getQuery();
  }
  
  public String getQueryFor(DataVariable variable) {
    if (variable == null) {
      return "";
    }
    Iterator i = parameters.iterator();
    while (i.hasNext()) {
      Parameter parameter = (Parameter) i.next();
      if (parameter.getVariable().equals(variable)) {
        return parameter.getQuery();
      }
    }
    return "";
  }
  
  public void setQueryFor(DataVariable variable, String query) {
    if (variable == null) {
      return;
    }
    Iterator i = parameters.iterator();
    while (i.hasNext()) {
      Parameter parameter = (Parameter) i.next();
      if (parameter.getVariable().equals(variable)) {
        parameter.setQuery(query);
        return;
      }
    }
    Parameter parameter = new Parameter(variable, query);
    parameters.add(parameter);
  }

  public Parameter getParameterAt(int position) {
    return (Parameter) parameters.get(position);
  }
  
  public boolean usesVariableName(String variableName) {
    Iterator i = parameters.iterator();
    while (i.hasNext()) {
      Parameter parameter = (Parameter) i.next();
      if (parameter.getVariable().getName().equals(variableName)) {
        return true;
      }
    }
    return false;
  }
  
  public void changeDecompositionInQueries(String oldLabel, String newLabel) {
    String oldLabelAsElement = XMLUtilities.toValidXMLName(oldLabel);
    String newLabelAsElement = XMLUtilities.toValidXMLName(newLabel);
    
    Iterator i = parameters.iterator();
    while (i.hasNext()) {
      Parameter parameter = (Parameter) i.next();
      String updatedQuery = 
        parameter.getQuery().replaceAll(
            "/" + oldLabelAsElement + "/",
            "/" + newLabelAsElement + "/");
      parameter.setQuery(updatedQuery);
    }
  }

  public void changeVariableNameInQueries(String oldVariableName, String newVariableName) {
    Iterator i = parameters.iterator();
    while (i.hasNext()) {
      Parameter parameter = (Parameter) i.next();
      String updatedQuery = 
        parameter.getQuery().replaceAll(
            "/" + oldVariableName + "/",
            "/" + newVariableName+ "/");
      parameter.setQuery(updatedQuery);
    }
  }

  
  public Object clone() {
    ParameterList clone = new ParameterList();

    LinkedList clonedParameters = new LinkedList();
    
    Iterator i = parameters.iterator();
    while (i.hasNext()) {
      Parameter parameter = (Parameter) i.next();
      Parameter clonedParameter = new Parameter();

      clonedParameter.setList(clone);
      clonedParameter.setQuery(new String(parameter.getQuery()));
      clonedParameter.setVariable(parameter.getVariable());
      
      clonedParameters.add(clonedParameter);
      
      clone.setParameters(clonedParameters);
    }
    
    return clone;
  }
}
