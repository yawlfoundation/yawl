/*
 * Created on 6/08/2004
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

package au.edu.qut.yawl.editor.swing.data;

import au.edu.qut.yawl.editor.data.DataVariable;
import au.edu.qut.yawl.editor.data.DataVariableSet;

import au.edu.qut.yawl.editor.swing.AbstractOrderedRowTableModel;

public class DataVariableTableModel extends AbstractOrderedRowTableModel {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private DataVariableSet variableSet;
  
  private static final String[] COLUMN_LABELS = { 
    "Name",
    "Type",
    "Usage"  
  };
  
  public static final int NAME_COLUMN          = 0;
  public static final int TYPE_COLUMN          = 1;
  public static final int USAGE_COLUMN         = 2;

  public DataVariableTableModel() {
    super();
  }
  
  public DataVariableTableModel(DataVariableSet variableSet) {
    super();
    setVariableSet(variableSet);
  }
  
  public DataVariableSet getVariableSet() {
    return this.variableSet;
  }
  
  public void setVariableSet(DataVariableSet variableSet) {
    this.variableSet = variableSet;
    if (variableSet != null) {
      setOrderedRows(variableSet.getAllVariables());
    }
  } 

  public int getColumnCount() {
    return COLUMN_LABELS.length;
  }

  public String getColumnName(int columnIndex) {
    return COLUMN_LABELS[columnIndex];
  }
  
  public Class getColumnClass(int columnIndex) {
    return String.class;
  }
  
  public boolean isCellEditable(int row, int column) {
    return false;
  }
  
  public int getRowCount() {
    if (variableSet != null) {
      return getVariableSet().size();
    }
    return 0;
  }

  public void insertRow(int row) {
    DataVariable variable = new DataVariable();
    variable.setScope(variableSet);
    getVariableSet().add(row, variable); 
    fireTableRowsInserted(row, row);
  }
  
  public void removeRow(int row) {
    getVariableSet().remove(row);  
    fireTableRowsDeleted(row, row);
  }
  
  public void updateRow(int row) {
    fireTableRowsUpdated(row, row);
  }
  
  public void setNameAt(int row, String name) {
    getVariableSet().setNameAt(row, name);
    fireTableRowsUpdated(row, row);
  }

  public String getNameAt(int row) {
    return getVariableSet().getNameAt(row);
  }

  public boolean isValidName(String name) {
    return getVariableSet().isValidUserDefinedName(name);
  }

  public void setDataTypeAt(int row, String dataType) {
    getVariableSet().setDataTypeAt(row, dataType);
    fireTableRowsUpdated(row, row);
  }

  public String getDataTypeAt(int row) {
    return getVariableSet().getDataTypeAt(row);
  }
  
  public void setInitialValueAt(int row, String initialValue) {
    getVariableSet().setInitialValueAt(row, initialValue);
    fireTableRowsUpdated(row, row);
  }
  
  public Object getValueAt(int row, int col) {
    switch (col) {
      case NAME_COLUMN:  {
        return getVariableSet().getNameAt(row);
      }
      case TYPE_COLUMN:  {
        return getVariableSet().getDataTypeAt(row);
      }
      case USAGE_COLUMN:  {
        return DataVariable.usageToString(getVariableSet().getUsageAt(row));
      }
      default: {
        return null;
      }
    }
  }
  
  public DataVariable getVariableAt(int row) {
    return getVariableSet().getVariableAt(row);
  }
}
