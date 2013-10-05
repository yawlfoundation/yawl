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

import org.yawlfoundation.yawl.util.XNode;

import java.awt.*;
import java.text.NumberFormat;

/**
 * The abstract base class of all layout nodes (tasks, conditions, flows, decorators).
 *
 * @author Michael Adams
 * @date 7/06/12
 */
public abstract class YLayoutNode {

    private String _id;
    private Rectangle _bounds;
    private Rectangle _labelBounds;
    private Color _color;
    private Color _labelColor;
    private Color _fillColor;
    private String _designNotes;
    private Font _font;
    private NumberFormat _nbrFormatter;


    // only extended within this package
    protected YLayoutNode() { }


    public String getID() { return _id; }

    public void setID(String id) { _id = id; }


    public Rectangle getBounds() {
        return _bounds != null ? _bounds : YLayoutUtil.DEFAULT_RECTANGLE;
    }

    public void setBounds(Rectangle bounds) { _bounds = bounds; }


    public Rectangle getLabelBounds() {
        return _labelBounds != null ? _labelBounds : YLayoutUtil.DEFAULT_RECTANGLE;
    }

    public void setLabelBounds(Rectangle bounds) { _labelBounds = bounds; }

    protected boolean hasLabel() { return _labelBounds != null; }


    public Color getColor() {
        return _color != null ? _color : YLayout.DEFAULT_COLOR;
    }

    public void setColor(Color color) { _color = color; }

    public void setColor(int rgb) { _color = new Color(rgb); }


    public Color getLabelColor() {
        return _labelColor != null ? _labelColor : YLayout.DEFAULT_COLOR;
    }

    public void setLabelColor(Color color) { _labelColor = color; }

    public void setLabelColor(int rgb) { _labelColor = new Color(rgb); }


    public Color getFillColor() {
        return _fillColor != null ? _fillColor : YLayout.DEFAULT_FILL_COLOR;
    }

    public void setFillColor(Color color) { _fillColor = color; }

    public void setFillColor(int rgb) { _fillColor = new Color(rgb); }


    public Point getLocation() { return getBounds().getLocation(); }

    public Point getLabelLocation() { return getLabelBounds().getLocation(); }


    public Font getFont() { return _font; }

    public void setFont(Font font) { _font = font; }


    public Dimension getSize() { return getBounds().getSize(); }

    public Dimension getLabelSize() { return getLabelBounds().getSize(); }


    public String getDesignNotes() { return _designNotes; }

    public void setDesignNotes(String notes) { _designNotes = notes; }

    public boolean hasDesignNotes() { return _designNotes != null; }


    public NumberFormat getNumberFormatter() { return _nbrFormatter; }

    public void setNumberFormatter(NumberFormat format) { _nbrFormatter = format; }


    public boolean isContainer() { return hasLabel(); }


    /****************************************************************************/

    /**
     * Parses any attributes in this node's 'attribute' XML element
     * @param attributeNode the node describing the attributes
     */
    protected void parseAttributes(XNode attributeNode) {
        if (attributeNode == null) return;

        XNode boundsNode = attributeNode.getChild("bounds");
        if (boundsNode != null) setBounds(YLayoutUtil.parseRect(boundsNode, _nbrFormatter));

        String fillRGB = attributeNode.getChildText("backgroundColor");
        if (fillRGB != null) setFillColor(YLayoutUtil.strToInt(fillRGB));

        String foreRGB = attributeNode.getChildText("foregroundColor");
        if (foreRGB != null) setColor(YLayoutUtil.strToInt(foreRGB));

    }


    protected void parseLabel(XNode labelNode) {
        if (labelNode != null) {
            XNode attributeNode = labelNode.getChild("attributes");
            if (attributeNode != null) {
                setLabelBounds(YLayoutUtil.parseRect(
                        attributeNode.getChild("bounds"), _nbrFormatter));

                String foreRGB = attributeNode.getChildText("foregroundColor");
                setLabelColor(YLayoutUtil.strToInt(foreRGB));

                XNode fontNode = attributeNode.getChild("font");
               if (fontNode != null) parseFont(fontNode);
            }
        }
    }


    protected void parseFont(XNode fontNode) {
        String name = fontNode.getChildText("name");
        int style = YLayoutUtil.strToInt(fontNode.getChildText("style"));
        int size = YLayoutUtil.strToInt(fontNode.getChildText("size"));
        _font = new Font(name, style, size);
    }


    protected void parseVertexDesignNotes(XNode vertexNode) {
        setDesignNotes(vertexNode.getChildText("notes", true));
    }


    protected XNode getAttributesNode() {
        XNode node = new XNode("attributes");
        if (_bounds != null) node.addChild(
                YLayoutUtil.getRectNode("bounds", _bounds, _nbrFormatter));
        addCommonAttributes(node);
        return node;
    }


    protected XNode getLabelNode() {
        XNode node = new XNode("label");
        XNode attributeNode = node.addChild("attributes");
        if (_labelBounds != null) {
            attributeNode.addChild(YLayoutUtil.getRectNode(
                    "bounds", _labelBounds, _nbrFormatter));
        }
        if (_labelColor != null) {
            attributeNode.addChild("foregroundColor", _labelColor.getRGB());
        }
        if (_font != null) {
            attributeNode.addChild(getFontNode());
        }
        return node;
    }


    private XNode addCommonAttributes(XNode node) {
        if (_color != null) node.addChild("foregroundColor", _color.getRGB());
        if (_fillColor != null) node.addChild("backgroundColor", _fillColor.getRGB());
        return node;
    }


    protected XNode getDesignNotesNode() {
        XNode node = null;
        if (hasDesignNotes()) {
            node = new XNode("notes");
            node.setText(_designNotes, true);
        }
        return node;
    }


    protected XNode getFontNode() {
        XNode node = new XNode("font");
        node.addChild("name", _font.getName());
        node.addChild("style", _font.getStyle());
        node.addChild("size", _font.getSize());
        return node;
    }


    /******************************************************************************/

    /**
     * Parses an XNode describing the XML of this node's layout, into this layout node's
     * data members
     * @param node the XNode to parse
     */
    protected abstract void parse(XNode node);


    /**
     * Writes the values of this node's data members into an XNode describing the XML
     * of this node's layout, to be later used to generate the actual XML
     * @return a populated XNode object
     */
    protected abstract XNode toXNode();

}
