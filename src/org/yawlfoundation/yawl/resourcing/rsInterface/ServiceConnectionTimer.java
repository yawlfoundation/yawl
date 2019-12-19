/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

import org.yawlfoundation.yawl.authentication.*;
import org.yawlfoundation.yawl.resourcing.ResourceManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Michael Adams
 * @date 13/10/13
 */
public class ServiceConnectionTimer extends YSessionTimer {


    public ServiceConnectionTimer(ISessionCache cache) {
        super(cache);
    }

    // starts a timertask to timeout a session after the specified period of
    // inactivity - iff the timer interval set is +ve (a -ve interval means never timeout)
    protected TimeOut scheduleTimeout(YAbstractSession session) {
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

        public void run() {
            getCache().expire(_handle) ;
            ResourceManager.getInstance().removeCalendarStatusChangeListeners(_handle);
        }
    }

}
