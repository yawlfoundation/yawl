/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine.interfce;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import au.edu.qut.yawl.authentication.User;
import au.edu.qut.yawl.authentication.UserList;
import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.elements.data.YParameter;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.YEngineInterface;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.exceptions.YAWLException;
import au.edu.qut.yawl.exceptions.YAuthenticationException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.unmarshal.YMarshal;
import au.edu.qut.yawl.util.YVerificationMessage;

/**
 * This class allows access to all of the engines capabilities using Strings and
 * XML interface to pass information.
 *
 * 
 * @author Lachlan Aldred
 * Date: 21/01/2004
 * Time: 11:58:18
 * 
 */
public class EngineGatewayImpl implements EngineGateway {

    Logger logger = Logger.getLogger(EngineGatewayImpl.class);

    private YEngineInterface _engine;
    private UserList _userList;
    private boolean enginePersistenceFailure = false;
    private static final String OPEN_FAILURE = "<failure><reason>";
    private static final String CLOSE_FAILURE = "</reason></failure>";
    private static final String SUCCESS = "<success/>";
    private static final String OPEN_SUCCESS = "<success>";
    private static final String CLOSE_SUCCESS = "</success>";
    //todo REFACTOR get all the XML stuff from the engine encapsulated by this class
    /**
     *  Constructor
     */
    public EngineGatewayImpl(boolean persist) throws YPersistenceException {

        //
    	if (persist) {
    		_engine = EngineFactory.getTransactionalEngine();
    	} else {
    		_engine = EngineFactory.createYEngine(persist);
    	}
        _userList = UserList.getInstance();
    }

    /**
     * Indicates if the engine has encountered some form of persistence failure in its lifetime.<P>
     *
     * @return whether or not the engine failed to achieve persistence
     */
    public boolean enginePersistenceFailure()
    {
        return enginePersistenceFailure;
    }

    /**
     *
     * @param sessionHandle
     * @return
     * @throws RemoteException
     */
    public String getAvailableWorkItemIDs(String sessionHandle) throws RemoteException {
        try {
            _userList.checkConnection(sessionHandle);
        } catch (YAuthenticationException e) {
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
        Set allItems = _engine.getAvailableWorkItems();
        StringBuffer workItemsStr = new StringBuffer();
        workItemsStr.append("<ids>");
        for (Iterator iterator = allItems.iterator(); iterator.hasNext();) {
            YWorkItem workItem = (YWorkItem) iterator.next();
            workItemsStr.append("<workItemID>");
            workItemsStr.append(workItem.getWorkItemID().toString());
            workItemsStr.append("</workItemID>");
        }
        workItemsStr.append("</ids>");
        return workItemsStr.toString();
    }


    /**
     *
     * @param workItemID
     * @param sessionHandle
     * @return
     * @throws RemoteException
     */
    public String getWorkItemDetails(String workItemID, String sessionHandle) throws RemoteException {
        try {
            _userList.checkConnection(sessionHandle);
        } catch (YAuthenticationException e) {
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
        YWorkItem workItem = _engine.getWorkItem(workItemID);
        if (workItem != null) {
            return workItem.toXML();
        } else {
            return
                    OPEN_FAILURE +
                    "WorkItem with ID (" + workItemID + ") not found." +
                    CLOSE_FAILURE;
        }
    }


    /**
     *
     * @param specID the specification (process definition id)
     * @param sessionHandle the sessionhandle
     * @return a string version of the process definition
     * @throws RemoteException
     */
    public String getProcessDefinition(String specID, String sessionHandle) throws RemoteException {
        try {
            _userList.checkConnection(sessionHandle);
        } catch (YAuthenticationException e) {
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
        YSpecification spec = _engine.getProcessDefinition(specID);
        if (spec == null) {
            return
                    OPEN_FAILURE +
                    "Specification with ID (" + specID + ") not found." +
                    CLOSE_FAILURE;
        }
        List <YSpecification> specList =
                new Vector<YSpecification>();
        specList.add(spec);
        try {
            return YMarshal.marshal(specList);
        } catch (Exception e) {
            logger.error("Failed to marshal a specification into XML.", e);
            return "";
        }
    }


    /**
     *
     * @param workItemID
     * @param sessionHandle
     * @return
     * @throws RemoteException
     */
    public String suspendWorkItem(String workItemID, String sessionHandle) throws RemoteException {
        try {
            _userList.checkConnection(sessionHandle);
            YWorkItem item = _engine.suspendWorkItem(workItemID);

            if (item != null)
                return OPEN_SUCCESS + item.toXML() + CLOSE_SUCCESS;
            else
                return OPEN_FAILURE +
                        "WorkItem with ID [" + workItemID + "] not found." +
                        CLOSE_FAILURE;
        }
        catch (YAWLException e) {
            if (e instanceof YPersistenceException) {
                enginePersistenceFailure = true;
            }
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
    }

    /**
     *
     * @param workItemID
     * @param sessionHandle
     * @return
     * @throws RemoteException
     */
    public String unsuspendWorkItem(String workItemID, String sessionHandle) throws RemoteException {
        try {
            _userList.checkConnection(sessionHandle);

            YWorkItem item = _engine.unsuspendWorkItem(workItemID);
            if (item != null)
                return OPEN_SUCCESS + item.toXML() + CLOSE_SUCCESS;
            else
                return OPEN_FAILURE +
                        "WorkItem with ID [" + workItemID + "] not found." +
                        CLOSE_FAILURE;
        }
        catch (YAWLException e) {
            if (e instanceof YPersistenceException) {
                enginePersistenceFailure = true;
            }
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
    }


    /**
     *
     * @param workItemID
     * @param sessionHandle
     * @return
     * @throws RemoteException
     */
    public String rollbackWorkItem(String workItemID, String sessionHandle) throws RemoteException {
        try {
            _userList.checkConnection(sessionHandle);
            String userName = _userList.getUserID(sessionHandle);
            _engine.rollbackWorkItem(workItemID, userName);
            return SUCCESS;
        } catch (YAWLException e) {
            if (e instanceof YPersistenceException) {
                enginePersistenceFailure = true;
            }
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
    }


    /**
     *
     * @param workItemID work item id
     * @param data data
     * @param sessionHandle  sessionhandle
     * @return result XML message.
     * @throws RemoteException if used in RMI mode
     */
    public String completeWorkItem(String workItemID, String data, boolean force, String sessionHandle) throws RemoteException {
        try {
            _userList.checkConnection(sessionHandle);
            YWorkItem workItem = _engine.getWorkItem(workItemID);
            if (workItem != null) {
                _engine.completeWorkItem(workItem, data, force);
                return SUCCESS;
            } else {
                return OPEN_FAILURE +
                        "WorkItem with ID [" + workItemID + "] not found." +
                        CLOSE_FAILURE;
            }
        } catch (YAWLException e) {
            if (e instanceof YPersistenceException) {
                enginePersistenceFailure = true;
            }
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
    }


    /**
     *
     * @param workItemID
     * @param sessionHandle
     * @return
     * @throws RemoteException
     */
    public String startWorkItem(String workItemID, String sessionHandle) throws RemoteException {
        try {
            _userList.checkConnection(sessionHandle);

            YWorkItem item = _engine.getWorkItem(workItemID);
            if (item != null) {
                String userID = _userList.getUserID(sessionHandle);
                YWorkItem child = _engine.startWorkItem(item, userID);
                if( child == null ) {
                	throw new YAWLException(
                			"Engine failed to start work item " + item.toString() +
                			". The engine returned no work items." );
                }
                return OPEN_SUCCESS + child.toXML() + CLOSE_SUCCESS;
            }
            return OPEN_FAILURE + "No work item with id = " + workItemID + CLOSE_FAILURE;
        } catch (YAWLException e) {
            if (e instanceof YPersistenceException) {
                enginePersistenceFailure = true;
            }
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
    }


    /**
     * Creates a workitem sibling of of the workitemid param.  It will only do this
     * if:
     *    1) the task for workItemID is multiinstance,
     *    2) and if the task for workItemID is busy
     *    2) and if the task for workItemID allows dynamic instance creation,
     *    3) and if the number of instances has no yet reached max instances.
     *
     * @param workItemID the id of an existing instance to which to add a
     * new instance
     * @param paramValueForMICreation the data needed to create the new instance
     * @param sessionHandle session handle
     * @return an xml message indicating result.
     * @throws RemoteException
     */
    public String createNewInstance(String workItemID, String paramValueForMICreation, String sessionHandle) throws RemoteException {
        try {
            _userList.checkConnection(sessionHandle);
            YWorkItem existingItem = _engine.getWorkItem(workItemID);
            YWorkItem newItem = _engine.createNewInstance(existingItem, paramValueForMICreation);
            return OPEN_SUCCESS + newItem.toXML() + CLOSE_SUCCESS;
        } catch (YAWLException e) {
            if (e instanceof YPersistenceException) {
                enginePersistenceFailure = true;
            }
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
    }


    /**
     *
     * @param sessionHandle
     * @return
     * @throws RemoteException
     */
    public String describeAllWorkItems(String sessionHandle) throws RemoteException {
        try {
            _userList.checkConnection(sessionHandle);
        } catch (YAuthenticationException e) {
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
        return describeWorkItems(_engine.getAllWorkItems());
    }


    /**
     *
     * @param userID
     * @param password
     * @return
     * @throws RemoteException
     */
    public String connect(String userID, String password) throws RemoteException {
        try {
            return _userList.connect(userID, password);
        } catch (YAuthenticationException e) {
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
    }


    /**
     *
     * @param sessionHandle
     * @return
     * @throws RemoteException
     */
    public String checkConnection(String sessionHandle) throws RemoteException {
        try {
            return _userList.checkConnection(sessionHandle);
        } catch (YAuthenticationException e) {
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
    }


    /**
     *
     * @param sessionHandle
     * @return either "<success/>" or "<failure><reason>...</...>"
     */
    public String checkConnectionForAdmin(String sessionHandle) {
        try {
            return _userList.checkConnectionForAdmin(sessionHandle);
        } catch (YAuthenticationException e) {
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
    }


    /**
     * @param specificationID
     * @param taskID
     * @param sessionHandle
     * @return
     * @throws RemoteException
     */
    public String getTaskInformation(String specificationID, String taskID, String sessionHandle) throws RemoteException {
        try {
            _userList.checkConnection(sessionHandle);
        } catch (YAuthenticationException e) {
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
        YTask task = _engine.getTaskDefinition(specificationID, taskID);
        if (task != null) {
            return task.getInformation();
        } else {
            return OPEN_FAILURE + "The was no task found with ID " + taskID + CLOSE_FAILURE;
        }
    }


    /**
     *
     * @param workItemID
     * @param sessionHandle
     * @return either "&lt;success/&gt;" or &lt;failure&gt;&lt;reason&gt;...&lt;/...&gt;
     */
    public String checkElegibilityToAddInstances(String workItemID, String sessionHandle) {
        try {
            _userList.checkConnection(sessionHandle);
            _engine.checkElegibilityToAddInstances(workItemID);
            return SUCCESS;
        } catch (YAWLException e) {
            if (e instanceof YPersistenceException) {
                enginePersistenceFailure = true;
            }
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
    }


    /**
     * Gets a listing of
     * @param sessionHandle
     * @return
     * @throws RemoteException
     */
    public String getSpecificationList(String sessionHandle) throws RemoteException {
        try {
            _userList.checkConnection(sessionHandle);
        } catch (YAuthenticationException e) {
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
        Set specIDs = _engine.getSpecIDs();
        Set specs = new HashSet();
        for (Iterator iterator = specIDs.iterator(); iterator.hasNext();) {
            String specID = (String) iterator.next();
            YSpecification spec = _engine.getSpecification(specID);
            specs.add(spec);
        }
        return getDataForSpecifications(specs);
    }


    /**
     *
     * @param specID specID
     * @param caseParams format &lt;data&gt;[InputParam]*&lt;/data&gt; where
     * InputParam == &lt;varName&gt;var value&lt;/varName&gt;
     * @param sessionHandle
     * @return the case id of the launched case, or a diagnostic <failure/> msg.
     */
    public String launchCase(String specID, String caseParams, URI caseCompletionURI, String sessionHandle) {
        try {
            _userList.checkConnection(sessionHandle);
	    String username = _userList.getUserID(sessionHandle);
            return _engine.launchCase(username, specID, caseParams, caseCompletionURI);
        } catch (YAWLException e) {
            if (e instanceof YPersistenceException) {
                enginePersistenceFailure = true;
            }
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
    }


    /**
     * Given a process specification id return the cases that are its running
     * instances.
     * @param specID the process specification id string.
     * @param sessionHandle the sessionhandle
     * @return a XML message containing a set of caseID elements
     * that are run time instances of the process specification
     * with id = specID
     */
    public String getCasesForSpecification(String specID, String sessionHandle) {
        try {
            _userList.checkConnection(sessionHandle);
            Set caseIDs = _engine.getCasesForSpecification(specID);
            StringBuffer result = new StringBuffer();
            for (Iterator iterator = caseIDs.iterator(); iterator.hasNext();) {
                YIdentifier caseID = (YIdentifier) iterator.next();
                result.append("<caseID>");
                result.append(caseID.toString());
                result.append("</caseID>");
            }
            return result.toString();
        } catch (YAWLException e) {
            if (e instanceof YPersistenceException) {
                enginePersistenceFailure = true;
            }
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
    }


    /**
     * This method returns a complex XML message containing the state of a particular
     * case.  i.e. every token (identifier) and its position in every
     * condition/ task/ and internal condition.
     * @param caseID case id string
     * @param sessionHandle sessionHandle
     * @return XML state Message
     * @throws RemoteException
     */
    public String getCaseState(String caseID, String sessionHandle) throws RemoteException {
        try {
            _userList.checkConnection(sessionHandle);
        } catch (YAuthenticationException e) {
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
        YIdentifier id = _engine.getCaseID(caseID);
        if (id != null) {
            return _engine.getStateForCase(id);
        }
        return OPEN_FAILURE + "Case [" + caseID + "] not found." + CLOSE_FAILURE;
    }


    /**
     * Cancels a running case.
     * @param caseID the caseID string
     * @param sessionHandle sessionHandle
     * @return a diagnostic XML message indicating the result of method call.
     */
    public String cancelCase(String caseID, String sessionHandle) {
        try {
            _userList.checkConnection(sessionHandle);
        } catch (YAuthenticationException e) {
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
        YIdentifier id = _engine.getCaseID(caseID);
        if (id != null) {
            try {
                _engine.cancelCase(id);
                return SUCCESS;
            } catch (YPersistenceException e) {
                enginePersistenceFailure = true;
                return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
            }
        }
        return OPEN_FAILURE + "Case [" + caseID + "] not found." + CLOSE_FAILURE;
    }


    /**
     * Gets the child work items of a given work item id string.
     * @param workItemID
     * @param sessionHandle
     * @return an XML list of elements that describe each child work item.
     * @throws RemoteException
     */
    public String getChildrenOfWorkItem(String workItemID, String sessionHandle) throws RemoteException {
        try {
            _userList.checkConnection(sessionHandle);
        } catch (YAuthenticationException e) {
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
        YWorkItem item = _engine.getWorkItem(workItemID);
        return describeWorkItems(_engine.getChildrenOfWorkItem(item));
    }


    /**
     * Provides an XML list of options for manipulating a work item.  Uses the REST
     * philosophy of showing what you can do with a work item
     * and provides some links to further manipulate the work item.
     * @param workItemID work item id string
     * @param thisURL the url of the engine interface B server (i think).
     * @param sessionHandle the sesssion handle
     * @return a REST style list of options to change the work item under question.
     */
    public String getWorkItemOptions(String workItemID, String thisURL, String sessionHandle) {
        try {
            _userList.checkConnection(sessionHandle);
        } catch (YAuthenticationException e) {
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
        StringBuffer options = new StringBuffer();
        YWorkItem workItem = _engine.getWorkItem(workItemID);
        if (workItem != null) {
            if (workItem.getStatus() == YWorkItem.Status.Executing) {
                options.append("<option operation=\"suspend\">" +
                        "<documentation>Suspend the currently active workItem</documentation>" +
                        "<style>post</style>" +
                        "<url>").append(thisURL).append("?action=suspend</url></option>");
                options.append("<option operation=\"complete\">" +
                        "<documentation>Notify the engine that the work item is complete</documentation>" +
                        "<style>post</style>" + "<url>").append(thisURL).append("?action=complete</url>" +
                        "<bodyContent>Any return data needed for this work item.</bodyContent>" +
                        "</option>");
            }
            try {
                _engine.checkElegibilityToAddInstances(workItemID);
                options.append("<option operation=\"addNewInstance\">" +
                        "<documentation>Add a new Instance similar to this work item</documentation>" +
                        "<style>post</style>" + "<url>").
                        append(thisURL).
                        append("?action=createInstance</url>" +
                                "<bodyContent>The data for the new instance.</bodyContent>" +
                                "</option>");
            } catch (YAWLException e) {
                //just don't provide that option.
                if (e instanceof YPersistenceException) {
                    enginePersistenceFailure = true;
                }
            }
            if (workItem.getStatus() == YWorkItem.Status.Enabled
                    || workItem.getStatus() == YWorkItem.Status.Fired) {
                options.append("<option operation=\"start\">" +
                        "<documentation>Starts a work item</documentation>" +
                        "<style>post</style>" + "<url>").
                        append(thisURL).
                        append("?action=startOne&amp;user=[userID]</url>" +
                                "<returns>The data provided by the engine for " +
                                "processing the work item.</returns>" +
                                "</option>");
            }
        }
        return options.toString();
    }


//    /**
//     * Legacy code.  This class used to be an RMI server.
//     * @throws RemoteException
//     * @throws MalformedURLException
//     */
//    public void registerRMI() throws RemoteException, MalformedURLException {
//        if (System.getSecurityManager() == null) {
//        }
//        URL codeBaseURL = EngineGatewayImpl.class.getResource("EngineGatewayImpl.class");
//        String codeBaseStr = codeBaseURL.toString();
//        codeBaseStr = codeBaseStr.substring(0, codeBaseStr.lastIndexOf("au"));
//        System.setProperty("java.rmi.server.codebase", codeBaseStr);
//        System.out.println("EngineGateway bound");
//    }


    /**
     * Allows the user to load a specificationStr.
     * @param specificationStr a YAWL schema compliant process specificationStr
     * in its entirety, in string format.
     * @param sessionHandle a session handle
     * @return a diagnostic XML message indicating the result of loading the
     * specificationStr.
     */
    public String loadSpecification(String specificationStr, String fileName, String sessionHandle) {
        try {
            _userList.checkConnection(sessionHandle);
        } catch (YAuthenticationException e) {
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
        File temp = new File(fileName);
        if( temp.exists() ) {
        	temp.delete();
        }
        List errorMessages = new ArrayList();
        try {
            FileWriter writer = new FileWriter(temp);
            writer.write(specificationStr);
            writer.flush();
            writer.close();
            _engine.addSpecifications(temp, false, errorMessages);
        } catch (Exception e) {
            if (e instanceof YPersistenceException) {
                enginePersistenceFailure = true;
            }
            //e.printStackTrace();
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        } finally {
        	temp = new File(fileName);
            if( ! temp.delete() )
            	temp.deleteOnExit();
        }
        if (errorMessages.size() > 0) {
            StringBuffer errorMsg = new StringBuffer();
            errorMsg.append(OPEN_FAILURE);
            for (int i = 0; i < errorMessages.size(); i++) {
                YVerificationMessage message = (YVerificationMessage) errorMessages.get(i);
                errorMsg.append("<error>");
                Object src = message.getSource();
                if (src instanceof YTask) {
                    YDecomposition decomp = ((YTask) src).getDecompositionPrototype();
                    if (decomp != null) {
                        errorMsg.append("<src>").
                                append(decomp.getName() != null ?
                                        decomp.getName() : decomp.getId()).
                                append("</src>");
                    }
                }
                errorMsg.append("<message>").
                        append(message.getMessage()).
                        append("</message></error>");
            }
            errorMsg.append(CLOSE_FAILURE);
            return errorMsg.toString();
        } else {
            return SUCCESS;
        }
    }


    /**
     * Unloads the specification.
     * @param specID the specification id string
     * @param sessionHandle session handle
     * @return an XML message indicating the result of the operation.
     */
    public String unloadSpecification(String specID, String sessionHandle) {
        try {
            _userList.checkConnection(sessionHandle);
            _engine.unloadSpecification(specID);
            return SUCCESS;
        } catch (YAWLException e) {
            if (e instanceof YPersistenceException) {
                enginePersistenceFailure = true;
            }
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
    }

    /**
     * Creates a new user in the system.
     * @param userName the name of the user
     * @param password the users elected password.
     * @param isAdmin whther or not the user is to have admin priviledges.
     * @param sessionHandle
     * @return diagnostic XML message.
     */
    public String createUser(String userName, String password, boolean isAdmin, String sessionHandle) {
        try {
            _userList.checkConnectionForAdmin(sessionHandle);
            _userList.addUser(userName, password, isAdmin);
            return SUCCESS;
        } catch (YAWLException e) {
            if (e instanceof YPersistenceException) {
                enginePersistenceFailure = true;
            }
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
    }


    /**
     * Gets the list of users in the system.
     * @param sessionHandle session handle
     * @return an XML message showing each user in the system.
     */
    public String getUsers(String sessionHandle) {
        try {
            _userList.checkConnection(sessionHandle);
        } catch (YAuthenticationException e) {
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
        Set users = _userList.getUsers();
        StringBuffer result = new StringBuffer();
        for (Iterator iterator = users.iterator(); iterator.hasNext();) {
            User user = (User) iterator.next();
            result.append(user.toXML());
        }
        return result.toString();
    }


    /**
     * Returns an XML list (unrooted) of yawlService elements.
     * @param sessionHandle
     * @return either "<success/>" or "<failure><reason>...</...>"
     */
    public String getYAWLServices(String sessionHandle) {
        try {
            _userList.checkConnection(sessionHandle);
        } catch (YAuthenticationException e) {
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
        Set yawlServices = _engine.getYAWLServices();
        StringBuffer result = new StringBuffer();
        for (Iterator iterator = yawlServices.iterator(); iterator.hasNext();) {
            YAWLServiceReference service = (YAWLServiceReference) iterator.next();
            result.append(service.toXML());
        }
        return result.toString();
    }


    /**
     * Gets the documentation associated with the yawl service
     * @param yawlServiceURI
     * @param sessionHandle
     * @return doco as xml (if any) else failure message
     */
    public String getYAWLServiceDocumentation(String yawlServiceURI, String sessionHandle) {
        try {
            _userList.checkConnection(sessionHandle);
        } catch (YAuthenticationException e) {
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
        YAWLServiceReference service = _engine.getRegisteredYawlService(yawlServiceURI);
        if (null != service) {
            if (service.getDocumentation() != null) {
                return service.getDocumentation();
            } else {
                return OPEN_FAILURE +
                        "Yawl service [" + yawlServiceURI + "] has no documentation." +
                        CLOSE_FAILURE;
            }
        } else {
            return  OPEN_FAILURE +
                    "Yawl service [" + yawlServiceURI + "] not found." +
                    CLOSE_FAILURE;
        }
    }


    /**
     * Adds a new YAWL service to the engine
     * @param serviceStr an XML message containing the YAWL service details.
     * @param sessionHandle the session handle
     * @return diagnostic XML message.
     */
    public String addYAWLService(String serviceStr, String sessionHandle) {
        //todo check service heartbeat when we have a heartbeat query.
        try {
            _userList.checkConnection(sessionHandle);
        } catch (YAuthenticationException e) {
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
        YAWLServiceReference service = YAWLServiceReference.unmarshal(serviceStr);
        if (null != service) {
        	YAWLServiceReference ref = _engine.getRegisteredYawlService(service.getURI());
            if (null == ref || !ref.getEnabled()) {
                try {                	
                    _engine.addYawlService(service);
                    return SUCCESS;
                } catch (YPersistenceException e) {
                    enginePersistenceFailure = true;
                    return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
                }
            } else {
                return OPEN_FAILURE +
                        "Engine has already registered a service with the same URI [" +
                        service.toXML() + "]" + "." +
                        CLOSE_FAILURE;
            }
        } else {
            return OPEN_FAILURE +
                    "Failed to parse yawl service from [" +
                    serviceStr + "]" + "." +
                    CLOSE_FAILURE;
        }
    }

    public String removeYAWLService(String serviceURI, String sessionHandle) {
        try {
            _userList.checkConnection(sessionHandle);
        } catch (YAuthenticationException e) {
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
        YAWLServiceReference service = _engine.getRegisteredYawlService(serviceURI);
        if (null != service) {
            try {
                YAWLServiceReference ys = _engine.removeYawlService(serviceURI);
                if (null != ys) {
                    return SUCCESS;
                }
            } catch (YPersistenceException e) {
                enginePersistenceFailure = true;
                return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
            }
        }
        return OPEN_FAILURE +
                "Engine does not contain this YAWL service [ " + serviceURI + " ]." +
                CLOSE_FAILURE;

    }

    public String deleteUser(String userNameToDelete, String sessionHandle) throws RemoteException {
        try {
            _userList.checkConnectionForAdmin(sessionHandle);
            String inSessionUserID = _userList.getUserID(sessionHandle);
            _userList.removeUser(inSessionUserID, userNameToDelete);
            return SUCCESS;
        } catch (YAWLException e) {
            if (e instanceof YPersistenceException) {
                enginePersistenceFailure = true;
            }
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
    }

    public String changePassword(String password, String sessionHandle) throws RemoteException {
        try {
            _userList.checkConnection(sessionHandle);
            String userID = _userList.getUserID(sessionHandle);
            _userList.changePassword(userID, password);
            return SUCCESS;
        } catch (YAWLException e) {
            if (e instanceof YPersistenceException) {
                enginePersistenceFailure = true;
            }
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
    }


    private String describeWorkItems(Set workItems) {
        StringBuffer result = new StringBuffer();
        Iterator iter = workItems.iterator();
        while (iter.hasNext()) {
            YWorkItem workitem = (YWorkItem) iter.next();

            result.append(workitem.toXML());
        }
        return result.toString();
    }


    private String getDataForSpecifications(Set specSet) {
        StringBuffer specs = new StringBuffer();
        for (Iterator iterator = specSet.iterator(); iterator.hasNext();) {
            specs.append("<specificationData>");
            YSpecification spec = (YSpecification) iterator.next();
            specs.append("<id>").
                    append(spec.getID()).
                    append("</id>");
            if (spec.getName() != null) {
                specs.append("<name>").
                        append(spec.getName()).
                        append("</name>");
            }
            if (spec.getDocumentation() != null) {
                specs.append("<documentation>").
                        append(spec.getDocumentation()).
                        append("</documentation>");
            }
            Iterator inputParams = spec.getRootNet().getInputParameters().iterator();
            if (inputParams.hasNext()) {
                specs.append("<params>");
                while (inputParams.hasNext()) {
                    YParameter inputParam = (YParameter) inputParams.next();
                    specs.append(inputParam.toSummaryXML());
                }
                specs.append("</params>");
            }
            specs.append("<rootNetID>").
                    append(spec.getRootNet().getId()).
                    append("</rootNetID>");
            specs.append("<version>").
                    append(spec.getBetaVersion()).
                    append("</version>");

            specs.append("<status>").
                    append(_engine.getLoadStatus(spec.getID())).
                    append("</status>");
            specs.append("</specificationData>");
        }
        return specs.toString();
    }

    /***************************************************************************/

    /** The following methods are called by an Exception Service via Interface_X */

    public String setExceptionObserver(String observerURI) {
       try {
    	   _engine.addExceptionObserver(observerURI);
    	   return SUCCESS;
       } catch (Exception e) {
           return OPEN_FAILURE + "setExceptionObserver failed" + CLOSE_FAILURE ;
    	   
       }                 
    }


    public String removeExceptionObserver() {
       _engine.removeExceptionObservers();
        return SUCCESS;
    }


    public String updateWorkItemData(String workItemID, String data, String sessionHandle) {
        try {
            _userList.checkConnection(sessionHandle);
        } catch (YAuthenticationException e) {
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
        return String.valueOf(_engine.updateWorkItemData(workItemID, data));
    }


    public String updateCaseData(String caseID, String data, String sessionHandle) {
        try {
            _userList.checkConnection(sessionHandle);
        } catch (YAuthenticationException e) {
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
        return String.valueOf(_engine.updateCaseData(caseID, data));
    }


    public String restartWorkItem(String workItemID, String sessionHandle) throws RemoteException {
        try {
            _userList.checkConnection(sessionHandle);
        } catch (YAuthenticationException e) {
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
        String result = "";
        YWorkItem item = _engine.getWorkItem(workItemID);
        if (item != null) {
            item.setStatus(YWorkItem.Status.Enabled);
            result = startWorkItem(workItemID, sessionHandle);
        }
        return result ;
    }


    public String cancelWorkItem(String workItemID, String fail, String sessionHandle)
                                                                 throws RemoteException {
        try {
            _userList.checkConnection(sessionHandle);
            YWorkItem item = _engine.getWorkItem(workItemID);
            _engine.cancelWorkItem(item, fail.equalsIgnoreCase("true")) ;
            return SUCCESS ;
        }
        catch (YAuthenticationException e) {
            return OPEN_FAILURE + formatException( e ) + CLOSE_FAILURE;
        }
    }
    
    private static String formatException( Throwable t ) {
    	StringWriter w = new StringWriter();
    	t.printStackTrace( new PrintWriter( w ) );
    	try {
			return URLEncoder.encode( w.toString(), "UTF-8" );
		}
		catch( UnsupportedEncodingException e ) {
			throw new Error( "UTF-8 encoding should be supported", e );
		}
    }

    /***************************************************************************/
 
}
