/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package com.nexusbpm.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;
import au.edu.qut.yawl.util.VisitSpecificationOperation;
import au.edu.qut.yawl.util.VisitSpecificationOperation.Visitor;

import com.nexusbpm.editor.tree.SharedNode;
import com.nexusbpm.editor.tree.SharedNodeTreeModel;

/**
 * The RemoveVirtualFolderCommand removes a folder and all the folder's children.
 * 
 * @author Nathan Rose
 */
public class RemoveVirtualFolderCommand extends AbstractCommand {
    private SharedNode folderNode;
	private DataProxy folderProxy;
    
    private DataContext context;
    
    private List<DataProxy> proxies;
    private Map<DataProxy, Object> data;
    private Map<DataProxy, DataProxy> parents;
    
	public RemoveVirtualFolderCommand( SharedNode folder ) {
        this.folderNode = folder;
        this.folderProxy = folderNode.getProxy();
        this.context = folderProxy.getContext();
	}
    
    /**
     * Removes the folder and its children.
     * (Attach and detach are reversed for remove commands).
     */
    protected void attach() {
        for( int index = proxies.size() - 1; index >= 0; index-- ) {
            DataProxy proxy = proxies.get( index );
            Object object = data.get( proxy );
            DataProxy parent = parents.get( proxy );
            
            if( object instanceof YSpecification ) {
                context.delete( proxy );
            }
            else {
                context.detachProxy( proxy, object, parent );
            }
        }
    }
    
    /**
     * Reattaches the folder and its children.
     * (Attach and detach are reversed for remove commands).
     */
    protected void detach() {
        for( int index = 0; index < proxies.size(); index++ ) {
            DataProxy proxy = proxies.get( index );
            Object object = data.get( proxy );
            DataProxy parent = parents.get( proxy );
            
            context.attachProxy( proxy, object, parent );
            if( object instanceof YSpecification ) {
                context.save( proxy );
            }
        }
    }
    
    protected void perform() {
        proxies = new ArrayList<DataProxy>();
        data = new HashMap<DataProxy, Object>();
        parents = new HashMap<DataProxy, DataProxy>();
        
        SharedNode parentNode = (SharedNode) folderNode.getParent();
        assert parentNode != null : "parentNode was null";
        
        SharedNodeTreeModel model = (SharedNodeTreeModel) folderNode.getTreeModel();
        
        visitFolder( folderNode, parentNode, model, new SpecVisitor() );
    }
    
    private void visitFolder( SharedNode folder, SharedNode parent, SharedNodeTreeModel model, Visitor v ) {
        if( folder.getProxy().getData() instanceof YSpecification ) {
            VisitSpecificationOperation.visitSpecification(
                    (YSpecification) folder.getProxy().getData(),
                    parent.getProxy().getData(),
                    v );
        }
        else {
            v.visit( folder.getProxy().getData(), parent.getProxy().getData(), "" );
            if( model.getChildCount( folder ) > 0 ) {
                List<SharedNode> children = folder.getChildren();
                assert children != null : "folder had children but returned a null list";
                for( SharedNode child : children ) {
                    visitFolder( child, folder, model, v );
                }
            }
        }
    }
    
    private class SpecVisitor implements Visitor {
        /**
         * @see au.edu.qut.yawl.util.VisitSpecificationOperation.Visitor#visit(Object, Object, String)
         */
        public void visit( Object child, Object parent, String childLabel ) {
            DataProxy childProxy = context.getDataProxy( child, null );
            if( childProxy != null ) {
                DataProxy parentProxy = context.getParentProxy( childProxy );
                proxies.add( childProxy );
                data.put( childProxy, child );
                parents.put( childProxy, parentProxy );
            }
        }
    }
}
