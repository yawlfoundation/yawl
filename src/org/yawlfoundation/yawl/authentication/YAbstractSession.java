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

package org.yawlfoundation.yawl.authentication;

import org.yawlfoundation.yawl.engine.YEngine;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;

import java.util.UUID;

/**
 * Base class which represents an active session between the engine and an external
 * service or application.
 *
 * @author Michael Adams
 * @since 2.1
 */

public abstract class YAbstractSession {

    private String _handle ;                                    // the session handle
    private long _interval;


   /**
    * Creates an anonymous session with the engine.
    * @param timeOutSeconds the maximum idle time for this session (in seconds). A
    * value of 0 will default to 60 minutes; a value less than zero means this session
    * will never timeout.
    */
    public YAbstractSession(long timeOutSeconds) {
        _handle = UUID.randomUUID().toString();
        setInterval(timeOutSeconds);
    }


    public String getHandle() { return _handle; }

    public long getInterval() { return _interval; }


    /*****************************************************************/
    
    // sets secs to msecs, default to 60 mins if 0 seconds passed
    private void setInterval(long seconds) {
        _interval = (seconds == 0) ? 3600000 : seconds * 1000;
    }

}