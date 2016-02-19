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
import org.yawlfoundation.yawl.elements.YAttributeMap;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.engine.interfce.ServletUtils;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.YHttpServlet;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.Sessions;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.worklet.WorkletService;
import org.yawlfoundation.yawl.worklet.exception.ExceptionService;
import org.yawlfoundation.yawl.worklet.exception.ExletValidationError;
import org.yawlfoundation.yawl.worklet.exception.ExletValidator;
import org.yawlfoundation.yawl.worklet.rdr.*;
import org.yawlfoundation.yawl.worklet.rdrutil.RdrException;
import org.yawlfoundation.yawl.worklet.selection.WorkletRunner;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Set;

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
        if (!WorkletConstants.wsInitialised) {
            try {
                _ws = WorkletService.getInstance();
                _rdr = _ws.getRdrInterface();
                ServletContext context = getServletContext();

                WorkletConstants.setHomeDir(context.getRealPath("/"));

                String persistStr = context.getInitParameter("EnablePersistence");
                WorkletConstants.setPersist(persistStr.equalsIgnoreCase("TRUE"));

                WorkletConstants.setResourceServiceURL(context.getInitParameter("ResourceServiceURL"));

                String engineURI = context.getInitParameter("InterfaceB_BackEnd");
                _ws.getEngineClient().initEngineURI(engineURI);

                String ixStr = context.getInitParameter("EnableExceptionHandling");
                boolean exceptionHandlingEnabled = ixStr != null && ixStr.equalsIgnoreCase("TRUE");
                _ws.setExceptionServiceEnabled(exceptionHandlingEnabled);

                _sessions = new Sessions();
                _sessions.setupInterfaceA(engineURI.replaceFirst("/ib", "/ia"),
                        context.getInitParameter("EngineLogonUserName"),
                        context.getInitParameter("EngineLogonPassword"));

                if (exceptionHandlingEnabled) {
                    ExceptionService.getInst().completeInitialisation();
                }
                else {
                    _ws.completeInitialisation();
                }
            } catch (Exception e) {
                _log.error("Gateway Initialisation Exception", e);
            } finally {
                WorkletConstants.setServicetInitialised();
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
                    result = replace(req);
                }
                else if (action.equalsIgnoreCase("refresh")) {  // no longer required
                    result = "<success/>";
                }
                else if (action.equalsIgnoreCase("addListener")) {
                    result = response(_ws.getServer().addListener(req.getParameter("uri")));
                }
                else if (action.equalsIgnoreCase("removeListener")) {
                    result = response(_ws.getServer().removeListener(req.getParameter("uri")));
                }
                else if (action.equalsIgnoreCase("evaluate")) {
                    result = response(evaluate(req));
                }
                else if (action.equalsIgnoreCase("process")) {
                    result = response(process(req));
                }
                else if (action.equalsIgnoreCase("execute")) {
                    result = response(execute(req));
                }
                else if (action.equalsIgnoreCase("addNode")) {
                    result = response(addNode(req));
                }
                else if (action.equalsIgnoreCase("getNode")) {
                    result = getNode(req);
                }
                else if (action.equalsIgnoreCase("getRdrTree")) {
                    result = getRdrTree(req);
                }
                else if (action.equalsIgnoreCase("getRdrSet")) {
                    result = getRdrSet(req);
                }
                else if (action.equalsIgnoreCase("getRdrSetIDs")) {
                    result = getRdrSetIDs();
                }
                else if (action.equalsIgnoreCase("addRdrSet")) {
                    result = addRdrSet(req);
                }
                else if (action.equalsIgnoreCase("removeRdrSet")) {
                    result = removeRdrSet(req);
                }
                else if (action.equalsIgnoreCase("addWorklet")) {
                    result = addWorklet(req);
                }
                else if (action.equalsIgnoreCase("getWorklet")) {
                    result = getWorklet(req);
                }
                else if (action.equalsIgnoreCase("removeWorklet")) {
                    result = removeWorklet(req);
                }
                else if (action.equalsIgnoreCase("getWorkletNames")) {
                    result = getWorkletNames();
                }
                else if (action.equalsIgnoreCase("getRunningWorklets")) {
                    result = getRunningWorklets();
                }
                else if (action.equalsIgnoreCase("getOrphanedWorklets")) {
                    result = getOrphanedWorklets();
                }
                else if (action.equalsIgnoreCase("getWorkletInfoList")) {
                    result = getWorkletInfoList();
                }
                else if (action.equalsIgnoreCase("updateRdrSetTaskIDs")) {
                    result = updateRdrSetTaskIDs(req);
                }
                else {
                    result = fail("Unrecognised action: " + action);
                }
            }
            else {
                result = fail("Invalid or disconnected session handle");
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
        String specIdentifier = req.getParameter("specidentifier");
        String specVersion = req.getParameter("specversion");
        String specURI = req.getParameter("specuri");
        if (specVersion == null) specVersion = "0.1";
        if (!(specIdentifier == null && specURI == null)) {
            return new YSpecificationID(specIdentifier, specVersion, specURI);
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

        RdrConclusion conc = node.getConclusion();
        if (conc.isNullConclusion()) {
            return fail("Node conclusion contains no elements.");
        }
        List<ExletValidationError> errList =  new ExletValidator().validate(
                node.getConclusion(), _ws.getLoader().getAllWorkletKeys());
        if (! errList.isEmpty()) {
            return fail("Node contains invalid conclusion: " + errList.get(0).getMessage());
        }

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
                        "work item record provided for addNode");
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

        WorkItemRecord wir;
        try {
            wir = getWIR(req);              // guaranteed not null
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

        WorkItemRecord wir;
        try {
            wir = getWIR(req);              // guaranteed not null
        }
        catch (IOException ioe) {
            return fail(ioe.getMessage());
        }

        loadWorklets(req.getParameter("workletset"));
        return ExceptionService.getInst().raiseException(wir, rType, conclusion);
    }


    private String replace(HttpServletRequest req) {
        String itemID = req.getParameter("itemID");
        String exType = req.getParameter("exType");
        RuleType rType = RuleType.valueOf(exType);

        // get the service instance and call replace
        try {
            if (rType == RuleType.ItemSelection) {
                return _ws.replaceWorklet(itemID);
            }
            else {
                String caseID = req.getParameter("caseID");
                ExceptionService ex = ExceptionService.getInst();
                return ex.replaceWorklet(rType, caseID, itemID);
            }
        }
        catch (IOException ioe) {
            return fail(ioe.getMessage());
        }
    }


    private String getNode(HttpServletRequest req) {
        long nodeID = StringUtil.strToLong(req.getParameter("nodeid"), -1);
        RdrNode node = _rdr.getNode(nodeID);
        return node != null ? node.toXML() : fail("No rule node found with id: " + nodeID);
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

        return tree != null ? tree.toXML() : fail("No rule tree found for parameters.");
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

        return set != null ? set.toXML() : fail("No rule set found for specification.");
    }


    private String getRdrSetIDs() {
        XNode root = new XNode("rdrsetids");
        for (String id : _rdr.getRdrSetIDs()) {
             root.addChild("identifier", id);
        }
        return root.toString();
    }


    private String addRdrSet(HttpServletRequest req) {
        String xml = req.getParameter("ruleset");
        YSpecificationID specID = makeSpecID(req);
        if (specID != null) {
            return addRdrSet(specID, xml);
        }

        String processName = req.getParameter("name");
        if (processName != null) {
            return addRdrSet(processName, xml);
        }

        return fail("Invalid parameters");
    }


    private String addRdrSet(YSpecificationID specID, String xml) {
        if (! (xml == null || specID == null)) {
            RdrSet exists = new RdrSetLoader().load(specID);
            if (exists == null) {
                return addRdrSet(new RdrSet(specID), xml);
            }
            return fail("Rule set already exists for specification: " + specID.toString());
        }
        return fail("Invalid parameters");
    }


    private String addRdrSet(String processName, String xml) {
        if (! (xml == null || processName == null)) {
            RdrSet exists = new RdrSetLoader().load(processName);
            if (exists == null) {
                return addRdrSet(new RdrSet(processName), xml);
            }
            return fail("Rule set already exists for specification: " + processName);
        }
        return fail("Invalid parameters");
    }


    private String addRdrSet(RdrSet rdrSet, String xml) {
        rdrSet.fromXML(xml);
        if (! rdrSet.hasRules()) {
            return fail("Malformed XML in rule set");
        }
        Persister.insert(rdrSet);
        return "<success/>";
    }


    private String removeRdrSet(HttpServletRequest req) {
        String idString = req.getParameter("identifier");
        if (idString != null)  {
            RdrSet removed;
            if (idString.contains(":")) {
                YSpecificationID specID = new YSpecificationID().fromFullString(idString);
                removed = _rdr.removeRdrSet(specID);
            }
            else {
                removed = _rdr.removeRdrSet(idString);    // process name
            }
            return removed != null ? "<success/>" :
                    fail("No rule set for identifier: " + idString);
        }
        return fail("Invalid parameters");
    }


    private String updateRdrSetTaskIDs(HttpServletRequest req) {
        YSpecificationID specID = makeSpecID(req);
        String updateXML = req.getParameter("updates");
        if (! (specID == null || updateXML == null)) {
            YAttributeMap map = new YAttributeMap();
            map.fromXMLElements(updateXML);
            _rdr.updateTaskIDs(specID, map);
            return "<success/>";
        }
        return fail("Invalid parameters");
    }


    private void loadWorklets(String worklets) {
        if (worklets != null) {
            for (String worklet : StringUtil.xmlToSet(worklets)) {
                String result = _ws.getEngineClient().uploadWorklet(worklet);
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


    private String addWorklet(HttpServletRequest req) {
        YSpecificationID specID = makeSpecID(req);
        String workletXML = req.getParameter("worklet");
        if (! (specID == null || workletXML == null)) {
            if (_ws.getLoader().add(specID, workletXML)) {
                return "<success/>";
            }
        }
        return fail("Invalid parameters");
    }


    private String getWorklet(HttpServletRequest req) {
        YSpecificationID specID = makeSpecID(req);
        if (specID != null) {
            WorkletSpecification worklet = _ws.getLoader().get(specID);
            if (worklet != null) {
                return worklet.getXML();
            }
            return fail("No worklet found with specification id: " + specID.toString());
        }
        return fail("Invalid parameters");
    }


    private String removeWorklet(HttpServletRequest req) {
        String key = req.getParameter("key");
        if (key != null) {
            return _ws.getLoader().remove(key) ? "<success/>" :
                    fail("No worklet found with specification id key: " + key);
        }
        return fail("Invalid parameters");
    }


    private String getWorkletNames() {
        XNode root = new XNode("workletnames");
        for (String name : _ws.getLoader().getAllWorkletKeys()) {
            root.addChild("name", name);
        }
        return root.toString();
    }


    private String getWorkletInfoList() {
        XNode root = new XNode("worklet_info_list");
        for (WorkletSpecification wSpec : _ws.getLoader().loadAllWorkletSpecifications()) {
            root.addChild(new WorkletInfo(wSpec).toXNode());
        }
        return root.toString();
    }


    private String getRunningWorklets() {
        Set<WorkletRunner> runners = _ws.getAllRunners();
        if (runners.isEmpty()) {
            return fail("No worklet instances currently running");
        }
        XNode root = new XNode("runningworklets");
        for (WorkletRunner runner : runners) {
            root.addChild(runner.toXNode());
        }
        return root.toString();
    }


    private String getOrphanedWorklets() {
        XNode root = new XNode("orphan_worklets");
        for (WorkletSpecification wSpec : _ws.getLoader().getOrphanedWorklets()) {
            root.addChild(new WorkletInfo(wSpec).toXNode());
        }
        return root.toString();
    }


    private WorkItemRecord getWIR(HttpServletRequest req) throws IOException {
        String wirStr = req.getParameter("wir");
        if (wirStr == null) {
            throw new IOException("Work item has null value");
        }

        WorkItemRecord wir = Marshaller.unmarshalWorkItem(wirStr);
        if (wir == null) {
            throw new IOException("Work item record is invalid");
        }

        WorkItemRecord refreshedWir = _ws.getEngineClient().getEngineStoredWorkItem(wir);
        if (refreshedWir == null) {
            throw new IOException("Work item '" + wir.getID() +
                    "' is unknown to the Engine");
        }

        return refreshedWir;
    }

}
