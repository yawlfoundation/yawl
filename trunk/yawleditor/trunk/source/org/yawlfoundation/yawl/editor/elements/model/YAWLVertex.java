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

package org.yawlfoundation.yawl.editor.elements.model;

import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.Edge;
import org.jgraph.graph.GraphConstants;
import org.yawlfoundation.yawl.editor.foundations.XMLUtilities;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.util.StringUtil;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public abstract class YAWLVertex extends DefaultGraphCell implements YAWLCell {

    public static final int TOP = Decorator.TOP;
    public static final int BOTTOM = Decorator.BOTTOM;
    public static final int LEFT = Decorator.LEFT;
    public static final int RIGHT = Decorator.RIGHT;
    public static final int NOWHERE = Decorator.NOWHERE;

    protected transient Point2D _startPoint;

    public static final Color DEFAULT_VERTEX_FOREGROUND = Color.BLACK;

    private Color _backgroundColor =
            SpecificationModel.getInstance().getDefaultVertexBackgroundColor();

    public static final int DEFAULT_SIZE = 32;

    private transient static final Dimension size = new Dimension(DEFAULT_SIZE, DEFAULT_SIZE);

    private EngineIdentifier _actualEngineID;
    private String _engineLabel;
    private String _iconPath;
    private String _designNotes;
    private String _documentation;

    /**
     * This constructor is ONLY to be invoked when we are reconstructing a vertex
     * from saved state. Ports will not be created with this constructor, as they
     * are already part of the JGraph state-space.
     */
    public YAWLVertex() {
        this(null, null);
    }

    /**
     * This constructor is to be invoked whenever we are creating a new vertex
     * from scratch. It also creates the correct ports needed for the vertex
     * as an intended side-effect.
     */
    public YAWLVertex(Point2D startPoint) {
        this(startPoint, null);
    }

    public YAWLVertex(Point2D startPoint, String iconPath) {
        super();
        initialize(startPoint, iconPath);
    }

    private void initialize(Point2D startPoint, String iconPath) {
        _startPoint = (startPoint != null) ? startPoint : new Point(10, 10);
        buildElementDefaults();
        setIconPath(iconPath);
        if (startPoint != null) addDefaultPorts();
    }

    public String getEngineId() {
        if (_actualEngineID == null) {
            _actualEngineID = SpecificationModel.getInstance().getUniqueIdentifier(getEngineLabel());
        }
        return XMLUtilities.toValidXMLName(_actualEngineID.toString());
    }

    public EngineIdentifier getEngineIdentifier() {
        if (_actualEngineID == null) getEngineId();   // initialise
        return _actualEngineID;
    }

    public void setEngineID(EngineIdentifier id) {
        setEngineID(id, true);
    }

    public void setEngineID(EngineIdentifier id, boolean check) {
        _actualEngineID = check ? SpecificationModel.getInstance().ensureUniqueIdentifier(id) : id;
        _engineLabel = _actualEngineID.getName();
    }

    public String getEngineLabel() {
        if (_engineLabel == null) {
            _engineLabel = getLabel();
        }
        return _engineLabel;
    }
    
    public void setEngineLabel(String label) {
        if (label != null) {
            String[] parts = label.split("_");
            if (parts.length > 1) {
                String lastPart = parts[parts.length -1];
                if (StringUtil.isIntegerString(lastPart)) {
                    _engineLabel = label.substring(0, label.lastIndexOf('_'));
                }
            }
            else _engineLabel = label;

            if (_actualEngineID != null) {
                SpecificationModel.getInstance().removeUniqueIdentifier(_actualEngineID);
                _actualEngineID = null;    // reset
            }
        }
    }

    public Point2D getStartPoint() { return _startPoint; }


    public void setIconPath(String path) { _iconPath = path; }

    public String getIconPath() { return _iconPath; }


    public void setDesignNotes(String notes) { _designNotes = notes; }

    public String getDesignNotes() { return _designNotes; }


    public void setDocumentation(String doco) { _documentation = doco; }

    public String getDocumentation() { return _documentation; }


    public void setBackgroundColor(Color color) { _backgroundColor = color; }

    public Color getBackgroundColor() { return _backgroundColor; }


    public static Dimension getVertexSize() { return size; }


    public String getToolTipText() {
        if (getEngineIdToolTipText() != null) {
            return "<html><body>" + getEngineIdToolTipText() + "</body></html>";
        }
        return null;
    }

    public String getEngineIdToolTipText() {
        String engineID = getEngineId();
        if (! StringUtil.isNullOrEmpty(engineID)) {
            return "&nbsp;<b>Engine Id:</b> " + engineID + "&nbsp;<p>";
        }
        return null;
    }


    private void buildElementDefaults() {
        Map map = new HashMap();
        GraphConstants.setBounds(map, new Rectangle2D.Double(
                _startPoint.getX(), _startPoint.getY(),
                size.getWidth(), size.getHeight()));
        GraphConstants.setOpaque(map, true);
        GraphConstants.setSizeable(map, false);
        GraphConstants.setBackground(map, _backgroundColor);
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
        HashMap map = new HashMap();
        GraphConstants.setBounds(map, new Rectangle2D.Double(x - 1, y - 1, x + 1, y + 1));
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
        for (Object o : getChildren()) {
            if (o instanceof YAWLPort) {
                YAWLPort port = (YAWLPort) o;
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
        for (Object o : getChildren()) {
            if (o instanceof YAWLPort) {
                YAWLPort port = (YAWLPort) o;
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
        return (getLabel() != null);
    }

    public boolean hasDocumentation() {
        return (getDocumentation() != null);
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

    public HashSet getFlows() {
        HashSet flows = new HashSet();
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

    public HashSet<YAWLFlowRelation> getOutgoingFlows() {
        HashSet<YAWLFlowRelation> flows = new HashSet<YAWLFlowRelation>();
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

    public HashSet<YAWLFlowRelation> getIncomingFlows() {
        HashSet<YAWLFlowRelation> flows = new HashSet<YAWLFlowRelation>();
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
        GraphConstants.setBackground(map, _backgroundColor);
        getAttributes().applyMap(map);

        clone.setEngineLabel(getEngineLabel());
        clone.setIconPath(_iconPath);
        clone.setDesignNotes(_designNotes);
        clone.setDocumentation(_documentation);
        return clone;
    }


    public String toString() {
        return "[" + this.hashCode() + "]\nengine id: " + _actualEngineID;
    }
}
