package org.yawlfoundation.yawl.stateless.listener.event;

import org.yawlfoundation.yawl.stateless.engine.YWorkItem;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Michael Adams
 * @date 24/8/20
 */
public class YTimerEvent extends YEvent {

    private YWorkItem _item;
    private String expiryTimeString;

    public YTimerEvent(YEventType eType, YWorkItem item) {
        super(eType, item.getCaseID());
        _item = item;
    }


    public YWorkItem getItem() {
        return _item;
    }

    public String getExpiryTimeString() {
        if (expiryTimeString == null) {
            expiryTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                    .format(new Date(_item.getTimerExpiry()));
        }
        return expiryTimeString;
    }
}
