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

package org.yawlfoundation.yawl.monitor;

import org.jdom.Element;
import org.yawlfoundation.yawl.engine.instance.CaseInstance;
import org.yawlfoundation.yawl.engine.instance.ParameterInstance;
import org.yawlfoundation.yawl.engine.instance.WorkItemInstance;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceB_EnvironmentBasedClient;
import org.yawlfoundation.yawl.engine.interfce.interfaceE.YLogGatewayClient;
import org.yawlfoundation.yawl.logging.table.YLogEvent;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.ResourceEvent;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceGatewayClient;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceLogGatewayClient;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MonitorClient {

    private String _engineHandle;
    private String _resourceHandle;
    private static MonitorClient _me = null;
    private YLogGatewayClient _logClient;
    private ResourceLogGatewayClient _resLogClient;
    private ResourceGatewayClient _resClient;

    private InterfaceB_EnvironmentBasedClient _interfaceBClient;
    private long _startupTime ;                         // time the engine started

    private static final String _engineUser = "monitorService";
    private static final String _enginePassword = "yMonitor";

    public static MonitorClient getInstance() {
        if (_me == null) _me = new MonitorClient();
        return _me;
    }


    protected void initInterfaces(Map<String, String> urlMap) {
        String url = urlMap.get("engineGateway");
        if (url == null) url = "http://localhost:8080/yawl/ib";
        _interfaceBClient = new InterfaceB_EnvironmentBasedClient(url);

        url = urlMap.get("engineLogGateway");
        if (url == null) url = "http://localhost:8080/yawl/logGateway";
        _logClient = new YLogGatewayClient(url);

        url = urlMap.get("resourceGateway");
        if (url == null) url = "http://localhost:8080/resourceService/gateway";
        _resClient = new ResourceGatewayClient(url);

        url = urlMap.get("resourceLogGateway");
        if (url == null) url = "http://localhost:8080/resourceService/logGateway";
        _resLogClient = new ResourceLogGatewayClient(url);
    }


    //== ENGINE CALLS =============================================================//

    public List<CaseInstance> getCases() throws IOException {
        List<CaseInstance> caseList = null;
        String xml = _interfaceBClient.getCaseInstanceSummary(getEngineHandle());
        Element cases = JDOMUtil.stringToElement(xml);
        if (cases != null) {
            List children = cases.getChildren();
            if (! children.isEmpty()) {
                caseList = new ArrayList<CaseInstance>();
                for (Object child : children) {
                    caseList.add(new CaseInstance((Element) child));
                }
            }
            String startTime = cases.getAttributeValue("startuptime");
            if (startTime != null) {
                _startupTime = new Long(startTime);
            }
        }
        return caseList;
    }

    public List<WorkItemInstance> getWorkItems(String caseID) throws IOException {
        List<WorkItemInstance> itemList = null;
        String xml = _interfaceBClient.getWorkItemInstanceSummary(caseID, getEngineHandle());
        Element items = JDOMUtil.stringToElement(xml);
        if (items != null) {
            itemList = new ArrayList<WorkItemInstance>();
            List children = items.getChildren();
            for (Object child : children) {
                Element eChild = (Element) child;

                // ignore parent workitems
                if (! "statusIsParent".equals(eChild.getChildText("status"))) {
                    WorkItemInstance instance = new WorkItemInstance((Element) child);
                    itemList.add(instance);
                }
            }
        }
        return itemList;
    }

    public List<ParameterInstance> getParameters(String itemID) throws IOException {
        List<ParameterInstance> paramList = new ArrayList<ParameterInstance>();
        String caseID = getCaseFromItemID(itemID);
        itemID = checkForItemStarted(itemID, caseID);
        String xml = _interfaceBClient.getParameterInstanceSummary(caseID, itemID, getEngineHandle());
        Element params = JDOMUtil.stringToElement(xml);
        if (params != null) {
            List children = params.getChildren();
            for (Object child : children) {
                ParameterInstance instance = new ParameterInstance((Element) child);
                paramList.add(instance);
            }
        }
        return paramList;
    }


    public long getStartupTime() { return _startupTime; }


    //== PRIVATE ===================================//

    private String getCaseFromItemID(String itemID) {
        String caseID = itemID;
        if (caseID.contains(":")) caseID = itemID.substring(0, itemID.indexOf(':'));
        if (caseID.contains(".")) caseID = caseID.substring(0, caseID.indexOf('.'));
        return caseID;
    }


    /* tests that a sessionhandle is valid */
    private boolean connected(String handle) {
        return successful(handle);
    }


    /* returns the engine session handle, creating a new one if its invalid */
    private String getEngineHandle() {
        if (! connected(_engineHandle)) {
            try {
                _engineHandle = _interfaceBClient.connect(_engineUser, _enginePassword);
            }
            catch (IOException ioe) {
                _engineHandle = "<failure>Problem connecting to engine.</failure>";
            }
        }
        return _engineHandle;
    }


    /* returns the resource service session handle, creating a new one if its invalid */
    private String getResourceHandle() {
        if (! connected(_resourceHandle)) {
            try {
                _resourceHandle = _resLogClient.connect(_engineUser, _enginePassword);
            }
            catch (IOException ioe) {
                _resourceHandle = "<failure>Problem connecting to resource service.</failure>";
            }
        }
        return _resourceHandle;
    }


    private String validateUserCredentials(String userid, String password) {
        try {
            return _resClient.validateUserCredentials(userid, password, true, getResourceHandle());
        }
        catch (IOException ioe) {
            return "<failure>Unable to validate user - service unreachable.</failure>";
        }
    }


    private String checkForItemStarted(String itemID, String caseID) throws IOException {
        for (WorkItemInstance item : getWorkItems(caseID)) {
            if (itemID.endsWith(":" + item.getTaskID())) {
                return item.getID();
            }
        }
        return itemID;     // fallback
    }


    //== LOGIN & SESSION ===================================//

    /* called from msLogin to log a user with admin credentials into the service */
    public String login(String userid, String password) {
        try {
            String validationMsg = validateUserCredentials(userid, password);
            _engineHandle = successful(validationMsg) ?
                            _interfaceBClient.connect(_engineUser, _enginePassword) :
                            validationMsg;
        }
        catch (IOException ioe) {
            _engineHandle = "<failure>Problem connecting to engine.</failure>";
        }
        return _engineHandle;
    }


    public boolean successful(String msg) {
        return _interfaceBClient.successful(msg);
    }


    public String getCaseData(String itemID) throws IOException {
        String caseID = getCaseFromItemID(itemID);
        return _interfaceBClient.getCaseData(caseID, getEngineHandle());
    }


    public String getCaseEvent(String caseID, String event) throws IOException {
        return _logClient.getCaseEvent(caseID, event, getEngineHandle()) ;
    }


    public String getServiceName(long key) throws IOException {
        String result = _logClient.getServiceName(key, getEngineHandle()) ;
        if (successful(result)) {
            return StringUtil.unwrap(result);
        }
        else throw new IOException(result);
    }


    public List<YLogEvent> getEventsForWorkItem(String itemID) throws IOException {
        List<YLogEvent> eventList = new ArrayList<YLogEvent>();
        itemID = checkForItemStarted(itemID, getCaseFromItemID(itemID));
        String xml = _logClient.getEventsForTaskInstance(itemID, getEngineHandle()) ;
        if (successful(xml)) {
            Element events = JDOMUtil.stringToElement(xml);
            if (events != null) {
                List children = events.getChildren();
                for (Object child : children) {
                    YLogEvent event = new YLogEvent((Element) child);
                    eventList.add(event);
                }
            }
        }
        return eventList;
    }


    public List<ResourceEvent> getResourceEventsForWorkItem(String itemID) throws IOException {
        List<ResourceEvent> eventList = new ArrayList<ResourceEvent>();
        itemID = checkForItemStarted(itemID, getCaseFromItemID(itemID));
        String xml = _resLogClient.getWorkItemEvents(itemID, true, getResourceHandle());
        if (successful(xml)) {
            Element events = JDOMUtil.stringToElement(xml);
            if (events != null) {
                List children = events.getChildren();
                for (Object child : children) {
                    ResourceEvent event = new ResourceEvent((Element) child);
                    eventList.add(event);
                }
            }
        }
        return eventList;
    }


    public String getCaseStartedBy(String caseID) throws IOException {
        return _resLogClient.getCaseStartedBy(caseID, getResourceHandle());
    }

}