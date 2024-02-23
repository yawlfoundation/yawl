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

package org.yawlfoundation.yawl.authentication;

import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.engine.YEngine;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;

/**
 * Maintains an active session belonging to a custom service.
 *
 * @author Michael Adams
 * @since 2.1
 */

public class YServiceSession extends YSession {

    private YAWLServiceReference _service;                      // the connected service

    public YServiceSession(YAWLServiceReference service, long timeOutSeconds) {
        super(timeOutSeconds);
        _service = service ;
    }

    /**
     * Get's the service's URI.
     * @return the URI of the service associated with this session.  
     */
    public String getURI() {
        return  _service != null ? _service.getURI() : null;
    }

    public String getName() {
        return _service != null ? _service.getServiceName() : null;
    }

    /**
     * Gets the services's password.
     * @return the (hashed) password of the client associated with this session.
     */
    public String getPassword() {
        return _service != null ? _service.getServicePassword() : null;
    }


    /**
     * Updates (and persists) the password for the custom service.
     * @param password the (hashed) password to set for the service.
     * @throws YPersistenceException if there's some problem persisting the change.
     */
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