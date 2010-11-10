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
import org.yawlfoundation.yawl.resourcing.util.TaggedStringList;
import org.yawlfoundation.yawl.util.HttpURLValidator;

import java.io.IOException;
import java.util.*;

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
    private Map<String, TaggedStringList> _isListeners;

    public ResourceGatewayServer() {
        _isListeners = new Hashtable<String, TaggedStringList>();
    }


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


    public String registerSchedulingInterfaceListener(String logonID, String uri) {
        String msg = HttpURLValidator.validate(uri);
        if (successful(msg)) {
            TaggedStringList uriList = _isListeners.get(logonID);
            if (uriList == null) {
                uriList = new TaggedStringList(logonID, uri);
                _isListeners.put(logonID, uriList);
            }
            else uriList.add(uri);
        }
        return msg;
    }


    public void removeSchedulingInterfaceListener(String logonID, String uri) {
        TaggedStringList uriList = _isListeners.get(logonID);
        if (uriList != null) uriList.remove(uri);
    }


    public void removeSchedulingInterfaceListeners(String logonID) {
        _isListeners.remove(logonID);
    }
 

    public void announceResourceUnavailable(WorkItemRecord wir)
            throws IOException {
        Map<String, String> params = prepareParams(NOTIFY_RESOURCE_UNAVAILABLE);
        params.put("workItem", wir.toXML());
        executePost(_ixURI, params);
    }


    public void announceResourceCalendarStatusChange(String origOwner, String xml)
            throws IOException {
        Map<String, String> params = prepareParams(NOTIFY_UTILISATION_STATUS_CHANGE);
        params.put("xml", xml);
        for (String listener : getSchedulingInterfaceListeners(origOwner)) {
            executePost(listener, params);
        }    
    }


    public void redirectWorkItemToYawlService(String wirXML, String serviceURI)
            throws IOException {
        Map<String, String> paramsMap = prepareParamMap("handleEnabledItem", null);
        paramsMap.put("workItem", wirXML);
        executePost(serviceURI, paramsMap);
    }


    private Map<String, String> prepareParams(int action) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", String.valueOf(action)) ;
        return params;
    }


    private Set<String> getSchedulingInterfaceListeners(String logonID) {
        Set<String> listeners = new HashSet<String>();
        if (_isURI != null) listeners.add(_isURI);
        listeners.addAll(_isListeners.get(logonID));
        return listeners;
    }
}
