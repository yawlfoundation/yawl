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

import org.jdom2.Document;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.YTask;
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

    // map [scheme, gateways for scheme] of registered gateways
    private final Map<String, Set<ObserverGateway>> _gateways;

    private final ExecutorService _executor;

    private static final int THREADPOOL_SIZE = Runtime.getRuntime().availableProcessors();

    /**
     * Constructor
     */
    public ObserverGatewayController() {
        _gateways = new HashMap<String, Set<ObserverGateway>>();
        _executor = Executors.newFixedThreadPool(THREADPOOL_SIZE);
    }


    /**
     * Add an observerGateway, which will receive event callbacks from the engine.
     * @param gateway the gateway to add
     * @throws YAWLException if the observerGateway has a null scheme value.
     */
    void addGateway(ObserverGateway gateway) throws YAWLException {
        String scheme = gateway.getScheme();
        if (scheme == null) {
            throw new YAWLException("Cannot add: ObserverGateway has a null scheme.");
        }
        Set<ObserverGateway> schemeGateways = _gateways.get(scheme);
        if (schemeGateways == null) {
            schemeGateways = new HashSet<ObserverGateway>();
            _gateways.put(scheme, schemeGateways);
        }
        schemeGateways.add(gateway);
    }


    /**
     * Removes a previously registered observer gateway.
     * @param gateway the gateway to remove
     * @return the boolean result of the removal.
     */
    boolean removeGateway(ObserverGateway gateway) {
        boolean result = false;
        String scheme = gateway.getScheme();
        if (scheme != null) {
            Set<ObserverGateway> schemeGateways = _gateways.get(scheme);
            if (schemeGateways != null) {
                result = schemeGateways.remove(gateway);
                if (schemeGateways.isEmpty()) _gateways.remove(scheme);
            }
        }
        return result;
    }


    /**
     * Checks if there are any currently registered gateways.
     * @return true if there are no registered gateways
     */
    boolean isEmpty() { return _gateways.isEmpty(); }


    /**
     * Announce a set of work item notifications to the relevant observers
     * @param announcements the Set of announcements
     */
    protected void announce(final Set<YAnnouncement> announcements) {
        for (YAnnouncement announcement : announcements) {
            announce(announcement);
        }
    }


    /**
     * Announce a work item notification to the relevant observers
     * @param announcement the announcement
     */
    protected void announce(final YAnnouncement announcement) {
        if (announcement == null) return;
        _executor.execute(new Runnable() {
            public void run() {
                String scheme = announcement.getScheme();
                for (ObserverGateway gateway : getGatewaysForScheme(scheme)) {
                    switch (announcement.getEvent()) {
                        case ITEM_ADD: gateway.announceFiredWorkItem(announcement); break;
                        case ITEM_CANCEL: gateway.announceCancelledWorkItem(announcement); break;
                        case TIMER_EXPIRED: gateway.announceTimerExpiry(announcement); break;
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
     * @param services a set of the current engine-registered services
     * @param specID the specification id for thestarted case
     * @param caseID the completing case identifier
     * @param launchingService the service that launched the case
     * @param delayed true if this is a delayed case launch, false if immediate
     */
    public void notifyCaseStarting(final Set<YAWLServiceReference> services,
                                   final YSpecificationID specID, 
                                   final YIdentifier caseID, 
                                   final String launchingService,
                                   final boolean delayed) {
        _executor.execute(new Runnable() {
            public void run() {
                for (Set<ObserverGateway> gateways : _gateways.values()) {
            	    for (ObserverGateway gateway : gateways) {
                        String scheme = gateway.getScheme();
                        gateway.announceCaseStarted(
                                getServicesForScheme(services, scheme), specID, caseID,
                                launchingService, delayed);
                    }
                }
            }
        });
    }
  
    /**
     * Notify a case completion to all registered gateways.
     * @param services a set of the current engine-registered services
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
     * @param services a set of the current engine-registered services
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
     * @param services a set of the current engine-registered services
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
     * @param services a set of the current engine-registered services
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
     * @param services a set of the current engine-registered services
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
            	    for (ObserverGateway gateway : gateways) {
                        String scheme = gateway.getScheme();
                        gateway.announceEngineInitialised(
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
            	    for (ObserverGateway gateway : gateways) {
                        String scheme = gateway.getScheme();
                        gateway.announceCaseCancellation(
                                getServicesForScheme(services, scheme), id);
                    }
                }
            }
        });
    }


    /**
     * Notify environment that a case has deadlocked
     * @param services - all services registered with the engine
     * @param id - the case identifier
     * @param tasks - the set of deadlocked tasks
     */
    public void notifyDeadlock(final Set<YAWLServiceReference> services,
                               final YIdentifier id, final Set<YTask> tasks) {
        _executor.execute(new Runnable() {
            public void run() {
                for (Set<ObserverGateway> gateways : _gateways.values()) {
            	    for (ObserverGateway gateway : gateways) {
                        String scheme = gateway.getScheme();
                        gateway.announceDeadlock(
                                getServicesForScheme(services, scheme), id, tasks);
                    }
                }
            }
        });
    }


    /**
     * Invoke finalisation processing on all observer gateways and the
     * ExecutorService used to execute calls to them (called when
     * the Engine servlet is being destroyed). Do in same thread to avoid
     * issues where webapp is closed before gateways can complete their
     * termination processing.
     */
    public void shutdownObserverGateways() {
        for (Set<ObserverGateway> gateways : _gateways.values()) {
            for (ObserverGateway gateway : gateways) {
                gateway.shutdown();
            }
        }
        _executor.shutdown();
    }


    /**
     * Get the set of gateways which satisfy the requested scheme.
     * @param scheme The scheme or protocol the gateway needs to support.
     * @return Gateways of the scheme
     */
    private Set<ObserverGateway> getGatewaysForScheme(String scheme) {
        return _gateways.containsKey(scheme) ? _gateways.get(scheme) :
                Collections.<ObserverGateway>emptySet();
    }


    /**
     * Gets the set of all services of a particular scheme
     * @param services all services currently registered with the engine
     * @param scheme the scheme supported by services
     * @return the set of services that are endpoints for the scheme
     */
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
