/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

import org.apache.logging.log4j.LogManager;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.WorkQueue;
import org.yawlfoundation.yawl.resourcing.datastore.persistence.Persister;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceGatewayServer;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Handles the logging of resource 'events'
 *
 * @author Michael Adams
 * Date: 27/09/2007 
 */
public class EventLogger {

    private static boolean _loggingEnabled = false;
    private static boolean _logOffers = false;
    private static ResourceGatewayServer _eventServer;
    private static Map<String, Object> _specMap;
    private static Set<ResourceEventListener> _listeners;

    private static final ExecutorService _executor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors());


    public static enum event { offer, allocate, start, suspend, deallocate, delegate,
        reallocate_stateless, reallocate_stateful, skip, pile, cancel, chain, complete,
        unoffer, unchain, unpile, resume, timer_expired, launch_case, cancel_case,
        cancelled_by_case, busy, released, autotask_start, autotask_complete }

    public static enum audit { logon, logoff, invalid, unknown, shutdown, expired,
        gwlogon, gwlogoff, gwinvalid, gwunknown, gwexpired }


    public EventLogger() { }


    public static void setLogging(boolean flag) { _loggingEnabled = flag; }

    public static void setOfferLogging(boolean flag) { _logOffers = flag ; }

    public static void setEventServer(ResourceGatewayServer server) {
        _eventServer = server;
    }


    public static void addListener(ResourceEventListener listener) {
        if (_listeners == null) _listeners = new HashSet<ResourceEventListener>();
        _listeners.add(listener);
    }

    public static void removeListener(ResourceEventListener listener) {
        _listeners.remove(listener);
    }
    

    public static List<Runnable> shutdown() {
        List<Runnable> x = _executor.shutdownNow();
        try {
            _executor.awaitTermination(1, TimeUnit.SECONDS);
        }
        catch (InterruptedException ie) {
            // we're done
        }
        return x;
    }


    public static void log(WorkItemRecord wir, String pid, event eType) {
        if (_loggingEnabled) {
            insertEvent(getSpecificationKey(wir), wir, pid, eType);
        }
    }


    public static void logAutoTask(WorkItemRecord wir, boolean start) {
        event eType = start ? event.autotask_start : event.autotask_complete;
        log(wir, null, eType);
    }


    public static void log(WorkItemRecord wir, String pid, String eventString) {
        try {
            event eType = event.valueOf(eventString);
            log(wir, pid, eType);
        }
        catch (Exception e) {
            LogManager.getLogger(EventLogger.class).error("'" + eventString +
                    "' is not a valid event type.");
        }
    }


    public static void log(YSpecificationID specID, String caseID, String id, event eType) {
        if (_loggingEnabled) {
            insertEvent(getSpecificationKey(specID), caseID, id, eType);
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
        event eType = launch ? event.launch_case : event.cancel_case;
        log(specID, caseID, pid, eType);
    }


    public static void audit(String userid, audit eType) {
        if (_loggingEnabled) {
            AuditEvent auditEvent = new AuditEvent(userid, eType) ;
            insertEvent(auditEvent);
        }
    }


    private static void insertEvent(long specKey, String caseID, String pid, event eType) {
        ResourceEvent resEvent = new ResourceEvent(specKey, caseID, pid, eType);
        insertEvent(resEvent);
        announceEvent(resEvent);
    }


    private static void insertEvent(long specKey, WorkItemRecord wir, String pid, event eType) {
        ResourceEvent resEvent = new ResourceEvent(specKey, wir, pid, eType);
        insertEvent(resEvent);
        announceEvent(resEvent);
    }


    private static void insertEvent(final Object event) {
        _executor.execute(new Runnable() {
            public void run() {
                Persister.getInstance().insert(event);
            }
        });
    }


    private static void announceEvent(ResourceEvent event) {
        SpecLog specLog = (SpecLog) Persister.getInstance().get(
                SpecLog.class, event.get_specKey());
        YSpecificationID specID = specLog != null ? specLog.getSpecID() : null;
        if (_eventServer != null) {
            _eventServer.announceResourceEvent(event);
        }
        if (_listeners != null) {
            for (ResourceEventListener listener : _listeners) {
                listener.eventOccurred(specID, event);
            }
        }
    }


    /**
     * Gets the primary key for a specification record, or inserts a new entry if it
     * doesn't exist and returns its key.
     * @param ySpecID the identifiers of the specification
     * @return the primary key for the specification
     */
    public static long getSpecificationKey(YSpecificationID ySpecID) {
        if (ySpecID == null) return -1;
        if (_specMap == null) _specMap = Persister.getInstance().selectMap("SpecLog");
        String key = ySpecID.getKey() + ySpecID.getVersionAsString();
        long result = getSpecificationKey(key);
        if (result < 0) {
            SpecLog specEntry = new SpecLog(ySpecID);
            Persister.getInstance().insert(specEntry);
            _specMap.put(key, specEntry);
            result = specEntry.getLogID();
        }
        return result;
    }

    private static long getSpecificationKey(WorkItemRecord wir) {
        return getSpecificationKey(new YSpecificationID(wir));
    }

    // pre: specMap != null
    private static long getSpecificationKey(String key) {
        SpecLog specEntry = (SpecLog) _specMap.get(key);
        return (specEntry != null) ? specEntry.getLogID() : -1;
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
