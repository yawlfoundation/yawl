package org.yawlfoundation.yawl.worklet.selection;

import org.jdom2.Element;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
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

    protected String _caseID;
    protected WorkItemRecord _wir;
    protected RuleType _ruleType;

    public AbstractRunner() { }

    public AbstractRunner(XNode node) { fromXNode(node); }

    public AbstractRunner(String caseID, WorkItemRecord wir, RuleType ruleType) {
        _caseID = caseID;
        _wir = wir;
        _ruleType = ruleType;
        _wirID = wir.getID();                       // for persistence
        logLaunchEvent();
    }


    public long getID() { return _id; }

    public String getCaseID() { return _caseID; }

    public String getWorkItemID() { return _wirID; }

    public RuleType getRuleType() { return _ruleType; }


    public String getParentCaseID() {
        return getWir() != null ? _wir.getRootCaseID() : null;
    }


    public YSpecificationID getParentSpecID() {
        return getWir() != null ? new YSpecificationID(_wir) : null;
    }


    public String getParentWorkItemID() {
        return getWir() != null ? _wir.getParentID() : null;
    }

    public Element getWorkItemData() {
        return getWir() != null ? _wir.getDataList() : null;
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
        if (_wir != null) {
            root.addChild("wir", _wir.toXML());
        }
        root.addChild("ruletype", _ruleType.name());
        return root;
    }


    public void fromXNode(XNode node) {
        _caseID = node.getChildText("caseid");
        _wir = Marshaller.unmarshalWorkItem(node.getChildText("wir"));
        _ruleType = RuleType.fromString(node.getChildText("ruletype"));
    }


    public void logLaunchEvent() {
        Persister.insert(new LaunchEvent(getWir(), _ruleType,
                    _caseID, getWir().getDataListString()));
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
