package org.yawlfoundation.yawl.authentication;

import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.engine.YEngine;
import org.yawlfoundation.yawl.logging.table.YAuditEvent;

import java.util.Hashtable;

/**
 * An extended HashMap to handle connections to the engine from custom services & apps.
 * The map is of the form [sessionHandle, session].
 *
 * Author: Michael Adams
 */


public class YSessionCache extends Hashtable<String, YSession> {

    private static YSessionCache _me ;

    private YSessionCache() {
        super();
    }

    /******************************************************************************/

    // PUBLIC METHODS //

    public static YSessionCache getInstance() {
        if (_me == null) _me = new YSessionCache() ;
        return _me ;
    }


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


    public boolean isServiceConnected(String uri) {
        for (YSession session : this.values()) {
            if (session.getURI().equals(uri)) return true ;
        }
        return false;
    }


    public boolean isClientConnected(YExternalClient client) {
        for (YSession session : this.values()) {
            if (session.getClient() == client) return true ;
        }
        return false;
    }


    public YSession getSession(String handle) {
        if (handle != null) {
            return this.get(handle);
        }
        else return null;
    }


    public void expire(String handle) {
        YSession session = this.remove(handle);
        if (session != null) audit(session.getClient().getUserName(), YAuditEvent.Action.expired);
    }


    public void disconnect(YClient client) {
        for (String handle : this.keySet()) {
            YSession session = this.get(handle);
            if (session.getClient() == client) {
                disconnect(handle);
                break;
            }
        }
    }

    public void disconnect(String handle) {
        YSession session = this.remove(handle);
        if (session != null) audit(session.getClient().getUserName(), YAuditEvent.Action.logoff);
    }


    public void shutdown() {
        for (YSession session : this.values()) {
            audit(session.getClient().getUserName(), YAuditEvent.Action.shutdown);
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