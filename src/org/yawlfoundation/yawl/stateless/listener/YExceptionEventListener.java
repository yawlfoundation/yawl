package org.yawlfoundation.yawl.stateless.listener;

import org.yawlfoundation.yawl.stateless.listener.event.YExceptionEvent;

/**
 * @author Michael Adams
 * @date 21/8/20
 */
public interface YExceptionEventListener {

    void handleExceptionEvent(YExceptionEvent event);
}
