
package org.chiba.web.session;

/**
 * manages all XForms sessions of a single user.
 *
 * @author joern turner</a>
 * @version $Id: XFormsSessionManager.java,v 1.1 2006/09/10 19:50:51 joernt Exp $
 */
public interface XFormsSessionManager {
    static final String XFORMS_SESSION_MANAGER = "chiba.session.manager";

    /**
     * set the maximum amount of transient (in-memory) sessions allowed for a user
     * @param max the maximum amount of transient (in-memory) sessions allowed for a user
     */
    void setMaxSessions(int max);

    /**
     * set the timeout for XFormsSession lifetime
     * @param milliseconds the amount of time before wiping a session in seconds
     */
    void setTimeout(int milliseconds);

    int getSessionCount();

    /**
     * factory method for creating a XFormsSession object.
     *
     * @return an object of type XFormsSession
     */
    XFormsSession createXFormsSession();

    /**
     * must be called to register a XFormsSession with the Manager
     */
    void addXFormsSession(XFormsSession xfSession);

    /**
     * fetches a XFormsSession by its id
     * @param id the id of the session as created during createXFormsSession() call
     * @return returns the XFormsSession object associated with given id or null if object does not exist
     */
    XFormsSession getXFormsSession(String id);

    /**
     * deletes  XFormsSession object from internal pool of objects.
     * @param id
     */
    void deleteXFormsSession(String id);

    /**
     * checks for timed-out XFormsSessions and deletes these from internal pool.
     */
    void wipe();
}
