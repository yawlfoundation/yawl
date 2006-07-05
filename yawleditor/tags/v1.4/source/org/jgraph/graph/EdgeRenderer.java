/*
 * @(#)EdgeRenderer.java	1.0 03-JUL-04
 * 
 * Copyright (c) 2001-2005 Gaudenz Alder
 *  
 * See LICENSE file in distribution for licensing details of this source file
 */
package org.jgraph.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.UIManager;

import org.jgraph.JGraph;
import org.jgraph.util.Bezier;
import org.jgraph.util.Spline2D;

/**
 * This renderer displays entries that implement the CellView interface.
 * 
 * @version 1.0 1/1/02
 * @author Gaudenz Alder
 */

public class EdgeRenderer extends JComponent implements CellViewRenderer,
		Serializable {

	/** Static Graphics used for Font Metrics */
	protected static transient Graphics fontGraphics;

	// Headless environment does not allow graphics
	static {
		try {
			fontGraphics = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)
					.getGraphics();
		} catch (Error e) {
			// No font graphics
			fontGraphics = null;
		}
	}

	/** A switch for painting the extra labels */
	public boolean simpleExtraLabels = true;

	/** Override this if you want the extra labels to appear in a special fontJ */
	public Font extraLabelFont = null;

	/** Reference to the font metrics of the above */
	protected transient FontMetrics metrics;

	/** Cache the current graph for drawing */
	protected transient JGraph graph;

	/** Cache the current edgeview for drawing */
	protected transient EdgeView view;

	/** Painting attributes of the current edgeview */
	protected transient int beginDeco, endDeco, beginSize, endSize, lineStyle;

	/** Width of the current edge view */
	protected transient float lineWidth;

	/**
	 * Boolean attributes of the current edgeview. Fill flags are checked for
	 * valid decorations.
	 */
	protected transient boolean labelBorder, beginFill, endFill, focus,
			selected, preview, opaque, childrenSelected, labelTransformEnabled;

	/**
	 * Color attributes of the current edgeview. This components foreground is
	 * set to the edgecolor, the fontColor is in an extra variable. If the
	 * fontColor is null, the current foreground is used. The default background
	 * instead is used for text and is not visible if the label is not visible
	 * or if opaque is true.
	 */
	protected transient Color borderColor, defaultForeground,
			defaultBackground, fontColor;

	/** Contains the current dash pattern. Null means no pattern. */
	protected transient float[] lineDash;

	/** Contains the current dash offset. Null means no offset. */
	protected transient float dashOffset = 0.0f;

	/** The gradient color of the edge */
	protected transient Color gradientColor = null;

	/* THIS CODE WAS ADDED BY MARTIN KRUEGER 10/20/2003 */

	protected Bezier bezier;

	protected Spline2D spline;

	/* END */

	/**
	 * Constructs a renderer that may be used to render edges.
	 */
	public EdgeRenderer() {
		defaultForeground = UIManager.getColor("Tree.textForeground");
		defaultBackground = UIManager.getColor("Tree.textBackground");
	}

	/**
	 * Sets view to work with, caching necessary values until the next call of
	 * this method or until some other methods with explicitly specified
	 * different view
	 */
	void setView(CellView value) {
		if (value instanceof EdgeView) {
			view = (EdgeView) value;
			installAttributes(view);
		} else {
			view = null;
		}
	}

	/**
	 * Configure and return the renderer based on the passed in components. The
	 * value is typically set from messaging the graph with
	 * <code>convertValueToString</code>.
	 * 
	 * @param graph
	 *            the graph that that defines the rendering context.
	 * @param view
	 *            the cell view that should be rendered.
	 * @param sel
	 *            whether the object is selected.
	 * @param focus
	 *            whether the object has the focus.
	 * @param preview
	 *            whether we are drawing a preview.
	 * @return the component used to render the value.
	 */
	public Component getRendererComponent(JGraph graph, CellView view,
			boolean sel, boolean focus, boolean preview) {
		if (view instanceof EdgeView && graph != null) {
			this.graph = graph;
			this.focus = focus;
			this.selected = sel;
			this.preview = preview;
			this.childrenSelected = graph.getSelectionModel()
					.isChildrenSelected(view.getCell());
			setView(view);
			return this;
		}
		return null;
	}

	/**
	 * Returns true if the edge shape intersects the given rectangle.
	 */
	public boolean intersects(JGraph graph, CellView value, Rectangle rect) {
		if (value instanceof EdgeView && graph != null && value != null) {
			setView(value);

			// If we have two control points, we can get rid of hit
			// detection on do an intersection test on the two diagonals
			// of rect and the line between the two points
			EdgeView edgeView = (EdgeView) value;
			if (edgeView.getPointCount() == 2) {
				Point2D p0 = edgeView.getPoint(0);
				Point2D p1 = edgeView.getPoint(1);
				if (rect.intersectsLine(p0.getX(), p0.getY(), p1.getX(), p1
						.getY()))
					return true;
			} else {
				Graphics2D g2 = (Graphics2D) graph.getGraphics();
				if (g2.hit(rect, view.getShape(), true))
					return true;
			}
			Rectangle2D r = getLabelBounds(graph, view);
			if (r != null && r.intersects(rect))
				return true;
			Object[] labels = GraphConstants.getExtraLabels(view
					.getAllAttributes());
			if (labels != null) {
				for (int i = 0; i < labels.length; i++) {
					r = getExtraLabelBounds(graph, view, i);
					if (r != null && r.intersects(rect))
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns the bounds of the edge shape.
	 */
	public Rectangle2D getBounds(CellView value) {
		if (value instanceof EdgeView && value != null) {
			// No need to call setView as getPaintBounds will
			view = (EdgeView) value;
			Rectangle2D r = getPaintBounds(view);
			Rectangle2D rect = getLabelBounds(graph, view);
			if (rect != null)
				Rectangle2D.union(r, rect, r);
			Object[] labels = GraphConstants.getExtraLabels(view
					.getAllAttributes());
			if (labels != null) {
				for (int i = 0; i < labels.length; i++) {
					rect = getExtraLabelBounds(graph, view, i);
					if (rect != null)
						Rectangle2D.union(r, rect, r);
				}
			}
			int b = (int) Math.ceil(lineWidth);
			r.setFrame(r.getX() - b, r.getY() - b, r.getWidth() + 2 * b, r
					.getHeight()
					+ 2 * b);
			return r;
		}
		return null;
	}

	private boolean isLabelTransformEnabled() {
		return labelTransformEnabled;
	}

	/**
	 * Estimates whether the transform for label should be applied. With the
	 * transform, the label will be painted along the edge. To apply transform,
	 * rotate graphics by the angle returned from {@link #getLabelAngle}
	 * 
	 * @return true, if transform can be applied, false otherwise
	 */
	private boolean isLabelTransform(String label) {
		if (!isLabelTransformEnabled()) {
			return false;
		}
		Point2D p = getLabelPosition(view);
		if (p != null && label != null && label.length() > 0) {
			int sw = metrics.stringWidth(label);
			Point2D p1 = view.getPoint(0);
			Point2D p2 = view.getPoint(view.getPointCount() - 1);
			double length = Math.sqrt((p2.getX() - p1.getX())
					* (p2.getX() - p1.getX()) + (p2.getY() - p1.getY())
					* (p2.getY() - p1.getY()));
			if (!(length <= Double.NaN || length < sw)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Calculates the angle at which graphics should be rotated to paint label
	 * along the edge. Before calling this method always check that transform
	 * should be applied using {@linkisLabelTransform}
	 * 
	 * @return the value of the angle, 0 if the angle is zero or can't be
	 *         calculated
	 */
	private double getLabelAngle(String label) {
		Point2D p = getLabelPosition(view);
		double angle = 0;
		if (p != null && label != null && label.length() > 0) {
			int sw = metrics.stringWidth(label);
			// Note: For control points you may want to choose other
			// points depending on the segment the label is in.
			Point2D p1 = view.getPoint(0);
			Point2D p2 = view.getPoint(view.getPointCount() - 1);
			// Length of the edge
			double length = Math.sqrt((p2.getX() - p1.getX())
					* (p2.getX() - p1.getX()) + (p2.getY() - p1.getY())
					* (p2.getY() - p1.getY()));
			if (!(length <= Double.NaN || length < sw)) { // Label fits into
				// edge's length

				// To calculate projections of edge
				double cos = (p2.getX() - p1.getX()) / length;
				double sin = (p2.getY() - p1.getY()) / length;

				// Determine angle
				angle = Math.acos(cos);
				if (sin < 0) { // Second half
					angle = 2 * Math.PI - angle;
				}
			}
			if (angle > Math.PI / 2 && angle <= Math.PI * 3 / 2) {
				angle -= Math.PI;
			}
		}
		return angle;
	}

	/**
	 * Returns the label bounds of the specified view in the given graph.
	 */
	public Rectangle2D getLabelBounds(JGraph paintingContext, EdgeView view) {
		if (paintingContext == null && graph != null)
			paintingContext = graph;
		// No need to call setView as getLabelPosition will
		String label = (paintingContext != null) ? paintingContext
				.convertValueToString(view) : String.valueOf(view.getCell());
		if (label != null) {
			Point2D p = getLabelPosition(view);
			Dimension d = getLabelSize(view, label);
			return getLabelBounds(p, d, label);
		} else {
			return null;
		}
	}

	/**
	 * Returns the label bounds of the specified view in the given graph. Note:
	 * The index is the position of the String object for the label in the extra
	 * labels array of the view.
	 */
	public Rectangle2D getExtraLabelBounds(JGraph paintingContext,
			EdgeView view, int index) {
		if (paintingContext == null && graph != null)
			paintingContext = graph;
		setView(view);
		Object[] labels = GraphConstants
				.getExtraLabels(view.getAllAttributes());
		if (labels != null && index < labels.length) {
			Point2D p = getExtraLabelPosition(this.view, index);
			Dimension d = getExtraLabelSize(paintingContext, this.view, index);
			String label = (paintingContext != null) ? paintingContext
					.convertValueToString(labels[index]) : String
					.valueOf(labels[index]);
			return getLabelBounds(p, d, label);
		}
		return new Rectangle2D.Double(getX(), getY(), 0, 0);
	}

	/**
	 * Returns the label bounds of the specified view in the given graph.
	 */
	public Rectangle2D getLabelBounds(Point2D p, Dimension d, String label) {
		if (label != null && isLabelTransform(label)) {
			// With transform label is rotated, so we should
			// rotate the rectangle (sw, sh) and return the
			// bounding rectangle
			double angle = getLabelAngle(label);
			if (angle < 0)
				angle = -angle;
			if (angle > Math.PI / 2)
				angle %= Math.PI / 2;
			double yside = Math.abs(Math.cos(angle) * d.height
					+ Math.sin(angle) * d.width);
			double xside = Math.abs(d.width * Math.cos(angle) + d.height
					* Math.sin(angle));
			// Getting maximum is not good, but I don't want to be
			// drown in calculations
			if (xside > yside)
				yside = xside;
			if (yside > xside)
				xside = yside;
			angle = getLabelAngle(label);

			// Increasing by height is safe, but I think the precise
			// value is font.descent layed on edge vector and
			// projected on each axis
			d.width = (int) xside + d.height;
			d.height = (int) yside + d.height;
		}
		if (p != null && d != null) {
			double x = Math.max(0, p.getX() - (d.width / 2));
			double y = Math.max(0, p.getY() - (d.height / 2));
			return new Rectangle2D.Double(x, y, d.width + 1, d.height + 1);
		}
		return null;
	}

	/**
	 * Returns the label position of the specified view in the given graph.
	 */
	public Point2D getLabelPosition(EdgeView view) {
		setView(view);
		return getLabelPosition(view.getLabelPosition());
	}

	/**
	 * Returns the label position of the specified view in the given graph.
	 */
	public Point2D getExtraLabelPosition(EdgeView view, int index) {
		setView(view);
		Point2D[] pts = GraphConstants.getExtraLabelPositions(view
				.getAllAttributes());
		if (pts != null && index < pts.length)
			return getLabelPosition(pts[index]);
		return null;
	}

	/**
	 * Returns the label position of the specified view in the given graph.
	 */
	protected Point2D getLabelPosition(Point2D pos) {
		Rectangle2D tmp = getPaintBounds(view);
		int unit = GraphConstants.PERMILLE;
		Point2D p0 = view.getPoint(0);
		if (pos != null && tmp != null) {
			Point2D vector = view.getLabelVector();
			double dx = vector.getX();
			double dy = vector.getY();
			double len = Math.sqrt(dx * dx + dy * dy);
			if (len > 0) {
				double x = p0.getX() + (dx * pos.getX() / unit);
				double y = p0.getY() + (dy * pos.getX() / unit);
				x += (-dy * pos.getY() / len);
				y += (dx * pos.getY() / len);
				return new Point2D.Double(x, y);
			} else {
				return new Point2D.Double(p0.getX() + pos.getX(), p0.getY()
						+ pos.getY());
			}
		}
		return null;
	}

	/**
	 * Returns the label size of the specified view in the given graph.
	 */
	public Dimension getExtraLabelSize(JGraph paintingContext, EdgeView view,
			int index) {
		Object[] labels = GraphConstants
				.getExtraLabels(view.getAllAttributes());
		if (labels != null && index < labels.length) {
			String label = (paintingContext != null) ? paintingContext
					.convertValueToString(labels[index]) : String
					.valueOf(labels[index]);
			return getLabelSize(view, label);
		}
		return null;
	}

	/**
	 * Returns the label size of the specified view in the given graph.
	 */
	public Dimension getLabelSize(EdgeView view, String label) {
		if (label != null && fontGraphics != null) {
			fontGraphics.setFont(GraphConstants
					.getFont(view.getAllAttributes()));
			metrics = fontGraphics.getFontMetrics();
			int sw = metrics.stringWidth(label);
			int sh = metrics.getHeight();
			return new Dimension(sw, sh);
		}
		return null;
	}

	/**
	 * Installs the attributes of specified cell in this renderer instance. This
	 * means, retrieve every published key from the cells hashtable and set
	 * global variables or superclass properties accordingly.
	 * 
	 * @param view
	 *            the cell view to retrieve the attribute values from.
	 */
	protected void installAttributes(CellView view) {
		Map map = view.getAllAttributes();
		beginDeco = GraphConstants.getLineBegin(map);
		beginSize = GraphConstants.getBeginSize(map);
		beginFill = GraphConstants.isBeginFill(map) && isFillable(beginDeco);
		endDeco = GraphConstants.getLineEnd(map);
		endSize = GraphConstants.getEndSize(map);
		endFill = GraphConstants.isEndFill(map) && isFillable(endDeco);
		lineWidth = GraphConstants.getLineWidth(map);
		Edge.Routing routing = GraphConstants.getRouting(map);
		lineStyle = (routing != null && view instanceof EdgeView) ? routing
				.getPreferredLineStyle((EdgeView) view)
				: Edge.Routing.NO_PREFERENCE;
		if (lineStyle == Edge.Routing.NO_PREFERENCE)
			lineStyle = GraphConstants.getLineStyle(map);
		lineDash = GraphConstants.getDashPattern(map);
		dashOffset = GraphConstants.getDashOffset(map);
		borderColor = GraphConstants.getBorderColor(map);
		Color foreground = GraphConstants.getLineColor(map);
		setForeground((foreground != null) ? foreground : defaultForeground);
		Color background = GraphConstants.getBackground(map);
		setBackground((background != null) ? background : defaultBackground);
		Color gradientColor = GraphConstants.getGradientColor(map);
		setGradientColor(gradientColor);
		setOpaque(GraphConstants.isOpaque(map));
		setFont(GraphConstants.getFont(map));
		Color tmp = GraphConstants.getForeground(map);
		fontColor = (tmp != null) ? tmp : getForeground();
		labelTransformEnabled = GraphConstants.isLabelAlongEdge(map);
	}

	protected boolean isFillable(int decoration) {
		return !(decoration == GraphConstants.ARROW_SIMPLE
				|| decoration == GraphConstants.ARROW_LINE || decoration == GraphConstants.ARROW_DOUBLELINE);
	}

	/**
	 * Returns the bounds of the edge shape without label
	 */
	public Rectangle2D getPaintBounds(EdgeView view) {
		Rectangle2D rec = null;
		setView(view);
		if (view.getShape() != null)
			rec = view.getShape().getBounds();
		else
			rec = new Rectangle2D.Double(0, 0, 0, 0);
		return rec;
	}

	/**
	 * Paint the renderer.
	 */
	public void paint(Graphics g) {
		if (view.isLeaf()) {
			Shape edgeShape = view.getShape();
			// Sideeffect: beginShape, lineShape, endShape
			if (edgeShape != null) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
						RenderingHints.VALUE_STROKE_PURE);
				int c = BasicStroke.CAP_BUTT;
				int j = BasicStroke.JOIN_MITER;
				setOpaque(false);
				super.paint(g);
				translateGraphics(g);
				g.setColor(getForeground());
				if (lineWidth > 0) {
					g2.setStroke(new BasicStroke(lineWidth, c, j));
					if (gradientColor != null && !preview) {
						g2.setPaint(new GradientPaint(0, 0, getBackground(),
								getWidth(), getHeight(), gradientColor, true));
					}
					if (view.beginShape != null) {
						if (beginFill)
							g2.fill(view.beginShape);
						g2.draw(view.beginShape);
					}
					if (view.endShape != null) {
						if (endFill)
							g2.fill(view.endShape);
						g2.draw(view.endShape);
					}
					if (lineDash != null) // Dash For Line Only
						g2.setStroke(new BasicStroke(lineWidth, c, j, 10.0f,
								lineDash, dashOffset));
					if (view.lineShape != null)
						g2.draw(view.lineShape);
				}

				if (selected) { // Paint Selected
					g2.setStroke(GraphConstants.SELECTION_STROKE);
					g2.setColor(graph.getHighlightColor());
					if (view.beginShape != null)
						g2.draw(view.beginShape);
					if (view.lineShape != null)
						g2.draw(view.lineShape);
					if (view.endShape != null)
						g2.draw(view.endShape);
				}
				g2.setStroke(new BasicStroke(1));
				g
						.setFont((extraLabelFont != null) ? extraLabelFont
								: getFont());
				Object[] labels = GraphConstants.getExtraLabels(view
						.getAllAttributes());
				if (labels != null) {
					for (int i = 0; i < labels.length; i++)
						paintLabel(g, graph.convertValueToString(labels[i]),
								getExtraLabelPosition(view, i),
								false || !simpleExtraLabels);
				}
				if (graph.getEditingCell() != view.getCell()) {
					g.setFont(getFont());
					Object label = graph.convertValueToString(view);
					if (label != null) {
						paintLabel(g, label.toString(), getLabelPosition(view),
								true);
					}
				}
			}
		} else {
			paintSelectionBorder(g);
		}
	}

	/**
	 * Provided for subclassers to paint a selection border.
	 */
	protected void paintSelectionBorder(Graphics g) {
		((Graphics2D) g).setStroke(GraphConstants.SELECTION_STROKE);
		if (childrenSelected)
			g.setColor(graph.getGridColor());
		else if (focus && selected)
			g.setColor(graph.getLockedHandleColor());
		else if (selected)
			g.setColor(graph.getHighlightColor());
		if (childrenSelected || selected) {
			Dimension d = getSize();
			g.drawRect(0, 0, d.width - 1, d.height - 1);
		}
	}

	// This if for subclassers that to not want the graphics
	// to be relative to the top, left corner of this component.
	// Note: Override this method with an empty implementation
	// if you want absolute positions for your edges
	protected void translateGraphics(Graphics g) {
		g.translate(-getX(), -getY());
	}

	/**
	 * Paint the specified label for the current edgeview.
	 */
	protected void paintLabel(Graphics g, String label, Point2D p,
			boolean mainLabel) {
		if (p != null && label != null && label.length() > 0 && metrics != null) {
			int sw = metrics.stringWidth(label);
			int sh = metrics.getHeight();
			Graphics2D g2 = (Graphics2D) g;
			boolean applyTransform = isLabelTransform(label);
			double angle = 0;
			int dx = -sw / 2;
			int offset = graph.isMoveBelowZero() || applyTransform ? 0 : Math
					.min(0, (int) (dx + p.getX()));

			g2.translate(p.getX() - offset, p.getY());
			if (applyTransform) {
				angle = getLabelAngle(label);
				g2.rotate(angle);
			}
			if (isOpaque() && mainLabel) {
				g.setColor(getBackground());
				g.fillRect((int) (-sw / 2 - 1), (int) (-sh / 2 - 1), sw + 2,
						sh + 2);
			}
			if (borderColor != null && mainLabel) {
				g.setColor(borderColor);
				g.drawRect((int) (-sw / 2 - 1), (int) (-sh / 2 - 1), sw + 2,
						sh + 2);
			}

			int dy = +sh / 4;
			g.setColor(fontColor);
			if (applyTransform && borderColor == null && !isOpaque()) {
				// Shift label perpendicularly by the descent so it
				// doesn't cross the line.
				dy = -metrics.getDescent();
			}
			g.drawString(label, dx, dy);
			if (applyTransform) {
				// Undo the transform
				g2.rotate(-angle);
			}
			g2.translate(-p.getX() + offset, -p.getY());
		}
	}

	/**
	 * Returns the shape that represents the current edge in the context of the
	 * current graph. This method sets the global beginShape, lineShape and
	 * endShape variables as a side-effect.
	 */
	protected Shape createShape() {
		int n = view.getPointCount();
		if (n > 1) {
			// Following block may modify static vars as side effect (Flyweight
			// Design)
			EdgeView tmp = view;
			Point2D[] p = null;
			p = new Point2D[n];
			for (int i = 0; i < n; i++) {
				Point2D pt = tmp.getPoint(i);
				if (pt == null)
					return null; // exit
				p[i] = new Point2D.Double(pt.getX(), pt.getY());
			}

			// End of Side-Effect Block
			// Undo Possible MT-Side Effects
			if (view != tmp) {
				view = tmp;
				installAttributes(view);
			}
			// End of Undo
			if (view.sharedPath == null) {
				view.sharedPath = new GeneralPath(GeneralPath.WIND_NON_ZERO, n);
			} else {
				view.sharedPath.reset();
			}
			view.beginShape = view.lineShape = view.endShape = null;
			Point2D p0 = p[0];
			Point2D pe = p[n - 1];
			Point2D p1 = p[1];
			Point2D p2 = p[n - 2];

			if (lineStyle == GraphConstants.STYLE_BEZIER && n > 2) {
				bezier = new Bezier(p);
				p2 = bezier.getPoint(bezier.getPointCount() - 1);
			} else if (lineStyle == GraphConstants.STYLE_SPLINE && n > 2) {
				spline = new Spline2D(p);
				double[] point = spline.getPoint(0.9875);
				// Extrapolate p2 away from the end point, pe, to avoid integer
				// rounding errors becoming too large when creating the line end
				double scaledX = pe.getX() - ((pe.getX() - point[0]) * 128);
				double scaledY = pe.getY() - ((pe.getY() - point[1]) * 128);
				p2.setLocation(scaledX, scaledY);
			}

			if (beginDeco != GraphConstants.ARROW_NONE) {
				view.beginShape = createLineEnd(beginSize, beginDeco, p1, p0);
			}
			if (endDeco != GraphConstants.ARROW_NONE) {
				view.endShape = createLineEnd(endSize, endDeco, p2, pe);
			}
			view.sharedPath.moveTo((float) p0.getX(), (float) p0.getY());
			/* THIS CODE WAS ADDED BY MARTIN KRUEGER 10/20/2003 */
			if (lineStyle == GraphConstants.STYLE_BEZIER && n > 2) {
				Point2D[] b = bezier.getPoints();
				view.sharedPath.quadTo((float) b[0].getX(),
						(float) b[0].getY(), (float) p1.getX(), (float) p1
								.getY());
				for (int i = 2; i < n - 1; i++) {
					Point2D b0 = b[2 * i - 3];
					Point2D b1 = b[2 * i - 2];
					view.sharedPath.curveTo((float) b0.getX(), (float) b0
							.getY(), (float) b1.getX(), (float) b1.getY(),
							(float) p[i].getX(), (float) p[i].getY());
				}
				view.sharedPath.quadTo((float) b[b.length - 1].getX(),
						(float) b[b.length - 1].getY(),
						(float) p[n - 1].getX(), (float) p[n - 1].getY());
			} else if (lineStyle == GraphConstants.STYLE_SPLINE && n > 2) {
				for (double t = 0; t <= 1; t += 0.0125) {
					double[] xy = spline.getPoint(t);
					view.sharedPath.lineTo((float) xy[0], (float) xy[1]);
				}
			}
			/* END */
			else {
				for (int i = 1; i < n - 1; i++)
					view.sharedPath.lineTo((float) p[i].getX(), (float) p[i]
							.getY());
				view.sharedPath.lineTo((float) pe.getX(), (float) pe.getY());
			}
			view.sharedPath.moveTo((float) pe.getX(), (float) pe.getY());
			if (view.endShape == null && view.beginShape == null) {
				// With no end decorations the line shape is the same as the
				// shared path and memory
				view.lineShape = view.sharedPath;
			} else {
				view.lineShape = (GeneralPath) view.sharedPath.clone();
				if (view.endShape != null)
					view.sharedPath.append(view.endShape, true);
				if (view.beginShape != null)
					view.sharedPath.append(view.beginShape, true);
			}
			return view.sharedPath;
		}
		return null;
	}

	/**
	 * Paint the current view's direction. Sets tmpPoint as a side-effect such
	 * that the invoking method can use it to determine the connection point to
	 * this decoration.
	 */
	protected Shape createLineEnd(int size, int style, Point2D src, Point2D dst) {
		if (src == null || dst == null)
			return null;
		int d = (int) Math.max(1, dst.distance(src));
		int ax = (int) -(size * (dst.getX() - src.getX()) / d);
		int ay = (int) -(size * (dst.getY() - src.getY()) / d);
		if (style == GraphConstants.ARROW_DIAMOND) {
			Polygon poly = new Polygon();
			poly.addPoint((int) dst.getX(), (int) dst.getY());
			poly.addPoint((int) (dst.getX() + ax / 2 + ay / 3), (int) (dst
					.getY()
					+ ay / 2 - ax / 3));
			Point2D last = (Point2D) dst.clone();
			dst.setLocation(dst.getX() + ax, dst.getY() + ay);
			poly.addPoint((int) dst.getX(), (int) dst.getY());
			poly.addPoint((int) (last.getX() + ax / 2 - ay / 3), (int) (last
					.getY()
					+ ay / 2 + ax / 3));
			return poly;

		} else if (style == GraphConstants.ARROW_TECHNICAL
				|| style == GraphConstants.ARROW_CLASSIC) {
			Polygon poly = new Polygon();
			poly.addPoint((int) dst.getX(), (int) dst.getY());
			poly.addPoint((int) (dst.getX() + ax + ay / 2), (int) (dst.getY()
					+ ay - ax / 2));
			Point2D last = (Point2D) dst.clone();
			if (style == GraphConstants.ARROW_CLASSIC) {
				dst.setLocation((int) (dst.getX() + ax * 2 / 3), (int) (dst
						.getY() + ay * 2 / 3));
				poly.addPoint((int) dst.getX(), (int) dst.getY());
			} else if (style == GraphConstants.ARROW_DIAMOND) {
				dst.setLocation(dst.getX() + 2 * ax, dst.getY() + 2 * ay);
				poly.addPoint((int) dst.getX(), (int) dst.getY());
			} else
				dst.setLocation((int) (dst.getX() + ax),
						(int) (dst.getY() + ay));
			poly.addPoint((int) (last.getX() + ax - ay / 2), (int) (last.getY()
					+ ay + ax / 2));
			return poly;

		} else if (style == GraphConstants.ARROW_SIMPLE) {
			GeneralPath path = new GeneralPath(GeneralPath.WIND_NON_ZERO, 4);
			path.moveTo((float) (dst.getX() + ax + ay / 2), (float) (dst.getY()
					+ ay - ax / 2));
			path.lineTo((float) dst.getX(), (float) dst.getY());
			path.lineTo((float) (dst.getX() + ax - ay / 2), (float) (dst.getY()
					+ ay + ax / 2));
			return path;

		} else if (style == GraphConstants.ARROW_CIRCLE) {
			Ellipse2D ellipse = new Ellipse2D.Float((float) (dst.getX() + ax
					/ 2 - size / 2), (float) (dst.getY() + ay / 2 - size / 2),
					size, size);
			dst.setLocation(dst.getX() + ax, dst.getY() + ay);
			return ellipse;

		} else if (style == GraphConstants.ARROW_LINE
				|| style == GraphConstants.ARROW_DOUBLELINE) {
			GeneralPath path = new GeneralPath(GeneralPath.WIND_NON_ZERO, 4);
			path.moveTo((float) (dst.getX() + ax / 2 + ay / 2), (float) (dst
					.getY()
					+ ay / 2 - ax / 2));
			path.lineTo((float) (dst.getX() + ax / 2 - ay / 2), (float) (dst
					.getY()
					+ ay / 2 + ax / 2));
			if (style == GraphConstants.ARROW_DOUBLELINE) {
				path.moveTo((float) (dst.getX() + ax / 3 + ay / 2),
						(float) (dst.getY() + ay / 3 - ax / 2));
				path.lineTo((float) (dst.getX() + ax / 3 - ay / 2),
						(float) (dst.getY() + ay / 3 + ax / 2));
			}
			return path;
		}
		return null;
	}

	/**
	 * @return Returns the gradientColor.
	 */
	public Color getGradientColor() {
		return gradientColor;
	}

	/**
	 * @param gradientColor
	 *            The gradientColor to set.
	 */
	public void setGradientColor(Color gradientColor) {
		this.gradientColor = gradientColor;
	}

	/**
	 * Overridden for performance reasons. See the <a
	 * href="#override">Implementation Note </a> for more information.
	 */
	public void validate() {
	}

	/**
	 * Overridden for performance reasons. See the <a
	 * href="#override">Implementation Note </a> for more information.
	 */
	public void revalidate() {
	}

	/**
	 * Overridden for performance reasons. See the <a
	 * href="#override">Implementation Note </a> for more information.
	 */
	public void repaint(long tm, int x, int y, int width, int height) {
	}

	/**
	 * Overridden for performance reasons. See the <a
	 * href="#override">Implementation Note </a> for more information.
	 */
	public void repaint(Rectangle r) {
	}

	/**
	 * Overridden for performance reasons. See the <a
	 * href="#override">Implementation Note </a> for more information.
	 */
	protected void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		// Strings get interned...
		if (propertyName == "text")
			super.firePropertyChange(propertyName, oldValue, newValue);
	}

	/**
	 * Overridden for performance reasons. See the <a
	 * href="#override">Implementation Note </a> for more information.
	 */
	public void firePropertyChange(String propertyName, byte oldValue,
			byte newValue) {
	}

	/**
	 * Overridden for performance reasons. See the <a
	 * href="#override">Implementation Note </a> for more information.
	 */
	public void firePropertyChange(String propertyName, char oldValue,
			char newValue) {
	}

	/**
	 * Overridden for performance reasons. See the <a
	 * href="#override">Implementation Note </a> for more information.
	 */
	public void firePropertyChange(String propertyName, short oldValue,
			short newValue) {
	}

	/**
	 * Overridden for performance reasons. See the <a
	 * href="#override">Implementation Note </a> for more information.
	 */
	public void firePropertyChange(String propertyName, int oldValue,
			int newValue) {
	}

	/**
	 * Overridden for performance reasons. See the <a
	 * href="#override">Implementation Note </a> for more information.
	 */
	public void firePropertyChange(String propertyName, long oldValue,
			long newValue) {
	}

	/**
	 * Overridden for performance reasons. See the <a
	 * href="#override">Implementation Note </a> for more information.
	 */
	public void firePropertyChange(String propertyName, float oldValue,
			float newValue) {
	}

	/**
	 * Overridden for performance reasons. See the <a
	 * href="#override">Implementation Note </a> for more information.
	 */
	public void firePropertyChange(String propertyName, double oldValue,
			double newValue) {
	}

	/**
	 * Overridden for performance reasons. See the <a
	 * href="#override">Implementation Note </a> for more information.
	 */
	public void firePropertyChange(String propertyName, boolean oldValue,
			boolean newValue) {
	}

}
