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

import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.persistence.managed.DataProxy;
import au.edu.qut.yawl.persistence.managed.DataProxyStateChangeListener;

import com.nexusbpm.operation.WorkflowOperation;
import com.nexusbpm.services.NexusServiceConstants;
import com.nexusbpm.services.data.NexusServiceData;

/**
 * The SaveTaskChangesCommand saves changes made to the initial values of the
 * variables for a particular task.
 * 
 * @author Nathan Rose
 */
public class SaveTaskChangesCommand extends AbstractCommand {
	private DataProxy<YTask> taskProxy;
    
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
        taskProxy.fireUpdated(
                DataProxyStateChangeListener.PROPERTY_TASK_VARIABLES, oldData.clone(), newData.clone() );
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
            addedVariables.add( taskProxy.getData().getID() + NexusServiceConstants.NAME_SEPARATOR + name );
        }
        WorkflowOperation.detachVariablesFromNet( taskProxy.getData().getParent(), addedVariables );
        oldData.marshal( taskProxy.getData().getParent(), taskProxy.getData().getID() );
        taskProxy.fireUpdated(
                DataProxyStateChangeListener.PROPERTY_TASK_VARIABLES, newData.clone(), oldData.clone() );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#perform()
     */
    @Override
    protected void perform() throws Exception {
        oldData = NexusServiceData.unmarshal( taskProxy.getData(), false );
    }
}
