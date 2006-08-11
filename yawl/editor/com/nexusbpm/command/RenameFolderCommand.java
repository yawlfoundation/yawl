/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import operation.WorkflowOperation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.dao.DatasourceFolder;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;
import au.edu.qut.yawl.persistence.managed.DataProxyStateChangeListener;

/**
 * The RenameFolderCommand renames a folder and all its children.
 * 
 * @author Nathan Rose
 */
public class RenameFolderCommand extends AbstractFileSystemCommand {
    private static final Log LOG = LogFactory.getLog( RenameFolderCommand.class );
    
    private DataContext context;
    
    private DataProxy folderProxy;
    
    private String oldName;
    private String newName;
    
    private DatasourceFolder folder;
    
    private List<DatasourceFolder> children;
    private List<YSpecification> specs;
    
	public RenameFolderCommand( DataProxy folderProxy, String newName ) {
        this.folderProxy = folderProxy;
        this.newName = newName;
        this.context = folderProxy.getContext();
	}
    
	/**
     * @see com.nexusbpm.command.AbstractCommand#attach()
     */
    @Override
    protected void attach() throws Exception {
        rename( oldName, newName );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#detach()
     */
    @Override
    protected void detach() throws Exception {
        rename( newName, oldName );
    }
    
    private void rename( String oldName, String newName ) throws URISyntaxException, IOException {
        String oldPath = folder.getPath();
        String oldURI = "";
        if( folder.isSchemaFile() ) oldURI = folder.getFile().toURI().toASCIIString();
        folder.setName( newName );
        String newPath = folder.getPath();
        if( folder.isSchemaFile() ) {
            File to = new File( new URI( newPath ) );
            if( folder.getFile().renameTo( to ) ) {
                folder.setFile( to );
                LOG.info( "Rename folder from " + oldPath + " to " + newPath + " was successful" );
            }
            else {
                throw new IOException( "Could not rename directory from " + oldPath + " to " + newPath + "!" );
            }
            String newURI = folder.getFile().toURI().toASCIIString();
            for( DatasourceFolder child : children ) {
                String childURI = child.getFile().toURI().toASCIIString();
                if( childURI.startsWith( oldURI ) ) {
                    File n = new File( new URI( newURI + childURI.substring( oldURI.length() ) ) );
                    child.setFile( n );
                }
            }
        }
        
        for( YSpecification spec : specs ) {
            String id = spec.getID();
            if( id.startsWith( oldPath ) ) {
                String newID = newPath + id.substring( oldPath.length() );
                spec.setID( newID );
                if( context.getDataProxy( spec ) != null ) {
                    context.save( context.getDataProxy( spec ) );
                }
            }
        }
        
        folderProxy.setLabel( newName );
        folderProxy.fireUpdated(
                DataProxyStateChangeListener.PROPERTY_NAME,
                oldName, newName );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#perform()
     */
    @Override
    protected void perform() throws Exception {
        folder = (DatasourceFolder) folderProxy.getData();
        List<String> ids = getChildNames( context.getParentProxy( folderProxy ) );
        
        String oldPath = folder.getPath();
        oldName = folder.getName();
        newName = WorkflowOperation.getAvailableID( ids, WorkflowOperation.convertNameToID( newName ) );
        
        specs = new LinkedList<YSpecification>();
        children = new LinkedList<DatasourceFolder>();
        collectChildren( folderProxy, specs, children );
        children.remove( folder );
    }
}
