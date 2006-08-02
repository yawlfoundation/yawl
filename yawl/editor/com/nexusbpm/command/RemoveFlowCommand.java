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

import com.nexusbpm.editor.persistence.EditorDataProxy;

/**
 * The RemoveFlowCommand removes a flow from its prior's postsets and 
 * next's presets.
 * 
 * @author Matthew Sandoz
 * @author Nathan Rose
 */
public class RemoveFlowCommand extends AbstractCommand {

	private EditorDataProxy<YFlow> flowProxy;
    private YFlow flow;
    private DataContext context;
    
    private YExternalNetElement source;
    private YExternalNetElement sink;
	
	public RemoveFlowCommand( EditorDataProxy proxy ) {
        context = proxy.getContext();
		flowProxy = proxy;
        flow = flowProxy.getData();
	}
    
    /**
     * Removes the flow from the net elements it connects
     * (Attach and detach are reversed for remove commands).
     */
    protected void attach() {
        WorkflowOperation.detachFlowFromElements( flow );
        context.detachProxy( flowProxy, flow, context.getDataProxy( flow.getParent(), null ) );
    }
    
    /**
     * Reattaches the flow to the net elements it connects
     * (Attach and detach are reversed for remove commands).
     */
    protected void detach() {
        WorkflowOperation.attachFlowToElements( flow, source, sink );
        context.attachProxy( flowProxy, flow, context.getDataProxy( flow.getParent(), null ) );
    }
    
    protected void perform() {
        source = flow.getPriorElement();
        sink = flow.getNextElement();
    }
}
