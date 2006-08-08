/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;

import com.nexusbpm.editor.persistence.EditorDataProxy;
import com.nexusbpm.editor.tree.SharedNode;

/**
 * The DeleteFileCommand deletes a file or folder on the filesystem.
 * This command is not undoable.
 * 
 * @author Nathan Rose
 */
public class DeleteFileCommand implements Command {
    private static final Log LOG = LogFactory.getLog(DeleteFileCommand.class);
    
	private DataProxy folderProxy;
    
    private DataContext context;
    
	public DeleteFileCommand( DataProxy folderProxy ) {
		this.folderProxy = folderProxy;
        context = folderProxy.getContext();
	}
	
    public void execute() throws Exception {
        Object data = folderProxy.getData();
        assert data instanceof File : "delete file command is attempting to delete a non file/folder!";
        EditorDataProxy parentProxy = (EditorDataProxy) context.getParentProxy( folderProxy );
        SharedNode parent = parentProxy.getTreeNode();
        SharedNode folder = ((EditorDataProxy)folderProxy).getTreeNode();
        delete( folder, parent );
        
    }
    
    private void delete( SharedNode node, SharedNode parent ) {
        List l = node.getChildren();
        if( l != null ) {
            for( SharedNode child : new ArrayList<SharedNode>( l ) ) {
                delete( child, node );
            }
        }
        Object data = node.getProxy().getData();
        context.detachProxy( node.getProxy(), data, parent.getProxy() );
        if( data instanceof File ) {
            deleteFile( (File) data );
        }
        else {
            // should be an instance of YSpecification, YNet, YAtomicTask, etc
            LOG.trace( ( data == null ) ? "null" : data.getClass().getName() );
        }
    }
    
    private void deleteFile( File f ) {
        if( f.isDirectory() ) {
            File[] children = f.listFiles();
            for( int index = 0; index < children.length; index++ ) {
                deleteFile( children[ index ] );
            }
        }
        if( ! f.delete() ) {
            f.deleteOnExit();
            LOG.trace( f.toString() + " will be deleted on exit" );
        }
        else {
            LOG.trace( f.toString() + " was successfully deleted" );
        }
    }
    
    public void undo() throws Exception {
    }
    
    public void redo() throws Exception {
    }
    
    public boolean supportsUndo() {
        return false;
    }
}
