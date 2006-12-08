/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.tree;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.elements.YAWLServiceGateway;
import au.edu.qut.yawl.elements.YCondition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.state.YInternalCondition;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.persistence.dao.DatasourceFolder;
import au.edu.qut.yawl.persistence.managed.DataProxy;
import au.edu.qut.yawl.persistence.managed.DataProxyStateChangeListener;

import com.nexusbpm.editor.persistence.EditorDataProxy;
import com.nexusbpm.operation.WorkflowOperation;

public class SharedNodeTreeModel extends DefaultTreeModel implements DataProxyStateChangeListener {

	private static final Log LOG = LogFactory.getLog( SharedNodeTreeModel.class );

	public void propertyChange(PropertyChangeEvent evt) {
        if( evt.getPropertyName().equals( DataProxyStateChangeListener.PROPERTY_NAME ) ) {
            if( !shouldFilter(evt.getSource()) ) {
                doNodeChange( ((EditorDataProxy)evt.getSource()).getTreeNode() );
            }
        }
	}

    public void proxyAttaching(DataProxy proxy, Object data, DataProxy parent) {}
	public void proxyAttached(DataProxy proxy, Object data, DataProxy parent) {
        SharedNode parentNode = (SharedNode) getRoot();
        if( parent != null )
            parentNode = ((EditorDataProxy) parent).getTreeNode();
        if( !shouldFilter(proxy) ) {
            if( parentNode != null ) {
                insertNodeInto( ((EditorDataProxy) proxy).getTreeNode(), parentNode );
            }
            else
                setRoot( ((EditorDataProxy) proxy).getTreeNode() );
        }
	}
    
    public void insertNodeInto( SharedNode newChild, SharedNode parent ) {
        int index = 0;
        while( index < parent.getChildCount() ) {
            SharedNode neighbor = (SharedNode) parent.getChildAt( index );
            if( newChild == neighbor ) {
                new RuntimeException( "child already added!" ).fillInStackTrace().printStackTrace();
                return;
            }
            
            if( comparator.compare( newChild, neighbor ) < 0 ) {
                doInsertion( newChild, parent, index );
                return;
            }
            index += 1;
        }
        doInsertion( newChild, parent, index );
    }
    
    private void doNodeChange( final SharedNode node ) {
        Runnable r = new Runnable() {
            public void run() {
                SharedNodeTreeModel.super.nodeChanged( node );
                if( node.getParent() != null ) {
                    SharedNode parent = (SharedNode) node.getParent();
                    Collections.sort( parent.getChildren(), comparator );
                    SharedNodeTreeModel.super.nodeStructureChanged( parent );
                }
            }
        };
        runOnSwingThread( r );
    }
    
    private void doInsertion( final SharedNode newChild, final SharedNode parent, final int index ) {
        Runnable r = new Runnable() {
            public void run() {
                insertNodeInto( newChild, parent, index );
            }
        };
        runOnSwingThread( r );
    }
    
    private void doRemoval( final SharedNode node ) {
        Runnable r = new Runnable() {
            public void run() {
                SharedNodeTreeModel.super.removeNodeFromParent( node );
            }
        };
        runOnSwingThread( r );
    }
    
    private void runOnSwingThread( Runnable r ) {
        if( SwingUtilities.isEventDispatchThread() ) {
            r.run();
        }
        else {
            try {
                SwingUtilities.invokeAndWait( r );
            }
            catch( Exception e ) {
                // this shouldn't happen
                LOG.error( "Error updating tree!", e );
            }
        }
    }

	public void proxyDetaching(DataProxy proxy, Object data, DataProxy parent) {
//		SharedNode node = treeNodeCache.get(proxy);
//		SharedNode parent = (SharedNode) node.getParent();
//		this.removeNodeFromParent(node);
//		treeNodeCache.remove(proxy);
		if (!shouldFilter(proxy)) {
            SharedNode parentNode = (SharedNode) getRoot();
            if( parent != null )
                parentNode = ((EditorDataProxy) parent).getTreeNode();
//            if( parentNode != null )
//                parentNode.childCount = null;
			doRemoval(((EditorDataProxy) proxy).getTreeNode());
//			super.reload();
//			LOG.info("Well at least I tried to remove it...");
		}
	}
    public void proxyDetached(DataProxy proxy, Object data, DataProxy parent) {}

    // the components list is read only
    private boolean readOnly;
    
    public boolean isReadOnly() {
        return readOnly;
    }
    
    public SharedNodeTreeModel(SharedNode root) {
        super(root);
    }
    
	public SharedNodeTreeModel(SharedNode root, boolean readOnly) {
		super(root);
        this.readOnly = readOnly;
	}
	
	private boolean shouldFilter(Object proxy) {
		boolean shouldFilter = false;
		Object data = ((EditorDataProxy) proxy).getData();
		shouldFilter = ( data instanceof YFlow
			 || data instanceof YInternalCondition
			 || data instanceof YCondition );
        shouldFilter = shouldFilter ||
            ( data instanceof YAWLServiceGateway &&
                    WorkflowOperation.isGatewayANexusGateway( (YAWLServiceGateway) data ) );
//		LOG.info("filter: " + data.toString() + ":" + shouldFilter + ":" + data.getClass().getName());
		return shouldFilter;
	}
	
	private static SharedNodeComparator comparator = new SharedNodeComparator();
	private static class SharedNodeComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			SharedNode s1 = (SharedNode) o1;
			SharedNode s2 = (SharedNode) o2;
            if( isDirectory( s1.getProxy() ) && ! isDirectory( s2.getProxy() ) ) {
                return -1;
            }
            else if( ! isDirectory( s1.getProxy() ) && isDirectory( s2.getProxy() ) ) {
                return 1;
            }
			int result = s1.getProxy().getLabel().toLowerCase().compareTo(s2.getProxy().getLabel().toLowerCase());
			return result;
		}
        private boolean isDirectory( DataProxy proxy ) {
            if( proxy.getData() instanceof DatasourceFolder ) {
                return ((DatasourceFolder) proxy.getData()).isFolder();
            }
            else {
                return false;
            }
        }
    }

    public boolean isLeaf( Object node ) {
        SharedNode aNode = (SharedNode) node;
        Object aValue = aNode.getProxy().getData();
        if( aNode.initialized == true ) {
            return aNode.getChildCount() == 0;
        }
        else if( aValue instanceof YAWLServiceGateway ||
                aValue instanceof YFlow ||
                aValue instanceof YExternalNetElement ) {
            return true;
        }
        else {
            return getChildCount( node ) == 0;
        }
    }

    public int getChildCount( Object parent ) {
        initializeNode( (SharedNode) parent );
        return super.getChildCount( parent );
    }

    public Object getChild( Object parent, int index ) {
        initializeNode( (SharedNode) parent );
        return super.getChild( parent, index );
    }

    public int getIndexOfChild( Object parent, Object child ) {
        initializeNode( (SharedNode) parent );
        return super.getIndexOfChild( parent, child );
    }
    
    private void initializeNode( SharedNode parent ) {
        if( parent.initialized == false ) {
            List<SharedNode> children = new ArrayList<SharedNode>();
            EditorDataProxy proxy = parent.getProxy();
            Set<DataProxy> set = null;
            try {
            	set = proxy.getContext().getChildren(proxy, false);
            }
            catch( YPersistenceException e ) {
            	LOG.error( "Error getting children of node", e );
            }
            if( set != null ) {
                for( DataProxy childProxy: set ) {
                    SharedNode node = ((EditorDataProxy)childProxy).getTreeNode();
                    
                    if( parent.getChildren() == null || ! parent.getChildren().contains( node ) ) {
                        if( ! childProxy.containsChangeListener( this ) ) {
                            childProxy.addChangeListener( this );
                        }
                        
                        if ( ! shouldFilter( childProxy ) ) {
//                            String x = null;
//                            try {
//                                String label = ((EditorDataProxy) childProxy).getLabel();
//                                if (label == null) label = "Uninitialized";
//                                x = URLDecoder.decode(label, "UTF-8");
//                            } catch (UnsupportedEncodingException e) {
//                                e.printStackTrace();
//                            }
//                            int y1 = x.lastIndexOf("/");
//                            if (y1 == x.length() - 1) {
//                                y1 = x.substring(0,y1 - 1).lastIndexOf("/");
//                            }
//                            int y2 = x.lastIndexOf("\\");
//                            String x2 = x.substring(Math.max(y1, y2) + 1);
//                            node.getProxy().setLabel(x2);
                            
                            children.add(node);
                        }
                    }
                }
            }
            parent.initialized = true;
            for( SharedNode child : children ) {
                if( parent.getChildren() == null || ! parent.getChildren().contains( child ) ) {
                    insertNodeInto(child, parent);
                }
            }
        }
    }
}

