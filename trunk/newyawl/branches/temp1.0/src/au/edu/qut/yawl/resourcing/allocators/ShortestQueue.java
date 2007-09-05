package au.edu.qut.yawl.resourcing.allocators;

import au.edu.qut.yawl.resourcing.resource.Participant;

import java.util.*;

/**
 * Created by IntelliJ IDEA. User: Default Date: 10/07/2007 Time: 13:56:42 To change this
 * template use File | Settings | File Templates.
 */

public class ShortestQueue extends ResourceAllocator {

    public ShortestQueue() { super(); }

    public ShortestQueue(String name) {
        super(name) ;
        setDescription("The Shortest-Queue allocator blah blah.");
    }

    public ShortestQueue(String name, HashMap params) {
        super(name, params) ;
        setDescription("The Shortest-Queue allocator blah blah.");
    }


    public Participant performAllocation(Set l) { return null ;}

    public Set getParamKeys() {return null ; }
}
