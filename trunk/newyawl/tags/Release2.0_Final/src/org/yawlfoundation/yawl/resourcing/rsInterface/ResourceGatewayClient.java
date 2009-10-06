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
import java.util.Map;

/**
 * An API to be used by clients that for retrieving organisational model data from
 * the Resource Service.
 *
 *  @author Michael Adams
 *  14/08/2007
 *
 *  Last Date: 02/07/2008
 */

public class ResourceGatewayClient extends Interface_Client {

    /** the uri of the resource service's __ resource gateway__
     * a default would be "http://localhost:8080/resourceService/gateway"
     */
    private String _serviceURI ;

    /** the only constructor
     * @param uri the uri of the resourceService's gateway
     */
    public ResourceGatewayClient(String uri) {
        _serviceURI = uri ;
    }

    /*******************************************************************************/

    // GET METHODS - returning String //

    /**
     * a wrapper for the executeGet method - returns a String
     * @param action the name of the gateway method to call
     * @param handle a valid session handle
     * @return the resultant reply String
     * @throws IOException if the service can't be reached
     */
    private String performGet(String action, String handle) throws IOException {
        return executeGet(_serviceURI, prepareParamMap(action, handle)) ;
    }


    /**
     * A wrapper for a GET execution that returns a boolean-valued string
     * @param action the name of the action to perform
     * @param id an identifier used as a parameter for the boolean action passed
     * @param handle a valid session handle
     * @return the resultant reply String ("true" or "false")
     * @throws IOException if the service can't be reached
     */
    private String executeBooleanGet(String action, String id, String handle)
                                                             throws IOException {
        Map<String, String> params = prepareParamMap(action, handle);
        params.put("id", id);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Gets an XML list of all Resource Constraints known to the service
     * @param handle a valid session handle
     * @return the list of constraints as an XML string
     * @throws IOException if the service can't be reached
     */
    public String getConstraints(String handle) throws IOException {
        return performGet("getResourceConstraints", handle);
    }


    /**
     * Gets an XML list of all Resource Filters known to the service
     * @param handle a valid session handle
     * @return the list of filters as an XML string
     * @throws IOException if the service can't be reached
     */
    public String getFilters(String handle) throws IOException {
        return performGet("getResourceFilters", handle) ;
    }


    /**
     * Gets an XML list of all Resource Allocators known to the service
     * @param handle a valid session handle
     * @return the list of allocators as an XML string
     * @throws IOException if the service can't be reached
     */
    public String getAllocators(String handle) throws IOException {
        return performGet("getResourceAllocators", handle) ;
    }


    /**
     * Gets a concatenated XML list of all Resource Selectors (i.e. Constraints,
     * Filters and Allocators) known to the service
     * @param handle a valid session handle
     * @return the list of allocators as an XML string
     * @throws IOException if the service can't be reached
     */
    public String getAllSelectors(String handle) throws IOException {
        return performGet("getAllSelectors", handle) ;
    }


    /**
     * Gets an XML list of all Participants known to the service
     * @param handle a valid session handle
     * @return the list of participants as an XML string
     * @throws IOException if the service can't be reached
     */
    public String getParticipants(String handle) throws IOException {
        return performGet("getParticipants", handle) ;
    }


    /**
     * Gets an XML list of all Roles known to the service
     * @param handle a valid session handle
     * @return the list of roles as an XML string
     * @throws IOException if the service can't be reached
     */
    public String getRoles(String handle) throws IOException {
        return performGet("getRoles", handle) ;
    }


    /**
     * Gets an XML list of all Capabilities known to the service
     * @param handle a valid session handle
     * @return the list of capabilities as an XML string
     * @throws IOException if the service can't be reached
     */
    public String getCapabilities(String handle) throws IOException {
        return performGet("getCapabilities", handle) ;
    }


    /**
     * Gets an XML list of all Positions known to the service
     * @param handle a valid session handle
     * @return the list of positions as an XML string
     * @throws IOException if the service can't be reached
     */
    public String getPositions(String handle) throws IOException {
        return performGet("getPositions", handle) ;
    }


    /**
     * Gets an XML list of all OrgGroups known to the service
     * @param handle a valid session handle
     * @return the list of org groups as an XML string
     * @throws IOException if the service can't be reached
     */
    public String getOrgGroups(String handle) throws IOException {
        return performGet("getOrgGroups", handle) ;
    }


    /**
     * Gets an XML list of the full name of each Participant known to the service
     * @param handle a valid session handle
     * @return the list of participant names as an XML string
     * @throws IOException if the service can't be reached
     */
    public String getAllParticipantNames(String handle) throws IOException {
        return performGet("getAllParticipantNames", handle) ;
    }


    /**
     * Gets an XML list of the  name of each Role known to the service
     * @param handle a valid session handle
     * @return the list of role names as an XML string
     * @throws IOException if the service can't be reached
     */
    public String getAllRoleNames(String handle) throws IOException {
        return performGet("getAllRoleNames", handle) ;
    }


    /**
     * Gets an XML list of the Participants that are currently logged on to the service
     * @param handle a valid session handle
     * @return the list of participants as an XML string
     * @throws IOException if the service can't be reached
     */
    public String getActiveParticipants(String handle) throws IOException {
        return performGet("getActiveParticipants", handle) ;
    }


    /**
     * Gets a specific Participant
     * @param id the id of the participant to retrieve
     * @param handle a valid session handle
     * @return an XML string describing the participant
     * @throws IOException if the service can't be reached
     */
    public String getParticipant(String id, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getParticipant", handle);
        params.put("id", id);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Gets all the Roles the specified Participant is a member of
     * @param id the id of the participant
     * @param handle a valid session handle
     * @return an XML string describing the roles
     * @throws IOException if the service can't be reached
     */
    public String getParticipantRoles(String id, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getParticipantRoles", handle);
        params.put("id", id);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Gets all the Capabilities held by the specified Participant
     * @param id the id of the participant
     * @param handle a valid session handle
     * @return an XML string describing the capabilities
     * @throws IOException if the service can't be reached
     */
    public String getParticipantCapabilities(String id, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getParticipantCapabilities", handle);
        params.put("id", id);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Gets all the Positions held by the specified Participant
     * @param id the id of the participant
     * @param handle a valid session handle
     * @return an XML string describing the positions
     * @throws IOException if the service can't be reached
     */
    public String getParticipantPositions(String id, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getParticipantPositions", handle);
        params.put("id", id);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Gets all the participants holding the specified role (by name)
     * @param name the name of the role (not the id)
     * @param handle a valid session handle
     * @return an XML string describing the participants
     * @throws IOException if the service can't be reached
     */
    public String getParticipantsWithRole(String name, String handle)  throws IOException {
        Map<String, String> params = prepareParamMap("getParticipantsWithRole", handle);
        params.put("name", name);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Gets all the participants holding the specified position (by name)
     * @param name the name of the position (not the id)
     * @param handle a valid session handle
     * @return an XML string describing the participants
     * @throws IOException if the service can't be reached
     */
    public String getParticipantsWithPosition(String name, String handle)  throws IOException {
        Map<String, String> params = prepareParamMap("getParticipantsWithPosition", handle);
        params.put("name", name);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Gets all the participants holding the specified capability (by name)
     * @param name the name of the capability (not the id)
     * @param handle a valid session handle
     * @return an XML string describing the participants
     * @throws IOException if the service can't be reached
     */
    public String getParticipantsWithCapability(String name, String handle)  throws IOException {
        Map<String, String> params = prepareParamMap("getParticipantsWithCapability", handle);
        params.put("name", name);
        return executeGet(_serviceURI, params) ;
    }

    
    /**
     * Gets an XML list of the Codelets known to the service
     * @param handle a valid session handle
     * @return the list of codelets as an XML string
     * @throws IOException if the service can't be reached
     */
    public String getCodelets(String handle) throws IOException {
        return performGet("getCodelets", handle);
    }

    /*****************************************************************************/

    // GET METHODS - returning boolean //

    /**
     * Check if the id passed is that of a known Participant
     * @param participantID the id to check
     * @param handle a valid session handle
     * @return "true" if the id is valid, "false" if otherwise
     * @throws IOException if the service can't be reached
     */
    public String isKnownParticipant(String participantID, String handle)
                                                                throws IOException {
        return executeBooleanGet("isKnownParticipant", participantID, handle) ;
    }


    /**
     * Check if the id passed is that of a known Role
     * @param roleID the id to check
     * @param handle a valid session handle
     * @return "true" if the id is valid, "false" if otherwise
     * @throws IOException if the service can't be reached
     */
    public String isKnownRole(String roleID, String handle) throws IOException {
        return executeBooleanGet("isKnownRole", roleID, handle) ;
    }


    /**
     * Check if the id passed is that of a known Capability
     * @param capabilityID the id to check
     * @param handle a valid session handle
     * @return "true" if the id is valid, "false" if otherwise
     * @throws IOException if the service can't be reached
     */
    public String isKnownCapability(String capabilityID, String handle)
                                                                throws IOException {
        return executeBooleanGet("isKnownCapability", capabilityID, handle) ;
    }


    /**
     * Check if the id passed is that of a known Position
     * @param positionID the id to check
     * @param handle a valid session handle
     * @return "true" if the id is valid, "false" if otherwise
     * @throws IOException if the service can't be reached
     */
    public String isKnownPosition(String positionID, String handle) throws IOException {
        return executeBooleanGet("isKnownPosition", positionID, handle) ;
    }


    /**
     * Check if the id passed is that of a known OrgGroup
     * @param orgGroupID the id to check
     * @param handle a valid session handle
     * @return "true" if the id is valid, "false" if otherwise
     * @throws IOException if the service can't be reached
     */
    public String isKnownOrgGroup(String orgGroupID, String handle) throws IOException {
        return executeBooleanGet("isKnownOrgGroup", orgGroupID, handle) ;
    }


    /*******************************************************************************/

    /**
     * Connects an external entity to the resource service
     * @param userID the userid
     * @param password the corresponding password
     * @return a sessionHandle if successful, or a failure message if not
     * @throws IOException if the service can't be reached
     */
    public String connect(String userID, String password) throws IOException {
        Map<String, String> params = prepareParamMap("connect", null);
        params.put("userid", userID);
        params.put("password", password);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Check that a session handle is active
     * @param handle the session handle to check
     * @return "true" if the id is valid, "false" if otherwise
     * @throws IOException if the service can't be reached
     */
    public String checkConnection(String handle) throws IOException {
        return executeGet(_serviceURI, prepareParamMap("checkConnection", handle)) ;
    }


    /**
     * Disconnects an external entity from the resource service
     * @param handle the sessionHandle to disconnect
     * @throws IOException if the service can't be reached
     */
    public void disconnect(String handle) throws IOException {
        executePost(_serviceURI, prepareParamMap("disconnect", handle));
    }

    /******************************************************************************/

    // POST METHODS //

    /** Triggers a reload of org data
     * @param handle a valid session handle with admin access
     * @throws IOException if the service can't be reached
     */
    public void refreshOrgDataSet(String handle) throws IOException {
        executePost(_serviceURI, prepareParamMap("refreshOrgDataSet", handle));
    }

    
    /** (re)sets the timer for automatic org data reloads
     * @param minutes the number of minutes to wait between refreshes
     * @param handle a valid session handle with admin access
     * @throws IOException if the service can't be reached
     */
    public void resetOrgDataRefreshRate(int minutes, String handle) throws IOException {
        Map<String,String> params = prepareParamMap("resetOrgDataRefreshRate", handle);
        params.put("rate", String.valueOf(minutes));
        executePost(_serviceURI, params);
    }

    
}
