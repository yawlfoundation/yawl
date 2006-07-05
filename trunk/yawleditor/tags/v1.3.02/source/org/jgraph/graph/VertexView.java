/*
 * @(#)VertexView.java	1.0 1/1/02
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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.jgraph.JGraph;
import org.jgraph.plaf.GraphUI;
import org.jgraph.plaf.basic.BasicGraphUI;

/**
 * The default implementation of a vertex view.
 *
 * @version 1.0 1/1/02
 * @author Gaudenz Alder
 */

public class VertexView extends AbstractCellView {

	/** Renderer for the class. */
	public static VertexRenderer renderer = new VertexRenderer();

	/** Reference to the bounds attribute */
	protected Rectangle bounds;

	/** Cached bounds of all children if vertex is a group */
	protected Rectangle groupBounds = DefaultGraphCell.defaultBounds;

	/**
	 * Constructs a vertex view for the specified model object
	 * and the specified child views.
	 *
	 * @param cell reference to the model object
	 */
	public VertexView(Object cell, JGraph graph, CellMapper mapper) {
		super(cell, graph, mapper);
	}

	//
	// CellView Interface
	//

	/**
	 * Overrides the parent method to udpate the cached points.
	 */
	public void update() {
		super.update();
		bounds = GraphConstants.getBounds(allAttributes);
		groupBounds = null;
	}

	public void childUpdated() {
		super.childUpdated();
		groupBounds = null;
	}

	/**
	 * Returns the cached bounds for the vertex.
	 */
	public Rectangle getBounds() {
		if (!isLeaf()) {
			if (groupBounds == null)
				updateGroupBounds();
			return groupBounds;
		}
		return bounds;
	}

	public Rectangle getCachedBounds() {
		return bounds;
	}

	public void setCachedBounds(Rectangle bounds) {
		this.bounds = bounds;
	}

	protected void updateGroupBounds() {
		// Note: Prevent infinite recursion by removing
		// child edges that point to their parent.
		CellView[] childViews = getChildViews();
		LinkedList result = new LinkedList();
		for (int i = 0; i < childViews.length; i++)
			if (includeInGroupBounds(childViews[i]))
				result.add(childViews[i]);
		childViews = new CellView[result.size()];
		result.toArray(childViews);
		groupBounds = getBounds(childViews);
	}

	private boolean includeInGroupBounds(CellView view) {
		if (view instanceof EdgeView) {
			GraphModel model = graph.getModel();
			EdgeView edgeView = (EdgeView) view;
			if (edgeView.getCell() instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode edge =
					(DefaultMutableTreeNode) edgeView.getCell();
				if (model.getSource(edge) instanceof TreeNode) {
					TreeNode source = (TreeNode) model.getSource(edge);
					if (((DefaultMutableTreeNode) source.getParent())
						.isNodeDescendant(edge)) {
						return false;
					}
				}
				if (model.getTarget(edge) instanceof TreeNode) {
					TreeNode target = (TreeNode) model.getTarget(edge);
					if (((DefaultMutableTreeNode) target.getParent())
						.isNodeDescendant(edge)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Returns a renderer for the class.
	 */
	public CellViewRenderer getRenderer() {
		return renderer;
	}

	/**
	 * Returns a cell handle for the view, if the graph and the view
	 * are sizeable.
	 */
	public CellHandle getHandle(GraphContext context) {
		if (GraphConstants.isSizeable(getAllAttributes())
			&& context.getGraph().isSizeable())
			return new SizeHandle(this, context);
		return null;
	}

	//
	// Special Methods
	//

	/**
	 * Returns the center of this vertex.
	 */
	public Point getCenterPoint() {
		Rectangle r = getBounds();
		return new Point((int) r.getCenterX(), (int) r.getCenterY());
	}

	/**
	 * Returns the intersection of the bounding rectangle and the
	 * straight line between the source and the specified point p.
	 * The specified point is expected not to intersect the bounds.
	 * Note: You must override this method if you use a different
	 * renderer. This is because this method relies on the
	 * VertexRenderer interface, which can not be safely assumed
	 * for subclassers.
	 */
	public Point getPerimeterPoint(Point source, Point p) {
		return renderer.getPerimeterPoint(this, source, p);
	}

	public boolean isConstrainedSizeEvent(MouseEvent e) {
		GraphUI ui = graph.getUI();
		if (ui instanceof BasicGraphUI)
			return ((BasicGraphUI) ui).isConstrainedMoveEvent(e);
		return false;
	}

	public class SizeHandle implements CellHandle, Serializable {

		// Double Buffer
		protected transient Image offscreen;
		protected transient Graphics offgraphics;
		protected transient boolean firstDrag = true;

		protected transient JGraph graph;

		/* Reference to the temporary view for this handle. */
		protected transient VertexView vertex;

		protected transient CellView[] portViews;

		protected transient Rectangle cachedBounds;

		/* Reference to the context for the specified view. */
		protected transient GraphContext context;

		protected transient Rectangle initialBounds;

		protected transient CellView[] contextViews;

		/* Index of the active control point. -1 if none is active. */
		protected transient int index = -1;

		/* Array of control points represented as rectangles. */
		protected transient Rectangle[] r = new Rectangle[8];

		protected boolean firstOverlayInvocation = true;

		/** Array that holds the cursors for the different control points. */
		public transient int[] cursors =
			new int[] {
				Cursor.NW_RESIZE_CURSOR,
				Cursor.N_RESIZE_CURSOR,
				Cursor.NE_RESIZE_CURSOR,
				Cursor.W_RESIZE_CURSOR,
				Cursor.E_RESIZE_CURSOR,
				Cursor.SW_RESIZE_CURSOR,
				Cursor.S_RESIZE_CURSOR,
				Cursor.SE_RESIZE_CURSOR };

		public SizeHandle(VertexView vertexview, GraphContext ctx) {
			graph = ctx.getGraph();
			vertex = vertexview;
			// PortView Preview
			portViews = ctx.createTemporaryPortViews();
			initialBounds = new Rectangle(vertex.getBounds());
			context = ctx;
			for (int i = 0; i < r.length; i++)
				r[i] = new Rectangle();
			invalidate();
		}

		public void paint(Graphics g) {
			invalidate();
			g.setColor(graph.getHandleColor());
			for (int i = 0; i < r.length; i++)
				g.fill3DRect(r[i].x, r[i].y, r[i].width, r[i].height, true);
		}

		// Double Buffers by David Larsson
		protected void initOffscreen() {
			try {
				Rectangle rect = graph.getBounds();
				//RepaintManager repMan = RepaintManager.currentManager(graph);
				//offscreen = repMan.getVolatileOffscreenBuffer(getGraph(), (int) rect.getWidth(), (int) rect.getHeight());
				offscreen =
					new BufferedImage(
						rect.width,
						rect.height,
						BufferedImage.TYPE_INT_RGB);
				offgraphics = offscreen.getGraphics();
				offgraphics.setClip(
					0,
					0,
					(int) rect.getWidth(),
					(int) rect.getHeight());
				offgraphics.setColor(graph.getBackground());
				offgraphics.fillRect(
					0,
					0,
					(int) rect.getWidth(),
					(int) rect.getHeight());
				graph.getUI().paint(offgraphics, graph);
			} catch (Error e) {
				offscreen = null;
				offgraphics = null;
			}
		}

		public void overlay(Graphics g) {
			if (!firstOverlayInvocation) {
				if (cachedBounds != null) {
					g.setColor(Color.black);
					Rectangle tmp = graph.toScreen(new Rectangle(cachedBounds));
					g.drawRect(tmp.x, tmp.y, tmp.width - 2, tmp.height - 2);
				} else if (!initialBounds.equals(vertex.getBounds())) {
					Graphics2D g2 = (Graphics2D) g;
					AffineTransform oldTransform = g2.getTransform();
					g2.scale(graph.getScale(), graph.getScale());
					graph.getUI().paintCell(
						g,
						vertex,
						vertex.getBounds(),
						true);
					if (contextViews != null)
						for (int i = 0; i < contextViews.length; i++) {
							graph.getUI().paintCell(
								g,
								contextViews[i],
								contextViews[i].getBounds(),
								true);
						}
					g2.setTransform(oldTransform);
					if (portViews != null && graph.isPortsVisible())
						graph.getUI().paintPorts(g, portViews);
				}
			}
			firstOverlayInvocation = false;
		}

		/**
		 * Invoked when the mouse pointer has been moved on a component
		 * (with no buttons down).
		 */
		public void mouseMoved(MouseEvent event) {
			if (vertex != null) {
				for (int i = 0; i < r.length; i++) {
					if (r[i].contains(event.getPoint())) {
						graph.setCursor(new Cursor(cursors[i]));
						event.consume();
						return;
					}
				}
			}
		}

		/** Process mouse pressed event. */
		public void mousePressed(MouseEvent event) {
			if (!graph.isSizeable())
				return;
			for (int i = 0; i < r.length; i++) {
				if (r[i].contains(event.getPoint())) {
					Set set = new HashSet();
					set.add(vertex.getCell());
					contextViews = context.createTemporaryContextViews(set);
					Object[] all =
						AbstractCellView.getDescendantViews(
							new CellView[] { vertex });
					if (all.length
						>= org.jgraph.plaf.basic.BasicGraphUI.MAXHANDLES)
						cachedBounds = new Rectangle(initialBounds);
					event.consume();
					index = i;
					return;
				}
			}
		}

		/** Process mouse dragged event. */
		public void mouseDragged(MouseEvent event) {
			if (firstDrag
				&& graph.isDoubleBuffered()
				&& cachedBounds == null) {
				initOffscreen();
				firstDrag = false;
			}
			Rectangle dirty = null;
			Graphics g =
				(offgraphics != null) ? offgraphics : graph.getGraphics();
			if (index == -1)
				return;
			Rectangle newBounds = computeBounds(event);
			g.setColor(graph.getForeground());
			g.setXORMode(graph.getBackground().darker());
			overlay(g);
			if (offgraphics != null) {
				dirty = graph.toScreen(new Rectangle(vertex.getBounds()));
				Rectangle t =
					graph.toScreen(AbstractCellView.getBounds(contextViews));
				if (t != null)
					dirty.add(t);
			}
			if (cachedBounds != null)
				cachedBounds = newBounds;
			else {
				// Reset old Bounds
				CellView[] all =
					AbstractCellView.getDescendantViews(
						new CellView[] { vertex });
				for (int i = 0; i < all.length; i++) {
					CellView orig =
						graph.getGraphLayoutCache().getMapping(
							all[i].getCell(),
							false);
					Map origAttr =
						GraphConstants.cloneMap(orig.getAllAttributes());
					all[i].setAttributes(origAttr);
					all[i].refresh(false);
				}
				vertex.setBounds(newBounds);
				if (vertex != null)
					graph.getGraphLayoutCache().update(vertex);
				if (contextViews != null)
					graph.getGraphLayoutCache().update(contextViews);
			}
			overlay(g);
			if (offscreen != null) {
				dirty.add(graph.toScreen(new Rectangle(vertex.getBounds())));
				Rectangle t =
					graph.toScreen(AbstractCellView.getBounds(contextViews));
				if (t != null)
					dirty.add(t);
				dirty.grow(2, 2);
				int sx1 =
					(GraphConstants.NEGATIVE_ALLOWED)
						? dirty.x
						: Math.max(0, dirty.x);
				int sy1 =
					(GraphConstants.NEGATIVE_ALLOWED)
						? dirty.y
						: Math.max(0, dirty.y);
				int sx2 = sx1 + dirty.width;
				int sy2 = sy1 + dirty.height;
				graph.getGraphics().drawImage(
					offscreen,
					sx1,
					sy1,
					sx2,
					sy2,
					sx1,
					sy1,
					sx2,
					sy2,
					graph);
			}
		}

		protected Rectangle computeBounds(MouseEvent event) {
			int left = initialBounds.x;
			int right = initialBounds.x + initialBounds.width - 1;
			int top = initialBounds.y;
			int bottom = initialBounds.y + initialBounds.height - 1;
			Point p = graph.fromScreen(graph.snap(new Point(event.getPoint())));
			// Not into negative coordinates
			if (!GraphConstants.NEGATIVE_ALLOWED) {
				p.x = Math.max(0, p.x);
				p.y = Math.max(0, p.y);
			}
			// Bottom row
			if (index > 4)
				bottom = p.y;
			// Top row
			else if (index < 3)
				top = p.y;
			// Left col
			if (index == 0 || index == 3 || index == 5)
				left = p.x;
			// Right col
			else if (index == 2 || index == 4 || index == 7)
				right = p.x;
			int width = right - left;
			int height = bottom - top;
			if (isConstrainedSizeEvent(event)) {
				if (index == 3 || index == 4 || index == 5)
					height = width;
				else if (index == 1 || index == 6 || index == 2 || index == 7)
					width = height;
				else {
					height = width;
					top = bottom - height;
				}
			}
			if (width < 0) { // Flip over left side
				left += width;
				width = Math.abs(width);
			}
			if (height < 0) { // Flip over top side
				top += height;
				height = Math.abs(height);
			}
			return new Rectangle(left, top, width + 1, height + 1);
		}

		// Dispatch the edit event
		public void mouseReleased(MouseEvent e) {
			if (index != -1) {
				cachedBounds = computeBounds(e);
				vertex.setBounds(cachedBounds);
				CellView[] views =
					AbstractCellView.getDescendantViews(
						new CellView[] { vertex });
				Map attributes = GraphConstants.createAttributes(views, null);
				graph.getGraphLayoutCache().edit(attributes, null, null, null);
			}
			e.consume();
			cachedBounds = null;
			initialBounds = null;
			firstDrag = true;
		}

		private void invalidate() {
			// Retrieve current bounds and set local vars
			Rectangle tmp = graph.getCellBounds(vertex.getCell());
			if (tmp != null) {
				tmp = new Rectangle(tmp);
				graph.toScreen(tmp);
				int handlesize = graph.getHandleSize();
				int s2 = 2 * handlesize;
				int left = tmp.x - handlesize;
				int top = tmp.y - handlesize;
				int w2 = tmp.x + (tmp.width / 2) - handlesize;
				int h2 = tmp.y + (tmp.height / 2) - handlesize;
				int right = tmp.x + tmp.width - handlesize;
				int bottom = tmp.y + tmp.height - handlesize;
				// Update control point positions
				r[0].setBounds(left, top, s2, s2);
				r[1].setBounds(w2, top, s2, s2);
				r[2].setBounds(right, top, s2, s2);
				r[3].setBounds(left, h2, s2, s2);
				r[4].setBounds(right, h2, s2, s2);
				r[5].setBounds(left, bottom, s2, s2);
				r[6].setBounds(w2, bottom, s2, s2);
				r[7].setBounds(right, bottom, s2, s2);
			}
		}

	}

}
