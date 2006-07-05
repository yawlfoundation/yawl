/*
 * Created on 30/07/2004
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
package au.edu.qut.yawl.editor.swing.net;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JComboBox;

import au.edu.qut.yawl.editor.swing.data.DataVariableTable;
import au.edu.qut.yawl.editor.data.DataVariable;

import au.edu.qut.yawl.editor.net.NetGraphModel;
import au.edu.qut.yawl.editor.specification.SpecificationModel;

import au.edu.qut.yawl.editor.swing.AbstractTableUpdateDialog;
import au.edu.qut.yawl.editor.swing.JUtilities;
import au.edu.qut.yawl.editor.swing.data.TaskDataVariableUpdateDialog;
import au.edu.qut.yawl.editor.swing.data.DataVariableTableModel;
import au.edu.qut.yawl.editor.swing.data.DataVariableUpdateDialogFactory;


public class NetVariableDialog extends AbstractTableUpdateDialog {

  protected final TaskDataVariableUpdateDialog updateDialog;
  
  protected JComboBox netComboBox;
  
  private NetGraphModel netModel;
  
  public NetVariableDialog() {
    super(null,true);
    updateDialog  = DataVariableUpdateDialogFactory.getNetDialog(this);
  }

  protected void makeLastAdjustments() {
    pack();
    JUtilities.setMinSizeToCurrent(this);
    updateState();
  }
  
  public void setNetModel(NetGraphModel netModel) {
    this.netModel = netModel;
    getNetVariableTable().setVariableSet(netModel.getVariableSet());
    getNetVariableTable().selectRow(0);
    updateState();
    setTitle(getTitlePrefix() + getTitleSuffix());
  }
  
  public String getTitlePrefix() {
    return "Update variables of Net ";
  }
  
  public String getTitleSuffix() {
    if (netModel.getName() != null && !netModel.getName().equals("")) {
      return " \"" + netModel.getName() + "\"";
    }
    return "";
  }

  protected JTable buildTable() {
    return new DataVariableTable(new DataVariableTableModel());
  }
  
  protected void doCreateButtonAction() {
    int selectedRow = getNetVariableTable().getSelectedRow();
    if (selectedRow >= 0) {
      getNetVariableTable().insertRow(selectedRow);
      getNetVariableTable().selectRow(selectedRow);
    } else {
      getNetVariableTable().insertRow(0);
      getNetVariableTable().selectRow(0);
      selectedRow = 0;
    }
    updateDialog.setVariable(getNetVariableTable().getVariableAt(selectedRow));
    updateDialog.setVisible(true);
    refreshSelectedRow();
    updateState();
  }
  
  protected void doUpdateButtonAction() {
    DataVariable variable = 
      getNetVariableTable().getVariableAt(getNetVariableTable().getSelectedRow());
    updateDialog.setVariable(variable);

    updateDialog.setVisible(true);

    refreshSelectedRow();
  }
  
  protected void doRemoveButtonAction() {
    final int oldSelectedRow = getTable().getSelectedRow();
         
    final String variableName = getNetVariableTable().getNameAt(oldSelectedRow);
    
    int selectedValue = 
      JOptionPane.showConfirmDialog(this,
         "You are about to permanently delete variable \"" + 
         variableName + "\".\n\n" + 
         "Are you sure you want to delete it?\n", 
         "Deleting Net Variable",
         JOptionPane.WARNING_MESSAGE, 
         JOptionPane.YES_NO_OPTION);
    if(selectedValue != JOptionPane.YES_OPTION) {
      return;
    }
    
    DataVariable oldVariable = getNetVariableTable().getVariableAt(oldSelectedRow);

    getNetVariableTable().getVariableModel().removeRow(oldSelectedRow);
    if (oldSelectedRow == getTable().getRowCount()) {
      getNetVariableTable().selectRow(oldSelectedRow - 1);
    } else {
      getNetVariableTable().selectRow(oldSelectedRow);
    }

    SpecificationModel.getInstance().propogateVariableDeletion(oldVariable);

    updateState();
  }
  
  public DataVariableTable getNetVariableTable() {
    return (DataVariableTable) getTable();
  }
  
  private void refreshSelectedRow() {
    getNetVariableTable().updateRow(
      getNetVariableTable().getSelectedRow()
    );
  }
}