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

import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 3/10/11
 */
public class CostDriver implements XNodeIO {

    private long driverID;                                // hibernate primary key
    private String id;
    private DriverMetaData metadata;
    private Set<DriverEntity> entities;
    private Set<CostType> costTypes;
    private UnitCost unitCost;


    public CostDriver() {
        entities = new HashSet<DriverEntity>();
        costTypes = new HashSet<CostType>();
    }

    public CostDriver(XNode node) {
        this();
        fromXNode(node);
    }


    private long getDriverID() { return driverID; }

    private void setDriverID(long id) { driverID = id; }
    

    public String getID() { return id; }


    public UnitCost getUnitCost() { return unitCost; }
    
    public Set<DriverEntity> getEntities() { return entities; }
    
    public Set<CostType> getCostTypes() { return costTypes; }


    public String toXML() {
        return toXNode().toPrettyString();
    }


    public void fromXNode(XNode node) {
        id = node.getAttributeValue("id");
        metadata = new DriverMetaData(node.getChild("metadata"));
        node.getChild("entities").populateCollection(entities, new DriverEntity());
        node.getChild("costtypes").populateCollection(costTypes, new CostType());
        unitCost = new UnitCost(node.getChild("unitcost"));
    }

    public XNode toXNode() {
        XNode node = new XNode("driver");
        node.addAttribute("id", id);
        node.addChild(metadata.toXNode());
        node.addChild("entities").addCollection(entities);
        node.addChild("costtypes").addCollection(costTypes);
        node.addChild(unitCost.toXNode());
        return node;
    }

    public XNodeIO newInstance(XNode node) {
        return new CostDriver(node);
    }
}
