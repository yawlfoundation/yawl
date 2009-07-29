/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.rsInterface;

import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.QueueSet;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.UserPrivileges;
import org.yawlfoundation.yawl.resourcing.util.PasswordEncryptor;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

/**
 * This adapter class adds a layer to the resource gateway client, effectively
 * reconstituting the Strings returned from the gateway into java objects.
 *
 * Author: Michael Adams
 * Date: 26/10/2007
 * Version: 2.0
 *
 */

public class WorkQueueGatewayClientAdapter {

    protected WorkQueueGatewayClient _wqclient;        // the gateway client
    protected String _uri ;                            // the uri of the service gateway

    protected ResourceMarshaller _marshaller = new ResourceMarshaller();

    // CONSTRUCTORS //

    public WorkQueueGatewayClientAdapter() {}

    public WorkQueueGatewayClientAdapter(String uri) { setClientURI(uri) ; }


    // GETTER & SETTER //

    public void setClientURI(String uri) {
        _uri = uri ;
        _wqclient = new WorkQueueGatewayClient(uri) ;
    }

    public String getClientURI() { return _uri ; }


    public boolean successful(String result) {
        return (result != null) && (! result.startsWith("<failure>"));
    }


    private String successCheck(String xml) throws ResourceGatewayException {
        if (successful(xml)) {
            return xml;
        }
        else throw new ResourceGatewayException(xml);
    }

    
    /*****************************************************************************/

    // PUBLIC METHODS //

    /**
     * Checks that the connection to the service is valid
     * @param handle the current sessionhandle
     * @return true if the connection is valid, false if otherwise
     */
    public boolean checkConnection(String handle) {
        try {
            return successful(_wqclient.checkConnection(handle)) ;
        }
        catch (IOException ioe) { return false; }
    }


    /**
     * Attempts to connect to the service (as a service)
     * @param userid the userid
     * @param password  the corresponding password
     * @return a sessionhandle if successful, or a failure message if otherwise
     */
    public String connect(String userid, String password) {
        try {
            return _wqclient.connect(userid, password) ;
        }
        catch (IOException ioe) {
            return "<failure>IOException attempting to connect to Service.</failure>";
        }
    }


    /**
     * Attempts to connect to the service (as a user/participant)
     * @param userid the userid
     * @param password  the corresponding password
     * @return a sessionhandle if successful, or a failure message if otherwise
     */
    public String userlogin(String userid, String password) {
        try {
            return _wqclient.userlogin(userid, PasswordEncryptor.encrypt(password)) ;
        }
        catch (IOException ioe) {
            return "<failure>IOException attempting to connect to Service.</failure>";
        }
        catch (NoSuchAlgorithmException nsae) {
            return "<failure>Could not encrypt password.</failure>";
        }
    }


    /**
     * Disconnects a session from the service
     * @param handle the sessionhandle of the session to disconnect
     */
    public void disconnect(String handle) {
        try {
            _wqclient.disconnect(handle);
        }
        catch (IOException ioe) { } // nothing to do
    }


    public boolean isValidUserSession(String handle) throws IOException {
        return successful(_wqclient.isValidUserSession(handle));
    }


    public Participant getParticipantFromUserID(String userid, String handle)
            throws IOException, ResourceGatewayException {
        String xml = _wqclient.getParticipantFromUserID(userid, handle) ;
        successCheck(xml);
        Participant result = new Participant() ;
        result.fromXML(xml);
        return result;
    }


    public String getFullNameForUserID(String userid, String handle)
            throws IOException, ResourceGatewayException {
        return successCheck(_wqclient.getFullNameForUserID(userid, handle));
    }


    public UserPrivileges getUserPrivileges(String pid, String handle)
            throws IOException, ResourceGatewayException {
        String xml = _wqclient.getUserPrivileges(pid, handle);
        successCheck(xml);
        UserPrivileges result = new UserPrivileges() ;
        result.fromXML(xml);
        return result;
    }


    public Set<Participant> getReportingToParticipant(String pid, String handle)
            throws IOException, ResourceGatewayException {
        String xml = _wqclient.getReportingToParticipant(pid, handle) ;
        return _marshaller.unmarshallParticipants(successCheck(xml)) ;
    }


    public Set<Participant> getOrgGroupMembers(String oid, String handle)
            throws IOException, ResourceGatewayException {
        String xml = _wqclient.getOrgGroupMembers(oid, handle) ;
        return _marshaller.unmarshallParticipants(successCheck(xml)) ;
    }


    public Participant getParticipant(String pid, String handle)
            throws IOException, ResourceGatewayException {
        String xml = _wqclient.getParticipant(pid, handle) ;
        successCheck(xml);
        Participant result = new Participant() ;
        result.fromXML(xml);
        return result;
    }


    public Set<Participant> getAllParticipants(String handle)
            throws IOException, ResourceGatewayException {
        String xml = _wqclient.getAllParticipants(handle) ;
        return _marshaller.unmarshallParticipants(successCheck(xml)) ;
    }


    /*****************************************************************************/

    public QueueSet getAdminQueues(String handle)
            throws IOException, ResourceGatewayException {
        String xml = _wqclient.getAdminQueues(handle) ;
        successCheck(xml);
        QueueSet result = new QueueSet("admin", QueueSet.setType.adminSet, false) ;
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

    public Set<YParameter> getWorkItemParameters(String itemID, String handle)
            throws IOException, ResourceGatewayException {
        String result = successCheck(_wqclient.getWorkItemParameters(itemID, handle));
        return _marshaller.parseWorkItemParams(result);
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
                                                               queueType, handle) ;
        return _marshaller.unmarshallParticipants(successCheck(xml)) ;
    }


    public String acceptOffer(String pid, String itemID, String handle)
            throws IOException, ResourceGatewayException {
        return successCheck(_wqclient.acceptOffer(pid, itemID, handle));
    }

    
    public String startItem(String pid, String itemID, String handle)
            throws IOException, ResourceGatewayException {
        return successCheck(_wqclient.startItem(pid, itemID, handle));
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

    /********************************************************************************/

     // SPEC AND INSTANCE ACTIONS //

    public Set<SpecificationData> getLoadedSpecs(String handle) throws IOException {
        String xml = _wqclient.getLoadedSpecs(handle);
        return _marshaller.unmarshallSpecificationDataSet(xml);
    }


    public Set<SpecificationData> getSpecList(String handle) throws IOException {
        String xml = _wqclient.getSpecList(handle);
        return _marshaller.unmarshallSpecificationDataSet(xml);
    }


    public SpecificationData getSpecData(String specID, String version, String handle)
            throws IOException {
        String xml = _wqclient.getSpecData(specID, version, handle);
        return _marshaller.unmarshallSpecificationData(xml);
    }


    public String uploadSpecification(String fileContents, String fileName, String handle)
                                                                   throws IOException {
        return _wqclient.uploadSpecification(fileContents, fileName, handle) ;
    }


    public String unloadSpecification(String specID, String version, String handle)
            throws IOException {
        return _wqclient.unloadSpecification(specID, version, handle) ;
    }


    public String launchCase(String specID, String version, String caseData, String handle)
                                                                    throws IOException {
        return _wqclient.launchCase(specID, version, caseData, handle);
    }


    public String getRunningCases(String specID, String version, String handle) throws IOException {
        return _wqclient.getRunningCases(specID, version, handle) ;
    }


    public String cancelCase(String caseID, String handle) throws IOException {
        return _wqclient.cancelCase(caseID, handle) ;
    }


    public void updateWIRCache(WorkItemRecord wir, String handle) throws IOException {
        _wqclient.updateWIRCache(wir, handle);
    }


    public String getDecompID(WorkItemRecord wir, String handle) throws IOException {
        return _wqclient.getDecompID(wir, handle) ;
    }


    public String getCaseData(String caseID, String handle) throws IOException {
        return _wqclient.getCaseData(caseID, handle) ;
    }

    
    public String getWorkItemDurationsForParticipant(YSpecificationID specID,
                                           String taskName, String pid, String handle)
                                           throws IOException {
        return _wqclient.getWorkItemDurationsForParticipant(specID, taskName, pid, handle);
    }



    /********************************************************************************/

    // REGISTERED SERVICE INFO //

    public Set<YAWLServiceReference> getRegisteredServices(String handle) throws IOException {
        String xml = _wqclient.getRegisteredServices(handle) ;
        return _marshaller.unmarshallServices(xml);
    }


    public String removeRegisteredService(String id, String handle) throws IOException {
        return _wqclient.removeRegisteredService(id, handle);
    }


    public String addRegisteredService(YAWLServiceReference service, String handle)
                                                                    throws IOException {
        return _wqclient.addRegisteredService(service.getURI(), service.get_serviceName(),
                service.getDocumentation(), service.isAssignable(), handle);
    }

}