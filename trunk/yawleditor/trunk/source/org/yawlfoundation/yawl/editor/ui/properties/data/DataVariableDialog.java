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
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

/**
 * @author Michael Adams
 * @date 2/08/12
 */
public class DataVariableDialog extends JDialog
        implements ActionListener, TableModelListener {

    private VariableTablePanel netTablePanel;
    private VariableTablePanel taskInputTablePanel;
    private VariableTablePanel taskOutputTablePanel;
    private YNet net;
    private YDecomposition decomposition;          // for task
    private YTask task;
    private YDataHandler dataHandler;
    private OutputBindings outputBindings;
    private MultiInstanceHandler _miHandler;
    private JButton btnOK;
    private JButton btnApply;

    private boolean dirty;
    private boolean isInserting;


    public DataVariableDialog(YNet net) {
        super();
        initialise(net);
        setTitle("Data Variables for Net " + net.getID());
        add(getContentForNetLevel());
        setPreferredSize(new Dimension(620, 280));
        pack();
    }

    public DataVariableDialog(YNet net, YDecomposition decomposition, YAWLTask task) {
        super();
        initialise(net);
        this.decomposition = decomposition;
        this.task = task.getTask();
        outputBindings = new OutputBindings(this.task);
        if (this.task.isMultiInstance()) {
            _miHandler = new MultiInstanceHandler(this.task, outputBindings);
        }
        setTitle("Data Variables for Task " + decomposition.getID());
        add(getContentForTaskLevel());
        enableDefaultValueEditing();
        setPreferredSize(new Dimension(760, 580));
        pack();
    }


    public void actionPerformed(ActionEvent event) {
        String action = event.getActionCommand();
        if (! action.equals("Cancel") && dirty && allRowsValid()) {
            updateVariables();
            dirty = false;
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
        if (! (model instanceof NetVarTableModel)) enableDefaultValueEditing();
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
                (getTaskInputTable() == null || getTaskInputTable().allRowsValid()) &&
                (getTaskOutputTable() == null || getTaskOutputTable().allRowsValid());
    }


    protected void enableApplyButton() {
        dirty = true;
        enableButtonsIfValid();
    }

    // when one task table has changed, refresh the other one
    protected void notifyTableChanged(TableType tableType) {
        VariableTableModel model = null;
        switch (tableType) {
            case TaskInput: model = getTaskOutputTable().getTableModel(); break;
            case TaskOutput: model = getTaskInputTable().getTableModel(); break;
        }
        if (model != null) model.fireTableDataChanged();
    }


    protected VariableTablePanel getNetTablePanel() { return netTablePanel; }

    protected MultiInstanceHandler getMultiInstanceHandler() { return _miHandler; }


    protected String setMultiInstanceRow(VariableRow row) {
        try {
             _miHandler.setupMultiInstanceRow(row, getTaskInputTable(),
                     getTaskOutputTable(), this);
             return null;
        }
        catch (IllegalArgumentException iae) {
            return iae.getMessage();
        }
    }


    protected OutputBindings getOutputBindings() { return outputBindings; }


    protected void updateMappingsOnVarNameChange(VariableRow row, String newName) {
        if (taskInputTablePanel == null) return;   // only net table is showing

        String oldName = row.getName();
        if (oldName.isEmpty() || oldName.equals(newName)) return;

        String id = row.getDecompositionID();
        if (id.equals(net.getID())) {                 // net var name change
            for (VariableRow taskRow : getTaskInputTable().getVariables()) {
                if (taskRow.getMapping().contains(id + "/" + oldName + "/")) {
                    taskRow.setMapping(createMapping(
                            id, newName, taskRow.getDataType()));
                }
            }
            outputBindings.renameNetVarTarget(oldName, newName);
        }
        else if (row.isOutput()) {                 // task output var name change
            String oldBinding = createMapping(row.getDecompositionID(),
                    oldName, row.getDataType());
            String newBinding = createMapping(row.getDecompositionID(),
                    newName, row.getDataType());
            outputBindings.replaceBinding(oldName, oldBinding, newBinding);
            outputBindings.renameExternalTarget(oldName, newName);
        }
    }


    private String createMapping(String netID, String varName, String dataType) {
        StringBuilder s = new StringBuilder("/");
        s.append(netID)
         .append("/")
         .append(varName)
         .append("/")
         .append(SpecificationModel.getHandler().getDataHandler().getXQuerySuffix(
                 dataType));
        return s.toString();
    }


    private void initialise(YNet net) {
        this.net = net;
        dataHandler = SpecificationModel.getHandler().getDataHandler();
        setModal(true);
        setResizable(true);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationByPlatform(true);
    }


    private JPanel getContentForNetLevel() {
        createNetTablePanel();
        JPanel content = new JPanel();
        content.add(netTablePanel);
        content.add(createButtonBar());
        return content;
    }

    private JPanel getContentForTaskLevel() {
        createNetTablePanel();
        netTablePanel.setBorder(new TitledBorder("Net Variables"));
        getNetTable().setDragEnabled(true);
        getNetTable().setTransferHandler(
                new VariableRowTransferHandler(getNetTable(), outputBindings));

        JPanel content = new JPanel();
        content.setBorder(new EmptyBorder(10, 10, 10, 10));
        content.add(netTablePanel);
        content.add(createTaskPanel());
        content.add(createButtonBar());
        return content;
    }

    private void createNetTablePanel() {
        netTablePanel = createTablePanel(TableType.Net);
        getNetTable().getModel().addTableModelListener(this);
    }

    private JPanel createTaskPanel() {
        taskInputTablePanel = createTaskTablePanel(TableType.TaskInput);
        taskOutputTablePanel = createTaskTablePanel(TableType.TaskOutput);
        JPanel taskPanel = new JPanel(new GridLayout(0,2,20,20));
        taskPanel.setBorder(new EmptyBorder(20,0,10,0));
        taskPanel.add(taskInputTablePanel);
        taskPanel.add(taskOutputTablePanel);
        return taskPanel;
    }


    private VariableTablePanel createTaskTablePanel(TableType tableType) {
        VariableTablePanel taskPanel = createTablePanel(tableType);
        taskPanel.setBorder(new TitledBorder(tableType.getName() + " Variables"));
        setupTableForDropping(taskPanel.getTable());
        taskPanel.showMIButton(tableType == TableType.TaskInput && task.isMultiInstance());
        return taskPanel;
    }


    private VariableTablePanel createTablePanel(TableType tableType) {
        String name = tableType == TableType.Net ? net.getID() : task.getName();
        return new VariableTablePanel(createTableRows(tableType), tableType, name, this);
    }

    private void setupTableForDropping(VariableTable table) {
        table.getModel().addTableModelListener(this);
        table.setDropMode(DropMode.INSERT_ROWS);
        table.setTransferHandler(
                new VariableRowTransferHandler(table, outputBindings));
    }

    private JPanel createButtonBar() {
        JPanel panel = new JPanel(new GridLayout(0,3,5,5));
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
        button.setActionCommand(label);
        button.setMnemonic(label.charAt(0));
        button.addActionListener(this);
        return button;
    }


    private VariableTable getNetTable() { return netTablePanel.getTable(); }

    protected VariableTable getTaskInputTable() {
        return taskInputTablePanel != null ? taskInputTablePanel.getTable() : null;
    }

    protected VariableTable getTaskOutputTable() {
        return taskOutputTablePanel != null ? taskOutputTablePanel.getTable() : null; }


    private VariableTablePanel getTablePanelFromTableModel(VariableTableModel model) {
        if (model instanceof NetVarTableModel) return netTablePanel;
        if (model instanceof TaskInputVarTableModel) return taskInputTablePanel;
        if (model instanceof TaskOutputVarTableModel) return taskOutputTablePanel;
        return null;
    }


    private java.util.List<VariableRow> createTableRows(TableType tableType) {
        switch (tableType) {
            case Net: return createTableRows(net);
            case TaskInput: return createTableRows(decomposition.getInputParameters());
            case TaskOutput: return createTableRows(decomposition.getOutputParameters());
            default: return null;   // should never happen
        }
    }

    // for net
    private java.util.List<VariableRow> createTableRows(YNet net) {
        String netName = net.getID();
        Set<String> ioNames = new HashSet<String>();
        java.util.List<VariableRow> rows = new ArrayList<VariableRow>();

        // join inputs & outputs, then add them, and add input-only parameters too
        for (String name : net.getInputParameterNames()) {
            YParameter input = net.getInputParameters().get(name);
            if (net.getOutputParameterNames().contains(name)) {
                YParameter output = net.getOutputParameters().get(name);
                if (input.getDataTypeName().equals(output.getDataTypeName())) {
                    ioNames.add(name);
                }
            }
            rows.add(new VariableRow(input, ioNames.contains(name), netName));
        }

        // add output only
        Set<String> dummyLocalNames = new HashSet<String>();
        for (String name : net.getOutputParameterNames()) {
            if (! ioNames.contains(name)) {
                rows.add(new VariableRow(net.getOutputParameters().get(name), netName));
                dummyLocalNames.add(name);
            }
        }

        // add locals that weren't created to 'shadow' output-only parameters
        for (YVariable variable : net.getLocalVariables().values()) {
            if (! dummyLocalNames.contains(variable.getName())) {
                rows.add(new VariableRow(variable, netName));
            }
        }

        return rows;
    }


    // task variables
    private java.util.List<VariableRow> createTableRows(Map<String, YParameter> parameters) {
        java.util.List<VariableRow> rows = new ArrayList<VariableRow>();
        for (YParameter parameter : parameters.values()) {
            VariableRow row = new VariableRow(parameter, decomposition.getID());
            initMappings(row, getMapping(parameter));
            rows.add(row);
        }
        return rows;
    }


    private void initMappings(VariableRow row, String mapping) {
        if (_miHandler != null) {
            if (row.isInput()) {
                String miParam = _miHandler.getFormalInputParam();
                if (miParam != null && row.getName().equals(miParam)) {
                    row.setMultiInstance(true);
                    row.initMapping(mapping);
                }
            }
            else {    // output
                String target = _miHandler.getOutputTarget();
                if (target != null &&
                        outputBindings.getTarget(row.getName()).equals(target)) {
                    row.setMultiInstance(true);
                }
            }
        }
        else {
            row.initMapping(unwrapMapping(mapping));
        }
    }


    private String getMapping(YParameter parameter) {
        return getMapping(parameter.getPreferredName(), parameter.getParamType());
    }

    private String getMapping(String variableName, int type) {
        return (type == YDataHandler.INPUT) ?
                task.getDataBindingForInputParam(variableName) :
                outputBindings.getBinding(variableName);
    }


    private String unwrapMapping(String mapping) {
        if (mapping != null) {

            // remove outer {}'s, if any
            if (mapping.trim().startsWith("{")) {
                mapping = mapping.substring(mapping.indexOf('{') + 1,
                        mapping.lastIndexOf('}'));
            }

            // remove outer (param name) tags
            if (mapping.trim().startsWith("<")) {
                mapping = StringUtil.unwrap(mapping);
            }
        }
        return mapping;
    }


    private void enableDefaultValueEditing() {
        boolean modified = false;
        for (VariableRow outputRow : getTaskOutputTable().getVariables()) {
            boolean match = false;
            for (VariableRow inputRow : getTaskInputTable().getVariables()) {
                if (inputRow.equalsNameAndType(outputRow)) {
                    match = true;
                    break;
                }
            }
            outputRow.setOutputOnlyTask(! match);
            modified = modified || match;
        }
        if (modified) getTaskOutputTable().repaint();
    }


    private void updateVariables() {
        try {
            updateVariables(getNetTable(), net);
            updateVariables(getTaskInputTable(), decomposition);
            updateVariables(getTaskOutputTable(), decomposition);
            if (outputBindings != null) outputBindings.commit();
            if (_miHandler != null) _miHandler.commit();
            dirty = false;
        }
        catch (YDataHandlerException ydhe) {
            JOptionPane.showMessageDialog(this, ydhe.getMessage(),
                    "Failed to update data", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void updateVariables(VariableTable table, YDecomposition host)
            throws YDataHandlerException {
        if (table == null || ! table.isChanged()) return;

        for (VariableRow row : table.getRemovedVariables()) {
            removeVariable(row, host);
        }

        for (VariableRow row : table.getVariables()) {
            if (row.isModified()) {
                if (row.isNameChange()) {
                    handleNameChange(row, host);
                }
                if (row.isUsageChange()) {
                    handleUsageChange(row, host);     // only net tables can change usage
                }
                if (row.isDataTypeChange()) {
                    handleDataTypeChange(row, host);
                }
                if (row.isValueChange()) {
                    handleValueChange(row, host);
                }
                if (isTaskTable(table) && row.isMappingChange()) {
                    handleMappingChange(row);         // only task tables have mappings
                }
                if (isTaskTable(table) && row.isAttributeChange()) {
                    handleAttributeChange(row, host);
                }
            }
            else if (row.isNew()) {
                handleNewRow(row, host);
                if (isTaskTable(table)) handleMappingChange(row);
            }
        }

        if (table.hasChangedRowOrder()) {
            updateVariableIndex(table, host);
        }

        table.updatesApplied();
    }


    private boolean isTaskTable(VariableTable table) {
        return ! (table.getTableModel() instanceof NetVarTableModel);
    }


    private boolean handleUsageChange(VariableRow row, YDecomposition host)
            throws YDataHandlerException {
        return dataHandler.changeVariableScope(host.getID(), row.getName(),
                row.getStartingUsage(), row.getUsage());
    }


    private void handleNameChange(VariableRow row, YDecomposition host)
            throws YDataHandlerException {
        if (row.isLocal()) {
            dataHandler.renameVariable(host.getID(), row.getStartingName(),
                    row.getName(), YDataHandler.LOCAL);
        }
        else {
            if (row.isInput() || row.isInputOutput()) {
                dataHandler.renameVariable(host.getID(), row.getStartingName(),
                        row.getName(), YDataHandler.INPUT);
            }
            if (row.isOutput() || row.isInputOutput()) {
                dataHandler.renameVariable(host.getID(), row.getStartingName(),
                        row.getName(), YDataHandler.OUTPUT);
            }
        }
        if (host instanceof YNet) refreshTaskMappings();
    }


    private void handleDataTypeChange(VariableRow row, YDecomposition host)
            throws YDataHandlerException {
        dataHandler.setVariableDataType(host.getID(), row.getName(), row.getDataType(),
                row.getUsage());
    }


    private void handleValueChange(VariableRow row, YDecomposition host)
            throws YDataHandlerException {
        if (row.isLocal()) {
            dataHandler.setInitialValue(host.getID(), row.getName(), row.getValue());
        }
        else if (row.isOutput()) {
            dataHandler.setDefaultValue(host.getID(), row.getName(), row.getValue());
        }
    }


    private void handleMappingChange(VariableRow row) throws YDataHandlerException {
        String mapping = row.getFullMapping();
        if (mapping == null) return;
        String variableName = row.getName();
        dataHandler.setVariableMapping(net.getID(), task.getID(), variableName,
                mapping, row.getUsage());
    }


    private void handleAttributeChange(VariableRow row, YDecomposition host)
            throws YDataHandlerException {
        dataHandler.setVariableAttributes(host.getID(), row.getName(),
                row.getAttributes(), row.getUsage());
    }

    private void handleNewRow(VariableRow row, YDecomposition host)
            throws YDataHandlerException {
        String ns = "http://www.w3.org/2001/XMLSchema";
        dataHandler.addVariable(host.getID(), row.getName(), row.getDataType(),
                    ns, row.getUsage(), row.getValue(), row.getAttributes());
    }


    private void removeVariable(VariableRow row, YDecomposition host)
            throws YDataHandlerException {
        String name = row.getStartingName();
        if (name == null) return;                 // added and removed without updating

        if (row.isLocal()) {
            dataHandler.removeVariable(host.getID(), name, YDataHandler.LOCAL);
        }
        if (row.isInput() || row.isInputOutput()) {
            dataHandler.removeVariable(host.getID(), name, YDataHandler.INPUT);
        }
        if (row.isOutput() || row.isInputOutput()) {
            dataHandler.removeVariable(host.getID(), name, YDataHandler.OUTPUT);
        }
        if (host instanceof YNet) refreshTaskMappings();
    }


    private void refreshTaskMappings() {
        if (getTaskInputTable() == null) return;            // showing netvars only
        for (VariableRow row : getTaskInputTable().getVariables()) {
            String mapping = unwrapMapping(getMapping(row.getName(), YDataHandler.INPUT));
            row.initMapping(mapping);
        }
        getTaskInputTable().repaint();
        getTaskOutputTable().repaint();
    }


    private void updateVariableIndex(VariableTable table, YDecomposition host)
            throws YDataHandlerException {
        int index = 0;
        for (VariableRow row : table.getVariables()) {
            if (row.isLocal()) {
                dataHandler.setVariableIndex(host.getID(), row.getName(),
                        YDataHandler.LOCAL, index);
            }
            else {
                if (row.isInput() || row.isInputOutput()) {
                    dataHandler.setVariableIndex(host.getID(), row.getName(),
                            YDataHandler.INPUT, index);
                }
                if (row.isOutput() || row.isInputOutput()) {
                    dataHandler.setVariableIndex(host.getID(), row.getName(),
                            YDataHandler.OUTPUT, index);
                }
            }
            index++;
        }
    }

}


