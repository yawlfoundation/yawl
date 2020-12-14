package org.yawlfoundation.yawl.stateless;

import org.yawlfoundation.yawl.engine.WorkItemCompletion;
import org.yawlfoundation.yawl.exceptions.*;
import org.yawlfoundation.yawl.logging.YLogDataItemList;
import org.yawlfoundation.yawl.stateless.elements.YSpecification;
import org.yawlfoundation.yawl.stateless.engine.YEngine;
import org.yawlfoundation.yawl.stateless.engine.YNetRunner;
import org.yawlfoundation.yawl.stateless.engine.YWorkItem;
import org.yawlfoundation.yawl.stateless.listener.*;
import org.yawlfoundation.yawl.stateless.unmarshal.YMarshal;

import java.util.UUID;

/**
 * @author Michael Adams
 * @date 21/8/20
 */
public class YStatelessEngine {

    private final YEngine _engine;


    public YStatelessEngine() {
        _engine = new YEngine();
    }


    /**
     * Adds a listener for case events
     * @param listener an object that implements the YCaseEventListener interface
     */
    public void addCaseEventListener(YCaseEventListener listener) {
        _engine.getAnnouncer().addCaseEventListener(listener);
    }

    /**
     * Adds a listener for work item events
     * @param listener an object that implements a YWorkItemEventListener interface
     */
    public void addWorkItemEventListener(YWorkItemEventListener listener) {
        _engine.getAnnouncer().addWorkItemEventListener(listener);
    }

    /**
     * Adds a listener for exception events
     * @param listener an object that implements the YExceptionEventListener interface
     */
    public void addExceptionEventListener(YExceptionEventListener listener) {
        _engine.getAnnouncer().addExceptionEventListener(listener);
    }

    /**
     * Adds a listener for log events
     * @param listener an object that implements the YLogEventListener interface
     */
    public void addLogEventListener(YLogEventListener listener) {
        _engine.getAnnouncer().addLogEventListener(listener);
    }

    /**
     * Adds a listener for timer events
     * @param listener an object that implements the YTimerEventListener interface
     */
    public void addTimerEventListener(YTimerEventListener listener) {
        _engine.getAnnouncer().addTimerEventListener(listener);
    }


    /**
     * Removes a registered listener for case events
     * @param listener a previously registered YCaseEventListener
     */
    public void removeCaseEventListener(YCaseEventListener listener) {
        _engine.getAnnouncer().removeCaseEventListener(listener);
    }

    /**
     * Removes a registered listener for work item events
     * @param listener a previously registered YWorkItemEventListener
     */
    public void removeWorkItemEventListener(YWorkItemEventListener listener) {
        _engine.getAnnouncer().removeWorkItemEventListener(listener);
    }

    /**
     * Removes a registered listener for exception events
     * @param listener a previously registered YExceptionEventListener
     */
    public void removeExceptionEventListener(YExceptionEventListener listener) {
        _engine.getAnnouncer().removeExceptionEventListener(listener);
    }

    /**
     * Removes a registered listener for log events
     * @param listener a previously registered YLogEventListener
     */
    public void removeLogEventListener(YLogEventListener listener) {
        _engine.getAnnouncer().removeLogEventListener(listener);
    }

    /**
     * Removes a registered listener for timer events
     * @param listener a previously registered YTimerEventListener
     */
    public void removeTimerEventListener(YTimerEventListener listener) {
        _engine.getAnnouncer().removeTimerEventListener(listener);
    }


    /**
     * Transforms a YAWL specification XML string to a YSpecification object.
     * @param xml the XML representation of the YAWL specification
     * @return a populated YSpecification object
     * @throws YSyntaxException if the XML is malformed
     */
    public YSpecification unmarshalSpecification(String xml) throws YSyntaxException {
        return YMarshal.unmarshalSpecifications(xml).get(0);
    }


    /**
     * Launches a new case instance for the specification specified. A random UUID is
     * assigned as the case identifier.
     * @param spec the YAWL specification to create an instance from
     * @return a YNetRunner object encapsulating the current case state
     * @throws YStateException if there is a problem creating the case state
     * @throws YDataStateException if the data state cannot be initialised
     * @throws YEngineStateException if the engine is not in running state
     * @throws YQueryException if the data extraction query is malformed
     */
    public YNetRunner launchCase(YSpecification spec)
            throws YStateException, YDataStateException, YEngineStateException, YQueryException {
        return launchCase(spec, UUID.randomUUID().toString());
    }


    /**
     * Launches a new case instance for the specification specified
     * @param spec the YAWL specification to create an instance from
     * @param caseID the case identifier to assign to the new case
     * @return a YNetRunner object encapsulating the current case state
     * @throws YStateException if there is a problem creating the case state
     * @throws YDataStateException if the data state cannot be initialised
     * @throws YEngineStateException if the engine is not in running state
     * @throws YQueryException if the data extraction query is malformed
     */
    public YNetRunner launchCase(YSpecification spec, String caseID)
            throws YStateException, YDataStateException, YEngineStateException, YQueryException {
        return launchCase(spec, caseID, null);
    }


    /**
     * Launches a new case instance for the specification specified
     * @param spec the YAWL specification to create an instance from
     * @param caseID the case identifier to assign to the new case
     * @param caseParams an XML string denoting the initial data values for case starting
     * @return a YNetRunner object encapsulating the current case state
     * @throws YStateException if there is a problem creating the case state
     * @throws YDataStateException if the data state cannot be initialised
     * @throws YEngineStateException if the engine is not in running state
     * @throws YQueryException if the data extraction query is malformed
     */
    public YNetRunner launchCase(YSpecification spec, String caseID, String caseParams)
            throws YStateException, YDataStateException, YEngineStateException, YQueryException {
        return launchCase(spec, caseID, caseParams, null);
    }


    /**
     * Launches a new case instance for the specification specified
     * @param spec the YAWL specification to create an instance from
     * @param caseID the case identifier to assign to the new case
     * @param caseParams an XML string denoting the initial data values for case starting
     * @param logItems a list of info items to be logged when the case starts
     * @return a YNetRunner object encapsulating the current case state
     * @throws YStateException if there is a problem creating the case state
     * @throws YDataStateException if the data state cannot be initialised
     * @throws YEngineStateException if the engine is not in running state
     * @throws YQueryException if the data extraction query is malformed
     */
    public YNetRunner launchCase(YSpecification spec, String caseID, String caseParams, YLogDataItemList logItems)
            throws YStateException, YDataStateException, YEngineStateException, YQueryException {
        return _engine.launchCase(spec, caseID, caseParams, logItems);
    }


    /**
     * Suspends a currently running case
     * @param runner the current case state object
     * @throws YStateException if the case state is out-of-sync
     */
    public void suspendCase(YNetRunner runner) throws YStateException {
        _engine.suspendCase(runner);
    }

    /**
      * Resumes a currently suspended case
      * @param runner the current case state object
      * @throws YStateException if the case state is out-of-sync
      */
    public void resumeCase(YNetRunner runner)
            throws YStateException, YQueryException, YDataStateException {
        _engine.resumeCase(runner);
    }

    /**
     * Suspends an executing work item
     * @param workItem the work item to suspend
     * @return the suspended work item
     * @throws YStateException if the case state is out-of-sync
     */
    public YWorkItem suspendWorkItem(YWorkItem workItem) throws YStateException {
        return _engine.suspendWorkItem(workItem);
    }

    /**
     * Resumes a suspended work item
     * @param workItem the work item to suspend
     * @return the suspended work item
     * @throws YStateException if the case state is out-of-sync
     */
    public YWorkItem unsuspendWorkItem(YWorkItem workItem) throws YStateException {
        return _engine.unsuspendWorkItem(workItem) ;
    }

    /**
     * Rolls back a work item from executing to enabled
     * @param workItem the work item to roll back
     * @throws YStateException
     */
    public void rollbackWorkItem(YWorkItem workItem) throws YStateException {
        _engine.rollbackWorkItem(workItem);
    }

    /**
     * Completes a currently executing work item

     * @param workItem the work item to complete
     * @param data an XML string representing the item's output data
     * @param logPredicate a log predicate string to be populated and added to the log
     * @param completionType one of: NORMAL, FORCE or FAIL
     * @throws YEngineStateException if the engine is not in running state
     * @throws YStateException if there is a problem creating the case state
     * @throws YQueryException if the data extraction query is malformed
     * @throws YDataStateException if the data state cannot be initialised
     */
    public void completeWorkItem(YWorkItem workItem, String data,
                                 String logPredicate, WorkItemCompletion completionType )
            throws YEngineStateException, YStateException, YQueryException, YDataStateException {
        _engine.completeWorkItem(workItem, data, logPredicate, completionType);
    }

    /**
     * Completes a currently executing work item
     * @param workItem the work item to complete
     * @param data an XML string representing the item's output data
     * @param logPredicate a log predicate string to be populated and added to the log
     * @throws YEngineStateException if the engine is not in running state
     * @throws YStateException if there is a problem creating the case state
     * @throws YQueryException if the data extraction query is malformed
     * @throws YDataStateException if the data state cannot be initialised
     */
    public void completeWorkItem(YWorkItem workItem, String data,
                                 String logPredicate)
            throws YEngineStateException, YStateException, YQueryException, YDataStateException {
        _engine.completeWorkItem(workItem, data, logPredicate, WorkItemCompletion.Normal);
    }


    /**
     * Begins executing a currently enabled or fired work item
     * @param workItem the work item to start
     * @throws YEngineStateException if the engine is not in running state
     * @throws YStateException if there is a problem creating the case state
     * @throws YQueryException if the data extraction query is malformed
     * @throws YDataStateException if the data state cannot be initialised
     */
    public YWorkItem startWorkItem(YWorkItem workItem)
            throws YEngineStateException, YStateException, YQueryException, YDataStateException {
        return _engine.startWorkItem(workItem);
    }

    /**
     * Skips an enabled work item (immediately completes)
     * @param workItem the work item to skip
     * @throws YEngineStateException if the engine is not in running state
     * @throws YStateException if there is a problem creating the case state
     * @throws YQueryException if the data extraction query is malformed
     * @throws YDataStateException if the data state cannot be initialised
     */
    public YWorkItem skipWorkItem(YWorkItem workItem)
            throws YEngineStateException, YStateException, YQueryException, YDataStateException {
        return _engine.skipWorkItem(workItem);
    }


    /**
     * Creates a new work item instance from a dynamic multi-instance task
     * @param workItem the work item to create a new instance of
     * @param paramValueForMICreation format "<data>[InputParam]</data>
     *                                InputParam == <varName>varValue</varName>
     * @return the work item of the new instance.
     * @throws YStateException if the task is not able to create a new instance, due to
     *                         its state or its design.
     */
    public YWorkItem createNewInstance(YWorkItem workItem, String paramValueForMICreation)
            throws YStateException {
        return _engine.createNewInstance(workItem, paramValueForMICreation);
    }


    /**
     * Determines whether or not a task will allow a dynamically created new instance to
     * be created.  MultiInstance Task with dynamic instance creation is required.
     * @param workItem the work item to check
     * @throws YStateException if task is not MultiInstance, or
     *                         if task does not allow dynamic instance creation,
     *                         or if current number of instances is not less than the maxInstances
     *                         for the task.
     */
    public void checkElegibilityToAddInstances(YWorkItem workItem) throws YStateException {
        _engine.checkEligibilityToAddInstances(workItem);
    }

}
