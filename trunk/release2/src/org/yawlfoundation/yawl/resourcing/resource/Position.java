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

package org.yawlfoundation.yawl.resourcing.resource;

import org.jdom2.Element;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

/**
 * Represents an organisational position that may be held by a participant.
 *
 *  @author Michael Adams
 *  v0.1, 09/08/2007
 */

public class Position extends AbstractResourceAttribute implements Comparable {

    private String _positionID ;
    private String _title;
    private OrgGroup _orgGroup;
    private Position _reportsTo;

    // needed for non-hibernate db backends
    private String _orgGroupID ;
    private String _reportsToID ;


    public Position() { super(); }


    public Position(String title) {
        super();
        _title = title;
    }

    public Position(String positionID, String title, String desription,
                    OrgGroup orgGroup, Position reportsTo) {
        this(title) ;
        _positionID = positionID;
        _description = desription;
        _orgGroup = orgGroup ;
        _reportsTo = reportsTo;
    }

    public Position(Element e) {
        super();
        reconstitute(e);
    }


    public void setLabel(String label) { setTitle(label); }

    public String getPositionID() {
        return _positionID;
    }

    public void setPositionID(String positionID) {
        _positionID = positionID;
    }

    public String getTitle() {
        return _title;
    }

    public void setTitle(String title) {
        _title = title;
    }

    public String getName() { return getTitle(); }


    public Position getReportsTo() {
        return _reportsTo;
    }

    public void setReportsTo(Position reportsTo) {
        _reportsTo = reportsTo;
    }

    public boolean setReportsTo(String reportsToID) {
        if (reportsToID != null) {
            Position owner = getOrgDataSet().getPosition(reportsToID);
            if (owner != null) {
                setReportsTo(owner);
                return true;
            }
        }
        return false;
    }



    public OrgGroup getOrgGroup() {
        return _orgGroup;
    }

    public void setOrgGroup(OrgGroup orgGroup) {
        _orgGroup = orgGroup;
    }

    public boolean setOrgGroup(String groupID) {
        if (groupID != null) {
            OrgGroup group = getOrgDataSet().getOrgGroup(groupID);
            if (group != null) {
                setOrgGroup(group);
                return true;
            }
        }
        return false;
    }

    public boolean reportsTo(Position boss) {
        return ((_reportsTo != null) && (_reportsTo.equals(boss)));
    }


    public boolean ultimatelyReportsTo(Position manager) {
        boolean result = reportsTo(manager);
        if (! result)
             result = ((_reportsTo != null) && _reportsTo.ultimatelyReportsTo(manager)) ;
        return result ;
    }

    // two Position objects are equal if they have the same id
    public boolean equals(Object o) {
        return (o instanceof Position) && ((Position) o).getID().equals(_id);
    }

    public int compareTo(Object o) {
        if ((o == null) || (! (o instanceof Position))) return 1;
        return this.getTitle().compareTo(((Position) o).getTitle());
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder() ;
        xml.append(String.format("<position id=\"%s\">", _id)) ;
        xml.append(StringUtil.wrapEscaped(_title, "title"));
        xml.append(StringUtil.wrapEscaped(_positionID, "positionid"));
        xml.append(StringUtil.wrapEscaped(_description, "description"));
        xml.append(StringUtil.wrapEscaped(_notes, "notes"));
        if (_orgGroup != null)
            xml.append(StringUtil.wrap(_orgGroup.getID(), "orggroupid"));
        if (_reportsTo != null)
            xml.append(StringUtil.wrapEscaped(_reportsTo.getID(), "reportstoid"));
        xml.append("</position>");
        return xml.toString() ;
    }

    

    public void reconstitute(Element e) {
        super.reconstitute(e);
        setPositionID(JDOMUtil.decodeEscapes(e.getChildText("positionid")));
        setTitle(JDOMUtil.decodeEscapes(e.getChildText("title")));
        set_reportsToID(JDOMUtil.decodeEscapes(e.getChildText("reportstoid")));
        set_orgGroupID(e.getChildText("orggroupid"));
    }

    // Other-than-hibernate mappings

    public String get_orgGroupID() { return _orgGroupID; }

    public void set_orgGroupID(String orgGroupID) { _orgGroupID = orgGroupID; }

    public String get_reportsToID() { return _reportsToID; }

    public void set_reportsToID(String reportsToID) { _reportsToID = reportsToID; }
}
