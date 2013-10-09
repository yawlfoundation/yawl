/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.core.resourcing;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.editor.core.resourcing.entity.*;
import org.yawlfoundation.yawl.editor.core.resourcing.validation.InvalidReference;
import org.yawlfoundation.yawl.elements.YAtomicTask;
import org.yawlfoundation.yawl.resourcing.constraints.AbstractConstraint;
import org.yawlfoundation.yawl.resourcing.filters.AbstractFilter;
import org.yawlfoundation.yawl.resourcing.interactions.AbstractInteraction;
import org.yawlfoundation.yawl.resourcing.interactions.ResourceParseException;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Role;
import org.yawlfoundation.yawl.util.XNode;

import java.util.HashSet;
import java.util.Set;

/**
 * A reduced version of yawl's OfferInteraction class - we just need the storage parts,
 * no runtime required
 *
 * @author Michael Adams
 * @date 14/06/12
 */
public class BasicOfferInteraction extends AbstractInteraction {

    // distribution set
    private ParticipantSet _participants;
    private RoleSet _roles;
    private DynParamSet _dynParams;

    private FilterSet _filters;
    private ConstraintSet _constraints;
    private String _familiarParticipantTask ;



    protected BasicOfferInteraction(YAtomicTask task) {
        super(task.getID());
        _participants = new ParticipantSet();
        _roles = new RoleSet();
        _dynParams = new DynParamSet();
        _filters = new FilterSet();
        _constraints = new ConstraintSet();
    }


    public ParticipantSet getParticipantSet() { return _participants; }

    public RoleSet getRoleSet() { return _roles; }

    public DynParamSet getDynParamSet() { return _dynParams; }

    public FilterSet getFilterSet() { return _filters; }

    public ConstraintSet getConstraintSet() { return _constraints; }



    public void setFamiliarParticipantTask(String taskid) {
       _familiarParticipantTask = taskid ;
    }

    public String getFamiliarParticipantTask() {
        return _familiarParticipantTask;
    }

    public void clearFamiliarParticipantTask() {
        _familiarParticipantTask = null;
     }


    protected Set<InvalidReference> getInvalidReferences() {
        Set<InvalidReference> invalidReferences = new HashSet<InvalidReference>();
        invalidReferences.addAll(_participants.getInvalidReferences());
        invalidReferences.addAll(_roles.getInvalidReferences());
        invalidReferences.addAll(_dynParams.getInvalidReferences());
        invalidReferences.addAll(_filters.getInvalidReferences());
        invalidReferences.addAll(_constraints.getInvalidReferences());
        return invalidReferences;
    }



    protected void parse(Element e, Namespace nsYawl) throws ResourceParseException {

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
            _filters.parse(eDistSet, nsYawl) ;
            _constraints.parse(eDistSet, nsYawl) ;
        }
        else
            throw new ResourceParseException(
                    "Missing required element in Offer block: distributionSet") ;
    }


    private void parseInitialSet(Element e, Namespace nsYawl) throws ResourceParseException {

        Element eInitialSet = e.getChild("initialSet", nsYawl);
        if (eInitialSet != null) {
            _participants.parse(eInitialSet, nsYawl);
            _roles.parse(eInitialSet, nsYawl);
            _dynParams.parse(eInitialSet, nsYawl);
        }
        else throw new ResourceParseException(
            "Missing required distributionSet child element in Offer block: initialSet") ;
    }


    private void parseFamiliarTask(Element e, Namespace nsYawl) {

        // finally, get the familiar participant task
        Element eFamTask = e.getChild("familiarParticipant", nsYawl);
        if (eFamTask != null)
            _familiarParticipantTask = eFamTask.getAttributeValue("taskID");

    }


    /********************************************************************************/

    public String toXML() {
        XNode offerNode = new XNode("offer");
        offerNode.addAttribute("initiator", getInitiatorString());

        // the rest of the xml is only needed if it's system initiated
        if (isSystemInitiated()) {
            XNode distributionSetNode = offerNode.addChild("distributionSet");

            XNode initialSetNode = distributionSetNode.addChild("initialSet");
            for (Participant p : _participants.getAll()) {
                initialSetNode.addChild("participant", p.getID());
            }
            for (Role r : _roles.getAll()) {
                initialSetNode.addChild("role", r.getID());
            }
            for (DynParam p : _dynParams.getAll()) {
                initialSetNode.addContent(p.toXML());
            }

            if (! _filters.isEmpty()) {
                XNode filtersNode = distributionSetNode.addChild("filters");
                for (AbstractFilter filter : _filters.getAll()) {
                    filtersNode.addContent(filter.toXML());
                }
            }
            if (! _constraints.isEmpty()) {
                XNode constraintsNode = distributionSetNode.addChild("constraints");
                for (AbstractConstraint constraint : _constraints.getAll()) {
                    constraintsNode.addContent(constraint.toXML());
                }
            }

            if (_familiarParticipantTask != null) {
               XNode famTaskNode = offerNode.addChild("familiarParticipant");
               famTaskNode.addAttribute("taskID", _familiarParticipantTask);
            }
        }

        return offerNode.toString();
    }


}
