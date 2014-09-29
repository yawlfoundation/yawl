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

import org.jdom2.Element;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.engine.interfce.ServletUtils;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.YHttpServlet;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.Sessions;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.worklet.WorkletService;
import org.yawlfoundation.yawl.worklet.exception.ExceptionService;
import org.yawlfoundation.yawl.worklet.rdr.*;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * The WorkletGateway class acts as a gateway between the Worklet Selection
 * Service and the external RDREditor. It initialises the service with values from
 * 'web.xml' and provides functionality to trigger a running worklet replacement
 * due to an addition to the ruleset (by the editor). Future
 * implementations may extend this gateway for other purposes.
 *
 * @author Michael Adams
 *         v0.8, 13/08/2006
 */

public class WorkletGateway extends YHttpServlet {

    private WorkletService _ws;
    private Rdr _rdr;
    private Sessions _sessions;            // maintains sessions with external services

    public void init() {
        if (!Library.wsInitialised) {
            try {
                _ws = WorkletService.getInstance();
                _rdr = _ws.getRdrInterface();
                ServletContext context = getServletContext();

                Library.setHomeDir(context.getRealPath("/"));
                Library.setRepositoryDir(context.getInitParameter("Repository"));

                String persistStr = context.getInitParameter("EnablePersistence");
                Library.setPersist(persistStr.equalsIgnoreCase("TRUE"));

                Library.setResourceServiceURL(context.getInitParameter("ResourceServiceURL"));

                String engineURI = context.getInitParameter("InterfaceB_BackEnd");
                _ws.initEngineURI(engineURI);

                String ixStr = context.getInitParameter("EnableExceptionHandling");
                _ws.setExceptionServiceEnabled(
                        (ixStr != null) && ixStr.equalsIgnoreCase("TRUE"));

                _sessions = new Sessions();
                _sessions.setupInterfaceA(engineURI.replaceFirst("/ib", "/ia"),
                        context.getInitParameter("EngineLogonUserName"),
                        context.getInitParameter("EngineLogonPassword"));

                _ws.completeInitialisation();
                ExceptionService.getInst().completeInitialisation();
            } catch (Exception e) {
                _log.error("Gateway Initialisation Exception", e);
            } finally {
                Library.setServicetInitialised();
            }
        }
    }


    public void destroy() {
        _sessions.shutdown();
        _ws.shutdown();
        super.destroy();
    }


    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        String result = "";
        try {
            String action = req.getParameter("action");
            String handle = req.getParameter("sessionHandle");

            if (action == null) {
                result = "<html><head>" +
                        "<title>Worklet Dynamic Process Selection and Exception Service</title>" +
                        "</head><body>" +
                        "<H3>Welcome to the Worklet Dynamic Process Selection and " +
                        "Exception Service \"Gateway\"</H3>" +
                        "<p> The Worklet Gateway acts as a bridge between the Worklet " +
                        "Service and the external RDREditor (it isn't meant to be browsed " +
                        " to directly). It provides the " +
                        "functionality to trigger a running worklet replacement " +
                        "due to an addtion to the ruleset (by the editor).</p>" +
                        "</body></html>";
            } else if (action.equals("connect")) {
                String userid = req.getParameter("userid");
                String password = req.getParameter("password");
                result = _sessions.connect(userid, password);
            } else if (action.equals("checkConnection")) {
                result = String.valueOf(_sessions.checkConnection(handle));
            } else if (action.equals("disconnect")) {
                result = String.valueOf(_sessions.disconnect(handle));
            } else if (_sessions.checkConnection(handle)) {
                if (action.equalsIgnoreCase("replace")) {
                    _log.info("Received a request from the Rules Editor to replace " +
                            "a running worklet.");

                    String itemID = req.getParameter("itemID");
                    int exType = Integer.parseInt(req.getParameter("exType"));
                    RuleType rType = RuleType.values()[exType];

                    // get the service instance and call replace
                    if (rType == RuleType.ItemSelection) {
                        result = _ws.replaceWorklet(itemID);
                    } else {
                        String caseID = req.getParameter("caseID");
                        String trigger = req.getParameter("trigger");
                        ExceptionService ex = ExceptionService.getInst();
                        result = ex.replaceWorklet(rType, caseID, itemID, trigger);
                    }
                } else if (action.equalsIgnoreCase("refresh")) {
                    _ws.refreshRuleSet(makeSpecID(req));
                    result = "<success/>";
                } else if (action.equalsIgnoreCase("addListener")) {
                    result = response(_ws.getServer().addListener(req.getParameter("uri")));
                } else if (action.equalsIgnoreCase("removeListener")) {
                    result = response(_ws.getServer().removeListener(req.getParameter("uri")));
                } else if (action.equalsIgnoreCase("evaluate")) {
                    result = response(evaluate(req));
                } else if (action.equalsIgnoreCase("process")) {
                    result = response(process(req));
                } else if (action.equalsIgnoreCase("execute")) {
                    result = response(execute(req));
                } else if (action.equalsIgnoreCase("addNode")) {
                    result = response(addNode(req));
                } else if (action.equalsIgnoreCase("getNode")) {
                    result = getNode(req);
                } else if (action.equalsIgnoreCase("getRdrTree")) {
                    result = getRdrTree(req);
                } else if (action.equalsIgnoreCase("getRdrSet")) {
                    result = getRdrSet(req);
                }
            }
            else {
                result = "<failure>Invalid or disconnected session handle</failure>";
            }

            // generate the output
            OutputStreamWriter outputWriter = ServletUtils.prepareResponse(res);
            ServletUtils.finalizeResponse(outputWriter, result);
        }
        catch (Exception e) {
            _log.error("Exception in doPost()", e);
        }
    }


    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        doPost(req, res);
    }


    private YSpecificationID makeSpecID(HttpServletRequest req) {
        String specID = req.getParameter("specid");
        String specVersion = req.getParameter("specversion");
        String specURI = req.getParameter("specuri");
        if (specVersion == null) specVersion = "0.1";
        if (!(specID == null && specURI == null)) {
            return new YSpecificationID(specID, specVersion, specURI);
        } else return null;
    }


    private String addNode(HttpServletRequest req) {
        YSpecificationID specID = makeSpecID(req);
        String processName = req.getParameter("name");
        String taskID = req.getParameter("taskid");
        String rTypeStr = req.getParameter("rtype");
        String nodeXML = req.getParameter("node");
        if (rTypeStr == null) return fail("Rule Type has null value");
        if (nodeXML == null) return fail("Node is null");

        RuleType rType = RuleType.valueOf(rTypeStr);
        RdrNode node = new RdrNode(nodeXML);
        try {
            if (specID != null) {
                node = _rdr.addNode(specID, taskID, rType, node);
            }
            else if (processName != null) {
                node = _rdr.addNode(processName, taskID, rType, node);
            }
            else {
                String wirStr = req.getParameter("wir");
                if (wirStr == null) return fail("No specification, process name or " +
                        "work item record provided for evaluation");
                WorkItemRecord wir = Marshaller.unmarshalWorkItem(wirStr);
                node = _rdr.addNode(wir, rType, node);
            }
            return node.toXML();
        }
        catch(RdrException rdre) {
            return fail(rdre.getMessage());
        }
    }

    private String evaluate(HttpServletRequest req) {
        String rTypeStr = req.getParameter("rtype");
        if (rTypeStr == null) return fail("Rule Type has null value");
        String dataStr = req.getParameter("data");
        Element data = JDOMUtil.stringToElement(dataStr);
        if (data == null) return fail("No or invalid data provided for evaluation");

        RuleType rType = RuleType.valueOf(rTypeStr);
        String taskID = req.getParameter("taskid");
        YSpecificationID specID = makeSpecID(req);
        String processName = req.getParameter("name");
        RdrPair pair;
        if (specID != null) {
            pair = _rdr.evaluate(specID, taskID, data, rType);
        } else if (processName != null) {
            pair = _rdr.evaluate(processName, taskID, data, rType);
        } else {
            String wirStr = req.getParameter("wir");
            if (wirStr == null) return fail(
                    "No specification, process name or work item record provided for evaluation");
            WorkItemRecord wir = Marshaller.unmarshalWorkItem(wirStr);
            pair = _rdr.evaluate(wir, data, rType);
        }
        if (pair == null) {
            return fail("No rules found for parameters");
        } else if (pair.hasNullConclusion()) {
            return fail("No rule was satisfied for data parameters");
        }
        return pair.getConclusion().toXML();
    }

    private String process(HttpServletRequest req) {
        if (!_ws.isExceptionServiceEnabled()) {
            return fail("Exception handling is currently disabled. Please enable in it " +
                    "Worklet Service's web.xml");
        }

        String dataStr = req.getParameter("data");
        if (dataStr == null) return fail("Data value is null");

        String rTypeStr = req.getParameter("rtype");
        if (rTypeStr == null) return fail("Rule Type has null value");
        RuleType rType = RuleType.valueOf(rTypeStr);

        String wirStr = req.getParameter("wir");
        if (wirStr == null) return fail("Work item has null value");
        WorkItemRecord wir = Marshaller.unmarshalWorkItem(wirStr);

        try {
            WorkItemRecord refreshedWir = _ws.getEngineStoredWorkItem(wir);
            if (refreshedWir == null) return fail("Work item '" + wir.getID() +
                    "' is unknown to the Engine");
            wir = refreshedWir;
        }
        catch (IOException ioe) {
            return fail(ioe.getMessage());
        }

        Element data = JDOMUtil.stringToElement(dataStr);
        if (data != null) wir.setUpdatedData(data);

        if (rType == RuleType.ItemAbort) {
            return ExceptionService.getInst().handleWorkItemAbortException(wir, dataStr);
        } else if (rType == RuleType.ItemConstraintViolation) {
            return ExceptionService.getInst().handleConstraintViolationException(wir, dataStr);
        } else return fail("Invalid rule type '" + rType.toLongString() +
                "'. This method can only be used for workitem constraint violation" +
                " and workitem abort exception types");
    }


    private String execute(HttpServletRequest req) {
        if (!_ws.isExceptionServiceEnabled()) {
            return fail("Exception handling is currently disabled. Please enable in it " +
                    "Worklet Service's web.xml");
        }

        String concStr = req.getParameter("conclusion");
        if (concStr == null) return fail("RdrConclusion value is null");
        Element eConclusion = JDOMUtil.stringToElement(concStr);
        if (eConclusion == null) return fail("Invalid RdrConclusion value");
        RdrConclusion conclusion = new RdrConclusion(eConclusion);

        String rTypeStr = req.getParameter("rtype");
        if (rTypeStr == null) return fail("Rule Type has null value");
        if (rTypeStr.contains("Case")) {
            return fail("Case-level exception types cannot be executed using this method");
        }
        RuleType rType = RuleType.valueOf(rTypeStr);

        String wirStr = req.getParameter("wir");
        if (wirStr == null) return fail("Work item has null value");
        WorkItemRecord wir = Marshaller.unmarshalWorkItem(wirStr);

        try {
            WorkItemRecord refreshedWir = _ws.getEngineStoredWorkItem(wir);
            if (refreshedWir == null) return fail("Work item '" + wir.getID() +
                    "' is unknown to the Engine");
            wir = refreshedWir;
        }
        catch (IOException ioe) {
            return fail(ioe.getMessage());
        }

        loadWorklets(req.getParameter("workletset"));
        return ExceptionService.getInst().raiseException(wir, rType, conclusion);
    }


    private String getNode(HttpServletRequest req) {
        YSpecificationID specID = makeSpecID(req);
        String processName = req.getParameter("name");
        String taskID = req.getParameter("taskid");
        String rTypeStr = req.getParameter("rtype");
        int nodeID = StringUtil.strToInt(req.getParameter("nodeid"), -1);
        if (rTypeStr == null) return fail("Rule Type has null value");
        if (nodeID < 0) return fail("Invalid node id");

        RuleType rType = RuleType.valueOf(rTypeStr);
        RdrNode node;
        if (specID != null) {
            node = _rdr.getNode(specID, taskID, rType, nodeID);
        } else if (processName != null) {
            node = _rdr.getNode(processName, taskID, rType, nodeID);
        } else {
            String wirStr = req.getParameter("wir");
            if (wirStr == null) return fail(
                    "No specification, process name or work item record provided for evaluation");
            WorkItemRecord wir = Marshaller.unmarshalWorkItem(wirStr);
            node = _rdr.getNode(wir, rType, nodeID);
        }

        return node.toXML();
    }


    private String getRdrTree(HttpServletRequest req) {
        YSpecificationID specID = makeSpecID(req);
        String processName = req.getParameter("name");
        String taskID = req.getParameter("taskid");
        String rTypeStr = req.getParameter("rtype");
        if (rTypeStr == null) return fail("Rule Type has null value");

        RuleType rType = RuleType.valueOf(rTypeStr);
        RdrTree tree;
        if (specID != null) {
            tree = _rdr.getRdrTree(specID, taskID, rType);
        } else if (processName != null) {
            tree = _rdr.getRdrTree(processName, taskID, rType);
        } else {
            String wirStr = req.getParameter("wir");
            if (wirStr == null) return fail(
                    "No specification, process name or work item record provided for evaluation");
            WorkItemRecord wir = Marshaller.unmarshalWorkItem(wirStr);
            tree = _rdr.getRdrTree(wir, rType);
        }

        return tree.toXML();
    }


    private String getRdrSet(HttpServletRequest req) {
        YSpecificationID specID = makeSpecID(req);
        String processName = req.getParameter("name");
        RdrSet set;
        if (specID != null) {
            set = _rdr.getRdrSet(specID);
        } else if (processName != null) {
            set = _rdr.getRdrSet(processName);
        } else return fail("No specification or process name provided for set");

        return set.toXML();
    }


    private void loadWorklets(String worklets) {
        if (worklets != null) {
            for (String worklet : StringUtil.xmlToSet(worklets)) {
                String result = _ws.uploadWorklet(worklet);
                if (result != null) {
                    if (result.startsWith("<fail")) {
                        _log.warn(StringUtil.unwrap(result));
                    }
                    else {
                        _log.info("Worklet successfully uploaded");
                    }
                }
            }
        }
    }

}
