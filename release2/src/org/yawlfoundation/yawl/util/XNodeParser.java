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

import org.apache.log4j.Logger;
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
        _commentPattern = Pattern.compile("^<!--\\s*.*?\\s*-->", Pattern.DOTALL);
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
        try {
            while (s.length() > 0) {
                s = s.trim();

                // get any comments above the tag
                List<String> comments = _commentCutter.cut(s);
                s = _commentCutter.getText();

                // get the text inside the opening tag and use it to create a new XNode
                String tagDef = s.trim().substring(1, s.indexOf('>'));
                node = newNode(tagDef, depth, comments);

                // if this element is not fully enclosed in a single tag
                if (! tagDef.endsWith("/")) {                  // '>' is already removed

                    // get entire inner string to the matching closing tag (exclusive)
                    // and the remaining text (if any)
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
        catch (Exception e) {
            Logger.getLogger(this.getClass()).error("Invalid format parsing string: " + s);
            return null;
        }
    }


    /**
     * Creates a new XNode from the text provided
     * @param s the inner contents of an opening tag (ie. name + attributes)
     * @return the created node
     */
    private XNode newNode(String s, int depth, List<String> comments) {
        if (s.endsWith("/")) s = s.substring(0, s.length() - 1);  // lop off '/', if any

        String name = getFirstWord(s.trim());
        XNode node = new XNode(name);                 
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
        for (int i=0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c == ' ') || (c == '\n') || (c == '\t')) {
                return s.substring(0, i);
            }
        }
        return s;
    }

    
    private String strSubtract(String subtractee, String subtractor) {
        int i = subtractee.indexOf(subtractor);
        return (i > -1) ? subtractee.substring(i + subtractor.length()) : subtractee;
    }


    private String getContent(String s, String tag, String nodeName) {
        String openingTag = makeTag(tag, true);
        String closingTag = makeTag(nodeName, false);
        List<Integer> openers = getIndexList(s, openingTag);
        List<Integer> closers = getIndexList(s, closingTag);
        int start = s.indexOf(openingTag) + tag.length() + 2;
        int end = getCorrespondingCloserPos(openers, closers);
        return s.substring(start, end);
    }

    private List<Integer> getIndexList(String s, String sub) {
        int breakPos = sub.indexOf(' ');
        if (breakPos > -1) {                       // tag contains attribute(s)
            sub = sub.substring(0, breakPos + 1);  // so truncate it (with space suffix)
        }
        List<Integer> indexList = new ArrayList<Integer>();
        int pos = s.indexOf(sub);
        while (pos > -1) {
            if (lastCharDelineatesTag(s, pos + sub.length() - 1) &&
                    (! isEnclosedElement(s, pos))) {
                indexList.add(pos);
            }
            pos = s.indexOf(sub, pos + 1);
        }
        return indexList;
    }

    private boolean lastCharDelineatesTag(String s, int pos) {
        if (pos >= (s.length() - 1)) {
            return s.charAt(s.length() - 1) == '>';
        }
        else {
            return (s.charAt(pos) == ' ') || (s.charAt(pos) == '>');
        }
    }

    private boolean isEnclosedElement(String s, int start) {
        for (int i = start + 1; i < s.length(); i++) {
            if ((s.charAt(i) == '/') && ((i < s.length()-1) && (s.charAt(i+1) == '>'))) {
                return true;
            }
            else if (s.charAt(i) == '>') {
                return false;
            }
        }
        return false;
    }

    private int getCorrespondingCloserPos(List<Integer> openers, List<Integer> closers) {
        if (openers.size() == 1) return closers.get(0);

        int openIndex = 1;
        int closeIndex = 0;
        int accumulator = 1;
        while (accumulator > 0) {
            if (openers.get(openIndex) < closers.get(closeIndex)) {
                accumulator++;
                openIndex++;
            }
            else {
                accumulator--;
                if (accumulator > 0) closeIndex++;
            }
            if (openIndex == openers.size()) {
                return closers.get(closers.size() - 1);
            }
        }
        return closers.get(closeIndex);
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
