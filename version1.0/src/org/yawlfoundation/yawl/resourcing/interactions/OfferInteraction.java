/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.interactions;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.WorkQueue;
import org.yawlfoundation.yawl.resourcing.constraints.AbstractConstraint;
import org.yawlfoundation.yawl.resourcing.constraints.ConstraintFactory;
import org.yawlfoundation.yawl.resourcing.filters.AbstractFilter;
import org.yawlfoundation.yawl.resourcing.filters.FilterFactory;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Role;

import java.util.*;

/**
 *  This class describes the requirements of a task at the offer phase of
 *  allocating resources.
 *
 *  @author Michael Adams
 *  v0.1, 02/08/2007
 */

public class OfferInteraction extends AbstractInteraction {

    // initial distribution set
    private HashSet<Participant> _participants = new HashSet<Participant>();
    private HashSet<Role> _roles  = new HashSet<Role>();
    private HashSet<DynParam> _dynParams  = new HashSet<DynParam>();

    private HashSet<AbstractFilter> _filters  = new HashSet<AbstractFilter>();
    private HashSet<AbstractConstraint> _constraints  = new HashSet<AbstractConstraint>();


    private String _familiarParticipantTask ;

    private ResourceManager _rm = ResourceManager.getInstance() ;
    private Logger _log = Logger.getLogger(this.getClass());


    // Dynamic Parameter types
    public static final int USER_PARAM = 0;
    public static final int ROLE_PARAM = 1;

    /********************************************************************************/

    // CONSTRUCTORS //

    public OfferInteraction() { super() ; }                  // required for reflection

    public OfferInteraction(String ownerTaskID) { super(ownerTaskID) ; }

    /**
     * @param initiator - either AbstractInteraction.SYSTEM_INITIATED or
     *                    AbstractInteraction.USER_INITIATED
     */
    public OfferInteraction(int initiator) {
        super(initiator) ;
    }

    /********************************************************************************/

    // MEMBER MODIFIERS //

    /**
     * Adds a participant to the initial distribution list
     * @param id - the id of the participant
     */
    public void addParticipant(String id) {
        Participant p = _rm.getParticipant(id);
        if (p != null)
            _participants.add(p);
        else
            _log.warn("Unknown Participant ID in Offer spec: " + id);
    }

    public void addParticipantUnchecked(String id) {
        Participant p = new Participant(id);
        _participants.add(p);
    }


    /**
     * variation of the above
     * @param p - the Participant object to add to the initial distribution list
     */
    public void addParticipant(Participant p) {
        if (_rm.isKnownParticipant(p))
           _participants.add(p);
        else
            _log.warn("Could not add unknown Participant to Offer: " + p.getID());
    }


    public void addParticipantsByID(String idList) {
        String[] ids = idList.split(",") ;
        for (String id : ids) addParticipant(id.trim());
    }


    public void addParticipantsByID(Set idSet) {
        for (Object id : idSet) addParticipant((String) id);
    }


    public void addParticipants(Set pSet) {
        for (Object id : pSet) addParticipant((Participant) id);
    }



    public void addRole(String rid) {
        Role r = _rm.getRole(rid);  
        if (r != null)
            _roles.add(r);
        else
            _log.warn("Unknown Role ID in Offer spec: " + rid);
    }

    public void addRoleUnchecked(String rid) {
        Role r =  new Role();
        r.setID(rid);
        _roles.add(r);
    }


    public void addRole(Role r) {
        if (_rm.isKnownRole(r))
            _roles.add(r) ;
        else
            _log.warn("Could not add unknown Role to Offer: " + r.getID());
    }


    public void addRoles(String roleList) {
        String[] roles = roleList.split(",") ;
        for (String role : roles) addRole(role.trim());
    }


    public void addRoles(Set rSet) {
        for (Object role : rSet) addRole((Role) role);
    }




    public boolean addInputParam(String name, int type) {
        if ((type == USER_PARAM) || (type == ROLE_PARAM)) {
            DynParam p = new DynParam(name, type);
            _dynParams.add(p);
            return true ;
        }
        else return false ;
    }

    public void addInputParams(Map pMap) {
        for (Object name : pMap.keySet()) {
            int type = Integer.parseInt((String)pMap.get(name)) ;
            addInputParam((String) name, type) ;
        }
    }

    public void addFilters(Set filters) {
        _filters.addAll(filters);
    }


    public void addFilter(AbstractFilter f) {
        _filters.add(f);
    }


    public void addConstraints(Set constraints) {
        _constraints.addAll(constraints);
    }


    public void addConstraint(AbstractConstraint c) {
        _constraints.add(c);
    }


    public void setFamiliarParticipantTask(String taskid) {
       _familiarParticipantTask = taskid ;
    }


    /********************************************************************************/

    /**
     * Takes the initial distribution set of participants, then expands any roles and/or
     * dynamic parameters to their 'set of participants' equivalents, then applies the
     * specified filters and/or constraints, and returns the final distribution set of
     * participants.
     *
     * @param  wir the workitem being offered
     * @return the final distribution set of Participant objects
     */
    public Set<Participant> performOffer(WorkItemRecord wir) {
        HashSet<Participant> distributionSet = new HashSet<Participant>();

        if (_familiarParticipantTask != null) {
            // get who completed that task, & offer this item to them
            // distributionSet.add(getWhoCompletedTask(_familiarParticipantTask)) ;
        }
        else {
            // make sure each participant is added only once
            ArrayList<String> uniqueIDs = new ArrayList<String>() ;

            // add Participants
            for (Participant p : _participants) {
                uniqueIDs.add(p.getID()) ;
                distributionSet.add(p) ;
            }

            // add roles
            for (Role role : _roles) {
                Set<Participant> pSet = role.getResources();
                for (Participant p : pSet) {
                    addParticipantToDistributionSet(distributionSet, uniqueIDs, p) ;
                }
            }

            // add dynamic params
            for (DynParam param : _dynParams) {
                Set<Participant> pSet = param.evaluate(wir);
                for (Participant p : pSet) {
                    addParticipantToDistributionSet(distributionSet, uniqueIDs, p) ;
                }
            }

            // apply each filter
            for (AbstractFilter filter : _filters)
                distributionSet =
                    (HashSet<Participant>) filter.performFilter(distributionSet) ;

            // apply each constraint
            for (AbstractConstraint constraint : _constraints)
                distributionSet =
                    (HashSet<Participant>) constraint.performConstraint(distributionSet, wir) ;

        }

        // ok - got our final set
        return distributionSet ;
    }


    public void withdrawOffer(WorkItemRecord wir, HashSet<Participant> offeredSet) {
        if (offeredSet != null) {
            for (Participant p : offeredSet) {
                p.getWorkQueues().removeFromQueue(wir, WorkQueue.OFFERED);
                _rm.announceModifiedQueue(p.getID()) ;
            }
        }
        else
            _log.warn("Workitem '" + wir.getID() + "' does not have 'Offered' status");
    }


    private void addParticipantToDistributionSet(HashSet<Participant> distributionSet,
                                                 ArrayList<String> uniqueIDs,
                                                 Participant p) {
        if (! uniqueIDs.contains(p.getID())) {
             uniqueIDs.add(p.getID()) ;
             distributionSet.add(p) ;
        }
    }

    /********************************************************************************/

    // Resource Specification Offer Parsing Methods //

    public void parse(Element e, Namespace nsYawl) throws ResourceParseException {

        parseInitiator(e, nsYawl);

        // if offer is not system-initiated, there's no more to do
        if (! isSystemInitiated()) return ;

        parseDistributionSet(e, nsYawl) ;
        parseFamiliarTask(e, nsYawl) ;
    }


    private void parseDistributionSet(Element e, Namespace nsYawl)
                                                       throws ResourceParseException {
        Element eDistSet = e.getChild("distributionSet", nsYawl);
        if (eDistSet != null) {
            parseInitialSet(eDistSet, nsYawl) ;
            parseFilters(eDistSet, nsYawl) ;
            parseConstraints(eDistSet, nsYawl) ;
        }
        else
            throw new ResourceParseException(
                    "Missing required element in Offer block: distributionSet") ;
    }


    private void parseInitialSet(Element e, Namespace nsYawl) throws ResourceParseException {

        Element eInitialSet = e.getChild("initialSet", nsYawl);
        if (eInitialSet != null) {
            parseParticipants(eInitialSet, nsYawl);
            parseRoles(eInitialSet, nsYawl);
            parseDynParams(eInitialSet, nsYawl);
        }
        else throw new ResourceParseException(
            "Missing required distributionSet child element in Offer block: initialSet") ;
    }


    private void parseParticipants(Element e, Namespace nsYawl) {

        // from the specified initial set, add all participants
        List participants = e.getChildren("participant", nsYawl);
        Iterator itr = participants.iterator();
        while (itr.hasNext()) {
            Element eParticipant = (Element) itr.next();
            String participant = eParticipant.getText();
            if (participant.indexOf(',') > -1)
                addParticipantsByID(participant);
            else
                addParticipant(participant);
        }
    }


    private void parseRoles(Element e, Namespace nsYawl) {

        // ... and roles
        List roles = e.getChildren("role", nsYawl);
        Iterator itr = roles.iterator();
        while (itr.hasNext()) {
            Element eRole = (Element) itr.next();
            String role = eRole.getText();
            if (role.indexOf(',') > -1)
                addRoles(role);
            else
                addRole(role);
        }
    }


    private void parseDynParams(Element e, Namespace nsYawl) {

        // ... and input parameters
        List params = e.getChildren("param", nsYawl);
        Iterator itr = params.iterator();
        while (itr.hasNext()) {
            Element eParam = (Element) itr.next();
            String name = eParam.getChildText("name", nsYawl);
            String refers = eParam.getChildText("refers", nsYawl);
            int pType = refers.equals("role") ? ROLE_PARAM : USER_PARAM;
            addInputParam(name, pType);
        }
    }


    private void parseFilters(Element e, Namespace nsYawl) throws ResourceParseException {

        // get the Filters
        Element eFilters = e.getChild("filters", nsYawl);
        if (eFilters != null) {
            List filters = eFilters.getChildren("filter", nsYawl);
            if (filters == null)
                throw new ResourceParseException(
                        "No filter elements found in filters element");

            Iterator itr = filters.iterator();
            while (itr.hasNext()) {
                Element eFilter = (Element) itr.next();
                String filterClassName = eFilter.getChildText("name", nsYawl);
                if (filterClassName != null) {
                    AbstractFilter filter = FilterFactory.getInstance(filterClassName);
                    if (filter != null) {
                        filter.setParams(parseParams(eFilter, nsYawl));
                        _filters.add(filter);
                    }
                    else throw new ResourceParseException("Unknown filter name: " +
                                                                   filterClassName);
                }
                else throw new ResourceParseException("Missing filter element: name");
            }
        }
    }

    private void parseConstraints(Element e, Namespace nsYawl)
                                                        throws ResourceParseException {
        // get the Constraints
        Element eConstraints = e.getChild("constraints", nsYawl);
        if (eConstraints != null) {
            List constraints = eConstraints.getChildren("constraint", nsYawl);
            if (constraints == null)
                throw new ResourceParseException(
                        "No constraint elements found in constraints element");

            Iterator itr = constraints.iterator();
            while (itr.hasNext()) {
                Element eConstraint = (Element) itr.next();
                String constraintClassName = eConstraint.getChildText("name", nsYawl);
                if (constraintClassName != null) {
                    AbstractConstraint constraint =
                            ConstraintFactory.getInstance(constraintClassName);
                    if (constraint != null) {
                        constraint.setParams(parseParams(eConstraint, nsYawl));
                        _constraints.add(constraint);
                    }
                    else throw new ResourceParseException("Unknown constraint name: " +
                                                                   constraintClassName);
                }
                else throw new ResourceParseException("Missing constraint element: name");
            }
        }
    }


    private void parseFamiliarTask(Element e, Namespace nsYawl) {

        // finally, get the familiar participant task
        Element eFamTask = e.getChild("familiarParticipant", nsYawl);
        if (eFamTask != null)
            _familiarParticipantTask = eFamTask.getAttributeValue("taskID");

    }

    /********************************************************************************/
    
    public String toXML() {
        Iterator itr ;
        StringBuilder xml = new StringBuilder("<offer ");

        xml.append("initiator=\"").append(getInitiatorString()).append("\">");

        // the rest of the xml is only needed if it's system intiated
        if (isSystemInitiated()) {
            xml.append("<distributionSet>") ;
            xml.append("<initialSet>");

            if (_participants != null) {
                itr = _participants.iterator();
                while (itr.hasNext()) {
                    Participant p = (Participant) itr.next();
                    xml.append("<participant>").append(p.getID()).append("</participant>");
                }
            }
            if (_roles != null) {
                itr = _roles.iterator() ;
                while (itr.hasNext()) {
                    Role r = (Role) itr.next();
                    xml.append("<role>").append(r.getID()).append("</role>");
                }
            }
            if (_dynParams != null) {
                itr = _dynParams.iterator() ;
                while (itr.hasNext()) {
                    DynParam p = (DynParam) itr.next();
                    xml.append(p.toXML());
                }
            }

            xml.append("</initialSet>");

            if ((_filters != null) && (! _filters.isEmpty())) {
                xml.append("<filters>") ;
                itr = _filters.iterator() ;
                while (itr.hasNext()) {
                    AbstractFilter filter = (AbstractFilter) itr.next();
                    xml.append(filter.toXML());
                }
                xml.append("</filters>") ;
            }

            if ((_constraints != null) && (! _constraints.isEmpty())) {
                xml.append("<constraints>") ;
                itr = _constraints.iterator() ;
                while (itr.hasNext()) {
                    AbstractConstraint constraint = (AbstractConstraint) itr.next();
                    xml.append(constraint.toXML());
                }
                xml.append("</constraints>") ;
            }

            xml.append("</distributionSet>") ;

            if (_familiarParticipantTask != null) {
                xml.append("<familiarParticipant taskID=\"");
                xml.append(_familiarParticipantTask).append("\"/>");
            }
        }
        
        xml.append("</offer>");
        return xml.toString();
    }

    /*******************************************************************************/
    /*******************************************************************************/


    /**
     * A class that encapsulates one dynamic parameter - i.e. a data variable that at
     * runtime will contain a value corresponding to a participant or role that the
     * task is to be offered to.
     */
    private class DynParam {

        private String _name ;              // the name of the data variable
        private int _refers ;               // participant or role

        /** the constructor
         *
         * @param name - the name of a data variable of this task that willcontain
         *               a runtime value specifying a particular participant or role.
         * @param refers - either USER_PARAM or ROLE_PARAM
         */
        public DynParam(String name, int refers) {
            _name = name ;
            _refers = refers ;
        }

    /*******************************************************************************/

        // GETTERS & SETTERS //

        public String getName() { return _name ; }

        public int getRefers() { return _refers ; }

        public void setName(String name) { _name = name; }

        public void setRefers(int refers) { _refers = refers; }

        public String getRefersString() {
            if (_refers == USER_PARAM) return "participant" ;
            else return "role" ;
        }


        public Set<Participant> evaluate(WorkItemRecord wir) {
            HashSet<Participant> result = new HashSet<Participant>() ;
            String varID = wir.getWorkItemData().getChildText(_name) ;

            if (_refers == USER_PARAM) {
                Participant p = _rm.getParticipant(varID) ;
                if (p != null)
                    result.add(p) ;
                else
                    _log.error("Unknown participant ID in dynamic parameter: " + _name );
            }
            else {
                Set<Participant> rpSet = _rm.getRoleParticipants(varID) ;
                if (rpSet != null)
                    result.addAll(rpSet) ;
                else
                    _log.error("Unknown role ID in dynamic parameter: " + _name );
            }
            if (result.isEmpty()) result = null ;
            return result ;
        }

    /*******************************************************************************/

        /** this is for the spec file */
        public String toXML() {
            StringBuilder xml = new StringBuilder("<param>");
            xml.append("<name>").append(_name).append("</name>");
            xml.append("<refers>").append(getRefersString()).append("</refers>");
            xml.append("</param>");
            return xml.toString();
        }
    }

    /*******************************************************************************/


}
