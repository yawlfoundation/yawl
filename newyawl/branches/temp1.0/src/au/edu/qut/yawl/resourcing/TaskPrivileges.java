/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing;

import au.edu.qut.yawl.resourcing.resource.Participant;
import au.edu.qut.yawl.resourcing.resource.Role;

import java.util.*;

import org.jdom.Element;

/**
 * Maps each of a set of User-Task Privileges to a set of Participants (if any) for
 * one particular task. The mappings are listed in the specification file, and may map
 * a privilege to individual participants and/or all the participants that fulfil a
 * certain Role or Roles.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.1, 13/08/2007
 */



public class TaskPrivileges {

    // user-task privileges
    private HashSet<Participant> _canSuspend = new HashSet<Participant>();
    private HashSet<Participant> _canReallocateStateless = new HashSet<Participant>();
    private HashSet<Participant> _canReallocateStateful = new HashSet<Participant>();
    private HashSet<Participant> _canDeallocate = new HashSet<Participant>() ;
    private HashSet<Participant> _canDelegate = new HashSet<Participant>() ;
    private HashSet<Participant> _canSkip = new HashSet<Participant>();
    private HashSet<Participant> _canPile = new HashSet<Participant>() ;

    private static final int CAN_SUSPEND = 0 ;
    private static final int CAN_REALLOCATE_STATELESS = 1 ;
    private static final int CAN_REALLOCATE_STATEFUL = 2 ;
    private static final int CAN_DEALLOCATE = 3 ;
    private static final int CAN_DELEGATE = 4 ;
    private static final int CAN_SKIP = 5 ;
    private static final int CAN_PILE = 6 ;

    
    public TaskPrivileges() {}


    private boolean contains(HashSet<Participant> set, Participant p) {
        return ((set != null) && (p != null) && (set.contains(p)));
    }


    private void add(HashSet<Participant> set, Participant p) {
        if (p != null) set.add(p) ;
    }


    private void add(HashSet<Participant> set, String uid) {
        Participant p = new Participant(uid) ;           // todo: change when db inf up
        add(set, p) ;
    }


    private void addSet(HashSet<Participant> set, Set<Participant> participants) {
        if (participants != null) set.addAll(participants) ;
    }


    private boolean validPrivilege(int priv) {
        return ((priv >= CAN_SUSPEND) && (priv <= CAN_PILE));
    }


    /** convenience methods */

    public boolean canSuspend(Participant p) {
        return contains(_canSuspend, p) ;
    }

    public boolean canReallocateStateless(Participant p) {
        return contains(_canReallocateStateless, p) ;
    }

    public boolean canReallocateStateful(Participant p) {
        return contains(_canReallocateStateful, p) ;
    }

    public boolean canDeallocate(Participant p) {
        return contains(_canDeallocate, p) ;
    }

    public boolean canDelegate(Participant p) {
        return contains(_canDelegate, p) ;
    }

    public boolean canSkip(Participant p) {
        return contains(_canSkip, p) ;
    }

    public boolean canPile(Participant p) {
        return contains(_canPile, p) ;
    }


    public void add(int priv, String id) {
        if (validPrivilege(priv)) {
            switch (priv) {
                case CAN_SUSPEND : add(_canSuspend, id); break;
                case CAN_REALLOCATE_STATELESS : add(_canReallocateStateless, id); break;
                case CAN_REALLOCATE_STATEFUL : add(_canReallocateStateful, id); break;
                case CAN_DEALLOCATE : add(_canDeallocate, id); break;
                case CAN_DELEGATE : add(_canDelegate, id); break;
                case CAN_SKIP : add(_canSkip, id); break;
                case CAN_PILE : add(_canPile, id);
            }
        }
    }

    public void add(int priv, Participant p) {
        if (validPrivilege(priv)) {
            switch (priv) {
                case CAN_SUSPEND : add(_canSuspend,p); break;
                case CAN_REALLOCATE_STATELESS : add(_canReallocateStateless, p); break;
                case CAN_REALLOCATE_STATEFUL : add(_canReallocateStateful, p); break;
                case CAN_DEALLOCATE : add(_canDeallocate, p); break;
                case CAN_DELEGATE : add(_canDelegate, p); break;
                case CAN_SKIP : add(_canSkip, p); break;
                case CAN_PILE : add(_canPile, p);
            }
        }
    }

    public void add(int priv, Set<Participant> pSet) {
        if (validPrivilege(priv)) {
            switch (priv) {
                case CAN_SUSPEND : addSet(_canSuspend, pSet); break;
                case CAN_REALLOCATE_STATELESS : addSet(_canReallocateStateless, pSet); break;
                case CAN_REALLOCATE_STATEFUL : addSet(_canReallocateStateful, pSet); break;
                case CAN_DEALLOCATE : addSet(_canDeallocate, pSet); break;
                case CAN_DELEGATE : addSet(_canDelegate, pSet); break;
                case CAN_SKIP : addSet(_canSkip, pSet); break;
                case CAN_PILE : addSet(_canPile, pSet);
            }
        }
    }

    public void addByID(int priv, Set pSet) {
        if (validPrivilege(priv)) {
            switch (priv) {
                case CAN_SUSPEND : addSuspendByID(pSet); break;
                case CAN_REALLOCATE_STATELESS : addReallocateStatelessByID(pSet); break;
                case CAN_REALLOCATE_STATEFUL : addReallocateStatefulByID(pSet); break;
                case CAN_DEALLOCATE : addDeallocateByID(pSet); break;
                case CAN_DELEGATE : addDelegateByID(pSet); break;
                case CAN_SKIP : addSkipByID(pSet); break;
                case CAN_PILE : addPileByID(pSet);
            }
        }
    }



    public void addSuspend(Participant p) {
        add(_canSuspend, p) ;
    }

    public void addReallocateStateless(Participant p) {
        add(_canReallocateStateless, p) ;
    }

    public void addReallocateStateful(Participant p) {
        add(_canReallocateStateful, p) ;
    }

    public void addDeallocate(Participant p) {
        add(_canDeallocate, p) ;
    }

    public void addDelegate(Participant p) {
        add(_canDelegate, p) ;
    }

    public void addSkip(Participant p) {
        add(_canSkip, p) ;
    }

    public void addPile(Participant p) {
        add(_canPile, p) ;
    }


    
    public void addSuspend(Set<Participant> p) {
        addSet(_canSuspend, p) ;
    }

    public void addReallocateStateless(Set<Participant> p) {
        addSet(_canReallocateStateless, p) ;
    }

    public void addReallocateStateful(Set<Participant> p) {
        addSet(_canReallocateStateful, p) ;
    }

    public void addDeallocate(Set<Participant> p) {
        addSet(_canDeallocate, p) ;
    }

    public void addDelegate(Set<Participant> p) {
        addSet(_canDelegate, p) ;
    }

    public void addSkip(Set<Participant> p) {
        addSet(_canSkip, p) ;
    }

    public void addPile(Set<Participant> p) {
        addSet(_canPile, p) ;
    }

    public void addSuspendByID(String userid) {
        add(_canSuspend, userid) ;
    }

    public void addReallocateStatelessByID(String userid) {
        add(_canReallocateStateless, userid) ;
    }

    public void addReallocateStatefulByID(String userid) {
        add(_canReallocateStateful, userid) ;
    }

    public void addDeallocateByID(String userid) {
        add(_canDeallocate, userid) ;
    }

    public void addDelegateByID(String userid) {
        add(_canDelegate, userid) ;
    }

    public void addSkipByID(String userid) {
        add(_canSkip, userid) ;
    }

    public void addPileByID(String userid) {
        add(_canPile, userid) ;
    }


    public void addSuspendByID(Set userids) {
        for (Object id : userids)
            add(_canSuspend, (String) id) ;
    }

    public void addReallocateStatelessByID(Set userids) {
        for (Object id : userids)
            add(_canReallocateStateless, (String) id) ;
    }

    public void addReallocateStatefulByID(Set userids) {
        for (Object id : userids)
            add(_canReallocateStateful, (String) id) ;
    }

    public void addDeallocateByID(Set userids) {
        for (Object id : userids)
            add(_canDeallocate, (String) id) ;
    }

    public void addDelegateByID(Set userids) {
        for (Object id : userids)
            add(_canDelegate, (String) id) ;
    }

    public void addSkipByID(Set userids) {
        for (Object id : userids)
            add(_canSkip, (String) id) ;
    }

    public void addPileByID(Set userids) {
        for (Object id : userids)
            add(_canPile, (String) id) ;
    }


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

    private HashSet<Participant> getPrivSet(String privStr) {
        return getPrivSet(getPriv(privStr)) ;
    }

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

    private int getPriv(String privStr) {
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


    /** Parses the xml element passed into a set of privileges and who has them */
    public void parse(Element e) {
        if (e != null) {
            List ePrivileges = e.getChildren("privilege");
            Iterator itr = ePrivileges.iterator() ;
            while (itr.hasNext()) {
                Element ePrivilege = (Element) itr.next();

                // get the privilege set we're referring to
                HashSet<Participant> privSet = getPrivSet(ePrivilege.getChildText("name")) ;

                // get the set of participant and/or role tags for this privilege
                List eSet = ePrivilege.getChildren();
                Iterator sitr = eSet.iterator() ;
                while (sitr.hasNext()) {
                    Element eResource = (Element) sitr.next();

                    // if it's a 'role' child, translate to a set of participants
                    if (eResource.getName().equals("role")) {
                        Set<Participant> pSet = ResourceManager.getInstance()
                                              .getRoleParticipants(eResource.getText()) ;
                        addSet(privSet, pSet);
                    }

                    //
                    else add(privSet, eResource.getText());
                }
            }
        }        
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder("<privileges>");

        for (int i=CAN_SUSPEND; i<=CAN_PILE; i++)
           xml.append(buildXMLForPrivilege(getPrivSet(i), getPrivString(i))) ;

        xml.append("</privileges>");
        return xml.toString();
    }

    
    private String buildXMLForPrivilege(HashSet<Participant> privilege, String name) {
        if (! privilege.isEmpty()) {
            StringBuilder xml = new StringBuilder("<privilege>");
            xml.append("<name>").append(name).append("</name>");
            xml.append("<set>");
            for (Participant p : privilege) {
                    xml.append("<participant>") ;
                    xml.append(p.getID());
                    xml.append("</participant>");
            }
            xml.append("</set>");
            xml.append("</privilege>");
            return xml.toString();
        }
        else return "" ;
    }

}


