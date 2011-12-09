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

package org.yawlfoundation.yawl.resourcing.resource;

import org.jdom.Element;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.XNode;

/**
 * A role that a resource might perform.
 *
 *  @author Michael Adams
 *  v0.1, 21/08/2007
 */

public class Role extends AbstractResourceAttribute implements Comparable {

    private String _role ;
    private Role _belongsTo ;                   // is this role part of a larger group
    private String _belongsToID ;               // needed for non-hibernate db backends


    public Role() { super() ; }

    public Role(String role) {
        super() ;
        _role = role;
    }

    public Role(Element e) {
        super();
        reconstitute(e);
    }


    public String getName() { return _role; }

    public void setName(String role) {
        _role = role;
    }


    public Role getOwnerRole() { return _belongsTo; }

    public void setOwnerRole(Role owner) {
        _belongsTo = owner;
    }

    public boolean setOwnerRole(String ownerRoleID) {
        if (ownerRoleID != null) {
            Role ownerRole = getOrgDataSet().getRole(ownerRoleID);
            if (ownerRole != null) {
                setOwnerRole(ownerRole);
                return true;
            }
        }
        return false;
    }


    public boolean belongsTo(Role owner) {
        return ((_belongsTo != null) && (_belongsTo.equals(owner)));
    }


    public boolean ultimatelyBelongsTo(Role owner) {
        boolean result = belongsTo(owner);
        if (! result)
             result = ((_belongsTo != null) && _belongsTo.ultimatelyBelongsTo(owner)) ;
        return result ;
    }


    public boolean equals(Object o) {
        return (o instanceof Role) && ((Role) o).getID().equals(_id);
    }

    public int compareTo(Object o) {
        if ((o == null) || (! (o instanceof Role))) return 1;
        return this.getName().compareTo(((Role) o).getName());
    }
   

    public String toXML() {
        XNode xml = new XNode("role");
        xml.addAttribute("id", String.valueOf(_id));
        xml.addChild("name", _role, true);
        xml.addChild("description", _description, true);
        xml.addChild("notes", _notes, true);
        if (_belongsTo !=null) xml.addChild("belongsToID", _belongsTo.getID());
        return xml.toString() ;
    }

    public void reconstitute(Element e) {
        super.reconstitute(e);
        setName(JDOMUtil.decodeEscapes(e.getChildText("name")));
        set_belongsToID(e.getChildText("belongsToID"));
    }

    // Other-than-hibernate mappings

    public String get_belongsToID() { return _belongsToID; }

    public void set_belongsToID(String belongsToID) { _belongsToID = belongsToID; }

}
