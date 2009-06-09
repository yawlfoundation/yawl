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


    public static WorkQueueGateway getInstance() {
        if (_me == null) _me = new WorkQueueGateway();
        return _me ;
    }

    
    public void doPost(HttpServletRequest req, HttpServletResponse res)
                                throws IOException, ServletException {
        String result = "";
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
            result = _rm.serviceConnect(userid, password);
        }
        else if (action.equalsIgnoreCase("userlogin")) {
            String userid = req.getParameter("userid");
            String password = req.getParameter("password");
            result = _rm.login(userid, password);
        }
        else if (action.equalsIgnoreCase("checkConnection")) {
            result = String.valueOf(_rm.checkServiceConnection(handle)) ;
        }
        else if (action.equals("isValidUserSession")) {
            result = String.valueOf(_rm.isValidUserSession(handle)) ;
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
    

    private String doAction(String action, HttpServletRequest req) throws IOException {
        String result = "";
        String handle = req.getParameter("sessionHandle");
        String userid = req.getParameter("userid") ;
        String pid = req.getParameter("participantid");
        String itemid = req.getParameter("workitemid");

        if (action.equals("getParticipantFromUserID")) {
            Participant p = _rm.getParticipantFromUserID(userid);
            result = (p != null) ? p.toXML() : "<null>" ;  
        }
        else if (action.equals("getFullNameForUserID")) {
            result = _rm.getFullNameForUserID(userid) ;
        }
        else if (action.equals("getUserPrivileges")) {
            Participant p = _rm.getParticipant(pid);
            if (p != null) {
                UserPrivileges up = p.getUserPrivileges();
                if (up != null) result = up.toXML();
            }
        }
        else if (action.equals("getParticipantsReportingTo")) {
            Set<Participant> set = _rm.getParticipantsReportingTo(pid);
            result = _marshaller.marshallParticipants(set) ;
        }
        else if (action.equals("getOrgGroupMembers")) {
            String groupid = req.getParameter("groupid");
            OrgGroup og = _rm.getOrgGroup(groupid);
            Set<Participant> set = _rm.getOrgGroupMembers(og);
            result = _marshaller.marshallParticipants(set) ;
        }        
        else if (action.equals("getParticipant")) {
            result = _rm.getParticipant(pid).toXML();
        }
        else if (action.equals("getParticipants")) {
            Set<Participant> set = _rm.getParticipants();
            result = _marshaller.marshallParticipants(set) ;
        }
        else if (action.equals("getAdminQueues")) {
            QueueSet qSet = _rm.getAdminQueues();
            result = qSet.toXML() ;
        }
        else if (action.equals("getWorkItem")) {
            String itemID = req.getParameter("itemid");
            result = _rm.getWorkItem(itemID);
        }
        else if (action.equals("updateWorkItemData")) {
            String itemID = req.getParameter("itemid");
            String data = req.getParameter("data");
            result = _rm.updateWorkItemData(itemID, data);            
        }
        else if (action.equals("getQueuedWorkItems")) {
            int queueType = new Integer(req.getParameter("queue")) ;
            Participant p = _rm.getParticipant(pid);
            if (p != null) {
                Set<WorkItemRecord> set = p.getWorkQueues().getQueuedWorkItems(queueType);
                result = _marshaller.marshallWorkItemRecords(set);
            }
        }
        else if (action.equals("getParticipantsAssignedWorkItem")) {
            int queueType = new Integer(req.getParameter("queue")) ;
            Set<Participant> set = _rm.getParticipantsAssignedWorkItem(itemid, queueType);
            result = _marshaller.marshallParticipants(set) ;
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
            if (specData != null)
                result = new ResourceMarshaller().marshallSpecificationData(specData);
        }
        else if (action.equals("getRunningCases")) {
            String specID = req.getParameter("specid") ;
            String version = req.getParameter("version");
            result = _rm.getRunningCases(new YSpecificationID(specID, version), handle) ;
        }
        else if (action.equals("getDecompID")) {
            WorkItemRecord wir = _rm.getWorkItemCache().get(itemid) ;
            result = _rm.getDecompID(wir) ;
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
        }
        else if (action.equals("acceptOffer")) {
            Participant p = _rm.getParticipant(pid);
            WorkItemRecord wir = _rm.getWorkItemCache().get(itemid) ;
            if (! wir.getResourceStatus().equals(WorkItemRecord.statusResourceOffered)) {
                result = "<failure>A workitem must have 'Offered' status to accept an offer." +
                         " This workitem has '" + wir.getResourceStatus() + "' status.</failure>";
            }
            else _rm.acceptOffer(p, wir);
        }
        else if (action.equals("startWorkItem")) {
            Participant p = _rm.getParticipant(pid);
            WorkItemRecord wir = _rm.getWorkItemCache().get(itemid) ;
            _rm.start(p, wir, handle);
        }
        else if (action.equals("deallocateWorkItem")) {
            Participant p = _rm.getParticipant(pid);
            WorkItemRecord wir = _rm.getWorkItemCache().get(itemid) ;
            boolean success = _rm.deallocateWorkItem(p, wir) ;
            result = String.valueOf(success);
        }
        else if (action.equals("skipWorkItem")) {
            Participant p = _rm.getParticipant(pid);
            WorkItemRecord wir = _rm.getWorkItemCache().get(itemid) ;
            boolean success = _rm.skipWorkItem(p, wir, handle) ;
            result = String.valueOf(success);
        }
        else if (action.equals("pileWorkItem")) {
            Participant p = _rm.getParticipant(pid);
            WorkItemRecord wir = _rm.getWorkItemCache().get(itemid) ;
            result = _rm.pileWorkItem(p, wir) ;
        }
        else if (action.equals("suspendWorkItem")) {
            Participant p = _rm.getParticipant(pid);
            WorkItemRecord wir = _rm.getWorkItemCache().get(itemid) ;
            boolean success = _rm.suspendWorkItem(p, wir) ;
            result = String.valueOf(success);
        }
        else if (action.equals("unsuspendWorkItem")) {
            Participant p = _rm.getParticipant(pid);
            WorkItemRecord wir = _rm.getWorkItemCache().get(itemid) ;
            boolean success = _rm.unsuspendWorkItem(p, wir) ;
            result = String.valueOf(success);
        }
        else if (action.equals("completeWorkItem")) {
            Participant p = _rm.getParticipant(pid);
            WorkItemRecord wir = _rm.getWorkItemCache().get(itemid) ;
            result = _rm.checkinItem(p, wir, handle) ;
        }
        else if (action.equals("delegateWorkItem")) {
            String pFrom = req.getParameter("pfrom");
            String pTo = req.getParameter("pto");
            Participant pOrig = _rm.getParticipant(pFrom);
            Participant pDest = _rm.getParticipant(pTo);
            WorkItemRecord wir = _rm.getWorkItemCache().get(itemid) ;
            boolean success = _rm.delegateWorkItem(pOrig, pDest, wir) ;
            result = String.valueOf(success);
        }
        else if (action.equals("reallocateStatefulWorkItem")) {
            String pFrom = req.getParameter("pfrom");
            String pTo = req.getParameter("pto");
            Participant pOrig = _rm.getParticipant(pFrom);
            Participant pDest = _rm.getParticipant(pTo);
            WorkItemRecord wir = _rm.getWorkItemCache().get(itemid) ;
            boolean success = _rm.reallocateStatefulWorkItem(pOrig, pDest, wir) ;
            result = String.valueOf(success);
        }
        else if (action.equals("reallocateStatelessWorkItem")) {
            String pFrom = req.getParameter("pfrom");
            String pTo = req.getParameter("pto");
            Participant pOrig = _rm.getParticipant(pFrom);
            Participant pDest = _rm.getParticipant(pTo);
            WorkItemRecord wir = _rm.getWorkItemCache().get(itemid) ;
            boolean success = _rm.reallocateStatelessWorkItem(pOrig, pDest, wir) ;
            result = String.valueOf(success);
        }
        else if (action.equals("updateWorkItemCache")) {
            String wirAsXML = req.getParameter("wir") ;
            WorkItemRecord wir = Marshaller.unmarshalWorkItem(wirAsXML);
            _rm.getWorkItemCache().update(wir) ;
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
            String service = req.getParameter("service");
            YAWLServiceReference ysr = YAWLServiceReference.unmarshal(service);
            result = _rm.addRegisteredService(ysr, handle);
        }

        return result ;
    }


    private boolean authorisedCustomFormAction(String action, String handle) {
        if ((action.equals("getWorkItem")) || (action.equals("updateWorkItemData"))) {
            return _rm.isValidUserSession(handle);
        }
        else return false;
    }

}
