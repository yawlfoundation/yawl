/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.util;

import net.sf.saxon.om.Axis;
import net.sf.saxon.om.AxisIterator;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.value.Type;
import net.sf.saxon.xpath.XPathException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;
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
        Element el = new Element(nodeInfo.getLocalName());
        AxisIterator iter = nodeInfo.iterateAxis(Axis.CHILD, AnyNodeTest.getInstance());
        while (iter.hasNext()) {
            Item item = iter.next();
            switch (item.getItemType()) {
                case Type.ELEMENT:
                    el.addContent(createElement((NodeInfo) item));
                    break;
                default:
                    try {
                    	el.addContent(new Text(item.getStringValue()));
                    } catch (XPathException e) {
                        e.printStackTrace();
                    }
            }
        }
        iter = nodeInfo.iterateAxis(Axis.ATTRIBUTE, AnyNodeTest.getInstance());
        while (iter.hasNext()) {
            Item item = iter.next();
            el.setAttribute(((NodeInfo) item).getLocalName(), ((NodeInfo) item).getStringValue());
        }
        return el;
    }

    public String getString() {
        XMLOutputter outPutter = new XMLOutputter();
        return outPutter.outputString(_doc.getRootElement());
    }
}
