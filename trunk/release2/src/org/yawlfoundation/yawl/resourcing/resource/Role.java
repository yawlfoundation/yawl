/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
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
            Role ownerRole = _resMgr.getOrgDataSet().getRole(ownerRoleID);
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
