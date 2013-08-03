/*
 * Created on 20/09/2004
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
