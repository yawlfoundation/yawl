/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.resourcing.rsInterface;

import org.yawlfoundation.yawl.resourcing.ResourceManager;

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


    // sets secs to msecs, default to 60 mins if 0 seconds passed
    private void setInterval(long seconds) {
        _interval = (seconds == 0) ? 3600000 : seconds * 1000;
    }

    /**
     * Starts a timertask to timeout the connection after 'interval' msecs inactivity
     */
    private void startActivityTimer() {
        if (_interval > 0) {
            _activityTimer = new Timer() ;
            TimerTask tTask = new TimeOut();
            _activityTimer.schedule(tTask, _interval);
        }
    }

    public void resetActivityTimer() {
        cancelActivityTimer();                                // cancel old
        startActivityTimer();                                 // start new
    }

    public void cancelActivityTimer() {
        if (_activityTimer != null) {
            _activityTimer.cancel();                          // cancel old
            _activityTimer = null;                            // drop thread
        }
    }

    private class TimeOut extends TimerTask {
        public void run() {
            ConnectionCache.getInstance().expire(_handle) ;
            ResourceManager.getInstance().removeCalendarStatusChangeListeners(_handle);
        }
    }
}   
