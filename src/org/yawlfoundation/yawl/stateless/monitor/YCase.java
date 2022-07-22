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
    protected static final long DEFAULT_IDLE_TIMEOUT = 5000;
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
        _idleTimerTask = startIdleTimer();
    }


    protected void ping() {
        resetIdleTimer();
    }


    public String marshal() throws YStateException {
        if (_runner == null) {
            throw new YStateException("Missing state for case.");
        }
        return new YCaseExporter().marshal(_runner);
    }


    public YNetRunner getRunner() { return _runner; }


    public void cancelWorkItemTimers() {
        for (YNetRunner runner : _runner.getAllRunnersForCase()) {
            for (YWorkItem item : runner.getWorkItemRepository().getWorkItems()) {
                item.cancelTimer();
            }
        }
    }


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
        _idleTimerTask = startIdleTimer();
    }


    protected void cancelIdleTimer() {
        if (_idleTimerTask != null) _idleTimerTask.cancel();
    }

}
