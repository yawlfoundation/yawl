package com.nexusbpm.editor.tree;

import java.beans.PropertyChangeEvent;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.persistence.managed.DataProxy;
import au.edu.qut.yawl.persistence.managed.DataProxyStateChangeListener;

import com.nexusbpm.editor.persistence.EditorDataProxy;

/**
 * Abstract tree node for a tree data structure that extends
 * {@link DefaultMutableTreeNode} used in the components list, workspace/network
 * folders, and the data communicator editor. Each node represents a component
 * or an attribute of a component.
 * 
 * @see TreeNode
 * @see AttributeNode
 * 
 * @author Dean Mao
 */
public class SharedNode extends DefaultMutableTreeNode 
implements DataProxyStateChangeListener {

	private static final Log LOG = LogFactory.getLog( SharedNode.class );

	/**
	 * The tree model to which this node belongs.
	 * 
	 * @see TreeNode#getTreeModel()
	 */
	protected TreeModel _treeModel;

	/**
	 * The domain object controller for the domain object which this node
	 * represents.
	 * 
	 * @see TreeNode#getController()
	 */
	protected EditorDataProxy _proxy;
    
    /** Cache the child count (provides a HUGE performance improvement when using file DAO. */
    Integer childCount = null;

	/**
	 * Whether or not to ignore PropertyChangeEvents we get in propertyChange() as
	 * a result of implementing PropertyChangeListener. Usually we don't ignore
	 * them, but sometimes it's useful, such as in the RenameCommand class.
	 */
	protected boolean _ignorePropertyChangeEvents = false;

	/**
	 * @return the name to be displayed for this node.
	 */
	public String getName() {
		return "name";
	}

	/**
	 * Creates a SharedNode for domain object specified by the given controller.
	 * @param controller the controller for the domain object.
	 */
	public SharedNode( EditorDataProxy proxy ) {
		if( proxy != null ) {
			_proxy = proxy;
			_proxy.setTreeNode(this);
			_proxy.addChangeListener( this );
			setUserObject(proxy.getData());
		}
	}

	/**
	 * @return the controller for the domain object which this node represents.
	 */
	public EditorDataProxy getProxy() {
		return _proxy;
	}

	/**
	 * Catches property change events received as a result of implementing
	 * <code>PropertyChangeListener</code>. Property change events may be
	 * ignored.
	 * @param event the property change event that occured.
	 * @see #setIgnorePropertyChangeEvents(boolean)
	 */
	public void propertyChange( PropertyChangeEvent event ) {
        childCount = null;
		String propertyName = event.getPropertyName();
		LOG.debug( "Got property change event: "+propertyName );
	}

	public void proxyAttached(DataProxy proxy, Object data, DataProxy parent) {
        childCount = null;
    }
	public void proxyDetached(DataProxy proxy, Object data) {
        childCount = null;
		LOG.info("shared node rec'd detach " + proxy.getLabel() + ":" + data.toString());		
	}
		
	/**
	 * Sets this node to ignore or not ignore property change events from this
	 * node's domain object.
	 * @param b whether property changed events should be ignored.
	 */
	public void setIgnorePropertyChangeEvents( boolean b ) {
		_ignorePropertyChangeEvents = b;
	}

	/**
	 * @return whether property change events from this node's domain object are
	 *         ignored.
	 */
	public boolean getIgnorePropertyChangeEvents() {
		return _ignorePropertyChangeEvents;
	}

	/**
	 * @return the icon to be used for this node.
	 */
	public ImageIcon getIcon() {
		return _proxy.icon();
	}

	/**
	 * @return the TreeModel to which this node belongs.
	 */
	public TreeModel getTreeModel() {
		if( isRoot() ) {
			return _treeModel;
		}
		else {
			return ( (SharedNode) getRoot() ).getTreeModel();
		}
	}

	/**
	 * Sets the TreeModel to which this node belongs.
	 * @param treeModel the TreeModel to which this node belongs.
	 */
	public void setTreeModel( TreeModel treeModel ) {
		_treeModel = treeModel;
	}

	/**
	 * Removes this as a property change listener of this node's domain object.
	 */
	public void clear() {
		if( LOG.isDebugEnabled() ) {
			LOG.debug( "SharedNode.clear " + getClass().getName() );
		}

		if( _proxy != null ) {
			EditorDataProxy tmp = _proxy;
			_proxy = null;
			tmp.removeChangeListener( this );
		}
	}

	/**
	 * @throws Throwable not thrown.
	 * @see #clear()
	 * @see Object#finalize()
	 */
	public void finalize() throws Throwable {
		if( LOG.isDebugEnabled() ) {
			LOG.debug( "SharedNode.finalize " + getClass().getName() );
		}

		clear();

		super.finalize();
	}
	public boolean isLeaf() {
		boolean retval = false;
		if (getProxy().getData() instanceof YExternalNetElement) 
			retval = true;		
		return retval;
	}
}
