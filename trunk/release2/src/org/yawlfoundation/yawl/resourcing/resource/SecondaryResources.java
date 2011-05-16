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

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.EventLogger;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.LogMiner;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.ResourceEvent;
import org.yawlfoundation.yawl.resourcing.interactions.ResourceParseException;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanCategory;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;
import org.yawlfoundation.yawl.util.XNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages a set of secondary resources for a single task (via a ResourceMap)
 *
 * @author Michael Adams
 * @date 11/05/2011
 */
public class SecondaryResources {

    private final Set<Participant> _participants;
    private final Set<Role> _roles;
    private final Set<NonHumanResource> _nonHumanResources;
    private final Set<NonHumanCategory> _nonHumanCategories;
    private final ResourceManager _rm;
    private final Logger _log;

    public SecondaryResources() {
        _participants = new HashSet<Participant>();
        _roles = new HashSet<Role>();
        _nonHumanResources = new HashSet<NonHumanResource>();
        _nonHumanCategories = new HashSet<NonHumanCategory>();
        _rm = ResourceManager.getInstance();
        _log = Logger.getLogger(this.getClass());
    }



    public void addParticipant(String id) {
        Participant p = _rm.getOrgDataSet().getParticipant(id);
        if (p != null) _participants.add(p);
        else _log.warn("Unknown Participant ID in secondary resources spec: " + id);
    }


    public void addParticipant(Participant p) {
        if (_rm.getOrgDataSet().isKnownParticipant(p))
           _participants.add(p);
        else
            _log.warn("Could not add unknown Participant to secondary resources: " +
                    p.getID());
    }


    public void addParticipantUnchecked(String id) {
        _participants.add(new Participant(id));
    }



    public void addRole(String id) {
        Role role = _rm.getOrgDataSet().getRole(id);
        if (role != null) _roles.add(role);
        else _log.warn("Unknown Role ID in secondary resources spec: " + id);
    }


    public void addRole(Role role) {
        if (_rm.getOrgDataSet().isKnownRole(role))
           _roles.add(role);
        else
            _log.warn("Could not add unknown Role to secondary resources: " +
                    role.getID());
    }


    public void addRoleUnchecked(String id) {
        Role role = new Role();
        role.setID(id);
        _roles.add(role);
    }



    public void addNonHumanResource(String id) {
        NonHumanResource resource = _rm.getOrgDataSet().getNonHumanResource(id);
        if (resource != null) _nonHumanResources.add(resource);
        else _log.warn("Unknown nonhuman resource ID in secondary resources spec: " + id);
    }


    public void addNonHumanResource(NonHumanResource resource) {
        if (_rm.getOrgDataSet().isKnownNonHumanResource(resource))
           _nonHumanResources.add(resource);
        else
            _log.warn("Could not add unknown nonhuman resource to secondary resources: " +
                    resource.getID());
    }


    public void addNonHumanResourceUnchecked(String id) {
        _nonHumanResources.add(new NonHumanResource(id));
    }



    public void addNonHumanCategory(String id) {
        NonHumanCategory category = _rm.getOrgDataSet().getNonHumanCategory(id);
        if (category != null) _nonHumanCategories.add(category);
        else _log.warn("Unknown nonhuman category ID in secondary resources spec: " + id);
    }


    public void addNonHumanCategory(NonHumanCategory category) {
        if (_rm.getOrgDataSet().isKnownNonHumanCategory(category))
           _nonHumanCategories.add(category);
        else
            _log.warn("Could not add unknown nonhuman category to secondary resources: " +
                    category.getID());
    }


    public void addNonHumanCategoryUnchecked(String id) {
        NonHumanCategory category = new NonHumanCategory();
        category.setID(id);
        _nonHumanCategories.add(category);
    }


    public Set<Participant> getParticipants() { return _participants; }

    public Set<Role> getRoles() { return _roles; }

    public Set<NonHumanResource> getNonHumanResources() { return _nonHumanResources; }

    public Set<NonHumanCategory> getNonHumanCategories() { return _nonHumanCategories; }


    public boolean hasResources() {
        return (_participants.size() + _roles.size() +
                _nonHumanResources.size() + _nonHumanCategories.size()) > 0;
    }
    
    /***********************************************************************/

    public String toXML() {
        XNode node = new XNode("secondary");
        for (Participant p : _participants) {
            node.addChild("participant", p.getID());
        }
        for (Role r : _roles) {
            node.addChild("role", r.getID());
        }
        for (NonHumanResource r : _nonHumanResources) {
            node.addChild("nonHumanResource", r.getID());
        }
        for (NonHumanCategory c : _nonHumanCategories) {
            node.addChild("nonHumanCategory", c.getID());
        }
        return node.toPrettyString();
    }


    public void parse(Element e, Namespace nsYawl) throws ResourceParseException {
        if (e == null) return;
        List participants = e.getChildren("participant", nsYawl);
        for (Object o : participants) {
            addParticipant(((Element) o).getText());
        }
        List roles = e.getChildren("role", nsYawl);
        for (Object o : roles) {
            addRole(((Element) o).getText());
        }
        List nonHumanResources = e.getChildren("nonHumanResource", nsYawl);
        for (Object o : nonHumanResources) {
            addNonHumanResource(((Element) o).getText());
        }
        List nonHumanCategories = e.getChildren("nonHumanCategory", nsYawl);
        for (Object o : nonHumanCategories) {
            addNonHumanCategory(((Element) o).getText());
        }
    }


    /***********************************************************************/

    public void engage(WorkItemRecord wir) {
        if (hasResources()) {
            List<AbstractResource> selected = new ArrayList<AbstractResource>();
            for (Participant p : _participants) {
                if (isAvailable(p)) selected.add(p);
                else announceUnavailable(p, wir);
            }
            for (Role r : _roles) {
                AbstractResource selection = selectOneFromRole(r);
                if (selection != null) selected.add(selection);
                else announceUnavailable("role '" + r.getName(), wir);
            }
            for (NonHumanResource r : _nonHumanResources) {
                if (isAvailable(r)) selected.add(r);
                else announceUnavailable(r, wir);
            }
            for (NonHumanCategory c : _nonHumanCategories) {
                AbstractResource selection = selectOneFromCategory(c);
                if (selection != null) selected.add(selection);
                else announceUnavailable("nonhuman resource category '" + c.getName(), wir);
            }
            for (AbstractResource resource : selected) {
                EventLogger.log(wir, resource.getID(), EventLogger.event.busy);
            }
        }
    }


    public void disengage(WorkItemRecord wir) {
        if (hasResources()) {
            for (Object o : LogMiner.getInstance().getBusyResources(wir.getID())) {
                ResourceEvent event = (ResourceEvent) o;
                EventLogger.log(wir, event.get_resourceID(), EventLogger.event.released);
            }
        }
    }


    /**************************************************************************/

    private boolean isAvailable(AbstractResource resource) {
        return resource.isAvailable() && isDisengaged(resource.getID());
    }


    private void announceUnavailable(AbstractResource resource, WorkItemRecord wir) {
        ResourceManager.getInstance().announceResourceUnavailable(resource, wir, false);
        _log.warn("Secondary Resource '" + resource.getName() +
                "' unavailable for work item: " + wir.getID());
    }


    private void announceUnavailable(String name, WorkItemRecord wir) {
        ResourceManager.getInstance().announceResourceUnavailable(null, wir, false);
        _log.warn("There are no available members of " + name +
                "' to allocate as a secondary resource for work item: " + wir.getID());
    }


    private AbstractResource selectOneFromRole(Role r) {
        List<AbstractResource> roleList = new ArrayList<AbstractResource>(r.getResources());
        roleList.removeAll(_participants);
        return getRandomAvailableResource(roleList);
    }


    private AbstractResource selectOneFromCategory(NonHumanCategory c) {
        List<AbstractResource> categoryList = new ArrayList<AbstractResource>(c.getResources());
        categoryList.removeAll(_nonHumanResources);
        return getRandomAvailableResource(categoryList);
    }


    private AbstractResource getRandomAvailableResource(List<AbstractResource> resourceList) {
        if ((resourceList == null) || resourceList.isEmpty()) {
            return null;     // none found
        }
        
        // while the list has resources, remove a random selection and check if a
        // reservation for it would succeed
        while (resourceList.size() > 0) {
            AbstractResource actual = resourceList.remove(
                    (int) Math.floor(Math.random() * resourceList.size()));
            if (isAvailable(actual)) {
                return actual;                                      // found a candidate
            }
        }
        return null;
    }


    public boolean isDisengaged(String resourceID) {
        List result = LogMiner.getInstance().getLastBusyOrReleaseEvents(resourceID);
        if (result.size() > 0) {
            long busyStamp = -1, releasedStamp = -1;
            for (Object o : result) {
                Object[] content = (Object[]) o;
                if (content[0].equals("busy")) {
                    busyStamp = (Long) content[1];
                }
                else releasedStamp = (Long) content[1];
            }

            // if two events, it's available if released comes after busy
            return releasedStamp >= busyStamp;
        }
        return true;                                       // no result = available
    }
}
