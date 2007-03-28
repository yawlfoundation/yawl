/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.engine.interfce;

import java.io.IOException;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import au.edu.qut.yawl.authentication.UserList;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.exceptions.YAWLException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.util.XmlUtilities;
import au.edu.qut.yawl.worklist.model.Marshaller;
import au.edu.qut.yawl.worklist.model.SpecificationData;
import au.edu.qut.yawl.worklist.model.TaskInformation;
import au.edu.qut.yawl.worklist.model.WorkItemRecord;
import au.edu.qut.yawl.worklist.model.WorklistModel;

/**
 * This class is an API for accessing the YAWL engine internally, for services.
 * Adapted from {@link InterfaceBWebsideController}.
 * @author Lachlan Aldred
 * @author Nathan Rose
 */
public abstract class InterfaceBInternalServiceController {
	private static Logger logger = Logger.getLogger(InterfaceBInternalServiceController.class);
    protected static final String XSD_STRINGTYPE = "string";
    protected static final String XSD_ANYURI_TYPE = "anyURI";
    protected static final String XSD_NCNAME_TYPE = "NCName";
    protected static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
    
    private SAXBuilder builder = new SAXBuilder();
    private WorklistModel model = new WorklistModel();
    
    private EngineGateway engine;
    private String sessionHandle;

    /**
     * Constructs a controller.
     * @throws YPersistenceException 
     */
    public InterfaceBInternalServiceController() throws YPersistenceException {
    	engine = new EngineGatewayImpl(true);
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
    public abstract void handleCompleteCaseEvent(String caseID, String casedata);

    /**
     * Override this method if you wish to allow other tools to find out what
     * input parameters are required for your custom YAWL service to work.
     * @return an array of input parameters.
     */
    public abstract YParameter[] describeRequiredParams();
    
    public abstract String getDocumentation();
    
    public abstract String getServiceName();
    
    public final String getServiceURI() {
    	return "internal://" + getServiceName();
    }

    private EngineGateway getEngine() throws YAWLException {
    	try {
	    	if(!UserList._permissionGranted.equals(engine.checkConnection(sessionHandle))) {
	    		sessionHandle = engine.connect("admin", "YAWL");
	    	}
	    	return engine;
    	}
    	catch(RemoteException e) {
    		throw new YAWLException(e);
    	}
    }
    
    /**
     * Checks a work item out of the engine.  Also stores a local copy of the active item.
     * @param workItemID the work item id.
     * @return the resultant checked-out workitem.
     * @throws YAWLException 
     */
    public WorkItemRecord checkOut(String workItemID) throws YAWLException {
    	try {
	    	WorkItemRecord resultItem = null;
	        
	        String msg = getEngine().startWorkItem(workItemID, sessionHandle);
	        if(successful(msg)) {
	            try {
	                Document doc = builder.build(new StringReader(msg));
	                Element workItemElem = doc.getRootElement().getChild("workItem");
	                resultItem = Marshaller.unmarshalWorkItem(workItemElem);
	                model.addWorkItem(resultItem);
	            } catch(Exception e) {
	                throw new YAWLException(e);
	            }
	        } else {
	        	throw new YAWLException(msg, XmlUtilities.getError(msg));
	        }
	        return resultItem;
    	} catch(RemoteException e) {
    		throw new YAWLException(e);
    	}
    }
    
    /**
     * Checks a work item back into the engine.
     * @param workItemID the work item id.
     * @param inputData the input data.
     * @param outputData the output data.
     * @param sessionHandle the session handle.
     * @return a diagnostic result of the action - in XML.
     * @throws YAWLException 
     * @deprecated replaced by checkInWorkItem(String workItemID, Element inputData, Element outputData, String sessionHandle)
     */
    public String checkInWorkItem(String workItemID, String inputData, String outputData) throws YAWLException {
		Element inputDataEl = null;
		Element outputDataEl = null;
		
		try {
			Document inputDataDoc = builder.build(new StringReader(inputData));
			inputDataEl = inputDataDoc.getRootElement();
			
			Document outputDataDoc = builder.build(new StringReader(outputData));
			outputDataEl = outputDataDoc.getRootElement();
		} catch(JDOMException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return checkInWorkItem(workItemID, inputDataEl, outputDataEl);
	}

    /**
	 * Checks a work item into the engine.
	 * 
	 * @param workItemID
	 *            the work item id.
	 * @param inputData
	 *            the input data as an XML String.
	 * @param outputData
	 *            the output data as an XML String
	 * @param sessionHandle
	 * @return a diagnostic result of the action - in XML.
     * @throws YAWLException 
	 */
    public String checkInWorkItem(String workItemID, Element inputData, Element outputData) throws YAWLException {
    	try {
	        //first merge the input and output data together
	        String mergedlOutputData = Marshaller.getMergedOutputData(inputData, outputData);
	        
	        WorkItemRecord workitem = null;
	        workitem = getWorkItem(workItemID);
	        
	        SpecificationData specData = getSpecificationData(workitem.getSpecificationID());
	
	        String filteredOutputData;
	
	        if (!(specData.usesSimpleRootData())) {
	            TaskInformation taskInfo = getTaskInformation(
	                    workitem.getSpecificationID(),
	                    workitem.getTaskID());
	            List outputParams = taskInfo.getParamSchema().getOutputParams();
	
	            filteredOutputData = Marshaller.filterDataAgainstOutputParams(
	                    mergedlOutputData, outputParams);
	        } else {
	            filteredOutputData = mergedlOutputData;
	        }
	
	        String result = getEngine().completeWorkItem(workItemID, filteredOutputData, false, sessionHandle);
	        model.removeRemotelyCachedWorkItem(workItemID);
	        return result;
    	}
    	catch(RemoteException e) {
    		throw new YAWLException(e);
    	} catch(JDOMException e) {
    		throw new YAWLException(e);
		} catch(IOException e) {
			throw new YAWLException(e);
		}
    }
    
    public WorkItemRecord getWorkItem(String workItemID) throws YAWLException {
    	WorkItemRecord record = getCachedWorkItem(workItemID);
    	if (record==null) {
    		record = getEngineStoredWorkItem(workItemID);
    		model.addWorkItem(record);
        }
    	return record;
    }
    
    /**
     * Gets a locally stored copy of a work item.
     * @param workItemID
     * @return a local (to the custom service) cached copy of the workitem.
     */
    public WorkItemRecord getCachedWorkItem(String workItemID) {
        return model.getWorkItem(workItemID);
    }

    public WorkItemRecord getEngineStoredWorkItem(String workItemID) throws YAWLException {
    	try {
	    	SAXBuilder builder = new SAXBuilder();
	        List workItems = new ArrayList();
	        String result = getEngine().describeAllWorkItems(sessionHandle);
	        
	        if(successful(result)) {
	            Document doc = builder.build(new StringReader("<root>" + result + "</root>"));
	            Iterator workItemEls = doc.getRootElement().getChildren().iterator();
	            while(workItemEls.hasNext()) {
	                Element workItemElement = (Element) workItemEls.next();
	                WorkItemRecord workItem = Marshaller.unmarshalWorkItem(workItemElement);
	                workItems.add(workItem);
	            }
	        } else {
	        	throw new YAWLException(result, XmlUtilities.getError(result));
	        }
	        
	        for(Iterator iterator = workItems.iterator(); iterator.hasNext();) {
	            WorkItemRecord record = (WorkItemRecord) iterator.next();
	            if(record.getID().equals(workItemID)) {
	                return record;
	            }
	        }
	        return null;
    	}
    	catch(RemoteException e) {
    		throw new YAWLException(e);
    	} catch(JDOMException e) {
			throw new YAWLException(e);
		} catch(IOException e) {
			throw new YAWLException(e);
		}
    }

    public TaskInformation getTaskInformation(String specificationID, String taskID) throws YAWLException {
    	try {
	        TaskInformation taskInfo = null;
	        String taskInfoStr = getEngine().getTaskInformation(specificationID, taskID, sessionHandle);
		    
	        if (successful(taskInfoStr)) {
	            taskInfo = Marshaller.unmarshalTaskInformation(taskInfoStr);
	        }
	        
	        return taskInfo;
    	}
    	catch(RemoteException e) {
    		throw new YAWLException(e);
    	}
    }

    public List<WorkItemRecord> getChildren(String workItemID) throws YAWLException {
    	try {
	    	String result = getEngine().getChildrenOfWorkItem(workItemID, sessionHandle);
	    	
	    	SAXBuilder builder = new SAXBuilder();
	        List workItems = new ArrayList();
	        if (result != null && successful(result)) {
	            Document doc = builder.build(new StringReader("<root>" + result + "</root>"));
	            Iterator workItemEls = doc.getRootElement().getChildren().iterator();
	            while (workItemEls.hasNext()) {
	                Element workItemElement = (Element) workItemEls.next();
	                WorkItemRecord workItem = Marshaller.unmarshalWorkItem(workItemElement);
	                workItems.add(workItem);
	            }
	        }
	        return workItems;
    	}
    	catch(RemoteException e) {
    		throw new YAWLException(e);
    	} catch(JDOMException e) {
    		throw new YAWLException(e);
		} catch(IOException e) {
			throw new YAWLException(e);
		}
    }

    public List<SpecificationData> getSpecificationPrototypesList() throws YAWLException {
    	try {
	    	String result = getEngine().getSpecificationList(sessionHandle);
	        return Marshaller.unmarshalSpecificationSummary("<root>" + result + "</root>");
    	}
    	catch(RemoteException e) {
    		throw new YAWLException(e);
    	}
    }

    /**
     * Gets the specification data object with the spec id.  If called it will return the
     * specdata object and the entire XML representation of the specification.
     * @see au.edu.qut.yawl.worklist.model.SpecificationData
     * @param specID
     * @return a Specification data object for spec id else null.
     * @throws YAWLException 
     */
    public SpecificationData getSpecificationData(String specID) throws YAWLException {
    	try {
	    	List specs = getSpecificationPrototypesList();
	    	for (int i = 0; i < specs.size(); i++) {
	    		SpecificationData data = (SpecificationData) specs.get(i);
	
	    		if (data.getID().equals(specID)) {
	    			String specAsXML = data.getAsXML();
	    			if (specAsXML == null) {
	    				specAsXML = getEngine().getProcessDefinition(specID, sessionHandle);
	    				data.setSpecAsXML(specAsXML);
	    			}
	    			return data;
	    		}
	    	}
	        return null;
    	}
    	catch(RemoteException e) {
    		throw new YAWLException(e);
    	}
    }

    /**
     * Utility method for implementers to use for helping to check all instances
     * of a given task out of the engine.
     * @param enabledWorkItem an enabled WorkItemRecord.
     * @return a list of work item records that correspond to the executing work-items
     * that should be checked back into the engine when the task is complete.
     * @throws YAWLException 
     */
    protected List<WorkItemRecord> checkOutAllInstancesOfThisTask(WorkItemRecord enabledWorkItem) throws YAWLException {
        if (null == enabledWorkItem) {
            throw new IllegalArgumentException("Param enabledWorkItem cannot be null.");
        }
        if (!enabledWorkItem.getStatus().equals(YWorkItem.Status.Enabled)) {
            throw new IllegalArgumentException("Param enabledWorkItem must be enabled.");
        }

        //first of all checkout an enabled work item
        WorkItemRecord result = checkOut(enabledWorkItem.getID());

        logger.debug("Result of item [" + enabledWorkItem.getID() + "] checkout is : " + result);

        //if the work item has any children
        List mixedChildren = getChildren(enabledWorkItem.getID());
        for (int i = 0; i < mixedChildren.size(); i++) {
            WorkItemRecord itemRecord = (WorkItemRecord) mixedChildren.get(i);
            if (YWorkItem.Status.Fired.equals(itemRecord.getStatus())) {
                logger.debug("Result of item [" + itemRecord.getID() + "] checkout is : " +
                        checkOut(itemRecord.getID()));
            }
        }
        return getChildren(enabledWorkItem.getID());
    }

    /**
     * Utility method to prepare the reply element for checking work item back into
     * the engine.
     * @param enabledWorkItem the enabled work item.
     * @return an empty root element correctly named according to the version
     * of the process specification.
     * @throws YAWLException 
     */
    protected Element prepareReplyRootElement(WorkItemRecord enabledWorkItem) throws YAWLException {
        Element replyToEngineRootDataElement;

        //prepare reply root element.
        SpecificationData sdata = getSpecificationData(enabledWorkItem.getSpecificationID());
        
        TaskInformation taskInfo = getTaskInformation(
        		enabledWorkItem.getSpecificationID(),
                enabledWorkItem.getTaskID());
        
        String decompID = taskInfo.getDecompositionID();
        if(sdata.usesSimpleRootData()) {
            replyToEngineRootDataElement = new Element("data");
        } else {
            replyToEngineRootDataElement = new Element(decompID);
        }
        return replyToEngineRootDataElement;
    }

    /**
     * checks to see if the inupt string doesn't contain a <failure>..</failure> message.
     * @param input
     */
    public boolean successful(String input) {
        return Interface_Client.successful(input);
    }
}
