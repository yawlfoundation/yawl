/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing.interactions;

import au.edu.qut.yawl.resourcing.allocators.AbstractAllocator;
import au.edu.qut.yawl.resourcing.allocators.AllocatorFactory;
import au.edu.qut.yawl.resourcing.resource.Participant;

import java.util.Set;

import org.jdom.Element;
import org.jdom.Namespace;

/**
 *  This class describes the requirements of a task at the allocate phase of
 *  allocating resources.
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
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

    public void parse(Element e, Namespace nsYawl) {
        if (e != null) {
            parseInitiator(e, nsYawl) ;
            Element eAllocator = e.getChild("allocator", nsYawl) ;
            if (eAllocator != null) {
                String allocatorClassName = eAllocator.getChildText("name", nsYawl) ;
                if (allocatorClassName != null) {
                    _allocator = AllocatorFactory.getInstance(allocatorClassName);
                    _allocator.setParams(parseParams(eAllocator, nsYawl));
                }
            }    
        }
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder("<allocate>");
        xml.append("<initiator>").append(getInitiatorString()).append("</initiator>");
        if (_allocator != null) xml.append(_allocator.toXML()) ;
        xml.append("</allocate>");
        return xml.toString();
    }
}
