/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.rsInterface;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.engine.interfce.ServletUtils;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.QueueSet;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.WorkQueue;
import org.yawlfoundation.yawl.resourcing.resource.OrgGroup;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.UserPrivileges;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Set;

/**
 *  The WorkQueue Gateway provides a gateway (or a set of API) between the Resource
 *  Service and the participant workqueue jsps.
 *
 *  @author Michael Adams
 *  v0.1, 13/08/2007
 *
 *  Last Date: 20/09/2007
 */

public class WorkQueueGateway extends HttpServlet {

    private ResourceManager _rm = ResourceManager.getInstance() ;
    private ResourceMarshaller _marshaller = new ResourceMarshaller();
    private static final Logger _log = Logger.getLogger(WorkQueueGateway.class);
    private static WorkQueueGateway _me;

    private static final String success = "<success/>";



    public static WorkQueueGateway getInstance() {
        if (_me == null) _me = new WorkQueueGateway();
        return _me ;
    }

    
    public void doPost(HttpServletRequest req, HttpServletResponse res)
                                throws IOException, ServletException {
        String result ;
        String action = req.getParameter("action");
        String handle = req.getParameter("sessionHandle");

        if (action == null) {
            result = "<html><head>" +
                    "<title>YAWL Resource Service WorkQueue Gateway</title>" +
                    "</head><body>" +
                    "<H3>Welcome to the YAWL Resource Service \"WorkQueue Gateway\"</H3>" +
                    "<p>The WorkQueue Gateway acts as a bridge between the Resource " +
                    "Service and a user interface implementation " +
                    "(it isn't meant to be browsed to directly).</p>" +
                    "</body></html>";
        }
        else if (action.equalsIgnoreCase("connect")) {
            String userid = req.getParameter("userid");
            String password = req.getParameter("password");
            result = _rm.serviceConnect(userid, password);           // service connect
        }
        else if (action.equalsIgnoreCase("userlogin")) {
            String userid = req.getParameter("userid");
            String password = req.getParameter("password");
            result = _rm.login(userid, password);                    // user connect
        }
        else if (action.equalsIgnoreCase("checkConnection")) {
            result = _rm.checkServiceConnection(handle) ? success :
                      fail("Invalid or disconnected session handle");
        }
        else if (action.equals("isValidUserSession")) {
            result = _rm.isValidUserSession(handle) ? success : 
                      fail("Invalid or disconnected session handle");
        }
        else if (authorisedCustomFormAction(action, handle)) {
            result = doAction(action, req);
        }
        else if (_rm.checkServiceConnection(handle)) {
            result = doAction(action, req);
        }
        else
            throw new IOException("Invalid or disconnected session handle");

        // generate the output
        OutputStreamWriter outputWriter = ServletUtils.prepareResponse(res);
        ServletUtils.finalizeResponse(outputWriter, result);
    }


    public void doGet(HttpServletRequest req, HttpServletResponse res)
                                throws IOException, ServletException {
        doPost(req, res);
    }
    

    /*** Private Methods *******************************/

    private String doAction(String action, HttpServletRequest req) throws IOException {
        String result = fail("Unknown action: " + action);          // assume the worst
        String handle = req.getParameter("sessionHandle");
        String userid = req.getParameter("userid") ;
        String pid = req.getParameter("participantid");
        String itemid = req.getParameter("workitemid");

        if (action.equals("getParticipantFromUserID")) {
            Participant p = _rm.getParticipantFromUserID(userid);
            result = (p != null) ? p.toXML() : fail("Unknown userid: " + userid) ;
        }
        else if (action.equals("getFullNameForUserID")) {
            String name = _rm.getFullNameForUserID(userid) ;
            result = (name != null) ? name : fail("Unknown userid: " + userid) ;
        }
        else if (action.equals("getUserPrivileges")) {
            Participant p = _rm.getParticipant(pid);
            if (p != null) {
                UserPrivileges up = p.getUserPrivileges();
                result = (up != null) ? up.toXML() :
                          fail("No privileges available for participant id: " + pid);
            }
            else result = fail("Unknown participant id: " + pid);
        }
        else if (action.equals("getParticipantsReportingTo")) {
            Set<Participant> set = _rm.getParticipantsReportingTo(pid);
            result = (set != null) ? _marshaller.marshallParticipants(set) :
                     fail("Invalid participant id or no participants reporting to: " + pid);
        }
        else if (action.equals("getOrgGroupMembers")) {
            String groupid = req.getParameter("groupid");
            OrgGroup og = _rm.getOrgGroup(groupid);
            if (og != null) {
                Set<Participant> set = _rm.getOrgGroupMembers(og);     // set never null
                result = _marshaller.marshallParticipants(set) ;
            }
            else result = fail("Unknown org group id: " + groupid);
        }        
        else if (action.equals("getParticipant")) {
            Participant p = _rm.getParticipant(pid);
            result = (p != null) ? _rm.getParticipant(pid).toXML() :
                      fail("Unknown participant id: " + pid);
        }
        else if (action.equals("getParticipants")) {
            Set<Participant> set = _rm.getParticipants();
            result = (set != null) ? _marshaller.marshallParticipants(set) :
                      fail("No participants found");
        }
        else if (action.equals("getAdminQueues")) {
            QueueSet qSet = _rm.getAdminQueues();
            result = qSet.toXML() ;                                    // set never null
        }
        else if (action.equals("getWorkItem")) {
            String itemID = req.getParameter("itemid");
            result = _rm.getWorkItem(itemID);
        }
        else if (action.equals("getWorkItemParameters")) {
            String itemID = req.getParameter("itemid");
            result = _rm.getTaskParamsAsXML(itemID);  
        }
        else if (action.equals("updateWorkItemData")) {
            String itemID = req.getParameter("itemid");
            String data = req.getParameter("data");
            result = _rm.updateWorkItemData(itemID, data);            
        }
        else if (action.equals("getQueuedWorkItems")) {
            int queueType = getQueueType(req.getParameter("queue")) ;
            if (WorkQueue.isValidQueueType(queueType)) {
                Participant p = _rm.getParticipant(pid);
                if (p != null) {
                    QueueSet qSet = p.getWorkQueues();
                    if (qSet != null) {
                       Set<WorkItemRecord> set = qSet.getQueuedWorkItems(queueType);
                       result = _marshaller.marshallWorkItemRecords(set) ;
                    }
                    else result = _marshaller.marshallWorkItemRecords(null);
                }
                else result = fail("Unknown participant id: " + pid);
            }
            else result = fail("Invalid queue type: " + req.getParameter("queue")) ;
        }
        else if (action.equals("getParticipantsAssignedWorkItem")) {
            int queueType = getQueueType(req.getParameter("queue")) ;
            if (WorkQueue.isValidQueueType(queueType)) {
                Set<Participant> set = _rm.getParticipantsAssignedWorkItem(itemid, queueType);
                result = _marshaller.marshallParticipants(set) ;
            }
            else result = fail("Invalid queue type: " + req.getParameter("queue")) ;
        }
        else if (action.equals("getWorkItemDurationsForParticipant")) {
            String specName = req.getParameter("specname");
            String version = req.getParameter("version");
            String taskName = req.getParameter("taskname");
            YSpecificationID specID = new YSpecificationID(specName, version);
            result = _rm.getWorkItemDurationsForParticipant(specID, taskName, pid);
        }
        else if (action.equals("getLoadedSpecs")) {
            Set<SpecificationData> set = _rm.getLoadedSpecs(handle) ;
            result = _marshaller.marshallSpecificationDataSet(set) ;
        }
        else if (action.equals("getSpecList")) {
            Set<SpecificationData> set = _rm.getSpecList(handle) ;
            result = _marshaller.marshallSpecificationDataSet(set) ;
        }
        else if (action.equals("getSpecData")) {
            String specID = req.getParameter("specid") ;
            String version = req.getParameter("version");
            SpecificationData specData = _rm.getSpecData(
                    new YSpecificationID(specID, version), handle);
            result = _marshaller.marshallSpecificationData(specData);
        }
        else if (action.equals("getRunningCases")) {
            String specID = req.getParameter("specid") ;
            String version = req.getParameter("version");
            result = _rm.getRunningCases(new YSpecificationID(specID, version), handle) ;
        }
        else if (action.equals("getDecompID")) {
            WorkItemRecord wir = _rm.getWorkItemCache().get(itemid) ;
            String decompID = _rm.getDecompID(wir) ;
            result = (decompID != null) ? decompID : fail("Unknown workitem: " + itemid);
        }
        else if (action.equals("getCaseData")) {
            String caseID = req.getParameter("caseid") ;
            result = _rm.getCaseData(caseID, handle) ;
        }
        else if (action.equals("getRegisteredServices")) {
            result = _rm.getRegisteredServicesAsXML(handle);
        }
        else if (action.equals("disconnect")) {
            _rm.serviceDisconnect(handle);
            result = success;
        }
        else if (action.equals("acceptOffer")) {
            result = doResourceAction(req, action);
        }
        else if (action.equals("startWorkItem")) {
            result = doResourceAction(req, action);
        }
        else if (action.equals("deallocateWorkItem")) {
            result = doResourceAction(req, action);
        }
        else if (action.equals("skipWorkItem")) {
            result = doResourceAction(req, action);
        }
        else if (action.equals("pileWorkItem")) {
            result = doResourceAction(req, action);
        }
        else if (action.equals("suspendWorkItem")) {
            result = doResourceAction(req, action);
        }
        else if (action.equals("unsuspendWorkItem")) {
            result = doResourceAction(req, action);
        }
        else if (action.equals("completeWorkItem")) {
            result = doResourceAction(req, action);
        }
        else if (action.equals("delegateWorkItem")) {
            result = doResourceMoveAction(req, action);
        }
        else if (action.equals("reallocateStatefulWorkItem")) {
            result = doResourceMoveAction(req, action);
        }
        else if (action.equals("reallocateStatelessWorkItem")) {
            result = doResourceMoveAction(req, action);
        }
        else if (action.equals("updateWorkItemCache")) {
            String wirAsXML = req.getParameter("wir") ;
            WorkItemRecord wir = Marshaller.unmarshalWorkItem(wirAsXML);
            if (wir != null) {
                _rm.getWorkItemCache().update(wir) ;
                result = success;
            }
            else result = fail("Malformed or empty work item XML record");
        }
        else if (action.equals("synchroniseCaches")) {
            _rm.sanitiseCaches();
            result = success;
        }

        // the following calls are convenience pass-throughs to engine interfaces A & B

        else if (action.equals("uploadSpecification")) {
            String fileContents = req.getParameter("fileContents") ;
            String fileName = req.getParameter("fileName");
            result = _rm.uploadSpecification(fileContents, fileName, handle) ;
        }
        else if (action.equals("unloadSpecification")) {
            String specID = req.getParameter("specid") ;
            String version = req.getParameter("version");
            result = _rm.unloadSpecification(specID, version, handle);           
        }
        else if (action.equals("launchCase")) {
            String specID = req.getParameter("specid") ;
            String caseData = req.getParameter("casedata") ;
            String version = req.getParameter("version");
            result = _rm.launchCase(specID, version, caseData, handle);
        }
        else if (action.equals("cancelCase")) {
            String caseID = req.getParameter("caseid") ;
            result = _rm.cancelCase(caseID, handle) ;
        }
        else if (action.equals("removeRegisteredService")) {
            String id = req.getParameter("serviceid");
            result = _rm.removeRegisteredService(id, handle);
        }
        else if (action.equals("addRegisteredService")) {
            String uri = req.getParameter("uri");
            String name = req.getParameter("name");
            String doco = req.getParameter("doco");
            String assignable = req.getParameter("assignable");
            YAWLServiceReference ysr = new YAWLServiceReference(uri, null, name);
            ysr.setDocumentation(doco);
            if (assignable != null) {
                ysr.set_assignable(assignable.equals("true"));
            }
            result = _rm.addRegisteredService(ysr, handle);
        }

        return result ;
    }

    // returns true if the action can be performed without admin-level privileges
    private boolean authorisedCustomFormAction(String action, String handle) {
        if ((action.equals("getWorkItem")) || (action.equals("updateWorkItemData"))) {
            return _rm.isValidUserSession(handle);
        }
        else return false;
    }


    private String doResourceAction(HttpServletRequest req, String action) {
        String result;
        String pid = req.getParameter("participantid");
        String itemid = req.getParameter("workitemid");

        Participant p = _rm.getParticipant(pid);
        if (p != null) {
            WorkItemRecord wir = _rm.getWorkItemCache().get(itemid) ;
            if (wir != null) {
                result = doResourceAction(p, wir, req, action);
            }
            else result = fail("Unknown workitem: " + itemid);
        }
        else result =  fail("Unknown participant: " + pid);

        return result;
    }

    private String doResourceMoveAction(HttpServletRequest req, String action) {
        String result;
        String pFrom = req.getParameter("pfrom");
        String pTo = req.getParameter("pto");
        String itemid = req.getParameter("workitemid");

        Participant pOrig = _rm.getParticipant(pFrom);
        if (pOrig != null) {
            Participant pDest = _rm.getParticipant(pTo);
            if (pDest != null) {
                WorkItemRecord wir = _rm.getWorkItemCache().get(itemid) ;
                if (wir != null) {
                    result = doResourceAction(pOrig, pDest, wir, action);
                }
                else result = fail("Unknown workitem: " + itemid);
            }
            else result =  fail("Unknown destination participant: " + pDest);
        }
        else result =  fail("Unknown source participant: " + pFrom);

        return result;
    }


    private String doResourceAction(Participant p, WorkItemRecord wir,
                                    HttpServletRequest req, String action) {
        String result = "<failure/>";
        String handle = req.getParameter("sessionHandle");
        boolean successful ;

        if (action.equals("acceptOffer")) {
            if (wir.hasResourceStatus(WorkItemRecord.statusResourceOffered)) {
                _rm.acceptOffer(p, wir);
                result = success;
            }
            else result = fail("Offered", "accepted", wir.getResourceStatus());
        }
        else if (action.equals("startWorkItem")) {
            if (wir.hasResourceStatus(WorkItemRecord.statusResourceUnoffered) ||
                wir.hasResourceStatus(WorkItemRecord.statusResourceOffered) ||
                wir.hasResourceStatus(WorkItemRecord.statusResourceAllocated)) {

                successful = _rm.start(p, wir, handle);
                result = successful ? success : fail("Could not start workitem: " +
                        wir.getID());
            }
            else result = fail("Unoffered', 'Offered' or 'Allocated", "accepted",
                    wir.getResourceStatus());                
        }
        else if (action.equals("deallocateWorkItem")) {
            if (wir.hasResourceStatus(WorkItemRecord.statusResourceAllocated)) {
                successful = _rm.deallocateWorkItem(p, wir) ;
                result = successful ? success : fail("Could not deallocated workitem: " +
                         wir.getID());
            }
            else result = fail("Allocated", "deallocated", wir.getResourceStatus());            
        }
        else if (action.equals("skipWorkItem")) {
            if (wir.hasResourceStatus(WorkItemRecord.statusResourceAllocated)) {
                successful = _rm.skipWorkItem(p, wir, handle) ;
                result = successful ? success : fail("Could not skip workitem: " + wir.getID());
            }
            else result = fail("Allocated", "skipped", wir.getResourceStatus());
        }
        else if (action.equals("pileWorkItem")) {
            if (wir.hasResourceStatus(WorkItemRecord.statusResourceAllocated)) {
                result = _rm.pileWorkItem(p, wir) ;
                if (result.startsWith("Cannot")) result = fail(result);
            }
            else result = fail("Allocated", "piled", wir.getResourceStatus());
        }
        else if (action.equals("suspendWorkItem")) {
            if (wir.hasResourceStatus(WorkItemRecord.statusResourceStarted)) {
                successful = _rm.suspendWorkItem(p, wir) ;
                result = successful ? success : fail("Could not suspend workitem: " +
                        wir.getID());
            }
            else result = fail("Started", "suspended", wir.getResourceStatus());            
        }
        else if (action.equals("unsuspendWorkItem")) {
            if (wir.hasResourceStatus(WorkItemRecord.statusResourceSuspended)) {
                successful = _rm.unsuspendWorkItem(p, wir) ;
                result = successful ? success : fail("Could not unsuspend workitem: " +
                        wir.getID());
            }
            else result = fail("Suspended", "unsuspended", wir.getResourceStatus());
        }
        else if (action.equals("completeWorkItem")) {
            if (wir.hasResourceStatus(WorkItemRecord.statusResourceStarted)) {
                result = _rm.checkinItem(p, wir, handle) ;
            }
            else result = fail("Started", "completed", wir.getResourceStatus());            
        }
        return result;
    }


    private String doResourceAction(Participant pOrig, Participant pDest,
                                    WorkItemRecord wir, String action) {
        String result = "<failure/>";
        boolean successful ;

        if (action.equals("delegateWorkItem")) {
            if (wir.hasResourceStatus(WorkItemRecord.statusResourceAllocated)) {
                successful = _rm.delegateWorkItem(pOrig, pDest, wir) ;
                result = successful ? success : fail("Could not delegate workitem: " +
                        wir.getID());
            }
            else result = fail("Allocated", "delegated", wir.getResourceStatus());
        }
        else if (action.equals("reallocateStatefulWorkItem")) {
            if (wir.hasResourceStatus(WorkItemRecord.statusResourceStarted)) {
                successful = _rm.reallocateStatefulWorkItem(pOrig, pDest, wir) ;
                result = successful ? success : fail("Could not reallocate workitem: " +
                        wir.getID());
            }
            else result = fail("Started", "reallocated", wir.getResourceStatus());
        }
        else if (action.equals("reallocateStatelessWorkItem")) {
            if (wir.hasResourceStatus(WorkItemRecord.statusResourceStarted)) {
                successful = _rm.reallocateStatelessWorkItem(pOrig, pDest, wir) ;
                result = successful ? success : fail("Could not reallocate workitem: " +
                        wir.getID());
            }
            else result = fail("Started", "reallocated", wir.getResourceStatus());
        }
        return result;
    }


    private int getQueueType(String queue) {
        try {
            return new Integer(queue) ;
        }
        catch (NumberFormatException nfe) {
            return -1 ;
        }
    }


    private String fail(String msg) {
        return "<failure>" + msg + "</failure>";
    }


    private String fail(String reqStatus, String action, String hasStatus) {
        return fail(
           String.format("Only a workitem with '%s' status can be %s. This workitem has '%s' status.",
                   reqStatus, action, hasStatus)
        );
    }

}
