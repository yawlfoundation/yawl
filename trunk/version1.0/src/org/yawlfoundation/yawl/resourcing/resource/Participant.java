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
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.datastore.WorkItemCache;
import org.yawlfoundation.yawl.util.StringUtil;
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

    private ResourceManager _resMgr = ResourceManager.getInstance() ;
    private boolean _persisting ;

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

    public Participant(String lastName, String firstName, String userID) {
//        this(true);
        super();
        setUserID(userID) ;
        _lastname = lastName ;
        _firstname = firstName ;
    }

    public Participant(String lastName, String firstName, String userid, boolean persist) {
        this(lastName, firstName, userid) ;
        _persisting = persist ;
    }

    public Participant(String lastname, String firstname, String userID,
                       boolean isAdministrator, Set<Position> positions,
                       Set<Role> roles, Set<Capability> capabilities) {
        this(true) ;
        _lastname = lastname;
        _firstname = firstname;
        setUserID(userID) ;
        _isAdministrator = isAdministrator;
        _positions = positions;
        _roles = roles;
        _capabilities = capabilities;
    }


    public Participant clone() {

        // create a new Participant with persistence OFF
        Participant result = new Participant(_lastname, _firstname, _userID, false);
        result.setAdministrator(_isAdministrator);
        result.setUserPrivileges(_privileges.clone());
        result.setPassword(_password);
        result.setID(_resourceID);
        result.setNotes(_notes);
        result.setDescription(_description);
        for (Role r : _roles) result.addRole(r);
        for (Position p : _positions ) result.addPosition(p) ;
        for (Capability c : _capabilities) result.addCapability(c);
        return result ;
    }

    // copies values from p to this (does NOT change id)
    public void merge(Participant p) {
        _lastname = p.getLastName();
        _firstname = p.getFirstName();
        setUserID(p.getUserID());
        _isAdministrator = p.isAdministrator();
        _password = p.getPassword();
        _notes = p.getNotes();
        _description = p.getDescription();
        setRoles(p.getRoles());
        setPositions(p.getPositions());
        setCapabilities(p.getCapabilities());
        _privileges.merge(p.getUserPrivileges());
    }


    private void updateThis() {
        if (_persisting) _resMgr.updateParticipant(this);
    }

    public void save() { _resMgr.updateParticipant(this); }

    public void setPersisting(boolean persisting) {
        _persisting = persisting;
    }

    public boolean isPersisting() { return _persisting; }

    /** GETTERS & SETTERS **/

    public void setID(String id) {
        _resourceID = id;
        _privileges.setID(id);
        if (_qSet != null) _qSet.setID(id);
    }

    public String getFirstName() { return _firstname ; }

    public void setFirstName(String name) {
        _firstname = name ;
        updateThis();
    }

    public String getLastName() { return _lastname ; }

    public void setLastName(String name) {
        _lastname = name ;
        updateThis();
    }

    public String getFullName() {
        return String.format( "%s %s", _firstname, _lastname);
    }


    public String getUserID() { return _userID; }

    public void setUserID(String id) {
        _userID = id.replaceAll(" ", "_") ;   // replace spaces with underscrores
        updateThis();
    }


    public String getPassword() { return _password; }

    public void setPassword(String pw) {
        _password = pw ;
        updateThis();
    }


    public boolean isAdministrator() { return _isAdministrator; }

    public void setAdministrator(boolean canAdministrate) {
        _isAdministrator = canAdministrate ;
        updateThis();
    }


    public void setUserPrivileges(UserPrivileges up) {
        if (up != null) {
            _privileges = up ;
            _privileges.setID(_resourceID);
            updateThis();
        }
    }

    public UserPrivileges getUserPrivileges() {
       return _privileges ;
    }


    public Set<Role> getRoles() { return _roles ; }

    public void setRoles(Set<Role> roleSet) {
        removeRoles();
        for (Role role : roleSet) addRole(role)  ;
    }

    public void addRole(Role role) {
        if (role != null) {
            _roles.add(role) ;
            role.addResource(this);
            updateThis();
        }
    }

    public void addRole(String rid) {
        addRole(_resMgr.getRole(rid));
    }

    public void removeRole(Role role) {
        if (_roles.remove(role)) {
            role.removeResource(this);
            updateThis();
        }
    }

    public void removeRole(String rid) {
        for (Role r : _roles) {
            if (r.getID().equals(rid)) {
                removeRole(r);
                break;
            }
        }
    }

    public void removeRoles() {
        for (Role r : _roles) r.removeResource(this);
        _roles.clear();
    }

    public boolean hasRole(Role role) { return _roles.contains(role) ; }



    public Set<Capability> getCapabilities() { return _capabilities ; }

    public void setCapabilities(Set<Capability> capSet) {
        removeCapabilities();
        for (Capability cap : capSet) addCapability(cap) ;
    }

    public void addCapability(Capability cap) {
        if (cap != null) {
            _capabilities.add(cap) ;
            cap.addResource(this);
            updateThis();
        }
    }

    public void addCapability(String cid) {
        addCapability(_resMgr.getCapability(cid));
    }

    public void removeCapability(Capability cap) {
        if (_capabilities.remove(cap)) {
            cap.removeResource(this);
            updateThis();
        }
    }

    public void removeCapability(String cid) {
        for (Capability c : _capabilities) {
            if (c.getID().equals(cid)) {
                removeCapability(c);
                break;
            }
        }
    }

    public void removeCapabilities() {
        for (Capability c : _capabilities) c.removeResource(this);
        _capabilities.clear();
    }


    public boolean hasCapability(Capability cap) { return _capabilities.contains(cap) ; }



    public Set<Position> getPositions() { return _positions ; }

    public void setPositions(Set<Position> posSet) {
        removePositions();
        for (Position pos : posSet) addPosition(pos)  ;
    }

    public void addPosition(Position pos) {
        if (pos != null) {
            _positions.add(pos) ;
            pos.addResource(this);
            updateThis();
        }
    }

    public void addPosition(String pid) {
        addPosition(_resMgr.getPosition(pid));
    }


    public void removePosition(Position pos) {
        if (_positions.remove(pos)) {
            pos.removeResource(this);
            updateThis();
        }
    }

    public void removePosition(String pid) {
        for (Position p : _positions) {
            if (p.getID().equals(pid)) {
                removePosition(p);
                break;
            }
        }
    }

    public void removePositions() {
        for (Position p : _positions) p.removeResource(this);
        _positions.clear();
    }


    public boolean hasPosition(Position pos) { return _positions.contains(pos) ; }


    public void removeAttributeReferences() {
        removeRoles();
        removePositions();
        removeCapabilities();
    }

    public QueueSet getWorkQueues() { return _qSet ; }

    public void setWorkQueues(QueueSet q) {
        _qSet = q ;
        updateThis();
    }

    /** returns and initialised qSet if init is true */
    public QueueSet getWorkQueues(boolean init) {
        if (init && _qSet == null) createQueueSet(false);
        return _qSet ;
    }

    public QueueSet createQueueSet(boolean persisting) {
        _qSet = new QueueSet(_resourceID, QueueSet.setType.participantSet, persisting) ;
        updateThis();
        return _qSet ;
    }


    public void restoreWorkQueue(WorkQueue q, WorkItemCache cache, boolean persisting) {
        if (_qSet == null) createQueueSet(persisting) ;
        _qSet.restoreWorkQueue(q, cache) ;
        updateThis();
    }

    public boolean equals(Object o) {
        return (o instanceof Participant) && ((Participant) o).getID().equals(_resourceID);
    }

    public String getSummaryXML() {
        StringBuilder xml = new StringBuilder() ;
        xml.append(String.format("<participant id=\"%s\">", _resourceID)) ;
        xml.append(StringUtil.wrap(_userID, "userid"));
        xml.append(StringUtil.wrap(_firstname, "firstname"));
        xml.append(StringUtil.wrap(_lastname, "lastname"));
        xml.append(StringUtil.wrap(String.valueOf(_isAdministrator), "isAdministrator")) ;
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
