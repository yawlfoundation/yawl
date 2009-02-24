/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.allocators;

import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.EventLogger;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.ResourceEvent;
import org.yawlfoundation.yawl.resourcing.datastore.persistence.Persister;
import org.yawlfoundation.yawl.resourcing.resource.Participant;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Allocates a workitem to a participant on a round-robin basis, to the participant who
 * has performed the task the least number of times.
 *
 *  Create Date: 09/11/2007. Last Date: 12/11/2007
 *
 *  @author Michael Adams (BPM Group, QUT Australia)
 *  @version 2.0
 */


public class RoundRobinByLeastFrequency extends AbstractAllocator {

    public RoundRobinByLeastFrequency() {
        super();
        setName(this.getClass().getSimpleName()) ;        
        setDisplayName("Round Robin (by least frequency)");
        setDescription("The Round-Robin (by frequency) allocator distributes a workitem " +
                       "to the participant in the distribution set who has performed " +
                       "the task the least number of times.");
    }


    public Participant performAllocation(Set<Participant> participants,
                                         WorkItemRecord wir) {
        Participant chosen = null;
        long maxFrequency = Long.MAX_VALUE;                                     // initiator
        if ((participants != null) && (participants.size() > 0)) {
            if (participants.size() == 1) {
                chosen = participants.iterator().next();               // only one in set
            }
            else {
                // more than one part. in the set
                List events = getLoggedEvents(wir);
                if (events != null) {
                    for (Participant p : participants) {
                        long frequency = getFrequency(events, p);
                        if (frequency == 0) {
                            chosen = p; break;         // this p has never performed item
                        }
                        else {
                            if (frequency < maxFrequency) {
                                chosen = p ;
                                maxFrequency = frequency ;
                            }
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


    private List getLoggedEvents(WorkItemRecord wir) {
        Persister persister = Persister.getInstance() ;
        if (persister != null) {
            String eventStr = EventLogger.event.complete.name();
            String specID = wir.getSpecificationID();
            String taskID = wir.getTaskID();
            return persister.selectWhere("ResourceEvent",
                  String.format("_event='%s' and tbl._specID='%s' and tbl._taskID='%s'",
                                eventStr, specID, taskID)) ;
        }
        else return null;
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