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
import org.yawlfoundation.yawl.engine.announcement.YAnnouncement;
import org.yawlfoundation.yawl.exceptions.YAWLException;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class which encapsulates the management and processing of InterfaceB event observers.
 *
 * @author Andrew Hastie
 * @author Michael Adams (for 2.2)
 * @date 23-Aug-2005
 *
 */
public class ObserverGatewayController {

    private final Map<String, Set<ObserverGateway>> _gateways;
    private final ExecutorService _executor;

    private static final int THREADPOOL_SIZE = Runtime.getRuntime().availableProcessors();

    /**
     * Constructor
     */
    public ObserverGatewayController() {
        _gateways = new Hashtable<String, Set<ObserverGateway>>();
        _executor = Executors.newFixedThreadPool(THREADPOOL_SIZE);
    }


    /**
     * Add an observerGateway, which will receive event callbacks from the engine.
     * @param observerGateway the gateway to add
     * @throws YAWLException if the observerGateway has a null scheme value.
     */
    void addGateway(ObserverGateway observerGateway) throws YAWLException {
        String scheme = observerGateway.getScheme();
        if (scheme == null) {
            throw new YAWLException("Cannot add: ObserverGateway has a null scheme.");
        }
        Set<ObserverGateway> schemeGateways = _gateways.get(scheme);
        if (schemeGateways == null) {
            schemeGateways = new HashSet<ObserverGateway>();
            _gateways.put(scheme, schemeGateways);
        }
        schemeGateways.add(observerGateway);
    }


    /**
     * Removes a previously registered observer observerGateway.
     * @param observerGateway the gateway to remove
     * @return the boolean result of the removal.
     */
    boolean removeGateway(ObserverGateway observerGateway) {
        boolean result = false;
        String scheme = observerGateway.getScheme();
        if (scheme != null) {
            Set<ObserverGateway> schemeGateways = _gateways.get(scheme);
            if (schemeGateways != null) {
                result = schemeGateways.remove(observerGateway);
                if (schemeGateways.isEmpty()) _gateways.remove(scheme);
            }
        }
        return result;
    }


    /**
     * Checks if there are any registered gateways.
     * @return true if there are no registered gateways
     */
    boolean isEmpty() { return _gateways.isEmpty(); }

    protected void announce(final Set<YAnnouncement> announcements) {
        for (YAnnouncement announcement : announcements) {
            announce(announcement);
        }
    }


    protected void announce(final YAnnouncement announcement) {
        if (announcement == null) return;
        _executor.execute(new Runnable() {
            public void run() {
                String scheme = announcement.getScheme();
                for (ObserverGateway gateway : getGatewaysForScheme(scheme)) {
                    switch (announcement.getEvent()) {
                        case FIRED_ITEM: gateway.announceFiredWorkItem(announcement); break;
                        case CANCELLED_ITEM: gateway.announceCancelledWorkItem(announcement); break;
                        case TIMER_EXPIRY: gateway.announceTimerExpiry(announcement); break;
                    }
                }
            }
        });
    }





    /**
     * Notify the case completion to a particular service that has registered as a
     * completion observer for this case when the case was launched.
     * @param service the service to notify
     * @param caseID the completing case identifier
     * @param caseData the final case data document
     */
    public void notifyCaseCompletion(final YAWLServiceReference service,
                                     final YIdentifier caseID, final Document caseData) {
        _executor.execute(new Runnable() {
            public void run() {
                for (ObserverGateway gateway : getGatewaysForScheme(service.getScheme())) {
                    gateway.announceCaseCompletion(service, caseID, caseData);
                }
            }
        });
    }


    /**
     * Notify a case completion to all registered gateways.
     * @param caseID the completing case identifier
     * @param caseData the final case data document
     */
    public void notifyCaseCompletion(final Set<YAWLServiceReference> services,
                                     final YIdentifier caseID, final Document caseData) {
        _executor.execute(new Runnable() {
            public void run() {
                for (Set<ObserverGateway> gateways : _gateways.values()) {
            	    for (ObserverGateway gateway : gateways) {
                        String scheme = gateway.getScheme();
                        gateway.announceCaseCompletion(
                                getServicesForScheme(services, scheme), caseID, caseData);
                    }
                }
            }
        });
    }


    /**
     * Notify of a change of status for a work item to all registered gateways.
     * @param workItem the work item that has changed status
     * @param oldStatus previous status
     * @param newStatus new status
     */
    public void notifyWorkItemStatusChange(final Set<YAWLServiceReference> services,
                                           final YWorkItem workItem,
                                           final YWorkItemStatus oldStatus,
                                           final YWorkItemStatus newStatus) {
        _executor.execute(new Runnable() {
            public void run() {
                for (Set<ObserverGateway> gateways : _gateways.values()) {
            	    for (ObserverGateway gateway : gateways) {
                        String scheme = gateway.getScheme();
                        gateway.announceWorkItemStatusChange(
                                getServicesForScheme(services, scheme), workItem,
                                oldStatus, newStatus);
                    }
                }
            }
        });
    }


    /**
     * Notify the case is suspending
     * @param caseID the suspending case identifier
     */
    public void notifyCaseSuspending(final YIdentifier caseID,
                                     final Set<YAWLServiceReference> services) {
        _executor.execute(new Runnable() {
            public void run() {
                for (Set<ObserverGateway> gateways : _gateways.values()) {
            	    for (ObserverGateway gateway : gateways) {
                        String scheme = gateway.getScheme();
                        gateway.announceCaseSuspending(
                                getServicesForScheme(services, scheme), caseID);
                    }
                }
            }
        });
    }


    /**
     * Notify the case is suspended
     * @param caseID the suspended case identifier
     */
    public void notifyCaseSuspended(final YIdentifier caseID,
                                    final Set<YAWLServiceReference> services) {
        _executor.execute(new Runnable() {
            public void run() {
                for (Set<ObserverGateway> gateways : _gateways.values()) {
            	    for (ObserverGateway gateway : gateways) {
                        String scheme = gateway.getScheme();
                        gateway.announceCaseSuspended(
                                getServicesForScheme(services, scheme), caseID);
                    }
                }
            }
        });
    }


    /**
     * Notify the case is resumption
     * @param caseID the resuming case identifier
     */
    public void notifyCaseResumption(final YIdentifier caseID,
                                     final Set<YAWLServiceReference> services) {
        _executor.execute(new Runnable() {
            public void run() {
                for (Set<ObserverGateway> gateways : _gateways.values()) {
            	    for (ObserverGateway gateway : gateways) {
                        String scheme = gateway.getScheme();
                        gateway.announceCaseResumption(
                                getServicesForScheme(services, scheme), caseID);
                    }
                }
            }
        });
    }


    /**
     * Notify the engine has completed initialisation and is running
     * @param services all services registered with the engine
     * @param maxWaitSeconds the maximum seconds to wait for services to be contactable
     *
     */
    public void notifyEngineInitialised(final Set<YAWLServiceReference> services,
                                        final int maxWaitSeconds) {
        _executor.execute(new Runnable() {
            public void run() {
                for (Set<ObserverGateway> gateways : _gateways.values()) {
            	    for (ObserverGateway observerGateway : gateways) {
                        String scheme = observerGateway.getScheme();
                        observerGateway.announceEngineInitialised(
                                getServicesForScheme(services, scheme),
                                maxWaitSeconds);
                    }
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
                                       final YIdentifier id) {
        _executor.execute(new Runnable() {
            public void run() {
                for (Set<ObserverGateway> gateways : _gateways.values()) {
            	    for (ObserverGateway observerGateway : gateways) {
                        String scheme = observerGateway.getScheme();
                        observerGateway.announceCaseCancellation(
                                getServicesForScheme(services, scheme), id);
                    }
                }
            }
        });
    }

    /**
     * Invoke finalisation processing on all observer gateways (called when
     * the Engine servlet is being destroyed)
     */
    public void shutdownObserverGateways() {
        _executor.execute(new Runnable() {
            public void run() {
                for (Set<ObserverGateway> gateways : _gateways.values()) {
            	    for (ObserverGateway observerGateway : gateways) {
                        observerGateway.shutdown();
                    }
                }
            }
        });
        _executor.shutdown();
    }


    /**
     * Helper method which returns a vector of gateways which satisfy the requested protocol.<P>
     *
     * @param scheme    The scheme or protocol the gateway needs to support.
     * @return Gateways of protocol scheme
     */
    private Set<ObserverGateway> getGatewaysForScheme(String scheme) {
        return _gateways.containsKey(scheme) ? _gateways.get(scheme) :
                Collections.<ObserverGateway>emptySet();
    }


    private Set<YAWLServiceReference> getServicesForScheme(
            Set<YAWLServiceReference> services, String scheme) {
        Set<YAWLServiceReference> matches = new HashSet<YAWLServiceReference>();
        for (YAWLServiceReference service : services) {
            if (service.getScheme().equals(scheme)) {
                matches.add(service);
            }
        }
        return matches;
    }




}
