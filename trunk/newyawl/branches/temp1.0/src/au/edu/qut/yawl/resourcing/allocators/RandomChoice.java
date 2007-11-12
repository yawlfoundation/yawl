/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing.allocators;

import au.edu.qut.yawl.resourcing.resource.Participant;

import java.util.Random;
import java.util.Set;

/**
 * Performs allocation based on a random selection from a set
 *
 *  Create Date: 23/08/2007. Last Date: 12/11/2007
 *
 *  @author Michael Adams (BPM Group, QUT Australia)
 *  @version 1.0
 */


public class RandomChoice extends AbstractAllocator {

    public RandomChoice() {
        super() ;
        setName(this.getClass().getSimpleName()) ;
        setDisplayName("Random Choice") ;
        setDescription("The Random Choice allocator chooses one participant from " +
                       "the distribution set on a random basis.");
    }


    /**
     * randomly selects a single participant from the list provided
     * @param participants a distribution set of participants
     * @return a single participant, or null if participants is null or empty
     */
    public Participant performAllocation(Set<Participant> participants) {

        if (participants == null) return null ;            // case: null set

        Object[] op = participants.toArray();
        if (op.length == 0) return null ;                  // case: empty set
        if (op.length == 1) return (Participant) op[0] ;   // case: only one member

        return (Participant) op[new Random().nextInt(op.length-1)];
    }

}
