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

package org.yawlfoundation.yawl.resourcing.util;

import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.YPredicateParser;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.ResourceMap;
import org.yawlfoundation.yawl.resourcing.WorkQueue;
import org.yawlfoundation.yawl.resourcing.allocators.AbstractAllocator;
import org.yawlfoundation.yawl.resourcing.constraints.AbstractConstraint;
import org.yawlfoundation.yawl.resourcing.filters.AbstractFilter;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Role;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: Michael Adams
 * Creation Date: 1/03/2010
 */
public class LogPredicateParser extends YPredicateParser {

    private WorkItemRecord _wir ;
    private Participant _participant;
    private ResourceMap _resMap;

    public LogPredicateParser(Participant p, WorkItemRecord wir) {
        super();
        _participant = p;
        _wir = wir;
        _resMap = ResourceManager.getInstance().getResourceMap(wir);
    }


    public String valueOf (String s) {
        if (s.equals("${participant:name}")) {
            if (_participant != null) s = _participant.getFullName();
        }
        else if (s.equals("${participant:userid}")) {
            if (_participant != null) s = _participant.getUserID();
        }
        else if (s.equals("${participant:offeredQueueSize}")) {
            if (_participant != null)
                s = String.valueOf(_participant.getWorkQueues().getQueueSize(WorkQueue.OFFERED));
        }
        else if (s.equals("${participant:allocatedQueueSize}")) {
            if (_participant != null)
                s = String.valueOf(_participant.getWorkQueues().getQueueSize(WorkQueue.ALLOCATED));
        }
        else if (s.equals("${participant:startedQueueSize}")) {
            if (_participant != null)
                s = String.valueOf(_participant.getWorkQueues().getQueueSize(WorkQueue.STARTED));
        }
        else if (s.equals("${participant:suspendedQueueSize}")) {
            if (_participant != null)
                s = String.valueOf(_participant.getWorkQueues().getQueueSize(WorkQueue.SUSPENDED));
        }
        else if (s.equals("${resource:initiator:offer}")) {
            s = _resMap.getOfferInteraction().getInitiatorString();
        }
        else if (s.equals("${resource:initiator:allocate}")) {
            s = _resMap.getAllocateInteraction().getInitiatorString();
        }
        else if (s.equals("${resource:initiator:start}")) {
            s = _resMap.getStartInteraction().getInitiatorString();
        }
        else if (s.equals("${resource:offerset}")) {
            HashSet<Participant> set = _resMap.getOfferedParticipants(_wir.getID());
            if (set != null) {
                Set<String> names = new HashSet<String>();
                for (Participant p : set) {
                     names.add(p.getFullName());
                }
                s = namesToCSV(names);
            }
            else s = "n/a";
        }
        else if (s.equals("${resource:piler}")) {
            String piler = _resMap.getPiledResource().getFullName();
            s = (piler != null) ? piler : "n/a";
        }
        else if (s.equals("${resource:deallocators}")) {
            List<String> list = _resMap.getIgnoredList(_wir.getID());
            if (list != null) {
                Set<String> names = new HashSet<String>();
                ResourceManager rm = ResourceManager.getInstance();
                for (String id : list) {
                    names.add(rm.getOrgDataSet().getParticipant(id).getFullName());
                }
                s = namesToCSV(names);
            }
            else s = "n/a";
        }
        else if (s.equals("${resource:allocator}")) {
            AbstractAllocator allocator = _resMap.getAllocateInteraction().getAllocator();
            s = (allocator != null) ? allocator.getName() : "n/a";
        }
        else if (s.equals("${resource:roles}")) {
            Set<Role> roles = _resMap.getOfferInteraction().getRoles();
            if (! roles.isEmpty()) {
                Set<String> names = new HashSet<String>();
                for (Role r : roles) {
                    names.add(r.getName());
                }
                s = namesToCSV(names);
            }
            else s = "n/a";
        }
        else if (s.equals("${resource:dynParams}")) {
            Set<String> names = _resMap.getOfferInteraction().getDynParamNames();
            s = (! names.isEmpty()) ? namesToCSV(names) : "n/a";
        }
        else if (s.equals("${resource:filters}")) {
            Set<AbstractFilter> filters = _resMap.getOfferInteraction().getFilters();
            if (! filters.isEmpty()) {
                Set<String> names = new HashSet<String>();
                for (AbstractFilter filter : filters) {
                    names.add(filter.getDisplayName());
                }
                s = namesToCSV(names);
            }
            else s = "n/a";
        }
        else if (s.equals("${resource:constraints}")) {
            Set<AbstractConstraint> constraints = _resMap.getOfferInteraction().getConstraints();
            if (! constraints.isEmpty()) {
                Set<String> names = new HashSet<String>();
                for (AbstractConstraint filter : constraints) {
                    names.add(filter.getDisplayName());
                }
                s = namesToCSV(names);
            }
            else s = "n/a";
        }
        return s;
    }
}
