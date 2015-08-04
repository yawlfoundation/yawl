/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.interactions;

import org.yawlfoundation.yawl.resourcing.allocators.AbstractAllocator;
import org.yawlfoundation.yawl.resourcing.allocators.AllocatorFactory;
import org.yawlfoundation.yawl.resourcing.resource.Participant;

import java.util.Set;

import org.jdom.Element;
import org.jdom.Namespace;

/**
 *  This class describes the requirements of a task at the allocate phase of
 *  allocating resources.
 *
 *  @author Michael Adams
 *  v0.1, 02/08/2007
 */

public class AllocateInteraction extends AbstractInteraction {

    private AbstractAllocator _allocator ;

    public AllocateInteraction(int initiator) {
        super(initiator) ;
    }

    public AllocateInteraction() { super(); }

    public AllocateInteraction(String ownerTaskID) { super(ownerTaskID) ; }


    public void setAllocator(AbstractAllocator allocator) {
        _allocator = allocator ;
    }

    public Participant performAllocation(Set offerSet) {
        return _allocator.performAllocation(offerSet) ;
    }

    public void parse(Element e, Namespace nsYawl) throws ResourceParseException {
        parseInitiator(e, nsYawl) ;

        Element eAllocator = e.getChild("allocator", nsYawl) ;
        if (eAllocator != null) {
            String allocatorClassName = eAllocator.getChildText("name", nsYawl) ;
            if (allocatorClassName != null) {
                _allocator = AllocatorFactory.getInstance(allocatorClassName);
                if (_allocator != null)
                    _allocator.setParams(parseParams(eAllocator, nsYawl));
                else
                    throw new ResourceParseException("Unknown allocator name: " +
                                                               allocatorClassName);
            }
            else throw new ResourceParseException("Missing allocator element: name") ;
        }
    }



    public String toXML() {
        StringBuilder xml = new StringBuilder("<allocate ");
        xml.append("initiator=\"").append(getInitiatorString()).append("\">");
        
        if (isSystemInitiated())
             if (_allocator != null) xml.append(_allocator.toXML()) ;
        xml.append("</allocate>");
        return xml.toString();
    }
}
