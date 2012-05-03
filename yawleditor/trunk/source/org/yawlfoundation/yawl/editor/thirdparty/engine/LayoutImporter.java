package org.yawlfoundation.yawl.editor.thirdparty.engine;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.GraphCell;
import org.yawlfoundation.yawl.editor.elements.model.*;
import org.yawlfoundation.yawl.editor.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.swing.YAWLEditorDesktop;
import org.yawlfoundation.yawl.editor.swing.net.YAWLEditorNetPanel;
import org.yawlfoundation.yawl.editor.foundations.ResourceLoader;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

/**
 * Author: Michael Adams
 * Creation Date: 6/10/2008
 */
public class LayoutImporter {

    public LayoutImporter() {}

    private static Namespace _ns ;
    private static NumberFormat _nbrFormatter;
    private static Locale _locale;

    private static final int SPLIT = 1;
    private static final int JOIN = 2;

    // import is a reserved word
    public static void importAndApply(SpecificationModel model, Element layout) {
        _ns = layout.getNamespace();
        setLocale(layout);

        Element spec = layout.getChild("specification", _ns);
        if (spec != null) {
            List nets = spec.getChildren("net", _ns);
            for (Object o : nets) {
                Element eNet = (Element) o;
                String id = eNet.getAttributeValue("id");
                NetGraphModel netModel = model.getNet(id);
                setNetLayout(eNet, netModel);

                netModel.getGraph().getGraphLayoutCache().reload();
            }
            String labelFontSize = spec.getChildText("labelFontSize", _ns);
            if (labelFontSize != null) {
                model.undoableSetFontSize(15, new Integer(labelFontSize));
            }
            Element eBgColor = spec.getChild("defaultBgColor", _ns);
            if (eBgColor != null) {
                String defaultBgColor = eBgColor.getAttributeValue("defaultBgColor");
                if (defaultBgColor != null) {
                    model.setDefaultNetBackgroundColor(new Integer(defaultBgColor));
                }                
            }
            Element desktopFrame = spec.getChild("size", _ns);
            if (desktopFrame != null) {
                Dimension dimension = createDimension(desktopFrame);
                YAWLEditorDesktop.getInstance().setPreferredSize(dimension);
            }
        }
    }


    private static void setNetLayout(Element e, NetGraphModel netModel) {
        NetRootSorter rootMap = new NetRootSorter(netModel.getRoots()) ;

        List containers = e.getChildren("container", _ns);
        for (Object o : containers) {
            setContainerLayout((Element) o, rootMap, netModel);
        }

        setVertexListLayout(e, rootMap, netModel);           // anon. tasks & conditions

        List flows = e.getChildren("flow", _ns);
        for (Object o : flows) {
            Element eFlow = (Element) o;
            String source = eFlow.getAttributeValue("source");
            String target = eFlow.getAttributeValue("target");
            setFlowLayout(eFlow, rootMap.getFlow(source, target));
        }

        String bgColor = e.getAttributeValue("bgColor");
        if (bgColor != null) {
            netModel.getGraph().setBackground(new Color(new Integer(bgColor)));
        }

        String bgImagePath = e.getChildText("bgImage", _ns);
        if (bgImagePath != null) {
            ImageIcon bgImage = ResourceLoader.getExternalImageAsIcon(bgImagePath);
            if (bgImage != null) {
                bgImage.setDescription(bgImagePath);   // store path
                netModel.getGraph().setBackgroundImage(bgImage);
            }    
        }

        String scale = e.getChildText("scale", _ns);
        if (scale != null) {
            netModel.getGraph().setScale(doubleFormat(scale));
        }

        String cancelTaskID = e.getChildText("cancellationtask", _ns);
        if (cancelTaskID != null) {
            YAWLTask cancelTask = (YAWLTask) rootMap.getVertex(cancelTaskID) ;
            if (cancelTask != null) {
                netModel.getGraph().changeCancellationSet(cancelTask);
            }
        }

        Rectangle frameBounds = createRectangle(e.getChild("frame", _ns));
        Rectangle viewportBounds = createRectangle(e.getChild("viewport", _ns)) ;
        Rectangle graphBounds = createRectangle(e.getChild("bounds", _ns));
        YAWLEditorNetPanel netFrame = netModel.getGraph().getFrame();

        if (viewportBounds != null) {
            Rectangle x = new Rectangle(-graphBounds.x, -graphBounds.y,
                    viewportBounds.width, viewportBounds.height);
            netFrame.getScrollPane().scrollRectToVisible(x);
           netFrame.getScrollPane().revalidate(); 
        }
    }


    private static void setVertexListLayout(Element e, NetRootSorter rootMap,
                                            NetGraphModel netModel) {
        List vertexList = e.getChildren("vertex", _ns);
        for (Object o : vertexList) {
            Element eVertex = (Element) o;
            String id = eVertex.getAttributeValue("id");
            YAWLVertex vertex = rootMap.getVertex(id);
            setVertexLayout(eVertex, vertex);

            if (vertex instanceof YAWLTask)
                removeImplicitDecorators(netModel, vertex, 0);
         }
    }


    private static void setContainerLayout(Element e, NetRootSorter rootMap,
                                           NetGraphModel netModel) {
        String id = e.getAttributeValue("id");

        Element eLabel = e.getChild("label", _ns);
        if (eLabel != null) {
            setLabelLayout(eLabel, rootMap.getLabel(id));
        }

        int explicitTypes = 0;
        List decorators = e.getChildren("decorator", _ns);
        for (Object o : decorators) {
            Element eDecorator = (Element) o;
            String type = eDecorator.getAttributeValue("type");
            Decorator decorator = rootMap.getDecorator(id, type);
            explicitTypes += getExplicitDecoratorType(decorator);
            setDecoratorLayout(eDecorator, decorator);
        }
        removeImplicitDecorators(netModel, rootMap.getVertex(id), explicitTypes);

        Element eVertex = e.getChild("vertex", _ns);
        if (eVertex != null) {
            setVertexLayout(eVertex, rootMap.getVertex(id));
        }
    }


    private static void setFlowLayout(Element e, YAWLFlowRelation flow) {
        if (flow == null) return ;
        String label = e.getChildText("label", _ns);
        if (label != null) flow.setUserObject(StringUtil.xmlDecode(label));
        adjustPorts(flow, e.getChild("ports", _ns));
        AttributeMap attributeMap = createAttributeMap(e.getChild("attributes", _ns));
        if (! attributeMap.isEmpty())
            flow.getAttributes().applyMap(attributeMap);
    }


    private static void setDecoratorLayout(Element e, Decorator decorator) {
        if (decorator == null) return;
        String position = e.getChildText("position", _ns);
        if (position != null) decorator.setCardinalPosition(new Integer(position));
        
        AttributeMap attributeMap = createAttributeMap(e.getChild("attributes", _ns));
        if (! attributeMap.isEmpty())
            decorator.getAttributes().applyMap(attributeMap);

        decorator.refreshPortLocations();
    }

    
    private static void setLabelLayout(Element e, VertexLabel label) {
        if (label == null) return;        
        Element userobject = e.getChild("userobject", _ns);
        if (userobject != null) {
            Element html = userobject.getChild("html", _ns) ;
            if (html != null) {
                html.setNamespace(null);
                label.setUserObject(JDOMUtil.elementToStringDump(html));
            }
        }
        AttributeMap attributeMap = createAttributeMap(e.getChild("attributes", _ns));
        if (! attributeMap.isEmpty())
            label.getAttributes().applyMap(attributeMap);
    }


    // tasks & conditions
    private static void setVertexLayout(Element e, YAWLVertex vertex) {
        if (vertex != null) {
            Element startpoint = e.getChild("startpoint");
            if (startpoint != null) {
                vertex.getStartPoint().setLocation(createPoint(startpoint));
            }
            String iconPath = e.getChildText("iconpath", _ns);
            if (iconPath != null) {
                vertex.setIconPath(iconPath);
            }

            AttributeMap attributeMap = createAttributeMap(e.getChild("attributes", _ns));
            if (! attributeMap.isEmpty()) {
                vertex.getAttributes().applyMap(attributeMap);
            }

            String notes = e.getChildText("notes", _ns);
            if (notes != null) vertex.setDesignNotes(StringUtil.xmlDecode(notes));
        }
    }


    private static AttributeMap createAttributeMap(Element e) {
        List attributes = e.getChildren();
        AttributeMap attributeMap = new AttributeMap();
        for (Object o : attributes) {
            Element eAttribute = (Element) o;
            if (eAttribute.getName().equals("backgroundColor")) {
                Color color = createColor(eAttribute);
                attributeMap.put("backgroundColor", color);
            }
            else if (eAttribute.getName().equals("foregroundColor")) {
                Color color = createColor(eAttribute);
                attributeMap.put("foregroundColor", color);
            }
            else if (eAttribute.getName().equals("linecolor")) {
                Color color = createColor(eAttribute);
                attributeMap.put("linecolor", color);
            }
            else if (eAttribute.getName().equals("bounds")) {
                AttributeMap.SerializableRectangle2D rect = createRectangle2D(eAttribute);
                attributeMap.put("bounds", rect);
            }
            else if (eAttribute.getName().equals("points")) {
                attributeMap.put("points", createPointsList(eAttribute));
            }
            else if (eAttribute.getName().equals("size")) {
                attributeMap.put("size", createDimension(eAttribute));
            }
            else if (eAttribute.getName().equals("labelposition")) {
                attributeMap.put("labelposition", createPoint2D(eAttribute));
            }
            else if (eAttribute.getName().equals("offset")) {
                attributeMap.put("offset", createPoint2D(eAttribute));
            }
            else if (eAttribute.getName().equals("font")) {
                attributeMap.put("font", createFont(eAttribute));
            }
            else if (eAttribute.getName().equals("lineEnd")) {
                attributeMap.put("lineEnd", new Integer(eAttribute.getText()));
            }
            else if (eAttribute.getName().equals("lineStyle")) {
                attributeMap.put("lineStyle", new Integer(eAttribute.getText()));
            }
            else putBooleanAttribute(attributeMap, eAttribute) ;
        }
        return attributeMap;
    }


    private static void adjustPorts(YAWLFlowRelation flow, Element e) {
        int inPos = new Integer(e.getAttributeValue("in"));
        int outPos = new Integer(e.getAttributeValue("out"));
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


    private static int getExplicitDecoratorType(Decorator decorator) {
        if (decorator instanceof JoinDecorator) {
            return JOIN;
        }
        else if (decorator instanceof SplitDecorator) {
            return SPLIT;
        }
        return 0;
    }

    private static void removeImplicitDecorators(NetGraphModel netModel,
                                                 YAWLVertex vertex,
                                                 int explicitTypes) {

        // only interested in tasks with at least one implicit deocrator
        if ((! (vertex instanceof YAWLTask)) || (explicitTypes == SPLIT + JOIN)) return;

        YAWLTask task = (YAWLTask) vertex;
        if ((task.hasJoinDecorator() && (explicitTypes != JOIN))) {
           netModel.setJoinDecorator(task, JoinDecorator.NO_TYPE, JoinDecorator.NOWHERE);
        }
        if ((task.hasSplitDecorator() && (explicitTypes != SPLIT))) {
          netModel.setSplitDecorator(task, SplitDecorator.NO_TYPE, SplitDecorator.NOWHERE);
        }
    }

    private static Color createColor(Element e) {
        String rgb = e.getText();
        if (rgb != null) {
            return new Color(new Integer(rgb));
        }
        else return Color.WHITE;                                            // default
    }


    private static Dimension createDimension(Element e) {
        String w = e.getAttributeValue("w");
        String h = e.getAttributeValue("h");
        return new Dimension(new Integer(w), new Integer(h));
    }


    private static Font createFont(Element e) {
        String name = e.getChildText("name", _ns);
        String style = e.getChildText("style", _ns);
        String size = e.getChildText("size", _ns);
        return new Font(name, new Integer(style), new Integer(size)) ;
    }


    private static List<Point2D.Double> createPointsList(Element e) {
        List<Point2D.Double> points = new ArrayList<Point2D.Double>();
        List values = e.getChildren();
        for (Object o : values) {
            points.add(createPoint((Element) o));
        }
        return points;
    }


    private static Point2D.Double createPoint(Element e) {
        String x = e.getAttributeValue("x");
        String y = e.getAttributeValue("y");
        return new Point2D.Double(doubleFormat(x), doubleFormat(y));
    }


    private static AttributeMap.SerializablePoint2D createPoint2D(Element e) {
        String x = e.getAttributeValue("x");
        String y = e.getAttributeValue("y");
        return new AttributeMap.SerializablePoint2D(doubleFormat(x), doubleFormat(y));
    }


    private static AttributeMap.SerializableRectangle2D createRectangle2D(Element e) {
        String x = e.getAttributeValue("x");
        String y = e.getAttributeValue("y");
        String w = e.getAttributeValue("w");
        String h = e.getAttributeValue("h");
        return new AttributeMap.SerializableRectangle2D(doubleFormat(x),
                          doubleFormat(y), doubleFormat(w), doubleFormat(h));
    }


    private static Rectangle createRectangle(Element e) {
        if (e != null) {
            int x = new Integer(e.getAttributeValue("x"));
            int y = new Integer(e.getAttributeValue("y"));
            int w = new Integer(e.getAttributeValue("w"));
            int h = new Integer(e.getAttributeValue("h"));
            return new Rectangle(x, y, w, h);
        }
        return null;
    }

    private static void putBooleanAttribute(AttributeMap map, Element e) {
        map.put(e.getName(), e.getText().equals("true"));
    }


    private static void setLocale(Element e) {
        Element eLocale = e.getChild("locale", _ns);
        if (eLocale != null) {
            String language = eLocale.getAttributeValue("language");
            String country = eLocale.getAttributeValue("country");
            _locale = new Locale(language, country);
        }
        else _locale = Locale.getDefault();

        _nbrFormatter = NumberFormat.getInstance(_locale);
    }


    // takes care of any locale issues with decimal separators
    private static Double doubleFormat(String d) {
        try {
            return _nbrFormatter.parse(d).doubleValue();
        }
        catch (Exception pe) {
            return (double) 1 ;
        }
    }

    private static void reposition(NetGraphModel netModel, YAWLVertex vertex) {
        GraphCell cell = vertex;
        AttributeMap.SerializableRectangle2D bounds =
            (AttributeMap.SerializableRectangle2D) vertex.getAttributes().get("bounds");

        if (vertex.getParent() != null)
            cell = (GraphCell) vertex.getParent();

        netModel.getGraph().moveElementTo(cell, bounds.getX(), bounds.getY());
    }


    /*******************************************************************************/

    private static class NetRootSorter {
        Map<String, YAWLFlowRelation> _flowMap = new HashMap<String, YAWLFlowRelation>();
        Map<String, YAWLVertex> _vertexMap = new HashMap<String, YAWLVertex>();
        Map<String, VertexLabel> _labelMap = new HashMap<String, VertexLabel>();
        Map<String, Decorator> _decoratorMap = new HashMap<String, Decorator>();

        public NetRootSorter(List rootList) {
            for (Object o : rootList) {
                if (o instanceof YAWLVertex) {
                    YAWLVertex vertex = (YAWLVertex) o;
                    _vertexMap.put(vertex.getEngineId(), vertex);
                }
                else if (o instanceof YAWLFlowRelation) {
                    YAWLFlowRelation flow = (YAWLFlowRelation) o;
                    String id = String.format("%s::%s",
                            getPortID((YAWLPort) flow.getSource()),
                            getPortID((YAWLPort) flow.getTarget()));
                    _flowMap.put(id, flow);
                }
                else if (o instanceof VertexContainer) {
                    VertexContainer container = (VertexContainer) o;
                    String id = getContainerID(container);
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

        // decorators are removed as they are processed so that the remainder
        // (which are all implicit) can be safely removed later
        public void removeDecorator(String taskid, String type) {
            _decoratorMap.remove(taskid + "::" + type);
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


        private String unspace(String s) { return s.replaceAll(" ", "_"); }


    } // NetRootSorter

}
