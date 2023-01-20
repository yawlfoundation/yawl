package org.yawlfoundation.yawl.stateless.engine;

import org.yawlfoundation.yawl.stateless.listener.*;
import org.yawlfoundation.yawl.stateless.listener.event.*;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Michael Adams
 * @date 18/1/2023
 */
public class MultiThreadEventNotifier implements EventNotifier {

    private final ExecutorService _executor = Executors.newFixedThreadPool(12);


    @Override
    public void announceCaseEvent(Set<YCaseEventListener> listeners, YCaseEvent event) {
        for (YCaseEventListener listener : listeners) {
            _executor.execute(() -> listener.handleCaseEvent(event));
        }
    }


    @Override
    public void announceWorkItemEvent(Set<YWorkItemEventListener> listeners,
                                      YWorkItemEvent event) {
        for(YWorkItemEventListener listener : listeners) {
            _executor.execute(() -> listener.handleWorkItemEvent(event));
        }
    }


    @Override
    public void announceExceptionEvent(Set<YExceptionEventListener> listeners,
                                       YExceptionEvent event) {
        for (YExceptionEventListener listener : listeners) {
            _executor.execute(() -> listener.handleExceptionEvent(event));
        }
    }


    @Override
    public void announceLogEvent(Set<YLogEventListener> listeners, YLogEvent event) {
        for (YLogEventListener listener : listeners) {
            _executor.execute(() -> listener.handleLogEvent(event));
        }
    }


    @Override
    public void announceTimerEvent(Set<YTimerEventListener> listeners, YTimerEvent event) {
        for (YTimerEventListener listener : listeners) {
            _executor.execute(() -> listener.handleTimerEvent(event));
        }
    }
    
}
