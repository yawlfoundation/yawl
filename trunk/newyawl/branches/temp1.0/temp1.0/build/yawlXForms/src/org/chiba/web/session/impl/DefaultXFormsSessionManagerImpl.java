package org.chiba.web.session.impl;

import org.apache.log4j.Logger;
import org.chiba.web.session.XFormsSession;
import org.chiba.web.session.XFormsSessionManager;

import java.util.*;

/**
 * Simple default implementation of a XFormsSessionManager.
 *
 * @author joern turner</a>
 * @version $Id: DefaultXFormsSessionManagerImpl.java,v 1.6 2006/12/29 11:33:39 joernt Exp $
 *          <p/>
 *          todo: handle maxSessions
 *          todo: move timeout to XFormsSession
 *          todo: implement persistent sessions
 */
public class DefaultXFormsSessionManagerImpl extends Thread implements XFormsSessionManager {
    private static DefaultXFormsSessionManagerImpl instance = null;
    private Map xformsSessions;
    protected int maxSessions;
    private static final Logger LOGGER = Logger.getLogger(DefaultXFormsSessionManagerImpl.class);

    private static final int DEFAULT_TIMEOUT = 5*60*1000; //default is 5 Minutes (expressed in milliseconds)
    private int timeout = DEFAULT_TIMEOUT;
    private boolean stopped = false;
    private long interval = 1500;
    private boolean threadStarted = false;
    private static final String INSTANCE="instance";

    public static DefaultXFormsSessionManagerImpl getInstance() {
        if (instance == null) {
            instance = new DefaultXFormsSessionManagerImpl(INSTANCE);
        }
        return instance;

    }

    /**
     * set the maximum amount of transient (in-memory) sessions allowed for a user
     *
     * @param max the maximum amount of transient (in-memory) sessions allowed for a user
     */
    public void setMaxSessions(int max) {
        this.maxSessions = max;
    }

    public void setTimeout(int milliseconds) {
        this.timeout = milliseconds;
    }

    /**
     * set the interval the wiper thread will check for expired sessions. Setting a value of 0 causes the wiper
     * thread to be *not* started. Therefore XFormsSessions will never expire unless the Http Session is still
     * alive.
     *
     * @param milliseconds the interval the wiper is checking for expired sessions
     */
    public void setInterval(int milliseconds) {
        this.interval = milliseconds;
    }

    public void init() {
        start();
    }

    public int getSessionCount() {
        return this.xformsSessions.size();
    }

    private DefaultXFormsSessionManagerImpl(String instance) {
        this.xformsSessions = Collections.synchronizedMap(new HashMap());
    }

    /**
     * must be called to register a XFormsSession with the Manager
     */
    public void addXFormsSession(XFormsSession xfSession) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("added XFormsSession to SessionManager: " + xfSession.getKey());
            LOGGER.debug("Session count now: " + xformsSessions.size());
        }
        this.xformsSessions.put(xfSession.getKey(), xfSession);
    }

    /**
     * factory method for câ?„reating a XFormsSession object.
     *
     * @return an object of type XFormsSession
     */
    public XFormsSession createXFormsSession() {
        XFormsSession xFormsSession = new DefaultXFormsSessionImpl(this);
        this.xformsSessions.put(xFormsSession.getKey(), xFormsSession);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("created XFormsSession: " + xFormsSession.getKey());
        }

        return xFormsSession;
    }


    /**
     * fetches a XFormsSession by its id
     *
     * @param id the id of the session as created during createXFormsSession() call
     * @return returns the XFormsSession object associated with given id or null if object does not exist
     */
    public XFormsSession getXFormsSession(String id) {

        if (this.xformsSessions.containsKey(id)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("returning XFormsSession: " + id);
                LOGGER.debug("Session count now: " + xformsSessions.size());
            }
            return (XFormsSession) this.xformsSessions.get(id);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("XFormsSession: " + id + " not found");
            }
            return null;
        }
    }

    /**
     * deletes  XFormsSession object from internal pool of objects.
     *
     * @param id
     */
    public void deleteXFormsSession(String id) {

        if (this.xformsSessions.containsKey(id)) {
            this.xformsSessions.remove(id);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("deleted XFormsSession from SessionManager: " + id);
                LOGGER.debug("Session count now: " + xformsSessions.size());
            }
        }
    }

    /**
     * checks for timed-out XFormsSessions and deletes these from internal pool.
     */
    public void wipe() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("checking for expired sessions");
        }

        XFormsSession session;
        Iterator allSessions = this.xformsSessions.values().iterator();
        while (allSessions.hasNext()) {
            session = (XFormsSession) allSessions.next();

            if (isExpired(session)) {
                allSessions.remove();
                System.gc();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Removed expired XFormsSession: " + session.getKey() + " - lastUsed: " + new Date(session.getLastUseTime()));
                    LOGGER.debug("Session count now: " + xformsSessions.size());
                }
            }
        }
    }

    private boolean isExpired(XFormsSession session) {
        long now = System.currentTimeMillis();

        if ((now - session.getLastUseTime()) > (timeout)) {
            return true;
        } else {
            return false;
        }
    }


    public void run() {
        while (!stopped) {
            try {
                Thread.sleep(interval);
                wipe();
            } catch (InterruptedException e) {
                LOGGER.error("Exception while trying to sleep Thread");
            }
        }
    }

    public synchronized void start() {
        if (this.interval != 0 && !threadStarted) {
            super.start();
            this.threadStarted = true;
        }else{
            LOGGER.warn("No XForms session cleanup. Your server might run out of memory under load. To avoid this configure your web.xml accordingly.");
        }

    }

    public void kill() {
        this.stopped = true;
        instance = null;

    }

}
