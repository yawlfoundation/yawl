package com.nexusbpm.editor.tree;

import java.beans.PropertyChangeEvent;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.swing.tree.DefaultTreeModel;

import operation.WorkflowOperation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.elements.YAWLServiceGateway;
import au.edu.qut.yawl.elements.YCondition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.state.YInternalCondition;
import au.edu.qut.yawl.persistence.managed.DataProxy;
import au.edu.qut.yawl.persistence.managed.DataProxyStateChangeListener;

import com.nexusbpm.editor.persistence.EditorDataProxy;

public class SharedNodeTreeModel extends DefaultTreeModel implements DataProxyStateChangeListener {

	private static final Log LOG = LogFactory.getLog( SharedNodeTreeModel.class );

	public void propertyChange(PropertyChangeEvent evt) {
	}

	public void proxyAttached(DataProxy proxy, Object data) {
        if( !shouldFilter(proxy) )
            insertNodeInto( ((EditorDataProxy) proxy).getTreeNode(),
                ((EditorDataProxy)proxy.getContext().getParentProxy(proxy)).getTreeNode() );
	}
    
    public void insertNodeInto( SharedNode newChild, SharedNode parent ) {
        int index = 0;
        while( index < super.getChildCount( parent ) ) {
            SharedNode neighbor = (SharedNode) super.getChild( parent, index );
            if( comparator.compare( newChild, neighbor ) <= 0 ) {
                insertNodeInto( newChild, parent, index );
                return;
            }
            index += 1;
        }
        insertNodeInto( newChild, parent, index );
    }

	public void proxyDetached(DataProxy proxy, Object data) {
//		SharedNode node = treeNodeCache.get(proxy);
//		SharedNode parent = (SharedNode) node.getParent();
//		this.removeNodeFromParent(node);
//		treeNodeCache.remove(proxy);
		if (!shouldFilter(data)) {
			super.removeNodeFromParent(((EditorDataProxy) proxy).getTreeNode());
//			super.reload();
			LOG.info("Well at least I tried to remove it...");
		}
	}

//	 protected SharedNode root;

	public SharedNodeTreeModel(SharedNode root) {
		super(root);
//		this.root = root;
	}
	
	public List getChildren(SharedNode parent) {
		List<SharedNode> retval = new ArrayList<SharedNode>();
		SharedNode node;
		EditorDataProxy proxy = parent.getProxy();
		Set set = proxy.getContext().getChildren(proxy, false);
		if (set != null) { 
			for (Object childProxy: set) {
//				if (!treeNodeCache.containsKey(childProxy)) 
				{
					node = new SharedNode((EditorDataProxy) childProxy);
					((EditorDataProxy) childProxy).addChangeListener(this);
					String x = null;
					try {
						String label = ((EditorDataProxy) childProxy).getLabel();
						if (label == null) label = "Uninitialized";
						x = URLDecoder.decode(label, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					int y1 = x.lastIndexOf("/");
					if (y1 == x.length() - 1) {
						y1 = x.substring(0,y1 - 1).lastIndexOf("/");
					}
					int y2 = x.lastIndexOf("\\");
					String x2 = x.substring(Math.max(y1, y2) + 1);
					node.getProxy().setLabel(x2);
					if (!shouldFilter(childProxy)) {
//						super.insertNodeInto(node, parent, parent.getChildCount());
                        insertNodeInto(node, parent);
					}
				}
				if (!shouldFilter(childProxy))
					retval.add(node);
			}
		}
//		Collections.sort(retval, comparator);
		return retval;
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
		LOG.info("filter: " + data.toString() + ":" + shouldFilter + ":" + data.getClass().getName());
		return shouldFilter;
	}
	
	private static SharedNodeComparator comparator = new SharedNodeComparator();
	private static class SharedNodeComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			SharedNode s1 = (SharedNode) o1;
			SharedNode s2 = (SharedNode) o2;
			int result = s1.getProxy().getData().toString().compareTo(s2.getProxy().getData().toString());
			return result;
		}
	}	
	
	  public boolean isLeaf(Object node) {
		  SharedNode aNode = (SharedNode) node;
		  Object aValue = aNode.getProxy().getData();
		  boolean retval = false;
		  if (aValue instanceof YAWLServiceGateway 
				  || aValue instanceof YFlow
				  || aValue instanceof YExternalNetElement
		  ) retval = true;
		  return retval;
	  }

	  public int getChildCount(Object parent) {
		  if (((SharedNode)parent).getChildCount() == 0) {
			  getChildren((SharedNode) parent);
		  }
		  return super.getChildCount(parent);
	  }

	  public Object getChild(Object parent, int index) {
		  if (((SharedNode)parent).getChildCount() == 0) {
			  getChildren((SharedNode) parent);
		  }
		  return super.getChild(parent, index);
	  }

	  public int getIndexOfChild(Object parent, Object child) {
		  if (((SharedNode)parent).getChildCount() == 0) {
			  getChildren((SharedNode) parent);
		  }
		  return super.getIndexOfChild(parent, child);
	  }
}

