/*
 * Created on 20/09/2004
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

package au.edu.qut.yawl.editor.swing.data;

import java.util.Iterator;

import javax.swing.JComboBox;

import au.edu.qut.yawl.editor.data.DataVariable;
import au.edu.qut.yawl.editor.data.Decomposition;

public class DataVariableComboBox extends JComboBox {

  public static final int INPUT  = 0;
  public static final int OUTPUT = 1;
  
  private Decomposition decomposition;
  private int usage;
  
  public DataVariableComboBox() {
    super();
    initialise(DataVariable.USAGE_INPUT_AND_OUTPUT);
  }
  
  public DataVariableComboBox(int usage) {
    super();
    initialise(usage);
  }
  
  protected void initialise(int usage) {
    assert usage >= INPUT && usage <= OUTPUT : "Invalid usage passed";
    this.usage = usage;
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
    Iterator variableIterator = null;

    switch(usage) {
      case INPUT: {
        variableIterator = decomposition.getVariables().getInputAndLocalVariables().iterator();
        break;
      }
      case OUTPUT: {
        variableIterator = decomposition.getVariables().getOutputAndLocalVariables().iterator();
        break;
      }
    }
    return variableIterator;
  }
  
  public DataVariable getSelectedVariable() {
    String selectedVariableName = (String) getSelectedItem();
    if (decomposition != null) {
      return decomposition.getVariableWithName(selectedVariableName);
    }
    return null;
  }
}
