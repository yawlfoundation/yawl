/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package au.edu.qut.yawl.admintool.model;

/**
 * 
 * @author Lachlan Aldred
 * Date: 28/09/2005
 * Time: 18:16:40
 * 
 */
public class OrgGroup {
    private String _orgGroupName;
    private String _orgGroupType;
    private int _workingDayStartsAt;
    private int _workingDayFinishesAt;

    public OrgGroup() {
    }

    public String getOrgGroupName() {
        return _orgGroupName;
    }

    public void setOrgGroupName(String orgGroupName) {
        this._orgGroupName = orgGroupName;
    }

    public String getOrgGroupType() {
        return _orgGroupType;
    }

    public void setOrgGroupType(String orgGroupType) {
        this._orgGroupType = orgGroupType;
    }

    public int getWorkingDayStartsAt() {
        return _workingDayStartsAt;
    }

    public void setWorkingDayStartsAt(int workingDayStartsAt) {
        this._workingDayStartsAt = workingDayStartsAt;
    }

    public int getWorkingDayFinishesAt() {
        return _workingDayFinishesAt;
    }

    public void setWorkingDayFinishesAt(int workingDayFinishesAt) {
        this._workingDayFinishesAt = workingDayFinishesAt;
    }
}
