/*
 * Created on 18/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * 
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

import java.awt.Color;
import java.awt.Point;
import java.awt.Dimension;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.Edge;
import org.jgraph.graph.GraphConstants;

import au.edu.qut.yawl.editor.foundations.XMLUtilities;
import au.edu.qut.yawl.editor.specification.SpecificationModel;

public abstract class YAWLVertex extends DefaultGraphCell 
                                 implements YAWLCell {

  public static final int TOP       = Decorator.TOP;
  public static final int BOTTOM    = Decorator.BOTTOM;
  public static final int LEFT      = Decorator.LEFT;
  public static final int RIGHT     = Decorator.RIGHT;
  public static final int NOWHERE   = Decorator.NOWHERE;
  
  protected transient Point2D startPoint;
  
  public static final Color DEFAULT_VERTEX_BACKGROUND = Color.WHITE;
  public static final Color DEFAULT_VERTEX_FOREGROUND = Color.BLACK;
  
  public static final int DEFAULT_SIZE = 32;

  /* ALL yawl-specific attributes of this object and its descendants 
   * are to be stored in serializationProofAttributeMap, meaning we 
   * won't get problems with incompatible XML serializations as we add 
   * new attributes in the future. 
   */
  
  protected HashMap serializationProofAttributeMap = new HashMap();

  private transient static final Dimension size = new Dimension(DEFAULT_SIZE, DEFAULT_SIZE);

  /**
   * This constructor is ONLY to be invoked when we are reconstructing a vertex
   * from saved state. Ports will not be created with this constructor, as they
   * are already part of the JGraph state-space.
   */
  public YAWLVertex() {
    super();
    setEngineIdNumber(
        Long.toString(
            SpecificationModel.getInstance().getUniqueElementNumber()
        )
    );

    initialize(new Point(10,10), null);
  }

  /**
   * This constructor is to be invoked whenever we are creating a new vertex
   * from scratch. It also creates the correct ports needed for the vertex
   * as an intended side-effect.
   */
  public YAWLVertex(Point2D startPoint) {
    super();
    setEngineIdNumber(
        Long.toString(
            SpecificationModel.getInstance().getUniqueElementNumber()
        )
    );

    initialize(startPoint, null);
    addDefaultPorts();
  }
  
  public YAWLVertex(Point2D startPoint, String iconPath) {
    super();
    setEngineIdNumber(
        Long.toString(
            SpecificationModel.getInstance().getUniqueElementNumber()
        )
    );

    initialize(startPoint, iconPath);
    addDefaultPorts();
  }

  private void initialize(Point2D startPoint, String iconPath) {
    this.startPoint = startPoint;
    buildElementDefaults();
    setIconPath(iconPath);
  }
  
  public void setSerializationProofAttributeMap(HashMap map) {
    this.serializationProofAttributeMap = map;
  }
  
  public HashMap getSerializationProofAttributeMap() {
    return this.serializationProofAttributeMap;
  }
  
  public String getEngineId() {
    StringBuffer engineId = new StringBuffer();
    engineId.append(getEngineLabel());
    if (getEngineIdNumber() != null && !getEngineIdNumber().equals("")) {
      engineId.append("_" + getEngineIdNumber());
    }
    
    return XMLUtilities.toValidXMLName(engineId.toString());
  }
  
  public String getEngineLabel() {
    return getLabel();
  }
  
  public String getEngineIdNumber() {
    return (String) serializationProofAttributeMap.get("engineIdNumber");
  }
  
  public void setEngineIdNumber(String engineIdNumber) {
    serializationProofAttributeMap.put("engineIdNumber",engineIdNumber);
  }
  
  public void setIconPath(String iconPath) {
    serializationProofAttributeMap.put("iconPath",iconPath);
  }
  
  public String getIconPath() {
    return (String) serializationProofAttributeMap.get("iconPath");
  }

  public void setDesignNotes(String designNotes) {
    serializationProofAttributeMap.put("designNotes",designNotes);
  }
  
  public String getDesignNotes() {
    return (String) serializationProofAttributeMap.get("designNotes");
  }
  
  public String getToolTipText() {
    if (getEngineIdToolTipText() != null) {
      return "<html><body>" + getEngineIdToolTipText() + "</body></html>";
    } 
    return null;
  }
  
  public String getEngineIdToolTipText() {
    if (getEngineId() != null && !getEngineId().equals("")) {
      return "&nbsp;<b>Engine Id:</b> " + getEngineId().trim() + "&nbsp;<p>";
    }
    return null;
  }

  private void buildElementDefaults() {

    Map map = new HashMap();
    
    GraphConstants.setBounds(
        map, 
        new Rectangle2D.Double(
              startPoint.getX(),
              startPoint.getY(), 
              size.getWidth(),
              size.getHeight()
        )
    );
    GraphConstants.setOpaque(map, true);
    GraphConstants.setSizeable(map, false);
    GraphConstants.setBackground(map, Color.WHITE);
    GraphConstants.setForeground(map, Color.BLACK);
    GraphConstants.setEditable(map,false);
    
    getAttributes().applyMap(map);
  }

  public static Dimension getVertexSize() {
    return size;
  }

  protected void addDefaultPorts() {
    addDefaultLeftPort();
    addDefaultTopPort();
    addDefaultBottomPort();
    addDefaultRightPort();
  }
  
  protected void addDefaultLeftPort() {
    if (getPortAt(LEFT) == null) {
      addPort(0, GraphConstants.PERMILLE / 2, LEFT);  
    }
  }

  protected void addDefaultRightPort() {
    if (getPortAt(RIGHT) == null) {
      addPort(GraphConstants.PERMILLE, GraphConstants.PERMILLE / 2, RIGHT);
    }
  }
  
  protected void addDefaultTopPort() {
    if (getPortAt(TOP) == null) {
      addPort(GraphConstants.PERMILLE / 2, 0, TOP);  
    }
  }
  
  protected void addDefaultBottomPort() {
    if (getPortAt(BOTTOM) == null) {
      addPort(GraphConstants.PERMILLE / 2, GraphConstants.PERMILLE, BOTTOM);
    }
  }
  
  private void addPort(int x, int y, int position) {
    YAWLPort port = new YAWLPort();

    HashMap map = new HashMap();

    GraphConstants.setBounds(map, new Rectangle2D.Double(x-1,y-1,x+1,y+1));
    port.setPosition(position);
    GraphConstants.setOffset(map, new Point2D.Double(x, y));
    port.getAttributes().applyMap(map);
    
    add(port);
  }
  
  public YAWLPort getDefaultSourcePort() {
    return getPortAt(RIGHT);
  }
  
  public YAWLPort getDefaultTargetPort() {
    return getPortAt(LEFT);
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
    return null;
  }
  
  public boolean hasLabel() {
    if (getLabel() == null) {
      return false;
    }
    return true;
  }
  
  public void setBounds(Rectangle2D bounds) {
    Map map = new HashMap();
    GraphConstants.setBounds(map, bounds);
    getAttributes().applyMap(map);
  }

  public Rectangle2D getBounds() {
    return GraphConstants.getBounds(getAttributes());
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
  
  public YAWLPort[] getPorts() {
    YAWLPort[] ports = new YAWLPort[4];
    Object[] children = this.getChildren().toArray();
    
    int j = 0;
    for(int i = 0; i < children.length; i++) {
      if (children[i] instanceof YAWLPort) {
        ports[j++] = (YAWLPort) children[i];
      }
    }
    return ports;
  }

  
  public int getFlowCount() {
    int flowCount = 0;
    for(int i = 0; i <= 3; i++) {
      flowCount += getPorts()[i].getEdges().size();
    }
    return flowCount;
  }
  
  public HashSet getFlows() {
    HashSet flows = new HashSet();
    
    for(int i = 0; i <= 3; i++) {
      flows.addAll(getPorts()[i].getEdges());
    }

    return flows;
  }
  
  public HashSet<YAWLFlowRelation> getOutgoingFlows() {
    HashSet<YAWLFlowRelation> flows = new HashSet<YAWLFlowRelation>();
    
    for(int i = 0; i <= 3; i++) {
      if (getPorts()[i] != null && 
          getPorts()[i].getEdges().size() > 0) {
        Iterator edgeIterator = getPorts()[i].getEdges().iterator();
        while(edgeIterator.hasNext()) {
          YAWLFlowRelation flow = (YAWLFlowRelation) edgeIterator.next();
          if (flow.getSource().equals(getPorts()[i])) {
            flows.add(flow);
          }
        }
      }
    }
    return flows;
  }

  public HashSet<YAWLFlowRelation> getIncomingFlows() {
    HashSet<YAWLFlowRelation> flows = new HashSet<YAWLFlowRelation>();
    
    for(int i = 0; i <= 3; i++) {
      if (getPorts()[i] != null && 
          getPorts()[i].getEdges().size() > 0) {
        Iterator edgeIterator = getPorts()[i].getEdges().iterator();
        while(edgeIterator.hasNext()) {
          YAWLFlowRelation flow = (YAWLFlowRelation) edgeIterator.next();
          if (flow.getTarget().equals(getPorts()[i])) {
            flows.add(flow);
          }
        }
      }
    }
    return flows;
  }

  
  public YAWLFlowRelation getOnlyIncomingFlow() {
    HashSet flows = getIncomingFlows();
    if (flows.size() == 1) {
      return (YAWLFlowRelation) flows.iterator().next();
    }
    return null;

  }

  public YAWLFlowRelation getOnlyOutgoingFlow() {
    HashSet flows = getOutgoingFlows();
    if (flows.size() == 1) {
      return (YAWLFlowRelation) flows.iterator().next();
    }
    return null;
  }

  public abstract String getType();
  
  public Object clone() {
    YAWLVertex clone = (YAWLVertex) super.clone();

    Map map = new HashMap();
    
    GraphConstants.setForeground(map, DEFAULT_VERTEX_FOREGROUND);
    GraphConstants.setBackground(map, DEFAULT_VERTEX_BACKGROUND);

    getAttributes().applyMap(map);
    
    clone.setSerializationProofAttributeMap(
      (HashMap) getSerializationProofAttributeMap().clone()    
    );
    
    return clone;
  }
  
  public String toString() {
    return  "["+ this.hashCode() +"]\nengine id: "+ getEngineId();
  }
}
