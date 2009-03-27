/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.engine.interfce.interfaceB;

import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.exceptions.*;
import org.yawlfoundation.yawl.engine.ObserverGateway;
import org.yawlfoundation.yawl.engine.YWorkItem;
import org.yawlfoundation.yawl.engine.YSpecificationID;

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

    YWorkItem startWorkItem(YWorkItem workItem, String userID) throws YStateException, YDataStateException, YQueryException, YSchemaBuildingException, YPersistenceException, YEngineStateException;

    void completeWorkItem(YWorkItem workItem, String data, boolean force) throws YStateException, YDataStateException, YQueryException, YSchemaBuildingException, YPersistenceException, YEngineStateException;

    void rollbackWorkItem(String workItemID, String userName) throws YStateException, YPersistenceException;

    YWorkItem suspendWorkItem(String workItemID) throws YStateException, YPersistenceException;

    YWorkItem getWorkItem(String workItemID);

 /**
     * Returns an XML representation of the current net data of the case corresponding
     * to caseID.
     *
     * @param caseID to retrieve net data of
     * @return XML representation of the net
     */
    public String getCaseData(String caseID) throws YStateException;

    /**
     * Starts an instance of a specification (known as a 'case') within the engine.<P>
     *
     * @param username the username.
     * @param specID the specification id.
     * @param caseParams the XML string of the case params (can be null).
     * @param completionObserver the observer for completion events (can be null).
     * @return
     * @throws YStateException
     * @throws YDataStateException
     * @throws YSchemaBuildingException
     */
    String launchCase(String username, String specID, String caseParams, URI completionObserver) throws YStateException, YDataStateException, YSchemaBuildingException, YPersistenceException, YEngineStateException;

    /**
     * Starts an instance of a specification (known as a 'case') within the engine.<P>
     *
     * @param username the username.
     * @param specID the specification id.
     * @param caseParams the XML string of the case params (can be null).
     * @param completionObserver the observer for completion events (can be null).
     * @param caseID The case identifier to use (not supported in a persisting engine)
     * @return
     * @throws YStateException
     * @throws YDataStateException
     * @throws YSchemaBuildingException
     */
    String launchCase(String username, String specID, String caseParams, URI completionObserver, String caseID) throws YStateException, YDataStateException, YSchemaBuildingException, YPersistenceException, YEngineStateException;

    /**
     * Returns the next available caseID to be used when launching a new case where this is required to be known
     * via the launch request.
     *
     * @return  A unique CaseID which can be used to start a new case.
     * @throws YPersistenceException
     */
	String allocateCaseID() throws YPersistenceException;    

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
    YTask getTaskDefinition(YSpecificationID specificationID, String taskID);
}
