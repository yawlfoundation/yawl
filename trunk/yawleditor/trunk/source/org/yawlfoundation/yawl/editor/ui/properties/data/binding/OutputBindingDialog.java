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

import org.yawlfoundation.yawl.editor.ui.properties.data.DataUtils;
import org.yawlfoundation.yawl.editor.ui.properties.data.MultiInstanceHandler;
import org.yawlfoundation.yawl.editor.ui.properties.data.VariableRow;
import org.yawlfoundation.yawl.editor.ui.properties.data.validation.BindingTypeValidator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 25/11/2013
 */
public class OutputBindingDialog extends AbstractDataBindingDialog {

    private TaskVariablePanel _generatePanel;
    private NetVariablePanel _targetPanel;
    private final OutputBindings _outputBindings;
    private final WorkingSelection _workingSelection;
    private final Map<String, String> _externalUndoMap;


    public OutputBindingDialog(String taskID, VariableRow row,
                               java.util.List<VariableRow> netVarList,
                               java.util.List<VariableRow> taskVarList,
                               OutputBindings outputBindings) {
        super(taskID, row, netVarList, taskVarList);
        _outputBindings = outputBindings;
        _outputBindings.beginUpdates();
        _workingSelection = new WorkingSelection();
        _externalUndoMap = new HashMap<String, String>();
        initSpecificContent(row);
        setTypeValidator();
        _initialising = false;
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
                getMultiInstanceHandler().setJoinQueryUnwrapped(
                        formatQuery(getMIEditorText(), false));
                getMultiInstanceHandler().setOutputTarget(
                        _targetPanel.getSelectedVariableName());
            }
            setVisible(false);
        }
    }


    public boolean hasChanges() {
        return super.hasChanges() || ! _externalUndoMap.isEmpty();
    }

    public void setMultiInstanceHandler(MultiInstanceHandler miHandler) {
        super.setMultiInstanceHandler(miHandler);
        _targetPanel.setSelectedItem(miHandler.getOutputTarget());
        setEditorText(miHandler.getOutputQuery());
        setMIEditorText(miHandler.getJoinQueryUnwrapped());
    }


    protected String makeTitle(String taskID) {
       return super.makeTitle("Output", taskID);
    }


    protected String getMIPanelTitle() {
        return "MI Joining Query";
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

    private void initSpecificContent(VariableRow row) {
        _generatePanel.setSelectedItem(row.getName());
        String target = _outputBindings.getTarget(row.getName());
        boolean guessedTarget = false;
        if (target == null) {
            target = getBestGuessTarget(row);
            guessedTarget = true;
        }
        if (target != null) {
            _targetPanel.setSelectedItem(target);
            String binding;
            if (_outputBindings.isGateway(target)) {
                binding = target;
                _workingSelection.set(_targetPanel.getSelectedDataGateway(),
                        binding, true);
            }
            else {
                binding = _outputBindings.getBinding(target);
                _workingSelection.set(target, binding, false);
            }
            if (! guessedTarget) setEditorText(binding);
        }
    }


    private void setTypeValidator() {
        if (! getCurrentRow().isMultiInstance()) {
            setTypeValidator(new BindingTypeValidator(getTaskVarList(),
                   getTargetDataType()));
        }
    }


    private String getTargetDataType() {
        VariableRow netVarRow = getSelectedNetVariableRow();
        if (netVarRow != null) {
            return netVarRow.getDataType();
        }
        return "string";                               // default for external gateway
    }


    private String getBestGuessTarget(VariableRow taskVarRow) {

        // try a match on name first
        for (VariableRow netVarRow : getNetVarList()) {
             if (netVarRow.getName().equals(taskVarRow.getName())) {
                 return netVarRow.getName();
             }
        }

        // next, see if this task var is inside a larger mapping to a target
        String target = _outputBindings.getEmbeddedTarget(taskVarRow.getName());
        if (target != null) return target;

        // still no match, try on data type
        for (VariableRow netVarRow : getNetVarList()) {
             if (netVarRow.getDataType().equals(taskVarRow.getDataType())) {
                 return netVarRow.getDataType();
             }
        }

        // well, we tried - return the first listed var (if any)
        if (! getNetVarList().isEmpty()) return getNetVarList().get(0).getName();

        // no net vars? get the first gateway listed
        String gateway = _targetPanel.getFirstDataGateway();
        return gateway != null ? "#external:" + gateway + ":" : null;
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
                    setEditorText(DataUtils.createBinding(row));
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
        updateValidator(row);
        setTargetVariableName(row.getName());
        String binding = _outputBindings.getBinding(row.getName());
        setEditorText(binding);
        _workingSelection.set(row.getName(), binding, false);
    }

    private void handleGatewaySelection() {
        savePreviousSelection();
        String gateway = _targetPanel.getSelectedDataGateway();
        updateValidator(null);
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


    private void updateValidator(VariableRow row) {
        BindingTypeValidator validator = getTypeValidator();
        if (validator != null) {
            validator.setDataType(row != null ? row.getDataType() : "string");
        }
    }

    private void undoChanges() {
        _outputBindings.rollback();
    }


    private void savePreviousSelection() {
        String binding = getEditorText();
        if (isValidBinding(binding) && ! binding.equals(_workingSelection.binding)) {
            if (_workingSelection.isGateway) {
                _outputBindings.setExternalBinding(_generatePanel.getSelectedItem(),
                        formatQuery(binding, false));
            }
            else {
                _outputBindings.setBinding(_workingSelection.item,
                        formatQuery(binding, false));
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
