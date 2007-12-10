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

    public ServiceConnection(String userid) {
        _userid = userid ;
        _handle = UUID.randomUUID().toString();
        startActivityTimer();
    }

    public String getHandle() { return _handle; }

    public String getUserID() { return _userid; }

    /**
     * Starts a timer task to timeout the connection after 60 mins inactivity
     */
    private void startActivityTimer() {
        long interval = 30000 ;                          // set timeout to 30 min
        _activityTimer = new Timer() ;
        TimerTask tTask = new TimeOut();
        _activityTimer.schedule(tTask, interval);
    }

    public void resetActivityTimer() {
        if (_activityTimer != null) _activityTimer.cancel();  // cancel old
        startActivityTimer();                                 // start new
    }

    private class TimeOut extends TimerTask {
        public void run() {
            ConnectionCache.getInstance().disconnect(_handle) ;
        }
    }
}   
