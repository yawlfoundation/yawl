package org.yawlfoundation.yawl.authentication;

import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.engine.YEngine;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;

/**
 * Maintains a session between the engine and a custom service
 *
 * Author: Michael Adams
 */

public class YServiceSession extends YSession {

    private YAWLServiceReference _service;                      // the connected service

    public YServiceSession(YAWLServiceReference service, long timeOutSeconds) {
        super(timeOutSeconds);
        _service = service ;
    }


    public String getURI() {
        return  _service != null ? _service.getURI() : null;
    }

    public String getName() {
        return _service != null ? _service.getServiceName() : null;
    }

    public String getPassword() {
        return _service != null ? _service.getServicePassword() : null;
    }

    public void setPassword(String password) throws YPersistenceException {
        if (_service != null) {
            _service.setServicePassword(password);
            YEngine.getInstance().updateObject(_service);
        }
    }


    public void setService(YAWLServiceReference service) {
        _service = service;
    }

    public YAWLServiceReference getClient() {
        return _service;
    }
}