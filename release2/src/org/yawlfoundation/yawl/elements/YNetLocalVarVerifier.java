/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.elements;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.YVerificationHandler;

import java.util.*;

/**
 * Walks the net in reverse to discover any task-level input data variables that map from
 * net-level local variables that have no initial value and won't be assigned a value by a
 * task earlier in the net.
 *
 * @author Michael Adams
 * @date 3/05/2010
 */
public class YNetLocalVarVerifier {

    private YNet _net;
    private Map<String, LocalTaskMap> _uninitialisedLocalVars;
    private YVerificationHandler _handler;
    private Logger _log;

    /**
     * Constructor
     * @param net the net to verify
     */
    public YNetLocalVarVerifier(YNet net) {
        _net = net;
        _uninitialisedLocalVars = new Hashtable<String, LocalTaskMap>();
        _log = Logger.getLogger(this.getClass());
    }


    /**
     * Verifies the net
     */
    public void verify(YVerificationHandler handler) {
        long startTime = System.nanoTime();

        _handler = handler;

        // get all local vars that don't have an initial value
        getUnitialisedLocalVars();

        // populate maps with tasks that have any uninitialised locals in mappings
        populateLocalTaskMaps();

        // check all paths for each input task for each local var
        for (LocalTaskMap localTaskMap : _uninitialisedLocalVars.values()) {
            verify(localTaskMap);
        }

        _log.debug("Net: " + _net.getID() + " | Duration: " +
                   (System.nanoTime() - startTime) + "nanoseconds.");
    }


    /**
     * Verifies all tasks with variables that reference a particular uninitialised local
     * variable
     * @param map a map of referencing tasks for a local variable
     */
    private void verify(LocalTaskMap map) {

        // for each affected task, check each of its backward paths to the start
        // condition to see if any tasks on the path output a value to the local var
        for (YTask task : map.getInputTasks()) {
            verify(map, task, task, new ArrayList<YExternalNetElement>(),
                    new Stack<Integer>());
        }

    }


    /**
     * Called recursively to walks a reverse path from a task that has an input mapping
     * from an uninitialised local variable in an attempt to find a task that has an
     * output mapping to that local variable. If it reaches the Input Condition an
     * verification error message is constructed for that particular path.
     * @param map a map of tasks that reference a particular local variable
     * @param subjectTask the originating task being checked
     * @param baseElement the element we're up to in this traversal (changed each recursion)
     * @param visited the list of elements visited so far in this walk
     * @param andStack a stack that keeps track of visited AND splits and joins
     */
    private void verify(LocalTaskMap map, YTask subjectTask,
                       YExternalNetElement baseElement, List<YExternalNetElement> visited,
                       Stack<Integer> andStack) {

        // add the root net element to the set of those visited on this path
        visited.add(baseElement);
        updateAndStack(baseElement, andStack);

        // for each incoming element to the base element (only one if no join)
        for (YExternalNetElement preElement : baseElement.getPresetElements()) {

            _log.debug("Net: " + _net.getID() + " | Local var: " +
                    map.getLocalVar().getPreferredName() +  " | Subject: " +
                    subjectTask.getName() + " | PreElement: " + preElement.toString());

            // discard previously walked paths for an XOR join
            if (isMultiPathXORJoin(baseElement)) unwindOrJoins((YTask) baseElement, visited);

            // if this element has already been visited on this path,
            // it is a loop so don't go further on it
            if (! visited.contains(preElement)) {

                // if we're back to the start of the net, this local var is a problem
                if (preElement instanceof YInputCondition) {

                    // ...unless there are other paths to check from an AND join
                    if (! allPathsWalked(andStack)) continue;

                    // otherwise add an error or warning message for this path
                    visited.add(preElement);
                    addMessage(map, subjectTask, visited);
                    visited = resetVisited(baseElement);
                }
                else if (preElement instanceof YTask) {
                    YTask preTask = (YTask) preElement;

                    // if it is mandatory for the task to output a value for this
                    // local var, then this path is ok - otherwise call recursive
                    if (map.isInitialisingTask(preTask)) {
                        visited = resetVisited(baseElement);                        
                    }
                    else {
                        verify(map, subjectTask, preTask, visited, andStack);
                    }
                }
                else {

                    // a plain condition - call recursive
                    verify(map, subjectTask, preElement, visited, andStack);
                }
            }
        }
    }


    /**
     * Builds the set of uninitialised local variables for this net
     */
    private void getUnitialisedLocalVars() {
        Set<String> outputParamNames = _net.getOutputParameterNames();

        for (YVariable local : _net.getLocalVariables().values()) {

            // if its optional or a complex type with minOccurs=0 then this local var
            // doesn't need a value when its mapped to a task input var
            if (local.isOptional() || ! local.requiresInputValue()) continue;

            // if it needs an initial value but doesn't have one
            if (StringUtil.isNullOrEmpty(local.getInitialValue())) {

                // output parameters have a mirrored local var created, although they
                // are not true local vars, so any of those need to be ignored
                if (! outputParamNames.contains(local.getPreferredName())) {
                    LocalTaskMap localMap = new LocalTaskMap(local);
                    _uninitialisedLocalVars.put(local.getPreferredName(), localMap);
                }
            }
        }
    }


    /**
     * Populates the map created for each uninitialised local variable with the set
     * of tasks that reference it
     */
    private void populateLocalTaskMaps() {
        for (YTask task : _net.getNetTasks()) {
            populateMapsForTask(task, YParameter._INPUT_PARAM_TYPE);     // input params
            populateMapsForTask(task, YParameter._OUTPUT_PARAM_TYPE);    // output params
        }
    }


    /**
     * Adds a task to the map for an uninitialised local variable if the task
     * references the local variable
     * @param task the task to check/add
     * @param paramType one of YParameter._INPUT_PARAM_TYPE or YParameter._OUTPUT_PARAM_TYPE
     */
    private void populateMapsForTask(YTask task, int paramType) {
        for (String paramName : getParamNamesForTask(task, paramType)) {
            if (paramName != null) {
                addTaskIfQueryHasLocalVar(task, paramName, paramType);
            }    
        }
    }


    /**
     * Reinitialises the List of visited elements, then adds the element passed as its
     * first member
     * @param element the element to add to the newly initialised list
     * @return the reinitialised list
     */
    private List<YExternalNetElement> resetVisited(YExternalNetElement element) {
        List<YExternalNetElement> newVisited = new ArrayList<YExternalNetElement>();
        newVisited.add(element);
        return newVisited;
    }


    /**
     * Adds a task to the map of tasks referencing an uninitialised local variable if
     * the task parameter references the local variable via a mapping
     * @param task the task to check/add
     * @param paramName the name of the task variable
     * @param paramType one of YParameter._INPUT_PARAM_TYPE or YParameter._OUTPUT_PARAM_TYPE
     */
    private void addTaskIfQueryHasLocalVar(YTask task, String paramName, int paramType) {
        String query = getQueryForParam(task, paramName, paramType);
        if (query != null) {
            for (String localVarName : _uninitialisedLocalVars.keySet()) {

                // if this task has an uninit. local var in its mapping query
                if (queryReferencesLocalVar(query, localVarName, paramType) ||
                    miTaskOutputsToLocalVar(task, query, localVarName, paramType)) {
                    LocalTaskMap taskMap = _uninitialisedLocalVars.get(localVarName);
                    taskMap.add(task, paramType, paramName);
                }    
            }
        }        
    }


    /**
     * Checks whether the output assignment variable of a multiple instance task maps to a
     * local variable
     * @param task the MI task
     * @param query the output XQuery
     * @param localVarName the local variable name
     * @param paramType one of YParameter._INPUT_PARAM_TYPE or YParameter._OUTPUT_PARAM_TYPE
     * @return true if the output assignment variable of a multiple instance task maps to a
     * local variable
     */
    private boolean miTaskOutputsToLocalVar(YTask task, String query, String localVarName,
                                            int paramType) {
        if (task.isMultiInstance() && (! isInputParamType(paramType))) {
            String outputVar = task.getMIOutputAssignmentVar(query);
            return (outputVar != null) && outputVar.equals(localVarName);
        }
        return false;
    }


    /**
     * Checks whether an XQuery mapping references a local variable
     * @param query the XQuery mapping
     * @param localVarName the local variable name
     * @param paramType one of YParameter._INPUT_PARAM_TYPE or YParameter._OUTPUT_PARAM_TYPE
     * @return the XQuery mapping references a local variable
     */
    private boolean queryReferencesLocalVar(String query, String localVarName, int paramType) {
        String mask = getVarMask(localVarName, paramType);
        return isInputParamType(paramType) ? query.contains(mask) : query.startsWith(mask);
    }


    /**
     * Builds a template to use to search an XQuery mapping for a local variable name
     * @param name the name of the local variable to search the XQuery for
     * @param paramType one of YParameter._INPUT_PARAM_TYPE or YParameter._OUTPUT_PARAM_TYPE
     * @return the search template
     */
    private String getVarMask(String name, int paramType) {
        StringBuilder s = new StringBuilder(30);
        if (isInputParamType(paramType))

            // an input mapping contains an XPath of the net and variable name
            s.append('/').append(_net.getID()).append('/').append(name).append('/');
        else

            // an output mapping starts with an xml element of the variable's name
            s.append('<').append(name).append('>');

        return s.toString();
    }


    /**
     * Gets the list of variable names for a task
     * @param task the task
     * @param paramType the type of variables concerned (input or output)
     * @return the list of variables of the task of the type
     */
    private Collection<String> getParamNamesForTask(YTask task, int paramType) {
            return isInputParamType(paramType) ?
                    task.getParamNamesForTaskStarting() :
                    task.getParamNamesForTaskCompletion();
    }


    /**
     * Gets the XQuery mapping for a task variable
     * @param task the task containing the variable
     * @param paramName the name of the variable
     * @param paramType the variable usage type (input or output)
     * @return the XQuery mapping
     */
    private String getQueryForParam(YTask task, String paramName, int paramType) {
        return isInputParamType(paramType) ?
                task.getDataBindingForInputParam(paramName) :
                task.getDataBindingForOutputParam(paramName);
    }


    /**
     * Checks if the paramType is the input param type
     * @param paramType the type to check
     * @return true if its the input param type
     */
    private boolean isInputParamType(int paramType) {
        return paramType == YParameter._INPUT_PARAM_TYPE;
    }


    /**
     * Checks if an element is an XOR-join task with multiple inflows
     * @param element the element to check
     * @return true if the element is an XOR-join task with multiple inflows
     */
    private boolean isMultiPathXORJoin(YExternalNetElement element) {
        return (element instanceof YTask) &&
                ((YTask) element).getJoinType() == YTask._XOR &&
                element.getPresetElements().size() > 1;
    }


    /**
     * For XOR-join tasks with multiple incoming flows, discard a previously walked
     * path from it
     * @param task the task with the XOR join
     * @param visited the list of visited elements
     */
    private void unwindOrJoins(YTask task,
                               List<YExternalNetElement> visited) {
        Set<YExternalNetElement> toDiscard = new HashSet<YExternalNetElement>();

        // discard all visited elements back to the XOR-join
        for (int i = visited.size()-1; i >= 0; i--) {
            YExternalNetElement elemVisited = visited.get(i);
            if (elemVisited.equals(task)) break;              // met the join, so done
            toDiscard.add(elemVisited);
        }
        for (YExternalNetElement elemVisited : toDiscard) {
            visited.remove(elemVisited);
        }
    }


    /**
     * Updates the stack of paths walked between an AND split and its corresponding AND join
     * @param element the task or condition to check for an AND decoration
     * @param stack the stack of paths
     */
    private void updateAndStack(YExternalNetElement element, Stack<Integer> stack) {
        if (element instanceof YTask) {
            YTask task = (YTask) element;
            if (task.getSplitType() == YTask._AND) {
                int outFlowCount = task.getPostsetElements().size();

                // if this split has multiple outflows, decrement (since we're reversing)
                if ((outFlowCount > 1) && (! stack.isEmpty())) {
                    stack.push(stack.pop() - 1);                // decrement and replace
                }
            }
            if (task.getJoinType() == YTask._AND) {
                int inFlowCount = task.getPresetElements().size();

                // if this join has multiple inflows, push the flow count on the stack
                if (inFlowCount > 1) {
                    stack.push(task.getPresetElements().size());
                }    
            }
        }
    }


    /**
     * Checks the stack of paths incoming to an AND join to determine if they have all
     * been walked
     * @param stack the stack of paths leading to an AND join
     * @return true if all paths have been walked
     */
    private boolean allPathsWalked(Stack<Integer> stack) {
        if (stack.isEmpty()) return true;
        if (stack.peek() == 0) {
            stack.pop();
            return true;
        }
        return false;
    }


    /**
     * Constructs a verification (error) message for a particular path
     * @param map the map of tasks that reference a local variable
     * @param task the originating task of a particular (reverse) path
     * @param visited a list of tasks and conditions met along the (reverse) path
     *                from the task to the Input Condition
     * @return the constructed message
     */
    private void addMessage(LocalTaskMap map, YTask task,
                            List<YExternalNetElement> visited) {

        YVariable localVar = map.getLocalVar();

        // tailor the message for string types (a warning, since an empty string value
        // will validate at runtime) and other types (error, since validation will fail)
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
                    "mapping to '%s'.", localVar.getPreferredName(), task.getName(),
                    localVar.getPreferredName() ) ;
        }

        // construct the chain of visited elements
        String visitedChain = "[";
        for (int i = visited.size()-1; i >= 0; i--) {
            YExternalNetElement element = visited.get(i);

            // ignore implicit conditions
            if (! ((element instanceof YCondition) && ((YCondition) element).isImplicit())) {
                if (visitedChain.length() > 1) visitedChain += ", ";
                visitedChain += visited.get(i).getID();
            }
        }
        visitedChain += "]";

        // put it all together
        String msg = String.format("Task '%s' in Net '%s' references Local Variable " +
                "'%s' via an input mapping, however it may be possible for '%s' to %s, via " +
                "the path %s.%s",
                task.getName(), _net.getID(), localVar.getPreferredName(),
                localVar.getPreferredName(), subMsg, visitedChain, postMsg);

        // add it to the handler
        if (postMsg.equals("")) {         // a warning
           _handler.warn(_net, msg);
        }
        else _handler.error(_net, msg);
    }


    /**************************************************************************/

    /**
     * Stores the set of input and output tasks that reference a specific net-level
     * uninitialised local variable
     */
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

        /**
         * Checks whether a task will map a value to the local variable
         * @param task the task to check
         * @return true if this task maps a value
         */
        public boolean isInitialisingTask(YTask task) {
            return _outputTasks.containsKey(task) && _outputTasks.get(task);
        }


        /**
         * Adds a task to the set of tasks that reference the local variable
         * @param task the task to add
         * @param paramType one of YParameter._INPUT_PARAM_TYPE or
         *                  YParameter._OUTPUT_PARAM_TYPE
         * @param paramName the name of the variable
         */
        public void add(YTask task, int paramType, String paramName) {
            if (isInputParamType(paramType)) {
                _inputTasks.add(task);
            }
            else {

                // this construct required to please hashtable.put
                boolean optional = isOptionalParam(task, paramName);
                _outputTasks.put(task, Boolean.valueOf(! optional));
            }
        }


        /**
         * Checks if a variable is optional (i.e. requires an output value)
         * @param task the task that contains the variable
         * @param paramName the name of the variable
         * @return true if this variable is optional
         */
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
