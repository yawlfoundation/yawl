/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing.rsInterface;

import au.edu.qut.yawl.engine.interfce.Interface_Client;
import au.edu.qut.yawl.util.JDOMConversionTools;

import java.io.IOException;
import java.util.HashMap;

import org.jdom.Element;

/**
 * An API to be used by clients that want to converse with the Resource Service.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  14/08/2007
 *
 *  Last Date: 24/10/2007
 */

public class ResourceInterfaceClient extends Interface_Client {

    /** the uri of the resource service's **gateway**
     * a default would be "http://localhost:8080/resourceService/gateway" */
    private String _serviceURI ;

    /** the only constructor
     * @param uri the uri of the resourceService's gateway */
    public ResourceInterfaceClient(String uri) {
        _serviceURI = uri ;
    }

    /*******************************************************************************/

    // GET METHODS - returning Element //

    /**
     * a wrapper for the executeGet method - returns an Element 
     * @param action the name of the gateway method to call
     * @return the resultant Element
     * @throws IOException
     */
    private Element performGet(String action, String handle) throws IOException {
        String resultStr = executeGet(_serviceURI + "?action=" + action +
                                                    "&handle=" + handle) ;
        return JDOMConversionTools.stringToElement(resultStr) ;
    }

    private boolean executeBooleanGet(String action, String id, String handle)
                                                             throws IOException {
        String result = executeGet(_serviceURI + "?action=" + action + "&id=" + id +
                                                 "&handle=" + handle) ;
        return result.equalsIgnoreCase("true");
    }


    public Element getConstraints(String handle) throws IOException {
        return performGet("getResourceConstraints", handle);
    }

    public Element getFilters(String handle) throws IOException {
        return performGet("getResourceFilters", handle) ;
    }

    public Element getAllocators(String handle) throws IOException {
        return performGet("getResourceAllocators", handle) ;
    }

    public Element getAllSelectors(String handle) throws IOException {
        return performGet("getAllSelectors", handle) ;
    }

    public Element getParticipants(String handle) throws IOException {
        return performGet("getParticipants", handle) ;
    }

    public Element getRoles(String handle) throws IOException {
        return performGet("getRoles", handle) ;
    }

    public Element getCapabilities(String handle) throws IOException {
        return performGet("getCapabilities", handle) ;
    }

    public Element getPositions(String handle) throws IOException {
        return performGet("getPositions", handle) ;
    }

    public Element getOrgGroups(String handle) throws IOException {
        return performGet("getOrgGroups", handle) ;
    }


    // GET METHODS - returning String //

    public String getAllParticipantNames(String handle) throws IOException {
        return executeGet(_serviceURI + "?action=getAllParticipantNames&handle=" + handle) ;
    }

    public String getAllRoleNames(String handle) throws IOException {
        return executeGet(_serviceURI + "?action=getAllRoleNames&handle=" + handle) ;
    }


    // GET METHODS - returning boolean //

    public boolean isKnownParticipant(String participantID, String handle)
                                                                throws IOException {
        return executeBooleanGet("isKnownParticipant", participantID, handle) ;
    }

    public boolean isKnownRole(String roleID, String handle) throws IOException {
        return executeBooleanGet("isKnownRole", roleID, handle) ;
    }

    public boolean isKnownCapability(String capabilityID, String handle)
                                                                throws IOException {
        return executeBooleanGet("isKnownCapability", capabilityID, handle) ;
    }

    public boolean isKnownPosition(String positionID, String handle) throws IOException {
        return executeBooleanGet("isKnownPosition", positionID, handle) ;
    }

    public boolean isKnownOrgGroup(String orgGroupID, String handle) throws IOException {
        return executeBooleanGet("isKnownOrgGroup", orgGroupID, handle) ;
    }

    public boolean checkConnection(String handle) throws IOException {
        String result = executeGet(_serviceURI + "?action=checkConnection&handle=" + handle) ;
        return result.equalsIgnoreCase("true");  
    }

    /**
     * Connects an external entity to the resource service
     * @param userID
     * @param password
     * @return a sessionHandle if successful, or a failure message if not
     * @throws IOException
     */
    public String connect(String userID, String password) throws IOException {
//        HashMap<String,String> params = new HashMap<String,String>();
//        params.put("action", "connect") ;
//        params.put("userid", userID);
//        params.put("password", password);
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
