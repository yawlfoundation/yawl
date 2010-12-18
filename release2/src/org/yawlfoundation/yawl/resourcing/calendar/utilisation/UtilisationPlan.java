/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing.calendar.utilisation;

import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 6/10/2010
 */
public class UtilisationPlan extends StatusMessage {

    private String _caseID;
    private List<Activity> _activityList;

    public UtilisationPlan() { }

    public UtilisationPlan(String caseID) {
        _caseID = caseID;
    }

    public UtilisationPlan(XNode node) {
        fromXNode(node);
    }


    /************************************************************************/

    public String getCaseID() { return _caseID; }

    public void setCaseID(String id) { _caseID = id; }


    public List<Activity> getActivityList() { return _activityList; }

    public void setActivityList(List<Activity> list) {
        _activityList = list;
    }

    public boolean addActivity(Activity a) {
        if (! hasActivities()) _activityList = new ArrayList<Activity>();
        return _activityList.add(a);
    }

    public boolean removeActivity(Activity a) {
        return (hasActivities()) && _activityList.remove(a);
    }

    public Activity getActivity(String name) {
        if ((name != null) && (hasActivities())) {
            for (Activity activity : _activityList) {
                if (activity.getName().equals(name)) {
                    return activity;
                }
            }
        }
        return null;
    }

    public boolean hasActivities() {
        return _activityList != null;
    }


    public boolean hasErrors() {
        return hasError() || activitiesHaveErrors();
    }


    public boolean activitiesHaveErrors() {
        if (hasActivities()) {
            for (Activity activity : _activityList) {
                if (activity.hasErrors()) {
                    return true;
                }
            }
        }
        return false;
    }


    public String toXML() {
        return toXNode().toString();
    }

    public void fromXML(String xml) {
        fromXNode(new XNodeParser().parse(xml));
    }

    public XNode toXNode() {
        XNode node = new XNode("ResourceUtilisationPlan");
        addAttributes(node);
        node.addChild("CaseId", _caseID);
        if (_activityList != null) {
           for (Activity activity : _activityList) {
                node.addChild(activity.toXNode());
            }
        }
        return node;
    }

    public void fromXNode(XNode node) {
        super.fromXNode(node);
        setCaseID(node.getChildText("CaseId"));
        for (XNode activityNode : node.getChildren("Activity")) {
            addActivity(new Activity(activityNode));
        }
    }
}

