/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YStateException;
import au.edu.qut.yawl.util.YVerificationMessage;
import org.jdom.JDOMException;

import java.io.File;
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
     * @param specificationFile
     * @param ignoreErors
     * @param errorMessages
     * @return
     * @throws JDOMException
     * @throws IOException
     * @throws YPersistenceException
     */
    List addSpecifications(File specificationFile, boolean ignoreErors, List<YVerificationMessage> errorMessages) throws JDOMException, IOException, YPersistenceException;

    boolean loadSpecification(YSpecification spec);

    Set getLoadedSpecifications() throws YPersistenceException;

    /**
     * Returns the process specification identified by its ID.<P>
     *
     * The process must have been loaded into the engine either when it initialised (using persisted specifications
     * held in the database), or specifications added during the lifetime of the current engine instance. Null is
     * returned if the process ID requested is not found in the engines internal cache.
     * @param specID
     * @return
     */
    YSpecification getSpecification(String specID);

    /**
     * Unloads a specification from the engine.<P>
     *
     * @param specID
     * @throws YStateException
     */
    void unloadSpecification(String specID) throws YStateException, YPersistenceException;

    /**
     * Given a process specification id return the cases that are its running
     * instances.
     *
     * @param specID the process specification id string.
     * @return a set of YIdentifer caseIDs that are run time instances of the
     *         process specification with id = specID
     */
    Set getCasesForSpecification(String specID);

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
    void cancelCase(YIdentifier id) throws YPersistenceException;

    /**
     * Returns a set of users currently loaded within the engine.<P>
     *
     * @return
     */
    Set getUsers();

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
    String getLoadStatus(String specID);

    /**
     * Returns the specification for a loaded specification via the supplied specification ID.<P>
     * @param specID
     * @return
     */
    YSpecification getProcessDefinition(String specID);

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
}
