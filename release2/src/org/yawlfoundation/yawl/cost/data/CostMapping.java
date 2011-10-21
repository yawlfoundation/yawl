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

import java.util.List;

/**
 * @author Michael Adams
 * @date 3/10/11
 */
public class CostMapping implements XNodeIO {

    private MappingIdentifier left;
    private MappingIdentifier right;
    private CostModel.EntityType mappingType;

    public CostMapping() { }

    public CostMapping(XNode node) {
        this();
        fromXNode(node);
    }


    public String toXML() {
        return toXNode().toPrettyString();
    }


    public void fromXNode(XNode node) {
        List<XNode> idNodes = node.getChildren("identifier");
        left = new MappingIdentifier(idNodes.get(0));
        right = new MappingIdentifier(idNodes.get(1));
        mappingType = CostModel.EntityType.valueOf(node.getAttributeValue("perspective"));
    }


    public XNode toXNode() {
        XNode node = new XNode("mapping");
        node.addChild(left.toXNode());
        node.addChild(right.toXNode());
        node.addAttribute("perspective", mappingType.name());
        return node;
    }


    public XNodeIO newInstance(XNode node) {
        return new CostMapping(node);
    }
}
