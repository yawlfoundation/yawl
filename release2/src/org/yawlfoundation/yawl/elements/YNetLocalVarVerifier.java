package org.yawlfoundation.yawl.elements;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.YVerificationMessage;

import java.util.*;

/**
 * Author: Michael Adams
 * Creation Date: 3/05/2010
 */
public class YNetLocalVarVerifier {

    private YNet _net;
    private Map<String, LocalTaskMap> _uninitialisedLocalVars;
    private Logger _log;

    public YNetLocalVarVerifier(YNet net) {
        _net = net;
        _uninitialisedLocalVars = new Hashtable<String, LocalTaskMap>();
        _log = Logger.getLogger(this.getClass());
    }


    public List<YVerificationMessage> verify() {
        long startTime = System.nanoTime();

        List<YVerificationMessage> messages = new Vector<YVerificationMessage>();

        // get all local vars that don't have an initial value
        getUnitialisedLocalVars();

        // populate maps with tasks that have any uninitialised locals in mappings
        populateLocalTaskMaps();

        // check all paths for each input task for each local var
        for (LocalTaskMap localTaskMap : _uninitialisedLocalVars.values()) {
            messages.addAll(verify(localTaskMap));
        }

        _log.debug("Net: " + _net.getID() + " | Duration: " +
                   (System.nanoTime() - startTime) + "nanoseconds.");

        return messages;
    }


    private List<YVerificationMessage> verify(LocalTaskMap map) {
        List<YVerificationMessage> messages = new Vector<YVerificationMessage>();

        // for each affected task, check each of its backward paths to the start
        // condition to see if any tasks on the path output a value to the local var
        for (YTask task : map.getInputTasks()) {
            messages.addAll(verify(map, task, task, new ArrayList<YExternalNetElement>()));
        }

        return messages;
    }


    private List<YVerificationMessage> verify(LocalTaskMap map, YTask subjectTask,
                       YExternalNetElement baseElement, List<YExternalNetElement> visited) {
        List<YVerificationMessage> messages = new Vector<YVerificationMessage>();

        // add the root net element to the set of those visited on this path
        visited.add(baseElement);

        Set<YExternalNetElement> preSet = baseElement.getPresetElements();
        for (YExternalNetElement preElement : preSet) {

            _log.debug("Net: " + _net.getID() + " | Local var: " +
                    map.getLocalVar().getName() +  " | Subject: " + subjectTask.getName() +
                    " | PreElement: " + preElement.toString()); 

            // if this element has already been visited on this path,
            // it is a loop so don't go further on it
            if (! visited.contains(preElement)) {

                // if we're back to the start of the net, this local var is a problem
                if (preElement instanceof YInputCondition) {
                    visited.add(preElement);
                    messages.add(getMessage(map, subjectTask, visited));
                    visited = resetVisited(baseElement);
                }
                else if (preElement instanceof YTask) {
                    YTask preTask = (YTask) preElement;

                    // if it is mandatory for the task to output a value for this
                    // local var, then this path is ok - otherwise recurse
                    if (map.isInitialisingTask(preTask)) {
                        visited = resetVisited(baseElement);                        
                    }
                    else {
                        messages.addAll(verify(map, subjectTask, preTask, visited));
                    }
                }
                else {

                    // a plain condition - recurse
                    messages.addAll(verify(map, subjectTask, preElement, visited));
                }
            }
        }
        return messages;
    }


    private void getUnitialisedLocalVars() {
        Set<String> outputParamNames = _net.getOutputParameterNames();

        for (YVariable local : _net.getLocalVariables().values()) {
            if (StringUtil.isNullOrEmpty(local.getInitialValue())) {

                // output parameters have a mirrored local var created, although they
                // are not true local vars, so any of those need to be ignored
                if (! outputParamNames.contains(local.getName())) {
                    LocalTaskMap localMap = new LocalTaskMap(local);
                    _uninitialisedLocalVars.put(local.getName(), localMap);
                }
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
            if (paramName != null) {
                addTaskIfQueryHasLocalVar(task, paramName, input);
            }    
        }
    }

    private List<YExternalNetElement> resetVisited(YExternalNetElement element) {
        List<YExternalNetElement> newVisited = new ArrayList<YExternalNetElement>();
        newVisited.add(element);
        return newVisited;
    }

    private void addTaskIfQueryHasLocalVar(YTask task, String paramName, boolean input) {
        String query = getQueryForParam(task, paramName, input);
        if (query != null) {
            for (String localVarName : _uninitialisedLocalVars.keySet()) {

                // if this task has an uninit. local task in its mapping query
                if (queryReferencesLocalVar(query, localVarName, input)) {
                    LocalTaskMap taskMap = _uninitialisedLocalVars.get(localVarName);
                    taskMap.add(task, input, paramName);
                }    
            }
        }        
    }


    private boolean queryReferencesLocalVar(String query, String localVarName, boolean input) {
        String mask = getVarMask(localVarName, input);
        return input ? query.contains(mask) : query.startsWith(mask);
    }


    private String getVarMask(String name, boolean input) {
        StringBuilder s = new StringBuilder(30);
        if (input)
            s.append('/').append(_net.getID()).append('/').append(name).append('/');
        else
            s.append('<').append(name).append('>');
        return s.toString();
    }


    private Collection<String> getParamNamesForTask(YTask task, boolean input) {
        return input ? task.getParamNamesForTaskStarting() :
                       task.getParamNamesForTaskCompletion();
    }

    private String getQueryForParam(YTask task, String paramName, boolean input) {
        return input ? task.getDataBindingForInputParam(paramName) :
                       task.getDataBindingForOutputParam(paramName);
    }


    private YVerificationMessage getMessage(LocalTaskMap map, YTask task,
                                            List<YExternalNetElement> visited) {

        YVariable localVar = map.getLocalVar();

        String subMsg;
        String postMsg;
        if (localVar.getDataTypeName().equals("string")) {
            subMsg = "contain an empty string value when the mapping occurs";
            postMsg = "";
        }
        else {
            subMsg = "be uninitialised when the mapping is attempted";
            postMsg = String.format(" Please assign an initial value to '%s' or ensure " +
                    "that a task which precedes task '%s' has a mandatory output " +
                    "mapping to '%s'.", localVar.getName(), task.getName(),
                    localVar.getName() ) ;
        }

        String visitedChain = "[";
        for (int i = visited.size()-1; i >= 0; i--) {
            YExternalNetElement element = visited.get(i);
            if (! ((element instanceof YCondition) && ((YCondition) element).isImplicit())) {
                if (visitedChain.length() > 1) visitedChain += ", ";
                visitedChain += visited.get(i).getID();
            }
        }
        visitedChain += "]";
        
        String msg = String.format("Task '%s' in Net '%s' references Local Variable " +
                "'%s' via an input mapping, however it may be possible for '%s' to %s, via " +
                "the path %s.%s",
                task.getName(), _net.getID(), localVar.getName(), localVar.getName(),
                subMsg, visitedChain, postMsg);

        return new YVerificationMessage(this, msg, YVerificationMessage.WARNING_STATUS);
    }


    /**************************************************************************/

    private class LocalTaskMap {

        private YVariable _localVar;
        private Set<YTask> _inputTasks;
        private Map<YTask, Boolean> _outputTasks;


        public LocalTaskMap(YVariable localVar) {
            _localVar = localVar;
            _inputTasks = new HashSet<YTask>();
            _outputTasks = new Hashtable<YTask, Boolean>();
        }

        public YVariable getLocalVar() { return _localVar; }

        public Set<YTask> getInputTasks() { return _inputTasks; }

        public Set<YTask> getOutputTasks() { return _outputTasks.keySet(); }

        public boolean isInitialisingTask(YTask task) {
            return _outputTasks.containsKey(task) && _outputTasks.get(task);
        }

        public void add(YTask task, boolean input, String paramName) {
            if (! input) {

                // this format required to please hashtable.put
                boolean optional = isOptionalParam(task, paramName);
                _outputTasks.put(task, Boolean.valueOf(! optional));
            }
            else _inputTasks.add(task);
        }


        private boolean isOptionalParam(YTask task, String paramName) {
            if (task instanceof YAtomicTask) {
               YDecomposition decomp = task.getDecompositionPrototype();
                if (decomp != null) {
                    Map<String, YParameter> paramMap = decomp.getOutputParameters();
                    if (paramMap != null) {
                        YParameter param = paramMap.get(paramName);
                        return (param != null) && param.isOptional();
                    }
                }
            }
            return false;
        }
    }
    
}
