package org.yawlfoundation.yawl.monitor;

import org.jdom.Element;
import org.yawlfoundation.yawl.engine.instance.CaseInstance;
import org.yawlfoundation.yawl.engine.instance.ParameterInstance;
import org.yawlfoundation.yawl.engine.instance.WorkItemInstance;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;
import org.yawlfoundation.yawl.engine.interfce.interfaceE.YLogGatewayClient;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceLogGatewayClient;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MonitorClient extends InterfaceBWebsideController {

    private String _sessionHandle = null;
    private String _resSessionHandle = null;
    private static MonitorClient _me = null;
    private YLogGatewayClient _logClient = null;
    private ResourceLogGatewayClient _resLogClient = null;
    private long _startupTime ;                         // time the engine started


    private MonitorClient() {
        super();
        _logClient = new YLogGatewayClient("http://localhost:8080/yawl/logGateway");
        _resLogClient = new ResourceLogGatewayClient("http://localhost:8080/resourceService/logGateway");
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
                String startTime = cases.getAttributeValue("startuptime");
                if (startTime != null) {
                    _startupTime = new Long(startTime);
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
                    Element eChild = (Element) child;

                    // ignore parent workitems
                    if (! "statusIsParent".equals(eChild.getChildText("status"))) {
                        WorkItemInstance instance = new WorkItemInstance((Element) child);
                        itemList.add(instance);
                    }    
                }
            }
        }
        return itemList;
    }

    public List<ParameterInstance> getParameters(String itemID) throws IOException {
        List<ParameterInstance> paramList = null;
        if (connected()) {
            String caseID = getCaseFromItemID(itemID);
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

    public long getStartupTime() { return _startupTime; }



    private boolean connected() {
        if (_sessionHandle == null) {
           _sessionHandle = connect();
        }
        return (_sessionHandle != null);
    }

    public String connect() {
        return login(DEFAULT_ENGINE_USERNAME, DEFAULT_ENGINE_PASSWORD);
    }

    private boolean resLogConnected() {
        if (_resSessionHandle == null) {
           _resSessionHandle = resLogConnect();
        }
        return (_resSessionHandle != null);
    }

    public String resLogConnect() {
        return resLogLogin(DEFAULT_ENGINE_USERNAME, DEFAULT_ENGINE_PASSWORD);
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

    public String resLogLogin(String userid, String password) {
        String handle = null;
        try {
            handle = _resLogClient.connect(userid, password);
        }
        catch (IOException ioe) {
            System.out.println("Problem connecting to resourceService.");
        }
        return handle;
    }

    private String getCaseFromItemID(String itemID) {
        String caseID = itemID.split(":")[0];
        if (caseID.contains(".")) caseID = caseID.split("\\.")[0];
        return caseID;
    }


    public String getCaseData(String itemID) throws IOException {
        if (connected()) {
            String caseID = getCaseFromItemID(itemID);
            return _interfaceBClient.getCaseData(caseID, _sessionHandle);
        }
        return "";
    }


    public String getCaseEvent(String caseID, String event) throws IOException {
        if (connected()) {
            return _logClient.getCaseEvent(caseID, event, _sessionHandle) ;
        }
        return "";
    }


    public String getServiceName(long key) throws IOException {
        if (connected()) {
            String result = _logClient.getServiceName(key, _sessionHandle) ;
            if (! result.startsWith("<fail")) {
                return StringUtil.unwrap(result);
            }
        }
        return "";
    }


    public String getCaseStartedBy(String caseID) throws IOException {
        if (resLogConnected()) {
            return _resLogClient.getCaseStartedBy(caseID, _resSessionHandle);
        }
        return "Unavailable";
    }


}