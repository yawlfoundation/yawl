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

public class MappingIdentifier {

    String id;
    MappingIdType _type;
    String term;

    public MappingIdentifier() { }

    public MappingIdentifier(XNode node) {
        fromXNode(node);
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public MappingIdType getType() { return _type; }

    public void setType(MappingIdType type) { this._type = type; }

    public String getTerm() { return term; }

    public void setTerm(String name) { this.term = name; }


    public XNode toXNode() {
        XNode node = new XNode(_type.name());
        node.addAttribute("id", id);
        node.addAttribute("term", term);
        return node;
    }


    public void fromXNode(XNode node) {
        _type = MappingIdType.valueOf(node.getName());
        id = node.getAttributeValue("id");
        term = node.getAttributeValue("term");
    }


    // for hibernate

    // for hibernate
    private int get_type() {
        return _type != null ? _type.ordinal() : -1;
    }

    private void setFacetStatus(int ordinal) {
        _type = (ordinal == -1) ? null : MappingIdType.values()[ordinal];
    }
}