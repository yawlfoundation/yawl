/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.io.File;
import java.net.URI;
import java.util.List;

import operation.WorkflowOperation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;

import com.nexusbpm.editor.tree.SharedNode;
import com.nexusbpm.editor.tree.SharedNodeTreeModel;

/**
 * The CreateFolderCommand creates a sub-folder in the specified folder.
 * 
 * @author Nathan Rose
 */
public class CreateFolderCommand extends AbstractFileSystemCommand {
    private static final Log LOG = LogFactory.getLog( CreateSpecificationCommand.class );
    
	private DataContext context;
    
    private SharedNode parentNode;
    private DataProxy parentProxy;
    private String folderName;
	
    private Object folder;
    private DataProxy folderProxy;
    
    /**
     * NOTE: the parent proxy needs to be connected to the context.
     * @param parent
     * @param netName
     */
	public CreateFolderCommand( SharedNode parentNode, String folderName ) {
        this.parentNode = parentNode;
        this.parentProxy = parentNode.getProxy();
        this.folderName = folderName;
		this.context = parentProxy.getContext();
	}
	
    /**
     * @see com.nexusbpm.command.AbstractCommand#attach()
     */
    @Override
    protected void attach() throws Exception {
        if( folder instanceof File ) {
            if( ((File) folder).mkdir() ) {
                LOG.debug( "Folder '" + folder + "' was created successfully" );
            }
            else {
                LOG.warn( "The folder '" + folder + "' could not be created." );
            }
        }
        context.attachProxy( folderProxy, folder, parentProxy );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#detach()
     */
    @Override
    protected void detach() throws Exception {
        context.detachProxy( folderProxy, folder, parentProxy );
        if( folder instanceof File ) {
            if( ((File) folder).delete() ) {
                LOG.debug( "file '" + folder + "' was deleted successfully" );
            }
            else {
                LOG.warn( "The folder '" + folder + "' could not be deleted." );
            }
        }
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#perform()
     */
    @Override
    protected void perform() throws Exception {
        Object parent = parentProxy.getData();
        String parentPath = getPath( parent, true );
        List<String> ids = getChildNames( parentNode );
        String name;
        
        name = WorkflowOperation.getAvailableID( ids, WorkflowOperation.convertNameToID( folderName ) );
        
        String fullname = parentPath + "/";
        if( fullname.endsWith( "//" ) )
            fullname = fullname.substring( 0, fullname.length() - 1 );
        fullname = fullname + name;
        
        if( parentPath.startsWith( "file:" ) ) {
            // create new folder
            LOG.debug( "creating real folder with name " + fullname );
            folder = new File( new URI( fullname ) );
        }
        else {
            // create new virtual folder
            LOG.debug( "creating virtual folder with name " + fullname );
            folder = fullname;
        }
        
        folderProxy = context.createProxy( folder, (SharedNodeTreeModel) parentNode.getTreeModel() );
        folderProxy.setLabel( fullname.substring( fullname.lastIndexOf( "/" ) + 1 ) );
    }
}
