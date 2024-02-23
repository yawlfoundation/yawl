package org.yawlfoundation.yawl.stateless.listener;

import org.yawlfoundation.yawl.stateless.listener.event.YTimerEvent;

/**
 * @author Michael Adams
 * @date 24/8/20
 */
public interface YTimerEventListener {

    void handleTimerEvent(YTimerEvent event);
}
