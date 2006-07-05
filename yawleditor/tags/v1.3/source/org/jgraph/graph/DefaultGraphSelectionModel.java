/*
 * @(#)DefaultGraphSelectionModel.java	1.0 1/1/02
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

import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import javax.swing.event.EventListenerList;
import javax.swing.event.SwingPropertyChangeSupport;

import org.jgraph.JGraph;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;

/**
 * Default implementation of GraphSelectionModel.  Listeners are notified
 *
 * @version 1.0 1/1/02
 * @author Gaudenz Alder
 */
public class DefaultGraphSelectionModel
	implements GraphSelectionModel, Cloneable, Serializable {

	/** Property name for selectionMode. */
	public static final String SELECTION_MODE_PROPERTY = "selectionMode";

	/** Value that represents selected state in cellStates. */
	public static final int SELECTED = -1;

	/** Object value that represents the unselected state in cellStates. */
	public static final Integer UNSELECTED = new Integer(0);

	/** Reference to the parent graph. Used to find parents and childs. */
	protected JGraph graph;

	/** Used to message registered listeners. */
	protected SwingPropertyChangeSupport changeSupport;

	/** Event listener list. */
	protected EventListenerList listenerList = new EventListenerList();

	/**
	 * Mode for the selection, will be either SINGLE_TREE_SELECTION,
	 * CONTIGUOUS_TREE_SELECTION or DISCONTIGUOUS_TREE_SELECTION.
	 */
	protected int selectionMode;

	/** Boolean that indicates if the model allows stepping-into groups. */
	protected boolean childrenSelectable = true;

	/** Maps the cells to their selection state. */
	protected Map cellStates = new Hashtable();

	/** List that contains the selected items. */
	protected List selection = new ArrayList();

	/** Constructs a DefaultGraphSelectionModel for the specified graph. */
	public DefaultGraphSelectionModel(JGraph graph) {
		this.graph = graph;
	}

	/**
	 * Sets the selection mode, which must be one of SINGLE_TREE_SELECTION,
	 */
	public void setSelectionMode(int mode) {
		int oldMode = selectionMode;

		selectionMode = mode;
		if (selectionMode != GraphSelectionModel.MULTIPLE_GRAPH_SELECTION
			&& selectionMode != GraphSelectionModel.SINGLE_GRAPH_SELECTION)
			selectionMode = GraphSelectionModel.MULTIPLE_GRAPH_SELECTION;
		if (oldMode != selectionMode && changeSupport != null)
			changeSupport.firePropertyChange(
				SELECTION_MODE_PROPERTY,
				new Integer(oldMode),
				new Integer(selectionMode));
	}

	/**
	 * Returns the selection mode, one of <code>SINGLE_TREE_SELECTION</code>,
	 * <code>DISCONTIGUOUS_TREE_SELECTION</code> or
	 * <code>CONTIGUOUS_TREE_SELECTION</code>.
	 */
	public int getSelectionMode() {
		return selectionMode;
	}

	/**
	  * Sets if the selection model allows the selection
	  * of children.
	  */
	public void setChildrenSelectable(boolean flag) {
		childrenSelectable = flag;
	}

	/**
	  * Returns true if the selection model allows the selection
	  * of children.
	  */
	public boolean isChildrenSelectable() {
		return childrenSelectable;
	}

	/**
	  * Hook for subclassers for fine-grained control over stepping-into cells.
	  * This implementation returns <code>childrenSelectable</code>.
	  */
	protected boolean isChildrenSelectable(Object cell) {
		return childrenSelectable;
	}

	/**
	  * Sets the selection to path. If this represents a change, then
	  * the TreeSelectionListeners are notified. If <code>path</code> is
	  * null, this has the same effect as invoking <code>clearSelection</code>.
	  *
	  * @param path new path to select
	  */
	public void setSelectionCell(Object cell) {
		if (cell == null)
			setSelectionCells(null);
		else
			setSelectionCells(new Object[] { cell });
	}

	/**
	  * Sets the selection to <code>cells</code>.  If this represents a
	  * change the GraphSelectionListeners are notified.  Potentially
	  * paths will be held by this object; in other words don't change
	  * any of the objects in the array once passed in.
	  * <p>If <code>paths</code> is
	  * null, this has the same effect as invoking <code>clearSelection</code>.
	  * <p>The lead path is set to the last path in <code>pPaths</code>.
	  * <p>If the selection mode is <code>CONTIGUOUS_TREE_SELECTION</code>,
	  * and adding the new paths would make the selection discontiguous,
	  * the selection is reset to the first TreePath in <code>paths</code>.
	  *
	  * @param paths new selection
	  */
	public void setSelectionCells(Object[] cells) {
		if (cells != null) {
			if (selectionMode == GraphSelectionModel.SINGLE_GRAPH_SELECTION
				&& cells.length > 0)
				cells = new Object[] { cells[cells.length - 1] };
			cellStates.clear();
			Vector change = new Vector();
			List newSelection = new ArrayList();
			for (int i = 0; i < cells.length; i++) {
				if (cells[i] != null) {
					change.addElement(
						new CellPlaceHolder(
							cells[i],
							!selection.remove(cells[i])));
					select(newSelection, cells[i]);
				}
			}
			Iterator it = selection.iterator();
			while (it.hasNext())
				change.addElement(new CellPlaceHolder(it.next(), false));
			selection = newSelection;
			if (change.size() > 0) {
				notifyCellChange(change);
			}
		}
	}

	/**
	  * Adds path to the current selection. If path is not currently
	  * in the selection the TreeSelectionListeners are notified. This has
	  * no effect if <code>path</code> is null.
	  *
	  * @param path the new path to add to the current selection
	  */
	public void addSelectionCell(Object cell) {
		if (cell != null)
			addSelectionCells(new Object[] { cell });
	}

	/**
	  * Adds cells to the current selection. If any of the paths in
	  * paths are not currently in the selection the TreeSelectionListeners
	  * are notified. This has
	  * no effect if <code>paths</code> is null.
	  * <p>The lead path is set to the last element in <code>paths</code>.
	  * <p>If the selection mode is <code>CONTIGUOUS_TREE_SELECTION</code>,
	  * and adding the new paths would make the selection discontiguous.
	  * Then two things can result: if the TreePaths in <code>paths</code>
	  * are contiguous, then the selection becomes these TreePaths,
	  * otherwise the TreePaths aren't contiguous and the selection becomes
	  * the first TreePath in <code>paths</code>.
	  *
	  * @param path the new path to add to the current selection
	  */
	public void addSelectionCells(Object[] cells) {
		if (cells != null) {
			if (selectionMode == GraphSelectionModel.SINGLE_GRAPH_SELECTION)
				setSelectionCells(cells);
			else {
				Vector change = new Vector();
				for (int i = 0; i < cells.length; i++) {
					if (cells[i] != null) {
						boolean newness = select(selection, cells[i]);
						if (newness) {
							change.addElement(
								new CellPlaceHolder(cells[i], true));
							Object parent =
								graph.getModel().getParent(cells[i]);
							if (parent != null)
								change.addElement(
									new CellPlaceHolder(parent, false));
						}
					}
				}
				if (change.size() > 0)
					notifyCellChange(change);
			}
		}
	}

	/**
	  * Removes path from the selection. If path is in the selection
	  * The TreeSelectionListeners are notified. This has no effect if
	  * <code>path</code> is null.
	  *
	  * @param path the path to remove from the selection
	  */
	public void removeSelectionCell(Object cell) {
		if (cell != null)
			removeSelectionCells(new Object[] { cell });
	}

	/**
	  * Removes paths from the selection.  If any of the paths in paths
	  * are in the selection the TreeSelectionListeners are notified.
	  * This has no effect if <code>paths</code> is null.
	  *
	  * @param path the path to remove from the selection
	  */
	public void removeSelectionCells(Object[] cells) {
		if (cells != null) {
			Vector change = new Vector();
			for (int i = 0; i < cells.length; i++) {
				if (cells[i] != null) {
					boolean removed = deselect(cells[i]);
					if (removed) {
						change.addElement(new CellPlaceHolder(cells[i], false));
						Object parent = graph.getModel().getParent(cells[i]);
						if (parent != null)
							change.addElement(
								new CellPlaceHolder(parent, false));
					}
				}
			}
			if (change.size() > 0)
				notifyCellChange(change);
		}
	}

	/**
	  * Returns the cells that are currently selectable.
	  */
	public Object[] getSelectables() {
		if (isChildrenSelectable()) {
			List result = new ArrayList();
			// Roots Are Always Selectable
			Stack s = new Stack();
			Object[] cells =
				graph.getGraphLayoutCache().getCells(
					graph.getGraphLayoutCache().getRoots());
			for (int i = 0; i < cells.length; i++)
				s.add(cells[i]);
			GraphModel model = graph.getModel();
			// Children of Selected Cells Are Selectable
			while (!s.isEmpty()) {
				Object cell = s.pop();
				if (!model.isPort(cell))
					result.add(cell);
				if (isChildrenSelectable(cell)
					&& getSelectedChildCount(cell) != 0) {
					for (int i = 0; i < model.getChildCount(cell); i++)
						s.add(model.getChild(cell, i));
				}
			}
			return result.toArray();
		}
		return graph.getRoots();
	}

	/**
	  * Returns the first cell in the selection. This is useful if there
	  * if only one item currently selected.
	  */
	public Object getSelectionCell() {
		if (selection != null && selection.size() > 0)
			return selection.toArray()[0];
		return null;
	}

	/**
	  * Returns the cells in the selection. This will return null (or an
	  * empty array) if nothing is currently selected.
	  */
	public Object[] getSelectionCells() {
		if (selection != null)
			return selection.toArray();
		return null;
	}

	/**
	 * Returns the number of paths that are selected.
	 */
	public int getSelectionCount() {
		return (selection == null) ? 0 : selection.size();
	}

	/**
	  * Returns true if the cell, <code>cell</code>,
	  * is in the current selection.
	  */
	public boolean isCellSelected(Object cell) {
		int count = getSelectedChildCount(cell);
		return (count == SELECTED);
	}

	/**
	  * Returns true if the cell, <code>cell</code>,
	  * has selected children.
	  */
	public boolean isChildrenSelected(Object cell) {
		int count = getSelectedChildCount(cell);
		return (count > 0);
	}

	/**
	  * Returns true if the selection is currently empty.
	  */
	public boolean isSelectionEmpty() {
		return (selection.isEmpty());
	}

	/**
	  * Empties the current selection.  If this represents a change in the
	  * current selection, the selection listeners are notified.
	  */
	public void clearSelection() {
		if (selection != null) {
			Vector change = new Vector();
			Iterator it = cellStates.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				change.addElement(new CellPlaceHolder(entry.getKey(), false));
			}
			selection.clear();
			cellStates.clear();
			if (change.size() > 0)
				notifyCellChange(change);
		}
	}

	//
	// Internal Datastructures
	//

	/**
	  * Returns the number of selected childs for <code>cell</code>.
	  */
	protected int getSelectedChildCount(Object cell) {
		if (cell != null) {
			Integer state = (Integer) cellStates.get(cell);
			if (state == null) {
				state = UNSELECTED;
				cellStates.put(cell, state);
			}
			return state.intValue();
		}
		return 0;
	}

	/**
	  * Sets the number of selected childs for <code>cell</code>
	  * to <code>count</code>.
	  */
	protected void setSelectedChildCount(Object cell, int count) {
		Integer i = new Integer(count);
		cellStates.put(cell, i);
	}

	/**
	  * Selects a single cell and updates all datastructures.
	  * No listeners are notified. Override this method to control
	  * individual cell selection.
	  */
	protected boolean select(List list, Object cell) {
		if (!isCellSelected(cell)
			&& graph.getGraphLayoutCache().isVisible(cell)) {
			GraphModel model = graph.getModel();
			// Deselect and Update All Parents
			Object parent = model.getParent(cell);
			while (parent != null) {
				int count = getSelectedChildCount(parent);
				// Deselect Selected Parents
				if (count == SELECTED)
					count = 0;
				// Increase Child Count
				count++;
				setSelectedChildCount(parent, count);
				// Remove From Selection
				selection.remove(parent);
				// Next Parent
				parent = model.getParent(parent);
			}
			// Deselect All Children
			Object[] tmp = new Object[] { cell };
			Set childs = DefaultGraphModel.getDescendants(model, tmp);
			// Remove Current Cell From Flat-View
			childs.remove(cell);
			Iterator it = childs.iterator();
			while (it.hasNext()) {
				Object child = it.next();
				if (child != null && !model.isPort(child)) {
					// Remove Child From Selection
					selection.remove(child);
					// Remove Child State
					cellStates.remove(child);
				}
			}
			// Set Selected State for Current
			setSelectedChildCount(cell, SELECTED);
			// Add Current To HashSet and Return
			return list.add(cell);
		}
		return false;
	}

	/**
	  * Deselects a single cell and updates all datastructures.
	  * No listeners are notified.
	  */
	protected boolean deselect(Object cell) {
		if (isCellSelected(cell)) {
			// Update All Parents
			Object parent = graph.getModel().getParent(cell);
			boolean firstParent = true;
			int change = -1;
			while (parent != null && change != 0) {
				int count = getSelectedChildCount(parent);
				count += change;
				// Select First Parent If No More Children
				if (count == 0 && firstParent) {
					change = 0;
					count = SELECTED;
					selection.add(parent);
				}
				// Update Selection Count
				setSelectedChildCount(parent, count);
				// Next Parent
				parent = graph.getModel().getParent(parent);
				firstParent = false;
			}
			// Remove State of Current Cell
			cellStates.remove(cell);
			// Remove Current from Selection and Return
			return selection.remove(cell);
		}
		return false;
	}

	//
	// Listeners
	//

	/**
	  * Adds x to the list of listeners that are notified each time the
	  * set of selected TreePaths changes.
	  *
	  * @param x the new listener to be added
	  */
	public void addGraphSelectionListener(GraphSelectionListener x) {
		listenerList.add(GraphSelectionListener.class, x);
	}

	/**
	  * Removes x from the list of listeners that are notified each time
	  * the set of selected TreePaths changes.
	  *
	  * @param x the listener to remove
	  */
	public void removeGraphSelectionListener(GraphSelectionListener x) {
		listenerList.remove(GraphSelectionListener.class, x);
	}

	/**
	 * Notifies all listeners that are registered for
	 * tree selection events on this object.
	 * @see #addGraphSelectionListener
	 * @see EventListenerList
	 */
	protected void fireValueChanged(GraphSelectionEvent e) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// TreeSelectionEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == GraphSelectionListener.class) {
				// Lazily create the event:
				// if (e == null)
				// e = new ListSelectionEvent(this, firstIndex, lastIndex);
				 ((GraphSelectionListener) listeners[i + 1]).valueChanged(e);
			}
		}
	}

	/**
	 * Returns an array of all the listeners of the given type that
	 * were added to this model.
	 *
	 * @return all of the objects receiving <em>listenerType</em> notifications
	 *          from this model
	 *
	 * @since 1.3
	 */
	public EventListener[] getListeners(Class listenerType) {
		return listenerList.getListeners(listenerType);
	}

	/**
	 * Adds a PropertyChangeListener to the listener list.
	 * The listener is registered for all properties.
	 * <p>
	 * A PropertyChangeEvent will get fired when the selection mode
	 * changes.
	 *
	 * @param listener  the PropertyChangeListener to be added
	 */
	public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
		if (changeSupport == null) {
			changeSupport = new SwingPropertyChangeSupport(this);
		}
		changeSupport.addPropertyChangeListener(listener);
	}

	/**
	 * Removes a PropertyChangeListener from the listener list.
	 * This removes a PropertyChangeListener that was registered
	 * for all properties.
	 *
	 * @param listener  the PropertyChangeListener to be removed
	 */

	public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
		if (changeSupport == null) {
			return;
		}
		changeSupport.removePropertyChangeListener(listener);
	}

	/**
	  * Notifies listeners of a change in path. changePaths should contain
	  * instances of PathPlaceHolder.
	  */
	protected void notifyCellChange(Vector changedCells) {
		int cCellCount = changedCells.size();
		boolean[] newness = new boolean[cCellCount];
		Object[] cells = new Object[cCellCount];
		CellPlaceHolder placeholder;

		for (int counter = 0; counter < cCellCount; counter++) {
			placeholder = (CellPlaceHolder) changedCells.elementAt(counter);
			newness[counter] = placeholder.isNew;
			cells[counter] = placeholder.cell;
		}

		GraphSelectionEvent event =
			new GraphSelectionEvent(this, cells, newness);

		fireValueChanged(event);
	}

	/**
	 * Returns a clone of this object with the same selection.
	 * This method does not duplicate
	 * selection listeners and property listeners.
	 *
	 * @exception CloneNotSupportedException never thrown by instances of
	 *                                       this class
	 */
	public Object clone() throws CloneNotSupportedException {
		DefaultGraphSelectionModel clone =
			(DefaultGraphSelectionModel) super.clone();
		clone.changeSupport = null;
		if (selection != null)
			clone.selection = new ArrayList(selection);
		clone.listenerList = new EventListenerList();
		return clone;
	}

	/**
	 * Holds a path and whether or not it is new.
	 */
	protected class CellPlaceHolder {
		protected boolean isNew;
		protected Object cell;

		protected CellPlaceHolder(Object cell, boolean isNew) {
			this.cell = cell;
			this.isNew = isNew;
		}

		/**
		 * Returns the cell.
		 * @return Object
		 */
		public Object getCell() {
			return cell;
		}

		/**
		 * Returns the isNew.
		 * @return boolean
		 */
		public boolean isNew() {
			return isNew;
		}

		/**
		 * Sets the cell.
		 * @param cell The cell to set
		 */
		public void setCell(Object cell) {
			this.cell = cell;
		}

		/**
		 * Sets the isNew.
		 * @param isNew The isNew to set
		 */
		public void setNew(boolean isNew) {
			this.isNew = isNew;
		}

	}

}
