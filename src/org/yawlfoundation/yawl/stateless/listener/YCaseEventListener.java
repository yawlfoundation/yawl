package org.yawlfoundation.yawl.stateless.listener;

import org.yawlfoundation.yawl.stateless.listener.event.YCaseEvent;

/**
 * @author Michael Adams
 * @date 21/8/20
 */
public interface YCaseEventListener {

    void handleCaseEvent(YCaseEvent event);
}
