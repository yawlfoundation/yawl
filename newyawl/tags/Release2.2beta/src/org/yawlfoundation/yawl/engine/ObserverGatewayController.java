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

package org.yawlfoundation.yawl.engine;

import org.jdom.Document;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.announcement.Announcements;
import org.yawlfoundation.yawl.engine.announcement.CancelWorkItemAnnouncement;
import org.yawlfoundation.yawl.engine.announcement.NewWorkItemAnnouncement;

import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Class which encapsulates the management and processing of InterfaceBClients.<P>
 *
 * @author Andrew Hastie
 *         Creation Date: 23-Aug-2005
 */
public class ObserverGatewayController
{
    private Vector<ObserverGateway> gateways;
    private ExecutorService executor;
    private static final int THREADPOOL_SIZE = Runtime.getRuntime().availableProcessors();

    /**
     * Constructor
     */
    public ObserverGatewayController()
    {
        gateways = new Vector<ObserverGateway>();
        executor = Executors.newFixedThreadPool(THREADPOOL_SIZE);
    }

    /**
     * Add a new observer observerGateway which will receive callbacks from the engine.<P>
     *
     * @param observerGateway
     */
    void addGateway(ObserverGateway observerGateway)
    {
        gateways.add(observerGateway);
    }

    /**
     * Removes a previously registered observer observerGateway.<P>
     *
     * @param observerGateway
     */
    void removeGateway(ObserverGateway observerGateway)
    {
        gateways.remove(observerGateway);
    }

    /**
     * Notify affected gateways of a new work item being posted.<P>
     *
     * @param announcements
     */
    void notifyAddWorkItems(final Announcements<NewWorkItemAnnouncement> announcements)
    {
        FutureTask<Integer> task = new FutureTask<Integer>(new Runnable()
        {
            public void run()
            {
                for(String scheme : announcements.getSchemes())
                {
                    Vector<ObserverGateway> affectedShims = getGatewaysForProtocol(scheme);
                    Announcements<NewWorkItemAnnouncement> announceies =
                            announcements.getAnnouncementsForScheme(scheme);

                    for(ObserverGateway observerGateway : affectedShims)
                    {
                        observerGateway.announceWorkItems(announceies);
                    }
                }
            }
        }, 1);

        runFutureTask(task, announcements);
    }

    /**
     * Notify affected gateways of a work item being removed.<P>
     *
     * @param announcements
     */
    void notifyRemoveWorkItems(final Announcements<CancelWorkItemAnnouncement> announcements)
    {
        FutureTask<Integer> task = new FutureTask<Integer>(new Runnable()
        {
            public void run()
            {
                for(String scheme : announcements.getSchemes())
                {
                    Vector<ObserverGateway> affectedShims = getGatewaysForProtocol(scheme);
                    Announcements<CancelWorkItemAnnouncement> announceies =
                                          announcements.getAnnouncementsForScheme(scheme);

                    for(ObserverGateway observerGateway : affectedShims)
                    {
                        observerGateway.cancelAllWorkItemsInGroupOf(announceies);
                    }
                }
            }
        }, 1);

        runFutureTask(task, announcements);
    }


    private void runFutureTask(FutureTask task, Announcements announcements) {
        try {
            executor.execute(task);
            if (((Integer) task.get()) == 1) announcements.clear();
        }
        catch (InterruptedException ie) {
            // nothing to do
        }
        catch (ExecutionException ee) {
            // nothing to do
        }
    }


    void notifyTimerExpiry(final YAWLServiceReference yawlService, final YWorkItem item)
    {
        executor.execute(new Runnable()
        {
            public void run()
            {
                Vector<ObserverGateway> affectedShims = getGatewaysForProtocol(yawlService.getScheme());

                for(ObserverGateway observerGateway : affectedShims)
                {
                    observerGateway.announceTimerExpiry(yawlService, item);
                }                    
            }
        });
    }

    /**
     * Helper method which returns a vector of gateways which satisfy the requested protocol.<P>
     *
     * @param scheme    The scheme or protocol the gateway needs to support.
     * @return Gateways of protocol scheme
     */
    private Vector<ObserverGateway> getGatewaysForProtocol(String scheme)
    {
        Vector<ObserverGateway> temp = new Vector<ObserverGateway>();

        for(ObserverGateway observerGateway : gateways)
        {
            String gatewayScheme = observerGateway.getScheme();
            if ((gatewayScheme != null) && gatewayScheme.trim().equalsIgnoreCase(scheme))
            {
                temp.add(observerGateway);
            }
        }

        return temp;
    }


    /**
     * Notify the case completion
     * @param yawlService
     * @param caseID
     * @param casedata
     */
    public void notifyCaseCompletion(final YAWLServiceReference yawlService, final YIdentifier caseID, final Document casedata)
    {
        executor.execute(new Runnable()
        {
            public void run()
            {
                Vector<ObserverGateway> affectedShims = getGatewaysForProtocol(yawlService.getScheme());

                for(ObserverGateway observerGateway : affectedShims)
                {
                    observerGateway.announceCaseCompletion(yawlService, caseID, casedata);
                }
            }
        });
    }


    /**
     * Notify the case completion
     * @param caseID
     * @param casedata
     */
    public void notifyCaseCompletion(final YIdentifier caseID, final Document casedata)
    {
        executor.execute(new Runnable()
        {
            public void run()
            {
                for(ObserverGateway observerGateway : gateways)
                {
                    observerGateway.announceCaseCompletion(caseID, casedata);
                }
            }
        });
    }

    /**
     * Notify of a change of status for a work item.
     * @param workItem that has changed
     * @param oldStatus previous status
     * @param newStatus new status
     */
    public void notifyWorkItemStatusChange(final YWorkItem workItem, final YWorkItemStatus oldStatus, final YWorkItemStatus newStatus)
    {
        executor.execute(new Runnable()
        {
            public void run()
            {
                for(ObserverGateway observerGateway : gateways)
                {
                    observerGateway.announceWorkItemStatusChange(workItem, oldStatus, newStatus);
                }
            }
        });
    }

    /**
     * Notify the case is suspending
     * @param caseID
     */
    public void notifyCaseSuspending(final YIdentifier caseID)
    {
        executor.execute(new Runnable()
        {
            public void run()
            {
                for(ObserverGateway observerGateway : gateways)
                {
                    observerGateway.announceCaseSuspending(caseID);
                }
            }
        });
    }

    /**
     * Notify the case is suspended
     * @param caseID
     */
    public void notifyCaseSuspended(final YIdentifier caseID)
    {
        executor.execute(new Runnable()
        {
            public void run()
            {
                for(ObserverGateway observerGateway : gateways)
                {
                    observerGateway.announceCaseSuspended(caseID);
                }
            }
        });
    }

    /**
     * Notify the case is resumption
     * @param caseID
     */
    public void notifyCaseResumption(final YIdentifier caseID)
    {
        executor.execute(new Runnable()
        {
            public void run()
            {
                for(ObserverGateway observerGateway : gateways)
                {
                    observerGateway.announceCaseResumption(caseID);
                }
            }
        });
    }


    /**
     * Notify the engine has completed initialisation and is running
     * @param services - all services registered with the engine
     */
    public void notifyEngineInitialised(final Set<YAWLServiceReference> services)
    {
        executor.execute(new Runnable()
        {
            public void run()
            {
                for(ObserverGateway observerGateway : gateways)
                {
                    observerGateway.announceEngineInitialised(services);
                }
            }
        });
    }


    /**
     * Notify environment that the engine has cancelled a case
     * @param services - all services registered with the engine
     * @param id - the case identifier
     */
    public void notifyCaseCancellation(final Set<YAWLServiceReference> services,
                                       final YIdentifier id)
    {
        executor.execute(new Runnable()
        {
            public void run()
            {
                for(ObserverGateway observerGateway : gateways)
                {
                    observerGateway.announceCaseCancellation(services, id);
                }
            }
        });
    }

    /**
     * Invoke finalisation processing on all observer gateways (called when
     * the Engine servlet is being destroyed)
     */
    public void shutdownObserverGateways() {
      	for (ObserverGateway observerGateway : gateways) {
            observerGateway.shutdown();
        }
        executor.shutdownNow();
    }

}
