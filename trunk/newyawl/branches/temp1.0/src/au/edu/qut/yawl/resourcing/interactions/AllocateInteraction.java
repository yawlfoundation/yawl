package au.edu.qut.yawl.resourcing.interactions;

import au.edu.qut.yawl.resourcing.allocators.ResourceAllocator;
import au.edu.qut.yawl.resourcing.allocators.AllocatorFactory;
import au.edu.qut.yawl.resourcing.resource.Participant;

import java.util.Set;

import org.jdom.Element;

/**
 * Created by IntelliJ IDEA. User: Default Date: 9/07/2007 Time: 15:19:44 To change this
 * template use File | Settings | File Templates.
 */
public class AllocateInteraction extends AbstractInteraction {

    private ResourceAllocator _allocator ;

    public AllocateInteraction(int initiator) {
        super(initiator) ;
    }

    public AllocateInteraction() { super(); }


    public void setAllocator(ResourceAllocator allocator) {
        _allocator = allocator ;
    }

    public Participant performAllocation(Set offerSet) {
        return _allocator.performAllocation(offerSet) ;
    }

    public void parse(Element e) {
        if (e != null) {
            parseInitiator(e) ;
            Element eAllocator = e.getChild("allocator") ;
            String allocatorClassName = eAllocator.getChildText("name") ;
            if (allocatorClassName != null) {
                _allocator = AllocatorFactory.getInstance(allocatorClassName);
                _allocator.setParams(parseParams(eAllocator));
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
