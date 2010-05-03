package org.yawlfoundation.yawl.resourcing.rsInterface;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Author: Michael Adams
 * Date: Oct 24, 2007
 * Time: 1:06:45 PM
 */

public class ServiceConnection {

    private String _handle ;
    private String _userid ;
    private Timer _activityTimer ;
    private long _interval;

    public ServiceConnection(String userid, long timeOutSeconds) {
        _userid = userid ;
        _handle = UUID.randomUUID().toString();
        setInterval(timeOutSeconds);
        startActivityTimer();
    }

    public String getHandle() { return _handle; }

    public String getUserID() { return _userid; }


    private void setInterval(long seconds) {
        if (seconds == 0)
            _interval = 3600000 ;                         // default 60 min in millisecs
        else if (seconds <= -1)
            _interval = Long.MAX_VALUE;                   // never time out
        else
            _interval = seconds * 1000;                   // secs --> msecs
    }

    /**
     * Starts a timertask to timeout the connection after 'interval' msecs inactivity
     */
    private void startActivityTimer() {
        _activityTimer = new Timer() ;
        TimerTask tTask = new TimeOut();
        _activityTimer.schedule(tTask, _interval);
    }

    public void resetActivityTimer() {
        if (_activityTimer != null) _activityTimer.cancel();  // cancel old
        startActivityTimer();                                 // start new
    }

    private class TimeOut extends TimerTask {
        public void run() {
            ConnectionCache.getInstance().expire(_handle) ;
        }
    }
}   
