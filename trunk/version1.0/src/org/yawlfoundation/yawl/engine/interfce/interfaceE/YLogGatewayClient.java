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

/**
 * An API to be used by clients that want to retrieve data from the YAWL process logs.
 *
 *  @author Michael Adams
 *  29/10/2007
 *
 *  Last Date: 06/11/2007
 */

public class YLogGatewayClient extends Interface_Client {

    /** the uri of yawl's **logGateway**
     * a default would be "http://localhost:8080/yawl/logGateway" */
    private String _logURI;

    /** the only constructor
     * @param uri the uri of the resourceService's gateway */
    public YLogGatewayClient(String uri) {
        _logURI = uri ;
    }

    /*******************************************************************************/

    // GET METHODS - returning String //

    /**
     * a wrapper for the executeGet method - returns a String
     * @param action the name of the gateway method to call
     * @param pName the name of the parameter passed
     * @param pValue the parameter's value
     * @param handle an active sessionhandle
     * @return the resultant Element
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    private String performGet(String action, String pName, String pValue, String handle)
                                                                     throws IOException {
        StringBuilder sb = new StringBuilder(_logURI) ;
        sb.append("?action=").append(action) ;
        if (pName != null)
            sb.append("&").append(pName).append("=").append(pValue);
        sb.append("&handle=").append(handle);
        return executeGet(sb.toString());
    }

    /**
     * Gets all of the CaseEventIDs in the process logs
     * @param handle an active sessionhandle
     * @return an XML'd String list of CaseEventIDs
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getAllCaseEventIDs(String handle) throws IOException  {
        return performGet("getAllCaseEventIDs", null, null, handle) ;
    }

    /**
     * Gets all of the CaseEventIDs in the process logs for a particular event type
     * @param eventType one of "started", "completed" or "cancelled"
     * @param handle an active sessionhandle
     * @return an XML'd String list of CaseEventIDs for the event type passed
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getAllCaseEventIDs(String eventType, String handle) throws IOException  {
        return performGet("getAllCaseEventIDs", "eventtype", eventType, handle) ;
    }

    /**
     * Gets a list of all distinct spec ids (i.e. spec names) in the process logs
     * @param handle an active sessionhandle
     * @return an XML'd String list of specification ids
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getAllSpecIDs(String handle) throws IOException  {
        return performGet("getAllSpecIDs", null, null, handle) ;
    }

    /**
     * Gets a list of "started" cases for a specification (all log columns)
     * @param specID the specification id to get the cases for
     * @param handle an active sessionhandle
     * @return an XML'd String list of case data for the spec id passed
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getCaseEventsForSpec(String specID, String handle) throws IOException {
        return performGet("getCaseEventsForSpec", "specid", specID, handle) ;
    }

    /**
     * Gets a list of "started" CaseEventIDs for a specification
     * @param specID the specification id to get the CaseEventIDs for
     * @param handle an active sessionhandle
     * @return an XML'd String list of CaseEventIDs for the spec id passed
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getCaseEventIDsForSpec(String specID, String handle) throws IOException {
        return performGet("getCaseEventIDsForSpec", "specid", specID, handle) ;
    }

    /**
     * Gets a list of all child workitem data for a particular parent item
     * @param parentEventID the id of the parent "executing" event
     * @param handle an active sessionhandle
     * @return an XML'd String list of child workitem data of the parent
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getChildWorkItemEventsForParent(String parentEventID, String handle)
                                                                     throws IOException {
        return performGet("getChildWorkItemEventsForParent", "eventid", parentEventID,
                                                                      handle) ;
    }

    /**
     * Gets a list of parent workitem data for a particular case
     * @param caseEventID the id of the "started" case event
     * @param handle an active sessionhandle
     * @return an XML'd String list of parent workitem data for the particular case
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getParentWorkItemEventsForCase(String caseEventID, String handle)
                                                                     throws IOException {
        return performGet("getParentWorkItemEventsForCase", "eventid", caseEventID,
                                                                      handle) ;
    }

    /**
     * Gets a list of parent workitem data for a particular case id
     * @param caseID the id of the "started" case 
     * @param handle an active sessionhandle
     * @return an XML'd String list of parent workitem data for the particular case
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getParentWorkItemEventsForCaseID(String caseID, String handle)
                                                                     throws IOException {
        return performGet("getParentWorkItemEventsForCaseID", "caseid", caseID, handle) ;
    }


    /**
     * Gets the time of a case event
     * @param handle an active sessionhandle
     * @param eventID the id of the case event
     * @return an XML'd String encapsulating the long value of the event timestamp
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getCaseEventTime(String eventID, String handle) throws IOException  {
        return performGet("getCaseEventTime", "eventid", eventID, handle) ;
    }


    /**
     * Gets the time of a case event
     * @param handle an active sessionhandle
     * @param caseID the case id 
     * @param eventType one of "started", "completed" or "cancelled"
     * @return an XML'd String encapsulating the long value of the event timestamp
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String getCaseEventTime(String caseID, String eventType, String handle)
                                                                  throws IOException  {
        StringBuilder sb = new StringBuilder(_logURI) ;
        sb.append("?action=getCaseEventTime") ;
        sb.append("&caseid=").append(caseID);
        sb.append("&eventtype=").append(eventType);
        sb.append("&handle=").append(handle);
        return executeGet(sb.toString());
    }


    /**
     * Gets the list of input and output parameters and values for a workitem
     * @param childEventID the workitem instance log id
     * @param handle an active sessionhandle
     * @return an XML'd String of the workitem's data
     * @throws IOException if there's a problem connecting to the engine
     */
    public String getChildWorkItemData(String childEventID, String handle)
                                                                     throws IOException {
        return performGet("getWorkItemDataForChildWorkItemEventID", "eventid",
                             childEventID, handle) ;
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
        StringBuilder sb = new StringBuilder(_logURI);
        sb.append("?action=connect&userid=").append(userID)
          .append("&password=").append(password);

        return executeGet(sb.toString());
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

    /**
     * Checks if a sesionhandle is active and has admin privileges
     * @param handle the sessionhandle to check
     * @return true if the session is admin and alive, false if otherwise
     * @throws java.io.IOException if there's a problem connecting to the engine
     */
    public String checkConnectionForAdmin(String handle) throws IOException {
        return performGet("checkConnectionForAdmin", null, null, handle) ;       
    }

}