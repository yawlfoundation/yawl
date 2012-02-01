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

import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.engine.YEngine;
import org.yawlfoundation.yawl.logging.table.YAuditEvent;

import java.util.concurrent.ConcurrentHashMap;

/**
 * An extended Hashtable that manages connections to the engine from custom services
 * and external applications.
 * <p/>
 * The map is of the form [sessionHandle, session].
 *
 * @author Michael Adams
 * @since 2.1
 */


public class YSessionCache extends ConcurrentHashMap<String, YSession> {

    public YSessionCache() {
        super();
    }

    /******************************************************************************/

    // PUBLIC METHODS //

    /**
     * Creates and stores a new session between the the Engine and a custom service
     * or external application.
     * @param name the username of the external client
     * @param password the corresponding (hashed) password
     * @param timeOutSeconds the maximum idle time for this session (in seconds). A
     * value of 0 will default to 60 minutes; a value less than zero means this session
     * will never timeout.
     * @return a valid session handle, or an appropriate error message
     */
    public String connect(String name, String password, long timeOutSeconds) {
        if (name == null) return failMsg("Null user name"); 
        String result ;

        // first check if its an external client
        YExternalClient client = YEngine.getInstance().getExternalClient(name);
        if (client != null) {
            if (validateCredentials(client, password)) {

                // (an 'admin' is enabled check has already been done in EngineGatewayImpl)
                YSession session = name.equals("admin") ?
                        new YSession(client, timeOutSeconds) :
                        new YExternalSession(client, timeOutSeconds);
                result = storeSession(session);
            }
            else result = badPassword(name);
        }
        else {

            // now check if its a service
            YAWLServiceReference service = getService(name);
            if (service != null) {
                if (validateCredentials(service, password)) {
                    result = storeSession(new YServiceSession(service, timeOutSeconds));
                }
                else result = badPassword(name);
            }
            else result = unknownUser(name);
        }
        return result ;
    }


    /**
     * Checks that a session handle represents an active session. If it does, the
     * session idle timer is restarted also.
     * @param handle the session handle held by a client or service.
     * @return true if the handle's session is active.
     */
    public boolean checkConnection(String handle) {
        boolean result = false;
        if (handle != null) {
            YSession session = this.get(handle) ;
            if (session != null) {
                session.refresh();
                result = true ;
            }
        }
        return result ;
    }


    /**
     * Checks that a particular custom service has an active session with the Engine.
     * @param uri the uri of the custom service.
     * @return true if the service has an active session.
     */
    public boolean isServiceConnected(String uri) {
        for (YSession session : this.values()) {
            if (session.getURI().equals(uri)) return true ;
        }
        return false;
    }


    /**
     * Checks that a particular external client has an active session with the Engine.
     * @param client the client.
     * @return true if the client has an active session.
     */
    public boolean isClientConnected(YExternalClient client) {
        for (YSession session : this.values()) {
            if (session.getClient() == client) return true ;
        }
        return false;
    }


    /**
     * Gets the session associated with a sesion handle.
     * @param handle a session handle.
     * @return the session object associated with the handle, or null if the handle is
     * invalid or inactive.
     */
    public YSession getSession(String handle) {
        if (handle != null) {
            return this.get(handle);
        }
        else return null;
    }


    /**
     * Removes a session from the set of active sessions after an idle timeout.
     * Also writes the expiration to the session audit log.
     * @param handle the session handle of the session to remove.
     */
    public void expire(String handle) {
        YSession session = this.remove(handle);
        if (session != null) audit(session.getClient().getUserName(), YAuditEvent.Action.expired);
    }


    /**
     * Ends an active session of a custom service or external application.
     * @param client the service or application to disconnect from the Engine.  Also
     * writes the disconnection to the session audit log.
     */
    public void disconnect(YClient client) {
        for (String handle : this.keySet()) {
            YSession session = this.get(handle);
            if (session.getClient() == client) {
                disconnect(handle);
                break;
            }
        }
    }


    /**
     * Ends an active session of a custom service or external application.
     * @param handle the session handle of a service or application to disconnect
     * from the Engine. Also writes the disconnection to the session audit log.
     */
    public void disconnect(String handle) {
        YSession session = this.remove(handle);
        if (session != null) audit(session.getClient().getUserName(), YAuditEvent.Action.logoff);
    }


    /**
     * Called when the hosting server shuts down to write a shutdown record for each
     * active session to the audit log.
     */
    public void shutdown() {
        for (YSession session : this.values()) {
            audit(session.getClient().getUserName(), YAuditEvent.Action.shutdown);
            session.shutdown();
        }
    }


    /******************************************************************************/

    // PRIVATE METHODS //

    private boolean validateCredentials(YAWLServiceReference service, String password) {
        return service.getServicePassword().equals(password);
    }


    private boolean validateCredentials(YExternalClient client, String password) {
        return client.getPassword().equals(password);
    }


    private YAWLServiceReference getService(String name) {
        YEngine engine = YEngine.getInstance();
        if (name.equals("DefaultWorklist")) {
            return engine.getDefaultWorklist();
        }
        for (YAWLServiceReference service : engine.getYAWLServices()) {
            if (service.getServiceName().equals(name)) return service;
        }
        return null;
    }


    private String storeSession(YSession session) {
        String handle = session.getHandle();
        this.put(handle, session);
        audit(session.getClient().getUserName(), YAuditEvent.Action.logon);
        return handle;        
    }


    private String failMsg(String msg) {
        return String.format("<failure>%s</failure>", msg) ;
    }


    private void audit(String username, YAuditEvent.Action action) {
        YEngine.getInstance().writeAudit(new YAuditEvent(username, action));
    }


    private String badPassword(String username) {
        audit(username, YAuditEvent.Action.invalid);
        return failMsg("Incorrect Password");
    }


    private String unknownUser(String username) {
        audit(username, YAuditEvent.Action.unknown);        
        return failMsg("Unknown service or client: " + username);
    }

}