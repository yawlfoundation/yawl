package org.yawlfoundation.yawl.editor.ui.properties.data;

import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.data.YVariable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Michael Adams
 * @date 2/08/12
 */
public class DataVariableDialog extends JDialog implements ActionListener, TableModelListener {

    private VariableTablePanel tablePanel;
    private YNet net;
    private String returnValue;

    private JButton btnApply;


    public DataVariableDialog(YNet net, Component parent) {
        super();
        this.net = net;
        setModal(true);
        setTitle("Data Variables for Net " + net.getName());
        setResizable(false);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        add(getContent());
        setLocationRelativeTo(parent);
        setPreferredSize(new Dimension(420, 280));
        pack();
    }


    public String showDialog() {
        setVisible(true);
        return returnValue;
    }


    public void actionPerformed(ActionEvent event) {
        String action = event.getActionCommand();
        if (! action.equals("Cancel")) {
            updateVariables();
            returnValue = "Yes";
            btnApply.setEnabled(false);
        }
        else returnValue = "No";

        if (! action.equals("Apply")) {
            setVisible(false);
        }
    }


    public void tableChanged(TableModelEvent e) {
         btnApply.setEnabled(true);
    }


    private JPanel getContent() {
        tablePanel = new VariableTablePanel(net);
        getTable().getModel().addTableModelListener(this);

        JPanel content = new JPanel();
        content.add(tablePanel);
        content.add(createButtonBar());
        return content;
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


    private NetVarTable getTable() { return tablePanel.getTable(); }


    private void updateVariables() {
        for (NetVariableRow row : getTable().getVariables()) {
            if (row.isModified()) {
                if (row.isUsageChange()) {
                    handleUsageChange(row);
                }
                else if (row.isNameChange() || row.isDataTypeChange()) {
                    handleNameChange(row);
                }
                else if (row.isValueChange()) {
                    handleValueChange(row);
                }
            }
            else if (row.isNew()) {
                handleNewRow(row);
            }
        }

        for (NetVariableRow row : getTable().getRemovedVariables()) {
            removeVariable(row);
        }

        if (getTable().hasChangedRowOrder()) {
            setVariableOrdering();
        }
    }


    private void handleUsageChange(NetVariableRow row) {
        switch (row.getStartingUsage()) {
            case Local : handleUsageChangeFromLocal(row); break;
            case Input: handleUsageChangeFromInput(row); break;
            case Output: handleUsageChangeFromOutput(row); break;
            case InputOutput: handleUsageChangeFromInputOutput(row); break;
        }
    }


    private void handleUsageChangeFromLocal(NetVariableRow row) {
        YVariable localVar = net.removeLocalVariable(row.getStartingName());
        if (row.isInput() || row.isInputOutput()) {
            net.addInputParameter(newYParameter(localVar, row,
                    YParameter._INPUT_PARAM_TYPE));
        }
        if (row.isOutput() || row.isInputOutput()) {
            net.addOutputParameter(newYParameter(localVar, row,
                    YParameter._OUTPUT_PARAM_TYPE));
        }
    }


    private void handleUsageChangeFromInput(NetVariableRow row) {
        YParameter input = net.getInputParameters().get(row.getStartingName());
        if (row.isLocal()) {
            net.setLocalVariable(newLocalVar(input, row));
        }
        else {
            net.addOutputParameter(newYParameter(input, row, YParameter._OUTPUT_PARAM_TYPE));
        }
        if (! row.isInputOutput()) {   // i.e. local or output only
            net.removeInputParameter(input);
        }
    }


    private void handleUsageChangeFromOutput(NetVariableRow row) {
        YParameter output = net.getOutputParameters().get(row.getStartingName());
        if (row.isLocal()) {
            net.setLocalVariable(newLocalVar(output, row));
        }
        else {
            net.addInputParameter(newYParameter(output, row, YParameter._INPUT_PARAM_TYPE));
        }
        if (! row.isInputOutput()) {   // i.e. local or input only
            net.removeOutputParameter(output);
        }
    }


    private void handleUsageChangeFromInputOutput(NetVariableRow row) {
        YParameter output = net.getOutputParameters().get(row.getStartingName());
        if (row.isLocal()) {
            net.setLocalVariable(newLocalVar(output, row));
        }
        else if (row.isInput()) {
            net.removeOutputParameter(output);
        }
        else {
            YParameter input = net.getInputParameters().get(row.getStartingName());
            net.removeInputParameter(input);
        }
    }


    private void handleNameChange(NetVariableRow row) {
        if (row.isLocal()) {
            YVariable localVar = net.removeLocalVariable(row.getStartingName());
            localVar.setDataTypeAndName(row.getDataType(), row.getName(),
                    localVar.getDataTypeNameSpace());
            net.setLocalVariable(localVar);
        }
        else if (row.isInput() || row.isInputOutput()) {
            YParameter parameter = net.removeInputParameter(row.getStartingName());
            parameter.setDataTypeAndName(row.getDataType(), row.getName(),
                    parameter.getDataTypeNameSpace());
            net.addInputParameter(parameter);
        }
        else if (row.isOutput() || row.isInputOutput()) {
            YParameter parameter = net.removeOutputParameter(row.getStartingName());
            parameter.setDataTypeAndName(row.getDataType(), row.getName(),
                    parameter.getDataTypeNameSpace());
            net.addOutputParameter(parameter);
        }
        handleValueChange(row);
    }


    private void handleValueChange(NetVariableRow row) {
        if (row.isLocal()) {
            net.getLocalVariables().get(row.getStartingName()).setInitialValue(row.getValue());
        }
        else if (row.isOutput()) {
            net.getOutputParameters().get(row.getStartingName()).setDefaultValue(row.getValue());
        }
    }


    private void handleNewRow(NetVariableRow row) {
        String ns = "http://www.w3.org/2001/XMLSchema";
        if (row.isLocal()) {
            YVariable localVar = new YVariable(net);
            localVar.setDataTypeAndName(row.getDataType(), row.getName(), ns);
            localVar.setInitialValue(row.getValue());
            net.setLocalVariable(localVar);
            return;
        }
        if (row.isInput() || row.isInputOutput()) {
            YParameter parameter = new YParameter(net, YParameter._INPUT_PARAM_TYPE);
            parameter.setDataTypeAndName(row.getDataType(), row.getName(), ns);
            net.addInputParameter(parameter);
        }
        if (row.isOutput() || row.isInputOutput()) {
            YParameter parameter = new YParameter(net, YParameter._OUTPUT_PARAM_TYPE);
            parameter.setDataTypeAndName(row.getDataType(), row.getName(), ns);
            if (row.isOutput()) parameter.setDefaultValue(row.getValue());
            net.addOutputParameter(parameter);
        }
    }


    private YParameter newYParameter(YVariable variable, NetVariableRow row, int type) {
        YParameter parameter = new YParameter(net, type);
        parameter.setDataTypeAndName(row.getDataType(), row.getName(),
                variable.getDataTypeNameSpace());
        if (row.isOutput()) parameter.setDefaultValue(variable.getInitialValue());
        return parameter;
    }


    private YVariable newLocalVar(YParameter parameter, NetVariableRow row) {
        YVariable localVar = new YVariable(net);
        localVar.setDataTypeAndName(row.getDataType(), row.getName(),
                parameter.getDataTypeNameSpace());
        if (parameter.getParamType() == YParameter._OUTPUT_PARAM_TYPE) {
            localVar.setInitialValue(parameter.getDefaultValue());
        }
        return localVar;
    }


    private void removeVariable(NetVariableRow row) {
        if (row.getStartingUsage() == NetVariableRow.Usage.Local) {
            net.removeLocalVariable(row.getStartingName());
        }
        else {
            if (row.getStartingUsage() == NetVariableRow.Usage.Input ||
                    row.getStartingUsage() == NetVariableRow.Usage.InputOutput) {
                net.removeInputParameter(row.getStartingName());
            }
            if (row.getStartingUsage() == NetVariableRow.Usage.Output ||
                    row.getStartingUsage() == NetVariableRow.Usage.InputOutput) {
                net.removeOutputParameter(row.getStartingName());
            }
        }
    }


    private void setVariableOrdering() {
        int index = 0;
        for (NetVariableRow row : getTable().getVariables()) {
            if (row.isLocal()) {
                net.getLocalVariables().get(row.getName()).setOrdering(index);
            }
            else {
                if (row.isInput() || row.isInputOutput()) {
                    net.getInputParameters().get(row.getName()).setOrdering(index);
                }
                if (row.isOutput() || row.isInputOutput()) {
                    net.getOutputParameters().get(row.getName()).setOrdering(index);
                }
            }
            index++;
        }
    }

}


