package org.yawlfoundation.yawl.elements;

import org.yawlfoundation.yawl.util.YVerificationMessage;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.elements.data.YParameter;

import java.util.*;

/**
 * Author: Michael Adams
 * Creation Date: 3/05/2010
 */
public class YNetLocalVarVerifier {

    private YNet _net;
    private final String _paramMask;
    private Map<String, LocalTaskMap> _uninitialisedLocalVars;

    public YNetLocalVarVerifier(YNet net) {
        _net = net;
        _uninitialisedLocalVars = new Hashtable<String, LocalTaskMap>();
        _paramMask = "/" + _net.getName() + "/$$/";
    }


    public List<YVerificationMessage> verify() {
        List<YVerificationMessage> messages = new Vector<YVerificationMessage>();

        // get all local vars that don't have an initial value
        getUnitialisedLocalVars();

        // populate maps with tasks that have any uninitialised locals in mappings
        populateLocalTaskMaps();

        // check all paths for each input task for each local var
        for (LocalTaskMap localTaskMap : _uninitialisedLocalVars.values()) {
            messages.addAll(verify(localTaskMap));
        }

        return messages;
    }


    private List<YVerificationMessage> verify(LocalTaskMap map) {
        List<YVerificationMessage> messages = new Vector<YVerificationMessage>();

        // for each affected task, check each of its backward paths to the start
        // condition to see if any tasks on the path output a value to the local var
        for (YTask task : map.getInputTasks()) {
            if (! map.getCheckedTasks().contains(task)) {
                messages.addAll(verify(map, task));
            }
        }

        return messages;
    }


    private List<YVerificationMessage> verify(LocalTaskMap map, YExternalNetElement element) {
        List<YVerificationMessage> messages = new Vector<YVerificationMessage>();

        Set<YExternalNetElement> preSet = element.getPresetElements();
        for (YExternalNetElement preElement : preSet) {
            if (preElement instanceof YInputCondition) {
                // add message
            }
            else if (preElement instanceof YTask) {
                YTask preTask = (YTask) preElement;

                if (map.getOutputTasks().contains(preTask)) {
                    if (map.isMandatory(preTask)) {
                        // this path is ok
                    }
                    else {
                        // add warning

                        verify(map, preTask);        // recurse
                    }
                }
                else if (map.getInputTasks().contains(preTask)) {
                    map.getCheckedTasks().add(preTask);
                }
            }
            else {           // plain condition
                verify(map, preElement);             // recurse
            }
        }

        return messages;
    }


    private void getUnitialisedLocalVars() {
        for (YVariable local : _net.getLocalVariables().values()) {
            if (StringUtil.isNullOrEmpty(local.getInitialValue())) {
                LocalTaskMap localMap = new LocalTaskMap(local);
                _uninitialisedLocalVars.put(local.getName(), localMap);
            }
        }
    }


    private void populateLocalTaskMaps() {
        for (YTask task : _net.getNetTasks()) {
            populateMapsForTask(task, true);          // input params
            populateMapsForTask(task, false);         // output params
        }
    }

    private void populateMapsForTask(YTask task, boolean input) {
        for (String paramName : getParamNamesForTask(task, input)) {
            addTaskIfQueryHasLocalVar(task, paramName, input);
        }
    }


    private void addTaskIfQueryHasLocalVar(YTask task, String paramName, boolean input) {
        String query = getQueryForParam(task, paramName, input);
        for (String localVarName : _uninitialisedLocalVars.keySet()) {
            String mask = _paramMask.replace("$$", localVarName);

            // if this task has an uninit. local task in its mapping query
            if (query.contains(mask)) {
                LocalTaskMap taskMap = _uninitialisedLocalVars.get(localVarName);
                taskMap.add(task, input, paramName);
            }
        }        
    }


    private Collection<String> getParamNamesForTask(YTask task, boolean input) {
        return input ? task.getParamNamesForTaskStarting() :
                       task.getParamNamesForTaskCompletion();
    }

    private String getQueryForParam(YTask task, String paramName, boolean input) {
        return input ? task.getDataBindingForInputParam(paramName) :
                       task.getDataBindingForOutputParam(paramName);
    }


    private boolean isMandatoryParam(YTask task, String paramName) {
        YDecomposition decomp = task.getDecompositionPrototype();
        if (decomp != null) {
            Map<String, YParameter> paramMap = decomp.getOutputParameters();
            if (paramMap != null) {
                YParameter param = paramMap.get(paramName);
                return (param != null) && param.isMandatory();
            }
        }
        return false;
    }


    /**************************************************************************/

    private class LocalTaskMap {

        private YVariable _localVar;
        private Set<YTask> _inputTasks;
        private Map<YTask, Boolean> _outputTasks;
        private Set<YTask> _checkedInputTasks;


        public LocalTaskMap(YVariable localVar) {
            _localVar = localVar;
            _inputTasks = new HashSet<YTask>();
            _outputTasks = new Hashtable<YTask, Boolean>();
            _checkedInputTasks = new HashSet<YTask>();
        }

        public YVariable getLocalVar() { return _localVar; }

        public Set<YTask> getInputTasks() { return _inputTasks; }

        public Set<YTask> getOutputTasks() { return _outputTasks.keySet(); }

        public Set<YTask> getCheckedTasks() { return _checkedInputTasks; }

        public boolean isMandatory(YTask task) {
            return _outputTasks.containsKey(task) && _outputTasks.get(task);
        }

        public boolean add(YTask task, boolean input, String paramName) {
            return input ? _inputTasks.add(task) :
                           _outputTasks.put(task, isMandatoryParam(task, paramName));
        }


        private boolean isMandatoryParam(YTask task, String paramName) {
            YDecomposition decomp = task.getDecompositionPrototype();
            if (decomp != null) {
                Map<String, YParameter> paramMap = decomp.getOutputParameters();
                if (paramMap != null) {
                    YParameter param = paramMap.get(paramName);
                    return (param != null) && param.isMandatory();
                }
            }
            return false;
        }
    }
    
}
