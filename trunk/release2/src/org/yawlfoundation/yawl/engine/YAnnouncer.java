/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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
import org.jdom.Document;
import org.yawlfoundation.yawl.elements.YAWLServiceGateway;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.YAtomicTask;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.announcement.AnnouncementContext;
import org.yawlfoundation.yawl.engine.announcement.Announcements;
import org.yawlfoundation.yawl.engine.announcement.CancelWorkItemAnnouncement;
import org.yawlfoundation.yawl.engine.announcement.NewWorkItemAnnouncement;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceB_EngineBasedClient;
import org.yawlfoundation.yawl.engine.interfce.interfaceX.InterfaceX_EngineSideClient;
import org.yawlfoundation.yawl.exceptions.YStateException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: Michael Adams
 * Creation Date: 10/04/2010
 */
public class YAnnouncer {

    private ObserverGatewayController _controller;
    private Logger _logger;
    private boolean _itemsAlreadyAnnounced;
    private AnnouncementContext _announcementContext;
    private YEngine _engine;
    private Set<InterfaceX_EngineSideClient> _interfaceXListeners;

    /**
     * Reannouncement Contexts:
     *  Normal =  posted due to an extra-engine request.
     *  Recovering = posted due to restart processing within the engine. Note: In this
     *  context, the underlying engine status may be running rather than initialising!
     */
    public static final int REANNOUNCEMENT_CONTEXT_NORMAL  = 0;
    public static final int REANNOUNCEMENT_CONTEXT_RECOVERING = 1;


    public YAnnouncer(YEngine engine) {
        _engine = engine;
        _logger = Logger.getLogger(this.getClass());

        // Initialise the standard Observer Gateways.
        // Currently the only standard gateway is the HTTP driven Servlet client.        
        _controller = new ObserverGatewayController();
        _controller.addGateway(new InterfaceB_EngineBasedClient());

        _interfaceXListeners = new HashSet<InterfaceX_EngineSideClient>();
    }

    public ObserverGatewayController getObserverGatewayController() {
        return _controller;
    }

    public void setObserverGatewayController(ObserverGatewayController controller) {
        _controller = controller;
    }


    public void addInterfaceXListener(InterfaceX_EngineSideClient listener) {
        _interfaceXListeners.add(listener);
    }

    public void addInterfaceXListener(String uri) {
        addInterfaceXListener(new InterfaceX_EngineSideClient(uri));
    }

    public boolean removeInterfaceXListener(InterfaceX_EngineSideClient listener) {
        return _interfaceXListeners.remove(listener);
    }

    public boolean removeInterfaceXListener(String uri) {
        for (InterfaceX_EngineSideClient listener : _interfaceXListeners) {
            if (listener.getURI().equals(uri)) {
                return removeInterfaceXListener(listener);
            }
        }
        return false;
    }

    public boolean hasInterfaceXListeners() {
        return ! _interfaceXListeners.isEmpty();
    }

    private void setAnnouncementContext(AnnouncementContext context) {
        _announcementContext = context;
    }


    public  AnnouncementContext getAnnouncementContext() {
        return _announcementContext;
    }

    public void notifyServletInitialisationComplete() {
        announceEngineInitialisationCompletion();
    }



    public synchronized void registerInterfaceBObserverGateway(ObserverGateway gateway) {

        _controller.addGateway(gateway);

        //MLF: moved from restore logic. There is no point in reannouncing before the first gateway
        //     is registered as the announcements will simply fall on deaf errors. Obviously we
        //     also don't want to do it everytime either!
        if (! _itemsAlreadyAnnounced) {
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
            _itemsAlreadyAnnounced = true;
        }
    }


    protected void announceTasks(Announcements<NewWorkItemAnnouncement> announcements) {
        debug("Announcing ", announcements.size() + " workitems.");
        _controller.notifyAddWorkItems(announcements);
    }


    public void notifyCaseSuspending(YIdentifier id) {
        _controller.notifyCaseSuspending(id);
    }

    public void notifyCaseSuspended(YIdentifier id) {
        _controller.notifyCaseSuspended(id);
    }

    public void notifyCaseResumption(YIdentifier id) {
        _controller.notifyCaseResumption(id);
    }

    public void announceCancellationToEnvironment(Announcements<CancelWorkItemAnnouncement> announcements) {
        debug("Announcing ", announcements.size() + " cancelled workitems.");
        _controller.notifyRemoveWorkItems(announcements);
    }

    public void announceEngineInitialisationCompletion() {
        _controller.notifyEngineInitialised(_engine.getYAWLServices());
    }

    public void announceCaseCancellationToEnvironment(YIdentifier id) {
        _controller.notifyCaseCancellation(_engine.getYAWLServices(), id);
        announceCaseCancellationToInterfaceXListeners(id);
    }


    protected void announceCaseCompletionToEnvironment(YAWLServiceReference yawlService,
                                                       YIdentifier caseID, Document casedata) {
        _controller.notifyCaseCompletion(yawlService, caseID, casedata);
    }

    protected void announceCaseCompletionToEnvironment(YIdentifier caseID, Document casedata) {
        _controller.notifyCaseCompletion(caseID, casedata);
    }

    public void announceWorkItemStatusChange(YWorkItem workItem, YWorkItemStatus oldStatus,
                                             YWorkItemStatus newStatus) {
        debug("Announcing workitem status change from '", oldStatus + "' to new status '",
                newStatus + "' for workitem '", workItem.getWorkItemID().toString(), "'.");
        _controller.notifyWorkItemStatusChange(workItem, oldStatus, newStatus);
    }


    public int reannounceWorkItems(Set<YWorkItem> workItems) throws YStateException {
        for (YWorkItem workitem : workItems) {
            reannounceWorkItem(workitem);
        }
        return workItems.size();
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
    public int reannounceExecutingWorkItems() throws YStateException {
        debug("--> reannounceExecutingWorkItems");
        return reannounceWorkItems(_engine.getWorkItemRepository().getExecutingWorkItems());
    }


    /**
     * Causes the engine to re-announce all workitems which are in an "fired" state.<P>
     *
     * @return The number of fired workitems that were reannounced
     */
    public int reannounceFiredWorkItems() throws YStateException {
        debug("--> reannounceFiredWorkItems");
        return reannounceWorkItems(_engine.getWorkItemRepository().getFiredWorkItems());
    }

    /**
     * Causes the engine to re-announce a specific workitem regardless of state.<P>
     */
    public void reannounceWorkItem(YWorkItem workItem) throws YStateException
    {
        debug("--> reannounceWorkItem: WorkitemID=" + workItem.getWorkItemID().getTaskID());

        YNetRunner netRunner = _engine.getNetRunner(workItem);
        if (netRunner != null) {
            announceToEnvironment(workItem, netRunner.get_caseIDForNet());
        }

        debug("<-- reannounceEnabledWorkItem");
    }


    public void announceToEnvironment(YWorkItem workItem, YIdentifier caseID) {
        debug("--> announceToEnvironment");

        YTask task = _engine.getTaskDefinition(workItem.getSpecificationID(), workItem.getTaskID());
        YAtomicTask atomicTask = (YAtomicTask) task;
        YAWLServiceGateway wsgw = (YAWLServiceGateway) atomicTask.getDecompositionPrototype();
        if (wsgw != null) {
            YAWLServiceReference ys = wsgw.getYawlService();
            if (ys != null) {
                YWorkItem item = _engine.getWorkItemRepository().get(caseID.toString(),
                        atomicTask.getID());
                if (item == null) {
                    throw new RuntimeException("Unable to find YWorKItem for atomic task '" +
                            atomicTask.getID() + "' of case '" + caseID + "'.");
                }
                if (item.getStatus() == YWorkItemStatus.statusIsParent) item.add_child(item);

                try {
                    Announcements<NewWorkItemAnnouncement> items =
                            new Announcements<NewWorkItemAnnouncement>();
                    items.addAnnouncement(new NewWorkItemAnnouncement(ys, item,
                            getAnnouncementContext()));
                    announceTasks(items);
                }
                catch (YStateException e) {
                    _logger.error("Failed to announce task '" + atomicTask.getID() +
                            "' of case '" + caseID + "': ", e);
                }
            }
            else _logger.warn("No YawlService defined, unable to announce task '" +
                    atomicTask.getID() + "' of case '" + caseID + "'.");
        }
        else _logger.warn("No YAWLServiceGateway defined, unable to announce task '" +
                atomicTask.getID() + "' of case '" + caseID + "'.");

        debug("<-- announceToEnvironment");
    }


    // this method should be called by an IB service when it decides it is not going
    // to handle (i.e. checkout) a workitem announced to it. It passes the workitem to
    // the default worklist service for normal assignment.
    public void rejectAnnouncedEnabledTask(YWorkItem item) {
        YAWLServiceReference defaultWorklist = _engine.getDefaultWorklist();
        if (_engine.getDefaultWorklist() != null) {
            debug("Announcing enabled task ", item.getIDString(), " on service ",
                          defaultWorklist.getServiceID());
            try {
                Announcements<NewWorkItemAnnouncement> items =
                                 new Announcements<NewWorkItemAnnouncement>();
                items.addAnnouncement(new NewWorkItemAnnouncement(
                        defaultWorklist, item, getAnnouncementContext()));
                announceTasks(items);
            }
            catch (YStateException yse) {
                _logger.error("Failed to announce enablement of workitem '" +
                               item.getIDString() + "': ", yse);
            }
        }
    }


    public NewWorkItemAnnouncement createNewWorkItemAnnouncement(YAWLServiceReference ys,
                                                                 YWorkItem item) {
        if (ys == null) ys = _engine.getDefaultWorklist();
        return (ys != null) ?
                new NewWorkItemAnnouncement(ys, item, getAnnouncementContext()) : null;
    }


    /** These next four methods announce an exception event to the observer */
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


    public void announceCaseCancellationToInterfaceXListeners(YIdentifier caseID) {
        for (InterfaceX_EngineSideClient listener : _interfaceXListeners) {
            debug("Announcing Cancel Case for case ", caseID.toString(),
                         " on client ",  listener.toString());
            listener.announceCaseCancellation(caseID.get_idString());
        }
    }


    public void announceTimeServiceExpiry(YWorkItem item, List timeOutTaskIds) {
        for (InterfaceX_EngineSideClient listener : _interfaceXListeners) {
            debug("Announcing Time Out for item ", item.getWorkItemID().toString(),
                    " on client ", listener.toString());
            listener.announceTimeOut(item, timeOutTaskIds);
        }
    }


    public void announceTimerExpiryEvent(YWorkItem item) {
        YAWLServiceReference defaultWorklist = _engine.getDefaultWorklist();
        if (defaultWorklist != null)
            _controller.notifyTimerExpiry(defaultWorklist, item);

        for (InterfaceX_EngineSideClient listener : _interfaceXListeners) {
            listener.announceTimeOut(item, null);
        }
        _engine.getInstanceCache().setTimerExpired(item);
    }

    public void announceCancelledWorkItem(YWorkItem item) {
        YAWLServiceGateway wsgw = (YAWLServiceGateway) item.getTask().getDecompositionPrototype();
        if (wsgw != null) {
            YAWLServiceReference ys = wsgw.getYawlService();
            if (ys == null) ys = YEngine.getInstance().getDefaultWorklist();

            try {
                Announcements<CancelWorkItemAnnouncement> announcements =
                                         new Announcements<CancelWorkItemAnnouncement>();
                announcements.addAnnouncement(new CancelWorkItemAnnouncement(ys, item));
                YEngine.getInstance().getAnnouncer().announceCancellationToEnvironment(announcements);
            }
            catch (YStateException e) {
                Logger.getLogger(this.getClass()).error(
                        "Failed to announce cancellation of workitem '" +
                              item.getIDString() + "': ",e);
            }
        }
    }


    protected void debug(final String... phrases) {
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


    protected void shutdownObserverGateways() {
    	  _controller.shutdownObserverGateways();
    }


    protected void shutdownInterfaceXListeners() {
        for (InterfaceX_EngineSideClient listener : _interfaceXListeners) {
            listener.shutdown();
        }
     }


}
