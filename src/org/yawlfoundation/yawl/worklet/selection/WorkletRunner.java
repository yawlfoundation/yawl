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

package org.yawlfoundation.yawl.worklet.selection;

import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.worklet.rdr.RuleType;
import org.yawlfoundation.yawl.worklet.support.Persister;

/**
 * Metadata for a currently running worklet instance
 *
 * @author Michael Adams
 * @date 3/09/15
 */
public class WorkletRunner extends AbstractRunner {

    protected String _parentCaseID;
    protected YSpecificationID _specID;
    protected YSpecificationID _parentSpecID;

    public WorkletRunner() { }

    public WorkletRunner(XNode node) { fromXNode(node); }


    public WorkletRunner(String workletCaseID, YSpecificationID workletSpecID,
                         WorkItemRecord wir, RuleType ruleType) {
        super(workletCaseID, wir, ruleType);
        _specID = workletSpecID;
        if (_wir != null) {
            _parentSpecID = new YSpecificationID(wir);
        }
    }


    public YSpecificationID getWorkletSpecID() { return _specID; }


    public void setParentCaseID(String caseID) { _parentCaseID = caseID; }

    public String getParentCaseID() {
        return getWir() != null ? _wir.getRootCaseID() : _parentCaseID;
    }


    public void setParentSpecID(YSpecificationID specID) { _parentSpecID = specID; }

    public YSpecificationID getParentSpecID() { return _parentSpecID; }



    public void logLaunchEvent() {
        WorkItemRecord wir = getWir();
        if (wir != null) {
            logLaunchEvent(new YSpecificationID(wir), wir.getDataListString());
        }
    }


    public void logLaunchEvent(YSpecificationID specID, String data) {
        Persister.insert(new LaunchEvent(
                specID, getTaskID(), getWorkItemID(), _ruleType,
                getParentCaseID(), _caseID, data
        ));
    }


    public XNode toXNode() {
        XNode root = super.toXNode();
        if (_parentCaseID != null) {
            root.addChild("parentcaseid", _parentCaseID);
        }
        root.addChild(_specID.toXNode());
        if (_parentSpecID != null) {
            XNode pSpecNode = root.addChild("parentspecid");
            pSpecNode.addChild(_parentSpecID.toXNode());
        }
        return root;
    }


    public void fromXNode(XNode node) {
        super.fromXNode(node);
        _parentCaseID = node.getChildText("parentcaseid");
        _specID = new YSpecificationID(node.getChild("specificationid"));
        XNode pSpecNode = node.getChild("parentspecid").getChild("specificationid");
        if (pSpecNode != null) {
            _parentSpecID = new YSpecificationID(pSpecNode);
        }
    }

}
