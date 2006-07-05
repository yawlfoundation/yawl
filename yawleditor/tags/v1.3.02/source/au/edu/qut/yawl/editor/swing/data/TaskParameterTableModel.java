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
package au.edu.qut.yawl.editor.swing.data;

import javax.swing.table.AbstractTableModel;

import au.edu.qut.yawl.editor.data.ParameterList;
import au.edu.qut.yawl.editor.data.Parameter;
import au.edu.qut.yawl.editor.data.DataVariable;

public abstract class TaskParameterTableModel extends AbstractTableModel {

  protected ParameterList parameterList;

  public static final int XQUERY_COLUMN   = 0;
  public static final int VARIABLE_COLUMN = 1;
  
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
