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

package org.yawlfoundation.yawl.engine.interfce.interfaceX;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.yawlfoundation.yawl.engine.YWorkItem;
import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  InterfaceX_EngineSideClient passes exception event calls from the engine to the
 *  exception service.
 *
 *  This class is a member class of Interface X, which provides an interface
 *  between the YAWL Engine and a Custom YAWL Service that manages exception
 *  handling at the process level.
 *
 *  InterfaceB_EngineBasedClient was used as a template for this class.
 *
 *  Schematic of Interface X:
 *                                          |
 *                           EXCEPTION      |                              INTERFACE X
 *                            GATEWAY       |                                SERVICE
 *                  (implements) |          |                       (implements) |
 *                               |          |                                    |
 *  +==========+   ----->   ENGINE-SIDE  ---|-->   SERVICE-SIDE  ----->   +=============+
 *  || YAWL   ||              CLIENT        |        SERVER               || EXCEPTION ||
 *  || ENGINE ||                            |                             ||  SERVICE  ||
 *  +==========+   <-----   ENGINE-SIDE  <--|---   SERVICE-SIDE  <-----   +=============+
 *                            SERVER        |         CLIENT
 *                                          |
 *  @author Michael Adams                   |
 *  @version 0.8, 04/07/2006
 */

public class InterfaceX_EngineSideClient extends Interface_Client implements ExceptionGateway {

    protected static Logger logger = Logger.getLogger(InterfaceX_EngineSideClient.class);

    // event types
    protected static final int NOTIFY_CHECK_CASE_CONSTRAINTS = 0;
    protected static final int NOTIFY_CHECK_ITEM_CONSTRAINTS = 1;
    protected static final int NOTIFY_WORKITEM_ABORT = 2;
    protected static final int NOTIFY_TIMEOUT = 3;
    protected static final int NOTIFY_RESOURCE_UNAVAILABLE = 4;
    protected static final int NOTIFY_CONSTRAINT_VIOLATION = 5;
    protected static final int NOTIFY_CANCELLED_CASE = 6;

    private String _observerURI ;

    // the constructor //
    public InterfaceX_EngineSideClient(String observerURI) {
        _observerURI = observerURI ;
    }


    /**
     * Indicates which protocol this shim services
     *
     * @return the scheme
     */
    public String getScheme() {
        return "http";
    }

    public void setURI(String uri) {
        _observerURI = uri ;
    }

    public String getURI() {
        return _observerURI ;
    }

    /*****************************************************************************/

    // ANNOUNCEMENT METHODS - SEE EXCEPTIONGATEWAY FOR COMMENTS //

    public void announceCheckWorkItemConstraints(YWorkItem item, Document data, boolean preCheck) {
        new Handler(_observerURI, item, data, preCheck, NOTIFY_CHECK_ITEM_CONSTRAINTS).start();
    }


    public void announceCheckCaseConstraints(String specID, String caseID,
                                             String data, boolean preCheck) {
        new Handler(_observerURI, specID, caseID, data, preCheck,
                                              NOTIFY_CHECK_CASE_CONSTRAINTS).start();
    }


    public void announceWorkitemAbort(YWorkItem item) {
        new Handler(_observerURI, item, NOTIFY_WORKITEM_ABORT).start();
    }



    public void announceTimeOut(YWorkItem item, List taskList){
       new Handler(_observerURI, item, taskList, NOTIFY_TIMEOUT).start();
    }


    public void announceResourceUnavailable(YWorkItem item){
        new Handler(_observerURI, item, NOTIFY_RESOURCE_UNAVAILABLE).start();
    }


    public void announceConstraintViolation(YWorkItem item){
        new Handler(_observerURI, item, NOTIFY_CONSTRAINT_VIOLATION).start();
    }


    public void announceCaseCancellation(String caseID){
        new Handler(_observerURI, caseID, NOTIFY_CANCELLED_CASE).start();

    }


  ////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////////////

    /**
     * Handler class called by the above announcement methods - passes the events
     * to the service side.
     */

    private class Handler extends Thread {
        private YWorkItem _workItem;
        private String _observerURI;
        private String _caseID;
        private int _command;
        private boolean _preCheck;
        private Document _dataDoc ;
        private String _dataStr ;
        private String _specID ;
        private List _taskList ;


        /**
         * Different constructors for different event types
         */

        public Handler(String observerURI, YWorkItem workItem, int command) {
            _observerURI = observerURI;
            _workItem = workItem;
            _command = command;
        }

        public Handler(String observerURI, String caseID, int command) {
            _observerURI = observerURI;
            _caseID = caseID;
            _command = command;
        }

        public Handler(String observerURI, YWorkItem workItem, List taskList, int command) {
            _observerURI = observerURI;
            _workItem = workItem;
            _taskList = taskList;
            _command = command;
        }

        public Handler(String observerURI, YWorkItem workItem, Document data,
                       boolean preCheck, int command) {
            _observerURI = observerURI;
            _workItem = workItem;
            _preCheck = preCheck;
            _command = command;
            _dataDoc = data ;
        }

        public Handler(String observerURI, String specID, String caseID, String data,
                       boolean preCheck, int command) {
            _observerURI = observerURI;
            _specID = specID ;
            _caseID = caseID;
            _preCheck = preCheck;
            _command = command;
            _dataStr = data ;
        }

        // POST the event
        public void run() {

           Map<String, String> paramsMap = new HashMap<String, String>();
           try {

               // all events have an event type
                paramsMap.put("action", String.valueOf(_command));

                // additional params as required
                switch (_command) {
                    case InterfaceX_EngineSideClient.NOTIFY_CHECK_CASE_CONSTRAINTS:
                        paramsMap.put("specID", _specID);
                        paramsMap.put("caseID", _caseID);
                        paramsMap.put("preCheck", String.valueOf(_preCheck));
                        paramsMap.put("data", _dataStr);
                        break ;
                    case InterfaceX_EngineSideClient.NOTIFY_CHECK_ITEM_CONSTRAINTS:
                        paramsMap.put("workItem", _workItem.toXML());
                        paramsMap.put("preCheck", String.valueOf(_preCheck));
                        paramsMap.put("data", JDOMUtil.documentToString(_dataDoc));
                        break ;
                    case InterfaceX_EngineSideClient.NOTIFY_CANCELLED_CASE:
                        paramsMap.put("caseID", _caseID);
                        break ;
                    case InterfaceX_EngineSideClient.NOTIFY_TIMEOUT:
                        paramsMap.put("workItem", _workItem.toXML());
                        if (_taskList != null)
                            paramsMap.put("taskList", _taskList.toString());
                        break ;
                }

                // run the post with the appropriate params
                executePost(_observerURI, paramsMap);

            } catch (IOException e) {
                logger.error("failed to call YAWL service", e);
                e.printStackTrace();
            }
        }
    }
}
