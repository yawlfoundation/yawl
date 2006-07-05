/*
 * Created on 12/12/2003
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

import org.jgraph.graph.DefaultGraphCell;

import org.jgraph.graph.GraphConstants;

import java.util.Map;
import java.util.HashSet;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Point;

import javax.swing.ImageIcon;
import au.edu.qut.yawl.editor.foundations.ResourceLoader;

public abstract class Decorator extends DefaultGraphCell 
                                implements YAWLCell {

  public static final int NO_TYPE   = -1;
  public static final int AND_TYPE  = 0;
  public static final int OR_TYPE   = 1;
  public static final int XOR_TYPE  = 2;

  public static final int TOP       = 10;
  public static final int BOTTOM    = 11;
  public static final int LEFT      = 12;
  public static final int RIGHT     = 13;
  public static final int NOWHERE   = 14;
  
  public static final int LONG_EDGE_PORT = 15;
  public static final int SHORT_EDGE_PORT = 16;

  private static final int DEFAULT_PORT = 2;
  
  private int cardinalPosition;
  private int type;
  
  private YAWLTask task;
  
  public static final int PORT_NUMBER = 5;
  
  private DecoratorPort[] ports = new DecoratorPort[PORT_NUMBER];

  public Decorator() {
    initialize(null, NO_TYPE, NOWHERE);
  }
  
  public Decorator(YAWLTask task, int type, int position) {
    initialize(task, type, position);
  }
  
  private void initialize(YAWLTask task, int type, int position) {
    setType(type);
    setCardinalPosition(position);
    setTask(task);
    if (task != null) {
      buildElement();
      addPorts();
    }
  }
  
  public void setTask(YAWLTask task) {
    this.task = task;
  }
  
  public YAWLTask getTask() {
    return this.task;
  }

  private void buildElement() {
    Map map = GraphConstants.createMap();
    GraphConstants.setSize(map, getSizeRelativeToTask());
    GraphConstants.setBounds(map, 
      new Rectangle(getLocationRelativeToTask(), getSizeRelativeToTask()));
    GraphConstants.setOpaque(map, true);
    GraphConstants.setSizeable(map, true);
    GraphConstants.setForeground(map, GraphConstants.getForeground(task.getAttributes()));
    GraphConstants.setBackground(map, GraphConstants.getBackground(task.getAttributes()));
    changeAttributes(map);
  }

  public Dimension getSizeRelativeToTask() {
    switch(cardinalPosition) {
      case TOP: case BOTTOM: {
        return new Dimension(task.getBounds().width,
                             Math.round(task.getBounds().height/3));
      }
      case LEFT: case RIGHT: {
        return new Dimension(Math.round(task.getBounds().width/3),
                             task.getBounds().height);
      }
    }
    return null;
  }
  
  public Point getLocationRelativeToTask() {
    switch(cardinalPosition) {
      case TOP: {
        Point startPoint = task.getLocation();
        startPoint.translate(0,-1 * getSizeRelativeToTask().height + 1);
        return startPoint;
      }
      case BOTTOM: {
        Point startPoint = task.getLocation();
        startPoint.translate(0,task.getBounds().height - 1);
        return startPoint;
      }
      case LEFT: {
        Point startPoint = task.getLocation();
        startPoint.translate(-1 * getSizeRelativeToTask().width + 1,0);
        return startPoint;
      }
      case RIGHT: {
        Point startPoint = task.getLocation();
        startPoint.translate(task.getBounds().width - 1,0);
        return startPoint;
      }
      default: {
        return new Point(0,0);
      }
    }
  }
  
  public void setCardinalPosition(int position) {
    assert position >= TOP && position <= RIGHT : "Invalid position supplied";
    this.cardinalPosition = position;
  }
  
  public int getCardinalPosition() {
    return cardinalPosition; 
  }
  
  public int getType() {
    return type;
  }
  
  public void setType(int type) {
    assert type >= AND_TYPE && type <= XOR_TYPE: "Invalid type supplied";
    this.type = type;
  }
   
  public void addPorts() {
    this.ports = new DecoratorPort[5];
    for(int i = 0; i < PORT_NUMBER; i++) {
      ports[i] = new DecoratorPort();
    }
    bindPorts();
  }
  
  private void bindPorts() {
    for(int i = 0; i < PORT_NUMBER; i++) {
      add(ports[i]);
      setPortLocation(ports[i], getLocationFor(i));
    }
  }
  
  public DecoratorPort[] getPorts() {
    return this.ports;
  }
  
  public void setPorts(DecoratorPort[] ports) {
    this.ports = ports;
  }
  
  private Point getLocationFor(int portNumber) {
    switch(getCardinalPosition()) {
      case TOP: {
        switch(portNumber) {
          case 0: {
            return new Point(0, GraphConstants.PERMILLE/2);  
          }
          case 1: {
            return new Point(GraphConstants.PERMILLE / 4, 0);  
          }
          case 2: {
            return new Point(GraphConstants.PERMILLE / 2, 0);  
          }
          case 3: {
            return new Point(GraphConstants.PERMILLE / 4 * 3, 0); 
          }
          case 4: {
            return new Point(GraphConstants.PERMILLE,   
                             GraphConstants.PERMILLE/2);              
          }
        }
        break;
      }
      case BOTTOM: {
        switch(portNumber) {
          case 0: {
            return new Point(GraphConstants.PERMILLE,   
                             GraphConstants.PERMILLE/2);              
          }
          case 1: {
            return new Point(GraphConstants.PERMILLE / 4 * 3, 
                             GraphConstants.PERMILLE);
          }
          case 2: {
            return new Point(GraphConstants.PERMILLE / 2, 
                             GraphConstants.PERMILLE);
          }
          case 3: {
            return new Point(GraphConstants.PERMILLE / 4, 
                             GraphConstants.PERMILLE);
          }
          case 4: {
            return new Point(0, GraphConstants.PERMILLE/2);  
          }
        }
        break;  
      }
      case LEFT: {
        switch(portNumber) {
          case 0: {
            return new Point(GraphConstants.PERMILLE/2, 
                             GraphConstants.PERMILLE);              
          }
          case 1: {
            return new Point(0, GraphConstants.PERMILLE / 4); 
          }
          case 2: {
            return new Point(0, GraphConstants.PERMILLE / 2); 
          }
          case 3: {
            return new Point(0, GraphConstants.PERMILLE / 4 * 3); 
          }
          case 4: {
            return new Point(GraphConstants.PERMILLE/2, 0);  
          }
        }
        break;
      }
      case RIGHT: {
        switch(portNumber) {
          case 0: {
            return new Point(GraphConstants.PERMILLE/2, 0); 
          }
          case 1: {
            return new Point(GraphConstants.PERMILLE, 
                             GraphConstants.PERMILLE / 4); 
          }
          case 2: {
            return new Point(GraphConstants.PERMILLE, 
                             GraphConstants.PERMILLE / 2); 
          }
          case 3: {
            return new Point(GraphConstants.PERMILLE, 
                             GraphConstants.PERMILLE / 4 * 3); 
          }
          case 4: {
            return new Point(GraphConstants.PERMILLE/2,  
                             GraphConstants.PERMILLE);              
          }
        }
        break;
      }
    }
    return new Point(0,0);
  }


  protected void setPortLocation(DecoratorPort port, Point point) {
    Map map = GraphConstants.createMap();
    GraphConstants.setOffset(map, point);
    GraphConstants.setBounds(map, new Rectangle(point.x-1,point.y-1,
                                                point.x+2,point.y+2));
    port.changeAttributes(map);
  }
 
 
  protected static ImageIcon getIconByName(String iconName) {
    return ResourceLoader.getImageAsIcon("/au/edu/qut/yawl/editor/resources/elements/decorators/" 
           + iconName + ".gif");
  }
  
  public boolean isRemovable() {
    return true;
  }
  
  public boolean isCopyable() {
    return true;
  }

  public DecoratorPort getPortAtIndex(int index) {
    return ports[index];
  }
  
  public boolean isLongEdgePort(DecoratorPort port) {
    for(int i = 0; i <= 4; i++) {
      if (ports[i].equals(port)) {
        if (i == 0 || i == 4) {
          return false;
        }
        return true;
      }
    }
    return false;
  }
  
  public int getFlowCount() {
    int flowCount = 0;
    for(int i = 0; i <= 4; i++) {
      flowCount += ports[i].getEdges().size();
    }
    return flowCount;
  }
  
  public HashSet getFlows() {
    HashSet flows = new HashSet();
    
    for(int i = 0; i <= 4; i++) {
      flows.addAll(ports[i].getEdges());
    }

    return flows;
  }
  
  public DecoratorPort getPortWithOnlyFlow() {
    for(int i = 0; i <= 4; i++) {
      if (ports[i].getEdges().size() > 0) {
       return ports[i];
      }        
    }
    return null;
  }
  
  public DecoratorPort getDefaultPort() {
    return ports[DEFAULT_PORT]; 
  }
  
  public YAWLFlowRelation getOnlyFlow(){
     if(getPortWithOnlyFlow() != null) {
      return (YAWLFlowRelation)
        (getPortWithOnlyFlow().getEdges().toArray())[0];
    }
    return null;
  }
}
