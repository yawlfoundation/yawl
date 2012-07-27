/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.core.layout;

import org.yawlfoundation.yawl.elements.YExternalNetElement;
import org.yawlfoundation.yawl.util.XNode;

import java.awt.*;
import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Store the layout information for a particular flow
 *
 * @author Michael Adams
 * @date 7/06/12
 */
public class YFlowLayout extends YLayoutNode {

    private YExternalNetElement _source;
    private YExternalNetElement _target;
    private int _sourcePort;
    private int _targetPort;
    private LineStyle _style;
    private Point2D.Double _offset;
    private java.util.List<Point2D.Double> _points;
    private String _label;
    private Point2D.Double _labelPosition;
    private Color _lineColor;


    /**
     * Creates a YFlowLayout object
     * @param source the source task/condition for this flow
     * @param target the target task/condition for this flow
     * @param formatter a number format for a specific locale
     */
    public YFlowLayout(YExternalNetElement source, YExternalNetElement target,
                       NumberFormat formatter) {
        _source = source;
        _target = target;
        _points = new ArrayList<Point2D.Double>();
        setNumberFormatter(formatter);
    }


    /**
     * Gets the id of this flow, for indexing purposes
     * @return a synthetic id, comprising ["source id" + "::" + "target id"]
     */
    public String getID() {
        return _source.getID() + "::" + _target.getID();
    }


    public YExternalNetElement getSource() { return _source; }

    public  void setSource(YExternalNetElement source) { _source = source; }


    public YExternalNetElement getTarget() { return _target; }

    public  void setTarget(YExternalNetElement target) { _target = target; }


    public int getSourcePort() { return _sourcePort; }

    public void setSourcePort(int sourcePort) { _sourcePort = sourcePort; }


    public int getTargetPort() { return _targetPort; }

    public void setTargetPort(int targetPort) { _targetPort = targetPort; }


    public LineStyle getLineStyle() { return _style; }

    public void setLineStyle(LineStyle style) { _style = style; }


    public Point2D.Double getOffset() { return _offset; }

    public void setOffset(Point2D.Double offset) { _offset = offset; }


    public java.util.List<Point2D.Double> getPoints() { return _points; }

    public void setPoints(java.util.List<Point2D.Double> points) { _points = points; }

    public boolean hasPoints() { return ! _points.isEmpty(); }


    public String getLabel() { return _label; }

    public void setLabel(String label) { _label = label; }

    public boolean hasLabel() { return _label != null; }


    public Point2D.Double getLabelPosition() { return _labelPosition; }

    public void setLabelPosition(Point2D.Double labelPosition) {
        _labelPosition = labelPosition;
    }


    public Color getLineColor() { return _lineColor; }

    public void setLineColor(Color color) { _lineColor = color; }

    public void setLineColor(int rgb) { _lineColor = new Color(rgb); }


    /**********************************************************************/

    /**
     * Parses the layout content for this flow, and populates its layout tree. This
     * method is called from this flow's parent YNetLayout object.
     * @param node the XML node describing this net's layout
     */
    protected void parse(XNode node) {
        parseLabel(node.getChild("label"));
        parsePorts(node.getChild("ports"));
        parseAttributes(node.getChild("attributes"));
        parseLineColor(node.getChild("attributes"));
    }


    /**
     * Creates an XNode representation of this flow's layout, to generate its XML
     * representation. This method is called from this flow's parent YNetLayout object.
     * @return a populated XNode object
     */
    protected XNode toXNode() {
        XNode node = new XNode("flow");
        node.addAttribute("source", _source.getID());
        node.addAttribute("target", _target.getID());
        if (hasLabel()) node.addChild("label", _label, true);
        node.addChild(getPortsNode());
        node.addChild(getAttributesNode());
        return node;
    }



    private void parseLabel(XNode labelNode) {
        if (labelNode != null) {
            _label = labelNode.getText(true);
        }
    }

    private void parsePorts(XNode portsNode) {
        if (portsNode != null) {
            _sourcePort = YLayoutUtil.strToInt(portsNode.getAttributeValue("in"));
            _targetPort = YLayoutUtil.strToInt(portsNode.getAttributeValue("out"));
        }
        else {
            _sourcePort = 0;
            _targetPort = 0;
        }
    }


    @Override
    protected void parseAttributes(XNode attributeNode) {
        if (attributeNode != null) {
            parseOffset(attributeNode.getChild("offset"));
            parseLabelPosition(attributeNode.getChild("labelposition"));
            parseLineStyle(attributeNode.getChild("lineStyle"));
            parsePoints(attributeNode.getChild("points"));
        }
    }


    @Override
    protected XNode getAttributesNode() {
        XNode node = new XNode("attributes");
        if (_offset != null) node.addChild(getOffsetNode());
        if (_labelPosition != null) node.addChild(getLabelPositionNode());
        node.addChild("lineStyle", _style.getCardinality());
        if (hasPoints()) node.addChild(getPointsNode());
        if (_lineColor != null) node.addChild("lineColor", _lineColor.getRGB());
        return node;
    }


    private void parseLabelPosition(XNode labelPositionNode) {
        if (labelPositionNode != null) {
            _labelPosition = YLayoutUtil.parsePosition(labelPositionNode,
                    getNumberFormatter());
        }
    }


    private void parseLineStyle(XNode styleNode) {
        _style = (styleNode == null) ? LineStyle.Orthogonal :
                 LineStyle.valueOf(YLayoutUtil.strToInt(styleNode.getText()));
    }


    private void parsePoints(XNode pointsNode) {
        if (pointsNode != null) {
            for (XNode pointNode : pointsNode.getChildren()) {
                _points.add(YLayoutUtil.parsePosition(pointNode, getNumberFormatter()));
            }
        }
    }


    private void parseLineColor(XNode attributeNode) {
        String linecolor = attributeNode.getChildText("linecolor");
        if (linecolor != null) setLineColor(YLayoutUtil.strToInt(linecolor));
    }


    private void parseOffset(XNode offsetNode) {
        if (offsetNode != null) {
            _offset = YLayoutUtil.parsePosition(offsetNode, getNumberFormatter());
        }
    }


    private XNode getPortsNode() {
        XNode node = new XNode("ports");
        node.addAttribute("in", _sourcePort);
        node.addAttribute("out", _targetPort);
        return node;
    }


    private XNode getPointsNode() {
        XNode node = new XNode("points");
        for (Point2D.Double point : _points) {
            node.addChild(YLayoutUtil.toPointNode(point, "value"));
        }
        return node;
    }


    private XNode getOffsetNode() {
        return YLayoutUtil.toPointNode(_offset, "offset");
    }


    private XNode getLabelPositionNode() {
        return YLayoutUtil.toPointNode(_labelPosition, "labelposition");
    }

}
