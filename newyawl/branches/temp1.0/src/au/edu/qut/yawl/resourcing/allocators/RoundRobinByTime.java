/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing.allocators;

import au.edu.qut.yawl.resourcing.resource.Participant;

import java.util.Set;

/**
 * Allocates a workitem to a participant on a round-robin basis, to the participant who
 * has not performed the task for the longest time.
 *
 *  Create Date: 23/08/2007. Last Date: 12/11/2007
 *
 *  @author Michael Adams (BPM Group, QUT Australia)
 *  @version 1.0
 */

public class RoundRobinByTime extends AbstractAllocator {

    public RoundRobinByTime() {
        super();
        setName(this.getClass().getSimpleName()) ;        
        setDisplayName("Round Robin (by time)");
        setDescription("The Round-Robin (by Time) allocator distributes a workitem to the " +
                       "participant in the distribution set who performed " +
                       "the task the least recently.");
    }


    public Participant performAllocation(Set<Participant> participants) { return null ;}

}
