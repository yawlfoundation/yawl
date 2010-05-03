package org.yawlfoundation.yawl.util;

import org.jdom.Document;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility for building xml strings. Handles elements, attributes, comments and text
 * (and that's all - no edge cases). Comments are printed _above_ the xml tag.
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

    private XNode _parent;
    private List<XNode> _children;
    private Map<String, String> _attributes;
    private String _name;
    private String _text;
    private List<String> _comments;
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

    public XNode addChild(String name, String text, boolean escape) {
        if (escape) {
            text = JDOMUtil.encodeEscapes(text);
        }
        return addChild(new XNode(name, text));
    }

    public boolean removeChild(XNode child) {
        return (_children != null) && _children.remove(child);
    }

    /**************************************************************************/

    public void addComment(String comment) {
        if (_comments == null) _comments = new ArrayList<String>();
        _comments.add(comment);
    }

    public void addComments(List<String> comments) {
        if (_comments == null) _comments = new ArrayList<String>();
        _comments.addAll(comments);
    }

    public boolean hasComments() {
        return (_comments != null);
    }

    public List<String> getComments() {
        return _comments;
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

    /* returns the first child */
    public XNode getChild() {
        if ((_children != null) && (_children.size() > 0)) {
            return _children.get(0);
        }
        return null;
    }


    public List<XNode> getChildren() {
        return (_children != null) ? _children : new ArrayList<XNode>();

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

    public String getText() {
        return _text;
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

    public int getCommentCount() {
        return (_comments == null) ? 0 : _comments.size();
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
        StringBuilder s = new StringBuilder(getInitialToStringSize());
        if (header) s.append(_header).append(newline);
        String tabs = getIndent(offset, tabSize);
        if (pretty) s.append(tabs);
        if (hasComments()) s.append(printComments(pretty, tabs));
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


    private String printComments(boolean pretty, String tabs) {
        if (hasComments()) {
            StringBuilder s = new StringBuilder(_comments.size() * 30);
            for (String comment : _comments) {
                if (pretty) s.append(tabs);
                s.append("<!-- ").append(comment).append(" -->");
                if (pretty) s.append(newline);
            }
            return s.toString();
        }
        return "";
    }

}
