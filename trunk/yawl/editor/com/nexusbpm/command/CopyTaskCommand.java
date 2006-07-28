/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import operation.WorkflowOperation;
import au.edu.qut.yawl.elements.YAWLServiceGateway;
import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.data.YVariable;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;

import com.nexusbpm.NexusWorkflow;
import com.nexusbpm.editor.persistence.YTaskEditorExtension;
import com.nexusbpm.editor.tree.SharedNode;
import com.nexusbpm.editor.tree.SharedNodeTreeModel;

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
    private SharedNode netNode;
    private DataProxy<YAtomicTask> originalTaskProxy;
    private DataProxy<YNet> netProxy;
    private Point location;
    
    private DataContext context;
    
    private YAtomicTask task;
    private YAWLServiceGateway gateway;
    private List<YVariable> vars;
    
    private DataProxy<YAtomicTask> taskProxy;
    private DataProxy<YAWLServiceGateway> gatewayProxy;
	
	public CopyTaskCommand( SharedNode taskNode, SharedNode netNode, Point location ) {
		this.netNode = netNode;
        this.originalTaskProxy = taskNode.getProxy();
        this.netProxy = netNode.getProxy();
        this.location = location;
        this.context = netProxy.getContext();
	}
    
    public void attach() {
        WorkflowOperation.attachDecompositionToSpec( gateway, netProxy.getData().getParent() );
        WorkflowOperation.attachNetElementToNet( task, netProxy.getData() );
        WorkflowOperation.attachVariablesToNet( vars, netProxy.getData() );
        DataProxy specProxy = context.getDataProxy( netProxy.getData().getParent(), null );
        context.attachProxy( gatewayProxy, gateway, specProxy );
        context.attachProxy( taskProxy, task, netProxy );
    }
    
    public void detach() {
        WorkflowOperation.detachDecompositionFromSpec( gateway );
        WorkflowOperation.detachNetElementFromNet( task );
        WorkflowOperation.detachVariablesFromNet( vars );
        context.detachProxy( gatewayProxy );
        context.detachProxy( taskProxy );
    }
    
	public void perform() {
		YAtomicTask sourceTask = originalTaskProxy.getData();
		YNet targetNet = (YNet) netNode.getProxy().getData();
        YSpecification targetSpec = (YSpecification) ((SharedNode)netNode.getParent()).getProxy().getData();
        
        String taskID = WorkflowOperation.getAvailableNetElementID( targetNet, sourceTask.getID() );
        
        task = WorkflowOperation.createTask( taskID, sourceTask.getName(), targetNet, gateway );
        gateway = WorkflowOperation.createGateway( taskID, targetNet );
        
        Set<String> names = new HashSet<String>();
        
        for( String name : sourceTask.getDecompositionPrototype().getInputParameterNames() ) {
            names.add( name );
            YParameter iparam = new YParameter( gateway, YParameter._INPUT_PARAM_TYPE );
            iparam.setDataTypeAndName( NexusWorkflow.VARTYPE_STRING, name, NexusWorkflow.XML_SCHEMA_URL );
            gateway.setInputParam( iparam );
            task.setDataBindingForInputParam(
                    WorkflowOperation.createInputBindingString(
                            targetNet.getId(),
                            name,
                            taskID,
                            name ), name );
        }
        
        for( String name : sourceTask.getDecompositionPrototype().getOutputParamNames() ) {
            names.add( name );
            YParameter oparam = new YParameter( gateway, YParameter._OUTPUT_PARAM_TYPE );
            oparam.setDataTypeAndName( NexusWorkflow.VARTYPE_STRING, name, NexusWorkflow.XML_SCHEMA_URL );
            gateway.setOutputParameter( oparam );
            task.setDataBindingForOutputExpression(
                    WorkflowOperation.createOutputBindingString(
                            targetNet.getId(),
                            taskID,
                            taskID + NexusWorkflow.NAME_SEPARATOR + name,
                            name ), taskID + NexusWorkflow.NAME_SEPARATOR + name );
        }
        
        vars = new ArrayList<YVariable>( names.size() + 1 );
        for( String name : names ) {
            YVariable var = new YVariable( null );
            var.setDataTypeAndName(
                    NexusWorkflow.VARTYPE_STRING,
                    taskID + NexusWorkflow.NAME_SEPARATOR + name,
                    NexusWorkflow.XML_SCHEMA_URL );
            var.setInitialValue( sourceTask.getParent().getLocalVariable(
                    sourceTask.getID() + NexusWorkflow.NAME_SEPARATOR + name ).getInitialValue() );
            vars.add( var );
        }
        
        gatewayProxy = context.createProxy( gateway, (SharedNodeTreeModel) netNode.getTreeModel() );
        taskProxy = context.createProxy( task, (SharedNodeTreeModel) netNode.getTreeModel() );
        
        YTaskEditorExtension editor = new YTaskEditorExtension( task );
        editor.setBounds( new Rectangle2D.Double(
                location.getX() - 35, location.getY() - 35,
                location.getX() + 50, location.getY() + 50 ) );
	}
}
