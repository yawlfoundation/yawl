package org.yawlfoundation.yawl.worklet.support;

import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;
import org.yawlfoundation.yawl.worklet.rdr.RuleType;

/**
 * @author Michael Adams
 * @date 1/09/15
 */
public class WorkletInstanceSummary {

    private String workletCaseID;
    private YSpecificationID workletSpecID;
    private long workletStartTime;
    private String parentCaseID;
    private YSpecificationID parentSpecID;
    private long parentStartTime;
    private String parentTaskID;
    private RuleType ruleType;

    public String getWorkletCaseID() {
        return workletCaseID;
    }

    public void setWorkletCaseID(String workletCaseID) {
        this.workletCaseID = workletCaseID;
    }

    public YSpecificationID getWorkletSpecID() {
        return workletSpecID;
    }

    public void setWorkletSpecID(YSpecificationID workletSpecID) {
        this.workletSpecID = workletSpecID;
    }

    public long getWorkletStartTime() {
        return workletStartTime;
    }

    public void setWorkletStartTime(long workletStartTime) {
        this.workletStartTime = workletStartTime;
    }

    public String getParentCaseID() {
        return parentCaseID;
    }

    public void setParentCaseID(String parentCaseID) {
        this.parentCaseID = parentCaseID;
    }

    public YSpecificationID getParentSpecID() {
        return parentSpecID;
    }

    public void setParentSpecID(YSpecificationID parentSpecID) {
        this.parentSpecID = parentSpecID;
    }

    public long getParentStartTime() {
        return parentStartTime;
    }

    public void setParentStartTime(long parentStartTime) {
        this.parentStartTime = parentStartTime;
    }

    public String getParentTaskID() {
        return parentTaskID;
    }

    public void setParentTaskID(String parentTaskID) {
        this.parentTaskID = parentTaskID;
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }


    public void fromXML(String xml) throws IllegalArgumentException {
        XNode root = new XNodeParser().parse(xml);
        if (root == null) {
            throw new IllegalArgumentException("XML String is malformed or missing.");
        }
        workletCaseID = root.getChildText("wcaseid");
        workletSpecID = buildSpecID(root.getChild("wspecid"));
        workletStartTime = StringUtil.strToLong(root.getChildText("wstart"), 0);
        parentCaseID = root.getChildText("pcaseid");
        parentSpecID = buildSpecID(root.getChild("pspecid"));
        parentStartTime = StringUtil.strToLong(root.getChildText("pstart"), 0);
        parentTaskID = root.getChildText("ptask");
        ruleType = RuleType.fromString(root.getChildText("ruletype"));
    }


    public XNode toXNode() {
        XNode root = new XNode("workletinstance");
        root.addChild("wcaseid", workletCaseID);
        root.addChild("wspecid").addChild(workletSpecID.toXNode());
        root.addChild("wstart", workletStartTime);
        root.addChild("pcaseid", parentCaseID);
        root.addChild("pspecid").addChild(parentSpecID.toXNode());
        root.addChild("pstart", parentStartTime);
        root.addChild("ptask", parentTaskID);
        root.addChild("ruletype", ruleType.name());
        return root;
    }

    public String toXML()  {
        return toXNode().toPrettyString();
    }


    private YSpecificationID buildSpecID(XNode idNode) {
        return new YSpecificationID(idNode.getChildText("identifier"),
                idNode.getChildText("version"), idNode.getChildText("uri"));
    }

}
