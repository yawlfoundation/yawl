/*
 * Created on 18/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2003 Queensland University of Technology
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package au.edu.qut.yawl.editor.elements.model;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.awt.Color;

import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.Edge;
import org.jgraph.graph.GraphConstants;

public abstract class YAWLVertex extends DefaultGraphCell 
                                 implements YAWLCell {

  public static final int TOP       = Decorator.TOP;
  public static final int BOTTOM    = Decorator.BOTTOM;
  public static final int LEFT      = Decorator.LEFT;
  public static final int RIGHT     = Decorator.RIGHT;
  public static final int NOWHERE   = -2;
  
  protected transient Point2D startPoint;
  
  public static final Color DEFAULT_VERTEX_BACKGROUND = Color.WHITE;
  public static final Color DEFAULT_VERTEX_FOREGROUND = Color.BLACK;

  private transient static final Dimension size = new Dimension(32, 32);
  
  public YAWLVertex() {
    super();
  }

  public YAWLVertex(Point2D startPoint) {
    super();
    initialize(startPoint);
  }

  private void initialize(Point2D startPoint) {
    this.startPoint = startPoint;
    buildElementDefaults();
    buildElement();
  }

  private void buildElementDefaults() {
    Map map = GraphConstants.createMap();
    GraphConstants.setBounds(map, new Rectangle((Point) startPoint, size));
    GraphConstants.setOpaque(map, true);
    GraphConstants.setSizeable(map, true);
    GraphConstants.setBackground(map, Color.WHITE);
    GraphConstants.setForeground(map, Color.BLACK);
    changeAttributes(map);
  }

  public static Dimension getIconSize() {
    return size;
  }

  protected void addDefaultPorts() {
    addPort(0, GraphConstants.PERMILLE / 2, LEFT);  
    addPort(GraphConstants.PERMILLE / 2, 0, TOP);  
    addPort(GraphConstants.PERMILLE / 2, GraphConstants.PERMILLE, BOTTOM);
    addPort(GraphConstants.PERMILLE, GraphConstants.PERMILLE / 2, RIGHT);
  }
  
  protected void addPort(int x, int y, int position) {
    Map map = GraphConstants.createMap();
    GraphConstants.setOffset(map, new Point(x,y));
    GraphConstants.setBounds(map, new Rectangle(x-1,y-1,x+1,y+1));
    YAWLPort port = new YAWLPort();
    port.setPosition(position);
    port.changeAttributes(map);
    add(port);
  }
  
  public YAWLPort getPortAt(int position) {
    List children = this.getChildren();
    for (int i = 0; i < children.size(); i++) {
      if (children.get(i) instanceof YAWLPort) {
        YAWLPort port = (YAWLPort) children.get(i);
        if (port.getPosition() == position) {
          return port;
        }
      }
    }
    return null;
  }
  
  public int getPositionOfIncommingFlow() {
    List children = this.getChildren();
    for (int i = 0; i < children.size(); i++) {
      if (children.get(i) instanceof YAWLPort) {
        YAWLPort port = (YAWLPort) children.get(i);
        if (port.getEdges().size() == 1) {
          Edge edge = (Edge) port.getEdges().toArray()[0];
          if (edge.getTarget() == port) {
            return port.getPosition();
          }
        }
      }
    }
    return NOWHERE;    
  }

  public int getPositionOfOutgoingFlow() {
    List children = this.getChildren();
    for (int i = 0; i < children.size(); i++) {
      if (children.get(i) instanceof YAWLPort) {
        YAWLPort port = (YAWLPort) children.get(i);
        if (port.getEdges().size() == 1) {
          Edge edge = (Edge) port.getEdges().toArray()[0];
          if (edge.getSource() == port) {
            return port.getPosition();
          }
        }
      }
    }
    return NOWHERE;    
  }

  public String getLabel() {
    VertexContainer container = (VertexContainer) this.getParent();
    if (container != null && container.getLabel() != null) {
      return container.getLabel().getLabel();
    }
    return "";
  }
  
  public void setBounds(Rectangle bounds) {
    Map map = GraphConstants.createMap();
    GraphConstants.setBounds(map, bounds);
    changeAttributes(map);
  }

  public Rectangle getBounds() {
    Map map = this.getAttributes();
    return GraphConstants.getBounds(map);
  }
  
  public boolean isRemovable() {
    return true; 
  }
  
  public boolean isCopyable() {
    return true; 
  }
  
  public boolean generatesOutgoingFlows() {
    return true;
  }
  
  public boolean acceptsIncommingFlows() {
    return true; 
  }
  
  protected void buildElement() {
    addDefaultPorts();
  }
  
  public abstract String getType();
  
  public Object clone() {
    YAWLVertex clone = (YAWLVertex) super.clone();

    Map map = GraphConstants.createMap();
    
    GraphConstants.setForeground(map, DEFAULT_VERTEX_FOREGROUND);
    GraphConstants.setBackground(map, DEFAULT_VERTEX_BACKGROUND);

    clone.changeAttributes(map);
    
    return clone;
  }
}
