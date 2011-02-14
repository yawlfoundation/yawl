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

import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.util.PasswordEncryptor;

import java.io.IOException;
import java.util.Map;

/**
 * An API to be used by clients for retrieving organisational model data from
 * the Resource Service.
 *
 *  @author Michael Adams
 *  14/08/2007
 */

public class ResourceGatewayClient extends Interface_Client {

    /** the uri of the resource service's __ resource gateway__
     * a default would be "http://localhost:8080/resourceService/gateway"
     */
    private String _serviceURI ;

    /** empty constructor */
    public ResourceGatewayClient() {
        super();
    }

    /** constructor
     * @param uri the uri of the resourceService's gateway
     */
    public ResourceGatewayClient(String uri) {
        super();
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
     * Gets the id and name of every object of the type specified
     * @param type the name of the object required
     * @param format how to format the output; valid values are "XML" or "JSON"
     * @param handle a valid session handle
     * @return an XML string describing the OrgGroup id/name pairs, or an appropriate
     * error message
     * @throws IOException if the service can't be reached
     */
    private String getIdentifiers(String type, String format, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("get" + type + "Identifiers", handle);
        if (format != null) params.put("format", format);
        return executeGet(_serviceURI, params) ;
    }


    /***********************************************************************************/

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
     * Gets an XML list of all NonHumanResources known to the service
     * @param handle a valid session handle
     * @return the list of NonHumanResources as an XML string
     * @throws IOException if the service can't be reached
     */
    public String getNonHumanResources(String handle) throws IOException {
        return performGet("getNonHumanResources", handle) ;
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
     * Gets an XML list of the name of each NonHumanResource known to the service
     * @param handle a valid session handle
     * @return the list of NonHumanResource names as an XML string
     * @throws IOException if the service can't be reached
     */
    public String getAllNonHumanResourceNames(String handle) throws IOException {
        return performGet("getAllNonHumanResourceNames", handle) ;
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
     * Gets a specific NonHumanResource
     * @param id the id of the NonHumanResource to retrieve
     * @param handle a valid session handle
     * @return an XML string describing the NonHumanResource
     * @throws IOException if the service can't be reached
     */
    public String getNonHumanResource(String id, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getNonHumanResource", handle);
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
     * Gets the participant with the specified user id
     * @param userID the userid to match to a participant
     * @param handle a valid session handle
     * @return an XML string describing the participant, or an appropriate error message
     * @throws IOException if the service can't be reached
     */
    public String getParticipantFromUserID(String userID, String handle)  throws IOException {
        Map<String, String> params = prepareParamMap("getParticipantFromUserID", handle);
        params.put("id", userID);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Gets the role with the specified id
     * @param id the id of the role to get
     * @param handle a valid session handle
     * @return an XML string describing the role, or an appropriate error message
     * @throws IOException if the service can't be reached
     */
    public String getRole(String id, String handle)  throws IOException {
        Map<String, String> params = prepareParamMap("getRole", handle);
        params.put("id", id);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Gets the role with the specified name
     * @param name the name of the role to get
     * @param handle a valid session handle
     * @return an XML string describing the role, or an appropriate error message
     * @throws IOException if the service can't be reached
     */
    public String getRoleByName(String name, String handle)  throws IOException {
        Map<String, String> params = prepareParamMap("getRoleByName", handle);
        params.put("name", name);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Gets the Capability with the specified id
     * @param id the id of the Capability to get
     * @param handle a valid session handle
     * @return an XML string describing the Capability, or an appropriate error message
     * @throws IOException if the service can't be reached
     */
    public String getCapability(String id, String handle)  throws IOException {
        Map<String, String> params = prepareParamMap("getCapability", handle);
        params.put("id", id);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Gets the Capability with the specified name
     * @param name the name of the Capability to get
     * @param handle a valid session handle
     * @return an XML string describing the Capability, or an appropriate error message
     * @throws IOException if the service can't be reached
     */
    public String getCapabilityByName(String name, String handle)  throws IOException {
        Map<String, String> params = prepareParamMap("getCapabilityByName", handle);
        params.put("name", name);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Gets the Position with the specified id
     * @param id the id of the Position to get
     * @param handle a valid session handle
     * @return an XML string describing the Position, or an appropriate error message
     * @throws IOException if the service can't be reached
     */
    public String getPosition(String id, String handle)  throws IOException {
        Map<String, String> params = prepareParamMap("getPosition", handle);
        params.put("id", id);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Gets the Position with the specified name
     * @param name the name of the Position to get
     * @param handle a valid session handle
     * @return an XML string describing the Position, or an appropriate error message
     * @throws IOException if the service can't be reached
     */
    public String getPositionByName(String name, String handle)  throws IOException {
        Map<String, String> params = prepareParamMap("getPositionByName", handle);
        params.put("name", name);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Gets the OrgGroup with the specified id
     * @param id the id of the OrgGroup to get
     * @param handle a valid session handle
     * @return an XML string describing the OrgGroup, or an appropriate error message
     * @throws IOException if the service can't be reached
     */
    public String getOrgGroup(String id, String handle)  throws IOException {
        Map<String, String> params = prepareParamMap("getOrgGroup", handle);
        params.put("id", id);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Gets the OrgGroup with the specified name
     * @param name the name of the OrgGroup to get
     * @param handle a valid session handle
     * @return an XML string describing the OrgGroup, or an appropriate error message
     * @throws IOException if the service can't be reached
     */
    public String getOrgGroupByName(String name, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getOrgGroupByName", handle);
        params.put("name", name);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Gets the NonHumanResource with the specified name
     * @param name the name of the NonHumanResource to get
     * @param handle a valid session handle
     * @return an XML string describing the NonHumanResource, or an appropriate error message
     * @throws IOException if the service can't be reached
     */
    public String getNonHumanResourceByName(String name, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getNonHumanResourceByName", handle);
        params.put("name", name);
        return executeGet(_serviceURI, params) ;
    }    


    /**
     * Gets the id and chosen identifier of every Participant
     * @param id a valid ResourceDataSet.Identifier; one of: FullName, ReverseFullName,
     * LastName or Userid  (0..3)
     * @param format how to format the output; valid values are "XML" or "JSON"
     * @param handle a valid session handle
     * @return a string describing the Participant id/name pairs, or an appropriate
     * error message
     * @throws IOException if the service can't be reached
     */
    public String getParticipantIdentifiers(int id, String format, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("getParticipantIdentifiers", handle);
        params.put("id", String.valueOf(id));
        if (format != null) params.put("format", format);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Gets the id and chosen identifier of every Participant
     * @param format how to format the output; valid values are "XML" or "JSON"
     * @param handle a valid session handle
     * @return a string describing the Participant id/name pairs, or an appropriate
     * error message
     * @throws IOException if the service can't be reached
     */
    public String getParticipantIdentifiers(String format, String handle)
            throws IOException {
        return getParticipantIdentifiers(0, format, handle);
    }


    /**
     * Gets the id and chosen identifier of every Participant
     * @param id a valid ResourceDataSet.Identifier; one of: FullName, ReverseFullName,
     * LastName or Userid  (0..3)
     * @param handle a valid session handle
     * @return an XML string describing the Participant id/name pairs, or an appropriate
     * error message
     * @throws IOException if the service can't be reached
     */
    public String getParticipantIdentifiers(int id, String handle) throws IOException {
        return getParticipantIdentifiers(id, null, handle);
    }


    /**
     * Gets the id and fullname of every Participant
     * @param handle a valid session handle
     * @return an XML string describing the Participant id/name pairs, or an appropriate
     * error message
     * @throws IOException if the service can't be reached
     */
    public String getParticipantIdentifiers(String handle) throws IOException {
        return getParticipantIdentifiers(0, null, handle);
    }


    /**
     * Gets the id and name of every NonHumanResource
     * @param handle a valid session handle
     * @return an XML string describing the NonHumanResource id/name pairs, or an appropriate
     * error message
     * @throws IOException if the service can't be reached
     */
    public String getNonHumanResourceIdentifiers(String handle) throws IOException {
        return getNonHumanResourceIdentifiers(null, handle);
    }


    /**
     * Gets the id and name of every NonHumanResource
     * @param format how to format the output; valid values are "XML" or "JSON"
     * @param handle a valid session handle
     * @return an XML string describing the NonHumanResource id/name pairs, or an appropriate
     * error message
     * @throws IOException if the service can't be reached
     */
    public String getNonHumanResourceIdentifiers(String format, String handle) throws IOException {
        return getIdentifiers("NonHumanResource", format, handle);
    }


    /**
     * Gets the id and name of every Role
     * @param handle a valid session handle
     * @return an XML string describing the Role id/name pairs, or an appropriate
     * error message
     * @throws IOException if the service can't be reached
     */
    public String getRoleIdentifiers(String handle) throws IOException {
        return getRoleIdentifiers(null, handle);
    }


    /**
     * Gets the id and name of every Role
     * @param format how to format the output; valid values are "XML" or "JSON"
     * @param handle a valid session handle
     * @return an XML string describing the Role id/name pairs, or an appropriate
     * error message
     * @throws IOException if the service can't be reached
     */
    public String getRoleIdentifiers(String format, String handle) throws IOException {
        return getIdentifiers("Role", format, handle);
    }


    /**
     * Gets the id and name of every Position
     * @param handle a valid session handle
     * @return an XML string describing the Position id/name pairs, or an appropriate
     * error message
     * @throws IOException if the service can't be reached
     */
    public String getPositionIdentifiers(String handle) throws IOException {
        return getPositionIdentifiers(null, handle);
    }


    /**
     * Gets the id and name of every Position
     * @param format how to format the output; valid values are "XML" or "JSON"
     * @param handle a valid session handle
     * @return an XML string describing the Position id/name pairs, or an appropriate
     * error message
     * @throws IOException if the service can't be reached
     */
    public String getPositionIdentifiers(String format, String handle) throws IOException {
        return getIdentifiers("Position", format, handle);
    }


    /**
     * Gets the id and name of every Capability
     * @param handle a valid session handle
     * @return an XML string describing the Capability id/name pairs, or an appropriate
     * error message
     * @throws IOException if the service can't be reached
     */
    public String getCapabilityIdentifiers(String handle) throws IOException {
        return getCapabilityIdentifiers(null, handle);
    }


    /**
     * Gets the id and name of every Capability
     * @param format how to format the output; valid values are "XML" or "JSON"
     * @param handle a valid session handle
     * @return an XML string describing the Capability id/name pairs, or an appropriate
     * error message
     * @throws IOException if the service can't be reached
     */
    public String getCapabilityIdentifiers(String format, String handle) throws IOException {
        return getIdentifiers("Capability", format, handle);
    }


    /**
     * Gets the id and name of every OrgGroup
     * @param handle a valid session handle
     * @return an XML string describing the OrgGroup id/name pairs, or an appropriate
     * error message
     * @throws IOException if the service can't be reached
     */
    public String getOrgGroupIdentifiers(String handle) throws IOException {
        return getOrgGroupIdentifiers(null, handle);
    }


    /**
     * Gets the id and name of every OrgGroup
     * @param format how to format the output; valid values are "XML" or "JSON"
     * @param handle a valid session handle
     * @return an XML string describing the OrgGroup id/name pairs, or an appropriate
     * error message
     * @throws IOException if the service can't be reached
     */
    public String getOrgGroupIdentifiers(String format, String handle) throws IOException {
        return getIdentifiers("OrgGroup", format, handle);
    }


    /**
     * Gets the UserPrivileges of the participant with the specified id
     * @param id the id of the Participant to get the user privileges of
     * @param handle a valid session handle
     * @return an XML string describing the UserPrivileges, or an appropriate error message
     * @throws IOException if the service can't be reached
     */
    public String getUserPrivileges(String id, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getUserPrivileges", handle);
        params.put("id", id);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Gets the NonHumanCategory with the specified id
     * @param id a valid NonHumanCategory identifier
     * @param handle a valid session handle
     * @return an XML string describing the category, or an appropriate error message
     * @throws IOException if the service can't be reached
     */
    public String getNonHumanCategory(String id, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getNonHumanCategory", handle);
        params.put("id", id);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Gets the NonHumanCategory with the specified name
     * @param name a valid NonHumanCategory name
     * @param handle a valid session handle
     * @return an XML string describing the category, or an appropriate error message
     * @throws IOException if the service can't be reached
     */
    public String getNonHumanCategoryByName(String name, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("getNonHumanCategoryByName",
                handle);
        params.put("name", name);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Gets all the NonHumanResource categories known to the service
     * @param handle a valid session handle
     * @return an XML string describing the categories, or an appropriate error message
     * @throws IOException if the service can't be reached
     */
    public String getNonHumanCategories(String handle) throws IOException {
        return getNonHumanCategories(null, handle);
    }


    /**
     * Gets all the NonHumanResource categories known to the service
     * @param format how to format the output; valid values are "XML" or "JSON"
     * @param handle a valid session handle
     * @return an XML string describing the categories, or an appropriate error message
     * @throws IOException if the service can't be reached
     */
    public String getNonHumanCategories(String format, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getNonHumanCategories", handle);
        if (format != null) params.put("format", format);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Gets all the NonHumanResource sub-categories for the specified category
     * @param category the category to get the sub-categories for 
     * @param handle a valid session handle
     * @return an XML string describing the categories, or an appropriate error message
     * @throws IOException if the service can't be reached
     */
    public String getNonHumanSubCategories(String category, String handle)
            throws IOException {
        return getNonHumanSubCategories(category, null, handle);
    }


    /**
     * Gets all the NonHumanResource sub-categories for the specified category
     * @param category the category to get the sub-categories for
     * @param format how to format the output; valid values are "XML" or "JSON"
     * @param handle a valid session handle
     * @return an XML string describing the categories, or an appropriate error message
     * @throws IOException if the service can't be reached
     */
    public String getNonHumanSubCategories(String category, String format, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("getNonHumanSubCategories", handle);
        params.put("category", category);
        if (format != null) params.put("format", format);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Gets all the NonHumanResource categories known to the service, together with
     * each of their subcategories
     * @param handle a valid session handle
     * @return an XML string describing the categories, or an appropriate error message
     * @throws IOException if the service can't be reached
     */
    public String getNonHumanCategorySet(String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getNonHumanCategorySet", handle);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Checks the user credentials passed against those stored
     * @param userid the userid of the Participant
     * @param password the associated password
     * @param checkForAdmin when true, also checks that the user has admin privileges
     * @param handle a valid session handle
     * @return an XML message of success or an appropriate error message
     * @throws IOException if the service can't be reached
     */
    public String validateUserCredentials(String userid, String password,
                              boolean checkForAdmin, String handle)  throws IOException {
        Map<String, String> params = prepareParamMap("validateUserCredentials", handle);
        params.put("userid", userid);
        params.put("password", password);
        params.put("checkForAdmin", String.valueOf(checkForAdmin));
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


    /**
     * Gets an XML list of the required parameters for the specified codelet
     * @param codeletName the simple name of the codelet
     * @param handle a valid session handle
     * @return the list of the codelet's required parameters as an XML string, or an
     * error message if the codelet could not be found
     * @throws IOException if the service can't be reached
     */
    public String getCodeletParameters(String codeletName, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("getCodeletParameters", handle);
        params.put("name", codeletName);
        return executeGet(_serviceURI, params) ;
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


    /**
     * Check if the id passed is that of a known NonHumanResource
     * @param id the id to check
     * @param handle a valid session handle
     * @return "true" if the id is valid, "false" if otherwise
     * @throws IOException if the service can't be reached
     */
    public String isKnownNonHumanResource(String id, String handle) throws IOException {
        return executeBooleanGet("isKnownNonHumanResource", id, handle) ;
    }


    /**
     * Check if the id passed is that of a known NonHumanCategory
     * @param id the id to check
     * @param handle a valid session handle
     * @return "true" if the id is valid, "false" if otherwise
     * @throws IOException if the service can't be reached
     */
    public String isKnownNonHumanCategory(String id, String handle) throws IOException {
        return executeBooleanGet("isKnownNonHumanCategory", id, handle) ;
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
        params.put("password", PasswordEncryptor.encrypt(password, null));
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

    // ADD, UPDATE & REMOVE METHODS

    /**
     * Adds a new Participant to the Resource Service's org data
     * @param userid - must not already exist in the service's org data
     * @param password
     * @param encrypt - if true, will encrypt the password before it is stored
     * @param lastname
     * @param firstname
     * @param admin - if true, will set this Participant as an administrator
     * @param description
     * @param notes
     * @param handle a current sessionhandle with admin privileges
     * @return if successful, the id of the newly added participant; if not, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String addParticipant(String userid, String password, boolean encrypt,
                                 String lastname, String firstname, boolean admin,
                                 String description, String notes, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("addParticipant", handle);
        params.put("userid", userid);
        params.put("password", password);
        params.put("encrypt", String.valueOf(encrypt));
        params.put("lastname", lastname);
        params.put("firstname", firstname);
        params.put("admin", String.valueOf(admin));
        params.put("description", description);
        params.put("notes", notes);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Adds a new NonHumanResource to the Resource Service's org data
     * @param name - must not already exist in the service's org data
     * @param category
     * @param subcategory
     * @param description
     * @param notes
     * @param handle a current sessionhandle with admin privileges
     * @return if successful, the id of the newly added NonHumanResource; if not, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String addNonHumanResource(String name, String category, String subcategory,
                 String description, String notes, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("addNonHumanResource", handle);
        params.put("name", name);
        params.put("category", category);
        params.put("subcategory", subcategory);
        params.put("description", description);
        params.put("notes", notes);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Adds a new Capability to the Resource Service's org data
     * @param name - must not already exist in the service's org data
     * @param description
     * @param notes
     * @param handle a current sessionhandle with admin privileges
     * @return if successful, the id of the newly added capability; if not, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String addCapability(String name, String description, String notes,
                                String handle) throws IOException {
        Map<String, String> params = prepareParamMap("addCapability", handle);
        params.put("name", name);
        params.put("description", description);
        params.put("notes", notes);
        return executeGet(_serviceURI, params) ;        
    }


    /**
     * Adds a new Role to the Resource Service's org data
     * @param name - must not already exist in the service's org data
     * @param description
     * @param notes
     * @param containingRoleID the id of the Role this Role will 'belong to' (may be null)
     * @param handle a current sessionhandle with admin privileges
     * @return if successful, the id of the newly added role; if not, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String addRole(String name, String description, String notes,
                          String containingRoleID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("addRole", handle);
        params.put("name", name);
        params.put("description", description);
        params.put("notes", notes);
        params.put("containingroleid", containingRoleID);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Adds a new Position to the Resource Service's org data
     * @param name - must not already exist in the service's org data
     * @param description
     * @param notes
     * @param orgGroupID the id of the OrgGroup this Position will 'belong to'
     * (may be null)
     * @param containingPositionID the id of the Position this Position will 'report to'
     * (may be null)
     * @param handle a current sessionhandle with admin privileges
     * @return if successful, the id of the newly added position; if not, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String addPosition(String name, String positionID, String description,
                              String notes, String orgGroupID, String containingPositionID,
                              String handle) throws IOException {
        Map<String, String> params = prepareParamMap("addPosition", handle);
        params.put("name", name);
        params.put("positionid", positionID);
        params.put("orggroupid", orgGroupID);
        params.put("description", description);
        params.put("notes", notes);
        params.put("containingpositionid", containingPositionID);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Adds a new OrgGroup to the Resource Service's org data
     * @param name - must not already exist in the service's org data
     * @param groupType one of the OrgGroup.GroupType's
     * @param description
     * @param notes
     * @param containingGroupID the id of the OrgGroup this OrgGroup will 'belong to'
     * (may be null)
     * @param handle a current sessionhandle with admin privileges
     * @return if successful, the id of the newly added group; if not, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String addOrgGroup(String name, String groupType, String description,
                              String notes, String containingGroupID,
                              String handle) throws IOException {
        Map<String, String> params = prepareParamMap("addOrgGroup", handle);
        params.put("name", name);
        params.put("grouptype", groupType);
        params.put("description", description);
        params.put("notes", notes);
        params.put("containinggroupid", containingGroupID);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Adds a Participant to the set of Participants occupying a Role
     * @param participantID the (existing) id of the Participant to add
     * @param roleID the (existing) id of the Role to add the Participant to
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String addParticipantToRole(String participantID, String roleID,
                                       String handle) throws IOException {
        Map<String, String> params = prepareParamMap("addParticipantToRole", handle);
        params.put("participantid", participantID);
        params.put("roleid", roleID);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Adds a Participant to the set of Participants holding a Capability
     * @param participantID the (existing) id of the Participant to add
     * @param capabilityID the (existing) id of the Capability to add the Participant to
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String addParticipantToCapability(String participantID, String capabilityID,
                                       String handle) throws IOException {
        Map<String, String> params = prepareParamMap("addParticipantToCapability", handle);
        params.put("participantid", participantID);
        params.put("capabilityid", capabilityID);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Adds a Participant to the set of Participants holding a Position
     * @param participantID the (existing) id of the Participant to add
     * @param positionID the (existing) id of the Position to add the Participant to
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String addParticipantToPosition(String participantID, String positionID,
                                           String handle) throws IOException {
        Map<String, String> params = prepareParamMap("addParticipantToPosition", handle);
        params.put("participantid", participantID);
        params.put("positionid", positionID);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Adds a new category to the set of NonHumanResource categories
     * @param category the category name to add
     * @param handle a current sessionhandle with admin privileges
     * @return a String conversion of the (long) category key value, or
     * if not successful, an explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String addNonHumanCategory(String category, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("addNonHumanCategory", handle);
        params.put("category", category);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Adds a new subcategory to the specified NonHumanResource category. If the
     * category does not exist, it will be created
     * @param category the (existing) category name
     * @param subcategory the subcategory name to add
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String addNonHumanSubCategoryByName(String category, String subcategory,
                                                 String handle) throws IOException {
        Map<String, String> params = prepareParamMap("addNonHumanSubCategory", handle);
        params.put("category", category);
        params.put("subcategory", subcategory);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Adds a new subcategory to the specified NonHumanResource category
     * @param id the id of the (existing) category
     * @param subcategory the subcategory name to add
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String addNonHumanSubCategory(String id, String subcategory,
                                                 String handle) throws IOException {
        Map<String, String> params = prepareParamMap("addNonHumanSubCategory", handle);
        params.put("id", id);
        params.put("subcategory", subcategory);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Updates a Participant record with the specified values. Note that besides
     * participantid and handle, all other String parameters may be null, which will
     * indicate that the current value for that field is not to be updated (ie. the
     * existing value is maintained).
     * @param participantID the (existing) id of the Participant
     * @param userid
     * @param password
     * @param encrypt - if true, will encrypt the password before it is stored
     * @param lastname
     * @param firstname
     * @param admin - if true, will set this Participant as an administrator
     * @param description
     * @param notes
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String updateParticipant(String participantID, String userid, String password,
                                    boolean encrypt, String lastname,String firstname,
                                    boolean admin, String description, String notes,
                                    String handle) throws IOException {
        Map<String, String> params = prepareParamMap("updateParticipant", handle);
        params.put("participantid", participantID);
        params.put("userid", userid);
        params.put("password", password);
        params.put("encrypt", String.valueOf(encrypt));
        params.put("lastname", lastname);
        params.put("firstname", firstname);
        params.put("admin", String.valueOf(admin));
        params.put("description", description);
        params.put("notes", notes);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Updates a NonHumanResource with the specified values. Note that besides
     * resourceID and handle, all other String parameters may be null, which will
     * indicate that the current value for that field is not to be updated (ie. the
     * existing value is maintained).
     * @param resourceID - the (existing) id of the NonHumanResource
     * @param name
     * @param category
     * @param subcategory
     * @param description
     * @param notes
     * @param handle a current sessionhandle with admin privileges
     * @return  a message indicating success, or if not, an explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String updateNonHumanResource(String resourceID, String name, String category,
                    String subcategory, String description, String notes, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("updateNonHumanResource", handle);
        params.put("resourceID", resourceID);
        params.put("name", name);
        params.put("category", category);
        params.put("subcategory", subcategory);
        params.put("description", description);
        params.put("notes", notes);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Updates a Capability record with the specified values. Note that besides
     * capabilityID and handle, all other parameters may be null, which will
     * indicate that the current value for that field is not to be updated (ie. the
     * existing value is maintained).
     * @param capabilityID the (existing) id of the Capability
     * @param description
     * @param notes
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String updateCapability(String capabilityID, String name, String description,
                                   String notes, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("updateCapability", handle);
        params.put("capabilityid", capabilityID);
        params.put("name", name);
        params.put("description", description);
        params.put("notes", notes);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Updates a Role record with the specified values. Note that besides
     * roleID and handle, all other parameters may be null, which will
     * indicate that the current value for that field is not to be updated (ie. the
     * existing value is maintained).
     * @param roleID the (existing) id of the Capability
     * @param name
     * @param description
     * @param notes
     * @param containingRoleID the id of the Role this Role will 'belong to'
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String updateRole(String roleID, String name, String description, String notes,
                             String containingRoleID, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("updateRole", handle);
        params.put("roleid", roleID);
        params.put("name", name);
        params.put("description", description);
        params.put("notes", notes);
        params.put("containingroleid", containingRoleID);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Updates a Position record with the specified values. Note that besides
     * posID and handle, all other parameters may be null, which will
     * indicate that the current value for that field is not to be updated (ie. the
     * existing value is maintained).
     * @param posID the (existing) id of the Position
     * @param name
     * @param description
     * @param notes
     * @param orgGroupID the id of the OrgGroup this Position will 'belong to' 
     * @param containingPositionID the id of the Position this Position will 'report to'
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String updatePosition(String posID, String name, String positionID,
                                 String description, String notes, String orgGroupID,
                                 String containingPositionID, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("updatePosition", handle);
        params.put("posid", posID);
        params.put("name", name);
        params.put("positionid", positionID);
        params.put("orggroupid", orgGroupID);
        params.put("description", description);
        params.put("notes", notes);
        params.put("containingpositionid", containingPositionID);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Updates an OrgGroup record with the specified values. Note that besides
     * groupID and handle, all other parameters may be null, which will
     * indicate that the current value for that field is not to be updated (ie. the
     * existing value is maintained).
     * @param groupID the (existing) id of the OrgGroup
     * @param name
     * @param groupType one of the OrgGroup.GroupType's
     * @param description
     * @param notes
     * @param containingGroupID the id of the OrgGroup this OrgGroup will 'belong to'
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String updateOrgGroup(String groupID, String name, String groupType,
                                 String description, String notes, String containingGroupID,
                                 String handle) throws IOException {
        Map<String, String> params = prepareParamMap("updateOrgGroup", handle);
        params.put("groupid", groupID);
        params.put("name", name);
        params.put("grouptype", groupType);
        params.put("description", description);
        params.put("notes", notes);
        params.put("containinggroupid", containingGroupID);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Updates a NonHumanCategory with the specified values. Note that besides
     * categoryid and handle, all other String parameters may be null, which will
     * indicate that the current value for that field is not to be updated (ie. the
     * existing value is maintained).
     * @param categoryID the (existing) id of the NonHumanCategory
     * @param name
     * @param description
     * @param notes
     * @param handle a current sessionhandle with admin privileges
     * @return  a message indicating success, or if not, an explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String updateNonHumanCategory(String categoryID, String name,
                                         String description, String notes, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("updateNonHumanCategory", handle);
        params.put("categoryID", categoryID);
        params.put("name", name);
        params.put("description", description);
        params.put("notes", notes);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Removes (deletes) the specified Participant from the Service's org data
     * @param participantID the id of the Participant to remove
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String removeParticipant(String participantID, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("removeParticipant", handle);
        params.put("participantid", participantID);
        return executeGet(_serviceURI, params) ;        
    }


    /**
     * Removes (deletes) the specified NonHumanResource from the Service's org data
     * @param resourceID the id of the Participant to remove
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String removeNonHumanResource(String resourceID, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("removeNonHumanResource", handle);
        params.put("resourceid", resourceID);     
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Removes (deletes) the specified Capability from the Service's org data
     * @param capabilityID the id of the Capability to remove
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String removeCapability(String capabilityID, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("removeCapability", handle);
        params.put("capabilityid", capabilityID);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Removes (deletes) the specified Role from the Service's org data
     * @param roleID the id of the Role to remove
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String removeRole(String roleID, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("removeRole", handle);
        params.put("roleid", roleID);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Removes (deletes) the specified Position from the Service's org data
     * @param positionID the id of the Position to remove
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String removePosition(String positionID, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("removePosition", handle);
        params.put("positionid", positionID);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Removes (deletes) the specified OrgGroup from the Service's org data
     * @param groupID the id of the OrgGroup to remove
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String removeOrgGroup(String groupID, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("removeOrgGroup", handle);
        params.put("groupid", groupID);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Removes (deletes) the specified Participant from the set of Participants occupying
     * the specified Role
     * @param participantID the id of the Participant to remove
     * @param roleID the id of the Role to remove the Particpant from
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String removeParticipantFromRole(String participantID, String roleID,
                                            String handle) throws IOException {
        Map<String, String> params = prepareParamMap("removeParticipantFromRole", handle);
        params.put("participantid", participantID);
        params.put("roleid", roleID);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Removes (deletes) the specified Participant from the set of Participants holding
     * the specified Capability
     * @param participantID the id of the Participant to remove
     * @param capabilityID the id of the Capability to remove the Particpant from
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String removeParticipantFromCapability(String participantID, String capabilityID,
                                       String handle) throws IOException {
        Map<String, String> params = prepareParamMap("removeParticipantFromCapability", handle);
        params.put("participantid", participantID);
        params.put("capabilityid", capabilityID);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Removes (deletes) the specified Participant from the set of Participants occupying
     * the specified Position
     * @param participantID the id of the Participant to remove
     * @param positionID the id of the Position to remove the Particpant from
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String removeParticipantFromPosition(String participantID, String positionID,
                                           String handle) throws IOException {
        Map<String, String> params = prepareParamMap("removeParticipantFromPosition", handle);
        params.put("participantid", participantID);
        params.put("positionid", positionID);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Removes the specified category from the set of NonHumanResource categories; any
     * sub-categories of the specified categories are also removed.
     * @param id the identifier of the category to remove
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String removeNonHumanCategory(String id, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("removeNonHumanCategory", handle);
        params.put("id", id);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Removes the specified category from the set of NonHumanResource categories; any
     * sub-categories of the specified categories are also removed.
     * @param category the name of the category to remove
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String removeNonHumanCategoryByName(String category, String handle)
            throws IOException {
        Map<String, String> params =
                prepareParamMap("removeNonHumanCategoryByName", handle);
        params.put("category", category);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Removes the specified category from the set of NonHumanResource categories; any
     * sub-categories of the specified categories are also removed.
     * @param category the name of the category of which the subcategory is a member
     * @param subcategory the name of the subcategory to remove
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String removeNonHumanSubCategoryByName(String category, String subcategory,
                                                    String handle) throws IOException {
        Map<String, String> params =
                prepareParamMap("removeNonHumanSubCategoryByName", handle);
        params.put("category", category);
        params.put("subcategory", subcategory);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Removes the specified category from the set of NonHumanResource categories; any
     * sub-categories of the specified categories are also removed.
     * @param id the identifier of the category of which the subcategory is a member
     * @param subcategory the name of the subcategory to remove
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String removeNonHumanSubCategory(String id, String subcategory,
                                                    String handle) throws IOException {
        Map<String, String> params = prepareParamMap("removeNonHumanSubCategory", handle);
        params.put("id", id);
        params.put("subcategory", subcategory);
        return executeGet(_serviceURI, params) ;
    }



    /***************************************************************************/

    // SET METHODS //

    /**
     * Sets the containing Role for a Role (i.e. the 'parent' Role the Role 'belongs to')
     * @param roleID the id of the 'child' Role
     * @param containingRoleID the id of the containing or parent Role that encapsulates
     * the 'child' Role
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String setContainingRole(String roleID, String containingRoleID, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("setContainingRole", handle);
        params.put("roleid", roleID);
        params.put("containingroleid", containingRoleID);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Sets the containing OrgGroup for an OrgGroup (i.e. the 'parent' OrgGroup the
     * OrgGroup 'belongs to')
     * @param groupID the id of the 'child' OrgGroup
     * @param containingGroupID the id of the containing or parent OrgGroup that
     * encapsulates the 'child' OrgGroup
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String setContainingOrgGroup(String groupID, String containingGroupID, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("setContainingOrgGroup", handle);
        params.put("groupid", groupID);
        params.put("containinggroupid", containingGroupID);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Sets the containing Position for a Position (i.e. the 'parent' Position the
     * Position 'reports to')
     * @param positionID the id of the 'child' Position
     * @param containingPositionID the id of the containing or parent Position that
     * encapsulates the 'child' Position
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String setContainingPosition(String positionID, String containingPositionID,
                                        String handle) throws IOException {
        Map<String, String> params = prepareParamMap("setContainingPosition", handle);
        params.put("positionid", positionID);
        params.put("containingpositionid", containingPositionID);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Sets the containing OrgGroup for a Position (i.e. the OrgGroup the
     * Position 'belongs to')
     * @param positionID the id of the Position
     * @param groupID the id of the containing OrgGroup
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String setPositionOrgGroup(String positionID, String groupID,
                                        String handle) throws IOException {
        Map<String, String> params = prepareParamMap("setPositionOrgGroup", handle);
        params.put("positionid", positionID);
        params.put("groupid", groupID);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Sets the user privileges of the specified participant individually
     * @param participantID the (existing) id of the participant
     * @param canChooseItemToStart
     * @param canStartConcurrent
     * @param canReorder
     * @param canViewTeamItems
     * @param canViewOrgGroupItems
     * @param canChainExecution
     * @param canManageCases
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String setParticipantPrivileges(String participantID,
                                           boolean canChooseItemToStart,
                                           boolean canStartConcurrent,
                                           boolean canReorder,
                                           boolean canViewTeamItems,
                                           boolean canViewOrgGroupItems,
                                           boolean canChainExecution,
                                           boolean canManageCases,
                                           String handle) throws IOException {
        StringBuilder bitstring = new StringBuilder();
        bitstring.append(canChooseItemToStart ? 1 : 0);
        bitstring.append(canStartConcurrent ? 1 : 0);
        bitstring.append(canReorder ? 1 : 0);
        bitstring.append(canViewTeamItems ? 1 : 0);
        bitstring.append(canViewOrgGroupItems ? 1 : 0);
        bitstring.append(canChainExecution ? 1 : 0);
        bitstring.append(canManageCases ? 1 : 0);
        bitstring.append(0);                                // carteblanche
        return setParticipantPrivileges(participantID, bitstring.toString(), handle);
    }


    /**
     * Sets the user privileges of the specified participant individually via a 'bit string'
     * @param participantID the (existing) id of the participant
     * @param bits an eight character string where each character corresponds to a
     * privilege (see the alternate setParticipantPrivileges() method for the ordering).
     * For each privilege, a character value of '1' indicates that the privilege
     * is to be granted and a character of any other value indicates that the privilege
     * is to be denied. The last character overrides all other privileges, so that a
     * value of '1' in the last position grants all privileges, while any other value
     * in the last position defers to the individual settings of each privilege.
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String setParticipantPrivileges(String participantID, String bits,
                                           String handle) throws IOException {
        if (bits.length() != 8) {
            return "<failure>There must be exactly 8 characters in the 'bits' " +
                   "String parameter</failure>";
        }
        Map<String, String> params = prepareParamMap("setParticipantPrivileges", handle);
        params.put("participantid", participantID);
        params.put("bitstring", bits);
        return executeGet(_serviceURI, params) ;
    }


    /**
     * Sets the user privileges of the specified participant on an 'all on' or 'all off'
     * basis
     * @param participantID the (existing) id of the participant
     * @param carteblanche if true, grants the participant all user privileges; if false,
     * denies the participant all user privileges
     * @param handle a current sessionhandle with admin privileges
     * @return a message indicating success, or if not successful, an
     * explanatory error message
     * @throws IOException if the service can't be reached
     */
    public String setParticipantPrivileges(String participantID,
                                           boolean carteblanche,
                                           String handle) throws IOException {
        String whitecard = carteblanche ? "1" : "0";
        return setParticipantPrivileges(participantID, "0000000" + whitecard, handle);               
    }


    /******************************************************************************/

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
