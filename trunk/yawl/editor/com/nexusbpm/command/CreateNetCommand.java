/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.awt.geom.Rectangle2D;

import operation.WorkflowOperation;
import au.edu.qut.yawl.elements.YInputCondition;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YOutputCondition;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;

import com.nexusbpm.editor.persistence.EditorDataProxy;
import com.nexusbpm.editor.tree.SharedNode;
import com.nexusbpm.editor.tree.SharedNodeTreeModel;

/**
 * The CreateNetCommand creates a net in the specified specification.
 * The created net is stored in the command for later undoing.
 * 
 * @author Nathan Rose
 */
public class CreateNetCommand extends AbstractCommand {
	private DataContext context;
    private EditorDataProxy<YSpecification> specProxy;
    private SharedNode specNode;
    private YNet net;
    private DataProxy<YNet> netProxy;
    private String netName;
    
    private YInputCondition input;
    private DataProxy<YInputCondition> inputProxy;
    private YOutputCondition output;
    private DataProxy<YOutputCondition> outputProxy;
    
    /**
     * NOTE: the parent proxy needs to be connected to the context.
     * @param parent
     * @param netName
     */
	public CreateNetCommand( SharedNode specNode, String netName) {
        this.specNode = specNode;
        this.specProxy = specNode.getProxy();
		this.context = specProxy.getContext();
        this.netName = netName;
	}
	
    /**
     * @see com.nexusbpm.command.AbstractCommand#attach()
     */
    @Override
    protected void attach() throws Exception {
        WorkflowOperation.attachDecompositionToSpec( net, specProxy.getData() );
        context.attachProxy( netProxy, net, specProxy );
        WorkflowOperation.attachNetElementToNet( input, net );
        context.attachProxy( inputProxy, input, netProxy );
        WorkflowOperation.attachNetElementToNet( output, net );
        context.attachProxy( outputProxy, output, netProxy );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#detach()
     */
    @Override
    protected void detach() throws Exception {
        WorkflowOperation.detachNetElementFromNet( output );
        context.detachProxy( outputProxy, output, netProxy );
        WorkflowOperation.detachNetElementFromNet( input );
        context.detachProxy( inputProxy, input, netProxy );
        WorkflowOperation.detachDecompositionFromSpec( net );
        context.detachProxy( netProxy, net, specProxy );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#perform()
     */
    @Override
    protected void perform() throws Exception {
        net = WorkflowOperation.createNet( netName, "Net", specProxy.getData() );
        netProxy = context.createProxy( net, (SharedNodeTreeModel) specNode.getTreeModel() );
        input = WorkflowOperation.createInputCondition();
        WorkflowOperation.setBoundsOfNetElement( input, new Rectangle2D.Double( 5, 100, 50, 50 ) );
        inputProxy = context.createProxy( input, (SharedNodeTreeModel) specNode.getTreeModel() );
        output = WorkflowOperation.createOutputCondition();
        WorkflowOperation.setBoundsOfNetElement( output, new Rectangle2D.Double( 300, 100, 50, 50 ) );
        outputProxy = context.createProxy( output, (SharedNodeTreeModel) specNode.getTreeModel() );
    }
}
