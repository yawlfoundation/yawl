package com.nexusbpm.editor.editors.net.cells;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgraph.JGraph;
import org.jgraph.cellview.JGraphMultilineView;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.GraphCellEditor;
import org.jgraph.graph.VertexView;

import com.nexusbpm.editor.editors.net.renderer.DefaultRenderer;

public class DefaultView extends VertexView implements PortHighlightable {

  private static final Log LOG = LogFactory.getLog(DefaultView.class);

  private static final int DEFAULT_WIDTH = 50;
  private static final int DEFAULT_HEIGHT = 50;
  
  
  private static DefaultRenderer renderer = new DefaultRenderer();

  private MyMultiLinedEditor editor2 = new MyMultiLinedEditor();

  public DefaultView(Object cell) {
    super(cell);
  }

  public GraphCellEditor getEditor() {
    return editor2;
  }
  
  private boolean isMouseOver;

  /**
   * @return Returns the isMouseOver.
   */
  public boolean isMouseOver() {
    return isMouseOver;
  }

  /**
   * @param isMouseOver
   *          The isMouseOver to set.
   */
  public void setMouseOver(boolean isMouseOver) {
    this.isMouseOver = isMouseOver;
  }


  private boolean isMouseOverPort;

  /**
   * @return Returns the isMouseOver.
   */
  public boolean isMouseOverPort() {
    return isMouseOverPort;
  }

  /**
   * @param isMouseOver
   *          The isMouseOver to set.
   */
  public void setMouseOverPort(boolean isMouseOverPort) {
    this.isMouseOverPort = isMouseOverPort;
  }

  public Point2D getPerimeterPoint(Point2D source, Point2D p) {
	    Rectangle2D bounds = this.getBounds();
	    Rectangle2D iconOffsetBounds = renderer.getNameRenderer().getBounds();

    double x = bounds.getX() + iconOffsetBounds.getX();
    double y = bounds.getY() + iconOffsetBounds.getY();
    double a = DEFAULT_WIDTH / 2;
    double b = DEFAULT_HEIGHT / 2;
    double eccentricity = Math.sqrt(1 - ((b / a) * (b / a)));

    double width = bounds.getWidth();
    double height = renderer.getNameRenderer().getPreferredSize().getHeight();

    double xCenter = (x + (width / 2));
    double yCenter = (y + (height / 2));
    double dx = p.getX() - xCenter;
    double dy = p.getY() - yCenter;

    double theta = Math.atan2(dy, dx);
    double eSquared = eccentricity * eccentricity;
    double rPrime = a * Math.sqrt((1 - eSquared) / (1 - (eSquared * (Math.cos(theta) * Math.cos(theta)))));

    double ex = rPrime * Math.cos(theta);
    double ey = rPrime * Math.sin(theta);
    
    return new Point2D.Double(ex + xCenter, ey + yCenter);
  }

  public CellViewRenderer getRenderer() {
    return renderer;
  }

  
  class MyMultiLinedEditor extends JGraphMultilineView.MultiLinedEditor {
    /*
     * (non-Javadoc)
     * 
     * @see org.jgraph.graph.GraphCellEditor#getGraphCellEditorComponent(org.jgraph.JGraph,
     *      java.lang.Object, boolean)
     */
    public Component getGraphCellEditorComponent(JGraph graph, Object cell, boolean isSelected) {
      Component component = super.getGraphCellEditorComponent(graph, cell, isSelected);
      Dimension dim = ((DefaultRenderer) DefaultView.this.getRendererComponent(graph, false, false, false))
          .getNameRenderer().getPreferredSize();
      offsetY = (int) dim.getHeight();
      return component;
    }
  }
}