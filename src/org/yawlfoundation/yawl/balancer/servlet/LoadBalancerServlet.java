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

package org.yawlfoundation.yawl.balancer.servlet;

import org.yawlfoundation.yawl.balancer.Actions;
import org.yawlfoundation.yawl.balancer.ComplexityMetric;
import org.yawlfoundation.yawl.balancer.ResultProcessor;
import org.yawlfoundation.yawl.balancer.config.Config;
import org.yawlfoundation.yawl.balancer.config.ConfigChangeListener;
import org.yawlfoundation.yawl.balancer.instance.EngineInstance;
import org.yawlfoundation.yawl.balancer.instance.EngineSet;
import org.yawlfoundation.yawl.balancer.monitor.Monitor;
import org.yawlfoundation.yawl.balancer.polling.PollingService;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.engine.interfce.ServletUtils;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.engine.interfce.YHttpServlet;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.unmarshal.YMarshal;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author Michael Adams
 * @date 22/6/17
 */
public class LoadBalancerServlet extends YHttpServlet implements ConfigChangeListener {

    private final ForwardClient _forwardClient;

    private final ResultProcessor _resultProcessor;
    private final EngineSet _engineSet;
    private final Actions _actions;

    private Map<YSpecificationID, String> _specMap;
    private Map<YSpecificationID, Double> _specComplexityMap;
    private boolean _allInitialized = false;


    public LoadBalancerServlet() {
        super();
        _forwardClient = new ForwardClient("loadBalancer", "yBalance");
        _resultProcessor = new ResultProcessor();
        _engineSet = new EngineSet(new Monitor(this));
        _actions = new Actions();
    }


    @Override
    public void init() throws ServletException {
        Config.load(getServletContext());
        Config.addChangeListener(this);
        _engineSet.initialize();
   }


    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("HEAD".equals(req.getMethod())) return;
        doPost(req, resp);
    }

    
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        checkEnginesActive();
        String action = req.getParameter("action");
        if (action != null) {
            String result;
            if (action.startsWith("obs_")) {      // case completion/cancellation
                result = removeCase(req);
            }
            else if (_actions.isSessionAction(action)) {
                result = handleSessionAction(req);
            }
            else if (_actions.isItemAction(action)) {
                result = handleItemAction(req);
            }
            else if (_actions.isCaseAction(action)) {
                result = handleCaseAction(req);
            }
            else if (_actions.isLaunchAction(action)) {
                result = handleLaunchAction(req);
            }
            else if (_actions.isSpecAction(action)) {
                result = handleSpecAction(req);
            }
            else {
                result = handleAction(req);
            }
            processResponse(resp, result);
        }
        else {
            _log.error("Null action in call");
        }
    }


    @Override
    public void destroy() {
        _engineSet.closeAll();
        PollingService.shutdown();
        Config.stopConfigMonitoring();
        super.destroy();
    }


    public void configChanged(Map<String, String> changedValues) {
        if (changedValues.containsKey("poll_interval")) {
            PollingService.reschedule();
        }
    }


    private void checkEnginesActive() {
        if (!_allInitialized) {
            _engineSet.waitUntilAllInitialized();    // also filters unavailable
            _allInitialized = true;
        }
    }

    
    private void processResponse(HttpServletResponse resp, String result)
            throws IOException {
        ServletUtils.sendResponse(resp, response(result));
    }


    private String handleAction(HttpServletRequest req) throws IOException {
        return forward(req);
    }


    private String handleSessionAction(HttpServletRequest req)
            throws IOException {
        EngineInstance authenticator = _engineSet.getAuthenticator();
        if (! authenticator.isRestored() && "connect".equals(req.getParameter("action"))) {
            restoreSpecifications();
            restoreCases();
            PollingService.schedule();
        }
        return _forwardClient.executePost(authenticator.getURL(
                req.getPathInfo()), buildParamMap(req));
    }


    private String handleLaunchAction(HttpServletRequest req) throws IOException {
        EngineInstance instance = _engineSet.getIdlestEngine();
        Map<String, String> params = buildParamMap(req);
        if (checkOrAddSpec(instance, params)) {
            params.put("caseid", _engineSet.getNextCaseNbr());
            String result = _forwardClient.executePost(instance, params, req.getPathInfo());
            if (successful(result)) {
                YSpecificationID specID = getSpecID(params);
                instance.addCase(result, specID);
                instance.addComplexityMetric(_specComplexityMap.get(specID));
                _log.info("Case {} launched on engine {}",
                        result, instance.getName());
            }
            return result;
        }
        return "Failed to launch case: unknown specification";
    }

    
    private String handleItemAction(HttpServletRequest req) throws IOException {
        String caseID = getRootCaseID(req.getParameter("workItemID"));
        return handleCaseSpecificAction(req, caseID);
    }


    private String handleCaseAction(HttpServletRequest req) throws IOException {
        return handleCaseSpecificAction(req, req.getParameter("caseid"));
    }


    private String handleCaseSpecificAction(HttpServletRequest req, String caseID)
            throws IOException {
        if (caseID == null) {
            caseID = req.getParameter("caseID");
        }
        if (caseID != null) {
            EngineInstance instance = _engineSet.getEngineForCase(caseID);
            if (instance != null) {
                return _forwardClient.executePost(instance, buildParamMap(req), "/ib");
            }
        }
        return "<failure>Unknown Case Identifier</failure>";
    }


    private String handleSpecAction(HttpServletRequest req) throws IOException {
        String action = req.getParameter("action");
        Map<String, String> params = buildParamMap(req);
        if (action.equals("upload")) {
            String result = _forwardClient.executePost(_engineSet.getRandomInstance(),
                    params, "/ia");
            if (successful(result)) {
                String specXML = req.getParameter("specXML");
                YSpecificationID specID = getSpecIDFromXML(specXML);
                _specMap.put(specID, specXML);
                _specComplexityMap.put(specID, new ComplexityMetric().calc(specXML));
            }
            return result;
        }
        else {           // unload
            YSpecificationID specID = getSpecID(params);
            for (EngineInstance instance : _engineSet.getAll()) {
                if (isLoaded(instance, specID)) {
                    _forwardClient.executePost(instance, params, "/ia");
                }
            }
            _specMap.remove(specID);
            _specComplexityMap.remove(specID);
            return "<success/>";
        }
    }


    private void forward(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
        RequestDispatcher rd = req.getRequestDispatcher("/yawl/ib");
        rd.forward(req, resp);
    }


    private String forward(HttpServletRequest req) throws IOException {
        Set<String> resultSet = new HashSet<String>();
        Map<String, String> params = buildParamMap(req);
        for (EngineInstance instance : _engineSet.getAll()) {
            resultSet.add(_forwardClient.forward(instance, params,
                    req.getPathInfo(), req.getMethod()));
        }
        return _resultProcessor.process(resultSet);
    }


    public ForwardClient getForwardClient() { return _forwardClient; }

    public EngineSet getEngineSet() { return _engineSet; }


    private String getRootCaseID(String itemID) {
        if (itemID != null) {
            String caseID = itemID.substring(0, itemID.indexOf(':'));
            int firstDot = caseID.indexOf('.');
            return (firstDot > -1) ? caseID.substring(0, firstDot) : caseID;
        }
        return null;
    }


    private Map<String, String> buildParamMap(HttpServletRequest req) {
        Map<String, String> params = new HashMap<String, String>();
        Enumeration e = req.getParameterNames();
        while(e.hasMoreElements()) {
            String param = (String) e.nextElement();
            String value = req.getParameter(param);
            params.put(param, value);
        }
        return params;
    }


    private void restoreCases() throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "getAllRunningCases");
        for (EngineInstance instance : _engineSet.getAll()) {
            String result = _forwardClient.executePost(instance, params, "/ib");
            if (successful(result)) {
                XNode node = _resultProcessor.parse(result);
                if (node != null) {
                    Map<String, YSpecificationID> caseMap =
                            new HashMap<String, YSpecificationID>();
                    for (XNode specNode : node.getChildren()) {
                        YSpecificationID specID = getSpecIDFromXNode(specNode);
                        for (XNode caseNode : specNode.getChildren()) {
                            caseMap.put(caseNode.getText(), specID);
                        }
                    }
                    instance.addCases(caseMap);
                }
            }
            instance.setRestored(true);
        }
    }


    private void restoreSpecifications() throws IOException {
        _specMap = new HashMap<YSpecificationID, String>();
        _specComplexityMap = new HashMap<YSpecificationID, Double>();
        ComplexityMetric complexityMetric = new ComplexityMetric();
        for (EngineInstance instance : _engineSet.getAll()) {
            for (SpecificationData specData : getSpecList(instance)) {
                String specXML = getSpecification(instance, specData);
                if (specXML != null) {
                    _specMap.put(specData.getID(), specXML);
                    _specComplexityMap.put(specData.getID(),
                            complexityMetric.calc(specXML));
                }
            }
        }
    }



    private String removeCase(HttpServletRequest req) {
        String caseID = req.getParameter("caseid");
        for (EngineInstance instance : _engineSet.getAll()) {
            if (instance.hasCase(caseID)) {
                YSpecificationID specID = instance.removeCase(caseID);
                if (specID != null) {
                    instance.removeComplexityMetric(_specComplexityMap.get(specID));
                }
                return "OK";
            }
        }
        return "NOK";
    }


    // xml has already been validated via upload call
    private YSpecificationID getSpecIDFromXML(String specXML) throws IOException {
        try {
            List<YSpecification> specList = YMarshal.unmarshalSpecifications(specXML, false);
            if (specList.isEmpty()) {
                throw new IOException("Failed to parse specification xml");
            }
            return specList.get(0).getSpecificationID();
        }
        catch (YSyntaxException yse) {
             throw new IOException(yse.getMessage());
        }
    }


    private YSpecificationID getSpecIDFromXNode(XNode node) {
        return new YSpecificationID(node.getAttributeValue("identifier"),
                node.getAttributeValue("version"),
                node.getAttributeValue("uri"));
    }

    
    private List<SpecificationData> getSpecList(EngineInstance instance) throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "getSpecificationPrototypesList");
        String result = _forwardClient.executePost(instance, params, "/ib");
        return successful(result) && ! StringUtil.unwrap(result).isEmpty() ?
                Marshaller.unmarshalSpecificationSummary(response(result)) :
                Collections.<SpecificationData>emptyList();
    }

    
    private String getSpecification(EngineInstance instance, SpecificationData specData)
            throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "getSpecification");
        params.put("specidentifier", specData.getSpecIdentifier());
        params.put("specversion", specData.getSpecVersion());
        params.put("specuri", specData.getSpecURI());

        String result = _forwardClient.executePost(instance, params, "/ib");
        return successful(result) ? result : null;
    }


    private boolean checkOrAddSpec(EngineInstance instance, Map<String, String> params)
            throws IOException {
        YSpecificationID specID = getSpecID(params);
        if (! isLoaded(instance, specID)) {
            String specXML = _specMap.get(specID);
            if (specXML != null) {
                return uploadSpec(instance, specXML);
            }
        }
        return true;
    }


    private YSpecificationID getSpecID(Map<String, String> params) {
        return new YSpecificationID(params.get("specidentifier"),
                        params.get("specversion"), params.get("specuri"));
    }


    private boolean uploadSpec(EngineInstance instance, String specXML) throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", "upload");
        params.put("specXML", specXML);
        return successful(_forwardClient.executePost(instance, params, "/ia"));
    }



    private boolean isLoaded(EngineInstance instance, YSpecificationID specID)
            throws IOException {
        for (SpecificationData specData : getSpecList(instance)) {
            if (specData.getID().equals(specID)) {
                return true;
            }
        }
        return false;
    }

    
    private boolean successful(String xml) { return _forwardClient.successful(xml); }

}
