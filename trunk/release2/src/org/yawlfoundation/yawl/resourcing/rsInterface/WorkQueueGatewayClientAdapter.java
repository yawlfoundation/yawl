/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.rsInterface;

import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.YSpecificationID;
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
 * Version: 0.1
 *
 */

public class WorkQueueGatewayClientAdapter {

    protected WorkQueueGatewayClient _wqclient;        // the gateway client
    protected String _uri ;                           // the uri of the service gateway

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

    
    /*****************************************************************************/

    // PUBLIC METHODS //

    /**
     * Checks that the connection to the service is valid
     * @param handle the current sessionhandle
     * @return true if the connection is valid, false if otherwise
     */
    public boolean checkConnection(String handle) {
        try {
            return _wqclient.checkConnection(handle).equals("true") ;
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
            return _wqclient.userlogin(userid,
                    PasswordEncryptor.getInstance().encrypt(password)) ;
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
        return _wqclient.isValidUserSession(handle).equalsIgnoreCase("true");
    }


    public Participant getParticipantFromUserID(String userid, String handle)
                                                                  throws IOException {
        Participant result = null;
        String xml = _wqclient.getParticipantFromUserID(userid, handle) ;
        if (xml != null) {
            result = new Participant() ;
            result.fromXML(xml);
        }
        return result; 
    }


    public String getFullNameForUserID(String userid, String handle) throws IOException {
        return _wqclient.getFullNameForUserID(userid, handle);
    }


    public UserPrivileges getUserPrivileges(String pid, String handle) throws IOException {
        UserPrivileges result = null;
        String xml = _wqclient.getUserPrivileges(pid, handle);
        if (xml != null) {
            result = new UserPrivileges() ;
            result.fromXML(xml);
        }
        return result;
    }


    public Set<Participant> getReportingToParticipant(String pid, String handle)
                                                                throws IOException {
        String xml = _wqclient.getReportingToParticipant(pid, handle) ;
        return _marshaller.unmarshallParticipants(xml) ;
    }


    public Set<Participant> getOrgGroupMembers(String oid, String handle)
                                                                throws IOException {
        String xml = _wqclient.getOrgGroupMembers(oid, handle) ;
        return _marshaller.unmarshallParticipants(xml) ;
    }


    public Participant getParticipant(String pid, String handle) throws IOException {
        Participant result = null;
        String xml = _wqclient.getParticipant(pid, handle) ;
        if (xml != null) {
            result = new Participant() ;
            result.fromXML(xml);
        }
        return result;
    }


    public Set<Participant> getAllParticipants(String handle) throws IOException {
        String xml = _wqclient.getAllParticipants(handle) ;
        return _marshaller.unmarshallParticipants(xml) ;
    }


    /*****************************************************************************/

    public QueueSet getAdminQueues(String handle) throws IOException {
        QueueSet result = null;
        String xml = _wqclient.getAdminQueues(handle) ;
        if (xml != null) {
            result = new QueueSet("admin", QueueSet.setType.adminSet, false) ;
            result.fromXML(xml);
        }
        return result;
    }


    public Set<WorkItemRecord> getQueuedWorkItems(String pid, int queue, String handle)
                                                                    throws IOException {
        String xml = _wqclient.getQueuedWorkItems(pid, queue, handle);
        return _marshaller.unmarshallWorkItemRecords(xml);
    }


    public String getWorkItem(String itemID, String handle) throws IOException {
        return _wqclient.getWorkItem(itemID, handle) ;
    }


    public String updateWorkItemData(String itemID, String data, String handle)
            throws IOException {
        return _wqclient.updateWorkItemData(itemID, data, handle) ;
    }


    public Set<Participant> getParticipantsAssignedWorkItem(String workItemID,
                                   int queueType, String handle) throws IOException {
        String xml = _wqclient.getParticipantsAssignedWorkItem(workItemID,
                                                               queueType, handle) ;
        return _marshaller.unmarshallParticipants(xml) ;
    }


    public void acceptOffer(String pid, String itemID, String handle) throws IOException {
        _wqclient.acceptOffer(pid, itemID, handle);
    }

    
    public boolean startItem(String pid, String itemID, String handle) throws IOException {
        String result = _wqclient.startItem(pid, itemID, handle);
        return result.equals("true");
    }


    public boolean deallocateItem(String pid, String itemID, String handle)
                                                                    throws IOException {
        String result = _wqclient.deallocateItem(pid, itemID, handle);
        return result.equals("true");
    }


    public boolean delegateItem(String pFrom, String pTo, String itemID, String handle)
                                                                     throws IOException {
        String result = _wqclient.delegateItem(pFrom, pTo, itemID, handle);
        return result.equals("true");
    }


    public boolean reallocateItem(String pFrom, String pTo, String itemID,
                               boolean stateful, String handle) throws IOException {
        String result = _wqclient.reallocateItem(pFrom, pTo, itemID, stateful, handle);
        return result.equals("true");
    }


    public boolean skipItem(String pid, String itemID, String handle) throws IOException {
        String result = _wqclient.skipItem(pid, itemID, handle);
        return result.equals("true");
    }


    public String pileItem(String pid, String itemID, String handle) throws IOException {
        return _wqclient.pileItem(pid, itemID, handle);
    }


    public boolean suspendItem(String pid, String itemID, String handle) throws IOException {
        String result = _wqclient.suspendItem(pid, itemID, handle);
        return result.equals("true");
    }

    public boolean unsuspendItem(String pid, String itemID, String handle) throws IOException {
        String result = _wqclient.unsuspendItem(pid, itemID, handle);
        return result.equals("true");
    }

    public boolean completeItem(String pid, String itemID, String handle) throws IOException {
        String result = _wqclient.completeItem(pid, itemID, handle);
        return result.equals("true");
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
        return _wqclient.addRegisteredService(service, handle);
    }

}