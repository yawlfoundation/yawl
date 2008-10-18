package org.yawlfoundation.yawl.editor.thirdparty.engine;

import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;
import org.yawlfoundation.yawl.editor.elements.model.*;
import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.swing.YAWLEditorDesktop;
import org.yawlfoundation.yawl.editor.swing.net.YAWLEditorNetFrame;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Set;

/**
 * Author: Michael Adams
 * Creation Date: 24/08/2008
 */
public class LayoutExporter {

    public LayoutExporter() {}

    public String export(SpecificationModel model) {
        StringBuilder xml = new StringBuilder("<layout>");
        xml.append(writeLocale());

        xml.append("<specification id=\"");
        xml.append(model.getId());
        if (model.getDefaultNetBackgroundColor() != Color.WHITE.getRGB()) {
            xml.append("\" defaultBgColor=\"");
            xml.append(String.valueOf(model.getDefaultNetBackgroundColor()));
        }
        xml.append("\">");

        xml.append(writeDesktopDimension());

        for (NetGraphModel net : model.getNets()) {
            xml.append(getNetLayout(net));
        }
        if (model.getFontSize() != 15) {
            xml.append(StringUtil.wrap(String.valueOf(model.getFontSize()),
                    "labelFontSize"));
        }
        xml.append("</specification></layout>");
        return xml.toString();
    }


    private String getNetLayout(NetGraphModel net) {
        String bgColor = "";
        Color bgNet = net.getGraph().getBackground();
        if (! isBlackOrWhite(bgNet, false)) {
            bgColor = String.format(" bgColor=\"%d\"", bgNet.getRGB());
        }
        StringBuilder xml = new StringBuilder(
                String.format("<net id=\"%s\"%s>", unspace(net.getName()), bgColor));

        xml.append(getNetFrameDimensions(net.getGraph()));

        double scale = net.getGraph().getScale();
        if (Math.abs(scale-1) > 0.01) {                      // allow for rounding error
            xml.append(StringUtil.wrap(String.format("%.3f",scale), "scale"));
        }

        for (Object o : net.getRoots()) {
           if (o instanceof VertexContainer)
               xml.append(getContainerLayout((VertexContainer) o));
           else if (o instanceof YAWLFlowRelation)
               xml.append(getFlowLayout((YAWLFlowRelation) o));
           else if (o instanceof YAWLVertex)                              // empty tasks
               xml.append(getVertexLayout((YAWLVertex) o, false));
        }

        xml.append("</net>");
        return xml.toString();
    }


    private String getContainerLayout(VertexContainer container) {
        String id = getContainerID(container);
        StringBuilder xml = new StringBuilder(String.format("<container id=\"%s\">", id));

        for (Object o : container.getChildren()) {
            if (o instanceof VertexLabel)
                xml.append(getLabelLayout((VertexLabel) o));
            else if (o instanceof YAWLVertex)
                xml.append(getVertexLayout((YAWLVertex) o, true));
            else if (o instanceof Decorator)
                xml.append(getDecoratorLayout((Decorator) o));
        }

        xml.append("</container>");
        return xml.toString();
    }


    private String getFlowLayout(YAWLFlowRelation flow) {
        StringBuilder xml = new StringBuilder(getFlowTagContents(flow));
        xml.append(">");

        String label = (String) flow.getUserObject();
        if (label != null) {
            xml.append(StringUtil.wrap(label, "label"));
        }
        xml.append(writePorts(flow));
        xml.append(writeAttributes(flow));
        xml.append("</flow>");
        return xml.toString();
    }


    private String getLabelLayout(VertexLabel label) {
        StringBuilder xml = new StringBuilder("<label>") ;
        xml.append(writeAttributes(label));
        xml.append("</label>");
        return xml.toString();
    }


    private String getVertexLayout(YAWLVertex vertex, boolean inContainer) {
        StringBuilder xml = new StringBuilder("<vertex");
        if (! inContainer) {
            xml.append(String.format(" id=\"%s\"", vertex.getEngineId()));
        }
        xml.append(">");

        String iconpath = vertex.getIconPath();
        if (iconpath != null)
            xml.append(StringUtil.wrap(iconpath, "iconpath"));

        xml.append(writeAttributes(vertex));
        xml.append("</vertex>");
        return xml.toString();
    }


    private String getDecoratorLayout(Decorator decorator) {
        StringBuilder xml = new StringBuilder(
        String.format("<decorator type=\"%s\">",
                        unspace(decorator.toString())));
        xml.append(StringUtil.wrap(String.valueOf(decorator.getCardinalPosition()),
                "position"));
        xml.append(writeAttributes(decorator));
        xml.append("</decorator>");
        return xml.toString();
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


    private String getFlowTagContents(YAWLFlowRelation flow) {
        return String.format("<flow source=\"%s\" target=\"%s\"",
                    getPortID((YAWLPort) flow.getSource()),
                    getPortID((YAWLPort) flow.getTarget()));

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

    private String getLabelID(VertexLabel label) {
        for (Enumeration sibling = label.getParent().children();
             sibling.hasMoreElements();) {
            Object o = sibling.nextElement();
            if (o instanceof YAWLVertex) {
                return ((YAWLVertex) o).getEngineId();
            }
        }
        return label.getLabel();                                // default
    }


    private String writePorts(YAWLFlowRelation flow) {
        return String.format("<ports in=\"%d\" out=\"%d\"/>",
                getFlowPortPosition(flow, true),
                getFlowPortPosition(flow, false)) ;
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


    private String writePoint(String key, AttributeMap.SerializablePoint2D point) {
        double x = point.getX(), y = point.getY();
        return String.format("<%s x=\"%1.1f\" y=\"%1.1f\"/>", key, x, y) ;
    }


    private String writeAttributes(DefaultGraphCell cell) {
        AttributeMap map = cell.getAttributes();
        StringBuilder xml = new StringBuilder("<attributes>");
        for (Object o : map.keySet()) {
            String attribute = (String) o;
            Object value = map.get(o);

            // only write non-default colours
            if (value instanceof Color) {
                xml.append(writeColorAttribute(attribute, (Color) value));
            }

            // always write bounds
            else if (value instanceof AttributeMap.SerializableRectangle2D) {
                xml.append(writeRectangle(attribute,
                        (AttributeMap.SerializableRectangle2D) value));
            }

            else if (value instanceof AttributeMap.SerializablePoint2D) {
                xml.append(writePoint(attribute,
                        (AttributeMap.SerializablePoint2D) value));
            }

            // always write size
            else if (value instanceof Dimension) {
                xml.append(writeDimension(attribute, (Dimension) value));
            }

            // always write points
            else if (value instanceof java.util.List) {
                xml.append(writeList(attribute, (java.util.List) value));
            }

            else if (attribute.equals("lineStyle")) {
                xml.append(StringUtil.wrap(map.get(o).toString(), attribute));
            }

//            else if (!((cell instanceof YAWLVertex) || (value instanceof Font))) {
//                xml.append(StringUtil.wrap(map.get(o).toString(), o.toString()));
//            }
        }
        xml.append("</attributes>");
        return xml.toString();
    }


    private String writeColorAttribute(String key, Color color) {
        if ((key.equals("backgroundColor") && isBlackOrWhite(color, false)) ||
           ((key.equals("foregroundColor") || key.equals("linecolor")) &&
                   isBlackOrWhite(color, true))) {
            return "";
        }
        else {
           return StringUtil.wrap(String.valueOf(color.getRGB()), key);
        }

//        int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
//        return String.format("<%s r=\"%d\" g=\"%d\" b=\"%d\"/>", key, r, g, b);
    }

    private boolean isBlackOrWhite(Color color, boolean black) {
        int hue = black ? 0 : 255;
        return (color.getRed() == hue) &&
               (color.getGreen() == hue) &&
               (color.getBlue() == hue);
    }


    private String writeDimension(String key, Dimension dimension) {
        Double h = dimension.getHeight();
        Double w = dimension.getWidth();
        return String.format("<%s w=\"%1.0f\" h=\"%1.0f\"/>", key, w, h);
    }


    private String writeRectangle(String key, AttributeMap.SerializableRectangle2D value) {
        double x = value.getX(), y = value.getY(),
               w = value.getWidth(), h = value.getHeight();
        return String.format("<%s x=\"%1.1f\" y=\"%1.1f\" w=\"%1.1f\" h=\"%1.1f\"/>",
                              key, x, y, w, h);
    }


    private String writeList(String key, java.util.List list) {
        if ((list != null) && (! list.isEmpty())) {
            Object o = list.get(0);
            if (o instanceof Point2D.Double) {
                return writeDoubleList(key, list);
            }
        }
        return "";
    }


    private String writeDoubleList(String key, java.util.List list) {
        StringBuilder xml = new StringBuilder(String.format("<%s>", key));
        for (Object o: list) {
            Point2D.Double value = (Point2D.Double) o;
            xml.append(String.format("<value x=\"%1.1f\" y=\"%1.1f\"/>",
                       value.getX(), value.getY()));
        }
        xml.append(String.format("</%s>", key)) ;
        return xml.toString();
    }


    private String writeFont(String key, Font font) {
        StringBuilder xml = new StringBuilder(String.format("<%s>", key));
        xml.append(StringUtil.wrap(font.getName(), "name"));
        xml.append(StringUtil.wrap(String.valueOf(font.getStyle()), "style"));
        xml.append(StringUtil.wrap(String.valueOf(font.getSize()), "size"));
        xml.append(String.format("</%s>", key)) ;
        return xml.toString();
    }


    private String unspace(String s) { return s.replaceAll(" ", "_"); }


    private String getNetFrameDimensions(NetGraph netGraph) {
        String template = "<frame x=\"%d\" y=\"%d\" w=\"%d\" h=\"%d\"/>";
        JInternalFrame[] frames = YAWLEditorDesktop.getInstance().getAllFrames();
        for (JInternalFrame frame : frames) {
            NetGraph frameGraph = ((YAWLEditorNetFrame) frame).getNet();
            if (frameGraph == netGraph) {
                Rectangle bounds = frame.getBounds();
                return String.format(template, bounds.x, bounds.y,
                          bounds.width, bounds.height);
            }
        }
        return String.format(template, 10, 10, 210, 210);                   // default
    }


    private String writeDesktopDimension() {
        String template = "<size w=\"%d\" h=\"%d\"/>";
        Dimension dimension = YAWLEditorDesktop.getInstance().getPreferredSize();
        if (dimension != null) {
            return String.format(template, dimension.width, dimension.height);
        }
        return String.format(template, 800, 600) ;                          // default
    }


    private String writeLocale() {
        Locale locale = Locale.getDefault();
        return String.format("<locale language=\"%s\" country=\"%s\"/>",
                locale.getLanguage(), locale.getCountry());
    }
    
}
