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

package org.yawlfoundation.yawl.resourcing;

import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceB_EnvironmentBasedClient;
import org.yawlfoundation.yawl.engine.interfce.interfaceE.YLogGatewayClient;
import org.yawlfoundation.yawl.resourcing.client.CostClient;
import org.yawlfoundation.yawl.resourcing.client.DocStoreClient;
import org.yawlfoundation.yawl.resourcing.client.MailClient;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.EventLogger;
import org.yawlfoundation.yawl.resourcing.resource.AbstractResource;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceGatewayServer;
import org.yawlfoundation.yawl.util.AbstractEngineClient;
import org.yawlfoundation.yawl.util.HttpURLValidator;

import java.io.IOException;

/**
 * Handles all of the interface connections and calls for the Resource Service
 * to the Engine and other services as needed, besides those that go through
 * InterfaceBWebsideController
 *
 * @author Michael Adams
 * @date 22/02/12
 */
public class InterfaceClients extends AbstractEngineClient {

    private String _exceptionServiceURI = null ;
    private String _schedulingServiceURI = null ;

    // client reference objects
    private YLogGatewayClient _interfaceEClient;
    private final ResourceGatewayServer _gatewayServer;
    private CostClient _costServiceClient;
    private DocStoreClient _docStoreClient;
    private MailClient _mailServiceClient;

    private static final String DEF_URI = "http://localhost:8080/resourceService/ib";
    private static final String SERVICE_NAME = "resourceService";


    /**
     * Initialises the class. Package access only.
     * @param logonName the resource service's logon name (from web.xml)
     * @param password the resource service's logon password (from web.xml)
     */
    protected InterfaceClients(String logonName, String password) {
        super(logonName, password, DEF_URI, SERVICE_NAME);
        _gatewayServer = new ResourceGatewayServer();
        EventLogger.setEventServer(_gatewayServer);
    }


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
                            String schedulingURI, String costServiceURI,
                            String docStoreURI, String mailServiceURI) {
        initEngineURI(engineURI);
        if (engineURI != null) {
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
        if (mailServiceURI != null) {
             _mailServiceClient = new MailClient(
                     mailServiceURI, _engineLogonName, _engineLogonPassword);
         }
    }


    /**
     * Reestablishes clients when the resource service restarts
     * @param client the Interface B client from InterfaceBWebsideController
     */
    public void reestablishClients(InterfaceB_EnvironmentBasedClient client) {
        super.reestablishClients(client);
        String uriE = _interfaceEClient.getBackEndURI();
        _interfaceEClient = new YLogGatewayClient(uriE);
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


    public String addEventListener(String uri) {
        return _gatewayServer.addEventListener(uri);
    }


    public boolean removeEventListener(String uri) {
        return _gatewayServer.removeEventListener(uri);
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


    /******************************************************************************/

    // Interface E methods //

    public String getEngineXESLog(YSpecificationID specID, boolean withData,
                                  boolean ignoreUnknownLabels) {
        try {
            return _interfaceEClient.getSpecificationXESLog(specID, withData,
                    ignoreUnknownLabels, getSessionHandle());
        }
        catch (IOException ioe) {
            return null;
        }
    }

    public String getEngineSpecificationStatistics(YSpecificationID specID,
                                                   long from, long to) {
        try {
            return _interfaceEClient.getSpecificationStatistics(specID, from, to,
                                                     getSessionHandle());
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

    public boolean hasMailClient() { return _mailServiceClient != null; }

    public MailClient getMailClient() { return _mailServiceClient; }

    public boolean hasExceptionServiceEnabled() { return (_exceptionServiceURI != null); }

    public String getExceptionServiceURI() { return _exceptionServiceURI; }


    protected void removeCaseFromDocStore(String caseID) {
        try {
            String response = _docStoreClient.completeCase(caseID, _docStoreClient.getHandle());
            _log.debug(response);
        }
        catch (IOException ioe) {
            _log.warn("Unable to remove uploaded docs for case {} (if any)" +
                    " - could not connect to Document Store", caseID);
        }
    }

}
