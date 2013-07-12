/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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
import org.yawlfoundation.yawl.util.XNodeParser;

import java.util.Map;

/**
 * A unit of measure against which a cost may be allocated. Examples may be hour, week,
 * month, litre, tonne.
 *
 * @author Michael Adams
 * @date 3/10/11
 */
public class UnitCost {

    private enum TimeUnit { hour, minute, second, millisecond, fixed }

    private TimeUnit unit;
    private CostValue costValue;
    private FacetStatus facetStatus;


    public UnitCost() { }

    public UnitCost(String unit) { setUnit(unit); }

    protected UnitCost(XNode node) { fromXNode(node); }


    public String getUnit() { return unit.name(); }

    public void setUnit(String u) { unit = TimeUnit.valueOf(u); }


    public FacetStatus getDuration() { return facetStatus; }


    public CostValue getCostValue() { return costValue; }

    public void setCostValue(CostValue value) { costValue = value; }


    public double getCostPerMSec(Map<String, String> dataMap) {
        long msecFactor = 1;
        switch (unit) {
            case hour   : msecFactor *= 60 ;
            case minute : msecFactor *= 60 ;
            case second : msecFactor *= 1000 ;
            case millisecond : return costValue.getAmount(dataMap) / msecFactor;
            default: return costValue.getAmount(dataMap);     // includes 'fixed'
        }
    }


    public XNode toXNode()  {
        XNode node = new XNode("unitcost");
        node.addChildren(costValue.toXNode().getChildren());
        node.addChild("unit", unit.name());
        if (facetStatus != FacetStatus.nil) {
            node.addChild("status", facetStatus.name());
        }
        return node;
    }

    public String toXML() {
        return toXNode().toString();
    }


    public void fromXNode(XNode node) {
        if (node != null) {
            setUnit(node.getChildText("unit"));
            costValue = new CostValue(node);
            String status = node.getChildText("status");
            facetStatus = (status != null) ? FacetStatus.valueOf(status) : FacetStatus.nil;
        }
    }

    public void fromXML(String xml) {
        XNode node = new XNodeParser(true).parse(xml);
        fromXNode(node);
    }
}
