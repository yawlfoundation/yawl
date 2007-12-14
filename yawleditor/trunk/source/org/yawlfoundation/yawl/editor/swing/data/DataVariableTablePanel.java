/*
 * Created on 27/01/2005
 * YAWLEditor v1.08 
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

package org.yawlfoundation.yawl.editor.swing.data;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.yawlfoundation.yawl.editor.data.DataVariable;
import org.yawlfoundation.yawl.editor.data.DataVariableSet;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.swing.AbstractDoneDialog;
import org.yawlfoundation.yawl.editor.swing.AbstractTableUpdatePanel;
import org.yawlfoundation.yawl.editor.swing.JOrderedSingleSelectTable;

public class DataVariableTablePanel extends AbstractTableUpdatePanel {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private AbstractDoneDialog parent;
  protected TaskDataVariableUpdateDialog updateDialog;

  public DataVariableTablePanel(AbstractDoneDialog dialog) {
    super();
    this.parent = dialog;
  }
  
  protected JOrderedSingleSelectTable buildTable() {
    return new DataVariableTable(new DataVariableTableModel());
  }
  
  protected void doCreateButtonAction() {
    int selectedRow = getVariableTable().getSelectedRow();
    
    if (selectedRow < 0) {
      selectedRow = 0;
    }
    
    final int lastRow = getVariableTable().getRowCount();
    
    getVariableTable().insertRow(lastRow);
    getVariableTable().selectRow(lastRow);
    
    updateDialog.setVariable(
      getVariableTable().getVariableAt(
        getVariableTable().getSelectedRow()
      )
    );

    updateDialog.setVisible(true);

    if (updateDialog.cancelButtonSelected()) {
       getVariableTable().removeRow(lastRow);
    } else {
      refreshSelectedRow();
    }
    updateState();
  }
  
  protected void doUpdateButtonAction() {
    DataVariable variable = 
      getVariableTable().getVariableAt(getVariableTable().getSelectedRow());
    updateDialog.setVariable(variable);

    updateDialog.setVisible(true);

    refreshSelectedRow();
    updateState();
  }
  
  protected void doRemoveButtonAction() {
    final int oldSelectedRow = getTable().getSelectedRow();
    
    final String variableName = getVariableTable().getNameAt(oldSelectedRow);
    
    int selectedValue = 
      JOptionPane.showConfirmDialog(this,
         "You are about to permanently delete variable \"" + 
         variableName + "\".\n\n" + 
         "Are you sure you want to delete it?\n", 
         "Deleting Task Variable",
         JOptionPane.WARNING_MESSAGE, 
         JOptionPane.YES_NO_OPTION);
    if(selectedValue != JOptionPane.YES_OPTION) {
      return;
    }

    
    SpecificationModel.getInstance().propogateVariableDeletion(
        getVariableTable().getVariableAt(oldSelectedRow)  
      );
    
    getVariableTable().getVariableModel().removeRow(oldSelectedRow);
    if (oldSelectedRow == getTable().getRowCount()) {
      getVariableTable().selectRow(oldSelectedRow - 1);
    } else {
      getVariableTable().selectRow(oldSelectedRow);
    }
    updateState();
  }
  
  public DataVariableTable getVariableTable() {
    return (DataVariableTable) getTable();
  }
  
  public void updateState() {
    super.updateState();
    setSize(getPreferredSize());
    parent.pack();
  }
  
  public int rowLimit() {
    return Integer.MAX_VALUE;    
  }
  
  private void refreshSelectedRow() {
    getVariableTable().updateRow(
      getVariableTable().getSelectedRow()
    );
  }

  public void setScope(int scope) {
    if (scope == DataVariable.SCOPE_NET) {
      updateDialog = DataVariableUpdateDialogFactory.getNetDialog(parent);
    } else {
      updateDialog = DataVariableUpdateDialogFactory.getTaskDialog(parent);
    }
  }
  
  public void setVariables(DataVariableSet variables) {
    getVariableTable().setVariableSet(variables);
    getVariableTable().selectRow(0);
    updateState();
  }
  
  public DataVariableSet getVariables() {
    return getVariableTable().getVariableModel().getVariableSet();
  }
}