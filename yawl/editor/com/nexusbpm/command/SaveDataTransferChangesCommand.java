/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import au.edu.qut.yawl.elements.KeyValue;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.data.YVariable;
import au.edu.qut.yawl.persistence.managed.DataProxy;
import au.edu.qut.yawl.persistence.managed.DataProxyStateChangeListener;

import com.nexusbpm.operation.WorkflowOperation;
import com.nexusbpm.services.NexusServiceConstants;
import com.nexusbpm.services.data.NexusServiceData;

/**
 * The SaveDataTransferChangesCommand saves changes made in the data
 * transfer editor.
 * 
 * @author Nathan Rose
 */
public class SaveDataTransferChangesCommand extends AbstractCommand {
	private DataProxy<YTask> taskProxy;
    
    private YTask task;
    private YNet net;
    
    private NexusServiceData oldData;
    private NexusServiceData newData;
    
    private Set<VariableMapping> mappings;
    
    private Map<String, KeyValue> oldInputMappings;
    private Set<KeyValue> oldOutputMappings;
    
    private Map<String, String> newInputMappings;
    private Map<String, String> newOutputMappings;
    
    /** The names of the variables added for the task (added on the net). */
    private Set<String> addSet;
    /** The actual variables added for the task (added on the net). */
    private Set<YVariable> addSetVariables;
    private Set<YParameter> addSetParams;
    
    /** The names of the variables for the task that were removed (from the net). */
    private Set<String> deleteSet;
    /** The actual variables for the task that were removed (from the net). */
    private Set<YVariable> deleteSetVariables;
    private Set<YParameter> deleteSetParams;
    
    /**
     * <strong>note:</strong> there should be a mapping included for every input/output parameter!
     */
	public SaveDataTransferChangesCommand(
            DataProxy<YTask> taskProxy, // the task to edit
            NexusServiceData data, // the initial values to set
            Set<VariableMapping> mappings // the data mappings for the variables
            ) {
        this.taskProxy = taskProxy;
        this.newData = data;
        this.mappings = mappings;
        this.task = taskProxy.getData();
        this.net = task.getParent();
	}
	
    /**
     * @see com.nexusbpm.command.AbstractCommand#attach()
     */
    @Override
    protected void attach() throws Exception {
        // remove the old variables from the net
        WorkflowOperation.detachVariablesFromNet( deleteSetVariables );
        
        // attach the new variables to the net
        WorkflowOperation.attachVariablesToNet( addSetVariables, net );
        
        // remove old input and output params from the gateway
        WorkflowOperation.detachParametersFromDecomposition( deleteSetParams );
        
        // add new input and output params to the gateway
        WorkflowOperation.attachParametersToDecomposition( addSetParams, task.getDecompositionPrototype() );
        
        // remove old input and output mappings from the task
        task.getDataMappingsForTaskStartingSet().clear();
        task.getDataMappingsForTaskCompletionSet().clear();
        
        // add new input and output mappings to the task
        for( String name : newInputMappings.keySet() ) {
            task.setDataBindingForInputParam( newInputMappings.get( name ), name );
        }
        for( String name : newOutputMappings.keySet() ) {
            task.setDataBindingForOutputExpression( newOutputMappings.get( name ), name );
        }
        
        // save the initial values into the net variables
        newData.marshal( net, task.getID() );
        
        taskProxy.fireUpdated(
                DataProxyStateChangeListener.PROPERTY_TASK_VARIABLES, oldData.clone(), newData.clone() );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#detach()
     */
    @Override
    protected void detach() throws Exception {
        // remove the new variables from the net
        WorkflowOperation.detachVariablesFromNet( addSetVariables );
        
        // re-attach the old variables to the net
        WorkflowOperation.attachVariablesToNet( deleteSetVariables, net );
        
        // remove new input and output params from the gateway
        WorkflowOperation.detachParametersFromDecomposition( addSetParams );
        
        // re-add oldinput and output params to the gateway
        WorkflowOperation.attachParametersToDecomposition( deleteSetParams, task.getDecompositionPrototype() );
        
        // remove new input and output mappings from the task
        task.getDataMappingsForTaskStartingSet().clear();
        task.getDataMappingsForTaskCompletionSet().clear();
        
        // re-add old input and output mappings to the task
        for( String name : oldInputMappings.keySet() ) {
            task.setDataBindingForInputParam( oldInputMappings.get( name ).getValue(), name );
        }
        for( KeyValue k : oldOutputMappings ) {
            task.setDataBindingForOutputExpression( k.getValue(), k.getKey() );
        }
        
        // save the initial values into the net variables
        oldData.marshal( net, task.getID() );
        
        taskProxy.fireUpdated(
                DataProxyStateChangeListener.PROPERTY_TASK_VARIABLES, newData.clone(), oldData.clone() );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#perform()
     */
    @Override
    protected void perform() throws Exception {
        oldData = NexusServiceData.unmarshal( task, false );
        
        deleteSet = new HashSet<String>();
        deleteSetVariables = new HashSet<YVariable>();
        deleteSetParams = new HashSet<YParameter>();
        
        addSet = new HashSet<String>();
        addSetVariables  = new HashSet<YVariable>();
        addSetParams = new HashSet<YParameter>();
        
        newInputMappings = new HashMap<String, String>();
        newOutputMappings = new HashMap<String, String>();
        
        // set up the delete set
        for( String name : oldData.getVariableNames() ) {
            if( newData.getType( name ) == null ) {
                deleteSet.add( name );
                YVariable var = net.getLocalVariable( task.getID() + NexusServiceConstants.NAME_SEPARATOR + name );
                assert var != null : "trying to remove a null variable";
                deleteSetVariables.add( var );
                for( YParameter param : task.getDecompositionPrototype().getInputParameters() ) {
                    if( param.getName().equals( name ) ) {
                        deleteSetParams.add( param );
                    }
                }
                for( YParameter param : task.getDecompositionPrototype().getOutputParameters() ) {
                    if( param.getName().equals( name ) ) {
                        deleteSetParams.add( param );
                    }
                }
            }
        }
        
        // set up the add set
        for( String name : newData.getVariableNames() ) {
            if( oldData.getType( name ) == null ) {
                addSet.add( name );
                YVariable var = WorkflowOperation.createStringVariable( task.getID(), name, "" );
                addSetVariables.add( var );
                YParameter iparam =
                    WorkflowOperation.createStringParameter( name, YParameter._INPUT_PARAM_TYPE );
                YParameter oparam =
                    WorkflowOperation.createStringParameter( name, YParameter._OUTPUT_PARAM_TYPE );
                
                addSetParams.add( iparam );
                addSetParams.add( oparam );
            }
        }
        
        // get old mappings
        oldInputMappings = new HashMap<String, KeyValue>( task.getDataMappingsForTaskStartingSet() );
        oldOutputMappings = new HashSet<KeyValue>( task.getDataMappingsForTaskCompletionSet() );
        
        // create new mappings
        for( VariableMapping mapping : mappings ) {
            newInputMappings.put(
                    mapping.targetVariable,
                    WorkflowOperation.createInputBindingString(
                            net.getId(),
                            mapping.targetVariable,
                            mapping.sourceElementID,
                            mapping.sourceVariableName ) );
            newOutputMappings.put(
                    task.getID() + NexusServiceConstants.NAME_SEPARATOR + mapping.targetVariable,
                    WorkflowOperation.createOutputBindingString(
                            net.getId(),
                            task.getID(),
                            task.getID() + NexusServiceConstants.NAME_SEPARATOR + mapping.targetVariable,
                            mapping.targetVariable ) );
        }
    }
    
    public static class VariableMapping {
        String targetVariable;
        String sourceElementID;
        String sourceVariableName;
        public VariableMapping( String targetVariable, String sourceElementID, String sourceVariableName ) {
            this.targetVariable = targetVariable;
            this.sourceElementID = sourceElementID;
            this.sourceVariableName = sourceVariableName;
        }
    }
}
