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

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 21/11/2013
 */
public class InputBindingDialog extends AbstractDataBindingDialog {

    private TaskVariablePanel _targetPanel;
    private NetVariablePanel _generatePanel;


    public InputBindingDialog(VariableRow row,
                              java.util.List<VariableRow> netVarList,
                              java.util.List<VariableRow> taskVarList) {
        super(row, netVarList, taskVarList);
        if (! row.isMultiInstance()) {
            setTypeValidator(new BindingTypeValidator(netVarList, row, row.getDataType()));
        }
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
        else if (action.equals("taskVarComboSelection")) {
            handleTaskVarSelection();
        }
        else if (action.equals("Cancel")) {
            undoChanges();
            setVisible(false);
        }
        else if (action.equals("OK")) {
            VariableRow row = getCurrentRow();
            row.setMapping(formatQuery(getEditorText(), false));
            if (row.isMultiInstance()) {
                row.setMIQuery(formatQuery(getMIEditorText(), false));
            }
            setVisible(false);
        }
    }


    protected JPanel buildTargetPanel() {
        _targetPanel = new TaskVariablePanel("Input To", getTaskVarList(), this);
        return _targetPanel;
    }


    protected JPanel buildGeneratePanel() {
        _generatePanel = new NetVariablePanel("Generate Binding From",
                getNetVarList(), null);
        return _generatePanel;
    }


    protected void initContent(VariableRow row) {
        super.initContent(row);
        _targetPanel.setSelectedItem(row.getName());
        _generatePanel.setSelectedItem(getBindingSource(row));
        setEditorText(row.getMapping());
        if (getCurrentRow().isMultiInstance()) {
            setMIEditorText(getCurrentRow().getMIQuery());
            _targetPanel.disableSelections();
            _generatePanel.disableSelections();
        }
    }


    private String getBindingSource(VariableRow row) {
        String binding = row.getMapping();
        if (binding == null) return null;
        if (binding.contains("#external:")) return binding;

        DefaultBinding defBinding = new DefaultBinding(binding);
        if (defBinding.isCustomBinding() && ! row.isMultiInstance()) return null;

        String netVarName = null;
        VariableRow netVarRow = getNetVariableRow(defBinding.getVariableName());
        if (netVarRow != null && netVarRow.getDecompositionID().equals(
                defBinding.getContainerName())) {
            netVarName = netVarRow.getName();
        }
        return netVarName;
    }


    private VariableRow getSelectedTaskVariableRow() {
        return getTaskVariableRow(_targetPanel.getSelectedItem());
    }

    private void handleTaskVarSelection() {
        if (! getEditorText().equals(getCurrentRow().getMapping())) {
            saveToUndo();
            getCurrentRow().setMapping(formatQuery(getEditorText(), false));
        }
        VariableRow row = getSelectedTaskVariableRow();
        BindingTypeValidator validator = getTypeValidator();
        if (validator != null) {
            validator.setRootElementName(row.getName());
            validator.setDataType(row.getDataType());
        }
        setCurrentRow(row);
        setTargetVariableName(row.getName());
        setEditorText(row.getMapping());
    }


    private void saveToUndo() {
        Map<String, String> undoMap = getUndoMap();
        VariableRow currentRow = getCurrentRow();
        if (! undoMap.containsKey(currentRow.getName())) {
            undoMap.put(currentRow.getName(), currentRow.getMapping());
        }
    }


    private void undoChanges() {
        Map<String, String> undoMap = getUndoMap();
        for (String varName : undoMap.keySet()) {
            VariableRow row = getTaskVariableRow(varName);
            row.setMapping(formatQuery(undoMap.get(varName), false));
        }
        undoMap.clear();
    }

    private void generateBinding() {
        String netVar = _generatePanel.getSelectedVariableName();
        if (netVar != null) {
            generateBindingFromNetVar(netVar);
            return;
        }
        String gateway = _generatePanel.getSelectedDataGateway();
        if (gateway != null) {
            generateExternalBinding(gateway);
        }
    }


    // update the binding text based on the new net var selection
    private void generateBindingFromNetVar(String selectedNetVarName) {
        VariableRow row = getNetVariableRow(selectedNetVarName);
        if (row != null) {
            setEditorText(createBinding(row));
        }
    }


    private void generateExternalBinding(String gateway) {
        VariableRow row = getSelectedTaskVariableRow();
        if (row != null) {
            String expression ="#external:" + gateway + ":" + row.getName();
            setEditorText(expression);
        }
    }


    private void resetBinding() {
        VariableRow row = getSelectedTaskVariableRow();
        if (row != null) {
            setEditorText(row.getStartingMapping());
        }
    }

}
