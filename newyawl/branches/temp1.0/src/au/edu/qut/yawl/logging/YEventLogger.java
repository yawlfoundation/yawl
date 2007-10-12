/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.logging;

import au.edu.qut.yawl.engine.*;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import net.sf.hibernate.Query;
import net.sf.hibernate.HibernateException;

import java.util.*;

import org.apache.log4j.Logger;

/**
 * Handles all logging of case, workitem and workitem data events to the process logs.
 *
 * Completely refactored for v1.0 by Michael Adams
 * 09/10/2007
 */

public class YEventLogger {

    // caches primary key ids of case start events for use as workitemevent's FKs
    private HashMap<String,String> _caseEventIDMap = new HashMap<String,String>() ;

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

    // WORKITEM EVENT //

    public String logWorkItemEvent(YPersistenceManager pmgr, YWorkItem workItem,
                                   String eventName, String resourceID)
                                                       throws YPersistenceException {
        
        return this.logWorkItemEvent(pmgr, workItem.getCaseID().toString(),
                                     workItem.getTaskID(),
                                     eventName, resourceID, null);
    }


    public String logWorkItemEvent(YPersistenceManager pmgr, String caseID,
                                   String taskID, String eventName,
                                   String resourceID) throws YPersistenceException {

        return this.logWorkItemEvent(pmgr, caseID, taskID, eventName,
                                     resourceID, null);
    }

    /**
     * Logs one workitem event (change of status)
     *
     * @param pmgr a reference to the persistence writer class
     * @param caseID the case the workitem is a member of
     * @param taskID the name of the task that this workitem is an 'instance' of
     * @param eventName the event that has occurred
     * @param resourceID the userid of the user that 'caused' the event
     * @param time the timestamp of when the event occurred
     * @return the primary key value of this event record
     * @throws YPersistenceException if row cannot be created in the log
     */
    public String logWorkItemEvent(YPersistenceManager pmgr, String caseID,
                                   String taskID, String eventName,
                                   String resourceID, String time)
                                                       throws YPersistenceException {

        if ((pmgr == null) || (! enabled)) return "";

        // get foreign key value for the case event associated with this workitem
        String caseEventID = getCaseEventID(pmgr, caseID);
        if (caseEventID == null) return "" ;               // couldn't get foreign key

        // change 'IsParent' to something more meaningful for the log
        if (eventName.equals(YWorkItem.statusIsParent)) eventName = "Decompose" ;

        // create new primary key value for this event row
        String eventID = createPK();

        // set timestamp if not supplied
        long eventTime = (time != null) ? Long.parseLong(time) : now();

        // persist the event row
        pmgr.storeObjectFromExternal(new YWorkItemEvent(eventID, caseEventID, caseID,
                                            taskID, resourceID, eventName, eventTime));
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


    /**
     * Retrieves the corresponding primary key id of the case event row for a case id,
     * for use as a foreign key when writing workitem events
     *
     * @param pmgr a reference to the persistence writer class
     * @param caseID the case id to get the PK for
     * @return the case event id for the caseid passed
     */
    private String getCaseEventID(YPersistenceManager pmgr, String caseID) {

        // strip off minor part of id (i.e. truncate decimals)
        if (caseID.indexOf('.') > -1) caseID = caseID.substring(0, caseID.indexOf('.'));

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
                        if (startEvent != null) result = startEvent.get_caseEventID();
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

}
