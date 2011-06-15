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

import java.util.Set;


/**
 * Interface to be implemented by 'shim' classes which register with the engine to receive callbacks when tasks
 * are posted and cancelled.
 *
 * @author Andrew Hastie
 *         Creation Date: 23-Aug-2005
 */
public interface ObserverGateway
{
    String getScheme();

    /**
     * Called by the engine when a new workitem gets enabled.<P>
     *
     * @param announcement
     */
    void announceFiredWorkItem(final YAnnouncement announcement);

    /**
     * Called by the engine when a previously posted workitem has been cancelled.<P>
     *
     * @param announcement
     */
    void announceCancelledWorkItem(final YAnnouncement announcement);

    /**
     * Called by the engine when a timer for a workitem expires.<P>
     *
     * @param announcement
     */
    void announceTimerExpiry(final YAnnouncement announcement);


    /**
     * Called by engine to announce when a case is complete.
     * @param yawlService the yawl service
     * @param caseID the case that completed
     * @param casedata the output data of the case
     */
    void announceCaseCompletion(YAWLServiceReference yawlService, 
                                YIdentifier caseID, Document casedata);

    /**
     * Called by engine to announce when a case is complete.
     * @param caseID the case that completed
     * @param casedata the output data of the case
     */
    void announceCaseCompletion(Set<YAWLServiceReference> services, YIdentifier caseID,
                                Document casedata);

    /**
     * Called by the engine to annouce when a case suspends (i.e. becomes fully
     * suspended as opposed to entering the 'suspending' state.
     *
     * @param caseID
     */
    void announceCaseSuspended(Set<YAWLServiceReference> services, YIdentifier caseID);

    /**
     * Called by the engine to annouce when a case starts to suspends (i.e. enters the
     * suspending state as opposed to entering the fully 'suspended' state.
     *
     * @param caseID
     */
    void announceCaseSuspending(Set<YAWLServiceReference> services, YIdentifier caseID);

    /**
     * Called by the engine to annouce when a case resumes from a previous 'suspending' or 'suspended' state.
     *
     * @param caseID
     */
    void announceCaseResumption(Set<YAWLServiceReference> services, YIdentifier caseID);

    /**
     * Notify of a change of status for a work item.
     * @param workItem that has changed
     * @param oldStatus previous status
     * @param newStatus new status
     */
    void announceWorkItemStatusChange(Set<YAWLServiceReference> services,
                                      YWorkItem workItem,
                                      YWorkItemStatus oldStatus,
                                      YWorkItemStatus newStatus);

    /**
     * Notify the engine has completed initialisation and is running
     */
    public void announceEngineInitialised(Set<YAWLServiceReference> services, int maxWaitSeconds) ;

    /**
     * Notify the engine has cancelled a case
     */
    public void announceCaseCancellation(Set<YAWLServiceReference> services, YIdentifier id) ;

    /**
     * Called when the Engine is shutdown (servlet destroyed); the observer gateway should
     * to do its own finalisation processing
     */
    public void shutdown();

}
