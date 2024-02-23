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

/**
* @author Michael Adams
* @date 14/10/11
*/
public class DriverMetaData {

    String name;
    String description;
    String type;

    public DriverMetaData() { }

    public DriverMetaData(XNode node) { fromXNode(node); }


    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }

    public void setDescription(String description) {
        this.description = description;
    }

    public XNode toXNode() {
        XNode node = new XNode("metadata");
        node.addChild("name", name);
        node.addChild("description", description);
        node.addChild("type", type);
        return node;
    }

    public void fromXNode(XNode node) {
        name = node.getChildText("name");
        description = node.getChildText("description");
        type = node.getChildText("type");
    }
}
