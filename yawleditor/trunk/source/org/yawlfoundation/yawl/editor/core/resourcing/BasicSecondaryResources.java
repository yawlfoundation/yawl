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
import org.yawlfoundation.yawl.resourcing.interactions.ResourceParseException;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Role;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;
import org.yawlfoundation.yawl.util.XNode;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 14/06/12
 */
public class BasicSecondaryResources {

    private YAtomicTask _task;
    private ParticipantSet participants;                // unique resource
    private NonHumanResourceSet nonHumanResources;      // unique resource
    private RoleSet roles;                             // allows duplicates
    private NonHumanCategorySet nonHumanCategories;

    public BasicSecondaryResources(YAtomicTask task) {
        _task = task;
        participants = new ParticipantSet();
        roles = new RoleSet(EntityCollection.ALLOW_DUPLICATES);
        nonHumanResources = new NonHumanResourceSet();
        nonHumanCategories = new NonHumanCategorySet(EntityCollection.ALLOW_DUPLICATES);
    }


    public ParticipantSet getParticipantSet() { return participants; }

    public RoleSet getRoleSet() { return roles; }

    public NonHumanResourceSet getNonHumanResourceSet() { return nonHumanResources; }

    public NonHumanCategorySet getNonHumanCategorySet() { return nonHumanCategories; }


    public Set<InvalidReference> getInvalidReferences() {
        Set<InvalidReference> references = new HashSet<InvalidReference>();
        references.addAll(participants.getInvalidReferences());
        references.addAll(roles.getInvalidReferences());
        references.addAll(nonHumanResources.getInvalidReferences());
        references.addAll(nonHumanCategories.getInvalidReferences());
        return references;
    }


    public boolean hasResources() {
        return getResourcesCount() > 0;
    }


    public int getResourcesCount() {
        return participants.size() + roles.size() +
                nonHumanResources.size() + nonHumanCategories.size();
    }


    public void parse(Element e, Namespace nsYawl) throws ResourceParseException {
        participants.parse(e, nsYawl);
        roles.parse(e, nsYawl);
        nonHumanResources.parse(e, nsYawl);
        nonHumanCategories.parse(e, nsYawl);
    }


    protected String toXML() {
        if (! hasResources()) return "";

        XNode node = new XNode("secondary");
        for (Participant p : participants.getAll()) {
            node.addChild("participant", p.getID());
        }
        for (Role r : roles.getAll()) {
            node.addChild("role", r.getID());
        }
        for (NonHumanResource r : nonHumanResources.getAll()) {
            node.addChild("nonHumanResource", r.getID());
        }
        for (GenericNonHumanCategory c : nonHumanCategories.getAll()) {
            XNode child = node.addChild("nonHumanCategory", c.getID());
            if (c.getSubcategory() != null) {
                child.addAttribute("subcategory", c.getSubcategory());
            }
        }
        return node.toPrettyString();
    }

}
