package org.yawlfoundation.yawl.editor.ui.engine;

import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;
import org.yawlfoundation.yawl.editor.core.layout.*;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.swing.YAWLEditorDesktop;
import org.yawlfoundation.yawl.elements.YSpecification;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Set;

/**
 * Author: Michael Adams
 * Creation Date: 24/08/2008
 */
public class LayoutExporter {

    private Font defaultFont;

    public LayoutExporter() {}

    public String export(SpecificationModel model) {
        YLayout layout = parse(model);
        return layout != null ? layout.toXML() : null;
    }


    public YLayout parse(SpecificationModel model) {
        YSpecification _specification = SpecificationModel.getHandler().getSpecification();
        YLayout layout = new YLayout(_specification);
        if (model.getDefaultNetBackgroundColor() != Color.WHITE.getRGB()) {
            layout.setGlobalFillColor(new Color(model.getDefaultNetBackgroundColor()));
        }
        defaultFont = getDefaultFont(model.getFontSize());
        layout.setGlobalFontSize(model.getFontSize());
        layout.setSize(getDesktopSize());

        for (NetGraphModel net : model.getNets()) {
            YNetLayout netLayout = layout.newNetLayoutInstance(unspace(net.getName()));
            layout.addNetLayout(getNetLayout(net, netLayout));
        }
        return layout;
    }


    private YNetLayout getNetLayout(NetGraphModel net, YNetLayout layout) {
        NetGraph graph = net.getGraph();
        Color bgNet = graph.getBackground();
        if (! isBlackOrWhite(bgNet, false)) {
            layout.setFillColor(bgNet);
        }
        ImageIcon bgImage = graph.getBackgroundImage();
        if (bgImage != null) {                            // desc contains path to image
            layout.setBackgroundImagePath(bgImage.getDescription());
        }

        layout.setViewport(graph.getFrame().getCurrentViewportBounds());
        layout.setBounds(graph.getBounds());
        layout.setScale(graph.getScale());

        // if the net currently has a cancellation set showing, remember it
        YAWLTask cancelTask = graph.getCancellationSetModel().getTriggeringTask();
        if (cancelTask != null) {
            layout.setCancellationTaskID(cancelTask.getEngineId());
        }

        for (Object o : net.getRoots()) {
           if (o instanceof VertexContainer) {
               addContainerLayout((VertexContainer) o, layout);
           }
           else if (o instanceof YAWLFlowRelation) {
               addFlowLayout((YAWLFlowRelation) o, layout);
           }
           else if (o instanceof YAWLVertex) {              // conditions & empty tasks
               YAWLVertex vertex = (YAWLVertex) o;
               YNetElementNode nodeLayout = getLayoutForVertex(vertex, layout,
                       vertex.getEngineId());
               addVertexLayout(vertex, nodeLayout);
               layout.addLayoutNode(nodeLayout);
           }
        }
        return layout;
    }


    private YNetElementNode getLayoutForVertex(YAWLVertex vertex, YNetLayout netLayout,
                                               String id) {
        if (vertex instanceof YAWLTask) {
            return netLayout.newTaskLayoutInstance(id);
        }
        else {
            return netLayout.newConditionLayoutInstance(id);
        }
    }


    private void addContainerLayout(VertexContainer container, YNetLayout netLayout) {
        YNetElementNode layout = getLayoutForVertex(container.getVertex(), netLayout,
                getContainerID(container));

        for (Object o : container.getChildren()) {
            if (o instanceof VertexLabel) {
                addLabelLayout((VertexLabel) o, layout);
            }
            else if (o instanceof YAWLVertex) {
                addVertexLayout((YAWLVertex) o, layout);
            }
            else if (o instanceof Decorator) {
                addDecoratorLayout((Decorator) o, (YTaskLayout) layout);
            }
        }
        netLayout.addLayoutNode(layout);
    }


    private void addFlowLayout(YAWLFlowRelation flow, YNetLayout netLayout) {
        String sourceID = getPortID((YAWLPort) flow.getSource());
        String targetID = getPortID((YAWLPort) flow.getTarget());
        YFlowLayout layout = netLayout.newFlowLayoutInstance(sourceID, targetID);

        layout.setSourcePort(getFlowPortPosition(flow, true));
        layout.setTargetPort(getFlowPortPosition(flow, false));

        String label = (String) flow.getUserObject();
        if (label != null) {
            layout.setLabel(label);
        }
        getFlowAttributes(flow, layout);
        netLayout.addFlowLayout(layout);
    }


    private void addLabelLayout(VertexLabel label, YNetElementNode layout) {
        getNodeAttributes(label, layout);
    }


    private void addVertexLayout(YAWLVertex vertex, YLayoutNode layout) {
        getNodeAttributes(vertex, layout);

        String notes = vertex.getDesignNotes();
        if ((notes != null) && (notes.length() > 0)) {
            layout.setDesignNotes(notes);
        }

        String iconPath = vertex.getIconPath();    // only tasks have icons
        if (iconPath != null) {
            ((YTaskLayout) layout).setIconPath(vertex.getIconPath());
        }
    }


    private void addDecoratorLayout(Decorator decorator, YTaskLayout taskLayout) {
        YDecoratorLayout layout = taskLayout.newDecoratorLayoutInstance();
        layout.setType(unspace(decorator.toString()));
        layout.setPosition(decorator.getCardinalPosition());
        getNodeAttributes(decorator, layout);
        taskLayout.addDecoratorLayout(layout);
    }


    private String getContainerID(VertexContainer container) {
        if (container != null) {
            for (Object o : container.getChildren()) {
                if (o instanceof YAWLVertex)
                    return ((YAWLVertex) o).getEngineId();
            }
        }
        return "null";
    }


    private String getPortID(YAWLPort port) {
        YAWLCell cell = (YAWLCell) port.getParent();
        if (cell instanceof Decorator) {
            Decorator decorator = (Decorator) cell;
            return (getContainerID((VertexContainer) decorator.getParent()));
         }
        else {
            return ((YAWLVertex) cell).getEngineId();
        }
    }


    private int getFlowPortPosition(YAWLFlowRelation flow, boolean source) {
        YAWLPort p = source ? (YAWLPort) flow.getSource() : (YAWLPort) flow.getTarget();
        YAWLPort[] ports = new YAWLPort[0];
        YAWLCell cell = (YAWLCell) p.getParent();
        if (cell instanceof Decorator) {
            ports = ((Decorator) cell).getPorts();
        }
        else if (cell instanceof YAWLVertex) {
            ports = ((YAWLVertex) cell).getPorts();
        }
        for (int i=0; i < ports.length; i++) {
            YAWLPort port = ports[i];
            Set edges = port.getEdges();
            for (Object o : edges) {
                if (o instanceof YAWLFlowRelation) {
                    YAWLFlowRelation edge = (YAWLFlowRelation) o;
                    if (edge == flow) {
                        if (port instanceof DecoratorPort)
                            return i;
                        else
                            return port.getPosition();
                    }
                }
            }
        }
        return YAWLVertex.NOWHERE;
    }


    private void getNodeAttributes(DefaultGraphCell cell, YLayoutNode layout) {
        AttributeMap map = cell.getAttributes();
        for (Object o : map.keySet()) {
            String key = (String) o;
            Object value = map.get(o);

            if (key.equals("backgroundColor") && ! isWhite((Color) value)) {
                layout.setFillColor((Color) value);
            }
            else if (key.equals("foregroundColor") && ! isBlack((Color) value)) {
                layout.setColor((Color) value);
            }
            else if (key.equals("bounds")) {
                Rectangle r = ((AttributeMap.SerializableRectangle2D) value).getBounds();
                if (cell instanceof VertexLabel) {
                    layout.setLabelBounds(r);
                }
                else {
                    layout.setBounds(r);
                }
            }
            else if (key.equals("font") && ! defaultFont.equals(value)) {
                layout.setFont((Font) value);
            }
        }
    }


    private void getFlowAttributes(DefaultGraphCell cell, YFlowLayout layout) {
        AttributeMap map = cell.getAttributes();
        for (Object o : map.keySet()) {
            String key = (String) o;
            Object value = map.get(o);

            if (key.equals("points")) {
                layout.setPoints((java.util.List<Point2D.Double>) value);
            }
            else if (key.equals("lineStyle")) {
                layout.setLineStyle(LineStyle.valueOf(new Integer(value.toString())));
            }
            else if (key.equals("offset")) {
                layout.setOffset((Point2D.Double) value);
            }
            else if (key.equals("linecolor")) {
                layout.setLineColor(new Integer(value.toString()));
            }
            else if (key.equals("labelposition")) {
                layout.setLabelPosition((Point2D.Double) value);
            }
        }
    }

    private boolean isBlack(Color color) { return isBlackOrWhite(color, true); }

    private boolean isWhite(Color color) { return isBlackOrWhite(color, false); }

    private boolean isBlackOrWhite(Color color, boolean black) {
        int hue = black ? 0 : 255;
        return (color.getRed() == hue) &&
               (color.getGreen() == hue) &&
               (color.getBlue() == hue);
    }


    private String unspace(String s) { return s.replaceAll(" ", "_"); }


    private Dimension getDesktopSize() {
        Dimension size = YAWLEditorDesktop.getInstance().getPreferredSize();
        if (size == null) size = new Dimension(800, 600);        // default
        return size;
    }


    private Font getDefaultFont(float size) {
        return UIManager.getDefaults().getFont("Label.font").deriveFont(size);
    }

}
