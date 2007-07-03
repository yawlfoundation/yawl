/*
 * Created on 13/08/2004
 * YAWLEditor v1.1 
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

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import au.edu.qut.yawl.editor.data.Decomposition;
import au.edu.qut.yawl.editor.data.Parameter;
import au.edu.qut.yawl.editor.elements.model.YAWLTask;
import au.edu.qut.yawl.editor.net.NetGraph;
import au.edu.qut.yawl.editor.swing.JOrderedSingleSelectTable;

public abstract class TaskParameterTable extends JOrderedSingleSelectTable {
  
  protected Decomposition outputVariableScope;
  protected Decomposition inputVariableScope;
  
  public TaskParameterTable() {
    super();
    setFormat();
  }
  
  private void setFormat() {
    if (getModel() == null || !(getModel() instanceof TaskParameterTableModel)) {
      return;
    }
    getColumn(getColumnName(TaskParameterTableModel.XQUERY_COLUMN)).setMinWidth(
        getMessageWidth(getColumnName(TaskParameterTableModel.XQUERY_COLUMN) + "-")
    );
    getColumn(getColumnName(TaskParameterTableModel.VARIABLE_COLUMN)).setMinWidth(
        getMaximumVariableNameWidth()
    );
    getColumn(getColumnName(TaskParameterTableModel.VARIABLE_COLUMN)).setPreferredWidth(
        getMaximumVariableNameWidth()
    );
    getColumn(getColumnName(TaskParameterTableModel.VARIABLE_COLUMN)).setMaxWidth(
        getMaximumVariableNameWidth()
    );
  }

  private int getMessageWidth(String message) {
    if (message == null) {
      return 0;
    }
    return  getFontMetrics(getFont()).stringWidth(message) + 5;
  }

  private int getMaximumVariableNameWidth() {
    int maxWidth = getMessageWidth(getColumnName(TaskParameterTableModel.VARIABLE_COLUMN) + "-");
    for(int i = 0; i < getRowCount(); i++) {
      maxWidth = Math.max(maxWidth, getMessageWidth(getVariableAt(i)));
    }
    return maxWidth;
  }
  
  public Component prepareRenderer(TableCellRenderer renderer,
      int row, 
      int col) {

    JComponent component = (JComponent) super.prepareRenderer(renderer, row, col);
    ((JLabel) component).setHorizontalAlignment(JLabel.CENTER);

    return component;
  }

  
  public String getVariableAt(int row) {
    if (getParameterModel() == null) {
      return null;
    }
    return getParameterModel().getVariableNameAt(row);
  }
  
  public String getQueryAt(int row) {
    return getParameterModel().getQueryAt(row);
  }
  
  public Parameter getParameterAt(int row) {
    return getParameterModel().getParameterAt(row);
  }
  
  public TaskParameterTableModel getParameterModel() {
    return (TaskParameterTableModel) getModel();
  }

  public Decomposition getInputVariableScope() {
    return this.inputVariableScope;
  }

  public void setInputVariableScope(Decomposition inputVariableScope) {
    this.inputVariableScope = inputVariableScope;
    setFormat();
  }
  
  public Decomposition getOutputVariableScope() {
    return this.outputVariableScope;
  }

  public void setOutputVariableScope(Decomposition outputVariableScope) {
    this.outputVariableScope = outputVariableScope;
  }

  public void removeRow(int row) {
    getParameterModel().removeRow(row);
  }
  
  public void insertRow(int row) {
    getParameterModel().insertRow(row);
  }
  
  public void updateRow(int row) {
    getParameterModel().updateRow(row);
  }

  public abstract void setScope(YAWLTask task, NetGraph graph);
  
  public int rowLimit() {
    // Task Parameter create/update
    // actions can increase row size, so we keep the row
    // limit above the maximum currently there for task parameter tables.
    
    return getOutputVariableScope().getVariableCount() + 1;
  }
}
