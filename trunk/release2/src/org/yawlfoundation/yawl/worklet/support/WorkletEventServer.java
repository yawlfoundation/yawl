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

package org.yawlfoundation.yawl.worklet.support;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.HttpURLValidator;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.worklet.rdr.RuleType;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author: Michael Adams
 * Creation Date: 12/12/2012
 */
public class WorkletEventServer extends Interface_Client {

    public static enum Event { CaseException, ItemException, Shutdown }
    
    private static final int THREADPOOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService executor = Executors.newFixedThreadPool(THREADPOOL_SIZE);
    private static Logger _log = Logger.getLogger(WorkletEventServer.class);

    private Set<String> _listeners;
        
    private static final String ADD_LISTENER_SUCCESS = "Listener added";
    private static final String REMOVE_LISTENER_SUCCESS = "Listener removed";
    private static final String UNKNOWN_LISTENER = "Unknown Listener";
    private static final String NULL_LISTENER = "Null Listener";



    public WorkletEventServer() {
        _listeners = new HashSet<String>();
    }


    public boolean hasListeners() {
        return _listeners != null;
    }


    public String addListener(String uri) {
        if (uri != null) return NULL_LISTENER;
        String msg = HttpURLValidator.validate(uri);
        if (successful(msg)) {
            _listeners.add(uri);
            return ADD_LISTENER_SUCCESS;
        }
        return msg;
    }


    public String removeListener(String uri) {
        if (uri != null) {
            return _listeners.remove(uri) ? REMOVE_LISTENER_SUCCESS : UNKNOWN_LISTENER;
        }
        return NULL_LISTENER;
    }


    /**************************************************************************/

    public void announceException(String caseID, Element caseData, RuleType rType) {
        if (hasListeners()) {
            Map<String, String> params = prepareParams(Event.CaseException);
            params.put("caseid", caseID);
            announceException(params, caseData, rType);
        }
    }


    public void announceException(WorkItemRecord wir, Element caseData, RuleType rType) {
        if (hasListeners()) {
            Map<String, String> params = prepareParams(Event.ItemException);
            params.put("wir", wir.toXML());
            announceException(params, caseData, rType);
        }
    }


    public void shutdownListeners() {
        announce(prepareParams(Event.Shutdown));
    }
    

    /*****************************************************************************/
    
    private void announceException(Map<String, String> params, Element caseData,
                                   RuleType rType) {
        params.put("casedata", JDOMUtil.elementToString(caseData));
        params.put("ruletype", rType.name());
        announce(params);
    }


    private void announce(Map<String, String> params) {
        for (String uri : _listeners) {
            executor.execute(new Announcement(uri, params));
        }        
    }


    private Map<String, String> prepareParams(Event event) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", event.name()) ;
        return params;
    }
    

    /****************************************************************************/

    private class Announcement implements Runnable {

        Map<String, String> _params;
        String _uri;

        Announcement(String uri, Map<String, String> params) {
            _params = params;
            _uri = uri;
        }

        public void run() {
            try {
                executePost(_uri, _params);
            }
            catch (IOException ioe) {
                _log.error("Failed to announce worklet event '" + _params.get("action") +
                     "' to URI '" + _uri + "'", ioe);
            }
        }

    }

}
