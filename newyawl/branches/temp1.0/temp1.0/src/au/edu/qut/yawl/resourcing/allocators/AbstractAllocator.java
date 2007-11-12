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

import java.util.HashMap;
import java.util.Set;

import org.jdom.Element;

/**
 * The base class for all allocators.
 *
 * Create Date: 03/08/2007. Last Date: 09/11/2007
 *
 *  @author Michael Adams (BPM Group, QUT Australia)
 *  @version 1.0
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

    /*******************************************************************************/

    /**
     * Generates the XML required for the specification file
     * @return an XML'd String containing the member values of an instantiation of this
     *         class
     */
    public String toXML() {
        StringBuilder result = new StringBuilder("<allocator>");
        result.append(super.toXML());
        result.append("</allocator>");
        return result.toString();
    }

    /**
     * Creates a runtime instance of this class from an XML description of it
     * @param elAllocator a JDOM Element describing the class
     * @return the instantiated object
     */
    public static AbstractAllocator unmarshal(Element elAllocator) {
        AbstractAllocator allocator =
                AllocatorFactory.getInstance(elAllocator.getChildText("name")) ;
        Element eParams = elAllocator.getChild("params");
        if (eParams != null)
            allocator.setParams(unmarshalParams(eParams));
        return allocator ;
    }

    /*******************************************************************************/

    // ABSTRACT METHOD - to be implemented by extending classes //

    /**
     * Performs an allocation using some strategy
     * @param resources the distribution set of participants
     * @return the Participant chosen by the allocation strategy
     */
    public abstract Participant performAllocation(Set<Participant> resources) ;

    /*******************************************************************************/


}
