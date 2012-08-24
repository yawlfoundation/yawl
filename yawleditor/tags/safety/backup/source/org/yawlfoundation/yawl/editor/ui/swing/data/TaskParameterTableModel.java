/*
 * Created on 13/08/2004
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


import org.yawlfoundation.yawl.editor.ui.data.ParameterList;
import org.yawlfoundation.yawl.editor.ui.data.Parameter;
import org.yawlfoundation.yawl.editor.ui.data.DataVariable;
import org.yawlfoundation.yawl.editor.ui.swing.AbstractOrderedRowTableModel;

public class TaskParameterTableModel extends AbstractOrderedRowTableModel {

  protected ParameterList parameterList;

  public static final int XQUERY_COLUMN   = 0;
  public static final int VARIABLE_COLUMN = 1;

  private static final String[] COLUMN_LABELS = { 
    "Expression",
    "Variable"
  };
  
  public int getColumnCount() {
    return COLUMN_LABELS.length;
  }
  
  public String getColumnName(int columnIndex) {
    return COLUMN_LABELS[columnIndex];
  }
  
  public boolean isCellEditable(int row, int column) {
    return false;
  }
  
  public Class getColumnClass(int columnIndex) {
    return String.class;
  }

  public int getRowCount() {
    if (parameterList != null) {
      return parameterList.size();
    } 
    return 0;
  }

  public void insertRow(int row) {
    parameterList.add(row, new DataVariable(),""); 
    fireTableRowsInserted(row, row);
  }
  
  public void removeRow(int row) {
    parameterList.remove(row);  
    fireTableRowsDeleted(row, row);
  }
  
  public void updateRow(int row) {
    fireTableRowsUpdated(row, row);
  }
  
  public Object getValueAt(int row, int col) {
    switch (col) {
      case XQUERY_COLUMN: {
        return getQueryAt(row);
      }
      case VARIABLE_COLUMN: {
        return getVariableNameAt(row);
      }
      default: {
        return null;
      }
    }
  }
  
  public DataVariable getVariableAt(int row) {
    return parameterList.getVariableAt(row);
  }
  
  public String getVariableNameAt(int row) {
    return parameterList.getVariableNameAt(row);
  }
  
  public String getQueryAt(int row) {
    return parameterList.getQueryAt(row);
  }

  public Parameter getParameterAt(int row) {
    return parameterList.getParameterAt(row);
  }

}
