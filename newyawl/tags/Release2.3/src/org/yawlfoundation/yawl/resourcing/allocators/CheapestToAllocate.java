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

package org.yawlfoundation.yawl.resourcing.allocators;

import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.EventLogger;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.util.XNode;

import java.util.Map;
import java.util.Set;

/**
 * Performs allocation based on the least average cost to allocate a work item
 *
 *  @author Michael Adams
 */


public class CheapestToAllocate extends AbstractAllocator {

    public CheapestToAllocate() {
        super() ;
        setName(this.getClass().getSimpleName()) ;
        setDisplayName("Cheapest to Allocate") ;
        setDescription("The Cheapest to Allocate allocator chooses the participant from " +
                       "the distribution set who, on average, has recorded the " +
                       "cheapest cost from item offer to allocating the item.");
    }


    /**
     * Selects a single participant from the list provided that has the lowest cost
     * rate for the work item in question
     * @param participants a distribution set of participants
     * @param wir the work item to allocate
     * @return a single participant, or null if participants is null or empty
     */
    public Participant performAllocation(Set<Participant> participants,
                                         WorkItemRecord wir) {

        if ((participants == null) || participants.isEmpty()) return null ;
        if (participants.size() == 1) return participants.iterator().next();

        Map<String, Participant> pMap = participantSetToMap(participants);
        String choice = getCheapestAverage(getCostMap(pMap.keySet(), wir), wir);

        // fall through & return random choice if cost choose failed
        return (choice != null) ? pMap.get(choice) :
                new RandomChoice().performAllocation(participants, wir);
    }


    /**
     * Gets the participant id in the map corresponding to the lowest cost amount
     * @param costMap a map of participant ids and their associated costs
     * @param wir the work item to allocate
     * @return the participant id with the lowest cost
     */
    private String getCheapestAverage(Map<String, XNode> costMap, WorkItemRecord wir) {
        String choice = null;
        if (costMap != null) {
            Map<String, Long> durationMap = getAvgDurations(
                    EventLogger.event.offer, EventLogger.event.allocate, wir);
            double lowest = Double.MAX_VALUE;
            for (String pid : costMap.keySet()) {
                if (durationMap.containsKey(pid)) {
                    double cost = getCost(costMap.get(pid), "assigned") * durationMap.get(pid);
                    if ((cost > 0) && (cost < lowest)) {
                        choice = pid;
                        lowest = cost;
                    }
                }
            }
        }
        return choice;
    }

}
