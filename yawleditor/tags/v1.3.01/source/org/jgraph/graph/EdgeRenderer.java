/*
 * @(#)EdgeRenderer.java	1.0 1/1/02
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.UIManager;

import org.jgraph.JGraph;

/**
 * This renderer displays entries that implement the CellView interface
 * and supports the following attributes:
 * <li>
 * GraphConstants.POINTS
 * GraphConstants.FONT
 * GraphConstants.OPAQUE
 * GraphConstants.BORDER
 * GraphConstants.BORDERCOLOR
 * GraphConstants.LINECOLOR
 * GraphConstants.LINEWIDTH
 * GraphConstants.FOREGROUND
 * GraphConstants.BACKGROUND
 * GraphConstants.DASHPATTERN
 * GraphConstants.LINESTYLE
 * GraphConstants.START
 * GraphConstants.END
 * GraphConstants.STARTSIZE
 * GraphConstants.ENDSIZE
 * </li>
 *
 * @version 1.0 1/1/02
 * @author Gaudenz Alder
 */

public class EdgeRenderer
	extends JComponent
	implements CellViewRenderer, Serializable {
	/** Static Graphics used for Font Metrics */
	protected transient Graphics fontGraphics =
		new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).getGraphics();

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

	/** Boolean attributes of the current edgeview. Fill flags are checked
	 *  for valid decorations.
	 */
	protected transient boolean labelBorder,
		beginFill,
		endFill,
		focus,
		selected,
		preview,
		opaque;

	/** Color attributes of the current edgeview. This components foreground
	 *  is set to the edgecolor, the fontColor is in an extra variable. If
	 *  the fontColor is null, the current foreground is used.
	 *  The default background instead is used for text and is not visible
	 *  if the label is not visible or if opaque is true.
	 */
	protected transient Color borderColor,
		defaultForeground,
		defaultBackground,
		fontColor;

	/** Contains the current dash pattern. Null means no pattern. */
	protected transient float[] lineDash;

	/**
	 * Constructs a renderer that may be used to render edges.
	 */
	public EdgeRenderer() {
		defaultForeground = UIManager.getColor("Tree.textForeground");
		defaultBackground = UIManager.getColor("Tree.textBackground");
	}

	/**
     * Sets view to work with, caching necessary values
     * until the next call of this method or until some other methods with
     * explicitly specified different view
     */
     void setView(CellView value) {
         if (value instanceof EdgeView) {
             if (view != value) {
                 view = (EdgeView) value;
                 installAttributes(view);
             }
         } else {
             view = null;
         }
     }

    /**
	 * Configure and return the renderer based on the passed in
	 * components. The value is typically set from messaging the
	 * graph with <code>convertValueToString</code>.
	 *
	 * @param   graph the graph that that defines the rendering context.
	 * @param   value the object that should be rendered.
	 * @param   selected whether the object is selected.
	 * @param   hasFocus whether the object has the focus.
	 * @param   isPreview whether we are drawing a preview.
	 * @return	the component used to render the value.
	 */
	public Component getRendererComponent(
		JGraph graph,
		CellView view,
		boolean sel,
		boolean focus,
		boolean preview) {
		if (view instanceof EdgeView && graph != null) {
			this.graph = graph;
			this.focus = focus;
			this.selected = sel;
			this.preview = preview;
			setView(view);
			return this;
		}
		return null;
	}

	/**
	 * Returns true if the edge shape intersects the given rectangle.
	 */
	public boolean intersects(Graphics g, CellView value, Rectangle r) {
		if (value instanceof EdgeView && g != null && value != null) {
            setView(value);
            Graphics2D g2 = (Graphics2D) g;
            boolean hit = g2.hit(r, view.getShape(), true);
			if (hit)
				return true;
			Rectangle rect = view.getLabelBounds();
			if (rect != null)
				return rect.intersects(r);
		}
		return false;
	}

	/**
	 * Returns the bounds of the edge shape.
	 */
	public Rectangle getBounds(CellView value) {
		if (value instanceof EdgeView && value != null) {
            setView(value);
			Rectangle r = getPaintBounds(view);
			Rectangle label = getLabelBounds(view);
			if (label != null)
				r = r.union(label);
			int b = (int) Math.ceil(lineWidth);
			r.x = r.x - b;
			r.y = r.y - b;
			r.width = r.width + 2 * b;
			r.height = r.height + 2 * b;
			return r;
		}
		return null;
	}

	/**
	 * Returns the label bounds of the specified view in the given graph.
	 */
	public Rectangle getLabelBounds(EdgeView view) {
        setView(view);
		Point p = getLabelPosition(this.view);
		Dimension d = getLabelSize(this.view);
		if (p != null && d != null) {
			p.translate(-d.width / 2, -d.height / 2);
			return new Rectangle(p.x, p.y, d.width + 1, d.height + 1);
		}
		return null;
	}

	/**
	 * Returns the label position of the specified view in the given graph.
	 */
	public Point getLabelPosition(EdgeView view) {
        setView(view);
		Rectangle tmp = getPaintBounds(view);
		Point pos = view.getLabelPosition();
		int unit = GraphConstants.PERMILLE;
		Point p0 = view.getPoint(0);
		Point pe = view.getPoint(view.getPointCount()-1);
		if (pos != null && tmp != null) {
		    int x0 = tmp.x;
		    int xdir = 1;
		    if (p0.x > pe.x) {
			x0 += tmp.width;
			xdir = -1;
		    }
		    int y0 = tmp.y;
		    int ydir = 1;
		    if (p0.y > pe.y) {
			y0 += tmp.height;
			ydir = -1;
		    }
		    int x = x0 + xdir * (tmp.width * pos.x / unit);
		    int y = y0 + ydir * (tmp.height * pos.y / unit);
		    return new Point(x, y);
		}
		return null;
	}

	/**
	 * Returns the label size of the specified view in the given graph.
	 */
	public Dimension getLabelSize(EdgeView view) {
        setView(view);
		Object label = view.getGraph().convertValueToString(view);
		if (label != null && label.toString().length() > 0) {
			fontGraphics.setFont(
				GraphConstants.getFont(view.getAllAttributes()));
			metrics = fontGraphics.getFontMetrics();
			int sw = metrics.stringWidth(label.toString());
			int sh = metrics.getHeight();
			return new Dimension(sw, sh);
		}
		return null;
	}

	/**
	 * Installs the attributes of specified cell in this
	 * renderer instance. This means, retrieve every published
	 * key from the cells hashtable and set global variables
	 * or superclass properties accordingly.
	 *
	 * @param   cell to retrieve the attribute values from.
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
		lineStyle = GraphConstants.getLineStyle(map);
		lineDash = GraphConstants.getDashPattern(map);
		borderColor = GraphConstants.getBorderColor(map);
		Color foreground = GraphConstants.getLineColor(map);
		setForeground((foreground != null) ? foreground : defaultForeground);
		Color background = GraphConstants.getBackground(map);
		setBackground((background != null) ? background : defaultBackground);
		setOpaque(GraphConstants.isOpaque(map));
		setFont(GraphConstants.getFont(map));
		Color tmp = GraphConstants.getForeground(map);
		fontColor = (tmp != null) ? tmp : getForeground();
		fontGraphics.setFont(getFont());
		metrics = fontGraphics.getFontMetrics();
	}

	protected boolean isFillable(int decoration) {
		return !(
			decoration == GraphConstants.ARROW_SIMPLE
				|| decoration == GraphConstants.ARROW_LINE
				|| decoration == GraphConstants.ARROW_DOUBLELINE);
	}

	/**
	 * Returns the bounds of the edge shape without label
	 */
	public Rectangle getPaintBounds(EdgeView view) {
        setView(view);
		return view.getShape().getBounds();
	}

	/**
	 * Paint the renderer.
	 */
	public void paint(Graphics g) {
		Shape edgeShape = view.getShape();
		// Sideeffect: beginShape, lineShape, endShape
		if (edgeShape != null) {
			Graphics2D g2 = (Graphics2D) g;
			int c = BasicStroke.CAP_BUTT;
			int j = BasicStroke.JOIN_MITER;
			g2.setStroke(new BasicStroke(lineWidth, c, j));
			translateGraphics(g);
			g.setColor(getForeground());
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
				g2.setStroke(
					new BasicStroke(lineWidth, c, j, 10.0f, lineDash, 0.0f));
			if (view.lineShape != null)
				g2.draw(view.lineShape);
				
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
			if (graph.getEditingCell() != view.getCell()) {
				Object label = graph.convertValueToString(view);
				if (label != null) {
					g2.setStroke(new BasicStroke(1));
					g.setFont(getFont());
					paintLabel(g, label.toString());
				}
			}
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
	protected void paintLabel(Graphics g, String label) {
		Point p = getLabelPosition(view);
		if (p != null && label != null && label.length() > 0) {
			int sw = metrics.stringWidth(label);
			int sh = metrics.getHeight();
			if (isOpaque()) {
				g.setColor(getBackground());
				g.fillRect(p.x - sw / 2 - 1, p.y - sh / 2 - 1, sw + 2, sh + 2);
			}
			if (borderColor != null) {
				g.setColor(borderColor);
				g.drawRect(p.x - sw / 2 - 1, p.y - sh / 2 - 1, sw + 2, sh + 2);
			}
			g.setColor(fontColor);
			g.drawString(label, p.x - sw / 2, p.y + sh / 4);
		}
	}

	/**
	 * Returns the shape that represents the current edge
	 * in the context of the current graph.
	 * This method sets the global beginShape, lineShape
	 * and endShape variables as a side-effect.
	 */
	protected Shape createShape() {
            int n = view.getPointCount();
            if (n > 1) {
                // Following block may modify global vars as side effect (Flyweight Design)
                EdgeView tmp = view;
                Point[] p = new Point[n];
                for (int i = 0; i < n; i++)
                    p[i] = new Point(tmp.getPoint(i));
                // End of Side-Effect Block
                // Undo Global Side Effects
                if (view != tmp) {
                    view = tmp;
                    installAttributes(view);
                }
                // End of Undo
                if (view.sharedPath == null) {
                    view.sharedPath = new GeneralPath(GeneralPath.WIND_NON_ZERO);
                } else {
                    view.sharedPath.reset();
                }
                view.beginShape = view.lineShape = view.endShape = null;
                Point p0 = p[0];
                Point pe = p[n - 1];
                Point p1 = p[1];
                Point p2 = p[n - 2];
                if (beginDeco != GraphConstants.ARROW_NONE) {
                    view.beginShape = createLineEnd(beginSize, beginDeco, p1, p0);
                }
                if (endDeco != GraphConstants.ARROW_NONE) {
                    view.endShape = createLineEnd(endSize, endDeco, p2, pe);
                }
                view.sharedPath.moveTo(p0.x, p0.y);
                if (lineStyle == GraphConstants.STYLE_QUADRATIC && n > 2)
                    view.sharedPath.quadTo(p1.x, p1.y, pe.x, pe.y);
                else if (lineStyle == GraphConstants.STYLE_BEZIER && n > 3)
                    view.sharedPath.curveTo(p1.x, p1.y, p2.x, p2.y, pe.x, pe.y);
                else {
                    for (int i = 1; i < n - 1; i++)
                        view.sharedPath.lineTo(p[i].x, p[i].y);
                    view.sharedPath.lineTo(pe.x, pe.y);
                }
                view.sharedPath.moveTo(pe.x, pe.y);
                view.lineShape = (GeneralPath) view.sharedPath.clone();
                if (view.endShape != null)
                    view.sharedPath.append(view.endShape, true);
                if (view.beginShape != null)
                    view.sharedPath.append(view.beginShape, true);
                return view.sharedPath;
            }
            return null;
	}

	/**
	 * Paint the current view's direction. Sets tmpPoint as a side-effect
	 * such that the invoking method can use it to determine the
	 * connection point to this decoration.
	 */
	protected Shape createLineEnd(int size, int style, Point src, Point dst) {
		int d = (int) Math.max(1, dst.distance(src));
		int ax = - (size * (dst.x - src.x) / d);
		int ay = - (size * (dst.y - src.y) / d);
		if (style == GraphConstants.ARROW_DIAMOND) {
			Polygon poly = new Polygon();
			poly.addPoint(dst.x, dst.y);
			poly.addPoint(dst.x + ax / 2 + ay / 3, dst.y + ay / 2 - ax / 3);
			Point last = new Point(dst);
			dst.setLocation(dst.x + ax, dst.y + ay);
			poly.addPoint(dst.x, dst.y);
			poly.addPoint(last.x + ax / 2 - ay / 3, last.y + ay / 2 + ax / 3);
			return poly;

		} else if (
			style == GraphConstants.ARROW_TECHNICAL
				|| style == GraphConstants.ARROW_CLASSIC) {
			Polygon poly = new Polygon();
			poly.addPoint(dst.x, dst.y);
			poly.addPoint(dst.x + ax + ay / 2, dst.y + ay - ax / 2);
			Point last = new Point(dst);
			if (style == GraphConstants.ARROW_CLASSIC) {
				dst.setLocation(dst.x + ax * 2 / 3, dst.y + ay * 2 / 3);
				poly.addPoint(dst.x, dst.y);
			} else if (style == GraphConstants.ARROW_DIAMOND) {
				dst.setLocation(dst.x + 2 * ax, dst.y + 2 * ay);
				poly.addPoint(dst.x, dst.y);
			} else
				dst.setLocation(dst.x + ax, dst.y + ay);
			poly.addPoint(last.x + ax - ay / 2, last.y + ay + ax / 2);
			return poly;

		} else if (style == GraphConstants.ARROW_SIMPLE) {
			GeneralPath path = new GeneralPath(GeneralPath.WIND_NON_ZERO, 4);
			path.moveTo(dst.x + ax + ay / 2, dst.y + ay - ax / 2);
			path.lineTo(dst.x, dst.y);
			path.lineTo(dst.x + ax - ay / 2, dst.y + ay + ax / 2);
			return path;

		} else if (style == GraphConstants.ARROW_CIRCLE) {
			Ellipse2D ellipse =
				new Ellipse2D.Float(
					dst.x + ax / 2 - size / 2,
					dst.y + ay / 2 - size / 2,
					size,
					size);
			dst.setLocation(dst.x + ax, dst.y + ay);
			return ellipse;

		} else if (
			style == GraphConstants.ARROW_LINE
				|| style == GraphConstants.ARROW_DOUBLELINE) {
			GeneralPath path = new GeneralPath(GeneralPath.WIND_NON_ZERO, 4);
			path.moveTo(dst.x + ax / 2 + ay / 2, dst.y + ay / 2 - ax / 2);
			path.lineTo(dst.x + ax / 2 - ay / 2, dst.y + ay / 2 + ax / 2);
			if (style == GraphConstants.ARROW_DOUBLELINE) {
				path.moveTo(dst.x + ax / 3 + ay / 2, dst.y + ay / 3 - ax / 2);
				path.lineTo(dst.x + ax / 3 - ay / 2, dst.y + ay / 3 + ax / 2);
			}
			return path;
		}
		return null;
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	public void validate() {
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	public void revalidate() {
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	public void repaint(long tm, int x, int y, int width, int height) {
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	public void repaint(Rectangle r) {
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	protected void firePropertyChange(
		String propertyName,
		Object oldValue,
		Object newValue) {
		// Strings get interned...
		if (propertyName == "text")
			super.firePropertyChange(propertyName, oldValue, newValue);
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	public void firePropertyChange(
		String propertyName,
		byte oldValue,
		byte newValue) {
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	public void firePropertyChange(
		String propertyName,
		char oldValue,
		char newValue) {
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	public void firePropertyChange(
		String propertyName,
		short oldValue,
		short newValue) {
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	public void firePropertyChange(
		String propertyName,
		int oldValue,
		int newValue) {
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	public void firePropertyChange(
		String propertyName,
		long oldValue,
		long newValue) {
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	public void firePropertyChange(
		String propertyName,
		float oldValue,
		float newValue) {
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	public void firePropertyChange(
		String propertyName,
		double oldValue,
		double newValue) {
	}

	/**
	 * Overridden for performance reasons.
	 * See the <a href="#override">Implementation Note</a>
	 * for more information.
	 */
	public void firePropertyChange(
		String propertyName,
		boolean oldValue,
		boolean newValue) {
	}

}
