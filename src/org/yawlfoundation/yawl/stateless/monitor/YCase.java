package org.yawlfoundation.yawl.stateless.monitor;

import org.yawlfoundation.yawl.exceptions.YStateException;
import org.yawlfoundation.yawl.stateless.engine.YNetRunner;
import org.yawlfoundation.yawl.stateless.engine.YWorkItem;
import org.yawlfoundation.yawl.stateless.listener.event.YCaseEvent;
import org.yawlfoundation.yawl.stateless.listener.event.YEventType;

import java.util.Timer;
import java.util.TimerTask;


/**
 * @author Michael Adams
 * @date 21/8/20
 */
public class YCase {

    private static final Timer IDLE_TIMER = new Timer(true);
    protected static final long DEFAULT_IDLE_TIMEOUT = 0;
    private long _idleTimeout;
    private TimerTask _idleTimerTask;

    private final YNetRunner _runner;


    protected YCase(YNetRunner runner) {
        this(runner, DEFAULT_IDLE_TIMEOUT);
    }


    protected YCase(YNetRunner runner, long idleTimeout) {
        _runner = runner;
        setIdleTimeout(idleTimeout);
    }


    protected void setIdleTimeout(long timeout) {
        _idleTimeout = timeout;
        if (isIdleTimerEnabled()) {
            _idleTimerTask = startIdleTimer();
        }
    }


    protected void ping() {
        if (isIdleTimerEnabled()) {
            resetIdleTimer();
        }
    }


    public String marshal() throws YStateException {
        if (_runner == null) {
            throw new YStateException("Missing state for case.");
        }
        return new YCaseExporter().marshal(_runner);
    }


    public YNetRunner getRunner() { return _runner; }


    public void removeWorkItemTimers() {
        for (YNetRunner runner : _runner.getAllRunnersForCase()) {
            for (YWorkItem item : runner.getWorkItemRepository().getWorkItems()) {
                item.setSuppressTimerEventNotifications(true); // suppress timer cancelled event
                item.cancelTimer();
            }
        }
    }


    private boolean isIdleTimerEnabled() { return _idleTimeout > 0; }


    private TimerTask startIdleTimer() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                YCaseEvent event = new YCaseEvent(YEventType.CASE_IDLE_TIMEOUT, _runner);
                _runner.getAnnouncer().announceCaseEvent(event);
            }
        };
        IDLE_TIMER.schedule(task, _idleTimeout);
        return task;
    }


    protected void resetIdleTimer() {
        cancelIdleTimer();
        if (isIdleTimerEnabled()) {
            _idleTimerTask = startIdleTimer();
        }
    }


    // restart only if the timer task is currently not running
    protected void restartIdleTimer() {
        if (isIdleTimerEnabled() && _idleTimerTask == null) {
            _idleTimerTask = startIdleTimer();
        }
    }


    protected void cancelIdleTimer() {
        if (_idleTimerTask != null) {
            _idleTimerTask.cancel();
            _idleTimerTask = null;
        }
    }


    // a case is considered to be idle if it currently has a running idle timer
    protected boolean isIdle() throws YStateException {
        if (! isIdleTimerEnabled()) {
            throw new YStateException("Idle monitoring is disabled for case.");
        }
        return _idleTimerTask != null;
    }

}
