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

package org.yawlfoundation.yawl.cost.data;

import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeIO;
import org.yawlfoundation.yawl.util.XNodeParser;

/**
 * A classification of cost drawers. Examples include labour, rent, electricity.
 *
 * @author Michael Adams
 * @date 3/10/11
 */
public class CostType implements XNodeIO {

    private long costTypeID;           // hibernate primary key
    private String type;

    public CostType() { }

    public CostType(String unit) { setType(unit); }

    public CostType(XNode node) { fromXNode(node); }


    public long getCostTypeID() { return costTypeID; }

    public void setCostTypeID(long id) { costTypeID = id; }


    public String getType() { return type; }

    public void setType(String u) { type = u; }


    public XNode toXNode() {
        return new XNode("costtype", type);
    }

    public String toXML() {
        return toXNode().toString();
    }

    public void fromXNode(XNode node) {
        if (node != null) {
            type = node.getText();
        }
    }

    public void fromXML(String xml) {
        fromXNode(new XNodeParser(true).parse(xml));
    }

    public XNodeIO newInstance(XNode node) {        
        return new CostType(node);
    }
}
