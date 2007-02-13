package au.edu.qut.yawl.engine.interfce;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Category;
import org.jdom.Document;

import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.util.JDOMConversionTools;

public class ServiceHandler extends Thread {
    
	protected static Category logger = Category.getInstance(ServiceHandler.class);
	
	private YWorkItem _workItem;
    private URI _yawlService;
    private String _command; 
    private YIdentifier _caseID;
    private Document _casedata;

    public ServiceHandler(URI yawlService, YWorkItem workItem, String command) {
        _workItem = workItem;
        _yawlService = yawlService;
        _command = command;
    }

    public ServiceHandler(URI yawlService, YIdentifier caseID, Document casedata, String command) {
        _yawlService = yawlService;
        _caseID = caseID;
        _command = command;
        _casedata = casedata;
    }

    public void run() {
        try {
            if (InterfaceB_EngineBasedClient.ADDWORKITEM_CMD.equals(_command)) {
                String urlOfYawlService = _yawlService.toString();
                String workItemXML = _workItem.toXML();
                Map paramsMap = new HashMap();
                paramsMap.put("workItem", workItemXML);
                paramsMap.put("action", "handleEnabledItem");
                Interface_Client.executePost(urlOfYawlService, paramsMap);
            } else if (InterfaceB_EngineBasedClient.CANCELALLWORKITEMS_CMD.equals(_command)) {
                Iterator iter = _workItem.getChildren().iterator();
                InterfaceB_EngineBasedClient.cancelWorkItem(_yawlService, _workItem);
                while (iter.hasNext()) {
                    YWorkItem item = (YWorkItem) iter.next();
                    InterfaceB_EngineBasedClient.cancelWorkItem(_yawlService, item);
                }
            } else if (InterfaceB_EngineBasedClient.CANCELWORKITEM_CMD.equals(_command)) {
                //cancel the parent
                String urlOfYawlService = _yawlService.toString();
                String workItemXML = _workItem.toXML();
                Map paramsMap = new HashMap();
                paramsMap.put("workItem", workItemXML);
                paramsMap.put("action", "cancelWorkItem");
                Interface_Client.executePost(urlOfYawlService, paramsMap);
            } else if (InterfaceB_EngineBasedClient.ANNOUNCE_COMPLETE_CASE_CMD.equals(_command)) {
                String urlOfYawlService = _yawlService.toString();
                String caseID = _caseID.toString();
                String casedataStr = JDOMConversionTools.documentToString(_casedata) ;
                Map paramsMap = new HashMap();
                paramsMap.put("action", _command);
                paramsMap.put("caseID", caseID);
                paramsMap.put("casedata", casedataStr) ;
                Interface_Client.executePost(urlOfYawlService, paramsMap);
            }
        } catch (IOException e) {
            logger.error("failed to call YAWL service", e); 
            EngineFactory.getExistingEngine().announceServiceUnavailable(_workItem, _yawlService);                                
            //e.printStackTrace();
        }            
    }
}
