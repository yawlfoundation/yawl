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

package org.yawlfoundation.yawl.resourcing.interactions;

import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.WorkQueue;
import org.yawlfoundation.yawl.resourcing.constraints.AbstractConstraint;
import org.yawlfoundation.yawl.resourcing.filters.AbstractFilter;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Role;
import org.yawlfoundation.yawl.resourcing.util.PluginFactory;

import java.io.IOException;
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

    // complete distribution set expanded to a set of participants
    private HashSet<Participant> _distributionSet = new HashSet<Participant>();

    private HashSet<AbstractFilter> _filters  = new HashSet<AbstractFilter>();
    private HashSet<AbstractConstraint> _constraints  = new HashSet<AbstractConstraint>();


    private String _familiarParticipantTask ;

    private ResourceManager _rm = ResourceManager.getInstance() ;
    private static final Logger _log = Logger.getLogger(OfferInteraction.class);


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
        Participant p = _rm.getOrgDataSet().getParticipant(id);
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
        if (_rm.getOrgDataSet().isKnownParticipant(p))
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
        Role r = _rm.getOrgDataSet().getRole(rid);  
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
        if (_rm.getOrgDataSet().isKnownRole(r))
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


    public Set<Participant> getParticipants() { return _participants; }

    public Set<Role> getRoles() { return _roles; }

    public Set<AbstractFilter> getFilters() { return _filters; }

    public Set<AbstractConstraint> getConstraints() { return _constraints; }

    public Set<Participant> getDistributionSet() { return _distributionSet; }


    public Set<String> getDynParamNames() {
        Set<String> names = new HashSet<String>();
        for (DynParam param : _dynParams) {
            names.add(param.getName() + "[" + param.getRefersString() + "]");
        }
        return names;
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
        _distributionSet = new HashSet<Participant>();

        // if familiar task specified, get the participant(s) who completed that task,
        // & offer this item to them - no more to do
        if (_familiarParticipantTask != null) {
            Set<Participant> pSet = _rm.getWhoCompletedTask(_familiarParticipantTask, wir);
            if (pSet != null) _distributionSet.addAll(pSet) ;
        }
        else {
            // make sure each participant is added only once
            ArrayList<String> uniqueIDs = new ArrayList<String>() ;

            // add Participants
            for (Participant p : _participants) {
                uniqueIDs.add(p.getID()) ;
                _distributionSet.add(p) ;
            }

            // add roles
            for (Role role : _roles) {
                Set<Participant> pSet = _rm.getOrgDataSet().castToParticipantSet(role.getResources());
                pSet.addAll(_rm.getOrgDataSet().getParticipantsInDescendantRoles(role));
                for (Participant p : pSet) {
                    addParticipantToDistributionSet(_distributionSet, uniqueIDs, p) ;
                }
            }

            // add dynamic params
            for (DynParam param : _dynParams) {
                Set<Participant> pSet = param.evaluate(wir);
                for (Participant p : pSet) {
                    addParticipantToDistributionSet(_distributionSet, uniqueIDs, p) ;
                }
            }

            // apply each filter
            for (AbstractFilter filter : _filters)
                _distributionSet =
                    (HashSet<Participant>) filter.performFilter(_distributionSet) ;

            // apply each constraint
            for (AbstractConstraint constraint : _constraints)
                _distributionSet =
                    (HashSet<Participant>) constraint.performConstraint(_distributionSet, wir) ;

        }

        // ok - got our final set
        return _distributionSet ;
    }


    public void withdrawOffer(WorkItemRecord wir, HashSet<Participant> offeredSet) {
        if (offeredSet != null) {
            for (Participant p : offeredSet) {
                p.getWorkQueues().removeFromQueue(wir, WorkQueue.OFFERED);
                _rm.announceModifiedQueue(p.getID()) ;
            }
        }

        // a fired instance of a multi-instance workitem on the unoffered queue will
        // never have been offered, so the warning should be suppressed for those
        else if (! wir.getStatus().equals(WorkItemRecord.statusFired)) {
            _log.warn("Workitem '" + wir.getID() + "' does not have 'Offered' status, " +
                      "or is no longer active");
        }
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
        for (Element eParticipant : e.getChildren("participant", nsYawl)) {
            String participant = eParticipant.getText();
            if (participant.indexOf(',') > -1)
                addParticipantsByID(participant);
            else
                addParticipant(participant);
        }
    }


    private void parseRoles(Element e, Namespace nsYawl) {

        // ... and roles
        for (Element eRole : e.getChildren("role", nsYawl)) {
            String role = eRole.getText();
            if (role.indexOf(',') > -1)
                addRoles(role);
            else
                addRole(role);
        }
    }


    private void parseDynParams(Element e, Namespace nsYawl) {

        // ... and input parameters
        for (Element eParam : e.getChildren("param", nsYawl)) {
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
            List<Element> filters = eFilters.getChildren("filter", nsYawl);
            if (filters == null)
                throw new ResourceParseException(
                        "No filter elements found in filters element");

            for (Element eFilter : filters) {
                String filterClassName = eFilter.getChildText("name", nsYawl);
                if (filterClassName != null) {
                    AbstractFilter filter = PluginFactory.newFilterInstance(filterClassName);
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
            List<Element> constraints = eConstraints.getChildren("constraint", nsYawl);
            if (constraints == null)
                throw new ResourceParseException(
                        "No constraint elements found in constraints element");

            for (Element eConstraint : constraints) {
                String constraintClassName = eConstraint.getChildText("name", nsYawl);
                if (constraintClassName != null) {
                    AbstractConstraint constraint =
                            PluginFactory.newConstraintInstance(constraintClassName);
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
        StringBuilder xml = new StringBuilder("<offer ");

        xml.append("initiator=\"").append(getInitiatorString()).append("\">");

        // the rest of the xml is only needed if it's system initiated
        if (isSystemInitiated()) {
            xml.append("<distributionSet>") ;
            xml.append("<initialSet>");

            if (_participants != null) {
                for (Participant p : _participants) {
                    xml.append("<participant>").append(p.getID()).append("</participant>");
                }
            }
            if (_roles != null) {
                for (Role r : _roles) {
                    xml.append("<role>").append(r.getID()).append("</role>");
                }
            }
            if (_dynParams != null) {
                for (DynParam p : _dynParams) {
                    xml.append(p.toXML());
                }
            }

            xml.append("</initialSet>");

            if ((_filters != null) && (! _filters.isEmpty())) {
                xml.append("<filters>") ;
                for (AbstractFilter filter : _filters) {
                    xml.append(filter.toXML());
                }
                xml.append("</filters>") ;
            }

            if ((_constraints != null) && (! _constraints.isEmpty())) {
                xml.append("<constraints>") ;
                for (AbstractConstraint constraint : _constraints) {
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
         * @param name - the name of a data variable of this task that will contain
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
            HashSet<Participant> result = new HashSet<Participant>();
            if (_refers == USER_PARAM) {
                for (String varID : getVarIDList(wir)) {
                    Participant p = _rm.getParticipantFromUserID(varID);
                    if (p != null)
                        result.add(p) ;
                    else
                        _log.error("Unknown participant userID '" + varID +
                                "' in dynamic parameter: " + _name );
                }
            }
            else {
                for (String varID : getVarIDList(wir)) {
                    Role r = _rm.getOrgDataSet().getRoleByName(varID) ;
                    if (r != null) {
                        Set<Participant> rpSet = _rm.getOrgDataSet().getRoleParticipants(r.getID()) ;
                        if (rpSet != null) result.addAll(rpSet) ;
                    }
                    else
                        _log.error("Unknown role '" + varID +
                                "' in dynamic parameter: " + _name );
                }
            }
            return result ;
        }


        private String getNetParamValue (WorkItemRecord wir, String name) {
            String result = null ;
            try {
                result = _rm.getNetParamValue(wir.getCaseID(), _name);
                if (result == null)
                    _log.error("Unable to retrieve value from net parameter '" +
                               name + "' for deferred allocation of workitem '" +
                               wir.getID() + "'.");
            }
            catch (IOException ioe) {
                _log.error("Caught exception attempting to retrieve value from net parameter '" +
                           name + "' for deferred allocation of workitem '" +
                           wir.getID() + "'.");                
            }
            return result;
        }


        private List<String> getVarIDList(WorkItemRecord wir) {
            List<String> idList = new ArrayList<String>();
            String varValue = getNetParamValue(wir, _name);
            if (varValue != null) {
                for (String id : varValue.split(",")) {
                     idList.add(id.trim());
                }
            }
            return idList;
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

    }  // end of private class DynParam

    /*******************************************************************************/

}
