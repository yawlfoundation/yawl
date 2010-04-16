package org.yawlfoundation.yawl.util;

import org.jdom.Document;
import org.jdom.Element;

/**
 * Author: Michael Adams
 * Creation Date: 22/03/2010
 */
public class XNodeParser {

    private boolean _check;                                         // validation flag

    public XNodeParser() {
        _check = false;
    }

    public XNodeParser(boolean check) {
        _check = check;
    }


    /**
     * Parses a simple xml string into an XNode structure
     * @param s the string to parse
     * @return the root XNode, with contents
     */
    public XNode parse(String s) {
        if (s == null) return null;

        // remove any header
        if (s.startsWith("<?xml")) s = s.substring(s.indexOf("?>") + 1);

        // if well-formedness check required use JDOM to check it
        if (_check && (JDOMUtil.stringToElement(s) == null)) return null;
        
        return parse(s, 0, null);
    }


    public XNode parse(Element e) {
        return parse(JDOMUtil.elementToString(e));
    }

    public XNode parse(Document d) {
        return parse(JDOMUtil.documentToString(d));
    }


    private XNode parse(String s, int depth, XNode parent) {
         XNode node = null;

        while (s.length() > 0) {

            // get the text inside the opening tag and use it to create a new XNode
            String tagDef = s.trim().substring(1, s.indexOf('>'));
            node = newNode(tagDef);
            node.setDepth(depth);

            // if this element is not fully enclosed in a single tag
            if (! tagDef.endsWith("/")) {                     // '>' is already removed

                // get entire inner string to the matching closing tag (exclusive) and the
                // remaining text (if any)
                String content = getContent(s, tagDef);
                s = getSiblingText(s, tagDef, content);

                // if contents starts with a tag
                if (content.startsWith("<")) {
                    node.addChild(parse(content, depth + 1, node));      // recurse
                }
                else {
                    node.setText(content);
                }
                if ((parent != null) && (s.length() > 0)) parent.addChild(node);
            }
        }
        return node;
    }


    /**
     * Creates a new XNode from the text provided
     * @param s the inner contents of an opening tag (ie. name + attributes)
     * @return the created node
     */
    private XNode newNode(String s) {
        if (s.endsWith("/")) s = s.substring(0, s.length() - 1);  // lop off '/', if any

        String[] parts = s.split("\\s+");                         // split on whitespace
        XNode node = new XNode(parts[0]);                  // parts[0] is the tag's name

        // if there are any attributes defined, add them to the node
        for (int i=1; i<parts.length; i++) {
            String[] attribute = parts[i].split("=");
            node.addAttribute(attribute[0], dequote(attribute[1]));
        }
        return node;
    }

    
    private String dequote(String s) {
        return s.substring(1, s.lastIndexOf('"'));          // remove surrounding quotes
    }


    private String getContent(String s, String tag) {
        int start = s.indexOf(makeTag(tag, true)) + 2 + tag.length();
        int end = s.indexOf(makeTag(tag, false));
        return s.substring(start, end);
    }

    private String getSiblingText(String s, String tag, String content) {
        String del = makeTag(tag, true) + content + makeTag(tag, false);
        return s.replaceFirst(del, "").trim();
    }

    private String makeTag(String name, boolean opening) {
        String start = opening ? "<" : "</";
        return start + name + ">";
    }
}
