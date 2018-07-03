package org.yawlfoundation.yawl.authentication;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Michael Adams
 * @date 13/10/13
 */
public class YSessionTimer {

    protected static final Timer TIMER = new Timer();

    private Map<YAbstractSession, TimerTask> _sessionMap;
    private ISessionCache _cache;


    public YSessionTimer(ISessionCache cache) {
        _sessionMap = new HashMap<YAbstractSession, TimerTask>();
        _cache = cache;
    }


    public ISessionCache getCache() { return _cache; }


    public boolean add(YAbstractSession session) {
        if (session == null) return false;
        TimerTask timerTask = scheduleTimeout(session);
        if (timerTask != null) {
            _sessionMap.put(session, timerTask);
        }
        return timerTask != null;
    }


    public boolean reset(YAbstractSession session) {
        if (session == null) return false;
        expire(session);
        return add(session);
    }


    public boolean expire(YAbstractSession session) {
        if (session != null) {
            TimerTask task = _sessionMap.remove(session);
            if (task != null) {
                task.cancel();
                TIMER.purge();
                return true;
            }
        }
        return false;
    }

    public void shutdown() {
        TIMER.cancel();
        TIMER.purge();
    }

    // starts a timertask to timeout a session after the specified period of
    // inactivity - iff the timer interval set is +ve (a -ve interval means never timeout)
    protected TimerTask scheduleTimeout(YAbstractSession session) {
        long interval = session.getInterval();
        if (interval > 0) {
            TimeOut timeout = new TimeOut(session.getHandle());
            TIMER.schedule(timeout, interval);
            return timeout;
        }
        return null;
    }

    /***************************************************************************/

    // expires (removes) the active session. Called when a session timer expires.
    private class TimeOut extends TimerTask {

        private final String _handle;

        public TimeOut(String handle) { _handle = handle; }

        public void run() { _cache.expire(_handle) ; }
    }



}
