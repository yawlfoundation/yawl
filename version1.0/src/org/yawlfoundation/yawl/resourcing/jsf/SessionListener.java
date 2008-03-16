package org.yawlfoundation.yawl.resourcing.jsf;

import org.apache.log4j.Logger;

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

    private Logger _log = Logger.getLogger(SessionListener.class);

    public SessionListener() {}


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
            System.out.println("Error while logging out at session destroyed : "
                    + e.getMessage());
        }
    }

    public void handleSessionTimeout(HttpSession session) {
     //   SessionBean sb = (SessionBean) session.getAttribute("SessionBean");
    }

}

