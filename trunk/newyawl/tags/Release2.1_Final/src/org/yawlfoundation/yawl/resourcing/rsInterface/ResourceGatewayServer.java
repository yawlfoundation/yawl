/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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

import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: Michael Adams
 * Creation Date: 12/06/2009
 */
public class ResourceGatewayServer extends Interface_Client {

    //'borrowed' from InterfaceX_EngineSideClient
    protected static final int NOTIFY_RESOURCE_UNAVAILABLE = 4;

    private String _ixURI ;                            // the uri to Interface X Service

    public ResourceGatewayServer(String uri) {
        _ixURI = uri;
    }

    public void announceResourceUnavailable(WorkItemRecord wir)
            throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", String.valueOf(NOTIFY_RESOURCE_UNAVAILABLE)) ;
        params.put("workItem", wir.toXML());
        executePost(_ixURI, params);
    }
}
