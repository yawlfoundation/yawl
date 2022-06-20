package org.yawlfoundation.yawl.stateless.listener.event;

import org.yawlfoundation.yawl.stateless.engine.YWorkItem;

/**
 * @author Michael Adams
 * @date 24/8/20
 */
public class YTimerEvent extends YEvent {

    private YWorkItem _item;

    public YTimerEvent(YEventType eType, YWorkItem item) {
        super(eType, item.getCaseID());
        _item = item;
    }


    public YWorkItem getItem() {
        return _item;
    }
}
