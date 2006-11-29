/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import com.nexusbpm.operation.WorkflowOperation;

import au.edu.qut.yawl.elements.YCondition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YInputCondition;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YOutputCondition;
import au.edu.qut.yawl.exceptions.YStateException;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;
import au.edu.qut.yawl.persistence.managed.DataProxyStateChangeListener;

/**
 * The CreateFlowCommand creates a flow between two external net elements.
 * The created flow is stored in the command for later undoing. The created
 * flow is minimally configured and must be customized afterwards.
 * 
 * @author Matthew Sandoz
 * @author Nathan Rose
 */
public class CreateFlowCommand extends AbstractCommand {
	private DataProxy<YExternalNetElement> sourceProxy;
	private DataProxy<YExternalNetElement> targetProxy;
    private DataProxy<YNet> netProxy;
    private YFlow flow;
	private DataProxy<YFlow> flowProxy;
    private DataContext context;
    private DataProxyStateChangeListener listener;
	
	public CreateFlowCommand( DataProxy sourceProxy, DataProxy targetProxy,
            DataProxyStateChangeListener listener ) {
        this.sourceProxy = sourceProxy;
        this.targetProxy = targetProxy;
        this.context = targetProxy.getContext();
        this.listener = listener;
	}
    
	/**
     * @see com.nexusbpm.command.AbstractCommand#attach()
     */
    @Override
    protected void attach() throws Exception {
        WorkflowOperation.attachFlowToElements( flow, sourceProxy.getData(), targetProxy.getData() );
        context.attachProxy( flowProxy, flow, netProxy );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#detach()
     */
    @Override
    protected void detach() throws Exception {
        WorkflowOperation.detachFlowFromElements( flow );
        context.detachProxy( flowProxy, flow, netProxy );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#perform()
     */
    @Override
    protected void perform() throws Exception {
        netProxy = context.getDataProxy( targetProxy.getData().getParent() );
        String error = validateFlow( sourceProxy.getData(), targetProxy.getData() );
        if( error != null ) {
            throw new YStateException( error );
        }
        flow = WorkflowOperation.createFlow();
        flowProxy = context.createProxy( flow, listener );
    }
    
    public static String validateFlow( Object source, Object target ) {
        if( source instanceof YCondition &&
                ((YCondition) source).getPostsetFlows().size() > 0 ) {
            return "Cannot have multiple outgoing edges from a condition!";
        }
        else if( target instanceof YCondition &&
                ((YCondition) target).getPresetFlows().size() > 0 ) {
            return "Cannot have multiple incoming edges to a condition!";
        }
        else if( source instanceof YOutputCondition ) {
            return "Output conditions cannot have outgoing edges!";
        }
        else if( target instanceof YInputCondition ) {
            return "Input conditions cannot have incoming edges!";
        }
        else if( ! ( source instanceof YExternalNetElement ) ) {
            return "Source is not a net element!";
        }
        else if( ! ( target instanceof YExternalNetElement ) ) {
            return "Sink is not a net element!";
        }
        else if( source == target ) {
            return "Cannot have a flow connect the same net element to itself!";
        }
        return null;
    }
}
