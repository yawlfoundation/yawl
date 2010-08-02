/*
 * Created on 23/01/2005
 * YAWLEditor v1.08 
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2004-5 Queensland University of Technology
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

package org.yawlfoundation.yawl.editor.swing.data;

import java.util.Iterator;

import org.yawlfoundation.yawl.editor.data.DataVariable;
import org.yawlfoundation.yawl.editor.data.Decomposition;
import org.yawlfoundation.yawl.editor.data.Parameter;

public class VariableParameterComboBox extends DataVariableComboBox {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private Parameter parameter;
  
  public VariableParameterComboBox() {
    super();
    initialise(DataVariable.USAGE_INPUT_AND_OUTPUT);
  }
  
  public VariableParameterComboBox(int usage) {
    super();
    initialise(usage);
  }
  
  public void setDetail(Parameter parameter, Decomposition decomposition) {
    this.parameter = parameter;
    setDecomposition(decomposition);
    refresh();
  }
 
 
  protected void addDataVariables() {
    if (getDecomposition() == null) {
      return;
    }
    
    Iterator variableIterator = getUsageBasedIterator();

    while(variableIterator.hasNext()) {
      DataVariable variable = 
        (DataVariable) variableIterator.next();

      if (parameter.getVariable() != null && parameter.getVariable().equals(variable)) {
          addItem(variable.getName());
      }
      if (parameter.getList() != null && !parameter.getList().usesVariableName(variable.getName())) {
          addItem(variable.getName());
      }
    }
  }
}
