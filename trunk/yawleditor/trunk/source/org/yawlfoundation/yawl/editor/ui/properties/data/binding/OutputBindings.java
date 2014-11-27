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
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.data.external.ExternalDBGatewayFactory;
import org.yawlfoundation.yawl.util.StringUtil;

import java.util.HashMap;
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

    private final YTask _task;
    private Map<String, String> _netVarBindings;        // [binding, netVarName]
    private Map<String, String> _externalBindings;      // [taskVarName, ext. binding]
    private Set<String> _orphanedBindings;
    private RollbackPoint _rollbackPoint;


    public OutputBindings(YTask task) {
        _task = task;
        _netVarBindings = new HashMap<String, String>();
        _externalBindings = new HashMap<String, String>();
        _orphanedBindings = new HashSet<String>();
    }


    public Map<String, String> getBindingsSummary() {
        Map<String, String> bindings = new HashMap<String, String>();
        bindings.putAll(_task.getDataMappingsForTaskCompletion());
        for (String binding : _orphanedBindings) {
            bindings.remove(binding);
        }
        for (String binding : _netVarBindings.keySet()) {
            String netVar = _netVarBindings.get(binding);
            String currentMapping = _task.getDataBindingForOutputParam(netVar);
            if (currentMapping != null) {
                bindings.remove(currentMapping);
            }
            bindings.put(binding, netVar);
        }
        for (String binding : _externalBindings.values()) {
            bindings.put(binding, "");
        }
        return bindings;
    }

    /**
     * Gets the binding targeting the named net-level variable, with its outer tags
     * removed
     * @param netVarName the target net-level variable
     * @return the corresponding binding
     */
    public String getBinding(String netVarName) {
        return getBinding(netVarName, true);
    }


    /**
     * Gets the binding targeting the named net-level variable
     * @param netVarName the target net-level variable
     * @param unwrap true to remove the outer tags from the binding
     * @return the corresponding binding
     */
    public String getBinding(String netVarName, boolean unwrap) {

        // try update cache first, then those in the task
        String binding = getAddedBinding(netVarName);
        if (binding == null) binding = _task.getDataBindingForOutputParam(netVarName);
        if (binding != null) binding = binding.trim();
        return unwrap ? DataUtils.unwrapBinding(binding) : binding;
    }


    /**
     * Sets the binding for a target net-level variable, and inserts the appropriate
     * outer xml tags
     * @param netVarName the target net-level variable
     * @param binding the binding to associate with the net-level variable
     */
    public void setBinding(String netVarName, String binding) {
        setBinding(netVarName, binding, true);
    }


    /**
     * Sets the binding for a target net-level variable
     * @param netVarName the target net-level variable
     * @param binding the binding to associate with the net-level variable
     * @param wrap true to insert outer xml tags (of the net var) around the binding
     */
    public void setBinding(String netVarName, String binding, boolean wrap) {
        if (! (binding == null || netVarName == null)) {
            removeAddedBinding(netVarName);
            String adjustedBinding = wrap ?
                    DataUtils.wrapBinding(netVarName, binding) : binding;
            _netVarBindings.put(adjustedBinding, netVarName);
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


    public String getTargetForBinding(String binding) {
        String target = _netVarBindings.get(binding);
        return target != null ? target :
                _task.getDataMappingsForTaskCompletion().get(binding);
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


    public String getEmbeddedTarget(String taskVarName) {
        String xpath = '/' + _task.getDecompositionPrototype().getID() + '/' + taskVarName;
        String target = getEmbeddedTarget(_netVarBindings, xpath);
        if (target == null) {
            target = getEmbeddedTarget(_task.getDataMappingsForTaskCompletion(), xpath);
        }
        return target;
    }


    public String getBindingFromSource(String taskVarName) {
        String netVarName = getTarget(taskVarName);
        return netVarName != null ? getBinding(netVarName) : null;
    }


    public String getBindingFromSource(String taskVarName, boolean unwrap) {
        String netVarName = getTarget(taskVarName);
        return netVarName != null ? getBinding(netVarName, unwrap) : null;
    }


    public boolean setBindingFromSource(String taskVarName, String binding) {
        String netVarName = getTarget(taskVarName);
        if (netVarName != null) {
            setBinding(netVarName, binding);
        }
        return netVarName != null;
    }


    public void replaceBinding(String taskName, String oldBinding, String newBinding) {
        String netVar = getTarget(taskName);
        if (! (netVar == null || isGateway(netVar))) {
            _netVarBindings.remove(DataUtils.wrapBinding(netVar, oldBinding));
            setBinding(netVar, newBinding);
        }
    }


    public void removeBindingForTarget(String netVarName) {
        if (removeAddedBinding(netVarName) == null) {
            Set<String> toBeRemoved = new HashSet<String>();
            for (String binding : _task.getDataMappingsForTaskCompletion().keySet()) {
                String varName = _task.getDataMappingsForTaskCompletion().get(binding);
                if (varName.equals(netVarName)) {
                    toBeRemoved.add(binding);
                }
            }
            for (String binding : toBeRemoved) {
                _task.getDataMappingsForTaskCompletion().remove(binding);
            }
        }
    }


    public void removeBindingForSource(String taskVarName) {
        String netVarName = getTarget(taskVarName);
        if (netVarName != null) removeBindingForTarget(netVarName);
    }

    public void removeBinding(String binding) {
        if (_netVarBindings.remove(binding) == null) {
            _task.getDataMappingsForTaskCompletion().remove(binding);
        }
    }


    public boolean renameTarget(String oldName, String newName) {
        return renameNetVarTarget(oldName, newName) ||
                renameExternalTarget(oldName, newName);
    }

    public boolean renameNetVarTarget(String oldName, String newName) {
        String binding = getBinding(oldName, false);
        if (binding != null) {
            _orphanedBindings.add(binding);
            setBinding(newName, DataUtils.unwrapBinding(binding));
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


    public void beginUpdates() {
        _rollbackPoint = new RollbackPoint();
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

    public void rollback() {
        _externalBindings = _rollbackPoint.externalBindings;
        _netVarBindings = _rollbackPoint.netVarBindings;
        _orphanedBindings = _rollbackPoint.orphanedBindings;
    }


    public void clear() {
        _externalBindings.clear();
        _netVarBindings.clear();
        _orphanedBindings.clear();
        _rollbackPoint = null;
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


    private String getAddedBinding(String netVarName) {
        if (netVarName == null) return null;
        for (String binding : _netVarBindings.keySet()) {
            String varName = _netVarBindings.get(binding);
             if (netVarName.equals(varName)) {
                 return binding;
             }
        }
        return null;
    }


    private String removeAddedBinding(String netVarName) {
        String binding = getAddedBinding(netVarName);
        return binding != null ? _netVarBindings.remove(binding) : null;
    }


    private String getTarget(Map<String, String> bindings, String taskVarName) {
        String varTag = '<' + taskVarName + '>';

        // if query starts with var tag, we have a match
        for (String outputQuery : bindings.keySet()) {
            if (outputQuery.startsWith(varTag)) {
                return bindings.get(outputQuery);
            }
        }

        // try for a single mapping back to a different net-var name
        return getSingleEmbeddedTarget(bindings, taskVarName);
    }


    // binding contains a single xpath, and it references the task
    private String getSingleEmbeddedTarget(Map<String, String> bindings, String taskVarName) {
        String xpath = '/' + _task.getDecompositionPrototype().getID() + '/' + taskVarName;
        for (String outputQuery : bindings.keySet()) {
            if (outputQuery.contains(xpath)) {
                if (StringUtil.unwrap(outputQuery).contains("<")) continue;
                int nextCharPos = outputQuery.indexOf(xpath) + xpath.length();
                char next = outputQuery.charAt(nextCharPos);
                if (next == '/' || next == '}' || next == '<') {
                    return bindings.get(outputQuery);
                }
            }
        }
        return null;
    }


    // returns the target net var if there is a task var XPath embedded within the
    // output binding for the target net var
    private String getEmbeddedTarget(Map<String, String> bindings, String xpath) {
        for (String outputQuery : bindings.keySet()) {
            if (outputQuery.contains(xpath)) {
                int nextCharPos = outputQuery.indexOf(xpath) + xpath.length();
                char next = outputQuery.charAt(nextCharPos);
                if (next == '/' || next == '}' || next == '<') {
                    return bindings.get(outputQuery);
                }
            }
        }
        return null;
    }


    protected boolean isGateway(String binding) {
        return ExternalDBGatewayFactory.isExternalDBMappingExpression(binding);
    }


    class RollbackPoint {

        private final Map<String, String> netVarBindings;
        private final Map<String, String> externalBindings;
        private final Set<String> orphanedBindings;

        RollbackPoint() {
            netVarBindings = new HashMap<String, String>(_netVarBindings);
            externalBindings = new HashMap<String, String>(_externalBindings);
            orphanedBindings = new HashSet<String>(_orphanedBindings);
        }

    }

}
