/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.engine;

import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.YWorkItem;
import org.yawlfoundation.yawl.engine.YWorkItemStatus;

import org.jdom.Document;


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
     * @param yawlService
     * @param item
     */
    void announceWorkItem(YAWLServiceReference yawlService, YWorkItem item);

    /**
     * Called by the engine when a previously posted workitem has been cancelled.<P>
     *
     * @param yawlService
     * @param item
     */
    void cancelAllWorkItemsInGroupOf(YAWLServiceReference yawlService, YWorkItem item);


    /**
     * Called by engine to announce when a case is complete.
     * @param yawlService the yawl service
     * @param caseID the case that completed
     */
    void announceCaseCompletion(YAWLServiceReference yawlService, 
                                YIdentifier caseID, Document casedata);
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
}
