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
import au.edu.qut.yawl.engine.interfce.InterfaceB_EngineBasedClient;

import org.jdom.Document;

import java.util.Vector;
import java.util.Iterator;

/**
 * Class which encapsulates the management and processing of InterfaceBClients.<P>
 *
 * @author Andrew Hastie
 *         Creation Date: 23-Aug-2005
 */
public class ObserverGatewayController
{
    private Vector gateways = null;


    /**
     * Constructor
     */
    public ObserverGatewayController()
    {
        gateways = new Vector();
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
        Vector affectedShims = getGatewaysForProtocol(yawlService.getScheme());

        Iterator iter = affectedShims.iterator();
        while(iter.hasNext())
        {
            ObserverGateway observerGateway = (ObserverGateway)iter.next();
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
        Vector affectedShims = getGatewaysForProtocol(yawlService.getScheme());

        Iterator iter = affectedShims.iterator();
        while(iter.hasNext())
        {
            ObserverGateway observerGateway = (ObserverGateway)iter.next();
            observerGateway.cancelAllWorkItemsInGroupOf(yawlService, item);
        }
    }

    /**
     * Helper method which returns a vactor of gateways which satisfy the requested protocol.<P>
     *
     * @param scheme    The scheme or protocol the gateway needs to support.
     * @return
     */
    private Vector getGatewaysForProtocol(String scheme)
    {
        Vector temp = new Vector();

        Iterator iter = gateways.iterator();
        while(iter.hasNext())
        {
            ObserverGateway observerGateway = (ObserverGateway)iter.next();
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
        Vector affectedShims = getGatewaysForProtocol(yawlService.getScheme());

        Iterator iter = affectedShims.iterator();
        while (iter.hasNext()) {
            ObserverGateway observerGateway = (ObserverGateway)iter.next();
            observerGateway.announceCaseCompletion(yawlService, caseID, casedata);
        }
    }
}
