/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.editor.ui.swing.data;

import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.data.YVariable;

import javax.swing.*;
import java.util.Iterator;

public class DataVariableComboBox extends JComboBox {

  private YDecomposition decomposition;
  private int validUsageType;
  
  public DataVariableComboBox() {
    super();
  }
  
  public DataVariableComboBox(int validUsageType) {
    super();
    initialise(validUsageType);
  }
  
  protected void initialise(int usage) {
    setValidUsageType(usage);
    refresh();
  }
  
  public void setValidUsageType(int validUsageType) {
    this.validUsageType = validUsageType;
  }
   
  public void setDecomposition(YDecomposition decomposition) {
    this.decomposition = decomposition;
    refresh();
  }

  public YDecomposition getDecomposition() {
    return this.decomposition;
  }

  protected void refresh() {
    removeAllItems();
    addDataVariables();
  }
  
  protected void addDataVariables() {
    if (getDecomposition() == null) {
      return;
    }
    
    Iterator variableIterator = getUsageBasedIterator();
    
    while(variableIterator.hasNext()) {
        YVariable variable =
        (YVariable) variableIterator.next();
      addItem(variable.getName());
    }
  }
  
  protected Iterator getUsageBasedIterator() {
//    return getDecomposition().getVariables().getVariablesWithValidUsage(getValidUsageType()).iterator();
    return null;
  }
  
  public YVariable getSelectedVariable() {
    String selectedVariableName = (String) getSelectedItem();
//    if (getDecomposition() != null) {
//      return getDecomposition().getVariableWithName(selectedVariableName);
//    }
    return null;
  }
}
