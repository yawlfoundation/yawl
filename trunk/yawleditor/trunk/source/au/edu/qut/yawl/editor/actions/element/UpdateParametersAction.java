/*
 * Created on 09/10/2003
 * YAWLEditor v1.0 
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
 *
 */

package au.edu.qut.yawl.editor.actions.element;

import au.edu.qut.yawl.editor.net.NetGraph;
import au.edu.qut.yawl.editor.swing.element.AbstractTaskDoneDialog;
import au.edu.qut.yawl.editor.swing.AbstractTableUpdatePanel;
import au.edu.qut.yawl.editor.swing.JOrderedSingleSelectTable;
import au.edu.qut.yawl.editor.swing.TooltipTogglingWidget;
import au.edu.qut.yawl.editor.swing.data.ParameterUpdateDialog;
import au.edu.qut.yawl.editor.swing.data.TaskParameterTable;
import au.edu.qut.yawl.editor.swing.data.TaskParameterTableModel;
import au.edu.qut.yawl.editor.swing.data.DataVariableTable;

import au.edu.qut.yawl.editor.actions.net.YAWLSelectedNetAction;
import au.edu.qut.yawl.editor.data.ParameterList;

import au.edu.qut.yawl.editor.elements.model.YAWLTask;


import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.JScrollPane;

import javax.swing.JTable;

public class UpdateParametersAction extends YAWLSelectedNetAction 
                                    implements TooltipTogglingWidget {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final UpdateParametersDialog parametersDialog = new UpdateParametersDialog();
  
  private NetGraph graph;
  private YAWLTask task;
  
  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Update Parameters...");
    putValue(Action.LONG_DESCRIPTION, "Update Parameters for this task.");
    putValue(Action.SMALL_ICON, getIconByName("Blank"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_P));
  }
  
  public UpdateParametersAction(YAWLTask task, NetGraph graph) {
    super();
    this.task = task;
    this.graph = graph;
  } 
  
  public void actionPerformed(ActionEvent event) {
    parametersDialog.setScope(task, graph);
    parametersDialog.setVisible(true);
  }
  
  public String getEnabledTooltipText() {
    return " Update Parameters for this task ";
  }
  
  public String getDisabledTooltipText() {
    return " You must have task with a decomposition selected" + 
           " to update its parameters ";
  }
}

class UpdateParametersDialog extends AbstractTaskDoneDialog  {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private DataVariableTable netVariableTable;
  private DataVariableTable taskVariableTable;
  
  private InputParameterUpdatePanel inputPanel;
  private OutputParameterUpdatePanel outputPanel;
  
  public UpdateParametersDialog() {
    super(null, true, false);
    setContentPanel(getUpdateParametersPanel());
  }
  
  protected void makeLastAdjustments() {
    pack();
    updateState();
  }
  
  public void setScope(YAWLTask task, NetGraph graph) {
    setTask(task,graph);
    inputPanel.setScope(task, graph);
    outputPanel.setScope(task, graph);

    netVariableTable.setVariableSet(
        graph.getNetModel().getVariableSet()
    );
    taskVariableTable.setVariableSet(
        task.getVariables()
    );

    pack();
  }
  
  public String getTitlePrefix() {
    return "Update Parameters for ";
  }
    
  private JPanel getUpdateParametersPanel() {
    
    GridBagConstraints gbc = new GridBagConstraints();

    JPanel panel = new JPanel(new GridBagLayout());
    
    panel.setBorder(new EmptyBorder(12,12,0,11));

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 0.5;
    gbc.weighty = 0.5;
    gbc.gridheight = 1;
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.insets = new Insets(0,0,5,0); 
    
    panel.add(getInputPanel(),gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.weightx = 0;
    gbc.weighty = 0;
    gbc.gridheight = 1;
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;

    panel.add(getNetPanel(), gbc);

    gbc.gridx++;

    panel.add(getTaskPanel(), gbc);
    
    gbc.gridx = 0;
    gbc.gridy++; 
    gbc.weighty = 0.5;
    gbc.gridheight = 1;
    gbc.gridwidth = 2;
    gbc.insets = new Insets(0,0,0,0); 

    panel.add(getOutputPanel(),gbc);
 
    return panel;    
  }
  
  private JPanel getNetPanel() {
    JPanel netPanel = new JPanel (new BorderLayout());
    
    netVariableTable = new DataVariableTable();
    
    JScrollPane netScrollPane = new JScrollPane(netVariableTable);
    netScrollPane.setBorder(new EmptyBorder(0,5,5,5));
    
    netPanel.setBorder(new TitledBorder("Net Variables"));
    netPanel.add(netScrollPane, BorderLayout.CENTER);
    
    return netPanel;
  }

  private JPanel getTaskPanel() {
    JPanel taskPanel = new JPanel (new BorderLayout());
    
    taskVariableTable = new DataVariableTable();
    
    JScrollPane taskScrollPane = new JScrollPane(taskVariableTable);
    taskScrollPane.setBorder(new EmptyBorder(0,5,5,5));
    
    taskPanel.setBorder(new TitledBorder("Task Variables"));
    taskPanel.add(taskScrollPane, BorderLayout.CENTER);
    
    return taskPanel;
  }

  
  private JPanel getInputPanel() {
    JPanel panel = new JPanel(new BorderLayout());

    inputPanel = new InputParameterUpdatePanel(this);
    inputPanel.setBorder(new EmptyBorder(0,5,5,5));
    
    panel.setBorder(new TitledBorder("Input Parameters"));
    panel.add(inputPanel, BorderLayout.CENTER);
    
    return panel;
  }

  private JPanel getOutputPanel() {
    JPanel panel = new JPanel(new BorderLayout());

    outputPanel = new OutputParameterUpdatePanel(this);
    outputPanel.setBorder(new EmptyBorder(0,5,5,5));
    
    panel.setBorder(new TitledBorder("Output Parameters"));
    panel.add(outputPanel, BorderLayout.CENTER);
    
    return panel;
  }
  
  public void updateState() {
    netVariableTable.updateState();
    taskVariableTable.updateState();
    inputPanel.updateState();
    outputPanel.updateState();
    pack();
  }
}

class InputParameterUpdatePanel extends ParameterUpdatePanel {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public InputParameterUpdatePanel(UpdateParametersDialog parent) {
    super(parent,ParameterUpdateDialog.NET_TO_TASK);
  }
  
  public JOrderedSingleSelectTable buildTable() {
    return new TaskInputParameterTable();
  }
  
  public int rowLimit() {
    return this.getParameterTable().rowLimit();
  }
}

class OutputParameterUpdatePanel extends ParameterUpdatePanel {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public OutputParameterUpdatePanel(UpdateParametersDialog parent) {
    super(parent,ParameterUpdateDialog.TASK_TO_NET);
  }
  
  public JOrderedSingleSelectTable buildTable() {
    return new TaskOutputParameterTable();
  }

  public int rowLimit() {
    return this.getParameterTable().rowLimit();
  }
}

abstract class ParameterUpdatePanel extends AbstractTableUpdatePanel {
  private UpdateParametersDialog parent;

  protected final ParameterUpdateDialog updateDialog;
  
  public ParameterUpdatePanel(UpdateParametersDialog parent, int transitionType) {
    this.parent = parent;
    updateDialog = new ParameterUpdateDialog(parent, transitionType);
  }
  
  public void setScope(YAWLTask task, NetGraph graph) {
    getParameterTable().setScope(task, graph);
    getParameterTable().selectRow(0);
    updateState();
  }
  
  public void doCreateButtonAction() {
    int selectedRow = getParameterTable().getSelectedRow();
    
    if (selectedRow < 0) {
      selectedRow = 0;
    }

    final int lastRow = getParameterTable().getRowCount();
    
    getParameterTable().insertRow(lastRow);
    getParameterTable().selectRow(lastRow);
    
    updateDialog.setInputVariableScope(
        getParameterTable().getInputVariableScope()
    );
    
    updateDialog.setOutputVariableScope(
        getParameterTable().getOutputVariableScope()
    );
    
    updateDialog.setParameter(
      getParameterTable().getParameterAt(
        getParameterTable().getSelectedRow()
      )
    );
    
    updateDialog.setVisible(true);
    
    if (updateDialog.cancelButtonSelected()) {
      getParameterTable().removeRow(lastRow);
    } else {
      refreshSelectedRow();
    }
    updateState();
    parent.updateState();
  }

  public void doUpdateButtonAction() {
    updateDialog.setInputVariableScope(
        getParameterTable().getInputVariableScope()
    );
    
    updateDialog.setOutputVariableScope(
        getParameterTable().getOutputVariableScope()
    );
    updateDialog.setParameter(
        getParameterTable().getParameterAt(
          getParameterTable().getSelectedRow()
        )
    );
    updateState();
    updateDialog.setVisible(true);
    refreshSelectedRow();
    parent.updateState();
  }

  public void doRemoveButtonAction() {
    final int oldSelectedRow = getTable().getSelectedRow();
    
    int selectedValue = 
     JOptionPane.showConfirmDialog(this,
        "You are about to permanently delete the parameter for " + 
        "variable \"" + getParameterTable().getVariableAt(oldSelectedRow) + "\".\n\n" + 
        "Are you sure you want to delete it?\n", 
        "Deleting Parameter",
        JOptionPane.WARNING_MESSAGE, 
        JOptionPane.YES_NO_OPTION);
    if(selectedValue != JOptionPane.YES_OPTION) {
     return;
    }
    
    getParameterTable().removeRow(oldSelectedRow);
    if (oldSelectedRow == getTable().getRowCount()) {
     getParameterTable().selectRow(oldSelectedRow - 1);
    } else {
     getParameterTable().selectRow(oldSelectedRow);
    }
    updateState();
    parent.updateState();
  }
  

  public TaskParameterTable getParameterTable() {
    return (TaskParameterTable) getTable();
  }

  private void refreshSelectedRow() {
    getParameterTable().updateRow(
      getParameterTable().getSelectedRow()
    );
  }
}

class TaskInputParameterTable extends TaskParameterTable {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public void setScope(YAWLTask task, NetGraph graph) {
    if (task != null) {
      setInputParameterModel(task.getParameterLists().getInputParameters());
      setInputVariableScope(graph.getNetModel().getDecomposition());
      setOutputVariableScope(task.getDecomposition());
    }
  }

  public void setInputParameterModel(ParameterList parameterList) {
    setModel(new TaskInputParameterTableModel(parameterList));
  }

  public TaskInputParameterTableModel getInputParameterModel() {
    return (TaskInputParameterTableModel) getModel();
  }
}

class TaskInputParameterTableModel extends TaskParameterTableModel {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final String[] COLUMN_LABELS = { 
    "XQuery",
    "Task Variable"
  };
  
  public TaskInputParameterTableModel(ParameterList parameterList) {
    this.parameterList = parameterList;
    setOrderedRows(parameterList.getParameters());
  }

  public int getColumnCount() {
    return COLUMN_LABELS.length;
  }

  public String getColumnName(int columnIndex) {
    return COLUMN_LABELS[columnIndex];
  }
}

class TaskOutputParameterTable extends TaskParameterTable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public void setScope(YAWLTask task, NetGraph graph) {
    if (task != null) {
      setOutputParameterModel(task.getParameterLists().getOutputParameters());
      setInputVariableScope(task.getDecomposition());
      setOutputVariableScope(graph.getNetModel().getDecomposition());
    }
  }
  
  public void setOutputParameterModel(ParameterList parameterList) {
    setModel(new TaskOutputParameterTableModel(parameterList));
  }

  public TaskOutputParameterTableModel getOutputParameterModel() {
    return (TaskOutputParameterTableModel) getModel();
  }
}

class TaskOutputParameterTableModel extends TaskParameterTableModel {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final String[] COLUMN_LABELS = { 
    "XQuery",
    "Net Variable"
  };
  
  public TaskOutputParameterTableModel(ParameterList parameterList) {
    super();
    this.parameterList = parameterList;
    setOrderedRows(parameterList.getParameters());
  }

  public int getColumnCount() {
    return COLUMN_LABELS.length;
  }

  public String getColumnName(int columnIndex) {
    return COLUMN_LABELS[columnIndex];
  }
}
