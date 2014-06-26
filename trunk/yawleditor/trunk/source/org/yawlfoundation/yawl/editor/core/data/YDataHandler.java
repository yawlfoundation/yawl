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

package org.yawlfoundation.yawl.editor.core.data;

import org.jdom2.Namespace;
import org.yawlfoundation.yawl.elements.*;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.logging.YLogPredicate;
import org.yawlfoundation.yawl.util.StringUtil;

import java.util.*;

/**
 * This class abstracts and handles all data perspective operations.
 *
 * @author Michael Adams
 * @date 16/08/12
 */
public class YDataHandler {

    private YSpecification _specification;
    private DataUtil _utils;

    public static final int LOCAL = -1;
    public static final int INPUT = YParameter._INPUT_PARAM_TYPE;       // 0
    public static final int OUTPUT = YParameter._OUTPUT_PARAM_TYPE;     // 1
    public static final int INPUT_OUTPUT = 2;



    /**
     * Creates a new data handler
     */
    public YDataHandler() { }


    /**
     * Creates a new data handler for a specified YAWL specification
     * @param specification the specification that this handler will work with
     */
    public YDataHandler(YSpecification specification) {
        setSpecification(specification);
    }


    /**
     * Gets the current specification
     * @return the specification that this handler is working with
     */
    public YSpecification getSpecification() { return _specification; }


    /**
     * Sets the current specification
     * @param specification the specification that this handler will work with
     */
    public void setSpecification(YSpecification specification) {
        _specification = specification;
        if (_specification != null) {
            _utils = new DataUtil(_specification.getDataSchema());
        }
    }


    /**
     * Disassociates this object from a specification
     */
    public void close() {
        _specification = null;
        _utils = null;
    }


    /**
     * Updates the id of a net or task decomposition
     * @param currentID the old net or task decomposition id
     * @param newID the new net or task decomposition id
     * @throws YDataHandlerException if there is no specification associated with this
     * handler, or it has no decomposition with the specified id
     */
    public YDecomposition renameDecomposition(String currentID, String newID)
            throws YDataHandlerException {
        checkSpecificationExists();
        YDecomposition current = getDecomposition(currentID);   // guaranteed not null
        _specification.removeDecomposition(currentID);
        current.setID(newID);
        _specification.addDecomposition(current);
        updateDecompositionReferences(currentID, newID);
        return current;
    }


    /**
     * Updates all references to a net or task decomposition in task mappings, when
     * the net or decomposition has had a name change
     * @param oldID the old net or task decomposition id
     * @param newID the new net or task decomposition id
     * @throws YDataHandlerException if there is no specification associated with this
     * handler, or it has no decomposition with the specified id
     */
    public void updateDecompositionReferences(String oldID, String newID)
            throws YDataHandlerException {
        YDecomposition decomposition = getDecomposition(newID);
        if (decomposition instanceof YNet) {
            updateInputReferencesToNet((YNet) decomposition, oldID);
        }
        else {
            updateOutputReferencesToDecomposition(decomposition, oldID);
        }
    }


    /**
     * Removes a decomposition from the current specification. Also removes all task
     * output mappings that refer to it.
     * @param decompositionID the id of the decomposition pending removal
     * @throws YDataHandlerException if there is no specification associated with this
     * handler, or it has no decomposition with the specified id
     */
    public void removeOutputReferencesToDecomposition(String decompositionID)
            throws YDataHandlerException {
        YDecomposition decomposition = getDecomposition(decompositionID);
        removeOutputReferencesToDecomposition(decomposition);
        _specification.getDecompositions().remove(decomposition);
    }


    /**
     * Removes a variable from the set of variables of a net or task decomposition.
     * If the decompositionID refers to a net, any task mappings that input from or
     * output to the variable are also removed.
     * @param decompositionID the id of the containing net or task decomposition
     * @param variableName the name of the variable to remove
     * @param scope one of INPUT, OUTPUT or LOCAL
     * @throws YDataHandlerException if there is no specification associated with this
     * handler, or it has no decomposition with the specified id, or the 'scope'
     * parameter is LOCAL and the referenced decomposition is not a net, or the 'scope'
     * parameter is invalid
     */
    public void removeVariable(String decompositionID, String variableName, int scope)
            throws YDataHandlerException {
        checkParameterType(scope);
        YDecomposition decomposition = getDecomposition(decompositionID);
        if (decomposition instanceof YNet) {
            removeNetVariable((YNet) decomposition, variableName);
        }
        else {
            if (scope == LOCAL) raise("No net found with id: " + decompositionID);
            removeTaskDecompositionVariable(decomposition, variableName, scope);
        }
    }


    /**
     * Renames a variable contained by a net or task decomposition. Any task mappings
     * that reference the variable are updated.
     * @param decompositionID the id of the net or task decomposition
     * @param oldName the current name of the variable
     * @param newName the new name to assign to the variable
     * @param scope one of INPUT, OUTPUT or LOCAL
     * @throws YDataHandlerException if there is no specification associated with this
     * handler, or it has no decomposition with the specified id, or the 'scope'
     * parameter is LOCAL and the referenced decomposition is not a net, or the 'scope'
     * parameter is invalid
     */
    public void renameVariable(String decompositionID, String oldName, String newName,
                               int scope) throws YDataHandlerException {
        checkParameterType(scope);
        YDecomposition decomposition = getDecomposition(decompositionID);
        if (decomposition instanceof YNet) {
            renameNetVariable((YNet) decomposition, oldName, newName);
        }
        else {
            if (scope == LOCAL) raise("No net found with id: " + decompositionID);
            renameTaskDecompositionVariable(getDecomposition(decompositionID),
                    oldName,  newName, scope);
        }
    }


    /**
     * Sets the sorting index of an input or output variable for either a net or a task
     * decomposition
     * @param decompositionID the id of the containing net or task decomposition
     * @param variableName the name of the variable to set
     * @param scope one of INPUT, OUTPUT or LOCAL
     * @param index the non-negative index to set
     * @return true if successful, false if the variable doesn't exist
     * @throws YDataHandlerException if there is no specification associated with this
     * handler, or it has no net or task decomposition with the specified id, or the
     * 'scope' parameter is invalid, or the 'index' parameter is negative
     */
    public boolean setVariableIndex(String decompositionID, String variableName,
                                       int scope, int index) throws YDataHandlerException {
        checkParameterType(scope);
        if (index < 0) raise("Index must be 0 or greater");
        return setVariableOrdering(getDecomposition(decompositionID), variableName,
                scope, index);
    }


    /**
     * Sets the initial value for a local (net-level) variable
     * @param netID the id of the net containing the local variable
     * @param variableName the name of the local variable
     * @param value the initial value
     * @throws YDataHandlerException if there is no specification associated with this
     * handler, or it has no net decomposition with the specified id, or there is no
     * local variable found with the specified name
     */
    public boolean setInitialValue(String netID, String variableName, String value)
            throws YDataHandlerException {
        YNet net = getNet(netID);
        YVariable localVar = net.getLocalVariables().get(variableName);
        if (localVar != null) {
            validateValue(localVar, value);
            localVar.setInitialValue(value);
        }
        else raise("No local variable found named: " + variableName +
                    ". Only a local variable can have an initial value.");

        return true;
    }


    /**
     * Sets the default value for an output-only variable (i.e. an output variable
     * without a corresponding input variable)
     * @param decompositionID the net or task decompositionID containing the variable
     * @param variableName the name of the variable
     * @param value the default value
     * @throws YDataHandlerException if there is no specification associated with this
     * handler, or it has no net or task decomposition with the specified id, or the
     * decomposition has no output-only parameter of that name
     */
    public boolean setDefaultValue(String decompositionID, String variableName, String value)
            throws YDataHandlerException {
        YDecomposition decomposition = getDecomposition(decompositionID);
        YVariable inputVar = decomposition.getInputParameters().get(variableName);
        YVariable outputVar = decomposition.getOutputParameters().get(variableName);
        if (inputVar == null && outputVar != null) {
            validateValue(outputVar, value);
            outputVar.setInitialValue(value);
        }
        else raise("No output-only variable found named: " + variableName +
                    ". Only an output-only variable can have an default value.");

        return true;
    }


    /**
     * Creates a new variable and adds it to a net or task decomposition. Note: An output
     * only variable requires a 'shadow' local variable to be created also.
     * @param decompositionID the id of the net or task decomposition
     * @param name the variable name
     * @param dataType  the variable data type
     * @param namespace the data type's namespace
     * @param scope one of LOCAL, INPUT, OUTPUT or INPUT_OUTPUT (scope INPUT_OUTPUT will
     *             result in the creation of two variables, one INPUT and one OUTPUT)
     * @param value the initial (if LOCAL) or default (if OUTPUT only) value to assign
     *              to the variable. The value is ignored if the scope is not LOCAL or
     *              OUTPUT only. May be null.
     * @param attributes the extended attributes for the variable. May be null.
     * @throws YDataHandlerException if there is no specification associated with this
     * handler, or it has no net or task decomposition with the specified id, or the
     * 'scope' parameter is invalid, or the net or task decomposition already has a
     * variable of that name
     */
    public void addVariable(String decompositionID, String name, String dataType,
                            String namespace, int scope, String value,
                            YAttributeMap attributes)
            throws YDataHandlerException {
        checkParameterType(scope);
        YDecomposition decomposition = getDecomposition(decompositionID);
        checkVariableNameUnique(decomposition, name, scope);
        if (scope == LOCAL || ((decomposition instanceof YNet) && scope == OUTPUT)) {
            YVariable localVar = new YVariable(decomposition);
            localVar.setDataTypeAndName(dataType, name, namespace);
            localVar.setInitialValue(value);
            ((YNet) decomposition).setLocalVariable(localVar);
        }
        if (scope == INPUT || scope == INPUT_OUTPUT) {
            YParameter parameter = new YParameter(decomposition, INPUT);
            parameter.setDataTypeAndName(dataType, name, namespace);
            if (attributes != null) parameter.setAttributes(attributes);
            decomposition.addInputParameter(parameter);
        }
        if (scope == OUTPUT || scope == INPUT_OUTPUT) {
            YParameter parameter = new YParameter(decomposition, OUTPUT);
            parameter.setDataTypeAndName(dataType, name, namespace);
            if (attributes != null) parameter.setAttributes(attributes);
            decomposition.addOutputParameter(parameter);
            if (! isInputOutput(decomposition, name, dataType)) {
                parameter.setDefaultValue(value);
            }
        }
    }


    /**
     * Changes the scope of a variable.
     * @param decompositionID the id of the net or task decomposition
     * @param variableName the name of the variable to change
     * @param oldScope the variable's current scope, one of LOCAL, INPUT, OUTPUT or
     *                 INPUT_OUTPUT
     * @param newScope the scope to set the variable to, one of LOCAL, INPUT, OUTPUT or
     *                 INPUT_OUTPUT
     * @return true if successful, false if a variable of that name doesn't exist
     * @throws YDataHandlerException if there is no specification associated with this
     * handler, or it has no net or task decomposition with the specified id, or the
     * 'oldScope' parameter is invalid, or the 'newScope' parameter is invalid
     */
    public boolean changeVariableScope(String decompositionID, String variableName,
                                int oldScope, int newScope) throws YDataHandlerException {
        checkParameterType(oldScope);
        checkParameterType(newScope);
        YDecomposition decomposition = getDecomposition(decompositionID);
        if ((oldScope == LOCAL || newScope == LOCAL) && ! (decomposition instanceof YNet)) {
            raise("No net found with id: " + decompositionID);
        }
        switch (oldScope) {
            case LOCAL: return changeLocalScope(decomposition, variableName, newScope);
            case INPUT: return changeInputScope(decomposition, variableName, newScope);
            case OUTPUT: return changeOutputScope(decomposition, variableName, newScope);
            case INPUT_OUTPUT: return changeInputOutputScope(decomposition, variableName,
                    newScope);
        }
        return false;
    }


    /**
     * Changes the data type of a variable.
     * @param decompositionID the id of the net or task decomposition
     * @param variableName the name of the variable to change
     * @param dataType the data type to set the variable to
     * @param scope one of LOCAL, INPUT, OUTPUT or INPUT_OUTPUT
     * @return true if successful, false if a variable of that name doesn't exist
     * @throws YDataHandlerException if there is no specification associated with this
     * handler, or it has no net or task decomposition with the specified id, or the
     * 'dataType' parameter is invalid, or the variable has a value that is incompatible
     * with the new data type, or the 'scope' parameter is invalid
     */
    public boolean setVariableDataType(String decompositionID, String variableName,
                        String dataType, int scope) throws YDataHandlerException {
        checkParameterType(scope);
        YDecomposition decomposition = getDecomposition(decompositionID);
        boolean success = true;
        if (scope == LOCAL) {
            if (decomposition instanceof YNet) {
                YVariable variable = ((YNet) decomposition).getLocalVariables()
                                        .get(variableName);
                if (variable == null) {
                    throw new YDataHandlerException("No variable found with name: " +
                            variableName);
                }
                return setVariableDataType(variable, dataType, variable.getInitialValue());
            }
            else raise("Invalid parameter scope for task decomposition: " +
                    getScopeName(scope));
        }
        else {
            if (scope == INPUT || scope == INPUT_OUTPUT) {
                success = setVariableDataType(
                        decomposition.getInputParameters().get(variableName), dataType, null);
            }
            if (scope == OUTPUT || scope == INPUT_OUTPUT) {
                YVariable variable = decomposition.getOutputParameters().get(variableName);
                if (variable == null) {
                    throw new YDataHandlerException("No variable found with name: " +
                            variableName);
                }
                success = success && setVariableDataType(
                        decomposition.getOutputParameters().get(variableName), dataType,
                        variable.getDefaultValue());
            }
        }
        return success;
    }


    /**
        * Changes the data type and value of a variable at the same time.
        * @param decompositionID the id of the net or task decomposition
        * @param variableName the name of the variable to change
        * @param dataType the data type to set the variable to
        * @param scope one of LOCAL, INPUT, OUTPUT or INPUT_OUTPUT
        * @param value the new value
        * @return true if successful, false if a variable of that name doesn't exist
        * @throws YDataHandlerException if there is no specification associated with this
        * handler, or it has no net or task decomposition with the specified id, or the
        * 'dataType' parameter is invalid, or the value is incompatible with the new data
        * type, or the 'scope' parameter is invalid
        */
       public boolean setVariableDataTypeAndValue(String decompositionID, String variableName,
                           String dataType, String value, int scope) throws YDataHandlerException {
           checkParameterType(scope);
           YDecomposition decomposition = getDecomposition(decompositionID);
           boolean success = true;
           if (scope == LOCAL) {
               if (decomposition instanceof YNet) {
                   YVariable variable = ((YNet) decomposition).getLocalVariables()
                                           .get(variableName);
                   if (variable == null) {
                       throw new YDataHandlerException("No local variable found with name: " +
                               variableName);
                   }
                   setVariableDataType(variable, dataType, value);
                   variable.setInitialValue(value);
               }
               else raise("Invalid parameter scope for task decomposition: " +
                       getScopeName(scope));
           }
           else {
               if (scope == INPUT || scope == INPUT_OUTPUT) {
                   success = setVariableDataType(
                           decomposition.getInputParameters().get(variableName), dataType, null);
               }
               if (scope == OUTPUT || scope == INPUT_OUTPUT) {
                   YVariable variable = decomposition.getOutputParameters().get(variableName);
                   if (variable == null) {
                       throw new YDataHandlerException("No output variable found with name: " +
                               variableName);
                   }
                   success = success && setVariableDataType(variable, dataType, value);
                   if (success) variable.setDefaultValue(value);
               }
           }
           return success;
       }


    /**
      * Sets or updates the extended attributes of a task-level variable.
      * @param decompositionID the id of the net or task decomposition
      * @param variableName the name of the variable to change
      * @param attributes the attributes to set for the variable
      * @param scope one of INPUT, OUTPUT or INPUT_OUTPUT
      * @return true if successful, false if a task-level variable of that name doesn't exist
      * @throws YDataHandlerException if there is no specification associated with this
      * handler, or it has no net or task decomposition with the specified id, or the
      * 'scope' parameter is invalid
      */
    public boolean setVariableAttributes(String decompositionID, String variableName,
                      YAttributeMap attributes, int scope) throws YDataHandlerException {
        checkParameterType(scope);
        YDecomposition decomposition = getDecomposition(decompositionID);
        boolean success = true;
        if (scope == INPUT || scope == INPUT_OUTPUT) {
            success = setVariableAttributes(
                    decomposition.getInputParameters().get(variableName), attributes);
        }
        if (scope == OUTPUT || scope == INPUT_OUTPUT) {
            success = success && setVariableAttributes(
                    decomposition.getOutputParameters().get(variableName), attributes);
        }
        return success;
    }


    /**
      * Sets or updates the log predicate of a task-level variable.
      * @param decompositionID the id of the net or task decomposition
      * @param variableName the name of the variable to change
      * @param predicate the log predicate to set for the variable
      * @param scope one of INPUT, OUTPUT or INPUT_OUTPUT
      * @return true if successful, false if a task-level variable of that name doesn't exist
      * @throws YDataHandlerException if there is no specification associated with this
      * handler, or it has no net or task decomposition with the specified id, or the
      * 'scope' parameter is invalid
      */
    public boolean setVariableLogPredicate(String decompositionID, String variableName,
                       YLogPredicate predicate, int scope) throws YDataHandlerException {
         checkParameterType(scope);
         YDecomposition decomposition = getDecomposition(decompositionID);
         boolean success = true;
         if (scope == INPUT || scope == INPUT_OUTPUT) {
             success = setVariableLogPredicate(
                     decomposition.getInputParameters().get(variableName), predicate);
         }
         if (scope == OUTPUT || scope == INPUT_OUTPUT) {
             success = success && setVariableLogPredicate(
                     decomposition.getOutputParameters().get(variableName), predicate);
         }
         return success;
    }


    /**
     * Sets a mapping for a task variable
     * @param netID the id of the task's containing net
     * @param taskID the id of the task
     * @param variableName the name of the variable to set the mapping for
     *                     (task variable name for INPUT, net variable name for OUTPUT)
     * @param mapping the mapping to set
     * @param scope one of INPUT or OUTPUT
     * @throws YDataHandlerException if there is no specification associated with this
     * handler, or it has no net decomposition with the specified id, or there is no task
     * found with the specified id, or the 'scope' parameter is invalid
     */
    public void setVariableMapping(String netID, String taskID,
                                   String variableName, String mapping, int scope)
            throws YDataHandlerException {
        checkParameterType(scope);
        YNet net = getNet(netID);
        YTask task = (YTask) net.getNetElement(taskID);
        if (task != null) {
            if (scope == LOCAL) {
                raise("Invalid scope parameter: " + scope);
            }
            if (scope == INPUT || scope == INPUT_OUTPUT) {
                task.setDataBindingForInputParam(mapping, variableName);
            }
            if (scope == OUTPUT || scope == INPUT_OUTPUT) {
                task.setDataBindingForOutputExpression(mapping, variableName);
            }
        }
        else raise("No task found with id: " + taskID);
    }


    /**
     * Sets a multi-instance query for a task - splitter query if scope is INPUT or
     * joiner query if scope is OUTPUT
     * @param netID the id of the task's containing net
     * @param taskID the id of the task
     * @param variableName the name of the multi-instance variable to set the query for
     * @param miQuery the query to set
     * @param scope one of INPUT or OUTPUT
     * @throws YDataHandlerException if there is no specification associated with this
     * handler, or it has no net decomposition with the specified id, or there is no task
     * found with the specified id, or the 'scope' parameter is invalid
     */
    public void setMIQuery(String netID, String taskID, String variableName,
                           String miQuery, int scope)
            throws YDataHandlerException {
        checkParameterType(scope);
        YNet net = getNet(netID);
        YTask task = (YTask) net.getNetElement(taskID);
        if (task != null) {
            task.getMultiInstanceAttributes().setMIFormalInputParam(variableName);
            if (scope == INPUT || scope == INPUT_OUTPUT) {
                task.getMultiInstanceAttributes().setUniqueInputMISplittingQuery(miQuery);
            }
            if (scope == OUTPUT || scope == INPUT_OUTPUT) {
                task.getMultiInstanceAttributes().setUniqueOutputMIJoiningQuery(miQuery);
            }
            else raise("Invalid scope parameter: " + scope);
        }
        else raise("No task found with id: " + taskID);
    }


    /**
     * A static method to convert a scope identifier into its corresponding name
     * @param scope the scope identifier
     * @return the name for that scope, or the empty String if 'scope' is invalid
     */
    public static String getScopeName(int scope) {
        switch (scope) {
            case LOCAL : return "Local";
            case INPUT : return "Input";
            case OUTPUT : return "Output";
            case INPUT_OUTPUT : return "InputOutput";
        }
        return "";
    }


    /**
     * A static method to get all scope names
     * @return a list of all the current scope names
     */
    public static List<String> getScopeNames() {
        List<String> names = new ArrayList<String>();
        for (int i = LOCAL; i <= INPUT_OUTPUT; i++) names.add(getScopeName(i));
        return names;
    }


    /*******************************************************************************/

    /**
     * Removes a variable from the set of variables for a net. Any task mappings that
     * input from or output to the variable are also removed.
     * @param net the containing net
     * @param variableName the name of the variable to remove
     */
    private void removeNetVariable(YNet net, String variableName) {

        // an output param may mean an I/O, an output only or a shadow for a local
        YParameter outputParam = net.getOutputParameters().get(variableName);

        // if local, process removal then delete its shadow output param
        YVariable variable = net.getLocalVariables().get(variableName);
        if (variable != null) {
            removeNetVariable(net, variable);
            if (outputParam != null) net.getOutputParameters().remove(variableName); // shadow
            return;
        }

        // if input, process its removal then if its an I/O delete the output param
        YParameter inputParam = net.getInputParameters().get(variableName);
        if (inputParam != null) {
            removeNetVariable(net, inputParam);
            if (outputParam != null) net.getOutputParameters().remove(variableName);  // I/O
        }

        // else output only, so process the output param's removal
        else if (outputParam != null) removeNetVariable(net, outputParam);
    }


    /**
     * Removes a variable from the set of variables for a net. Any task mappings that
     * input from or output to the variable are also removed.
     * @param net the containing net
     * @param variable the variable to remove
     */
    private void removeNetVariable(YNet net, YVariable variable) {
        if (variable == null) return;

        if (variable instanceof YParameter) {                           // in or out
            YParameter parameter = (YParameter) variable;
            if (parameter.getParamType() == INPUT) {
                net.removeInputParameter(parameter);
            }
            else if (parameter.getParamType() == OUTPUT) {
                net.removeOutputParameter(parameter);
            }
        }
        else {
            net.removeLocalVariable(variable.getPreferredName());
        }

        removeReferencingTaskMappings(net, variable.getPreferredName());
    }


    /**
     * Renames a variable within the set of variables for a net. Any task mappings that
     * input from or output to the variable are updated.
     * @param net the containing net
     * @param oldName the current name of the variable
     * @param newName the new name to assign to the variable
     * @return true if rename is successful, false if there was no variable found with
     * the name 'oldName'
     */
    private boolean renameNetVariable(YNet net, String oldName, String newName) {
        boolean renamed = false;
        YVariable variable = net.getLocalVariables().get(oldName);
        if (variable != null) {
            net.removeLocalVariable(oldName);
            renameVariable(variable, newName);
            net.setLocalVariable(variable);
            renamed = true;
        }
        renamed = renameInputParameter(net, oldName, newName) || renamed;
        renamed = renameOutputParameter(net, oldName, newName) || renamed;
        if (renamed) updateReferencingTaskMappings(net, oldName, newName);
        return renamed;
    }


    /**
     * Removes a variable from a task decomposition. Any task mappings that reference
     * the variable are also removed.
     * @param decomposition the decomposition to remove the variable from
     * @param variableName the name of the variable to remove
     * @param scope one of INPUT or OUTPUT
     */
    private void removeTaskDecompositionVariable(YDecomposition decomposition,
                                   String variableName, int scope) {
        Set<YTask> affectedTasks = getTasks(decomposition);
        if (scope == INPUT) {
            decomposition.getInputParameters().remove(variableName);
            for (YTask task : affectedTasks) {
                task.getDataMappingsForTaskStarting().remove(variableName);
            }
        }
        else {
            decomposition.getOutputParameters().remove(variableName);
            for (YTask task : affectedTasks) {
                for (String key : getCompletionMappingKeysForTaskVar(task, variableName)) {
                    task.getDataMappingsForTaskCompletion().remove(key);
                }
            }
        }
    }


    /**
     * Renames a variable contained by a task decomposition. Any task mappings that
     * reference the variable are updated.
     * @param decomposition the decomposition containing the variable
     * @param oldName the current name of the variable
     * @param newName the new name to assign to the variable
     * @param scope one of INPUT or OUTPUT
     */
    private void renameTaskDecompositionVariable(YDecomposition decomposition,
                                   String oldName, String newName, int scope) {
        Set<YTask> affectedTasks = getTasks(decomposition);
        if (scope == INPUT) {
            renameInputParameter(decomposition, oldName, newName);
            for (YTask task : affectedTasks) {
                String query = task.getDataMappingsForTaskStarting().remove(oldName);
                if (query != null) {
                    task.setDataBindingForInputParam(renameOuterTags(query, newName), newName);
                }
                if (task.isMultiInstance()) {

                    // task variables only appear in MI splitter queries
                    replaceTaskVariableInMISplitterQuery(task, oldName, newName);
                }
            }
        }
        else {
            renameOutputParameter(decomposition, oldName, newName);
            for (YTask task : affectedTasks) {
                Set<String> removeKeys = getCompletionMappingKeysForTaskVar(task, oldName);
                for (String outputQuery : removeKeys) {
                    task.getDataMappingsForTaskCompletion().put(
                            amendQuery(outputQuery, oldName, newName),
                            task.getDataMappingsForTaskCompletion().get(outputQuery));
                }
                removeTaskMappings(task.getDataMappingsForTaskCompletion(), removeKeys);
            }
        }
    }


    /**
     * Sets the sorting index of an input or output variable for either a net or a task
     * decomposition
     * @param decomposition the containing net or task decomposition
     * @param variableName the name of the variable to set
     * @param index the non-negative index to set
     * @return true if successful, false if the variable doesn't exist
     */
    private boolean setVariableOrdering(YDecomposition decomposition, String variableName,
                                       int scope, int index) {
        YVariable variable = null;
        switch (scope) {
            case LOCAL:
                variable = ((YNet) decomposition).getLocalVariables().get(variableName);
                break;
            case INPUT:
                variable = decomposition.getInputParameters().get(variableName);
                break;
            case OUTPUT:
                variable = decomposition.getOutputParameters().get(variableName);
               break;
        }
        return setVariableOrdering(variable, index);
    }


    /**
     * Sets the sorting index of a variable
     * @param variable the variable to set
     * @param index the non-negative index to set
     * @return true if successful, false if the variable doesn't exist
     */
    private boolean setVariableOrdering(YVariable variable, int index) {
        if (variable != null) variable.setOrdering(index);
        return variable != null;
    }


    /**
     * Deletes any task mappings that reference a specified Net-level variable
     * @param net the containing net
     * @param name the name of the net-level variable to search for
     */
    private void removeReferencingTaskMappings(YNet net, String name) {
        for (YTask task : net.getNetTasks()) {
            Set<String> removeKeys = getStartMappingKeysForNetVar(task, name);
            removeTaskMappings(task.getDataMappingsForTaskStarting(), removeKeys);

            removeKeys = getCompletionMappingKeysForNetVar(task, name);
            removeTaskMappings(task.getDataMappingsForTaskCompletion(), removeKeys);

            if (task.getSplitType() != YTask._AND) {
                removeReferencingFlowPredicates(task.getPostsetFlows(), name);
            }
        }
    }


    /**
     * Updates task mappings following the renaming of a net-level variable
     * @param net the containing net
     * @param oldName the current name of the net-level variable
     * @param newName the new name of the net-level variable
     */
    private void updateReferencingTaskMappings(YNet net, String oldName, String newName) {
        for (YTask task : net.getNetTasks()) {
            for (String taskVarName : getStartMappingKeysForNetVar(task, oldName)) {
                String inputQuery = task.getDataMappingsForTaskStarting().get(taskVarName);

                // overwrite entries - keys don't change
                task.getDataMappingsForTaskStarting().put(taskVarName,
                        amendQuery(inputQuery, oldName, newName));
            }

            Set<String> removeKeys = getCompletionMappingKeysForNetVar(task, oldName);
            for (String outputQuery : removeKeys) {

                // add new entries - keys have changed
                task.getDataMappingsForTaskCompletion().put(
                        renameOuterTags(outputQuery, newName), newName);
            }

            // remove old keys
            removeTaskMappings(task.getDataMappingsForTaskCompletion(), removeKeys);

            if (task.getSplitType() != YTask._AND) {
                amendReferencingFlowPredicates(task.getPostsetFlows(), oldName, newName);
            }
            if (task.isMultiInstance()) {
                replaceNetVariableInMIQueries(task, oldName, newName);
            }
        }
    }


    /**
     * Updates all task input mappings that reference the specified net, after the net
     * has had a name change
     * @param net the net that has had a name change
     * @param oldID the name it used to have before the change
     */
    private void updateInputReferencesToNet(YNet net, String oldID) {
        String searchKey = '{' + xpathDelimit(oldID);
        for (YTask task : net.getNetTasks()) {
            Map<String, String> mappings = task.getDataMappingsForTaskStarting();
            Map<String,String> replacements = new HashMap<String, String>();
            for (String taskVarName : mappings.keySet()) {
                String inputQuery = mappings.get(taskVarName);
                if (inputQuery.contains(searchKey)) {
                    replacements.put(taskVarName, amendQuery(inputQuery, oldID, net.getID()));
                }
            }
            mappings.putAll(replacements);

            if (task.getSplitType() != YTask._AND) {
                amendReferencingFlowPredicates(task.getPostsetFlows(), oldID, net.getID());
            }
        }
    }


    /**
     * Updates all task output mappings that reference the specified decomposition, after
     * the decomposition has had a name change
     * @param decomposition the decomposition that has had a name change
     * @param oldID the name it used to have before the change
     */
    private void updateOutputReferencesToDecomposition(YDecomposition decomposition,
                                                       String oldID) {
        String searchKey = '{' + xpathDelimit(oldID);
        for (YTask task : getTasks(decomposition)) {
            Map<String, String> mappings = task.getDataMappingsForTaskCompletion();
            Map<String,String> replacements = new HashMap<String, String>();
            Set<String> replacedKeys = new HashSet<String>();
            for (String outputQuery : mappings.keySet()) {
                if (outputQuery.contains(searchKey)) {
                    replacements.put(amendQuery(outputQuery, oldID, decomposition.getID()),
                            mappings.get(outputQuery));
                    replacedKeys.add(outputQuery);
                }
            }
            for (String key : replacedKeys) mappings.remove(key);
            mappings.putAll(replacements);

            if (task.isMultiInstance()) {
                replaceDecompositionIdInMIJoinerQuery(task, oldID, decomposition.getID());
            }
        }
    }


    /**
     * Removes all task output mappings that reference the specified decomposition, prior
     * to the decomposition being removed from the specification
     * @param decomposition the decomposition that is about to be removed
     */
    private void removeOutputReferencesToDecomposition(YDecomposition decomposition) {
        String searchKey = '{' + xpathDelimit(decomposition.getID());
        for (YTask task : getTasks(decomposition)) {
            Map<String, String> mappings = task.getDataMappingsForTaskCompletion();
            Set<String> replacedKeys = new HashSet<String>();
            for (String outputQuery : mappings.keySet()) {
                if (outputQuery.contains(searchKey)) {
                    replacedKeys.add(outputQuery);
                }
            }
            for (String key : replacedKeys) mappings.remove(key);
        }
    }


    /**
     * Gets all of the tasks in the current specification that decompose to a
     * specified decomposition
     * @param decomposition the decomposition to get the tasks for
     * @return the Set of YTasks that decompose to the decomposition
     */
    private Set<YTask> getTasks(YDecomposition decomposition) {
        Set<YTask> tasks = new HashSet<YTask>();
        for (YNet net : getNets()) {
            for (YTask task : net.getNetTasks()) {
                if (task.getDecompositionPrototype() == decomposition) {
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }


    /**
     * Gets the set of nets in this specification
     * @return the Set of YNets
     */
    private Set<YNet> getNets() {
        Set<YNet> nets = new HashSet<YNet>();
        for (YDecomposition decomposition : _specification.getDecompositions()) {
            if (decomposition instanceof YNet) {
                nets.add((YNet) decomposition);
            }
        }
        return nets;
    }


    /**
     * Gets the set of start mapping keys (decomposition variable names) where the key's
     * value (input query) contains a reference to the named net-level variable
     * @param task the task to search the mappings of
     * @param netVarName the net variable name to search for
     * @return the referencing set of decomposition variable names
     */
    private Set<String> getStartMappingKeysForNetVar(YTask task, String netVarName) {
        String searchKey = '/' + netVarName + '/';
        Set<String> referencingKeys = new HashSet<String>();
        for (String paramName : task.getDataMappingsForTaskStarting().keySet()) {
            String inputQuery = task.getDataMappingsForTaskStarting().get(paramName);
            if (inputQuery.contains(searchKey)) {
                referencingKeys.add(paramName);
            }
        }
        return referencingKeys;
    }


    /**
     * Gets the set of completion mapping keys (output queries) where the key's
     * value (net-level variable name) equals the named net-level variable, or the key
     * refers to the net-level variable in its outer element tag
     * @param task the task to search the mappings of
     * @param netVarName the net variable name to search for
     * @return the referencing set of output queries
     */
    private Set<String> getCompletionMappingKeysForNetVar(YTask task, String netVarName) {
        String tagKey = '<' + netVarName + '>';
        Set<String> referencingKeys = new HashSet<String>();
        for (String outputQuery : task.getDataMappingsForTaskCompletion().keySet()) {
            String varName = task.getDataMappingsForTaskCompletion().get(outputQuery);
            if (netVarName.equals(varName) || outputQuery.startsWith(tagKey)) {
                referencingKeys.add(outputQuery);
            }
        }
        return referencingKeys;
    }


    /**
     * Gets the set of completion mapping keys (output queries) where the key
     * contains a reference to the named task-level variable
     * @param task the task to search the mappings of
     * @param taskVarName the task variable name to search for
     * @return the referencing set of output queries
     */
    private Set<String> getCompletionMappingKeysForTaskVar(YTask task, String taskVarName) {
        String searchKey = xpathDelimit(task.getDecompositionPrototype().getID()) +
                taskVarName + '/';
        Set<String> referencingKeys = new HashSet<String>();
        for (String outputQuery : task.getDataMappingsForTaskCompletion().keySet()) {
            if (outputQuery.contains(searchKey)) {
                referencingKeys.add(outputQuery);
            }
        }
        return referencingKeys;
    }


    /**
     * Removes a set of mappings
     * @param mappings the mappings to remove from
     * @param keys the keys of the entries to remove
     */
    private void removeTaskMappings(Map<String, String> mappings, Set<String> keys) {
        for (String key : keys) mappings.remove(key);
    }


    /**
     * Replaces a predicate from a flow with a default 'false()' if the flow has a
     * predicate that contains a  reference to the named net-level variable
     * @param flows the set of flows to check
     * @param variableName the name of the net-level variable to check for
     */
    private void removeReferencingFlowPredicates(Set<YFlow> flows, String variableName) {
        String searchKey = xpathDelimit(variableName);
        for (YFlow flow : flows) {
             String predicate = flow.getXpathPredicate();
             if (predicate != null && predicate.contains(searchKey)) {
                 flow.setXpathPredicate("false()");
             }
        }
    }


    /**
     * Updates a predicate from a flow with a default with a renamed net-level variable
     * if the flow has a predicate that contains a reference to the variable's old name
     * @param flows the set of flows to check
     * @param oldName the current name of the net-level variable to check for
     * @param newName the new name of the net-level variable to check for
     */
    private void amendReferencingFlowPredicates(Set<YFlow> flows, String oldName,
                                                String newName) {
        String searchKey = xpathDelimit(oldName);
        String replaceKey = xpathDelimit(newName);
        for (YFlow flow : flows) {
            String predicate = flow.getXpathPredicate();
            if (predicate != null && predicate.contains(searchKey)) {
                flow.setXpathPredicate(amendQuery(predicate, searchKey, replaceKey));
            }
        }
    }


    /**
     * Replaces the old name with the new name, in an XPath expression
     * @param query the query to update
     * @param oldName the name to replace
     * @param newName the new name
     * @return the updated query
     */
    private String amendQuery(String query, String oldName, String newName) {
        return query.replaceFirst(xpathDelimit(oldName), xpathDelimit(newName));
    }


    /**
     * Delimits a String with / for searching or replacing within an XPath expression
     * @param label the String to delimit
     * @return the delimited String
     */
    private String xpathDelimit(String label) { return '/' + label + '/'; }


    /**
     * Renames a variable
     * @param variable the variable to rename
     * @param newName the name to assign to the variable
     */
    private void renameVariable(YVariable variable, String newName) {
        variable.setDataTypeAndName(variable.getDataTypeName(), newName,
                variable.getDataTypeNameSpace());
    }


    /**
     * Replaces the outer-tag name of an XML element string with a new name
     * @param element the element to update
     * @param newName the new name for the element's outermost tags
     * @return the updated XML element string
     */
    private String renameOuterTags(String element, String newName) {
        return StringUtil.wrap(StringUtil.unwrap(element), newName);
    }


    /**
     * Replaces the tag name of an XML element string with a new name, within a query
     * @param query the query to update
     * @param oldName the old name for the element's tags
     * @param newName the new name for the element's tags
     * @return the updated XML element string
     */
    private String replaceTags(String query, String oldName, String newName) {
        return query.replaceFirst('<' + oldName + '>', '<' + newName + '>')
                .replaceFirst("</" + oldName + '>', "</" + newName + '>');
    }


    /**
     * Renames an input parameter
     * @param decomposition the net or task decomposition containing the variable
     * @param oldName the name to replace
     * @param newName the new name
     * @return true if the rename was successful, false if no input parameter of that
     * name was found
     */
    private boolean renameInputParameter(YDecomposition decomposition, String oldName,
                                      String newName) {
        YParameter inputParam = decomposition.getInputParameters().get(oldName);
        if (inputParam != null) {
            decomposition.removeInputParameter(inputParam);
            renameVariable(inputParam, newName);
            decomposition.addInputParameter(inputParam);
        }
        return inputParam != null;
    }


    /**
     * Renames an output parameter
     * @param decomposition the net or task decomposition containing the variable
     * @param oldName the name to replace
     * @param newName the new name
     * @return true if the rename was successful, false if no output parameter of that
     * name was found
     */
    private boolean renameOutputParameter(YDecomposition decomposition, String oldName,
                                      String newName) {
        YParameter outputParam = decomposition.getOutputParameters().get(oldName);
        if (outputParam != null) {
            decomposition.removeOutputParameter(outputParam);
            renameVariable(outputParam, newName);
            decomposition.addOutputParameter(outputParam);
        }
        return outputParam != null;
    }


    /**
     * Updates the decomposition id in an MI join query
     * @param task the MI task
     * @param oldID the old decomposition id
     * @param newID the new decomposition id
     */
    private void replaceDecompositionIdInMIJoinerQuery(YTask task, String oldID,
                                                          String newID) {
        if (! task.isMultiInstance()) return;

        YMultiInstanceAttributes attributes = task.getMultiInstanceAttributes();
        String query = attributes.getMIJoiningQuery();
        if (query != null) {
            query = query.replaceFirst(' ' + xpathDelimit(oldID),
                    ' ' + xpathDelimit(newID));
            attributes.setUniqueOutputMIJoiningQuery(query);
        }
    }


    /**
     * Updates the decomposition id in an MI splitter query
     * @param task the MI task
     * @param oldID the old decomposition id
     * @param newID the new decomposition id
     */
    private void replaceTaskVariableInMISplitterQuery(YTask task, String oldID,
                                                      String newID) {
        if (! task.isMultiInstance()) return;

        YMultiInstanceAttributes attributes = task.getMultiInstanceAttributes();
        if (oldID.equals(attributes.getMIFormalInputParam())) {
            String query = attributes.getMISplittingQuery();
            if (query != null) {
                attributes.setUniqueInputMISplittingQuery(
                        replaceTags(query, oldID, newID));
            }
        }
    }



    /**
     * Updates a net variable name in the MI queries of a task
     * @param task the MI task
     * @param oldName the old decomposition id
     * @param newName the new decomposition id
     */
    private void replaceNetVariableInMIQueries(YTask task, String oldName,
                                                      String newName) {
        if (! task.isMultiInstance()) return;

        YMultiInstanceAttributes attributes = task.getMultiInstanceAttributes();
        if (oldName.equals(attributes.getMIFormalInputParam())) {
            String query = attributes.getMISplittingQuery();
            if (query != null) {
                query = query.replaceFirst(xpathDelimit(oldName), xpathDelimit(newName));
                attributes.setUniqueInputMISplittingQuery(query);
            }

            query = attributes.getMIJoiningQuery();
            if (query != null) {
                attributes.setUniqueOutputMIJoiningQuery(
                        replaceTags(query, oldName, newName));
            }
        }
    }



    /**
     * Changes the scope of a local variable to some other scope. Note: An output only
     * variable requires a 'shadow' local variable to be retained also.
     * @pre decomposition is a net decomposition
     * @param decomposition the containing net
     * @param variableName the name of the variable to change
     * @param newType the new scope to apply to the variable
     * @return true if successful, false if the local variable doesn't exist in the net
     */
    private boolean changeLocalScope(YDecomposition decomposition, String variableName,
                                    int newType) {
        YNet net = (YNet) decomposition;
        YVariable localVar = net.getLocalOrInputVariable(variableName);
        if (localVar != null) {
            if (newType == INPUT || newType == INPUT_OUTPUT) {
                YParameter parameter = newParameter(net, localVar, INPUT);
                decomposition.addInputParameter(parameter);
                net.removeLocalVariable(variableName);
            }
            if (newType == OUTPUT || newType == INPUT_OUTPUT) {
                YParameter parameter = newParameter(net, localVar, OUTPUT);
                decomposition.addOutputParameter(parameter);
            }
        }
        return localVar != null;
    }


    /**
     * Changes the scope of a input variable to some other scope. Note: An output only
     * variable requires a 'shadow' local variable to be retained also.
     * @pre if 'newType' is LOCAL, the decomposition is a net decomposition
     * @param decomposition the containing net or task decomposition
     * @param variableName the name of the variable to change
     * @param newType the new scope to apply to the variable
     * @return true if successful, false if the variable doesn't exist in the decomposition
     */
    private boolean changeInputScope(YDecomposition decomposition, String variableName,
                                    int newType) {
        YParameter inputParam = decomposition.getInputParameters().get(variableName);
        if (inputParam != null) {
            if (newType == LOCAL || ((decomposition instanceof YNet) && newType == OUTPUT)) {
                YVariable localVar = newLocalVariable((YNet) decomposition, inputParam);
                ((YNet) decomposition).setLocalVariable(localVar);
            }
            else if (newType == INPUT_OUTPUT || newType == OUTPUT) {
                YParameter outputParam = newParameter(decomposition, inputParam, OUTPUT);
                decomposition.addOutputParameter(outputParam);
            }
            if (newType != INPUT_OUTPUT) {
                decomposition.removeInputParameter(variableName);
            }
        }
        return inputParam != null;
    }


    /**
     * Changes the scope of a output variable to some other scope. Note: An output only
     * variable removal requires its 'shadow' local variable to be removed also.
     * @pre if 'newType' is LOCAL, the decomposition is a net decomposition
     * @param decomposition the containing net or task decomposition
     * @param variableName the name of the variable to change
     * @param newType the new scope to apply to the variable
     * @return true if successful, false if the variable doesn't exist in the decomposition
     */
    private boolean changeOutputScope(YDecomposition decomposition, String variableName,
                                    int newType) {
        YParameter outputParam = decomposition.getOutputParameters().get(variableName);
        if (outputParam != null) {
            if (newType != LOCAL && (decomposition instanceof YNet)) {
                ((YNet) decomposition).removeLocalVariable(variableName);
            }
            if (newType == INPUT_OUTPUT || newType == INPUT) {
                YParameter inputParam = newParameter(decomposition, outputParam, INPUT);
                decomposition.addInputParameter(inputParam);
            }
            if (newType != INPUT_OUTPUT) {
                decomposition.removeOutputParameter(variableName);
            }
        }
        return outputParam != null;
    }


    /**
     * Changes the scope of a input/output variable to some other scope. In actuality,
     * since an input/output variable is represented by two variables, this method
     * will remove one and amend the other, as required. Note: An output only
     * variable requires a 'shadow' local variable to be retained also.
     * @pre if 'newType' is LOCAL, the decomposition is a net decomposition
     * @param decomposition the containing net or task decomposition
     * @param variableName the name of the variable to change
     * @param newType the new scope to apply to the variable
     * @return true if successful, false if the variable doesn't exist in the decomposition
     */
    private boolean changeInputOutputScope(YDecomposition decomposition,
                                           String variableName, int newType) {
        if (newType == OUTPUT) {
            if (decomposition instanceof YNet) {
                YParameter inputParam = decomposition.removeInputParameter(variableName);
                if (inputParam != null) {
                    YVariable local = newLocalVariable((YNet) decomposition, inputParam);
                    ((YNet) decomposition).setLocalVariable(local);
                }
                return inputParam != null;
            }
            else {
                removeTaskDecompositionVariable(decomposition, variableName, INPUT);
            }
        }
        else if (newType == INPUT) {
            if (decomposition instanceof YNet) {
                return decomposition.removeOutputParameter(variableName) != null;
            }
            else {
                removeTaskDecompositionVariable(decomposition, variableName, OUTPUT);
            }
        }
        else if (newType == LOCAL) {
            decomposition.removeInputParameter(variableName);
            return changeOutputScope(decomposition, variableName, newType);
        }
        return true;
    }


    /**
     * Creates a new YParameter, based on the contents of the YVariable passed
     * @param decomposition the containing net or task decomposition
     * @param variable the variable to change
     * @param scope the scope of the parameter, one of INPUT or OUTPUT
     * @return the populated YParameter
     */
    private YParameter newParameter(YDecomposition decomposition, YVariable variable,
                                     int scope) {
        YParameter parameter = new YParameter(decomposition, scope);
        swapVariableContents(variable, parameter);
        return parameter;
    }


    /**
     * Creates a new local variable, i.e. a YVariable with LOCAL scope, based on the
     * contents of the YVariable passed
     * @param net the containing net
     * @param variable the name of the variable to change
     * @return the populated YVariable
     */
    private YVariable newLocalVariable(YNet net, YVariable variable) {
        YVariable localVar = new YVariable(net);
        swapVariableContents(variable, localVar);
        return localVar;
    }


    /**
     * Populates the primary fields of one variable with the matching ones of another
     * @param oldVar the source variable
     * @param newVar the target variable
     */
    private void swapVariableContents(YVariable oldVar, YVariable newVar) {
        newVar.setDataTypeAndName(oldVar.getDataTypeName(), oldVar.getPreferredName(),
                oldVar.getDataTypeNameSpace());
        newVar.setOrdering(oldVar.getOrdering());
    }


    /**
     * Modifies a variable's data type
     * @param variable the variable to modify
     * @param dataType the data type to assign
     * @return true if successful, false if the variable is null
     */
    private boolean setVariableDataType(YVariable variable, String dataType, String value)
            throws YDataHandlerException {
        if (variable == null) {
            throw new YDataHandlerException("No matching variable found");
        }
        if (! (StringUtil.isNullOrEmpty(value) || validate(dataType, value).isEmpty())) {
            throw new YDataHandlerException("Invalid data type for variable value");
        }
        variable.setDataTypeAndName(dataType, variable.getPreferredName(),
                    variable.getDataTypeNameSpace());
        return true;
    }


    /**
     * Modifies a variable's extended attribute map
     * @param variable the variable to modify
     * @param attributes the attributes to assign
     * @return true if successful, false if the variable is null
     */
    private boolean setVariableAttributes(YVariable variable, YAttributeMap attributes) {
         if (variable != null) {
             variable.setAttributes(attributes);
         }
         return variable != null;
     }


    private boolean setVariableLogPredicate(YVariable variable, YLogPredicate predicate) {
         if (variable != null) {
             variable.setLogPredicate(predicate);
         }
         return variable != null;
     }


    /**
     * Checks that a specification is associated with this object
     * @throws YDataHandlerException if there's no specification
     */
    private void checkSpecificationExists() throws YDataHandlerException {
        if (getSpecification() == null) {
            raise("No specification associated with this handler");
        }
    }


    /**
     * Checks that a scope value is a LOCAL, INPUT, OUTPUT or INPUT_OUTPUT scope
     * @throws YDataHandlerException if the scope value is invalid
     */
    private void checkParameterType(int scope) throws YDataHandlerException {
        switch (scope) {
            case LOCAL:
            case INPUT:
            case OUTPUT:
            case INPUT_OUTPUT: break;
            default: raise("Invalid scope parameter: " + scope);
        }
    }


    /**
     * Checks that a variable with a given name and scope does not already exist within
     * a decomposition
     * @param decomposition the containing net or task decomposition
     * @param name the name to check
     * @param scope the scope to check
     * @throws YDataHandlerException if a variable with a given name and scope
     * currently exists within the decomposition
     */
    private void checkVariableNameUnique(YDecomposition decomposition, String name,
                                         int scope) throws YDataHandlerException {
        if (scope == LOCAL) {
            if (decomposition instanceof YNet) {
                if (((YNet) decomposition).getLocalVariables().get(name) != null) {
                    raise("Net '" + decomposition.getID() +
                            "' already has a local variable called: " + name);
                }
            }
            else raise("Task decompositions cannot contain local variables");
        }
        if ((scope == INPUT || scope == INPUT_OUTPUT) &&
                decomposition.getInputParameters().get(name) != null) {
            raise("Decomposition '" + decomposition.getID() +
                    "' already has an input parameter called: " + name);
        }
        if ((scope == OUTPUT || scope == INPUT_OUTPUT) &&
                decomposition.getOutputParameters().get(name) != null) {
            raise("Decomposition '" + decomposition.getID() +
                    "' already has an output parameter called: " + name);
        }
    }


    /**
     * Checks whether a variable with a given name and data type exists as an
     * input/output variable within a net or task decomposition
     * @param decomposition the containing net or task decomposition
     * @param name the name to check
     * @param dataType the data type to check
     * @return true if the decomposition contains an input variable and and output
     * variable with the given name and type
     */
    private boolean isInputOutput(YDecomposition decomposition, String name,
                                  String dataType) {
        YParameter inputParam = decomposition.getInputParameters().get(name);
        YParameter outputParam = decomposition.getOutputParameters().get(name);
        return inputParam != null && outputParam != null &&
                inputParam.getDataTypeName().equals(dataType) &&
                outputParam.getDataTypeName().equals(dataType);
    }


    /**
     * Gets the net within the current specification with the specified id
     * @param netID the id of the net to select
     * @return the net matching the id
     * @throws YDataHandlerException if the current specification doesn't contain a
     * net with the id specified
     */
    private YNet getNet(String netID) throws YDataHandlerException {
        checkSpecificationExists();
        YDecomposition net = getSpecification().getDecomposition(netID);
        if (net instanceof YNet) {
            return (YNet) net;
        }
        raise("No net found with id: " + netID);   // throws exception
        return null;                               // keep compiler happy
    }


    /**
     * Gets the net or task decomposition within the current specification with the
     * specified id
     * @param decompositionID the id of the decomposition to select
     * @return the decomposition matching the id
     * @throws YDataHandlerException if the current specification doesn't contain a
     * net or task decomposition with the id specified
     */
    private YDecomposition getDecomposition(String decompositionID)
            throws YDataHandlerException {
        checkSpecificationExists();
        YDecomposition decomposition = getSpecification().getDecomposition(decompositionID);
        if (decomposition == null) {
            raise("No decomposition found with id: " + decompositionID);
        }
        return decomposition;
    }


    /**
     * Gets the task decomposition within the current specification with the
     * specified id
     * @param decompositionID the id of the decomposition to select
     * @return the decomposition matching the id
     * @throws YDataHandlerException if the current specification doesn't contain a
     * task decomposition with the id specified
     */
    private YDecomposition getTaskDecomposition(String decompositionID)
                throws YDataHandlerException {
        YDecomposition decomposition = getDecomposition(decompositionID);
        if (! (decomposition instanceof YAWLServiceGateway)) {
            raise(decompositionID + " refers to a YNet, not a YDecomposition");
        }
        return decomposition;
    }


    private void validateValue(YVariable variable, String value)
            throws YDataHandlerException{
        String dataType = variable.getDataTypeName();
        if (! (StringUtil.isNullOrEmpty(value) || validate(dataType, value).isEmpty())) {
            throw new YDataHandlerException("Invalid value for variable data type");
        }
    }


    /**
     * Raises a YDataHandlerException with the specified message
     * @param msg the exception message
     * @throws YDataHandlerException
     */
    private void raise(String msg) throws YDataHandlerException {
        throw new YDataHandlerException(msg);
    }


    /*****************************************************************************/

    // DataUtil passthroughs

    /**
     * Validates a value against type
     * @param dataType the type to validate against
     * @param value the value to validate
     * @return true id value is valid for the type passed
     * @throws YDataHandlerException if no specification is currently loaded
     */
    public List<String> validate(String dataType, String value) throws YDataHandlerException {
        return getUtils().getInstanceValidator().validate(dataType, value);
    }


    /**
     * Sets the data schema for subsequent validation activities
     * @param schema the specification's data schema
     * @throws YDataHandlerException if no specification is currently loaded
     */
    public void setSchema(String schema) throws YDataHandlerException {
        getUtils().setSpecificationSchema(schema);
    }


    /**
     * @return a list of all the data type names in the current schema, plus name for
     * internal types and all XSD types
     * @throws YDataHandlerException if no specification is currently loaded
     */
    public List<String> getDataTypeNames() throws YDataHandlerException {
        return getUtils().getDataTypeNames();
    }


    /**
     * @return a list of all built-in XSD data type names
     * @throws YDataHandlerException if no specification is currently loaded
     */
    public List<String> getBuiltInTypeNames() throws YDataHandlerException {
        return getUtils().getBuiltInTypeNames();
    }


    /**
     * @return a list of all YAWL internal data type names
     * @throws YDataHandlerException if no specification is currently loaded
     */
    public List<String> getInternalTypeNames() throws YDataHandlerException {
        return getUtils().getInternalTypeNames();
    }


    /**
     * @return a list of all user-defined data type names in the current schema
     * @throws YDataHandlerException if no specification is currently loaded
     */
    public List<String> getUserDefinedTypeNames() throws YDataHandlerException {
        return getUtils().getUserDefinedTypeNames();
    }


    /**
     * @return the namespace of the current data schema
     * @throws YDataHandlerException if no specification is currently loaded
     */
    public Namespace getDataSchemaNamespace() throws YDataHandlerException {
        return getUtils().getDataSchemaNamespace();
    }


    /**
     * Gets the data type and name of the inner element of a list type used as the
     * formal parameter of an MI task
     * @param dataType the MI (list) data type
     * @return the inner element name and data type
     * @throws YDataHandlerException if no specification is currently loaded
     */
    public String[] getMultiInstanceItemNameAndType(String dataType)
            throws YDataHandlerException {
        return getUtils().getMultiInstanceItemNameAndType(dataType);
    }


    /**
     * Gets the appropriate-for-type suffix for an XQuery
     * @param dataType the type to get the suffix for
     * @return the appropriate suffix
     * @throws YDataHandlerException if no specification is currently loaded
     */
    public String getXQuerySuffix(String dataType) throws YDataHandlerException {
        return getUtils().getXQuerySuffix(dataType);
    }



    private DataUtil getUtils() throws YDataHandlerException {
        checkSpecificationExists();
        return _utils;
    }

}
