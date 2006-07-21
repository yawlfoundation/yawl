package com.nexusbpm.editor.tree;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.elements.YAWLServiceGateway;
import au.edu.qut.yawl.elements.YCondition;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.state.YInternalCondition;
import au.edu.qut.yawl.persistence.managed.DataProxy;
import au.edu.qut.yawl.persistence.managed.DataProxyStateChangeListener;

import com.nexusbpm.editor.persistence.EditorDataProxy;

public class SharedNodeTreeModel extends DefaultTreeModel implements DataProxyStateChangeListener {

	public void propertyChange(PropertyChangeEvent evt) {
	}

	public void proxyAttached(DataProxy proxy, Object data) {
	}

	public void proxyDetached(DataProxy proxy, Object data) {
		treeNodeCache.remove(proxy);
		LOG.info("Well at least I tried to remove it...");
	}

	private static final Log LOG = LogFactory.getLog( SharedNodeTreeModel.class );
	public static Hashtable<EditorDataProxy, SharedNode> treeNodeCache = new Hashtable<EditorDataProxy, SharedNode>();

//	 protected SharedNode root;

	public SharedNodeTreeModel(SharedNode root) {
		super(root);
//		this.root = root;
	}
	
	public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
		LOG.info("Change " + evt.getPropertyName() + " from " + evt.getOldValue() + " to " + evt.getNewValue()); 
	}

	public List getChildren(SharedNode parent) {
		List<SharedNode> retval = new ArrayList<SharedNode>();
		EditorDataProxy proxy = parent.getProxy();
		Set set = proxy.getContext().getChildren(proxy, false);
		if (set != null) { 
			for (Object childProxy: set) {
				if (!treeNodeCache.containsKey(childProxy)) {
					SharedNode node = new SharedNode((EditorDataProxy) childProxy);
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
					treeNodeCache.put((EditorDataProxy) childProxy, node);
				}
				boolean shouldFilter = false;
				if (((EditorDataProxy)childProxy).getData() instanceof YAWLServiceGateway
		 				 || ((EditorDataProxy)childProxy).getData() instanceof YFlow
		 				 || ((EditorDataProxy)childProxy).getData() instanceof YInternalCondition
		 				 || ((EditorDataProxy)childProxy).getData() instanceof YCondition
 				 ) {
					shouldFilter = true;
				}
				if (!shouldFilter)
					retval.add(treeNodeCache.get(childProxy));
			}
		}
		Collections.sort(retval, comparator);
		return retval;
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
	
	public Object getRoot() { return root; }

	  // Tell JTree whether an object in the tree is a leaf or not
	  public boolean isLeaf(Object node) {
		  SharedNode aNode = (SharedNode) node;
		  Object aValue = aNode.getProxy().getData();
		  boolean retval = false;
		  if (aValue instanceof YAWLServiceGateway 
				  || aValue instanceof YFlow
		  ) retval = true;
//too file specific;		  if (node instanceof File) {
//			  if (((File) node).isFile() && !((File) node).getName().endsWith("xml")) {}
//		  }
//		  retval = ((SharedNode) node).getProxy().getData().getClass() != String.class;
//		  retval &= ((SharedNode) node).getProxy().getData().getClass() != DatasourceRoot.class;
		  return retval;
	  }

	  public int getChildCount(Object parent) {
		  return getChildren((SharedNode) parent).size();
	  }

	  public Object getChild(Object parent, int index) {
		  return getChildren((SharedNode) parent).get(index);
	  }

	  public int getIndexOfChild(Object parent, Object child) {
		  return getChildren((SharedNode) parent).indexOf(child);
	  }

	  // This method is only invoked by the JTree for editable trees.  
	  // This TreeModel does not allow editing, so we do not implement 
	  // this method.  The JTree editable property is false by default.
	  public void valueForPathChanged(TreePath path, Object newvalue) {}

	  // Since this is not an editable tree model, we never fire any events,
	  // so we don't actually have to keep track of interested listeners.
	  public void addTreeModelListener(TreeModelListener l) {}
	  public void removeTreeModelListener(TreeModelListener l) {}

}

