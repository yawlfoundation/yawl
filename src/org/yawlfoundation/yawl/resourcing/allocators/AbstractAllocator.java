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

import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.AbstractSelector;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.client.CostClient;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.EventLogger;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.ResourceEvent;
import org.yawlfoundation.yawl.resourcing.datastore.persistence.Persister;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.io.IOException;
import java.util.*;

/**
 * The base class for all allocators.
 *
 * Create Date: 03/08/2007. Last Date: 09/11/2007
 *
 *  @author Michael Adams (BPM Group, QUT Australia)
 *  @version 2.0
 */

public abstract class AbstractAllocator extends AbstractSelector {

    /** Constructors */

    public AbstractAllocator() { super(); }
    

    public AbstractAllocator(String name) {
        super(name);
    }


    public AbstractAllocator(String name, HashMap<String,String> params) {
       super(name, params) ;
    }


    public AbstractAllocator(String name, String description) {
       super(name, description) ;
    }


    public AbstractAllocator(String name, String desc, HashMap<String,String> params) {
        _name = name ;
        _params = params ;
        _description = desc ;
    }

    /*******************************************************************************/

    /**
     * Generates the XML required for the specification file
     * @return an XML'd String containing the member values of an instantiation of this
     *         class
     */
    public String toXML() {
        StringBuilder result = new StringBuilder("<allocator>");
        result.append(super.toXML());
        result.append("</allocator>");
        return result.toString();
    }


    /**
     * Gets a list of all resource log rows for a given specification + task + event
     * combination.
     * @param wir a workitem record which is an instance of the specification+task in
     * question.
     * @param event the type of event to get records for
     * @return the matching list of ResourceEvent objects (each one a row of the log)
     */
    protected List getLoggedEvents(WorkItemRecord wir, EventLogger.event event) {
        Persister persister = Persister.getInstance() ;
        if (persister != null) {
            YSpecificationID specID = new YSpecificationID(wir);
            long specKey = EventLogger.getSpecificationKey(specID);
            String eventStr = event.name();
            String taskName = wir.getTaskID();
            List events = persister.selectWhere("ResourceEvent",
                  String.format("_event='%s' AND tbl._specKey=%d AND tbl._taskID='%s'",
                                eventStr, specKey, taskName));
            persister.commit();
            return events;
        }
        else return null;
    }


    /**
     * Converts a set of participants to a map
     * @param pSet the set of participants
     * @return a map of participant id to participant
     */
    protected Map<String, Participant> participantSetToMap(Set<Participant> pSet) {
        Map<String, Participant> pMap = new Hashtable<String, Participant>();
        if (pSet != null) {
            for (Participant p : pSet) {
                pMap.put(p.getID(), p);
            }
        }
        return pMap;
    }
            

    /**
     * Gets from the cost service the cost per msec of each of the participants ids
     * listed for the work item
     * @param pids a set of participant ids
     * @param wir the work item to allocate
     * @return a map containing each participant id and its corresponding cost per msec
     * to perform this work item
     */
    protected Map<String, XNode> getCostMap(Set<String> pids, WorkItemRecord wir) {
        Map<String, XNode> costMap = null;
        CostClient client = ResourceManager.getInstance().getClients().getCostClient();

        // short-circuit if no cost service running
        if (client == null) return costMap;

        try {
            String handle = client.getHandle();
            String xml = client.getResourceCosts(
                    new YSpecificationID(wir), wir.getTaskName(), pids, handle);
            XNode node = new XNodeParser().parse(xml);
            if (node != null) {
                costMap = new Hashtable<String, XNode>();
                for (XNode resNode : node.getChildren()) {
                    costMap.put(resNode.getAttributeValue("id"), resNode);
                }
            }

        }
        catch (IOException ioe) {
            // nothing to do - null returned below
        }
        return costMap;
    }
    
    
    protected double getCost(XNode resNode, String duration) {
        double cost = 0;
        for (XNode driverNode : resNode.getChildren()) {
            if ((duration == null) || driverNode.getChildText("duration").equals(duration)) {
                cost += Double.parseDouble(driverNode.getChildText("amount"));
            }
        }
        return cost;
    }


    /**
     * Gets the average duration (in msecs) between two events for each participant
     * recorded against those events for a task
     * @param fromEvent the earlier event (start of duration)
     * @param toEvent the later event (end of duration)
     * @param wir a work item to allocate
     * @return a Map of participant ids and average durations
     */
    protected Map<String, Long> getAvgDurations(EventLogger.event fromEvent,
                                                EventLogger.event toEvent,
                                                WorkItemRecord wir) {
        List fromEvents = getLoggedEvents(wir, fromEvent);
        List toEvents = getLoggedEvents(wir, toEvent);
        return getAvgDurations(pairEvents(fromEvents, toEvents).values());
    }


    protected Map<String, Long> getAvgDurations(Collection<EventPair> pairs) {
        Map<String, Set<Long>> participantTimes = getParticipantTimes(pairs);
        Map<String, Long> avgDurations = new Hashtable<String, Long>();
        for (String pid : participantTimes.keySet()) {
            avgDurations.put(pid, getAverage(participantTimes.get(pid)));
        }
        return avgDurations;
    }


    /**
     * Merges a list of starting events and a list of ending events for a task's
     * instances into a map of event pairs
     * @param fromEvents the list of earlier events (start of duration)
     * @param toEvents the list of later events (end of duration)
     * @return a map of work item ids and the corresponding event pair
     */
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


    /**
     * Gets a set of durations for each participant referenced by a map of event pairs
     * @param pairedEvents a set of event pairs
     * @return a map of participant ids, each to a set of durations (in msecs)
     */
    private Map<String, Set<Long>> getParticipantTimes(Collection<EventPair> pairedEvents) {
        Map<String, Set<Long>> participantTimes = new Hashtable<String, Set<Long>>();
        for (EventPair pair : pairedEvents) {
            if (pair.hasPair()) {
                Set<Long> times = participantTimes.get(pair.fromEvent.get_resourceID());
                if (times == null) {
                    times = new HashSet<Long>();
                    participantTimes.put(pair.fromEvent.get_resourceID(), times);
                }
                times.add(pair.getDuration());
            }
        }
        return participantTimes;
    }


    /**
     * Gets the average of a set of long values
     * @param setOfLongs the set
     * @return the average of the set's values
     */
    private long getAverage(Set<Long> setOfLongs) {
        long total = 0;
        for (long l : setOfLongs) total += l;
        return total / setOfLongs.size();
    }


    protected class EventPair {
        ResourceEvent fromEvent;
        ResourceEvent toEvent;

        boolean hasPair() { return ! ((fromEvent == null) || (toEvent == null)); }

        long getDuration() { return toEvent.get_timeStamp() - fromEvent.get_timeStamp(); }
    }


    /*******************************************************************************/

    // ABSTRACT METHOD - to be implemented by extending classes //

    /**
     * Performs an allocation using some strategy
     * @param resources the distribution set of participants
     * @param wir the work item to allocate
     * @return the Participant chosen by the allocation strategy
     */
    public abstract Participant performAllocation(Set<Participant> resources,
                                                  WorkItemRecord wir) ;

    /*******************************************************************************/


}
