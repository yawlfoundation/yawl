/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.jsf;

import org.apache.log4j.Logger;
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
       _log = Logger.getLogger(SessionListener.class);
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
            _log.error("Error while logging out at session destroyed : ", e) ;
        }
    }

    public void handleSessionTimeout(HttpSession session) {
        ResourceManager.getInstance().expireSession(session.getId());
    }

}

