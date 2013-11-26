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

import org.apache.commons.collections15.map.HashedMap;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.data.external.ExternalDBGatewayFactory;

import java.util.Map;

/**
 * @author Michael Adams
 * @date 22/11/2013
 */
public class OutputBindings {

    private YTask _task;
    private Map<String, String> _netVarBindings;        // [binding, netVarName]
    private Map<String, String> _externalBindings;      // [taskVarName, ext. binding]


    public OutputBindings(YTask task) {
        _task = task;
        _netVarBindings = new HashedMap<String, String>();
        _externalBindings = new HashedMap<String, String>();
    }


    public String getBinding(String netVarName) {
        for (String binding : _netVarBindings.keySet()) {
            String varName = _netVarBindings.get(binding);
             if (netVarName.equals(varName)) {
                 return varName;
             }
        }
        return _task.getDataBindingForOutputParam(netVarName);
    }


    public void setBinding(String netVarName, String binding) {
        _netVarBindings.put(binding, netVarName);
    }


    public String getExternalBinding(String taskVarName) {
        if (taskVarName == null) return null;
        String binding = _externalBindings.get(taskVarName);
        return binding != null ? binding : getExternalBindingFromTask(taskVarName);
    }


    public String getExternalBinding(String taskVarName, String gateway) {
        for (String binding : _externalBindings.values()) {
            if (binding.endsWith(":" + gateway + ":" + taskVarName)) {
                return binding;
            }
        }
        return null;
    }


    public String getAnyExternalBindingForGateway(String gateway) {
        for (String binding : _externalBindings.values()) {
            if (binding.endsWith(":" + gateway + ":")) {
                return binding;
            }
        }
        return null;
    }


    public void setExternalBinding(String taskVarName, String binding) {
        _netVarBindings.put(taskVarName, binding);
    }


    public boolean hasBinding(String taskVarName) {
        return getTarget(taskVarName) != null;
    }


    public String getTarget(String taskVarName) {
        String binding = getExternalBinding(taskVarName);
        if (binding == null) {
            String decompKey = '/' + _task.getDecompositionPrototype().getID() + '/';
            for (String outputQuery : _task.getDataMappingsForTaskCompletion().keySet()) {
                if (outputQuery.contains(decompKey + taskVarName + '/')) {
                    return _task.getDataMappingsForTaskCompletion().get(outputQuery);
                }
            }
        }
        return null;
    }


    public void commit() {
        Map<String, String> currentBindings = _task.getDataMappingsForTaskCompletion();
        for (String mapping : _netVarBindings.keySet()) {
            String netVar = _netVarBindings.get(mapping);
            String currentMapping = _task.getDataBindingForOutputParam(netVar);
            if (currentMapping != null) {
                currentBindings.remove(currentMapping);
            }
            currentBindings.put(mapping, netVar);
        }
    }


    public void clear() {
        _externalBindings.clear();
        _netVarBindings.clear();
    }


    public boolean hasChanges() {
        return ! (_netVarBindings.isEmpty() || _externalBindings.isEmpty());
    }

    private String getExternalBindingFromTask(String taskVarName) {
        for (String binding : _task.getDataMappingsForTaskCompletion().keySet()) {
            if (isGateway(binding) && binding.endsWith(":" + taskVarName)) {
                return binding;
            }
        }
        return null;
    }


    protected boolean isGateway(String binding) {
        return ExternalDBGatewayFactory.isExternalDBMappingExpression(binding);
    }

}
