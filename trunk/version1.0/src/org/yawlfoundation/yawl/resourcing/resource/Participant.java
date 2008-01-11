/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.resource;

import org.yawlfoundation.yawl.resourcing.QueueSet;
import org.yawlfoundation.yawl.resourcing.WorkQueue;
import org.yawlfoundation.yawl.resourcing.datastore.WorkItemCache;
import org.jdom.Element;

import java.util.HashSet;
import java.util.Set;
import java.io.Serializable;

/**
 * Represents a single participant (i.e. human) resource. Also manages the participant's
 * work queues.
 *
 *  @author Michael Adams
 *  v0.1, 03/08/2007
 */

public class Participant extends AbstractResource implements Serializable {

    // participant descriptive data
    private String _lastname ;
    private String _firstname ;
    private String _userID ;
    private String _password ;
    private boolean _isAdministrator;
    private Set<Position> _positions = new HashSet<Position>();
    private Set<Role> _roles = new HashSet<Role>();
    private Set<Capability> _capabilities = new HashSet<Capability>();
    private UserPrivileges _privileges = new UserPrivileges(_resourceID) ;

    // participant's work queues
    private QueueSet _qSet ;



    /** CONSTRUCTORS **/


    public Participant() { super() ; }                   // for hibernate persistence

    public Participant(boolean newParticipant) {
        super() ;
        if (newParticipant) {
            _privileges = new UserPrivileges(_resourceID) ;
 //           _qSet = new QueueSet(_resourceID, QueueSet.setType.participantSet) ;
        }
    }

    public Participant(String id) {
        super();
        _resourceID = id ;
    }

    public Participant(String lastName, String firstName, String userid) {
//        this(true);
        super();
        _userID = userid ;
        _lastname = lastName ;
        _firstname = firstName ;
    }


    public Participant(String lastname, String firstname, String userID,
                       boolean isAdministrator, Set<Position> positions,
                       Set<Role> roles, Set<Capability> capabilities) {
        this(true) ;
        _lastname = lastname;
        _firstname = firstname;
        _userID = userID;
        _isAdministrator = isAdministrator;
        _positions = positions;
        _roles = roles;
        _capabilities = capabilities;
    }


    /** GETTERS & SETTERS **/

    public void setID(String id) {
        _resourceID = id;
        _privileges.setID(id);
        if (_qSet != null) _qSet.setID(id);          
    }

    public String getFirstName() { return _firstname ; }

    public void setFirstName(String name) { _firstname = name ; }

    public String getLastName() { return _lastname ; }

    public void setLastName(String name) { _lastname = name ; } 

    public String getFullName() {
        return String.format( "%s %s", _firstname, _lastname);
    }


    public String getUserID() { return _userID; }

    public void setUserID(String id) { _userID = id ; }


    public String getPassword() { return _password; }

    public void setPassword(String pw) { _password = pw ; }


    public boolean isAdministrator() { return _isAdministrator; }

    public void setAdministrator(boolean canAdministrate) {
        _isAdministrator = canAdministrate ;
    }


    public void setUserPrivileges(UserPrivileges up) {
        if (up != null) {
            _privileges = up ;
            _privileges.setID(_resourceID);
        }
    }

    public UserPrivileges getUserPrivileges() {
       return _privileges ;
    }


    public Set<Role> getRoles() { return _roles ; }

    public void setRoles(Set<Role> roleSet) {
        _roles.clear();
        for (Role role : roleSet) addRole(role)  ;
    }

    public void addRole(Role role) {
        if (role != null) {
            _roles.add(role) ;
            role.addResource(this);
        }
    }

    public void removeRole(Role role) {
        if (_roles.remove(role)) role.removeResource(this);
    }

    public boolean hasRole(Role role) { return _roles.contains(role) ; }



    public Set<Capability> getCapabilities() { return _capabilities ; }

    public void setCapabilities(Set<Capability> capSet) {
        _capabilities.clear();
        for (Capability cap : capSet) addCapability(cap) ;
    }

    public void addCapability(Capability cap) {
        if (cap != null) {
            _capabilities.add(cap) ;
            cap.addResource(this);
        }
    }

    public void removeCapability(Capability cap) {
        if (_capabilities.remove(cap)) cap.removeResource(this);
    }

    public boolean hasCapability(Capability cap) { return _capabilities.contains(cap) ; }



    public Set<Position> getPositions() { return _positions ; }

    public void setPositions(Set<Position> posSet) {
        _positions.clear();
        for (Position pos : posSet) addPosition(pos)  ;
    }

    public void addPosition(Position pos) {
        if (pos != null) {
            _positions.add(pos) ;
            pos.addResource(this);
        }
    }

    public void removePosition(Position pos) {
        if (_positions.remove(pos)) pos.removeResource(this);
    }

    public boolean hasPosition(Position pos) { return _positions.contains(pos) ; }


    public QueueSet getWorkQueues() { return _qSet ; }

    public void setWorkQueues(QueueSet q) { _qSet = q ; }

    /** returns and initialised qSet if init is true */
    public QueueSet getWorkQueues(boolean init) {
        if (init && _qSet == null) createWorkQueues(false);
        return _qSet ;
    }

    public QueueSet createWorkQueues(boolean persisting) {
        _qSet = new QueueSet(_resourceID, QueueSet.setType.participantSet, persisting) ;
        return _qSet ;
    }


    public void restoreWorkQueue(WorkQueue q, WorkItemCache cache, boolean persisting) {
        if (_qSet == null) createWorkQueues(persisting) ;
        _qSet.restoreWorkQueue(q, cache) ;
    }

    public boolean equals(Object o) {
        return (o instanceof Participant) && ((Participant) o).getID().equals(_resourceID);
    }

    public String getSummaryXML() {
        StringBuilder xml = new StringBuilder() ;
        xml.append(String.format("<participant id=\"%s\">", _resourceID)) ;
        xml.append(wrap(_userID, "userid"));
        xml.append(wrap(_firstname, "firstname"));
        xml.append(wrap(_lastname, "lastname"));
        xml.append(wrap(String.valueOf(_isAdministrator), "isAdministrator")) ;
        xml.append("</participant>");
        return xml.toString() ;
    }

    public void reconstitute(Element e) {
        setID(e.getAttributeValue("id"));
        setUserID(e.getChildText("userid"));
        setFirstName(e.getChildText("firstname"));
        setLastName(e.getChildText("lastname"));
        setAdministrator(e.getChildText("isAdministrator").equals("true"));
    }

}
