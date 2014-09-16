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

public class YSession extends YAbstractSession {

    private YClient _client;                            // overridden in child classes


    public YSession(long timeOutSeconds) {
        super(timeOutSeconds);
    }

    /**
     * Creates a session with the engine for the client.
     * @param client the external service or application requesting a session
     * @param timeOutSeconds the maximum idle time for this session (in seconds)
     * @see #YSession(long)
     */
    public YSession(YClient client, long timeOutSeconds) {
        super(timeOutSeconds);
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

}