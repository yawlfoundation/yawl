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

package org.yawlfoundation.yawl.editor.ui.specification.io;

import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.GraphConstants;
import org.yawlfoundation.yawl.editor.core.layout.*;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetUtilities;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.swing.net.YAWLEditorNetPanel;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Michael Adams
 * Creation Date: 6/10/2008
 */
public class LayoutImporter {

    private LayoutImporter() {}

    public static void importAndApply(YLayout layout) {
        for (YNetLayout netLayout : layout.getNetLayouts().values()) {
            NetGraphModel netModel = SpecificationModel.getNets().get(netLayout.getID());
            setNetLayout(netModel, netLayout);
            netModel.getGraph().getGraphLayoutCache().reload();
        }

        Color defaultBgColor = layout.getGlobalFillColor();
        if (defaultBgColor != null) {
            UserSettings.setNetBackgroundColour(defaultBgColor);
        }
        Dimension size = layout.getSize();
        if (size != null) {
            YAWLEditor.getNetsPane().setPreferredSize(size);
        }
    }


    public static void setNetLayout(NetGraphModel netModel, YNetLayout netLayout) {
        NetRootSorter rootMap = new NetRootSorter(netModel.getRoots());
        NetGraph graph = netModel.getGraph();

        for (YLayoutNode layoutNode : netLayout.getLayoutNodes()) {
            setNetElementLayout(netModel, rootMap, layoutNode);
        }

        for (YFlowLayout flowLayout : netLayout.getFlows()) {
            setFlowLayout(flowLayout, rootMap);
        }

        graph.setScale(netLayout.getScale());
        graph.setBackground(netLayout.getFillColor());
        ImageIcon bgImage = netLayout.getBackgroundImage();
        if (bgImage != null) graph.setBackgroundImage(bgImage);

        YTask cancelYTask = netLayout.getCancellationTask();
        if (cancelYTask != null) {
            YAWLTask cancelTask = (YAWLTask) rootMap.getVertex(cancelYTask.getID());
            if (cancelTask != null) {
                graph.changeCancellationSet(cancelTask);
            }
        }

        Rectangle viewportBounds = netLayout.getViewport();
        if (viewportBounds != null) {
            Rectangle graphBounds = netLayout.getBounds();
            YAWLEditorNetPanel netFrame = graph.getFrame();
            Rectangle rect = new Rectangle(-graphBounds.x, -graphBounds.y,
                    viewportBounds.width, viewportBounds.height);
            netFrame.getScrollPane().scrollRectToVisible(rect);
            netFrame.getScrollPane().revalidate();
        }
    }


    private static void setNetElementLayout(NetGraphModel netModel, NetRootSorter rootMap,
                                            YLayoutNode layoutNode) {
        if (layoutNode.isContainer()) {
            setContainerLayout(netModel, rootMap, layoutNode);
        }
        else {
            setVertexLayout(netModel, rootMap, layoutNode);
        }
    }


    private static void setContainerLayout(NetGraphModel netModel, NetRootSorter rootMap,
                                           YLayoutNode layoutNode) {
        String id = layoutNode.getID();

        // do label
        setLabelLayout(layoutNode, rootMap.getLabel(id));

        // do decorators (task only)
        if (layoutNode instanceof YTaskLayout) {
            YTaskLayout taskLayout = (YTaskLayout) layoutNode;
            for (YDecoratorLayout decLayout : taskLayout.getDecorators()) {
                Decorator decorator = rootMap.getDecorator(id, decLayout.getType().toString());
                setDecoratorLayout(decLayout, decorator);
            }
            removeImplicitDecorators(netModel, rootMap.getVertex(id), taskLayout);
        }

        // do vertex
        setVertexLayout(netModel, rootMap, layoutNode);
    }


    private static void setLabelLayout(YLayoutNode layout, VertexLabel label) {
        if (label != null) {
            AttributeMap updateMap = getLabelAttributes(layout);
            Rectangle loadedBounds = (Rectangle) updateMap.get(GraphConstants.BOUNDS);
            Rectangle2D.Double currentBounds = (Rectangle2D.Double)
                    label.getAttributes().get(GraphConstants.BOUNDS);

            // At this point, the label has already been added to the net, so has been
            // given the correct height and width for its font, but in a default location.
            // If we use the same bounds as the last save, the label may be truncated.
            // So, we use the saved location, but the current height & width.
            if (! (loadedBounds == null || currentBounds == null)) {
                updateMap.put(GraphConstants.BOUNDS, new Rectangle2D.Double(
                        loadedBounds.getX(),
                        loadedBounds.getY(),
                        currentBounds.getWidth(),
                        currentBounds.getHeight()));
            }
            label.getAttributes().applyMap(updateMap);
        }
    }


    // tasks & conditions
    private static void setVertexLayout(NetGraphModel netModel, NetRootSorter rootMap,
                                                YLayoutNode layoutNode) {
        YAWLVertex vertex = rootMap.getVertex(layoutNode.getID());
        if (vertex != null) {
            vertex.getAttributes().applyMap(getAttributes(layoutNode));
            if (layoutNode instanceof YTaskLayout) {
                YTaskLayout taskLayout = (YTaskLayout) layoutNode;
                ((YAWLTask) vertex).setIconPath(taskLayout.getIconPath());
                removeImplicitDecorators(netModel, vertex, taskLayout);
            }
            if (layoutNode.getDesignNotes() != null) {
                vertex.setDesignNotes(layoutNode.getDesignNotes());
            }
        }
    }


    private static void setFlowLayout(YFlowLayout flowLayout, NetRootSorter rootMap) {
        YAWLFlowRelation flow = rootMap.getFlow(flowLayout.getSource().getID(),
                flowLayout.getTarget().getID());
        if (flow == null) return ;

        String label = flowLayout.getLabel();
        if (label != null) flow.setUserObject(StringUtil.xmlDecode(label));
        adjustPorts(flow, flowLayout);
        flow.getAttributes().applyMap(getFlowAttributes(flowLayout));
    }


    private static void setDecoratorLayout(YDecoratorLayout layout, Decorator decorator) {
        if (decorator == null) return;
        decorator.setCardinalPosition(layout.getPosition().getCardinality());
        decorator.getAttributes().applyMap(getAttributes(layout));
        decorator.refreshPortLocations();
    }


    private static AttributeMap getAttributes(YLayoutNode layout) {
        AttributeMap attributeMap = new AttributeMap();
        attributeMap.put("backgroundColor", layout.getFillColor());
        attributeMap.put("foregroundColor", layout.getColor());
        attributeMap.put("bounds", layout.getBounds());
        if (layout.getFont() != null) {
            attributeMap.put("font", layout.getFont());
        }
        if (layout instanceof YDecoratorLayout) {
            attributeMap.put("size", new Dimension(layout.getBounds().getSize()));
        }
        return attributeMap;
    }


    private static AttributeMap getFlowAttributes(YFlowLayout layout) {
        AttributeMap attributeMap = new AttributeMap();
        attributeMap.put("lineStyle", layout.getLineStyle().getCardinality());
        if (layout.getLineColor() != null) {
            attributeMap.put("linecolor", layout.getLineColor());
        }
        if (layout.hasPoints()) {
            attributeMap.put("points", layout.getPoints());
        }
        if (layout.getLabelPosition() != null) {
            attributeMap.put("labelposition", layout.getLabelPosition());
        }
        if (layout.getOffset() != null) {
            attributeMap.put("offset", layout.getOffset());
        }
        return attributeMap;
    }


    private static AttributeMap getLabelAttributes(YLayoutNode layout) {
        AttributeMap attributeMap = new AttributeMap();
        if (layout.getLabelBounds() != null) {
            attributeMap.put("bounds", layout.getLabelBounds());
        }
        if (layout.getLabelColor() != null) {
            attributeMap.put("foregroundColor", layout.getLabelColor());
        }
        if (layout.getFont() != null) {
            attributeMap.put("font", layout.getFont());
        }
        return attributeMap;
    }


    private static void adjustPorts(YAWLFlowRelation flow, YFlowLayout flowLayout) {
        int inPos = flowLayout.getSourcePort();
        int outPos = flowLayout.getTargetPort();
        YAWLPort port ;
        if (! ((inPos == YAWLVertex.RIGHT) || (inPos == YAWLVertex.NOWHERE))) {
            port = setPortPosition(flow, (YAWLPort) flow.getSource(), inPos);
            flow.setSource(port);
        }
        if (! ((outPos == YAWLVertex.LEFT) || (outPos == YAWLVertex.NOWHERE))) {
            port = setPortPosition(flow, (YAWLPort) flow.getTarget(), outPos);
            flow.setTarget(port);
        }
    }


    private static YAWLPort setPortPosition(YAWLFlowRelation flow, YAWLPort oldPort, int pos) {
        YAWLPort newPort = null;
        if (oldPort != null) {
            YAWLCell cell = (YAWLCell) oldPort.getParent();
            if (cell instanceof Decorator) {
                newPort = ((Decorator) cell).getPortAtIndex(pos);
            }
            else {
                newPort = ((YAWLVertex) cell).getPortAt(pos);
            }
            if (newPort != null) {
                oldPort.removeEdge(flow);
                newPort.addEdge(flow);
            }
        }
        return newPort;
    }


    private static void removeImplicitDecorators(NetGraphModel netModel,
                                                 YAWLVertex vertex,
                                                 YTaskLayout layout) {
        YAWLTask task = (YAWLTask) vertex;
        if (task == null) return;
        if ((task.hasJoinDecorator() && (! layout.hasJoinLayout()))) {
            netModel.setJoinDecorator(task, JoinDecorator.NO_TYPE, JoinDecorator.NOWHERE);
        }
        if ((task.hasSplitDecorator() && (! layout.hasSplitLayout()))) {
            netModel.setSplitDecorator(task, SplitDecorator.NO_TYPE, SplitDecorator.NOWHERE);
        }
    }


    /*******************************************************************************/

    private static class NetRootSorter {
        final Map<String, YAWLFlowRelation> _flowMap = new HashMap<String, YAWLFlowRelation>();
        final Map<String, YAWLVertex> _vertexMap = new HashMap<String, YAWLVertex>();
        final Map<String, VertexLabel> _labelMap = new HashMap<String, VertexLabel>();
        final Map<String, Decorator> _decoratorMap = new HashMap<String, Decorator>();

        public NetRootSorter(List rootList) {
            for (Object o : rootList) {
                if (o instanceof YAWLVertex) {
                    YAWLVertex vertex = (YAWLVertex) o;
                    _vertexMap.put(vertex.getID(), vertex);
                }
                else if (o instanceof YAWLFlowRelation) {
                    YAWLFlowRelation flow = (YAWLFlowRelation) o;
                    String id = String.format("%s::%s",
                            NetUtilities.getPortID((YAWLPort) flow.getSource()),
                            NetUtilities.getPortID((YAWLPort) flow.getTarget()));
                    _flowMap.put(id, flow);
                }
                else if (o instanceof VertexContainer) {
                    VertexContainer container = (VertexContainer) o;
                    String id = NetUtilities.getContainerID(container);
                    for (Object child : container.getChildren()) {
                        if (child instanceof VertexLabel) {
                            VertexLabel label = (VertexLabel) child;
                            _labelMap.put(id, label);
                        }
                        else if (child instanceof YAWLVertex) {
                            YAWLVertex vertex = (YAWLVertex) child;
                            _vertexMap.put(id, vertex);
                        }
                        else if (child instanceof Decorator) {
                            Decorator decorator = (Decorator) child;
                            String decID = String.format("%s::%s", id,
                                            unspace(decorator.toString()));
                            _decoratorMap.put(decID, decorator);
                        }
                    }
                }
            }
        }


        public YAWLVertex getVertex(String id) {
            if (id == null) return null;
            return _vertexMap.get(id);
        }

        public YAWLFlowRelation getFlow(String source, String target) {
            if ((source == null) || (target == null)) return null;
            return _flowMap.get(source + "::" + target);
        }

        public Decorator getDecorator(String taskid, String type) {
            if ((taskid == null) || (type == null)) return null;
            return _decoratorMap.get(taskid + "::" + type);
        }

        public VertexLabel getLabel(String id) {
            if (id == null) return null;
            return _labelMap.get(id);
        }

        private String unspace(String s) { return s.replaceAll(" ", "_"); }

    } // NetRootSorter

}
