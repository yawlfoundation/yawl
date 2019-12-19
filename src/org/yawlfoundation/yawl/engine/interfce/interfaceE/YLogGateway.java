/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.engine.interfce.interfaceE;

import org.apache.logging.log4j.LogManager;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.EngineGatewayImpl;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.logging.YLogServer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/**
 *  The Log Gateway class acts as an API gateway between YAWL and its process logs.
 *
 *  @author Michael Adams
 *  v0.1, 29/10/2007
 *
 *  Last Date: 1/2009
 */

public class YLogGateway extends HttpServlet {

    private YLogServer _logSvr = YLogServer.getInstance() ;
    private EngineGatewayImpl _engine ;

    private final String _noEngine = "<failure>Not connected to YAWL Engine.</failure>";


    public void init() {
        try {
            _engine = new EngineGatewayImpl(true) ;            // get engine reference
        }
        catch (YPersistenceException ype) {
            LogManager.getLogger(YLogGateway.class).error(
                    "Could not connect to YAWL Engine.", ype);
        }
    }


    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        String result = "";
        String action = req.getParameter("action");
        String handle = req.getParameter("sessionHandle");
        String key = req.getParameter("key");

        if (action == null) {
            throw new IOException("YLogGateway called with null action.");
        }
        else if (action.equalsIgnoreCase("connect")) {
            String userid = req.getParameter("userid");
            String password = req.getParameter("password");
            if (_engine != null) {
                int interval = req.getSession().getMaxInactiveInterval();
                result = _engine.connect(userid, password, interval);
            }
            else result = _noEngine ;
        }
        else if (action.equalsIgnoreCase("checkConnection")) {
            if (_engine != null)
                result = _engine.checkConnection(handle) ;
            else result = _noEngine ;
        }
        else if (validConnection(handle)) {
            synchronized(_logSvr.getPersistenceManager()) {
                boolean isLocalTransaction = _logSvr.startTransaction();
                if (action.equals("getAllSpecifications")) {
                    result = _logSvr.getAllSpecifications();
                }
                else if (action.equals("getNetInstancesOfSpecification")) {
                    if (key != null) {
                        result = _logSvr.getNetInstancesOfSpecification(new Long(key)) ;
                    }
                    else {
                        YSpecificationID specID = getSpecificationID(req);
                        result = _logSvr.getNetInstancesOfSpecification(specID) ;
                    }
                }
                else if (action.equals("getCompleteCaseLogsForSpecification")) {
                    if (key != null) {
                        result = _logSvr.getCompleteCaseLogsForSpecification(new Long(key)) ;
                    }
                    else {
                        YSpecificationID specID = getSpecificationID(req);
                        result = _logSvr.getCompleteCaseLogsForSpecification(specID) ;
                    }
                }
                else if (action.equals("getSpecificationStatistics")) {
                    if (key != null) {
                        result = _logSvr.getSpecificationStatistics(new Long(key)) ;
                    }
                    else {
                        long from = strToLong(req.getParameter("from"));
                        long to = strToLong(req.getParameter("to"));
                        YSpecificationID specID = getSpecificationID(req);
                        result = _logSvr.getSpecificationStatistics(specID, from, to) ;
                    }
                }
                else if (action.equals("getSpecificationCaseIDs")) {
                    if (key != null) {
                        result = _logSvr.getSpecificationCaseIDs(new Long(key));
                    }
                    else {
                        YSpecificationID specID = getSpecificationID(req);
                        result = _logSvr.getSpecificationCaseIDs(specID);
                    }
                }
                else if (action.equals("getCaseEvents")) {
                    if (key != null) {
                        result = _logSvr.getCaseEvents(new Long(key));
                    }
                    else {
                        String caseID = req.getParameter("caseid") ;
                        result = _logSvr.getCaseEvents(caseID);
                    }
                }
                else if (action.equals("getDataForEvent")) {
                    result = _logSvr.getDataForEvent(new Long(key)) ;
                }
                else if (action.equals("getDataTypeForDataItem")) {
                    result = _logSvr.getDataTypeForDataItem(new Long(key)) ;
                }
                else if (action.equals("getTaskInstancesForCase")) {
                    String caseID = req.getParameter("caseid") ;
                    result = _logSvr.getTaskInstancesForCase(caseID);
                }
                else if (action.equals("getTaskInstancesForTask")) {
                    result = _logSvr.getTaskInstancesForTask(new Long(key));
                }
                else if (action.equals("getCaseEvent")) {
                    String event = req.getParameter("event");
                    String caseID = req.getParameter("caseid") ;
                    result = _logSvr.getCaseEvent(caseID, event);
                }
                else if (action.equals("getAllCasesStartedByService")) {
                    String name = req.getParameter("name") ;
                    result = _logSvr.getAllCasesStartedByService(name) ;
                }
                else if (action.equals("getAllCasesCancelledByService")) {
                    String name = req.getParameter("name") ;
                    result = _logSvr.getAllCasesCancelledByService(name) ;
                }
                else if (action.equals("getInstanceEvents")) {
                    result = _logSvr.getInstanceEvents(new Long(key)) ;
                }
                else if (action.equals("getServiceName")) {
                    result = _logSvr.getServiceName(new Long(key)) ;
                }
                else if (action.equals("getCompleteCaseLog")) {
                    String caseID = req.getParameter("caseid") ;
                    result = _logSvr.getCompleteCaseLog(caseID) ;
                }
                else if (action.equals("getEventsForTaskInstance")) {
                    String itemID = req.getParameter("itemid") ;
                    result = _logSvr.getEventsForTaskInstance(itemID) ;
                }
                else if (action.equals("getTaskInstancesForCaseTask")) {
                    String caseID = req.getParameter("caseid") ;
                    String taskName = req.getParameter("taskname") ;
                    result = _logSvr.getTaskInstancesForTask(caseID, taskName);
                }
                else if (action.equals("getSpecificationXESLog")) {
                    YSpecificationID specID = getSpecificationID(req);
                    String withDataStr = req.getParameter("withdata");
                    boolean withData = (withDataStr != null) && withDataStr.equalsIgnoreCase("true");
                    result = _logSvr.getSpecificationXESLog(specID, withData);
                }
                if (isLocalTransaction) _logSvr.commitTransaction();
            }
        }
        else throw new IOException("Invalid or disconnected session handle.");

        // generate the output
        res.setContentType("text/xml; charset=UTF-8");
        PrintWriter out = res.getWriter();
        out.write(result);
        out.flush();
        out.close();
    }


    public void doGet(HttpServletRequest req, HttpServletResponse res)
                                throws IOException, ServletException {
        doPost(req, res);
    }


    private boolean validConnection(String handle) {
        try {
            String result = _engine.checkConnection(handle) ;
            return result.equals("<success/>");
        }
        catch (Exception e) {
            return false;
        }
    }


    private YSpecificationID getSpecificationID(HttpServletRequest req) {
        String identifier = req.getParameter("identifier");
        String version = req.getParameter("version");
        String uri = req.getParameter("uri");
        return new YSpecificationID(identifier, version, uri);
    }


    private long strToLong(String s) {
        try {
            return new Long(s);
        }
        catch (NumberFormatException nfe) {
            return -1;
        }
    }
}