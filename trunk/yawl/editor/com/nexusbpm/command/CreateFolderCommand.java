/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.util.LinkedList;
import java.util.List;

import operation.WorkflowOperation;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;

import com.nexusbpm.editor.tree.SharedNode;
import com.nexusbpm.editor.tree.SharedNodeTreeModel;

/**
 * The CreateFolderCommand creates a sub-folder in the specified folder.
 * 
 * @author Nathan Rose
 */
public class CreateFolderCommand extends AbstractCommand {
	private DataContext context;
    private SharedNode parentNode;
    private String parentPath;
    private String folderName;
    private DataProxy folderProxy;
    private String fullname;
	
    /**
     * NOTE: the parent proxy needs to be connected to the context.
     * @param parent
     * @param netName
     */
	public CreateFolderCommand( SharedNode parentNode, String folderName ) {
        this.parentNode = parentNode;
        this.parentPath = parentNode.getProxy().getData().toString();
		this.context = parentNode.getProxy().getContext();
        this.folderName = folderName;
	}
	
    /**
     * @see com.nexusbpm.command.AbstractCommand#attach()
     */
    @Override
    protected void attach() throws Exception {
        context.attachProxy( folderProxy, fullname, parentNode.getProxy() );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#detach()
     */
    @Override
    protected void detach() throws Exception {
        context.detachProxy( folderProxy, fullname, parentNode.getProxy() );
    }
    
    /**
     * @see com.nexusbpm.command.AbstractCommand#perform()
     */
    @Override
    protected void perform() throws Exception {
        List<String> ids = new LinkedList<String>();
        for( int index = 0; index < parentNode.getChildCount(); index++ ) {
            ids.add( ((SharedNode) parentNode.getChildAt( index )).getProxy().getLabel() );
        }
        String name = WorkflowOperation.getAvailableID( ids, WorkflowOperation.convertNameToID( folderName ) );
        
        fullname = parentPath + "/";
        if( fullname.endsWith( "//" ) )
            fullname = fullname.substring( 0, fullname.length() - 1 );
        fullname = fullname + name;
        
        folderProxy = context.createProxy( fullname, (SharedNodeTreeModel) parentNode.getTreeModel() );
        folderProxy.setLabel( fullname.substring( fullname.lastIndexOf( "/" ) + 1 ) );
    }
}
