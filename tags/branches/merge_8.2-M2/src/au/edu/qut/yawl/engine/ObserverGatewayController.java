/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.state.YIdentifier;

import org.jdom.Document;

import java.util.Vector;

/**
 * Class which encapsulates the management and processing of InterfaceBClients.<P>
 *
 * @author Andrew Hastie
 *         Creation Date: 23-Aug-2005
 */
public class ObserverGatewayController
{
    private Vector<ObserverGateway> gateways = null;


    /**
     * Constructor
     */
    public ObserverGatewayController()
    {
        gateways = new Vector<ObserverGateway>();
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
     * @param yawlService
     * @param item
     */
    void notifyAddWorkItem(YAWLServiceReference yawlService, YWorkItem item)
    {
        Vector<ObserverGateway> affectedShims = getGatewaysForProtocol(yawlService.getScheme());

        for(ObserverGateway observerGateway : affectedShims)
        {
            observerGateway.announceWorkItem(yawlService, item);
        }
    }

    /**
     * Notify affected gateways of a work item being removed.<P>
     *
     * @param yawlService
     * @param item
     */
    void notifyRemoveWorkItem(YAWLServiceReference yawlService, YWorkItem item)
    {
        Vector<ObserverGateway> affectedShims = getGatewaysForProtocol(yawlService.getScheme());

        for(ObserverGateway observerGateway : affectedShims)
        {
            observerGateway.cancelAllWorkItemsInGroupOf(yawlService, item);
        }
    }

    /**
     * Helper method which returns a vactor of gateways which satisfy the requested protocol.<P>
     *
     * @param scheme    The scheme or protocol the gateway needs to support.
     * @return Gateways of protocol scheme

     */
    private Vector<ObserverGateway> getGatewaysForProtocol(String scheme)
    {
        Vector<ObserverGateway> temp = new Vector<ObserverGateway>();

        for(ObserverGateway observerGateway : gateways)
        {
            if (observerGateway.getScheme().trim().equalsIgnoreCase(scheme))
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
     */
    public void notifyCaseCompletion(YAWLServiceReference yawlService, 
                                     YIdentifier caseID, Document casedata) {
        Vector<ObserverGateway> affectedShims = getGatewaysForProtocol(yawlService.getScheme());

        for(ObserverGateway observerGateway : affectedShims)
        {
            observerGateway.announceCaseCompletion(yawlService, caseID, casedata);
        }
    }

    /**
     * Notify of a change of status for a work item.
     * @param workItem that has changed
     * @param oldStatus previous status
     * @param newStatus new status
     */
    public void notifyWorkItemStatusChange(YWorkItem workItem, YWorkItemStatus oldStatus, YWorkItemStatus newStatus)
    {
        for(ObserverGateway observerGateway : gateways)
        {
            observerGateway.announceWorkItemStatusChange(workItem, oldStatus, newStatus);
        }
    }

    /**
     * Notify the case is suspending
     * @param caseID
     */
    public void notifyCaseSuspending(YIdentifier caseID)
    {
        for(ObserverGateway observerGateway : gateways)
        {
            observerGateway.announceCaseSuspending(caseID);
        }
    }

    /**
     * Notify the case is suspended
     * @param caseID
     */
    public void notifyCaseSuspended(YIdentifier caseID)
    {
        for(ObserverGateway observerGateway : gateways)
        {
            observerGateway.announceCaseSuspended(caseID);
        }
    }

    /**
     * Notify the case is resumption
     * @param caseID
     */
    public void notifyCaseResumption(YIdentifier caseID)
    {
        for(ObserverGateway observerGateway : gateways)
        {
            observerGateway.announceCaseResumption(caseID);
        }
    }
}
