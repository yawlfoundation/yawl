/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.awt.Point;
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
import au.edu.qut.yawl.persistence.managed.DataProxyStateChangeListener;

/**
 * The CopyTaskCommand copies a task to a net. All existing YFlows are 
 * disconnected from the task. Also the mapping names must change to the
 * new parent net's name. Variables must be added to the net, and a gateway
 * may be added to the specification if it does not already exist in the 
 * necessary form.
 * 
 * @author Nathan Rose
 */
public class CopyTaskCommand extends AbstractCommand {
    private DataProxy<YAtomicTask> originalTaskProxy;
    private DataProxy<YNet> netProxy;
    private Point location;
    private DataProxy<YSpecification> specProxy;
    private DataProxyStateChangeListener listener;
    
    private DataContext context;
    
    private YAtomicTask task;
    private YAWLServiceGateway gateway;
    private List<YVariable> vars;
    
    private DataProxy<YAtomicTask> taskProxy;
    private DataProxy<YAWLServiceGateway> gatewayProxy;
	
	public CopyTaskCommand( DataProxy taskProxy, DataProxy netProxy, Point location,
            DataProxyStateChangeListener listener ) {
        this.originalTaskProxy = taskProxy;
        this.netProxy = netProxy;
        this.location = location;
        this.context = netProxy.getContext();
        this.listener = listener;
	}
    
    public void attach() {
        WorkflowOperation.attachDecompositionToSpec( gateway, netProxy.getData().getParent() );
        WorkflowOperation.attachNetElementToNet( task, netProxy.getData() );
        WorkflowOperation.attachVariablesToNet( vars, netProxy.getData() );
        context.attachProxy( gatewayProxy, gateway, specProxy );
        context.attachProxy( taskProxy, task, netProxy );
    }
    
    public void detach() {
        WorkflowOperation.detachDecompositionFromSpec( gateway );
        WorkflowOperation.detachNetElementFromNet( task );
        WorkflowOperation.detachVariablesFromNet( vars );
        context.detachProxy( gatewayProxy, gateway, specProxy );
        context.detachProxy( taskProxy, task, netProxy );
    }
    
	public void perform() {
        specProxy = context.getDataProxy( netProxy.getData().getParent() );
        vars = new ArrayList<YVariable>();
        
        task = WorkflowOperation.copyTask(
                originalTaskProxy.getData(),
                (YNet) netProxy.getData(),
                location,
                vars );
        gateway = (YAWLServiceGateway) task.getDecompositionPrototype();
        
        gatewayProxy = context.createProxy( gateway, listener );
        taskProxy = context.createProxy( task, listener );
	}
}
