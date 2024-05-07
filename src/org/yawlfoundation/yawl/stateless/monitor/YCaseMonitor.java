package org.yawlfoundation.yawl.stateless.monitor;

import org.yawlfoundation.yawl.exceptions.YStateException;
import org.yawlfoundation.yawl.stateless.elements.marking.YIdentifier;
import org.yawlfoundation.yawl.stateless.engine.YNetRunner;
import org.yawlfoundation.yawl.stateless.listener.YCaseEventListener;
import org.yawlfoundation.yawl.stateless.listener.YWorkItemEventListener;
import org.yawlfoundation.yawl.stateless.listener.event.YCaseEvent;
import org.yawlfoundation.yawl.stateless.listener.event.YWorkItemEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Michael Adams
 * @date 29/6/2022
 */
public class YCaseMonitor implements YCaseEventListener, YWorkItemEventListener {

    private final Map<YIdentifier, YCase> _caseMap;

    // if idle timeout value is non-positive, idle timer is disabled
    private long _idleTimeout;


    public YCaseMonitor(long idleTimeout) {
        _caseMap = new ConcurrentHashMap<>();
        _idleTimeout = idleTimeout;
    }


    public void setIdleTimeout(long timeout) { _idleTimeout = timeout; }


    @Override
    public void handleCaseEvent(YCaseEvent event) {
        switch (event.getEventType()) {
            case CASE_STARTING: initCase(event); break;
            case CASE_STARTED: addCase(event); break;             // update init'ed case
            case CASE_START_FAILED:
            case CASE_COMPLETED:
            case CASE_CANCELLED:
            case CASE_DEADLOCKED: removeCase(event); break;
        }
    }

    
    @Override
    public void handleWorkItemEvent(YWorkItemEvent event) {
        if (isTimerEnabled()) {
            YNetRunner runner = event.getWorkItem().getNetRunner().getTopRunner();
            YCase yCase = _caseMap.get(runner.getCaseID());
            if (yCase != null) {
                yCase.ping();
            }
        }
    }


    public boolean hasCase(YIdentifier caseID) {
        return _caseMap.containsKey(caseID);
    }


    private void initCase(YCaseEvent event) {
        YCase yCase = new YCase(null, 0);
        _caseMap.put(event.getCaseID(), yCase);
    }


    public void addCase(YCaseEvent event) {
        YCase yCase = new YCase(event.getRunner(), _idleTimeout);
        _caseMap.put(event.getCaseID(), yCase);
    }


    private void removeCase(YCaseEvent event) {
        YCase yCase = _caseMap.remove(event.getCaseID());
        if (yCase != null) {
            yCase.cancelIdleTimer();
        }
    }


    public YCase unloadCase(YIdentifier caseID) throws YStateException {
        YCase yCase = _caseMap.remove(caseID);
        if (yCase == null) {
            throw new YStateException("Unknown case: " + caseID);
        }
        return yCase;
    }


    public String marshalCase(YIdentifier caseID) throws YStateException {
        YCase yCase = _caseMap.remove(caseID);
        if (yCase == null) {
            throw new YStateException("Unknown case: " + caseID);
        }
        return yCase.marshal();
    }


    public void cancel() {
        for (YCase yCase: _caseMap.values()) {
            yCase.cancelIdleTimer();
        }
    }


    // called from YStatelessEngine immediately before a workitem processing begins.
    // timer (if any) will restart when the processing for the workitem completes.
    public void pauseIdleTimer(YIdentifier caseID) {
        YCase yCase = _caseMap.get(caseID);
        if (yCase != null) {
            yCase.cancelIdleTimer();
        }
    }


    public void resumeIdleTimer(YIdentifier caseID) {
        YCase yCase = _caseMap.get(caseID);
        if (yCase != null) {
            yCase.restartIdleTimer();
        }
    }


    public boolean isIdleCase(YIdentifier caseID) throws YStateException {
        YCase yCase = _caseMap.get(caseID);
        if (yCase == null) {
            throw new YStateException("Unknown case: " + caseID);
        }
        return yCase.isIdle();
    }


    private boolean isTimerEnabled() { return _idleTimeout > 0; }

}
