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
 *  Last Date: 19/09/2007
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
    private Element performGet(String action) throws IOException {
        String resultStr = executeGet(_serviceURI + "?action=" + action) ;
        return JDOMConversionTools.stringToElement(resultStr) ;
    }

    private boolean executeBooleanGet(String action, String id) throws IOException {
        String result = executeGet(_serviceURI + "?action=" + action + "&id=" + id);
        return result.equalsIgnoreCase("true");
    }


    public Element getConstraints() throws IOException {
        return performGet("getResourceConstraints");
    }

    public Element getFilters() throws IOException {
        return performGet("getResourceFilters") ;
    }

    public Element getAllocators() throws IOException {
        return performGet("getResourceAllocators") ;
    }

    public Element getAllSelectors() throws IOException {
        return performGet("getAllSelectors") ;
    }

    public Element getParticipants() throws IOException {
        return performGet("getParticipants") ;
    }

    public Element getRoles() throws IOException {
        return performGet("getRoles") ;
    }

    public Element getCapabilities() throws IOException {
        return performGet("getCapabilities") ;
    }

    public Element getPositions() throws IOException {
        return performGet("getPositions") ;
    }

    public Element getOrgGroups() throws IOException {
        return performGet("getOrgGroups") ;
    }


    // GET METHODS - returning String //

    public String getAllParticipantNames() throws IOException {
        return executeGet(_serviceURI + "?action=getAllParticipantNames") ;
    }

    public String getAllRoleNames() throws IOException {
        return executeGet(_serviceURI + "?action=getAllRoleNames") ;
    }


    // GET METHODS - returning boolean //

    public boolean isKnownParticipant(String participantID) throws IOException {
        return executeBooleanGet("isKnownParticipant", participantID) ;
    }

    public boolean isKnownRole(String roleID) throws IOException {
        return executeBooleanGet("isKnownRole", roleID) ;
    }

    public boolean isKnownCapability(String capabilityID) throws IOException {
        return executeBooleanGet("isKnownCapability", capabilityID) ;
    }

    public boolean isKnownPosition(String positionID) throws IOException {
        return executeBooleanGet("isKnownPosition", positionID) ;
    }

    public boolean isKnownOrgGroup(String orgGroupID) throws IOException {
        return executeBooleanGet("isKnownOrgGroup", orgGroupID) ;
    }


    /******************************************************************************/

    // POST METHODS //

    /** triggers a reload of org data */
    public void refreshOrgDataSet() throws IOException {
        HashMap<String,String> params = new HashMap<String,String>();
        params.put("action", "refreshOrgDataSet") ;
        executePost(_serviceURI, params);
    }

    
    /** (re)sets the timer for automatic org data reloads
     *
     * @param minutes the number of minutes to wait between refreshes
     * @throws IOException
     */
    public void resetOrgDataRefreshRate(int minutes) throws IOException {
        HashMap<String,String> params = new HashMap<String,String>();
        params.put("action", "resetOrgDataRefreshRate") ;
        params.put("rate", String.valueOf(minutes));
        executePost(_serviceURI, params);
    }

}
