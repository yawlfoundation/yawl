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

package org.yawlfoundation.yawl.engine;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.yawlfoundation.yawl.authentication.YSession;
import org.yawlfoundation.yawl.elements.YAWLServiceGateway;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.announcement.AnnouncementContext;
import org.yawlfoundation.yawl.engine.announcement.YAnnouncement;
import org.yawlfoundation.yawl.engine.announcement.YEngineEvent;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceB_EngineBasedClient;
import org.yawlfoundation.yawl.engine.interfce.interfaceX.InterfaceX_EngineSideClient;
import org.yawlfoundation.yawl.exceptions.YAWLException;
import org.yawlfoundation.yawl.exceptions.YStateException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.yawlfoundation.yawl.engine.announcement.YEngineEvent.*;

/**
 * Handles the announcement of engine-generated events to the environment.
 *
 * @author Michael Adams
 * @date 10/04/2010
 */
public class YAnnouncer {

    private final ObserverGatewayController _controller;
    private final YEngine _engine;
    private final Logger _logger;
    private final Set<InterfaceX_EngineSideClient> _interfaceXListeners;

    private AnnouncementContext _announcementContext;


    protected YAnnouncer(YEngine engine) {
        _engine = engine;
        _logger = Logger.getLogger(this.getClass());
        _interfaceXListeners = new HashSet<InterfaceX_EngineSideClient>();
        _announcementContext = AnnouncementContext.NORMAL;
        _controller = new ObserverGatewayController();

        // Initialise the standard Observer Gateway.
        // Currently the only standard gateway is the HTTP driven IB Servlet client.
        try {
            _controller.addGateway(new InterfaceB_EngineBasedClient());
        }
        catch (YAWLException ye) {
            _logger.warn("Failed to register default observer gateway. The Engine " +
                    "may be unable to send notifications to services!", ye);
        }
    }

    public ObserverGatewayController getObserverGatewayController() {
        return _controller;
    }


    public synchronized void registerInterfaceBObserverGateway(ObserverGateway gateway)
            throws YAWLException {
        boolean firstGateway = _controller.isEmpty();
        _controller.addGateway(gateway);
        if (firstGateway) rennounceRestoredItems();
    }


    protected AnnouncementContext getAnnouncementContext() {
        return _announcementContext;
    }


    private void setAnnouncementContext(AnnouncementContext context) {
        _announcementContext = context;
    }


    /******************************************************************************/
    // INTERFACE X LISTENER MGT //

    protected void addInterfaceXListener(InterfaceX_EngineSideClient listener) {
        _interfaceXListeners.add(listener);
    }

    protected void addInterfaceXListener(String uri) {
        addInterfaceXListener(new InterfaceX_EngineSideClient(uri));
    }

    protected boolean removeInterfaceXListener(InterfaceX_EngineSideClient listener) {
        return _interfaceXListeners.remove(listener);
    }

    protected boolean removeInterfaceXListener(String uri) {
        for (InterfaceX_EngineSideClient listener : _interfaceXListeners) {
            if (listener.getURI().equals(uri)) {
                return removeInterfaceXListener(listener);
            }
        }
        return false;
    }

    protected boolean hasInterfaceXListeners() {
        return ! _interfaceXListeners.isEmpty();
    }


    protected void shutdownInterfaceXListeners() {
        for (InterfaceX_EngineSideClient listener : _interfaceXListeners) {
            listener.shutdown();
        }
    }


    /******************************************************************************/
    // INTERFACE B (OBSERVER GATEWAY) ANNOUNCEMENTS //

    /**
     * Called by the engine when its initialisation is complete. Broadcast to all
     * registered services and gateways.
     * @param services the set of currently registered services
     * @param maxWaitSeconds timeout to announce to each service before giving up
     */
    protected void announceEngineInitialisationCompletion(
            Set<YAWLServiceReference> services, int maxWaitSeconds) {
        _controller.notifyEngineInitialised(services, maxWaitSeconds);
    }


    /**
     * Called by the engine when a case is cancelled. Broadcast to all registered
     * services and gateways.
     * @param id the identifier of the cancelled case
     * @param services the set of currently registered services
     */
    protected void announceCaseCancellation(YIdentifier id, Set<YAWLServiceReference> services) {
        _controller.notifyCaseCancellation(services, id);
        announceCaseCancellationToInterfaceXListeners(id);
    }


    /**
     * Called by YWorkItemTimer when a work item's timer expires. Announced only to
     * the designated service or gateway.
     * @param item the work item that has had its timer expire
     */
    public void announceTimerExpiryEvent(YWorkItem item) {
        announceToGateways(createAnnouncement(item, TIMER_EXPIRED));
        announceTimerExpiryToInterfaceXListeners(item);
        _engine.getInstanceCache().setTimerExpired(item);
    }


    /**
     * Called by the engine when a case is suspending. Broadcast to all registered
     * services and gateways.
     * @param id the identifier of the suspending case
     * @param services the set of currently registered services
     */
    protected void announceCaseSuspending(YIdentifier id, Set<YAWLServiceReference> services) {
        _controller.notifyCaseSuspending(id, services);
    }


    /**
     * Called by the engine when a case has suspended. Broadcast to all registered
     * services and gateways.
     * @param id the identifier of the suspended case
     * @param services the set of currently registered services
     */
    protected void announceCaseSuspended(YIdentifier id, Set<YAWLServiceReference> services) {
        _controller.notifyCaseSuspended(id, services);
    }


    /**
     * Called by the engine when a case has resumed from suspension. Broadcast to all
     * registered services and gateways.
     * @param id the identifier of the resumed case
     * @param services the set of currently registered services
     */
    protected void announceCaseResumption(YIdentifier id, Set<YAWLServiceReference> services) {
        _controller.notifyCaseResumption(id, services);
    }
    
    
    protected void announceCaseStart(YSpecificationID specID, YIdentifier caseID,
                                     String launchingService, boolean delayed) {

        // if delayed, service string is uri, if not its a current sessionhandle
        if (! delayed) {
            YSession session = _engine.getSessionCache().getSession(launchingService);
            launchingService = (session != null) ? session.getURI() : null;
        }
        _controller.notifyCaseStarting(_engine.getYAWLServices(), specID, caseID,
                launchingService, delayed);
    }
 

    /**
     * Called by a case's net runner when it completes. Announced only to the designated
     * service or gateway when the 'service' parameter is not null, otherwise it is
     * broadcast to all registered services and gateways.
     * @param service the name of the service or gateway to announce the case completion
     * to. If null, the event will be announced to all registered services and gateways
     * @param caseID the identifier of the completed case
     * @param caseData the final output data for the case
     */
    protected void announceCaseCompletion(YAWLServiceReference service,
                                          YIdentifier caseID, Document caseData) {
        if (service == null) {
            _controller.notifyCaseCompletion(_engine.getYAWLServices(), caseID, caseData);
        }
        else _controller.notifyCaseCompletion(service, caseID, caseData);
    }


    /**
     * Called by a workitem when it has a change of status. Broadcast to all
     * registered services and gateways.
     * @param item the work item that has had a change of status
     * @param oldStatus the previous status of the work item
     * @param newStatus the new status of the workitem
     */
    protected void announceWorkItemStatusChange(YWorkItem item, YWorkItemStatus oldStatus,
                                             YWorkItemStatus newStatus) {
        debug("Announcing workitem status change from '", oldStatus + "' to new status '",
                newStatus + "' for workitem '", item.getWorkItemID().toString(), "'.");
        _controller.notifyWorkItemStatusChange(_engine.getYAWLServices(), item,
                oldStatus, newStatus);
    }


    /**
     * Called by a workitem when it is cancelled. Announced only to the designated
     * service or gateway.
     * @param item the work item that has had a change of status
     */
    public void announceCancelledWorkItem(YWorkItem item) {
        announceToGateways(createAnnouncement(item, ITEM_CANCEL));
    }


    /**
     * Called by the engine when it begins to shutdown. Broadcast to all registered
     * services and gateways.
     */
    protected void shutdownObserverGateways() {
    	  _controller.shutdownObserverGateways();
    }


    /**
     * Called by the engine each time a net runner advances a case, resulting in the
     * enabling and/or cancelling of one or more work items.
     * @param announcements A set of work item enabling or cancellation events
     */
    protected void announceToGateways(Set<YAnnouncement> announcements) {
        if (announcements != null) {
            debug("Announcing ", announcements.size() + " events.");
            _controller.announce(announcements);
        }
    }


    private void announceToGateways(YAnnouncement announcement) {
        if (announcement != null) {
            debug("Announcing one event.");
            _controller.announce(announcement);
        }
    }


    protected YAnnouncement createAnnouncement(YAWLServiceReference ys, YWorkItem item,
                                               YEngineEvent event) {
        if (ys == null) ys = _engine.getDefaultWorklist();
        return (ys != null) ?
                new YAnnouncement(ys, item, event, getAnnouncementContext()) : null;
    }


    protected YAnnouncement createAnnouncement(YWorkItem item, YEngineEvent event) {
        YTask task = item.getTask();
        if ((task != null) && (task.getDecompositionPrototype() != null)) {
            YAWLServiceGateway wsgw = (YAWLServiceGateway) task.getDecompositionPrototype();
            if (wsgw != null) {
                return createAnnouncement(wsgw.getYawlService(), item, event);
            }
        }
        return null;
    }


    // this method should be called by an IB service when it decides it is not going
    // to handle (i.e. checkout) a workitem announced to it. It passes the workitem to
    // the default worklist service for normal assignment.
    public void rejectAnnouncedEnabledTask(YWorkItem item) {
        YAWLServiceReference defaultWorklist = _engine.getDefaultWorklist();
        if (defaultWorklist != null) {
            debug("Announcing enabled task ", item.getIDString(), " on service ",
                          defaultWorklist.getServiceID());
            announceToGateways(createAnnouncement(defaultWorklist, item, ITEM_ADD));
        }
    }


    /******************************************************************************/
    // INTERFACE X ANNOUNCEMENTS //

    protected void announceCheckWorkItemConstraints(YWorkItem item, Document data,
                                                    boolean preCheck) {
        for (InterfaceX_EngineSideClient listener : _interfaceXListeners) {
            debug("Announcing Check Constraints for task ", item.getIDString(),
                         " on client ", listener.toString());
            listener.announceCheckWorkItemConstraints(item, data, preCheck);
        }
    }


    protected void announceCheckCaseConstraints(YSpecificationID specID, String caseID,
                                                String data, boolean preCheck) {
        for (InterfaceX_EngineSideClient listener : _interfaceXListeners) {
            debug("Announcing Check Constraints for case ", caseID,
                         " on client ", listener.toString());
            listener.announceCheckCaseConstraints(specID, caseID, data, preCheck);
        }
    }


    protected void announceTimeServiceExpiry(YWorkItem item, List timeOutTaskIds) {
        for (InterfaceX_EngineSideClient listener : _interfaceXListeners) {
            debug("Announcing Time Out for item ", item.getWorkItemID().toString(),
                    " on client ", listener.toString());
            listener.announceTimeOut(item, timeOutTaskIds);
        }
    }


    private void announceCaseCancellationToInterfaceXListeners(YIdentifier caseID) {
        for (InterfaceX_EngineSideClient listener : _interfaceXListeners) {
            debug("Announcing Cancel Case for case ", caseID.toString(),
                         " on client ",  listener.toString());
            listener.announceCaseCancellation(caseID.get_idString());
        }
    }


    private void announceTimerExpiryToInterfaceXListeners(YWorkItem item) {
        for (InterfaceX_EngineSideClient listener : _interfaceXListeners) {
            listener.announceTimeOut(item, null);
        }
    }


    /******************************************************************************/
    // WORKITEM REANNOUNCEMENTS //

    private void rennounceRestoredItems() {

        //MLF: moved from restore logic. There is no point in reannouncing before the first gateway
        //     is registered as the announcements will simply fall on deaf errors. Obviously we
        //     also don't want to do it everytime either!
        int sum = 0;
        _logger.info("Detected first gateway registration. Reannouncing all work items.");

        setAnnouncementContext(AnnouncementContext.RECOVERING);

        try {
            _logger.info("Reannouncing all enabled workitems");
            int itemsReannounced = reannounceEnabledWorkItems();
            _logger.info("" + itemsReannounced + " enabled workitems reannounced");
            sum += itemsReannounced;

            _logger.info("Reannouncing all executing workitems");
            itemsReannounced = reannounceExecutingWorkItems();
            _logger.info("" + itemsReannounced + " executing workitems reannounced");
            sum += itemsReannounced;

            _logger.info("Reannouncing all fired workitems");
            itemsReannounced = reannounceFiredWorkItems();
            _logger.info("" + itemsReannounced + " fired workitems reannounced");
            sum += itemsReannounced;
        }
        catch (YStateException e) {
            _logger.error("Failure whilst reannouncing workitems. " +
                    "Some workitems might not have been reannounced.", e);
        }

        setAnnouncementContext(AnnouncementContext.NORMAL);
        _logger.info("Reannounced " + sum + " workitems in total.");
    }


    /**
     * Causes the engine to re-announce all workitems which are in an "enabled" state.<P>
     *
     * @return The number of enabled workitems that were reannounced
     */
    public int reannounceEnabledWorkItems() throws YStateException {
        debug("--> reannounceEnabledWorkItems");
        return reannounceWorkItems(_engine.getWorkItemRepository().getEnabledWorkItems());
    }


    /**
     * Causes the engine to re-announce all workitems which are in an "executing" state.<P>
     *
     * @return The number of executing workitems that were reannounced
     */
    protected int reannounceExecutingWorkItems() throws YStateException {
        debug("--> reannounceExecutingWorkItems");
        return reannounceWorkItems(_engine.getWorkItemRepository().getExecutingWorkItems());
    }


    /**
     * Causes the engine to re-announce all workitems which are in an "fired" state.<P>
     *
     * @return The number of fired workitems that were reannounced
     */
    protected int reannounceFiredWorkItems() throws YStateException {
        debug("--> reannounceFiredWorkItems");
        return reannounceWorkItems(_engine.getWorkItemRepository().getFiredWorkItems());
    }


    private int reannounceWorkItems(Set<YWorkItem> workItems) throws YStateException {
        Set<YAnnouncement> announcements = new HashSet<YAnnouncement>();
        for (YWorkItem workitem : workItems) {
            YAnnouncement announcement = createAnnouncement(workitem, ITEM_ADD);
            if (announcement != null) announcements.add(announcement);
        }
        announceToGateways(announcements);
        return workItems.size();
    }


    /**
     * Causes the engine to re-announce a specific workitem regardless of state.<P>
     */
    protected void reannounceWorkItem(YWorkItem workItem) throws YStateException {
        debug("--> reannounceWorkItem: WorkitemID=" + workItem.getWorkItemID().toString());
        announceToGateways(createAnnouncement(workItem, ITEM_ADD));
        debug("<-- reannounceEnabledWorkItem");
    }


    protected void announceDeadlock(YIdentifier caseID, Set<YTask> tasks) {
        _controller.notifyDeadlock(_engine.getYAWLServices(), caseID, tasks);
    }

    /****************************************************************************/

    private void debug(final String... phrases) {
        if (_logger.isDebugEnabled()) {
            if (phrases.length == 1) {
                _logger.debug(phrases[0]);
            }
            else {
                StringBuilder msg = new StringBuilder();
                for (String phrase : phrases) {
                    msg.append(phrase);
                }
                _logger.debug(msg.toString());
            }
        }
    }

}
