/*
 * @(#)DefaultEdge.java	1.0 1/1/02
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A simple implementation for an edge.
 *
 * @version 1.0 1/1/02
 * @author Gaudenz Alder
 */

public class DefaultEdge extends DefaultGraphCell implements Edge {

	protected static final int center = GraphConstants.PERMILLE / 2;

	protected static final Point defaultLabel = new Point(center, center);

	public static final ArrayList defaultPoints = new ArrayList();

	static {
		defaultPoints.add(new Point(10, 10));
		defaultPoints.add(new Point(20, 20));
	}

	/** Source and target of the edge. */
	protected Object source, target;

	/**
	 * Constructs an empty edge.
	 */
	public DefaultEdge() {
		this(null);
	}

	/**
	 * Constructs an edge that holds a reference to the specified user object.
	 *
	 * @param userObject reference to the user object
	 */
	public DefaultEdge(Object userObject) {
		this(userObject, false);
	}

	/**
	 * Constructs an edge that holds a reference to the specified user object
	 * and sets default values for points and the label position.
	 *
	 * @param userObject reference to the user object
	 */
	public DefaultEdge(Object userObject, boolean allowsChildren) {
		super(userObject, allowsChildren);
		GraphConstants.setPoints(attributes, new ArrayList(defaultPoints));
		GraphConstants.setLabelPosition(attributes, defaultLabel);
	}

	/**
	 * Override parent method to ensure non-null points.
	 */
	public Map changeAttributes(Map change) {
		Map undo = super.changeAttributes(change);
		List points = GraphConstants.getPoints(attributes);
		if (points == null)
			GraphConstants.setPoints(attributes, new ArrayList(defaultPoints));
		return undo;
	}

	/**
	 * Returns the source of the edge.
	 */
	public Object getSource() {
		return source;
	}

	/**
	 * Returns the target of the edge.
	 */
	public Object getTarget() {
		return target;
	}

	/**
	 * Sets the source of the edge.
	 */
	public void setSource(Object port) {
		source = port;
	}

	/**
	 * Returns the target of <code>edge</code>.
	 */
	public void setTarget(Object port) {
		target = port;
	}

	/**
	 * Create a clone of the cell. The cloning of the
	 * user object is deferred to the cloneUserObject()
	 * method.
	 *
	 * @return Object  a clone of this object.
	 */
	public Object clone() {
		DefaultEdge c = (DefaultEdge) super.clone();
		c.source = null;
		c.target = null;
		return c;
	}

	//
	// Default Routing
	// 

	public static class DefaultRouting implements Edge.Routing {

		public void route(EdgeView edge, java.util.List points) {
			int n = points.size();
			Point from = edge.getPoint(0);
			if (edge.getSource() instanceof PortView)
				from = ((PortView) edge.getSource()).getLocation(null);
			else if (edge.getSource() != null)
				from = edge.getSource().getBounds().getLocation();
			Point to = edge.getPoint(n - 1);
			if (edge.getTarget() instanceof PortView)
				to = ((PortView) edge.getTarget()).getLocation(null);
			else if (edge.getTarget() != null)
				to = edge.getTarget().getBounds().getLocation();
			if (from != null && to != null) {
				Point[] routed;
				// Handle self references
				if (edge.getSource() == edge.getTarget()
					&& edge.getSource() != null) {
					Rectangle bounds =
						edge.getSource().getParentView().getBounds();
					int height = edge.getGraph().getGridSize();
					int width = (int) (bounds.getWidth() / 3);
					routed = new Point[4];
					routed[0] =
						new Point(
							bounds.x + width,
							bounds.y + bounds.height);
					routed[1] =
						new Point(
							bounds.x + width,
							bounds.y + bounds.height + height);
					routed[2] =
						new Point(
							bounds.x + 2 * width,
							bounds.y + bounds.height + height);
					routed[3] =
						new Point(
							bounds.x + 2 * width,
							bounds.y + bounds.height);
				} else {
					int dx = Math.abs(from.x - to.x);
					int dy = Math.abs(from.y - to.y);
					int x2 = from.x + ((to.x - from.x) / 2);
					int y2 = from.y + ((to.y - from.y) / 2);
					routed = new Point[2];
					if (dx > dy) {
						routed[0] = new Point(x2, from.y);
						//new Point(to.x, from.y)
						routed[1] = new Point(x2, to.y);
					} else {
						routed[0] = new Point(from.x, y2);
						// new Point(from.x, to.y)
						routed[1] = new Point(to.x, y2);
					}
				}
				// Set/Add Points
				for (int i=0; i<routed.length; i++)
					if (points.size() > i+2)
						points.set(i+1, routed[i]);
					else
						points.add(i+1, routed[i]);
				// Remove spare points
				while (points.size() > routed.length+2) {
					points.remove(points.size()-2);
				}
			}
		}

	}

}
