/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine.interfce;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;

import au.edu.qut.yawl.deployment.DirectoryListener;
import au.edu.qut.yawl.deployment.ServiceBuilder;
import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.ObserverGateway;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.util.JDOMConversionTools;
import au.edu.qut.yawl.worklist.model.WorkItemRecord;


/**
 * @see InterfaceB_EngineBasedClient
 * @author Lachlan Aldred
 * @author Nathan Rose
 */
public class InterfaceB_InternalEngineBasedClient implements ObserverGateway {
    protected static Logger logger = Logger.getLogger(InterfaceB_InternalEngineBasedClient.class);

    protected static DirectoryListener dirlistener = new DirectoryListener();
    protected static ServiceBuilder servicebuilder = null;
    
    protected static final String ADDWORKITEM_CMD =             "announceWorkItem";
    protected static final String CANCELALLWORKITEMS_CMD =      "cancelAllInstancesUnderWorkItem";
    protected static final String CANCELWORKITEM_CMD =          "cancelWorkItem";
    protected static final String ANNOUNCE_COMPLETE_CASE_CMD =  "announceCompletion";

    public InterfaceB_InternalEngineBasedClient() throws YPersistenceException {
		try {
			File f = new File("./webapps/yawl/WEB-INF/lib/");
	    	if(f.exists()) {
	    		dirlistener = new DirectoryListener(f.getAbsoluteFile().getCanonicalPath());
	    	} else {
	    		System.out.println("Internal YAWL Service directory '/WEB-INF/lib/' does not exist!\n" +
	    				"Using 'common/lib' instead!");
	    		
	    	}
		} catch(IOException e) {
			logger.error("Error obtaining dynamic YAWL Service directory!", e);
		}
    	servicebuilder = dirlistener.initServiceDirectory();
    	//dirlistener.start();
    }
    
    /**
     * Indicates which protocol this shim services.<P>
     *
     * @return the scheme
     */
    public String getScheme() {
        return "internal";
    }

    /**
     * PRE: The work item is enabled.
     * announces a work item to a YAWL Service.
     * @param yawlService the reference to a YAWL service in the environment
     * @param workItem the work item to announce,
     */
    public void announceWorkItem(URI yawlService, YWorkItem workItem) {
        Handler myHandler = new Handler(yawlService, workItem, ADDWORKITEM_CMD);
        myHandler.start();
    }

    /**
     * Annonuces work item cancellation to the YAWL Service.
     * @param yawlService the YAWL service reference.
     * @param workItem the work item to cancel.
     */
    static void cancelWorkItem(URI yawlService, YWorkItem workItem) {
        Handler myHandler = new Handler(yawlService, workItem, CANCELWORKITEM_CMD);
        myHandler.start();
    }

    /**
     * Cancels the work item, and all child
     * workitems under the provided work item.
     * @param yawlService the yawl service reference.
     * @param workItem the parent work item to cancel.
     */
    public void cancelAllWorkItemsInGroupOf(URI yawlService, YWorkItem workItem) {
        //System.out.println("Thread::yawlService.getURI() = " + yawlService.getURI());
        //System.out.println("\rworkItem.toXML() = " + workItem.toXML());
        if(workItem.getParent() == null){
            Handler myHandler = new Handler(yawlService, workItem, CANCELALLWORKITEMS_CMD);
            myHandler.start();
        }
        else {
            Handler myHandler = new Handler(yawlService, workItem.getParent(), CANCELALLWORKITEMS_CMD);
            myHandler.start();
        }
    }

    /**
     * Called by engine to announce when a case is complete.
     * @param yawlService the yawl service
     * @param caseID the case that completed
     */
    public void announceCaseCompletion(URI yawlService, 
                                       YIdentifier caseID, Document casedata) {
        Handler myHandler = new Handler(yawlService, caseID, casedata, ANNOUNCE_COMPLETE_CASE_CMD);
        myHandler.start();
    }

    /**
     * Returns an array of YParameter objects that describe the YAWL service
     * being referenced.
     * @param yawlService the YAWL service reference.
     * @return an array of YParameter objects.
     * @throws IOException if connection problem
     * @throws JDOMException if XML content problem.
     */
    public YParameter[] getRequiredParamsForService(YAWLServiceReference yawlService) {
    	InterfaceBInternalServiceController service = servicebuilder.getServiceInstance(yawlService.getURI());

    	return service.describeRequiredParams();
    }

    static class Handler extends Thread {
        private YWorkItem _workItem;
        private URI _yawlService;
        private String _command; 
        private YIdentifier _caseID;
        private Document _casedata;

        public Handler(URI yawlService, YWorkItem workItem, String command) {
            _workItem = workItem;
            _yawlService = yawlService;
            _command = command;
        }

        public Handler(URI yawlService, YIdentifier caseID, Document casedata, String command) {
            _yawlService = yawlService;
            _caseID = caseID;
            _command = command;
            _casedata = casedata;
        }

        public void run() {
            try {
                if (ADDWORKITEM_CMD.equals(_command)) {
                    String urlOfYawlService = _yawlService.toString();
                    callHandleEnabled(urlOfYawlService, _workItem);
                } else if (CANCELALLWORKITEMS_CMD.equals(_command)) {
                    Iterator iter = _workItem.getChildren().iterator();
                    callCancelled(_yawlService.toString(), _workItem);
                    while (iter.hasNext()) {
                        YWorkItem item = (YWorkItem) iter.next();
                        callCancelled(_yawlService.toString(), item);
                    }
                } else if (CANCELWORKITEM_CMD.equals(_command)) {
                    //cancel the parent
                    String urlOfYawlService = _yawlService.toString();
                    callCancelled(urlOfYawlService, _workItem);
                } else if (ANNOUNCE_COMPLETE_CASE_CMD.equals(_command)) {
                    String urlOfYawlService = _yawlService.toString();
                    String casedata = JDOMConversionTools.documentToString(_casedata);
                    callCompletedCase(urlOfYawlService, _caseID, casedata);
                }
            } catch (Exception e) {
                logger.error("failed to call YAWL service", e);
                EngineFactory.getExistingEngine().announceServiceUnavailable(_workItem, _yawlService);
            }
        }
    }
    
    public static void callHandleEnabled(String url, YWorkItem workitem) {
    	InterfaceBInternalServiceController service = servicebuilder.getServiceInstance(url);
    	
    	if(service != null) {
    		service.handleEnabledWorkItemEvent(createWorkItemRecord(workitem).toYWorkItemXML());
    	} else {
    		for(InterfaceBInternalServiceController svc : servicebuilder.getServices()) {
    			logger.error("Service:" + svc.getServiceURI() + " " + svc.getDocumentation());
    		}
    		throw new RuntimeException("No internal service exists for " + url);
    	}
    }
    
    public static void callCancelled(String url, YWorkItem workitem) {
    	InterfaceBInternalServiceController service = servicebuilder.getServiceInstance(url);
    	
    	service.handleCancelledWorkItemEvent(createWorkItemRecord(workitem).toYWorkItemXML());
    }
    
    private static WorkItemRecord createWorkItemRecord(YWorkItem workItem) {
    	return new WorkItemRecord(
    			workItem.getCaseID().toString(),
    			workItem.getTaskID(),
    			workItem.getSpecificationID(),
    			workItem.getEnablementTimeStr(),
    			workItem.getStatus().toString());
    }
    
    public static void callCompletedCase(String url, YIdentifier caseID, String casedata) {
    	InterfaceBInternalServiceController service = servicebuilder.getServiceInstance(url);
    	
    	service.handleCompleteCaseEvent(caseID.toString(), casedata);
    }
}
