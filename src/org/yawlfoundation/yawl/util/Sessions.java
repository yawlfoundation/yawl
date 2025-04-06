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

package org.yawlfoundation.yawl.util;

import org.yawlfoundation.yawl.authentication.YClient;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.engine.interfce.interfaceA.InterfaceA_EnvironmentBasedClient;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * A generic session manager utility that can be used by custom services to allow
 * connections from any service or client application that has been registered in
 * the engine.
 *
 * @author Michael Adams
 * @date 9/11/11
 */
public class Sessions {
    
    private Map<String, String> idToHandle;             // userid <-> sessionhandle
    private Map<String, ScheduledFuture> handleToTimer; // sessionhandle <-> activity timer
    private InterfaceAClient iaClient;

    private static final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    private static final String INVALID_CREDENTIALS = "Invalid credentials";

    /**
     * Constructs a new Sessions object
     */
    public Sessions() {
        idToHandle = new ConcurrentHashMap<>();
        handleToTimer = new ConcurrentHashMap<>();
    }


    /**
     * Constructs a new Sessions object, and uses the parameters passed to connect to
     * the engine via Interface A so that the registered services' and client
     * applications' credentials can be retrieved.
     * @param iaURI The URI of the engine's Interface A
     * @param iaUserid the userid of the registered service or client app calling this method
     * @param iaPassword the password of the registered service or client app calling this method
     */
    public Sessions(String iaURI, String iaUserid, String iaPassword) {
        this();
        setupInterfaceA(iaURI, iaUserid, iaPassword);
    }


    /**
     * Uses the parameters passed to connect to the engine via Interface A so that the
     * registered services' and client applications' credentials can be retrieved. Note
     * this method must be called before any sessions can be authenticated
     * @param iaURI The URI of the engine's Interface A
     * @param iaUserid the userid of the registered service or client app calling this method
     * @param iaPassword the password of the registered service or client app calling this method
     */
    public void setupInterfaceA(String iaURI, String iaUserid, String iaPassword) {
        iaClient = new InterfaceAClient(iaURI, iaUserid, iaPassword);
    }


    /**
     * Cancels all current sessions and their inactivity timers.
     * Usually called from Servlet#destroy
     */
    public void shutdown() {
        for (String handle : handleToTimer.keySet()) {
            removeActivityTimer(handle);
        }
        scheduler.shutdownNow();
    }


    /**
     * Attempts to establish a session using the credentials passed
     * @param userid the userid of a registered service or client application
     * @param password the corresponding password
     * @return a session handle if successful, or a diagnostic error if not (incl.
     * unknown userid, invalid password, invalid or missing Interface A credentials, or
     * other problems connecting to the engine or retrieving registered credentials)
     */
    public String connect(String userid, String password) {
        try {
            String storedPassword = iaClient.getPassword(userid);
            if (storedPassword == null || storedPassword.startsWith("<fail")) {
                return failMsg(INVALID_CREDENTIALS);
            }
            if (storedPassword.equals(password)) {
                return getHandle(userid);                    // session established!
            }
            return failMsg(INVALID_CREDENTIALS);
        }
        catch (IOException ioe) {
            return failMsg(ioe.getMessage());
        }
    }


    /**
     * Checks that a session handle is valid and active (i.e. has not timed out through
     * inactivity)
     * @param handle the session handle to check
     * @return true if the session handle is known and active, false otherwise
     */
    public boolean checkConnection(String handle) {
        if (handle != null && handleToTimer.containsKey(handle)) {
            refreshActivityTimer(handle);
            return true;
        }
        return false;
    }


    /**
     * Ends a session
     * @param handle the session handle to cancel
     * @return true if the session handle was known and valid, and has now been
     * disconnected, false if the handle was already disconnected
     */
    public boolean disconnect(String handle) {
        for (String userid : idToHandle.keySet()) {
            String activeHandle = idToHandle.get(userid);
            if (activeHandle.equals(handle)) {
                idToHandle.remove(userid);
                removeActivityTimer(handle);
                return true;
            }
        }
        return false;
    }


    /*****************************************************************************/

    /**
     * Create and store a new session handle for a userid
     * @param userid the userid the create the handle for. It is assumed that the
     *               userid has already been authenticated
     * @return a newly created session handle
     */
    private String getHandle(String userid) {
        String handle = idToHandle.get(userid);
        if (handle == null) {
            handle = UUID.randomUUID().toString();
            idToHandle.put(userid, handle);
        }
        startActivityTimer(handle);
        return handle;
    }


    /**
     * Restarts an inactivity timer for a session handle
     * @param handle the session handle to restart the timer for
     */
    private void refreshActivityTimer(final String handle) {
        removeActivityTimer(handle);
        startActivityTimer(handle);
    }


    /**
     * Starts an inactivity timer for a session handle. The handle will expire after
     * 60 minutes of inactivity
     * @param handle the session handle to start the timer for
     */
    private void startActivityTimer(final String handle) {
        final ScheduledFuture<?> inactivityTimer = scheduler.schedule(
            new Runnable() {
                public void run() { disconnect(handle); }
            },
            60, TimeUnit.MINUTES);
        handleToTimer.put(handle, inactivityTimer);
    }


    /**
     * Cancels an inactivity timer for a session handle.
     * @param handle the session handle to cancel the timer for
     */
    private void removeActivityTimer(final String handle) {
        final ScheduledFuture<?> inactivityTimer = handleToTimer.remove(handle);
        if (inactivityTimer != null) inactivityTimer.cancel(true);
    }


    private String failMsg(String msg) { return StringUtil.wrap(msg, "failure"); }


    /*****************************************************************************/

    /**
     * Creates a client to the engine via IA to obtain logon credentials for custom
     * services and client application registered with the engine. A registered client
     * is accepted as a valid client of the service using this class.
     */
    class InterfaceAClient {

        InterfaceA_EnvironmentBasedClient iaClient;
        String iaUserid;
        String iaPassword;
        String iaHandle;
        String iaURI;
        static final String NO_CREDENTIALS = "No Interface A credentials supplied to service";


        /**
         * Creates an instance of this class, and sets the uri of engine's Interface A
         * and the logon credentials that can be used to connect via that interface
         * @param uri the uri of engine's Interface A
         * @param userid a userid registered as a custom service or client application
         *               logon user
         * @param password the corresponding password
         */
        InterfaceAClient(String uri, String userid, String password) {
            iaURI = uri;
            iaUserid = userid;
            iaPassword = password;
        }


        /**
         * Gets the password from the engine for the userid passed
         * @param id the userid to get the password for
         * @return the retrieved password, or an error msg if the id is unknown
         * @throws IOException if there's a problem connecting to the engine, or if
         * there's some other problem getting the password from the engine
         */
        String getPassword(String id) throws IOException {
            checkConnection();
            if (id == null) throw new IOException(INVALID_CREDENTIALS);

            // try for custom service first
            YClient service = getServiceAccount(id);
            if (service != null) {
                return service.getPassword();
            }

            // not a service, try a client
            return iaClient.getPassword(id, iaHandle);
        }


        /**
         * Gets the YAWL service using the userid passed
         * @param userid the userid of the service to get
         * @return the matching service, or null of a mtch isn't found
         * @throws IOException if there's a problem connecting to the engine
         */
        YClient getServiceAccount(String userid) throws IOException {
            for (YAWLServiceReference service : iaClient.getRegisteredYAWLServices(iaHandle)) {
                if (service.getServiceName().equals(userid)) {
                    return service;
                }
            }
            return null;
        }


        /**
         * Checks if the IA URI and credentials are available, then if there is an
         * active IA connection and, if not, creates one
         * @throws IOException if the URI or credentials are missing, or if there's a
         * problem creating a connection
         */
        void checkConnection() throws IOException {

            // check we have all the required data
            if ((iaUserid == null) || (iaPassword == null) || (iaURI == null)) {
                throw new IOException(NO_CREDENTIALS);
            }

            // if we haven't created a client yet, do it now
            if (iaClient == null) {
                iaClient = new InterfaceA_EnvironmentBasedClient(iaURI);
            }

            // if we have no handle, or if it has expired, establish a new connection
            if (iaHandle == null || ! iaClient.successful(iaHandle) ||
                    ! iaClient.successful(iaClient.checkConnection(iaHandle))) {
                iaHandle = iaClient.connect(iaUserid, iaPassword);
                if (! iaClient.successful(iaHandle)) {
                    throw new IOException(getInnerMsg(iaHandle));
                }
            }
        }


        /**
         * Strips the XML tags from an error message returned from the engine
         * @param xmlMessage the message to strip
         * @return the innermost text
         */
        String getInnerMsg(String xmlMessage) {
            while (xmlMessage.startsWith("<")) {
                xmlMessage = StringUtil.unwrap(xmlMessage);
            }
            return xmlMessage;
        }
    }
    
}
