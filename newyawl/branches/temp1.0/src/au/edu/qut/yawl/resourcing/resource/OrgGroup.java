/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing.resource;

import au.edu.qut.yawl.resourcing.ResourceManager;

/**
 * Represents an organisation group to which a position (occupied by a participant) may
 * belong. Note that one org group may belong to a larger org group (or conversely, an
 * org group may contain a number of smaller org groups).
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.1, 03/08/2007
 */

public class OrgGroup extends AbstractResourceAttribute {

    private String _groupName ;
    private GroupType _groupType ;
    private OrgGroup _belongsTo ;
    private String _belongsToID ;               // needed for non-hibernate db backends

    public static enum GroupType {
        GROUP, TEAM, UNIT, BRANCH, DIVISION, CLUSTER, DEPARTMENT
    }


    public OrgGroup() { super(); }

    public OrgGroup(String groupName, GroupType groupType,
                    OrgGroup belongsTo, String description) {
        _groupName = groupName ;
        _groupType = groupType ;
        _belongsTo = belongsTo ;
        _description = description ;
    }



    public String getGroupName() {
        return _groupName;
    }

    public void setGroupName(String groupName) {
        _groupName = groupName;
    }

    public GroupType getGroupType() {
        return _groupType;
    }

    public void setGroupType(GroupType groupType) {
        _groupType = groupType;
    }


    public OrgGroup getBelongsTo() {
        return _belongsTo;
    }

    public void setBelongsTo(OrgGroup belongsTo) {
        _belongsTo = belongsTo;
    }

    // Hibernate Mappings

    public String get_groupType() {
        return _groupType.name() ;
    }

    public void set_groupType(String name) {
        _groupType = GroupType.valueOf(name);
    }


    // Other-than-hibernate mappings

    public String get_belongsToID() {
        return _belongsToID;
    }

    public void set_belongsToID(String belongsToID) {
        _belongsToID = belongsToID;
    }
}
