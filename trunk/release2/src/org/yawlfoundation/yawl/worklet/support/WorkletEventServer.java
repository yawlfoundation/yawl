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
import org.yawlfoundation.yawl.worklet.selection.CheckedOutChildItem;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Announces worklet service events to registered listeners
 *
 * Author: Michael Adams
 * Creation Date: 12/12/2012
 */
public class WorkletEventServer extends Interface_Client {

    // an enum of announcement types
    public static enum Event { Selection, CaseException, ItemException, Shutdown }
    
    private static final int THREADPOOL_SIZE = Runtime.getRuntime().availableProcessors();
    private ExecutorService _executor;
    private final Logger _log;

    private Set<String> _listeners;                      // set of registered listeners
        
    private static final String ADD_LISTENER_SUCCESS = "Listener added successfully";
    private static final String REMOVE_LISTENER_SUCCESS = "Listener removed successfully";
    private static final String UNKNOWN_LISTENER = "Unknown Listener";
    private static final String NULL_LISTENER = "Null Listener";


    /**
     * Constructs a new event server
     */
    public WorkletEventServer() {
        _log = Logger.getLogger(WorkletEventServer.class);
    }


    /**
     * @return true if the server has at least one registered listener
     */
    public boolean hasListeners() {
        return ! (_listeners == null || _listeners.isEmpty());
    }


    /**
     * Adds a listener to the set of registered listeners
     * @param uri the URI of the listener to add
     * @return a message describing success or error
     */
    public String addListener(String uri) {
        if (uri == null) return NULL_LISTENER;
        String msg = HttpURLValidator.validate(uri);
        if (successful(msg)) {
            addValidatedListener(uri);
            return ADD_LISTENER_SUCCESS;
        }
        return msg;
    }


    /**
     * Removes a listener to the set of registered listeners
     * @param uri the URI of the listener to remove
     * @return a message describing success or error
     */
    public String removeListener(String uri) {
        if (uri != null) {
            String msg = _listeners.remove(uri) ? REMOVE_LISTENER_SUCCESS : UNKNOWN_LISTENER;
            if (! hasListeners()) {
                _executor.shutdown();
            }
            return msg;
        }
        return NULL_LISTENER;
    }


    /**************************************************************************/

    /**
     * Announces a case-level exception
     * @param caseID the id of the case on which the exception was raised
     * @param caseData the current case data used to evaluate the exception
     * @param rType the type of exception raised
     */
    public void announceException(String caseID, Element caseData, RuleType rType) {
        if (hasListeners()) {
            Map<String, String> params = prepareParams(Event.CaseException);
            params.put("caseid", caseID);
            announceException(params, caseData, rType);
        }
    }


    /**
     * Announces an item-level exception
     * @param wir the workitem on which the exception was raised
     * @param caseData the current case data used to evaluate the exception
     * @param rType the type of exception raised
     */
    public void announceException(WorkItemRecord wir, Element caseData, RuleType rType) {
        if (hasListeners()) {
            Map<String, String> params = prepareParams(Event.ItemException);
            params.put("wir", wir.toXML());
            announceException(params, caseData, rType);
        }
    }


    /**
     * Announces a worklet selection
     * @param item a descriptor containing case, workitem, data and raised worklet info
     */
    public void announceSelection(CheckedOutChildItem item) {
        if (hasListeners()) {
            Map<String, String> params = prepareParams(Event.Selection);
            params.put("wir", item.get_wirStr());
            Map<String, String> runners = item.getCaseMapAsCSVList();
            for (String key : runners.keySet()) {
                params.put(key, runners.get(key));
            }
            announce(params);
        }
    }


    /**
     * Announces the worklet service is shutting down
     */
    public void shutdownListeners() {
        if (hasListeners()) announce(prepareParams(Event.Shutdown));
        if (_executor != null) {
            _executor.shutdown();

            // give it a moment to complete or fail the announcement
            try {
                _executor.awaitTermination(10, TimeUnit.SECONDS);
            }
            catch (InterruptedException ie) {
                _executor.shutdown();
            }
        }
    }
    

    /*****************************************************************************/
    
    /**
     * Announces an exception to each registered listener
     * @param params a parameter map describing the exception
     * @param caseData the current case data used to evaluate the exception
     * @param rType the type of exception raised
     */
    private void announceException(Map<String, String> params, Element caseData,
                                   RuleType rType) {
        params.put("casedata", JDOMUtil.elementToString(caseData));
        params.put("ruletype", rType.name());
        announce(params);
    }


    /**
     * Makes an announcement to each registered listener
     * @param params a parameter map describing the announcement
     */
    private void announce(Map<String, String> params) {
        for (String uri : _listeners) {
            _executor.execute(new Announcement(uri, params));
        }
    }
    

    /**
     * Initialises a parameter map, inserting the type of event to announce
     * @param event
     * @return
     */
    private Map<String, String> prepareParams(Event event) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", event.name()) ;
        return params;
    }


    /**
     * Adds a pre-validated listener to the set of registered listeners
     * @param uri the URI of the listener to add
     */
    private void addValidatedListener(String uri) {
        if (! hasListeners()) {
            if (_listeners == null) _listeners = new HashSet<String>();
            _executor = Executors.newFixedThreadPool(THREADPOOL_SIZE);
        }
        _listeners.add(uri);
    }


    /****************************************************************************/

    /**
     * This class does the actual announcing (via http)
     */
    private class Announcement implements Runnable {

        Map<String, String> _params;
        String _uri;

        /**
         * Constructor
         * @param uri the URI to make the announcement to
         * @param params the announcement parameters
         */
        Announcement(String uri, Map<String, String> params) {
            _params = params;
            _uri = uri;
        }


        /**
         * Post the announcement
         */
        public void run() {
            try {
                executePost(_uri, _params);
            }
            catch (IOException ioe) {
                 _log.warn("Failed to announce worklet event '" +
                           _params.get("action") + "' to URI '" + _uri +
                           "'. Perhaps that listener is no longer available.");
            }
        }

    }

}
