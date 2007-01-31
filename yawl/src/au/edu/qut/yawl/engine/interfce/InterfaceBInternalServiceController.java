/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.engine.interfce;

import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.YEngine;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.engine.domain.YWorkItemRepository;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YQueryException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YStateException;
import au.edu.qut.yawl.worklist.model.*;



import java.io.IOException;

import java.util.List;
import java.util.Set;


/**
 * This class is an API for accessing the YAWL engine webapp.  It uses by default XML-over-HTTP
 * as a transport mechanism.
 * @author Lachlan Aldred
 * Date: 19/03/2004
 * Time: 11:44:49
 */
public abstract class InterfaceBInternalServiceController {


    protected static final String XSD_STRINGTYPE = "string";
    protected static final String XSD_ANYURI_TYPE = "anyURI";
    protected static final String XSD_NCNAME_TYPE = "NCName";
    protected static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema";

	private String serviceuri = null;
	
	public String getServiceURI() {
		// TODO Auto-generated method stub
		return serviceuri;
	}

	public void setServiceURI(String uri) {
		// TODO Auto-generated method stub
		this.serviceuri = uri;
		
	}
	
	public String getDocumentation() {
		return "Internal Service";
	}
    
    /**
     * Constructs a controller.
     */
    public InterfaceBInternalServiceController() {


    }



    /**
     * It recieves messages from the engine
     * notifying an enabled task and acts accordingly.  In this case it takes the message,
     * tries to check out the work item, and if successful it begins to start up a web service
     * invokation.
     * @param enabledWorkItem
     */
    public abstract void handleEnabledWorkItemEvent(YWorkItem enabledWorkItem);

    /**
     * By implementing this method and deploying a web app containing the implementation
     * the YAWL engine will send events to this method notifying your custom
     * YAWL service that an active work item has been cancelled by the
     * engine.
     * @param workItemRecord a "snapshot" of the work item cancelled in
     * the engine.
     */
    public abstract void handleCancelledWorkItemEvent(WorkItemRecord workItemRecord);


    /**
     * By overriding this method one can process case completion events.
     * @param caseID the id of the completed case.
     */
    public void handleCompleteCaseEvent(String caseID, String casedata) {

    }

    /**
     * Override this method if you wish to allow other tools to find out what
     * input parameters are required for your custom YAWL service to work.
     * @return an array of input parameters.
     */
    public YParameter[] describeRequiredParams() {
        return new YParameter[0];
    }



    /**
     * Checks a work item out of the engine.  Also stores a local copy of the active item.
     * @param workItemID the work item id.
     * @param sessionHandle the session handle
     * @return the resultant checked-out workitem.
     * @throws IOException if the engine cannot be connected with.
     */
    public YWorkItem checkOut(String workItemID) throws IOException, YSchemaBuildingException, YQueryException, YDataStateException, YStateException, YPersistenceException {
        YWorkItem resultItem = null;

//        YWorkItem item = YWorkItemRepository.getInstance().getWorkItem(workItemID);
        
        System.out.println("starting workitem");
       	resultItem = EngineFactory.getTransactionalEngine().startWorkItem(workItemID , "admin");
        System.out.println("started workitem");
        
        return resultItem;
    }



    /**
     * Checks a work item into the engine.
     * @param workItemID the work item id.
     * @param inputData the input data as an XML String.
     * @param outputData the output data as an XML String
     * @param sessionHandle
     * @return a diagnostic result of the action - in XML.
     * @throws IOException if there is a problem contacting the engine.
     * @throws JDOMException if there is a problem parsing XML of input data or output data
     */
    public void checkInWorkItem(String workItemID , String data) throws YStateException, YDataStateException, YQueryException, YSchemaBuildingException, YPersistenceException {
       	EngineFactory.getTransactionalEngine().completeWorkItem(workItemID, data, false);
    }

    
    public WorkItemRecord getWorkItem(String workItemID,String sessionHandle) throws IOException {
    	WorkItemRecord record = null;

    	return record;
    }


    public WorkItemRecord getEngineStoredWorkItem(String workItemID, String sessionHandle) throws IOException {

        return null;
    }


    public YTask getTaskInformation(String specificationID, String taskID) throws YPersistenceException {
        YTask task = EngineFactory.getTransactionalEngine().getTaskDefinition(specificationID, taskID);

        return task;
    }


    public Set getChildren(String workitemid) throws YPersistenceException {
    	return YWorkItemRepository.getInstance().getChildrenOf(workitemid);
    }





    /**
     * logs the failure of client to contact the YAWL engine.
     * gives some suggestion of why?
     * @param e
     * @param backEndURIStr
     */
    public static void logContactError(IOException e, String backEndURIStr) {

    }


    /**
     * checks to see if the inupt string doesn't contain a <failure>..</failure> message.
     * @param input
     */
    public boolean successful(String input) {
        return Interface_Client.successful(input);
    }


    public List getSpecificationPrototypesList(String sessionHandle)
            throws IOException {
        return null;//getSpecificationList(sessionHandle);
    }


    /**
     * Gets the specification data object with the spec id.  If called it will return the
     * specdata object and the entire XML representation of the specification.
     * @see au.edu.qut.yawl.worklist.model.SpecificationData
     * @param specID
     * @param sessionHandle
     * @return a Specification data object for spec id else null.
     * @throws java.io.IOException
     */
    public SpecificationData getSpecificationData(String specID, String sessionHandle) throws IOException {

        return null;
    }


}

