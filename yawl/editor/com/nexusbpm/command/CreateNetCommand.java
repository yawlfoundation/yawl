/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import operation.WorkflowOperation;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;

import com.nexusbpm.editor.persistence.EditorDataProxy;

/**
 * The CreateNetCommand creates a net in the specified specification.
 * The created net is stored in the command for later undoing.
 * 
 * @author Nathan Rose
 */
public class CreateNetCommand extends AbstractCommand {

	private DataContext context;
    private EditorDataProxy<YSpecification> specProxy;
    private YNet net;
    private DataProxy<YNet> netProxy;
    private String netName;
	
    /**
     * NOTE: the parent proxy needs to be connected to the context.
     * @param parent
     * @param netName
     */
	public CreateNetCommand(EditorDataProxy<YSpecification> specProxy, String netName) {
		this.context = specProxy.getContext();
        this.specProxy = specProxy;
        this.netName = netName;
	}
	
    /**
     * @see com.nexusbpm.command.AbstractCommand#attach()
     */
    @Override
    protected void attach() throws Exception {
        WorkflowOperation.attachDecompositionToSpec( specProxy.getData(), net );
        context.attachProxy( netProxy, net );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#detach()
     */
    @Override
    protected void detach() throws Exception {
        WorkflowOperation.detachDecompositionFromSpec( net );
        context.detachProxy( netProxy );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#perform()
     */
    @Override
    protected void perform() throws Exception {
        net = WorkflowOperation.createNet( netName );
        netProxy = context.createProxy( net, null );
    }
}
