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

package org.yawlfoundation.yawl.balancer.monitor;

import org.yawlfoundation.yawl.balancer.config.Config;
import org.yawlfoundation.yawl.balancer.instance.EngineInstance;
import org.yawlfoundation.yawl.balancer.instance.EngineSet;
import org.yawlfoundation.yawl.balancer.servlet.ForwardClient;
import org.yawlfoundation.yawl.balancer.servlet.LoadBalancerServlet;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.io.IOException;
import java.util.*;

/**
 * @author Michael Adams
 * @date 18/10/18
 */
public class Monitor implements BusynessListener {

    // used to ignore startup readings
    private final Map<EngineInstance, Integer> _initCounter;

    private final LoadBalancerServlet _servlet;


    public Monitor(LoadBalancerServlet servlet) {
        _initCounter = new HashMap<EngineInstance, Integer>();
        _servlet = servlet;
    }


    @Override
    public void busynessEvent(EngineInstance instance, double load) {
        if (shouldScaleDown(instance,load)) {
            scaleDown(instance);
        }
    }


    private boolean hasInitialised(EngineInstance instance) {
        Integer count = _initCounter.get(instance);
        if (count == null) {
            count = 0;
        }
        _initCounter.put(instance, ++count);
        return count > Config.getMonitorInitCount();
    }


    private void scaleDown(EngineInstance instance) {
        try {
            Set<String> runningCases = getRunningCases(instance);
            // servlet.suspendCaseRequests(runningCases);
            String casesXML = exportCases(instance);
            if (successful(casesXML)) {
                deactivateEngine(instance);
                redistributeCases(casesXML);
                cancelCases(instance, runningCases);
                // servlet.unsuspendCaseRequests(runningCases);
            }
        }
        catch (IOException ioe) {
            // message
        }
    }


    private Set<String> getRunningCases(EngineInstance instance) throws IOException {
        Map<String, String> params = getParams("getAllRunningCases");
        String result = post(instance, params);
        if (successful(result)) {
            XNode cases = new XNodeParser().parse(result);
            if (cases != null) {
                Set<String> caseIDs = new HashSet<String>();
                for (XNode caseNode : cases.getChildren()) {
                    caseIDs.add(caseNode.getText());
                }
                return caseIDs;
            }
        }
        return Collections.emptySet();
    }


    private String exportCases(EngineInstance instance) throws IOException {
        Map<String, String> params = getParams("exportAllCaseStates");
        return post(instance, params);
    }


    private void cancelCases(EngineInstance instance, Set<String> caseIDs) throws IOException {
        for (String caseID : caseIDs) {
            cancelCase(instance, caseID);
        }
    }


    private void cancelCase(EngineInstance instance, String caseID) throws IOException {
        Map<String, String> params = getParams("cancelCase");
        params.put("caseID", caseID);
        post(instance, params);
    }


    private String redistributeCases(EngineInstance instance, String caseXML) throws IOException {
        Map<String, String> params = getParams("importCases");
        params.put("xml", caseXML);
        return post(instance, params);
    }


    private Map<String,String> getParams(String action) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("action", action);
        return params;
    }


    private String post(EngineInstance instance, Map<String, String> params)
            throws IOException {
        return getForwardClient().executePost(instance, params, "/ib");
    }


    private boolean shouldScaleDown(EngineInstance instance, double load) {
        return hasInitialised(instance) && load < Config.getMinLoadThreshold()
                && getActiveEngineCount() > Config.getMinEngineCount();
    }


    private int getActiveEngineCount() { return getEngineSet().getActiveCount(); }


    private boolean deactivateEngine(EngineInstance instance) {
        return getEngineSet().deactivate(instance);
    }


    private String redistributeCases(String caseXML) throws IOException {
        return redistributeCases(getEngineSet().getIdlestEngine(), caseXML);
    }


    private EngineSet getEngineSet() { return _servlet.getEngineSet(); }

    private ForwardClient getForwardClient() { return _servlet.getForwardClient(); }

    
    private boolean successful(String xml) { return getForwardClient().successful(xml); }
    
}
