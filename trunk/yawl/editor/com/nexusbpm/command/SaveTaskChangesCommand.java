/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.util.ArrayList;
import java.util.List;

import operation.WorkflowOperation;
import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.persistence.managed.DataProxy;

import com.nexusbpm.NexusWorkflow;
import com.nexusbpm.services.data.NexusServiceData;

/**
 * The SaveTaskChangesCommand saves changes made to the initial values of the
 * variables for a particular task.
 * 
 * @author Nathan Rose
 */
public class SaveTaskChangesCommand extends AbstractCommand {
	private DataProxy<YAtomicTask> taskProxy;
    
    private NexusServiceData oldData;
    private NexusServiceData newData;
    
    /**
     * @param parent
     * @param netName
     */
	public SaveTaskChangesCommand( DataProxy taskProxy, NexusServiceData data ) {
        this.taskProxy = taskProxy;
        this.newData = data;
	}
	
    /**
     * @see com.nexusbpm.command.AbstractCommand#attach()
     */
    @Override
    protected void attach() throws Exception {
        newData.marshal( taskProxy.getData().getParent(), taskProxy.getData().getID() );
        taskProxy.fireUpdated( "variables", oldData.clone(), newData.clone() );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#detach()
     */
    @Override
    protected void detach() throws Exception {
        List<String> addedVariables = newData.getVariableNames();
        for( String name : oldData.getVariableNames() ) {
            addedVariables.remove( name );
        }
        for( String name : new ArrayList<String>( addedVariables ) ) {
            addedVariables.remove( name );
            addedVariables.add( taskProxy.getData().getID() + NexusWorkflow.NAME_SEPARATOR + name );
        }
        WorkflowOperation.detachVariablesFromNet( taskProxy.getData().getParent(), addedVariables );
        oldData.marshal( taskProxy.getData().getParent(), taskProxy.getData().getID() );
        taskProxy.fireUpdated( "variables", newData.clone(), oldData.clone() );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#perform()
     */
    @Override
    protected void perform() throws Exception {
        oldData = NexusServiceData.unmarshal( taskProxy.getData(), false );
    }
}
