/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing;

import org.jdom.Element;
import org.jdom.Namespace;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.util.XNode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Maps each of a set of User-Task Privileges to a set of Participants (if any) for
 * one particular task. The mappings are listed in the specification file, and may map
 * a privilege to individual participants and/or all the participants that fulfil a
 * certain Role or Roles.
 *
 *  @author Michael Adams
 *  v0.1, 13/08/2007
 *
 *  Last Date: 20/09/2007
 */

public class TaskPrivileges {

    // the user-task privileges sets
    private HashSet<Participant> _canSuspend = new HashSet<Participant>();
    private HashSet<Participant> _canReallocateStateless = new HashSet<Participant>();
    private HashSet<Participant> _canReallocateStateful = new HashSet<Participant>();
    private HashSet<Participant> _canDeallocate = new HashSet<Participant>() ;
    private HashSet<Participant> _canDelegate = new HashSet<Participant>() ;
    private HashSet<Participant> _canSkip = new HashSet<Participant>();
    private HashSet<Participant> _canPile = new HashSet<Participant>() ;

    // the privilege types
    public static final int CAN_SUSPEND = 0 ;
    public static final int CAN_REALLOCATE_STATELESS = 1 ;
    public static final int CAN_REALLOCATE_STATEFUL = 2 ;
    public static final int CAN_DEALLOCATE = 3 ;
    public static final int CAN_DELEGATE = 4 ;
    public static final int CAN_SKIP = 5 ;
    public static final int CAN_PILE = 6 ;

    // the specification & task this set of privileges applies to
    private YSpecificationID _specID;
    private String _taskID;

    // a flag to allow or disallow all for a particular privilege
    private boolean[] _allowAll = {false, false, false, false, false, false, false};

    // this set is used only when generating specification xml
    private HashSet[] _roleSet = new HashSet[7];

    /******************************************************************************/

    // CONSTRUCTORS //
    
    public TaskPrivileges() {}                                     // for persistence

    public TaskPrivileges(String taskID) { _taskID = taskID; }

    public TaskPrivileges(YSpecificationID specID, String taskID) {
        _specID = specID ;
        _taskID = taskID ;
    }


    // GETTER & SETTER //

    public YSpecificationID getSpecID() { return _specID; }

    public void setSpecID(YSpecificationID specID) { _specID = specID; }


    public String getTaskID() { return _taskID; }

    public void setTaskID(String taskID) { _taskID = taskID; }


    /*******************************************************************************/

    // PRIVATE METHODS //

    /** @return true if the Participant p is a member of the set */
    private boolean contains(HashSet<Participant> set, Participant p) {
        return ((set != null) && (p != null) && (set.contains(p)));
    }


    /** @return true if all participants have been granted the specified privilege */
    private boolean allAllowed(int privilege) { return _allowAll[privilege] ; }


    /** @return true if priv is a valid privilege type */
    private boolean validPrivilege(int priv) {
        return ((priv >= CAN_SUSPEND) && (priv <= CAN_PILE));
    }


    /** returns true if the privilege has been granted to one or more Roles */
    private boolean hasRoles(int priv) { return (_roleSet[priv] != null) ; }


    /** these 3 add methods adds to a privilege set a Participant, a Participant by ID,
     *  and a Set of Particpants respectively */

    private void add(HashSet<Participant> set, Participant p) {
        if (p != null) set.add(p) ;
    }

    private void add(HashSet<Participant> set, String pid) {
        Participant p = ResourceManager.getInstance().getOrgDataSet().getParticipant(pid) ;
        add(set, p) ;
    }

    private void addSet(HashSet<Participant> set, Set<Participant> participants) {
        if (participants != null) set.addAll(participants) ;
    }

    /** returns the relevant privilege set for the privilege type passed */
    private HashSet<Participant> getPrivSet(int priv) {
        switch (priv) {
            case CAN_SUSPEND : return _canSuspend ;
            case CAN_REALLOCATE_STATELESS : return _canReallocateStateless;
            case CAN_REALLOCATE_STATEFUL : return _canReallocateStateful;
            case CAN_DEALLOCATE : return _canDeallocate;
            case CAN_DELEGATE : return _canDelegate;
            case CAN_SKIP : return _canSkip;
            case CAN_PILE : return _canPile;
        }
        return null ;
    }

    /** returns the relevant privilege set for the privilege string passed */
    private HashSet<Participant> getPrivSet(String privStr) {
        return getPrivSet(getPrivilegeType(privStr)) ;
    }

    /** returns the string version of the privilege type */
    private String getPrivString(int priv) {
        switch (priv) {
            case CAN_SUSPEND : return "canSuspend" ;
            case CAN_REALLOCATE_STATELESS : return "canReallocateStateless";
            case CAN_REALLOCATE_STATEFUL : return "canReallocateStateful";
            case CAN_DEALLOCATE : return "canDeallocate";
            case CAN_DELEGATE : return "canDelegate";
            case CAN_SKIP : return "canSkip";
            case CAN_PILE : return "canPile";
        }
        return null ;
    }

    /** returns the privilege type for the string version passed */
    public int getPrivilegeType(String privStr) {
        if (privStr.equalsIgnoreCase("canSuspend")) return CAN_SUSPEND ;
        if (privStr.equalsIgnoreCase("canReallocateStateless"))
                                                    return CAN_REALLOCATE_STATELESS;
        if (privStr.equalsIgnoreCase("canReallocateStateful"))
                                                    return CAN_REALLOCATE_STATEFUL;
        if (privStr.equalsIgnoreCase("canDeallocate")) return CAN_DEALLOCATE;
        if (privStr.equalsIgnoreCase("canDelegate")) return CAN_DELEGATE;
        if (privStr.equalsIgnoreCase("canSkip")) return CAN_SKIP;
        if (privStr.equalsIgnoreCase("canPile")) return CAN_PILE;
        return -1 ;
    }


    /******************************************************************************/

    // GRANT PRIVILEGE METHODS //

    /**
     * Grants the privilege the Participant with id passed
     * @param priv the privilege to grant
     * @param id the id of the Participant (must be a known Participant id)
     */
    public void grant(int priv, String id) {
        if (validPrivilege(priv)) add(getPrivSet(priv), id) ;
    }

    /**
     * Grants a privilege to a Participant
     * @param priv the privilege to grant
     * @param p the Participant
     */
    public void grant(int priv, Participant p) {
        if (validPrivilege(priv)) add(getPrivSet(priv), p) ;
    }

    public void grant(String privName, Participant p) {
        grant(getPrivilegeType(privName), p) ;
    }


    /**
     * Grants a privilege to set of Participant
     * @param priv the privilege to grant
     * @param pSet the Participant Set
     */
    public void grant(int priv, Set<Participant> pSet) {
        if (validPrivilege(priv)) addSet(getPrivSet(priv), pSet) ; 
    }

    public void grant(String privName, Set<Participant> pSet) {
        grant(getPrivilegeType(privName), pSet) ;
    }

    /**
     * Grants a privilege to set of Participant using a set of ids
     * @param priv the privilege to grant
     * @param pSet the Set of Participant ids
     */
    public void grantByID(int priv, Set pSet) {
        if (validPrivilege(priv)) {
            switch (priv) {
                case CAN_SUSPEND : grantSuspendByID(pSet); break;
                case CAN_REALLOCATE_STATELESS : grantReallocateStatelessByID(pSet); break;
                case CAN_REALLOCATE_STATEFUL : grantReallocateStatefulByID(pSet); break;
                case CAN_DEALLOCATE : grantDeallocateByID(pSet); break;
                case CAN_DELEGATE : grantDelegateByID(pSet); break;
                case CAN_SKIP : grantSkipByID(pSet); break;
                case CAN_PILE : grantPileByID(pSet);
            }
        }
    }

    /** grant a privilege to all Participants */
    public void allowAll(int privilege) { _allowAll[privilege] = true ; }

    public void allowAll(String privilegeStr) {
        int type = getPrivilegeType(privilegeStr);
        if (validPrivilege(type)) allowAll(type) ;
    }



    /** disallow a privilege for all Participants */
    public void disallowAll(int privilege) { _allowAll[privilege] = false ; }

    public void disallowAll(String privilegeStr) {
        int type = getPrivilegeType(privilegeStr);
        if (validPrivilege(type)) disallowAll(type) ;
    }
    


    /** grant the privilege to the Participant without checking the validity of the id.
        Written to allow the editor to generate specification xml */
    public void addParticipantToPrivilegeUnchecked(String pid, int priv) {
        getPrivSet(priv).add(new Participant(pid)) ;
    }

    /** grant the privilege to the Role without checking the validity of the id.
        Written to allow the editor to generate specification xml */
    public void addRoleToPrivilegeUnchecked(String rid, int priv) {
        if (_roleSet[priv] == null) _roleSet[priv] = new HashSet() ;
        _roleSet[priv].add(rid) ;
    }


    /** Convenience grant methods (by Participant, id, Participant set, id Set) */

    public void grantSuspend(Participant p) { add(_canSuspend, p) ; }

    public void grantReallocateStateless(Participant p) {
        add(_canReallocateStateless, p) ;
    }

    public void grantReallocateStateful(Participant p) {
        add(_canReallocateStateful, p) ;
    }

    public void grantDeallocate(Participant p) { add(_canDeallocate, p) ; }

    public void grantDelegate(Participant p) { add(_canDelegate, p) ; }

    public void grantSkip(Participant p) { add(_canSkip, p) ; }

    public void grantPile(Participant p) { add(_canPile, p) ; }


    public void grantSuspend(Set<Participant> p) { addSet(_canSuspend, p) ; }

    public void grantReallocateStateless(Set<Participant> p) {
        addSet(_canReallocateStateless, p) ;
    }

    public void grantReallocateStateful(Set<Participant> p) {
        addSet(_canReallocateStateful, p) ;
    }

    public void grantDeallocate(Set<Participant> p) { addSet(_canDeallocate, p) ; }

    public void grantDelegate(Set<Participant> p) { addSet(_canDelegate, p) ; }

    public void grantSkip(Set<Participant> p) { addSet(_canSkip, p) ; }

    public void grantPile(Set<Participant> p) { addSet(_canPile, p) ; }


    public void grantSuspendByID(String userid) { add(_canSuspend, userid) ; }

    public void grantReallocateStatelessByID(String userid) {
        add(_canReallocateStateless, userid) ;
    }

    public void grantReallocateStatefulByID(String userid) {
        add(_canReallocateStateful, userid) ;
    }

    public void grantDeallocateByID(String userid) { add(_canDeallocate, userid) ; }

    public void grantDelegateByID(String userid) {add(_canDelegate, userid) ; }

    public void grantSkipByID(String userid) { add(_canSkip, userid) ; }

    public void grantPileByID(String userid) { add(_canPile, userid) ; }


    public void grantSuspendByID(Set userids) {
        for (Object id : userids) add(_canSuspend, (String) id) ;
    }

    public void grantReallocateStatelessByID(Set userids) {
        for (Object id : userids) add(_canReallocateStateless, (String) id) ;
    }

    public void grantReallocateStatefulByID(Set userids) {
        for (Object id : userids) add(_canReallocateStateful, (String) id) ;
    }

    public void grantDeallocateByID(Set userids) {
        for (Object id : userids) add(_canDeallocate, (String) id) ;
    }

    public void grantDelegateByID(Set userids) {
        for (Object id : userids) add(_canDelegate, (String) id) ;
    }

    public void grantSkipByID(Set userids) {
        for (Object id : userids) add(_canSkip, (String) id) ;
    }

    public void grantPileByID(Set userids) {
        for (Object id : userids) add(_canPile, (String) id) ;
    }


    /*********************************************************************************/

    /** Convenience boolean methods - return true if the Particpant passed has
     *  been granted the privilege */

    public boolean canSuspend(Participant p) {
        return hasPrivilege(p, CAN_SUSPEND) ;
    }

    public boolean canReallocateStateless(Participant p) {
        return hasPrivilege(p, CAN_REALLOCATE_STATELESS);
    }

    public boolean canReallocateStateful(Participant p) {
        return hasPrivilege(p, CAN_REALLOCATE_STATEFUL) ;
    }

    public boolean canDeallocate(Participant p) {
        return hasPrivilege(p, CAN_DEALLOCATE) ;
    }

    public boolean canDelegate(Participant p) { return hasPrivilege(p, CAN_DELEGATE) ; }

    public boolean canSkip(Participant p) { return hasPrivilege(p, CAN_SKIP) ; }

    public boolean canPile(Participant p) { return hasPrivilege(p, CAN_PILE); }

    
    /** returns true if the Participant has been granted the Privilege */
    public boolean hasPrivilege(Participant p, int privilege) {

        // if allAllowed, set membership means exclusion rather than inclusion
        return allAllowed(privilege) ^ contains(getPrivSet(privilege), p) ;
    }


    /** returns true if at least one Participant has been granted the Privilege */
    public boolean hasParticipants(int priv) { return (! getPrivSet(priv).isEmpty()) ; }


    /*********************************************************************************/

    // CLASS INPUT AND OUTPUT //


    /** Parses the xml element passed into a set of privileges and who has them
     *
     * @param e the element containing the privileges from the spec xml
     */
    public void parse(Element e, Namespace nsYawl) {
        if (e != null) {
            List ePrivileges = e.getChildren("privilege", nsYawl);

            // if no privileges element to deal with, we're done
            if (ePrivileges == null) return;

            for (Object o : ePrivileges) {
                Element ePrivilege = (Element) o;

                // get the privilege set we're referring to
                String privName = ePrivilege.getChildText("name", nsYawl);
                int priv = getPrivilegeType(privName);
                HashSet<Participant> privSet = getPrivSet(privName);

                // are all allowed?
                String allowall = ePrivilege.getChildText("allowall", nsYawl);
                if (allowall != null) {
                    if (allowall.equalsIgnoreCase("true")) allowAll(priv);
                    else disallowAll(priv);
                }

                // get the set of participant and/or role tags for this privilege
                List eSet = ePrivilege.getChildren();
                for (Object obj : eSet) {
                    Element eResource = (Element) obj;

                    // if it's a 'role' child, unpack it to a set of participants ...
                    if (eResource.getName().equals("role")) {
                        String rid = eResource.getText();
                        Set<Participant> pSet = ResourceManager.getInstance()
                                .getOrgDataSet().getRoleParticipants(rid);
                        addSet(privSet, pSet);

                        // remember that this set was added as a role
                        addRoleToPrivilegeUnchecked(rid, priv);
                    }

                    // ... otherwise add the participant to the privilege
                    else add(privSet, eResource.getText());
                }
            }
        }        
    }

    /** creates xml to describe the entire object */
    public String toXML() {
        XNode privileges = buildPrivilegesNode();

//        // get the set of participants and roles for each privilege
//        for (int i=CAN_SUSPEND; i<=CAN_PILE; i++)
//            privileges.addChild(buildXMLForPrivilege(i));

//        StringBuilder xml = new StringBuilder("<privileges>");
//
//        for (int i=CAN_SUSPEND; i<=CAN_PILE; i++)
//           xml.append(buildXMLForPrivilege(i)) ;
//
//        xml.append("</privileges>");

        // output xml only if the element isn't empty 
        if (privileges.length() > 26)
            return privileges.toString();
        else
            return "" ;                        // returns "" because null gives "null"
    }


    public String toXML(boolean full) {
        XNode xml = new XNode("taskprivileges");
        if (full) {
            XNode specid = xml.addChild("specid");
            specid.addChild("identifier", _specID.getIdentifier());
            specid.addChild("uri", _specID.getUri());
            specid.addChild("version", _specID.getVersionAsString());
            xml.addChild("taskid", _taskID);
        }
        xml.addChild(buildPrivilegesNode());
        return xml.toString();
    }


    private XNode buildPrivilegesNode() {
        XNode privileges = new XNode("privileges");

        // get the set of participants and roles for each privilege
        for (int i=CAN_SUSPEND; i<=CAN_PILE; i++) {
            privileges.addChild(buildXMLForPrivilege(i));
        }
        return privileges;
    }
    

//    /** creates xml to describe a single privilege */
//    private String buildXMLForPrivilege(int priv) {
//
//        // if anyone has been granted access for this privilege
//        if (allAllowed(priv) || hasParticipants(priv) || hasRoles(priv)) {
//            StringBuilder xml = new StringBuilder("<privilege>");
//            xml.append("<name>").append(getPrivString(priv)).append("</name>");
//
//            if (allAllowed(priv))
//                xml.append("<allowall>").append(true).append("</allowall>");
//
//            // individual participants or roles go in the 'set' child
//            if (hasParticipants(priv) || hasRoles(priv)) {
//                xml.append("<set>");
//                HashSet<Participant> pSet = getPrivSet(priv) ;
//                for (Participant p : pSet) {
//                    xml.append("<participant>")
//                       .append(p.getID())
//                       .append("</participant>");
//                }
//
//                // roleSet stores ids only
//                if (hasRoles(priv)) {
//                    for (Iterator itr = _roleSet[priv].iterator() ; itr.hasNext();)
//                        xml.append("<role>").append(itr.next()).append("</role>");
//                }
//                xml.append("</set>");
//            }
//            xml.append("</privilege>");
//            return xml.toString();
//        }
//        else return "" ;
//    }

    /** creates xml to describe a single privilege */
    private XNode buildXMLForPrivilege(int priv) {

        // if anyone has been granted access for this privilege
        if (allAllowed(priv) || hasParticipants(priv) || hasRoles(priv)) {
            XNode privilege = new XNode("privilege");
            privilege.addChild("name", getPrivString(priv));
            if (allAllowed(priv)) {
                privilege.addChild("allowall", "true");
            }

            // individual participants or roles go in the 'set' child
            if (hasParticipants(priv) || hasRoles(priv)) {
                XNode set = privilege.addChild("set");
                HashSet<Participant> pSet = getPrivSet(priv) ;
                for (Participant p : pSet) {
                    set.addChild("participant", p.getID());
                }

                // roleSet stores ids only
                if (hasRoles(priv)) {
                    for (Object o : _roleSet[priv]) {
                        set.addChild("role", (String) o);
                    }
                }
            }
            return privilege;
        }
        else return null ;
    }

}


