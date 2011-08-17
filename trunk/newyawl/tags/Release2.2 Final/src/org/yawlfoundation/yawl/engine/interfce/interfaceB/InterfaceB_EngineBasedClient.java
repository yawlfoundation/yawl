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
import org.yawlfoundation.yawl.engine.announcement.YAnnouncement;
import org.yawlfoundation.yawl.engine.announcement.YEngineEvent;
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

    protected static final Logger _logger = Logger.getLogger(InterfaceB_EngineBasedClient.class);
    private static final ExecutorService _executor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


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
        _executor.execute(new Handler(announcement.getYawlService(),
                announcement.getItem(), ITEM_ADD));
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
        _executor.execute(new Handler(yawlService, workItem, ITEM_CANCEL));
    }


    /**
     * Announces a workitem timer expiry
     * @param announcement the yawl service reference.
     */
    public void announceTimerExpiry(YAnnouncement announcement) {
        YAWLServiceReference yawlService = announcement.getYawlService();
        YWorkItem workItem = announcement.getItem();
        _executor.execute(new Handler(yawlService, workItem, TIMER_EXPIRED));
    }


    /**
     * Called by the engine to announce when a case suspends (i.e. becomes fully
     * suspended) as opposed to entering the 'suspending' state.
     */
    public void announceCaseSuspended(Set<YAWLServiceReference> services, YIdentifier caseID) {
        for (YAWLServiceReference service : services) {
            _executor.execute(new Handler(service, caseID, CASE_SUSPENDED));
        }
    }

    /**
     * Called by the engine to announce when a case starts to suspends (i.e. enters the
     * suspending state) as opposed to entering the fully 'suspended' state.
     */
    public void announceCaseSuspending(Set<YAWLServiceReference> services, YIdentifier caseID) {
        for (YAWLServiceReference service : services) {
            _executor.execute(new Handler(service, caseID, CASE_SUSPENDING));
        }
    }

    /**
     * Called by the engine to announce when a case resumes from a previous 'suspending'
     * or 'suspended' state.
     */
    public void announceCaseResumption(Set<YAWLServiceReference> services, YIdentifier caseID) {
        for (YAWLServiceReference service : services) {
            _executor.execute(new Handler(service, caseID, CASE_RESUMED));
        }
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
        for (YAWLServiceReference service : services) {
            _executor.execute(new Handler(service, workItem, oldStatus.toString(),
                                            newStatus.toString(), ITEM_STATUS));
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
        _executor.execute(new Handler(yawlService, caseID, caseData, CASE_COMPLETE));
    }

    /**
     * Called by the engine when it has completed initialisation and is running
     * @param services a set of custom services to receive the announcement
     * @param maxWaitSeconds the maximum seconds to wait for services to be contactable
     */
    public void announceEngineInitialised(Set<YAWLServiceReference> services, int maxWaitSeconds) {
        for (YAWLServiceReference service : services) {
            _executor.execute(new Handler(service, maxWaitSeconds, ENGINE_INIT));
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
            _executor.execute(new Handler(service, id, CASE_CANCELLED));
        }
    }


    /**
     * Called by the engine to announce shutdown of the engine's servlet container
     */
    public void shutdown() {
        _executor.shutdownNow();

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
        private YEngineEvent _command;
        private YIdentifier _caseID;
        private Document _caseData;
        private String _oldStatus;
        private String _newStatus;
        private int _pingTimeout = 5;

        public Handler(YAWLServiceReference yawlService, YWorkItem workItem, YEngineEvent command) {
            _workItem = workItem;
            _yawlService = yawlService;
            _command = command;
        }

        public Handler(YAWLServiceReference yawlService, YWorkItem workItem,
                       String oldStatus, String newStatus, YEngineEvent command) {
            _workItem = workItem;
            _yawlService = yawlService;
            _command = command;
            _oldStatus = oldStatus;
            _newStatus = newStatus;
        }

        public Handler(YAWLServiceReference yawlService, YIdentifier caseID,
                        Document casedata, YEngineEvent command) {
            _yawlService = yawlService;
            _caseID = caseID;
            _command = command;
            _caseData = casedata;
        }

        public Handler(YAWLServiceReference yawlService, int pingTimeout, YEngineEvent command) {
            _yawlService = yawlService;
            _pingTimeout = pingTimeout;
            _command = command;
        }

        public Handler(YAWLServiceReference yawlService, YIdentifier id, YEngineEvent command) {
            _yawlService = yawlService;
            _caseID = id;
            _command = command;
        }


        /**
         * Load parameter map as required, then POST the message to the custom service
         */
        public void run() {
            Map<String, String> paramsMap = prepareParamMap(_command.label(), null);
            if (_workItem != null) paramsMap.put("workItem", _workItem.toXML());
            if (_caseID != null) paramsMap.put("caseID", _caseID.toString());
            try {
                switch (_command) {
                    case ITEM_STATUS: {
                        paramsMap.put("oldStatus", _oldStatus);
                        paramsMap.put("newStatus", _newStatus);
                        break;
                    }
                    case CASE_COMPLETE: {
                        paramsMap.put("casedata", JDOMUtil.documentToString(_caseData));
                        break;
                    }
                    case ENGINE_INIT: {
                        HttpURLValidator.pingUntilAvailable(_yawlService.getURI(), _pingTimeout);
                        break;
                    }
                }
                executePost(_yawlService.getURI(), paramsMap);
            }
            catch (ConnectException ce) {
                if (_command == ITEM_ADD) {
                    redirectWorkItem(true);
                }
                else if (_command == ENGINE_INIT) {
                    _logger.warn(MessageFormat.format(
                            "Failed to announce engine initialisation to {0} at URI [{1}]",
                            _yawlService.getServiceName(), _yawlService.getURI()));
                }
            }
            catch (IOException e) {

                if (_command == ITEM_ADD) {
                    redirectWorkItem(false);
                }

                // ignore broadcast announcements for missing services
                else if (! _command.isBroadcast()) {
                    _logger.warn("Failed to call YAWL service", e);
                }
            }            
        }

        
        private void redirectWorkItem(boolean connect) {
            YAWLServiceReference defWorklist = YEngine.getInstance().getDefaultWorklist();
            if (defWorklist == null) {
                _logger.error(MessageFormat.format(
                        "Could not {0} YAWL Service at URL [{1}] to announce enabled workitem" +
                        " [{2}], and cannot redirect workitem to default worklist handler" +
                        " because there is no default handler known to the engine.",
                        connect ? "connect to" : "find", _yawlService.getURI(),
                        _workItem.getIDString()));
            }
            else if (! defWorklist.getURI().equals(_yawlService.getURI())) {
                _logger.warn(MessageFormat.format(
                        "Could not {0} YAWL Service at URL [{1}] to announce enabled workitem" +
                        " [{2}]. Redirecting workitem to default worklist handler.",
                        connect ? "connect to" : "find", _yawlService.getURI(),
                        _workItem.getIDString()));
                YEngine.getInstance().getAnnouncer().rejectAnnouncedEnabledTask(_workItem);
            }
            else {
                _logger.error(MessageFormat.format(
                        "Could not announce enabled workitem [{0}] to default worklist " +
                        "handler at URL [{1}]. Either the handler is missing or offline, " +
                        "or the URL is invalid.",
                        _workItem.getIDString(), _yawlService.getURI()));
            }
        }
    }
}
