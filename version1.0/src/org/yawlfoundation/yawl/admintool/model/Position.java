/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.admintool.model;

/**
 * 
 * @author Lachlan Aldred
 * Date: 28/09/2005
 * Time: 18:15:50
 * 
 */
public class Position {
    private String _positionID;
    private String _positionName;
    private String _belongsToOrgGroup;

    public Position() {
    }

    public String getPositionID() {
        return _positionID;
    }

    public void setPositionID(String positionID) {
        _positionID = positionID;
    }

    public String getPositionName() {
        return _positionName;
    }

    public void setPositionName(String positionName) {
        _positionName = positionName;
    }

    public String getBelongsToOrgGroup() {
        return _belongsToOrgGroup;
    }

    public void setBelongsToOrgGroup(String belongsToOrgGroup) {
        _belongsToOrgGroup = belongsToOrgGroup;
    }
}
