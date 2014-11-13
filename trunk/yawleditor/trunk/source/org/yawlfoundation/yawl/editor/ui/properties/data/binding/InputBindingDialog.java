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
import java.util.Map;

/**
 * @author Michael Adams
 * @date 21/11/2013
 */
public class InputBindingDialog extends AbstractDataBindingDialog {

    private TaskVariablePanel _targetPanel;
    private NetVariablePanel _generatePanel;


    public InputBindingDialog(String taskID, VariableRow row,
                              java.util.List<VariableRow> netVarList,
                              java.util.List<VariableRow> taskVarList) {
        super(taskID, row, netVarList, taskVarList);
        setTypeValidator(row);
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
        else if (action.equals("taskVarComboSelection")) {
            handleTaskVarSelection();
        }
        else if (action.equals("Cancel")) {
            undoChanges();
            setVisible(false);
        }
        else if (action.equals("OK")) {
            VariableRow row = getCurrentRow();
            row.setBinding(formatQuery(getEditorText(), false));
            if (row.isMultiInstance()) {
                getMultiInstanceHandler().setSplitQuery(
                        formatQuery(getMIEditorText(), false));
            }
            setVisible(false);
        }
    }

    public void setMultiInstanceHandler(MultiInstanceHandler miHandler) {
        super.setMultiInstanceHandler(miHandler);
        setMIEditorText(miHandler.getSplitQuery());
    }

    protected String makeTitle(String taskID) {
       return super.makeTitle("Input", taskID);
    }

    protected String getMIPanelTitle() {
        return "MI Splitting Query";
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
        String source = getBindingSource(row);
        if (source == null) source = row.getName(); // if no source, guess on name match
        _generatePanel.setSelectedItem(source);
        setEditorText(row.getBinding());
    }


    private void setTypeValidator(VariableRow row) {
        if (! row.isMultiInstance()) {
            setTypeValidator(new BindingTypeValidator(getNetVarList(),
                    row.getDataType()));
        }
    }

    private String getBindingSource(VariableRow row) {
        String binding = row.getBinding();
        if (binding == null) {
            return getMatchingNetVarName(row);
        }
        if (binding.contains("#external:")) return binding;

        DefaultBinding defBinding = new DefaultBinding(binding);
        if (defBinding.isCustomBinding() && ! row.isMultiInstance()) {
            return getFirstNetVarWithDataType(row.getDataType());
        }

        String source = null;
        VariableRow netVarRow = getNetVariableRow(defBinding.getVariableName());
        if (netVarRow != null && netVarRow.getDecompositionID().equals(
                defBinding.getDecompositionID())) {
            source = netVarRow.getName();
        }
        return source != null ? source : getFirstNetVarWithDataType(row.getDataType());
    }


    private VariableRow getSelectedTaskVariableRow() {
        return getTaskVariableRow(_targetPanel.getSelectedItem());
    }

    private void handleTaskVarSelection() {
        String binding = getEditorText();
        if (isValidBinding(binding) && ! binding.equals(getCurrentRow().getBinding())) {
            saveToUndo();
            getCurrentRow().setBinding(formatQuery(getEditorText(), false));
        }
        VariableRow row = getSelectedTaskVariableRow();
        BindingTypeValidator validator = getTypeValidator();
        if (validator != null) {
            validator.setDataType(row.getDataType());
        }
        setCurrentRow(row);
        setTargetVariableName(row.getName());
        setEditorText(row.getBinding());
    }


    private void saveToUndo() {
        Map<String, String> undoMap = getUndoMap();
        VariableRow currentRow = getCurrentRow();
        if (! undoMap.containsKey(currentRow.getName())) {
            undoMap.put(currentRow.getName(), currentRow.getBinding());
        }
    }


    private void undoChanges() {
        Map<String, String> undoMap = getUndoMap();
        for (String varName : undoMap.keySet()) {
            VariableRow row = getTaskVariableRow(varName);
            row.setBinding(formatQuery(undoMap.get(varName), false));
        }
        undoMap.clear();
    }


    // returns the name of the net var row with the same name as the row passed, or if no
    // match, the name of the first net var row that match the datatype of the row passed
    private String getMatchingNetVarName(VariableRow row) {
        VariableRow netVarRow = getNetVariableRow(row.getName());
        return netVarRow != null ? netVarRow.getName() :
                getFirstNetVarWithDataType(row.getDataType());
    }


    private String getFirstNetVarWithDataType(String dataType) {
        for (VariableRow netVarRow : getNetVarList()) {
            if (netVarRow.getDataType().equals(dataType)) {
                return netVarRow.getName();
            }
        }
        return null;
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
            setEditorText(DataUtils.createBinding(row));
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
            setEditorText(row.getStartingBinding());
        }
    }

}
