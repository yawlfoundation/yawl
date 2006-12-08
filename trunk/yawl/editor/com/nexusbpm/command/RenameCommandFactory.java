/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.dao.DatasourceFolder;
import au.edu.qut.yawl.persistence.managed.DataProxy;

/**
 * The RenameCommandFactory returns the appropriate rename command for a DataProxy.
 * 
 * @author Nathan Rose
 */
public final class RenameCommandFactory {
	public static final AbstractCommand getRenameCommand(
			DataProxy proxy, String newName, String oldName ) {
		Object data = proxy.getData();
		if( data instanceof YSpecification ) {
			return new RenameSpecificationCommand( proxy, newName );
		}
		else if( data instanceof DatasourceFolder ) {
			return new RenameFolderCommand( proxy, newName );
		}
		else {
			return new RenameElementCommand( proxy, newName, oldName );
		}
	}
	
	public static abstract class AbstractRenameCommand extends AbstractCommand {
		protected DataProxy proxy;
	    
	    private String oldName;
	    private String newName;
	    
	    protected AbstractRenameCommand( DataProxy proxy, String newName, String oldName ) {
	    	this.proxy = proxy;
	        this.oldName = oldName;
	        this.newName = newName;
	    }
	    
	    /**
	     * @see com.nexusbpm.command.AbstractCommand#attach()
	     */
	    @Override
	    protected final void attach() throws Exception {
	        rename( newName, oldName );
	    }
	    
	    /**
	     * @see com.nexusbpm.command.AbstractCommand#detach()
	     */
	    @Override
	    protected final void detach() throws Exception {
	        rename( oldName, newName );
	    }
	    
	    /**
	     * @see com.nexusbpm.command.AbstractCommand#perform()
	     */
	    @Override
	    protected final void perform() throws Exception {
	        // empty (we already have old and new names because they were passed to the constructor)
	    }
	    
	    protected abstract void rename( String newName, String oldName );
	}
}
