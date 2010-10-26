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

    public static final int NOTIFY_UTILISATION_STATUS_CHANGE = 0;


    private String _ixURI ;      // the uri to Interface X Service (exception handling)
    private String _isURI ;      // the uri to Interface S Service (resource scheduling)

    public ResourceGatewayServer() { }


    public void setExceptionInterfaceURI(String uri) {
        _ixURI = uri;
    }


    public void setSchedulingInterfaceURI(String uri) {
        _isURI = uri;
    }


    public boolean hasExceptionListener() {
        return _ixURI != null;
    }


    public boolean hasSchedulingListener() {
        return _isURI != null;
    }


    public void announceResourceUnavailable(WorkItemRecord wir)
            throws IOException {
        Map<String, String> params = prepareParams(NOTIFY_RESOURCE_UNAVAILABLE);
        params.put("workItem", wir.toXML());
        executePost(_ixURI, params);
    }


    private void announceUtilisationStatusChange(String caseID, String activityID, long timestamp
                                         ) throws IOException {
        Map<String, String> params = prepareParams(NOTIFY_UTILISATION_STATUS_CHANGE);
        params.put("caseid", caseID);
        params.put("activityid", activityID);
        params.put("timestamp", String.valueOf(timestamp));
        executePost(_isURI, params);
    }


    private Map<String, String> prepareParams(int action) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", String.valueOf(action)) ;
        return params;
    }
}
