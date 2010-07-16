/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.rsInterface;

import org.jdom.Element;
import org.yawlfoundation.yawl.resourcing.AbstractSelector;
import org.yawlfoundation.yawl.resourcing.jsf.comparator.ParticipantNameComparator;
import org.yawlfoundation.yawl.resourcing.resource.*;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.io.IOException;
import java.util.*;

/**
 * This adapter class adds a layer to the resource gateway client, effectively
 * reconstituting the Strings returned from the gateway into java objects.
 *
 * Author: Michael Adams
 * Date: 26/10/2007
 *
 */

public class ResourceGatewayClientAdapter {

    protected ResourceGatewayClient _rgclient;        // the gateway client
    protected String _uri ;                           // the uri of the service gateway

    protected ResourceMarshaller _marshaller = new ResourceMarshaller();

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

    public boolean successful(String result) {
        return (result != null) && (! result.startsWith("<failure>"));
    }


    protected String successCheck(String xml) throws ResourceGatewayException {
        if (successful(xml)) {
            return xml;
        }
        else throw new ResourceGatewayException(xml);
    }
    

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
        result.addAll(Arrays.asList(csvArray));

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
        Element parent = JDOMUtil.stringToElement(s);
        return (parent != null) ? parent.getChildren() : null;
    }


    /**
     * Creates a new class instance of the name passed.
     * @pre className is the valid name of a class that extends from
     *      the base AbstractResourceAtttribute
     * @param className the name of the extended class to create
     * @return An instantiated class of type 'className', or null if there's a problem
     */
    private AbstractResourceAttribute newAttributeClass(String className) {
        String pkg = "org.yawlfoundation.yawl.resourcing.resource." ;
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
         String pkg = "org.yawlfoundation.yawl.resourcing." ;
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
            for (Object o : eList) {

                // instantiate a class of the appropriate type
                AbstractResourceAttribute ra = newAttributeClass(className);
                if (ra != null) {

                    // pass the element to the new object to repopulate members
                    ra.reconstitute((Element) o);
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
            for (Object o : eList) {

                // instantiate a class of the appropriate type
                AbstractSelector as = newSelectorClass(className);
                if (as != null) {

                    // pass the element to the new object to repopulate members
                    as.reconstitute((Element) o);
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
        catch (IOException ioe) {
            // nothing to do
        }
    }


    /**
     * Gets the full name of each participant
     * @param handle the current sessionhandle
     * @return a List of participant full names
     * @throws IOException if there was a problem connecting to the resource service
     * @throws ResourceGatewayException if there was a problem getting the names
     */
    public List<String> getAllParticipantNames(String handle)
            throws IOException, ResourceGatewayException {
        return CSVToStringList(successCheck(_rgclient.getAllParticipantNames(handle))) ;
    }


    /**
     * Gets the name of each role
     * @param handle the current sessionhandle
     * @return a List of role names
     * @throws IOException if there was a problem connecting to the resource service
     * @throws ResourceGatewayException if there was a problem getting the names
     */
    public List<String> getAllRoleNames(String handle)
            throws IOException, ResourceGatewayException {
        return CSVToStringList(successCheck(_rgclient.getAllRoleNames(handle))) ;
    }


    /**
     * Gets a complete list of available Role objects
     * @param handle the current sessionhandle
     * @return a List of Role objects
     * @throws IOException if there was a problem connecting to the resource service
     * @throws ResourceGatewayException if there was a problem getting the roles
     */
    public List getRoles(String handle) throws IOException, ResourceGatewayException {
        String rStr = successCheck(_rgclient.getRoles(handle)) ;
        return xmlStringToResourceAttributeList(rStr, "Role") ;
    }


    /**
     * Gets a complete list of available Capability objects
     * @param handle the current sessionhandle
     * @return a List of Capability objects
     * @throws IOException if there was a problem connecting to the resource service
     * @throws ResourceGatewayException if there was a problem getting the capabilities
     */
    public List getCapabilities(String handle) throws IOException, ResourceGatewayException {
        String cStr = successCheck(_rgclient.getCapabilities(handle)) ;
        return xmlStringToResourceAttributeList(cStr, "Capability") ;
    }


    /**
     * Gets a complete list of available Position objects
     * @param handle the current sessionhandle
     * @return a List of Position objects
     * @throws IOException if there was a problem connecting to the resource service
     * @throws ResourceGatewayException if there was a problem getting the positions
     */
    public List getPositions(String handle) throws IOException, ResourceGatewayException {
        String cStr = successCheck(_rgclient.getPositions(handle)) ;
        return xmlStringToResourceAttributeList(cStr, "Position") ;
    }


    /**
     * Gets a complete list of available OrgGroup objects
     * @param handle the current sessionhandle
     * @return a List of OrgGroup objects
     * @throws IOException if there was a problem connecting to the resource service
     * @throws ResourceGatewayException if there was a problem getting the groups
     */
    public List getOrgGroups(String handle) throws IOException, ResourceGatewayException {
        String cStr = successCheck(_rgclient.getOrgGroups(handle)) ;
        return xmlStringToResourceAttributeList(cStr, "OrgGroup") ;
    }


    /**
     * Gets a complete list of available Participant objects
     * @param handle the current sessionhandle
     * @return a List of Participant objects
     * @throws IOException if there was a problem connecting to the resource service
     * @throws ResourceGatewayException if there was a problem getting the participants
     */
    public List<Participant> getParticipants(String handle)
            throws IOException, ResourceGatewayException {
        String xml = successCheck(_rgclient.getParticipants(handle)) ;
        Set<Participant> set = _marshaller.unmarshallParticipants(xml) ;
        if (set != null) {
            ArrayList<Participant> result = new ArrayList<Participant>(set);
            Collections.sort(result, new ParticipantNameComparator());
            return result;
        }
        else return null;
    }


    /**
     * Gets a particular Participant
     * @param pid the id of the Participant to get
     * @param handle the current session handle
     * @return a Participant object for the id, or null if not found
     * @throws IOException if there was a problem connecting to the resource service
     * @throws ResourceGatewayException if there was a problem getting the participant
     */
    public Participant getParticipant(String pid, String handle)
            throws IOException, ResourceGatewayException {
        String pStr = successCheck(_rgclient.getParticipant(pid, handle)) ;
        return new Participant(JDOMUtil.stringToElement(pStr)) ;
    }


    /**
     * Gets a complete list of available Constraint objects
     * @param handle the current sessionhandle
     * @return a List of Constraint objects (instantiated as GenericConstraint objs)
     * @throws IOException if there was a problem connecting to the resource service
     * @throws ResourceGatewayException if there was a problem getting the constraints
     */
    public List getConstraints(String handle) throws IOException, ResourceGatewayException {
        String cStr = successCheck(_rgclient.getConstraints(handle)) ;
        return xmlStringToSelectorList(cStr, "constraints.GenericConstraint") ;
    }
    

    /**
     * Gets a complete list of available Allocator objects
     * @param handle the current sessionhandle
     * @return a List of Allocator objects (instantiated as GenericAllocator objs)
     * @throws IOException if there was a problem connecting to the resource service
     * @throws ResourceGatewayException if there was a problem getting the allocators
     */
    public List getAllocators(String handle) throws IOException, ResourceGatewayException {
        String aStr = successCheck(_rgclient.getAllocators(handle)) ;
        return xmlStringToSelectorList(aStr, "allocators.GenericAllocator") ;
    }


    /**
     * Gets a complete list of available Filter objects
     * @param handle the current sessionhandle
     * @return a List of Filter objects (instantiated as GenericFilter objs)
     * @throws IOException if there was a problem connecting to the resource service
     * @throws ResourceGatewayException if there was a problem getting the filters
     */
    public List getFilters(String handle) throws IOException, ResourceGatewayException {
        String aStr = successCheck(_rgclient.getFilters(handle)) ;
        return xmlStringToSelectorList(aStr, "filters.GenericFilter") ;
    }


    /**
     * Gets a complete list of available codelets
     * @param handle the current sessionhandle
     * @return a List of AbstractCodelet objects
     * @throws IOException if there was a problem connecting to the resource service
     * @throws ResourceGatewayException if there was a problem getting the codelets
     */
    public Map<String, String> getCodeletMap(String handle)
            throws IOException, ResourceGatewayException {
        Map<String, String> result = new TreeMap<String, String>();
        String cStr = successCheck(_rgclient.getCodelets(handle)) ;
        Element eList = JDOMUtil.stringToElement(cStr);
        if (eList != null) {
            for (Object o : eList.getChildren()) {
                Element codelet = (Element) o;
                result.put(codelet.getChildText("name"),
                       JDOMUtil.decodeEscapes(codelet.getChildText("description")));
            }
        }
        return result ;
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


    /**
     * Gets the set of Participants that are currently logged on
     * @param handle the current sessionhandle
     * @return the Set of currently logged on Participants
     * @throws IOException if there was a problem connecting to the resource service
     * @throws ResourceGatewayException if there was a problem getting the participants
     */
    public Set<Participant> getActiveParticipants(String handle)
            throws IOException, ResourceGatewayException {
        String xml = successCheck(_rgclient.getActiveParticipants(handle)) ;
        return _marshaller.unmarshallParticipants(xml) ;
    }


    /**
     * Gets a list of all roles 'occupied' by the specified participant
     * @param pid the id of the Participant of interest
     * @param handle the current sessionhandle
     * @return a List of (Role) objects
     * @throws IOException if there was a problem connecting to the resource service
     * @throws ResourceGatewayException if there was a problem getting the roles
     */
    public List getParticipantRoles(String pid, String handle)
            throws IOException, ResourceGatewayException {
        String rStr = successCheck(_rgclient.getParticipantRoles(pid, handle)) ;
        return xmlStringToResourceAttributeList(rStr, "Role") ;
    }    


    /**
     * Gets a list of all capabilities held by the specified participant
     * @param pid the id of the Participant of interest
     * @param handle the current sessionhandle
     * @return a List of (Capability) objects
     * @throws IOException if there was a problem connecting to the resource service
     * @throws ResourceGatewayException if there was a problem getting the capabilities
     */
    public List getParticipantCapabilities(String pid, String handle)
            throws IOException, ResourceGatewayException {
        String cStr = successCheck(_rgclient.getParticipantCapabilities(pid, handle)) ;
        return xmlStringToResourceAttributeList(cStr, "Capability") ;
    }


    /**
     * Gets a list of all positions held by the specified participant
     * @param pid the id of the Participant of interest
     * @param handle the current sessionhandle
     * @return a List of (Position) objects
     * @throws IOException if there was a problem connecting to the resource service
     * @throws ResourceGatewayException if there was a problem getting the positions
     */
    public List getParticipantPositions(String pid, String handle)
            throws IOException, ResourceGatewayException {
        String pStr = successCheck(_rgclient.getParticipantPositions(pid, handle)) ;
        return xmlStringToResourceAttributeList(pStr, "Position") ;
    }


    /**
     * Gets the set of Participants that 'occupy' the named role
     * @param roleName the name of the role in question (not the id)
     * @param handle the current sessionhandle
     * @return a Set of Participant objects
     * @throws IOException if there was a problem connecting to the resource service
     * @throws ResourceGatewayException if there was a problem getting the participants
     */
    public Set<Participant> getParticipantsWithRole(String roleName, String handle)
            throws IOException, ResourceGatewayException {
        String xml = successCheck(_rgclient.getParticipantsWithRole(roleName, handle)) ;
        return _marshaller.unmarshallParticipants(xml) ;
    }


    /**
     * Gets the set of Participants that 'occupy' the named position
     * @param positionName the name of the position in question (not the id)
     * @param handle the current sessionhandle
     * @return a Set of Participant objects
     * @throws IOException if there was a problem connecting to the resource service
     * @throws ResourceGatewayException if there was a problem getting the participants
     */
    public Set<Participant> getParticipantsWithPosition(String positionName, String handle)
            throws IOException, ResourceGatewayException {
        String xml = successCheck(_rgclient.getParticipantsWithPosition(positionName, handle)) ;
        return _marshaller.unmarshallParticipants(xml) ;
    }


    /**
     * Gets the set of Participants that hold the named capability
     * @param capabilityName the name of the capability in question (not the id)
     * @param handle the current sessionhandle
     * @return a Set of Participant objects
     * @throws IOException if there was a problem connecting to the resource service
     * @throws ResourceGatewayException if there was a problem getting the participants
     */
    public Set<Participant> getParticipantsWithCapability(String capabilityName, String handle)
            throws IOException, ResourceGatewayException {
        String xml = successCheck(_rgclient.getParticipantsWithCapability(
                                  capabilityName, handle)) ;
        return _marshaller.unmarshallParticipants(xml) ;
    }


    /**
     * Gets the Participant with the specified user id
     * @param userID the userid to match to a Participant
     * @param handle a valid session handle
     * @return the matching Participant
     * @throws IOException if the service can't be reached
     * @throws ResourceGatewayException if there is some problem getting the Participant
     */
    public Participant getParticipantFromUserID(String userID, String handle)
            throws IOException, ResourceGatewayException {
        String xml = successCheck(_rgclient.getParticipantFromUserID(userID, handle));
        return new Participant(JDOMUtil.stringToElement(xml));
    }


    /**
     * Gets the Role with the specified id
     * @param id the id of the Role to get
     * @param handle a valid session handle
     * @return the matching Role
     * @throws IOException if the service can't be reached
     * @throws ResourceGatewayException if there is some problem getting the Role
     */
    public Role getRole(String id, String handle)
            throws IOException, ResourceGatewayException {
        String xml = successCheck(_rgclient.getRole(id, handle));
        return new Role(JDOMUtil.stringToElement(xml));
    }


    /**
     * Gets the Role with the specified name
     * @param name the id of the Role to get
     * @param handle a valid session handle
     * @return the matching Role
     * @throws IOException if the service can't be reached
     * @throws ResourceGatewayException if there is some problem getting the Role
     */
    public Role getRoleByName(String name, String handle)
            throws IOException, ResourceGatewayException {
        String xml = successCheck(_rgclient.getRoleByName(name, handle));
        return new Role(JDOMUtil.stringToElement(xml));
    }


    /**
     * Gets the Capability with the specified id
     * @param id the id of the Capability to get
     * @param handle a valid session handle
     * @return the matching Capability
     * @throws IOException if the service can't be reached
     * @throws ResourceGatewayException if there is some problem getting the Capability
     */
    public Capability getCapability(String id, String handle)
            throws IOException, ResourceGatewayException {
        String xml = successCheck(_rgclient.getCapability(id, handle));
        return new Capability(JDOMUtil.stringToElement(xml));
    }


    /**
     * Gets the Capability with the specified name
     * @param name the id of the Capability to get
     * @param handle a valid session handle
     * @return the matching Capability
     * @throws IOException if the service can't be reached
     * @throws ResourceGatewayException if there is some problem getting the Capability
     */
    public Capability getCapabilityByName(String name, String handle)
            throws IOException, ResourceGatewayException {
        String xml = successCheck(_rgclient.getCapabilityByName(name, handle));
        return new Capability(JDOMUtil.stringToElement(xml));
    }


    /**
     * Gets the Position with the specified id
     * @param id the id of the Position to get
     * @param handle a valid session handle
     * @return the matching Position
     * @throws IOException if the service can't be reached
     * @throws ResourceGatewayException if there is some problem getting the Position
     */
    public Position getPosition(String id, String handle)
            throws IOException, ResourceGatewayException {
        String xml = successCheck(_rgclient.getPosition(id, handle));
        return new Position(JDOMUtil.stringToElement(xml));
    }


    /**
     * Gets the Position with the specified name
     * @param name the id of the Position to get
     * @param handle a valid session handle
     * @return the matching Position
     * @throws IOException if the service can't be reached
     * @throws ResourceGatewayException if there is some problem getting the Position
     */
    public Position getPositionByName(String name, String handle)
            throws IOException, ResourceGatewayException {
        String xml = successCheck(_rgclient.getPositionByName(name, handle));
        return new Position(JDOMUtil.stringToElement(xml));
    }


    /**
     * Gets the OrgGroup with the specified id
     * @param id the id of the OrgGroup to get
     * @param handle a valid session handle
     * @return the matching OrgGroup
     * @throws IOException if the service can't be reached
     * @throws ResourceGatewayException if there is some problem getting the OrgGroup
     */
    public OrgGroup getOrgGroup(String id, String handle)
            throws IOException, ResourceGatewayException {
        String xml = successCheck(_rgclient.getOrgGroup(id, handle));
        return new OrgGroup(JDOMUtil.stringToElement(xml));
    }


    /**
     * Gets the OrgGroup with the specified name
     * @param name the id of the OrgGroup to get
     * @param handle a valid session handle
     * @return the matching OrgGroup
     * @throws IOException if the service can't be reached
     * @throws ResourceGatewayException if there is some problem getting the OrgGroup
     */
    public OrgGroup getOrgGroupByName(String name, String handle)
            throws IOException, ResourceGatewayException {
        String xml = successCheck(_rgclient.getOrgGroupByName(name, handle));
        return new OrgGroup(JDOMUtil.stringToElement(xml));
    }


    /**
     * Gets the UserPrivileges of the Participant with the specified id
     * @param id the id of the Participant to get the UserPrivileges for
     * @param handle a valid session handle
     * @return the matching UserPrivileges
     * @throws IOException if the service can't be reached
     * @throws ResourceGatewayException if there is some problem getting the UserPrivileges
     */
    public UserPrivileges getUserPrivileges(String id, String handle)
            throws IOException, ResourceGatewayException {
        String xml = successCheck(_rgclient.getUserPrivileges(id, handle));
        return new UserPrivileges(JDOMUtil.stringToElement(xml));
    }


    /******************************************************************************/

    /**
     * Adds a new participant to the service's org data
     * @param p the Participant
     * @param handle the current sessionhandle
     * @return the id of the newly added Participant, or an error message if there was
     * a problem (note: the returned id should be subsequently added to the Participant
     * object using p.setID() )
     * @throws IOException if there was a problem connecting to the resource service
     */
    public String addParticipant(Participant p, String handle) throws IOException {
        return addParticipant(p, false, handle);
    }


    /**
     * Adds a new participant to the service's org data
     * @param p the Participant
     * @param encryptPassword if true, will encrypt the current password within 'p'
     * before storing it on the server side (recommended for plain text passwords)
     * @param handle the current sessionhandle
     * @return the id of the newly added Participant, or an error message if there was
     * a problem (note: the returned id should be subsequently added to the Participant
     * object using p.setID() )
     * @throws IOException if there was a problem connecting to the resource service
     */
    public String addParticipant(Participant p, boolean encryptPassword, String handle)
            throws IOException {
        return _rgclient.addParticipant(p.getUserID(), p.getPassword(), encryptPassword,
                p.getLastName(), p.getFirstName(), p.isAdministrator(),
                p.getDescription(), p.getNotes(), handle);
    }


    /**
     * Adds a new capability to the service's org data
     * @param cap the Capability
     * @param handle the current sessionhandle
     * @return the id of the newly added Capability, or an error message if there was
     * a problem (note: the returned id should be subsequently added to the Capability
     * object using cap.setID() )
     * @throws IOException if there was a problem connecting to the resource service
     */
    public String addCapability(Capability cap, String handle) throws IOException {
        return _rgclient.addCapability(cap.getCapability(), cap.getDescription(),
                cap.getNotes(), handle);
    }


    /**
     * Adds a new role to the service's org data
     * @param role the Role
     * @param handle the current sessionhandle
     * @return the id of the newly added Role, or an error message if there was
     * a problem (note: the returned id should be subsequently added to the Role
     * object using role.setID() )
     * @throws IOException if there was a problem connecting to the resource service
     */
    public String addRole(Role role, String handle) throws IOException {
        Role belongsTo = role.getOwnerRole();
        String ownerID = (belongsTo != null) ? belongsTo.getID() : null;
        return _rgclient.addRole(role.getName(), role.getDescription(), role.getNotes(),
                ownerID, handle);
    }


    /**
     * Adds a new position to the service's org data
     * @param pos the Position
     * @param handle the current sessionhandle
     * @return the id of the newly added Position, or an error message if there was
     * a problem (note: the returned id should be subsequently added to the Position
     * object using pos.setID() )
     * @throws IOException if there was a problem connecting to the resource service
     */
    public String addPosition(Position pos, String handle) throws IOException {
        OrgGroup group = pos.getOrgGroup();
        String orgGroupID = (group != null) ? group.getID() : null;
        Position reportsTo = pos.getReportsTo();
        String ownerID = (reportsTo != null) ? reportsTo.getID() : null;
        return _rgclient.addPosition(pos.getTitle(), pos.getPositionID(),
                pos.getDescription(), pos.getNotes(), orgGroupID, ownerID, handle);
    }


    /**
     * Adds a new org group to the service's org data
     * @param group the OrgGroup
     * @param handle the current sessionhandle
     * @return the id of the newly added OrgGroup, or an error message if there was
     * a problem (note: the returned id should be subsequently added to the OrgGroup
     * object using group.setID() )
     * @throws IOException if there was a problem connecting to the resource service
     */
    public String addOrgGroup(OrgGroup group, String handle) throws IOException {
        OrgGroup belongsTo = group.getBelongsTo();
        String ownerID = (belongsTo != null) ? belongsTo.getID() : null;
        return _rgclient.addOrgGroup(group.getGroupName(), group.getGroupType().name(),
                group.getDescription(), group.getNotes(), ownerID, handle);
    }


    /**
     * Adds the specified Participant to the specified Role
     * @param p the Participant
     * @param role the Role to add the Particpant to
     * @param handle the current sessionhandle
     * @return a message indicating success, or describing a problem encountered
     * @throws IOException if there was a problem connecting to the resource service
     */
    public String addParticipantToRole(Participant p, Role role, String handle)
            throws IOException {
        return _rgclient.addParticipantToRole(p.getID(), role.getID(), handle);
    }


    /**
     * Adds the specified Participant to the specified Capability
     * @param p the Participant
     * @param cap the Capability to add the Particpant to
     * @param handle the current sessionhandle
     * @return a message indicating success, or describing a problem encountered
     * @throws IOException if there was a problem connecting to the resource service
     */
    public String addParticipantToCapability(Participant p, Capability cap, String handle)
            throws IOException {
        return _rgclient.addParticipantToCapability(p.getID(), cap.getID(), handle);
    }


    /**
     * Adds the specified Participant to the specified Position
     * @param p the Participant
     * @param pos the Position to add the Particpant to
     * @param handle the current sessionhandle
     * @return a message indicating success, or describing a problem encountered
     * @throws IOException if there was a problem connecting to the resource service
     */
    public String addParticipantToPosition(Participant p, Position pos, String handle)
            throws IOException {
        return _rgclient.addParticipantToPosition(p.getID(), pos.getID(), handle);
    }


    /**
     * Updates the Participant stored in the service's org data with the modified values
     * of the Participant specified
     * @param p the Participant
     * @param encryptPassword if true, will encrypt the current password within 'p'
     * before storing it on the server side (recommended for plain text passwords)
     * @param handle the current sessionhandle
     * @return a message indicating success, or describing a problem encountered
     * @throws IOException if there was a problem connecting to the resource service
     */
    public String updateParticipant(Participant p, boolean encryptPassword, String handle)
            throws IOException {
        return _rgclient.updateParticipant(p.getID(), p.getUserID(), p.getPassword(),
                encryptPassword, p.getLastName(), p.getFirstName(), p.isAdministrator(),
                p.getDescription(), p.getNotes(), handle);
    }


    /**
     * Updates the Participant stored in the service's org data with the modified values
     * of the Participant specified
     * @param p the Participant
     * @param handle the current sessionhandle
     * @return a message indicating success, or describing a problem encountered
     * @throws IOException if there was a problem connecting to the resource service
     */
    public String updateParticipant(Participant p, String handle) throws IOException {
        return updateParticipant(p, false, handle);
    }


    /**
     * Updates the Capability stored in the service's org data with the modified values
     * of the Capability specified
     * @param cap the Capability
     * @param handle the current sessionhandle
     * @return a message indicating success, or describing a problem encountered
     * @throws IOException if there was a problem connecting to the resource service
     */
    public String updateCapability(Capability cap, String handle) throws IOException {
        return _rgclient.updateCapability(cap.getID(), cap.getCapability(),
                cap.getDescription(), cap.getNotes(), handle);
    }


    /**
     * Updates the Role stored in the service's org data with the modified values
     * of the Role specified
     * @param role the Role
     * @param handle the current sessionhandle
     * @return a message indicating success, or describing a problem encountered
     * @throws IOException if there was a problem connecting to the resource service
     */
    public String updateRole(Role role, String handle) throws IOException {
        Role belongsTo = role.getOwnerRole();
        String ownerID = (belongsTo != null) ? belongsTo.getID() : null;
        return _rgclient.updateRole(role.getID(), role.getName(), role.getDescription(),
                role.getNotes(), ownerID, handle);
    }


    /**
     * Updates the Position stored in the service's org data with the modified values
     * of the Position specified
     * @param pos the Position
     * @param handle the current sessionhandle
     * @return a message indicating success, or describing a problem encountered
     * @throws IOException if there was a problem connecting to the resource service
     */
    public String updatePosition(Position pos, String handle) throws IOException {
        OrgGroup group = pos.getOrgGroup();
        String orgGroupID = (group != null) ? group.getID() : null;
        Position reportsTo = pos.getReportsTo();
        String ownerID = (reportsTo != null) ? reportsTo.getID() : null;
        return _rgclient.updatePosition(pos.getID(), pos.getTitle(), pos.getPositionID(),
                pos.getDescription(), pos.getNotes(), orgGroupID, ownerID, handle);
    }


    /**
     * Updates the OrgGroup stored in the service's org data with the modified values
     * of the OrgGroup specified
     * @param group the OrgGroup
     * @param handle the current sessionhandle
     * @return a message indicating success, or describing a problem encountered
     * @throws IOException if there was a problem connecting to the resource service
     */
    public String updateOrgGroup(OrgGroup group, String handle) throws IOException {
        OrgGroup belongsTo = group.getBelongsTo();
        String ownerID = (belongsTo != null) ? belongsTo.getID() : null;
        return _rgclient.updateOrgGroup(group.getID(), group.getGroupName(),
                group.getGroupType().name(), group.getDescription(), group.getNotes(),
                ownerID, handle);
    }


    /**
     * Removes (deletes) the specified Participant from the service's org data
     * @param p the Participant
     * @param handle the current sessionhandle
     * @return a message indicating success, or describing a problem encountered
     * @throws IOException if there was a problem connecting to the resource service
     */
    public String removeParticipant(Participant p, String handle) throws IOException {
        return _rgclient.removeParticipant(p.getID(), handle);
    }


    /**
     * Removes (deletes) the specified Capability from the service's org data
     * @param cap the Capability
     * @param handle the current sessionhandle
     * @return a message indicating success, or describing a problem encountered
     * @throws IOException if there was a problem connecting to the resource service
     */
    public String removeCapability(Capability cap, String handle) throws IOException {
        return _rgclient.removeCapability(cap.getID(), handle);
    }


    /**
     * Removes (deletes) the specified Role from the service's org data
     * @param role the Role
     * @param handle the current sessionhandle
     * @return a message indicating success, or describing a problem encountered
     * @throws IOException if there was a problem connecting to the resource service
     */
    public String removeRole(Role role, String handle) throws IOException {
         return _rgclient.removeRole(role.getID(), handle);
    }


    /**
     * Removes (deletes) the specified Position from the service's org data
     * @param pos the Position
     * @param handle the current sessionhandle
     * @return a message indicating success, or describing a problem encountered
     * @throws IOException if there was a problem connecting to the resource service
     */
    public String removePosition(Position pos, String handle) throws IOException {
         return _rgclient.removePosition(pos.getID(), handle);
    }


    /**
     * Removes (deletes) the specified OrgGroup from the service's org data
     * @param group the OrgGroup
     * @param handle the current sessionhandle
     * @return a message indicating success, or describing a problem encountered
     * @throws IOException if there was a problem connecting to the resource service
     */
    public String removeOrgGroup(OrgGroup group, String handle) throws IOException {
         return _rgclient.removeOrgGroup(group.getID(), handle);
    }


    /**
     * Removes (deletes) the specified Participant from the set of Participants
     * occupying the specified Role
     * @param p the Participant
     * @param role the Role to remove the specified Participant from
     * @param handle the current sessionhandle
     * @return a message indicating success, or describing a problem encountered
     * @throws IOException if there was a problem connecting to the resource service
     */
    public String removeParticipantFromRole(Participant p, Role role, String handle)
            throws IOException {
        return _rgclient.removeParticipantFromRole(p.getID(), role.getID(), handle);
    }


    /**
     * Removes (deletes) the specified Participant from the set of Participants
     * holding the specified Capability
     * @param p the Participant
     * @param cap the Capability to remove the specified Participant from
     * @param handle the current sessionhandle
     * @return a message indicating success, or describing a problem encountered
     * @throws IOException if there was a problem connecting to the resource service
     */
    public String removeParticipantFromCapability(Participant p, Capability cap, String handle)
            throws IOException {
        return _rgclient.removeParticipantFromCapability(p.getID(), cap.getID(), handle);
    }


    /**
     * Removes (deletes) the specified Participant from the set of Participants
     * occupying the specified Position
     * @param p the Participant
     * @param pos the Position to remove the specified Participant from
     * @param handle the current sessionhandle
     * @return a message indicating success, or describing a problem encountered
     * @throws IOException if there was a problem connecting to the resource service
     */
    public String removeParticipantFromPosition(Participant p, Position pos, String handle)
            throws IOException {
        return _rgclient.removeParticipantFromPosition(p.getID(), pos.getID(), handle);
    }


    /**
     * Sets the containing Role for a Role (i.e. the 'parent' Role the Role 'belongs to')
     * @param role the Role
     * @param containingRole the containing or parent Role that encapsulates role
     * @param handle the current sessionhandle
     * @return a message indicating success, or describing a problem encountered
     * @throws IOException if there was a problem connecting to the resource service
     */
    public String setContainingRole(Role role, Role containingRole, String handle)
            throws IOException {
        return _rgclient.setContainingRole(role.getID(), containingRole.getID(), handle);
    }


    /**
     * Sets the containing OrgGroup for an OrgGroup (i.e. the 'parent' OrgGroup the
     * OrgGroup 'belongs to')
     * @param group the OrgGroup
     * @param containingGroup the containing or parent OrgGroup that encapsulates group
     * @param handle the current sessionhandle
     * @return a message indicating success, or describing a problem encountered
     * @throws IOException if there was a problem connecting to the resource service
     */
    public String setContainingOrgGroup(OrgGroup group, OrgGroup containingGroup,
                                        String handle) throws IOException {
        return _rgclient.setContainingOrgGroup(group.getID(), containingGroup.getID(),
                                               handle);
    }


    /**
     * Sets the containing Position for a Position (i.e. the 'parent' Position the
     * Position 'reports to')
     * @param pos the Position
     * @param containingPos the containing or parent Position that encapsulates pos
     * @param handle the current sessionhandle
     * @return a message indicating success, or describing a problem encountered
     * @throws IOException if there was a problem connecting to the resource service
     */
    public String setContainingPosition(Position pos, Position containingPos,
                                        String handle) throws IOException {
        return _rgclient.setContainingPosition(pos.getID(), containingPos.getID(), handle);
    }


    /**
     * Sets the containing OrgGroup for a Position
     * @param pos the Position
     * @param group the containing OrgGroup
     * @param handle the current sessionhandle
     * @return a message indicating success, or describing a problem encountered
     * @throws IOException if there was a problem connecting to the resource service
     */
    public String setPositionOrgGroup(Position pos, OrgGroup group, String handle)
            throws IOException {
        return _rgclient.setPositionOrgGroup(pos.getID(), group.getID(), handle);
    }


    /**
     * Sets the user privileges for a Participant
     * @param p the Participant
     * @param privs the UserPrivileges object
     * @param handle the current sessionhandle
     * @return a message indicating success, or describing a problem encountered
     * @throws IOException if there was a problem connecting to the resource service
     */
    public String setParticipantPrivileges(Participant p, UserPrivileges privs, String handle)
            throws IOException {
        return _rgclient.setParticipantPrivileges(p.getID(), privs.getPrivilegesAsBits(),
                handle);
    }


    /**
     * Sets the user privileges for a Participant
     * @param p the Participant (note: the privilege values used are the ones currently
     * within the Participant object specified) 
     * @param handle the current sessionhandle
     * @return a message indicating success, or describing a problem encountered
     * @throws IOException if there was a problem connecting to the resource service
     */
    public String setParticipantPrivileges(Participant p, String handle)
            throws IOException {
        return setParticipantPrivileges(p, p.getUserPrivileges(), handle);
    }


}