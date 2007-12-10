/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.logging;

import org.yawlfoundation.yawl.engine.*;
import static org.yawlfoundation.yawl.engine.YWorkItemStatus.*;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.hibernate.Query;
import org.hibernate.HibernateException;

import java.util.*;

import org.apache.log4j.Logger;

/**
 * Handles all logging of case, workitem and workitem data events to the process logs.
 *
 * Completely refactored for v1.0 by Michael Adams
 * 09/10/2007
 */

public class YEventLogger {

    // caches primary key ids of case start events for use as ParentWorkItemEvent's FKs
    // [caseID, caseEventID]
    private HashMap<String,String> _caseEventIDMap = new HashMap<String,String>() ;

    // caches primary key ids of parent decompose events for ChildWorkItemEvent's FKs
    // [caseID, taskID, parentEventID]
    private HashMap<String,HashMap<String,String>> _parentEventIDMap =
                                       new HashMap<String,HashMap<String,String>>() ;

    // caches cancelled case ids to prevent double logging (as cancelled & completed)
    private HashSet<String> _cancelledCaseSet = new HashSet<String>();

    private Logger _log = Logger.getLogger(YEventLogger.class);
    private boolean enabled = true;
    private static YEventLogger _me = null;


    /** @return an instantiated event logger*/
    public static YEventLogger getInstance() {
        if (_me == null) _me = new YEventLogger();
        return _me;
    }

    /* enables event logging (the default) */
    public void enable() { enabled = true; }

    /* disables event logging */
    public void disable() {enabled = false; }


    /*******************************************************************************/

    // CASE EVENT //

    /**
     * Logs one case event (i.e. start, complete, cancel)
     *
     * @param pmgr a reference to the persistence writer class
     * @param caseID the id of this case
     * @param resourceID the userid of the user started the case (used only for starting)
     * @param eventType one of 'started', 'cancelled' or 'completed'
     * @param specID the case's specification id
     * @throws YPersistenceException if row cannot be created in the log
     */
    public void logCaseEvent(YPersistenceManager pmgr, String caseID, String resourceID,
                          String eventType, String specID) throws YPersistenceException {

        if ((pmgr != null) && enabled) {

             // get new primary key value for this event row
            String eventID = createPK();

            // if starting, cache PK for use with workitem events
            if (eventType.equals(YCaseEvent.START))
                _caseEventIDMap.put(caseID, eventID);
            else
                _caseEventIDMap.remove(caseID);         // done with this one

            // persist the event row
            YCaseEvent caseEvent = new YCaseEvent(eventID, caseID, now(), eventType,
                                                  resourceID, specID, null);
            pmgr.storeObjectFromExternal(caseEvent);
        }
    }


    public void logCaseCreated(YPersistenceManager pmgr, String caseID, String resourceID,
                               String specID) throws YPersistenceException {
        logCaseEvent(pmgr, caseID, resourceID, YCaseEvent.START, specID) ;
    }


    public void logCaseCancelled(YPersistenceManager pmgr, String caseID)
                                                     throws YPersistenceException {
        logCaseEvent(pmgr, caseID, null, YCaseEvent.CANCEL, null) ;
        _cancelledCaseSet.add(caseID) ;                     // remember cancellation
    }


    public void logCaseCompleted(YPersistenceManager pmgr, String caseID)
                                                     throws YPersistenceException {

        // prevent double logging of cancellation and completion
        if (! _cancelledCaseSet.contains(caseID))
            logCaseEvent(pmgr, caseID, null, YCaseEvent.COMPLETE, null);
        else
            _cancelledCaseSet.remove(caseID) ;               // done with this one
    }


    /*******************************************************************************/

    // PARENT WORKITEM EVENT //

    /**
     * Logs one parent workitem event (change of status)
     *
     * @param pmgr a reference to the persistence writer class
     * @param workItem the workitem that triggered the event
     * @param eventName the event that has occurred
     * @param resourceID the userid of the user that 'caused' the event
     * @throws YPersistenceException if row cannot be created in the log
     */
    public void logParentWorkItemEvent(YPersistenceManager pmgr, YWorkItem workItem,
                                       YWorkItemStatus eventName, String resourceID)
                                                       throws YPersistenceException {
        if ((pmgr != null) && (enabled)) {
            String caseID = workItem.getCaseID().toString() ;
            String taskID = workItem.getTaskID();
            String eventStr ;

            // get foreign key value for the case event associated with this workitem
            String caseEventID = getCaseEventID(pmgr, caseID);
            if (caseEventID == null) {
                _log.error("Could not get case event foreign key to log event");
                return ;                                   // couldn't get foreign key
            }

            // create new primary key value for this event row
            String eventID = createPK();

            // change 'IsParent' to something more meaningful for the log
            if (eventName.equals(statusIsParent)) {
                eventStr = "Decompose" ;
                cacheParentEventID(caseID, taskID, eventID) ;   // cache the key mapping
            }
            else
                eventStr = eventName.toString();

            // remove key mapping from cache on completion
            System.out.println("logparent, caseid = " + caseID + ", taskID = " + taskID + ",eventname = " + eventName.toString() + ", eventID = " + eventID);
            if (workItem.hasFinishedStatus()) _parentEventIDMap.remove(caseID) ;

            // persist the event row
            pmgr.storeObjectFromExternal(new YParentWorkItemEvent(eventID, caseEventID,
                                         caseID, taskID, resourceID, eventStr, now()));
        }
    }


    /*******************************************************************************/

    // CHILD WORKITEM EVENT //

    public String logWorkItemEvent(YPersistenceManager pmgr, YWorkItem workItem,
                                   YWorkItemStatus eventName, String resourceID)
                                                       throws YPersistenceException {
        
        return this.logWorkItemEvent(pmgr, workItem.getCaseID().toString(),
                                     workItem.getTaskID(), eventName, resourceID, null);
    }


    /**
     * Logs one workitem event (change of status)
     *
     * @param pmgr a reference to the persistence writer class
     * @param caseID the case the workitem is a member of
     * @param taskID the task the workitem is an instance of
     * @param eventName the event that has occurred
     * @param resourceID the userid of the user that 'caused' the event
     * @param time the timestamp of when the event occurred
     * @return the primary key value of this event record
     * @throws YPersistenceException if row cannot be created in the log
     */
    public String logWorkItemEvent(YPersistenceManager pmgr, String caseID, String taskID,
                                   YWorkItemStatus eventName, String resourceID,
                                   String time) throws YPersistenceException {

        if ((pmgr == null) || (! enabled)) return "";

        // get foreign key value for the parent event associated with this workitem
        String parentEventID = getParentEventID(pmgr, caseID, taskID);
        if (parentEventID == null) return "" ;               // couldn't get foreign key

        // create new primary key value for this event row
        String eventID = createPK();

        // set timestamp if not supplied
        long eventTime = (time != null) ? Long.parseLong(time) : now();

        // persist the event row
        pmgr.storeObjectFromExternal(new YChildWorkItemEvent(eventID, parentEventID,
                                   caseID, resourceID, eventName.toString(), eventTime));
        return eventID ;
    }


    /*****************************************************************************/

    // WORKITEM DATA EVENT //

    /**
     * Records the input value of each workitem data parameter when the workitem starts
     * (i.e. moves to 'executing') and the output value of each when it completes.
     *
     * @param pmgr a reference to the persistence writer class
     * @param name the name of the data parameter
     * @param data its value
     * @param workItemEventID the FK value for the associated workitem event
     * @param io has value 'i' for input, 'o' for output
     * @return true if successful
     * @throws YPersistenceException if row cannot be created in the log
     */
    public boolean logData(YPersistenceManager pmgr, String name, String data,
                       String workItemEventID, char io) throws YPersistenceException {

        boolean result = false ;

        if ((pmgr != null) && enabled) {
            pmgr.storeObjectFromExternal(
                    new YWorkItemDataEvent(createPK(), workItemEventID, name, data, io));
            result = true;
        }
        return result ;
    }


    /*******************************************************************************/

    //PRIVATE METHODS //

    /** @return a random UUID string to serve as a unique primary key */
    private String createPK() { return UUID.randomUUID().toString(); }


    /** @return the current system time */
    private long now() { return System.currentTimeMillis(); }

    /** adds the parent eventid to a cache for use as primary key for child events */
    private void cacheParentEventID(String caseID, String taskID, String eventID) {
        HashMap<String, String> taskMap = new HashMap<String,String>();
        taskMap.put(taskID, eventID);
        _parentEventIDMap.put(caseID, taskMap) ;
    }

    /** @return the parent eventid for the case and task ids passed */
    private String getParentEventIDFromCache(String caseID, String taskID) {
        String result = null ;
        HashMap<String,String> taskMap = _parentEventIDMap.get(caseID);
        if (taskMap != null) result = taskMap.get(taskID);
        return result ;
    }

    /**
     * Retrieves the corresponding primary key id of the case event row for a case id,
     * for use as a foreign key when writing workitem events
     *
     * @param pmgr a reference to the persistence writer class
     * @param caseID the case id to get the PK for
     * @return the case event id for the caseid passed
     */
    private String getCaseEventID(YPersistenceManager pmgr, String caseID) {

        // try the case id map first
        String result = _caseEventIDMap.get(caseID);

        // if it's not in the map, try getting it from the event logs
        if (result == null) {
            if (pmgr != null) {
                try {
                    String qStr = String.format("from YCaseEvent as yce where " +
                            "yce._caseID = '%s' and yce._eventType = 'started'", caseID);

                    Query qry = pmgr.createQuery(qStr) ;
                    if ((qry != null) && (! qry.list().isEmpty())) {
                        YCaseEvent startEvent = (YCaseEvent) qry.iterate().next();
                        if (startEvent != null) {
                            result = startEvent.get_caseEventID();
                            _caseEventIDMap.put(caseID, result) ;
                        }
                    }
                }

                // on exception report only, result will be null
                catch (YPersistenceException ype) {
                    _log.error("Persistence Exception trying to retrieve case event id",
                                ype);
                }
                catch (HibernateException he) {
                    _log.error("Hibernate Exception trying to retrieve case event id", he);
                }
            }
        }
        return result ;
    }


    /**
     * Retrieves the corresponding primary key id of the parent event row for a case id,
     * for use as a foreign key when writing workitem events
     *
     * @param pmgr a reference to the persistence writer class
     * @param caseID the case id to get the PK for
     * @param taskID the task id to get the PK for
     * @return the parent workitem event id for the caseID & taskID passed
     */
    private String getParentEventID(YPersistenceManager pmgr, String caseID,
                                    String taskID) {
        System.out.println("getparentid, caseid = " + caseID + ", taskId = " + taskID);

        // strip off minor part of id (i.e. truncate decimals)
        if (caseID.indexOf('.') > -1) caseID = caseID.substring(0, caseID.indexOf('.'));

        System.out.println("getparentid after strip, caseid = " + caseID);

        // try the case id map first
        String result = getParentEventIDFromCache(caseID, taskID) ;

        System.out.println("getparentid, result = " + result);

        // if it's not in the map, try getting it from the event logs
        if (result == null) {
            if (pmgr != null) {
                try {
                    String qStr = String.format("from YParentWorkItemEvent as pe where " +
                            "pe._caseID = '%s' and pe._taskID = '%s' and " +
                            "pe._eventName = 'Enabled'", caseID, taskID);

                    Query qry = pmgr.createQuery(qStr) ;
                    if ((qry != null) && (! qry.list().isEmpty())) {
                        YParentWorkItemEvent enabledEvent =
                                       (YParentWorkItemEvent) qry.iterate().next();
                        if (enabledEvent != null) {
                            result = enabledEvent.get_parentWorkItemEventID();
                            cacheParentEventID(caseID, taskID, result);
                        }
                    }
                }

                // on exception report only, result will be null
                catch (YPersistenceException ype) {
                    _log.error("Persistence Exception trying to retrieve parent event id",
                                ype);
                }
                catch (HibernateException he) {
                    _log.error("Hibernate Exception trying to retrieve parent event id",
                                he);
                }
            }
        }
        return result ;
    }

}
