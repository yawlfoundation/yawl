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
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.util.XNode;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * Performs allocation based on most cost
 *
 *  @author Michael Adams
 */


public class DearestResource extends AbstractAllocator {

    public DearestResource() {
        super() ;
        setName(this.getClass().getSimpleName()) ;
        setDisplayName("Dearest Resource") ;
        setDescription("The Dearest Resource allocator chooses the participant from " +
                       "the distribution set with the most expensive cost.");
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

        Map<String, Participant> pMap = new Hashtable<String, Participant>();
        for (Participant p : participants) {
            pMap.put(p.getID(), p);
        }
        String choice = getHighestCost(getCostMap(pMap.keySet(), wir));

        // fall through & return random choice if cost choose failed
        return (choice != null) ? pMap.get(choice) :
                new RandomChoice().performAllocation(participants, wir);
    }


    /**
     * Gets the participant id in the map corresponding to the lowest cost amount
     * @param costMap a map of participant ids and their associated costs
     * @return the participant id with the lowest cost
     */
    private String getHighestCost(Map<String, XNode> costMap) {
        String choice = null;
        if (costMap != null) {
            double highest = Double.MIN_VALUE;
            for (String pid : costMap.keySet()) {
                double cost = getCost(costMap.get(pid), null);
                if (cost > highest) {
                    choice = pid;
                    highest = cost;
                }
            }
        }
        return choice;
    }

}
