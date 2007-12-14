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

package org.yawlfoundation.yawl.editor.swing.resourcing;

import java.util.List;

import org.yawlfoundation.yawl.editor.data.DataVariable;

import org.yawlfoundation.yawl.editor.resourcing.DataVariableContent;
import org.yawlfoundation.yawl.editor.swing.AbstractOrderedRowTableModel;

public class ResourcingInputParamTableModel extends AbstractOrderedRowTableModel {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private List<DataVariableContent> variableContentList;
  
  private static final String[] COLUMN_LABELS = { 
    "Name",
    "Type",
    "Contains"  
  };

  public static final int NAME_COLUMN          = 0;
  public static final int TYPE_COLUMN          = 1;
  public static final int CONTAINS_COLUMN      = 2;

  public ResourcingInputParamTableModel() {
    super();
  }
  
  public ResourcingInputParamTableModel(List<DataVariableContent> variableContentList) {
    super();
    setVariableContentList(variableContentList);
  }
  
  public List<DataVariableContent> getVariableContentList() {
    return this.variableContentList;
  }
  
  public void setVariableContentList(List<DataVariableContent> variableContentList) {
    this.variableContentList = variableContentList;
    if (variableContentList != null) {
      setOrderedRows(variableContentList);
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
    if(column == CONTAINS_COLUMN) {
      return true;
    }
    return false;
  }
  
  public int getRowCount() {
    if (variableContentList != null) {
      return getVariableContentList().size();
    }
    return 0;
  }
  
  public DataVariable getVariableAt(int row) {
    return getVariableContentList().get(row).getVariable();
  }
  
  public String getNameAt(int row) {
    return getVariableAt(row).getName();
  }
  
  public String getDataTypeAt(int row) {
    return getVariableAt(row).getDataType();
  }
  
  public DataVariableContent getContentAt(int row) {
    return getVariableContentList().get(row);
  }
  
  public void setVariableContentAt(int row, String variableContents) {
    getContentAt(row).setContentType(variableContents);
    fireTableRowsUpdated(row, row);
  }
  
  public Object getValueAt(int row, int col) {
    switch (col) {
      case NAME_COLUMN:  {
        return getNameAt(row);
      }
      case TYPE_COLUMN:  {
        return getDataTypeAt(row);
      }
      case CONTAINS_COLUMN:  {
        return getContentAt(row).getContentTypeAsString();
      }
      default: {
        return null;
      }
    }
  }
}
