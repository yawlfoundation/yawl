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

package org.yawlfoundation.yawl.resourcing;

import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.authentication.YExternalClient;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceA.InterfaceA_EnvironmentBasedClient;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceB_EnvironmentBasedClient;
import org.yawlfoundation.yawl.engine.interfce.interfaceE.YLogGatewayClient;
import org.yawlfoundation.yawl.logging.YLogDataItemList;
import org.yawlfoundation.yawl.resourcing.client.CostClient;
import org.yawlfoundation.yawl.resourcing.client.DocStoreClient;
import org.yawlfoundation.yawl.resourcing.resource.AbstractResource;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceGatewayServer;
import org.yawlfoundation.yawl.util.*;

import javax.xml.datatype.Duration;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handles all of the interface connections and calls for the Resource Service
 * to the Engine and other services as needed, besides those that go through
 * InterfaceBWebsideController
 *
 * @author Michael Adams
 * @date 22/02/12
 */
public class InterfaceClients {

    private String _engineSessionHandle = null ;
    private String _engineLogonName;
    private String _engineLogonPassword;

    private String _serviceURI = null;
    private String _engineURI = null;
    private String _exceptionServiceURI = null ;
    private String _schedulingServiceURI = null ;
    private Namespace _yNameSpace =
            Namespace.getNamespace("http://www.yawlfoundation.org/yawlschema");

    // client reference objects
    private InterfaceA_EnvironmentBasedClient _interfaceAClient ;
    private InterfaceB_EnvironmentBasedClient _interfaceBClient ;
    private YLogGatewayClient _interfaceEClient;
    private ResourceGatewayServer _gatewayServer;
    private CostClient _costServiceClient;
    private DocStoreClient _docStoreClient;
    

    // String literals
    private static final String ADMIN_STR = "admin";
    private static final String WORKITEM_ERR = "Unknown workitem";
    private static final String SUCCESS_STR = "<success/>";
    private static final String FAIL_STR = "failure";

    private Logger _log ;                                 // debug log4j file    


    /**
     * Initialises the class. Package access only.
     * @param logonName the resource service's logon name (from web.xml)
     * @param password the resource service's logon password (from web.xml)
     */
    protected InterfaceClients(String logonName, String password) {
        _engineLogonName = logonName;
        _engineLogonPassword = password;
        _log = Logger.getLogger(InterfaceClients.class);
        _gatewayServer = new ResourceGatewayServer();
    }


    /**
     * Assigns the IB client from InterfaceBWebsideController
     * @param client an IB Client instance
     */
    protected void setInterfaceBClient(InterfaceB_EnvironmentBasedClient client) {
        _interfaceBClient = client;
    }


    /**
     * @return the YAWL namespace
     */
    protected Namespace getNamespace() { return _yNameSpace; }


    /**
     * Gets the stored URI of the Engine
     * @return the engine's URI
     */
    protected String getEngineURI() { return _engineURI; }


    /**
     * Called on servlet startup with the various uris needed to initialise the
     * various clients
     * @param engineURI the URI of the Engine's Interface B
     * @param exceptionURI the URI of the Worklet Exception Service
     * @param schedulingURI the URI of the Scheduling Service
     * @param costServiceURI the URI of the Cost Service
     * @param docStoreURI the URI of the Document Store
     */
    public void initClients(String engineURI, String exceptionURI,
                        String schedulingURI, String costServiceURI, String docStoreURI) {
        _engineURI = engineURI;
        if (engineURI != null) {
            _interfaceAClient = new InterfaceA_EnvironmentBasedClient(
                                                 engineURI.replaceFirst("/ib", "/ia"));
            _interfaceEClient = new YLogGatewayClient(
                                         engineURI.replaceFirst("/ib", "/logGateway"));
        }
        if (exceptionURI != null) {
            _exceptionServiceURI = exceptionURI;
            _gatewayServer.setExceptionInterfaceURI(exceptionURI + "/ix");
        }
        if (schedulingURI != null) {
            _schedulingServiceURI = schedulingURI;
            _gatewayServer.setSchedulingInterfaceURI(schedulingURI);
        }
        if (costServiceURI != null) {
            _costServiceClient = new CostClient(
                    costServiceURI, _engineLogonName, _engineLogonPassword);
        }
        if (docStoreURI != null) {
            _docStoreClient = new DocStoreClient(
                    docStoreURI, _engineLogonName, _engineLogonPassword);
        }
    }


    /**
     * Reestablishes clients when the resource service restarts
     * @param client the Interface B client from InterfaceBWebsideController
     */
    protected void reestablishClients(InterfaceB_EnvironmentBasedClient client) {
        String uriA = _interfaceAClient.getBackEndURI();
        String uriE = _interfaceEClient.getBackEndURI();
        _interfaceAClient = new InterfaceA_EnvironmentBasedClient(uriA);
        _interfaceBClient = client;
        _interfaceEClient = new YLogGatewayClient(uriE);
        _engineSessionHandle = null;
    }


    /**
     * Sets the Resource Service URI as read from that stored in the Engine
     */
    protected void setServiceURI() {
        _serviceURI = "http://localhost:8080/resourceService/ib";         // a default
        Set<YAWLServiceReference> services = getRegisteredServices();
        if (services != null) {
            for (YAWLServiceReference service : services) {
                if (service.getURI().contains("resourceService")) {
                    _serviceURI = service.getURI();
                }
            }
        }
    }


    /**
     * Gets the Resource Service's URI, initialising it if necessary
     * @return the Service's URI, or a default if not found
     */
    public String getServiceURI() {
        if (_serviceURI == null) setServiceURI();
        return _serviceURI;
    }


    /**
     * Gets the URI for a named service, as stored in the Engine
     * @param serviceName the name of the service to get the URI for
     * @return the URI, or null if not found
     */
    private String getServiceURI(String serviceName) {
        Set<YAWLServiceReference> serviceSet = getRegisteredServices();
        if (serviceSet != null) {
            for (YAWLServiceReference service : serviceSet) {
                if (service.getServiceName().equals(serviceName)) {
                    return service.getURI();
                }
            }
        }
        return null;
    }

    /**
     * Check that the engine is contactable
     * @return true if Engine is contactable
     */
    protected boolean engineIsAvailable() {
        String errMsg = "Failed to locate a running YAWL engine at URL '" +
                        _engineURI + "'. ";
        int timeout = 5;
        boolean available = false;
        try {
            available = HttpURLValidator.pingUntilAvailable(_engineURI, timeout);
            if (! available) {
                _log.error(errMsg + "Service functionality may be limited.");
            }
        }
        catch (MalformedURLException mue) {
            _log.error(errMsg + mue.getMessage());
        }
        return available;
    }


    /** Checks if there is a connection to the engine, and
     *  if there isn't, attempts to connect
     *  @return true if connected to the engine
     */
    protected boolean connected() {

        // use borrowed mutex for ResourceManager for IB calls
        synchronized(ResourceManager.getInstance().getIBEventMutex()) {
            try {
                // if not connected
                if ((_engineSessionHandle == null) ||
                        (_engineSessionHandle.length() == 0) ||
                        (! checkConnection(_engineSessionHandle))) {

                    _engineSessionHandle = _interfaceBClient.connect(
                            _engineLogonName, _engineLogonPassword);
                }
            }
            catch (IOException ioe) {
                _log.error("Exception attempting to connect to engine", ioe);
            }
            return (_interfaceBClient.successful(_engineSessionHandle)) ;
        }
    }


    /**
     * @return the current session handle for the engine
     */
    protected String getEngineSessionHandle() {
        connected();                           // (re)establish connection if required
        return _engineSessionHandle;
    }


    /**
     * Checks the current session handle for validity
     * @param sessionHandle the handle to check
     * @return true if handle is valid and active
     * @throws IOException if there's a problem connection to the Engine through IB
     */
    private boolean checkConnection(String sessionHandle) throws IOException {
        String msg = _interfaceBClient.checkConnection(sessionHandle);
        return _interfaceBClient.successful(msg);
    }


    /******************************************************************************/

    // Server methods (outgoing event announcements) //

    public void announceResourceUnavailable(WorkItemRecord wir) {
        announceResourceUnavailable(null, wir, true);
    }

    public void announceResourceUnavailable(AbstractResource resource,
                                            WorkItemRecord wir, boolean primary) {
        if (resource != null) {
            try {
                String caseData = wir != null ? getCaseData(wir.getRootCaseID()) : null;
                _gatewayServer.announceResourceUnavailable(resource.getID(), wir,
                        caseData, primary);
            }
            catch (IOException ioe) {
                _log.error("Failed to announce unavailable resource to environment", ioe);
            }
        }
    }

    public void announceResourceCalendarStatusChange(String origAgent, String changeXML) {
        try {
            _gatewayServer.announceResourceCalendarStatusChange(origAgent, changeXML);
        }
        catch (IOException ioe) {
            _log.error("Failed to announce resource calendar status change to environment", ioe);
        }
    }

    public String registerCalendarStatusChangeListener(String uri, String userID) {
        return (userID == null) ? fail("Invalid session handle") :
                _gatewayServer.registerSchedulingInterfaceListener(userID, uri);
    }

    public void removeCalendarStatusChangeListener(String uri, String userID) {
        if (userID != null) _gatewayServer.removeSchedulingInterfaceListener(userID, uri);
    }

    public void removeCalendarStatusChangeListeners(String userID) {
        if (userID != null) _gatewayServer.removeSchedulingInterfaceListeners(userID);
    }


    /**
     * Dispatches a work item to a YAWL Custom Service for handling.
     * @param wir the work item to be redirected.
     * @param serviceName the name of the service to redirect it to
     * @pre The item id refers to a work item that is currently in the list of items known
     * to the Resource Service, and the work item has enabled or fired status
     * @pre The service name refers to a service registered in the engine
     * @pre The service is up and running
     * @return a success or diagnostic error message
     */
    protected String redirectWorkItemToYawlService(WorkItemRecord wir, String serviceName) {
        String result;
        if (wir != null) {                                       // wir exists...
            if (wir.isEnabledOrFired()) {                        // and has right status
                String serviceURI = getServiceURI(serviceName);
                if (serviceURI != null) {                            // service exists...
                    result = HttpURLValidator.validate(serviceURI);
                    if (_interfaceBClient.successful(result)) {      // and is online
                        try {
                            if (_gatewayServer != null) {
                                _gatewayServer.redirectWorkItemToYawlService(wir.toXML(),
                                        serviceURI);
                                return SUCCESS_STR;
                            }
                            else result = fail("Gateway server unavailable");
                        }
                        catch (Exception e) {
                            _log.error("Failed to redirect workitem: " + wir.getID(), e);
                            result = fail(e.getMessage());
                        }
                    }
                }
                else result = fail("Unknown or unregistered service name: " + serviceName);
            }
            else result = fail("Only work items with enabled or fired status may be " +
                    "redirected; work item [" + wir.getID() + "] has status: " + wir.getStatus());
        }
        else result = fail(WORKITEM_ERR);

        return result;
    }


    /*****************************************************************************/

    // Interface A methods //

    protected String getEngineBuildProperties() {
        try {
            return _interfaceAClient.getBuildProperties(getEngineSessionHandle());
        }
        catch (IOException ioe) {
            return fail("IO Exception retrieving engine build properties.");
        }
    }


    public Set<YAWLServiceReference> getRegisteredServices() {
        return _interfaceAClient.getRegisteredYAWLServices(getEngineSessionHandle());
    }


    protected String getAdminUserPassword() {
        try {
            return _interfaceAClient.getPassword(ADMIN_STR, getEngineSessionHandle());
        }
        catch (IOException ioe) {
            return fail("Could not connect to YAWL Engine");
        }
    }


    public String uploadSpecification(String fileContents, String fileName) {
        try {
            return _interfaceAClient.uploadSpecification(fileContents, getEngineSessionHandle());
        }
        catch (IOException ioe) {
            _log.error("IOException uploading specification " + fileName, ioe);
            return fail("<reason><error>IOException uploading specification " +
                    fileName + "</error></reason>");
        }
    }


    protected String unloadSpecification(YSpecificationID specID) throws IOException {
        return _interfaceAClient.unloadSpecification(specID, getEngineSessionHandle());
    }


    public String getRegisteredServicesAsXML() throws IOException {
        return _interfaceAClient.getRegisteredYAWLServicesAsXML(getEngineSessionHandle());
    }


    protected String addRegisteredService(YAWLServiceReference service) throws IOException {
        return _interfaceAClient.addYAWLService(service, getEngineSessionHandle());
    }


    protected String removeRegisteredService(String id) throws IOException {
        return _interfaceAClient.removeYAWLService(id, getEngineSessionHandle());
    }


    public Set<YExternalClient> getExternalClients() throws IOException {
        return _interfaceAClient.getClientAccounts(getEngineSessionHandle());
    }


    protected String addExternalClient(YExternalClient client) throws IOException {
        return _interfaceAClient.addClientAccount(client, getEngineSessionHandle());
    }


    protected String removeExternalClient(String id) throws IOException {
        return _interfaceAClient.removeClientAccount(id, getEngineSessionHandle());
    }


    protected String updateExternalClient(String id, String password, String doco)
            throws IOException {
        return _interfaceAClient.updateClientAccount(id, password, doco,
                getEngineSessionHandle());
    }

    protected String getIABackendURI() {
        return _interfaceAClient.getBackEndURI();
    }


    /*****************************************************************************/

    // Interface B methods //

    protected List<WorkItemRecord> getWorkItemsForService() throws IOException {
        return _interfaceBClient.getWorkItemsForService(getServiceURI(), getEngineSessionHandle());
    }
    
    protected String suspendWorkItem(String itemID) throws IOException {
        return _interfaceBClient.suspendWorkItem(itemID, getEngineSessionHandle());
    }

    protected String unsuspendWorkItem(String itemID) throws IOException {
        return _interfaceBClient.unsuspendWorkItem(itemID, getEngineSessionHandle());
    }

    protected String skipWorkItem(String itemID) throws IOException {
        return _interfaceBClient.skipWorkItem(itemID, getEngineSessionHandle());
    }

    public String getRunningCases(YSpecificationID specID) throws IOException {
        return _interfaceBClient.getCases(specID, getEngineSessionHandle());
    }

    protected String cancelCase(String caseID) throws IOException {
        return _interfaceBClient.cancelCase(caseID, getEngineSessionHandle());
    }

    public String getCaseData(String caseID) throws IOException {
        return _interfaceBClient.getCaseData(caseID, getEngineSessionHandle()) ;
    }

    protected String getSpecificationDataSchema(YSpecificationID specID) throws IOException {
        return _interfaceBClient.getSpecificationDataSchema(specID, getEngineSessionHandle());
    }


    protected List<WorkItemRecord> getLiveWorkItemsForCase(String caseID) {
        try {
            return _interfaceBClient.getLiveWorkItemsForIdentifier("case", caseID,
                                                         getEngineSessionHandle()) ;
        }
        catch (Exception e) {
            _log.error("Exception attempting to retrieve work item list from engine");
        }
        return null;
    }

    protected String launchCase(YSpecificationID specID, String caseData,
                                YLogDataItemList logList) throws IOException {
        if (_serviceURI == null) setServiceURI();
        return _interfaceBClient.launchCase(specID, caseData, getEngineSessionHandle(),
                         logList, _serviceURI) ;
    }

    protected String launchCase(YSpecificationID specID, String caseData,
                                YLogDataItemList logList, long delay) throws IOException {
        if (_serviceURI == null) setServiceURI();
        return _interfaceBClient.launchCase(specID, caseData, getEngineSessionHandle(),
                         logList, _serviceURI, delay);
    }

    protected String launchCase(YSpecificationID specID, String caseData,
                                YLogDataItemList logList, Date delay) throws IOException {
        if (_serviceURI == null) setServiceURI();
        return _interfaceBClient.launchCase(specID, caseData, getEngineSessionHandle(),
                         logList, _serviceURI, delay);
    }

    protected String launchCase(YSpecificationID specID, String caseData,
                                YLogDataItemList logList, Duration delay) throws IOException {
        if (_serviceURI == null) setServiceURI();
        return _interfaceBClient.launchCase(specID, caseData, getEngineSessionHandle(),
                         logList, _serviceURI, delay);
    }


    public XNode getAllRunningCases() {
        try {
            String caseStr = _interfaceBClient.getAllRunningCases(getEngineSessionHandle());
            if (_interfaceBClient.successful(caseStr)) {
                return new XNodeParser().parse(StringUtil.unwrap(caseStr));
            }
        }
        catch (IOException ioe) {
            _log.error("Could not get Running Case list: ", ioe);
        }
        return null;
    }

    protected Set<String> getAllRunningCaseIDs() {
        Set<String> result = new HashSet<String>();
        XNode node = getAllRunningCases();
        if (node != null) {
            for (XNode specNode : node.getChildren()) {
                for (XNode caseNode : specNode.getChildren()) {
                    result.add(caseNode.getText());
                }
            }
        }
        return result ;
    }

    public boolean isRunningCaseID(String caseID) {
        for (String runningCaseID : getAllRunningCaseIDs()) {
            if (runningCaseID.equals(caseID)) {
                return true;
            }
        }
        return false;
    }


    protected List<String> getRunningCasesAsList(YSpecificationID specID) {
        try {
            String casesAsXML = _interfaceBClient.getCases(specID, getEngineSessionHandle());
            if (_interfaceBClient.successful(casesAsXML))
                return Marshaller.unmarshalCaseIDs(casesAsXML);
        }
        catch (IOException ioe) {
            _log.error("IO Exception retrieving running cases list", ioe) ;
        }
        return null;
    }

    protected String getTaskParamsAsXML(YSpecificationID specID, String taskID) throws IOException {
        String xml = _interfaceBClient.getTaskInformationStr(specID, taskID, getEngineSessionHandle());
        if (xml != null) {
            Element response = JDOMUtil.stringToElement(xml);
            if (response != null) {
                Element taskInfo = response.getChild("taskInfo");
                if (taskInfo != null) {
                    Element params = taskInfo.getChild("params");
                    return JDOMUtil.elementToString(params);
                }
            }
        }
        return "";
    }

    protected boolean canAddNewInstance(WorkItemRecord wir) {
        try {
            return _interfaceBClient.successful(
                    _interfaceBClient.checkPermissionToAddInstances(wir.getID(),
                            getEngineSessionHandle()));
        }
        catch (IOException ioe) {
            return false;
        }
    }


    protected WorkItemRecord createNewWorkItemInstance(String id, String value) {
        WorkItemRecord result = null;
        try {
            String xml = _interfaceBClient.createNewInstance(id, value, getEngineSessionHandle());
            if (_interfaceBClient.successful(xml)) {
                result = Marshaller.unmarshalWorkItem(StringUtil.unwrap(xml));
            }
            else _log.error(xml);
        }
        catch (IOException ioe) {
            // nothing to do
        }
        return result;
    }


    /******************************************************************************/

    // Interface E methods //

    public String getEngineXESLog(YSpecificationID specID, boolean withData) {
        try {
            return _interfaceEClient.getSpecificationXESLog(specID, withData,
                                                     getEngineSessionHandle());
        }
        catch (IOException ioe) {
            return null;
        }
    }

    public String getEngineSpecificationStatistics(YSpecificationID specID,
                                                   long from, long to) {
        try {
            return _interfaceEClient.getSpecificationStatistics(specID, from, to,
                                                     getEngineSessionHandle());
        }
        catch (IOException ioe) {
            return null;
        }
    }


    /****************************************************************************/

    public boolean hasCostClient() { return _costServiceClient != null; }

    public CostClient getCostClient() { return _costServiceClient; }

    public boolean hasDocStoreClient() { return _docStoreClient != null; }

    public DocStoreClient getDocStoreClient() { return _docStoreClient; }

    public boolean hasExceptionServiceEnabled() { return (_exceptionServiceURI != null); }

    public String getExceptionServiceURI() { return _exceptionServiceURI; }


    protected void removeCaseFromDocStore(String caseID) {
        try {
            String response = _docStoreClient.completeCase(caseID, _docStoreClient.getHandle());
            _log.debug(response);
        }
        catch (IOException ioe) {
            _log.error("Error removing uploaded docs for case " + caseID +
                    " - could not connect to Document Store");
        }
    }


    /********************************************************************************/

    private String fail(String msg) {
        return StringUtil.wrap(msg, FAIL_STR);
    }
    
}
