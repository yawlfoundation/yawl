package org.yawlfoundation.yawl.stateless.listener.event;

import org.yawlfoundation.yawl.stateless.engine.YWorkItem;
import org.yawlfoundation.yawl.engine.YWorkItemStatus;

/**
 * @author Michael Adams
 * @date 24/8/20
 */
public class YWorkItemEvent extends YEvent {

    private YWorkItemStatus _prevStatus;

    public YWorkItemEvent(YEventType eType, YWorkItem item) {
        super(eType, item.getCaseID());
        setWorkItem(item);
    }

    public YWorkItemEvent(YEventType eType, YWorkItem item, YWorkItemStatus prevStatus) {
        super(eType, item.getCaseID());
        setWorkItem(item);
        setPreviousStatus(prevStatus);
    }


    public void setPreviousStatus(YWorkItemStatus status) { _prevStatus = status; }

    public YWorkItemStatus getPreviousStatus() { return _prevStatus; }

    public YWorkItemStatus getCurrentStatus() {
        YWorkItem item = getWorkItem();
        return item != null ? item.getStatus() : null;
    }

}
