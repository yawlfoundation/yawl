/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.editor.ui.elements.model;

import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.Edge;
import org.jgraph.graph.GraphConstants;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;
import org.yawlfoundation.yawl.elements.YExternalNetElement;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class YAWLVertex extends DefaultGraphCell implements YAWLCell {

    public static final int TOP = Decorator.TOP;
    public static final int BOTTOM = Decorator.BOTTOM;
    public static final int LEFT = Decorator.LEFT;
    public static final int RIGHT = Decorator.RIGHT;
    public static final int NOWHERE = Decorator.NOWHERE;

    private Point2D _startPoint;

    public static final Color DEFAULT_VERTEX_FOREGROUND = Color.BLACK;
    private static final int DEFAULT_SIZE = 32;

    private static final Dimension size = new Dimension(DEFAULT_SIZE, DEFAULT_SIZE);
    private Color DEFAULT_VERTEX_BACKGROUND = UserSettings.getVertexBackgroundColour();
    private String _designNotes;

    protected YExternalNetElement _yawlElement;


    /**
     * This constructor is to be invoked whenever we are creating a new vertex
     * from scratch. It also creates the correct ports needed for the vertex
     * as an intended side-effect.
     */
    public YAWLVertex(Point2D startPoint) {
        super();
        initialize(startPoint);
    }

    private void initialize(Point2D startPoint) {
        _startPoint = (startPoint != null) ? startPoint : new Point(10, 10);
        buildElementDefaults();
        if (startPoint != null) addDefaultPorts();
    }

    public abstract String getID();

    public abstract void setID(String id);

    public abstract String getName();
    
    public abstract void setName(String name);

    public abstract void setDocumentation(String doco);

    public abstract String getDocumentation();


    public Point2D getStartPoint() { return _startPoint; }


    public void setYAWLElement(YExternalNetElement element) {
        _yawlElement = element;
    }

    public YExternalNetElement getYAWLElement() { return _yawlElement; }


    public void setDesignNotes(String notes) { _designNotes = notes; }

    public String getDesignNotes() { return _designNotes; }


    public void setBackgroundColor(Color color) {
        Map map = new HashMap();
        GraphConstants.setBackground(map, color);
        getAttributes().applyMap(map);
    }


    public Color getBackgroundColor() {
        Color bg = (Color) getAttributes().get("backgroundColor");
        return bg != null ? bg : DEFAULT_VERTEX_BACKGROUND;
    }


    public static Dimension getVertexSize() { return size; }


    public String getToolTipText() {
        return "<html><body>" + getInnerToolTipText() + "</body></html>";
    }

    public String getInnerToolTipText() {
        StringBuilder s = new StringBuilder();
        s.append("&nbsp;<b>");
        s.append(getType());
        s.append(":</b> ");
        s.append(getID());
        s.append("&nbsp;<p>");
        return s.toString();
    }


    private void buildElementDefaults() {
        Map map = new HashMap();
        GraphConstants.setBounds(map, new Rectangle2D.Double(
                _startPoint.getX(), _startPoint.getY(),
                size.getWidth(), size.getHeight()));
        GraphConstants.setOpaque(map, true);
        GraphConstants.setSizeable(map, false);
        GraphConstants.setBackground(map, DEFAULT_VERTEX_BACKGROUND);
        GraphConstants.setForeground(map, DEFAULT_VERTEX_FOREGROUND);
        GraphConstants.setEditable(map, false);
        getAttributes().applyMap(map);
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
        port.setPosition(position);
        HashMap map = new HashMap();
        GraphConstants.setBounds(map, new Rectangle2D.Double(x - 1, y - 1, x + 1, y + 1));
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
        for (Object o : getChildren()) {
            if (o instanceof YAWLPort) {
                YAWLPort port = (YAWLPort) o;
                if (port.getPosition() == position) {
                    return port;
                }
            }
        }
        return null;
    }

    public int getPositionOfIncomingFlow() {
        return getPositionOfFlow(false);
    }

    public int getPositionOfOutgoingFlow() {
        return getPositionOfFlow(true);
    }

    private int getPositionOfFlow(boolean outgoing) {
        for (Object o : getChildren()) {
            if (o instanceof YAWLPort) {
                YAWLPort port = (YAWLPort) o;
                if (port.getEdges().size() == 1) {
                    Edge edge = (Edge) port.getEdges().toArray()[0];
                    Object edgePort = outgoing ? edge.getSource() : edge.getTarget();
                    if (edgePort == port) {
                        return port.getPosition();
                    }
                }
            }
        }
        return NOWHERE;
    }

    public String getLabel() {
        VertexLabel vertexLabel = getVertexLabel();
        return vertexLabel != null ? vertexLabel.getText(): null;
    }

    public VertexLabel getVertexLabel() {
        VertexContainer container = (VertexContainer) this.getParent();
        return container != null ? container.getLabel() : null;
    }


    public boolean hasLabel() {
        return (getLabel() != null);
    }


    public void setBounds(Rectangle2D bounds) {
        Map map = new HashMap();
        GraphConstants.setBounds(map, bounds);
        getAttributes().applyMap(map);
    }

    public Rectangle2D getBounds() {
        return GraphConstants.getBounds(getAttributes());
    }

    public boolean isRemovable() { return true; }

    public boolean isCopyable() { return true; }

    public boolean generatesOutgoingFlows() { return true; }

    public boolean acceptsIncomingFlows() { return true; }


    public YAWLPort[] getPorts() {
        YAWLPort[] ports = new YAWLPort[4];
        int j = 0;
        for (Object o : getChildren()) {
            if (o instanceof YAWLPort) {
                ports[j++] = (YAWLPort) o;
            }
        }
        return ports;
    }


    public int getFlowCount() {
        int flowCount = 0;
        for (YAWLPort port : getPorts()) {
            flowCount += port.getEdges().size();
        }
        return flowCount;
    }

    public Set getFlows() {
        Set<YAWLFlowRelation> flows = new HashSet<YAWLFlowRelation>();
        for (YAWLPort port : getPorts()) {
            flows.addAll(port.getEdges());
        }
        return flows;
    }


    public void detachFlow(YAWLFlowRelation flow) {
        for (YAWLPort port : getPorts()) {
            for (Object o : port.getEdges()) {
                YAWLFlowRelation f = (YAWLFlowRelation) o;
                if (f.equals(flow)) {
                    port.getEdges().remove(f);
                    break;
                }
            }
        }
    }

    public Set<YAWLFlowRelation> getOutgoingFlows() {
        Set<YAWLFlowRelation> flows = new HashSet<YAWLFlowRelation>();
        for (YAWLPort port : getPorts()) {
            if (port != null) {
                for (Object o : port.getEdges()) {
                    YAWLFlowRelation flow = (YAWLFlowRelation) o;
                    if (flow.getSource().equals(port)) {
                        flows.add(flow);
                    }
                }
            }
        }
        return flows;
    }

    public Set<YAWLFlowRelation> getIncomingFlows() {
        Set<YAWLFlowRelation> flows = new HashSet<YAWLFlowRelation>();
        for (YAWLPort port : getPorts()) {
            if (port != null) {
                for (Object o : port.getEdges()) {
                    YAWLFlowRelation flow = (YAWLFlowRelation) o;
                    if (flow.getTarget().equals(port)) {
                        flows.add(flow);
                    }
                }
            }
        }
        return flows;
    }


    public YAWLFlowRelation getOnlyIncomingFlow() {
        Set<YAWLFlowRelation> flows = getIncomingFlows();
        return flows.size() == 1 ? flows.iterator().next() : null;
    }


    public YAWLFlowRelation getOnlyOutgoingFlow() {
        Set<YAWLFlowRelation> flows = getOutgoingFlows();
        return flows.size() == 1 ? flows.iterator().next() : null;
    }


    public abstract String getType();

    public Object clone() {
        YAWLVertex clone = (YAWLVertex) super.clone();

        Map map = new HashMap();
        GraphConstants.setForeground(map, DEFAULT_VERTEX_FOREGROUND);
        GraphConstants.setBackground(map, getBackgroundColor());
        getAttributes().applyMap(map);

        clone.setName(getName());
        clone.setDesignNotes(_designNotes);
        clone.setDocumentation(getDocumentation());
        return clone;
    }


    public String toString() {
        return "[" + this.hashCode() + "]\nengine id: " + getID();
    }
}
