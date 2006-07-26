/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import operation.WorkflowOperation;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;

import com.nexusbpm.editor.persistence.EditorDataProxy;
import com.nexusbpm.editor.tree.SharedNode;
import com.nexusbpm.editor.tree.SharedNodeTreeModel;

/**
 * The CreateFlowCommand creates a flow between two external net elements.
 * The created flow is stored in the command for later undoing. The created
 * flow is minimally configured and must be customized afterwards.
 * 
 * @author Matthew Sandoz
 * @author Nathan Rose
 */
public class CreateFlowCommand extends AbstractCommand {
	private EditorDataProxy<YExternalNetElement> sourceProxy;
	private EditorDataProxy<YExternalNetElement> targetProxy;
    private SharedNode targetNode;
    private YFlow flow;
	private DataProxy<YFlow> flowProxy;
    private DataContext context;
	
	public CreateFlowCommand(SharedNode sourceNode, SharedNode targetNode) {
        this.sourceProxy = sourceNode.getProxy();
		this.targetNode = targetNode;
        this.targetProxy = targetNode.getProxy();
        this.context = targetProxy.getContext();
	}
    
	/**
     * @see com.nexusbpm.command.AbstractCommand#attach()
     */
    @Override
    protected void attach() throws Exception {
        WorkflowOperation.attachFlowToElements( flow, sourceProxy.getData(), targetProxy.getData() );
        context.attachProxy( flowProxy, flow, context.getDataProxy( flow.getParent(), null ) );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#detach()
     */
    @Override
    protected void detach() throws Exception {
        WorkflowOperation.detachFlowFromElements( flow );
        context.detachProxy( flowProxy );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#perform()
     */
    @Override
    protected void perform() throws Exception {
        flow = WorkflowOperation.createFlow();
        flowProxy = context.createProxy( flow, (SharedNodeTreeModel) targetNode.getTreeModel() );
    }
}
