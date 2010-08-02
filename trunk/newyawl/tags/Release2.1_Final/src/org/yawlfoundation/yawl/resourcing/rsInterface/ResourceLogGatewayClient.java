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
 * An API to be used by clients that want to retrieve data from the resource service
 * process logs.
 *
 *  @author Michael Adams
 *  03/02/2010
 */

public class ResourceLogGatewayClient extends Interface_Client {

    /** the uri of the YAWL Engine's __logGateway__
     * a default would be "http://localhost:8080/resourceService/logGateway"
     */
    private String _logURI;

    /** the only constructor
     * @param uri the uri of the YAWL engine's log gateway
     */
    public ResourceLogGatewayClient(String uri) {
        _logURI = uri ;
    }

    /*******************************************************************************/

    // GET METHODS - returning String //

    /**
     * a wrapper for the executeGet method - accepts a String value
     * @param action the name of the gateway method to call
     * @param pName the name of the parameter passed
     * @param pValue the parameter's value
     * @param handle an active sessionhandle
     * @return the resultant String response (log data or error message)
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    private String performGet(String action, String pName, String pValue, String handle)
                                                                     throws IOException {
        Map<String, String> params = prepareParamMap(action, handle);
        if (pName != null) params.put(pName, pValue);
        return executeGet(_logURI, params);
    }


    /**
     * a wrapper for the executeGet method - accepts a long value
     * @param action the name of the gateway method to call
     * @param pName the name of the parameter passed
     * @param pValue the parameter's value
     * @param handle an active sessionhandle
     * @return the resultant String response (log data or error message)
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    private String performGet(String action, String pName, long pValue, String handle)
                                                                     throws IOException {
        return performGet(action, pName, String.valueOf(pValue), handle);
    }


    /**
     * Gets an summary xml list of all the specifications logged
     * @param caseID the case id
     * @param handle an active sessionhandle
     * @return the resultant String response (log data or error message)
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getCaseStartedBy(String caseID, String handle) throws IOException {
        return performGet("getCaseStartedBy", "caseid", caseID, handle);
    }


    /**
     * Gets an summary xml list of all logged events for a workitem
     * @param itemID the workitem's id string
     * @param handle an active sessionhandle
     * @return the resultant String response (log data or error message)
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getWorkItemEvents(String itemID, boolean fullName, String handle)
            throws IOException {
        Map<String, String> params = prepareParamMap("getWorkItemEvents", handle);
        params.put("itemid", itemID);
        params.put("fullname", String.valueOf(fullName));
        return executeGet(_logURI, params);
    }



    /**
     * Gets all events for all cases of the specification passed
     * @param identifier the unique identifier of the specification
     * @param version the specification's version number
     * @param uri the specification's uri
     * @param handle an active sessionhandle
     * @return the resultant String response (log data or error message)
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getSpecificationXESLog(String identifier, String version,
                                       String uri, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getSpecificationXESLog", handle);
        params.put("identifier", identifier);
        params.put("version", version);
        params.put("uri", uri);
        return executeGet(_logURI, params);
    }

    
    /**
     * Gets all events for all cases of the specification passed, from both the resource
     * service and the engine, merged together
     * @param identifier the unique identifier of the specification
     * @param version the specification's version number
     * @param uri the specification's uri
     * @param handle an active sessionhandle
     * @return the resultant String response (log data or error message)
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getMergedXESLog(String identifier, String version,
                                       String uri, String handle) throws IOException {
        return getMergedXESLog(identifier, version, uri, false, handle);
    }


    /**
     * Gets all events for all cases of the specification passed, from both the resource
     * service and the engine, merged together, optionally including the data value
     * changes from the engine's log
     * @param identifier the unique identifier of the specification
     * @param version the specification's version number
     * @param uri the specification's uri
     * @param withData if true, the data value changes in the engine log are included
     * @param handle an active sessionhandle
     * @return the resultant String response (log data or error message)
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getMergedXESLog(String identifier, String version, String uri,
                                  boolean withData, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getMergedXESLog", handle);
        params.put("identifier", identifier);
        params.put("version", version);
        params.put("uri", uri);
        params.put("withdata", String.valueOf(withData));
        return executeGet(_logURI, params);
    }


    /*****************************************************************************/

    // CONVENIENCE CONNECTION METHODS - ALSO IN INTERFACE A //

    /**
     * Connects an external entity to the engine
     * @param userID the userid
     * @param password the password
     * @return a sessionHandle if successful, or a failure message if not
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String connect(String userID, String password) throws IOException {
        Map<String, String> params = prepareParamMap("connect", null);
        params.put("userid", userID);
        params.put("password", PasswordEncryptor.encrypt(password, null));
        return executeGet(_logURI, params);
    }


    /**
     * Checks if a sessionhandle is active
     * @param handle the sessionhandle to check
     * @return true if the session is alive, false if otherwise
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String checkConnection(String handle) throws IOException {
        return performGet("checkConnection", null, null, handle) ;
    }

}