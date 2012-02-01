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

package org.yawlfoundation.yawl.resourcing.util;

import org.yawlfoundation.yawl.resourcing.constraints.AbstractConstraint;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceGatewayClientAdapter;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * An small example of how one might use the resource client api
 *
 * Author: Michael Adams
 * Date: Oct 26, 2007
 */

public class ExampleAdapter extends ResourceGatewayClientAdapter {

    private String _handle ;
    private String _userName = "admin" ;
    private String _password = "YAWL" ;
    private String _defURI = "http://localhost:8080/resourceService/gateway" ;

    public ExampleAdapter() {
        super();
        setClientURI(_defURI) ;
        _handle = connect(_userName, _password) ;
    }

    public ExampleAdapter(String uri) {
        super(uri) ;
        _handle = connect(_userName, _password) ;
    }

    // attempts a connection if not already connected
    private boolean connected() {
        if (! checkConnection(_handle)) {
            _handle = connect(_userName, _password) ;
            return _rgclient.successful(_handle) ;
        }
        else return true ;
    }


    // get participants example
    private void getParticipants() {
        if (connected()) {
            try {
                List example = getParticipants(_handle) ;
                if (example != null) {
                    Iterator itr = example.iterator();
                    while (itr.hasNext()) {
                        Participant p = (Participant) itr.next();
                        String name = p.getFullName();
                        String id = p.getID();
                        // ... and so on
                    }
                }
            }
            catch (Exception ioe) {
                // some problem connecting to service gateway
            }
        }
    }


    // get Constraints example
    private void getConstraints() {
        if (connected()) {
            try {
                List example = getConstraints(_handle) ;
                if (example != null) {
                    Iterator itr = example.iterator();
                    while (itr.hasNext()) {
                        AbstractConstraint c = (AbstractConstraint) itr.next();
                        String name = c.getDisplayName() ;
                        String desc = c.getDescription() ;
                        Set<String> keys = c.getKeys();
                        // ... and so on
                    }
                }
            }
            catch (Exception ioe) {
                // some problem connecting to service gateway
            }
        }
    }

}
