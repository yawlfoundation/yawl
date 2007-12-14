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

package org.yawlfoundation.yawl.editor.swing.data;

import java.util.Iterator;

import javax.swing.JComboBox;

import org.yawlfoundation.yawl.editor.data.DataVariable;
import org.yawlfoundation.yawl.editor.data.Decomposition;

public class DataVariableComboBox extends JComboBox {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private Decomposition decomposition;
  private int validUsageType;
  
  public DataVariableComboBox() {
    super();
  }
  
  public DataVariableComboBox(int validUsageType) {
    super();
    initialise(validUsageType);
  }
  
  protected void initialise(int usage) {
    this.validUsageType = usage;
    refresh();
  }
  
  public void setDecomposition(Decomposition decomposition) {
    this.decomposition = decomposition;
    refresh();
  }
  
  public Decomposition getDecomposition() {
    return this.decomposition;
  }
  
  protected void refresh() {
    removeAllItems();
    addDataVariables();
  }
  
  protected void addDataVariables() {
    if (decomposition == null) {
      return;
    }
    
    Iterator variableIterator = getUsageBasedIterator();
    
    while(variableIterator.hasNext()) {
      DataVariable variable = 
        (DataVariable) variableIterator.next();
      addItem(variable.getName());
    }
  }
  
  protected Iterator getUsageBasedIterator() {
    return decomposition.getVariables().getVariablesWithValidUsage(validUsageType).iterator();
  }
  
  public DataVariable getSelectedVariable() {
    String selectedVariableName = (String) getSelectedItem();
    if (decomposition != null) {
      return decomposition.getVariableWithName(selectedVariableName);
    }
    return null;
  }
}
