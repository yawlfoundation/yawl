/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.stateless.engine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.YWorkItemStatus;
import org.yawlfoundation.yawl.logging.YLogDataItemList;
import org.yawlfoundation.yawl.stateless.elements.YSpecification;
import org.yawlfoundation.yawl.stateless.elements.YTask;
import org.yawlfoundation.yawl.stateless.elements.marking.YIdentifier;
import org.yawlfoundation.yawl.stateless.listener.*;
import org.yawlfoundation.yawl.stateless.listener.event.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Handles the announcement of engine-generated events to the environment.
 *
 * @author Michael Adams
 * @date 10/04/2010
 */
public class YAnnouncer {

    private final Logger _logger;
    private final Set<YExceptionEventListener> _exceptionListeners;
    private final Set<YCaseEventListener> _caseListeners;
    private final Set<YWorkItemEventListener> _workItemListeners;
    private final Set<YTimerEventListener> _timerListeners;
    private final Set<YLogEventListener> _logListeners;
    private final ExecutorService _executor = Executors.newFixedThreadPool(12);
    private final YEngine _engine;


    protected YAnnouncer(YEngine engine) {
        _engine = engine;
        _logger = LogManager.getLogger(this.getClass());
        _exceptionListeners = new HashSet<>();
        _caseListeners = new HashSet<>();
        _workItemListeners = new HashSet<>();
        _logListeners = new HashSet<>();
        _timerListeners = new HashSet<>();
    }


    public boolean addCaseEventListener(YCaseEventListener listener) {
        return _caseListeners.add(listener);
    }

    public boolean addWorkItemEventListener(YWorkItemEventListener listener) {
        return _workItemListeners.add(listener);
    }

    public boolean addExceptionEventListener(YExceptionEventListener listener) {
        return _exceptionListeners.add(listener);
    }

    public boolean addLogEventListener(YLogEventListener listener) {
        return _logListeners.add(listener);
    }

    public boolean addTimerEventListener(YTimerEventListener listener) {
        return _timerListeners.add(listener);
    }

    public boolean removeCaseEventListener(YCaseEventListener listener) {
        return _caseListeners.remove(listener);
    }

    public boolean removeWorkItemEventListener(YWorkItemEventListener listener) {
        return _workItemListeners.remove(listener);
    }

    public boolean removeExceptionEventListener(YExceptionEventListener listener) {
        return _exceptionListeners.remove(listener);
    }

    public boolean removeLogEventListener(YLogEventListener listener) {
        return _logListeners.remove(listener);
    }

    public boolean removeTimerEventListener(YTimerEventListener listener) {
        return _timerListeners.remove(listener);
    }

    public YEngine getEngine() { return _engine; }
    
    public void announceCaseEvent(YCaseEvent event) {
        for (YCaseEventListener listener : _caseListeners) {
            _executor.execute(() -> listener.handleCaseEvent(event));
        }
    }

    public void announceWorkItemEvent(YWorkItemEvent event) {
        for (YWorkItemEventListener listener : _workItemListeners) {
            _executor.execute(() -> listener.handleWorkItemEvent(event));
        }
    }

    public void announceExceptionEvent(YExceptionEvent event) {
        for (YExceptionEventListener listener : _exceptionListeners) {
            _executor.execute(() -> listener.handleExceptionEvent(event));
        }
    }

    public void announceLogEvent(YLogEvent event) {
        for (YLogEventListener listener : _logListeners) {
            _executor.execute(() -> listener.handleLogEvent(event));
        }
    }

    public void announceTimerEvent(YTimerEvent event) {
        for (YTimerEventListener listener : _timerListeners) {
            _executor.execute(() -> listener.handleTimerEvent(event));
        }
    }

    public void announceEvents(Set<YEvent> eventSet) {
        for (YEvent event : eventSet) {
            if (event instanceof YWorkItemEvent) {
                announceWorkItemEvent((YWorkItemEvent) event);
            }
            else if (event instanceof YLogEvent) {
                announceLogEvent((YLogEvent) event);
            }
            else if (event instanceof YCaseEvent) {
                announceCaseEvent((YCaseEvent) event);
            }
            else if (event instanceof YTimerEvent) {
                 announceTimerEvent((YTimerEvent) event);
             }
            else if (event instanceof YExceptionEvent) {
                 announceExceptionEvent((YExceptionEvent) event);
             }
        }
    }

    public void announceRunnerEvents(Set<YWorkItemEvent> eventSet) {
        for (YWorkItemEvent event : eventSet) {
            announceWorkItemEvent(event);
        }
    }


    /******************************************************************************/
    // CASE & WORKITEM ANNOUNCEMENTS //

    
    /**
     * Called by the engine when a case is cancelled. Broadcast to all case and exception listeners.
     */
    protected void announceCaseCancellation(YNetRunner runner) {
        YIdentifier caseID = runner.getCaseID();
        if (! _caseListeners.isEmpty()) {
            announceCaseEvent(new YCaseEvent(YEventType.CASE_CANCELLED, caseID));
        }
        if (! _exceptionListeners.isEmpty()) {
            announceExceptionEvent(new YExceptionEvent(YEventType.CASE_CANCELLED, caseID));
        }
        if (! _logListeners.isEmpty()) {
            announceLogEvent(new YLogEvent(YEventType.CASE_CANCELLED, caseID,
                    runner.getSpecificationID(), null));
        }
    }


    /**
     * Called by YWorkItemTimer when a work item's timer expires. Announced only to
     * the designated service or gateway.
     * @param item the work item that has had its timer expire
     */
    public void announceTimerExpiryEvent(YWorkItem item) {
//        if (! _workItemListeners.isEmpty()) {
//            announceWorkItemEvent(new YWorkItemEvent(YEventType.TIMER_EXPIRED, item));
//        }
        if (! _timerListeners.isEmpty()) {
            announceTimerEvent(new YTimerEvent(YEventType.TIMER_EXPIRED, item));
        }
        if (! _exceptionListeners.isEmpty()) {
            announceExceptionEvent(new YExceptionEvent(YEventType.TIMER_EXPIRED, item));
        }
    }


    public void announceTimerStartedEvent(YWorkItem item) {
//        if (! _workItemListeners.isEmpty()) {
//             announceWorkItemEvent(new YWorkItemEvent(YEventType.TIMER_STARTED, item));
//         }
         if (! _timerListeners.isEmpty()) {
             announceTimerEvent(new YTimerEvent(YEventType.TIMER_STARTED, item));
         }
    }


    public void announceTimerCancelledEvent(YWorkItem item) {
//        if (! _workItemListeners.isEmpty()) {
//             announceWorkItemEvent(new YWorkItemEvent(YEventType.TIMER_CANCELLED, item));
//         }
         if (! _timerListeners.isEmpty()) {
             announceTimerEvent(new YTimerEvent(YEventType.TIMER_CANCELLED, item));
         }
    }


    /**
     * Called by the engine when a case is suspending. Broadcast to all case listeners.
     * @param id the identifier of the suspending case
     */
    protected void announceCaseSuspending(YIdentifier id) {
        if (! _caseListeners.isEmpty()) {
            announceCaseEvent(new YCaseEvent(YEventType.CASE_SUSPENDING, id));
        }
    }


    /**
     * Called by the engine when a case has suspended. Broadcast to all case listeners.
     * @param id the identifier of the suspended case
     */
    protected void announceCaseSuspended(YIdentifier id) {
        if (! _caseListeners.isEmpty()) {
            announceCaseEvent(new YCaseEvent(YEventType.CASE_SUSPENDED, id));
        }
    }


    /**
     * Called by the engine when a case has resumed from suspension. Broadcast to case listeners.
     * @param id the identifier of the resumed case
     */
    protected void announceCaseResumption(YIdentifier id) {
        if (! _caseListeners.isEmpty()) {
            announceCaseEvent(new YCaseEvent(YEventType.CASE_RESUMED, id));
        }
    }
    
    
    protected void announceCaseStart(YSpecification spec, YIdentifier id, YLogDataItemList logData) {
        if (! _caseListeners.isEmpty()) {
            announceCaseEvent(new YCaseEvent(YEventType.CASE_START, id, spec));
        }
        if (! _logListeners.isEmpty()) {
            announceLogEvent(new YLogEvent(YEventType.CASE_START, id, spec, logData));
        }
    }
 

    /**
     * Called by a case's net runner when it completes. Announced to case listeners.
     * @param id the identifier of the completed case
     * @param caseData the final output data for the case
     */
    protected void announceCaseCompletion(YSpecificationID specID, YIdentifier id, Document caseData) {
        _logger.debug("Announcing case '{}' complete.", id.toString());
        if (! _caseListeners.isEmpty()) {
            YCaseEvent event = new YCaseEvent(YEventType.CASE_COMPLETE, id, specID);
            event.setData(caseData);
            announceCaseEvent(event);
        }
     }


    /**
     * Called by a workitem when it has a change of status. Broadcast to all
     * registered services and gateways.
     * @param item the work item that has had a change of status
     * @param oldStatus the previous status of the work item
     */
    protected void announceWorkItemStatusChange(YWorkItem item, YWorkItemStatus oldStatus) {
        _logger.debug("Announcing workitem status change to new status '{}' " +
                "for workitem '{}'.", item.getStatus(), item.getWorkItemID().toString());
        if (! _workItemListeners.isEmpty()) {
            announceWorkItemEvent(new YWorkItemEvent(YEventType.ITEM_STATUS_CHANGE, item, oldStatus));
        }
    }


    /**
     * Called by a workitem when it is cancelled. Announced only to the designated
     * service or gateway.
     * @param item the work item that has had a change of status
     */
    public void announceCancelledWorkItem(YWorkItem item) {
        if (! _workItemListeners.isEmpty()) {
            announceWorkItemEvent(new YWorkItemEvent(YEventType.ITEM_CANCEL, item));
        }
    }


    // this method triggered by an IB service when it decides it is not going
    // to handle (i.e. checkout) a workitem announced to it. It passes the workitem to
    // the default worklist service for normal assignment.
    public void rejectAnnouncedEnabledTask(YWorkItem item) {

        // also raise an item abort exception for custom handling by services
        announceWorkItemAbort(item);
    }


    /******************************************************************************/
    // EXCEPTION ANNOUNCEMENTS //

    protected void announceCheckWorkItemConstraints(YWorkItem item, Document data,
                                                    boolean preCheck) {
        _logger.debug("Announcing Check Constraints for item {}", item.getIDString());
        if (! _exceptionListeners.isEmpty()) {
            YEventType eType = preCheck ? YEventType.ITEM_CHECK_PRECONSTRAINTS :
                    YEventType.ITEM_CHECK_POSTCONSTRAINTS;
            announceExceptionEvent(new YExceptionEvent(eType, item, data));
        }
    }


    protected void announceCheckCaseConstraints(YSpecificationID specID, YIdentifier id,
                                                Document dataDoc, boolean preCheck) {
        _logger.debug("Announcing Check Constraints for case {}", id);
        if (! _exceptionListeners.isEmpty()) {
            YEventType eType = preCheck ? YEventType.CASE_CHECK_PRECONSTRAINTS :
                    YEventType.CASE_CHECK_POSTCONSTRAINTS;
            announceExceptionEvent(new YExceptionEvent(eType, specID, id, dataDoc));
        }
     }


    private void announceWorkItemAbort(YWorkItem item) {
        if (! _exceptionListeners.isEmpty()) {
            announceExceptionEvent(new YExceptionEvent(YEventType.ITEM_ABORT, item));
        }
    }



    private void announceTimerExpiryToInterfaceXListeners(YWorkItem item) {
        if (! _exceptionListeners.isEmpty()) {
            announceExceptionEvent(new YExceptionEvent(YEventType.TIMER_EXPIRED, item));
        }
     }


    protected void announceDeadlock(YIdentifier id, Set<YTask> tasks) {
        if (! _caseListeners.isEmpty()) {
            announceCaseEvent(new YCaseEvent(YEventType.CASE_DEADLOCKED, id, tasks));
        }
    }


    /******************************************************************************/
    // LOG ANNOUNCEMENTS //

    public void logNetCompleted(YIdentifier caseIDForNet, YSpecificationID specID, YLogDataItemList dataList) {
        announceLogEvent(new YLogEvent(YEventType.NET_COMPLETED, caseIDForNet, specID, dataList));
    }

}
