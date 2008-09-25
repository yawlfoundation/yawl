/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */

package org.yawlfoundation.yawl.engine.interfce.interfaceB;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.ObserverGateway;
import org.yawlfoundation.yawl.engine.YWorkItem;
import org.yawlfoundation.yawl.engine.YWorkItemStatus;
import org.yawlfoundation.yawl.engine.announcement.Announcements;
import org.yawlfoundation.yawl.engine.announcement.CancelWorkItemAnnouncement;
import org.yawlfoundation.yawl.engine.announcement.NewWorkItemAnnouncement;
import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.unmarshal.YDecompositionParser;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;


/**
 * 
 * @author Lachlan Aldred
 * Date: 22/01/2004
 * Time: 17:19:12
 *
 * @author Michael Adams (refactored for v2.0, 06/2008)
 */
public class InterfaceB_EngineBasedClient extends Interface_Client implements ObserverGateway {
    protected static Logger logger = Logger.getLogger(InterfaceB_EngineBasedClient.class);

    protected static final String ADDWORKITEM_CMD =             "announceWorkItem";
    protected static final String CANCELALLWORKITEMS_CMD =      "cancelAllInstancesUnderWorkItem";
    protected static final String CANCELWORKITEM_CMD =          "cancelWorkItem";
    protected static final String ANNOUNCE_COMPLETE_CASE_CMD =  "announceCompletion";
    protected static final String ANNOUNCE_TIMER_EXPIRY_CMD =   "announceTimerExpiry";
    protected static final String ANNOUNCE_INIT_ENGINE =        "announceEngineInitialised";

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
            Handler myHandler = new Handler(announcement.getYawlService(),
                                            announcement.getItem(), ADDWORKITEM_CMD);
            myHandler.start();
        }
    }

    /**
     * Annonuces work item cancellation to the YAWL Service.
     * @param yawlService the YAWL service reference.
     * @param workItem the work item to cancel.
     */
    static void cancelWorkItem(YAWLServiceReference yawlService, YWorkItem workItem) {
        Handler myHandler = new Handler(yawlService, workItem, "cancelWorkItem");
        myHandler.start();
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
            if (workItem.getParent() == null) {
                Handler myHandler = new Handler(yawlService, workItem,
                                                "cancelAllInstancesUnderWorkItem");
                myHandler.start();
            }
            else {
                Handler myHandler = new Handler(yawlService, workItem.getParent(),
                                                "cancelAllInstancesUnderWorkItem");
                myHandler.start();
            }
        }
    }


    /**
     * Announces a workitem timer expiry
     * @param yawlService the yawl service reference.
     * @param workItem the work item that has expired
     */
    public void announceTimerExpiry(YAWLServiceReference yawlService, YWorkItem workItem) {
        Handler myHandler = new Handler(yawlService, workItem, ANNOUNCE_TIMER_EXPIRY_CMD);
        myHandler.start();
    }



    /**
     * Called by the engine to annouce when a case suspends (i.e. becomes fully
     * suspended as opposed to entering the 'suspending' state.
     */
    public void announceCaseSuspended(YIdentifier caseID)
    {
        //todo MLF: this has been stubbed
    }

    /**
     * Called by the engine to annouce when a case starts to suspends (i.e. enters the
     * suspending state as opposed to entering the fully 'suspended' state.
     */
    public void announceCaseSuspending(YIdentifier caseID)
    {
        //todo MLF: this has been stubbed
    }

    /**
     * Called by the engine to annouce when a case resumes from a previous 'suspending' or 'suspended' state.
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
    public void announceWorkItemStatusChange(YWorkItem workItem, YWorkItemStatus oldStatus, YWorkItemStatus newStatus)
    {
        //todo MLF: this has been stubbed
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
        Handler myHandler = new Handler(yawlService, caseID, casedata, "announceCompletion");
        myHandler.start();
    }

    /**
     * Called by the engine when it has completed initialisation and is running
     */
    public void announceNotifyEngineInitialised(Set<YAWLServiceReference> services) {
        for (YAWLServiceReference service : services) {
            Handler myHandler = new Handler(service, ANNOUNCE_INIT_ENGINE);
            myHandler.start();
        }
    }


    /**
     * Returns an array of YParameter objects that describe the YAWL service
     * being referenced.
     * @param yawlService the YAWL service reference.
     * @return an array of YParameter objects.
     * @throws IOException if connection problem
     * @throws JDOMException if XML content problem.
     */
    public static YParameter[] getRequiredParamsForService(YAWLServiceReference yawlService)
                                                     throws IOException, JDOMException {
        List paramResults = new ArrayList();

        String urlOfYawlService = yawlService.getURI();

        String parametersAsString = executeGet(
                                     urlOfYawlService + "?action=ParameterInfoRequest");
        //above should have returned a xml doc containing params descriptions
        //of required params to operate custom service.
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new StringReader(parametersAsString));
        List paramsASXML = doc.getRootElement().getChildren();
        for (int i = 0; i < paramsASXML.size(); i++) {
            Element paramElem = (Element) paramsASXML.get(i);

            YParameter param = new YParameter(null, paramElem.getName());
            YDecompositionParser.parseParameter(
                    paramElem,
                    param,
                    null,
                    false);
            paramResults.add(param);
        }
        return (YParameter[]) paramResults.toArray(new YParameter[paramResults.size()]);
    }

    /*******************************************************************************/
    /*******************************************************************************/
    
    static class Handler extends Thread {
        private YWorkItem _workItem;
        private YAWLServiceReference _yawlService;
        private String _command; 
        private YIdentifier _caseID;
        private Document _casedata;

        public Handler(YAWLServiceReference yawlService, YWorkItem workItem, String command) {
            _workItem = workItem;
            _yawlService = yawlService;
            _command = command;
        }

        public Handler(YAWLServiceReference yawlService, YIdentifier caseID,
                        Document casedata, String command) {
            _yawlService = yawlService;
            _caseID = caseID;
            _command = command;
            _casedata = casedata;
        }

        public Handler(YAWLServiceReference yawlService,  String command) {
            _yawlService = yawlService;
            _command = command;
        }

        private Map<String, String> prepareParamMap(String action) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("action", action);
            return map;
        }

        public void run() {
            try {
                if (ADDWORKITEM_CMD.equals(_command)) {
                    String urlOfYawlService = _yawlService.getURI();
                    String workItemXML = _workItem.toXML();
                    Map<String, String> paramsMap = prepareParamMap("handleEnabledItem");
                    paramsMap.put("workItem", workItemXML);
                    executePost(urlOfYawlService, paramsMap);
                }
                else if (CANCELALLWORKITEMS_CMD.equals(_command)) {
                    cancelWorkItem(_yawlService, _workItem);
                    Set children = _workItem.getChildren();
                    if (children != null) {
                        Iterator iter = children.iterator();
                        while (iter.hasNext()) {
                            YWorkItem item = (YWorkItem) iter.next();
                            cancelWorkItem(_yawlService, item);
                        }    
                    }
                }
                else if (CANCELWORKITEM_CMD.equals(_command)) {
                    String urlOfYawlService = _yawlService.getURI();
                    String workItemXML = _workItem.toXML();
                    Map<String, String> paramsMap = prepareParamMap("cancelWorkItem");
                    paramsMap.put("workItem", workItemXML);
                    executePost(urlOfYawlService, paramsMap);
                }
                else if (ANNOUNCE_COMPLETE_CASE_CMD.equals(_command)) {
                    String urlOfYawlService = _yawlService.getURI();
                    String caseID = _caseID.toString();
                    String casedataStr = JDOMUtil.documentToString(_casedata) ;
                    Map<String, String> paramsMap = prepareParamMap(_command);
                    paramsMap.put("caseID", caseID);
                    paramsMap.put("casedata", casedataStr) ;
                    executePost(urlOfYawlService, paramsMap);
                }
                else if (ANNOUNCE_TIMER_EXPIRY_CMD.equals(_command)) {
                    String urlOfYawlService = _yawlService.getURI();
                    String workItemXML = _workItem.toXML();
                    Map<String, String> paramsMap = prepareParamMap("timerExpiry");
                    paramsMap.put("workItem", workItemXML);
                    executePost(urlOfYawlService, paramsMap);
                }
                else if (ANNOUNCE_INIT_ENGINE.equals(_command)) {
                    String urlOfYawlService = _yawlService.getURI();
                    Map<String, String> paramsMap = prepareParamMap(_command);
                    executePost(urlOfYawlService, paramsMap);
                }
            } catch (IOException e) {

                // ignore initialisation announcements execeptions for missing services 
                if (! ANNOUNCE_INIT_ENGINE.equals(_command)) {
                    logger.error("failed to call YAWL service", e);
                    e.printStackTrace();
                }
            }            
        }
    }
}
