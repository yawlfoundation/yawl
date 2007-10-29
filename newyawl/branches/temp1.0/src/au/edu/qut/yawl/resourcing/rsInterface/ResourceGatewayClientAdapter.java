/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing.rsInterface;

import au.edu.qut.yawl.resourcing.AbstractSelector;
import au.edu.qut.yawl.resourcing.resource.AbstractResourceAttribute;
import au.edu.qut.yawl.resourcing.resource.Participant;
import au.edu.qut.yawl.util.JDOMConversionTools;
import org.jdom.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This adapter class adds a layer to the resource gateway client, effectively
 * reconstituting the Strings returned from the gateway into java objects.
 *
 * Author: Michael Adams
 * Date: 26/10/2007
 * Version: 0.1
 *
 */

public class ResourceGatewayClientAdapter {

    protected ResourceGatewayClient _rgclient;        // the gatewy client
    protected String _uri ;                           // the uri of the service gateway


    // CONSTRUCTORS //

    public ResourceGatewayClientAdapter() {}

    public ResourceGatewayClientAdapter(String uri) { setClientURI(uri) ; }


    // GETTER & SETTER //

    public void setClientURI(String uri) {
        _uri = uri ;
        _rgclient = new ResourceGatewayClient(uri) ;
    }

    public String getClientURI() { return _uri ; }

    /*****************************************************************************/

    // PRIVATE METHODS //

    /**
     * Converts a CSV string into a List of String objects
     * @param csv the string of items separated by commas
     * @return a List of items as Strings
     */
    private List<String> CSVToStringList(String csv) {
        if (csv == null) return null ;
        List<String> result = new ArrayList<String>() ;

        // split the string on the commas and add each resultant string to the List
        String[] csvArray = csv.split(",");
        for (int i=0; i< csvArray.length; i++) result.add(csvArray[i]) ;

        if (result.isEmpty()) return null;
        return result;
    }


    /**
     * Converts the string passed to a JDOM Element and returns its child Elements
     * @param s the xml string to be converted
     * @return a list of child elements of the converted element passed
     */
    private List getChildren(String s) {
        if (s == null) return null;
        return JDOMConversionTools.stringToElement(s).getChildren();
    }


    /**
     * Creates a new class instance of the name passed.
     * @pre className is the valid name of a class that extends from
     *      the base AbstractResourceAtttribute
     * @param className the name of the extended class to create
     * @return An instantiated class of type 'className', or null if there's a problem
     */
    private AbstractResourceAttribute newAttributeClass(String className) {
        String pkg = "au.edu.qut.yawl.resourcing.resource." ;
        try {
			return (AbstractResourceAttribute) Class.forName(pkg + className)
                                                     .newInstance() ;
        }
        catch (Exception cnfe) {
            return null ;
        }
    }


    /**
     * Creates a new class instance of the name passed.
     * @pre className is the valid name of a class that extends from
     *      the base AbstractSelector
     * @param className the name of the extended class to create
     * @return An instantiated class of type 'className', or null if there's a problem
     */
    private AbstractSelector newSelectorClass(String className) {
         String pkg = "au.edu.qut.yawl.resourcing." ;
         try {
             return (AbstractSelector) Class.forName(pkg + className).newInstance() ;
         }
         catch (Exception cnfe) {
             System.out.println(pkg+className);
             cnfe.printStackTrace();
             return null ;
         }
     }


    /**
     * Converts a string version of a JDOM Element into a List of instantiated
     * ResourceAttribute child objects (i.e. Role, Position, Capability, OrgGroup)
     * @param xml the string to be converted to a JDOM Element
     * @param className the type of child class to instantiate
     * @return a List of instantiated objects
     */
    private List<AbstractResourceAttribute> xmlStringToResourceAttributeList(
                                            String xml, String className) {
        List<AbstractResourceAttribute> result = new ArrayList<AbstractResourceAttribute>();

        // get List of child elements
        List eList = getChildren(xml);
        if (eList != null) {
            Iterator itr = eList.iterator();
            while (itr.hasNext()) {
                Element item = (Element) itr.next();

                // instantiate a class of the appropriate type
                AbstractResourceAttribute ra = newAttributeClass(className);
                if (ra != null) {

                    // pass the element to the new object to repopulate members
                    ra.reconstitute(item);
                    result.add(ra);
                }
            }
        }
        if (result.isEmpty()) return null;
        return result ;
    }


    /**
      * Converts a string version of a JDOM Element into a List of instantiated
      * ResourceAttribute child objects (i.e. GenericConstraint, GenericFilter,
      * GenericAllocator)
      * @param xml the string to be converted to a JDOM Element
      * @param className the type of child class to instantiate
      * @return a List of instantiated objects
      */    
    private List<AbstractSelector> xmlStringToSelectorList(String xml, String className) {
        List<AbstractSelector> result = new ArrayList<AbstractSelector>();

        // get List of child elements
        List eList = getChildren(xml);
        if (eList != null) {
            Iterator itr = eList.iterator();
            while (itr.hasNext()) {
                Element item = (Element) itr.next();

                // instantiate a class of the appropriate type
                AbstractSelector as = newSelectorClass(className);
                if (as != null) {

                    // pass the element to the new object to repopulate members
                    as.reconstitute(item);
                    result.add(as);
                }
                else break ;
            }
        }

        if (result.isEmpty()) return null;
        return result ;
    }

    //*******************************************************************************/

    // PUBLIC METHODS //

    /**
     * Checks that the connection to the service is valid
     * @param handle the current sessionhandle
     * @return true if the connection is valid, false if otherwise
     */
    public boolean checkConnection(String handle) {
        try {
            return _rgclient.checkConnection(handle).equals("true") ;
        }
        catch (IOException ioe) { return false; }
    }


    /**
     * Attempts to connect to the service
     * @param userid the userid
     * @param password  the corresponding password
     * @return a sessionhandle if successful, or a failure message if otherwise
     */
    public String connect(String userid, String password) {
        try {
            return _rgclient.connect(userid, password) ;
        }
        catch (IOException ioe) {
            return "<failure>IOException attempting to connect to Service.</failure>";
        }
    }


    /**
     * Disconnects a session from the service
     * @param handle the sessionhandle of the session to disconnect
     */
    public void disconnect(String handle) {
        try {
            _rgclient.disconnect(handle);
        }
        catch (IOException ioe) { } // nothing to do
    }


    /**
     * Gets the full name of each participant
     * @param handle the current sessionhandle
     * @return a List of participant full names
     * @throws IOException if there was a problem connecting to the engine
     */
    public List<String> getAllParticipantNames(String handle) throws IOException {
        return CSVToStringList(_rgclient.getAllParticipantNames(handle)) ;
    }


    /**
     * Gets the name of each role
     * @param handle the current sessionhandle
     * @return a List of role names
     * @throws IOException if there was a problem connecting to the engine
     */
    public List<String> getAllRoleNames(String handle) throws IOException {
        return CSVToStringList(_rgclient.getAllRoleNames(handle)) ;
    }


    /**
     * Gets a complete list of available Role objects
     * @param handle the current sessionhandle
     * @return a List of Role objects
     * @throws IOException if there was a problem connecting to the engine
     */
    public List getRoles(String handle) throws IOException {
        String rStr = _rgclient.getRoles(handle) ;
        return xmlStringToResourceAttributeList(rStr, "Role") ;
    }


    /**
     * Gets a complete list of available Capability objects
     * @param handle the current sessionhandle
     * @return a List of Capability objects
     * @throws IOException if there was a problem connecting to the engine
     */
    public List getCapabilities(String handle) throws IOException {
        String cStr = _rgclient.getCapabilities(handle) ;
        return xmlStringToResourceAttributeList(cStr, "Capability") ;
    }


    /**
     * Gets a complete list of available Position objects
     * @param handle the current sessionhandle
     * @return a List of Position objects
     * @throws IOException if there was a problem connecting to the engine
     */
    public List getPositions(String handle) throws IOException {
        String cStr = _rgclient.getPositions(handle) ;
        return xmlStringToResourceAttributeList(cStr, "Position") ;
    }


    /**
     * Gets a complete list of available OrgGroup objects
     * @param handle the current sessionhandle
     * @return a List of OrgGroup objects
     * @throws IOException if there was a problem connecting to the engine
     */
    public List getOrgGroups(String handle) throws IOException {
        String cStr = _rgclient.getOrgGroups(handle) ;
        return xmlStringToResourceAttributeList(cStr, "OrgGroup") ;
    }


    /**
     * Gets a complete list of available Participant objects
     * @param handle the current sessionhandle
     * @return a List of Participant objects
     * @throws IOException if there was a problem connecting to the engine
     */
    public List getParticipants(String handle) throws IOException {
        List<Participant> result = new ArrayList<Participant>();
        String cStr = _rgclient.getParticipants(handle) ;

        // each child is one Participant (as xml)
        List eList = getChildren(cStr);
        if (eList != null) {
            Iterator itr = eList.iterator();
            while (itr.hasNext()) {
                Element e = (Element) itr.next();
                Participant p = new Participant();

                // repopulate the members from its xml
                p.reconstitute(e);
                result.add(p);
            }
        }
        if (result.isEmpty()) return null;
        return result ;
    }


    /**
     * Gets a complete list of available Constraint objects
     * @param handle the current sessionhandle
     * @return a List of Constraint objects (instantiated as GenericConstraint objs)
     * @throws IOException if there was a problem connecting to the engine
     */    
    public List getConstraints(String handle) throws IOException {
        String cStr = _rgclient.getConstraints(handle) ;
        return xmlStringToSelectorList(cStr, "constraints.GenericConstraint") ;
    }
    

    /**
     * Gets a complete list of available Allocator objects
     * @param handle the current sessionhandle
     * @return a List of Allocator objects (instantiated as GenericAllocator objs)
     * @throws IOException if there was a problem connecting to the engine
     */
    public List getAllocators(String handle) throws IOException {
        String aStr = _rgclient.getAllocators(handle) ;
        return xmlStringToSelectorList(aStr, "allocators.GenericAllocator") ;
    }


    /**
     * Gets a complete list of available Filter objects
     * @param handle the current sessionhandle
     * @return a List of Filter objects (instantiated as GenericFilter objs)
     * @throws IOException if there was a problem connecting to the engine
     */
    public List getFilters(String handle) throws IOException {
        String aStr = _rgclient.getFilters(handle) ;
        return xmlStringToSelectorList(aStr, "filters.GenericFilter") ;
    }


    /**
     * Checks if an id corresponds to a capability id known to the service
     * @param capabilityID the id to check
     * @param handle the current sessionhandle
     * @return true if the id is known (valid), false if otherwise
     */
    public boolean isKnownCapability(String capabilityID, String handle) {
        try {
            return _rgclient.isKnownCapability(capabilityID, handle).equals("true") ;
        }
        catch (IOException ioe) { return false; }
    }


    /**
     * Checks if an id corresponds to a role id known to the service
     * @param roleID the id to check
     * @param handle the current sessionhandle
     * @return true if the id is known (valid), false if otherwise
     */
    public boolean isKnownRole(String roleID, String handle) {
        try {
            return _rgclient.isKnownRole(roleID, handle).equals("true") ;
        }
        catch (IOException ioe) { return false; }
    }


    /**
     * Checks if an id corresponds to a participant id known to the service
     * @param participantID the id to check
     * @param handle the current sessionhandle
     * @return true if the id is known (valid), false if otherwise
     */
    public boolean isKnownParticipant(String participantID, String handle) {
        try {
            return _rgclient.isKnownParticipant(participantID, handle).equals("true") ;
        }
        catch (IOException ioe) { return false; }
    }


    /**
     * Checks if an id corresponds to a position id known to the service
     * @param positionID the id to check
     * @param handle the current sessionhandle
     * @return true if the id is known (valid), false if otherwise
     */    
    public boolean isKnownPosition(String positionID, String handle) {
        try {
            return _rgclient.isKnownPosition(positionID, handle).equals("true") ;
        }
        catch (IOException ioe) { return false; }
    }


    /**
      * Checks if an id corresponds to a OrgGroup id known to the service
      * @param groupID the id to check
      * @param handle the current sessionhandle
      * @return true if the id is known (valid), false if otherwise
      */
    public boolean isKnownOrgGroup(String groupID, String handle) {
        try {
            return _rgclient.isKnownOrgGroup(groupID, handle).equals("true") ;
        }
        catch (IOException ioe) { return false; }
    }
    
}
