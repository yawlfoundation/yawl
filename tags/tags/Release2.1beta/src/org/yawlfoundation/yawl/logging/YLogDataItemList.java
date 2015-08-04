package org.yawlfoundation.yawl.logging;

import org.jdom.Element;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.util.List;
import java.util.Vector;

/**
 * Author: Michael Adams
 * Creation Date: 9/04/2009
 */
public class YLogDataItemList extends Vector<YLogDataItem> {

    public YLogDataItemList() {
        super();
    }

    public YLogDataItemList(YLogDataItem firstItem) {
        super();
        this.add(firstItem);
    }

    public YLogDataItemList(String xml) {
        super();
        fromXML(xml);
    }

    public YLogDataItemList(Element xml) {
        super();
        fromXML(JDOMUtil.elementToString(xml));
    }
    

    public String toXML() {
        StringBuilder s = new StringBuilder("<logdataitemlist>");
        for (YLogDataItem item : this) {
            s.append(item.toXML());
        }
        s.append("</logdataitemlist>");
        return s.toString();
    }


    private void fromXML(String xml) {
        Element e = JDOMUtil.stringToElement(xml);
        if (e != null) {
            List children = e.getChildren();
            for (Object child : children) {
               this.add(new YLogDataItem((Element) child));
            }
        }    
    }



}
