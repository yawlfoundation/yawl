/*
 * @(#)EdgeView.java	1.0 1/1/02
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

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.io.Serializable;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.jgraph.JGraph;
import org.jgraph.plaf.GraphUI;
import org.jgraph.plaf.basic.BasicGraphUI;

/**
 * The default implementation of an edge view.
 *
 * @version 1.0 1/1/02
 * @author Gaudenz Alder
 */

public class EdgeView extends AbstractCellView {

	/** Renderer for the class. */
	public static EdgeRenderer renderer = new EdgeRenderer();

	/** List of points of the edge. May contain ports. */
	protected java.util.List points;

	/** Cached source and target portview of the edge. */
	protected CellView source, target;

	/** Cached label position of the edge. */
	protected Point labelPosition;

	/** Drawing attributes that are created on the fly */
	public Shape beginShape, endShape, lineShape;

	/** Shared-path tune-up. */
	public transient GeneralPath sharedPath = null;

	private Rectangle cachedLabelBounds = null;
	private Rectangle cachedBounds = null;

	/**
	 * Constructs an edge view for the specified model object.
	 *
	 * @param cell reference to the model object
	 */
	public EdgeView(Object cell, JGraph graph, CellMapper mapper) {
		super(cell, graph, mapper);
	}

	//
	// Data Source
	//

	/**
	 * Overrides the parent method to udpate the cached points,
	 * source and target port. If the source or target is removed,
	 * a point is inserted into the array of points.
	 */
	public void refresh(boolean createDependentViews) {
		super.refresh(createDependentViews);
		// Sync Source- and Targetport
		if (points != null) {
			Object modelSource = getModel().getSource(cell);
			Object modelTarget = getModel().getTarget(cell);
			setSource(mapper.getMapping(modelSource, createDependentViews));
			setTarget(mapper.getMapping(modelTarget, createDependentViews));
			// Re-Route
			Edge.Routing routing = GraphConstants.getRouting(allAttributes);
			if (routing != null)
				routing.route(this, points);
		}
	}

	/**
	 * Update attributes and recurse children.
	 */
	public void update() {
		super.update();
		points = GraphConstants.getPoints(allAttributes);
		labelPosition = GraphConstants.getLabelPosition(allAttributes);
		Edge.Routing routing = GraphConstants.getRouting(allAttributes);
		if (routing != null)
			routing.route(this, points);
		// Synchronize Points and PortViews
		if (getModel().getSource(cell) != null)
			setSource(getSource());
		if (getModel().getTarget(cell) != null)
			setTarget(getTarget());
		// Clear cached shapes
		beginShape = null;
		endShape = null;
		lineShape = null;
		sharedPath = null;
		cachedBounds = null;
		cachedLabelBounds = null;
	}

	void invalidate() {
		sharedPath = null;
		cachedBounds = null;
		cachedLabelBounds = null;
	}
	public static long shapeHits = 0;
	/**
	 * Returns the shape of the view according to the last rendering state
	 */
	public final Shape getShape() {
		if (sharedPath != null) {
			return sharedPath;
		} else {
			shapeHits++;
			return sharedPath = (GeneralPath) getEdgeRenderer().createShape();
		}
	}

	/**
	 * Returns the bounds of label according to the last rendering state
	 */
	public final Rectangle getLabelBounds() {
		if (cachedLabelBounds != null) {
			return cachedLabelBounds;
		} else {
			return cachedLabelBounds = getEdgeRenderer().getLabelBounds(this);
		}
	}

	//
	// View Methods
	//

	/**
	 * Returns true if this view intersects the given rectangle.
	 */
	public boolean intersects(Graphics g, Rectangle rect) {
		return getEdgeRenderer().intersects(g, this, rect);
	}

	/**
	 * Returns the location for this portview.
	 */
	public Rectangle getBounds() {
		if (cachedBounds != null) {
			return cachedBounds;
		} else {
			return cachedBounds = getEdgeRenderer().getBounds(this);
		}
	}

	/**
	 * Returns the local renderer. Do not access the renderer
	 * field directly. Use this method instead!
	 */
	public EdgeRenderer getEdgeRenderer() {
		return (EdgeRenderer) getRenderer();
	}

	/**
	 * Returns a renderer for the class.
	 */
	public CellViewRenderer getRenderer() {
		return renderer;
	}

	/**
	 * Returns a cell handle for the view.
	 */
	public CellHandle getHandle(GraphContext context) {
		return new EdgeHandle(this, context);
	}

	//
	// Cached Values
	//

	/**
	 * Returns the CellView that represents the source of the edge.
	 */
	public CellView getSource() {
		return source;
	}

	/**
	 * Sets the <code>sourceView</code> of the edge.
	 */
	public void setSource(CellView sourceView) {
		source = sourceView;
		if (source != null)
			points.set(0, source);
		else
			points.set(0, getPoint(0));
		invalidate();
	}

	/**
	 * Returns the CellView that represents the target of the edge.
	 */
	public CellView getTarget() {
		return target;
	}

	/**
	 * Sets the <code>targetView</code> of the edge.
	 */
	public void setTarget(CellView targetView) {
		target = targetView;
		int n = points.size() - 1;
		if (target != null)
			points.set(n, target);
		else
			points.set(n, getPoint(n));
		invalidate();
	}

	/**
	 * Returns a point that describes the position of the label.
	 */
	public Point getLabelPosition() {
		return labelPosition;
	}

	/**
	 * Sets the description of the label position.
	 */
	public void setLabelPosition(Point pos) {
		labelPosition.setLocation(pos);
		invalidate();
	}

	//
	// Points
	//

	/**
	 * Returns the points.
	 * @return java.util.List
	 */
	public java.util.List getPoints() {
		return points;
	}

	/**
	 * Returns the number of point for this edge.
	 */
	public int getPointCount() {
		return points.size();
	}

	/**
	 * Returns the cached points for this edge.
	 */
	public Point getPoint(int index) {
		Object obj = points.get(index);
		if (obj instanceof PortView)
			// Port Location Seen From This Edge
			return ((PortView) obj).getLocation(this);
		else if (obj instanceof CellView)
			return ((CellView) obj).getBounds().getLocation();
		else if (obj instanceof Point)
			// Regular Point
			return (Point) obj;
		return null;
	}

	/**
	 * Sets the point at <code>index</code> to <code>p</code>.
	 */
	public void setPoint(int index, Point p) {
		points.set(index, p);
		invalidate();
	}

	/**
	 * Adds <code>p</code> at position <code>index</code>.
	 */
	public void addPoint(int index, Point p) {
		points.add(index, p);
		invalidate();
	}

	/**
	 * Removes the point at position <code>index</code>.
	 */
	public void removePoint(int index) {
		points.remove(index);
		invalidate();
	}

	/**
	 * Returning true signifies a mouse event adds a new point to an edge.
	 */
	public boolean isAddPointEvent(MouseEvent event) {
		return SwingUtilities.isRightMouseButton(event);
	}

	/**
	 * Returning true signifies a mouse event removes a given point.
	 */
	public boolean isRemovePointEvent(MouseEvent event) {
		return SwingUtilities.isRightMouseButton(event);
	}

	//
	// Routing
	//

	public static double getLength(CellView view) {
		double cost = 1;
		if (view instanceof EdgeView) {
			EdgeView edge = (EdgeView) view;
			Point last = null, current = null;
			for (int i = 0; i < edge.getPointCount(); i++) {
				current = edge.getPoint(i);
				if (last != null)
					cost += last.distance(current);
				last = current;
			}
		}
		return cost;
	}

	public boolean isConstrainedMoveEvent(MouseEvent e) {
		GraphUI ui = graph.getUI();
		if (ui instanceof BasicGraphUI)
			return ((BasicGraphUI) ui).isConstrainedMoveEvent(e);
		return false;
	}

	//
	// Handle
	//

	// This implementation uses the point instance to make the change. No index
	// is used for the current point because routing could change the index during
	// the move operation.
	public class EdgeHandle implements CellHandle, Serializable {

		protected JGraph graph;

		/* Pointer to the edge and its clone. */
		protected EdgeView edge, orig;

		/* Boolean indicating whether the source, target or label is being edited. */
		protected boolean label = false, source = false, target = false;

		/* Pointer to the currently selected point. */
		protected Point currentPoint;

		/* Array of control points represented as rectangles. */
		protected transient Rectangle[] r;

		/* A control point for the label position. */
		protected transient Rectangle loc;

		protected boolean firstOverlayCall = true;

		protected boolean isEdgeConnectable = true;

		protected EdgeView relevantEdge = null;

		public EdgeHandle(EdgeView edge, GraphContext ctx) {
			this.graph = ctx.getGraph();
			this.edge = edge;
			loc = new Rectangle();
			orig =
				(EdgeView) graph.getGraphLayoutCache().getMapping(
					edge.getCell(),
					false);
			reloadPoints(orig);
			isEdgeConnectable =
				GraphConstants.isConnectable(edge.getAllAttributes());
		}

		protected void reloadPoints(EdgeView edge) {
			relevantEdge = edge;
			r = new Rectangle[edge.getPointCount()];
			for (int i = 0; i < r.length; i++)
				r[i] = new Rectangle();
			invalidate();
		}

		// Update and paint control points
		public void paint(Graphics g) {
			invalidate();
			for (int i = 0; i < r.length; i++) {
				if (isEdgeConnectable)
					g.setColor(graph.getHandleColor());
				else
					g.setColor(graph.getLockedHandleColor());
				g.fill3DRect(r[i].x, r[i].y, r[i].width, r[i].height, true);
				CellView port = null;
				if (i == 0 && edge.getSource() != null)
					port = edge.getSource();
				else if (i == r.length - 1 && edge.getTarget() != null)
					port = edge.getTarget();
				if (port != null) {
					g.setColor(graph.getLockedHandleColor());
					Point tmp =
						GraphConstants.getOffset(port.getAllAttributes());
					if (tmp != null) {
						g.drawLine(
							r[i].x + 1,
							r[i].y + 1,
							r[i].x + r[i].width - 3,
							r[i].y + r[i].height - 3);
						g.drawLine(
							r[i].x + 1,
							r[i].y + r[i].height - 3,
							r[i].x + r[i].width - 3,
							r[i].y + 1);
					} else
						g.drawRect(
							r[i].x + 2,
							r[i].y + 2,
							r[i].width - 5,
							r[i].height - 5);
				}
			}
		}

		public void overlay(Graphics g) {
			if (edge != null && !firstOverlayCall) {
				//g.setColor(graph.getBackground()); // JDK 1.3
				g.setColor(graph.getForeground());
				//g.setXORMode(graph.getBackground());
				g.setXORMode(graph.getBackground().darker());
				Graphics2D g2 = (Graphics2D) g;
				AffineTransform oldTransform = g2.getTransform();
				g2.scale(graph.getScale(), graph.getScale());
				graph.getUI().paintCell(g, edge, edge.getBounds(), true);
				g2.setTransform(oldTransform);
				if (isSourceEditing() && edge.getSource() != null)
					paintPort(g, edge.getSource());
				else if (isTargetEditing() && edge.getTarget() != null)
					paintPort(g, edge.getTarget());
			}
			firstOverlayCall = false;
		}

		protected void paintPort(Graphics g, CellView p) {
			boolean offset =
				(GraphConstants.getOffset(p.getAllAttributes()) != null);
			Rectangle r =
				(offset) ? p.getBounds() : p.getParentView().getBounds();
			r = graph.toScreen(new Rectangle(r));
			int s = 3;
			r.translate(-s, -s);
			r.setSize(r.width + 2 * s, r.height + 2 * s);
			graph.getUI().paintCell(g, p, r, true);
		}

		protected boolean snap(boolean source, Point point) {
			boolean connect = graph.isConnectable() && isEdgeConnectable;
			Object port = graph.getPortForLocation(point.x, point.y);
			if (port != null && connect) {
				CellView portView =
					graph.getGraphLayoutCache().getMapping(port, false);
				if (GraphConstants
					.isConnectable(
						portView.getParentView().getAllAttributes())) {
					Object cell = edge.getCell();
					if (source
						&& edge.getSource() != portView
						&& getModel().acceptsSource(cell, port)) {
						overlay(graph.getGraphics());
						edge.setSource(portView);
						edge.update();
						overlay(graph.getGraphics());
					} else if (
						!source
							&& edge.getTarget() != portView
							&& getModel().acceptsTarget(cell, port)) {
						overlay(graph.getGraphics());
						edge.setTarget(portView);
						edge.update();
						overlay(graph.getGraphics());
					}
					return portView != null;
				}
			}
			return false;
		}

		protected boolean isSourceEditing() {
			return source;
			//return (index == 0 && edge.getSource() != null);
		}

		protected boolean isTargetEditing() {
			return target;
			//return (
			//	index == edge.getPointCount() - 1 && edge.getTarget() != null);
		}

		/* Returns true if either the source, target, label or a point is being edited. */
		protected boolean isEditing() {
			return source || target || label || currentPoint != null;
		}

		/**
		 * Invoked when the mouse pointer has been moved on a component
		 * (with no buttons down).
		 */
		public void mouseMoved(MouseEvent event) {
			for (int i = 0; i < r.length; i++)
				if (r[i].contains(event.getPoint())) {
					graph.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
					event.consume();
					return;
				}
			if (loc.contains(event.getPoint())
				&& graph.isMoveable()
				&& GraphConstants.isMoveable(edge.getAllAttributes())) {
				graph.setCursor(new Cursor(Cursor.HAND_CURSOR));
				event.consume();
			}
		}

		// Handle mouse pressed event.
		public void mousePressed(MouseEvent event) {
			/* INV: currentPoint = null; source = target = label = false; */
			boolean bendable =
				graph.isBendable()
					&& GraphConstants.isBendable(edge.getAllAttributes());
			boolean disconnectable =
				graph.isDisconnectable()
					&& GraphConstants.isDisconnectable(orig.getAllAttributes());
			int x = event.getX();
			int y = event.getY();
			// Detect hit on control point
			int index = 0;
			for (index = 0; index < r.length; index++) {
				if (r[index].contains(x, y)) {
					currentPoint = edge.getPoint(index);
					source =
						index == 0
							&& (edge.getSource() == null
								|| (disconnectable
									&& GraphConstants.isDisconnectable(
										edge
											.getSource()
											.getParentView()
											.getAllAttributes())));
					target =
						index == r.length - 1
							&& (edge.getTarget() == null
								|| (disconnectable
									&& GraphConstants.isDisconnectable(
										edge
											.getTarget()
											.getParentView()
											.getAllAttributes())));
					break;
				}
			}
			// Detect hit on label
			if (!isEditing()
				&& graph.isMoveable()
				&& GraphConstants.isMoveable(edge.getAllAttributes())
				&& loc != null
				&& loc.contains(x, y)
				&& !isAddPointEvent(event)
				&& !isRemovePointEvent(event)) {
				if (event.getClickCount() == graph.getEditClickCount())
					graph.startEditingAtCell(edge);
				else
					label = true;
				// Remove Point
			}
			if (isRemovePointEvent(event)
				&& currentPoint != null
				&& bendable) {
				edge.removePoint(index);
				mouseReleased(event);
				// Add Point
			} else if (isAddPointEvent(event) && !isEditing() && bendable) {
				int s = graph.getHandleSize();
				Rectangle rect =
					graph.fromScreen(new Rectangle(x - s, y - s, 2 * s, 2 * s));
				if (edge.intersects(graph.getGraphics(), rect)) {
					Point point =
						graph.fromScreen(
							graph.snap(new Point(event.getPoint())));
					double min = Double.MAX_VALUE, dist = 0;
					for (int i = 0; i < edge.getPointCount() - 1; i++) {
						Point p = edge.getPoint(i);
						Point p1 = edge.getPoint(i + 1);
						dist = new Line2D.Double(p, p1).ptLineDistSq(point);
						if (dist < min) {
							min = dist;
							index = i + 1;
						}
					}
					edge.addPoint(index, point);
					currentPoint = point;
					reloadPoints(edge);
					paint(graph.getGraphics());
				}
			}
			if (isEditing())
				event.consume();
		}

		public void mouseDragged(MouseEvent event) {
			Point p = graph.fromScreen(new Point(event.getPoint()));
			// Move Label
			if (label) {
				Rectangle r = edge.getBounds();
				if (r != null) {
					Point p0 = edge.getPoint(0);
					Point pe = edge.getPoint(edge.getPointCount() - 1);
					int vx = p.x - r.x;
					if (p0.x > pe.x)
						vx = r.x + r.width - p.x;
					int vy = p.y - r.y;
					if (p0.y > pe.y)
						vy = r.y + r.height - p.y;
					int xunit = 1;
					if (r.width != 0)
						xunit = GraphConstants.PERMILLE / r.width;
					int yunit = 1;
					if (r.height != 0)
						yunit = GraphConstants.PERMILLE / r.height;
					p = new Point(vx * xunit, vy * yunit);
					overlay(graph.getGraphics());
					edge.setLabelPosition(p);
					edge.update();
					overlay(graph.getGraphics());
				}
			} else if (isEditing()) {
				// Find Source/Target Port
				if (!((source && snap(true, p))
					|| (target && snap(false, p)))) { // Else Use Point
					if ((source && getModel().acceptsSource(cell, null))
						|| (target && getModel().acceptsTarget(cell, null))
						|| !(source || target)) {
						overlay(graph.getGraphics());
						p =
							graph.fromScreen(
								graph.snap(new Point(event.getPoint())));
						// Constrained movement
						if (isConstrainedMoveEvent(event)) {
							// Reset Initial Positions
							EdgeView orig =
								(EdgeView) graph
									.getGraphLayoutCache()
									.getMapping(
									edge.getCell(),
									false);
							int index = 0;
							if (target)
								index = orig.getPointCount() - 1;
							else
								edge.getPoints().indexOf(currentPoint);
							Point origPoint = orig.getPoint(index);
							int totDx = p.x - origPoint.x;
							int totDy = p.y - origPoint.y;
							if (Math.abs(totDx) < Math.abs(totDy))
								p.x = origPoint.x;
							else
								p.y = origPoint.y;
						}
						// Do not move into negative space
						p.x =
							GraphConstants.NEGATIVE_ALLOWED
								? p.x
								: Math.max(0, p.x);
						p.y =
							GraphConstants.NEGATIVE_ALLOWED
								? p.y
								: Math.max(0, p.y);
						currentPoint.setLocation(p);
						if (source) {
							edge.setPoint(0, p);
							edge.setSource(null);
						} else if (target) {
							edge.setPoint(edge.getPointCount() - 1, p);
							edge.setTarget(null);
						}
						edge.update();
						overlay(graph.getGraphics());
					}
				}
			}
		} // Handle mouse released event

		public void mouseReleased(MouseEvent e) {
			boolean clone = e.isControlDown() && graph.isCloneable();
			ConnectionSet cs = createConnectionSet(edge, edge.getCell(), clone);
			Map nested =
				GraphConstants.createAttributes(new CellView[] { edge }, null);
			if (clone) {
				Map cellMap = graph.cloneCells(new Object[] { edge.getCell()});
				nested = GraphConstants.replaceKeys(cellMap, nested);
				cs = cs.clone(cellMap);
				graph.getGraphLayoutCache().insert(
					cellMap.values().toArray(),
					nested,
					cs,
					null,
					null);
			} else
				graph.getGraphLayoutCache().edit(nested, cs, null, null);
			e.consume();
		}

		protected ConnectionSet createConnectionSet(
			EdgeView view,
			Object edge,
			boolean verbose) {
			ConnectionSet cs = new ConnectionSet();
			Object sourcePort = null, targetPort = null;
			if (view.getSource() != null)
				sourcePort = view.getSource().getCell();
			if (view.getTarget() != null)
				targetPort = view.getTarget().getCell();
			if (verbose || sourcePort != getModel().getSource(edge))
				cs.connect(edge, sourcePort, true);
			if (verbose || targetPort != getModel().getTarget(edge))
				cs.connect(edge, targetPort, false);
			return cs;
		}

		// Update control points
		protected void invalidate() {
			EdgeView e = relevantEdge;
			int handlesize = graph.getHandleSize();
			EdgeRenderer er = (EdgeRenderer) edge.getRenderer();
			for (int i = 0; i < r.length; i++) {
				Point p = graph.toScreen(new Point(e.getPoint(i)));
				r[i].setBounds(
					p.x - handlesize,
					p.y - handlesize,
					2 * handlesize,
					2 * handlesize);
				p = graph.toScreen(er.getLabelPosition(e));
				Dimension d = er.getLabelSize(e);
				if (p != null && d != null) {
					Point s = graph.toScreen(new Point(d.width, d.height));
					loc.setBounds(p.x - s.x / 2, p.y - s.y / 2, s.x, s.y);
				}
			}
		}

	}

}
