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

/**
* @author Michael Adams
* @date 14/10/11
*/
public class DriverEntity implements XNodeIO {

    private long entityID;           // hibernate primary key
    private String id;
    private EntityType entityType;
    private String name;
    private String value;        // data entity only
    
    
    public DriverEntity() { }
    
    public DriverEntity(XNode node) { fromXNode(node); }


    public long getEntityID() { return entityID; }

    public void setEntityID(long id) { entityID = id;}


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void fromXNode(XNode node) {
        id = node.getAttributeValue("id");
        entityType = EntityType.valueOf(node.getAttributeValue("perspective"));
        name = node.getChildText("name");
        value = node.getChildText("value");
    }

    public XNode toXNode() {
        XNode node = new XNode("entity");
        node.addAttribute("id", id);
        node.addAttribute("perspective", entityType.name());
        node.addChild("name", name);
        if (value != null) node.addChild("value", value);
        return node;
    }

    public XNodeIO newInstance(XNode node) {
        return new DriverEntity(node); 
    }
}
