/*
 * Created on 13/08/2004
 * YAWLEditor v1.1 
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

package au.edu.qut.yawl.editor.swing.data;

import au.edu.qut.yawl.editor.data.Decomposition;
import au.edu.qut.yawl.editor.data.Parameter;
import au.edu.qut.yawl.editor.elements.model.YAWLTask;
import au.edu.qut.yawl.editor.net.NetGraph;
import au.edu.qut.yawl.editor.swing.JSingleSelectTable;

public abstract class TaskParameterTable extends JSingleSelectTable {
  
  protected Decomposition outputVariableScope;
  protected Decomposition inputVariableScope;
  
  public String getVariableAt(int row) {
    return getParameterModel().getVariableNameAt(row);
  }
  
  public String getQueryAt(int row) {
    return getParameterModel().getQueryAt(row);
  }
  
  public Parameter getParameterAt(int row) {
    return getParameterModel().getParameterAt(row);
  }
  
  public TaskParameterTableModel getParameterModel() {
    return (TaskParameterTableModel) getModel();
  }

  public Decomposition getInputVariableScope() {
    return this.inputVariableScope;
  }

  public void setInputVariableScope(Decomposition inputVariableScope) {
    this.inputVariableScope = inputVariableScope;
  }
  
  public Decomposition getOutputVariableScope() {
    return this.outputVariableScope;
  }

  public void setOutputVariableScope(Decomposition outputVariableScope) {
    this.outputVariableScope = outputVariableScope;
  }

  public void removeRow(int row) {
    getParameterModel().removeRow(row);
  }
  
  public void insertRow(int row) {
    getParameterModel().insertRow(row);
  }
  
  public void updateRow(int row) {
    getParameterModel().updateRow(row);
  }

  public abstract void setScope(YAWLTask task, NetGraph graph);
  
  public int rowLimit() {
    // Task Parameter create/update
    // actions can increase row size, so we keep the row
    // limit above the maximum currently there for task parameter tables.
    
    return getOutputVariableScope().getVariableCount() + 1;
  }
}
