package au.edu.qut.yawl.resourcing.allocators;

import au.edu.qut.yawl.resourcing.resource.Participant;

import java.util.Set;

/**
 * Created by IntelliJ IDEA. User: Default Date: 14/08/2007 Time: 14:47:57 To change this
 * template use File | Settings | File Templates.
 */
public class GenericAllocator extends AbstractAllocator {

    public GenericAllocator(String name) {
        super(name);
    }

    public GenericAllocator() { super() ; }


    public Set getParamKeys() { return null; }

    public Participant performAllocation(Set resources) { return null; }
}
