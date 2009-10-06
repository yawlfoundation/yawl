package org.yawlfoundation.yawl.monitor;

import org.jdom.Element;
import org.yawlfoundation.yawl.engine.instance.CaseInstance;
import org.yawlfoundation.yawl.engine.instance.ParameterInstance;
import org.yawlfoundation.yawl.engine.instance.WorkItemInstance;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MonitorClient extends InterfaceBWebsideController {

    private String _sessionHandle = null;
    private static MonitorClient _me = null;

    private MonitorClient() {
        super();
    }

    public static MonitorClient getInstance() {
        if (_me == null) _me = new MonitorClient();
        return _me;
    }

    // implement abstract methods of super class
    public void handleEnabledWorkItemEvent(WorkItemRecord enabledWorkItem) {}
    public void handleCancelledWorkItemEvent(WorkItemRecord workItemRecord) {}

    
    public List<CaseInstance> getCases() throws IOException {
        List<CaseInstance> caseList = null;
        if (connected()) {
            String xml = _interfaceBClient.getCaseInstanceSummary(_sessionHandle);
            Element cases = JDOMUtil.stringToElement(xml);
            if (cases != null) {
                caseList = new ArrayList<CaseInstance>();
                List children = cases.getChildren();
                for (Object child : children) {
                    CaseInstance instance = new CaseInstance((Element) child);
                    caseList.add(instance);
                }
            }
        }
        return caseList;
    }

    public List<WorkItemInstance> getWorkItems(String caseID) throws IOException {
        List<WorkItemInstance> itemList = null;
        if (connected()) {
            String xml = _interfaceBClient.getWorkItemInstanceSummary(caseID, _sessionHandle);
            Element items = JDOMUtil.stringToElement(xml);
            if (items != null) {
                itemList = new ArrayList<WorkItemInstance>();
                List children = items.getChildren();
                for (Object child : children) {
                    WorkItemInstance instance = new WorkItemInstance((Element) child);
                    itemList.add(instance);
                }
            }
        }
        return itemList;
    }

    public List<ParameterInstance> getParameters(String caseID, String itemID) throws IOException {
        List<ParameterInstance> paramList = null;
        if (connected()) {
            String xml = _interfaceBClient.getParameterInstanceSummary(caseID, itemID, _sessionHandle);
            Element params = JDOMUtil.stringToElement(xml);
            if (params != null) {
                paramList = new ArrayList<ParameterInstance>();
                List children = params.getChildren();
                for (Object child : children) {
                    ParameterInstance instance = new ParameterInstance((Element) child);
                    paramList.add(instance);
                }
            }
        }
        return paramList;
    }



    private boolean connected() {
        if (_sessionHandle == null) {
           _sessionHandle = connect();
        }
        return (_sessionHandle != null);
    }

    public String connect() {
        return login(DEFAULT_ENGINE_USERNAME, DEFAULT_ENGINE_PASSWORD);
    }

    public String login(String userid, String password) {
        String handle = null;
        try {
            handle = connect(userid, password);
        }
        catch (IOException ioe) {
            System.out.println("Problem connecting to engine.");
        }
        return handle;
    }


}