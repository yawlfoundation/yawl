package org.yawlfoundation.yawl.util;

import org.jdom.Document;
import org.jdom.Element;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A utility for building xml strings. Handles elements, attributes, comments and text
 * (and that's all - no edge cases).
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

    private XNode _parent;
    private Map<String, XNode> _children;
    private Map<String, String> _attributes;
    private String _name;
    private String _text;
    private String _comment;
    private int _depth = 0;
    private static int _defTabSize = 2;

    public XNode(String name) {
        _name = name;
    }

    public XNode(String name, String text) {
        _name = name;
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

    /**************************************************************************/


    public XNode addChild(XNode child) {
        if (_children == null) _children = new LinkedHashMap<String, XNode>();
        child.setParent(this);
        child.setDepth(_depth + 1);
        _children.put(child.getName(), child);
        return child;
    }

    public XNode addChild(String name) {
        return addChild(new XNode(name));
    }

    public XNode addChild(String name, String text) {
        return addChild(new XNode(name, text));
    }

    public XNode addChild(String name, String text, boolean escape) {
        if (escape) {
            text = JDOMUtil.encodeEscapes(text);
        }
        return addChild(new XNode(name, text));
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
            return _children.get(name);
        }
        return null;
    }

    public String getAttributeValue(String key) {
        if (_attributes != null) {
            return _attributes.get(key);
        }
        return null;
    }

    public String getName() {
        return _name;
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
    }

    public boolean hasChildren() {
        return _children != null;
    }


    public String toString() {
        return toString(false, _depth, _defTabSize);
    }

    public String toPrettyString() {                           // print with indents
        return toString(true, _depth, _defTabSize);
    }


    public String toPrettyString(int tabSize) {                    // print with indents
        return toString(true, _depth, tabSize);
    }

    private String toString(boolean pretty, int offset, int tabSize) {
        StringBuilder s = new StringBuilder();
        String tabs = getIndent(offset, tabSize);
        if (pretty) s.append(tabs);
        s.append("<").append(_name);

        if (_attributes != null) {
            for (String key : _attributes.keySet()) {
                s.append(String.format(" %s=\"%s\"", key, _attributes.get(key)));
            }
        }

        if ((_children == null) && (_text == null)) {
            s.append("/");
        }
        else {
            s.append(">");
            if (_children != null) {
                if (pretty) s.append(newline);
                for (XNode child : _children.values()) {
                    s.append(child.toString(pretty, offset, tabSize));
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

}
