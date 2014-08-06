/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.editor.ui.properties.data;

import org.yawlfoundation.yawl.editor.core.data.YDataHandler;
import org.yawlfoundation.yawl.editor.core.data.YDataHandlerException;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.properties.data.binding.OutputBindings;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.elements.YCompositeTask;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.data.YParameter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 2/08/12
 */
public class DataVariableDialog extends JDialog
        implements ActionListener, TableModelListener {

    private VariableTablePanel netTablePanel;
    private VariableTablePanel taskTablePanel;
    private YNet net;
    private YDecomposition decomposition;          // for task
    private YTask task;
    private DataUpdater dataUpdater;

    private OutputBindings outputBindings;
    private MultiInstanceHandler _miHandler;
    private JButton btnOK;
    private JButton btnApply;

    private boolean dirty;
    private boolean isInserting;


    public DataVariableDialog(YNet net) {
        super();
        initialise(net, null, null);
        add(getContentForNetLevel());
        setPreferredSize(new Dimension(620, 290));
        setMinimumSize(new Dimension(400, 200));
        pack();
    }

    public DataVariableDialog(YNet net, YDecomposition decomposition, YAWLTask task) {
        super();
        initialise(net, decomposition, task);
        add(getContentForTaskLevel());
        setPreferredSize(new Dimension(760, 580));
        setMinimumSize(new Dimension(400, 420));
        pack();
    }


    public void actionPerformed(ActionEvent event) {
        String action = event.getActionCommand();
        if (! action.equals("Cancel") && dirty && allRowsValid()) {
            stopCellEditing();
            if (! updateVariables()) return;                       // abort on error
            btnApply.setEnabled(false);
            SpecificationUndoManager.getInstance().setDirty(true);
        }

        if (! action.equals("Apply")) {
            setVisible(false);
        }
    }


    public void tableChanged(TableModelEvent e) {
        dirty = true;
        isInserting = (e.getType() == TableModelEvent.INSERT);
        enableButtonsIfValid();
        VariableTableModel model = (VariableTableModel) e.getSource();
        model.setTableChanged(true);
        getTablePanelFromTableModel(model).enableButtons(true);
    }


    protected void enableButtonsIfValid() {
        boolean allRowsValid = !isInserting && allRowsValid();
        btnApply.setEnabled(allRowsValid && dirty);
        btnOK.setEnabled(allRowsValid);
    }

    protected void setInserting(boolean inserting) {
        isInserting = inserting;
    }

    protected boolean allRowsValid() {
        return getNetTable().allRowsValid() &&
                (getTaskTable() == null || getTaskTable().allRowsValid());
    }


    protected void enableApplyButton() {
        dirty = true;
        enableButtonsIfValid();
    }

    protected void stopCellEditing() {
        stopCellEditing(getNetTable());
        stopCellEditing(getTaskTable());
    }

    protected void stopCellEditing(JTable table) {
        if (table != null && table.isEditing()) table.getCellEditor().stopCellEditing();
    }


    protected VariableTablePanel getNetTablePanel() { return netTablePanel; }

    public MultiInstanceHandler getMultiInstanceHandler() { return _miHandler; }


    protected String setMultiInstanceRow(VariableRow row) {
        try {
             _miHandler.setupMultiInstanceRow(row, getNetTable(), getTaskTable());
             return null;
        }
        catch (IllegalArgumentException iae) {
            return iae.getMessage();
        }
    }


    public OutputBindings getOutputBindings() { return outputBindings; }


    protected void updateMappingsOnVarNameChange(VariableRow row, String newName) {
        if (taskTablePanel == null) return;   // only net table is showing

        String oldName = row.getName();
        if (oldName.isEmpty() || oldName.equals(newName)) return;

        String id = row.getElementID();
        if (id.equals(net.getID())) {                 // net var name change
            for (VariableRow taskRow : getTaskTable().getVariables()) {
                if (taskRow.getMapping().contains(id + "/" + oldName + "/")) {
                    taskRow.setMapping(createMapping(
                            id, newName, taskRow.getDataType()));
                }
            }
            outputBindings.renameNetVarTarget(oldName, newName);
        }
        else if (row.isOutput() || row.isInputOutput()) { // task output var name change
            String oldBinding = createMapping(row.getElementID(),
                    oldName, row.getDataType());
            String newBinding = createMapping(row.getElementID(),
                    newName, row.getDataType());
            outputBindings.replaceBinding(oldName, oldBinding, newBinding);
            outputBindings.renameExternalTarget(oldName, newName);

            if (row.isMultiInstance()) {
                _miHandler.renameItem(row, newName);
            }
        }
    }


    protected boolean createAutoBinding(VariableRow row) {
        return createBinding(row, row.getUsage());
    }


    protected boolean createBinding(VariableRow row, int usage) {
        if (hasMatchingNetVar(row)) {
            if ((usage == YDataHandler.INPUT || usage == YDataHandler.INPUT_OUTPUT)
                    && row.getMapping() == null) {
                row.setMapping(createMapping(net.getID(), row.getName(),
                       row.getDataType()));
            }
            if ((usage == YDataHandler.OUTPUT || usage == YDataHandler.INPUT_OUTPUT)
                    && outputBindings.getBinding(row.getName()) == null) {
                outputBindings.setBinding(row.getName(),
                        createMapping(task.getID(), row.getName(), row.getDataType()));
            }
            return true;
        }
        return false;
    }


    private boolean hasMatchingNetVar(VariableRow taskRow) {
        return getMatchingRow(taskRow, getNetTable()) != null;
    }


    private VariableRow getMatchingRow(VariableRow row, VariableTable table) {
        for (VariableRow otherRow : table.getVariables()) {
            if (otherRow.getName().equals(row.getName()) &&
                    otherRow.getDataType().equals(row.getDataType())) {
                return otherRow;
            }
        }
        return null;
    }


    private String createMapping(String decompositionID, String varName, String dataType) {
        StringBuilder s = new StringBuilder("/");
        s.append(decompositionID)
         .append("/")
         .append(varName)
         .append("/")
         .append(getXQuerySuffix(dataType));
        return s.toString();
    }


    private String getXQuerySuffix(String dataType) {
        try {
            return SpecificationModel.getHandler().getDataHandler().getXQuerySuffix(
                             dataType);
        }
        catch (YDataHandlerException ydhe) {
            return "";
        }
    }


    private void initialise(YNet net, YDecomposition decomposition, YAWLTask task) {
        setModal(true);
        setResizable(true);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationByPlatform(true);
        this.net = net;
        String title;
        if (! (decomposition == null || task == null)) {
            title = " Decomposition " + decomposition.getID() +
                             " [Task: " + task.getID() + "]";
            this.decomposition = decomposition;
            this.task = task.getTask();                         // YTask from YAWLTask
            outputBindings = new OutputBindings(this.task);
            if (this.task.isMultiInstance()) {
                _miHandler = new MultiInstanceHandler(this.task, outputBindings);
            }
        }
        else {
            title = " Net " + net.getID();
        }
        setTitle("Data Variables for " + title);

        // task and outputBindings will be null for net-level dialog
        dataUpdater = new DataUpdater(net.getID(), this.task, outputBindings);
    }


    private JPanel getContentForNetLevel() {
        createNetTablePanel();
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(0, 0, 10, 0));
        content.add(netTablePanel, BorderLayout.CENTER);
        content.add(createButtonBar(), BorderLayout.SOUTH);
        return content;
    }

    private JPanel getContentForTaskLevel() {
        createNetTablePanel();
        netTablePanel.setBorder(new TitledBorder("Net Variables"));
        createTaskPanel();

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(10, 10, 10, 10));
        JPanel subContent = new JPanel(new GridLayout(0, 1, 10, 10));
        subContent.add(netTablePanel);
        subContent.add(taskTablePanel);
        content.add(subContent, BorderLayout.CENTER);
        content.add(createButtonBar(), BorderLayout.SOUTH);
        return content;
    }

    private void createNetTablePanel() {
        netTablePanel = createTablePanel(TableType.Net);
        getNetTable().getModel().addTableModelListener(this);
        getNetTable().setDragEnabled(true);
        getNetTable().setTransferHandler(
                new VariableRowTransferHandler(getNetTable(), outputBindings));
    }

    private void createTaskPanel() {
        taskTablePanel = createTaskTablePanel(TableType.Task);
        getTaskTable().getModel().addTableModelListener(this);
    }


    private VariableTablePanel createTaskTablePanel(TableType tableType) {
        VariableTablePanel taskPanel = createTablePanel(tableType);
        taskPanel.setBorder(new TitledBorder(tableType.getName() + " Variables"));
        setupTableForDropping(taskPanel.getTable());
        taskPanel.showMIButton(tableType == TableType.Task && task.isMultiInstance());
        return taskPanel;
    }


    private VariableTablePanel createTablePanel(TableType tableType) {
        String name = tableType == TableType.Net ? net.getID() : task.getID();
        return new VariableTablePanel(createTableRows(tableType), tableType, name, this);
    }


    private void setupTableForDropping(VariableTable table) {
        table.setDropMode(DropMode.INSERT_ROWS);
        table.setTransferHandler(new VariableRowTransferHandler(table, outputBindings));
    }


    private JPanel createButtonBar() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10,0,0,0));
        panel.add(createButton("Cancel"));
        btnApply = createButton("Apply");
        btnApply.setEnabled(false);
        panel.add(btnApply);
        btnOK = createButton("OK");
        panel.add(btnOK);
        return panel;
    }


    private JButton createButton(String label) {
        JButton button = new JButton(label);
        button.setPreferredSize(new Dimension(70,25));
        button.setActionCommand(label);
        button.setMnemonic(label.charAt(0));
        button.addActionListener(this);
        return button;
    }


    private VariableTable getNetTable() { return netTablePanel.getTable(); }


    public VariableTable getTaskTable() {
        return taskTablePanel != null ? taskTablePanel.getTable() : null;
    }


    public YTask getTask() { return task; }


    public boolean isCompositeTask() {
        return task instanceof YCompositeTask;
    }


    private VariableTablePanel getTablePanelFromTableModel(VariableTableModel model) {
        if (model instanceof TaskVarTableModel) return taskTablePanel;
        if (model instanceof NetVarTableModel) return netTablePanel;
        return null;
    }


    private java.util.List<VariableRow> createTableRows(TableType tableType) {
        TableRowFactory rowFactory = new TableRowFactory();
        if (tableType == TableType.Net) {
            return rowFactory.createRows(net, null);
        }
        else {
            java.util.List<VariableRow> rows = rowFactory.createRows(decomposition, task);
            initMappings(rows);
            return rows;
        }
    }


    private void initMappings(java.util.List<VariableRow> rows) {
        for (VariableRow row : rows) {
            if (row.isInput() || row.isInputOutput()) {
                initMapping(row, YDataHandler.INPUT);
            }
        }
    }


    private void initMapping(VariableRow row, int type) {
        Map<String, YParameter> parameterMap = type == YDataHandler.INPUT ?
                decomposition.getInputParameters() : decomposition.getOutputParameters();
        YParameter parameter = parameterMap.get(row.getName());
        if (parameter != null) {
            initMapping(row, getMapping(parameter));
        }
    }

    private void initMapping(VariableRow row, String mapping) {
        if (_miHandler != null) {
            if (row.isOutput() || row.isInputOutput()) {
                if (_miHandler.outputQueryBindsFrom(row.getName())) {
                    row.setMultiInstance(true);
                }
            }
            if (row.isInput() || row.isInputOutput()) {
                String miParam = _miHandler.getFormalInputParam();
                if (miParam != null && row.getName().equals(miParam)) {
                    row.setMultiInstance(true);
                    row.initMapping(mapping);
                    return;                           // init'ed and done
                }
            }
        }
        row.initMapping(DataUtils.unwrapBinding(mapping));
    }


    private String getMapping(YParameter parameter) {
        return getMapping(parameter.getPreferredName(), parameter.getParamType());
    }


    private String getMapping(String variableName, int type) {
        return (type == YDataHandler.INPUT) ?
                task.getDataBindingForInputParam(variableName) :
                outputBindings.getBinding(variableName);
    }


    private boolean updateVariables() {
        try {
            dataUpdater.update(getNetTable(), net);
            dataUpdater.update(getTaskTable(), decomposition);
            if (_miHandler != null) _miHandler.commit();
            if (outputBindings != null) outputBindings.commit();
            dirty = false;
            return true;
        }
        catch (YDataHandlerException ydhe) {
            JOptionPane.showMessageDialog(this, ydhe.getMessage(),
                    "Failed to update data", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

}


