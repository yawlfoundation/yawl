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

import org.jdom.Document;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.announcement.Announcements;
import org.yawlfoundation.yawl.engine.announcement.CancelWorkItemAnnouncement;
import org.yawlfoundation.yawl.engine.announcement.NewWorkItemAnnouncement;

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
     * @param announcements
     */
    void announceWorkItems(Announcements<NewWorkItemAnnouncement> announcements);

    /**
     * Called by the engine when a previously posted workitem has been cancelled.<P>
     *
     * @param announcements
     */
    void cancelAllWorkItemsInGroupOf(Announcements<CancelWorkItemAnnouncement> announcements);

    /**
     * Called by the engine when a timer for a workitem expires.<P>
     *
     * @param yawlService
     * @param item
     */
    void announceTimerExpiry(YAWLServiceReference yawlService, YWorkItem item);


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
    void announceCaseCompletion(YIdentifier caseID, Document casedata);

    /**
     * Called by the engine to annouce when a case suspends (i.e. becomes fully
     * suspended as opposed to entering the 'suspending' state.
     *
     * @param caseID
     */
    void announceCaseSuspended(YIdentifier caseID);

    /**
     * Called by the engine to annouce when a case starts to suspends (i.e. enters the
     * suspending state as opposed to entering the fully 'suspended' state.
     *
     * @param caseID
     */
    void announceCaseSuspending(YIdentifier caseID);

    /**
     * Called by the engine to annouce when a case resumes from a previous 'suspending' or 'suspended' state.
     *
     * @param caseID
     */
    void announceCaseResumption(YIdentifier caseID);

    /**
     * Notify of a change of status for a work item.
     * @param workItem that has changed
     * @param oldStatus previous status
     * @param newStatus new status
     */
    void announceWorkItemStatusChange(YWorkItem workItem, YWorkItemStatus oldStatus, YWorkItemStatus newStatus);

    /**
     * Notify the engine has completed initialisation and is running
     */
    public void announceEngineInitialised(Set<YAWLServiceReference> services) ;

    /**
     * Notify the engine has cancelled a case
     */
    public void announceCaseCancellation(Set<YAWLServiceReference> services, YIdentifier id) ;


}
