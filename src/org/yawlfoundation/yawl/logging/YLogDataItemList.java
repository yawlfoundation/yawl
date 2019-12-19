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

package org.yawlfoundation.yawl.logging;

import org.jdom2.Element;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.util.ArrayList;

/**
 * Author: Michael Adams
 * Creation Date: 9/04/2009
 */
public class YLogDataItemList extends ArrayList<YLogDataItem> {

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


    public void fromXML(String xml) {
        Element e = JDOMUtil.stringToElement(xml);
        if (e != null) {
            for (Element child : e.getChildren()) {
               this.add(new YLogDataItem(child));
            }
        }    
    }

}
