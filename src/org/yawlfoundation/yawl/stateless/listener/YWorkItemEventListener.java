package org.yawlfoundation.yawl.stateless.listener;

import org.yawlfoundation.yawl.stateless.listener.event.YWorkItemEvent;

/**
 * @author Michael Adams
 * @date 21/8/20
 */
public interface YWorkItemEventListener {

    void handleWorkItemEvent(YWorkItemEvent event);
}
