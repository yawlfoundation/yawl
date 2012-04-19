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

import org.yawlfoundation.yawl.editor.resourcing.CodeletData;
import org.yawlfoundation.yawl.editor.swing.AbstractOrderedRowTableModel;

import java.util.List;

public class CodeletSelectTableModel extends AbstractOrderedRowTableModel {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private List<CodeletData> codeletDataList;

  private static final String[] COLUMN_LABELS = { "Name", "Description" };

  public static final int NAME_COLUMN = 0;
  public static final int DESC_COLUMN = 1;

  public CodeletSelectTableModel() {
    super();
  }

  public CodeletSelectTableModel(List<CodeletData> codeletDataList) {
    super();
    setCodeletDataList(codeletDataList);
  }

  public List<CodeletData> getCodeletDataList() {
    return this.codeletDataList;
  }

  public void setCodeletDataList(List<CodeletData> codeletDataList) {
    this.codeletDataList = codeletDataList;
    if (codeletDataList != null) {
      setOrderedRows(codeletDataList);
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
    if (codeletDataList != null) {
      return getCodeletDataList().size();
    }
    return 0;
  }

  public CodeletData getCodeletAt(int row) {
    return getCodeletDataList().get(row);
  }

  public String getNameAt(int row) {
    return getCodeletAt(row).getSimpleName();
  }

    public String getCanonicalNameAt(int row) {
      return getCodeletAt(row).getName();
    }

  public String getDescriptionAt(int row) {
    return getCodeletAt(row).getDescription();
  }



  public Object getValueAt(int row, int col) {
    switch (col) {
      case NAME_COLUMN:  {
        return getNameAt(row);
      }
      case DESC_COLUMN:  {
        return getDescriptionAt(row);
      }
      default: {
        return null;
      }
    }
  }
}