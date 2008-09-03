package org.yawlfoundation.yawl.editor.thirdparty.engine;

import org.jgraph.graph.AttributeMap;
import org.yawlfoundation.yawl.editor.elements.model.*;
import org.yawlfoundation.yawl.editor.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Author: Michael Adams
 * Creation Date: 24/08/2008
 */
public class LayoutExporter {

    public LayoutExporter() {}

    public String export(SpecificationModel model) {
        StringBuilder xml = new StringBuilder("<layout>");

        for (NetGraphModel net : model.getNets()) {
            xml.append(getNetLayout(net));
        }

        xml.append("</layout>");
        return JDOMUtil.formatXMLString(xml.toString());
    }


    private String getNetLayout(NetGraphModel net) {
        StringBuilder xml = new StringBuilder(
                String.format("<net id=\"%s\">", net.getName()));

        for (Object o : net.getRoots()) {
           if (o instanceof YAWLCondition)
               xml.append(getConditionLayout((YAWLCondition) o));
           else if (o instanceof VertexContainer)
               xml.append(getContainerLayout((VertexContainer) o));
           else if (o instanceof YAWLFlowRelation)
               xml.append(getFlowLayout((YAWLFlowRelation) o));
        }

        xml.append("</net>");
        return xml.toString();
    }


    private String getConditionLayout(YAWLCondition condition) {
        StringBuilder xml = new StringBuilder(
                String.format("<condition id=\"%s\">", condition.getEngineId()));

        xml.append(writeStartPoint(condition.getStartPoint()));
        xml.append(StringUtil.wrap(condition.getIconPath(), "iconpath"));
        xml.append(writeAttributes(condition.getAttributes()));

        xml.append("</condition>");
        return xml.toString();

    }


    private String getContainerLayout(VertexContainer container) {
        StringBuilder xml = new StringBuilder(
                String.format("<container id=\"%s\">", "dummy"));

        for (Object o : container.getChildren()) {
            if (o instanceof VertexLabel)
                xml.append(getLabelLayout((VertexLabel) o));
            else if (o instanceof YAWLTask)
                xml.append(getTaskLayout((YAWLTask) o));
        }

        xml.append("</container>");
        return xml.toString();

    }


    private String getFlowLayout(YAWLFlowRelation flow) {
        StringBuilder xml = new StringBuilder(
                String.format("<flow id=\"%s\">", flow.getTargetLabel()));

        xml.append(writeAttributes(flow.getAttributes()));

        xml.append("</flow>");
        return xml.toString();

    }


    private String getLabelLayout(VertexLabel label) {
        StringBuilder xml = new StringBuilder(
                String.format("<label id=\"%s\">", label.getLabel()));


        xml.append(StringUtil.wrap((String) label.getUserObject(), "userobject"));
        xml.append(writeAttributes(label.getAttributes()));

        xml.append("</label>");
        return xml.toString();

    }


    private String getTaskLayout(YAWLTask task) {
        StringBuilder xml = new StringBuilder(
                String.format("<task id=\"%s\">", task.getLabel()));

        xml.append(writeStartPoint(task.getStartPoint()));
        xml.append(StringUtil.wrap(task.getIconPath(), "iconpath"));
        xml.append(writeAttributes(task.getAttributes()));

        xml.append("</task>");
        return xml.toString();

    }


    private String writeStartPoint(Point2D point) {
        double x = point.getX(), y = point.getY();
        return String.format("<startpoint x=\"%1.1f\" y=\"%1.1f\"/>", x, y) ;
    }


    private String writeAttributes(AttributeMap map) {
        StringBuilder xml = new StringBuilder("<attributes>");
        for (Object o : map.keySet()) {
            String attribute = (String) o;
            Object value = map.get(o);
            if (value instanceof Color) {
                xml.append(writeColorAttribute(attribute, (Color) value));
            }
            else if (value instanceof AttributeMap.SerializableRectangle2D) {
                xml.append(writeRectangle(attribute,
                        (AttributeMap.SerializableRectangle2D) value));
            }
            else if (value instanceof java.util.List) {
                xml.append(writeList(attribute, (java.util.List) value));
            }
            else if (value instanceof Font) {
                xml.append(writeFont(attribute, (Font) value));
            }
            else
                xml.append(StringUtil.wrap(map.get(o).toString(), o.toString()));
        }
        xml.append("</attributes>");
        return xml.toString();
    }


    private String writeColorAttribute(String key, Color color) {
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
        return String.format("<%s r=\"%d\" g=\"%d\" b=\"%d\"/>", key, r, g, b);
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
        xml.append(StringUtil.wrap(font.getFamily(), "family"));
        xml.append(StringUtil.wrap(font.getName(), "name"));
        xml.append(StringUtil.wrap(String.valueOf(font.getStyle()), "style"));
        xml.append(StringUtil.wrap(String.valueOf(font.getSize()), "size"));
        xml.append(String.format("</%s>", key)) ;
        return xml.toString();
    }


}
