/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.QueueSet;
import org.yawlfoundation.yawl.resourcing.TaskPrivileges;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.UserPrivileges;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * This adapter class adds a transformation layer to the resource gateway client,
 * effectively reconstituting the Strings returned from the gateway into java objects.
 * <p/>
 * Author: Michael Adams
 * Date: 26/10/2007
 * Version: 2.0
 */

public class WorkQueueGatewayClientAdapter {

    protected WorkQueueGatewayClient _wqclient;        // the gateway client
    protected String _uri;                            // the uri of the service gateway

    protected ResourceMarshaller _marshaller = new ResourceMarshaller();

    // CONSTRUCTORS //

    public WorkQueueGatewayClientAdapter() {}

    public WorkQueueGatewayClientAdapter(String uri) { setClientURI(uri); }


    // GETTER & SETTER //

    public void setClientURI(String uri) {
        _uri = uri;
        _wqclient = new WorkQueueGatewayClient(uri);
    }

    public String getClientURI() { return _uri; }

    public WorkQueueGatewayClient getClient() { return _wqclient; }


    public boolean successful(String result) {
        return (result != null) && (!result.startsWith("<failure>"));
    }


    protected String successCheck(String xml) throws ResourceGatewayException {
        if (successful(xml)) {
            return xml;
        } else throw new ResourceGatewayException(xml);
    }


    /*****************************************************************************/

    // PUBLIC METHODS //

    /**
     * Checks that the connection to the service is valid
     *
     * @param handle the current sessionhandle
     * @return true if the connection is valid, false if otherwise
     */
    public boolean checkConnection(String handle) {
        try {
            return successful(_wqclient.checkConnection(handle));
        } catch (IOException ioe) {
            return false;
        }
    }


    /**
     * Attempts to connect to the service (as a service)
     *
     * @param userid   the userid
     * @param password the corresponding password
     * @return a sessionhandle if successful, or a failure message if otherwise
     */
    public String connect(String userid, String password) {
        try {
            return _wqclient.connect(userid, password);
        } catch (IOException ioe) {
            return "<failure>IOException attempting to connect to Service.</failure>";
        }
    }


    /**
     * Attempts to connect to the service (as a user/participant)
     *
     * @param userid   the userid
     * @param password the corresponding password
     * @return a sessionhandle if successful, or a failure message if otherwise
     */
    public String userlogin(String userid, String password) {
        return userlogin(userid, password, true);
    }


    /**
     * Logs out a user session (as opposed to a service connection)
     *
     * @param handle the session handle
     * @return a success message
     * @throws IOException
     */
    public String userlogout(String handle) {
        try {
            return _wqclient.userlogout(handle);
        } catch (IOException ioe) {
            return "<failure>";
        }
    }


    /**
     * Attempts to connect to the service (as a user/participant)
     *
     * @param userid   the userid
     * @param password the corresponding password
     * @param encrypt  true if encryption is required (most cases); false if the
     *                 password is already encrypted
     * @return a sessionhandle if successful, or a failure message if otherwise
     */
    public String userlogin(String userid, String password, boolean encrypt) {
        try {
            return _wqclient.userlogin(userid, password, encrypt);
        } catch (IOException ioe) {
            return "<failure>IOException attempting to connect to Service.</failure>";
        }
    }


    /**
     * Disconnects a session from the service
     *
     * @param handle the sessionhandle of the session to disconnect
     */
    public void disconnect(String handle) {
        try {
            _wqclient.disconnect(handle);
        } catch (IOException ioe) {
        } // nothing to do
    }


    public boolean isValidUserSession(String handle) throws IOException {
        return successful(_wqclient.isValidUserSession(handle));
    }


    public Participant getParticipantFromUserID(String userid, String handle)
            throws IOException, ResourceGatewayException {
        String xml = _wqclient.getParticipantFromUserID(userid, handle);
        return _marshaller.unmarshallParticipant(successCheck(xml));
    }


    public String getFullNameForUserID(String userid, String handle)
            throws IOException, ResourceGatewayException {
        return successCheck(_wqclient.getFullNameForUserID(userid, handle));
    }


    public UserPrivileges getUserPrivileges(String pid, String handle)
            throws IOException, ResourceGatewayException {
        String xml = _wqclient.getUserPrivileges(pid, handle);
        successCheck(xml);
        UserPrivileges result = new UserPrivileges();
        result.fromXML(xml);
        return result;
    }


    public TaskPrivileges getTaskPrivileges(String itemid, String handle)
            throws IOException, ResourceGatewayException {
        String xml = _wqclient.getTaskPrivileges(itemid, handle);
        XNode root = new XNodeParser().parse(successCheck(xml));
        XNode specidNode = root.getChild("specid");
        YSpecificationID specid = new YSpecificationID(specidNode.getChildText("identifier"),
                specidNode.getChildText("version"), specidNode.getChildText("uri"));

        TaskPrivileges taskPrivileges = new TaskPrivileges(specid, root.getChildText("taskid"));

        for (XNode privilege : root.getChild("privileges").getChildren()) {
            String name = privilege.getChildText("name");
            String allowallStr = privilege.getChildText("allowall");
            if (allowallStr != null) {
                if (allowallStr.equals("true")) {
                    taskPrivileges.allowAll(name);
                } else taskPrivileges.disallowAll(name);
            }

            XNode set = privilege.getChild("set");
            if (set != null) {
                Map<String, Participant> pMap = new Hashtable<String, Participant>();
                for (XNode pNode : set.getChildren("participant")) {
                    String pid = pNode.getText();
                    Participant p = pMap.get(pid);
                    if (p == null) {
                        p = getParticipant(pid, handle);
                        if (p != null) {
                            pMap.put(pid, p);
                        }
                    }
                    if (p != null) {
                        taskPrivileges.grant(name, p);
                    }
                }
                for (XNode rNode : set.getChildren("role")) {
                    String rid = rNode.getText();
                    if (rid != null) {
                        Set<Participant> pSet = getRoleMembers(rid, handle);
                        if (pSet != null) {
                            taskPrivileges.grant(name, pSet);
                        }
                    }
                }
            }
        }
        return taskPrivileges;
    }


    public Set<Participant> getReportingToParticipant(String pid, String handle)
            throws IOException, ResourceGatewayException {
        String xml = _wqclient.getReportingToParticipant(pid, handle);
        return _marshaller.unmarshallParticipants(successCheck(xml));
    }


    public Set<Participant> getOrgGroupMembers(String oid, String handle)
            throws IOException, ResourceGatewayException {
        String xml = _wqclient.getOrgGroupMembers(oid, handle);
        return _marshaller.unmarshallParticipants(successCheck(xml));
    }


    public Set<Participant> getRoleMembers(String rid, String handle)
            throws IOException, ResourceGatewayException {
        String xml = _wqclient.getRoleMembers(rid, handle);
        return _marshaller.unmarshallParticipants(successCheck(xml));
    }


    public Participant getParticipant(String pid, String handle)
            throws IOException, ResourceGatewayException {
        String xml = _wqclient.getParticipant(pid, handle);
        return _marshaller.unmarshallParticipant(successCheck(xml));
    }


    public Set<Participant> getAllParticipants(String handle)
            throws IOException, ResourceGatewayException {
        String xml = _wqclient.getAllParticipants(handle);
        return _marshaller.unmarshallParticipants(successCheck(xml));
    }


    public Set<Participant> getDistributionSet(String itemID, String handle)
            throws IOException, ResourceGatewayException {
        String xml = _wqclient.getDistributionSet(itemID, handle);
        return _marshaller.unmarshallParticipants(successCheck(xml));
    }


    /**
     * *************************************************************************
     */

    public QueueSet getAdminQueues(String handle)
            throws IOException, ResourceGatewayException {
        String xml = _wqclient.getAdminQueues(handle);
        successCheck(xml);
        QueueSet result = new QueueSet("admin", QueueSet.setType.adminSet, false);
        result.fromXML(xml);
        return result;
    }


    public Set<WorkItemRecord> getQueuedWorkItems(String pid, int queue, String handle)
            throws IOException, ResourceGatewayException {
        String xml = _wqclient.getQueuedWorkItems(pid, queue, handle);
        return _marshaller.unmarshallWorkItemRecords(successCheck(xml));
    }


    public String getWorkItem(String itemID, String handle)
            throws IOException, ResourceGatewayException {
        return successCheck(_wqclient.getWorkItem(itemID, handle));
    }


    public Set<WorkItemRecord> getWorkItemChildren(String itemID, String handle)
            throws IOException, ResourceGatewayException {
        String xml = _wqclient.getWorkItemChildren(itemID, handle);
        return _marshaller.unmarshallWorkItemRecords(successCheck(xml));
    }


    public Set<YParameter> getWorkItemParameters(String itemID, String handle)
            throws IOException, ResourceGatewayException {
        String result = successCheck(_wqclient.getWorkItemParameters(itemID, handle));
        return _marshaller.parseWorkItemParams(result);
    }


    public Set<YParameter> getWorkItemOutputOnlyParameters(String itemID, String handle)
            throws IOException, ResourceGatewayException {
        String result = successCheck(_wqclient.getWorkItemOutputOnlyParameters(itemID, handle));
        return _marshaller.parseWorkItemParams(result);
    }


    public String getWorkItemDataSchema(String itemID, String handle)
            throws IOException, ResourceGatewayException {
        return successCheck(_wqclient.getWorkItemDataSchema(itemID, handle));
    }


    public String getCaseDataSchema(YSpecificationID specID, String handle)
            throws IOException, ResourceGatewayException {
        return successCheck(_wqclient.getCaseDataSchema(specID, handle));
    }


    public boolean synchroniseCaches(String handle) throws IOException {
        return successful(_wqclient.synchroniseCaches(handle));
    }


    public String updateWorkItemData(String itemID, String data, String handle)
            throws IOException, ResourceGatewayException {
        return successCheck(_wqclient.updateWorkItemData(itemID, data, handle));
    }


    public Set<Participant> getParticipantsAssignedWorkItem(String workItemID,
                                                            int queueType, String handle)
            throws IOException, ResourceGatewayException {
        String xml = _wqclient.getParticipantsAssignedWorkItem(workItemID,
                queueType, handle);
        return _marshaller.unmarshallParticipants(successCheck(xml));
    }


    public String acceptOffer(String pid, String itemID, String handle)
            throws IOException, ResourceGatewayException {
        return successCheck(_wqclient.acceptOffer(pid, itemID, handle));
    }


    public WorkItemRecord startItem(String pid, String itemID, String handle)
            throws IOException, ResourceGatewayException {
        String xml = successCheck(_wqclient.startItem(pid, itemID, handle));
        return _marshaller.unmarshallWorkItemRecord(xml);
    }


    public String deallocateItem(String pid, String itemID, String handle)
            throws IOException, ResourceGatewayException {
        return successCheck(_wqclient.deallocateItem(pid, itemID, handle));
    }


    public String delegateItem(String pFrom, String pTo, String itemID, String handle)
            throws IOException, ResourceGatewayException {
        return successCheck(_wqclient.delegateItem(pFrom, pTo, itemID, handle));
    }


    public String reallocateItem(String pFrom, String pTo, String itemID,
                                 boolean stateful, String handle)
            throws IOException, ResourceGatewayException {
        return successCheck(_wqclient.reallocateItem(pFrom, pTo, itemID, stateful, handle));
    }


    public String skipItem(String pid, String itemID, String handle)
            throws IOException, ResourceGatewayException {
        return successCheck(_wqclient.skipItem(pid, itemID, handle));
    }


    public String pileItem(String pid, String itemID, String handle)
            throws IOException, ResourceGatewayException {
        return successCheck(_wqclient.pileItem(pid, itemID, handle));
    }


    public String suspendItem(String pid, String itemID, String handle)
            throws IOException, ResourceGatewayException {
        return successCheck(_wqclient.suspendItem(pid, itemID, handle));
    }

    public String unsuspendItem(String pid, String itemID, String handle)
            throws IOException, ResourceGatewayException {
        return successCheck(_wqclient.unsuspendItem(pid, itemID, handle));
    }

    public String completeItem(String pid, String itemID, String handle)
            throws IOException, ResourceGatewayException {
        return successCheck(_wqclient.completeItem(pid, itemID, handle));
    }

    public String offerItem(Set<String> pid, String itemID, String handle)
            throws IOException, ResourceGatewayException {
        return successCheck(_wqclient.offerItem(pid, itemID, handle));
    }

    public String reofferItem(Set<String> pid, String itemID, String handle)
            throws IOException, ResourceGatewayException {
        return successCheck(_wqclient.reofferItem(pid, itemID, handle));
    }

    public String allocateItem(String pid, String itemID, String handle)
            throws IOException, ResourceGatewayException {
        return successCheck(_wqclient.allocateItem(pid, itemID, handle));
    }

    public String reallocateItem(String pid, String itemID, String handle)
            throws IOException, ResourceGatewayException {
        return successCheck(_wqclient.reallocateItem(pid, itemID, handle));
    }

    public String restartItem(String pid, String itemID, String handle)
            throws IOException, ResourceGatewayException {
        return successCheck(_wqclient.restartItem(pid, itemID, handle));
    }


    public String redirectWorkItemToYawlService(String itemID, String serviceName,
                                                String handle)
            throws IOException, ResourceGatewayException {
        return successCheck(_wqclient.redirectWorkItemToYawlService(itemID,
                serviceName, handle));
    }

    /**
     * ****************************************************************************
     */

    // SPEC AND INSTANCE ACTIONS //
    public Set<SpecificationData> getLoadedSpecs(String handle) throws IOException {
        String xml = _wqclient.getLoadedSpecs(handle);
        return _marshaller.unmarshallSpecificationDataSet(xml);
    }


    public Set<SpecificationData> getSpecList(String handle) throws IOException {
        String xml = _wqclient.getSpecList(handle);
        return _marshaller.unmarshallSpecificationDataSet(xml);
    }


    public SpecificationData getSpecData(YSpecificationID specID, String handle)
            throws IOException {
        String xml = _wqclient.getSpecData(specID, handle);
        return _marshaller.unmarshallSpecificationData(xml);
    }


    public String uploadSpecification(String fileContents, String fileName, String handle)
            throws IOException {
        return _wqclient.uploadSpecification(fileContents, fileName, handle);
    }


    public String unloadSpecification(YSpecificationID specID, String handle)
            throws IOException {
        return _wqclient.unloadSpecification(specID, handle);
    }


    public String launchCase(YSpecificationID specID, String caseData, String handle)
            throws IOException {
        return _wqclient.launchCase(specID, caseData, handle);
    }


    public String getRunningCases(YSpecificationID specID, String handle) throws IOException {
        return _wqclient.getRunningCases(specID, handle);
    }


    public String cancelCase(String caseID, String handle) throws IOException {
        return _wqclient.cancelCase(caseID, handle);
    }


    public void updateWIRCache(WorkItemRecord wir, String handle) throws IOException {
        _wqclient.updateWIRCache(wir, handle);
    }


    public String getDecompID(WorkItemRecord wir, String handle) throws IOException {
        return _wqclient.getDecompID(wir, handle);
    }


    public String getCaseData(String caseID, String handle) throws IOException {
        return _wqclient.getCaseData(caseID, handle);
    }


    public String getWorkItemDurationsForParticipant(YSpecificationID specID,
                                                     String taskName, String pid, String handle)
            throws IOException {
        return _wqclient.getWorkItemDurationsForParticipant(specID, taskName, pid, handle);
    }


    /**
     * ****************************************************************************
     */

    // REGISTERED SERVICE INFO //
    public Set<YAWLServiceReference> getRegisteredServices(String handle) throws IOException {
        String xml = _wqclient.getRegisteredServices(handle);
        return _marshaller.unmarshallServices(xml);
    }


    public String removeRegisteredService(String id, String handle) throws IOException {
        return _wqclient.removeRegisteredService(id, handle);
    }


    public String addRegisteredService(YAWLServiceReference service, String handle)
            throws IOException {
        return _wqclient.addRegisteredService(service.getURI(), service.get_serviceName(),
                service.getPassword(), service.getDocumentation(), service.isAssignable(),
                handle);
    }


    public boolean addResourceEventListener(String uri, String handle) throws IOException {
        return successful(_wqclient.addResourceEventListener(uri, handle));
    }


    public boolean removeResourceEventListener(String uri, String handle) throws IOException {
        return successful(_wqclient.removeResourceEventListener(uri, handle));
    }

}