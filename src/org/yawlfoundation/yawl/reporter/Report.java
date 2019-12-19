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

package org.yawlfoundation.yawl.reporter;

import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Michael Adams
 * @date 1/10/15
 */
public class Report {

    private String _subject;
    private final Map<String, String> _content;

    public Report() { _content = new TreeMap<String, String>(); }

    public Report(String subject) {
        this();
        _subject = subject;
    }


    public void add(String heading, String text) {
        _content.put(heading, text);
    }

    public void setSubject(String subject) { _subject = subject; }

    public String getSubject() { return _subject; }

    public void addContent(String heading, String text) { _content.put(heading, text); }


    public String getHTML() {
        XNode html = new XNode("html");
        for (String heading : _content.keySet()) {
            html.addChild("h3", heading);
            html.addChild("p", _content.get(heading));
        }
        return html.toString();
    }




    public String toXML() {
        XNode node = new XNode("report");
        node.addChild("subject", _subject != null ? _subject : "No subject");
        XNode content = node.addChild("content");
        for (String heading : _content.keySet()) {
            XNode entry = content.addChild("entry");
            entry.addAttribute("header", heading);
            entry.addChild("text", _content.get(heading));
        }
        return node.toString();
    }


    public void fromXML(String xml) throws IOException {
        XNode node = new XNodeParser().parse(xml);
        if (node == null) {
            throw new IOException("Malformed report XML");
        }
        _subject = node.getChildText("subject");
        XNode content = node.getChild("content");
        for (XNode entry : content.getChildren()) {
            _content.put(entry.getAttributeValue("header"), entry.getChildText("text"));
        }
    }

}
