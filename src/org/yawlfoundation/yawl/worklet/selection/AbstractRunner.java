package org.yawlfoundation.yawl.worklet.selection;

import org.jdom2.Element;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.worklet.WorkletService;
import org.yawlfoundation.yawl.worklet.rdr.RuleType;
import org.yawlfoundation.yawl.worklet.support.EngineClient;

import java.io.IOException;

/**
 * @author Michael Adams
 * @date 3/09/15
 */
public abstract class AbstractRunner {

    private long _id;                                             // for persistence
    protected String _wirID;                                      // for persistence

    // for WorkletRunners, the case id of the launched worklet
    // for ExletRunners, the id of the case the raised the exlet
    protected String _caseID;

    protected RuleType _ruleType;
    protected WorkItemRecord _wir;              // can be null for case-level exception
    protected long _ruleNodeId;                 // the node that triggered this runner
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


    public void setRuleNodeID(long nodeId) { _ruleNodeId = nodeId; }

    public long getRuleNodeID() { return _ruleNodeId; }


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
                EngineClient client = WorkletService.getInstance().getEngineClient();
                if (client != null) {
                    _wir = client.getEngineStoredWorkItem(_wirID);
                }
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
        if (_wirID != null) root.addChild("wirid", _wirID);
        if (getWir() != null) root.addContent(_wir.toXML());
        if (_dataString != null) {
            root.addChild("datastring", _dataString);
        }
        root.addChild("ruletype", _ruleType.toString());
        root.addChild("ruleNode", _ruleNodeId);
        return root;
    }


    public void fromXNode(XNode node) {
        _caseID = node.getChildText("caseid");
        XNode wirNode = node.getChild("workItemRecord");
        if (wirNode != null) {
            _wir = Marshaller.unmarshalWorkItem(wirNode.toString());
            if (_wir != null) {
                _wirID = _wir.getID();
            }
        }
        _wirID = node.getChildText("wirid");
        _dataString = node.getChildText("datastring");
        _ruleType = RuleType.fromString(node.getChildText("ruletype"));
        _ruleNodeId = StringUtil.strToLong(node.getChildText("ruleNode"), -1);
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
