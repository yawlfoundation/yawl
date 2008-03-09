/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.rsInterface;

import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *  The WorkQueue Gateway provides a gateway (or a set of API) between the Resource
 *  Service and the participant workqueue jsps.
 *
 *  @author Michael Adams
 *  v0.1, 13/08/2007
 *
 *  Last Date: 07/03/2008
 */

public class WorkQueueGatewayClient extends Interface_Client {

   /** the uri of the resource service's **workqueue gateway**
     * a default would be "http://localhost:8080/resourceService/workqueuegateway" */
    private String _serviceURI ;


    /** a mapping of parameter names and values */
    private Map<String, String> params = new HashMap<String, String>();


    /** the only constructor
     * @param uri the uri of the resourceService's gateway */
    public WorkQueueGatewayClient(String uri) {
        _serviceURI = uri ;
    }

    /*******************************************************************************/

    // GET & POST WRAPPER METHODS //

    /**
     * a wrapper for the executeGet method - returns a String
     * @param action the name of the gateway method to call
     * @return the resultant Element
     * @throws IOException
     */
    private String performGet(String action, String handle) throws IOException {
        return executeGet(_serviceURI + "?action=" + action + "&handle=" + handle) ;

    }

    /**
     * a wrapper for the executeGet method - returns a String
     * @param action the name of the gateway method to call
     * @param params a map of parameters and values
     * @param handle an active sessionhandle
     * @return the resultant Element
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    private String performGet(String action, Map<String, String> params, String handle)
                                                                     throws IOException {
        return executeGet(buildRequest(action, params, handle));
    }

    private String performPost(String action, Map<String, String> map, String handle)
                                                                     throws IOException {
        if (map == null) map = new HashMap<String, String>();
        map.put("action", action);
        map.put("sessionHandle", handle);
        return executePost(_serviceURI, map);
    }

    private String buildRequest(String action, Map<String, String> params, String handle) {
        StringBuilder request = new StringBuilder(_serviceURI) ;
        request.append("?action=").append(action) ;
        for (String pName : params.keySet())
            request.append("&").append(pName).append("=").append(params.get(pName));
        request.append("&handle=").append(handle);
        return request.toString();
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
        return executeGet(_serviceURI + "?action=connect&userid=" + userID +
                                        "&password=" + password);
    }


    /**
     * Disconnects an external entity from the resource service
     * @param handle the sessionHandle to disconnect
     * @throws IOException
     */
    public void disconnect(String handle) throws IOException {
        performPost("disconnect", null, handle);
    }


    public String login(String userid, String password) throws IOException {
        return connect(userid, password);
    }


    public void logout(String handle) throws IOException {
        disconnect(handle);
    }


    public String isValidSession(String handle) throws IOException {
        return performGet("isValidSession", handle) ;
    }


    public String checkConnection(String handle) throws IOException {
        return performGet("checkConnection", handle) ;
    }


    /******************************************************************************/

    // PARTICIPANT INFO //

    public String getParticipantFromUserID(String userid, String handle)
                                                               throws IOException {
        params.clear();
        params.put("userid", userid);
        return performGet("getParticipantFromUserID", params, handle) ;
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


    public String getReportingToParticipant(String pid, String handle) throws IOException {
        params.clear();
        params.put("participantid", pid) ;
        return performGet("getParticipantsReportingTo", params, handle) ;
    }


    public String getParticipant(String pid, String handle) throws IOException {
        params.clear();
        params.put("participantid", pid) ;
        return performGet("getParticipant", params, handle) ;
    }


    public String getAllParticipants(String handle) throws IOException {
        return performGet("getParticipants", handle) ;
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


    public String getParticipantsAssignedWorkItem(String workItemID, int queueType,
                                                  String handle) throws IOException {
        params.clear();
        params.put("workitemid", workItemID) ;
        params.put("queue", String.valueOf(queueType));
        return performGet("getParticipantsAssignedWorkItem", params, handle) ;
    }


    public void acceptOffer(String pid, String itemID, String handle) throws IOException {
        params.clear();
        params.put("participantid", pid) ;
        params.put("workitemid", itemID) ;
        performPost("acceptOffer", params, handle) ;
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
        return performPost("checkinItem", params, handle) ;
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


    public String getSpecData(String specID, String handle) throws IOException {
        params.clear();
        params.put("specid", specID);
        return performGet("getSpecData", params, handle) ;
    }


    public String uploadSpecification(String fileContents, String fileName, String handle)
                                                                     throws IOException {
        params.clear();
        params.put("filecontents", fileContents);
        params.put("filename", fileName);
        return performPost("uploadSpecification", params, handle) ;
    }


    public String unloadSpecification(String specID, String handle) throws IOException {
        params.clear();
        params.put("specid", specID);
        return performPost("unloadSpecification", params, handle);
    }


    public String launchCase(String specID, String caseData, String handle)
                                                                   throws IOException {
        params.clear();
        params.put("specid", specID);
        params.put("casedata", caseData);
        return performPost("launchCase", params, handle) ;
    }


    public String getRunningCases(String specID, String handle) throws IOException {
        params.clear();
        params.put("specid", specID);
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
        params.put("itemid", wir.getID()) ;
        return performGet("getDecompID", params, handle) ;
    }


    public String getCaseData(String caseID, String handle) throws IOException {
         params.clear();
         params.put("caseid", caseID) ;
         return performPost("getCaseData", params, handle);
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


    public String addRegisteredService(YAWLServiceReference service, String handle)
                                                                    throws IOException {
        params.clear();
        params.put("service", service.toXMLComplete()) ;
        return performPost("addRegisteredService", params, handle);
    }


}