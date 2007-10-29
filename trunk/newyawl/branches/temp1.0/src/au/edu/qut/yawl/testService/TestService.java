package au.edu.qut.yawl.testService;

import au.edu.qut.yawl.engine.interfce.InterfaceBWebsideController;
import au.edu.qut.yawl.resourcing.ResourceMap;
import au.edu.qut.yawl.resourcing.TaskPrivileges;
import au.edu.qut.yawl.resourcing.allocators.GenericAllocator;
import au.edu.qut.yawl.resourcing.constraints.GenericConstraint;
import au.edu.qut.yawl.resourcing.filters.GenericFilter;
import au.edu.qut.yawl.resourcing.interactions.AbstractInteraction;
import au.edu.qut.yawl.resourcing.interactions.AllocateInteraction;
import au.edu.qut.yawl.resourcing.interactions.OfferInteraction;
import au.edu.qut.yawl.resourcing.interactions.StartInteraction;
import au.edu.qut.yawl.resourcing.resource.Participant;
import au.edu.qut.yawl.resourcing.resource.Role;
import au.edu.qut.yawl.resourcing.rsInterface.ResourceGatewayClientAdapter;
import au.edu.qut.yawl.worklist.model.WorkItemRecord;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: Default Date: 17/09/2007 Time: 17:48:13 To change this
 * template use File | Settings | File Templates.
 */
public class TestService extends InterfaceBWebsideController {

    public TestService() { super(); }

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

        // STEP 1: get required data from resource service (via resource gateway)
        String resURL = "http://localhost:8080/resourceService/gateway";
        ResourceGatewayClientAdapter resClient = new ResourceGatewayClientAdapter(resURL) ;

        // get full sets of filters, constraints and allocators
        String handle = resClient.connect("admin", "YAWL");

        List constraints = resClient.getConstraints(handle);

        List filters = resClient.getFilters(handle);
        List allocators = resClient.getAllocators(handle);

        // get full sets of Org Data
        List participants = resClient.getParticipants(handle);
        List roles = resClient.getRoles(handle);

        // not used here - shown for example only
        List capabilities = resClient.getCapabilities(handle);
        List positions = resClient.getPositions(handle);
        List orgGroups = resClient.getOrgGroups(handle);

        // STEP 2: Present data to designer - designer makes choices
        //         We'll simulate some choices here - assume designer chooses the
        //         first item in each set

        // choose one participant and one role
        Participant p = (Participant) participants.get(0);
        String participantID = p.getID() ;

        Role r = (Role) roles.get(0);
        String roleID = r.getID();

        // take the first selector from each and create a (relevant) generic selector
        GenericFilter gf = (GenericFilter) filters.get(0);
        String name = gf.getName();
        String fName = gf.getDisplayName() ;
        gf.addParam("fparam", roleID);

        GenericConstraint gc = (GenericConstraint) constraints.get(0);
        String cName = gc.getDisplayName();
        gc.addParam("cparam", "12");

        GenericAllocator ga = (GenericAllocator) allocators.get(0);
        String aName = ga.getDisplayName() ;
        ga.addParam("aparam", "qwerty");


        // STEP 3: Instantiate the resource objects

        // set up offers
        OfferInteraction offer = new OfferInteraction(AbstractInteraction.SYSTEM_INITIATED);
        offer.addParticipantUnchecked(participantID);
        offer.addRoleUnchecked(roleID);
        offer.addInputParam("aParamName", OfferInteraction.USER_PARAM) ;
        offer.addFilter(gf) ;
        offer.addConstraint(gc) ;
        offer.setFamiliarParticipantTask("famTask18");

        // set up allocator
        AllocateInteraction allocate = new AllocateInteraction(
                                                   AbstractInteraction.SYSTEM_INITIATED);
        allocate.setAllocator(ga);

        // set up start
        StartInteraction start = new StartInteraction(AbstractInteraction.SYSTEM_INITIATED);

        // add some user-task privileges
        TaskPrivileges tp = new TaskPrivileges();
        tp.addParticipantToPrivilegeUnchecked(participantID, TaskPrivileges.CAN_DEALLOCATE);
        tp.addParticipantToPrivilegeUnchecked(participantID, TaskPrivileges.CAN_SKIP);


        // STEP 4: Build the task's ResourceMap with the above objects
        //         There are two ways of doing this:

        // 4(a): Create a ResourceMap, set it up and assign it to the YTask

        // ResourceMap constructor expects a reference to the taskID
        ResourceMap rMap = new ResourceMap("task_23") ;

        // OR 4(b): get a reference to the task's ResourceMap (commented example below)

        /** YTask task = new YAtomicTask() ;  */

        // boolean true will create a new ResourceMap inside the task (if it doesn't
        // already exist)
        /** ResourceMap rMap = task.getResourceMap(true) ; */


        // add objects created above to the resource map
        rMap.setOfferInteraction(offer);
        rMap.setAllocateInteraction(allocate);
        rMap.setStartInteraction(start);
        rMap.setTaskPrivileges(tp);


        // STEP 5: Calling the YTask.toXML() will also build the resourcing stuff
        /** task.setResourceMap(rMap);  */

        // out to xml
        String xmlout = rMap.toXML() ;

        // this is here to test the output to a file
       /**  Document doc = JDOMConversionTools.stringToDocument(xmlout);
         JDOMConversionTools.documentToFile(doc, "c:/temp/resourcingout.xml"); */

        output.append(xmlout) ;

        /******* TEST CODE ENDS HERE *************************************************/

        output.append("</p></body></html>");
        outputWriter.write(output.toString());
        outputWriter.flush();
        outputWriter.close();

    }


}
