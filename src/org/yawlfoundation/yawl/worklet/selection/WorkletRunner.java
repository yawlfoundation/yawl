package org.yawlfoundation.yawl.worklet.selection;

import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.worklet.rdr.RuleType;

/**
 * Basic metadata for a currently running worklet instance
 *
 * @author Michael Adams
 * @date 3/09/15
 */
public class WorkletRunner extends AbstractRunner {

    protected YSpecificationID _specID;

    public WorkletRunner() { }

    public WorkletRunner(XNode node) { fromXNode(node); }


    public WorkletRunner(String workletCaseID, YSpecificationID workletSpecID,
                         WorkItemRecord wir, RuleType ruleType) {
        super(workletCaseID, wir, ruleType);
        _specID = workletSpecID;
    }


    public YSpecificationID getWorkletSpecID() { return _specID; }


    public XNode toXNode() {
        XNode root = super.toXNode();
        root.addChild(_specID.toXNode());
        return root;
    }


    public void fromXNode(XNode node) {
        super.fromXNode(node);
        _specID = new YSpecificationID(node.getChild("specificationid"));
    }

}
