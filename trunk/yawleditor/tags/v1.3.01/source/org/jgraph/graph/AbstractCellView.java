/*
 * @(#)AbstractCellView.java	1.0 1/1/02
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import javax.swing.SwingUtilities;

import org.jgraph.JGraph;

/**
 * The abstract base class for all cell views.
 *
 * @version 1.0 1/3/02
 * @author Gaudenz Alder
 */

public abstract class AbstractCellView implements CellView, Serializable {

	/** Editor for the cell. */
	protected static GraphCellEditor cellEditor = new DefaultGraphCellEditor();

	/** Reference to the graph*/
	protected JGraph graph = null;

	/** Reference to the cell mapper for the view */
	protected CellMapper mapper = null;

	/** Reference to the cell for this view */
	protected Object cell = null;

	/** Cached parent view */
	protected CellView parent = null;

	/** Cached child views. */
	protected java.util.List childViews = new ArrayList();

	/** Hashtable for attributes. Initially empty */
	protected Map attributes = GraphConstants.createMap();

	protected Map allAttributes;

	/**
	 * Constructs a view for the specified model object,
	 * and invokes update on the new instance.
	 *
	 * @param cell reference to the model object
	 */
	public AbstractCellView(Object cell, JGraph graph, CellMapper mapper) {
		this.cell = cell;
		this.graph = graph;
		this.mapper = mapper;
		updateAllAttributes();
	}

	//
	// Data Source
	//

	/**
	 * Returns the graph associated with the view.
	 */
	public JGraph getGraph() {
		return graph;
	}

	/**
	 * Returns the model associated with the view.
	 */
	public GraphModel getModel() {
		return graph.getModel();
	}

	/**
	 * Returns the cell mapper associated with the view.
	 */
	public CellMapper getMapper() {
		return mapper;
	}

	/**
	 * Returns the model object that this view represents.
	 */
	public Object getCell() {
		return cell;
	}

	/**
	 * Create child views and reload properties. Invokes update first.
	 */
	public void refresh(boolean createDependentViews) {
		// Cache Cell Attributes in View
		update();
		// Cache Parent View
		if (mapper != null && getModel() != null) {
			// Create parent only if it's visible in the graph
			Object par = getModel().getParent(cell);
			if (graph.getGraphLayoutCache().isVisible(par)) {
				CellView tmp = mapper.getMapping(par, createDependentViews);
				if (tmp != parent)
					removeFromParent();
				parent = tmp;
			} else if (parent != null) {
				removeFromParent();
				parent = null;
			}
		}
		// Cache Child Views
		boolean reorder = !graph.getGraphLayoutCache().isOrdered();
		if (reorder)
			childViews.clear();
		GraphModel model = getModel();
		for (int i = 0; i < model.getChildCount(cell); i++) {
			Object child = model.getChild(cell, i);
			if (graph.getGraphLayoutCache().isVisible(child)) {
				CellView view = mapper.getMapping(child, createDependentViews);
				if (view != null) {
					// Ignore Ports
					if (!model.isPort(child)
						&& (!childViews.contains(view) || reorder))
						childViews.add(view);
				}
			}
		}
	}

	/**
	 * Update attributes and recurse children.
	 */
	public void update() {
		updateAllAttributes();
		// Notify Parent
		childUpdated();
	}

	/**
	 * This method implements the merge between the
	 * cell's and the view's attributes. The view's
	 * attributes override the cell's attributes
	 * with one exception.
	 */
	protected void updateAllAttributes() {
		allAttributes = getModel().getAttributes(cell);
		if (allAttributes != null) {
			allAttributes = GraphConstants.cloneMap(allAttributes);
		} else
			allAttributes = GraphConstants.createMap();
		allAttributes.putAll(attributes);
	}

	public void childUpdated() {
		if (parent != null)
			parent.childUpdated();
	}

	//
	// Graph Structure
	//

	/**
	 * Returns the parent view for this view.
	 */
	public CellView getParentView() {
		return parent;
	}

	/**
	 * Returns the child views of this view.
	 */
	public CellView[] getChildViews() {
		CellView[] array = new CellView[childViews.size()];
		childViews.toArray(array);
		return array;
	}

	/**
	 * Returns all views, including descendants that have a parent
	 * in <code>views</code> without the PortViews.
	 * Note: Iterative Implementation using view.getChildViews
	 */
	public static CellView[] getDescendantViews(CellView[] views) {
		Stack stack = new Stack();
		for (int i = 0; i < views.length; i++)
			stack.add(views[i]);
		ArrayList result = new ArrayList();
		while (!stack.isEmpty()) {
			CellView tmp = (CellView) stack.pop();
			Object[] children = tmp.getChildViews();
			for (int i = 0; i < children.length; i++)
				stack.add(children[i]);
			result.add(tmp);
		}
		CellView[] ret = new CellView[result.size()];
		result.toArray(ret);
		return ret;
	}

	/**
	 * Removes this view from the list of childs of the parent.
	 */
	public void removeFromParent() {
		if (parent instanceof AbstractCellView) {
			java.util.List list = ((AbstractCellView) parent).childViews;
			list.remove(this);
		}
	}

	/**
	 * Returns true if the view is a leaf.
	 */
	public boolean isLeaf() {
		return childViews.isEmpty();
	}

	//
	// View Attributes
	//

	/**
	 * Return the attributes of the view.
	 */
	public Map getAttributes() {
		return attributes;
	}

	/**
	 * Returns the attributes of the view combined with the
	 * attributes of the corresponding cell. The view's attributes
	 * override the cell's attributes with the same key.
	 */
	public Map getAllAttributes() {
		return allAttributes;
	}

	/**
	 * Applies <code>change</code> to the attributes of the view
	 * and calls update.
	 */
	public Map setAttributes(Map change) {
		Map undo = GraphConstants.applyMap(change, attributes);
		update();
		return undo;
	}

	//
	// View Methods
	//

	/**
	 * Returns the bounding rectangle for this view.
	 */
	public abstract Rectangle getBounds();

	/**
	 * Returns the bounding box for the specified views.
	 */
	public static Rectangle getBounds(CellView[] views) {
		if (views != null && views.length > 0) {
			Rectangle ret = null;
			for (int i = 0; i < views.length; i++) {
				if (views[i] != null) {
					Rectangle r = views[i].getBounds();
					if (r != null) {
						if (ret == null)
							ret = new Rectangle(r);
						else
							SwingUtilities.computeUnion(
								r.x,
								r.y,
								r.width,
								r.height,
								ret);
					}
				}
			}
			return ret;
		}
		return null;
	}

	/**
	 * Sets the bounds of <code>view</code>.
	 * Calls translateView and scaleView.
	 */
	public void setBounds(Rectangle bounds) {
		Rectangle oldBounds = getBounds();
		Point p0;
		Dimension last;
		if (oldBounds != null) {
			p0 = oldBounds.getLocation();
			last = oldBounds.getSize();
		} else {
			p0 = new Point(0, 0);
			last = new Dimension(0, 0);
		}
		Point pe = bounds.getLocation(); // Translate
		Rectangle localBounds = new Rectangle(bounds);
		if (GraphConstants.isMoveable(getAttributes()) && !pe.equals(p0))
			translate(pe.x - p0.x, pe.y - p0.y);
		else
			localBounds.setSize(
				bounds.width - pe.x + p0.x,
				bounds.height - pe.y + p0.y);
		Dimension next = localBounds.getSize(); // Scale
		if (!last.equals(next) && last.width > 0 && last.height > 0) {
			double sx = (double) next.width / (double) last.width;
			double sy = (double) next.height / (double) last.height;
			scale(sx, sy, pe);
		}
	}

	/**
	 * Translates <code>view</code> (group) by <code>dx, dy</code>.
	 */
	protected void translate(int dx, int dy) {
		if (isLeaf())
			GraphConstants.translate(getAttributes(), dx, dy);
		else {
			Iterator it = childViews.iterator();
			while (it.hasNext()) {
				Object view = it.next();
				if (view instanceof AbstractCellView) {
					AbstractCellView child = (AbstractCellView) view;
					child.translate(dx, dy);
				}
			}
		}
	}

	/**
	 * Scale <code>view</code> (group) by <code>sx, sy</code>.
	 */
	protected void scale(double sx, double sy, Point origin) {
		if (isLeaf())
			GraphConstants.scale(getAttributes(), sx, sy, origin);
		else {
			Iterator it = childViews.iterator();
			while (it.hasNext()) {
				Object view = it.next();
				if (view instanceof AbstractCellView) {
					AbstractCellView child = (AbstractCellView) view;
					Map attributes = child.getAttributes();
					if (GraphConstants.isSizeable(attributes)
						|| GraphConstants.isAutoSize(attributes))
						child.scale(sx, sy, origin);
				}
			}
		}
	}

	/**
	 * Returns true if the view intersects the given rectangle.
	 */
	public boolean intersects(Graphics g, Rectangle rect) {
		if (isLeaf()) {
			Rectangle bounds = getBounds();
			if (bounds != null)
				return bounds.intersects(rect);
		} else { // Check If Children Intersect
			Iterator it = childViews.iterator();
			while (it.hasNext())
				if (((CellView) it.next()).intersects(g, rect))
					return true;
		}
		return false;
	}

	//
	// View Editors
	//

	/**
	 * Returns a renderer component, configured for the view.
	 */
	public Component getRendererComponent(
		JGraph graph,
		boolean selected,
		boolean focus,
		boolean preview) {
		CellViewRenderer cvr = getRenderer();
		if (cvr != null)
			return cvr.getRendererComponent(
				graph,
				this,
				selected,
				focus,
				preview);
		return null;
	}

	protected abstract CellViewRenderer getRenderer();

	/**
	 * Returns a cell handle for the view.
	 */
	public abstract CellHandle getHandle(GraphContext context);

	/**
	 * Returns a cell editor for the view.
	 */
	public GraphCellEditor getEditor() {
		return cellEditor;
	}

}
