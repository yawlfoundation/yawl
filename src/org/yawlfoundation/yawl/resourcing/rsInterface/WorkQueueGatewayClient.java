/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The WorkQueue Gateway externalises the full worklist functionality of the
 * Resource Service by providign a gateway (or a set of API) between the
 * Service and the participant workqueue jsps.
 *
 * @author Michael Adams
 *         v0.1, 13/08/2007
 *         <p/>
 *         Last Date: 17/12/2008
 */

public class WorkQueueGatewayClient extends Interface_Client {

    /**
     * the uri of the resource service's **workqueue gateway**
     * a default would be "http://localhost:8080/resourceService/workqueuegateway"
     */
    private String _serviceURI;

    
    /**
     * empty constructor
     */
    public WorkQueueGatewayClient() {
        super();
    }

    /**
     * constructor
     *
     * @param uri the uri of the resourceService's workqueue gateway
     */
    public WorkQueueGatewayClient(String uri) {
        super();
        _serviceURI = uri;
    }

    /*******************************************************************************/
    
    private String idListToXML(Set<String> idList) {
        StringBuilder ids = new StringBuilder("<ids>");
        if (idList != null) {
            for (String id : idList) {
                ids.append("<id>").append(id).append("</id>");
            }
        }
        ids.append("</ids>");
        return ids.toString();
    }


    private String idStringToXML(String id) {
        if (id != null) {
            Set<String> set = new HashSet<String>();
            set.add(id);
            return idListToXML(set);
        } else return null;
    }


    /*******************************************************************************/

    // CONNECTION METHODS //

    /**
     * Connects an external entity to the resource service
     *
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
     *
     * @param handle the sessionHandle to disconnect
     * @throws IOException
     */
    public void disconnect(String handle) throws IOException {
        executePost(_serviceURI, prepareParamMap("disconnect", handle));
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
        return executePost(_serviceURI, prepareParamMap("userlogout", handle));
    }


    public String isValidUserSession(String handle) throws IOException {
        return executeGet(_serviceURI, prepareParamMap("isValidUserSession", handle));
    }


    public String checkConnection(String handle) throws IOException {
        return executeGet(_serviceURI, prepareParamMap("checkConnection", handle));
    }


    /**
     * **************************************************************************
     */

    // PARTICIPANT INFO //
    public String getParticipantFromUserID(String userid, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("getParticipantFromUserID", handle);
        params.put("userid", userid);
        String result = executeGet(_serviceURI, params);
        return (result.equals("<null>")) ? null : result;
    }


    public String getFullNameForUserID(String userid, String handle) throws IOException {
        String result = null;
        if (userid != null) {
            Map<String, String> params = prepareParamMap("getFullNameForUserID", handle);
            params.put("userid", userid);
            result = executeGet(_serviceURI, params);
        }
        return result;
    }


    public String getUserPrivileges(String pid, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getUserPrivileges", handle);
        params.put("participantid", pid);
        return executeGet(_serviceURI, params);
    }


    public String getTaskPrivileges(String itemid, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getTaskPrivileges", handle);
        params.put("workitemid", itemid);
        return executeGet(_serviceURI, params);
    }


    public String getReportingToParticipant(String pid, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getParticipantsReportingTo", handle);
        params.put("participantid", pid);
        return executeGet(_serviceURI, params);
    }


    public String getOrgGroupMembers(String oid, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getOrgGroupMembers", handle);
        params.put("groupid", oid);
        return executeGet(_serviceURI, params);
    }


    public String getRoleMembers(String rid, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getRoleMembers", handle);
        params.put("roleid", rid);
        return executeGet(_serviceURI, params);
    }


    public String getParticipant(String pid, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getParticipant", handle);
        params.put("participantid", pid);
        return executeGet(_serviceURI, params);
    }


    public String getAllParticipants(String handle) throws IOException {
        return executeGet(_serviceURI, prepareParamMap("getParticipants", handle));
    }


    public String getDistributionSet(String itemid, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getDistributionSet", handle);
        params.put("workitemid", itemid);
        return executeGet(_serviceURI, params);
    }

    /**
     * ****************************************************************************
     */

    // QUEUE INFO & MANIPULATION //
    public String getAdminQueues(String handle) throws IOException {
        return executeGet(_serviceURI, prepareParamMap("getAdminQueues", handle));
    }


    public String getParticipantQueues(String pid, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getParticipantQueues", handle);
        params.put("participantid", pid);
        return executeGet(_serviceURI, params);
    }


    public String getQueuedWorkItems(String pid, int queue, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("getQueuedWorkItems", handle);
        params.put("participantid", pid);
        params.put("queue", String.valueOf(queue));
        return executeGet(_serviceURI, params);
    }


    public String getWorkItem(String itemID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getWorkItem", handle);
        params.put("workitemid", itemID);
        return executeGet(_serviceURI, params);
    }


    public String getWorkItemChildren(String itemID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getWorkItemChildren", handle);
        params.put("workitemid", itemID);
        return executeGet(_serviceURI, params);
    }


    public String updateWorkItemData(String itemID, String data, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("updateWorkItemData", handle);
        params.put("workitemid", itemID);
        params.put("data", data);
        return executeGet(_serviceURI, params);
    }


    public String getWorkItemParameters(String itemID, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("getWorkItemParameters", handle);
        params.put("workitemid", itemID);
        return executeGet(_serviceURI, params);
    }


    public String getWorkItemDataSchema(String itemID, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("getWorkItemDataSchema", handle);
        params.put("workitemid", itemID);
        return executeGet(_serviceURI, params);
    }


    public String getCaseDataSchema(YSpecificationID specID, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("getCaseDataSchema", handle);
        params.putAll(specID.toMap());
        return executeGet(_serviceURI, params);
    }


    public String getWorkItemOutputOnlyParameters(String itemID, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("getWorkItemOutputOnlyParameters", handle);
        params.put("workitemid", itemID);
        return executeGet(_serviceURI, params);
    }


    public String synchroniseCaches(String handle) throws IOException {
        return executeGet(_serviceURI, prepareParamMap("synchroniseCaches", handle));
    }


    public String getParticipantsAssignedWorkItem(String workItemID, int queueType,
                                                  String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getParticipantsAssignedWorkItem", handle);
        params.put("workitemid", workItemID);
        params.put("queue", String.valueOf(queueType));
        return executeGet(_serviceURI, params);
    }


    public String acceptOffer(String pid, String itemID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("acceptOffer", handle);
        params.put("participantid", pid);
        params.put("workitemid", itemID);
        return executePost(_serviceURI, params);
    }


    public String startItem(String pid, String itemID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("startWorkItem", handle);
        params.put("participantid", pid);
        params.put("workitemid", itemID);
        return executePost(_serviceURI, params);
    }


    public String deallocateItem(String pid, String itemID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("deallocateWorkItem", handle);
        params.put("participantid", pid);
        params.put("workitemid", itemID);
        return executePost(_serviceURI, params);
    }


    public String delegateItem(String pFrom, String pTo, String itemID, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("delegateWorkItem", handle);
        params.put("pfrom", pFrom);
        params.put("pto", pTo);
        params.put("workitemid", itemID);
        return executePost(_serviceURI, params);
    }


    public String skipItem(String pid, String itemID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("skipWorkItem", handle);
        params.put("participantid", pid);
        params.put("workitemid", itemID);
        return executePost(_serviceURI, params);
    }


    public String pileItem(String pid, String itemID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("pileWorkItem", handle);
        params.put("participantid", pid);
        params.put("workitemid", itemID);
        return executePost(_serviceURI, params);
    }


    public String chainCase(String pid, String itemID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("chainCase", handle);
        params.put("participantid", pid);
        params.put("workitemid", itemID);
        return executePost(_serviceURI, params);
    }


    public String suspendItem(String pid, String itemID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("suspendWorkItem", handle);
        params.put("participantid", pid);
        params.put("workitemid", itemID);
        return executePost(_serviceURI, params);
    }


    public String reallocateItem(String pFrom, String pTo, String itemID,
                                 boolean stateful, String handle) throws IOException {
        if (pFrom == null) {
            return reallocateItem(pTo, itemID, handle);    // admin queue realloc
        }
        String action = stateful ? "reallocateStatefulWorkItem" : "reallocateStatelessWorkItem";
        Map<String, String> params = prepareParamMap(action, handle);
        params.put("pfrom", pFrom);
        params.put("pto", pTo);
        params.put("workitemid", itemID);
        return executePost(_serviceURI, params);
    }


    public String completeItem(String pid, String itemID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("completeWorkItem", handle);
        params.put("participantid", pid);
        params.put("workitemid", itemID);
        return executePost(_serviceURI, params);
    }


    public String unsuspendItem(String pid, String itemID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("unsuspendWorkItem", handle);
        params.put("participantid", pid);
        params.put("workitemid", itemID);
        return executePost(_serviceURI, params);
    }


    public String offerItem(Set<String> pids, String itemID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("offerWorkItem", handle);
        params.put("participantids", idListToXML(pids));
        params.put("workitemid", itemID);
        return executePost(_serviceURI, params);
    }


    public String reofferItem(Set<String> pids, String itemID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("reofferWorkItem", handle);
        params.put("participantids", idListToXML(pids));
        params.put("workitemid", itemID);
        return executePost(_serviceURI, params);
    }


    public String allocateItem(String pid, String itemID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("allocateWorkItem", handle);
        params.put("participantids", idStringToXML(pid));
        params.put("workitemid", itemID);
        return executePost(_serviceURI, params);
    }


    public String reallocateItem(String pid, String itemID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("reallocateWorkItem", handle);
        params.put("participantids", idStringToXML(pid));
        params.put("workitemid", itemID);
        return executePost(_serviceURI, params);
    }


    public String restartItem(String pid, String itemID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("restartWorkItem", handle);
        params.put("participantids", idStringToXML(pid));
        params.put("workitemid", itemID);
        return executePost(_serviceURI, params);
    }


    public String redirectWorkItemToYawlService(String itemID, String serviceName,
                                                String handle) throws IOException {
        Map<String, String> params = prepareParamMap("redirectWorkItemToYawlService", handle);
        params.put("workitemid", itemID);
        params.put("serviceName", serviceName);
        return executePost(_serviceURI, params);
    }


    /**
     * ****************************************************************************
     */

    // SPEC AND INSTANCE ACTIONS //
    public String getLoadedSpecs(String handle) throws IOException {
        return executeGet(_serviceURI, prepareParamMap("getLoadedSpecs", handle));
    }


    public String getSpecList(String handle) throws IOException {
        return executeGet(_serviceURI, prepareParamMap("getSpecList", handle));
    }


    public String getSpecData(YSpecificationID specID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getSpecData", handle);
        params.putAll(specID.toMap());
        return executeGet(_serviceURI, params);
    }


    public String uploadSpecification(String fileContents, String fileName, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("uploadSpecification", handle);
        params.put("filecontents", fileContents);
        params.put("filename", fileName);
        return executePost(_serviceURI, params);
    }


    public String unloadSpecification(YSpecificationID specID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("unloadSpecification", handle);
        params.putAll(specID.toMap());
        return executePost(_serviceURI, params);
    }


    public String launchCase(YSpecificationID specID, String caseData, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("launchCase", handle);
        params.putAll(specID.toMap());
        params.put("casedata", caseData);
        return executePost(_serviceURI, params);
    }


    public String getRunningCases(YSpecificationID specID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getRunningCases", handle);
        params.putAll(specID.toMap());
        return executeGet(_serviceURI, params);
    }


    public String cancelCase(String caseID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("cancelCase", handle);
        params.put("caseid", caseID);
        return executePost(_serviceURI, params);
    }


    public String updateWIRCache(WorkItemRecord wir, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("updateWorkItemCache", handle);
        params.put("wir", wir.toXML());
        return executePost(_serviceURI, params);
    }


    public String getDecompID(WorkItemRecord wir, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getDecompID", handle);
        params.put("workitemid", wir.getID());
        return executeGet(_serviceURI, params);
    }


    public String getCaseData(String caseID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getCaseData", handle);
        params.put("caseid", caseID);
        return executeGet(_serviceURI, params);
    }

    public String getWorkItemDurationsForParticipant(YSpecificationID specID,
                                                     String taskName, String pid, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("getWorkItemDurationsForParticipant", handle);
        params.putAll(specID.toMap());
        params.put("taskname", taskName);
        params.put("participantid", pid);
        return executeGet(_serviceURI, params);
    }


    /**
     * ****************************************************************************
     */

    // REGISTERED SERVICE INFO //
    public String getRegisteredServices(String handle) throws IOException {
        return executeGet(_serviceURI, prepareParamMap("getRegisteredServices", handle));
    }


    public String removeRegisteredService(String id, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("removeRegisteredService", handle);
        params.put("serviceid", id);
        return executePost(_serviceURI, params);
    }


    public String addRegisteredService(String uri, String name, String password, String doco,
                                       boolean assignable, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("addRegisteredService", handle);
        params.put("uri", uri);
        params.put("name", name);
        params.put("password", password);
        params.put("doco", doco);
        params.put("assignable", String.valueOf(assignable));
        return executePost(_serviceURI, params);
    }

    public String removeExternalClient(String id, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("removeExternalClient", handle);
        params.put("name", id);
        return executePost(_serviceURI, params);
    }


    public String addExternalClient(String name, String password, String doco, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("addExternalClient", handle);
        params.put("name", name);
        params.put("password", password);
        params.put("doco", doco);
        return executePost(_serviceURI, params);
    }

    public String addResourceEventListener(String uri, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("addResourceEventListener", handle);
        params.put("uri", uri);
        return executePost(_serviceURI, params);
    }


    public String removeResourceEventListener(String uri, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("removeResourceEventListener", handle);
        params.put("uri", uri);
        return executePost(_serviceURI, params);
    }

}