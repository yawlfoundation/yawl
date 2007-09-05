package au.edu.qut.yawl.resourcing.allocators;

import au.edu.qut.yawl.resourcing.resource.Participant;

import java.util.*;

/**
 * Created by IntelliJ IDEA. User: Default Date: 10/07/2007 Time: 13:56:42 To change this
 * template use File | Settings | File Templates.
 */

public class RoundRobin extends ResourceAllocator {

    public RoundRobin() { super(); }

    public RoundRobin(String name) {
        super(name) ;
        setDescription("The Round-Robin allocator blah blah.");
    }

    public RoundRobin(String name, HashMap params) {
        super(name, params) ;
        setDescription("The Round-Robin allocator blah blah.");
    }


    public Participant performAllocation(Set l) { return null ;}

    public Set getParamKeys() {return null ; }
}
