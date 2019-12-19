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

package org.yawlfoundation.yawl.engine;

import org.jdom2.Document;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.announcement.YAnnouncement;

import java.util.Set;


/**
 * Interface to be implemented by 'shim' classes which register with the engine to
 * receive callbacks when tasks are posted and cancelled.
 *
 * @author Andrew Hastie
 *         Creation Date: 23-Aug-2005
 */
public interface ObserverGateway {

    /**
     * Gets the scheme part of the url of an implementing gateway.
     * @return the scheme of the gateway
     */
    String getScheme();


    /**
     * Called by the engine when a new workitem gets enabled.
     * @param announcement the work item descriptors
     */
    void announceFiredWorkItem(final YAnnouncement announcement);


    /**
     * Called by the engine when a previously posted workitem has been cancelled.
     * @param announcement the work item descriptors
     */
    void announceCancelledWorkItem(final YAnnouncement announcement);


    /**
     * Called by the engine when a timer for a workitem expires.
     * @param announcement the work item descriptors
     */
    void announceTimerExpiry(final YAnnouncement announcement);


    /**
     * Called by engine when a case is complete and a completion observer was
     * specified at case launch.
     * @param yawlService the yawl service nominated as the completion observer
     * @param caseID the case that completed
     * @param caseData the output data of the case
     */
    void announceCaseCompletion(YAWLServiceReference yawlService, 
                                YIdentifier caseID, Document caseData);

    /**
     * Called by engine to announce when a case has commenced.
     *
     * @param services the set of registered custom services
     * @param specID   the specification id of the started case
     * @param caseID   the case that has started
     * @param launchingService the service that started the case
     * @param delayed true if this is a delayed case launch, false if immediate
     */
     void announceCaseStarted(Set<YAWLServiceReference> services,
                              YSpecificationID specID, YIdentifier caseID,
                              String launchingService, boolean delayed);

    /**
     * Called by engine to announce when a case is complete and a completion
     * observer was not specified at case launch. This announcement is sent to all
     * services on all registered gateways.
     * @param services the set of services currently registered with the engine
     * @param caseID the case that completed
     * @param caseData the output data of the case
     */
    void announceCaseCompletion(Set<YAWLServiceReference> services, YIdentifier caseID,
                                Document caseData);

    /**
     * Called by the engine to announce when a case suspends (i.e. becomes fully
     * suspended as opposed to entering the 'suspending' state).
     * @param services the set of services currently registered with the engine
     * @param caseID the identifier of the suspended case
     */
    void announceCaseSuspended(Set<YAWLServiceReference> services, YIdentifier caseID);


    /**
     * Called by the engine to announce when a case starts to suspends (i.e. enters the
     * suspending state as opposed to entering the fully 'suspended' state).
     * @param services the set of services currently registered with the engine
     * @param caseID the identifier of the suspending case
     */
    void announceCaseSuspending(Set<YAWLServiceReference> services, YIdentifier caseID);


    /**
     * Called by the engine to announce when a case resumes from a previous 'suspending'
     * or 'suspended' state.
     * @param services the set of services currently registered with the engine
     * @param caseID the identifier of the suspended case
     */
    void announceCaseResumption(Set<YAWLServiceReference> services, YIdentifier caseID);


    /**
     * Called by the engine to notify of a change of status for a work item.
     * @param services the set of services currently registered with the engine
     * @param workItem that has changed
     * @param oldStatus previous status
     * @param newStatus new status
     */
    void announceWorkItemStatusChange(Set<YAWLServiceReference> services,
                                      YWorkItem workItem,
                                      YWorkItemStatus oldStatus,
                                      YWorkItemStatus newStatus);

    /**
     * Called by the engine to notify that it has completed initialisation and is running.
     * @param services the set of services currently registered with the engine
     * @param maxWaitSeconds the maximum amount of time to wait attempting to
     * contact the services managed by the gateway before giving up
     */
    void announceEngineInitialised(Set<YAWLServiceReference> services, int maxWaitSeconds) ;


    /**
     * Called by the engine to announce a case has been cancelled.
     * @param services the set of services currently registered with the engine
     * @param id the identifier of the cancelled case
     */
    void announceCaseCancellation(Set<YAWLServiceReference> services, YIdentifier id) ;


    /**
     * Called by the engine to announce that a case has deadlocked
     * @param services the set of services currently registered with the engine
     * @param id the identifier of the deadlocked case
     * @param tasks the set of deadlocked tasks
     */
    void announceDeadlock(Set<YAWLServiceReference> services, YIdentifier id,
                               Set<YTask> tasks);


    /**
     * Called when the Engine is shutdown (servlet destroyed); the observer gateway should
     * to do its own finalisation processing.
     */
    void shutdown();

}
