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

package org.yawlfoundation.yawl.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.authentication.YExternalClient;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceA.InterfaceA_EnvironmentBasedClient;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceB_EnvironmentBasedClient;
import org.yawlfoundation.yawl.logging.YLogDataItemList;

import javax.xml.datatype.Duration;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base class that abstracts interface connections and calls between custom services
 * and the Engine and other services as needed, besides those that go through
 * InterfaceBWebsideController
 *
 * @author Michael Adams
 * @date 05/01/2015
 */
public abstract class AbstractEngineClient {

    protected String _sessionHandle = null ;
    protected String _engineLogonName;
    protected String _engineLogonPassword;
    protected String _defaultURI;
    protected String _serviceName;
    protected String _serviceURI = null;
    protected String _engineURI = null;
    private Namespace _yNameSpace =
            Namespace.getNamespace("http://www.yawlfoundation.org/yawlschema");

    // client reference objects
    protected InterfaceA_EnvironmentBasedClient _interfaceAClient ;
    protected InterfaceB_EnvironmentBasedClient _interfaceBClient ;

    // String literals
    protected static final String ADMIN_STR = "admin";
    protected static final String WORKITEM_ERR = "Unknown workitem";
    protected static final String SUCCESS_STR = "<success/>";
    protected static final String FAIL_STR = "failure";

    protected Logger _log ;                                 // debug log4j file

    private static final Object _mutex = new Object();


    /**
     * Initialises the class. Package access only.
     * @param logonName the service's logon name (from web.xml)
     * @param password the service's logon password (from web.xml)
     */
    protected AbstractEngineClient(String logonName, String password,
                                   String uri, String name) {
        _engineLogonName = logonName;
        _engineLogonPassword = password;
        _defaultURI = uri;
        _serviceURI = uri;
        _serviceName = name;
        _log = LogManager.getLogger(this.getClass());
    }


    /**
     * Assigns the IB client from InterfaceBWebsideController
     * @param client an IB Client instance
     */
    public void setInterfaceBClient(InterfaceB_EnvironmentBasedClient client) {
        if (client != null) {
            _interfaceBClient = client;
        }
        else if (_engineURI != null && engineIsAvailable()) {
                _interfaceBClient = new InterfaceB_EnvironmentBasedClient(_engineURI);
        }
        else throw new IllegalArgumentException("Unable to setup engine client. " +
                    "Client = " + client);
    }


    /**
     * @return the YAWL namespace
     */
    protected Namespace getNamespace() { return _yNameSpace; }


    /**
     * Gets the stored URI of the Engine
     * @return the engine's URI
     */
    public String getEngineURI() { return _engineURI; }


    /**
     * Called on servlet startup with the various uris needed to initialise the
     * various clients
     * @param engineURI the URI of the Engine's Interface B
     */
    public void initEngineURI(String engineURI) {
        _engineURI = engineURI;
        if (engineURI != null) {
            _interfaceAClient = new InterfaceA_EnvironmentBasedClient(
                                                 engineURI.replaceFirst("/ib", "/ia"));
        }
    }


    /**
     * Reestablishes clients when the service restarts
     * @param client the Interface B client from InterfaceBWebsideController
     */
    public void reestablishClients(InterfaceB_EnvironmentBasedClient client) {
        String uriA = _interfaceAClient.getBackEndURI();
        _interfaceAClient = new InterfaceA_EnvironmentBasedClient(uriA);
        _interfaceBClient = client;
        _sessionHandle = null;
    }


    /**
     * Sets the Service URI as read from that stored in the Engine
     */
    public void setServiceURI() {
        _serviceURI = _defaultURI;         // a default
        Set<YAWLServiceReference> services = getRegisteredServices();
        if (services != null) {
            for (YAWLServiceReference service : services) {
                if (service.getURI().contains(_serviceName)) {
                    _serviceURI = service.getURI();
                }
            }
        }
    }


    /**
     * Gets the Service's URI, initialising it if necessary
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
    protected String getServiceURI(String serviceName) {
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
    public boolean engineIsAvailable() {
        String errMsg = "Failed to locate a running YAWL engine at URL '" +
                        _engineURI + "'. ";
        int timeout = 5;
        boolean available = false;
        try {
            available = HttpURLValidator.pingUntilAvailable(_engineURI, timeout);
            if (! available) {
                _log.error("{} Service functionality may be limited.", errMsg);
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
        synchronized(_mutex) {
            try {
                // if not connected
                if ((_sessionHandle == null) ||
                        (_sessionHandle.length() == 0) ||
                        (! checkConnection(_sessionHandle))) {

                    if (_interfaceBClient == null) setInterfaceBClient(null);

                    _sessionHandle = _interfaceBClient.connect(
                            _engineLogonName, _engineLogonPassword);
                }
            }
            catch (IOException ioe) {
                _log.error("Exception attempting to connect to engine", ioe);
            }
            catch (NullPointerException npe) {
                _log.error("Failed to initialise Interface B Client");
                return false;
            }
            return (successful(_sessionHandle)) ;
        }
    }


    /**
     * @return the current session handle for the engine
     */
    public String getSessionHandle() {
        connected();                           // (re)establish connection if required
        return _sessionHandle;
    }


    public boolean successful(String input) {
        return _interfaceBClient.successful(input);
    }


    /**
     * Checks the current session handle for validity
     * @param sessionHandle the handle to check
     * @return true if handle is valid and active
     * @throws IOException if there's a problem connection to the Engine through IB
     */
    private boolean checkConnection(String sessionHandle) throws IOException {
        return successful(_interfaceBClient.checkConnection(sessionHandle));
    }


    /**
     * returns true if the session specified is an admin session
     */
    public boolean isAdminSession(String sessionHandle) {
        try {
            return successful(_interfaceAClient.checkConnection(sessionHandle));
        } catch (IOException ioe) {
            return false;
        }
    }



    /******************************************************************************/

    // Interface A methods //

    public String getEngineBuildProperties() {
        try {
            return _interfaceAClient.getBuildProperties(getSessionHandle());
        }
        catch (IOException ioe) {
            return fail("IO Exception retrieving engine build properties.");
        }
    }


    public Set<YAWLServiceReference> getRegisteredServices() {
        return _interfaceAClient.getRegisteredYAWLServices(getSessionHandle());
    }


    public String getAdminUserPassword() {
        try {
            return _interfaceAClient.getPassword(ADMIN_STR, getSessionHandle());
        }
        catch (IOException ioe) {
            return fail("Could not connect to YAWL Engine");
        }
    }


    public String uploadSpecification(String fileContents, String fileName) {
        try {
            return _interfaceAClient.uploadSpecification(fileContents, getSessionHandle());
        }
        catch (IOException ioe) {
            _log.error("IOException uploading specification " + fileName, ioe);
            return fail("<reason><error>IOException uploading specification " +
                    fileName + "</error></reason>");
        }
    }


    public String unloadSpecification(YSpecificationID specID) throws IOException {
        return _interfaceAClient.unloadSpecification(specID, getSessionHandle());
    }


    public String getRegisteredServicesAsXML() throws IOException {
        return _interfaceAClient.getRegisteredYAWLServicesAsXML(getSessionHandle());
    }


    public String addRegisteredService(YAWLServiceReference service) throws IOException {
        return _interfaceAClient.addYAWLService(service, getSessionHandle());
    }


    public String removeRegisteredService(String id) throws IOException {
        return _interfaceAClient.removeYAWLService(id, getSessionHandle());
    }


    public Set<YExternalClient> getExternalClients() throws IOException {
        return _interfaceAClient.getClientAccounts(getSessionHandle());
    }


    public String addExternalClient(YExternalClient client) throws IOException {
        return _interfaceAClient.addClientAccount(client, getSessionHandle());
    }


    public String removeExternalClient(String id) throws IOException {
        return _interfaceAClient.removeClientAccount(id, getSessionHandle());
    }


    public String updateExternalClient(String id, String password, String doco)
            throws IOException {
        return _interfaceAClient.updateClientAccount(id, password, doco,
                getSessionHandle());
    }

    public String getIABackendURI() {
        return _interfaceAClient.getBackEndURI();
    }


    /*****************************************************************************/

    // Interface B methods //

    public List<WorkItemRecord> getWorkItemsForService() throws IOException {
        return _interfaceBClient.getWorkItemsForService(getServiceURI(), getSessionHandle());
    }
    
    public String suspendWorkItem(String itemID) throws IOException {
        return _interfaceBClient.suspendWorkItem(itemID, getSessionHandle());
    }

    public String unsuspendWorkItem(String itemID) throws IOException {
        return _interfaceBClient.unsuspendWorkItem(itemID, getSessionHandle());
    }

    public String skipWorkItem(String itemID) throws IOException {
        return _interfaceBClient.skipWorkItem(itemID, getSessionHandle());
    }

    public String getRunningCases(YSpecificationID specID) throws IOException {
        return _interfaceBClient.getCases(specID, getSessionHandle());
    }

    public String cancelCase(String caseID) throws IOException {
        return _interfaceBClient.cancelCase(caseID, getSessionHandle());
    }

    public String getCases(YSpecificationID specID) throws IOException {
         return _interfaceBClient.getCases(specID, getSessionHandle()) ;
    }

    public String getCaseData(String caseID) throws IOException {
        return _interfaceBClient.getCaseData(caseID, getSessionHandle()) ;
    }

    public String getSpecificationDataSchema(YSpecificationID specID) throws IOException {
        return _interfaceBClient.getSpecificationDataSchema(specID, getSessionHandle());
    }

    public String getSpecification(YSpecificationID specID) throws IOException {
        return _interfaceBClient.getSpecification(specID, getSessionHandle());
    }

    public String getMITaskAttributes(YSpecificationID specID, String taskID)
            throws IOException {
       return _interfaceBClient.getMITaskAttributes(specID, taskID, getSessionHandle());
    }


    public List<WorkItemRecord> getLiveWorkItemsForCase(String caseID) {
        try {
            return _interfaceBClient.getLiveWorkItemsForIdentifier("case", caseID,
                                                         getSessionHandle()) ;
        }
        catch (Exception e) {
            _log.error("Exception attempting to retrieve work item list from engine");
        }
        return null;
    }


    public List<WorkItemRecord> getLiveWorkItemsForIdentifier(String idType, String id)
            throws IOException, JDOMException {
        return _interfaceBClient.getLiveWorkItemsForIdentifier(idType, id, getSessionHandle());
    }


    public List<WorkItemRecord> getAllLiveWorkItems() throws IOException {
        return _interfaceBClient.getCompleteListOfLiveWorkItems(getSessionHandle());
    }


    public String launchCase(YSpecificationID specID, String caseData,
                                YLogDataItemList logList) throws IOException {
        if (_serviceURI == null) setServiceURI();
        return _interfaceBClient.launchCase(specID, caseData, getSessionHandle(),
                         logList, _serviceURI) ;
    }

    public String launchCase(YSpecificationID specID, String caseData,
                                YLogDataItemList logList, long delay) throws IOException {
        if (_serviceURI == null) setServiceURI();
        return _interfaceBClient.launchCase(specID, caseData, getSessionHandle(),
                         logList, _serviceURI, delay);
    }

    public String launchCase(YSpecificationID specID, String caseData,
                                YLogDataItemList logList, Date delay) throws IOException {
        if (_serviceURI == null) setServiceURI();
        return _interfaceBClient.launchCase(specID, caseData, getSessionHandle(),
                         logList, _serviceURI, delay);
    }

    public String launchCase(YSpecificationID specID, String caseData,
                                YLogDataItemList logList, Duration delay) throws IOException {
        if (_serviceURI == null) setServiceURI();
        return _interfaceBClient.launchCase(specID, caseData, getSessionHandle(),
                         logList, _serviceURI, delay);
    }


    public XNode getAllRunningCases() {
        try {
            String caseStr = _interfaceBClient.getAllRunningCases(getSessionHandle());
            if (successful(caseStr)) {
                return new XNodeParser().parse(StringUtil.unwrap(caseStr));
            }
        }
        catch (IOException ioe) {
            _log.error("Could not get Running Case list: ", ioe);
        }
        return null;
    }

    public Set<String> getAllRunningCaseIDs() {
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


    public List<String> getRunningCasesAsList(YSpecificationID specID) {
        try {
            String casesAsXML = _interfaceBClient.getCases(specID, getSessionHandle());
            if (successful(casesAsXML))
                return Marshaller.unmarshalCaseIDs(casesAsXML);
        }
        catch (IOException ioe) {
            _log.error("IO Exception retrieving running cases list", ioe) ;
        }
        return null;
    }

    public String getTaskParamsAsXML(YSpecificationID specID, String taskID) throws IOException {
        String xml = _interfaceBClient.getTaskInformationStr(specID, taskID, getSessionHandle());
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

    public boolean canAddNewInstance(WorkItemRecord wir) {
        try {
            return successful(_interfaceBClient.checkPermissionToAddInstances(wir.getID(),
                            getSessionHandle()));
        }
        catch (IOException ioe) {
            return false;
        }
    }


    public WorkItemRecord createNewWorkItemInstance(String id, String value) {
        WorkItemRecord result = null;
        try {
            String xml = _interfaceBClient.createNewInstance(id, value, getSessionHandle());
            if (successful(xml)) {
                result = Marshaller.unmarshalWorkItem(StringUtil.unwrap(xml));
            }
            else _log.error(xml);
        }
        catch (IOException ioe) {
            // nothing to do
        }
        return result;
    }


    /********************************************************************************/

    protected String fail(String msg) {
        return StringUtil.wrap(msg, FAIL_STR);
    }
    
}
