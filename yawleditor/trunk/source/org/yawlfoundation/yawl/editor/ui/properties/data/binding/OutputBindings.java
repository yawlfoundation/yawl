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
import org.yawlfoundation.yawl.elements.YMultiInstanceAttributes;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.data.external.ExternalDBGatewayFactory;
import org.yawlfoundation.yawl.util.StringUtil;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Stores all output data binding updates until they are committed or cleared
 *
 * @author Michael Adams
 * @date 22/11/2013
 */
public class OutputBindings {

    private YTask _task;
    private Map<String, String> _netVarBindings;        // [binding, netVarName]
    private Map<String, String> _externalBindings;      // [taskVarName, ext. binding]
    private Set<String> _orphanedBindings;


    public OutputBindings(YTask task) {
        _task = task;
        _netVarBindings = new HashedMap<String, String>();
        _externalBindings = new HashedMap<String, String>();
        _orphanedBindings = new HashSet<String>();
    }


    public String getBinding(String netVarName) {
        for (String binding : _netVarBindings.keySet()) {
            String varName = _netVarBindings.get(binding);
             if (netVarName.equals(varName)) {
                 return unwrapMapping(binding);
             }
        }
        return unwrapMapping(_task.getDataBindingForOutputParam(netVarName));
    }

    public String getBindingTarget(String binding) {
        String netVar = _netVarBindings.get(binding);
        return netVar != null ? netVar :
                _task.getDataMappingsForTaskCompletion().get(binding);
    }


    public void setBinding(String netVarName, String binding) {
        if (! (binding == null || netVarName == null)) {
            _netVarBindings.put(binding, netVarName);
        }
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
        if (! (binding == null || taskVarName == null)) {
            _externalBindings.put(taskVarName, binding);
        }
    }


    public boolean hasBinding(String taskVarName) {
        return getTarget(taskVarName) != null;
    }


    public String getTarget(String taskVarName) {
        String target = getExternalBinding(taskVarName);
        if (target == null) {
            target = getTarget(_netVarBindings, taskVarName);
        }
        if (target == null) {
            target = getTarget(_task.getDataMappingsForTaskCompletion(), taskVarName);
        }
        return target;
    }

    public String getBindingFromSource(String taskVarName) {
        String netVarName = getTarget(taskVarName);
        return netVarName != null ? getBinding(netVarName) : null;
    }


    public boolean setBindingFromSource(String taskVarName, String binding) {
        String netVarName = getTarget(taskVarName);
        if (netVarName != null) {
            setBinding(netVarName, binding);
        }
        return netVarName != null;
    }


    public void replaceBinding(String oldBinding, String newBinding) {
        String netVar = getBindingTarget(oldBinding);
        if (netVar != null) {
            setBinding(netVar, newBinding);
        }
    }


    public boolean renameTarget(String oldName, String newName) {
        return renameNetVarTarget(oldName, newName) ||
                renameExternalTarget(oldName, newName);
    }

    public boolean renameNetVarTarget(String oldName, String newName) {
        String binding = getBinding(oldName);
        if (binding != null) {
            _orphanedBindings.add(binding);
            setBinding(newName, binding);
        }
        return binding != null;
    }


    public boolean renameExternalTarget(String oldName, String newName) {
        String binding = getExternalBinding(oldName);
        if (binding != null) {
            _orphanedBindings.add(binding);
            setExternalBinding(newName, binding.replace(":" + oldName, ":" + newName));
        }
        return binding != null;
    }


    public String getMiJoinQuery() {
        YMultiInstanceAttributes attributes = _task.getMultiInstanceAttributes();
        return attributes != null ? attributes.getMIJoiningQuery() : null;
    }


    public void commit() {
        Map<String, String> currentBindings = _task.getDataMappingsForTaskCompletion();
        for (String binding : _orphanedBindings) {
            currentBindings.remove(binding);
        }
        for (String binding : _netVarBindings.keySet()) {
            String netVar = _netVarBindings.get(binding);
            String currentMapping = _task.getDataBindingForOutputParam(netVar);
            if (currentMapping != null) {
                currentBindings.remove(currentMapping);
            }
            currentBindings.put(binding, netVar);
        }
        for (String binding : _externalBindings.values()) {
            currentBindings.put(binding, "");
        }
        clear();
    }


    public void clear() {
        _externalBindings.clear();
        _netVarBindings.clear();
        _orphanedBindings.clear();
    }


    public boolean hasChanges() {
        return ! (_netVarBindings.isEmpty() && _externalBindings.isEmpty());
    }

    private String getExternalBindingFromTask(String taskVarName) {
        for (String binding : _task.getDataMappingsForTaskCompletion().keySet()) {
            if (isGateway(binding) && binding.endsWith(":" + taskVarName)) {
                return binding;
            }
        }
        return null;
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

    private String getTarget(Map<String, String> bindings, String taskVarName) {
        String decompKey = '/' + _task.getDecompositionPrototype().getID() + '/';
        for (String outputQuery : bindings.keySet()) {
            if (outputQuery.contains(decompKey + taskVarName + '/')) {
                return bindings.get(outputQuery);
            }
        }
        return null;
    }


    protected boolean isGateway(String binding) {
        return ExternalDBGatewayFactory.isExternalDBMappingExpression(binding);
    }

}
