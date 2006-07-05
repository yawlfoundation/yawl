/*
 * @(#)PortView.java	1.0 1/1/02
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

import java.awt.Point;
import java.awt.Rectangle;

import org.jgraph.JGraph;

/**
 * The default implementation of a port view.
 *
 * @version 1.0 1/1/02
 * @author Gaudenz Alder
 */

public class PortView extends AbstractCellView {

	/** Default size for all ports is 6. */
	protected static int size = 6;

	/** Renderer for the class. */
	public static PortRenderer renderer = new PortRenderer();

	/** Cache of the last valid parent. //FIX: Better solution? */
	protected transient CellView lastParent;

	/**
	 * Constructs a view that holds a reference to the specified cell,
	 * anchor and parent vertex.
	 *
	 * @param cell reference to the cell in the model
	 * @param anchor view of the parent
	 * @param anchor view of the anchor port
	 */
	public PortView(Object cell, JGraph graph, CellMapper mapper) {
		super(cell, graph, mapper);
	}

	//
	// CellView interface
	//

	/**
	 * This method ensures a non-null value. If the super method
	 * returns null then the last valid parent is returned.
	 * Note: If a vertex is removed, all ports will be replaced
	 * in connected edges. The ports are replaced by the center
	 * point of the <i>last</i> valid vertex view.
	 */
	public CellView getParentView() {
		CellView parent = super.getParentView();
		if (parent == null)
			parent = lastParent;
		else
			lastParent = parent;
		return parent;
	}

	/**
	 * Returns the bounds for the port view.
	 */
	public Rectangle getBounds() {
		Rectangle bounds = new Rectangle(getLocation(null));
		bounds.x = bounds.x - size / 2;
		bounds.y = bounds.y - size / 2;
		bounds.width = bounds.width + size;
		bounds.height = bounds.height + size;
		return bounds;
	}

	/**
	 * Returns a renderer for the class.
	 */
	public CellViewRenderer getRenderer() {
		return renderer;
	}

	/**
	 * Returns <code>null</code>.
	 */
	public CellHandle getHandle(GraphContext context) {
		return null;
	}

	//
	// Special Methods
	//

	/**
	 * Returns the point that the port represents with respect
	 * to <code>edge</code>. <code>edge</code> may be <code>null</code>.
	 */
	public Point getLocation(EdgeView edge) {
		Object modelAnchor = null;
		if (cell instanceof Port)
			modelAnchor = ((Port) cell).getAnchor();
		PortView anchor = (PortView) mapper.getMapping(modelAnchor, false);
		Point pos = null;
		boolean isAbsolute = GraphConstants.isAbsolute(allAttributes);
		Point offset = GraphConstants.getOffset(allAttributes);
		VertexView vertex = (VertexView) getParentView();
		// If No Edge Return Center
		if (vertex != null) {
			if (edge == null && offset == null)
				pos = vertex.getCenterPoint();
			// Apply Offset
			if (offset != null) {
				int x = offset.x;
				int y = offset.y;
				Rectangle r = vertex.getBounds();
				// Absolute Offset
				if (!isAbsolute) {
					x = (int) (x * (r.width - 1) / GraphConstants.PERMILLE);
					y = (int) (y * (r.height - 1) / GraphConstants.PERMILLE);
				} // Offset from Anchor
				pos =
					(anchor != null)
						? anchor.getLocation(edge)
						: r.getLocation();
				pos = new Point(pos.x + x, pos.y + y);
			} else if (edge != null) {
				// Floating Port
				Point nearest = getNextPoint(edge);
				if (nearest == null)
					// If "Dangling" Port Return Center
					return vertex.getCenterPoint();
				pos = vertex.getPerimeterPoint(pos, nearest);
			}
		}
		return pos;
	}

	/**
	 * Returns the point that is closest to the port view on
	 * <code>edge</code>. Returns <code>null</code> if
	 * <code>edge</code> has less than 2 points.
	 */
	protected Point getNextPoint(EdgeView edge) {
		int n = edge.getPointCount();
		if (n > 1) {
			if (edge.getSource() == this)
				return getEdgePoint(edge, 1);
			else if (edge.getTarget() == this)
				return getEdgePoint(edge, n - 2);
		}
		return null;
	}

	/**
	 * Returns the point of <code>edge</code> at index
	 * <code>index</code>. Avoids calling <code>getLocation</code>
	 * on the opposite port of <code>edge</code> (possible
	 * infinite recursion).
	 */
	protected Point getEdgePoint(EdgeView view, int index) {
		Object obj = view.points.get(index);
		if (obj instanceof Point)
			return (Point) obj;
		else if (obj instanceof PortView) {
			VertexView vertex = (VertexView) ((CellView) obj).getParentView();
			if (vertex != null)
				return vertex.getCenterPoint();
		}
		return null;
	}

}
