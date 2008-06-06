/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.rsInterface;

import org.yawlfoundation.yawl.engine.interfce.Interface_Client;

import java.io.IOException;
import java.util.HashMap;

/**
 * An API to be used by clients that want to converse with the Resource Service.
 *
 *  @author Michael Adams
 *  14/08/2007
 *
 *  Last Date: 24/10/2007
 */

public class ResourceGatewayClient extends Interface_Client {

    /** the uri of the resource service's **gateway**
     * a default would be "http://localhost:8080/resourceService/gateway" */
    private String _serviceURI ;

    /** the only constructor
     * @param uri the uri of the resourceService's gateway */
    public ResourceGatewayClient(String uri) {
        _serviceURI = uri ;
    }

    /*******************************************************************************/

    // GET METHODS - returning String //

    /**
     * a wrapper for the executeGet method - returns a String
     * @param action the name of the gateway method to call
     * @return the resultant Element
     * @throws IOException
     */
    private String performGet(String action, String handle) throws IOException {
        return executeGet(_serviceURI + "?action=" + action + "&handle=" + handle) ;

    }

    private String executeBooleanGet(String action, String id, String handle)
                                                             throws IOException {
        return executeGet(_serviceURI + "?action=" + action + "&id=" + id +
                                                              "&handle=" + handle) ;
    }


    public String getConstraints(String handle) throws IOException {
        return performGet("getResourceConstraints", handle);
    }

    public String getFilters(String handle) throws IOException {
        return performGet("getResourceFilters", handle) ;
    }

    public String getAllocators(String handle) throws IOException {
        return performGet("getResourceAllocators", handle) ;
    }

    public String getAllSelectors(String handle) throws IOException {
        return performGet("getAllSelectors", handle) ;
    }

    public String getParticipants(String handle) throws IOException {
        return performGet("getParticipants", handle) ;
    }

    public String getRoles(String handle) throws IOException {
        return performGet("getRoles", handle) ;
    }

    public String getCapabilities(String handle) throws IOException {
        return performGet("getCapabilities", handle) ;
    }

    public String getPositions(String handle) throws IOException {
        return performGet("getPositions", handle) ;
    }

    public String getOrgGroups(String handle) throws IOException {
        return performGet("getOrgGroups", handle) ;
    }

    public String getAllParticipantNames(String handle) throws IOException {
        return performGet("getAllParticipantNames", handle) ;
    }

    public String getAllRoleNames(String handle) throws IOException {
        return performGet("getAllRoleNames", handle) ;
    }


    public String getActiveParticipants(String handle) throws IOException {
        return performGet("getActiveParticipants", handle) ;
    }



    // GET METHODS - returning boolean //

    public String isKnownParticipant(String participantID, String handle)
                                                                throws IOException {
        return executeBooleanGet("isKnownParticipant", participantID, handle) ;
    }

    public String isKnownRole(String roleID, String handle) throws IOException {
        return executeBooleanGet("isKnownRole", roleID, handle) ;
    }

    public String isKnownCapability(String capabilityID, String handle)
                                                                throws IOException {
        return executeBooleanGet("isKnownCapability", capabilityID, handle) ;
    }

    public String isKnownPosition(String positionID, String handle) throws IOException {
        return executeBooleanGet("isKnownPosition", positionID, handle) ;
    }

    public String isKnownOrgGroup(String orgGroupID, String handle) throws IOException {
        return executeBooleanGet("isKnownOrgGroup", orgGroupID, handle) ;
    }

    public String checkConnection(String handle) throws IOException {
        return executeGet(_serviceURI + "?action=checkConnection&handle=" + handle) ;  
    }

    public String getParticipant(String id, String handle) throws IOException {
        return executeGet(_serviceURI + "?action=getParticipant&id=" + id +
                                        "&handle=" + handle) ;
    }

    public String getParticipantRoles(String id, String handle) throws IOException {
        return executeGet(_serviceURI + "?action=getParticipantRoles&id=" + id +
                                        "&handle=" + handle) ;
    }



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


    /******************************************************************************/

    // POST METHODS //

    /** triggers a reload of org data */
    public void refreshOrgDataSet(String handle) throws IOException {
        HashMap<String,String> params = new HashMap<String,String>();
        params.put("action", "refreshOrgDataSet") ;
        params.put("handle", handle);
        executePost(_serviceURI, params);
    }

    
    /** (re)sets the timer for automatic org data reloads
     *
     * @param minutes the number of minutes to wait between refreshes
     * @throws IOException
     */
    public void resetOrgDataRefreshRate(int minutes, String handle) throws IOException {
        HashMap<String,String> params = new HashMap<String,String>();
        params.put("action", "resetOrgDataRefreshRate") ;
        params.put("rate", String.valueOf(minutes));
        params.put("handle", handle);
        executePost(_serviceURI, params);
    }

    
    /**
     * Disconnects an external entity from the resource service
     * @param handle the sessionHandle to disconnect
     * @throws IOException
     */
    public void disconnect(String handle) throws IOException {
        HashMap<String,String> params = new HashMap<String,String>();
        params.put("action", "disconnect") ;
        params.put("handle", handle);
        executePost(_serviceURI, params);
    }


}
