package org.yawlfoundation.yawl.resourcing.rsInterface;

import org.yawlfoundation.yawl.resourcing.datastore.eventlog.EventLogger;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

/**
 * An extended HashMap to handle connections from external entities (such as the YAWL
 * Editor) and the resource service. The map is of the form [sessionHandle, connection].
 *
 * Author: Michael Adams
 * Date: Oct 24, 2007
 * Time: 9:47:08 AM
 * Version: 0.1
 *
 */


public class ConnectionCache extends Hashtable<String, ServiceConnection> {

    private static ConnectionCache _me ;
    private Hashtable<String,String> _userdb ;

    private ConnectionCache() {
        super();
        initUserDB() ;
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
                ServiceConnection con = new ServiceConnection(userid, timeOutSeconds) ;
                result = con.getHandle();
                this.put(result, con);
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
        ServiceConnection con = this.remove(handle);
        EventLogger.audit(con.getUserID(), EventLogger.audit.gwlogoff);
    }


    public void expire(String handle) {
        ServiceConnection con = this.remove(handle);
        EventLogger.audit(con.getUserID(), EventLogger.audit.gwexpired);
    }


    public boolean checkConnection(String handle) {
        boolean result = false;
        if (handle != null) {
            ServiceConnection con = this.get(handle) ;
            if (con != null) {
                con.resetActivityTimer();
                result = true ;
            }
        }
        return result ;
    }


    public void shutdown() {
        for (ServiceConnection con : this.values()) {
            EventLogger.audit(con.getUserID(), EventLogger.audit.shutdown);            
        }
    }


    public boolean hasUser(String userid) {
        return _userdb.containsKey(userid);
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

    
    private boolean validPassword(String userid, String password) {
        return _userdb.get(userid).equals(password);
    }


    private boolean validUser(String userid) {
        return _userdb.containsKey(userid) ;

    }

    private void initUserDB() {
        _userdb = new Hashtable<String,String>();
    }


    private String failMsg(String msg) {
        return String.format("<failure>%s</failure>", msg) ;
    }



}
