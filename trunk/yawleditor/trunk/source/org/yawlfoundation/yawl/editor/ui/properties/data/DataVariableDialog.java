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
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
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
    private Map<String, String> outputVariableMap;
    private JButton btnOK;
    private JButton btnApply;

    private boolean dirty;
    private boolean isInserting;


    public DataVariableDialog(YNet net) {
        super();
        initialise(net);
        setTitle("Data Variables for Net " + net.getID());
        add(getContentForNetLevel());
        setPreferredSize(new Dimension(420, 280));
        pack();
    }

    public DataVariableDialog(YNet net, YDecomposition decomposition, YAWLTask task) {
        super();
        initialise(net);
        this.decomposition = decomposition;
        this.task = task.getTask();
        setTitle("Data Variables for Task " + decomposition.getID());
        add(getContentForTaskLevel());
        enableDefaultValueEditing();
        setPreferredSize(new Dimension(660, 580));
        pack();
    }


    public void actionPerformed(ActionEvent event) {
        String action = event.getActionCommand();
        if (! action.equals("Cancel") && dirty && allRowsValid()) {
            updateVariables();
            dirty = false;
            btnApply.setEnabled(false);
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

    protected VariableTablePanel getNetTablePanel() { return netTablePanel; }


    protected boolean setMultiInstanceRow(VariableRow row) {
        if (row.isMultiInstance()) return true;        // already multi instance

        String itemType = dataHandler.getMultiInstanceItemType(row.getDataType());
        if (itemType == null) return false;            // invalid mi type

        VariableTable table = row.isInput() ? getTaskInputTable() : getTaskOutputTable();
        table.setMultiInstanceRow();
        row.setName(row.getName() + "_Item");
        row.setDataType(itemType);

        String mapping = row.getMapping();
        if (mapping != null) {
            String workedMapping = mapping.substring(0, mapping.lastIndexOf('/'));
            row.setMapping(workedMapping);
            if (row.isInput()) {
                row.setMIQuery("for $s in " + workedMapping + "/* return $s");
            }
            else if (row.isOutput()) {
                row.setMIQuery("for $j in " + workedMapping + " return $j");
            }
        }
        return true;
    }


    protected YParameter getParameter(String name, int usage) {
        if (usage == YDataHandler.INPUT || usage == YDataHandler.INPUT_OUTPUT) {
            return decomposition.getInputParameters().get(name);
        }
        else {
            return decomposition.getOutputParameters().get(name);
        }
    }


    protected void updateMappingsOnVarNameChange(VariableRow row, String newName) {
        if (taskInputTablePanel == null) return;   // only net table is showing

        String oldName = row.getName();
        if (oldName.isEmpty() || oldName.equals(newName)) return;

        String id = row.getDecompositionID();
        if (id.equals(net.getID())) {
            for (VariableRow taskRow : getTaskInputTable().getVariables()) {
                if (taskRow.getMapping().contains(id + "/" + oldName + "/")) {
                    taskRow.setMapping(createMapping(
                            id, newName, taskRow.getDataType()));
                }
            }
            for (VariableRow taskRow : getTaskOutputTable().getVariables()) {
                if (taskRow.getNetVarForOutputMapping().equals(oldName)) {
                    taskRow.setNetVarForOutputMapping(newName);
                }
            }
        }
        else if (row.isOutput()) {                 // task output var name change
            row.setMapping(createMapping(
                        row.getDecompositionID(), newName, row.getDataType()));
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
        setResizable(false);
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
        createOutputVariableMap();
        createNetTablePanel();
        netTablePanel.setBorder(new TitledBorder("Net Variables"));
        getNetTable().setDragEnabled(true);
        getNetTable().setTransferHandler(new VariableRowTransferHandler(getNetTable()));

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
        taskPanel.showMIButton(task.isMultiInstance());
        return taskPanel;
    }


    private VariableTablePanel createTablePanel(TableType tableType) {
        String name = tableType == TableType.Net ? net.getID() : task.getName();
        return new VariableTablePanel(createTableRows(tableType), tableType, name, this);
    }

    private void setupTableForDropping(VariableTable table) {
        table.getModel().addTableModelListener(this);
        table.setDropMode(DropMode.INSERT_ROWS);
        table.setTransferHandler(new VariableRowTransferHandler(table));
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

    private VariableTable getTaskInputTable() {
        return taskInputTablePanel != null ? taskInputTablePanel.getTable() : null;
    }

    private VariableTable getTaskOutputTable() {
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
        for (YVariable variable : net.getLocalVariables().values()) {
            rows.add(new VariableRow(variable, netName));
        }

        // join inputs & outputs
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

        for (String name : net.getOutputParameterNames()) {
            if (! ioNames.contains(name)) {
                rows.add(new VariableRow(net.getOutputParameters().get(name), netName));
            }
        }

        return rows;
    }

    // task variables
    private java.util.List<VariableRow> createTableRows(Map<String, YParameter> parameters) {
        String miParam = getMIParamName();
        java.util.List<VariableRow> rows = new ArrayList<VariableRow>();
        for (YParameter parameter : parameters.values()) {
            VariableRow row = new VariableRow(parameter, decomposition.getID());
            initMappings(row, getMapping(parameter), miParam);
            rows.add(row);
        }
        return rows;
    }


    private void initMappings(VariableRow row, String mapping, String miParam) {
        if (miParam != null && row.getName().equals(miParam)) {
            row.setMultiInstance(true);
            row.initMIQuery(getMIQueryForScope(row.isInput()));
            row.initMapping(row.isInput() ? mapping : unwrapMapping(mapping));
            if (row.isOutput()) {
                row.setNetVarForOutputMapping(
                        task.getMIOutputAssignmentVar(
                        task.getMultiInstanceAttributes().getMIFormalOutputQuery()));
            }
        }
        else {
            row.initMapping(unwrapMapping(mapping));
            if (row.isOutput()) {
                row.setNetVarForOutputMapping(outputVariableMap.get(row.getName()));
            }
        }
    }

    private String getMIParamName() {
        return task.isMultiInstance() ?
                task.getMultiInstanceAttributes().getMIFormalInputParam() : null;
    }


    private String getMIQueryForScope(boolean isInput) {
        if (! task.isMultiInstance()) return null;
        return isInput ?
                task.getMultiInstanceAttributes().getMISplittingQuery() :
                unwrapMapping(task.getMultiInstanceAttributes().getMIJoiningQuery());
    }


    private String getMapping(YParameter parameter) {
        return getMapping(parameter.getPreferredName(), parameter.getParamType());
    }

    private String getMapping(String variableName, int type) {
        return (type == YDataHandler.INPUT) ?
                task.getDataBindingForInputParam(variableName) :
                task.getDataBindingForOutputParam(outputVariableMap.get(variableName));
    }


    private String unwrapMapping(String mapping) {
        if (mapping != null) {
            if (mapping.contains("{")) {
                return mapping.substring(mapping.indexOf('{') + 1, mapping.lastIndexOf('}'));
            }
            if (mapping.contains("<")) {
                return StringUtil.unwrap(mapping);
            }
        }
        return mapping;
    }


    private String getNetVarFromMapping(String mapping) {
        if (mapping == null) return null;
        int openIndex = mapping.indexOf('<');
        return openIndex > -1 ? mapping.substring(openIndex + 1, mapping.indexOf('>'))
                : null;
    }

    // [outparam name, net var it outputs to]
    private void createOutputVariableMap() {
        outputVariableMap = new Hashtable<String, String>();
        Set<String> paramNames = decomposition.getOutputParameterNames();
        String decompKey = '/' + decomposition.getID() + '/';
        for (String outputQuery : task.getDataMappingsForTaskCompletion().keySet()) {
            for (String paramName : paramNames) {
                if (outputQuery.contains(decompKey + paramName + '/')) {
                    outputVariableMap.put(paramName,
                            task.getDataMappingsForTaskCompletion().get(outputQuery));
                }
            }
        }
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


    private YDecomposition getDecomposition(TableType tableType) {
        return tableType == TableType.Net ? net : decomposition;
    }


    private void updateVariables() {
        try {
            updateVariables(getNetTable(), net);
            updateVariables(getTaskInputTable(), decomposition);
            updateVariables(getTaskOutputTable(), decomposition);
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
                if (isTaskTable(table) && row.isMultiInstance() && row.isMIQueryChange()) {
                    handleMIQueryChange(row);         // only task tables have mappings
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
        String variableName = row.isInput() ? row.getName() : row.getNetVarForOutputMapping();
        dataHandler.setVariableMapping(net.getID(), task.getID(), variableName,
                mapping, row.getUsage());
    }


    private void handleMIQueryChange(VariableRow row) throws YDataHandlerException {
        String miQuery = row.getFullMIQuery();
        if (miQuery == null) return;
        String variableName = row.isInput() ? row.getName() : row.getNetVarForOutputMapping();
        dataHandler.setMIQuery(net.getID(), task.getID(), variableName,
                miQuery, row.getUsage());
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
        for (VariableRow row : getTaskOutputTable().getVariables()) {
            String mapping = unwrapMapping(getMapping(row.getName(), YDataHandler.OUTPUT));
            row.initMapping(mapping);
            row.setNetVarForOutputMapping(getNetVarFromMapping(mapping));
        }
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


