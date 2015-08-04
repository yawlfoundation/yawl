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
