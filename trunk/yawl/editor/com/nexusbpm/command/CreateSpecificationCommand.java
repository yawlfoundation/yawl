/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.io.File;

import operation.WorkflowOperation;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;

import com.nexusbpm.editor.tree.SharedNode;
import com.nexusbpm.editor.tree.SharedNodeTreeModel;

/**
 * The CreateSpecificationCommand creates a specification under the specified
 * parent. The created specification is stored in the command for later undoing.
 * 
 * @author Nathan Rose
 */
public class CreateSpecificationCommand extends AbstractCommand {
	private DataContext context;
    private SharedNode parentNode;
    private YSpecification specification;
    private DataProxy<YSpecification> specProxy;
    private String specName;
	
    /**
     * NOTE: the parent proxy needs to be connected to the context.
     * @param parent
     * @param specName
     */
	public CreateSpecificationCommand( SharedNode parentNode, String specName) {
        this.parentNode = parentNode;
		this.context = parentNode.getProxy().getContext();
        this.specName = specName;
	}
	
    /**
     * @see com.nexusbpm.command.AbstractCommand#attach()
     */
    @Override
    protected void attach() throws Exception {
        context.attachProxy( specProxy, specification, parentNode.getProxy() );
        context.save( specProxy );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#detach()
     */
    @Override
    protected void detach() throws Exception {
        context.delete( specProxy );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#perform()
     */
    @Override
    protected void perform() throws Exception {
        Object parent = parentNode.getProxy().getData();
        if( parent instanceof File ) {
            parent = ((File) parent).toURI();
        }
        specification = WorkflowOperation.createSpecification(
                parent.toString(), specName, "Specification" );
        specProxy = context.createProxy( specification, (SharedNodeTreeModel) parentNode.getTreeModel() );
    }
}
