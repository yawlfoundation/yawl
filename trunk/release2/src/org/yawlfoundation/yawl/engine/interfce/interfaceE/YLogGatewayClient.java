/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.engine.interfce.interfaceE;

import org.yawlfoundation.yawl.engine.interfce.Interface_Client;

import java.io.IOException;
import java.util.Map;

/**
 * An API to be used by clients that want to retrieve data from the YAWL process logs.
 *
 *  @author Michael Adams
 *  29/10/2007
 *
 *  Last Date: 02/12/2009 (for v2.1)
 */

public class YLogGatewayClient extends Interface_Client {

    /** the uri of the YAWL Engine's __logGateway__
     * a default would be "http://localhost:8080/yawl/logGateway"
     */
    private String _logURI;

    /** the only constructor
     * @param uri the uri of the YAWL engine's log gateway
     */
    public YLogGatewayClient(String uri) {
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
     * @param handle an active sessionhandle
     * @return the resultant String response (log data or error message)
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getAllSpecifications(String handle) throws IOException {
        return performGet("getAllSpecifications", null, null, handle);
    }


    /**
     * Gets all of the net instances (root and sub-net) of all the logged cases
     * based on the specification details passed
     * @param identifier the unique identifier of the specification
     * @param version the specification's version number
     * @param uri the specification's uri
     * @param handle an active sessionhandle
     * @return the resultant String response (log data or error message)
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getNetInstancesOfSpecification(String identifier, String version,
                                       String uri, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getNetInstancesOfSpecification", handle);
        params.put("identifier", identifier);
        params.put("version", version);
        params.put("uri", uri);
        return executeGet(_logURI, params);
    }


    /**
     * Gets all of the net instances (root and sub-net) of all the logged cases
     * based on the specification key passed
     * @param specKey the primary key identifier of the specification in its log table
     * @param handle an active sessionhandle
     * @return the resultant String response (log data or error message)
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getNetInstancesOfSpecification(long specKey, String handle) throws IOException {
        return performGet("getNetInstancesOfSpecification", "key", specKey, handle);
    }


    /**
     * Gets a complete listing of all the cases launched from the specification data
     * passed, including all net & task instances, events and data items
     * @param identifier the unique identifier of the specification
     * @param version the specification's version number
     * @param uri the specification's uri
     * @param handle an active sessionhandle
     * @return the resultant String response (log data or error message)
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getCompleteCaseLogsForSpecification(String identifier, String version,
                                       String uri, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getCompleteCaseLogsForSpecification", handle);
        params.put("identifier", identifier);
        params.put("version", version);
        params.put("uri", uri);
        return executeGet(_logURI, params);
    }


    /**
     * Gets a complete listing  of all the cases launched from the specification key
     * passed, including all net & task instances, events and data items
     * @param specKey the primary key identifier of the specification in its log table
     * @param handle an active sessionhandle
     * @return the resultant String response (log data or error message)
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getCompleteCaseLogsForSpecification(long specKey, String handle)
            throws IOException {
        return performGet("getCompleteCaseLogsForSpecification", "key", specKey, handle);
    }


    /**
     * Gets all the case level events for the root net key passed
     * @param rootNetInstanceKey the primary key identifier of the root net instance of
     * the case
     * @param handle an active sessionhandle
     * @return the resultant String response (log data or error message)
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getCaseEvents(long rootNetInstanceKey, String handle) throws IOException {
        return performGet("getCaseEvents", "key", rootNetInstanceKey, handle);
    }


    /**
     * Gets all the case level events for the case id passed
     * @param caseID the case id of the case to get the events for
     * @param handle an active sessionhandle
     * @return the resultant String response (log data or error message)
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getCaseEvents(String caseID, String handle) throws IOException {
        return performGet("getCaseEvents", "caseid", caseID, handle);
    }


    /**
     * Gets all of the data items logged with the event requested
     * @param eventKey the primary key identifier of the event
     * @param handle an active sessionhandle
     * @return the resultant String response (log data or error message)
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getDataForEvent(long eventKey, String handle) throws IOException {
        return performGet("getDataForEvent", "key", eventKey, handle);
    }


    /**
     * Gets all of the net or task instances for the instance key passed
     * @param instanceKey the primary key identifier of the net or task instance
     * @param handle an active sessionhandle
     * @return the resultant String response (log data or error message)
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getInstanceEvents(long instanceKey, String handle) throws IOException {
        return performGet("getInstanceEvents", "key",  instanceKey, handle);
    }


    /**
     * Gets the data type for the key passed
     * @param dataTypeKey the primary key identifier of the data type
     * @param handle an active sessionhandle
     * @return the resultant String response (log data or error message)
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getDataTypeForDataItem(long dataTypeKey, String handle) throws IOException {
        return performGet("getDataTypeForDataItem", "key", dataTypeKey, handle);
    }


    /**
     * Gets a comprehensive listing of all of the data logged for the case passed,
     * including all net & task instances, events and data items
     * @param caseID the case id to get the log data for
     * @param handle an active sessionhandle
     * @return the resultant String response (log data or error message)
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getCompleteCaseLog(String caseID, String handle) throws IOException {
        return performGet("getCompleteCaseLog", "caseid", caseID, handle);                
    }


    /**
     * Gets a listing of all the task instances (ie work items) created for the case
     * @param caseID the case id to get the log data for
     * @param handle an active sessionhandle
     * @return the resultant String response (log data or error message)
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getTaskInstancesForCase(String caseID, String handle) throws IOException {
        return performGet("getTaskInstancesForCase", "caseid", caseID, handle);
    }


    /**
     * Gets a listing of all the task instances (ie work items) created for the task
     * @param taskKey the primary key identifier of the task definition
     * @param handle an active sessionhandle
     * @return the resultant String response (log data or error message)
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getTaskInstancesForTask(long taskKey, String handle) throws IOException {
        return performGet("getTaskInstancesForTask", "key", taskKey, handle);
    }


    /**
     * Gets the log record of a particular case-level event
     * @param caseID the case id to get the event for
     * @param eventType the 'name' of the event (eg 'CaseStart')
     * @param handle an active sessionhandle
     * @return the resultant String response (log data or error message)
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getCaseEvent(String caseID, String eventType, String handle) throws IOException {
        Map<String, String> params = prepareParamMap("getCaseEvent", handle);
        params.put("caseid", caseID);
        params.put("event", eventType);
        return executeGet(_logURI, params);
    }


    /**
     * Gets a list of all of the 'CaseStart' events triggered by the service
     * @param serviceName the name of the registered service
     * @param handle an active sessionhandle
     * @return an XML'd String list of Case Start events triggered by the service
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getAllCasesStartedByService(String serviceName, String handle) throws IOException {
        return performGet("getAllCasesStartedByService", "name", serviceName, handle);
    }


    /**
     * Gets a list of all of the 'CaseCancel' events triggered by the service 
     * @param serviceName the name of the registered service
     * @param handle an active sessionhandle
     * @return an XML'd String list of Case Cancellation events triggered by the service
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getAllCasesCancelledByService(String serviceName, String handle) throws IOException {
        return performGet("getAllCasesCancelledByService", "name", serviceName, handle);
    }


    /**
     * Gets the name of the service referenced by the key passed
     * @param serviceKey the primary key identifier of the service
     * @param handle an active sessionhandle
     * @return the resultant String response (log data or error message)
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getServiceName(long serviceKey, String handle) throws IOException {
        return performGet("getServiceName", "key", serviceKey, handle);
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
        params.put("password", password);
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