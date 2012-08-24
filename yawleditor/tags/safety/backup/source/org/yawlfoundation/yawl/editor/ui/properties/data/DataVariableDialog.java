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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 2/08/12
 */
public class DataVariableDialog extends JDialog implements ActionListener, TableModelListener {

    private VariableTablePanel netTablePanel;
    private VariableTablePanel taskInputTablePanel;
    private VariableTablePanel taskOutputTablePanel;
    private YNet net;
    private YDecomposition decomposition;          // for task
    private YTask task;
    private YDataHandler dataHandler;

    private JButton btnApply;


    public DataVariableDialog(YNet net) {
        super();
        initialise(net);
        setTitle("Data Variables for Net " + net.getName());
        add(getContentForNetLevel());
        setPreferredSize(new Dimension(420, 280));
        pack();
    }

    public DataVariableDialog(YNet net, YDecomposition decomposition, YAWLTask task) {
        super();
        initialise(net);
        this.decomposition = decomposition;
        this.task = getTask(task.getEngineId());
        setTitle("Data Variables for Task " + decomposition.getName());
        add(getContentForTaskLevel());
        enableDefaultValueEditing();
        setPreferredSize(new Dimension(660, 580));
        pack();
    }


    public VariableTablePanel getNetTablePanel() { return netTablePanel; }


    public void actionPerformed(ActionEvent event) {
        String action = event.getActionCommand();
        if (! action.equals("Cancel")) {
            updateVariables();
            btnApply.setEnabled(false);
        }

        if (! action.equals("Apply")) {
            setVisible(false);
        }
    }


    public void tableChanged(TableModelEvent e) {
        enableApplyButton();
        ((VariableTableModel) e.getSource()).setTableChanged(true);
        if (! (e.getSource() instanceof NetVarTableModel)) enableDefaultValueEditing();
    }


    public void enableApplyButton() { btnApply.setEnabled(true); }


    private YTask getTask(String name) {
        return (YTask) net.getNetElement(name);
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
        taskInputTablePanel = createTablePanel(TableType.TaskInput);
        taskInputTablePanel.setBorder(new TitledBorder("Task Input Variables"));
        setupTableForDropping(getTaskInputTable());

        taskOutputTablePanel = createTablePanel(TableType.TaskOutput);
        taskOutputTablePanel.setBorder(new TitledBorder("Task Output Variables"));
        setupTableForDropping(getTaskOutputTable());

        JPanel taskPanel = new JPanel(new GridLayout(0,2,20,20));
        taskPanel.setBorder(new EmptyBorder(20,0,10,0));
        taskPanel.add(taskInputTablePanel);
        taskPanel.add(taskOutputTablePanel);
        return taskPanel;
    }

    private VariableTablePanel createTablePanel(TableType tableType) {
        String name = tableType == TableType.Net ? net.getName() : task.getName();
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
        panel.add(createButton("OK"));
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

    private VariableTable getTaskInputTable() { return taskInputTablePanel.getTable(); }

    private VariableTable getTaskOutputTable() { return taskOutputTablePanel.getTable(); }


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
        String netName = net.getName();
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
        java.util.List<VariableRow> rows = new ArrayList<VariableRow>();
        for (YParameter parameter : parameters.values()) {
            VariableRow row = new VariableRow(parameter, decomposition.getName());
            String mapping = getMapping(parameter);
            row.setInitialMapping(unwrapMapping(mapping));
            if (row.isOutput()) {
                row.setNetVarForOutputMapping(getNetVarFromMapping(mapping));
            }
            rows.add(row);
        }
        return rows;
    }


    private String getMapping(YParameter parameter) {
        return getMapping(parameter.getPreferredName(), parameter.getParamType());
    }

    private String getMapping(String variableName, int type) {
        return (type == YDataHandler.INPUT) ?
                task.getDataBindingForInputParam(variableName) :
                task.getDataBindingForOutputParam(variableName);
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


    private void handleMappingChange(VariableRow row)
            throws YDataHandlerException {
        String mapping = row.getFullMapping();
        if (mapping == null) return;
        String variableName = row.isInput() ? row.getName() : row.getNetVarForOutputMapping();
        dataHandler.setVariableMapping(net.getID(), task.getID(), variableName,
                mapping, row.getUsage());
    }


    private void handleNewRow(VariableRow row, YDecomposition host)
            throws YDataHandlerException {
        String ns = "http://www.w3.org/2001/XMLSchema";
        dataHandler.addVariable(host.getID(), row.getName(), row.getDataType(),
                    ns, row.getUsage(), row.getValue());
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
            row.setInitialMapping(mapping);
        }
        getTaskInputTable().repaint();
        for (VariableRow row : getTaskOutputTable().getVariables()) {
            String mapping = unwrapMapping(getMapping(row.getName(), YDataHandler.OUTPUT));
            row.setInitialMapping(mapping);
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


