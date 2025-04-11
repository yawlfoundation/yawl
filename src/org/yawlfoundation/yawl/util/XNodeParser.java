/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

import org.apache.logging.log4j.LogManager;
import org.jdom2.Document;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Author: Michael Adams
 * Creation Date: 22/03/2010
 */
public class XNodeParser {

    private boolean _check;                                         // validation flag
    private boolean _suppressMessages;
    private Pattern _attributeSplitter;
    private List<String> _openingComments;                          // if root node only
    private List<String> _closingComments;                          // if root node only

    private static final String UTF8_BOM = "\uFEFF";

    public XNodeParser() {
        this(false);
    }

    public XNodeParser(boolean check) {
        _check = check;
        _suppressMessages = false;
        _attributeSplitter = Pattern.compile("\\s*=\\s*\"|\\s*\"\\s*");
    }


    /**
     * Parses a simple xml string into an XNode structure
     * @param s the string to parse
     * @return the root XNode, with contents
     */
    public XNode parse(String s) {
        if (s == null || s.isEmpty()) return null;

        // remove UTF-8 BOM char, if any
        if (s.startsWith(UTF8_BOM)) s = s.substring(1);

        // remove any headers
        if (s.startsWith("<?xml")) s = s.substring(s.indexOf("?>") + 2).trim();
        if (s.startsWith("<!DOCTYPE")) s = s.substring(s.indexOf('>') + 2).trim();

        // if well-formedness check required use JDOM to check it
        if (_check && (JDOMUtil.stringToElement(s) == null)) return null;
        
        return parse(s, 0);
    }


    public XNode parse(Element e) {
        return parse(JDOMUtil.elementToString(e));
    }

    
    public XNode parse(Document d) {
        return parse(JDOMUtil.documentToString(d));
    }


    public void suppressMessages(boolean suppress) { _suppressMessages = suppress; }


    /************************************************************************/

    private XNode parse(String s, int depth) {
        XNode node;
        init();
        try {
            if ((s == null) || (! s.trim().startsWith("<"))) {
                throw new IllegalArgumentException("bad input string");
            }

            // handle any comments before or after the root element
            if (depth == 0) s = processOutlyingComments(s.trim());

            // get the text inside the opening tag and use it to create a new XNode
            String tagDef = s.trim().substring(1, s.indexOf('>'));
            node = newNode(tagDef, depth);

            // if this element is not fully enclosed in a single tag
            if (! tagDef.endsWith("/")) {                  // '>' is already removed

                // parse entire inner string to the matching closing tag (exclusive)
                for (String content : parseContent(s)) {
                    if (content.startsWith("<!--")) {
                        node.addComment(extractComment(content));
                    }
                    else if (content.startsWith("<![")) {
                        node.addCDATA(extractCDATA(content));
                    }
                    else if (content.startsWith("<")) {
                        node.addChild(parse(content, depth + 1));      // recurse
                    }
                    else {
                        if (content.contains("{") && !content.contains("${")) {
                            content = JDOMUtil.decodeEscapes(content);
                        }
                        node.setText(content);
                    }
                }
            }
            return node;
        }
        catch (Exception e) {
            if (! _suppressMessages) {
                LogManager.getLogger(this.getClass()).error(
                        "Invalid format parsing string [{}] - {}", s, e.getMessage());
            }
            return null;
        }
    }

    
    /**
     * Creates a new XNode from the text provided
     * @param s the inner contents of an opening tag (ie. name + attributes)
     * @return the created node
     */
    private XNode newNode(String s, int depth) {
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

        // if this is the root node and there's outlying comments, add them
        if (depth == 0) addOutlyingComments(node);

        return node;
    }


    private String getFirstWord(String s) {
        for (int i=0; i < s.length(); i++) {
            if (Character.isWhitespace(s.charAt(i))) {
                return s.substring(0, i);
            }
        }
        return s;
    }

    
    private String strSubtract(String subtractee, String subtractor) {
        int i = subtractee.indexOf(subtractor);
        return (i > -1) ? subtractee.substring(i + subtractor.length()) : subtractee;
    }


    private List<String> parseContent(String content) {
        List<String> contentList = new ArrayList<String>();
        String subContent;
        content = content.substring(content.indexOf('>') + 1, content.lastIndexOf('<')).trim();
        while (content.length() > 0) {
            if (content.startsWith("<!--")) {   // comment
                subContent = content.substring(0, content.indexOf("-->") + 3);
            }
            else if (content.startsWith("<![")) { // CDATA
                subContent = content.substring(0, content.indexOf("]>") + 2);
            }
            else if (content.startsWith("<")) { // child content
                subContent = getSubContent(content);
            }
            else {
                subContent = content;          // text
            }
            contentList.add(subContent);
            content = content.substring(subContent.length()).trim();
        }
        return contentList;
    }


    private String getSubContent(String s) {
        int end = s.indexOf('>');
        String tag = s.substring(1, end);
        if (! tag.endsWith("/")) {
            String tagName = getFirstWord(tag);
            List<Integer> openers = getIndexList(s, "<" + tagName);
            List<Integer> closers = getIndexList(s, "</" + tagName);
            end = getCorrespondingCloserPos(openers, closers) + tagName.length() + 2;
        }
        return s.substring(0, end + 1);
    }


    private List<Integer> getIndexList(String s, String sub) {
        int offset = sub.length();
        List<Integer> indexList = new ArrayList<Integer>();
        int pos = s.indexOf(sub);
        while (pos > -1) {
            if (isBookEndTag(s, pos + offset)) {
                indexList.add(pos);
            }
            pos = s.indexOf(sub, pos + offset);
        }
        return indexList;
    }


    private boolean isBookEndTag(String s, int pos) {
        return lastCharDelineatesTag(s, pos) && (! isSelfClosingTag(s, pos)) ;
    }


    private boolean lastCharDelineatesTag(String s, int pos) {
        if (pos >= (s.length() - 1)) {
            return s.charAt(s.length() - 1) == '>';
        }
        else {
            return Character.isWhitespace(s.charAt(pos)) || (s.charAt(pos) == '>');
        }
    }


    private boolean isSelfClosingTag(String s, int pos) {
        while ((pos < s.length() - 2) && Character.isWhitespace(s.charAt(pos))) pos++;   // ignore whitespace
        return (s.charAt(pos) == '/') && (s.charAt(pos+1) == '>');
    }


    private int getCorrespondingCloserPos(List<Integer> openers, List<Integer> closers) {
        if ((openers.size() == 1) || (closers.size()) == 1) return closers.get(0);

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
            if (openIndex == openers.size() || closeIndex == closers.size()) {
                return closers.get(closers.size() - 1);
            }
        }
        return closers.get(closeIndex);
    }


    private void init() {
        _openingComments = null;
        _closingComments = null;
    }

    private String processOpeningComments(String s) {
        if (! s.startsWith("<!--")) return s;
        _openingComments = new ArrayList<String>();
        while (s.startsWith("<!--")) {
            String comment = extractComment(s);
            _openingComments.add(comment);
            s = s.substring(s.indexOf("-->") + 3).trim();
        }
        return s;
    }


    private String processClosingComments(String s) {
        if (! s.endsWith("-->")) return s;
        _closingComments = new ArrayList<String>();
        while (s.endsWith("-->")) {
            String comment = extractTrailingComment(s);
            _closingComments.add(comment);
            s = s.substring(0, s.lastIndexOf("<!--")).trim();
        }
        return s;
    }


    private String processOutlyingComments(String s) {
        return processOpeningComments(processClosingComments(s));
    }


    private void addOutlyingComments(XNode node) {
        if (_openingComments != null) {
            for (String comment : _openingComments) node.addOpeningComment(comment);
        }
        if (_closingComments != null) {
            for (String comment : _closingComments) node.addClosingComment(comment);
        }
    }


    private String extractComment(String rawComment) {
        return rawComment.substring(4, rawComment.indexOf("-->")).trim();
    }

    private String extractTrailingComment(String rawComment) {
        return rawComment.substring(rawComment.lastIndexOf("<!--") + 4,
                rawComment.length() - 3).trim();
    }

    private String extractCDATA(String rawCDATA) {
        return rawCDATA.substring(9, rawCDATA.indexOf("]]>"));
    }

}
