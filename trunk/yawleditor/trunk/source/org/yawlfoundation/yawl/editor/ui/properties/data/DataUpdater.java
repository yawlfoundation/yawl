/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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
import org.yawlfoundation.yawl.editor.ui.properties.data.binding.OutputBindings;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YTask;

import java.util.Map;

/**
 * @author Michael Adams
 * @date 9/07/2014
 */
public class DataUpdater {

    private VariableTable _table;
    private OutputBindings _outputBindings;
    private YTask _task;
    private String _netID;
    private static final YDataHandler _dataHandler =
            SpecificationModel.getHandler().getDataHandler();

    public DataUpdater(String netID, YTask task, OutputBindings outputBindings) {
        _netID = netID;
        _task = task;
        _outputBindings = outputBindings;
    }


    public void update(VariableTable table, YDecomposition host)
            throws YDataHandlerException {
        if (table == null || ! table.isChanged()) return;
        _table = table;

        for (VariableRow row : table.getRemovedVariables()) {
            removeVariable(row, host);
        }

        for (VariableRow row : table.getVariables()) {
            if (row.isModified()) {
                if (row.isNameChange()) {
                    handleNameChange(row, host);
                }
                if (row.isUsageChange()) {
                    handleUsageChange(row, host);
                }

                if (row.isDataTypeAndValueChange()) {
                    handleDataTypeAndValueChange(row, host);
                }
                else if (row.isDataTypeChange()) {
                    handleDataTypeChange(row, host);
                }
                else if (row.isValueChange()) {
                    handleValueChange(row, host);
                }

                if (isTaskTable(table) && row.isMappingChange()) {
                    handleMappingChange(row);         // only task tables have mappings
                }
                if (isTaskTable(table) && row.isAttributeChange()) {
                    handleAttributeChange(row, host);
                }
                if (isTaskTable(table) && row.isLogPredicateChange()) {
                    handleLogPredicateChange(row, host);
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

        if (isTaskTable(table)) updateMappingsForUsage();

        table.updatesApplied();
    }


    private boolean isTaskTable(VariableTable table) {
        return table.getTableModel() instanceof TaskVarTableModel;
    }


    private boolean handleUsageChange(VariableRow row, YDecomposition host)
            throws YDataHandlerException {
        return _dataHandler.changeVariableScope(host.getID(), row.getName(),
                row.getStartingUsage(), row.getUsage());
    }


    private void handleNameChange(VariableRow row, YDecomposition host)
            throws YDataHandlerException {
        if (row.isLocal()) {
            _dataHandler.renameVariable(host.getID(), row.getStartingName(),
                    row.getName(), YDataHandler.LOCAL);
        }
        else {
            if (row.isInput() || row.isInputOutput()) {
                _dataHandler.renameVariable(host.getID(), row.getStartingName(),
                        row.getName(), YDataHandler.INPUT);
            }
            if (row.isOutput() || row.isInputOutput()) {
                _dataHandler.renameVariable(host.getID(), row.getStartingName(),
                        row.getName(), YDataHandler.OUTPUT);
            }
        }
        if (host instanceof YNet) refreshTaskMappings();
    }


    private void handleDataTypeChange(VariableRow row, YDecomposition host)
            throws YDataHandlerException {
        _dataHandler.setVariableDataType(host.getID(), row.getName(), row.getDataType(),
                row.getUsage());
    }


    private void handleDataTypeAndValueChange(VariableRow row, YDecomposition host)
            throws YDataHandlerException {
        _dataHandler.setVariableDataTypeAndValue(host.getID(), row.getName(),
                row.getDataType(), row.getValue(), row.getUsage());
    }


    private void handleValueChange(VariableRow row, YDecomposition host)
            throws YDataHandlerException {
        if (row.isLocal()) {
            _dataHandler.setInitialValue(host.getID(), row.getName(), row.getValue());
        }
        else if (row.isOutput()) {
            _dataHandler.setDefaultValue(host.getID(), row.getName(), row.getValue());
        }
    }


    // only input mappings handled here - outputs done via output bindings
    private void handleMappingChange(VariableRow row) throws YDataHandlerException {
        if (row.isOutput()) return;
        String mapping = row.getFullMapping();
        if (mapping == null) return;
        String variableName = row.getName();
        _dataHandler.setVariableMapping(_netID, _task.getID(), variableName,
                mapping, YDataHandler.INPUT);
    }


    private void handleAttributeChange(VariableRow row, YDecomposition host)
            throws YDataHandlerException {
        _dataHandler.setVariableAttributes(host.getID(), row.getName(),
                row.getAttributes(), row.getUsage());
    }


    private void handleLogPredicateChange(VariableRow row, YDecomposition host)
            throws YDataHandlerException {
        _dataHandler.setVariableLogPredicate(host.getID(), row.getName(),
                row.getLogPredicate(), row.getUsage());
    }


    private void handleNewRow(VariableRow row, YDecomposition host)
            throws YDataHandlerException {
        String ns = "http://www.w3.org/2001/XMLSchema";
        _dataHandler.addVariable(host.getID(), row.getName(), row.getDataType(),
                ns, row.getUsage(), row.getValue(), row.getAttributes());
    }


    private void removeVariable(VariableRow row, YDecomposition host)
            throws YDataHandlerException {
        String name = row.getStartingName();
        if (name == null) return;                 // added and removed without updating

        if (row.isLocal()) {
            _dataHandler.removeVariable(host.getID(), name, YDataHandler.LOCAL);
        }
        if (row.isInput() || row.isInputOutput()) {
            _dataHandler.removeVariable(host.getID(), name, YDataHandler.INPUT);
        }
        if (row.isOutput() || row.isInputOutput()) {
            _dataHandler.removeVariable(host.getID(), name, YDataHandler.OUTPUT);
        }
        if (host instanceof YNet) refreshTaskMappings();
    }


    private void refreshTaskMappings() {
        if (_table.getTableType() == TableType.Net) return;      // showing netvars only
        for (VariableRow row : _table.getVariables()) {
            if (! row.isOutput()) {
                String mapping = DataUtils.unwrapBinding(
                        _task.getDataBindingForInputParam(row.getName()));
                row.initMapping(mapping);
            }
        }
        _table.repaint();
    }


    private void updateMappingsForUsage() {
        if (_table.getTableType() == TableType.Net) return;      // showing netvars only
        Map<String, String> inputMap = _task.getDataMappingsForTaskStarting();
        for (VariableRow row : _table.getVariables()) {
            if (row.isInput()) {
                _outputBindings.removeBindingForSource(row.getName());
            }
            else if (row.isOutput()) {
                inputMap.remove(row.getName());
            }
        }
    }


    private void updateVariableIndex(VariableTable table, YDecomposition host)
            throws YDataHandlerException {
        int index = 0;
        for (VariableRow row : table.getVariables()) {
            if (row.isLocal()) {
                _dataHandler.setVariableIndex(host.getID(), row.getName(),
                        YDataHandler.LOCAL, index);
            }
            else {
                if (row.isInput() || row.isInputOutput()) {
                    _dataHandler.setVariableIndex(host.getID(), row.getName(),
                            YDataHandler.INPUT, index);
                }
                if (row.isOutput() || row.isInputOutput()) {
                    _dataHandler.setVariableIndex(host.getID(), row.getName(),
                            YDataHandler.OUTPUT, index);
                }
            }
            index++;
        }
    }

}
