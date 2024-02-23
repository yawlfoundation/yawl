/*
 * Copyright (c) 2004-2019 The YAWL Foundation. All rights reserved.
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
import org.yawlfoundation.yawl.util.XNode;

import java.util.*;


/**
 * Performs allocation based on the least average cost to allocate a work item
 *
 *  @author Mairen Zeevenhoven & Michael Adams
 *  @date 20-12-2019
 */


public class RiskAssessment extends AbstractAllocator {

    public RiskAssessment() {
        super();
        setName(this.getClass().getSimpleName());
        setDisplayName("RiskAssessment");
        setDescription("The RiskAssessment allocator chooses the best performing resource based " +
                "on a specified time threshold in combination with a risk probability " +
                "of the resource completing the task within the time threshold");
    }

    /**
     * Selects a single participant from the list provided that has the best probability
     * of completion within a time threshold at the cheapest rate
     *
     * @param participants a distribution set of participants
     * @param wir          the work item to allocate
     * @return a single participant, or null if participants is null or empty
     */
    public Participant performAllocation(Set<Participant> participants,
                                         WorkItemRecord wir) {
        if ((participants == null) || participants.isEmpty()) return null;
        if (participants.size() == 1) return participants.iterator().next();

        Map<String, Participant> pMap = participantSetToMap(participants);
        String choice = getBestChoice(getCostMap(pMap.keySet(), wir), pMap.keySet(), wir);

        // fall through & return random choice if cost choose failed
        return (choice != null) ? pMap.get(choice) :
                new RandomChoice().performAllocation(participants, wir);
    }


    /**
     * Gets the participant id in the map corresponding to the lowest cost amount
     *
     * @param pSet a map of participant ids and their associated costs
     * @param wir  the work item to allocate
     * @return the participant id with the lowest cost
     */

    private String getBestChoice(Map<String, XNode> costMap, Collection<String> pSet, WorkItemRecord wir) {

        String choice = null;
        if (pSet != null) {
//          Get Average durations
            Map<String, Double> frequencyUX = getFrequencyUX(wir, 6000);

            Map<String, Long> durationMap = getAvgDurations(
                    EventLogger.event.allocate, EventLogger.event.complete, wir);

//          Get amount of completions of this activity
            List events = getLoggedEvents(wir, EventLogger.event.complete);

            double lowest = Double.MAX_VALUE;
//          Check each participant in the participant set
            for (String pid : pSet) {

                double Threshold = 0.2;

                if (durationMap.containsKey(pid))  {

                    double riskPercentage = frequencyUX.get(pid);

//                  initialize the average duration from allocation - completion
                    long duration = durationMap.get(pid);
//                  Get the cost for this resource
                    double cost = getCost(costMap.get(pid), null);
//                  create variable for the cost for executing the task
                    double executionCost = duration * cost;

                    if ((duration > 0) && (executionCost < lowest) && (riskPercentage > 0.8)){
                        choice = pid;
                        lowest = executionCost;

                    }

                }
            }
        }
        return choice;
    }


    private Map<String, Double> getFrequencyUX(WorkItemRecord wir, long threshold) {
        // get a map of durations
        Map<String, List<Long>> durations = getDurations(EventLogger.event.allocate,EventLogger.event.complete, wir);

        Map<String, Double> probabilities = new HashMap<String, Double>();

        for (String pid : durations.keySet()) {
            List<Long> pidDurations = durations.get(pid);

            int countUnderThreshold = 0;
            for (Long period : pidDurations) {
                if (period <= threshold) {
                    countUnderThreshold++;
                }
            }

//          probability is the number of times under threshold / all times for pid
            double probability = countUnderThreshold / (double) pidDurations.size();
            probabilities.put(pid, probability);
        }

        return probabilities;
    }


    protected Map<String, List<Long>> getDurations(EventLogger.event fromEvent,
                                                EventLogger.event toEvent,
                                                WorkItemRecord wir) {
        List fromEvents = getLoggedEvents(wir, fromEvent);
        List toEvents = getLoggedEvents(wir, toEvent);

        Collection<EventPair> pairs = pairEvents(fromEvents, toEvents).values();

        return getParticipantTimes(pairs);
    }

    
    private Map<String, List<Long>> getParticipantTimes(Collection<EventPair> pairedEvents) {
        Map<String, List<Long>> participantTimes = new Hashtable<String, List<Long>>();
        for (EventPair pair : pairedEvents) {
            if (pair.hasPair()) {
                List<Long> times = participantTimes.get(pair.fromEvent.get_resourceID());
                if (times == null) {
                    times = new ArrayList<Long>();
                    participantTimes.put(pair.fromEvent.get_resourceID(), times);
                }
                times.add(pair.getDuration());
            }
        }
        return participantTimes;
    }


    private Map<String, EventPair> pairEvents(List fromEvents, List toEvents) {
        Map<String, EventPair> pairedEvents = new Hashtable<String, EventPair>();
        if (! ((fromEvents == null) || (toEvents == null))) {
            for (Object o : fromEvents) {
                ResourceEvent re = (ResourceEvent) o;
                EventPair pair = new EventPair();
                pair.fromEvent = re;
                pairedEvents.put(re.get_itemID(), pair);
            }
            for (Object o : toEvents) {
                ResourceEvent re = (ResourceEvent) o;
                EventPair pair = pairedEvents.get(re.get_itemID());
                if (pair != null) pair.toEvent = re;
            }
        }
        return pairedEvents;
    }
}
