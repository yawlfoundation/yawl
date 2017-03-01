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

package org.yawlfoundation.yawl.resourcing.rsInterface;

import org.apache.logging.log4j.LogManager;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.ResourceEvent;
import org.yawlfoundation.yawl.resourcing.util.TaggedStringList;
import org.yawlfoundation.yawl.util.HttpURLValidator;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Author: Michael Adams
 * Creation Date: 12/06/2009
 */
public class ResourceGatewayServer extends Interface_Client {

    //'borrowed' from InterfaceX_EngineSideClient
    protected static final int NOTIFY_RESOURCE_UNAVAILABLE = 4;

    public static final int NOTIFY_UTILISATION_STATUS_CHANGE = 0;
    public static final int NOTIFY_RESOURCE_EVENT = 1;


    private String _ixURI ;      // the uri to Interface X Service (exception handling)
    private String _isURI ;      // the uri to Interface S Service (resource scheduling)
    private Map<String, TaggedStringList> _isListeners;
    private Set<String> _eventListeners;

    public ResourceGatewayServer() {
        _isListeners = new HashMap<String, TaggedStringList>();
        _eventListeners = new HashSet<String>();
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


    public boolean hasSchedulingListener(String origOwner) {
        return ! getSchedulingInterfaceListeners(origOwner).isEmpty();
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


    public String addEventListener(String uri) {
        String msg = HttpURLValidator.validate(uri);
        if (successful(msg)) {
            _eventListeners.add(uri);
        }
        return msg;
    }


    public boolean removeEventListener(String uri) {
        return _eventListeners.remove(uri);
    }
 

    public void announceResourceUnavailable(String resourceID, WorkItemRecord wir,
            String caseData, boolean primary) throws IOException {
        if (hasExceptionListener()) {
            Map<String, String> params = prepareParams(NOTIFY_RESOURCE_UNAVAILABLE);
            params.put("resourceid", resourceID);
            params.put("workItem", wir.toXML());
            params.put("data", caseData);
            params.put("primary", String.valueOf(primary));
            executePost(_ixURI, params);
        }
    }


    public void announceResourceCalendarStatusChange(String origOwner, String xml)
            throws IOException {
        if (hasSchedulingListener(origOwner)) {
            Map<String, String> params = prepareParams(NOTIFY_UTILISATION_STATUS_CHANGE);
            params.put("xml", xml);
            for (String listener : getSchedulingInterfaceListeners(origOwner)) {
                executePost(listener, params);
            }
        }
    }


    public void announceResourceEvent(YSpecificationID specID, ResourceEvent event) {
        for (String listener : _eventListeners) {
            Map<String, String> params = prepareParams(NOTIFY_RESOURCE_EVENT);
            params.putAll(specID.toMap());
            params.put("event", event.toXML());
            try {
                executePost(listener, params);
            }
            catch (IOException ioe) {
                LogManager.getLogger(this.getClass()).warn(
                        "Failed to announce event to listener: {}", listener);
            }
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
