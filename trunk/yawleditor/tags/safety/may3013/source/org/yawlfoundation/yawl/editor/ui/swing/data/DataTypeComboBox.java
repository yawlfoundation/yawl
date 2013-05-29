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

import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.schema.XSDType;

import javax.swing.*;
import java.util.Arrays;

public class DataTypeComboBox extends JComboBox {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public DataTypeComboBox() {
    super();
    refresh();
  }
  
  public void refresh() {
    removeAllItems();
    addBaseDataTypes();
    addUserSuppliedDataTypes();    
  }
  
  private void addBaseDataTypes() {
    for(String type : XSDType.getInstance().getBuiltInTypeList()) {
      addItem(type);
    }
  }
  
  private void addUserSuppliedDataTypes() {
    if (!SpecificationModel.getInstance().hasValidDataTypeDefinition()) {
      return;
    }
    
    // Pre-sorting of the user-supplied data types requested by M2 Investments.
    
    Object[] dataTypeNames = SpecificationModel.getInstance().getDataTypes().toArray();
    Arrays.sort(dataTypeNames);
    
    for(int i = 0; i < dataTypeNames.length; i++) {
      addItem(dataTypeNames[i]);
    }
  }
}
