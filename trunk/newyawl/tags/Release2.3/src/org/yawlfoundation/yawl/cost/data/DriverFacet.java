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
import org.yawlfoundation.yawl.util.XNodeIO;

/**
* @author Michael Adams
* @date 14/10/11
*/
public class DriverFacet implements XNodeIO {

    private long entityID;           // hibernate primary key
    private String id;
    private FacetAspect facetAspect;
    private String name;
    private String value;        // data entity only
    
    
    public DriverFacet() { }
    
    public DriverFacet(XNode node) { fromXNode(node); }


    public long getEntityID() { return entityID; }

    public void setEntityID(long id) { entityID = id;}


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FacetAspect getFacetAspect() {
        return facetAspect;
    }

    public void setFacetAspect(FacetAspect facetAspect) {
        this.facetAspect = facetAspect;
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
        facetAspect = FacetAspect.valueOf(node.getAttributeValue("aspect"));
        name = node.getChildText("name");
        value = node.getChildText("value");
    }

    public XNode toXNode() {
        XNode node = new XNode("facet");
        node.addAttribute("id", id);
        node.addAttribute("aspect", facetAspect.name());
        node.addChild("name", name);
        if (value != null) node.addChild("value", value);
        return node;
    }

    public XNodeIO newInstance(XNode node) {
        return new DriverFacet(node);
    }
}
