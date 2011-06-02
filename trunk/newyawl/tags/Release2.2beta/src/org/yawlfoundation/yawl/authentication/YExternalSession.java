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

package org.yawlfoundation.yawl.authentication;

import org.yawlfoundation.yawl.engine.YEngine;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;

/**
 * Maintains an active session belonging to an external application client.
 *
 * @author Michael Adams
 * @since 2.1
 * @date 24/11/2009
 */
public class YExternalSession extends YSession {

    private YExternalClient _client;                          // the connected client

    public YExternalSession(YExternalClient client, long timeOutSeconds) {
        super(timeOutSeconds);
        _client = client ;
    }


    /**
     * Implementation of super abstract method that has no meaning for external clients.
     * @return null (always)
     */
    public String getURI() {
        return null;                                     // clients don't require a uri
    }

    public String getName() {
        return _client != null ? _client.getUserName() : null;
    }

    /**
     * Gets the client's password.
     * @return the (hashed) password of the client associated with this session. 
     */
    public String getPassword() {
        return _client != null ? _client.getPassword() : null;
    }


    /**
     * Updates (and persists) the password for an external client.
     * @param password the (hashed) password to set for the external client.
     * @throws YPersistenceException if there's some problem persisting the change.
     */
    public void setPassword(String password) throws YPersistenceException {
        if (_client != null) {
            _client.setPassword(password);
            YEngine.getInstance().updateObject(_client);
        }
    }

    public void setClient(YExternalClient client) {
        _client = client;
    }

    public YExternalClient getClient() {
        return _client;
    }
}
