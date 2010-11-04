/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.resourcing.datastore.eventlog;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.resourcing.WorkQueue;
import org.yawlfoundation.yawl.resourcing.datastore.persistence.Persister;

import java.util.Map;

/**
 * Handles the logging of resource 'events'
 *
 * @author Michael Adams
 * Date: 27/09/2007 
 */
public class EventLogger {

    private static boolean _logging ;
    private static boolean _logOffers ;
    private static Persister _persister = Persister.getInstance() ;
    private static Map<String, Object> _specMap;

    public static enum event { offer, allocate, start, suspend, deallocate, delegate,
        reallocate_stateless, reallocate_stateful, skip, pile, cancel, chain, complete,
        unoffer, unchain, unpile, resume, timer_expired, launch_case, cancel_case,
        cancelled_by_case, in_use, released }

    public static enum audit { logon, logoff, invalid, unknown, shutdown, expired,
        gwlogon, gwlogoff, gwinvalid, gwunknown, gwexpired }


    public EventLogger() { }


    public static void setLogging(boolean flag) {
        _logging = flag;
    }

    public static void setOfferLogging(boolean flag) { _logOffers = flag ; }



    public static void log(WorkItemRecord wir, String pid, event eType) {
        if (_logging) {
            long specKey = getSpecificationKey(wir);
            ResourceEvent resEvent = new ResourceEvent(specKey, wir, pid, eType);
            insertEvent(resEvent);
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


    public static void log(YSpecificationID specID, String caseID, String id, event eType) {
        if (_logging) {
            long specKey = getSpecificationKey(specID);
            ResourceEvent resEvent = new ResourceEvent(specKey, caseID, id, eType);
            insertEvent(resEvent);
        }
    }


    public static void log(WorkItemRecord wir, String pid, int eType) {
        switch (eType) {
            case WorkQueue.OFFERED   : if (_logOffers) log(wir, pid, event.offer); break;
            case WorkQueue.ALLOCATED : log(wir, pid, event.allocate); break;
            case WorkQueue.STARTED   : log(wir, pid, event.start); break;
            case WorkQueue.SUSPENDED : log(wir, pid, event.suspend); break;
            case WorkQueue.UNOFFERED : log(wir, "system", event.unoffer);
        }
    }


    public static void log(YSpecificationID specID, String caseID, String pid, boolean launch) {
        if (_logging) {
            long specKey = getSpecificationKey(specID);
            event eType = launch ? event.launch_case : event.cancel_case;
            ResourceEvent resEvent = new ResourceEvent(specKey, caseID, pid, eType);
            insertEvent(resEvent);
        }
    }


    public static void audit(String userid, audit eType) {
        if (_logging) {
            AuditEvent auditEvent = new AuditEvent(userid, eType) ;
            insertEvent(auditEvent);
        }
    }


    private static void insertEvent(Object event) {
        if (_persister != null) _persister.insert(event);
    }


    /**
     * Gets the primary key for a specification record, or inserts a new entry if it
     * doesn't exist and returns its key.
     * @param ySpecID the identifiers of the specification
     * @return the primary key for the specification
     * @throws YPersistenceException if there's a problem with the persistence layer
     */
    public static long getSpecificationKey(YSpecificationID ySpecID) {
        long result = -1;
        if (ySpecID == null) return -1;
        SpecLog specEntry = null;
        String key = ySpecID.getKey() + ySpecID.getVersionAsString();

        if (_persister != null) {
            if (_specMap == null) {
                _specMap = _persister.selectMap("SpecLog");
            }
            if (! _specMap.isEmpty()) {
                specEntry = (SpecLog) _specMap.get(key);
                if (specEntry != null) {
                    result = specEntry.getLogID();
                }
            }
            if (specEntry == null) {
                specEntry = new SpecLog(ySpecID);
                _persister.insert(specEntry);
                _specMap.put(key, specEntry);
                result = specEntry.getLogID();               
            }
        }
        return result;
    }

    public static long getSpecificationKey(WorkItemRecord wir) {
        return getSpecificationKey(new YSpecificationID(wir));
    }


    public static event getEventByName(String eventName) {
        return (eventName != null) ? getEnum(event.class, eventName) : null;
    }


    public static audit getAuditEventByName(String eventName) {
        return (eventName != null) ? getEnum(audit.class, eventName) : null;
    }


    private static <T extends Enum<T>> T getEnum(Class<T> e, String eventName) {
        if (eventName == null) return null;
        try {
            return Enum.valueOf(e, eventName);
        }
        catch (IllegalArgumentException iae) {
            return null;
        }
    }


}
