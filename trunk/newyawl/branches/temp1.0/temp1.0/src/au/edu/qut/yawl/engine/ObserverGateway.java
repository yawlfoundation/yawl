/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.state.YIdentifier;

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
}
