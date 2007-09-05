package au.edu.qut.yawl.resourcing.allocators;

import au.edu.qut.yawl.resourcing.AbstractSelector;
import au.edu.qut.yawl.resourcing.resource.Participant;

import java.util.*;

import org.jdom.Element;

/**
 * Created by IntelliJ IDEA. User: Default Date: 10/07/2007 Time: 13:49:00 To change this
 * template use File | Settings | File Templates.
 */

public abstract class ResourceAllocator extends AbstractSelector {

    /** Constructors */

    public ResourceAllocator() { super(); }

    public ResourceAllocator(String name) {
        super();
        _name = name ;
    }

    public ResourceAllocator(String name, HashMap params) {
        this(name) ;
        _params = params ;
    }

    public ResourceAllocator(String name, HashMap params, String desc) {
        this(name, params) ;
        _description = desc ;
    }

    public ResourceAllocator(String name, HashMap params, String desc, Set keys) {
        this(name, params, desc) ;
        _keys = keys ;
    }


    public String toXML() {
        StringBuilder result = new StringBuilder("<allocator>");
        result.append(super.toXML());
        result.append("</allocator>");
        return result.toString();
    }

    public static ResourceAllocator unmarshal(Element elAllocator) {
        ResourceAllocator allocator =
                AllocatorFactory.getInstance(elAllocator.getChildText("name")) ;
        Element eParams = elAllocator.getChild("params");
        if (eParams != null)
            allocator.setParams(unmarshalParams(eParams));
        return allocator ;
    }


    /** abstract method */

    public abstract Participant performAllocation(Set resources) ;


}
