/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;

/**
 * The SaveSpecificationCommand saves a specification to its containing context.
 * This command is <em>not</em> undo-able.
 * 
 * @author Nathan Rose
 */
public class SaveSpecificationCommand implements Command {
    private DataProxy<YSpecification> specProxy;
    
    private DataContext context;
	
	public SaveSpecificationCommand( DataProxy specProxy ) {
        this.specProxy = specProxy;
        this.context = specProxy.getContext();
	}
    
    /**
     * @see com.nexusbpm.command.Command#execute()
     */
    public void execute() throws Exception {
        context.save( specProxy );
    }
    
    /**
     * @see com.nexusbpm.command.Command#redo()
     */
    public void redo() throws Exception {
        throw new UnsupportedOperationException( "Cannot redo a save!" );
    }
    
    /**
     * @see com.nexusbpm.command.Command#supportsUndo()
     */
    public boolean supportsUndo() {
        return false;
    }
    
    /**
     * @see com.nexusbpm.command.Command#undo()
     */
    public void undo() throws Exception {
        throw new UnsupportedOperationException( "Cannot undo a save!" );
    }
}
