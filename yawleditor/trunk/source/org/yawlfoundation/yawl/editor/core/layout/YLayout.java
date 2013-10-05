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

import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.awt.*;
import java.text.NumberFormat;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

/**
 * This class represents the root of a layout tree for a specification. Each
 * net, task, condition and flow in the specification has a 1:1 association with
 * a layout object located within this tree.
 *
 * @author Michael Adams
 * @date 7/06/12
 */
public class YLayout {

    public static final Color DEFAULT_COLOR = Color.BLACK;
    public static final Color DEFAULT_FILL_COLOR = Color.WHITE;
    public static final int DEFAULT_FONT_SIZE = 15;


    private YSpecification _specification;        // spec this layout is for
    private Locale _locale;                       // where are we?
    private NumberFormat _nbrFormatter;           // for specific locale formats
    private Dimension _size;                      // saved canvas size
    private int _globalFontSize;
    private Color _globalFillColor;
    private Map<String, YNetLayout> _nets;        // the nets of this spec


    // hidden default constructor
    private YLayout() {
        _nets = new Hashtable<String, YNetLayout>();
        _locale = Locale.getDefault();
        _nbrFormatter = NumberFormat.getInstance(_locale);
    }


    /**
     * Creates a new layout tree for a specification
     * @param spec the specification associated with this layout tree
     */
    public YLayout(YSpecification spec) {
        this();
        _specification = spec;
    }


    /**
     * Create a new net layout for this layout tree. This method should only be used
     * when the actual YNet is not available to pass to the YNetLayout constructor
     * @param id the id of the new net
     * @return an instantiated YNetLayout instance
     */
    public YNetLayout newNetLayoutInstance(String id) {
        return new YNetLayout(new YNet(id, _specification), _nbrFormatter);
    }


    /**
     * Parses an XML string describing the layout for this specification, and
     * populates the layout tree
     * @param layoutXML the XML description to parse
     * @throws YLayoutParseException
     * @pre The XML has already been validated against schema
     */
    public void parse(String layoutXML) throws YLayoutParseException {
        XNode layoutNode = new XNodeParser(true).parse(layoutXML);
        if (layoutNode == null) {
            throw new YLayoutParseException("Invalid layout XML.");
        }
        parse(layoutNode);
    }


    /**
     * Parses an XNode describing the layout for this specification, and
     * populates the layout tree
     * @param node the XNode description to parse
     * @throws YLayoutParseException
     */
    public void parse(XNode node) throws YLayoutParseException {
        if (node == null) {
            throw new YLayoutParseException("Null layout XML.");
        }
        if (! node.getName().equals("layout")) {
            throw new YLayoutParseException("Invalid root element: " + node.getName());
        }

        parseLocale(node.getChild("locale"));

        XNode specChild = node.getChild("specification");
        if (specChild == null) {
            throw new YLayoutParseException("Missing 'specification' node.");
        }

        _size = YLayoutUtil.parseSize(specChild.getChild("size"));
        _globalFontSize = StringUtil.strToInt(
                specChild.getChildText("labelFontSize"), DEFAULT_FONT_SIZE);
        parseFillColor(specChild.getAttributeValue("defaultBgColor"));
        parseNets(specChild);
    }


    public YSpecification getSpecification() { return _specification; }

    public int getNetCount() { return _nets != null ? _nets.size() : 0; }

    public boolean hasNets() { return getNetCount() > 0; }


    public Locale getLocale() { return _locale; }

    public void setLocale(Locale locale) { _locale = locale; }


    public Dimension getSize() { return _size; }

    public void setSize(Dimension size) { _size = size; }

    public void setSize(int w, int h) { _size = new Dimension(w, h); }


    public int getGlobalFontSize() { return _globalFontSize; }

    public void setGlobalFontSize(int fontSize) { _globalFontSize = fontSize; }


    public Color getGlobalFillColor() { return _globalFillColor; }

    public void setGlobalFillColor(Color color) { _globalFillColor = color; }


    public Map<String, YNetLayout> getNetLayouts() { return _nets; }

    public YNetLayout getNetLayout(String netID) { return _nets.get(netID); }

    public void addNetLayout(YNetLayout netLayout) {
        _nets.put(netLayout.getID(), netLayout);
    }

    public YNetLayout removeNetLayout(String netID) {
        return _nets.remove(netID);
    }


    /****** Convenience pass-through methods *************/

    public YTaskLayout getTaskLayout(String netID, String taskID) {
        YNetLayout netLayout = _nets.get(netID);
        return netLayout != null ? netLayout.getTaskLayout(taskID) : null;
    }


    public YConditionLayout getConditionLayout(String netID, String conditionID) {
        YNetLayout netLayout = _nets.get(netID);
        return netLayout != null ? netLayout.getConditionLayout(conditionID) : null;
    }


    public YFlowLayout getFlowLayout(String netID, String sourceID, String targetID) {
        YNetLayout netLayout = _nets.get(netID);
        return netLayout != null ? netLayout.getFlowLayout(sourceID, targetID) : null;
    }

    /*****************************************************/

    /**
     * Outputs an XML string description of this layout tree
     * @return the generated XML
     */
    public String toXML() {
        return toXNode().toPrettyString();
    }


    public XNode toXNode() {
        XNode node = new XNode("layout");
        node.addChild(getLocaleNode());
        node.addChild(getSpecificationNode());
        return node;
    }


    /**************************************************************************/

    private void parseLocale(XNode node) {
        if (node != null) {
            String language = node.getAttributeValue("language");
            String country = node.getAttributeValue("country");
            if (! (language == null || country == null)) {
                _locale = new Locale(language, country);
            }
        }
        else _locale = Locale.getDefault();

        // create a number format for the specific locale
        _nbrFormatter = NumberFormat.getInstance(_locale);
    }


    private void parseFillColor(String rgbStr) {
         if (rgbStr != null) {
             int rgb = YLayoutUtil.strToInt(rgbStr);
             _globalFillColor = (rgb != 0) ? new Color(rgb) : DEFAULT_FILL_COLOR;
         }
     }


    // parses each of the net layouts in this spec layout
    private void parseNets(XNode node) throws YLayoutParseException {

        // there must be at least one net
        if (! node.hasChild("net")) {
            throw new YLayoutParseException("No 'net' node found.");
        }

        for (XNode netNode : node.getChildren("net")) {
            String id = netNode.getAttributeValue("id");
            if (id == null) {
                throw new YLayoutParseException("Missing 'id' attribute on 'net' node.");
            }
            YNet net = getNet(id);
            if (net == null) {
                throw new YLayoutParseException("Can't locate net with id=" + id);
            }
            YNetLayout netLayout = new YNetLayout(net, _nbrFormatter);
            netLayout.parse(netNode);
            _nets.put(id, netLayout);
        }
    }


    private YNet getNet(String id) {
        for (YDecomposition dec : _specification.getDecompositions()) {
            if ((dec instanceof YNet) && dec.getID().equals(id)) {
                return (YNet) dec;
            }
        }
        return null;
    }


    private XNode getLocaleNode() {
        XNode node = new XNode("locale");
        node.addAttribute("language", _locale.getLanguage());
        node.addAttribute("country", _locale.getCountry());
        return node;
    }


    private XNode getSizeNode() {
        XNode node = new XNode("size");
        node.addAttribute("w", (int) _size.getWidth());
        node.addAttribute("h", (int) _size.getHeight());
        return node;
    }


    private XNode getSpecificationNode() {
        XNode node = new XNode("specification");
        node.addAttribute("id", _specification.getURI());

        // only write non-default colors
        if (! (_globalFillColor == null || _globalFillColor.equals(Color.WHITE))) {
            node.addAttribute("defaultBgColor", _globalFillColor.getRGB());
        }
        node.addChild(getSizeNode());
        for (YNetLayout layout : _nets.values()) {
            node.addChild(layout.toXNode());
        }
        if (_globalFontSize != DEFAULT_FONT_SIZE) {
            node.addChild("labelFontSize", _globalFontSize);
        }
        return node;
    }

}
