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

package org.yawlfoundation.yawl.resourcing.allocators;

import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.EventLogger;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.ResourceEvent;
import org.yawlfoundation.yawl.resourcing.resource.Participant;

import java.util.*;

/**
 * Performs allocation based on the least average cost to allocate a work item
 *
 *  @author Michael Adams
 */


public class FastestResource extends AbstractAllocator {

    public FastestResource() {
        super() ;
        setName(this.getClass().getSimpleName()) ;
        setDisplayName("Fastest Resource") ;
        setDescription("The Fastest Resource allocator chooses the participant from " +
                       "the distribution set who, on average, has recorded the " +
                       "fastest time from item creation to completing the item.");
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
        String choice = getFastestAverage(pMap.keySet(), wir);

        // fall through & return random choice if cost choose failed
        return (choice != null) ? pMap.get(choice) :
                new RandomChoice().performAllocation(participants, wir);
    }


    /**
     * Gets the participant id in the map corresponding to the lowest cost amount
     * @param pSet a map of participant ids and their associated costs
     * @param wir the work item to allocate
     * @return the participant id with the lowest cost
     */
    private String getFastestAverage(Collection<String> pSet, WorkItemRecord wir) {
        String choice = null;
        if (pSet != null) {
            List completes = getLoggedEvents(wir, EventLogger.event.complete);
            if (completes != null) {

                // only want to count items that completed
                Map<String, EventPair> pairs = new Hashtable<String, EventPair>();
                for (Object o : completes) {
                    ResourceEvent event = (ResourceEvent) o;
                    EventPair pair = new EventPair();
                    pair.toEvent = event;
                    pairs.put(event.get_resourceID(), pair);
                }
                processEvents(getLoggedEvents(wir, EventLogger.event.start), pairs);
                processEvents(getLoggedEvents(wir, EventLogger.event.allocate), pairs);
                processEvents(getLoggedEvents(wir, EventLogger.event.offer), pairs);

                Map<String, Long> durationMap = getAvgDurations(pairs.values());
                double lowest = Double.MAX_VALUE;
                for (String pid : pSet) {
                    if (durationMap.containsKey(pid)) {
                        long duration = durationMap.get(pid);
                        if ((duration > 0) && (duration < lowest)) {
                            choice = pid;
                            lowest = duration;
                        }
                    }
                }
            }
        }
        return choice;
    }
        
        
    private void processEvents(List events, Map<String, EventPair> pairs) {
        if (! ((events == null) || (pairs == null))) {
            for (Object o : events) {
                ResourceEvent event = (ResourceEvent) o;
                EventPair pair = pairs.get(event.get_resourceID());
                if (pair != null) {
                    pair.fromEvent = event;
                }
            }
        }
    }

}
