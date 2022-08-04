package org.yawlfoundation.yawl.stateless;

import org.yawlfoundation.yawl.engine.WorkItemCompletion;
import org.yawlfoundation.yawl.exceptions.*;
import org.yawlfoundation.yawl.logging.YLogDataItemList;
import org.yawlfoundation.yawl.stateless.elements.YSpecification;
import org.yawlfoundation.yawl.stateless.elements.marking.YIdentifier;
import org.yawlfoundation.yawl.stateless.engine.YEngine;
import org.yawlfoundation.yawl.stateless.engine.YNetRunner;
import org.yawlfoundation.yawl.stateless.engine.YWorkItem;
import org.yawlfoundation.yawl.stateless.listener.*;
import org.yawlfoundation.yawl.stateless.listener.event.YCaseEvent;
import org.yawlfoundation.yawl.stateless.listener.event.YEvent;
import org.yawlfoundation.yawl.stateless.listener.event.YEventType;
import org.yawlfoundation.yawl.stateless.monitor.YCase;
import org.yawlfoundation.yawl.stateless.monitor.YCaseExporter;
import org.yawlfoundation.yawl.stateless.monitor.YCaseImporter;
import org.yawlfoundation.yawl.stateless.monitor.YCaseMonitor;
import org.yawlfoundation.yawl.stateless.unmarshal.YMarshal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Michael Adams
 * @date 21/8/20
 */
public class YStatelessEngine {

    private final YEngine _engine;
    private YCaseMonitor _caseMonitor;                        // watches for idle cases


    /**
     * Create a new stateless YAWL engine
     */
    public YStatelessEngine() {
        _engine = new YEngine();
    }


    /**
     * Create a new stateless YAWL engine, and start a case monitor that will
     * announce idle cases to listeners.
     * @param idleCaseTimerMsecs the number of milliseconds that a case is allowed to
     *                           remain idle. After the msecs have passed, all
     *                           engine YCaseEventListeners will be notified of the
     *                           CASE_IDLE_TIMEOUT event. A negative value disable
     *                           the case monitor and the announcement of timeout events.
     */
    public YStatelessEngine(long idleCaseTimerMsecs) {
        this();
        setIdleCaseTimer(idleCaseTimerMsecs);
    }


    /**
     * Set the idle case timer value for case monitoring. If case monitoring is currently
     * enabled, the idle time is updated. If case monitoring not is currently enabled,
     * and the msecs value is positive, case monitoring is started with that value.
     * @param msecs the number of milliseconds that a case is allowed to remain idle.
     *              After the msecs have passed, all engine YCaseEventListeners will be
     *              notified of the CASE_IDLE_TIMEOUT event. A non-positive value disable
     *              the case idle time monitoring and the announcement of timeout events.
     */
    public void setIdleCaseTimer(long msecs) {
        if (_caseMonitor != null) {
            _caseMonitor.setIdleTimeout(msecs);
        }
        else if (msecs > 0) {
            setCaseMonitoringEnabled(true, msecs);
        }
    }


    /**
     * Enable or disable a case monitor that tracks all running cases.
     * @param enable If true, enables the case monitor, but without monitoring case idle
     *               times. If false, disables case monitoring (and discards all
     *               currently monitored cases)
     */
    public void setCaseMonitoringEnabled(boolean enable) {
        setCaseMonitoringEnabled(enable, 0);
    }


    /**
     * Enable or disable a case monitor that tracks all running cases, and optionally
     * monitor each case for idle time.
     * @param enable If true, enables the case monitor, and if the case monitor is
     *               already enabled, updates its timeout value for all current cases.
     *               If false, disables case monitoring (and discards all currently
     *               monitored cases)
     * @param idleTimeout the number of milliseconds that a case is allowed to remain idle.
     *              After the msecs have passed, all engine YCaseEventListeners will be
     *              notified of the CASE_IDLE_TIMEOUT event. A negative value disable
     *              idle time monitor and the announcement of timeout events.
     */
    public void setCaseMonitoringEnabled(boolean enable, long idleTimeout) {
        if (enable) {
            if (_caseMonitor == null) {
                _caseMonitor = new YCaseMonitor(idleTimeout);
                addCaseEventListener(_caseMonitor);
                addWorkItemEventListener(_caseMonitor);
            }
            else {
                setIdleCaseTimer(idleTimeout);
            }
        }
        else {
            if (_caseMonitor != null) {
                 removeCaseEventListener(_caseMonitor);
                 removeWorkItemEventListener(_caseMonitor);
                 _caseMonitor.cancel();
                 _caseMonitor = null;
            }
        }
    }


    public boolean isCaseMonitoringEnabled() { return _caseMonitor != null; }
    

    public int getEngineNbr() { return _engine.getEngineNbr(); }
    
    
    /**
     * Add a listener for case events
     * @param listener an object that implements the YCaseEventListener interface
     */
    public void addCaseEventListener(YCaseEventListener listener) {
        _engine.getAnnouncer().addCaseEventListener(listener);
    }
    

    /**
     * Add a listener for work item events
     * @param listener an object that implements a YWorkItemEventListener interface
     */
    public void addWorkItemEventListener(YWorkItemEventListener listener) {
        _engine.getAnnouncer().addWorkItemEventListener(listener);
    }

    /**
     * Add a listener for exception events
     * @param listener an object that implements the YExceptionEventListener interface
     */
    public void addExceptionEventListener(YExceptionEventListener listener) {
        _engine.getAnnouncer().addExceptionEventListener(listener);
    }

    /**
     * Add a listener for log events
     * @param listener an object that implements the YLogEventListener interface
     */
    public void addLogEventListener(YLogEventListener listener) {
        _engine.getAnnouncer().addLogEventListener(listener);
    }

    /**
     * Add a listener for timer events
     * @param listener an object that implements the YTimerEventListener interface
     */
    public void addTimerEventListener(YTimerEventListener listener) {
        _engine.getAnnouncer().addTimerEventListener(listener);
    }


    /**
     * Remove a registered listener for case events
     * @param listener a previously registered YCaseEventListener
     */
    public void removeCaseEventListener(YCaseEventListener listener) {
        _engine.getAnnouncer().removeCaseEventListener(listener);
    }

    /**
     * Remove a registered listener for work item events
     * @param listener a previously registered YWorkItemEventListener
     */
    public void removeWorkItemEventListener(YWorkItemEventListener listener) {
        _engine.getAnnouncer().removeWorkItemEventListener(listener);
    }

    /**
     * Remove a registered listener for exception events
     * @param listener a previously registered YExceptionEventListener
     */
    public void removeExceptionEventListener(YExceptionEventListener listener) {
        _engine.getAnnouncer().removeExceptionEventListener(listener);
    }

    /**
     * Remove a registered listener for log events
     * @param listener a previously registered YLogEventListener
     */
    public void removeLogEventListener(YLogEventListener listener) {
        _engine.getAnnouncer().removeLogEventListener(listener);
    }

    /**
     * Remove a registered listener for timer events
     * @param listener a previously registered YTimerEventListener
     */
    public void removeTimerEventListener(YTimerEventListener listener) {
        _engine.getAnnouncer().removeTimerEventListener(listener);
    }


    /**
     * Transform a YAWL specification XML string to a YSpecification object.
     * @param xml the XML representation of the YAWL specification
     * @return a populated YSpecification object
     * @throws YSyntaxException if the XML is malformed
     */
    public YSpecification unmarshalSpecification(String xml) throws YSyntaxException {
        return YMarshal.unmarshalSpecifications(xml).get(0);
    }


    /**
     * Launch a new case instance for the specification specified. A random UUID is
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
     * Launch a new case instance for the specification specified
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
     * Launch a new case instance for the specification specified
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
     * Launch a new case instance for the specification specified
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
     * Suspend a currently running case
     * @param runner the current case state object
     * @throws YStateException if the case state is out-of-sync
     */
    public void suspendCase(YNetRunner runner) throws YStateException {
        _engine.suspendCase(runner);
    }

    /**
      * Resume a currently suspended case
      * @param runner the current case state object
      * @throws YStateException if the case state is out-of-sync
      */
    public void resumeCase(YNetRunner runner)
            throws YStateException, YQueryException, YDataStateException {
        _engine.resumeCase(runner);
    }

    /**
     * Suspend an executing work item
     * @param workItem the work item to suspend
     * @return the suspended work item
     * @throws YStateException if the case state is out-of-sync
     */
    public YWorkItem suspendWorkItem(YWorkItem workItem) throws YStateException {
        return _engine.suspendWorkItem(workItem);
    }

    /**
     * Resume a suspended work item
     * @param workItem the work item to suspend
     * @return the suspended work item
     * @throws YStateException if the case state is out-of-sync
     */
    public YWorkItem unsuspendWorkItem(YWorkItem workItem) throws YStateException {
        return _engine.unsuspendWorkItem(workItem) ;
    }

    /**
     * Roll back a work item from executing to enabled
     * @param workItem the work item to roll back
     * @throws YStateException
     */
    public void rollbackWorkItem(YWorkItem workItem) throws YStateException {
        _engine.rollbackWorkItem(workItem);
    }

    /**
     * Complete a currently executing work item

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
     * Complete a currently executing work item
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
     * Begin executing a currently enabled or fired work item
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
     * Skip an enabled work item (immediately completes)
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
     * Create a new work item instance from a dynamic multi-instance task
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
     * Determine whether a task will allow a dynamically created new instance to
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


    public void cancelCase(YNetRunner runner) {
        for (YNetRunner aRunner : runner.getAllRunnersForCase()) {
            aRunner.cancel();
        }
        runner.getAnnouncer().announceCaseEvent(
                new YCaseEvent(YEventType.CASE_CANCELLED, runner.getTopRunner()));
    }


    /**
     * Get the complete state of a case, marshalled to an XML document
     * @param caseID the case to get
     * @return an XML document of the case state
     * @throws YStateException if there's any problem capturing the current state
     */
    public String unloadCase(YIdentifier caseID) throws YStateException {
        if (_caseMonitor == null) {
            throw new YStateException("This engine is not monitoring idle cases");
        }
        YCase yCase = _caseMonitor.unloadCase(caseID);           // notnull guaranteed
        yCase.cancelWorkItemTimers();
        String caseXML = yCase.marshal();                        // ditto
        _engine.getAnnouncer().announceCaseEvent(
                new YCaseEvent(YEventType.CASE_UNLOADED, yCase.getRunner()));
        return caseXML;
    }


    /**
     * Marshals an active case to an XML document
     * @param runner a runner within the case
     * @return an XML document of the case state
     * @throws YStateException if there's any problem marshaling the current state
     */
    public String marshalCase(YNetRunner runner) throws YStateException {
        if (runner == null) {
            throw new YStateException("Missing state for case: runner is null.");
        }
        return new YCaseExporter().marshal(runner);
    }


    /**
     * Restores a case instance from its XML representation (previously returned from
     * unloadCase()
     * @param caseXML the XML of the case state to restore
     * @return The primary net runner of the case
     * @throws YSyntaxException if there's an error in the specification portion of the xml
     * @throws YStateException if there's any problem restoring the current state
     */
    public YNetRunner restoreCase(String caseXML) throws YSyntaxException, YStateException {
        List<YNetRunner> runners = new YCaseImporter().unmarshal(caseXML, _engine.getAnnouncer());
        List<YEvent> events = new ArrayList<>();
        YNetRunner topRunner = null;
        for (YNetRunner runner : runners) {
            events.addAll(runner.generateItemReannouncements());
            if (topRunner == null) {
                topRunner = runner.getTopRunner();
            }
        }
        if (topRunner == null) throw new YStateException("Failed to restore case runner");
        events.add(0, new YCaseEvent(YEventType.CASE_RESTORED, topRunner));
        _engine.getAnnouncer().announceEvents(events);
        return topRunner;
    }

}
