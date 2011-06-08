/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.engine.interfce.interfaceB;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.ObserverGateway;
import org.yawlfoundation.yawl.engine.YEngine;
import org.yawlfoundation.yawl.engine.YWorkItem;
import org.yawlfoundation.yawl.engine.YWorkItemStatus;
import org.yawlfoundation.yawl.engine.announcement.Announcements;
import org.yawlfoundation.yawl.engine.announcement.CancelWorkItemAnnouncement;
import org.yawlfoundation.yawl.engine.announcement.NewWorkItemAnnouncement;
import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.unmarshal.YDecompositionParser;
import org.yawlfoundation.yawl.util.HttpURLValidator;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.io.IOException;
import java.net.ConnectException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * This interface announces specific Engine events to listening custom services
 *
 * @author Lachlan Aldred
 * Date: 22/01/2004
 * Time: 17:19:12
 *
 * @author Michael Adams (refactored for v2.0, 06/2008 - 12/2008)
 */

public class InterfaceB_EngineBasedClient extends Interface_Client implements ObserverGateway {

    protected static final Logger logger = Logger.getLogger(InterfaceB_EngineBasedClient.class);
    private static final int THREADPOOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService executor = Executors.newFixedThreadPool(THREADPOOL_SIZE);

    protected static final String ADDWORKITEM_CMD =             "announceWorkItem";
    protected static final String CANCELALLWORKITEMS_CMD =      "cancelAllInstancesUnderWorkItem";
    protected static final String CANCELWORKITEM_CMD =          "cancelWorkItem";
    protected static final String ANNOUNCE_COMPLETE_CASE_CMD =  "announceCompletion";
    protected static final String ANNOUNCE_TIMER_EXPIRY_CMD =   "announceTimerExpiry";
    protected static final String ANNOUNCE_INIT_ENGINE =        "announceEngineInitialised";
    protected static final String ANNOUNCE_CASE_CANCELLED =     "announceCaseCancelled";
    protected static final String ANNOUNCE_ITEM_STATUS =        "announceItemStatus";


    /**
     * Indicates which protocol this shim services.<P>
     *
     * @return the scheme
     */
    public String getScheme() {
        return "http";
    }

    /**
     * PRE: The work item is enabled.
     * announces a work item to a YAWL Service.
     * @param announcements
     */
    public void announceWorkItems(Announcements<NewWorkItemAnnouncement> announcements)
    {
        for (NewWorkItemAnnouncement announcement :
                announcements.getAnnouncementsForScheme(getScheme()).getAllAnnouncements())
        {
            executor.execute(new Handler(announcement.getYawlService(),
                                            announcement.getItem(), ADDWORKITEM_CMD));
        }
    }

    /**
     * Announces work item cancellation to the YAWL Service.
     * @param yawlService the YAWL service reference.
     * @param workItem the work item to cancel.
     */
    public void cancelWorkItem(YAWLServiceReference yawlService, YWorkItem workItem) {
        executor.execute(new Handler(yawlService, workItem, "cancelWorkItem"));
    }

    /**
     * Cancels the work item, and all child
     * workitems under the provided work item.
     * @param announcements
     */
    public void cancelAllWorkItemsInGroupOf(Announcements<CancelWorkItemAnnouncement> announcements)
    {
        for (CancelWorkItemAnnouncement announcement :
                announcements.getAnnouncementsForScheme(getScheme()).getAllAnnouncements())
        {
            YAWLServiceReference yawlService = announcement.getYawlService();
            YWorkItem workItem = announcement.getItem();
            if (workItem.getParent() != null) workItem = workItem.getParent();
            executor.execute(new Handler(yawlService, workItem,
                    "cancelAllInstancesUnderWorkItem"));
        }
    }


    /**
     * Announces a workitem timer expiry
     * @param yawlService the yawl service reference.
     * @param workItem the work item that has expired
     */
    public void announceTimerExpiry(YAWLServiceReference yawlService, YWorkItem workItem) {
        executor.execute(new Handler(yawlService, workItem, ANNOUNCE_TIMER_EXPIRY_CMD));
    }


    /**
     * Called by the engine to annouce when a case suspends (i.e. becomes fully
     * suspended) as opposed to entering the 'suspending' state.
     */
    public void announceCaseSuspended(YIdentifier caseID)
    {
        //todo MLF: this has been stubbed
    }

    /**
     * Called by the engine to annouce when a case starts to suspends (i.e. enters the
     * suspending state) as opposed to entering the fully 'suspended' state.
     */
    public void announceCaseSuspending(YIdentifier caseID)
    {
        //todo MLF: this has been stubbed
    }

    /**
     * Called by the engine to annouce when a case resumes from a previous 'suspending'
     * or 'suspended' state.
     */
    public void announceCaseResumption(YIdentifier caseID)
    {
        //todo MLF: this has been stubbed
    }

    /**
     * Notify of a change of status for a work item.
     *
     * @param workItem  that has changed
     * @param oldStatus previous status
     * @param newStatus new status
     */
    public void announceWorkItemStatusChange(YWorkItem workItem, YWorkItemStatus oldStatus,
                                             YWorkItemStatus newStatus)
    {
       Set<YAWLServiceReference> services = YEngine.getInstance().getYAWLServices() ;
        for (YAWLServiceReference service : services) {
            executor.execute(new Handler(service, workItem, oldStatus.toString(),
                                            newStatus.toString(), ANNOUNCE_ITEM_STATUS));
        }
    }

    /**
     * Called by engine to announce when a case is complete.
     *
     * @param caseID   the case that completed
     * @param casedata the output data of the case
     */
    public void announceCaseCompletion(YIdentifier caseID, Document casedata)
    {
        //todo MLF: this has been stubbed
    }

    /**
     * Called by engine to announce when a case is complete.
     * @param yawlService the yawl service
     * @param caseID the case that completed
     */
    public void announceCaseCompletion(YAWLServiceReference yawlService, 
                                       YIdentifier caseID, Document casedata) {
        executor.execute(new Handler(yawlService, caseID, casedata, "announceCompletion"));
    }

    /**
     * Called by the engine when it has completed initialisation and is running
     * @param services a set of custom services to receive the announcement
     * @param maxWaitSeconds the maximum seconds to wait for services to be contactable
     */
    public void announceEngineInitialised(Set<YAWLServiceReference> services, int maxWaitSeconds) {
        for (YAWLServiceReference service : services) {
            executor.execute(new Handler(service, maxWaitSeconds, ANNOUNCE_INIT_ENGINE));
        }
    }

    /**
     * Called by the engine to announce the cancellation of a case
     * @param services a set of custom services to receive the announcement
     * @param id the case id of the cancelled case
     */
    public void announceCaseCancellation(Set<YAWLServiceReference> services,
                                               YIdentifier id) {
        for (YAWLServiceReference service : services) {
            executor.execute(new Handler(service, id, ANNOUNCE_CASE_CANCELLED));
        }
    }


    /**
     * Called by the engine to announce shutdown of the engine's servlet container
     */
    public void shutdown() {
        executor.shutdownNow();
    	  // Nothing else to do - Interface B Clients handle shutdown within their own servlet.
    }


    /**
     * Returns an array of YParameter objects that describe the YAWL service
     * being referenced.
     * @param yawlService the YAWL service reference.
     * @return an array of YParameter objects.
     * @throws IOException if connection problem
     * @throws JDOMException if XML content problem.
     */
    public YParameter[] getRequiredParamsForService(YAWLServiceReference yawlService)
                                                     throws IOException, JDOMException {
        List<YParameter> paramResults = new ArrayList<YParameter>();
        Map<String, String> paramMap = new Hashtable<String, String>();
        paramMap.put("action", "ParameterInfoRequest");
        String parametersAsString = executeGet(yawlService.getURI(), paramMap);

        // above should have returned a xml doc containing params descriptions
        // of required params to operate custom service.
        Element eParams = JDOMUtil.stringToElement(parametersAsString);
        if (eParams != null) {
            List params = eParams.getChildren();
            for (Object o : params) {
                Element paramElem = (Element) o;
                YParameter param = new YParameter(null, paramElem.getName());
                YDecompositionParser.parseParameter(paramElem, param, null, false);
                paramResults.add(param);
            }
        }
        return paramResults.toArray(new YParameter[paramResults.size()]);
    }

    /*******************************************************************************/
    /*******************************************************************************/

    /*
     * This internal class sends the specified announcement and any required
     * parameter values as HTTP POST messages to external custom services
     */

    private class Handler implements Runnable {
        private YWorkItem _workItem;
        private YAWLServiceReference _yawlService;
        private String _command; 
        private YIdentifier _caseID;
        private Document _casedata;
        private String _oldStatus;
        private String _newStatus;
        private int _pingTimeout = 5;

        public Handler(YAWLServiceReference yawlService, YWorkItem workItem, String command) {
            _workItem = workItem;
            _yawlService = yawlService;
            _command = command;
        }

        public Handler(YAWLServiceReference yawlService, YWorkItem workItem,
                       String oldStatus, String newStatus, String command) {
            _workItem = workItem;
            _yawlService = yawlService;
            _command = command;
            _oldStatus = oldStatus;
            _newStatus = newStatus;
        }

        public Handler(YAWLServiceReference yawlService, YIdentifier caseID,
                        Document casedata, String command) {
            _yawlService = yawlService;
            _caseID = caseID;
            _command = command;
            _casedata = casedata;
        }

        public Handler(YAWLServiceReference yawlService, String command) {
            _yawlService = yawlService;
            _command = command;
        }

        public Handler(YAWLServiceReference yawlService, int pingTimeout, String command) {
            _yawlService = yawlService;
            _pingTimeout = pingTimeout;
            _command = command;
        }

        public Handler(YAWLServiceReference yawlService, YIdentifier id, String command) {
            _yawlService = yawlService;
            _caseID = id;
            _command = command;
        }


        /**
         * Load parameter map as required, then POST the message to the custom service
         */
        public void run() {
            try {
                if (ADDWORKITEM_CMD.equals(_command)) {
                    String urlOfYawlService = _yawlService.getURI();
                    String workItemXML = _workItem.toXML();
                    Map<String, String> paramsMap = prepareParamMap("handleEnabledItem", null);
                    paramsMap.put("workItem", workItemXML);
                    executePost(urlOfYawlService, paramsMap);
                }
                else if (CANCELALLWORKITEMS_CMD.equals(_command)) {
                    cancelWorkItem(_yawlService, _workItem);
                    Set<YWorkItem> children = _workItem.getChildren();
                    if (children != null) {
                        for (YWorkItem item : children) {
                            cancelWorkItem(_yawlService, item);
                        }    
                    }
                }
                else if (CANCELWORKITEM_CMD.equals(_command)) {
                    String urlOfYawlService = _yawlService.getURI();
                    String workItemXML = _workItem.toXML();
                    Map<String, String> paramsMap = prepareParamMap("cancelWorkItem", null);
                    paramsMap.put("workItem", workItemXML);
                    executePost(urlOfYawlService, paramsMap);
                }
                else if (ANNOUNCE_COMPLETE_CASE_CMD.equals(_command)) {
                    String urlOfYawlService = _yawlService.getURI();
                    String caseID = _caseID.toString();
                    String casedataStr = JDOMUtil.documentToString(_casedata) ;
                    Map<String, String> paramsMap = prepareParamMap(_command, null);
                    paramsMap.put("caseID", caseID);
                    paramsMap.put("casedata", casedataStr) ;
                    executePost(urlOfYawlService, paramsMap);
                }
                else if (ANNOUNCE_CASE_CANCELLED.equals(_command)) {
                    String urlOfYawlService = _yawlService.getURI();
                    String caseID = _caseID.toString();
                    Map<String, String> paramsMap = prepareParamMap(_command, null);
                    paramsMap.put("caseID", caseID);
                    executePost(urlOfYawlService, paramsMap);
                }
                else if (ANNOUNCE_TIMER_EXPIRY_CMD.equals(_command)) {
                    String urlOfYawlService = _yawlService.getURI();
                    String workItemXML = _workItem.toXML();
                    Map<String, String> paramsMap = prepareParamMap("timerExpiry", null);
                    paramsMap.put("workItem", workItemXML);
                    executePost(urlOfYawlService, paramsMap);
                }
                else if (ANNOUNCE_INIT_ENGINE.equals(_command)) {
                    String urlOfYawlService = _yawlService.getURI();
                    HttpURLValidator.pingUntilAvailable(urlOfYawlService, _pingTimeout);
                    Map<String, String> paramsMap = prepareParamMap(_command, null);
                    executePost(urlOfYawlService, paramsMap);
                }
                else if (ANNOUNCE_ITEM_STATUS.equals(_command)) {
                    String urlOfYawlService = _yawlService.getURI();
                    Map<String, String> paramsMap = prepareParamMap(_command, null);
                    paramsMap.put("workItem", _workItem.toXML());
                    paramsMap.put("oldStatus", _oldStatus);
                    paramsMap.put("newStatus", _newStatus);
                    executePost(urlOfYawlService, paramsMap);
                }
            }
            catch (ConnectException ce) {
                if (ADDWORKITEM_CMD.equals(_command)) {
                    redirectWorkItem(true);
                }
                else if (ANNOUNCE_INIT_ENGINE.equals(_command)) {
                    logger.warn(MessageFormat.format(
                            "Failed to announce engine initialisation to {0} at URI [{1}]",
                            _yawlService.getServiceName(), _yawlService.getURI()));
                }
            }
            catch (IOException e) {

                if (ADDWORKITEM_CMD.equals(_command)) {
                    redirectWorkItem(false);
                }

                // ignore broadcast announcements for missing services
                else if (! (ANNOUNCE_INIT_ENGINE.equals(_command) ||
                       ANNOUNCE_CASE_CANCELLED.equals(_command) ||
                       ANNOUNCE_ITEM_STATUS.equals(_command))) {
                    logger.error("Failed to call YAWL service", e);
                    e.printStackTrace();
                }
            }            
        }

        
        private void redirectWorkItem(boolean connect) {
            YAWLServiceReference defWorklist = YEngine.getInstance().getDefaultWorklist();
            if (defWorklist == null) {
                logger.error(MessageFormat.format(
                        "Could not {0} YAWL Service at URL [{1}] to announce enabled workitem" +
                        " [{2}], and cannot redirect workitem to default worklist handler" +
                        " because there is no default handler known to the engine.",
                        connect ? "connect to" : "find", _yawlService.getURI(),
                        _workItem.getIDString()));
            }
            else if (! defWorklist.getURI().equals(_yawlService.getURI())) {
                logger.warn(MessageFormat.format(
                        "Could not {0} YAWL Service at URL [{1}] to announce enabled workitem" +
                        " [{2}]. Redirecting workitem to default worklist handler.",
                        connect ? "connect to" : "find", _yawlService.getURI(),
                        _workItem.getIDString()));
                YEngine.getInstance().getAnnouncer().rejectAnnouncedEnabledTask(_workItem);
            }
            else {
                logger.error(MessageFormat.format(
                        "Could not announce enabled workitem [{0}] to default worklist " +
                        "handler at URL [{1}]. Either the handler is missing or offline, " +
                        "or the URL is invalid.",
                        _workItem.getIDString(), _yawlService.getURI()));
            }
        }
    }
}
