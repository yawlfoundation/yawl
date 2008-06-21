/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.engine.interfce.interfaceB;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.*;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.io.IOException;
import java.util.*;

/**
 * 
 * @author Lachlan Aldred
 * Date: 27/01/2004
 * Time: 18:50:10
 *
 * Refactored for v2.0 by Michael Adams
 * 
 */

public class InterfaceB_EnvironmentBasedClient extends Interface_Client {
    private String _backEndURIStr;


    /**
     * Constructor.
     * @param backEndURIStr the back end uri of where to find
     * the engine.  A default deployment this value is
     * http://localhost:8080/yawl/ib
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
        Map<String, String> params = prepareParamMap("connect", null);
        params.put("userid", userID);
        params.put("password", password);
        return executePost(_backEndURIStr, params);
    }


    /**
     * Returns a list of WorkItemRecord objects. All the work items that are
     * currently active inside the engine.
     * @see org.yawlfoundation.yawl.engine.interfce.WorkItemRecord
     * @param sessionHandle the session handle
     * @return the list of workitem objects
     * @throws IOException if engine can't be found.
     * @throws JDOMException
     */
    public List<WorkItemRecord> getCompleteListOfLiveWorkItems(String sessionHandle)
            throws IOException {
        return unPackWorkItemList(getCompleteListOfLiveWorkItemsAsXML(sessionHandle));
    }


    public String getCompleteListOfLiveWorkItemsAsXML(String sessionHandle)
               throws IOException {
        return executeGet(_backEndURIStr, prepareParamMap("getLiveItems", sessionHandle));
    }


    /**
     * Retrieves a List of live workitems for the case or spec id passed
     * @param idType : "case" for a case's workitems, "spec" for a specification's,
     *        "task" for a specific taskID
     * @param id the identifier for the case/spec/task
     * @param sessionHandle the session handle
     * @return the List of live workitems
     * @throws IOException if there's a problem connecting to the engine
     * @throws JDOMException if there's a problem with xml conversions
     */
    public List<WorkItemRecord> getLiveWorkItemsForIdentifier(String idType, String id,
                               String sessionHandle) throws IOException, JDOMException {
        ArrayList<WorkItemRecord> result = new ArrayList<WorkItemRecord>() ;
        List<WorkItemRecord> wirs = getCompleteListOfLiveWorkItems(sessionHandle) ;

        if (wirs != null) {

            // find out which wirs belong to the specified case/spec/task
            for (WorkItemRecord wir : wirs) {
                if ((idType.equalsIgnoreCase("spec") &&
                       wir.getSpecificationID().equals(id)) ||
                    (idType.equalsIgnoreCase("case") &&
                       (wir.getCaseID().equals(id) ||
                        wir.getCaseID().startsWith(id + "."))) ||
                    (idType.equalsIgnoreCase("task") &&
                        wir.getTaskID().equals(id)))
                  result.add(wir);
            }
        }
        if (result.isEmpty()) result = null ;
        return result ;
    }

    public String getLiveWorkItemsForIdentifierAsXML(String idType, String id,
                         String sessionHandle) throws IOException, JDOMException {
        List<WorkItemRecord> wirList = getLiveWorkItemsForIdentifier(idType, id,
                                                                     sessionHandle) ;
        if (! wirList.isEmpty()) {
            StringBuilder xml = new StringBuilder("<itemrecords>");
            for (WorkItemRecord wir : wirList) {
                xml.append(wir.toXML());
            }
            xml.append("</itemrecords>");
            return xml.toString();
        }
        return null ;
    }


    /**
     * Creates a list of SpecificationData objects loaded into the engine.
     * These are brief meta data summary
     * information objects that describe a worklfow specification.
     * @param sessionHandle
     * @return  the list of spec data objects
     * @throws IOException if engine can't be found.
     */
    public List<SpecificationData> getSpecificationList(String sessionHandle)
            throws IOException {
        String result = executeGet(_backEndURIStr,
                        prepareParamMap("getSpecificationPrototypesList", sessionHandle));

        return Marshaller.unmarshalSpecificationSummary(result);
    }


    /**
     * Gets an XML representation of a workflow specification.
     * @param specID the specid.
     * @param sessionHandle the session handle
     * @return the XML representation, or an XML diagnostic error message.
     * @throws IOException if the engine can't be found.
     */
    public String getSpecification(String specID, String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("getSpecification", sessionHandle);
        params.put("specID", specID) ;
        return stripOuterElement(executeGet(_backEndURIStr, params));
    }

    
    /** this overload is to handle YSpecificationID objects */
    public String getSpecification(YSpecificationID specID, String sessionHandle)
                                                                 throws IOException {
        Map<String, String> params = prepareParamMap("getSpecification", sessionHandle);
        params.put("specID", specID.getSpecName()) ;
        params.put("version", specID.getVersion().toString());
        return stripOuterElement(executeGet(_backEndURIStr, params));
    }


    public String getSpecificationDataSchema(String specID, String sessionHandle)
                                                                 throws IOException {
        Map<String, String> params = prepareParamMap("getSpecificationDataSchema",
                                                      sessionHandle);
        params.put("specID", specID) ;
        return stripOuterElement(executeGet(_backEndURIStr, params));
    }


    /** this overload is to handle YSpecificationID objects */
    public String getSpecificationDataSchema(YSpecificationID specID, String sessionHandle)
                                                                 throws IOException {
        Map<String, String> params = prepareParamMap("getSpecificationDataSchema",
                                                      sessionHandle);
        params.put("specID", specID.getSpecName()) ;
        params.put("version", specID.getVersion().toString());
        return stripOuterElement(executeGet(_backEndURIStr, params));
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
        Map<String, String> params = prepareParamMap("checkout", sessionHandle);
        params.put("workItemID", workItemID);
        return executePost(_backEndURIStr, params);
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
    public String getTaskInformationStr(String specificationID, String taskID,
                                        String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("taskInformation", sessionHandle);
        params.put("specID", specificationID);
        params.put("taskID", taskID);
        return executeGet(_backEndURIStr, params);
    }


    /** this overload handles YSpecificationID objects */
    public String getTaskInformationStr(YSpecificationID specificationID, String taskID,
                                        String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("taskInformation", sessionHandle);
        params.put("specID", specificationID.getSpecName());
        params.put("version", specificationID.getVersion().toString());
        params.put("taskID", taskID);
        return executeGet(_backEndURIStr, params);
    }


    /**
     * Checks whether the connection with the engine is alive, authenticated
     * properly.
     * @param sessionHandle the session handle
     * @return a diagnostic message indicating connection state.
     * @throws IOException if engine cannot be found.
     */
    public String checkConnection(String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("checkConnection", sessionHandle);
        return executeGet(_backEndURIStr, params);
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
    public String checkInWorkItem(String workItemID, String data, String sessionHandle)
            throws IOException {
        Map<String, String> params = prepareParamMap("checkin", sessionHandle);
        params.put("data", data);
        params.put("workItemID", workItemID);
        return executePost(_backEndURIStr, params);
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
    public String checkPermissionToAddInstances(String workItemID, String sessionHandle)
            throws IOException {
        Map<String, String> params = prepareParamMap("checkAddInstanceEligible",
                                                      sessionHandle);
        params.put("workItemID", workItemID);
        return executeGet(_backEndURIStr, params);
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
        Map<String, String> params = prepareParamMap("createInstance", sessionHandle);
        params.put("paramValueForMICreation", paramValueForMICreation);
        params.put("workItemID", workItemID);
        return executePost(_backEndURIStr, params);
    }


    /**
     * Skips a work item.
     * @param workItemID the work item id.
     * @param sessionHandle the sessoin handle
     * @return diagnostic XML message
     * @throws IOException if the engine can't be found.
     */
    public String skipWorkItem(String workItemID, String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("skip", sessionHandle);
        params.put("workItemID", workItemID);
        return executePost(_backEndURIStr, params);
    }


    /**
     * Suspends a work item.
     * @param workItemID the work item id.
     * @param sessionHandle the sessoin handle
     * @return diagnostic XML message
     * @throws IOException if the engine can't be found.
     */
    public String suspendWorkItem(String workItemID, String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("suspend", sessionHandle);
        params.put("workItemID", workItemID);
        return executePost(_backEndURIStr, params);
    }


    /**
      * Unuspends a work item.
      * @param workItemID the work item id.
      * @param sessionHandle the sessoin handle
      * @return diagnostic XML message
      * @throws IOException if the engine can't be found.
      */
     public String unsuspendWorkItem(String workItemID, String sessionHandle) throws IOException {
         Map<String, String> params = prepareParamMap("unsuspend", sessionHandle);
         params.put("workItemID", workItemID);
         return executePost(_backEndURIStr, params);
     }


    /**
     * Rolls back a work item from executing to fired.
     * @param workItemID the work item id.
     * @param sessionHandle the sessoin handle
     * @return diagnostic XML message
     * @throws IOException if the engine can't be found.
     */
    public String rollbackWorkItem(String workItemID, String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("rollback", sessionHandle);
        params.put("workItemID", workItemID);
        return executePost(_backEndURIStr, params);
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
    public String launchCase(String specID, String caseParams, String sessionHandle)
            throws IOException {
        Map<String, String> params = prepareParamMap("launchCase", sessionHandle);
        params.put("specID", specID);
        if (caseParams != null) params.put("caseParams", caseParams);
        return executePost(_backEndURIStr, params);
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
        Map<String, String> params = prepareParamMap("launchCase", sessionHandle);
        params.put("specID", specID);
        if (caseParams != null) params.put("caseParams", caseParams);
        if (completionObserverURI != null)    
            params.put("completionObserverURI", completionObserverURI);
        return executePost(_backEndURIStr, params);
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
        Map<String, String> params = prepareParamMap("getCasesForSpecification",
                                                      sessionHandle);
        params.put("specID", specID);
        return executeGet(_backEndURIStr, params);
    }


    /** and an overload for YSpecification versions */
    public String getCases(YSpecificationID specID, String sessionHandle)
                                                                 throws IOException {
        Map<String, String> params = prepareParamMap("getCasesForSpecification",
                                                      sessionHandle);
        params.put("specID", specID.getSpecName());
        params.put("version", specID.getVersion().toString());
        return executeGet(_backEndURIStr, params);
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
        Map<String, String> params = prepareParamMap("getCaseState", sessionHandle);
        params.put("caseID", caseID);
        return stripOuterElement(executeGet(_backEndURIStr, params));
    }


    /**
     * Gets the data of the case
     * @param caseID the case id.
     * @param sessionHandle the session handle
     * @return An XML representation of the case data, or a diagnostic error
     * message.
     * @throws IOException if engine cannot be found
     */
    public String getCaseData(String caseID, String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("getCaseData", sessionHandle);
        params.put("caseID", caseID);
        return stripOuterElement(executeGet(_backEndURIStr, params));
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
        Map<String, String> params = prepareParamMap("cancelCase", sessionHandle);
        params.put("caseID", caseID);
        return executePost(_backEndURIStr, params);
    }


    /**
     * Finds out the children of a given work item. Only work item that are parents
     * have children.  See YAWL paper and other documentation - good luck.
     * @param workItemID the work item id.
     * @param sessionHandle the session handle
     * @return a Java.util.List of WorkItemRecord objects.
     * @throws IOException if the engine cannot be found.
     */
    public List<WorkItemRecord> getChildrenOfWorkItem(String workItemID,
                                               String sessionHandle) throws IOException{
        Map<String, String> params = prepareParamMap("getChildren", sessionHandle);
        params.put("workItemID", workItemID);
        return unPackWorkItemList(executeGet(_backEndURIStr, params));
    }


    /**
     * Parses the XML string and returns a TaskInfo object
     * (easier to manage).
     * @param taskInfoStr  the task information String
     * @see this.getTaskInformationStr
     * @return the TaskInformation object
     */
    public static TaskInformation parseTaskInformation(String taskInfoStr) {
        taskInfoStr = stripOuterElement(taskInfoStr);
        return successful(taskInfoStr) ?
               Marshaller.unmarshalTaskInformation(taskInfoStr) : null ;
    }


    /**
     * Checks if the session has administrative access
     * @param sessionHandle the session to check
     * @return true if this session has administration privileges
     * @throws IOException
     */
    public boolean isAdministrator(String sessionHandle) throws IOException {
        String result = executeGet(_backEndURIStr, prepareParamMap("checkIsAdmin",
                                                                   sessionHandle));
        return (result.indexOf("administrator") != -1);
    }

    /**
     * Gets an XML representation of information the task declaration.
     * This can be parsed into a copy of a YTask by using the
     * @param specID the spec id.
     * @param taskID the task id.
     * @param sessionHandle the session handle
     * @return an XML Representation of the task information
     * @throws IOException
     */
    public String getMITaskAttributes(String specID, String taskID,
                                      String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("getMITaskAttributes", sessionHandle);
        params.put("taskID", taskID);
        params.put("specID", specID);
        return executeGet(_backEndURIStr, params);
    }

    /**
     * Gets the set of resourcing specifications for the specified spec and task
     * @param specID the specification id
     * @param taskID the id of the task to get the resourcing specs for
     * @param sessionHandle the session handle
     * @return an XML Representation of the resourcing information for the task
     * @throws IOException
     */
    public String getResourcingSpecs(String specID, String taskID,
                                      String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("getResourcingSpecs", sessionHandle);
        params.put("taskID", taskID);
        params.put("specID", specID);
        String result = executeGet(_backEndURIStr, params);
        return (successful(result)) ? stripOuterElement(result) : result ;
    }


    private List<WorkItemRecord> unPackWorkItemList(String xml) {
        List<WorkItemRecord> result = new ArrayList<WorkItemRecord>();
        if (xml != null && successful(xml)) {
            Document doc = JDOMUtil.stringToDocument(xml);
            if (doc != null) {
                Iterator itr = doc.getRootElement().getChildren().iterator();
                while (itr.hasNext()) {
                    Element item = (Element) itr.next();
                    result.add(Marshaller.unmarshalWorkItem(item));
                }
            }
        }
        return result;
    }

}
