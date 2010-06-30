/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing.rsInterface;

import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.PasswordEncryptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *  The WorkQueue Gateway externalises the full worklist functionality of the
 *  Resource Service by providign a gateway (or a set of API) between the 
 *  Service and the participant workqueue jsps.
 *
 *  @author Michael Adams
 *  v0.1, 13/08/2007
 *
 *  Last Date: 17/12/2008
 */

public class WorkQueueGatewayClient extends Interface_Client {

   /** the uri of the resource service's **workqueue gateway**
    * a default would be "http://localhost:8080/resourceService/workqueuegateway"
    */
    private String _serviceURI ;


    /** a mapping of parameter names and values */
    private Map<String, String> params = new HashMap<String, String>();


    /** empty constructor */
    public WorkQueueGatewayClient() {
        super();
    }

    /** constructor
     * @param uri the uri of the resourceService's workqueue gateway
     */
    public WorkQueueGatewayClient(String uri) {
        super();
        _serviceURI = uri ;
    }

    /*******************************************************************************/

    // GET & POST WRAPPER METHODS //

    /**
     * a wrapper for the executeGet method
     * @param action the name of the gateway method to call
     * @return the resultant reply String
     * @throws IOException if the service can't be reached
     */
    private String performGet(String action, String handle) throws IOException {
        return executeGet(_serviceURI, prepareParamMap(action, handle)) ;

    }

    /**
     * a wrapper for the executeGet method - returns a String
     * @param action the name of the gateway method to call
     * @param map a map of parameters and values
     * @param handle an active sessionhandle
     * @return the resultant reply String
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    private String performGet(String action, Map<String, String> map, String handle)
                                                                     throws IOException {
        Map<String, String> params = prepareParamMap(action, handle);
        if (map != null) params.putAll(map);
        return executeGet(_serviceURI, params);
    }

    private String performPost(String action, Map<String, String> map, String handle)
                                                                     throws IOException {
        Map<String, String> params = prepareParamMap(action, handle);
        if (map != null) params.putAll(map);
        return executePost(_serviceURI, params);
    }


    /*******************************************************************************/

    // CONNECTION METHODS //

    /**
     * Connects an external entity to the resource service
     * @param userID
     * @param password
     * @return a sessionHandle if successful, or a failure message if not
     * @throws IOException
     */
    public String connect(String userID, String password) throws IOException {
        Map<String, String> params = prepareParamMap("connect", null);
        params.put("userid", userID);
        params.put("password", PasswordEncryptor.encrypt(password, null));
        return executeGet(_serviceURI, params);
    }


    /**
     * Disconnects an external entity from the resource service
     * @param handle the sessionHandle to disconnect
     * @throws IOException
     */
    public void disconnect(String handle) throws IOException {
        performPost("disconnect", null, handle);
    }


    public String userlogin(String userID, String password, boolean encrypt) throws IOException {
        if (userID.equals("admin")) {
            return "<failure>UserID 'admin' is not a valid participant.</failure>";
        }
        Map<String, String> params = prepareParamMap("userlogin", null);
        params.put("userid", userID);
        params.put("password", password);
        params.put("encrypt", String.valueOf(encrypt));
        return executeGet(_serviceURI, params);
    }

    
    public String userlogin(String userID, String password) throws IOException {
        return userlogin(userID, password, true);
    }


    public String userlogout(String handle) throws IOException {
       return performPost("userlogout", null, handle);
    }


    public String isValidUserSession(String handle) throws IOException {
        return performGet("isValidUserSession", handle) ;
    }


    public String checkConnection(String handle) throws IOException {
        return performGet("checkConnection", handle) ;
    }


    /******************************************************************************/

    // PARTICIPANT INFO //

    public String getParticipantFromUserID(String userid, String handle)
                                                               throws IOException {
        Map<String, String> params = prepareParamMap("getParticipantFromUserID", handle);
        params.put("userid", userid);
        String result = executeGet(_serviceURI, params) ;
        return (result.equals("<null>")) ? null : result;
    }


    public String getFullNameForUserID(String userid, String handle) throws IOException {
        String result = null ;
        if (userid != null) {
            params.clear();
            params.put("userid", userid);
            result = performGet("getFullNameForUserID", params, handle) ;
        }
        return result;
    }


    public String getUserPrivileges(String pid, String handle) throws IOException {
        params.clear();
        params.put("participantid", pid) ;
        return performGet("getUserPrivileges", params, handle) ;
    }


    public String getTaskPrivileges(String itemid, String handle) throws IOException {
        params.clear();
        params.put("workitemid", itemid) ;
        return performGet("getTaskPrivileges", params, handle) ;
    }


    public String getReportingToParticipant(String pid, String handle) throws IOException {
        params.clear();
        params.put("participantid", pid) ;
        return performGet("getParticipantsReportingTo", params, handle) ;
    }


    public String getOrgGroupMembers(String oid, String handle) throws IOException {
        params.clear();
        params.put("groupid", oid) ;
        return performGet("getOrgGroupMembers", params, handle) ;
    }


    public String getRoleMembers(String rid, String handle) throws IOException {
        params.clear();
        params.put("roleid", rid) ;
        return performGet("getRoleMembers", params, handle) ;
    }


    public String getParticipant(String pid, String handle) throws IOException {
        params.clear();
        params.put("participantid", pid) ;
        return performGet("getParticipant", params, handle) ;
    }


    public String getAllParticipants(String handle) throws IOException {
        return performGet("getParticipants", handle) ;
    }


    public String getDistributionSet(String itemid, String handle) throws IOException {
        params.clear();
        params.put("workitemid", itemid) ;
        return performGet("getDistributionSet", params, handle) ;
    }

    /********************************************************************************/

    // QUEUE INFO & MANIPULATION //

    public String getAdminQueues(String handle) throws IOException {
        return performGet("getAdminQueues", handle);
    }


    public String getQueuedWorkItems(String pid, int queue, String handle)
                                                                    throws IOException {
        params.clear();
        params.put("participantid", pid) ;
        params.put("queue", String.valueOf(queue));
        return performGet("getQueuedWorkItems", params, handle) ;
    }

    
    public String getWorkItem(String itemID, String handle) throws IOException {
        params.clear();
        params.put("workitemid", itemID);
        return performGet("getWorkItem", params, handle);
    }


    public String getWorkItemChildren(String itemID, String handle) throws IOException {
        params.clear();
        params.put("workitemid", itemID);
        return performGet("getWorkItemChildren", params, handle);
    }


    public String updateWorkItemData(String itemID, String data, String handle)
            throws IOException {
        params.clear();
        params.put("workitemid", itemID);
        params.put("data", data);
        return performGet("updateWorkItemData", params, handle);        
    }


    public String getWorkItemParameters(String itemID, String handle)
            throws IOException {
        params.clear();
        params.put("workitemid", itemID);
        return performGet("getWorkItemParameters", params, handle);        
    }


    public String getWorkItemDataSchema(String itemID, String handle)
            throws IOException {
        params.clear();
        params.put("workitemid", itemID);
        return performGet("getWorkItemDataSchema", params, handle);
    }

    
    public String getWorkItemOutputOnlyParameters(String itemID, String handle)
            throws IOException {
        params.clear();
        params.put("workitemid", itemID);
        return performGet("getWorkItemOutputOnlyParameters", params, handle);
    }


    public String synchroniseCaches(String handle) throws IOException {
        return performGet("synchroniseCaches", null, handle) ;
    }


    public String getParticipantsAssignedWorkItem(String workItemID, int queueType,
                                                  String handle) throws IOException {
        params.clear();
        params.put("workitemid", workItemID) ;
        params.put("queue", String.valueOf(queueType));
        return performGet("getParticipantsAssignedWorkItem", params, handle) ;
    }


    public String acceptOffer(String pid, String itemID, String handle) throws IOException {
        params.clear();
        params.put("participantid", pid) ;
        params.put("workitemid", itemID) ;
        return performPost("acceptOffer", params, handle) ;
    }


    public String startItem(String pid, String itemID, String handle) throws IOException {
        params.clear();
        params.put("participantid", pid) ;
        params.put("workitemid", itemID) ;
        return performPost("startWorkItem", params, handle) ;
    }


    public String deallocateItem(String pid, String itemID, String handle) throws IOException {
        params.clear();
        params.put("participantid", pid) ;
        params.put("workitemid", itemID) ;
        return performPost("deallocateWorkItem", params, handle) ;
    }

    
    public String delegateItem(String pFrom, String pTo, String itemID, String handle)
                                                                     throws IOException {
        params.clear();
        params.put("pfrom", pFrom) ;
        params.put("pto", pTo) ;
        params.put("workitemid", itemID) ;
        return performPost("delegateWorkItem", params, handle) ;
    }


    public String skipItem(String pid, String itemID, String handle) throws IOException {
        params.clear();
        params.put("participantid", pid) ;
        params.put("workitemid", itemID) ;
        return performPost("skipWorkItem", params, handle) ;
    }


    public String pileItem(String pid, String itemID, String handle) throws IOException {
        params.clear();
        params.put("participantid", pid) ;
        params.put("workitemid", itemID) ;
        return performPost("pileWorkItem", params, handle) ;
    }


    public String suspendItem(String pid, String itemID, String handle) throws IOException {
        params.clear();
        params.put("participantid", pid) ;
        params.put("workitemid", itemID) ;
        return performPost("suspendWorkItem", params, handle) ;
    }

    
    public String reallocateItem(String pFrom, String pTo, String itemID,
                               boolean stateful, String handle) throws IOException {
        params.clear();
        params.put("pfrom", pFrom) ;
        params.put("pto", pTo) ;
        params.put("workitemid", itemID) ;
        if (stateful)
               return performPost("reallocateStatefulWorkItem", params, handle) ;
        else
               return performPost("reallocateStatelessWorkItem", params, handle) ;
    }

    
    public String completeItem(String pid, String itemID, String handle) throws IOException {
        params.clear();
        params.put("participantid", pid) ;
        params.put("workitemid", itemID) ;
        return performPost("completeWorkItem", params, handle) ;
    }


    public String unsuspendItem(String pid, String itemID, String handle) throws IOException {
        params.clear();
        params.put("participantid", pid) ;
        params.put("workitemid", itemID) ;
        return performPost("unsuspendWorkItem", params, handle) ;
    }


    /********************************************************************************/

    // SPEC AND INSTANCE ACTIONS //

    public String getLoadedSpecs(String handle) throws IOException {
        return performGet("getLoadedSpecs", handle) ;
    }


    public String getSpecList(String handle) throws IOException {
        return performGet("getSpecList", handle) ;
    }


    public String getSpecData(YSpecificationID specID, String handle) throws IOException {
        params.clear();
        params.putAll(specID.toMap());
        return performGet("getSpecData", params, handle) ;
    }


    public String uploadSpecification(String fileContents, String fileName, String handle)
                                                                     throws IOException {
        params.clear();
        params.put("filecontents", fileContents);
        params.put("filename", fileName);
        return performPost("uploadSpecification", params, handle) ;
    }


    public String unloadSpecification(YSpecificationID specID, String handle) throws IOException {
        params.clear();
        params.putAll(specID.toMap());
        return performPost("unloadSpecification", params, handle);
    }


    public String launchCase(YSpecificationID specID, String caseData, String handle)
                                                                   throws IOException {
        params.clear();
        params.putAll(specID.toMap());
        params.put("casedata", caseData);
        return performPost("launchCase", params, handle) ;
    }


    public String getRunningCases(YSpecificationID specID, String handle) throws IOException {
        params.clear();
        params.putAll(specID.toMap());
        return performGet("getRunningCases", params, handle) ;
    }


    public String cancelCase(String caseID, String handle) throws IOException {
        params.clear();
        params.put("caseid", caseID);
        return performPost("cancelCase", params, handle);
    }


    public String updateWIRCache(WorkItemRecord wir, String handle) throws IOException {
        params.clear();
        params.put("wir", wir.toXML()) ;
        return performPost("updateWorkItemCache", params, handle) ;
    }


    public String getDecompID(WorkItemRecord wir, String handle) throws IOException {
        params.clear();
        params.put("workitemid", wir.getID()) ;
        return performGet("getDecompID", params, handle) ;
    }


    public String getCaseData(String caseID, String handle) throws IOException {
         params.clear();
         params.put("caseid", caseID) ;
         return performGet("getCaseData", params, handle);
     }

    public String getWorkItemDurationsForParticipant(YSpecificationID specID,
                                           String taskName, String pid, String handle)
                                           throws IOException {
        params.clear();
        params.putAll(specID.toMap());
        params.put("taskname", taskName);
        params.put("participantid", pid);
        return performGet("getWorkItemDurationsForParticipant", params, handle);
    }



    /********************************************************************************/

    // REGISTERED SERVICE INFO //

    public String getRegisteredServices(String handle) throws IOException {
        return performGet("getRegisteredServices", handle);
    }


    public String removeRegisteredService(String id, String handle) throws IOException {
        params.clear();
        params.put("serviceid", id) ;
        return performPost("removeRegisteredService", params, handle);
    }


    public String addRegisteredService(String uri, String name, String doco,
                                       boolean assignable, String handle) throws IOException {
        params.clear();
        params.put("uri", uri) ;
        params.put("name", name) ;
        params.put("doco", doco) ;
        params.put("assignable", String.valueOf(assignable)) ;
        return performPost("addRegisteredService", params, handle);
    }


}