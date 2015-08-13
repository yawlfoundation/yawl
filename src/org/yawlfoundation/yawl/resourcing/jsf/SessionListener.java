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

package org.yawlfoundation.yawl.resourcing.jsf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yawlfoundation.yawl.resourcing.ResourceManager;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Takes the necessary steps when a session times out
 *
 * Author: Michael Adams
 * Creation Date: 14/03/2008
 *
 * Based on code sourced from:
 * http://techieexchange.blogspot.com/2008/02/jsf-session-expiry-timeout-solution.html
 */

public class SessionListener implements HttpSessionListener {

    private Logger _log;

    public SessionListener() {
       _log = LogManager.getLogger(SessionListener.class);
    }


    // can be used to log new session etc.
    public void sessionCreated(HttpSessionEvent event) { }

    // can be used to cleanup after session invalidates
    public void sessionDestroyed(HttpSessionEvent event) {

        // get the session being destroyed
        HttpSession session = event.getSession();
        try {
            handleSessionTimeout(session);
        }
        catch(Exception e) {
            _log.warn("Session [" + session.getId() +  "] was destroyed before " +
                      "an expired session audit event could be logged for it.") ;
        }
    }

    public void handleSessionTimeout(HttpSession session) {
        ResourceManager.getInstance().expireSession(session.getId());
    }

}

