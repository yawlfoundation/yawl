package org.yawlfoundation.yawl.stateless.listener;

import org.yawlfoundation.yawl.stateless.listener.event.YLogEvent;

/**
 * @author Michael Adams
 * @date 21/8/20
 */
public interface YLogEventListener {

    void handleLogEvent(YLogEvent event) ;
}
