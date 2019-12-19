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

package org.yawlfoundation.yawl.resourcing.rsInterface;

import org.yawlfoundation.yawl.authentication.ISessionCache;
import org.yawlfoundation.yawl.authentication.YSession;
import org.yawlfoundation.yawl.logging.table.YAuditEvent;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.EventLogger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An extended HashMap to handle connections from external entities (such as the YAWL
 * Editor) and the resource service. The map is of the form [sessionHandle, connection].
 *
 * @author Michael Adams
 * @since 2.0
 * @date Oct 24, 2007
 */


public class ConnectionCache extends ConcurrentHashMap<String, ServiceConnection>
                             implements ISessionCache {

    private static ConnectionCache _me ;
    private Map<String,String> _userdb ;
    private ServiceConnectionTimer _timer;

    private ConnectionCache() {
        super();
        initUserDB() ;
        _timer = new ServiceConnectionTimer(this);
        _me = this ;
    }

    /******************************************************************************/

    // PUBLIC METHODS //   

    public static ConnectionCache getInstance() {
        if (_me == null) _me = new ConnectionCache() ;
        return _me ;
    }


    public void addUsers(Map<String, String> users) {
        _userdb.putAll(users);
    }


    public void addUser(String userid, String password) {
        _userdb.put(userid, password);
    }


    public void updateUser(String userid, String password) {
        _userdb.put(userid, password);
    }


    public void deleteUser(String userid) {
        _userdb.remove(userid);
    }


    public void clearUsers() {
        _userdb.clear();
    }

    
    public String connect(String userid, String password, long timeOutSeconds) {
        String result ;
        if (validUser(userid))  {
            if (validPassword(userid, password)) {
                result = storeSession(new ServiceConnection(userid, timeOutSeconds));
                EventLogger.audit(userid, EventLogger.audit.gwlogon);
            }
            else {
                result = failMsg("Incorrect Password");
                EventLogger.audit(userid, EventLogger.audit.gwinvalid);
            }
        }
        else {
            result = failMsg(String.format("Unknown Username: '%s'", userid));
            EventLogger.audit(userid, EventLogger.audit.gwunknown);
        }

        return result ;
    }


    public void disconnect(String handle) {
        removeConnection(handle, EventLogger.audit.gwlogoff);
    }


    public void expire(String handle) {
        removeConnection(handle, EventLogger.audit.gwexpired);
    }

    public ServiceConnection getSession(String handle) {
        if (handle != null) {
             return this.get(handle);
         }
         return null;
    }


    public boolean checkConnection(String handle) {
        boolean result = false;
        if (handle != null) {
            ServiceConnection con = this.get(handle) ;
            if (con != null) {
                _timer.reset(con);
                result = true ;
            }
        }
        return result ;
    }


    public void shutdown() {
        for (ServiceConnection con : this.values()) {
            EventLogger.audit(con.getUserID(), EventLogger.audit.shutdown);
        }
        _timer.shutdown();
    }


    public boolean hasUser(String userid) {
        return (userid != null) && _userdb.containsKey(userid);
    }


    public boolean hasUsers() {
        return ! _userdb.isEmpty();
    }

    
    public String getPassword(String userid) {
        return _userdb.get(userid);
    }

    /******************************************************************************/

    // PRIVATE METHODS //

    private boolean connected(String userid) {
        Collection<ServiceConnection> cons = this.values() ;
        for (ServiceConnection con : cons)
            if (con.getUserID().equals(userid)) return true ;
        return false;
    }


    private void removeConnection(String handle, EventLogger.audit auditType) {
        ServiceConnection con = this.remove(handle);
        if (con != null) {
            _timer.expire(con);
            EventLogger.audit(con.getUserID(), auditType);
        }
    }

    
    private boolean validPassword(String userid, String password) {
        return (userid != null) && _userdb.get(userid).equals(password);
    }


    private boolean validUser(String userid) {
        return (userid != null) && _userdb.containsKey(userid) ;

    }

    private void initUserDB() {
        _userdb = new HashMap<String, String>();
    }


    private String failMsg(String msg) {
        return String.format("<failure>%s</failure>", msg) ;
    }

    private String storeSession(ServiceConnection session) {
        String handle = session.getHandle();
        this.put(handle, session);
        _timer.add(session);
        return handle;
    }

}

