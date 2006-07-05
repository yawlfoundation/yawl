/*
 * @(#)GraphLayoutCache.java	1.0 1/1/02
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

import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.Stack;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import org.jgraph.event.GraphModelEvent;

/**
 * An object that defines the view of a graphmodel. This object
 * maps between model cells and views and provides a set of methods
 * to change these views.
 * The view may also contain its own set of attributes and is therefore
 * an extension of an Observable, which may be observed by the GraphUI.
 * It uses the model to send its changes to the command history.
 *
 * @version 1.0 1/1/02
 * @author Gaudenz Alder
 */

public class GraphLayoutCache
	extends Observable
	implements CellMapper, Serializable {

	/* Boolean indicating whether new or changed edges should be made visible if
	 * their source and target vertices are visible.
	 * This setting has no effect in non-partial views.
	 */
	public boolean showAllEdgesForVisibleVertices = true;

	/* Boolean indicating whether edges should be made visible if
	 * their connected vertices become visible. This does cause new edges to
	 * be displayed automatically.
	 * This setting has no effect in non-partial views.
	 */
	public boolean showEdgesOnShow = true;

	/* Boolean indicating whether attached edges should be made invisible
	 * if their source or target port is hidden.
	 * This setting has no effect in non-partial views.
	 */
	public boolean hideEdgesOnHide = true;

	/* Boolean indicating whether edges should be made invisible if
	 * their connected vertices become invisible (for example when removed).
	 * This setting has no effect in non-partial views.
	 */
	public boolean hideEdgesOnBecomeInvisible = true;

	/* Boolean indicating whether cellviews should be remembered once visible
	 * in this GraphLayoutCache.
	 */
	public boolean rememberCellViews = true;

	/* Reference to the graphModel */
	protected GraphModel graphModel;

	/* Maps cells to views. */
	protected Map mapping = new Hashtable();

	/* Reference to the cell mapper, typically this. */
	protected CellMapper mapper;

	/* Factory to create the views. */
	protected CellViewFactory factory = null;

	/* The set of visible cells. */
	protected Set visibleSet = new HashSet();

	/* Ordered list of roots for the view. */
	protected List roots = new ArrayList();

	/* Cached array of all ports for the view. */
	protected PortView[] ports;

	/**
	 * Remebered cell views.
	 */
	protected transient Map hiddenSet = new Hashtable();

	/* View overrides model layering. */
	protected boolean ordered = false;

	/* Only portions of the model are visible. */
	protected boolean partial = false;

	/**
	 * Constructs a view for the specified model that uses
	 * <code>factory</code> to create its views.
	 *
	 * @param model the model that constitues the data source
	 */
	public GraphLayoutCache(GraphModel model, CellViewFactory factory) {
		this(model, factory, false, false);
	}

	/**
	 * Constructs a view for the specified model that uses
	 * <code>factory</code> to create its views.
	 *
	 * @param model the model that constitues the data source
	 */
	public GraphLayoutCache(
		GraphModel model,
		CellViewFactory factory,
		boolean ordered,
		boolean partial) {
		this(model, factory, ordered, partial, true, true, true, true, true);
	}

	/**
	 * Constructs a view for the specified model that uses
	 * <code>factory</code> to create its views.
	 *
	 * @param model the model that constitues the data source
	 */
	public GraphLayoutCache(
		GraphModel model,
		CellViewFactory factory,
		boolean ordered,
		boolean partial,
		boolean rememberCellViews,
		boolean showAllEdgesForVisibleVertices,
		boolean showEdgesOnShow,
		boolean hideEdgesOnHide,
		boolean hideEdgesOnBecomeInvisible) {
		this.factory = factory;
		this.ordered = ordered;
		this.partial = partial;
		this.rememberCellViews = rememberCellViews;
		this.showAllEdgesForVisibleVertices = showAllEdgesForVisibleVertices;
		this.showEdgesOnShow = showEdgesOnShow;
		this.hideEdgesOnHide = hideEdgesOnHide;
		this.hideEdgesOnBecomeInvisible = hideEdgesOnBecomeInvisible;
		setModel(model);
	}

	//
	// Accessors
	//

	/**
	 * Sets the factory that creates the cell views.
	 */
	public void setFactory(CellViewFactory factory) {
		this.factory = factory;
	}

	/**
	 * Returns the factory that was passed to the constructor.
	 */
	public CellViewFactory getFactory() {
		return factory;
	}

	/**
	 * Sets the current model.
	 */
	public void setModel(GraphModel model) {
		roots.clear();
		mapping.clear();
		hiddenSet.clear();
		visibleSet.clear();
		graphModel = model;
		Object[] cells = DefaultGraphModel.getRoots(model);
		if (!isPartial())
			insertRoots(getMapping(cells, true));
		// AutoSize for Existing Cells
		if (cells != null)
			for (int i = 0; i < cells.length; i++)
				factory.updateAutoSize(getMapping(cells[i], false));
		// Update PortView Cache
		updatePorts();
	}

	// Remaps all existing views using the CellViewFactory
	// and replaces the respective root views.
	public synchronized void reload() {
		List newRoots = new ArrayList();
		Map oldMapping = new Hashtable(mapping);
		mapping.clear();

		Iterator it = oldMapping.keySet().iterator();
		while (it.hasNext()) {
			Object cell = it.next();
			CellView oldView = (CellView) oldMapping.get(cell);
			CellView newView = getMapping(cell, true);
			newView.setAttributes(oldView.getAttributes());
			if (roots.contains(oldView))
				newRoots.add(newView);
		}
		// replace hidden
		hiddenSet.clear();
		roots = newRoots;
	}

	/**
	 * Returns the current model.
	 */
	public GraphModel getModel() {
		return graphModel;
	}

	/**
	 * Returns the roots of the view.
	 */
	public CellView[] getRoots() {
		CellView[] views = new CellView[roots.size()];
		roots.toArray(views);
		return views;
	}

	/**
	 * Return all cells that intersect the given rectangle.
	 */
	public CellView[] getRoots(Rectangle clip) {
		java.util.List result = new ArrayList();
		CellView[] views = getRoots();
		for (int i = 0; i < views.length; i++)
			if (views[i].getBounds().intersects(clip))
				result.add(views[i]);
		views = new CellView[result.size()];
		result.toArray(views);
		return views;
	}

	/**
	 * Returns the ports of the view.
	 */
	public PortView[] getPorts() {
		return ports;
	}

	/**
	 * Updates the cached array of ports.
	 */
	protected void updatePorts() {
		Object[] roots = DefaultGraphModel.getRoots(graphModel);
		Set set = DefaultGraphModel.getDescendants(graphModel, roots);
		if (set != null) {
			Object[] all = set.toArray(); //order(set.toArray());
			ArrayList result = new ArrayList();
			for (int i = 0; i < all.length; i++) {
				if (graphModel.isPort(all[i])) {
					CellView portView = getMapping(all[i], false);
					if (portView != null) {
						result.add(portView);
						portView.refresh(false);
					}
				}
			}
			ports = new PortView[result.size()];
			result.toArray(ports);
		}
	}

	public void refresh(CellView[] views, boolean create) {
		if (views != null)
			for (int i = 0; i < views.length; i++)
				refresh(views[i], create);
	}

	public void refresh(CellView view, boolean create) {
		if (view != null) {
			view.refresh(create);
			CellView[] children = view.getChildViews();
			for (int i = 0; i < children.length; i++)
				refresh(children[i], create);
		}
	}

	public void update(CellView[] views) {
		if (views != null)
			for (int i = 0; i < views.length; i++)
				update(views[i]);
	}

	public void update(CellView view) {
		if (view != null) {
			view.update();
			CellView[] children = view.getChildViews();
			for (int i = 0; i < children.length; i++)
				update(children[i]);
		}
	}

	//
	// Update View based on Model Change
	//

	/**
	 * Called from BasicGraphUI.ModelHandler to update the view
	 * based on the specified GraphModelEvent.
	 */
	public void graphChanged(GraphModelEvent.GraphModelChange change) {
		// Get Old Attributes From GraphModelChange (Undo) -- used to remap removed cells
		CellView[] views = change.getViews(this);
		if (views != null) {
			// Only ex-visible views are piggybacked
			for (int i = 0; i < views.length; i++)
				if (views[i] != null) {
					// Do not use putMapping because cells are invisible
					mapping.put(views[i].getCell(), views[i]);
				}
			// Ensure visible state
			setVisibleImpl(getCells(views), true);
		}
		// Fetch View Order Of Changed Cells (Before any changes)
		Object[] changed = order(change.getChanged()); // change.getChanged(); ?
		// Fetch Views to Insert before Removal (Special case: two step process, see setModel)
		CellView[] insertViews = getMapping(change.getInserted(), true);
		// Remove and Hide Roots
		views = removeRoots(change.getRemoved());
		// Store Removed Attributes In GraphModelChange (Undo)
		change.putViews(this, views);
		// Insert New Roots
		insertRoots(insertViews);
		// Hide edges with invisible source or target
		if (isPartial()) {
			// Then show
			showCellsForChange(change);
			// First hide
			hideCellsForChange(change);
		}
		// Refresh Changed Cells
		if (changed != null && changed.length > 0) {
			// Restore All Cells in Model Order (Replace Roots)
			if (!isOrdered()) {
				roots.clear();
				Object[] rootCells = DefaultGraphModel.getRoots(graphModel);
				CellView[] rootViews = getMapping(rootCells, false);
				for (int i = 0; i < rootViews.length; i++) {
					if (rootViews[i] != null) {
						// && isVisible(rootViews[i].getCell())) {
						roots.add(rootViews[i]);
						rootViews[i].refresh(true);
						factory.updateAutoSize(rootViews[i]);
					}
				}
			}
			for (int i = 0; i < changed.length; i++) {
				CellView view = getMapping(changed[i], false);
				if (view != null) { // && isVisible(view.getCell())) {
					view.refresh(true);
					// Update child edges in groups (routing)
					update(view);
					factory.updateAutoSize(view);
					if (isOrdered()) {
						CellView parentView = view.getParentView();
						Object par =
							(parentView != null) ? parentView.getCell() : null;
						boolean isRoot = roots.contains(view);
						// Adopt Orphans
						if (par == null && !isRoot)
							roots.add(view);
						// Root Lost
						else if (par != null && isRoot)
							roots.remove(view);
					}
				}
			}
		}
		// Refresh inserted cells
		Object[] inserted = change.getInserted();
		if (inserted != null && inserted.length > 0) {
			for (int i = 0; i < inserted.length; i++)
				factory.updateAutoSize(getMapping(inserted[i], false));
		}
		// Refresh Context of Changed Cells (=Connected Edges)
		refresh(getMapping(change.getContext(), false), false);
		// Update Cached Ports If Necessary
		Object[] removed = change.getRemoved();
		if ((removed != null && removed.length > 0)
			|| (inserted != null && inserted.length > 0)
			|| !isOrdered())
			updatePorts();
	}

	protected void hideCellsForChange(
		GraphModelEvent.GraphModelChange change) {
		// Hide visible edges between invisible vertices
		// 1. Remove attached edges of removed cells
		// 2. Remove edges who's source or target has changed to
		//    invisible.
		Object[] tmp = change.getRemoved();
		Set removed = new HashSet();
		if (tmp != null)
			for (int i = 0; i < tmp.length; i++)
				removed.add(tmp[i]);
		if (hideEdgesOnBecomeInvisible) {
			Object[] changed = change.getChanged();
			for (int i = 0; i < changed.length; i++) {
				CellView view = getMapping(changed[i], false);
				if (view instanceof EdgeView) {
					EdgeView edge = (EdgeView) view;
					Object oldSource =
						(edge.getSource() == null)
							? null
							: edge.getSource().getCell();
					Object oldTarget =
						(edge.getTarget() == null)
							? null
							: edge.getTarget().getCell();
					Object newSource = graphModel.getSource(changed[i]);
					Object newTarget = graphModel.getTarget(changed[i]);
					if ((removed.contains(oldSource)
						|| removed.contains(oldTarget)) /*1*/
						|| ((newSource != null && !isVisible(newSource))
							|| (newTarget != null
								&& !isVisible(newTarget)))) /*2*/ {
						//System.out.println("hiding: " + edge);
						setVisibleImpl(new Object[] { changed[i] }, false);
					}
				}
			}
		}
	}

	protected void showCellsForChange(
		GraphModelEvent.GraphModelChange change) {
		// Show new edges between visible vertices
		if (showAllEdgesForVisibleVertices) {
			Object[] inserted = change.getInserted();
			if (inserted != null) {
				for (int i = 0; i < inserted.length; i++) {
					if (!isVisible(inserted[i])) {
						if (showAllEdgesForVisibleVertices) {
							Object source = graphModel.getSource(inserted[i]);
							Object target = graphModel.getTarget(inserted[i]);
							if ((source != null || target != null)
								&& (isVisible(source) && isVisible(target)))
								setVisible(inserted[i], true);
						}
					}
				}
			}
			Object[] changed = change.getChanged();
			if (changed != null) {
				for (int i = 0; i < changed.length; i++) {
					if (!isVisible(changed[i])) {
						Object source = graphModel.getSource(changed[i]);
						Object target = graphModel.getTarget(changed[i]);
						if ((source != null || target != null)
							&& (isVisible(source) && isVisible(target)))
							setVisible(changed[i], true);
					}
				}
			}
		}
	}

	/**
	 * Adds the specified model root cells to the view.
	 */
	public void insertRoots(CellView[] views) {
		if (views != null) {
			refresh(views, true);
			for (int i = 0; i < views.length; i++) {
				if (views[i] != null
					&& getMapping(views[i].getCell(), false) != null) {
					CellView parentView = views[i].getParentView();
					Object parent =
						(parentView != null) ? parentView.getCell() : null;
					if (!(views[i] instanceof PortView)
						&& !roots.contains(views[i])
						&& parent == null) {
						roots.add(views[i]);
						// Remove children for non-partial views?
					}
				}
			}
		}
	}

	/**
	 * Removes the specified model root cells from the view by removing
	 * the mapping between the cell and its view and makes the cells
	 * invisible.
	 */
	public CellView[] removeRoots(Object[] cells) {
		if (cells != null) {
			CellView[] views = new CellView[cells.length];
			for (int i = 0; i < cells.length; i++) {
				views[i] = removeMapping(cells[i]);
				if (views[i] != null) {
					views[i].removeFromParent();
					roots.remove(views[i]);
				}
			}
			setVisibleImpl(cells, false);
			return views;
		}
		return null;
	}

	//
	// Cell Mapping
	//

	/**
	 * Takes an array of views and returns the array of the corresponding
	 * cells by using <code>getCell</code> for each view.
	 */
	public Object[] getCells(CellView[] views) {
		if (views != null) {
			Object[] cells = new Object[views.length];
			for (int i = 0; i < views.length; i++)
				if (views[i] != null)
					cells[i] = views[i].getCell();
			return cells;
		}
		return null;
	}

	/**
	 * Returns the view for the specified cell. If create is true
	 * and no view is found then a view is created using
	 * createView(Object).
	 */
	public CellView getMapping(Object cell, boolean create) {
		if (cell == null)
			return null;
		CellView view = (CellView) mapping.get(cell);
		if (view == null && create) {
			view = (CellView) hiddenSet.get(cell);
			if (view != null && isVisible(cell)) {
				putMapping(cell, view);
				hiddenSet.remove(cell);
			} else
				view = factory.createView(cell, this);
		}
		return view;
	}

	/**
	 * Returns the views for the specified array of cells without
	 * creating these views on the fly.
	 */
	public CellView[] getMapping(Object[] cells) {
		return getMapping(cells, false);
	}

	/**
	 * Returns the views for the specified array of cells. Returned
	 * array may contain null pointers if the respective cell is not
	 * mapped in this view and <code>create</code> is <code>false</code>.
	 */
	public CellView[] getMapping(Object[] cells, boolean create) {
		if (cells != null) {
			CellView[] result = new CellView[cells.length];
			for (int i = 0; i < cells.length; i++)
				result[i] = getMapping(cells[i], create);
			return result;
		}
		return null;
	}

	/**
	 * Associates the specified model cell with the specified view.
	 * Updates the portlist if necessary.
	 */
	public void putMapping(Object cell, CellView view) {
		// Remove isVisible-condition?
		if (cell != null && view != null && isVisible(cell))
			mapping.put(cell, view);
	}

	/**
	 * Removes the associaten for the specified model cell and
	 * returns the view that was previously associated with the cell.
	 * Updates the portlist if necessary.
	 */
	public CellView removeMapping(Object cell) {
		if (cell != null) {
			CellView view = (CellView) mapping.remove(cell);
			return view;
		}
		return null;
	}

	//
	// Partial View
	//

	// Null is always visible!
	public boolean isVisible(Object cell) {
		return !isPartial() || visibleSet.contains(cell) || cell == null;
	}

	public Set getVisibleSet() {
		return new HashSet(visibleSet);
	}

	public void setVisibleSet(Set visible) {
		visibleSet = visible;
	}

	public void setVisible(Object cell, boolean visible) {
		setVisible(new Object[] { cell }, visible);
	}

	public void setVisible(Object[] cells, boolean visible) {
		if (visible)
			setVisible(cells, null);
		else
			setVisible(null, cells);
	}

	public void setVisible(Object[] visible, Object[] invisible) {
		visible = addVisibleDependencies(visible, true);
		invisible = addVisibleDependencies(invisible, false);
		GraphViewEdit edit = new GraphViewEdit(null, visible, invisible);
		edit.end();
		graphModel.edit(null, null, null, new UndoableEdit[] { edit });
	}

	// This is used to augment the array passed to the setVisible method.
	public Object[] addVisibleDependencies(Object[] cells, boolean visible) {
		if (cells != null) {
			if (visible) {
				// Make Direct Child Ports and source and target vertex visible
				Set all = new HashSet();
				for (int i = 0; i < cells.length; i++) {
					all.add(cells[i]);
					// Add ports
					all.addAll(getPorts(cells[i]));
					// Add source vertex and ports
					all.addAll(getParentPorts(graphModel.getSource(cells[i])));
					// Add target vertex and ports
					all.addAll(getParentPorts(graphModel.getTarget(cells[i])));
				}
				// Show attached edge
				if (showEdgesOnShow) {
					Set tmp = DefaultGraphModel.getEdges(getModel(), cells);
					Iterator it = tmp.iterator();
					while (it.hasNext()) {
						Object obj = it.next();
						Object source = graphModel.getSource(obj);
						Object target = graphModel.getTarget(obj);
						if ((isVisible(source) || all.contains(source))
							&& (isVisible(target) || all.contains(target)))
							all.add(obj);
					}
				}
				all.removeAll(visibleSet);
				return all.toArray();
			} else {
				// Hide attached edges
				if (hideEdgesOnHide) {
					Set all = new HashSet();
					for (int i = 0; i < cells.length; i++) {
						all.addAll(getPorts(cells[i]));
						all.add(cells[i]);
					}
					all.addAll(DefaultGraphModel.getEdges(graphModel, cells));
					all.retainAll(visibleSet);
					return all.toArray();
				}
			}
		}
		return null;
	}

	// You must call update ports if this method returns true.
	public boolean setVisibleImpl(Object[] cells, boolean visible) {
		if (cells != null && isPartial()) {
			boolean updatePorts = false;
			// Update Visible Set
			for (int i = 0; i < cells.length; i++) {
				if (cells[i] != null) {
					if (visible)
						visibleSet.add(cells[i]);
					else
						visibleSet.remove(cells[i]);
				}
			}
			// Insert Root Views (if not already in place)
			for (int i = 0; i < cells.length; i++) {
				if (cells[i] != null) {
					if (!visible) {
						CellView view = getMapping(cells[i], false);
						if (view != null) {
							view.removeFromParent();
							view.refresh(false);
							removeMapping(cells[i]);
							roots.remove(view);
							if (graphModel.contains(cells[i])
								&& rememberCellViews)
								hiddenSet.put(view.getCell(), view);
							updatePorts = true;
						}
					} else { // don't check if in model, see DefaultGraphModel.insert
						CellView view = getMapping(cells[i], true);
						// Remove all children from roots
						CellView[] children =
							AbstractCellView.getDescendantViews(
								new CellView[] { view });
						for (int j = 0; j < children.length; j++)
							roots.remove(children[j]);
						// Refresh View
						view.refresh(false);
						// Link cellView into graphLayoutCache
						factory.updateAutoSize(view);
						CellView parentView = view.getParentView();
						if (parentView != null)
							parentView.refresh(true);
						else
							insertRoots(new CellView[] { view });
						updatePorts = true;
					}
				}
			}
			return updatePorts;
		}
		return false;
	}

	protected Collection getParentPorts(Object cell) {
		Object parent = graphModel.getParent(cell);
		Collection collection = getPorts(parent);
		collection.add(parent);
		return collection;
	}

	protected Collection getPorts(Object cell) {
		LinkedList list = new LinkedList();
		for (int i = 0; i < graphModel.getChildCount(cell); i++) {
			Object child = graphModel.getChild(cell, i);
			if (graphModel.isPort(child))
				list.add(child);
		}
		return list;
	}

	//
	// Change Support
	//

	public boolean isOrdered() {
		return ordered;
	}

	public boolean isPartial() {
		return partial;
	}

	/**
	 * Inserts the <code>cells</code> and connections into the model,
	 * and absorbs the local attributes. This implementation sets the
	 * inserted cells visible. (Note: No undo required for this visibility
	 * change.)
	 */
	public void insert(
		Object[] roots,
		Map attributes,
		ConnectionSet cs,
		ParentMap pm,
		UndoableEdit[] e) {
		// setVisibleImpl(all, true);
		Object[] visible = null;
		if (isPartial()) {
			Set tmp =
				new HashSet(
					DefaultGraphModel.getDescendants(graphModel, roots));
			tmp.removeAll(visibleSet);
			if (!tmp.isEmpty())
				visible = tmp.toArray();
		}
		GraphViewEdit edit = createLocalEdit(attributes, visible, null);
		// enable select on insert
		// see DefaultGraphModel.insert
		setVisibleImpl(visible, true);
		if (edit != null)
			e = augment(e, edit);
		// Absorb local attributes
		graphModel.insert(roots, attributes, cs, pm, e);
	}

	/**
	 * Removes <code>cells</code> from the model. If <code>removeChildren</code>
	 * is <code>true</code>, the children are also removed.
	 * Notifies the model- and undo listeners of the change.
	 */
	public void remove(Object[] roots) {
		graphModel.remove(roots);
	}

	/**
	 * Applies the <code>propertyMap</code> and the connection changes to
	 * the model. The initial <code>edits</code> that triggered the call
	 * are considered to be part of this transaction.
	 * Notifies the model- and undo listeners of the change.
	 * Note: The passed in attributes may contain PortViews.
	 */
	public void edit(
		Map attributes,
		ConnectionSet cs,
		ParentMap pm,
		UndoableEdit[] e) {
		//System.out.println("GraphLayoutCache_edit_attributes="+attributes);
		Object[] visible = null;
		if (isPartial()) {
			Set tmp = new HashSet(attributes.keySet());
			tmp.removeAll(visibleSet);
			if (!tmp.isEmpty())
				visible = tmp.toArray();
		}
		GraphViewEdit edit = createLocalEdit(attributes, visible, null);
		if (edit != null)
			e = augment(e, edit);
		// Pass to model
		graphModel.edit(attributes, cs, pm, e);
	}

	protected UndoableEdit[] augment(UndoableEdit[] e, UndoableEdit edit) {
		if (edit != null) {
			int size = (e != null) ? e.length + 1 : 1;
			UndoableEdit[] result = new UndoableEdit[size];
			if (e != null)
				System.arraycopy(e, 0, result, 0, size - 2);
			result[size - 1] = edit;
			return result;
		}
		return e;
	}

	/**
	 * Sends <code>cells</code> to back. Note: This expects an array of cells!
	 */
	public void toBack(Object[] cells) {
		if (cells != null && cells.length > 0) {
			if (isOrdered()) {
				CellView[] views = getMapping(cells, false);
				GraphViewLayerEdit edit =
					new GraphViewLayerEdit(
						this,
						views,
						GraphViewLayerEdit.BACK);
				graphModel.edit(null, null, null, new UndoableEdit[] { edit });
			} else
				graphModel.toBack(cells);
		}
	}

	/**
	 * Brings <code>cells</code> to front. Note: This expects an array of cells!
	 */
	public void toFront(Object[] cells) {
		if (cells != null && cells.length > 0) {
			if (isOrdered()) {
				CellView[] views = getMapping(cells, false);
				GraphViewLayerEdit edit =
					new GraphViewLayerEdit(
						this,
						views,
						GraphViewLayerEdit.FRONT);
				graphModel.edit(null, null, null, new UndoableEdit[] { edit });
			} else
				graphModel.toFront(cells);
		}
	}

	protected GraphViewEdit createLocalEdit(
		Map nested,
		Object[] visible,
		Object[] invisible) {
		if (visible != null || invisible != null) {
			GraphViewEdit edit = new GraphViewEdit(null, visible, invisible);
			edit.end();
			return edit;
		}
		return null;
	}

	//
	// Cell Order
	//

	/**
	 * Returns the specified cells in view-order if the model is not
	 * ordered.
	 */
	public Object[] order(Object[] cells) {
		if (cells != null) {
			if (graphModel != null && isOrdered()) {
				// Create Set for Lookup
				Set cellSet = new HashSet();
				for (int i = 0; i < cells.length; i++)
					cellSet.add(cells[i]);
				// Get Roots in View Order
				CellView[] views = getRoots();
				// Add Roots to Stack
				Stack s = new Stack();
				for (int i = 0; i < views.length; i++)
					s.add(views[i]);
				List result = new ArrayList();
				// Traverse All Children In View Order
				while (!s.isEmpty()) {
					CellView cell = (CellView) s.pop();
					// Add To List if Selected or Port
					if (cellSet.contains(cell.getCell()))
						result.add(cell.getCell());
					// Add Children to Stack
					CellView[] children = cell.getChildViews();
					for (int i = 0; i < children.length; i++)
						s.add(children[i]);
					// Add PortViews to Stack
					for (int i = graphModel.getChildCount(cell.getCell())-1; i >=0 ; i--) {
						Object port = graphModel.getChild(cell.getCell(), i);
						if (graphModel.isPort(port)) {
							CellView portView = getMapping(port, false);
							if (portView != null)
								s.add(portView);
						}
					}
				}
				// Invert Result
				int i = result.size();
				Object[] tmp = new Object[i];
				Iterator it = result.iterator();
				while (it.hasNext())
					tmp[--i] = it.next();
				return tmp;
			} else { // Simply Invert
				Object[] tmp = new Object[cells.length];
				for (int i = 0; i < cells.length; i++)
					tmp[cells.length - i - 1] = cells[i];
				return tmp;
			}
		}
		return cells;
	}

	/**
	 * An implementation of GraphViewChange.
	 */
	public class GraphViewEdit
		extends CompoundEdit
		implements
			GraphModelEvent.GraphViewChange,
			GraphModelEvent.ExecutableGraphChange {

		protected Object[] cells;

		protected CellView[] context, hidden;

		protected Map attributes;

		protected Object[] visible, invisible;

		/**
		* Constructs a GraphViewEdit. This modifies the attributes
		* of the specified views and may be used to notify UndoListeners.
		*
		* @param viewAttributes the map that defines the new attributes
		*/
		public GraphViewEdit(Map nested) {
			this(nested, null, null);
			attributes = nested;
		}

		/**
		* Constructs a GraphViewEdit. This modifies the attributes
		* of the specified views and may be used to notify UndoListeners.
		* This should also take an array of removed cell views, but it 
		* is not possible to add further UndoableEdits to an already
		* executed CompoundEdit, such as a GraphModel change. Thus, 
		* to handle implicit changes -- rather than piggybacking on
		* the model's event -- the CompoundEdit's addEdit method
		* should be extended to accept and instantly execute sub-
		* sequent edits (implicit changes to the view, such as
		* removing a mapping, hiding a view or the like).
		*
		* @param viewAttributes the map that defines the new attributes
		*/
		public GraphViewEdit(
			Map attributes,
			Object[] visible,
			Object[] invisible) {
			super();
			this.attributes = attributes;
			// Context cells
			if (attributes != null) {
				cells = attributes.keySet().toArray();
				Set ctx = DefaultGraphModel.getEdges(getModel(), cells);
				context = getMapping(ctx.toArray());
			}
			// Visibility Operation
			this.visible = visible;
			this.invisible = invisible;
		}

		public boolean isSignificant() {
			return true;
		}

		/**
		 * Returns the source of this change. This can either be a
		 * view or a model, if this change is a GraphModelChange.
		 */
		public Object getSource() {
			return GraphLayoutCache.this;
		}

		/**
		 * Returns the views that have changed.
		 */
		public Object[] getChanged() {
			if (attributes != null)
				return attributes.keySet().toArray();
			return null;
		}

		/**
		 * Returns the views that have not changed explicitly, but
		 * implicitly because one of their dependent cells has changed.
		 */
		public Object[] getContext() {
			return context;
		}

		/**
		 * Returns a map of (cell view, attribute) pairs.
		 */
		public Map getAttributes() {
			return attributes;
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
			// Remember or restore hidden cells
			if (hidden != null)
				for (int i = 0; i < hidden.length; i++)
					if (hidden[i] != null)
						mapping.put(hidden[i].getCell(), hidden[i]);
			if (!rememberCellViews) // already remembered
				hidden = getMapping(invisible);
			// Handle visibility
			boolean updatePorts =
				setVisibleImpl(visible, true)
					| setVisibleImpl(invisible, false);
			// Swap arrays
			Object[] tmp = visible;
			visible = invisible;
			invisible = tmp;
			// Handle attributes
			//System.out.println("GraphLayoutCache::GraphViewEdit::execute::attributes="+attributes);
			if (attributes != null)
				attributes = handleAttributes(attributes);
			if (updatePorts)
				updatePorts();
			// Refresh Context
			if (context != null)
				for (int i = 0; i < context.length; i++)
					if (context[i] != null)
						context[i].refresh(false);
			setChanged();
			notifyObservers(this);
		}

	}

	/**
	 * Attention: Undo will not work for routing-change if
	 * ROUTING and POINTS are stored in different locations.
	 * This happens if the model holds the routing attribute
	 * and the routing changes from unrouted to routed.
	 * In this case the points in the view are already routed
	 * according to the new scheme when written to the command
	 * history (-> no undo).
	 */
	protected Map handleAttributes(Map attributes) {
		Map undo = new Hashtable();
		CellView[] views = new CellView[attributes.size()];
		Iterator it = attributes.entrySet().iterator();
		int i = 0;
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			CellView cv = getMapping(entry.getKey(), false);
			views[i] = cv;
			i += 1;
			if (cv != null) {
				Map deltaNew = (Map) entry.getValue();
				//System.out.println("state=" + cv.getAttributes());
				//System.out.println("change=" + deltaNew);
				Map deltaOld = cv.setAttributes(deltaNew);
				cv.refresh(false);
				//System.out.println("state'=" + cv.getAttributes());
				//System.out.println("change'=" + deltaOld);
				undo.put(cv.getCell(), deltaOld);
				factory.updateAutoSize(cv);
			}
		}
		// Re-route all child edges
		update(views);
		return undo;
	}

	//
	// GraphViewLayerEdit
	//

	/**
	 * An implementation of GraphViewChange.
	 */
	public static class GraphViewLayerEdit
		extends AbstractUndoableEdit
		implements
			GraphModelEvent.GraphViewChange,
			GraphModelEvent.ExecutableGraphChange {

		public static final int FRONT = -1, BACK = -2;

		protected Object changeSource;
		protected transient Object[] cells;
		protected transient int[] next, prev;
		protected int layer;

		/**
		 * Constructs a GraphViewEdit. This modifies the view attributes
		 * of the specified cells and may be used to notify UndoListeners.
		 *
		 * @param source the source of the change (the view)
		 * @param changed the cellviews that changed
		 * @param viewAttributes the map that defines the new attributes
		 */
		public GraphViewLayerEdit(Object source, Object[] cells, int layer) {
			changeSource = source;
			this.cells = cells;
			this.layer = layer;
			next = new int[cells.length];
			prev = new int[cells.length];
			updateNext();
		}

		protected void updateNext() {
			for (int i = 0; i < next.length; i++)
				next[i] = layer;
		}

		/**
		 * Returns the source of this change. This can either be a
		 * view or a model, if this change is a GraphModelChange.
		 */
		public Object getSource() {
			return changeSource;
		}

		/**
		 * Returns the cells that have changed.
		 */
		public Object[] getChanged() {
			return cells;
		}

		/**
		 * Returns the views that have not changed explicitly, but
		 * implicitly because one of their dependent cells has changed.
		 */
		public Object[] getContext() {
			return null;
		}

		/**
		 * Returns a map of (cell view, attribute) pairs.
		 */
		public Map getAttributes() {
			return null;
		}

		/**
		 * Redoes a change.
		 *
		 * @exception CannotRedoException if the change cannot be redone
		 */
		public void redo() throws CannotRedoException {
			super.redo();
			updateNext();
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
			for (int i = 0; i < cells.length; i++) {
				List list = getParentList(cells[i]);
				if (list != null) {
					prev[i] = list.indexOf(cells[i]);
					if (prev[i] >= 0) {
						list.remove(prev[i]);
						int n = next[i];
						if (n == FRONT)
							n = list.size();
						else if (n == BACK)
							n = 0;
						list.add(n, cells[i]);
						next[i] = prev[i];
					}
				}
			}
			updateListeners();
		}

		protected void updateListeners() {
			((GraphLayoutCache) changeSource).setChanged();
			((GraphLayoutCache) changeSource).notifyObservers(this);
		}

		/**
		 * Returns the list that exclusively contains <code>view</code>.
		 */
		protected List getParentList(Object view) {
			if (view instanceof CellView) {
				CellView parent = ((CellView) view).getParentView();
				List list = null;
				if (parent == null)
					list = ((GraphLayoutCache) changeSource).roots;
				else if (parent instanceof AbstractCellView)
					list = ((AbstractCellView) parent).childViews;
				return list;
			}
			return null;
		}

	}

	//
	// Static Methods
	//

	/**
	 * Translates the specified views by the given amount.
	 */
	public static void translateViews(CellView[] views, int dx, int dy) {
		views = AbstractCellView.getDescendantViews(views);
		for (int i = 0; i < views.length; i++)
			if (views[i].isLeaf())
				GraphConstants.translate(views[i].getAllAttributes(), dx, dy);
	}

	/**
	 * Returns all views, including descendants that have a parent
	 * in <code>views</code>, especially the PortViews.
	 * Note: Iterative Implementation using model.getChild and getMapping
	 * on this cell mapper.
	 */
	public CellView[] getAllDescendants(CellView[] views) {
		Stack stack = new Stack();
		for (int i = 0; i < views.length; i++)
			if (views[i] != null)
				stack.add(views[i]);
		ArrayList result = new ArrayList();
		while (!stack.isEmpty()) {
			CellView tmp = (CellView) stack.pop();
			Object[] children = tmp.getChildViews();
			for (int i = 0; i < children.length; i++)
				stack.add(children[i]);
			result.add(tmp);
			// Add Port Views
			for (int i = 0; i < graphModel.getChildCount(tmp.getCell()); i++) {
				Object child = graphModel.getChild(tmp.getCell(), i);
				if (graphModel.isPort(child)) {
					CellView view = getMapping(child, false);
					if (view != null)
						stack.add(view);
				}
			}
		}
		CellView[] ret = new CellView[result.size()];
		result.toArray(ret);
		return ret;
	}

	/**
	 * Returns the hiddenSet.
	 * @return Map
	 */
	public Map getHiddenSet() {
		return hiddenSet;
	}

	/**
	 * Returns the hideEdgesOnBecomeInvisible.
	 * @return boolean
	 */
	public boolean isHideEdgesOnBecomeInvisible() {
		return hideEdgesOnBecomeInvisible;
	}

	/**
	 * Returns the hideEdgesOnHide.
	 * @return boolean
	 */
	public boolean isHideEdgesOnHide() {
		return hideEdgesOnHide;
	}

	/**
	 * Returns the rememberCellViews.
	 * @return boolean
	 */
	public boolean isRememberCellViews() {
		return rememberCellViews;
	}

	/**
	 * Returns the showAllEdgesForVisibleVertices.
	 * @return boolean
	 */
	public boolean isShowAllEdgesForVisibleVertices() {
		return showAllEdgesForVisibleVertices;
	}

	/**
	 * Returns the showEdgesOnShow.
	 * @return boolean
	 */
	public boolean isShowEdgesOnShow() {
		return showEdgesOnShow;
	}

	/**
	 * Sets the hiddenSet.
	 * @param hiddenSet The hiddenSet to set
	 */
	public void setHiddenSet(Map hiddenSet) {
		this.hiddenSet = hiddenSet;
	}

	/**
	 * Sets the hideEdgesOnBecomeInvisible.
	 * @param hideEdgesOnBecomeInvisible The hideEdgesOnBecomeInvisible to set
	 */
	public void setHideEdgesOnBecomeInvisible(boolean hideEdgesOnBecomeInvisible) {
		this.hideEdgesOnBecomeInvisible = hideEdgesOnBecomeInvisible;
	}

	/**
	 * Sets the hideEdgesOnHide.
	 * @param hideEdgesOnHide The hideEdgesOnHide to set
	 */
	public void setHideEdgesOnHide(boolean hideEdgesOnHide) {
		this.hideEdgesOnHide = hideEdgesOnHide;
	}

	/**
	 * Sets the rememberCellViews.
	 * @param rememberCellViews The rememberCellViews to set
	 */
	public void setRememberCellViews(boolean rememberCellViews) {
		this.rememberCellViews = rememberCellViews;
	}

	/**
	 * Sets the showAllEdgesForVisibleVertices.
	 * @param showAllEdgesForVisibleVertices The showAllEdgesForVisibleVertices to set
	 */
	public void setShowAllEdgesForVisibleVertices(boolean showAllEdgesForVisibleVertices) {
		this.showAllEdgesForVisibleVertices = showAllEdgesForVisibleVertices;
	}

	/**
	 * Sets the showEdgesOnShow.
	 * @param showEdgesOnShow The showEdgesOnShow to set
	 */
	public void setShowEdgesOnShow(boolean showEdgesOnShow) {
		this.showEdgesOnShow = showEdgesOnShow;
	}

}
