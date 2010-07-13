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

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.LogMiner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/**
 *  The Log Gateway class acts as an API gateway between the resource service and
 *  its process logs.
 *
 *  @author Michael Adams
 *  03/02/2010
 */

public class ResourceLogGateway extends HttpServlet {

    private static final Logger _log = Logger.getLogger(ResourceLogGateway.class);
    private LogMiner _logDB ;
    private ResourceManager _rm;

    private final String _noService = "<failure>Not connected to Resource Service.</failure>";
    private final String _noAction = "<failure>Resource Log Gateway called with invalid action.</failure>";


    public void init() {
        _logDB = LogMiner.getInstance() ;
        _rm = ResourceManager.getInstance();
    }


    public void doPost(HttpServletRequest req, HttpServletResponse res)
                               throws IOException {
        String result = "";
        String action = req.getParameter("action");
        String handle = req.getParameter("sessionHandle");
        String key = req.getParameter("key");

        if (action == null) {
            throw new IOException("ResourceLogGateway called with null action.");
        }
        else if (action.equalsIgnoreCase("connect")) {
           String userid = req.getParameter("userid");
           String password = req.getParameter("password");
           if (_rm != null) {
               int interval = req.getSession().getMaxInactiveInterval();
               result = _rm.serviceConnect(userid, password, interval);
           }
           else result = _noService;
       }
       else if (action.equalsIgnoreCase("checkConnection")) {
           if (_rm != null)
               result = String.valueOf(_rm.checkServiceConnection(handle));
           else result = _noService;
       }
       else if (validConnection(handle)) {
           if (action.equals("getCaseStartedBy")) {
               String caseID = req.getParameter("caseid") ;
               result = _logDB.getCaseStartedBy(caseID);
           }
           else if (action.equals("getWorkItemEvents")) {
               String itemID = req.getParameter("itemid") ;
               String fnStr = req.getParameter("fullname") ;
               boolean fullName = (fnStr != null) && fnStr.equalsIgnoreCase("true");
               result = _logDB.getWorkItemEvents(itemID, fullName);
           }
           else if (action.equals("getSpecificationXESLog")) {
               String identifier = req.getParameter("identifier") ;
               String version = req.getParameter("version") ;
               String uri = req.getParameter("uri") ;
               YSpecificationID specID = new YSpecificationID(identifier, version, uri);
               result = _logDB.getSpecificationXESLog(specID);
           }
           else if (action.equals("getMergedXESLog")) {
               String identifier = req.getParameter("identifier") ;
               String version = req.getParameter("version") ;
               String uri = req.getParameter("uri") ;
               YSpecificationID specID = new YSpecificationID(identifier, version, uri);
               String withDataStr = req.getParameter("withdata");
               boolean withData = (withDataStr != null) && withDataStr.equalsIgnoreCase("true");
               result = _logDB.getMergedXESLog(specID, withData);
           }
           else result = _noAction;
       }
       else throw new IOException("Invalid or disconnected session handle.");

       // generate the output
       res.setContentType("text/html");
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
            return _rm.checkServiceConnection(handle) ;
        }
        catch (Exception e) {
            return false;
        }
    }
}