/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing.allocators;

import au.edu.qut.yawl.resourcing.AbstractSelector;
import au.edu.qut.yawl.resourcing.resource.Participant;

import java.util.*;

import org.jdom.Element;

/**
 * The base class for all allocators. As well as a meaningful class name, extending
 * classes need to provide values for:
 *    _name --> a name for the allocators
 *    _description --> a user-oriented description of the allocators
 *    _params --> a set of parameter needed when applying the allocators (the param value
 *                should be set to null when initialised).
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.1, 03/08/2007
 */

public abstract class AbstractAllocator extends AbstractSelector {

    /** Constructors */

    public AbstractAllocator() { super(); }
    

    public AbstractAllocator(String name) {
        super(name);
    }


    public AbstractAllocator(String name, HashMap<String,String> params) {
       super(name, params) ;
    }


    public AbstractAllocator(String name, String description) {
       super(name, description) ;
    }


    public AbstractAllocator(String name, String desc, HashMap<String,String> params) {
        _name = name ;
        _params = params ;
        _description = desc ;
    }




    public String toXML() {
        StringBuilder result = new StringBuilder("<allocator>");
        result.append(super.toXML());
        result.append("</allocator>");
        return result.toString();
    }

    public static AbstractAllocator unmarshal(Element elAllocator) {
        AbstractAllocator allocator =
                AllocatorFactory.getInstance(elAllocator.getChildText("name")) ;
        Element eParams = elAllocator.getChild("params");
        if (eParams != null)
            allocator.setParams(unmarshalParams(eParams));
        return allocator ;
    }


    /** abstract method */

    public abstract Participant performAllocation(Set<Participant> resources) ;


}
