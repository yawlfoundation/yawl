/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.cost;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.engine.YSpecificationID;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/**
  *  Shell servlet for the cost service.
  *
  *  @author Michael Adams
  *  @date 11/07/2011
  *
  */

public class CostGateway extends HttpServlet {

    private CostService _service;
    private static final Logger _log = Logger.getLogger(CostGateway.class);


    /** Read settings from web.xml and use them to initialise the service */
    public void init() {
        try {
            ServletContext context = getServletContext();
            _service = CostService.getInstance();

            // load and process init params from web.xml
            String ixURI = context.getInitParameter("InterfaceX_BackEnd");
            if (ixURI != null) _service.setInterfaceXBackend(ixURI);
            
            String rsLogURI = context.getInitParameter("ResourceServiceLogGateway");
            if (rsLogURI != null) _service.setResourceLogURI(rsLogURI);

            _service.setEngineLogonName(context.getInitParameter("EngineLogonUserName"));
            _service.setEngineLogonPassword(context.getInitParameter("EngineLogonPassword"));
        }
        catch (Exception e) {
            _log.error("Cost Service Initialisation Exception", e);
        }
    }


    public void destroy() { }


    public void doGet(HttpServletRequest req, HttpServletResponse res)
                                throws IOException, ServletException {
        doPost(req, res);                                // redirect all GETs to POSTs
    }


    public void doPost(HttpServletRequest req, HttpServletResponse res)
                               throws IOException {
        String action = req.getParameter("action");
        YSpecificationID specID = constructSpecID(req);
        String taskID = req.getParameter("taskid");
        String result = null;

        if (action.equals("importModel")) {
            _service.importModel(req.getParameter("model"));
            result = "SUCCESS";
        }
        else if (action.equals("getAnnotatedLog")) {
            boolean withData = req.getParameter("withData").equalsIgnoreCase("true");
            result = _service.getAnnotatedLog(specID, withData);
        }
        else if (action.equals("getFunctionList")) {
            result = getFunctionList(specID, taskID);
        }
        else if (action.equals("getFixedCosts")) {
            result = getFixedCosts(specID, taskID);
        }
        else if (action.equals("calcCost")) {
            result = calcCost(specID, taskID, req.getParameter("costparams"));
        }
        else throw new IOException("Unknown Cost Service action: " + action);

        // generate the output
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();
        out.write(result);
        out.flush();
        out.close();
    }


    /**
     * Gets an XML list of all cost functions for the specified specification - task
     * combination.
     * @param specID the specification identifier
     * @param taskID the task identifier (may be null, in which case only the case level
     * functions are required)
     * @return an XML list of the cost functions requested, or an appropriate failure
     * message.
     */
    private String getFunctionList(YSpecificationID specID, String taskID) {
        // TODO
        return "";
    }


    /**
     * Gets an XML list of all the fixed costs for the specified specification - task
     * combination.
     * @param specID the specification identifier
     * @param taskID the task identifier (may be null, in which case only the case level
     * costs are required)
     * @return an XML list of the costs requested, or an appropriate failure message.
     */
    private String getFixedCosts(YSpecificationID specID, String taskID) {
        // TODO
        return "";
    }


    /**
     * Calculates the cost of an activity.
     * @param specID the specification identifier
     * @param taskID the task identifier (may be null, in which case only the case level
     * costs are required)
     * @param costParams an XML document containing data variables to be used in the
     * calculation (eg. resources, rates, time durations etc.)
     * @return an XML document containing the actual result of applying the costParams
     * to the relevant cost functions.
     */
    private String calcCost(YSpecificationID specID, String taskID, String costParams) {
        // TODO
        return "";
    }


    private YSpecificationID constructSpecID(HttpServletRequest req) {
        String version = req.getParameter("version") ;
        String uri = req.getParameter("uri") ;
        if ((uri != null) && (version != null)) {
            String identifier = req.getParameter("identifier") ;
            return new YSpecificationID(identifier, version, uri);
        }
        else return null;
    }


    private String failMessage(String msg) {
        return "<failure>" + msg + "</failure>";
    }

}
