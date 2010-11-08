/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.util;

import org.jdom.Document;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility for building xml strings. Handles elements, attributes, comments, text
 * and CDATA (and that's all - no edge cases).
 *
 * NOTE: To keep things simple, while this class allows a node to have both child
 * nodes and text, where both have values set the child nodes have precedence (ie.
 * the text is ignored).
 *
 * Author: Michael Adams
 * Creation Date: 19/03/2010
 */

public class XNode {

    static final String newline = System.getProperty("line.separator");
    static final String _header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    public static enum ContentType { text, comment, cdata }

    private XNode _parent;
    private List<XNode> _children;
    private Map<String, String> _attributes;
    private String _name;
    private String _text;
    private ContentType _contentType;
    private int _depth = 0;
    private static int _defTabSize = 2;

    public XNode(String name) {
        _name = name;
        _contentType = ContentType.text;  // default
    }

    public XNode(String name, String text) {
        this(name);
        _text = text;
    }

    /**************************************************************************/


    public void addAttribute(String key, String value) {
        if (_attributes == null) _attributes = new LinkedHashMap<String, String>();
        _attributes.put(key, value);
    }

    public void addAttribute(String key, String value, boolean escape) {
        if (escape) {
            value = JDOMUtil.encodeEscapes(value);
        }
        addAttribute(key, value);
    }

    public void addAttribute(String key, boolean value) {
        addAttribute(key, String.valueOf(value));
    }

    public void addAttribute(String key, byte value) {
        addAttribute(key, String.valueOf(value));
    }

    public void addAttribute(String key, short value) {
        addAttribute(key, String.valueOf(value));
    }

    public void addAttribute(String key, int value) {
        addAttribute(key, String.valueOf(value));
    }

    public void addAttribute(String key, long value) {
        addAttribute(key, String.valueOf(value));
    }

    public void addAttribute(String key, double value) {
        addAttribute(key, String.valueOf(value));
    }

    public void addAttribute(String key, float value) {
        addAttribute(key, String.valueOf(value));
    }

    public void addAttribute(String key, Object o) {
        addAttribute(key, o.toString());
    }


    /**************************************************************************/


    public XNode addChild(XNode child) {
        if (child != null) {
            if (_children == null) _children = new ArrayList<XNode>();
            child.setParent(this);
            child.setDepth(_depth + 1);
            _children.add(child);
        }
        return child;
    }

    public XNode addChild(String name) {
        return addChild(new XNode(name));
    }

    public XNode addChild(String name, String text) {
        return addChild(new XNode(name, text));
    }

    public XNode addChild(String name, boolean b) {
        return addChild(new XNode(name, String.valueOf(b)));
    }

    public XNode addChild(String name, long l) {
        return addChild(new XNode(name, String.valueOf(l)));
    }

    public XNode addChild(String name, int i) {
        return addChild(new XNode(name, String.valueOf(i)));
    }

    public XNode addChild(String name, byte b) {
        return addChild(new XNode(name, String.valueOf(b)));
    }

    public XNode addChild(String name, short s) {
        return addChild(new XNode(name, String.valueOf(s)));
    }

    public XNode addChild(String name, double d) {
        return addChild(new XNode(name, String.valueOf(d)));
    }

    public XNode addChild(String name, float f) {
        return addChild(new XNode(name, String.valueOf(f)));
    }

    public XNode addChild(String name, Object o) {
        return addChild(new XNode(name, o.toString()));
    }

    public XNode addChild(String name, String text, boolean escape) {
        if (escape) {
            text = JDOMUtil.encodeEscapes(text);
        }
        return addChild(new XNode(name, text));
    }

    public void addChildren(List<XNode> children) {
        for (XNode child : children) addChild(child);
    }

    public boolean removeChild(XNode child) {
        return (_children != null) && _children.remove(child);
    }


    /**************************************************************************/

    public XNode addComment(String comment) {
        XNode child = addChild("_!_", comment);
        child.setContentType(ContentType.comment);
        return child;
    }

    public boolean isComment() {
        return _contentType == ContentType.comment;
    }

    public XNode addCDATA(String cdata) {
        XNode child = addChild("_[_", cdata);
        child.setContentType(ContentType.cdata);
        return child;
    }

    public boolean isCDATA() {
        return _contentType == ContentType.cdata;
    }


    /**************************************************************************/

    public void setText(String text) {
        _text = text;
    }

    public void setText(boolean value) {
        setText(String.valueOf(value));
    }

    public void setText(byte value) {
        setText(String.valueOf(value));
    }

    public void setText(short value) {
        setText(String.valueOf(value));
    }

    public void setText(int value) {
        setText(String.valueOf(value));
    }

    public void setText(long value) {
        setText(String.valueOf(value));
    }

    public void setText(double value) {
        setText(String.valueOf(value));
    }

    public void setText(float value) {
        setText(String.valueOf(value));
    }

    /**************************************************************************/


    public XNode getChild(String name) {
        if (_children != null) {
            for (XNode child : _children) {
                if (child.getName().equals(name)) {
                    return child;
                }
            }    
        }
        return null;
    }

    /* returns the first text-type child */
    public XNode getChild() {
        if (_children != null) {
            for (XNode child : _children) {
                if (child.getContentType() == ContentType.text) {
                    return child;
                }
            }
        }
        return null;
    }


    public List<XNode> getChildren() {
        return (_children != null) ? _children : new ArrayList<XNode>();
    }


    public List<XNode> getChildren(ContentType cType) {
        List<XNode> matches = new ArrayList<XNode>();
        for (XNode child : getChildren()) {
            if (child.getContentType() == cType) {
                matches.add(child);
            }
        }
        return matches;
    }

    public List<XNode> getChildren(String name) {
        List<XNode> namedChildren = new ArrayList<XNode>();
        if (_children != null) {
            for (XNode child : _children) {
                if (child.getName().equals(name)) namedChildren.add(child);
            }
        }
        return namedChildren;
    }


    public String getChildText(String name) {
        XNode child = getChild(name);
        return (child != null) ? child.getText() : null;
    }

    public String getChildText(String name, boolean escape) {
        XNode child = getChild(name);
        return (child != null) ? child.getText(escape) : null;
    }


    public String getText() {
        return _text;
    }

    public String getText(boolean escape) {
        return escape ? JDOMUtil.decodeEscapes(_text) : _text;
    }


    public String getAttributeValue(String key) {
        if (_attributes != null) {
            return _attributes.get(key);
        }
        return null;
    }

    public Map<String, String> getAttributes() {
        return _attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        _attributes = attributes;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public void setParent(XNode parent) {
        _parent = parent;
    }

    public XNode getParent() {
        return _parent;
    }

    public int getDepth() {
        return _depth;
    }

    public void setDepth(int depth) {
        _depth = depth;
        if (hasChildren()) {
            depth++;
            for (XNode child : _children) {
                child.setDepth(depth);
            }
        }
    }

    public boolean hasChildren() {
        return _children != null;
    }

    public boolean hasChildren(String name) {
        return getChildren(name).size() > 0 ;
    }

    public int getAttributeCount() {
        return (_attributes == null) ? 0 : _attributes.size();
    }

    public int getChildCount() {
        return (_children == null) ? 0 : _children.size();
    }

    public int getTextLength() {
        return (_text == null) ? 0 : _text.length();
    }

    public int length() {
        return toString().length();
    }


    /**************************************************************************/


    public String toString() {
        return toString(false, _depth, _defTabSize, false);
    }

    public String toString(boolean header) {
        return toString(false, _depth, _defTabSize, header);
    }

    public String toPrettyString() {                               // print with indents
        return toString(true, _depth, _defTabSize, false);
    }

    public String toPrettyString(boolean header) {                 // print with indents
        return toString(true, _depth, _defTabSize, header);
    }

    public String toPrettyString(int tabSize) {                    // print with indents
        return toString(true, _depth, tabSize, false);
    }

    public String toPrettyString(boolean header, int tabSize) {    // print with indents
        return toString(true, _depth, tabSize, header);
    }

    private String toString(boolean pretty, int offset, int tabSize, boolean header) {
        String tabs = getIndent(offset, tabSize);
        if (isComment()) return printComment(pretty, tabs);
        if (isCDATA()) return printCDATA(pretty, tabs);

        StringBuilder s = new StringBuilder(getInitialToStringSize());
        if (header) s.append(_header).append(newline);
        if (pretty) s.append(tabs);

        s.append("<").append(_name);

        if (_attributes != null) {
            for (String key : _attributes.keySet()) {
                s.append(" ").append(key).append("=\"");
                s.append(_attributes.get(key)).append("\"");
            }
        }

        if ((_children == null) && (_text == null)) {
            s.append("/");
        }
        else {
            s.append(">");
            if (_children != null) {
                if (pretty) s.append(newline);
                for (XNode child : _children) {
                    s.append(child.toString(pretty, offset, tabSize, false));
                }
                if (pretty) s.append(tabs);
            }
            else {
                s.append(_text);
            }
            s.append("</").append(_name);
        }
        s.append(">");
        if (pretty) s.append(newline);

        return s.toString();
    }


    private int getInitialToStringSize() {

        // Estimate after some testing: 15 for tag start & finish (each) + 30 for text +
        // 20 for each attribute + 100 for each child
        return 60 + (20 * getAttributeCount()) + (100 * getChildCount());
    }


    public Element toElement() {
        return JDOMUtil.stringToElement(toString());
    }


    public Document toDocument() {
        return JDOMUtil.stringToDocument(toString());
    }


    private String getIndent(int offset, int tabSize) {
        int tabCount = _depth - offset;
        if (tabCount < 1) return "";
        char[] tabs = new char[tabCount * tabSize];
        for (int i=0; i<(tabCount * tabSize); i++) {
            tabs[i] = ' ';
        }
        return new String(tabs);
    }


    private String printSpecialText(String head, String foot, boolean pretty, String tabs) {
        StringBuilder s = new StringBuilder(_text.length() + 12);
        if (pretty) s.append(tabs);
        s.append(head).append(_text).append(foot);
        if (pretty) s.append(newline);
        return s.toString();
    }


    private String printComment(boolean pretty, String tabs) {
        return isComment() ? printSpecialText("<!-- ", " -->", pretty, tabs) : "";
    }


    private String printCDATA(boolean pretty, String tabs) {
        return isCDATA() ? printSpecialText("<![CDATA[", "]]>", pretty, tabs) : "";
    }

    private void setContentType(ContentType cType) {
        _contentType = cType;
    }

    private ContentType getContentType() {
        return _contentType;
    }

}
