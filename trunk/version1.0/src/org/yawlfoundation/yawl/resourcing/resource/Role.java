/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.resource;

import org.jdom.Element;

/**
 * A role that a resource might perform.
 *
 *  @author Michael Adams
 *  v0.1, 21/08/2007
 */

public class Role extends AbstractResourceAttribute {

    private String _role ;
    private Role _belongsTo ;                   // is this role part of a larger group
    private String _belongsToID ;               // needed for non-hibernate db backends


    public Role() { super() ; }

    public Role(String role) {
        super() ;
        _role = role;
    }


    public String getName() { return _role; }

    public void setName(String role) { _role = role; }


    public Role getOwnerRole() { return _belongsTo; }

    public void setOwnerRole(Role owner) { _belongsTo = owner; }

    public boolean equals(Object o) {
        return (o instanceof Role) && ((Role) o).getID().equals(_id);
    }
   

    public String getSummaryXML() {
        StringBuilder xml = new StringBuilder() ;
        xml.append(String.format("<role id=\"%s\">", _id)) ;
        xml.append(wrap(_role, "name"));
        xml.append(wrap(_description, "description"));
        xml.append("</role>");
        return xml.toString() ;
    }

        public void reconstitute(Element e) {
        super.reconstitute(e);
        setName(e.getChildText("name"));
    }

    // Other-than-hibernate mappings

    public String get_belongsToID() { return _belongsToID; }

    public void set_belongsToID(String belongsToID) { _belongsToID = belongsToID; }

}
