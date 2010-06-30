/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.authentication;

import org.yawlfoundation.yawl.engine.YEngine;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Base class which represents an active session between the engine and an external
 * service or application.
 *
 * @author Michael Adams
 * @since 2.1
 */

public class YSession {

    private String _handle ;                                    // the session handle
    private Timer _activityTimer ;
    private long _interval;

    private YClient _client;                            // overridden in child classes


   /**
    * Creates an anonymous session with the engine.
    * @param timeOutSeconds the maximum idle time for this session (in seconds). A
    * value of 0 will default to 60 minutes; a value less than zero means this session
    * will never timeout.
    */
    protected YSession(long timeOutSeconds) {
        _handle = UUID.randomUUID().toString();
        setInterval(timeOutSeconds);
        startActivityTimer();
    }


    /**
     * Creates a session with the engine for the client.
     * @param client the external service or application requesting a session
     * @param timeOutSeconds the maximum idle time for this session (in seconds)
     * @see #YSession(long)
     */
    public YSession(YClient client, long timeOutSeconds) {
        this(timeOutSeconds);
        _client = client;
    }


    /**
     * Overridden in all child classes.
     * @return This base version returns a null String (always)
     */
    public String getURI() { return null; }


    /**
     * Overridden in all child classes.
     * @return This base version returns a null String (always)
     */
    public String getPassword() { return null; }


    /**
     * Overridden in all child classes. This base version sets the password for
     * the generic 'admin' user only.
     * @param password the (hashed) password to set (change to) for the 'admin' user.
     */
    public void setPassword(String password) throws YPersistenceException {
        if (_client.getUserName().equals("admin")) {
            YExternalClient client = YEngine.getInstance().getExternalClient("admin");
            if (client != null) {
                client.setPassword(password);
                YEngine.getInstance().updateObject(client);
            }
        }
    }


    public YClient getClient() { return _client; }

    public String getHandle() { return _handle; }

    /**
     * Restarts the idle timer for this session.
     */
    public void refresh() { resetActivityTimer(); }


    /*****************************************************************/
    
    private void setInterval(long seconds) {
        if (seconds == 0)
            _interval = 3600000 ;                         // default 60 min in millisecs
        else if (seconds <= -1)
            _interval = Long.MAX_VALUE;                   // never time out
        else
            _interval = seconds * 1000;                   // secs --> msecs
    }


    // starts a timertask to timeout the connection the specified period of inactivity
    private void startActivityTimer() {
        _activityTimer = new Timer() ;
        TimerTask tTask = new TimeOut();
        _activityTimer.schedule(tTask, _interval);
    }

    // restarts a timer
    private void resetActivityTimer() {
        if (_activityTimer != null) _activityTimer.cancel();  // cancel old timer
        startActivityTimer();                                 // start new one
    }

    // disables (removes) this active session. Called when a session timer expires.
    private class TimeOut extends TimerTask {
        public void run() {
            YSessionCache.getInstance().expire(_handle) ;
        }
    }
}