/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

import net.sf.saxon.om.Axis;
import net.sf.saxon.om.AxisIterator;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.type.Type;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

/**
 * 
 * @author Lachlan Aldred
 * Date: 30/10/2003
 * Time: 18:20:55
 * 
 */
public class YSaxonOutPutter {
    private Document _doc;

    public YSaxonOutPutter(NodeInfo nodeInfo) {
        _doc = new Document();
        _doc.setRootElement(createElement(nodeInfo));
    }

    private Element createElement(NodeInfo nodeInfo) {
        Element el = new Element(nodeInfo.getLocalPart());
        AxisIterator iter = nodeInfo.iterateAxis(Axis.CHILD, AnyNodeTest.getInstance());
        while (iter.moveNext()) {
            Item item = iter.next();
            switch (((NodeInfo) item).getNodeKind()) {
                case Type.ELEMENT:
                    el.addContent(createElement((NodeInfo) item));
                    break;
                default:
//                    try {
                        el.setText(item.getStringValue());
//                    } catch (XPathException e) {
//                        e.printStackTrace();
//                    }
            }
        }
        iter = nodeInfo.iterateAxis(Axis.ATTRIBUTE, AnyNodeTest.getInstance());
        while (iter.moveNext()) {
            Item item = iter.next();
            el.setAttribute(((NodeInfo) item).getLocalPart(), ((NodeInfo) item).getStringValue());
        }
        return el;
    }

    public String getString() {
        XMLOutputter outPutter = new XMLOutputter();
        return outPutter.outputString(_doc.getRootElement());
    }
}
