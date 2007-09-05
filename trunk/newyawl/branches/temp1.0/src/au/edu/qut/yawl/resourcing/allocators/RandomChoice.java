package au.edu.qut.yawl.resourcing.allocators;

import au.edu.qut.yawl.resourcing.resource.Participant;

import java.util.*;

/**
 * Created by IntelliJ IDEA. User: Default Date: 10/07/2007 Time: 13:56:42 To change this
 * template use File | Settings | File Templates.
 */

public class RandomChoice extends ResourceAllocator {

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
