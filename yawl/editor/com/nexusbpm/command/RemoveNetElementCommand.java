/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nexusbpm.operation.WorkflowOperation;



import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;

/**
 * The RemoveNetElementCommand removes a YExternalNetElement from a network.
 * 
 * @author Nathan Rose
 */
public class RemoveNetElementCommand extends AbstractCommand {
    private DataContext context;
    private DataProxy<YExternalNetElement> elementProxy;
    private YExternalNetElement element;
    
    private DataProxy<YNet> netProxy;
    private YNet net;
    
    private List<RemoveFlowCommand> flowCommands;
    
    public RemoveNetElementCommand( DataProxy elementProxy ) {
        this.context = elementProxy.getContext();
        this.elementProxy = elementProxy;
    }
    
    /**
     * Removes the task from its net
     * (Attach and detach are reversed for remove commands).
     */
    protected void attach() {
        for( RemoveFlowCommand c : flowCommands ) {
            c.attach();
        }
        WorkflowOperation.detachNetElementFromNet( element );
        context.detachProxy( elementProxy, element, netProxy );
    }
    
    /**
     * Re-attaches the task to its net
     * (Attach and detach are reversed for remove commands).
     */
    protected void detach() {
        WorkflowOperation.attachNetElementToNet( element, net );
        context.attachProxy( elementProxy, element, netProxy );
        for( RemoveFlowCommand c : flowCommands ) {
            c.detach();
        }
    }
    
    protected void perform() {
        element = elementProxy.getData();
        flowCommands = new ArrayList<RemoveFlowCommand>();
        
        net = element.getParent();
        netProxy = context.getDataProxy( net );
        
        Set<YFlow> flows = new HashSet<YFlow>();
        
        for( YFlow flow : element.getPresetFlows() ) {
            flows.add( flow );
        }
        for( YFlow flow : element.getPostsetFlows() ) {
            flows.add( flow );
        }
        
        for( YFlow flow : flows ) {
            RemoveFlowCommand c = new RemoveFlowCommand( context.getDataProxy( flow ) );
            c.perform();
            flowCommands.add( c );
        }
    }
}
