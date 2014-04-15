/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.engine.interfce.interfaceB;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.*;
import org.yawlfoundation.yawl.logging.YLogDataItemList;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.PasswordEncryptor;

import javax.xml.datatype.Duration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * An API for custom services to call Engine functionalities regarding workitem
 * management, process progression and case state.
 *
 * @author Lachlan Aldred
 * @author Michael Adams (refactored for v2.0)
 * @date 27/01/2004
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

    public String getBackEndURI() { return _backEndURIStr; }


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
        params.put("password", PasswordEncryptor.encrypt(password, null));
        return executePost(_backEndURIStr, params);
    }


    /**
     * Disconnects an external entity from the engine
     * @param handle the sessionHandle to disconnect
     * @throws IOException if the engine can't be reached
     */
    public String disconnect(String handle) throws IOException {
        return executePost(_backEndURIStr, prepareParamMap("disconnect", handle));
    }


    /**
     * Returns a list (of WorkItemRecord) of all the work items that are
     * currently active in the engine.
     * @see org.yawlfoundation.yawl.engine.interfce.WorkItemRecord
     * @param sessionHandle the session handle
     * @return the list of WorkItemRecord objects
     * @throws IOException if engine can't be found.
     */
    public List<WorkItemRecord> getCompleteListOfLiveWorkItems(String sessionHandle)
            throws IOException {
        return unPackWorkItemList(getCompleteListOfLiveWorkItemsAsXML(sessionHandle));
    }

    /**
     * Returns an XML string describing all the work items that are
     * currently active in the engine.
     * @param sessionHandle the session handle
     * @return an XML representation of the set of live workitems
     * @throws IOException if engine can't be found.
     */
    public String getCompleteListOfLiveWorkItemsAsXML(String sessionHandle)
               throws IOException {
        return executeGet(_backEndURIStr, prepareParamMap("getLiveItems", sessionHandle));
    }


    /**
     * Returns an XML string describing a current work item.
     * @param itemID the workitem id
     * @param sessionHandle the session handle
     * @return an XML representation of the of workitem
     * @throws IOException if engine can't be found.
     */
    public String getWorkItem(String itemID, String sessionHandle)
               throws IOException {
        Map<String, String> params = prepareParamMap("getWorkItem", sessionHandle);
        params.put("workItemID", itemID) ;
        return executeGet(_backEndURIStr, params);
    }
    
    
    /**
     * Returns the expiry time of a work item's timer.
     * @param itemID the workitem id
     * @param sessionHandle the session handle
     * @return a long value representing the moment the timer will expire, or 0 if
     * the work item does not have a timer or does not exist
     * @throws IOException if engine can't be found.
     */
    public long getWorkItemExpiryTime(String itemID, String sessionHandle)
               throws IOException {
        Map<String, String> params = prepareParamMap("getWorkItemExpiryTime", sessionHandle);
        params.put("workItemID", itemID) ;
        String expiryTime = stripOuterElement(executeGet(_backEndURIStr, params));
        return (successful(expiryTime)) ? Long.valueOf(expiryTime) : 0;
    }
    


    /**
     * Returns the current set of active workitems for a case
     * @param caseID the case in question
     * @param sessionHandle the session handle
     * @return a list of live workitems (as WorkItemRecords)
     * @throws IOException if engine can't be found.
     */
    public List<WorkItemRecord> getWorkItemsForCase(String caseID, String sessionHandle)
            throws IOException {
        Map<String, String> params = prepareParamMap("getWorkItemsWithIdentifier", sessionHandle);
        params.put("id", caseID) ;
        params.put("idType", "case");
        return unPackWorkItemList(executeGet(_backEndURIStr, params));
    }


    /**
     * Returns the current set of active workitems for a specification
     * @param specName the specification in question
     * @param sessionHandle the session handle
     * @return a list of live workitems (as WorkItemRecords)
     * @throws IOException if engine can't be found.
     */
    public List<WorkItemRecord> getWorkItemsForSpecification(String specName, String sessionHandle)
            throws IOException {
        Map<String, String> params = prepareParamMap("getWorkItemsWithIdentifier", sessionHandle);
        params.put("id", specName) ;
        params.put("idType", "spec");
        return unPackWorkItemList(executeGet(_backEndURIStr, params));
    }


    /**
     * Returns the current set of active workitems that are instances of a specified task
     * @param taskID the task in question
     * @param sessionHandle the session handle
     * @return a list of live workitems (as WorkItemRecords)
     * @throws IOException if engine can't be found.
     */
    public List<WorkItemRecord> getWorkItemsForTask(String taskID, String sessionHandle)
            throws IOException {
        Map<String, String> params = prepareParamMap("getWorkItemsWithIdentifier", sessionHandle);
        params.put("id", taskID) ;
        params.put("idType", "task");
        return unPackWorkItemList(executeGet(_backEndURIStr, params));
    }


    /**
     * Retrieves a List of live workitems for the case or spec id passed
     * @param idType : "case" for a case's workitems, "spec" for a specification's,
     *        "task" for a specific taskID
     * @param id the identifier for the case/spec/task
     * @param sessionHandle the session handle
     * @return the List of live workitems (as WorkItemRecords)
     * @throws IOException if there's a problem connecting to the engine
     * @throws JDOMException if there's a problem with xml conversions
     */
    public List<WorkItemRecord> getLiveWorkItemsForIdentifier(String idType, String id,
                               String sessionHandle) throws IOException, JDOMException {
        if (idType.equalsIgnoreCase("spec")) {
            return getWorkItemsForSpecification(id, sessionHandle);
        }
        else if (idType.equalsIgnoreCase("case")) {
            return getWorkItemsForCase(id, sessionHandle);
        }
        else if (idType.equalsIgnoreCase("task")) {
            return getWorkItemsForTask(id, sessionHandle);
        }
        else return null;
    }


    /**
     * Returns the current set of active workitems that are associated with a specified
     * custom service
     * @param serviceURI the uri of the service in question
     * @param sessionHandle the session handle
     * @return a list of live workitems (as WorkItemRecords)
     * @throws IOException if engine can't be found.
     */
    public List<WorkItemRecord> getWorkItemsForService(String serviceURI, String sessionHandle)
            throws IOException {
        Map<String, String> params = prepareParamMap("getWorkItemsForService", sessionHandle);
        params.put("serviceuri", serviceURI) ;
        return unPackWorkItemList(executeGet(_backEndURIStr, params));
    }

    
    /**
     * Retrieves an XML description of live workitems for the case or spec id passed
     * @param idType : "case" for a case's workitems, "spec" for a specification's,
     *        "task" for a specific taskID
     * @param id the identifier for the case/spec/task
     * @param sessionHandle the session handle
     * @return the XML representation of live workitems in the engine
     * @throws IOException if there's a problem connecting to the engine
     * @throws JDOMException if there's a problem with xml conversions
     */
    public String getLiveWorkItemsForIdentifierAsXML(String idType, String id,
                         String sessionHandle) throws IOException, JDOMException {
        List<WorkItemRecord> wirList = getLiveWorkItemsForIdentifier(idType, id,
                                                                     sessionHandle) ;
        if (wirList != null) {
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
     * Creates a list of SpecificationData objects for the specifications currently
     * loaded into the engine. These are brief meta data summary
     * information objects that describe a worklfow specification.
     * @param sessionHandle the session handle
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
     * @deprecated superseded by getSpecification(YSpecificationID, String)
     * @param specID the specid.
     * @param sessionHandle the session handle
     * @return the XML representation, or an XML diagnostic error message.
     * @throws IOException if the engine can't be found.
     */
    public String getSpecification(String specID, String sessionHandle) throws IOException {
        return getSpecification(new YSpecificationID(specID), sessionHandle);
    }

    
    /**
     * Gets an XML representation of a workflow specification.
     * @param specID the specid.
     * @param sessionHandle the session handle
     * @return the XML representation, or an XML diagnostic error message.
     * @throws IOException if the engine can't be found.
     */
    public String getSpecification(YSpecificationID specID, String sessionHandle)
                                                                 throws IOException {
        Map<String, String> params = prepareParamMap("getSpecification", sessionHandle);
        params.putAll(specID.toMap());
        return stripOuterElement(executeGet(_backEndURIStr, params));
    }


    /**
     * Gets an XML representation of a workflow specification for a specified, currently
     * executing case.
     * @param caseID the identifier of a currently executing case.
     * @param sessionHandle the session handle
     * @return the XML representation, or an XML diagnostic error message.
     * @throws IOException if the engine can't be found.
     */
    public String getSpecificationForCase(String caseID, String sessionHandle)
                                                                 throws IOException {
        Map<String, String> params = prepareParamMap("getSpecificationForCase", sessionHandle);
        params.put("caseID", caseID);
        return stripOuterElement(executeGet(_backEndURIStr, params));
    }


    /**
     * Gets the user-defined data schema for a specification
     * @deprecated superseded by getSpecificationDataSchema(YSpecificationID, String)
     * @param specID the specification id
     * @param sessionHandle an active session handle
     * @return an XML representation, or an XML diagnostic error message.
     * @throws IOException if the engine can't be found.
     */
    public String getSpecificationDataSchema(String specID, String sessionHandle)
                                                                 throws IOException {
        return getSpecificationDataSchema(new YSpecificationID(specID), sessionHandle);
    }


    /**
     * Gets the user-defined data schema for a specification
     * @param specID the specification id
     * @param sessionHandle an active session handle
     * @return an XML representation, or an XML diagnostic error message.
     * @throws IOException if the engine can't be found.
     */
    public String getSpecificationDataSchema(YSpecificationID specID, String sessionHandle)
                                                                 throws IOException {
        Map<String, String> params = prepareParamMap("getSpecificationDataSchema",
                                                      sessionHandle);
        params.putAll(specID.toMap());
        return stripOuterElement(executeGet(_backEndURIStr, params));
    }


    /**
     * Allow a client to obtain ownership of a unit of work. This means that the
     * workitem must be enabled or fired first, and that upon successful checkout the
     * workitem will be exectuing.
     * @param workItemID the workitem id.
     * @param sessionHandle the sessionhandle
     * @return in case of success returns a WorkItemRecord object of the created
     * workitem. In case of failure returns a diagnostic XML message.
     * @throws IOException if the engine can't be found.
     */
    public String checkOutWorkItem(String workItemID, String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("checkout", sessionHandle);
        params.put("workItemID", workItemID);
        return executePost(_backEndURIStr, params);
    }


    public String rejectAnnouncedEnabledTask(String workItemID, String sessionHandle) 
            throws IOException {
        Map<String, String> params = prepareParamMap("rejectAnnouncedEnabledTask", sessionHandle);
        params.put("workItemID", workItemID);
        return executePost(_backEndURIStr, params);
    }


    public String getStartingDataSnapshot(String workItemID, String sessionHandle)
                throws IOException {
        Map<String, String> params = prepareParamMap("getStartingDataSnapshot", sessionHandle);
        params.put("workItemID", workItemID);
        return executePost(_backEndURIStr, params);
    }


    /**
     * Gets an XML representation of information the task declaration.
     * This can be parsed into a copy of a YTask
     * @deprecated superseded by getTaskInformationStr(YSpecificationID, String, String)
     * - this version should be used for pre-2.0 schema-based specifications only
     * @param specID the spec id.
     * @param taskID the task id.
     * @param sessionHandle the session handle
     * @return an XML Representation of the task information
     * @throws IOException if the engine can't be found.
     */
    public String getTaskInformationStr(String specID, String taskID,
                                        String sessionHandle) throws IOException {
        return getTaskInformationStr(new YSpecificationID(specID), taskID, sessionHandle);
    }


    /**
     * Gets an XML representation of information the task declaration.
     * This can be parsed into a copy of a YTask
     * @param specID the spec id.
     * @param taskID the task id.
     * @param sessionHandle the session handle
     * @return an XML Representation of the task information
     * @throws IOException if the engine can't be found.
     */
    public String getTaskInformationStr(YSpecificationID specID, String taskID,
                                        String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("taskInformation", sessionHandle);
        params.putAll(specID.toMap());
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
     * Succesfully doing so will cause the work item to be completed in the engine.
     * @param workItemID the work item id.
     * @param data formated data eg. <data><param1Name>value</param1Name></data>
     * @param logPredicate configurable logging string to be logged with the checkin
     * @param sessionHandle the session handle
     * @return in case success returns the work item as xml. In case of failure
     * returns the reason for failure.
     * @throws IOException if engine cannot be found.
     */
    public String checkInWorkItem(String workItemID, String data, String logPredicate, String sessionHandle)
            throws IOException {
        Map<String, String> params = prepareParamMap("checkin", sessionHandle);
        params.put("data", data);
        params.put("workItemID", workItemID);
        params.put("logPredicate", logPredicate);
        return executePost(_backEndURIStr, params);
    }


    /**
     * @deprecated since 2.1 - use checkInWorkItem(String, String, String, String) instead
     * Checks the work item back into the engine once the task is complete.
     * Succesfully doing so will cause the work item to be completed in the engine.
     * @param workItemID the work item id.
     * @param data formated data eg. <data><param1Name>value</param1Name></data>
     * @param sessionHandle the session handle
     * @return in case success returns the work item as xml. In case of failure
     * returns the reason for failure.
     * @throws IOException if engine cannot be found.
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
     * created new instance to be created.
     * @pre the 'parent' task is a MultiInstance Task with
     * dynamic instance creation.
     * @param workItemID the workItemID of a sibling work item.
     * @param sessionHandle the session handle
     * @return diagnostic message that should indicate permission
     * if task is MultiInstance, and
     * if task allows dynamic instance creation, and
     * if current number of instances is less than the maxInstances
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
     * @pre the referenced task is multi-instance and allows dynamic creation
     * @param workItemID the work item id of a sibling workitem
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
     * Rolls back a work item, from 'executing' to 'fired' status.
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
     * Launches a case instance of the latest version of the specification loaded.
     * @deprecated superseded by launchCase(YSpecificationID, String, String)
     * @param specID the specification id (see SpecificationData.getID())
     * @param caseParams the case params in XML. i.e.
     * <pre>
     *    &lt;data&gt;
     *        &lt;firstParam&gt;value&lt;/firstParam&gt;
     *        &lt;secondParam&gt;value&lt;/secondParam&gt;
     *    &lt;/data&gt;
     * </pre>
     * If there are no case params then null should be passed.
     * @param sessionHandle the session handle
     * @return returns a diagnostic message in case of failure
     * @throws IOException if engine can't be found
     */
    public String launchCase(String specID, String caseParams, String sessionHandle)
            throws IOException {
        return launchCase(new YSpecificationID(specID), caseParams,
                              null, sessionHandle);
    }
    
    /**
     * Launches a case instance of the latest version of the specification loaded.
     * @param specID the specification id
     * @param caseParams the case params in XML. i.e.
     * <pre>
     *    &lt;data&gt;
     *        &lt;firstParam&gt;value&lt;/firstParam&gt;
     *        &lt;secondParam&gt;value&lt;/secondParam&gt;
     *    &lt;/data&gt;
     * </pre>
     * If there are no case params then null should be passed.
     * @param sessionHandle the session handle
     * @return returns a diagnostic message in case of failure
     * @throws IOException if engine can't be found
     */
    public String launchCase(YSpecificationID specID, String caseParams,
                             YLogDataItemList logData, String sessionHandle)
            throws IOException {
        Map<String, String> params = prepareParamMap("launchCase", sessionHandle);
        params.putAll(specID.toMap());
        if (logData != null) params.put("logData", logData.toXML());
        if (caseParams != null) params.put("caseParams", caseParams);
        return executePost(_backEndURIStr, params);
    }


    /** 
     * Override of launchCase to provide the ability to add a listener
     * for the Case-Completion event
     * @deprecated superseded by launchCase(YSpecificationID, String, String)
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
        return launchCase(new YSpecificationID(specID), caseParams,
                          sessionHandle, null, completionObserverURI);
    }


    /**
     * Override of launchCase to provide the ability to add a listener
     * for the Case-Completion event
     * @param specID the specification id
     * @param caseParams the case params in XML.
     * @param sessionHandle the session handle
     * @param completionObserverURI the URI of the IB service that will listen
     *        for a case-completed event
     * @return returns a diagnostic message in case of failure
     * @throws IOException if engine can't be found
     */
    public String launchCase(YSpecificationID specID, String caseParams,
                             String sessionHandle, YLogDataItemList logData,
                             String completionObserverURI)
                                     throws IOException {
        Map<String, String> params = buildLaunchCaseParamMap(specID, caseParams, 
                sessionHandle, logData, completionObserverURI);
        return executePost(_backEndURIStr, params);
    }
    
    
    public String launchCase(YSpecificationID specID, String caseParams,
                             String sessionHandle, YLogDataItemList logData,
                             String completionObserverURI, long mSec) throws IOException {
        Map<String, String> params = buildLaunchCaseParamMap(specID, caseParams, 
                sessionHandle, logData, completionObserverURI);
        params.put("mSec", String.valueOf(mSec));
        return executePost(_backEndURIStr, params);
    }

    public String launchCase(YSpecificationID specID, String caseParams,
                             String sessionHandle, YLogDataItemList logData,
                             String completionObserverURI, Date start) throws IOException {
        Map<String, String> params = buildLaunchCaseParamMap(specID, caseParams,
                sessionHandle, logData, completionObserverURI);
        params.put("start", String.valueOf(start.getTime()));
        return executePost(_backEndURIStr, params);
    }

    public String launchCase(YSpecificationID specID, String caseParams,
                             String sessionHandle, YLogDataItemList logData,
                             String completionObserverURI, Duration wait) throws IOException {
        Map<String, String> params = buildLaunchCaseParamMap(specID, caseParams,
                sessionHandle, logData, completionObserverURI);
        params.put("wait", wait.toString());
        return executePost(_backEndURIStr, params);
    }


    private Map<String, String> buildLaunchCaseParamMap(YSpecificationID specID,
                                 String caseParams, String sessionHandle,
                                 YLogDataItemList logData, String completionObserverURI) {
        Map<String, String> params = prepareParamMap("launchCase", sessionHandle);
        params.putAll(specID.toMap());
        if (logData != null) params.put("logData", logData.toXML());
        if (caseParams != null) params.put("caseParams", caseParams);
        if (completionObserverURI != null)
            params.put("completionObserverURI", completionObserverURI);
        return params;
    }


    /**
     * Gets the set of active cases in the engine.
     * @deprecated superseded by getCases(YSpecificationID, String)
     * @param specID the specification id.
     * @param sessionHandle the session handle
     * @return an XML list of case ids that are instances of the spec
     * with specid.
     * @throws IOException if engine cannot be found
     */
    public String getCases(String specID, String sessionHandle) throws IOException {
        return getCases(new YSpecificationID(specID), sessionHandle);
    }


    /**
     * Gets the set of active cases in the engine.
     * @param specID the specification id.
     * @param sessionHandle the session handle
     * @return an XML list of case ids that are instances of the spec
     * with specid.
     * @throws IOException if engine cannot be found
     */
    public String getCases(YSpecificationID specID, String sessionHandle)
                                                                 throws IOException {
        Map<String, String> params = prepareParamMap("getCasesForSpecification",
                                                      sessionHandle);
        params.putAll(specID.toMap());
        return executeGet(_backEndURIStr, params);
    }


    public String getAllRunningCases(String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("getAllRunningCases",
                                                      sessionHandle);
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
     * Finds out the children of a given work item. Only work items that are parents
     * have children.
     * @param workItemID the work item id of the parent workitem.
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
     * @see #getTaskInformationStr(YSpecificationID, String, String)
     * @return the TaskInformation object
     */
    public TaskInformation parseTaskInformation(String taskInfoStr) {
        taskInfoStr = stripOuterElement(taskInfoStr);
        return successful(taskInfoStr) ?
               Marshaller.unmarshalTaskInformation(taskInfoStr) : null ;
    }


    /**
     * Checks if the session has administrative access
     * @param sessionHandle the session to check
     * @return true if this session has administration privileges
     * @throws IOException if the engine cannot be found.
     */
    public boolean isAdministrator(String sessionHandle) throws IOException {
        String result = executeGet(_backEndURIStr, prepareParamMap("checkIsAdmin",
                                                                   sessionHandle));
        return (result.contains("Granted"));
    }

    /**
     * Gets an XML representation of the attributes of a multi-instance task.
     * @deprecated superseded by getMITaskAttributes(YSpecificationID, String, String) -
     * this version should be used for pre-2.0 schema-based specifications only
     * @param specID the spec id.
     * @param taskID the task id.
     * @param sessionHandle the session handle
     * @return an XML Representation of the task information
     * @throws IOException if the engine cannot be found.
     */
    public String getMITaskAttributes(String specID, String taskID,
                                      String sessionHandle) throws IOException {
        return getMITaskAttributes(new YSpecificationID(specID), taskID, sessionHandle);
    }

    /**
     * Gets an XML representation of the attributes of a multi-instance task.
     * @param specID the spec id.
     * @param taskID the task id.
     * @param sessionHandle the session handle
     * @return an XML Representation of the task information
     * @throws IOException if the engine cannot be found.
     */
    public String getMITaskAttributes(YSpecificationID specID, String taskID,
                                      String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("getMITaskAttributes", sessionHandle);
        params.put("taskID", taskID);
        params.putAll(specID.toMap());
        return executeGet(_backEndURIStr, params);
    }

    /**
     * Gets the set of resourcing specifications for the specified task of the specified spec
     * @param specID the specification id
     * @param taskID the id of the task to get the resourcing specs for
     * @param sessionHandle the session handle
     * @return an XML Representation of the resourcing information for the task
     * @throws IOException if the engine cannot be found.
     */
    public String getResourcingSpecs(YSpecificationID specID, String taskID,
                                      String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("getResourcingSpecs", sessionHandle);
        params.put("taskID", taskID);
        params.putAll(specID.toMap());
        String result = executeGet(_backEndURIStr, params);
        return (successful(result)) ? stripOuterElement(result) : result ;
    }


    /**
     * Gets a summary table of all currently live process instances
     * @param sessionHandle the session handle
     * @return an XML Representation of live processes
     * @throws IOException if the engine cannot be found.
     */
    public String getCaseInstanceSummary(String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("getCaseInstanceSummary", sessionHandle);
        return stripOuterElement(executeGet(_backEndURIStr, params));
    }


    /**
     * Gets a summary table of all completed and live workitems for a case
     * @param caseID the case id of the process to get the workitems for
     * @param sessionHandle the session handle
     * @return an XML Representation of workitems for a live case
     * @throws IOException if the engine cannot be found.
     */
    public String getWorkItemInstanceSummary(String caseID, String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("getWorkItemInstanceSummary", sessionHandle);
        params.put("caseID", caseID);
        return stripOuterElement(executeGet(_backEndURIStr, params));
    }


    /**
     * Gets a summary table of all data parameters for the workitem of the case specified
     * @param caseID the case id of the process to get the workitems for
     * @param itemID the id of the workitem to get the data params for
     * @param sessionHandle the session handle
     * @return an XML Representation of parameters for a workitem
     * @throws IOException if the engine cannot be found.
     */
    public String getParameterInstanceSummary(String caseID, String itemID, String sessionHandle)
            throws IOException {
        Map<String, String> params = prepareParamMap("getParameterInstanceSummary", sessionHandle);
        params.put("caseID", caseID);
        params.put("workItemID", itemID);
        return stripOuterElement(executeGet(_backEndURIStr, params));
    }


    /**
     * A generic method for sending a HTTP POST message, with parameters, to a URL
     * external to the standard YAWL environment
     * @param url the external URL to which the message is posted
     * @param params a map of attribute-value pairs to post with the message
     * @return a reply string from the external URL
     * @throws IOException if the external URL is invalid or unresponsive.
     */
    public String postToExternalURL(String url, Map<String, String> params) throws IOException {
        return executePost(url, params);
    }


    public String stripOuterElement(String xml) {
        return super.stripOuterElement(xml);
    }


    /**
     * Transforms an xml-string set of WorkItemRecords into a list 
     * @param xml the string describing the WorkItemRecords
     * @return a list of WorkItemRecord objects
     */
    private List<WorkItemRecord> unPackWorkItemList(String xml) {
        List<WorkItemRecord> result = new ArrayList<WorkItemRecord>();
        if (xml != null && successful(xml)) {
            Document doc = JDOMUtil.stringToDocument(xml);
            if (doc != null) {
                for (Element item : doc.getRootElement().getChildren()) {
                    result.add(Marshaller.unmarshalWorkItem(item));
                }
            }
        }
        return result;
    }

}
