/*
 * @(#)GraphContext.java	1.0 1/1/02
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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgraph.JGraph;

/*
 * This object is used for interactive operations such as move and copy.
 * It offers two basic operations: 1. Create temporary views for a set
 * of cells, and 2. Create temporary views for a set of edges that are
 * connected to a set of cells.<p>
 * <strong>Note:</strong>The views are consistent subgraphs. Consequently,
 * if an edge points to a selected and an unselected port, the selected
 * port is replaced by its temporary view, but the unselected port is not.
 *
 * @version 1.0 1/1/02
 * @author Gaudenz Alder
 */

public class GraphContext implements CellMapper {

	/** Reference to the parent graph. */
	protected JGraph graph;

	/** Reference to the graphs GraphLayoutCache. */
	protected transient GraphLayoutCache graphLayoutCache;

	/** Reference to the cells. */
	protected Object[] cells;

	/** Set of all cells including all descendants. */
	protected Set cellSet;

	/** Number of all descendants without ports. */
	protected int cellCount;

	/** Map of (cell, view) pairs including ports. */
	protected Map views = new Hashtable();

	/**
	 * Constructs a graph context for <code>cells</code> with respect to
	 * the connections defined in the model, and the views in the view
	 * of <code>graph</code>.
	 */
	public GraphContext(JGraph graph, Object[] cells) {
		GraphModel model = graph.getModel();
		cellSet = DefaultGraphModel.getDescendants(model, cells);
		graphLayoutCache = graph.getGraphLayoutCache();
		this.graph = graph;
		this.cells = cells;
		// Count Non-Port Cells
		Iterator it = cellSet.iterator();
		while (it.hasNext())
			if (!model.isPort(it.next()))
				cellCount++;
	}

	/**
	 * Returns <code>true</code> if this object contains no cells.
	 */
	public boolean isEmpty() {
		return (cells == null || cells.length == 0);
	}

	/**
	 * Returns the number of all objects (cells and children) in this object.
	 */
	public int getDescendantCount() {
		return cellCount;
	}

	/**
	 * Returns the graph that was passed to the constructor.
	 */
	public JGraph getGraph() {
		return graph;
	}

	/**
	 * Returns the array that was passed to the constructor.
	 */
	public Object[] getCells() {
		return cells;
	}

	/**
	 * Returns <code>true</code> if <code>node</code> or one of
	 * its ancestors is contained in this object.
	 */
	public boolean contains(Object node) {
		return cellSet.contains(node);
	}

	/**
	 * Returns an new consistent array of views for <code>cells</code>.
	 */
	public CellView[] createTemporaryCellViews() {
		CellView[] cellViews = new CellView[cells.length];
		for (int i = 0; i < cells.length; i++)
			// Get View For Cell
			cellViews[i] = getMapping(cells[i], true);
		return cellViews;
	}

	/**
	 * Returns an new consistent array of views for the ports.
	 */
	public CellView[] createTemporaryPortViews() {
		GraphModel model = graph.getModel();
		ArrayList result = new ArrayList();
		Iterator it = cellSet.iterator();
		while (it.hasNext()) {
			Object cand = it.next();
			if (model.isPort(cand) && graph.getGraphLayoutCache().isVisible(cand))
				result.add(getMapping(cand, true));
		}
		// List -> CellView[] Conversion
		CellView[] array = new PortView[result.size()];
		result.toArray(array);
		return array;
	}

	/**
	 * Returns an new consistent array of views for the edges
	 * that are connected to and not contained in <code>cells</code>.
	 */
	public CellView[] createTemporaryContextViews() {
		return createTemporaryContextViews(cellSet);
	}

	/**
	 * Returns an new consistent array of views for the edges that
	 * are connected to and not contained in <code>cellSet</code>.
	 */
	public CellView[] createTemporaryContextViews(Set cellSet) {
		Object[] cells = cellSet.toArray();
		// Retrieve Edges From Model (FIX: Retrieve edges from view)
		Set set = DefaultGraphModel.getEdges(graph.getModel(), cells);
		List result = new ArrayList();
		// Iterate over Edges
		Iterator it = set.iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			// If Edge not on cellset, add its view if visible in graphview
			if (!cellSet.contains(obj)
				&& graphLayoutCache.isVisible(obj)
				&& graphLayoutCache.getMapping(obj, false) != null)
				// Note: Do not use getMapping, it ignores the create flag
				result.add(createMapping(obj));
		}
		// List -> CellView[] Conversion
		CellView[] array = new CellView[result.size()];
		result.toArray(array);
		return array;
	}

	/**
	 * Returns the <code>CellView</code> that is mapped to <code>cell</code>
	 * in the graph context. New views are created based on whether cell
	 * is containes in the context. The <code>create</code>-flag is ignored.
	 */
	public CellView getMapping(Object cell, boolean create) {
		if (cell != null) {
			CellView view = (CellView) views.get(cell);
			if (view != null)
				return view;
			else if (contains(cell))
				return createMapping(cell);
			else
				return graphLayoutCache.getMapping(cell, false);
		}
		return null;
	}

	public CellView createMapping(Object cell) {
		CellView src = graphLayoutCache.getMapping(cell, false);
		CellView view = graphLayoutCache.getFactory().createView(cell, this);
		// Fetch Attributes From Original View
		if (src != null) {
			view.setAttributes(GraphConstants.cloneMap(src.getAllAttributes()));
			// Override fetched PortViews
			view.refresh(false);
		}
		return view;
	}

	/**
	 * Disconnects the edges in <code>cells</code> from the sources
	 * and targets that are not in this context and returns a
	 * ConnectionSet that defines the disconnection.
	 */
	public ConnectionSet disconnect(CellView[] cells) {
		ConnectionSet cs = new ConnectionSet();
		for (int i = 0; i < cells.length; i++) {
			if (cells[i] instanceof EdgeView) {
				EdgeView view = (EdgeView) cells[i];
				CellView port = view.getSource();
				if (GraphConstants.isDisconnectable(view.getAllAttributes())) {
					if (port != null
						&& GraphConstants.isDisconnectable(
							port.getParentView().getAllAttributes())
						&& !contains(port.getCell())) {
						view.setSource(null);
						cs.disconnect(view.getCell(), true);
					}
					port = view.getTarget();
					if (port != null
						&& GraphConstants.isDisconnectable(
							port.getParentView().getAllAttributes())
						&& !contains(port.getCell())) {
						view.setTarget(null);
						cs.disconnect(view.getCell(), false);
					}
				}
			}
		}
		return cs;
	}

	/**
	 * Associates <code>cell</code> with <code>view</code>
	 * in the graph context.
	 */
	public void putMapping(Object cell, CellView view) {
		views.put(cell, view);
	}

}
