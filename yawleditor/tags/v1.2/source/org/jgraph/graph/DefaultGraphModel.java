/*
 * @(#)DefaultGraphModel.java	1.0 1/1/02
 * 
 * Copyright (c) 2001-2004, Gaudenz Alder 
 * All rights reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 * - Neither the name of JGraph nor the names of its contributors may be used
 *   to endorse or promote products derived from this software without specific
 *   prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jgraph.graph;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.swing.event.EventListenerList;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelListener;

/**
 * A simple implementation of a graph model.
 *
 * @version 1.0 1/1/02
 * @author Gaudenz Alder
 */

public class DefaultGraphModel
	extends UndoableEditSupport
	implements Serializable, GraphModel {

	/** The list of listeners that listen to the model. */
	protected transient EventListenerList listenerList =
		new EventListenerList();

	/** Default instance of an empty iterator. */
	protected transient Iterator emptyIterator = new EmptyIterator();

	/** Set that contains all root cells of this model. */
	protected List roots = new ArrayList();

	/** Indicates whether isLeaf is based on a node's allowsChildren value. */
	protected boolean asksAllowsChildren = false;

	/**
	 * Constructs a model that is not an attribute store.
	 */
	public DefaultGraphModel() {
	}

	//
	// Graph Model
	//

	/**
	 * Returns the number of roots in the model.  Returns 0 if the
	 * model is empty.
	 *
	 * @return  the number of roots in the model
	 */
	public int getRootCount() {
		return roots.size();
	}

	/**
	 * Returns the root at index <I>index</I> in the model.
	 * This should not return null if <i>index</i> is a valid
	 * index for the model (that is <i>index</i> >= 0 &&
	 * <i>index</i> < getRootCount()).
	 *
	 * @return  the root of at index <I>index</I>
	 */
	public Object getRootAt(int index) {
		return roots.get(index);
	}

	/**
	 * Returns the index of <code>root</code> in the model.
	 * If root is <code>null</code>, returns -1.
	 * @param parent a root in the model, obtained from this data source
	 * @return the index of the root in the model, or -1
	 *    if the parent is <code>null</code>
	 */
	public int getIndexOfRoot(Object root) {
		return roots.indexOf(root);
	}

	/**
	 * Returns <code>true</code> if <code>node</code> or one of its
	 * ancestors is in the model.
	 *
	 * @return <code>true</code> if  <code>node</code> is in the model
	 */
	public boolean contains(Object node) {
		Object parentNode = null;
		while ((parentNode = getParent(node))
			!= null)
			node = parentNode;
		return roots.contains(node);
	}

	/**
	 * Returns a <code>Map</code> that represents the attributes for
	 * the specified cell. This attributes have precedence over each
	 * view's attributes, regardless of isAttributeStore.
	 *
	 * @return attributes of <code>node</code> as a <code>Map</code>
	 */
	public Map getAttributes(Object node) {
		if (node instanceof GraphCell)
			return ((GraphCell) node).getAttributes();
		return null;
	}

	//
	// Graph Structure
	//

	/**
	 * Returns the source of <code>edge</code>. <I>edge</I> must be an object
	 * previously obtained from this data source.
	 *
	 * @return <code>Object</code> that represents the source of <i>edge</i>
	 */
	public Object getSource(Object edge) {
		if (edge instanceof Edge)
			return ((Edge) edge).getSource();
		return null;
	}

	/**
	 * Returns the target of <code>edge</code>. <I>edge</I> must be an object
	 * previously obtained from this data source.
	 *
	 * @return <code>Object</code> that represents the target of <i>edge</i>
	 */
	public Object getTarget(Object edge) {
		if (edge instanceof Edge)
			return ((Edge) edge).getTarget();
		return null;
	}

	/**
	 * Returns <code>true</code> if <code>port</code> is a valid source
	 * for <code>edge</code>. <I>edge</I> and <I>port</I> must be
	 * objects previously obtained from this data source.
	 *
	 * @return <code>true</code> if <code>port</code> is a valid source
	 *                           for <code>edge</code>.
	 */
	public boolean acceptsSource(Object edge, Object port) {
		return true;
	}

	/**
	 * Returns <code>true</code> if <code>port</code> is a valid target
	 * for <code>edge</code>. <I>edge</I> and <I>port</I> must be
	 * objects previously obtained from this data source.
	 *
	 * @return <code>true</code> if <code>port</code> is a valid target
	 *                           for <code>edge</code>.
	 */
	public boolean acceptsTarget(Object edge, Object port) {
		return true;
	}

	/**
	 * Returns an iterator of the edges connected to <code>port</code>.
	 * <I>port</I> must be a object previously obtained from
	 * this data source. This method never returns null.
	 *
	 * @param   port  a port in the graph, obtained from this data source
	 * @return  <code>Iterator</code> that represents the connected edges
	 */
	public Iterator edges(Object port) {
		if (port instanceof Port)
			return ((Port) port).edges();
		return emptyIterator;
	}

	/**
	 * Returns <code>true</code> if <code>edge</code> is a valid edge.
	 *
	 * @return <code>true</code> if <code>edge</code> is a valid edge.
	 */
	public boolean isEdge(Object edge) {
		return edge instanceof Edge;
	}

	/**
	 * Returns <code>true</code> if <code>port</code> is a valid
	 * port, possibly supporting edge connection.
	 *
	 * @return <code>true</code> if <code>port</code> is a valid port.
	 */
	public boolean isPort(Object port) {
		return port instanceof Port;
	}

	//
	// Group Structure
	//

 	/**
	 * Returns a map of (cell, clone)-pairs for all <code>cells</code>
	 * and their children. Special care is taken to replace the anchor
	 * references between ports. (Iterative implementation.)
	 */
	public Map cloneCells(Object[] cells) {
		Map map = new Hashtable();
		// Add Cells to Queue
		ArrayList q = new ArrayList();
		for (int i = 0; i < cells.length; i++)
			q.add(cells[i]);
		// Iterate Queue
		while (!q.isEmpty()) {
			// Remove Front Client From Queue
			Object node = q.remove(0);
			if (node instanceof DefaultGraphCell) {
				// Enqueue Children
				for (int i = 0; i < getChildCount(node); i++)
					q.add(getChild(node, i));
				// Re-Establish Parent Relation for Front Client
				DefaultGraphCell cell = (DefaultGraphCell) node;
				DefaultGraphCell clone = (DefaultGraphCell) cell.clone();
				Object par = getParent(cell);
				if (par != null) {
					DefaultMutableTreeNode p =
						(DefaultMutableTreeNode) map.get(par);
					if (p != null)
						p.add(clone);
				}
				// Store (cell, clone)-pair
				map.put(cell, clone);
			}
		}
		// Replace Anchors
		Iterator it = map.values().iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			// For All Ports in Result Map do...
			if (obj instanceof Port) {
				Object anchor = ((Port) obj).getAnchor();
				// Map Anchor to Cloned Anchor
				if (anchor != null)
					 ((Port) obj).setAnchor((Port) map.get(anchor));
			}
		}
		return map;
	}


	/**
	 * Returns the parent of <I>child</I> in the model.
	 * <I>child</I> must be a node previously obtained from
	 * this data source. This returns null if <i>child</i> is
	 * a root in the model.
	 *
	 * @param   child  a node in the graph, obtained from this data source
	 * @return  the parent of <I>child</I>
	 */
	public Object getParent(Object child) {
		if (child != null && child instanceof TreeNode)
			return ((TreeNode) child).getParent();
		return null;
	}

	/**
	 * Returns the index of child in parent.
	 * If either the parent or child is <code>null</code>, returns -1.
	 * @param parent a note in the tree, obtained from this data source
	 * @param child the node we are interested in
	 * @return the index of the child in the parent, or -1
	 *    if either the parent or the child is <code>null</code>
	 */
	public int getIndexOfChild(Object parent, Object child) {
		if (parent == null || child == null)
			return -1;
		return ((TreeNode) parent).getIndex((TreeNode) child);
	}

	/**
	 * Returns the child of <I>parent</I> at index <I>index</I> in the parent's
	 * child array.  <I>parent</I> must be a node previously obtained from
	 * this data source. This should not return null if <i>index</i>
	 * is a valid index for <i>parent</i> (that is <i>index</i> >= 0 &&
	 * <i>index</i> < getChildCount(<i>parent</i>)).
	 *
	 * @param   parent  a node in the tree, obtained from this data source
	 * @return  the child of <I>parent</I> at index <I>index</I>
	 */
	public Object getChild(Object parent, int index) {
		if (parent instanceof TreeNode)
			return ((TreeNode) parent).getChildAt(index);
		return null;
	}

	/**
	 * Returns the number of children of <I>parent</I>.  Returns 0 if the node
	 * is a leaf or if it has no children.  <I>parent</I> must be a node
	 * previously obtained from this data source.
	 *
	 * @param   parent  a node in the tree, obtained from this data source
	 * @return  the number of children of the node <I>parent</I>
	 */
	public int getChildCount(Object parent) {
		if (parent instanceof TreeNode)
			return ((TreeNode) parent).getChildCount();
		return 0;
	}

	/**
	 * Returns whether the specified node is a leaf node.
	 * The way the test is performed depends on the.
	 *
	 * @param node the node to check
	 * @return true if the node is a leaf node
	 */
	public boolean isLeaf(Object node) {
		if (asksAllowsChildren && node instanceof TreeNode)
			return !((TreeNode) node).getAllowsChildren();
		return ((TreeNode) node).isLeaf();
	}

	//
	// Change Support
	//

	/**
	 * Inserts the <code>roots</code> and connections into the model.
	 * Notifies the model- and undo listeners of the change. The passed-in
	 * edits are executed if they implement the
	 * <code>GraphModelEvent.ExecutableGraphChange</code> interface
	 * in ascending array-order, after execution of the model change.
	 * (Note: The external order is important in a
	 * special case: After insertion on a partial view, ie. one that does not
	 * display all cells of the model, the cell is made visible after
	 * it is inserted into the model. This requires the inserting view
	 * to be able to add the cell to the visible set before it is
	 * inserted into the model.)
	 * Note: The passed-in propertyMap may contains PortViews
	 * which must be turned into Points when stored in the model.
	 */
	public void insert(
		Object[] roots,
		Map attributes,
		ConnectionSet cs,
		ParentMap pm,
		UndoableEdit[] edits) {
		GraphModelEdit edit =
			createInsertEdit(roots, attributes, cs, pm, edits);
		if (edit != null) {
			edit.execute();
			if (edits != null) {
				for (int i = 0; i < edits.length; i++)
					if (edits[i]
						instanceof GraphModelEvent.ExecutableGraphChange)
						((GraphModelEvent.ExecutableGraphChange) edits[i])
							.execute();
			}
			postEdit(edit);
		}
	}

	/**
	 * Removes <code>cells</code> from the model.
	 * Notifies the model- and undo listeners of the change.
	 */
	public void remove(Object[] roots) {
		GraphModelEdit edit = createRemoveEdit(roots);
		if (edit != null) {
			edit.execute();
			postEdit(edit);
		}
	}

	/**
	 * Applies <code>attributes</code> and the connection changes to
	 * the model. The initial <code>edits</code> that triggered the call
	 * are considered to be part of this transaction.  The passed-in
	 * edits are executed if they implement the
	 * <code>GraphModelEvent.ExecutableGraphChange</code> interface
	 * in ascending array-order, after execution of the model change.
	 * Notifies the model- and undo listeners of the change.
	 * <strong>Note:</strong> If only <code>edits</code> is non-null, the
	 * edits are directly passed to the UndoableEditListeners.
	 * Note: The passed-in propertyMap may contains PortViews
	 * which must be turned into Points when stored in the model.
	 */
	public void edit(
		Map attributes,
		ConnectionSet cs,
		ParentMap pm,
		UndoableEdit[] edits) {
		if ((attributes == null || attributes.isEmpty())
			&& (cs == null || cs.isEmpty())
			&& pm == null
			&& edits != null
			&& edits.length == 1) {
			if (edits[0] instanceof GraphModelEvent.ExecutableGraphChange)
				 ((GraphModelEvent.ExecutableGraphChange) edits[0]).execute();
			postEdit(edits[0]); // UndoableEdit Relay
		} else {
			GraphModelEdit edit = createCellEdit(attributes, cs, pm, edits);
			//System.out.println("DefaultGraphModel_edit_attributes="+attributes);
			if (edit != null) {
				edit.execute();
				if (edits != null) {
					for (int i = 0; i < edits.length; i++)
						if (edits[i]
							instanceof GraphModelEvent.ExecutableGraphChange)
							((GraphModelEvent.ExecutableGraphChange) edits[i])
								.execute();
				}
				postEdit(edit);
			}
		}
	}

	/**
	 * Sends <code>cells</code> to back.
	 */
	public void toBack(Object[] cells) {
		GraphModelLayerEdit edit =
			createLayerEdit(cells, GraphModelLayerEdit.BACK);
		if (edit != null) {
			edit.execute();
			postEdit(edit);
		}
	}

	/**
	 * Brings <code>cells</code> to front.
	 */
	public void toFront(Object[] cells) {
		GraphModelLayerEdit edit =
			createLayerEdit(cells, GraphModelLayerEdit.FRONT);
		if (edit != null) {
			edit.execute();
			postEdit(edit);
		}
	}

	protected GraphModelLayerEdit createLayerEdit(Object[] cells, int layer) {
		return new GraphModelLayerEdit(cells, layer);
	}

	//
	// Edit Creation
	//

	/**
	 * Returns an edit that represents an insert.
	 */
	protected GraphModelEdit createInsertEdit(
		Object[] cells,
		Map attributeMap,
		ConnectionSet cs,
		ParentMap pm,
		UndoableEdit[] edits) {
	    GraphModelEdit edit =
		createEdit(cells, null, attributeMap, cs, pm);
	    if (edit != null) {
		if (edits != null)
		    for (int i = 0; i < edits.length; i++)
			edit.addEdit(edits[i]);
		edit.end();
	    }
	    return edit;
	}

	/**
	 * Returns an edit that represents a remove.
	 */
	protected GraphModelEdit createRemoveEdit(Object[] cells) {
		// Remove from GraphStructure
		ConnectionSet cs = ConnectionSet.create(this, cells, true);
		// Remove from Group Structure
		ParentMap pm = ParentMap.create(this, cells, true, false);
		// Construct Edit
		//GraphModelEdit edit = new GraphModelEdit(cells, cs, pm);
		GraphModelEdit edit = createEdit(null, cells, null, cs, pm);
		if (edit != null)
			edit.end();
		return edit;
	}

	/**
	 * Returns an edit that represents a change.
	 */
	protected GraphModelEdit createCellEdit(
		Map attributes,
		ConnectionSet cs,
		ParentMap pm,
		UndoableEdit[] edits) {
		//GraphModelEdit edit = new GraphModelEdit(cs, propertyMap, pm);
		GraphModelEdit edit = createEdit(null, null, attributes, cs, pm);
		if (edit != null) {
			if (edits != null)
				for (int i = 0; i < edits.length; i++)
					edit.addEdit(edits[i]);
			edit.end();
		}
		return edit;
	}

	protected GraphModelEdit createEdit(
		Object[] inserted,
		Object[] removed,
		Map attributes,
		ConnectionSet cs,
		ParentMap pm) {
		return new GraphModelEdit(
			inserted,
			removed,
			attributes,
			cs,
			pm);
	}

	//
	// Change Handling
	//

	/**
	 * Inserts <code>cells</code> into the model. Returns
	 * the cells that were inserted (including descendants).
	 */
	protected Object[] handleInsert(Object[] cells) {
		Object[] inserted = null;
		if (cells != null) {
			for (int i = 0; i < cells.length; i++)
				// Add to Roots if no parent
				if (getParent(cells[i]) == null)
					roots.add(cells[i]);
			// Return *all* inserted cells
			inserted = getDescendants(this, cells).toArray();
		}
		return inserted;
	}

	/**
	 * Removes <code>cells</code> from the model. Returns
	 * the cells that were removed as roots.
	 */
	protected Object[] handleRemove(Object[] cells) {
		List removedRoots = new ArrayList();
		if (cells != null)
			for (int i = 0; i < cells.length; i++)
				if (getParent(cells[i]) == null && roots.remove(cells[i]))
					removedRoots.add(cells[i]);
		return removedRoots.toArray();
	}

	/**
	 * Applies <code>cells</code> to the model. Returns
	 * a parent map that may be used to undo this change.
	 */
	protected ParentMap handleParentMap(ParentMap parentMap) {
		if (parentMap != null) {
			ParentMap undo = new ParentMap();
			Iterator it = parentMap.entries();
			while (it.hasNext()) {
				ParentMap.Entry entry = (ParentMap.Entry) it.next();
				Object child = entry.getChild();
				Object parent = entry.getParent();
				undo.addEntry(child, getParent(child));
				if (parent == null){
					if (child instanceof MutableTreeNode){
						((MutableTreeNode)child).removeFromParent();
					}
				} else {
					if (parent instanceof DefaultMutableTreeNode &&
						child instanceof MutableTreeNode){
						((DefaultMutableTreeNode)parent).add((MutableTreeNode)child);
					}
				}
				
				boolean isRoot = roots.contains(child);
				if (parent == null && !isRoot)
					roots.add(child);
				else if (parent != null && isRoot)
					roots.remove(child);
			}
			return undo;
		}
		return null;
	}

	/**
	 * Applies <code>attributes</code> to the cells specified as keys.
	 * Returns the <code>attributes</code> to undo the change.
	 */
	protected Map handleAttributes(Map attributes) {
		if (attributes != null) {
			Hashtable undo = new Hashtable();
			Iterator it = attributes.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				Object cell = entry.getKey();
				Map deltaNew = (Map) entry.getValue();
				//System.out.println("deltaNew="+deltaNew);
				//System.out.println("stateOld="+getAttributes(cell));
				if (cell instanceof GraphCell){
					Map deltaOld = ((GraphCell)cell).changeAttributes(deltaNew);
					//System.out.println("stateNew="+getAttributes(cell));
					//System.out.println("deltaOld="+deltaOld);
					undo.put(cell, deltaOld);
				} else {
					Map attr = getAttributes(cell);
					if (attr != null){
						Map deltaOld = GraphConstants.applyMap(deltaNew, attr);
						//System.out.println("stateNew="+getAttributes(cell));
						//System.out.println("deltaOld="+deltaOld);
						undo.put(cell, deltaOld);
					} 
				}
			}
			return undo;
		}
		return null;
	}

	//
	// Connection Set Handling
	//

	/**
	 * Applies <code>connectionSet</code> to the model. Returns
	 * a connection set that may be used to undo this change.
	 */
	protected ConnectionSet handleConnectionSet(ConnectionSet cs) {
		if (cs != null) {
			ConnectionSet csundo = new ConnectionSet();
			Iterator it = cs.connections();
			while (it.hasNext()) {
				ConnectionSet.Connection c =
					(ConnectionSet.Connection) it.next();
				Object edge = c.getEdge();
				if (c.isSource())
					csundo.connect(edge, getSource(edge), true);
				else
					csundo.connect(edge, getTarget(edge), false);
				handleConnection(c);
			}
			return csundo;
		}
		return null;
	}

	/**
	 * Inserts the specified connection into the model.
	 */
	protected void handleConnection(ConnectionSet.Connection c) {
		Object edge = c.getEdge();
		Object old = (c.isSource()) ? getSource(edge) : getTarget(edge);
		Object port = c.getPort();
		if (port != old) {
			connect(edge, old, c.isSource(), true);
			if (contains(port) && contains(edge))
				connect(edge, port, c.isSource(), false);
		}
	}

	/**
	 * Connects or disconnects the edge and port in this model
	 * based on <code>remove</code>. Subclassers should override
	 * this to update connectivity datastructures.
	 */
	protected void connect(
		Object edge,
		Object port,
		boolean isSource,
		boolean remove) {
		if (port instanceof Port)
			if (remove)
				 ((Port) port).removeEdge(edge);
			else
				 ((Port) port).addEdge(edge);
		if (remove)
			port = null;
		if (edge instanceof Edge) {
			if (isSource)
				 ((Edge) edge).setSource(port);
			else
				 ((Edge) edge).setTarget(port);
		}
	}

	//
	//  GraphModelListeners
	//

	/**
	 * Adds a listener for the GraphModelEvent posted after the graph changes.
	 *
	 * @see     #removeGraphModelListener
	 * @param   l       the listener to add
	 */
	public void addGraphModelListener(GraphModelListener l) {
		listenerList.add(GraphModelListener.class, l);
	}

	/**
	 * Removes a listener previously added with <B>addGraphModelListener()</B>.
	 *
	 * @see     #addGraphModelListener
	 * @param   l       the listener to remove
	 */
	public void removeGraphModelListener(GraphModelListener l) {
		listenerList.remove(GraphModelListener.class, l);
	}

	/*
	 * Notify all listeners that have registered interest for
	 * notification on this event type.  The event instance
	 * is lazily created using the parameters passed into
	 * the fire method.
	 * @see EventListenerList
	 */
	protected void fireGraphChanged(
		Object source,
		GraphModelEvent.GraphModelChange edit) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		GraphModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == GraphModelListener.class) {
				// Lazily create the event:
				if (e == null)
					e = new GraphModelEvent(source, edit);
				((GraphModelListener) listeners[i + 1]).graphChanged(e);
			}
		}
	}

	/**
	 * Return an array of all GraphModelListeners that were added to this model.
	 */
	public GraphModelListener[] getGraphModelListeners() {
		return (GraphModelListener[]) listenerList.getListeners(
			GraphModelListener.class);
	}

	//
	// GraphModelEdit
	//

	/**
	 * An implementation of GraphModelChange that can be added to the model
	 * event.
	 */
	public class GraphModelEdit
		extends CompoundEdit
		implements
			GraphModelEvent.GraphModelChange,
			GraphModelEvent.ExecutableGraphChange {

		/* Cells that were inserted/removed/changed during the last execution. */
		protected Object[] insert, changed, remove, context;

		/* Cells that were inserted/removed/changed during the last execution. */
		protected Object[] inserted, removed;

		/* Property map for the next execution. Attribute Map is
		   passed to the views on inserts. */
		protected Map attributes, previousAttributes;

		/* Parent map for the next execution. */
		protected ParentMap parentMap, previousParentMap;

		/* ConnectionSet for the next execution. */
		protected ConnectionSet connectionSet, previousConnectionSet;

		/* Piggybacked undo from the views. */
		protected Map cellViews = new Hashtable();

		/**
		 * Constructs an edit record.
		 *
		 * @param e the element
		 * @param index the index into the model >= 0
		 * @param removed a set of elements that were removed
		 * @param inserted a set of roots that were inserted
		 */
		public GraphModelEdit(
			Object[] inserted,
			Object[] removed,
			Map attributes,
			ConnectionSet connectionSet,
			ParentMap parentMap) {
			super();
			this.insert = inserted;
			this.remove = removed;
			this.connectionSet = connectionSet;
			this.attributes = attributes;
			this.parentMap = parentMap;
			previousAttributes = attributes;
			previousConnectionSet = connectionSet;
			previousParentMap = parentMap;
			// Remove Empty Parents
			if (parentMap != null) {
			    // Compute Empty Group
			    Map childCount = new Hashtable();
			    Iterator it = parentMap.entries();
			    while (it.hasNext()) {
				ParentMap.Entry entry = (ParentMap.Entry) it.next();
				Object child = entry.getChild();
				if (!isPort(child)) {
				    Object oldParent = getParent(child);
				    Object newParent = entry.getParent();
				    if (oldParent != newParent) {
					changeChildCount(childCount, oldParent, -1);
					changeChildCount(childCount, newParent, 1);
				    }
				}
			    }
			    handleEmptyGroups(filterParents(childCount, 0));
			}
		}

	        public Object[] filterParents(Map childCount, int children) {
		    ArrayList list = new ArrayList();
		    Iterator it = childCount.entrySet().iterator();
		    while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			if (entry.getValue() instanceof Integer) {
			    if (((Integer) entry.getValue()).intValue() == children)
				list.add(entry.getKey());
			}
		    }
		    return list.toArray();
		}
	        
	        protected void changeChildCount(Map childCount, Object parent, int change) {
		    if (parent != null) {
			Integer count = (Integer) childCount.get(parent);
			if (count == null) {
			    count = new Integer(getChildCount(parent));
			}
			int newValue = count.intValue() + change;
			childCount.put(parent, new Integer(newValue));
		    }
		}

		/**
		 * Adds the groups that become empty to the cells that
		 * will be removed. (Auto remove empty cells.) Removed
		 * cells will be re-inserted on undo, and the parent-
		 * child relations will be restored.
		 */
		protected void handleEmptyGroups(Object[] groups) {
			if (groups != null && groups.length > 0) {
				if (remove == null)
					remove = new Object[] {
				};
				Object[] tmp = new Object[remove.length + groups.length];
				System.arraycopy(remove, 0, tmp, 0, remove.length);
				System.arraycopy(groups, 0, tmp, remove.length, groups.length);
				remove = tmp;
			}
		}

		public boolean isSignificant() {
			return true;
		}

		/**
		 * Returns the source of this change. This can either be a
		 * view or a model, if this change is a GraphModelChange.
		 */
		public Object getSource() {
			return DefaultGraphModel.this;
		}

		/**
		 * Returns the cells that have changed. This includes the cells
		 * that have been changed through a call to getAttributes and the
		 * edges that have been changed with the ConnectionSet.
		 */
		public Object[] getChanged() {
			return changed;
		}

		/**
		 * Returns the objects that have not changed explicitly, but
		 * implicitly because one of their dependent cells has changed.
		 */
		public Object[] getContext() {
			return context;
		}

		/**
		 * Returns the cells that were inserted.
		 */
		public Object[] getInserted() {
			return inserted;
		}

		/**
		 * Returns the cells that were inserted.
		 */
		public Object[] getRemoved() {
			return removed;
		}

		/**
		 * Returns a map that contains (object, map) pairs
		 * of the attributes that have been stored in the model.
		 */
		public Map getPreviousAttributes() {
			return previousAttributes;
		}

		/**
		 * Returns a map of (object, view attributes). The objects
		 * are model objects which need to be mapped to views.
		 */
		public Map getAttributes() {
			return attributes;
		}

		/**
		 * Returns the connectionSet.
		 * @return ConnectionSet
		 */
		public ConnectionSet getConnectionSet() {
			return connectionSet;
		}

		public ConnectionSet getPreviousConnectionSet() {
			return previousConnectionSet;
		}

		/**
		 * Returns the parentMap.
		 * @return ParentMap
		 */
		public ParentMap getParentMap() {
			return parentMap;
		}

		public ParentMap getPreviousParentMap() {
			return previousParentMap;
		}

		/**
		 * Redoes a change.
		 *
		 * @exception CannotRedoException if the change cannot be redone
		 */
		public void redo() throws CannotRedoException {
			super.redo();
			execute();
		}

		/**
		 * Undoes a change.
		 *
		 * @exception CannotUndoException if the change cannot be undone
		 */
		public void undo() throws CannotUndoException {
			super.undo();
			execute();
		}

		/**
		 * Execute this edit such that the next invocation to this
		 * method will invert the last execution.
		 */
		public void execute() {
			// Compute Changed Cells
			Set tmp = new HashSet();
			if (attributes != null)
				tmp.addAll(attributes.keySet());
			if (parentMap != null)
				tmp.addAll(parentMap.getChangedNodes());
			// Note: One must also include the previous parents!
			if (connectionSet != null)
				tmp.addAll(connectionSet.getChangedEdges());
			if (remove != null) {
				for (int i = 0; i < remove.length; i++)
					tmp.remove(remove[i]);
			}
			changed = tmp.toArray();
			// Context cells
			Set ctx = getEdges(DefaultGraphModel.this, changed);
			context = ctx.toArray();
			// Do Execute
			inserted = insert;
			removed = remove;
			remove = handleInsert(inserted);
			previousParentMap = parentMap;
			parentMap = handleParentMap(parentMap);
			// Adds previous parents
			if (parentMap != null)
				tmp.addAll(parentMap.getChangedNodes());
			previousConnectionSet = connectionSet;
			connectionSet = handleConnectionSet(connectionSet);
			insert = handleRemove(removed);
			previousAttributes = attributes;
			attributes = handleAttributes(attributes);
			changed = tmp.toArray();
			// Fire Event	  
			fireGraphChanged(DefaultGraphModel.this, this);
		}

		public void putViews(GraphLayoutCache view, CellView[] views) {
			if (view != null && views != null)
				cellViews.put(view, views);
		}

		public CellView[] getViews(GraphLayoutCache view) {
			return (CellView[]) cellViews.get(view);
		}

		public String toString() {
			String s = new String();
			if (inserted != null) {
				s += "Inserted:\n";
				for (int i = 0; i < inserted.length; i++)
					s += "  " + inserted[i] + "\n";
			} else
				s += "None inserted\n";
			if (removed != null) {
				s += "Removed:\n";
				for (int i = 0; i < removed.length; i++)
					s += "  " + removed[i] + "\n";
			} else
				s += "None removed\n";
			if (changed != null && changed.length > 0) {
				s += "Changed:\n";
				for (int i = 0; i < changed.length; i++)
					s += "  " + changed[i] + "\n";
			} else
				s += "None changed\n";
			if (parentMap != null)
				s += parentMap.toString();
			else
				s += "No parent map\n";
			return s;
		}

	}

	/**
	 * An implementation of GraphViewChange.
	 */
	public class GraphModelLayerEdit
		extends GraphLayoutCache.GraphViewLayerEdit
		implements GraphModelEvent.GraphModelChange {

		// The cell that change are the parents, because they need to
		// reload their childs for reordering!
		protected Object[] parents;

		/**
		 * Constructs a GraphModelEdit. This modifies the order of the cells
		     * in the model.
		 */
		public GraphModelLayerEdit(Object[] cells, int layer) {
			super(DefaultGraphModel.this, cells, layer);
			// Construct Parent Array
			Set par = new HashSet();
			for (int i = 0; i < cells.length; i++) {
				if (cells[i] instanceof TreeNode)
					par.add(((TreeNode) cells[i]).getParent());
			}
			parents = par.toArray();
		}

		/**
		 * Returns the source of this change. This can either be a
		 * view or a model, if this change is a GraphModelChange.
		 */
		public Object getSource() {
			return DefaultGraphModel.this;
		}

		/**
		 * Returns the cells that have changed.
		 */
		public Object[] getChanged() {
			return parents;
		}

		/**
		 * Returns the cells that have changed.
		 */
		public Object[] getInserted() {
			return null;
		}

		/**
		 * Returns the cells that have changed.
		 */
		public Object[] getRemoved() {
			return null;
		}

		/**
		 * Returns null.
		 */
		public Map getPreviousAttributes() {
			return null;
		}
		
		public ConnectionSet getPreviousConnectionSet() {
			return null;
		}

		public ParentMap getPreviousParentMap() {
			return null;
		}

		/**
		 * Allows a <code>GraphLayoutCache</code> to add and execute and
		 * UndoableEdit in this change. This does also work if the
		 * parent edit has already been executed, in which case the
		 * to be added edit will be executed immediately, after
		 * addition.
		 * This is used to handle changes to the view that are 
		 * triggered by certain changes of the model. Such implicit
		 * edits may be associated with the view so that they may be
		 * undone and redone correctly, and are stored in the model's
		 * global history together with the parent event as one unit.
		 */
		public void addImplicitEdit(UndoableEdit edit) {
			// ignore	
		}

		/**
		 * Returns the views that have not changed explicitly, but
		 * implicitly because one of their dependent cells has changed.
		 */
		public CellView[] getViews(GraphLayoutCache view) {
			return null;
		}

		/**
		 * Returns the views that have not changed explicitly, but
		 * implicitly because one of their dependent cells has changed.
		 */
		public void putViews(GraphLayoutCache view, CellView[] cellViews) {
			// ignore
		}

		protected void updateListeners() {
			fireGraphChanged(DefaultGraphModel.this, this);
		}

		/**
		 * Returns the list that exclusively contains <code>view</code>.
		 */
		protected List getParentList(Object cell) {
			List list = null;
			if (cell instanceof DefaultMutableTreeNode) {
				Object parent = ((DefaultMutableTreeNode) cell).getParent();
				if (parent instanceof DefaultGraphCell)
					list = ((DefaultGraphCell) parent).getChildren();
				else
					list = roots;
			}
			return list;
		}

	}

	//
	// Static Methods
	//

	/**
	 * Returns the source vertex of the edge by calling getParent on
	 * getSource on the specified model.
	 */
	public static Object getSourceVertex(GraphModel model, Object edge) {
		if (model != null)
			return model.getParent(model.getSource(edge));
		return null;
	}

	/**
	 * Returns the target vertex of the edge by calling getParent on
	 * getTarget on the specified model.
	 */
	public static Object getTargetVertex(GraphModel model, Object edge) {
		if (model != null)
			return model.getParent(model.getTarget(edge));
		return null;
	}

	/**
	 * Returns the roots of the specified model as an array.
	 * This implementation only uses the GraphModel interface.
	 */
	public static Object[] getRoots(GraphModel model) {
		Object[] cells = null;
		if (model != null) {
			cells = new Object[model.getRootCount()];
			for (int i = 0; i < cells.length; i++)
				cells[i] = model.getRootAt(i);
		}
		return cells;
	}

	/**
	 * Return the set of edges that are connected to the
	 * specified cells. The array is flattened and then
	 * all attached edges that are not part of the cells
	 * array are returned.
	 */
	public static Set getEdges(GraphModel model, Object[] cells) {
		Set result = new HashSet();
		Set allCells = getDescendants(model, cells);
		if (allCells != null) {
			Iterator it = allCells.iterator();
			while (it.hasNext()) {
				Iterator edges = model.edges(it.next());
				while (edges.hasNext())
					result.add(edges.next());
			}
			result.removeAll(allCells);
		}
		return result;
	}

	/**
	 * Flattens the given array of root cells by adding the roots
	 * and their descandants. The resulting set contains all cells,
	 * which means it contains branches <strong>and</strong> leafs.
	 * Note: This is an iterative implementation. No recursion used.
	 * DEPRECATED: Use getDescendantList
	 */
	public static Set getDescendants(GraphModel model, Object[] cells) {
		if (cells != null) {
			Stack stack = new Stack();
			for (int i = 0; i < cells.length; i++)
				stack.add(cells[i]);
			HashSet result = new HashSet();
			while (!stack.isEmpty()) {
				Object tmp = stack.pop();
				for (int i = 0; i < model.getChildCount(tmp); i++)
					stack.add(model.getChild(tmp, i));
				if (tmp != null)
					result.add(tmp);
			}
			return result;
		}
		return null;
	}

	public static List getDescendantList(GraphModel model, Object[] cells) {
		if (cells != null) {
			Stack stack = new Stack();
			for (int i = cells.length-1; i >= 0; i--)
				stack.add(cells[i]);
			LinkedList result = new LinkedList();
			while (!stack.isEmpty()) {
				Object tmp = stack.pop();
				for (int i = model.getChildCount(tmp)-1; i >= 0; i--)
					stack.add(model.getChild(tmp, i));
				if (tmp != null)
					result.add(tmp);
			}
			return result;
		}
		return null;
	}

	// Serialization support
	private void readObject(ObjectInputStream s)
		throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		listenerList = new EventListenerList();
		emptyIterator = new EmptyIterator();
	}

	public static class EmptyIterator implements Iterator, Serializable {

		public boolean hasNext() {
			return false;
		}

		public Object next() {
			return null;
		}

		public void remove() {
			// nop
		}
	}
}
