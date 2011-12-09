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
import org.yawlfoundation.yawl.util.StringUtil;

/**
 * Represents an organisational group to which a position (occupied by a participant) may
 * belong. Note that one org group may belong to a larger org group (or alternately, an
 * org group may contain a number of smaller org groups).
 *
 *  @author Michael Adams
 *  v0.1, 03/08/2007
 */

public class OrgGroup extends AbstractResourceAttribute implements Comparable {

    private String _groupName ;
    private GroupType _groupType ;
    private OrgGroup _belongsTo ;
    private String _belongsToID ;               // needed for non-hibernate db backends

    public static enum GroupType {
        GROUP, TEAM, UNIT, BRANCH, DIVISION, CLUSTER, DEPARTMENT
    }


    public OrgGroup() {
        super();
        _groupType = GroupType.GROUP;
    }

    
    public OrgGroup(String groupName, GroupType groupType,
                    OrgGroup belongsTo, String description) {
        super();
        _groupName = groupName ;
        _groupType = groupType ;
        _belongsTo = belongsTo ;
        _description = description ;
    }

    public OrgGroup(String groupName, GroupType groupType,
                    OrgGroup belongsTo, String description, boolean persisting) {
        this(groupName, groupType, belongsTo, description);
        _persisting = persisting ;
    }

    public OrgGroup(Element e) {
        super();
        reconstitute(e);
    }


    public String getGroupName() {
        return _groupName;
    }

    public void setGroupName(String groupName) {
        _groupName = groupName;
    }

    public String getName() { return getGroupName(); }


    public GroupType getGroupType() {
        return _groupType;
    }


    public GroupType getGroupTypeFromString(String name) {
        GroupType gType = null;
        try {
            gType = GroupType.valueOf(name);
        }
        catch (Exception e) {
            // nothing to do - already null
        }
        return gType;
    }
    

    public void setGroupType(GroupType groupType) {
        _groupType = groupType;
    }


    public void setGroupType(String groupTypeStr) {
        GroupType groupType = getGroupTypeFromString(groupTypeStr) ;
        if (groupType != null) {
            _groupType = groupType;
        }    
    }


    public OrgGroup getBelongsTo() {
        return _belongsTo;
    }

    public void setBelongsTo(OrgGroup belongsTo) {
        _belongsTo = belongsTo;
    }

    public boolean setBelongsTo(String ownerID) {
        if (ownerID != null) {
            OrgGroup ownerGroup = getOrgDataSet().getOrgGroup(ownerID);
            if (ownerGroup != null) {
                setBelongsTo(ownerGroup);
                return true;
            }
        }
        return false;
    }

    
    public boolean hasResourceInHierarchy(AbstractResource resource) {
        return hasResource(resource) ||
               ((_belongsTo != null) && _belongsTo.hasResourceInHierarchy(resource)) ;
    }

    // Hibernate Mappings

    public String get_groupType() {
        return _groupType.name() ;
    }

    public void set_groupType(String name) {
        _groupType = GroupType.valueOf(name);
    }

    public boolean equals(Object o) {
        return (o instanceof OrgGroup) && ((OrgGroup) o).getID().equals(_id);
    }

    public int compareTo(Object o) {
        if ((o == null) || (! (o instanceof OrgGroup))) return 1;
        return this.getGroupName().compareTo(((OrgGroup) o).getGroupName());
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder() ;
        xml.append(String.format("<orggroup id=\"%s\">", _id)) ;
        xml.append(StringUtil.wrapEscaped(_groupName, "groupName"));
        xml.append(StringUtil.wrap(get_groupType(), "groupType"));
        xml.append(StringUtil.wrapEscaped(_description, "description"));
        xml.append(StringUtil.wrapEscaped(_notes, "notes"));
        if (_belongsTo !=null)
            xml.append(StringUtil.wrap(_belongsTo.getID(), "belongsToID")) ;
        xml.append("</orggroup>");
        return xml.toString() ;
    }

    public void reconstitute(Element e) {
        super.reconstitute(e);
        setGroupName(JDOMUtil.decodeEscapes(e.getChildText("groupName")));
        set_groupType(e.getChildText("groupType"));
        set_belongsToID(e.getChildText("belongsToID"));
    }

    // Other-than-hibernate mappings

    public String get_belongsToID() {
        return _belongsToID;
    }

    public void set_belongsToID(String belongsToID) {
        _belongsToID = belongsToID;
    }
}
