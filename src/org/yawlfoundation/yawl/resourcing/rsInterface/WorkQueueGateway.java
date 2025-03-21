/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.resourcing.rsInterface;

import org.yawlfoundation.yawl.authentication.YExternalClient;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.*;
import org.yawlfoundation.yawl.resourcing.*;
import org.yawlfoundation.yawl.resourcing.datastore.orgdata.ResourceDataSet;
import org.yawlfoundation.yawl.resourcing.resource.OrgGroup;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.UserPrivileges;
import org.yawlfoundation.yawl.resourcing.util.GadgetFeeder;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;
import org.yawlfoundation.yawl.util.YPredicateParser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The WorkQueue Gateway provides a gateway (or a set of API) between the Resource
 * Service and the participant workqueue jsps.
 *
 * @author Michael Adams
 *         v0.1, 13/08/2007
 *         <p/>
 *         Last Date: 20/09/2007
 */

public class WorkQueueGateway extends YHttpServlet {

    private ResourceManager _rm = ResourceManager.getInstance();
    private ResourceMarshaller _marshaller = new ResourceMarshaller();
    private static WorkQueueGateway _me;

    private static final String success = "<success/>";

    private boolean gadgetPostback = false;
    private Map gadgetParamMap = null;

    public static WorkQueueGateway getInstance() {
        if (_me == null) _me = new WorkQueueGateway();
        return _me;
    }


    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        String result;
        String action = req.getParameter("action");
        String handle = req.getParameter("sessionHandle");

        if (action == null) {
            if (gadgetPostback) {
                gadgetPostback = false;
                gadgetParamMap.putAll(req.getParameterMap());
                result = (new GadgetFeeder(gadgetParamMap)).getFeed();
            } else {
                result = "<html><head>" +
                        "<title>YAWL Resource Service WorkQueue Gateway</title>" +
                        "</head><body>" +
                        "<H3>Welcome to the YAWL Resource Service \"WorkQueue Gateway\"</H3>" +
                        "<p>The WorkQueue Gateway acts as a bridge between the Resource " +
                        "Service and a user interface implementation " +
                        "(it isn't meant to be browsed to directly).</p>" +
                        "</body></html>";
            }
        } else if (action.equalsIgnoreCase("getGadgetContent")) {
            gadgetPostback = true;
            gadgetParamMap = new HashMap(req.getParameterMap());
            result = (new GadgetFeeder(gadgetParamMap)).getFeed();
        } else if (action.equalsIgnoreCase("connect")) {
            String userid = req.getParameter("userid");
            String password = req.getParameter("password");
            if (StringUtil.strToBoolean(req.getParameter("encrypt"))) {
                password = encryptPassword(password);
            }
            int interval = req.getSession().getMaxInactiveInterval();
            result = _rm.serviceConnect(userid, password, interval);
        } else if (action.equalsIgnoreCase("userlogin")) {
            String userid = req.getParameter("userid");
            String password = req.getParameter("password");
            if (StringUtil.strToBoolean(req.getParameter("encrypt"))) {
                password = encryptPassword(password);
            }
            result = _rm.login(userid, password, req.getSession().getId()); // user connect
        } else if (action.equalsIgnoreCase("checkConnection")) {
            result = _rm.checkServiceConnection(handle) ? success :
                    fail("Invalid or disconnected session handle");
        } else if (action.equals("isValidUserSession")) {
            result = _rm.isValidUserSession(handle) ? success :
                    fail("Invalid or disconnected session handle");
        } else if (authorisedCustomFormAction(action, handle)) {
            result = doAction(action, req);
        } else if (_rm.checkServiceConnection(handle)) {
            result = doAction(action, req);
        } else
            throw new IOException("Invalid or disconnected session handle");

        // generate the output
        OutputStreamWriter outputWriter = ServletUtils.prepareResponse(res);
        ServletUtils.finalizeResponse(outputWriter, result);
    }


    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        doPost(req, res);
    }


    /**
     * Private Methods ******************************
     */

    private String doAction(String action, HttpServletRequest req) throws IOException {
        String result = fail("Unknown action: " + action);          // assume the worst
        String handle = req.getParameter("sessionHandle");
        String userid = req.getParameter("userid");
        String pid = req.getParameter("participantid");
        String itemid = req.getParameter("workitemid");
        String specid = req.getParameter("specidentifier");
        String specversion = req.getParameter("specversion");
        String specuri = req.getParameter("specuri");

        if (action.equals("getParticipantFromUserID")) {
            Participant p = _rm.getParticipantFromUserID(userid);
            result = (p != null) ? p.toXML() : fail("Unknown userid: " + userid);
        } else if (action.equals("getFullNameForUserID")) {
            String name = _rm.getFullNameForUserID(userid);
            result = (name != null) ? name : fail("Unknown userid: " + userid);
        } else if (action.equals("getUserPrivileges")) {
            Participant p = getOrgDataSet().getParticipant(pid);
            if (p != null) {
                UserPrivileges up = p.getUserPrivileges();
                result = (up != null) ? up.toXML() :
                        fail("No privileges available for participant id: " + pid);
            } else result = fail("Unknown participant id: " + pid);
        } else if (action.equals("getTaskPrivileges")) {
            TaskPrivileges privileges = _rm.getTaskPrivileges(itemid);
            result = (privileges != null) ? privileges.toXML(true) :
                    fail("Unknown workitem id: " + itemid);
        } else if (action.equals("getParticipantsReportingTo")) {
            Set<Participant> set = getOrgDataSet().getParticipantsReportingTo(pid);
            result = (set != null) ? _marshaller.marshallParticipants(set) :
                    fail("Invalid participant id or no participants reporting to: " + pid);
        } else if (action.equals("getOrgGroupMembers")) {
            String groupid = req.getParameter("groupid");
            OrgGroup og = getOrgDataSet().getOrgGroup(groupid);
            if (og != null) {
                Set<Participant> set = getOrgDataSet().getOrgGroupMembers(og); // set never null
                result = _marshaller.marshallParticipants(set);
            } else result = fail("Unknown org group id: " + groupid);
        } else if (action.equals("getRoleMembers")) {
            String rid = req.getParameter("roleid");
            if (getOrgDataSet().isKnownRole(rid)) {
                result = getOrgDataSet().getRoleParticipantsAsXML(rid);
            } else result = fail("Unknown role id: " + rid);
        } else if (action.equals("getParticipant")) {
            Participant p = getOrgDataSet().getParticipant(pid);
            result = (p != null) ? p.toXML() : fail("Unknown participant id: " + pid);
        } else if (action.equals("getParticipants")) {
            Set<Participant> set = getOrgDataSet().getParticipants();
            result = (set != null) ? _marshaller.marshallParticipants(set) :
                    fail("No participants found");
        } else if (action.equals("getDistributionSet")) {
            Set<Participant> set = _rm.getDistributionSet(itemid);
            result = (set != null) ? _marshaller.marshallParticipants(set) :
                    fail("No distribution set found");
        } else if (action.equals("getAdminQueues")) {
            QueueSet qSet = _rm.getAdminQueues();
            result = qSet.toXML();                                    // set never null
        } else if (action.equals("getParticipantQueues")) {
            Participant p = getOrgDataSet().getParticipant(pid);
            if (p != null) {
                QueueSet qSet = p.getWorkQueues();
                result = qSet.toXML();                                    // set never null
            } else result = fail("Unknown participant id: " + pid);
        } else if (action.equals("getWorkItem")) {
            result = _rm.getWorkItem(itemid);
        } else if (action.equals("getWorkItemChildren")) {
            Set<WorkItemRecord> children = _rm.getChildrenFromCache(itemid);
            result = (children != null) ? _marshaller.marshallWorkItemRecords(children) :
                    fail("No child items found for parent: " + itemid);
        } else if (action.equals("getWorkItemParameters")) {
            result = _rm.getTaskParamsAsXML(itemid);
        } else if (action.equals("getWorkItemOutputOnlyParameters")) {
            result = _rm.getOutputOnlyTaskParamsAsXML(itemid);
        } else if (action.equals("getWorkItemDataSchema")) {
            result = _rm.getDataSchema(itemid);
        } else if (action.equals("getCaseDataSchema")) {
            result = _rm.getDataSchema(new YSpecificationID(specid, specversion, specuri));
        } else if (action.equals("updateWorkItemData")) {
            String data = req.getParameter("data");
            result = _rm.updateWorkItemData(itemid, data);
        } else if (action.equals("getQueuedWorkItems")) {
            int queueType = getQueueType(req.getParameter("queue"));
            if (WorkQueue.isValidQueueType(queueType)) {
                Participant p = getOrgDataSet().getParticipant(pid);
                if (p != null) {
                    QueueSet qSet = p.getWorkQueues();
                    if (qSet != null) {
                        Set<WorkItemRecord> set = qSet.getQueuedWorkItems(queueType);
                        result = _marshaller.marshallWorkItemRecords(set);
                    } else
                        result = _marshaller.marshallWorkItemRecords((Set<WorkItemRecord>) null);
                } else result = fail("Unknown participant id: " + pid);
            } else result = fail("Invalid queue type: " + req.getParameter("queue"));
        } else if (action.equals("getParticipantsAssignedWorkItem")) {
            int queueType = getQueueType(req.getParameter("queue"));
            if (WorkQueue.isValidQueueType(queueType)) {
                Set<Participant> set = _rm.getParticipantsAssignedWorkItem(itemid, queueType);
                result = _marshaller.marshallParticipants(set);
            } else result = fail("Invalid queue type: " + req.getParameter("queue"));
        } else if (action.equals("getWorkItemDurationsForParticipant")) {
            String taskName = req.getParameter("taskname");
            YSpecificationID specID = new YSpecificationID(specid, specversion, specuri);
            result = _rm.getWorkItemDurationsForParticipant(specID, taskName, pid);
        } else if (action.equals("getLoadedSpecs")) {
            Set<SpecificationData> set = _rm.getLoadedSpecs();
            result = _marshaller.marshallSpecificationDataSet(set);
        } else if (action.equals("getSpecList")) {
            Set<SpecificationData> set = _rm.getSpecList();
            result = _marshaller.marshallSpecificationDataSet(set);
        } else if (action.equals("getSpecData")) {
            SpecificationData specData = _rm.getSpecData(
                    new YSpecificationID(specid, specversion, specuri));
            result = _marshaller.marshallSpecificationData(specData);
        } else if (action.equals("getRunningCases")) {
            result = _rm.getClients().getRunningCases(
                    new YSpecificationID(specid, specversion, specuri));
        } else if (action.equals("getDecompID")) {
            WorkItemRecord wir = _rm.getWorkItemCache().get(itemid);
            String decompID = _rm.getDecompID(wir);
            result = (decompID != null) ? decompID : fail("Unknown workitem: " + itemid);
        } else if (action.equals("getCaseData")) {
            String caseID = req.getParameter("caseid");
            result = _rm.getClients().getCaseData(caseID);
        } else if (action.equals("getRegisteredServices")) {
            result = _rm.getClients().getRegisteredServicesAsXML();
        } else if (action.equals("disconnect")) {
            _rm.serviceDisconnect(handle);
            result = success;
        } else if (action.equals("userlogout")) {
            _rm.logout(handle);
            result = success;
        } else if (action.equals("acceptOffer")) {
            result = doResourceAction(req, action);
        } else if (action.equals("startWorkItem")) {
            result = doResourceAction(req, action);
        } else if (action.equals("deallocateWorkItem")) {
            result = doResourceAction(req, action);
        } else if (action.equals("skipWorkItem")) {
            result = doResourceAction(req, action);
        } else if (action.equals("pileWorkItem")) {
            result = doResourceAction(req, action);
        } else if (action.equals("suspendWorkItem")) {
            result = doResourceAction(req, action);
        } else if (action.equals("unsuspendWorkItem")) {
            result = doResourceAction(req, action);
        } else if (action.equals("completeWorkItem")) {
            result = doResourceAction(req, action);
        } else if (action.equals("chainCase")) {
             result = doResourceAction(req, action);
        } else if (action.equals("offerWorkItem")) {
            result = doAdminQueueAction(req, action);
        } else if (action.equals("allocateWorkItem")) {
            result = doAdminQueueAction(req, action);
        } else if (action.equals("reofferWorkItem")) {
            result = doAdminQueueAction(req, action);
        } else if (action.equals("reallocateWorkItem")) {
            result = doAdminQueueAction(req, action);
        } else if (action.equals("restartWorkItem")) {
            result = doAdminQueueAction(req, action);
        } else if (action.equals("delegateWorkItem")) {
            result = doResourceMoveAction(req, action);
        } else if (action.equals("reallocateStatefulWorkItem")) {
            result = doResourceMoveAction(req, action);
        } else if (action.equals("reallocateStatelessWorkItem")) {
            result = doResourceMoveAction(req, action);
        } else if (action.equals("newInstance")) {
            String newValue = req.getParameter("newValue");
            WorkItemRecord wir = _rm.createNewWorkItemInstance(itemid, newValue);
            if (wir != null) {
                wir.setResourceStatus(WorkItemRecord.statusResourceUnoffered);
                result = response(wir.toXML());
            }
            else {
                result = fail("Failed to create new instance");
            }
        } else if (action.equals("updateWorkItemCache")) {
            String wirAsXML = req.getParameter("wir");
            WorkItemRecord wir = Marshaller.unmarshalWorkItem(wirAsXML);
            if (wir != null) {
                _rm.getWorkItemCache().update(wir);
                result = success;
            } else result = response(fail("Malformed or empty work item XML record"));
        } else if (action.equals("updateWorkQueuedItem")) {
            String wirAsXML = req.getParameter("wir");
            WorkItemRecord wir = Marshaller.unmarshalWorkItem(wirAsXML);
            if (wir != null) {
                Participant p = _rm.getOrgDataSet().getParticipant(pid);
                if (p != null) {
                    int queue = StringUtil.strToInt(req.getParameter("queue"), -1);       
                    if (queue >= WorkQueue.OFFERED && queue <= WorkQueue.SUSPENDED) {
                        _rm.getWorkItemCache().update(wir);
                        p.getWorkQueues().getQueue(queue).refresh(wir);
                        result = success;
                    } else result = fail("Invalid user queue type: " + queue);
                } else result = fail("Unknown participant: " + pid);
            } else result = fail("Malformed or empty work item XML record");
        }
        else if (action.equals("setWorkItemDocumentation")) {
           String docoText = req.getParameter("doco");
            WorkItemRecord wir = _rm.getWorkItemCache().get(itemid);
            if (! (wir == null || docoText == null || docoText.equals(wir.getDocumentation()))) {
                wir.setDocumentation(new YPredicateParser().parse(docoText));
                wir.setDocumentationChanged(true);
                _rm.getWorkItemCache().update(wir);
                for (Participant p : _rm.getParticipantsAssignedWorkItem(wir)) {
                    for (WorkQueue queue : p.getWorkQueues().getActiveQueues()) {
                         if (queue.get(itemid) != null) {
                             queue.refresh(wir);
                         }
                    }
                }
                result = success;
            } else result = fail("Unknown work item with id: " + itemid);
        }
        else if (action.equals("synchroniseCaches")) {
            _rm.sanitiseCaches();
            result = success;
        } else if (action.equals("redirectWorkItemToYawlService")) {
            String serviceName = req.getParameter("serviceName");
            result = _rm.redirectWorkItemToYawlService(itemid, serviceName);
        }
        else if (action.equals("addResourceEventListener")) {
            String uri = req.getParameter("uri");
            result = _rm.addEventListener(uri);
        }
        else if (action.equals("removeResourceEventListener")) {
            String uri = req.getParameter("uri");
            _rm.removeEventListener(uri);
            result = success;
        }
        else if (action.equals("getChainedCases")) {
           Participant p = getOrgDataSet().getParticipant(pid);
           if (p != null) {
               Set<String> cases = _rm.getChainedCases(p);
               XNode root = new XNode("chainedcases");
               for (String item : cases) {
                   root.addChild("item", item);
               }
               result = root.toString();
           }
           else result = fail("Unknown participant: " + pid);
        }
        else if (action.equals("getPiledItems")) {
            Participant p = getOrgDataSet().getParticipant(pid);
            if (p != null) {
                XNode root = new XNode("piledTasks");
                Set<ResourceMap> taskMaps = _rm.getPiledTaskMaps(p);
                for (ResourceMap map : taskMaps) {
                    XNode taskNode = root.addChild("task");
                    taskNode.addChild(map.getSpecID().toXNode());
                    taskNode.addChild("taskid", map.getTaskID());
                }
                result = root.toString();
            }
            else result = fail("Unknown participant: " + pid);
        }
        else if (action.equals("unchainCase")) {
            String caseID = req.getParameter("caseid");
            if (caseID != null) {
                _rm.removeChain(caseID);
                result = success;
            }
            else {
                result = fail("Missing case id parameter");
            }
        }
        else if (action.equals("unpileTask")) {
            YSpecificationID specID = new YSpecificationID(specid, specversion, specuri);
            String taskID = req.getParameter("taskid");
            ResourceMap map = _rm.getCachedResourceMap(specID, taskID);
            if (map != null) {
                Participant p = getOrgDataSet().getParticipant(pid);
                result = _rm.unpileTask(map, p);
            }
            else {
                result = fail("Unknown specification or task");
            }
        }


        // the following calls are convenience pass-throughs to engine interfaces A & B

        else if (action.equals("uploadSpecification")) {
            String fileContents = req.getParameter("fileContents");
            String fileName = req.getParameter("fileName");
            result = response(_rm.getClients().uploadSpecification(fileContents, fileName));
        } else if (action.equals("unloadSpecification")) {
            result = _rm.unloadSpecification(new YSpecificationID(specid, specversion, specuri));
        } else if (action.equals("launchCase")) {
            String caseData = req.getParameter("casedata");
            result = response(_rm.launchCase(new YSpecificationID(specid, specversion, specuri),
                    caseData, handle));
        } else if (action.equals("cancelCase")) {
            String caseID = req.getParameter("caseid");
            result = response(_rm.cancelCase(caseID, handle));
        } else if (action.equals("removeRegisteredService")) {
            String id = req.getParameter("serviceid");
            result = response(_rm.removeRegisteredService(id));
        } else if (action.equals("addRegisteredService")) {
            String uri = req.getParameter("uri");
            String name = req.getParameter("name");
            String pw = req.getParameter("password");
            String doco = req.getParameter("doco");
            String assignable = req.getParameter("assignable");
            YAWLServiceReference ysr = new YAWLServiceReference(uri, null, name, pw, doco);
            if (assignable != null) {
                ysr.set_assignable(assignable.equals("true"));
            }
            result = response(_rm.addRegisteredService(ysr));
        } else if (action.equals("addExternalClient")) {
            String name = req.getParameter("name");
            String pw = req.getParameter("password");
            String doco = req.getParameter("doco");
            result = response(_rm.addExternalClient(new YExternalClient(name, pw, doco)));
        } else if (action.equals("removeExternalClient")) {
            String id = req.getParameter("name");
            result = response(_rm.removeExternalClient(id));
        }

        return result;
    }

    // returns true if the action can be performed without admin-level privileges
    private boolean authorisedCustomFormAction(String action, String handle) {
        return (action.equals("getWorkItem") ||
                action.equals("getWorkItemParameters") ||
                action.equals("updateWorkItemData") ||
                action.equals("getWorkItemOutputOnlyParameters")) &&
                _rm.isValidUserSession(handle);
    }


    private String doResourceAction(HttpServletRequest req, String action) {
        String result;
        String pid = req.getParameter("participantid");
        String itemid = req.getParameter("workitemid");

        Participant p = getOrgDataSet().getParticipant(pid);
        if (p != null) {
            WorkItemRecord wir = _rm.getWorkItemCache().get(itemid);
            if (wir != null) {
                result = doResourceAction(p, wir, req, action);
            } else result = fail("Unknown workitem: " + itemid);
        } else result = fail("Unknown participant: " + pid);

        return response(result);
    }


    private String doAdminQueueAction(HttpServletRequest req, String action) {
        String result;
        String[] pids = xmlToArray(req.getParameter("participantids"));

        if (pids != null) {
            String itemid = req.getParameter("workitemid");
            WorkItemRecord wir = _rm.getWorkItemCache().get(itemid);
            if (wir != null) {
                action = StringUtil.capitalise(action.substring(0, action.indexOf('W')));
                if (action.startsWith("Re")) {
                    _rm.reassignWorklistedItem(wir, pids, action);
                    result = success;
                } else {
                    if (wir.hasResourceStatus(WorkItemRecord.statusResourceUnoffered)) {
                        result = _rm.assignUnofferedItem(wir, pids, action) ? success :
                                fail("Could not " + action + " workitem: " + itemid);
                    } else {
                        action += action.equals("Allocate") ? "d" : "ed";
                        result = fail("Unoffered", action, wir);
                    }
                }
            } else result = fail("Unknown workitem: " + itemid);
        } else result = fail("Missing or empty offer set.");

        return response(result);
    }


    private String doResourceMoveAction(HttpServletRequest req, String action) {
        String result;
        String pFrom = req.getParameter("pfrom");
        String pTo = req.getParameter("pto");
        String itemid = req.getParameter("workitemid");

        Participant pOrig = getOrgDataSet().getParticipant(pFrom);
        if (pOrig != null) {
            Participant pDest = getOrgDataSet().getParticipant(pTo);
            if (pDest != null) {
                WorkItemRecord wir = _rm.getWorkItemCache().get(itemid);
                if (wir != null) {
                    result = doResourceAction(pOrig, pDest, wir, action);
                } else result = fail("Unknown workitem: " + itemid);
            } else result = fail("Unknown destination participant: " + pDest);
        } else result = fail("Unknown source participant: " + pFrom);

        return result;
    }


    private String doResourceAction(Participant p, WorkItemRecord wir,
                                    HttpServletRequest req, String action) {
        String result = "<failure/>";
        boolean successful;

        if (action.equals("acceptOffer")) {
            if (wir.hasResourceStatus(WorkItemRecord.statusResourceOffered)) {
                _rm.acceptOffer(p, wir);
                result = success;
            } else result = fail("Offered", "accepted", wir);
        } else if (action.equals("startWorkItem")) {
            if (wir.hasResourceStatus(WorkItemRecord.statusResourceUnoffered) ||
                    wir.hasResourceStatus(WorkItemRecord.statusResourceOffered) ||
                    wir.hasResourceStatus(WorkItemRecord.statusResourceAllocated)) {

                // if offered, accept offer first (equivalent of an accept & start)
                if (wir.hasResourceStatus(WorkItemRecord.statusResourceOffered)) {
                    _rm.acceptOffer(p, wir);
                }

                if (_rm.start(p, wir)) {
                    WorkItemRecord child;
                    switch (wir.getStatus()) {
                        case WorkItemRecord.statusFired :  // MI instance - refresh
                        //    try {
                            //    child = _rm.getEngineStoredWorkItem(wir.getID());
                                child = _rm.getCachedWorkItem(wir.getID());
//                            }
//                            catch (IOException ioe) {
//                                return ioe.getMessage();
//                            }
                            break;
                        case WorkItemRecord.statusExecuting :  // already is the child
                            child = wir; break;
                        default :
                            child = _rm.getExecutingChild(wir); break;
                    }
                    
                    if (child != null) {
                        child.setResourceStatus(WorkItemRecord.statusResourceStarted);
                        result = child.toXML();
                    } else result = fail("Workitem '" + wir.getID() +
                            "' has started, but could not retrieve its executing child.");
                } else result = fail("Could not start workitem: " + wir.getID());
            } else result = fail("Unoffered', 'Offered' or 'Allocated", "started", wir);
        } else if (action.equals("completeWorkItem")) {
            if (wir.hasResourceStatus(WorkItemRecord.statusResourceStarted)) {
                result = _rm.checkinItem(p, wir);
            } else result = fail("Started", "completed", wir);
        } else if (action.equals("deallocateWorkItem")) {
            if (wir.hasResourceStatus(WorkItemRecord.statusResourceAllocated)) {
                successful = _rm.deallocateWorkItem(p, wir);
                result = successful ? success : fail("Could not deallocate workitem: " +
                        wir.getID());
            } else result = fail("Allocated", "deallocated", wir);
        } else if (action.equals("skipWorkItem")) {
            if (wir.hasResourceStatus(WorkItemRecord.statusResourceAllocated)) {
                successful = _rm.skipWorkItem(p, wir);
                result = successful ? success : fail("Could not skip workitem: " + wir.getID());
            } else result = fail("Allocated", "skipped", wir);
        } else if (action.equals("pileWorkItem")) {
            if (wir.hasResourceStatus(WorkItemRecord.statusResourceAllocated)) {
                result = _rm.pileWorkItem(p, wir);
                if (result.startsWith("Cannot")) result = fail(result);
            } else result = fail("Allocated", "piled", wir);
        } else if (action.equals("suspendWorkItem")) {
            if (wir.hasResourceStatus(WorkItemRecord.statusResourceStarted)) {
                successful = _rm.suspendWorkItem(p, wir);
                result = successful ? success : fail("Could not suspend workitem: " +
                        wir.getID());
            } else result = fail("Started", "suspended", wir);
        } else if (action.equals("unsuspendWorkItem")) {
            if (wir.hasResourceStatus(WorkItemRecord.statusResourceSuspended)) {
                successful = _rm.unsuspendWorkItem(p, wir);
                result = successful ? success : fail("Could not unsuspend workitem: " +
                        wir.getID());
            }
            else result = fail("Suspended", "unsuspended", wir);
        } else if (action.equals("chainCase")) {
            if (wir.hasResourceStatus(WorkItemRecord.statusResourceOffered)) {
                result = _rm.chainCase(p, wir);
            } else result = fail("Offered", "chained", wir);
        }
        return result;
    }


    private String doResourceAction(Participant pOrig, Participant pDest,
                                    WorkItemRecord wir, String action) {
        String result = "<failure/>";
        boolean successful;

        if (action.equals("delegateWorkItem")) {
            if (wir.hasResourceStatus(WorkItemRecord.statusResourceAllocated)) {
                successful = _rm.delegateWorkItem(pOrig, pDest, wir);
                result = successful ? success : fail("Could not delegate workitem: " +
                        wir.getID());
            } else result = fail("Allocated", "delegated", wir);
        } else if (action.equals("reallocateStatefulWorkItem")) {
            if (wir.hasResourceStatus(WorkItemRecord.statusResourceStarted)) {
                successful = _rm.reallocateStatefulWorkItem(pOrig, pDest, wir);
                result = successful ? success : fail("Could not reallocate workitem: " +
                        wir.getID());
            } else result = fail("Started", "reallocated", wir);
        } else if (action.equals("reallocateStatelessWorkItem")) {
            if (wir.hasResourceStatus(WorkItemRecord.statusResourceStarted)) {
                successful = _rm.reallocateStatelessWorkItem(pOrig, pDest, wir);
                result = successful ? success : fail("Could not reallocate workitem: " +
                        wir.getID());
            } else result = fail("Started", "reallocated", wir);
        }
        return response(result);
    }


    private ResourceDataSet getOrgDataSet() {
        while (_rm.isOrgDataRefreshing()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ie) {
                // deliberately do nothing
            }
        }
        return _rm.getOrgDataSet();
    }


    private int getQueueType(String queue) {
        try {
            return new Integer(queue);
        } catch (NumberFormatException nfe) {
            return -1;
        }
    }


    private String[] xmlToArray(String xml) {
        String[] items = null;
        if (xml != null) {
            XNode node = new XNodeParser().parse(xml);
            if (node != null) {
                items = new String[node.getChildCount()];
                int i = 0;
                for (XNode idNode : node.getChildren()) {
                    items[i++] = idNode.getText();
                }
            }
        }
        return items;
    }


    private String fail(String reqStatus, String action, WorkItemRecord wir) {
        return fail(reqStatus, action, wir.getID(), wir.getResourceStatus());
    }


    private String fail(String reqStatus, String action, String itemid, String hasStatus) {
        return fail(String.format(
                "Only a workitem with '%s' status can be %s. Workitem '%s' has '%s' status.",
                reqStatus, action, itemid, hasStatus));
    }

}
