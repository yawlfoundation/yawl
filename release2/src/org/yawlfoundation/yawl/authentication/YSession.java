package org.yawlfoundation.yawl.authentication;

import org.yawlfoundation.yawl.exceptions.YPersistenceException;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Maintains a session between the engine and an external service or application
 *
 * Author: Michael Adams
 */

public abstract class YSession {

    private String _handle ;                                    // the session handle
    private Timer _activityTimer ;


    public YSession() {
        _handle = UUID.randomUUID().toString();
        startActivityTimer();
    }

    public abstract String getURI();

    public abstract String getName();

    public abstract String getPassword();

    public abstract void setPassword(String password) throws YPersistenceException;


    public String getHandle() { return _handle; }

    public void refresh() { resetActivityTimer(); }


    /**
     * Starts a timertask to timeout the connection after 60 mins inactivity
     */
    private void startActivityTimer() {
        long interval = 3600000 ;                              // 60 min in millisecs
        _activityTimer = new Timer() ;
        TimerTask tTask = new TimeOut();
        _activityTimer.schedule(tTask, interval);
    }

    private void resetActivityTimer() {
        if (_activityTimer != null) _activityTimer.cancel();  // cancel old timer
        startActivityTimer();                                 // start new one
    }

    private class TimeOut extends TimerTask {
        public void run() {
            YSessionCache.getInstance().expire(_handle) ;
        }
    }
}