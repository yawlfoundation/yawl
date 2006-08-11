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

import au.edu.qut.yawl.persistence.dao.DatasourceFolder;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;
import au.edu.qut.yawl.persistence.managed.DataProxyStateChangeListener;

/**
 * The CreateFolderCommand creates a sub-folder in the specified folder.
 * 
 * @author Nathan Rose
 */
public class CreateFolderCommand extends AbstractFileSystemCommand {
    private static final Log LOG = LogFactory.getLog( CreateSpecificationCommand.class );
    
	private DataContext context;
    
    private DataProxy<DatasourceFolder> parentProxy;
    private String folderName;
	
    private DatasourceFolder folder;
    private DataProxy folderProxy;
    
    private DataProxyStateChangeListener listener;
    
    /**
     * NOTE: the parent proxy needs to be connected to the context.
     * @param parent
     * @param netName
     */
	public CreateFolderCommand( DataProxy parentProxy, String folderName,
            DataProxyStateChangeListener listener ) {
        this.parentProxy = parentProxy;
        this.folderName = folderName;
        this.listener = listener;
		this.context = parentProxy.getContext();
	}
	
    /**
     * @see com.nexusbpm.command.AbstractCommand#attach()
     */
    @Override
    protected void attach() throws Exception {
        if( folder.isSchemaFile() ) {
            if( folder.getFile().mkdir() ) {
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
        if( folder.isSchemaFile() ) {
            if( folder.getFile().delete() ) {
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
        DatasourceFolder parent = parentProxy.getData();
        String parentPath = parent.getPath();
        List<String> ids = getChildNames( parentProxy );
        String name;
        
        name = WorkflowOperation.getAvailableID( ids, WorkflowOperation.convertNameToID( folderName ) );
        
        String fullname = parentPath + "/";
        if( fullname.endsWith( "//" ) )
            fullname = fullname.substring( 0, fullname.length() - 1 );
        fullname = fullname + name;
        
        if( parentPath.startsWith( "file:" ) ) {
            // create new folder
            LOG.debug( "creating real folder with name " + fullname );
            folder = new DatasourceFolder( new File( new URI( fullname ) ), parent );
        }
        else {
            // create new virtual folder
            LOG.debug( "creating virtual folder with name " + fullname );
            folder = new DatasourceFolder( name, parent );
        }
        
        folderProxy = context.createProxy( folder, listener );
    }
}
