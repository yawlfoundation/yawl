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

package org.yawlfoundation.yawl.cost.interfce;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yawlfoundation.yawl.cost.CostService;
import org.yawlfoundation.yawl.cost.data.CostModelCache;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.util.Sessions;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;


/**
 * Shell servlet for the cost service.
 *
 * @author Michael Adams
 * @date 11/07/2011
 */

public class CostGateway extends HttpServlet {

    private CostService _service;
    private Sessions _sessions;
    private static final Logger _log = LogManager.getLogger(CostGateway.class);


    /**
     * Read settings from web.xml and use them to initialise the service
     */
    public void init() {
        try {
            ServletContext context = getServletContext();
            _service = CostService.getInstance();

            // load and process init params from web.xml
            String ixURI = context.getInitParameter("InterfaceX_BackEnd");
            if (ixURI != null) _service.setInterfaceXBackend(ixURI);

            String rsLogURI = context.getInitParameter("ResourceServiceLogGateway");
            if (rsLogURI != null) _service.setResourceLogURI(rsLogURI);

            String engineLogURI = context.getInitParameter("EngineLogGateway");
            if (engineLogURI != null) _service.setEngineLogURI(engineLogURI);

            String rsOrgDataURI = context.getInitParameter("ResourceServiceOrgDataGateway");
            if (rsOrgDataURI != null) _service.setResourceOrgDataURI(rsOrgDataURI);

            String engineLogonName = context.getInitParameter("EngineLogonUserName");
            String engineLogonPassword = context.getInitParameter("EngineLogonPassword");
            String iaURI = (ixURI != null) ? ixURI.replace("/ix", "/ia") : null;
            _service.setEngineLogonName(engineLogonName);
            _service.setEngineLogonPassword(engineLogonPassword);
            _sessions = new Sessions(iaURI, engineLogonName, engineLogonPassword);

            _service.setXSDPath(context.getResource("/xsd/costmodel.xsd"));
        }
        catch (Exception e) {
            _log.error("Cost Service Initialisation Exception", e);
        }
    }


    public void destroy() {
        _service.shutdown();
        _sessions.shutdown();
    }


    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        doPost(req, res);                                // redirect all GETs to POSTs
    }


    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        String action = req.getParameter("action");
        String handle = req.getParameter("sessionHandle");
        YSpecificationID specID = constructSpecID(req);
        String taskName = req.getParameter("taskname");
        String result = "";

        if (action.equals("connect")) {
            String userid = req.getParameter("userid");
            String password = req.getParameter("password");
            result = _sessions.connect(userid, password);
        } else if (action.equals("checkConnection")) {
            result = String.valueOf(_sessions.checkConnection(handle));
        } else if (action.equals("disconnect")) {
            result = String.valueOf(_sessions.disconnect(handle));
        } else if (_sessions.checkConnection(handle)) {
            if (action.equals("importModel")) {
                result = _service.importModel(req.getParameter("model"));
            } else if (action.equals("importModels")) {
                result = _service.importModels(req.getParameter("models"));
            } else if (action.equals("exportModels")) {
                result = _service.exportModels(specID);
            } else if (action.equals("exportModel")) {
                result = _service.exportModel(specID, req.getParameter("id"));
            } else if (action.equals("removeModel")) {
                result = _service.removeModel(specID, req.getParameter("id"));
            } else if (action.equals("clearModels")) {
                result = _service.clearModels(specID);
            } else if (action.equals("getAnnotatedLog")) {
                boolean withData = req.getParameter("withData").equalsIgnoreCase("true");
                result = _service.getAnnotatedLog(specID, withData);
            } else if (action.equals("getResourceCosts")) {
                result = getResourceCost(specID, taskName, req.getParameter("resources"));
            } else if (action.equals("getFunctionList")) {
                result = getFunctionList(specID, taskName);
            } else if (action.equals("getFixedCosts")) {
                result = getFixedCosts(specID, taskName);
            } else if (action.equals("evaluate")) {
                String caseID = req.getParameter("id");
                String predicate = req.getParameter("predicate");
                result = String.valueOf(_service.evaluate(specID, caseID, predicate));
            } else if (action.equals("calculate")) {
                String caseID = req.getParameter("id");
                String predicate = req.getParameter("predicate");
                result = String.valueOf(_service.calculate(specID, caseID, predicate));
            } else if (action.equals("disconnect")) {
                result = String.valueOf(_sessions.disconnect(handle));
            } else throw new IOException("Unknown Cost Service action: " + action);
        } else throw new IOException("Unknown or inactive session handle");

        // generate the output
        res.setContentType("text/xml; charset=UTF-8");
        PrintWriter out = res.getWriter();
        out.write(result);
        out.flush();
        out.close();
    }


    /**
     * Gets an XML list of all cost functions for the specified specification - task
     * combination.
     *
     * @param specID the specification identifier
     * @param taskID the task identifier (may be null, in which case only the case level
     *               functions are required)
     * @return an XML list of the cost functions requested, or an appropriate failure
     *         message.
     */
    private String getFunctionList(YSpecificationID specID, String taskID) {
        // TODO
        return "";
    }


    /**
     * Gets an XML list of all the fixed costs for the specified specification - task
     * combination.
     *
     * @param specID the specification identifier
     * @param taskID the task identifier (may be null, in which case only the case level
     *               costs are required)
     * @return an XML list of the costs requested, or an appropriate failure message.
     */
    private String getFixedCosts(YSpecificationID specID, String taskID) {
        // TODO
        return "";
    }


    /**
     * Calculates the cost of an activity.
     *
     * @param specID    the specification identifier
     * @param taskName  the task identifier (may be null, in which case only the case level
     *                  costs are required)
     * @param resources an XML set of participant ids
     * @return an XML string containing cost data for each listed resource for the task
     */
    private String getResourceCost(YSpecificationID specID, String taskName,
                                   String resources) {
        String reply = "";
        CostModelCache cache = _service.getModelCache(specID);
        if (cache != null) {
            XNode node = new XNodeParser().parse(resources);
            if (node != null) {
                Set<String> resourceSet = new HashSet<String>();
                for (XNode resource : node.getChildren()) {
                    resourceSet.add(resource.getText());
                }
                reply = cache.getDriverMatrix().getCostMapAsXML(taskName, resourceSet);
            } else reply = failMessage("Error parsing list of participant ids");
        } else reply = failMessage("No cost models for specification");

        return reply;
    }


    private YSpecificationID constructSpecID(HttpServletRequest req) {
        String version = req.getParameter("specversion");
        String uri = req.getParameter("specuri");
        if ((uri != null) && (version != null)) {
            String identifier = req.getParameter("specidentifier");
            return new YSpecificationID(identifier, version, uri);
        } else return null;
    }


    private String failMessage(String msg) {
        return "<failure>" + msg + "</failure>";
    }

}
