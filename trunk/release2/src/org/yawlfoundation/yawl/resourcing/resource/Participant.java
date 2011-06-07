/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing.resource;

import org.jdom.Element;
import org.yawlfoundation.yawl.resourcing.QueueSet;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.WorkQueue;
import org.yawlfoundation.yawl.resourcing.datastore.WorkItemCache;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.PasswordEncryptor;
import org.yawlfoundation.yawl.util.StringUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a single participant (i.e. human) resource. Also manages the participant's
 * work queues.
 *
 *  @author Michael Adams
 *  v0.1, 03/08/2007
 */

public class Participant extends AbstractResource implements Cloneable {

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
        }
    }

    public Participant(String id) {
        super();
        _resourceID = id ;
    }

    public Participant(String lastName, String firstName, String userID) {
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

    public Participant(Element e) {
        super();
        reconstitute(e);
    }


    public Participant clone() throws CloneNotSupportedException {

        // create a new Participant with persistence OFF
        Participant cloned = (Participant) super.clone();
        cloned.setPersisting(false);
        cloned.setID("_CLONE_" + _resourceID);                  // different id to this
        if (_privileges != null)
            cloned.setUserPrivileges(_privileges.clone());
        else
            cloned.setUserPrivileges(new UserPrivileges(_resourceID));

        for (Role r : _roles) cloned.addRole(r);
        for (Position p : _positions) cloned.addPosition(p);
        for (Capability c : _capabilities) cloned.addCapability(c);
        return cloned;
    }

    // copies values from p to this (does NOT change id)
    public void merge(Participant p) {
        super.merge(p);
        _lastname = p.getLastName();
        _firstname = p.getFirstName();
        setUserID(p.getUserID());
        _isAdministrator = p.isAdministrator();
        _password = p.getPassword();
        mergeRoles(p.getRoles());
        mergePositions(p.getPositions());
        mergeCapabilities(p.getCapabilities());

        if (_privileges == null) _privileges = new UserPrivileges(_resourceID) ; 
         _privileges.merge(p.getUserPrivileges());
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

    public String getName() {
        return getFullName();
    }

    public String getFirstName() { return _firstname ; }

    public void setFirstName(String name) {
        _firstname = name ;
    }

    public String getLastName() { return _lastname ; }

    public void setLastName(String name) {
        _lastname = name ;
    }

    public String getFullName() {
        return String.format( "%s %s", _firstname, _lastname);
    }


    public String getUserID() { return _userID; }

    public void setUserID(String id) {
        _userID = id.replaceAll(" ", "_") ;   // replace any spaces with underscrores
    }


    public String getPassword() { return _password; }

    public void setPassword(String pw) {
        _password = pw ;
    }

    public void setPassword(String pw, boolean encrypt) {
        if (encrypt) {
            pw = PasswordEncryptor.encrypt(pw, pw);
        }
        setPassword(pw) ;        
    }

    public boolean isValidPassword(String password) {
        return getPassword().equals(password);
    }


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
        removeRoles();
        for (Role role : roleSet) addRole(role)  ;
    }

    public void addRole(Role role) {
        if (role != null) {
            _roles.add(role) ;
            role.addResource(this);
        }
    }

    public void addRole(String rid) {
        addRole(_resMgr.getOrgDataSet().getRole(rid));
    }

    public void mergeRoles(Set<Role> roleSet) {
        for (Role r : roleSet) {
            if (! _roles.contains(r)) _roles.add(r);
        }
    }

    public void removeRole(Role role) {
        if (_roles.remove(role)) {
            role.removeResource(this);
        }
    }

    public void removeRole(String rid) {
        if (rid != null) {
            for (Role r : _roles) {
                if (r.getID().equals(rid)) {
                    removeRole(r);
                    break;
                }
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
        }
    }

    public void addCapability(String cid) {
        addCapability(_resMgr.getOrgDataSet().getCapability(cid));
    }

    public void mergeCapabilities(Set<Capability> capSet) {
        for (Capability c : capSet) {
            if (! _capabilities.contains(c)) _capabilities.add(c);
        }
    }


    public void removeCapability(Capability cap) {
        if (_capabilities.remove(cap)) {
            cap.removeResource(this);
        }
    }

    public void removeCapability(String cid) {
        if (cid != null) {
            for (Capability c : _capabilities) {
                if (c.getID().equals(cid)) {
                    removeCapability(c);
                    break;
                }
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
        for (Position pos : posSet) addPosition(pos);
    }

    public void addPosition(Position pos) {
        if (pos != null) {
            _positions.add(pos) ;
            pos.addResource(this);
        }
    }

    public void addPosition(String pid) {
        addPosition(_resMgr.getOrgDataSet().getPosition(pid));
    }


    public void removePosition(Position pos) {
        if (_positions.remove(pos)) {
            pos.removeResource(this);
        }
    }

    public void removePosition(String pid) {
        if (pid != null) {
            for (Position p : _positions) {
                if (p.getID().equals(pid)) {
                    removePosition(p);
                    break;
                }
            }
        }
    }

    public void removePositions() {
        for (Position p : _positions) p.removeResource(this);
        _positions.clear();
    }

    public void mergePositions(Set<Position> posSet) {
        for (Position p : posSet) {
            if (! _positions.contains(p)) _positions.add(p);
        }
    }


    public boolean hasPosition(Position pos) { return _positions.contains(pos) ; }


    public void removeAttributeReferences() {
        removeRoles();
        removePositions();
        removeCapabilities();
    }

    public Set<AbstractResourceAttribute> getAttributeReferences() {
        Set<AbstractResourceAttribute> attributes = new HashSet<AbstractResourceAttribute>();
        attributes.addAll(getRoles());
        attributes.addAll(getPositions());
        attributes.addAll(getCapabilities());
        return attributes;
    }

    public void setAttributeReferences(Set<AbstractResourceAttribute> attributes) {
        if (attributes != null) {
            removeAttributeReferences();
            for (AbstractResourceAttribute attribute : attributes) {
                if (attribute instanceof Role) {
                    addRole((Role) attribute);
                }
                else if (attribute instanceof Capability) {
                    addCapability((Capability) attribute);
                }
                else if (attribute instanceof Position) {
                    addPosition((Position) attribute);
                }
            }
        }
    }


    public boolean isOrgGroupMember(OrgGroup og) {
        for (Position p : _positions) {
            OrgGroup group = p.getOrgGroup();
            while (group != null) {
                if (group.equals(og)) return true;
                group = group.getBelongsTo(); 
            }
        }
        return false;
    }

    public QueueSet getWorkQueues() { return _qSet ; }

    public void setWorkQueues(QueueSet q) {
        _qSet = q ;
    }

    /** returns an initialised qSet if init is true */
    public QueueSet getWorkQueues(boolean init) {
        if (init && _qSet == null) createQueueSet(false);
        return _qSet ;
    }

    public QueueSet createQueueSet(boolean persisting) {
        _qSet = new QueueSet(_resourceID, QueueSet.setType.participantSet, persisting) ;
        return _qSet ;
    }


    public void attachWorkQueue(WorkQueue q, boolean persisting) {
        if (_qSet == null) createQueueSet(persisting) ;
        _qSet.setQueue(q) ;
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder() ;
        xml.append(String.format("<participant id=\"%s\">", _resourceID)) ;
        xml.append(StringUtil.wrapEscaped(_userID, "userid"));
        xml.append(StringUtil.wrapEscaped(_firstname, "firstname"));
        xml.append(StringUtil.wrapEscaped(_lastname, "lastname"));
        xml.append(StringUtil.wrapEscaped(String.valueOf(_isAdministrator), "isAdministrator")) ;

        xml.append("<roles>");
        for (Role role : _roles) xml.append(role.toXML()) ;
        xml.append("</roles>");

        xml.append("<positions>");
        for (Position position : _positions) xml.append(position.toXML()) ;
        xml.append("</positions>");

        xml.append("<capabilities>");
        for (Capability capability : _capabilities) xml.append(capability.toXML()) ;
        xml.append("</capabilities>");

        xml.append("</participant>");
        return xml.toString() ;
    }

    public void fromXML(String xml) {
        if (xml != null)
            reconstitute(JDOMUtil.stringToElement(xml)) ;       
    }

    public void reconstitute(Element e) {
        setID(e.getAttributeValue("id"));
        setUserID(JDOMUtil.decodeEscapes(e.getChildText("userid")));
        setFirstName(JDOMUtil.decodeEscapes(e.getChildText("firstname")));
        setLastName(JDOMUtil.decodeEscapes(e.getChildText("lastname")));
        setAdministrator(e.getChildText("isAdministrator").equals("true"));
    }

}
