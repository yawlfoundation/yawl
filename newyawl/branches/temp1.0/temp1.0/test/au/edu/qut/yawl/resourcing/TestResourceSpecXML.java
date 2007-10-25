package au.edu.qut.yawl.resourcing;

import junit.framework.TestCase;

import au.edu.qut.yawl.resourcing.interactions.*;
import au.edu.qut.yawl.resourcing.allocators.GenericAllocator;
import au.edu.qut.yawl.resourcing.resource.Participant;
import au.edu.qut.yawl.resourcing.constraints.GenericConstraint;
import au.edu.qut.yawl.resourcing.filters.GenericFilter;
import au.edu.qut.yawl.util.JDOMConversionTools;

import org.jdom.Document;
import org.jdom.Element;

/**
 *
 * @author Michael Adams
 * Date: 01/08/2007
 *
 */
public class TestResourceSpecXML extends TestCase {

    public void testBuildSpecXML(){

        ResourceManager rm = ResourceManager.getInstance();

        // get full set of resource selectors (ie. filters, constraints, allocators)
        String xml = rm.getAllSelectors() ;
        Element eSelectors = JDOMConversionTools.stringToElement(xml);
        Element eFilters = eSelectors.getChild("filters");
        Element eConstraints = eSelectors.getChild("constraints");
        Element eAllocators = eSelectors.getChild("allocators");

        // take the first selector from each and create a base selector
        String fName = eFilters.getChild("filter").getChildText("name") ;
        GenericFilter bf = new GenericFilter(fName) ;
        bf.addParam("fparam", "cashiers");

        String fCons = eConstraints.getChild("constraint").getChildText("name") ;
        GenericConstraint bc = new GenericConstraint(fCons) ;
        bc.addParam("cparam", "12");

        String fAlloc = eAllocators.getChild("allocator").getChildText("name") ;
        GenericAllocator ba = new GenericAllocator(fAlloc) ;
        ba.addParam("aparam", "qwerty");

        // set up offers
        OfferInteraction offer = new OfferInteraction(AbstractInteraction.SYSTEM_INITIATED);
        offer.addParticipant("aUserID");
        offer.addRole("aRole");
        offer.addInputParam("aParam", OfferInteraction.USER_PARAM) ;
        offer.addFilter(bf) ;
        offer.addConstraint(bc) ;
        offer.setFamiliarParticipantTask("famTask18");

        // set up allocator
        AllocateInteraction allocate = new AllocateInteraction(AbstractInteraction.SYSTEM_INITIATED);
        allocate.setAllocator(ba);

        // set up start
        StartInteraction start = new StartInteraction(AbstractInteraction.SYSTEM_INITIATED);

        // add some user-task privileges
        TaskPrivileges tp = new TaskPrivileges();
        tp.grantDeallocateByID("delallID");
        tp.grantDelegate(new Participant("newPIDDelegate"));


        // ResourceMap constructor would normally receive a ref to the parent taskID
        ResourceMap rMap = new ResourceMap("task_23") ;

        // add objects created above to the resource map
        rMap.setOfferInteraction(offer);
        rMap.setAllocateInteraction(allocate);
        rMap.setStartInteraction(start);
        rMap.setTaskPrivileges(tp);

//        YTask task = new YAtomicTask() ;
//
//        task.getResourceMap() ;

        // out to xml
        String xml2 = rMap.toXML() ;
        Document doc = JDOMConversionTools.stringToDocument(xml2);
        JDOMConversionTools.documentToFile(doc, "c:/temp/resourcing2.xml");

    }
}
