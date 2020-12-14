package org.yawlfoundation.yawl.stateless.listener.event;

import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.logging.YLogDataItemList;
import org.yawlfoundation.yawl.stateless.elements.YSpecification;
import org.yawlfoundation.yawl.stateless.elements.marking.YIdentifier;
import org.yawlfoundation.yawl.stateless.engine.YNetRunner;
import org.yawlfoundation.yawl.stateless.engine.YWorkItem;

/**
 * @author Michael Adams
 * @date 24/8/20
 */
public class YLogEvent extends YEvent {

    private YLogDataItemList _logData;
    private YWorkItem _workItem;
    private YNetRunner _netRunner;

    public YLogEvent(YEventType eType, YIdentifier caseID, YSpecification spec,
                     YLogDataItemList logData) {
        this(eType, caseID, spec.getSpecificationID(), logData);
    }

    public YLogEvent(YEventType eType, YIdentifier caseID, YSpecificationID specID,
                     YLogDataItemList logData) {
        super(eType, caseID);
        setSpecID(specID);
        setLogData(logData);
    }

    public YLogEvent(YEventType eType, YWorkItem item, YLogDataItemList logData) {
        this(eType, item.getCaseID(), item.getSpecificationID(), logData);
        setWorkItem(item);
    }

    public YLogEvent(YEventType eType, YNetRunner runner, YLogDataItemList logData) {
        this(eType, runner.getCaseID(), runner.getSpecificationID(), logData);
        setNetRunner(runner);
    }


    public void setLogData(YLogDataItemList logData) { _logData = logData; }

    public YLogDataItemList getLogData() { return _logData; }


    public void setWorkItem(YWorkItem item) { _workItem = item; }

    public YWorkItem getWorkItem() { return _workItem; }


    public void setNetRunner(YNetRunner runner) { _netRunner = runner; }

    public YNetRunner getNetRunner() { return _netRunner; }


    public String getItemStatus() {
        return _workItem != null ? _workItem.get_status() : null;
    }

}
