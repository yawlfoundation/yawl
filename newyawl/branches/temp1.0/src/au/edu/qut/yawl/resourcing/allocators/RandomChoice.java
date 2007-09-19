/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing.allocators;

import au.edu.qut.yawl.resourcing.resource.Participant;

import java.util.*;

/**
 * Performs allocation based on random choice
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.1, 23/08/2007
 */


public class RandomChoice extends AbstractAllocator {

    public RandomChoice() {
        super();
        setDescription("The Random-Choice allocator blah blah.");
    }

    public RandomChoice(String name) {
        this() ;
        setName(name) ;
    }

    public RandomChoice(String name, HashMap params) {
        this();
        setName(name) ;
        setParams(params);
    }

    /**
     * randomly selects a single participant from the list provided
     * @param participants a distribution set of participants
     * @return a single participant, or null if participants is null or empty
     */
    public Participant performAllocation(Set participants) {

        if (participants == null) return null ;            // case: null set

        Object[] op = participants.toArray();
        if (op.length == 0) return null ;                  // case: empty set
        if (op.length == 1) return (Participant) op[0] ;   // case: only one member

        return (Participant) op[new Random().nextInt(op.length-1)];
    }

    
    public Set getParamKeys() {return null ; }
}
