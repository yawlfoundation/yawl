/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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
import org.yawlfoundation.yawl.engine.interfce.interfaceA.InterfaceA_EnvironmentBasedClient;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Michael Adams
 * @date 9/11/11
 */
public class Sessions {
    
    private Map<String, String> idToHandle;
    private Map<String, ScheduledFuture> handleToTimer;
    private Map<String, String> credentials;
    private InterfaceA_EnvironmentBasedClient iaClient;
    private String iaUserid;
    private String iaPassword;
    private String iaHandle;
    private String iaURI;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private static final String INVALID_PASSWORD = "<failure>Invalid password</failure>";
    private static final String UNKNOWN_USER = "<failure>Unknown user id</failure>";
    private static final String NO_IA_CREDENTIALS =
            "<failure>No Interface A credentials supplied to service</failure>";

    public Sessions() {
        idToHandle = new Hashtable<String, String>();
        handleToTimer = new Hashtable<String, ScheduledFuture>();
        credentials = new Hashtable<String, String>();
        credentials.put("admin", PasswordEncryptor.encrypt("YAWL", "YAWL"));  // def user
    }
    
    
    public void setupInterfaceA(String uri, String userid, String password) {
        iaURI = uri;
        iaUserid = userid;
        iaPassword = password;
    }


    public void shutdown() {
        for (String handle : handleToTimer.keySet()) {
            removeActivityTimer(handle);
        }
    }
    
    public String connect(String userid, String password) {
        if (! credentials.containsKey(userid)) {
           getCredentialsFromEngine(userid);
        }
        if (credentials.containsKey(userid)) {
            if (credentials.get(userid).equals(password)) {
                return getHandle(userid);
            }
            else return INVALID_PASSWORD;
        }
        else return UNKNOWN_USER;
    }
    
    
    public boolean checkConnection(String handle) {
        if (handleToTimer.containsKey(handle)) {
            refreshActivityTimer(handle);
            return true;
        }
        return false;
    }
    
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

    private String getHandle(String userid) {
        String handle = idToHandle.get(userid);
        if (handle == null) {
            handle = UUID.randomUUID().toString();
            idToHandle.put(userid, handle);
        }
        startActivityTimer(handle);
        return handle;
    }


    private void refreshActivityTimer(final String handle) {
        removeActivityTimer(handle);
        startActivityTimer(handle);
    }


    private void startActivityTimer(final String handle) {
        final ScheduledFuture<?> inactivityTimer = scheduler.schedule(
            new Runnable() {
                public void run() { disconnect(handle); }
            },
            60, TimeUnit.MINUTES);
        handleToTimer.put(handle, inactivityTimer);
    }


    private void removeActivityTimer(final String handle) {
        final ScheduledFuture<?> inactivityTimer = handleToTimer.remove(handle);
        if (inactivityTimer != null) inactivityTimer.cancel(true);
    }


    private String getCredentialsFromEngine(String userid) {
        if ((iaUserid == null) || (iaPassword == null) || (iaURI == null)) {
            return NO_IA_CREDENTIALS;
        }
        if (iaClient == null) {
            iaClient = new InterfaceA_EnvironmentBasedClient(iaURI);
        }
        try {
            if ((iaHandle == null) ||
                    (! iaClient.successful(iaClient.checkConnection(iaHandle)))) {
                iaHandle = iaClient.connect(iaUserid, iaPassword);
            }
            YClient engineClient = iaClient.getYAWLService(userid, iaHandle);
            if (engineClient == null) {
                engineClient = iaClient.getClientAccount(userid, iaHandle);
            }
            if (engineClient != null) {
                credentials.put(userid, engineClient.getPassword());
            }
            else return UNKNOWN_USER;
        }
        catch (IOException ioe) {
            return "<failure>" + ioe.getMessage() + "</failure>";
        }
        return "<success/>";
    }
    
}
