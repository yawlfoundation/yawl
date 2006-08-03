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
import au.edu.qut.yawl.elements.YAWLServiceGateway;
import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.data.YVariable;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;

import com.nexusbpm.NexusWorkflow;

/**
 * The RemoveNexusTaskCommand removes a task from a network. 
 * 
 * @author Matthew Sandoz
 * @author Nathan Rose
 */
public class RemoveNexusTaskCommand extends AbstractCommand {
    private DataContext context;
	private DataProxy<YAtomicTask> taskProxy;
    private YAtomicTask task;
    
    private DataProxy<YNet> netProxy;
    private YNet net;
    
    private YAWLServiceGateway gateway;
    private DataProxy<YAWLServiceGateway> gatewayProxy;
    
    private List<YVariable> netVariables;
    
    private DataProxy<YSpecification> specProxy;
    private YSpecification spec;
    
	public RemoveNexusTaskCommand( DataProxy taskProxy ) {
        this.context = taskProxy.getContext();
		this.taskProxy = taskProxy;
        this.task = this.taskProxy.getData();
	}
    
    /**
     * Removes the task from its net
     * (Attach and detach are reversed for remove commands).
     */
    public void attach() {
        WorkflowOperation.detachNetElementFromNet( task );
        context.detachProxy( taskProxy, task, netProxy );
        WorkflowOperation.detachDecompositionFromSpec( gateway );
        context.detachProxy( gatewayProxy, gateway, specProxy );
        WorkflowOperation.detachVariablesFromNet( netVariables );
    }
    
    /**
     * Re-attaches the task to its net
     * (Attach and detach are reversed for remove commands).
     */
    public void detach() {
        WorkflowOperation.attachDecompositionToSpec( gateway, spec );
        context.attachProxy( gatewayProxy, gateway, specProxy );
        WorkflowOperation.attachNetElementToNet( task, net );
        context.attachProxy( taskProxy, task, netProxy );
        WorkflowOperation.attachVariablesToNet( netVariables, net );
    }
    
    public void perform() {
        net = task.getParent();
        netProxy = context.getDataProxy( net, null );
        
        spec = net.getParent();
        specProxy = context.getDataProxy( spec, null );
        
        gateway = (YAWLServiceGateway) task.getDecompositionPrototype();
        gatewayProxy = context.getDataProxy( gateway, null );
        
        String namePrefix = task.getID() + NexusWorkflow.NAME_SEPARATOR;
        netVariables = new ArrayList<YVariable>();
        
        for( YVariable var : net.getLocalVariables() ) {
            if( var.getName().startsWith( namePrefix ) ) {
                netVariables.add( var );
            }
        }
    }
}
