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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
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
    public InterfaceBInternalServiceController() {
//    	engine = new EngineGatewayImpl(true);
    }
    
    public final void setEngineGateway(Object engineGateway) {
    	this.engine = new InternalEngineGatewayImpl(engineGateway);
    }

    /**
     * It recieves messages from the engine
     * notifying an enabled task and acts accordingly.  In this case it takes the message,
     * tries to check out the work item, and if successful it begins to start up a web service
     * invokation.
     * @param enabledWorkItem
     */
    public abstract void handleEnabledWorkItemEvent(String workItemRecord);

    /**
     * By implementing this method and deploying a web app containing the implementation
     * the YAWL engine will send events to this method notifying your custom
     * YAWL service that an active work item has been cancelled by the
     * engine.
     * @param workItemRecord a "snapshot" of the work item cancelled in
     * the engine.
     */
    public abstract void handleCancelledWorkItemEvent(String workItemRecord);


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
    public YParameter[] describeRequiredParams() {
        return new YParameter[0];
    }
    
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
     * @return the resultant checked-out workitemrecord.
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
    
    private static class InternalEngineGatewayImpl implements EngineGateway {
    	private Object engine;
    	
    	private static final Class[] ONE_STRING = new Class[] {String.class};
    	private static final Class[] TWO_STRINGS = new Class[] {String.class, String.class};
    	private static final Class[] THREE_STRINGS = new Class[] {String.class, String.class, String.class};
    	/** String, String, boolean, String */
    	private static final Class[] SSBS = new Class[] {String.class, String.class, boolean.class, String.class};
    	
    	public InternalEngineGatewayImpl(Object engine) {
    		if(engine == null)
    			throw new IllegalArgumentException("EngineGateway must not be null!");
    		this.engine = engine;
    	}
    	
    	/**
    	 * Utility function that provides a common error message when a method isn't found.
    	 */
    	private Method getMethod(String name, Class[] parameterTypes) {
    		Method method = null;
    		Exception e = null;
			try {
				method = engine.getClass().getMethod(name, parameterTypes);
			} catch(SecurityException ex) {
				e = ex;
			} catch(NoSuchMethodException ex) {
				e = ex;
			}
    		if(method == null) {
    			String params = "";
    			if(parameterTypes != null) {
    				for(int index = 0; index < parameterTypes.length; index++) {
    					if(params.length() > 0)
    						params += ", ";
    					params += parameterTypes[index].getSimpleName();
    				}
    			}
    			throw new RuntimeException("Error: Improper engine gateway! Method " + name + "(" + params + ") not available!", e);
    		}
    		return method;
    	}
    	
    	/**
    	 * Utility function that provides a common error message when a method throws an exception.
    	 */
    	private Object invoke(Method method, Object... arguments) {
    		Exception e = null;
    		try {
				return method.invoke(engine, arguments);
			} catch(IllegalArgumentException ex) {
				e = ex;
			} catch(IllegalAccessException ex) {
				e = ex;
			} catch(InvocationTargetException ex) {
				e = ex;
			}
			String argString = "";
			if(arguments != null) {
				for(int index = 0; index < arguments.length; index++) {
					if(argString.length() > 0)
						argString += ", ";
					argString += arguments[index];
				}
			}
			throw new RuntimeException("Error invoking method " + method.toGenericString()
					+ " with arguments " + argString, e);
    	}
    	
		public String addYAWLService(String serviceStr, String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("addYAWLService", TWO_STRINGS),
					serviceStr,
					sessionHandle);
		}

		public String cancelCase(String caseID, String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("cancelCase", TWO_STRINGS),
					caseID,
					sessionHandle);
		}

		public String cancelWorkItem(String id, String fail, String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("cancelWorkItem", THREE_STRINGS),
					id,
					fail,
					sessionHandle);
		}

		public String changePassword(String password, String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("changePassword", TWO_STRINGS),
					password,
					sessionHandle);
		}

		public String checkConnection(String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("checkConnection", ONE_STRING),
					sessionHandle);
		}

		public String checkConnectionForAdmin(String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("checkConnectionForAdmin", ONE_STRING),
					sessionHandle);
		}

		public String checkElegibilityToAddInstances(String workItemID, String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("checkElegibilityToAddInstances", TWO_STRINGS),
					workItemID,
					sessionHandle);
		}

		public String completeWorkItem(String workItemID, String data, boolean force, String sessionHandle) throws RemoteException {
			// TODO check if this works because of auto-boxing and primitive type
			return (String) invoke(
					getMethod("completeWorkItem", SSBS),
					workItemID,
					data,
					force,
					sessionHandle);
		}

		public String connect(String userID, String password) throws RemoteException {
			return (String) invoke(
					getMethod("connect", TWO_STRINGS),
					userID,
					password);
		}

		public String createNewInstance(String workItemID, String paramValueForMICreation, String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("createNewInstance", THREE_STRINGS),
					workItemID,
					paramValueForMICreation,
					sessionHandle);
		}

		public String createUser(String userName, String password, boolean isAdmin, String sessionHandle) throws RemoteException {
			// TODO check if this works because of auto-boxing and primitive type
			return (String) invoke(
					getMethod("createUser", SSBS),
					userName,
					password,
					isAdmin,
					sessionHandle);
		}

		public String deleteUser(String userName, String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("deleteUser", TWO_STRINGS),
					userName,
					sessionHandle);
		}

		public String describeAllWorkItems(String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("describeAllWorkItems", ONE_STRING),
					sessionHandle);
		}

		public boolean enginePersistenceFailure() {
			// TODO check if this works because of auto-boxing and primitive type
			return ((Boolean) invoke(
					getMethod("enginePersistenceFailure", null)
					)).booleanValue();
		}

		public String getAvailableWorkItemIDs(String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("getAvailableWorkItemIDs", ONE_STRING),
					sessionHandle);
		}

		public String getCasesForSpecification(String specID, String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("getCasesForSpecification", TWO_STRINGS),
					specID,
					sessionHandle);
		}

		public String getCaseState(String caseID, String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("getCaseState", TWO_STRINGS),
					caseID,
					sessionHandle);
		}

		public String getChildrenOfWorkItem(String workItemID, String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("getChildrenOfWorkItem", TWO_STRINGS),
					workItemID,
					sessionHandle);
		}

		public String getProcessDefinition(String specID, String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("getProcessDefinition", TWO_STRINGS),
					specID,
					sessionHandle);
		}

		public String getSpecificationList(String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("getSpecificationList", ONE_STRING),
					sessionHandle);
		}

		public String getSpecificationsByRestriction(String restriction, String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("getSpecificationsByRestriction", TWO_STRINGS),
					restriction,
					sessionHandle);
		}

		public String getTaskInformation(String specificationID, String taskID, String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("getTaskInformation", THREE_STRINGS),
					specificationID,
					taskID,
					sessionHandle);
		}

		public String getUsers(String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("getUsers", ONE_STRING),
					sessionHandle);
		}

		public String getWorkItemDetails(String workItemID, String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("getWorkItemDetails", TWO_STRINGS),
					workItemID,
					sessionHandle);
		}

		public String getWorkItemOptions(String workItemID, String thisURL, String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("getWorkItemOptions", THREE_STRINGS),
					workItemID,
					thisURL,
					sessionHandle);
		}

		public String getYAWLServiceDocumentation(String yawlServiceURI, String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("getYAWLServiceDocumentation", TWO_STRINGS),
					yawlServiceURI,
					sessionHandle);
		}

		public String getYAWLServices(String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("getYAWLServices", ONE_STRING),
					sessionHandle);
		}

		public String launchCase(String specID, String caseParams, URI completionObserverURI, String sessionHandle) throws RemoteException {
			// TODO check if this works because of the usage of a non-string type
			return (String) invoke(
					getMethod(
							"launchCase",
							new Class[] {String.class, String.class, URI.class, String.class}),
					specID,
					caseParams,
					completionObserverURI,
					sessionHandle);
		}

		public String loadSpecification(String specification, String fileName, String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("loadSpecification", THREE_STRINGS),
					specification,
					fileName,
					sessionHandle);
		}

		public String removeExceptionObserver() {
			return (String) invoke(
					getMethod("removeExceptionObserver", null)
					);
		}

		public String removeYAWLService(String serviceURI, String sessionHandle) {
			return (String) invoke(
					getMethod("removeYAWLService", TWO_STRINGS),
					serviceURI,
					sessionHandle);
		}

		public String restartWorkItem(String workItemID, String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("restartWorkItem", TWO_STRINGS),
					workItemID,
					sessionHandle);
		}

		public String rollbackWorkItem(String workItemID, String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("rollbackWorkItem", TWO_STRINGS),
					workItemID,
					sessionHandle);
		}

		public String setExceptionObserver(String observerURI) {
			return (String) invoke(
					getMethod("setExceptionObserver", ONE_STRING),
					observerURI);
		}

		public String startWorkItem(String workItemID, String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("startWorkItem", TWO_STRINGS),
					workItemID,
					sessionHandle);
		}

		public String suspendWorkItem(String workItemID, String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("suspendWorkItem", TWO_STRINGS),
					workItemID,
					sessionHandle);
		}

		public String unloadSpecification(String specID, String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("unloadSpecification", TWO_STRINGS),
					specID,
					sessionHandle);
		}

		public String unsuspendWorkItem(String workItemID, String sessionHandle) throws RemoteException {
			return (String) invoke(
					getMethod("workItemID", TWO_STRINGS),
					workItemID, sessionHandle);
		}

		public String updateCaseData(String idStr, String data, String sessionHandle) {
			return (String) invoke(
					getMethod("updateCaseData", THREE_STRINGS),
					idStr,
					data,
					sessionHandle);
		}

		public String updateWorkItemData(String workItemID, String data, String sessionHandle) {
			return (String) invoke(
					getMethod("updateWorkItemData", THREE_STRINGS),
					workItemID,
					data,
					sessionHandle);
		}
    }
}
