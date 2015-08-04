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

import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeIO;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 3/10/11
 */
public class CostModel implements XNodeIO {

    private long modelID;                                           // hibernate primary key
    private String id;
    private YSpecificationID specID;
    private Set<CostMapping> mappings;
    private Set<CostDriver> drivers;
    private Set<CostFunction> functions;

    public CostModel() {
        mappings = new HashSet<CostMapping>();
        drivers = new HashSet<CostDriver>();
        functions = new HashSet<CostFunction>();
    }


    public CostModel(XNode model) {
        this();
        fromXNode(model);
    }


    private long getModelID() { return modelID; }

    private void setModelID(long id) { modelID = id; }

    
    public String getId() { return id; }
    
    public void setId(String id) { this.id = id; }


    public YSpecificationID getSpecID() {
        return specID;
    }

    public void setSpecID(YSpecificationID specID) {
        this.specID = specID;
    }

    public Set<CostDriver> getDrivers() {
        return drivers;
    }

    public void setSpecID(XNode node) {
        if (node != null) {
            String id = node.getChildText("identifier");
            String version = node.getChildText("version");
            String uri = node.getChildText("uri");
            setSpecID(new YSpecificationID(id, version, uri));
        }
    }


    public String toXML() {
        return toXNode().toPrettyString();
    }


    public void fromXNode(XNode node) {
        setSpecID(node.getChild("processid"));
        id = node.getAttributeValue("id");
        XNode mappingsNode = node.getChild("mappings");
        if (mappingsNode != null) {
            mappingsNode.populateCollection(mappings, new CostMapping());
        }
        XNode driversNode = node.getChild("drivers");
        if (driversNode != null) {
            driversNode.populateCollection(drivers, new CostDriver());
        }
        XNode functionsNode = node.getChild("functions");
        if (functionsNode != null) {
            functionsNode.populateCollection(functions, new CostFunction());
        }
    }


    public XNode toXNode() {
        XNode node = new XNode("costmodel");
        node.addAttribute("id", id);
        if (specID != null) {
            XNode specNode = node.addChild("processid");
            specNode.addChild("identifier", specID.getIdentifier());
            specNode.addChild("version", specID.getVersion());
            specNode.addChild("uri", specID.getUri());
        }
        if (! mappings.isEmpty()) {
            node.addChild("mappings").addCollection(mappings);
        }
        if (! drivers.isEmpty()) {
            node.addChild("drivers").addCollection(drivers);
        }
        if (! functions.isEmpty()) {
            node.addChild("functions").addCollection(functions);
        }
        return node;
    }


    public XNodeIO newInstance(XNode node) {
        return new CostModel(node);
    }

}
