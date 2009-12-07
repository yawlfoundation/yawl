/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.engine.interfce.interfaceA;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;
import org.yawlfoundation.yawl.authentication.YExternalClient;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.YWorkItem;
import org.yawlfoundation.yawl.engine.announcement.AnnouncementContext;
import org.yawlfoundation.yawl.exceptions.YEngineStateException;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.exceptions.YStateException;
import org.yawlfoundation.yawl.util.YVerificationMessage;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Defines the 'A' interface into the YAWL Engine corresponding to WfMC interface 5 - Administration + Monitoring.
 *
 * @author Andrew Hastie
 *         Creation Date: 10-Jun-2005
 */
public interface InterfaceAManagement {
    /**
     * Register an InterfaceA management client with the engine in order to recieve callbacks.<P>
     *
     * @param observer
     */
    void registerInterfaceAClient(InterfaceAManagementObserver observer);

    /**
     * Returns a set of keys for the loaded process specifications currently loaded into the engine.
     *
     * @return  Keys of loaded specifications
     */

    /**
     * Loads a process specification into the engine from an XML definition file created by the YAWL Designer.<P>
     *
     * @param specificationStr
     * @param ignoreErors
     * @param errorMessages
     * @return
     * @throws JDOMException
     * @throws IOException
     * @throws YPersistenceException
     */
    List<YSpecificationID> addSpecifications(String specificationStr, boolean ignoreErors,
                                             List<YVerificationMessage> errorMessages)
                               throws JDOMException, IOException, YPersistenceException;

    boolean loadSpecification(YSpecification spec);
    
    Set<YSpecificationID> getLoadedSpecifications() throws YPersistenceException;

    /**
     * Returns the process specification identified by its ID.<P>
     *
     * The process must have been loaded into the engine either when it initialised (using persisted specifications
     * held in the database), or specifications added during the lifetime of the current engine instance. Null is
     * returned if the process ID requested is not found in the engines internal cache. Further, this returns the
     * newest version of the specification running.
     * @param id
     */
    YSpecification getLatestSpecification(String id);

    YSpecification getSpecification(YSpecificationID specID);

    YSpecification getSpecificationForCase(YIdentifier caseID);


    /**
     * Unloads a specification from the engine.<P>
     *
     * @param specID
     * @throws YStateException
     */
    void unloadSpecification(YSpecificationID specID) throws YStateException, YPersistenceException;

    /**
     * Given a process specification id return the cases that are its running
     * instances.
     *
     * @param specID the process specification id string.
     * @return a set of YIdentifer caseIDs that are run time instances of the
     *         process specification with id = specID
     */
    Set<YIdentifier> getCasesForSpecification(YSpecificationID specID);

    /**
     * Returns the internal engine identifier for a case.<P>
     *
     * @param caseIDStr
     * @return
     */
    YIdentifier getCaseID(String caseIDStr) throws YPersistenceException;

    /**
     * Returns the text description for the state that a case is currently in.<P>
     * @param caseID
     * @return
     */
    String getStateTextForCase(YIdentifier caseID) throws YPersistenceException;

    /**
     * Returns the state for a case.<P>
     * @param caseID
     * @return
     */
    String getStateForCase(YIdentifier caseID) throws YPersistenceException;

    /**
     * Cancel the execution of a case.<P>
     *
     * @param id
     * @throws YPersistenceException
     */
    void cancelCase(YIdentifier id) throws YPersistenceException, YEngineStateException;

    /**
     * Returns a set of users currently loaded within the engine.<P>
     * Suspends execution of a case.
     *
     * @param id
     * @throws YPersistenceException
     */
    void suspendCase(YIdentifier id) throws YPersistenceException, YStateException;

    /**
     * Resumes execution of a case.
     *
     * @param id
     * @throws YPersistenceException
     */
    void resumeCase(YIdentifier id) throws YPersistenceException, YStateException;

    /**
     * Returns the execution status of a case.
     *
     * @param id
     * @return
     * @throws YPersistenceException
     */
    int getCaseExecutionStatus(YIdentifier id) throws YPersistenceException;




    /**
     * Returns a set of users currently loaded within the engine.<P>
     */
    Set getUsers();

    YExternalClient getExternalClient(String name);

    boolean addExternalClient(YExternalClient client) throws YPersistenceException ;

    YAWLServiceReference getRegisteredYawlService(String yawlServiceID);

    /**
     * Loads a YAWL service into the engine.<P>
     *
     * @param yawlService
     */
    void addYawlService(YAWLServiceReference yawlService) throws YPersistenceException;

    /**
     * Removes a YAWL service from the engine.<P>
     *
     * @param serviceURI
     * @return
     */
    YAWLServiceReference removeYawlService(String serviceURI) throws YPersistenceException;

    /**
     * Indicates the load status of the supplied specification ID.<P>
     *
     *
     * @param specID
     * @return
     */
    String getLoadStatus(YSpecificationID specID);

    /**
     * Causes the engine to re-announce all workitems which are in an "enabled" state.<P>
     *
     * @return The number of enabled workitems that were reannounced
     */
    int reannounceEnabledWorkItems() throws YStateException;

    /**
     * Causes the engine to re-announce all workitems which are in an "executing" state.<P>
     *
     * @return The number of executing workitems that were reannounced
     */
    int reannounceExecutingWorkItems() throws YStateException;

    /**
     * Causes the engine to re-announce all workitems which are in an "fired" state.<P>
     *
     * @return The number of fired workitems that were reannounced
     */
    int reannounceFiredWorkItems() throws YStateException;

    /**
     * Causes the engine to re-announce a specific workitem regardless of state.<P>
     *
     * Note: This interface current;y only supported workitems in the following states:
     * <li>Enabled
     * <li>Executing
     * <li>Fired
     */
    void reannounceWorkItem(YWorkItem workItem) throws YStateException;


    /**
     * Returns the specification for a loaded specification via the supplied specification ID.<P>
     * @param specID
     * @return
     */
    YSpecification getProcessDefinition(YSpecificationID specID);

    /**
     * Stores an object within the engine's persistent storage area.<P>
     *
     * @param object
     * @throws YPersistenceException
     */
    void storeObject(Object object) throws YPersistenceException;

    /**
     * Invokes a diagnostic dump of the engine's internal tables.
     */
    void dump();

    void dump(Logger logger);

    void setEngineStatus(int engineStatus);

    int getEngineStatus();

    AnnouncementContext getAnnouncementContext();

}
