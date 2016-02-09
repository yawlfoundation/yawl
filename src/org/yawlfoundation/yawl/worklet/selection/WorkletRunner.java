package org.yawlfoundation.yawl.worklet.selection;

import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.worklet.rdr.RuleType;
import org.yawlfoundation.yawl.worklet.support.Persister;

/**
 * Basic metadata for a currently running worklet instance
 *
 * @author Michael Adams
 * @date 3/09/15
 */
public class WorkletRunner extends AbstractRunner {

    protected String _parentCaseID;
    protected YSpecificationID _specID;

    public WorkletRunner() { }

    public WorkletRunner(XNode node) { fromXNode(node); }


    public WorkletRunner(String workletCaseID, YSpecificationID workletSpecID,
                         WorkItemRecord wir, RuleType ruleType) {
        super(workletCaseID, wir, ruleType);
        _specID = workletSpecID;

    }


    public YSpecificationID getWorkletSpecID() { return _specID; }

    public void setParentCaseID(String caseID) { _parentCaseID = caseID; }

    public String getParentCaseID() {
        return getWir() != null ? _wir.getRootCaseID() : _parentCaseID;
    }


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
        if (getParentCaseID() != null) {
            root.addChild("parentcaseid", getParentCaseID());
        }
        root.addChild(_specID.toXNode());
        return root;
    }


    public void fromXNode(XNode node) {
        super.fromXNode(node);
        _parentCaseID = node.getChildText("parentcaseid");
        _specID = new YSpecificationID(node.getChild("specificationid"));
    }

}
