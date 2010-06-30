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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: Michael Adams
 * Creation Date: 22/03/2010
 */
public class XNodeParser {

    private boolean _check;                                         // validation flag
    private Pattern _commentPattern;
    private Pattern _commentSplitter;
    private Pattern _attributeSplitter;
    private CommentCutter _commentCutter;

    
    public XNodeParser() {
        this(false);
    }

    public XNodeParser(boolean check) {
        _check = check;
        _commentPattern = Pattern.compile("^<!--\\s*.*?\\s*-->");
        _commentSplitter = Pattern.compile("<!--\\s*|\\s*-->");
        _attributeSplitter = Pattern.compile("\\s*=\\s*\"|\\s*\"\\s*");
        _commentCutter = new CommentCutter();
    }


    /**
     * Parses a simple xml string into an XNode structure
     * @param s the string to parse
     * @return the root XNode, with contents
     */
    public XNode parse(String s) {
        if (s == null) return null;

        // remove any header
        if (s.startsWith("<?xml")) s = s.substring(s.indexOf("?>") + 2).trim();

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
            s = s.trim();

            // get any comments above the tag
            List<String> comments = _commentCutter.cut(s);
            s = _commentCutter.getText();

            // get the text inside the opening tag and use it to create a new XNode
            String tagDef = s.trim().substring(1, s.indexOf('>'));
            node = newNode(tagDef, depth, comments);

            // if this element is not fully enclosed in a single tag
            if (! tagDef.endsWith("/")) {                     // '>' is already removed

                // get entire inner string to the matching closing tag (exclusive) and the
                // remaining text (if any)
                String content = getContent(s, tagDef, node.getName());
                s = getSiblingText(s, tagDef, content, node.getName());

                // if contents starts with a tag
                if (content.trim().startsWith("<")) {
                    node.addChild(parse(content, depth + 1, node));      // recurse
                }
                else {
                    node.setText(content);
                }
            }
            else {
                s = getSiblingText(s, tagDef, null, node.getName());
            }
            if ((parent != null) && (s.length() > 0)) parent.addChild(node);
        }
        return node;
    }


    /**
     * Creates a new XNode from the text provided
     * @param s the inner contents of an opening tag (ie. name + attributes)
     * @return the created node
     */
    private XNode newNode(String s, int depth, List<String> comments) {
        if (s.endsWith("/")) s = s.substring(0, s.length() - 1);  // lop off '/', if any

        String name = getFirstWord(s.trim());
        XNode node = new XNode(name);                  // parts[0] is the tag's name
        node.setDepth(depth);

        // if there are any attributes defined, add them to the node
        String attributes = strSubtract(s, name).trim();
        if (attributes.length() > 0) {
            String[] attributeParts = _attributeSplitter.split(attributes);
            for (int i=0; i < attributeParts.length - 1; i=i+2) {
                node.addAttribute(attributeParts[i].trim(), attributeParts[i+1].trim());
            }    
        }

        if (comments != null) node.addComments(comments);           // add any comments

        return node;
    }


    private String getFirstWord(String s) {
        int firstSpace = s.indexOf(" ");
        return (firstSpace > -1) ? s.substring(0, firstSpace) : s;
    }

    private String strSubtract(String subtractee, String subtractor) {
        int i = subtractee.indexOf(subtractor);
        return (i > -1) ? subtractee.substring(i + subtractor.length()) : subtractee;
    }


    private String getContent(String s, String tag, String nodeName) {
        int start = s.indexOf(makeTag(tag, true)) + 2 + tag.length();
        int end = s.indexOf(makeTag(nodeName, false));
        return s.substring(start, end);
    }

    private String getSiblingText(String s, String tag, String content, String nodeName) {
        String del = (content != null) ?
                     makeTag(tag, true) + content + makeTag(nodeName, false) :
                     "<" + tag + ">";
        return strSubtract(s, del).trim();
    }

    private String makeTag(String name, boolean opening) {
        String start = opening ? "<" : "</";
        return start + name + ">";
    }


    class CommentCutter {
        String cutText = null;
        List<String> comments = null;

        public CommentCutter() { }

        public List<String> cut(String s) {
            comments = null;
            if (s.startsWith("<!--")) {
                comments = new ArrayList<String>();
                while (s.startsWith("<!--")) {
                    Matcher m = _commentPattern.matcher(s);
                    if (m.find()) {
                        comments.add(extractComment(m.group()));
                        s = m.replaceFirst("").trim();
                    }
                }
            }
            cutText = s;

            return comments;
        }

        public String getText() { return cutText; }

        public List<String> getComments() { return comments; }

        private String extractComment(String rawComment) {
            return _commentSplitter.split(rawComment)[1];
        }
    }
}
