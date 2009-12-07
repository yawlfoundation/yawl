package org.yawlfoundation.yawl.authentication;

import org.yawlfoundation.yawl.engine.YEngine;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;

/**
 * Author: Michael Adams
 * Creation Date: 24/11/2009
 */
public class YExternalSession extends YSession {

    private YExternalClient _client;                          // the connected client

    public YExternalSession(YExternalClient client) {
        super();
        _client = client ;
    }


    public String getURI() {
        return null;                                     // clients done require a uri
    }

    public String getName() {
        return _client != null ? _client.getUserID() : null;
    }

    public String getPassword() {
        return _client != null ? _client.getPassword() : null;
    }

    public void setPassword(String password) throws YPersistenceException {
        if (_client != null) {
            _client.setPassword(password);
            YEngine.getInstance().updateObject(_client);
        }
    }

    public void setClient(YExternalClient service) {
        _client = service;
    }

    public YExternalClient getClient() {
        return _client;
    }
}
