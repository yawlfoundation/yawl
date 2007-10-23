package au.edu.qut.yawl.testService;

import au.edu.qut.yawl.engine.interfce.InterfaceBWebsideController;
import au.edu.qut.yawl.worklist.model.WorkItemRecord;
import au.edu.qut.yawl.resourcing.rsInterface.ResourceInterfaceClient;
import au.edu.qut.yawl.resourcing.TaskPrivileges;
import au.edu.qut.yawl.resourcing.ResourceMap;
import au.edu.qut.yawl.resourcing.interactions.OfferInteraction;
import au.edu.qut.yawl.resourcing.interactions.AbstractInteraction;
import au.edu.qut.yawl.resourcing.interactions.AllocateInteraction;
import au.edu.qut.yawl.resourcing.interactions.StartInteraction;
import au.edu.qut.yawl.resourcing.allocators.GenericAllocator;
import au.edu.qut.yawl.resourcing.constraints.GenericConstraint;
import au.edu.qut.yawl.resourcing.filters.GenericFilter;
import au.edu.qut.yawl.util.JDOMConversionTools;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;

import org.jdom.Element;

/**
 * Created by IntelliJ IDEA. User: Default Date: 17/09/2007 Time: 17:48:13 To change this
 * template use File | Settings | File Templates.
 */
public class TestService extends InterfaceBWebsideController {

    public void handleEnabledWorkItemEvent(WorkItemRecord enabledWorkItem) {

    }


    public void handleCancelledWorkItemEvent(WorkItemRecord workItemRecord) {

    }


    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("text/html");
        PrintWriter outputWriter = response.getWriter();
        StringBuffer output = new StringBuffer();

        output.append("<html><head><title>YAWL Test Service Welcome Page</title>")
              .append("</head><body><H3>Test Output</H3><p>");

        /******* TEST CODE STARTS HERE ***********************************************/

        // testing the resourceService gateway

        String resURL = "http://localhost:8080/resourceService/gateway";
        ResourceInterfaceClient resClient = new ResourceInterfaceClient(resURL) ;

//        Element ep = resClient.getParticipants();
//        Element er = resClient.getRoles();
//        Element ec = resClient.getCapabilities();
//        Element epo = resClient.getPositions();
//        Element eo = resClient.getOrgGroups();

//        Element e1 = resClient.getConstraints();
//        Element e2 = resClient.getFilters();
//        Element e3 = resClient.getAllocators();
//        Element e4 = resClient.getAllSelectors();

//        String s1 = resClient.getAllParticipantNames() ;
//        String s2 = resClient.getAllRoleNames() ;

//       output.append(s1).append(s2) ;
//
//        output.append(JDOMConversionTools.elementToString(ep));
//        output.append(JDOMConversionTools.elementToString(e1));
//        output.append(JDOMConversionTools.elementToString(e2));
//        output.append(JDOMConversionTools.elementToString(e3));
//        output.append(JDOMConversionTools.elementToString(e4));


        ///////////////////////////////////////////////////////////////////////////////

        // testing the spec xml build

        // STEP 1: get required data from resource service (via resource gateway)

//        String resURL = "http://localhost:8080/resourceService/gateway";
//        ResourceInterfaceClient resClient = new ResourceInterfaceClient(resURL) ;
//
        // get full sets of filters, constraints and allocators
        Element eConstraints = resClient.getConstraints();
        Element eFilters = resClient.getFilters();
        Element eAllocators = resClient.getAllocators();

        // get full sets of Org Data
        Element eParticipants = resClient.getParticipants();
        Element eRoles = resClient.getRoles();
        Element eCapabilities = resClient.getCapabilities();
        Element ePositions = resClient.getPositions();
        Element eOrgGroups = resClient.getOrgGroups();


        // STEP 2: Present data to designer - designer makes choices
        //         We'll simulate some choices here - assume designer chooses the
        //         first item in each set

        // choose one participant and one role
        String participantID = eParticipants.getChild("participant").getAttributeValue("id") ;
        String roleID = eRoles.getChild("role").getAttributeValue("id");

        System.out.println(roleID);


        // take the first selector from each and create a (relevant) base selector
        String fName = eFilters.getChild("filter").getChildText("name") ;
        GenericFilter bf = new GenericFilter(fName) ;
        bf.addParam("fparam", roleID);

        String fCons = eConstraints.getChild("constraint").getChildText("name") ;
        GenericConstraint bc = new GenericConstraint(fCons) ;
        bc.addParam("cparam", "12");

        String fAlloc = eAllocators.getChild("allocator").getChildText("name") ;
        GenericAllocator ba = new GenericAllocator(fAlloc) ;
        ba.addParam("aparam", "qwerty");


        // STEP 3: Instantiate the resource objects

        // set up offers
        OfferInteraction offer = new OfferInteraction(AbstractInteraction.SYSTEM_INITIATED);
        offer.addParticipantUnchecked(participantID);
        offer.addRoleUnchecked(roleID);
        offer.addInputParam("aParamName", OfferInteraction.USER_PARAM) ;
        offer.addFilter(bf) ;
        offer.addConstraint(bc) ;
        offer.setFamiliarParticipantTask("famTask18");

        // set up allocator
        AllocateInteraction allocate = new AllocateInteraction(
                                                   AbstractInteraction.SYSTEM_INITIATED);
        allocate.setAllocator(ba);

        // set up start
        StartInteraction start = new StartInteraction(AbstractInteraction.SYSTEM_INITIATED);

        // add some user-task privileges
        TaskPrivileges tp = new TaskPrivileges();
        tp.addParticipantToPrivilegeUnchecked(participantID, TaskPrivileges.CAN_DEALLOCATE);
        tp.addParticipantToPrivilegeUnchecked(participantID, TaskPrivileges.CAN_SKIP);


        // STEP 4: Build the task's ResourceMap with the above objects
        //         There are two ways of doing this:

        // 4(a): Create a ResourceMap, set it up and assign it to the YTask
        // ResourceMap constructor receives a reference to the taskID
        ResourceMap rMap = new ResourceMap("task_23") ;

        // OR 4(b): get a reference to the task's ResourceMap

    //    YTask task = new YAtomicTask() ;

        // boolean true will create a new ResourceMap inside the task (if it doesn't
        // already exist)
        // ResourceMap rMap = task.getResourceMap(true) ;


        // add objects created above to the resource map
        rMap.setOfferInteraction(offer);
        rMap.setAllocateInteraction(allocate);
        rMap.setStartInteraction(start);
        rMap.setTaskPrivileges(tp);


        // STEP 5: Calling the YTask.toXML() will also build the resourcing stuff
      //  task.setResourceMap(rMap);

        // out to xml
        String xmlout = rMap.toXML() ;
 //       Document doc = JDOMConversionTools.stringToDocument(xmlout);
 //       JDOMConversionTools.documentToFile(doc, "c:/temp/resourcingout.xml");

        output.append(xmlout) ;

        /******* TEST CODE ENDS HERE *************************************************/

        output.append("</p></body></html>");
        outputWriter.write(output.toString());
        outputWriter.flush();
        outputWriter.close();



    }


}
