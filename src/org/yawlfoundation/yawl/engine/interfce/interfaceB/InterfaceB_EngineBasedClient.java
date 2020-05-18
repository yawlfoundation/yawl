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

package org.yawlfoundation.yawl.engine.interfce.interfaceB;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.*;
import org.yawlfoundation.yawl.engine.announcement.YAnnouncement;
import org.yawlfoundation.yawl.engine.announcement.YEngineEvent;
import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.unmarshal.YDecompositionParser;
import org.yawlfoundation.yawl.util.HttpURLValidator;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.io.IOException;
import java.net.ConnectException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.yawlfoundation.yawl.engine.announcement.YEngineEvent.*;


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

    protected static final Logger _logger = LogManager.getLogger(InterfaceB_EngineBasedClient.class);
    private final Map<YAWLServiceReference, ExecutorService> _executorMap = new ConcurrentHashMap<>();


    /**
     * Indicates which protocol this shim services.
     * @return the scheme
     */
    public String getScheme() { return "http"; }

    /**
     * PRE: The work item is enabled.
     * announces a work item to a YAWL Service.
     * @param announcement
     */
    public void announceFiredWorkItem(YAnnouncement announcement) {
        Map<String, String> paramsMap = prepareParamMap(ITEM_ADD);
        paramsMap.put("workItem", announcement.getItem().toXML());
        YAWLServiceReference service = announcement.getYawlService();
        Handler handler = new Handler(service, paramsMap);
        handler.setWorkItem(announcement.getItem());         // needed for possible redirect
        getServiceExecutor(service).execute(handler);
    }


    public void announceCancelledWorkItem(YAnnouncement announcement) {
        YAWLServiceReference yawlService = announcement.getYawlService();
        YWorkItem workItem = announcement.getItem();
        if (workItem.getParent() != null) {
            YWorkItem parent = workItem.getParent();
            cancelWorkItem(yawlService, parent);
            Set<YWorkItem> children = parent.getChildren();
            if (children != null) {
                for (YWorkItem item : children) {
                    cancelWorkItem(yawlService, item);
                }
            }
        }
        else cancelWorkItem(yawlService, workItem);
    }


    /**
     * Announces work item cancellation to the YAWL Service.
     * @param yawlService the YAWL service reference.
     * @param workItem the work item to cancel.
     */
    public void cancelWorkItem(YAWLServiceReference yawlService, YWorkItem workItem) {
        Map<String, String> paramsMap = prepareParamMap(ITEM_CANCEL);
        paramsMap.put("workItem", workItem.toXML());
        getServiceExecutor(yawlService).execute(new Handler(yawlService, paramsMap));
    }


    /**
     * Announces a workitem timer expiry
     * @param announcement the yawl service reference.
     */
    public void announceTimerExpiry(YAnnouncement announcement) {
        Map<String, String> paramsMap = prepareParamMap(TIMER_EXPIRED);
        paramsMap.put("workItem", announcement.getItem().toXML());
        YAWLServiceReference yawlService = announcement.getYawlService();
        getServiceExecutor(yawlService).execute(new Handler(yawlService, paramsMap));
    }


    /**
     * Called by the engine to announce when a case suspends (i.e. becomes fully
     * suspended) as opposed to entering the 'suspending' state.
     */
    public void announceCaseSuspended(Set<YAWLServiceReference> services, YIdentifier caseID) {
        announceCaseSuspensionState(services, caseID, CASE_SUSPENDED);
    }


    /**
     * Called by the engine to announce when a case starts to suspends (i.e. enters the
     * suspending state) as opposed to entering the fully 'suspended' state.
     */
    public void announceCaseSuspending(Set<YAWLServiceReference> services, YIdentifier caseID) {
        announceCaseSuspensionState(services, caseID, CASE_SUSPENDING);
    }

    /**
     * Called by the engine to announce when a case resumes from a previous 'suspending'
     * or 'suspended' state.
     */
    public void announceCaseResumption(Set<YAWLServiceReference> services, YIdentifier caseID) {
        announceCaseSuspensionState(services, caseID, CASE_RESUMED);
    }

    /**
     * Notify of a change of status for a work item.
     *
     * @param workItem  that has changed
     * @param oldStatus previous status
     * @param newStatus new status
     */
    public void announceWorkItemStatusChange(Set<YAWLServiceReference> services,
                                             YWorkItem workItem,
                                             YWorkItemStatus oldStatus,
                                             YWorkItemStatus newStatus) {
        Map<String, String> paramsMap = prepareParamMap(ITEM_STATUS);
        paramsMap.put("workItem", workItem.toXML());
        paramsMap.put("oldStatus", oldStatus.toString());
        paramsMap.put("newStatus", newStatus.toString());
        for (YAWLServiceReference service : services) {
            getServiceExecutor(service).execute(new Handler(service, paramsMap));
        }
    }


    /**
     * Called by engine to announce when a case has commenced.
     *
     * @param services the set of registered custom services
     * @param specID   the specification id of the started case                
     * @param caseID   the case that has started
     * @param launchingService the service that started the case
     * @param delayed true if this is a delayed case launch, false if immediate
     */
    public void announceCaseStarted(Set<YAWLServiceReference> services,
                                    YSpecificationID specID, YIdentifier caseID,
                                    String launchingService, boolean delayed) {
        Map<String, String> paramsMap = prepareParamMap(CASE_START);
        paramsMap.putAll(specID.toMap());
        paramsMap.put("caseID", caseID.toString());
        paramsMap.put("launchingService", launchingService);
        paramsMap.put("delayed", String.valueOf(delayed));
        for (YAWLServiceReference service : services) {
            getServiceExecutor(service).execute(new Handler(service, paramsMap));
        }
    }

    /**
     * Called by engine to announce when a case is complete.
     *
     * @param caseID   the case that completed
     * @param caseData the output data of the case
     */
    public void announceCaseCompletion(Set<YAWLServiceReference> services,
                                       YIdentifier caseID, Document caseData) {
        for (YAWLServiceReference service : services) {
            announceCaseCompletion(service, caseID, caseData);
        }
    }

    /**
     * Called by engine to announce when a case is complete.
     * @param yawlService the yawl service
     * @param caseID the case that completed
     */
    public void announceCaseCompletion(YAWLServiceReference yawlService, 
                                       YIdentifier caseID, Document caseData) {
        Map<String, String> paramsMap = prepareParamMap(CASE_COMPLETE);
        paramsMap.put("caseID", caseID.toString());
        paramsMap.put("casedata", JDOMUtil.documentToString(caseData));
        getServiceExecutor(yawlService).execute(new Handler(yawlService, paramsMap));
    }


    /**
     * Called by the engine when it has completed initialisation and is running
     * @param services a set of custom services to receive the announcement
     * @param maxWaitSeconds the maximum seconds to wait for services to be contactable
     */
    public void announceEngineInitialised(Set<YAWLServiceReference> services, int maxWaitSeconds) {
        Map<String, String> paramsMap = prepareParamMap(ENGINE_INIT);
        paramsMap.put("maxWaitSeconds", String.valueOf(maxWaitSeconds));
        for (YAWLServiceReference service : services) {
            getServiceExecutor(service).execute(new Handler(service, paramsMap));
        }
    }

    /**
     * Called by the engine to announce the cancellation of a case
     * @param services a set of custom services to receive the announcement
     * @param id the case id of the cancelled case
     */
    public void announceCaseCancellation(Set<YAWLServiceReference> services,
                                               YIdentifier id) {
        Map<String, String> paramsMap = prepareParamMap(CASE_CANCELLED);
        paramsMap.put("caseID", id.toString());
        for (YAWLServiceReference service : services) {
            getServiceExecutor(service).execute(new Handler(service, paramsMap));
        }
    }


    /**
     * Called by the engine to announce the deadlock of a case
     * @param services a set of custom services to receive the announcement
     * @param id the case id of the deadlocked case
     * @param tasks the deadlocked tasks
     */
    public void announceDeadlock(Set<YAWLServiceReference> services, YIdentifier id,
                               Set<YTask> tasks) {
        Map<String, String> paramsMap = prepareParamMap(CASE_DEADLOCKED);
        paramsMap.put("caseID", id.toString());
        Set<String> list = new HashSet<String>();
        for (YTask task : tasks) list.add(task.getID());
        paramsMap.put("tasks", list.toString());
        for (YAWLServiceReference service : services) {
            getServiceExecutor(service).execute(new Handler(service, paramsMap));
        }
    }


    /**
     * Called by the engine to announce shutdown of the engine's servlet container
     */
    public void shutdown() {
        HttpURLValidator.cancelAll();
        for (ExecutorService executor : _executorMap.values()) {
            executor.shutdownNow();
        }

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
            for (Element paramElem : eParams.getChildren()) {
                YParameter param = new YParameter(null, paramElem.getName());
                YDecompositionParser.parseParameter(paramElem, param, null, false);
                paramResults.add(param);
            }
        }
        return paramResults.toArray(new YParameter[paramResults.size()]);
    }


    private void announceCaseSuspensionState(Set<YAWLServiceReference> services,
                                         YIdentifier caseID, YEngineEvent event) {
        Map<String, String> paramsMap = prepareParamMap(event);
        paramsMap.put("caseID", caseID.toString());
        for (YAWLServiceReference service : services) {
            getServiceExecutor(service).execute(new Handler(service, paramsMap));
        }
    }

    
    private Map<String, String> prepareParamMap(YEngineEvent event) {
        return super.prepareParamMap(event.label(), null);
    }

    // use a different 2-thread executor for each destination service
    private ExecutorService getServiceExecutor(YAWLServiceReference service) {
        ExecutorService executor = _executorMap.get(service);
        if (executor == null) {
            executor = Executors.newFixedThreadPool(2);
            _executorMap.put(service, executor);
        }
        return executor;
    }


    /*******************************************************************************/
    /*******************************************************************************/

    /*
     * This internal class sends the specified announcement and any required
     * parameter values as HTTP POST messages to external custom services
     */

    private class Handler implements Runnable {

        private final YAWLServiceReference _yawlService;
        private final Map<String, String> _paramsMap ;
        private YWorkItem _workItem;

        public Handler(YAWLServiceReference yawlService, Map<String, String> paramsMap) {
            _yawlService = yawlService;
            _paramsMap = paramsMap;
        }

        void setWorkItem(YWorkItem item) { _workItem = item; }


        /**
         * POST the message to the custom service
         */
         public void run() {
            String event = _paramsMap.get("action");
            try {
                if (event.equals(ENGINE_INIT.label())) {
                    int maxWait = Integer.parseInt(_paramsMap.get("maxWaitSeconds"));
                    HttpURLValidator.pingUntilAvailable(_yawlService.getURI(), maxWait);
                }
                
                executePost(_yawlService.getURI(), _paramsMap);
            }
            catch (ConnectException ce) {
                if (event.equals(ITEM_ADD.label())) {
                    redirectWorkItem(true);
                }
                else if (event.equals(ENGINE_INIT.label())) {
                    try {
                        _logger.warn("Failed to announce engine initialisation to {} at URI {}",
                            _yawlService.getServiceName(), _yawlService.getURI());
                    }
                    catch (IllegalStateException ise) {
                        // can happen on shutdown when the service has already stopped
                        // can be safely suppressed
                    }
                }
            }
            catch (IOException e) {

                if (event.equals(ITEM_ADD.label())) {
                    redirectWorkItem(false);
                }

                // ignore broadcast announcements for missing services
                else if (! YEngineEvent.fromString(event).isBroadcast()) {
                    _logger.warn("Failed to call YAWL service", e);
                }
            }            
        }

        
        private void redirectWorkItem(boolean connect) {
            YAWLServiceReference defWorklist = YEngine.getInstance().getDefaultWorklist();
            if (defWorklist == null) {
                _logger.error("Could not {} YAWL Service at URL {} to announce enabled " +
                        "workitem {}, and cannot redirect workitem to default worklist " +
                        "handler because there is no default handler known to the engine.",
                        connect ? "connect to" : "find", _yawlService.getURI(),
                        _workItem.getIDString());
            }
            else if (! defWorklist.getURI().equals(_yawlService.getURI())) {
                _logger.warn("Could not {} YAWL Service at URL {} to announce enabled workitem" +
                        " {}, or the service responded with an error (check the Tomcat" +
                        " log files for details). Redirecting workitem to default worklist handler.",
                        connect ? "connect to" : "find", _yawlService.getURI(),
                        _workItem.getIDString());
                YEngine.getInstance().getAnnouncer().rejectAnnouncedEnabledTask(_workItem);
            }
            else {
                _logger.error("Could not announce enabled workitem {} to default worklist " +
                        "handler at URL {}. Either the handler is missing or offline, " +
                        "or the URL is invalid.",
                        _workItem.getIDString(), _yawlService.getURI());
            }
        }
    }
}
