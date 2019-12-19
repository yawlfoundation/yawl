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

package org.yawlfoundation.yawl.cost.data;

import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeIO;

/**
 * @author Michael Adams
 * @date 3/10/11
 */
public class CostMapping implements XNodeIO {

    private long mappingID;                                        // hibernate primary key
    private MappingIdentifier cost;
    private MappingIdentifier workflow;
    private FacetAspect mappingType;

    public CostMapping() { }

    public CostMapping(XNode node) {
        this();
        fromXNode(node);
    }


    private long getMappingID() { return mappingID; }

    private void setMappingID(long id) { mappingID = id; }


    public String toXML() {
        return toXNode().toPrettyString();
    }


    public void fromXNode(XNode node) {
        cost = new MappingIdentifier(node.getChild("cost"));
        workflow = new MappingIdentifier(node.getChild("workflow"));
        String aspect = node.getAttributeValue("aspect");
        if (aspect != null) {
            mappingType = FacetAspect.valueOf(aspect);
        }
    }


    public XNode toXNode() {
        XNode node = new XNode("mapping");
        node.addChild(cost.toXNode());
        node.addChild(workflow.toXNode());
        if (mappingType != null) {
            node.addAttribute("aspect", mappingType.name());
        }
        return node;
    }


    public XNodeIO newInstance(XNode node) {
        return new CostMapping(node);
    }
}
