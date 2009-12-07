/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.datastore.eventlog;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.WorkQueue;
import org.yawlfoundation.yawl.resourcing.datastore.persistence.Persister;

/**
 * Handles the logging of resource 'events'
 *
 * @author: Michael Adams
 * Date: 27/09/2007 
 */
public class EventLogger {

    private static boolean _logging ;
    private static boolean _logOffers ;

    public EventLogger() {}

    public static enum event { offer, allocate, start, suspend, deallocate, delegate,
                               reallocate_stateless, reallocate_stateful, skip, pile,
                               cancel, chain, complete, unoffer, unchain, unpile, resume }

    public static enum audit { logon, logoff, invalid, unknown, shutdown, expired,
                               gwlogon, gwlogoff, gwinvalid, gwunknown, gwexpired}


    public static void setLogging(boolean flag) {
        _logging = flag;
    }

    public static void setOfferLogging(boolean flag) { _logOffers = flag ; }



    public static void log(WorkItemRecord wir, String pid, event eType) {
        if (_logging) {
            ResourceEvent resEvent = new ResourceEvent(wir, pid, eType);
            Persister persister = Persister.getInstance() ;
            if (persister != null) persister.insert(resEvent);
        }
    }


    public static void log(WorkItemRecord wir, String pid, String eventString) {
        try {
            event eType = event.valueOf(eventString);
            log(wir, pid, eType);
        }
        catch (Exception e) {
            Logger.getLogger(EventLogger.class).error("'" + eventString +
                                                      "' is not a valid event type.");
        }
    }


    public static void log(WorkItemRecord wir, String pid, int eType) {
        switch (eType) {
            case WorkQueue.OFFERED   : if (_logOffers) log(wir, pid, event.offer); break;
            case WorkQueue.ALLOCATED : log(wir, pid, event.allocate); break;
            case WorkQueue.STARTED   : log(wir, pid, event.start); break;
            case WorkQueue.SUSPENDED : log(wir, pid, event.suspend); break;
            case WorkQueue.UNOFFERED : log(wir, "admin", event.unoffer);
        }
    }


    public static void audit(String userid, audit eType) {
        if (_logging) {
            AuditEvent auditEvent = new AuditEvent(userid, eType) ;
            Persister persister = Persister.getInstance() ;
            if (persister != null) persister.insert(auditEvent);
        }
    }




}
