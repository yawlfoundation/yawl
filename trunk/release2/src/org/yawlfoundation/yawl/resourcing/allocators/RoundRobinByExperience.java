/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.allocators;

import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.ResourceEvent;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.EventLogger;
import org.yawlfoundation.yawl.resourcing.resource.Participant;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Allocates a workitem to a participant on a round-robin basis, to the participant who
 * has performed the task the most number of times.
 *
 *  Create Date: 23/02/2009.
 *
 *  @author Michael Adams (BPM Group, QUT Australia)
 *  @version 2.0
 */


public class RoundRobinByExperience extends AbstractAllocator {

    public RoundRobinByExperience() {
        super();
        setName(this.getClass().getSimpleName()) ;
        setDisplayName("Round Robin (by experience)");
        setDescription("The Round-Robin (by experience) allocator distributes a workitem " +
                       "to the participant in the distribution set who has performed " +
                       "the task the most number of times.");
    }


    public Participant performAllocation(Set<Participant> participants,
                                         WorkItemRecord wir) {
        Participant chosen = null;
        long mostFrequent = -1;                                          // initiator
        if ((participants != null) && (participants.size() > 0)) {
            if (participants.size() == 1) {
                chosen = participants.iterator().next();               // only one in set
            }
            else {
                // more than one part. in the set
                List events = getLoggedEvents(wir, EventLogger.event.complete);
                if (! events.isEmpty()) {
                    for (Participant p : participants) {
                        long frequency = getFrequency(events, p);
                        if (frequency > mostFrequent) {
                            chosen = p ;
                            mostFrequent = frequency ;
                        }
                    }
                }
                else {

                    // if log is unavailable, default to a random choice
                    chosen = new RandomChoice().performAllocation(participants, wir);
                }
            }
        }
        return chosen;
    }


    private long getFrequency(List events, Participant p) {
        long result = 0;
        Iterator itr = events.iterator();
        while (itr.hasNext()) {
            ResourceEvent event = (ResourceEvent) itr.next();
            if (event.get_participantID().equals(p.getID()))
                result++;
        }
        return result;
    }

}