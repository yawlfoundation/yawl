package org.yawlfoundation.yawl.stateless.engine;

import org.yawlfoundation.yawl.stateless.listener.*;
import org.yawlfoundation.yawl.stateless.listener.event.*;

import java.util.Set;

/**
 * @author Michael Adams
 * @date 18/1/2023
 */
public interface EventNotifier {

    void announceCaseEvent(Set<YCaseEventListener> listeners, YCaseEvent event);

    void announceWorkItemEvent(Set<YWorkItemEventListener> listeners, YWorkItemEvent event);

    void announceExceptionEvent(Set<YExceptionEventListener> listeners, YExceptionEvent event);

    void announceLogEvent(Set<YLogEventListener> listeners, YLogEvent event);

    void announceTimerEvent(Set<YTimerEventListener> listeners, YTimerEvent event);

}
