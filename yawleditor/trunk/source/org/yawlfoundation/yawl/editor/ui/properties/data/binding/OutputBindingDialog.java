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

package org.yawlfoundation.yawl.editor.ui.properties.data.binding;

import org.yawlfoundation.yawl.editor.ui.properties.data.VariableRow;
import org.yawlfoundation.yawl.editor.ui.properties.data.validation.BindingTypeValidator;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Michael Adams
 * @date 25/11/2013
 */
public class OutputBindingDialog extends AbstractDataBindingDialog {

    private TaskVariablePanel _generatePanel;
    private NetVariablePanel _targetPanel;
    private OutputBindings _outputBindings;
    private WorkingSelection _workingSelection;


    public OutputBindingDialog(VariableRow row,
                               java.util.List<VariableRow> netVarList,
                               java.util.List<VariableRow> taskVarList,
                               OutputBindings outputBindings) {
        super(row, netVarList, taskVarList);
        _outputBindings = outputBindings;
        _workingSelection = new WorkingSelection();
        setTypeValidator();
        initSpecificContent(row);
    }


    public void actionPerformed(ActionEvent event) {
        if (isInitialising()) return;
        String action = event.getActionCommand();
        if (action.equals("insertBinding")) {
            generateBinding();
        }
        else if (action.equals("resetBinding")) {
            resetBinding();
        }
        else if (action.equals("netVarComboSelection") || action.equals("netVarRadio")) {
            handleNetVarSelection();
        }
        else if (action.equals("gatewayComboSelection") || action.equals("gatewayRadio")) {
            handleGatewaySelection();
        }
        else if (action.equals("Cancel")) {
            undoChanges();
            setVisible(false);
        }
        else if (action.equals("OK")) {
            savePreviousSelection();
            if (getCurrentRow().isMultiInstance()) {
                getCurrentRow().setMIQuery(formatQuery(getMIEditorText(), false));
            }
            setVisible(false);
        }
    }


    public boolean hasChanges() {
        return _outputBindings.hasChanges();
    }


    protected JPanel buildTargetPanel() {
        _targetPanel = new NetVariablePanel("Output To", getNetVarList(), this);
        return _targetPanel;
    }

    protected JPanel buildGeneratePanel() {
        _generatePanel = new TaskVariablePanel("Generate Binding From",
                getTaskVarList(), null);
        return _generatePanel;
    }

    protected void initContent(VariableRow row) {
        super.initContent(row);
    }

    private void initSpecificContent(VariableRow row) {
        _generatePanel.setSelectedItem(row.getName());
        String target = _outputBindings.getTarget(row.getName());
        if (target == null) {
            target = getFirstNetVarName();
        }
        if (target != null) {
            String binding;
            _targetPanel.setSelectedItem(target);
            if (_outputBindings.isGateway(target)) {
                binding = target;
                _workingSelection.set(_targetPanel.getSelectedDataGateway(),
                        binding, true);
            }
            else {
                binding = _outputBindings.getBinding(target);
                _workingSelection.set(target, binding, false);
            }
            setEditorText(binding);
        }
        if (getCurrentRow().isMultiInstance()) {
            setMIEditorText(getCurrentRow().getMIQuery());
            _targetPanel.disableSelections();
            _generatePanel.disableSelections();
        }
    }


    private void setTypeValidator() {
        if (! getCurrentRow().isMultiInstance()) {
            setTypeValidator(new BindingTypeValidator(getTaskVarList(), getCurrentRow(),
                   getTargetDataType()));
        }
    }


    private String getTargetDataType() {
        String dataTypeName = "string";           // default
        String netVarName = getCurrentRow().getNetVarForOutputMapping();
        if (!StringUtil.isNullOrEmpty(netVarName)) {
            VariableRow netVarRow = getNetVariableRow(netVarName);
            if (netVarRow != null) {
                dataTypeName = netVarRow.getDataType();
            }
        }
        return dataTypeName;
    }


    private String getFirstNetVarName() {
        return getNetVarList().isEmpty() ? null : getNetVarList().get(0).getName();
    }


    private void generateBinding() {
        String taskVarName = _generatePanel.getSelectedItem();
        if (taskVarName != null) {
            VariableRow row = getTaskVariableRow(taskVarName);
            if (row != null) {
                if (_workingSelection.isGateway) {
                    setEditorText("#external:" + _workingSelection.item +
                            ":" + row.getName());
                }
                else {
                    setEditorText(createBinding(row));
                }
            }
        }
    }


    private void resetBinding() {
        String netVar = _targetPanel.getSelectedVariableName();
        if (netVar != null) {
            setEditorText(_outputBindings.getBinding(netVar));
        }
        else {
            String gateway = _targetPanel.getSelectedDataGateway();
            if (gateway != null) {
                String binding = _outputBindings.getExternalBinding(
                        _generatePanel.getSelectedItem());
                if (binding != null && binding.contains(":" + gateway + ":")) {
                    setEditorText(binding);
                }
            }
        }
    }


    private VariableRow getSelectedNetVariableRow() {
        return getNetVariableRow(_targetPanel.getSelectedVariableName());
    }

    private void handleNetVarSelection() {
        savePreviousSelection();
        VariableRow row = getSelectedNetVariableRow();
        BindingTypeValidator validator = getTypeValidator();
        if (validator != null) {
            validator.setRootElementName(row.getName());
            validator.setDataType(row.getDataType());
        }
        setTargetVariableName(row.getName());
        String binding = _outputBindings.getBinding(row.getName());
        setEditorText(binding);
        _workingSelection.set(row.getName(), binding, false);
    }

    private void handleGatewaySelection() {
        savePreviousSelection();
        String gateway = _targetPanel.getSelectedDataGateway();
        String binding = _outputBindings.getExternalBinding(
                _generatePanel.getSelectedItem(), gateway);
        if (binding == null) {
            binding = _outputBindings.getAnyExternalBindingForGateway(gateway);
            if (binding != null) {
                setTaskVarFromGatewayBinding(gateway);
            }
        }
        setEditorText(binding);
        _workingSelection.set(gateway, binding, true);
   }


    private void setTaskVarFromGatewayBinding(String binding) {
        String taskVarName = binding.substring(binding.lastIndexOf(':') + 1);
        _generatePanel.setSelectedItem(taskVarName);
    }


    private void undoChanges() {
        _outputBindings.clear();
    }


    private void savePreviousSelection() {
        if (! getEditorText().equals(_workingSelection.binding)) {
            if (_workingSelection.isGateway) {
                _outputBindings.setExternalBinding(_generatePanel.getSelectedItem(),
                        formatQuery(_workingSelection.binding, false));
            }
            else {
                _outputBindings.setBinding(_workingSelection.item,
                        formatQuery(_workingSelection.binding, false));
            }
        }
    }


    class WorkingSelection {
        String item;
        String binding;
        boolean isGateway;

        void set(String i, String b, boolean g) { item = i; binding = b; isGateway = g; }
    }

}
