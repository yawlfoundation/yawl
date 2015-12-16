package org.yawlfoundation.yawl.worklet.selection;

import org.jdom2.Element;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.worklet.WorkletService;
import org.yawlfoundation.yawl.worklet.rdr.RuleType;
import org.yawlfoundation.yawl.worklet.support.Persister;

import java.io.IOException;

/**
 * @author Michael Adams
 * @date 3/09/15
 */
public abstract class AbstractRunner {

    private long _id;                                             // for persistence
    protected String _wirID;                                      // for persistence

    // the worklet case id for WorkletRunners, the parent case id for ExletRunners
    protected String _caseID;

    protected WorkItemRecord _wir;
    protected RuleType _ruleType;
    protected long _ruleNodeId;                 // the node that triggered this runner

    // for case level runners
    protected String _parentCaseID;
    protected YSpecificationID _parentSpecID;
    protected String _dataString;

    public AbstractRunner() { }

    public AbstractRunner(XNode node) { fromXNode(node); }

    public AbstractRunner(String caseID, WorkItemRecord wir, RuleType ruleType) {
        _caseID = caseID;
        _wir = wir;
        if (wir != null) _wirID = wir.getID();                       // for persistence
        _ruleType = ruleType;
    }


    public long getID() { return _id; }

    public String getCaseID() { return _caseID; }

    public String getWorkItemID() { return _wirID; }

    public RuleType getRuleType() { return _ruleType; }

    public String getTaskID() {
        return getWir() != null ? _wir.getTaskID() : null;
    }

    public void setParentCaseID(String caseID) { _parentCaseID = caseID; }

    public void setParentSpecID(YSpecificationID specID) { _parentSpecID = specID; }

    public void setData(Element data) {
        _dataString = JDOMUtil.elementToString(data);
    }

    public void setRuleNodeId(long nodeId) { _ruleNodeId = nodeId; }

    public long getRuleNodeId() { return _ruleNodeId; }


    public String getParentCaseID() {
        return getWir() != null ? _wir.getRootCaseID() : _parentCaseID;
    }


    public YSpecificationID getParentSpecID() {
        return getWir() != null ? new YSpecificationID(_wir) : _parentSpecID;
    }


    public String getParentWorkItemID() {
        return getWir() != null ? _wir.getParentID() : null;
    }

    public Element getWorkItemData() {
        return getWir() != null ? _wir.getDataList() : null;
    }

    public String getDataListString() {
        return getWir() != null ? _wir.getDataListString() : _dataString;
    }

    // will be null after restart - get it from the engine (for item-level handlers only)
    public WorkItemRecord getWir() {
        if (_wirID != null && _wir == null) {
            try {
                _wir = WorkletService.getInstance().getEngineStoredWorkItem(_wirID);
            }
            catch (IOException ioe) {
                // fall through
            }
        }
        return _wir;
    }


    public XNode toXNode() {
        XNode root = new XNode("runner");
        root.addChild("caseid", _caseID);
        if (getWir() != null) root.addContent(_wir.toXML());
        root.addChild("parentcaseid", getParentCaseID());
        XNode pSpecNode = root.addChild("parentspecid");
        if (_parentSpecID != null) {
            pSpecNode.addChild(_parentSpecID.toXNode());
        }
        if (_dataString != null) {
            root.addChild("datastring", _dataString);
        }
        root.addChild("ruletype", _ruleType.toString());
        root.addChild("ruleNode", _ruleNodeId);
        return root;
    }


    public void fromXNode(XNode node) {
        _caseID = node.getChildText("caseid");
        _wir = Marshaller.unmarshalWorkItem(node.getChild("workItemRecord").toString());
        if (_wir != null) {
            _wirID = _wir.getID();
        }
        _parentCaseID = node.getChildText("parentcaseid");
        XNode pSpecNode = node.getChild("parentspecid").getChild("specificationid");
        if (pSpecNode != null) {
            _parentSpecID = new YSpecificationID(pSpecNode);
        }
        _dataString = node.getChildText("datastring");
        _ruleType = RuleType.fromString(node.getChildText("ruletype"));
        _ruleNodeId = StringUtil.strToLong(node.getChildText("ruleNode"), -1);
    }


    public void logLaunchEvent() {
        Persister.insert(new LaunchEvent(
                getParentSpecID(), getTaskID(), getWorkItemID(), _ruleType,
                getParentCaseID(), _caseID, getDataListString()
        ));
    }


    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Case: ").append(_caseID).append("; ");
        if (_wir != null) {
            s.append("Item: ").append(_wirID).append("; ");
        }
        s.append("Type: ").append(_ruleType.toString());
        return s.toString();
    }


    private void setCaseID(String id) { _caseID = id; }                // for persistence

    private void setID(long id) { _id = id; }

}
