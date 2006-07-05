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
import au.edu.qut.yawl.editor.specification.SpecificationModel;

public class DataTypeComboBox extends JComboBox {
  
  public DataTypeComboBox() {
    super();
    refresh();
  }
  
  public void refresh() {
    removeAllItems();
    addSimpleDataTypes();
    addComplexDataTypes();    
  }
  
  private void addSimpleDataTypes() {
    int i;
    for(i = 0; i < DataVariable.getSimpleDataTypes().length; i++) {
      addItem(DataVariable.getSimpleDataTypes()[i]);
    }
  }
  
  private void addComplexDataTypes() {
    if (!SpecificationModel.getInstance().hasValidDataTypeDefinition()) {
      return;
    }
    
    Iterator typeIterator = 
      SpecificationModel.getInstance().getDataTypes().iterator();
    while (typeIterator.hasNext()) {
      String type = (String) typeIterator.next();
      addItem(type);
    }
  }
}
