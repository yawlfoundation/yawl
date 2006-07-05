/*
 * Created on 22/09/2004
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

import au.edu.qut.yawl.editor.foundations.XMLUtilities;
import java.io.Serializable;

public class Decomposition implements Serializable {
  private String label;
  private String description;
  private DataVariableSet variables;
  
  public static final Decomposition DEFAULT = new Decomposition();
  
  public Decomposition() {
    this.label = "";
    this.description = "The default (empty) decomposition";
    setVariables(new DataVariableSet());
  }
  
  public String getLabel() {
    return this.label;
  }
  
  public String getLabelAsElementName() {
    return XMLUtilities.toValidElementName(getLabel());
  }
  
  public void setLabel(String label) {
    this.label = label;
  }
  
  public String getDescription() {
    return this.description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }

  public DataVariableSet getVariables() {
    return this.variables;
  }

  public void setVariables(DataVariableSet variables) {
    if (variables != null) {
      this.variables = variables;
      variables.setDecomposition(this);
    } 
  }
  
  public void addVariable(DataVariable variable) {
    variables.add(variable);
  }
  
  public void removeVariable(DataVariable variable) {
    variables.remove(variable);
  }
  
  public DataVariable getVariableWithName(String name) {
    return variables.getVariableWithName(name);
  }
  
  public DataVariable getVariableAt(int position) {
    return variables.getVariableAt(position);
  }
  
  public int getVariableCount() {
    return variables.size();
  }
}
