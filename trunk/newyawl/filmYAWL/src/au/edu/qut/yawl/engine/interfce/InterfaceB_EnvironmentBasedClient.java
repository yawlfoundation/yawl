/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine.interfce;

import au.edu.qut.yawl.worklist.model.Marshaller;
import au.edu.qut.yawl.worklist.model.TaskInformation;
import au.edu.qut.yawl.worklist.model.WorkItemRecord;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * 
 * @author Lachlan Aldred
 * Date: 27/01/2004
 * Time: 18:50:10
 * 
 */
public class InterfaceB_EnvironmentBasedClient extends Interface_Client {
    private String _backEndURIStr;


    /**
     * Constructor.
     * @param backEndURIStr the back end uri of where to find
     * the engine.  A default deployment this value is
     * http://131.181.70.9:8080/yawl/ib
     */
    public InterfaceB_EnvironmentBasedClient(String backEndURIStr) {
        _backEndURIStr = backEndURIStr;
    }


    /**
     * Connects the user to the engine.
     * @param userID the user id.
     * @param password the user password
     * @return the session handle
     * @throws IOException  if engine cannot be found
     */
    public String connect(String userID, String password) throws IOException {
        Map queryMap = new HashMap();
        queryMap.put("userid", userID);
        queryMap.put("password", password);
        return executePost(_backEndURIStr + "/connect", queryMap);
    }


    /**
     * Returns a list of WorkItemRecord objects. All the work items that are
     * currently active inside the engine.
     * @see au.edu.qut.yawl.worklist.model.WorkItemRecord
     * @param sessionHandle the session handle
     * @return the list of workitem objects
     * @throws IOException if engine can't be found.
     * @throws JDOMException
     */
    public List getCompleteListOfLiveWorkItems(String sessionHandle) throws IOException, JDOMException {
        String result = null;

        result = executeGet(_backEndURIStr +
                "?action=verbose&sessionHandle=" + sessionHandle);

        SAXBuilder builder = new SAXBuilder();
        List workItems = new ArrayList();

        if (result != null && successful(result)) {
            Document doc = builder.build(new StringReader(result));
            Iterator workItemEls = doc.getRootElement().getChildren().iterator();
            while (workItemEls.hasNext()) {
                Element workItemElement = (Element) workItemEls.next();
                WorkItemRecord workItem = Marshaller.unmarshalWorkItem(workItemElement);
                workItems.add(workItem);
            }
        }
        return workItems;
    }


    /**
     * Creates a list of SpecificationData objects loaded into the engine.
     * These are brief meta data summary
     * information objects that describe a worklfow specification.
     * @param sessionHandle
     * @return  the list of spec data objects
     * @throws IOException if engine can't be found.
     */
    public List getSpecificationList(String sessionHandle) throws IOException {
        String result = null;

        result = executeGet(_backEndURIStr +
                "?action=getSpecificationPrototypesList" +
                "&" +
                "sessionHandle=" + sessionHandle);

        List specList = Marshaller.unmarshalSpecificationSummary(result);
        return specList;
    }


    /**
     * Gets an XML representation of a workflow specification.
     * @param specID the specid.
     * @param sessionHandle the session handle
     * @return the XML representation, or an XML diagnostic error message.
     * @throws IOException if the engine can't be found.
     */
    public String getSpecification(String specID, String sessionHandle) throws IOException {

        return stripOuterElement(executeGet(_backEndURIStr +
                "?action=getSpecification" +
                "&" +
                "specID=" + specID +
                "&" +
                "sessionHandle=" + sessionHandle));

    }


    /**
     * Allows clients to obtain ownership of a unit of work.  This means that the
     * workitem must be enabled or fired first, and that upon successful checkout the
     * workitem will be exectuing.
     * @param workItemID the workitem id.
     * @param sessionHandle the sessionhandle
     * @return in case of success returns an XML representation of the created
     * workitem.  In case of failure returns a diagnostic XML message.
     * @throws IOException
     */
    public String checkOutWorkItem(String workItemID, String sessionHandle) throws IOException {
        HashMap params = new HashMap();
        params.put("sessionHandle", sessionHandle);
        params.put("action", "checkout");
        return executePost(_backEndURIStr + "/workItem/" + workItemID, params);
    }


    /**
     * Gets an XML representation of information the task declaration.
     * This can be parsed into a copy of a YTask by using the
     * @param specificationID the spec id.
     * @param taskID the task id.
     * @param sessionHandle the session handle
     * @return an XML Representation of the task information
     * @throws IOException
     */
    public String getTaskInformationStr(String specificationID, String taskID, String sessionHandle) throws IOException {
        String msg = null;
        msg = executeGet(
                _backEndURIStr + "/task/" + taskID +
                "?" +
                "action=taskInformation" +
                "&" +
                "specID=" + specificationID +
                "&" +
                "sessionHandle=" + sessionHandle);
        return msg;
    }


    /**
     * Checks whether the connection with the engine is alive, authenticated
     * properly.
     * @param sessionHandle the session handle
     * @return a diagnostic message indicating connection state.
     * @throws IOException if engine cannot be found.
     */
    public String checkConnection(String sessionHandle) throws IOException {
        return executeGet(_backEndURIStr + "?" +
                "action=checkConnection" +
                "&" +
                "sessionHandle=" + sessionHandle);

    }


    /**
     * Checks the work item back into the engine once the task is complete.
     * Succesfully doing so will cause the work item to be complete.
     * @param workItemID the work item id.
     * @param data formated data eg. <data><param1Name>value</param1Name></data>
     * @param sessionHandle the session handle
     * @return in case success returns the work item as xml. In case of failure
     * returns the reason for failure.
     * @throws IOException
     */
    public String checkInWorkItem(String workItemID, String data, String sessionHandle) throws IOException {
        //todo make the data param into an first class api object
        HashMap params = new HashMap();
        params.put("sessionHandle", sessionHandle);
        params.put("data", data);
        params.put("action", "checkin");
        String msg = executePost(_backEndURIStr + "/workItem/" + workItemID, params);
        return msg;
    }


    /**
     * Determines whether or not a task will allow a dynamically
     * created new instance to be created.  MultiInstance Task with
     * dyanmic instance creation.
     * @param workItemID the workItemID of a sibling work item.
     * @param sessionHandle the session handle
     * @return diagnostic message that should indicate permission
     * if task is MultiInstance, and
     * if task allows dynamic instance creation,
     * and if current number of instances is less than the maxInstances
     * for the task.
     * @throws IOException if engine cannot be found
     * if task does not allow dynamic instance creation,
     * or if current number of instances is not less than the maxInstances
     * for the task.
     */
    public String checkPermissionToAddInstances(String workItemID, String sessionHandle) throws IOException {
        return executeGet(_backEndURIStr + "/workItem/" + workItemID +
                "?" +
                "action=checkAddInstanceEligible" +
                "&" +
                "sessionHandle=" + sessionHandle);
    }


    /**
     * Creates a new instance of a multi instance task.
     * @param workItemID the work item id
     * @param paramValueForMICreation the data needed for creating a new instance.
     * @param sessionHandle the session handle
     * @return diagnostic string indicating result of action
     * @throws IOException if engine cannot be found.
     */
    public String createNewInstance(String workItemID,
                                    String paramValueForMICreation,
                                    String sessionHandle) throws IOException {
        Map paramsMap = new HashMap();
        paramsMap.put("sessionHandle", sessionHandle);
        paramsMap.put("action", "createInstance");
        paramsMap.put("paramValueForMICreation", paramValueForMICreation);
        return executePost(_backEndURIStr + "/workItem/" + workItemID,
                paramsMap);
    }


    /**
     * Suspneds a work item.
     * @param workItemID the work item id.
     * @param sessionHandle the sessoin handle
     * @return diagnostic XML message
     * @throws IOException if the engine can't be found.
     */
    public String suspendWorkItem(String workItemID, String sessionHandle) throws IOException {
         Map paramsMap = new HashMap();
        paramsMap.put("sessionHandle", sessionHandle);
        paramsMap.put("action", "suspend");
        return executePost(_backEndURIStr + "/workItem/" + workItemID,
                paramsMap);
    }


    /**
     * Rolls back a work item from executing to fired.
     * @param workItemID the work item id.
     * @param sessionHandle the sessoin handle
     * @return diagnostic XML message
     * @throws IOException if the engine can't be found.
     */
    public String rollbackWorkItem(String workItemID, String sessionHandle) throws IOException {
        Map paramsMap = new HashMap();
        paramsMap.put("sessionHandle", sessionHandle);
        paramsMap.put("action", "rollback");
        return executePost(_backEndURIStr + "/workItem/" + workItemID,
                paramsMap);
    }


    /**
     * Launches the case.
     * @param specID the specification id (see SpecificationData.getID())
     * @param caseParams the case params in XML. i.e.
     * <pre>
     *    &lt;data&gt;
     *        &lt;firstParam&gt;value&lt;/firstParam&gt;
     *        &lt;secondParam&gt;value&lt;/secondParam&gt;
     *    &lt;/data&gt;
     * </pre>
     * If there are no params then just pass in null.
     * @param sessionHandle the session handle
     * @return returns a diagnostic message in case of failure
     * @throws IOException if engine can't be found
     */
    public String launchCase(String specID, String caseParams, String sessionHandle) throws IOException {
        Map paramsMap = new HashMap();
        paramsMap.put("sessionHandle", sessionHandle);
        paramsMap.put("action", "launchCase");
        if (caseParams != null) {
            paramsMap.put("caseParams", caseParams);
        }
        return executePost(_backEndURIStr + "/specID/" + specID,
                paramsMap);
    }
    

    /** 
     * Override of launchCase to provide the ability to add a listener
     * for the Case-Completion event (MJA 06/12/05)
     *
     * @param specID the specification id (see SpecificationData.getID())
     * @param caseParams the case params in XML. 
     * @param sessionHandle the session handle
     * @param completionObserverURI the URI of the IB service that will listen
     *        for a case-completed event
     * @return returns a diagnostic message in case of failure
     * @throws IOException if engine can't be found
     */
    public String launchCase(String specID, String caseParams, 
                             String sessionHandle, String completionObserverURI) 
                                     throws IOException {
        Map paramsMap = new HashMap();
        paramsMap.put("sessionHandle", sessionHandle);
        paramsMap.put("action", "launchCase");
        if (caseParams != null) 
            paramsMap.put("caseParams", caseParams);
        if (completionObserverURI != null)    
            paramsMap.put("completionObserverURI", completionObserverURI);

        return executePost(_backEndURIStr + "/specID/" + specID, paramsMap);
    }



    /**
     * Gets the set of active cases in the engine.
     * @param specID the specification id.
     * @param sessionHandle
     * @return an XML list of case ids that are instances of the spec
     * with specid.
     * @throws IOException if engine cannot be found
     */
    public String getCases(String specID, String sessionHandle) throws IOException {
        return executeGet(_backEndURIStr + "/specID/" + specID +
                "?" +
                "action=getCasesForSpecification" +
                "&" +
                "sessionHandle=" + sessionHandle);
    }


    /**
     * Gets the state description of the case
     * @param caseID the case id.
     * @param sessionHandle the session handle
     * @return An XML representation of the case state, or a diagnostic error
     * message.
     * @throws IOException if engine cannot be found
     */
    public String getCaseState(String caseID, String sessionHandle) throws IOException {
        return stripOuterElement(executeGet(_backEndURIStr + "/caseID/" + caseID +
                "?" +
                "action=getState" +
                "&" +
                "sessionHandle=" + sessionHandle));
    }


    /**
     * Cancels the case with caseID.
     * @param caseID the case id.
     * @param sessionHandle the session handle
     * @return a diagnostic string providing information about the
     * result of cancellation.
     * @throws IOException if the engine cannot be found.
     */
    public String cancelCase(String caseID, String sessionHandle) throws IOException {
        Map params = new HashMap();
        params.put("action", "cancelCase");
        params.put("sessionHandle", sessionHandle);
        String result = executePost(_backEndURIStr + "/caseID/" + caseID, params);
        return result;
    }


    /**
     * Finds out the children of a given work item. Only work item that are parents
     * have children.  See YAWL paper and other documentation - good luck.
     * @param workItemID the work item id.
     * @param sessionHandle the session handle
     * @return a Java.util.List of WorkItemRecord objects.
     */
    public List getChildrenOfWorkItem(String workItemID, String sessionHandle) {
        String result = null;
        try {
            result = executeGet(_backEndURIStr + "/" + workItemID +
                    "?action=getChildren&sessionHandle=" + sessionHandle);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SAXBuilder builder = new SAXBuilder();
        List workItems = new ArrayList();
        try {
            if (result != null && successful(result)) {
                Document doc = builder.build(new StringReader(result));
                Iterator workItemEls = doc.getRootElement().getChildren().iterator();
                while (workItemEls.hasNext()) {
                    Element workItemElement = (Element) workItemEls.next();
                    WorkItemRecord workItem = Marshaller.unmarshalWorkItem(workItemElement);
                    workItems.add(workItem);
                }
            }
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return workItems;
    }


    /**
     * Parses the XML string and returns a TaskInfo object
     * (easier to manage).
     * @param taskInfoStr  the task information String
     * @see this.getTaskInformationStr
     * @return the TaskInformation object
     */
    public static TaskInformation parseTaskInformation(String taskInfoStr) {
        TaskInformation taskInfo = null;
        //strip off extraneous <response> tag
        int beginClipping = taskInfoStr.indexOf(">") + 1;
        int endClipping = taskInfoStr.lastIndexOf("<");
        if (beginClipping >= 0 && endClipping >= 0) {
            taskInfoStr = taskInfoStr.substring(beginClipping, endClipping);
        }
        if (successful(taskInfoStr)) {
            taskInfo = Marshaller.unmarshalTaskInformation(taskInfoStr);
        }
        return taskInfo;
    }


    public boolean isAdministrator(String sessionHandle) throws IOException {
        String result = executeGet(_backEndURIStr +
                "?action=checkIsAdmin&sessionHandle=" + sessionHandle);
        return result.indexOf("administrator") != -1;
    }
}
