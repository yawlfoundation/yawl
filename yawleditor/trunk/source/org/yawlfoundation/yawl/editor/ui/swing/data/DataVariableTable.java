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

package org.yawlfoundation.yawl.editor.ui.swing.data;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JLabel;

import javax.swing.table.TableCellRenderer;

import org.yawlfoundation.yawl.editor.ui.data.DataVariable;
import org.yawlfoundation.yawl.editor.ui.data.DataVariableSet;
import org.yawlfoundation.yawl.editor.ui.swing.JOrderedSingleSelectTable;

public class DataVariableTable extends JOrderedSingleSelectTable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private DataVariableSet variableSet;
  
  private static final int MAX_ROW_HEIGHT = 5;
  
  public DataVariableTable() {
    super();
    setModel(new DataVariableTableModel());
    setFormat();
  }
  
  public DataVariableTable(DataVariableTableModel model) {
    super();
    setModel(model);
    setFormat();
  }
  
  public void setFormat() {
    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    getColumn("Name").setMinWidth(
        getMaximumNameWidth()
    );
    getColumn("Name").setPreferredWidth(
        getMaximumNameWidth()
    );

    getColumn("Type").setMinWidth(
        getMaximumTypeWidth()
    );
    getColumn("Type").setPreferredWidth(
        getMaximumTypeWidth()
    );
    
    getColumn("Usage").setMinWidth(
        getMessageWidth(
            DataVariable.usageToString(DataVariable.USAGE_INPUT_AND_OUTPUT)
         )
    );
    getColumn("Usage").setPreferredWidth(
        getMessageWidth(
            DataVariable.usageToString(DataVariable.USAGE_INPUT_AND_OUTPUT)
         )
    );

    getColumn("Usage").setResizable(false);
  }
  
  public Dimension getPreferredScrollableViewportSize() {
    Dimension defaultPreferredSize = super.getPreferredSize();
    
    Dimension preferredSize = new Dimension(
        (int) defaultPreferredSize.getWidth(),
        (int) Math.min(
            defaultPreferredSize.getHeight(),
            getFontMetrics(getFont()).getHeight() * MAX_ROW_HEIGHT
        )
    );
    
    return preferredSize;
  }
  
  public Component prepareRenderer(TableCellRenderer renderer,
      int row, 
      int col) {

    JComponent component = (JComponent) super.prepareRenderer(renderer, row, col);
    JLabel componentAsLabel = (JLabel) component;
    if (col == DataVariableTableModel.USAGE_COLUMN || col == DataVariableTableModel.TYPE_COLUMN) {
      componentAsLabel.setHorizontalAlignment(JLabel.CENTER);
    } else {
      componentAsLabel.setHorizontalAlignment(JLabel.LEFT);
    }

    return component;
  }

  public void setVariableSet(DataVariableSet variableSet) {
    this.variableSet = variableSet;
    updateState();
  }
  
  public void updateState() {
    setModel(new DataVariableTableModel(variableSet));
    setFormat();
  }
  
  private int getMessageWidth(String message) {
    return  getFontMetrics(getFont()).stringWidth(message) + 5;
  }

  private int getMaximumNameWidth() {
    int maxWidth = getMessageWidth("Name-");
    for(int i = 0; i < this.getRowCount(); i++) {
      maxWidth = Math.max(maxWidth, getMessageWidth(this.getNameAt(i)));
    }
    return maxWidth;
  }

  private int getMaximumTypeWidth() {
    int maxWidth = getMessageWidth("Type-");
    for(int i = 0; i < this.getRowCount(); i++) {
      maxWidth = Math.max(maxWidth, getMessageWidth(this.getDataTypeAt(i)));
    }
    return maxWidth;
  }
  
  public DataVariableTableModel getVariableModel() {
    return (DataVariableTableModel) getModel();
  }

  public DataVariable getVariableAt(int row) {
    return getVariableModel().getVariableAt(row);
  }
  
  public void insertRow(int row) {
    getVariableModel().insertRow(row);
    setFormat();
  }
  
  public void updateRow(int row) {
    getVariableModel().updateRow(row);
    setFormat();
  }
  
  public void removeRow(int row) {
    getVariableModel().removeRow(row);
    setFormat();
  }
  
  public void setNameAt(int row, String name) {
    getVariableModel().setNameAt(row,name);
  }

  public String getNameAt(int row) {
    return getVariableModel().getNameAt(row);
  }

  public void setDataTypeAt(int row, String dataType) {
    getVariableModel().setDataTypeAt(row,dataType);
  }

  public String getDataTypeAt(int row) {
    return getVariableModel().getDataTypeAt(row);
  }

  
  public void setInitialValueAt(int row, String initialValue) {
    getVariableModel().setInitialValueAt(row,initialValue);
  }
  
  public boolean isValidName(String name) {
    return getVariableModel().isValidName(name);
  }
}