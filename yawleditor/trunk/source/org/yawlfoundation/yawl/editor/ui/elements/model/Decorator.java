/*
 * Created on 12/12/2003
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

package org.yawlfoundation.yawl.editor.ui.elements.model;

import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public abstract class Decorator extends DefaultGraphCell
        implements YAWLCell {

    public static final int NO_TYPE = -1;
    public static final int AND_TYPE = 0;
    public static final int OR_TYPE = 1;
    public static final int XOR_TYPE = 2;

    public static final int TOP = 10;
    public static final int BOTTOM = 11;
    public static final int LEFT = 12;
    public static final int RIGHT = 13;
    public static final int NOWHERE = 14;

    public static final int LONG_EDGE_PORT = 15;
    public static final int SHORT_EDGE_PORT = 16;

    private static final int DEFAULT_PORT = 2;
    public static final int PORT_NUMBER = 5;

    private YAWLTask _task;
    private int _cardinalPosition;
    private int _type;

    /**
     * This constructor is ONLY to be invoked when we are reconstructing a decorator
     * from saved state. Ports will not be created with this constructor, as they
     * are already part of the JGraph state-space.
     */

    public Decorator() {
        initialize(null, NO_TYPE, NOWHERE);
    }

    /**
     * This constructor is to be invoked whenever we are creating a new decorator
     * from scratch. It also creates the correct ports needed for the decorator
     * as an intended side-effect.
     */

    public Decorator(YAWLTask task, int type, int position) {
        initialize(task, type, position);
        if (task != null) {
            bindPorts();
        }
    }


    private void initialize(YAWLTask task, int type, int position) {
        setType(type);
        setCardinalPosition(position);
        setTask(task);
        if (task != null) {
            buildElement();
        }
    }

    public void setTask(YAWLTask task) { _task = task; }

    public YAWLTask getTask() { return _task; }

    private void buildElement() {
        Map map = new HashMap();

        GraphConstants.setSize(map, getSizeRelativeToTask());
        GraphConstants.setBounds(map,
                new Rectangle2D.Double(
                        getLocationRelativeToTask().getX(),
                        getLocationRelativeToTask().getY(),
                        getSizeRelativeToTask().getWidth(),
                        getSizeRelativeToTask().getHeight()
                )
        );
        GraphConstants.setOpaque(map, true);
        GraphConstants.setSizeable(map, false);

        GraphConstants.setForeground(map, GraphConstants.getForeground(getTask().getAttributes()));
        GraphConstants.setBackground(map, GraphConstants.getBackground(getTask().getAttributes()));

        getAttributes().applyMap(map);
    }

    public Dimension getSizeRelativeToTask() {
        switch (getCardinalPosition()) {
            case TOP:
            case BOTTOM: {
                return new Dimension((int) getTask().getBounds().getWidth(),
                        (int) Math.round(getTask().getBounds().getHeight() / 3));
            }
            case LEFT:
            case RIGHT: {
                return new Dimension((int) Math.round(getTask().getBounds().getWidth() / 3),
                        (int) getTask().getBounds().getHeight());
            }
        }
        return null;
    }

    public Point2D getLocationRelativeToTask() {
        switch (getCardinalPosition()) {
            case TOP: {
                Point2D startPoint = getTask().getLocation();
                return new Point2D.Double(
                        startPoint.getX(),
                        startPoint.getY() - getSizeRelativeToTask().getHeight() + 1
                );
            }
            case BOTTOM: {
                Point2D startPoint = getTask().getLocation();
                return new Point2D.Double(
                        startPoint.getX(),
                        startPoint.getY() + getTask().getBounds().getHeight() - 1
                );
            }
            case LEFT: {
                Point2D startPoint = getTask().getLocation();
                return new Point2D.Double(
                        startPoint.getX() - getSizeRelativeToTask().getWidth() + 1,
                        startPoint.getY()
                );
            }
            case RIGHT: {
                Point2D startPoint = getTask().getLocation();
                return new Point2D.Double(
                        startPoint.getX() + getTask().getBounds().getWidth() - 1,
                        startPoint.getY()
                );
            }
            default: {
                return new Point2D.Double(0, 0);
            }
        }
    }

    public void setCardinalPosition(int position) {
        assert position >= TOP && position <= RIGHT : "Invalid position supplied";
        _cardinalPosition = position;
    }

    public int getCardinalPosition() {
        return _cardinalPosition;
    }

    public int getType() {
        return _type;
    }

    public void setType(int type) {
        assert type >= AND_TYPE && type <= XOR_TYPE : "Invalid type supplied";
        _type = type;
    }

    private void bindPorts() {
        for (int i = 0; i < PORT_NUMBER; i++) {
            add(new DecoratorPort());
            setPortLocation(getPorts()[i], getLocationFor(i));
        }
    }

    public void refreshPortLocations() {
        DecoratorPort[] ports = getPorts();
        for (int i = 0; i < ports.length; i++) {
            setPortLocation(getPorts()[i], getLocationFor(i));
        }
    }

    public DecoratorPort[] getPorts() {
        DecoratorPort[] ports = new DecoratorPort[5];
        Object[] children = this.getChildren().toArray();

        int j = 0;
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof DecoratorPort) {
                ports[j++] = (DecoratorPort) children[i];
            }
        }

        return ports;
    }

    private Point2D getLocationFor(int portNumber) {
        switch (getCardinalPosition()) {
            case TOP: {
                switch (portNumber) {
                    case 0: {
                        return new Point2D.Double(0, GraphConstants.PERMILLE / 2);
                    }
                    case 1: {
                        return new Point2D.Double(GraphConstants.PERMILLE / 4, 0);
                    }
                    case 2: {
                        return new Point2D.Double(GraphConstants.PERMILLE / 2, 0);
                    }
                    case 3: {
                        return new Point2D.Double(GraphConstants.PERMILLE / 4 * 3, 0);
                    }
                    case 4: {
                        return new Point2D.Double(GraphConstants.PERMILLE,
                                GraphConstants.PERMILLE / 2);
                    }
                }
                break;
            }
            case BOTTOM: {
                switch (portNumber) {
                    case 0: {
                        return new Point2D.Double(GraphConstants.PERMILLE,
                                GraphConstants.PERMILLE / 2);
                    }
                    case 1: {
                        return new Point2D.Double(GraphConstants.PERMILLE / 4 * 3,
                                GraphConstants.PERMILLE);
                    }
                    case 2: {
                        return new Point2D.Double(GraphConstants.PERMILLE / 2,
                                GraphConstants.PERMILLE);
                    }
                    case 3: {
                        return new Point2D.Double(GraphConstants.PERMILLE / 4,
                                GraphConstants.PERMILLE);
                    }
                    case 4: {
                        return new Point2D.Double(0, GraphConstants.PERMILLE / 2);
                    }
                }
                break;
            }
            case LEFT: {
                switch (portNumber) {
                    case 0: {
                        return new Point2D.Double(GraphConstants.PERMILLE / 2,
                                GraphConstants.PERMILLE);
                    }
                    case 1: {
                        return new Point2D.Double(0, GraphConstants.PERMILLE / 4);
                    }
                    case 2: {
                        return new Point2D.Double(0, GraphConstants.PERMILLE / 2);
                    }
                    case 3: {
                        return new Point2D.Double(0, GraphConstants.PERMILLE / 4 * 3);
                    }
                    case 4: {
                        return new Point2D.Double(GraphConstants.PERMILLE / 2, 0);
                    }
                }
                break;
            }
            case RIGHT: {
                switch (portNumber) {
                    case 0: {
                        return new Point2D.Double(GraphConstants.PERMILLE / 2, 0);
                    }
                    case 1: {
                        return new Point2D.Double(GraphConstants.PERMILLE,
                                GraphConstants.PERMILLE / 4);
                    }
                    case 2: {
                        return new Point2D.Double(GraphConstants.PERMILLE,
                                GraphConstants.PERMILLE / 2);
                    }
                    case 3: {
                        return new Point2D.Double(GraphConstants.PERMILLE,
                                GraphConstants.PERMILLE / 4 * 3);
                    }
                    case 4: {
                        return new Point2D.Double(GraphConstants.PERMILLE / 2,
                                GraphConstants.PERMILLE);
                    }
                }
                break;
            }
        }
        return new Point2D.Double(0, 0);
    }

    protected void setPortLocation(DecoratorPort port, Point2D point) {
        Map map = new HashMap();
        GraphConstants.setOffset(map, point);
        GraphConstants.setBounds(map,
                new Rectangle2D.Double(
                        point.getX() - 1,
                        point.getY() - 1,
                        point.getX() + 2,
                        point.getY() + 2
                )
        );
        port.getAttributes().applyMap(map);
    }


    protected static ImageIcon getIconByName(String iconName) {
        return ResourceLoader.getImageAsIcon(
                "/org/yawlfoundation/yawl/editor/ui/resources/elements/decorators/"
                + iconName + ".gif");
    }

    public boolean isRemovable() {
        return true;
    }

    public boolean isCopyable() {
        return true;
    }

    public DecoratorPort getPortAtIndex(int index) {
        return getPorts()[index];
    }

    public boolean isLongEdgePort(DecoratorPort port) {
        for (int i = 0; i <= 4; i++) {
            if (getPorts()[i].equals(port)) {
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
        for (int i = 0; i <= 4; i++) {
            flowCount += getPorts()[i].getEdges().size();
        }
        return flowCount;
    }

    public HashSet<YAWLFlowRelation> getFlows() {
        HashSet<YAWLFlowRelation> flows = new HashSet<YAWLFlowRelation>();
        for (DecoratorPort port : getPorts()) {
            for (Object flowAsObject : port.getEdges()) {
                flows.add((YAWLFlowRelation) flowAsObject);
            }
        }

        return flows;
    }


    public boolean detachFlow(YAWLFlowRelation flow) {
        for (DecoratorPort port : getPorts()) {
            for (Object flowAsObject : port.getEdges()) {
                if (flow.equals(flowAsObject)) {
                    return port.removeEdge(flow);
                }
            }
        }
        return false;
    }


    public DecoratorPort getPortWithOnlyFlow() {
        for (int i = 0; i <= 4; i++) {
            if (getPorts()[i].getEdges().size() > 0) {
                return getPorts()[i];
            }
        }
        return null;
    }

    public DecoratorPort getDefaultPort() {
        return getPorts()[DEFAULT_PORT];
    }

    public YAWLFlowRelation getOnlyFlow() {
        if (getPortWithOnlyFlow() != null) {
            return (YAWLFlowRelation)
                    (getPortWithOnlyFlow().getEdges().toArray())[0];
        }
        return null;
    }
}
