package org.yawlfoundation.yawl.testService;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;
import org.yawlfoundation.yawl.engine.interfce.interfaceE.YLogGatewayClient;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.ResourceMap;
import org.yawlfoundation.yawl.resourcing.TaskPrivileges;
import org.yawlfoundation.yawl.resourcing.WorkQueue;
import org.yawlfoundation.yawl.resourcing.allocators.AbstractAllocator;
import org.yawlfoundation.yawl.resourcing.constraints.AbstractConstraint;
import org.yawlfoundation.yawl.resourcing.filters.AbstractFilter;
import org.yawlfoundation.yawl.resourcing.interactions.AbstractInteraction;
import org.yawlfoundation.yawl.resourcing.interactions.AllocateInteraction;
import org.yawlfoundation.yawl.resourcing.interactions.OfferInteraction;
import org.yawlfoundation.yawl.resourcing.interactions.StartInteraction;
import org.yawlfoundation.yawl.resourcing.resource.*;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceGatewayClientAdapter;
import org.yawlfoundation.yawl.resourcing.rsInterface.WorkQueueGatewayClient;
import org.yawlfoundation.yawl.resourcing.rsInterface.WorkQueueGatewayClientAdapter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by IntelliJ IDEA. User: Default Date: 17/09/2007 Time: 17:48:13 To change this
 * template use File | Settings | File Templates.
 */
public class TestService extends InterfaceBWebsideController {

    Logger _log = Logger.getLogger(this.getClass());

    public TestService() { super(); }

    public void handleEnabledWorkItemEvent(WorkItemRecord enabledWorkItem) {

    }


    public void handleCancelledWorkItemEvent(WorkItemRecord workItemRecord) {

    }


    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
 //       String url = "http://localhost:8080/testService/gateway";
     //   execJSP(url) ;

        response.setContentType("text/html");
        PrintWriter outputWriter = response.getWriter();
        StringBuffer output = new StringBuffer();

        output.append("<html><head><title>YAWL Test Service Welcome Page</title>")
              .append("</head><body><H3>Test Output</H3><p>");

   //     output.append(doResourceServiceGatewayTest()) ;
   //    output.append(createDummyOrgData());
   //      output.append(doLogGatewayTest()) ;
   //    output.append(doWorkQueueGatewayTest()) ;
   //     output.append(ibTest());
   //     output.append(doRandomTest()) ;
   //     output.append(doGetParticipantsTest()) ;
   //       output.append(controllerTest());
        output.append(stressTest());

         output.append("</p></body></html>");
         outputWriter.write(output.toString());
         outputWriter.flush();
         outputWriter.close();
    }

    private void prn(String s) { System.out.println(s) ; }

    public String execJSP(String urlStr) throws IOException {

    // create connection
    URL url = new URL(urlStr);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setDoInput(true);

     //retrieve reply
     String result = getReply(url.openStream());
     connection.disconnect();

     return result;
}


private static String getReply(InputStream is) throws IOException {
    InputStreamReader isr = new InputStreamReader(is);
    StringWriter out = new StringWriter(8192);
    char[] buffer = new char[8192];
    int count;

    while ((count = isr.read(buffer)) > 0)
        out.write(buffer, 0, count);

    isr.close();
    return out.toString();
}

    private String doGetParticipantsTest() {
        String resURL = "http://localhost:8080/resourceService/gateway";
        ResourceGatewayClientAdapter resClient = new ResourceGatewayClientAdapter(resURL) ;
        String handle = resClient.connect("admin", "YAWL");

        try {
            List<String> pSet = resClient.getAllParticipantNames(handle);
            for (String s : pSet) {
                System.out.println(s);
            }
        }
        catch (IOException ioe) {}
        return "";
    }

    private String doRandomTest() {
        int max = 11 ;
        int count = 1000 ;
        for (int i=0; i<count; i++) prn("" + new Random().nextInt(max-1)) ;
        return "";
    }

    private String doLogGatewayTest() throws IOException {
        YLogGatewayClient logClient = new YLogGatewayClient(
                                         "http://localhost:8080/yawl/logGateway") ;
        String handle = logClient.connect("admin", "YAWL") ;

        prn("handle = " + handle);

        // test all methods
        prn("getAllSpecIDs:");
        prn(logClient.getAllSpecIDs(handle));

        prn("");
        prn("getAllCaseEventIDs");
        prn(logClient.getAllCaseEventIDs(handle)) ;

        prn("");
        prn("getllCaseEventIDs - started events only:");
        prn(logClient.getAllCaseEventIDs("started", handle)) ;

        prn("");
        prn("getCaseEventIDsForSpec - casualty treatment:") ;
        prn(logClient.getCaseEventIDsForSpec("Casualty_Treatment", handle)) ;

        prn("");
        prn("getCaseEventsForSpec(Casualty_Treatment):");
        prn(logClient.getCaseEventsForSpec("Casualty_Treatment", handle));

        prn("");
        prn("getChildWorkItemEventsForParent - 3 events returned:");
        prn(logClient.getChildWorkItemEventsForParent(
                          "caa94661-056a-4025-b53a-b50a09f09ea7", handle)) ;

        prn("");
        prn("getParentWorkItemEventsForCase:");
        prn(logClient.getParentWorkItemEventsForCase(
                          "2d807928-85c3-41b6-80bf-76ae1de72491", handle)) ;

        prn("");
        prn("getParentWorkItemEventsForCaseID - 3:");
        prn(logClient.getParentWorkItemEventsForCaseID("3", handle)) ;

        prn("");
        prn("getCaseEventTime - caseeventid passed:");
        prn(logClient.getCaseEventTime("9a81dbb7-85f8-4ae4-a950-e29ca43cdc57", handle)) ;

        prn("");
        prn("getCaseEventTime - caseid and started passed:");
        prn(logClient.getCaseEventTime("260", "started", handle)) ;

        return "" ;
    }


    /*********************************************************************************/

    private String doWorkQueueGatewayTest() throws IOException {

        // STEP 1: get required data from resource service (via resource gateway)
        String resURL = "http://localhost:8080/resourceService/workqueuegateway";
        WorkQueueGatewayClientAdapter resClient = new WorkQueueGatewayClientAdapter(resURL);
        

        String handle = resClient.connect("admin", "YAWL");

        Participant p = resClient.getParticipantFromUserID("AdamsJ", handle);

        Set<WorkItemRecord> set = resClient.getQueuedWorkItems(p.getID(), WorkQueue.OFFERED, handle) ;

        System.out.println(set);

        return "";
    }

    private String doResourceServiceGatewayTest() throws IOException {
        /******* TEST CODE STARTS HERE ***********************************************/

        // testing the resourceService gateway

        // STEP 1: get required data from resource service (via resource gateway)
        String resURL = "http://localhost:8080/resourceService/gateway";
        ResourceGatewayClientAdapter resClient = new ResourceGatewayClientAdapter(resURL) ;

        // get full sets of filters, constraints and allocators
        String handle = resClient.connect("admin", "YAWL");

        // get full sets of Org Data
        List participants = resClient.getParticipants(handle);
        List roles = resClient.getRoles(handle);


        // not used here - shown for example only
        List capabilities = resClient.getCapabilities(handle);
        List positions = resClient.getPositions(handle);
        List orgGroups = resClient.getOrgGroups(handle);

        List constraints = resClient.getConstraints(handle);

        List filters = resClient.getFilters(handle);
        List allocators = resClient.getAllocators(handle);

        System.out.println("CONSTRAINTS");
        Iterator it = constraints.iterator();
        while (it.hasNext()) {
            AbstractConstraint ac = (AbstractConstraint) it.next() ;

            System.out.println("Name: " + ac.getName());
            System.out.println("DisplayName: " + ac.getDisplayName());
            System.out.println("Desc: " + ac.getDescription());
        }

        System.out.println("ALLOCATORS");
        it = allocators.iterator();
        while (it.hasNext()) {
            AbstractAllocator ac = (AbstractAllocator) it.next() ;
            System.out.println("Name: " + ac.getName());
            System.out.println("DisplayName: " + ac.getDisplayName());
            System.out.println("Desc: " + ac.getDescription());
        }

        System.out.println("FILTERS");    
        it = filters.iterator();
        while (it.hasNext()) {
            AbstractFilter ac = (AbstractFilter) it.next() ;
            System.out.println("Name: " + ac.getName());
            System.out.println("DisplayName: " + ac.getDisplayName());
            System.out.println("Desc: " + ac.getDescription());
        }



        // STEP 2: Present data to designer - designer makes choices
        //         We'll simulate some choices here - assume designer chooses the
        //         first item in each set

        // choose one participant and one role
        Participant p = (Participant) participants.get(0);
        String participantID = p.getID() ;

        Role r = (Role) roles.get(0);
        String roleID = r.getID();

        // take the first selector from each and create a (relevant) generic selector
        AbstractFilter gf = (AbstractFilter) filters.get(0);
        String name = gf.getName();
        String fName = gf.getDisplayName() ;
        gf.addParam("fparam", roleID);

        AbstractConstraint gc = (AbstractConstraint) constraints.get(0);
        String cName = gc.getDisplayName();
        gc.addParam("cparam", "12");

        AbstractAllocator ga = (AbstractAllocator) allocators.get(0);
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
      //  String xmlout = rMap.toXML() ;

        // this is here to test the output to a file
       /**  Document doc = JDOMUtil.stringToDocument(xmlout);
         JDOMUtil.documentToFile(doc, "c:/temp/resourcingout.xml"); */

        return rMap.toXML() ;
    }
        /******* TEST CODE ENDS HERE *************************************************/

        private String createDummyOrgData() {

            int HOW_MANY_PARTICIPANTS_TO_CREATE = 20;

            ResourceManager rm = ResourceManager.getInstance();
            rm.setPersisting(true);
            rm.initOrgDataSource("HibernateImpl", -1);
            Random rand = new Random();

            String[] f = {"Alex", "Bill", "Carol", "Diane", "Errol", "Frank", "George",
                    "Hilary", "Irene", "Joanne"};
            String[] l = {"Smith", "Jones", "Brown", "Black", "Roberts", "Lewis", "Johns",
                    "Green", "Gold", "Davies"};

            Role r2 = new Role("a larger role");
            r2.setPersisting(true);
            Role r = new Role("a shared role");
            r.setPersisting(true);

            rm.addRole(r2);
            rm.addRole(r);
            r.setOwnerRole(r2);

            OrgGroup o = new OrgGroup("mega", OrgGroup.GroupType.DIVISION, null, "mega");
            o.setPersisting(true);
            rm.addOrgGroup(o);

            OrgGroup o2 = new OrgGroup("minor", OrgGroup.GroupType.TEAM, o, "minor");
            o2.setPersisting(true);
            rm.addOrgGroup(o2);

            Position po = new Position("a position");
            po.setPersisting(true);
            Position p2 = new Position("manager");
            p2.setPersisting(true);
            rm.addPosition(p2);
            rm.addPosition(po);
            po.setReportsTo(p2);
            po.setOrgGroup(o2);
            p2.setOrgGroup(o2);

            Capability c = new Capability("a capability", "some description", true);
            rm.addCapability(c);


            for (int i = 0; i < HOW_MANY_PARTICIPANTS_TO_CREATE; i++) {
                String first = f[rand.nextInt(10)];
                String last = l[rand.nextInt(10)];
                String user = last + first.substring(0, 1);
                Participant p = new Participant(last, first, user, true);
                rm.addParticipant(p);

                p.setAdministrator(rand.nextBoolean());
                p.setPassword("apple");

                p.addPosition(po);
                p.addCapability(c);
                p.addRole(r);
                p.getUserPrivileges().allowAll();

            }

            return "Successfully created dummy org data";
        }


    private String ibTest() {
        try {
            String handle = _interfaceBClient.connect("admin", "YAWL");
            String doc = _interfaceBClient.getCaseData("2", handle);
            System.out.println(doc);
        }
        catch (Exception e) {}
            return "";
    }

    /********************************************************************************/

    private String stressTest() {
        // note: make sure "_stressTest.xml" is loaded in engine

        int numberOfCasesToStart = 40, i = 0;
        String obs = "http://localhost:8080/testService/ib";
        String result;

        _log.setLevel(Level.TRACE);

        try {
            String handle = _interfaceBClient.connect("admin", "YAWL");

            for (i=0; i<numberOfCasesToStart; i++) {
                result = _interfaceBClient.launchCase("StressTest", null, handle, obs);
                _log.trace("Case Started: " + result + ", case count: " + (i+1));
            }
        }
        catch (IOException ioe) {
            _log.error("IOException connecting to Engine.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return "Completed " + i + " case starts.";
    }


    public void handleCompleteCaseEvent(String caseID, String casedata) {
        _log.trace("Case Completed: " + caseID);
    }

    /*****************************************************************************/



    private String controllerTest() {
        Controller c = new Controller();
        c.getAllResources();
        return "";
    }

    class Controller extends WorkQueueGatewayClientAdapter{

 //       private static Controller instance;

private Controller() {
    super("http://localhost:8080/resourceService/workqueuegateway");
}

//public static Controller getInstance() {
//    if (instance == null)
//      instance = new Controller();
//    return (instance);
//  }
public String[] getAllResources()
  {
    try {
        String sessionHandler = connect("admin","YAWL");
        if(WorkQueueGatewayClient.successful(sessionHandler)){
            System.out.println("OK - Session:"+sessionHandler);
        }
        Set<Participant> participantSet = getAllParticipants(sessionHandler);
        Iterator iter = participantSet.iterator();
        String[] retValue = new String[participantSet.size()];
        int i = 0;
        while (iter.hasNext()) {
            Object obj = iter.next();
            Participant p = (Participant) obj;
            retValue[i] = p.getFullName();
            i++;
        }
        disconnect(sessionHandler);
        return retValue;
    } catch (IOException e) {
        e.printStackTrace();
        return null;
    }
  }
}

}
