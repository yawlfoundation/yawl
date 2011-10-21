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

import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeIO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 3/10/11
 */
public class CostModel implements XNodeIO {

    public enum EntityType { flow, resource, data }

    private YSpecificationID specID;
    private List<CostMapping> mappings;
    private List<CostDriver> drivers;
    private List<CostFunction> functions;

    public CostModel() {
        mappings = new ArrayList<CostMapping>();
        drivers = new ArrayList<CostDriver>();
        functions = new ArrayList<CostFunction>();
    }


    public CostModel(XNode model) {
        this();
        fromXNode(model);
    }


    public YSpecificationID getSpecID() {
        return specID;
    }

    public void setSpecID(YSpecificationID specID) {
        this.specID = specID;
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
        setSpecID(node.getChild("specificationid"));
        XNode mappingsNode = node.getChild("mappings");
        if (mappingsNode != null) {
            mappingsNode.populateListWithChildren(mappings, new CostMapping());
        }
        XNode driversNode = node.getChild("drivers");
        if (driversNode != null) {
            driversNode.populateListWithChildren(drivers, new CostDriver());
        }
        XNode functionsNode = node.getChild("functions");
        if (functionsNode != null) {
            functionsNode.populateListWithChildren(functions, new CostFunction());
        }
    }


    public XNode toXNode() {
        XNode node = new XNode("costmodel");
        if (specID != null) node.addContent(specID.toXML());
        if (! mappings.isEmpty()) {
            node.addChild("mappings").addList(mappings);
        }
        if (! drivers.isEmpty()) {
            node.addChild("drivers").addList(drivers);
        }
        if (! functions.isEmpty()) {
            node.addChild("functions").addList(functions);
        }
        return node;
    }


    public XNodeIO newInstance(XNode node) {
        return new CostModel(node);
    }

}
