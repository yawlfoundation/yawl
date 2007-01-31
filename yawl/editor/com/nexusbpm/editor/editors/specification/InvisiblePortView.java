/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.editors.specification;

import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.PortRenderer;
import org.jgraph.graph.PortView;

/**
 * @author Dean Mao
 * @created March 28, 2003
 */
public class InvisiblePortView extends PortView {
  protected static InvisiblePortRenderer renderer = new InvisiblePortRenderer();

  public static int SMALL_PORT_WIDTH = 27;

  public static int SMALL_PORT_HEIGHT = 27;

  public InvisiblePortView(Object cell) {
    super(cell);
  }
  
  public Rectangle2D getBounds() {
    Rectangle2D parentBounds = getParentView().getBounds();
    double x = parentBounds.getX() + ((parentBounds.getWidth() - SMALL_PORT_WIDTH) / 2);
    double y = parentBounds.getY() + 20;
    double width = SMALL_PORT_WIDTH;
    double height = SMALL_PORT_HEIGHT;
    return new Rectangle2D.Double(x, y, width, height);
  }

  public CellViewRenderer getRenderer() {
    return renderer;
  }

  public Point2D getLocation(EdgeView edge) {
    if (edge == null)
    return new Point2D.Double(this.getBounds().getCenterX(), this.getBounds().getCenterY());
    else
      return super.getLocation(edge);
  }

  public static class InvisiblePortRenderer extends PortRenderer {
    public void paint(Graphics g) {
      // null implementation (ie, don't paint anything!!)
    }
  }

}

