/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.exceptions.*;

import java.util.Set;
import java.net.URI;

/**
 * Defines the 'B' interface into the YAWL Engine corresponding to WfMC interfaces 2+3 - Workflow client applications and invoked applications.
 *
 * @author Andrew Hastie
 *         Creation Date: 10-Jun-2005
 */
public interface InterfaceBClient {
    /**
     * Register an InterfaceB client with the engine in order to receive callbacks.<P>
     *
     * @param observer
     */
    void registerInterfaceBObserver(InterfaceBClientObserver observer);


    /**
     * Registers an InterfaceB Observer Gateway with the engine in order to receive callbacks.<P>
     *  
     * @param gateway
     */
    void registerInterfaceBObserverGateway(ObserverGateway gateway);

    /**
     * Returns a set of all availkable workitems from the engine.<P>
     *
     * @return  Set of available work items
     */
    public Set getAvailableWorkItems();

    /**
     * Returns a set of all work items, regardless of state, from the engine.<P>
     *
     * @return  Set of work items
     */
    public Set getAllWorkItems();

    YWorkItem startWorkItem(YWorkItem workItem, String userID) throws YStateException, YDataStateException, YQueryException, YSchemaBuildingException, YPersistenceException;

    void completeWorkItem(YWorkItem workItem, String data) throws YStateException, YDataStateException, YQueryException, YSchemaBuildingException, YPersistenceException;

    void suspendWorkItem(String workItemID, String userName) throws YStateException, YPersistenceException;

    YWorkItem getWorkItem(String workItemID);

    /**
     * Starts an instance of a specification (known as a 'case') within the engine.<P>
     *
     * @param specID the specification id.
     * @param caseParams the XML string of the case params (can be null).
     * @param completionObserver the observer for completion events (can be null).
     * @return
     * @throws YStateException
     * @throws YDataStateException
     * @throws YSchemaBuildingException
     */
    String launchCase(String specID, String caseParams, URI completionObserver) throws YStateException, YDataStateException, YSchemaBuildingException, YPersistenceException;

    void checkElegibilityToAddInstances(String workItemID) throws YStateException;

    YWorkItem createNewInstance(YWorkItem workItem, String paramValueForMICreation) throws YStateException, YPersistenceException;

    Set getChildrenOfWorkItem(YWorkItem workItem);

    /**
     * Returns the task definition, not the task instance.
     *
     * @param specificationID the specification id
     * @param taskID          the task id
     * @return the task definition object.
     */
    YTask getTaskDefinition(String specificationID, String taskID);
}
