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

package org.yawlfoundation.yawl.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.yawlfoundation.yawl.authentication.YClient;
import org.yawlfoundation.yawl.authentication.YSession;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.*;
import org.yawlfoundation.yawl.logging.table.*;
import org.yawlfoundation.yawl.schema.XSDType;
import org.yawlfoundation.yawl.schema.internal.YInternalType;
import org.yawlfoundation.yawl.util.HibernateEngine;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.yawlfoundation.yawl.engine.YWorkItemStatus.statusIsParent;

/**
 * Handles all process logging of case, subnet, workitem and workitem data events.
 *
 * Log objects are instantiated and then written as a row to the appropriate
 * table via hibernate. Some tables store 'definition' information, others instantiation
 * information. All table rows are identified with auto-generated primary keys (not
 * mentioned in the summaries below). The log objects are:
 *  - YLogSpecification: spec id, version, name, root net key
 *  - YLogNet: name, FK to YLogSpecification
 *  - YLogNetInstance: case id, FK to YLogNet, FK to YLogTaskInstance (only for sub-nets)
 *  - YLogTask: name, FK to YLogNet (parent), FK to YLogNet (child - only for sub-nets)
 *  - YLogTaskInstance: case id, FK to YLogTask, FK to YLogNetInstance,
 *         FK to YLogTaskInstance (parent - only for child workitems)
 *  - YEvent: descriptor, timestamp, FK to instance (YLogNetInstance OR YLogTaskInstance),
 *         FK to YLogService, FK to YLogNetInstance (root net)
 *  - YLogService: name, url
 *  - YLogDataItemInstance: descriptor, attribute, value, FK to YEvent, FK to YLogDataType
 *  - YLogDataType: definition
 *
 * @author Michael Adams - completely refactored for v2.0 10/2007,
 * and again for v2.1 04-12/2009, and again for v2.2 11/2010
 *
 */

public class YEventLogger {

    // constants for the case and net-level events logged
    public final static String CASE_START = "CaseStart" ;
    public final static String CASE_COMPLETE = "CaseComplete";
    public final static String CASE_CANCEL = "CaseCancel";
    public final static String NET_UNFOLD = "NetUnfold";
    public final static String NET_START = "NetStart" ;
    public final static String NET_COMPLETE = "NetComplete";
    public final static String NET_CANCEL = "NetCancel";

    private final Logger _log = LogManager.getLogger(YEventLogger.class);
    private boolean _enabled = true;
    private static YEventLogger INSTANCE = null;
    private YEngine _engine;
    private HibernateEngine _db;

    private static final Object TASK_INST_MUTEX = new Object();
    private final YEventKeyCache _keyCache = new YEventKeyCache();

    private static final Class[] LOG_CLASSES = {
            YLogSpecification.class, YLogNet.class, YLogTask.class,
            YLogNetInstance.class, YLogTaskInstance.class, YLogEvent.class,
            YLogDataItemInstance.class, YLogDataType.class, YLogService.class
    };

    private static final ExecutorService _executor =
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    // PUBLIC INTERFACE METHODS //

    /**
     * Get a reference to the event logger object. This one is called by the engine on
     * startup.
     * @param engine a reference to the running engine
     * @return an instantiated event logger
     */
    public static YEventLogger getInstance(YEngine engine) {
        if (INSTANCE == null) INSTANCE = new YEventLogger();
        INSTANCE._engine = engine;
        if (! YEngine.isPersisting()) {
            INSTANCE.disable();
            INSTANCE._log.warn("Process logging disabled because Engine persistence is disabled.");
        }
        return INSTANCE;
    }


    /** @return an instantiated event logger */
    public static YEventLogger getInstance() {
        if (INSTANCE == null) INSTANCE = new YEventLogger();
        return INSTANCE;
    }


    /* enables event logging (the default) */
    public void enable() { _enabled = true; }

    /* disables event logging */
    public void disable() { _enabled = false; }


    public boolean isEnabled() { return _enabled; }


    public String getDataSchema(YSpecificationID specID, String dataTypeName) {
        if (XSDType.isBuiltInType(dataTypeName)) {
            return dataTypeName;                        // most likely scenario
        }
        else if (YInternalType.isType(dataTypeName)) {
            return YInternalType.valueOf(dataTypeName).getSchemaString();
        }

        // user-defined type
        String schema = _keyCache.dataSchema.getSchemaTypeAsString(specID, dataTypeName);
        return schema != null ? schema : dataTypeName;
    }

    public void removeSpecificationFromCache(YSpecificationID specID) {
        _keyCache.removeSpecification(specID);
        _keyCache.dataSchema.remove(specID);
    }


    //********************************************************************************//

    /**
     * Logs the launching of a new process instance.
     * @param ySpecID the id of the process specification
     * @param caseID the id of the case
     * @param datalist a list of data entries to log with this event
     * @param serviceRef a reference to the client service that launched the case
     */
    public void logCaseCreated(final YSpecificationID ySpecID,
                               final YIdentifier caseID, final YLogDataItemList datalist,
                               final String serviceRef) {
        if (loggingEnabled()) {
            _executor.execute(new Runnable() {
                @Override
                public void run() {
                    long netInstanceID = YEventLogger.this.insertNetInstance(caseID,
                            YEventLogger.this.getRootNetID(ySpecID), -1);
                    long serviceID = YEventLogger.this.getServiceID(serviceRef);
                    YEventLogger.this.logEvent(netInstanceID, CASE_START, datalist, serviceID, netInstanceID);
                    _keyCache.netInstances.put(caseID, netInstanceID);
                }
            });
        }
    }


    /**
     * Logs the launching of a sub-net (ie. the enablement of a composite task).
     * @param ySpecID the id of the process specification
     * @param runner the net runner launching this sub-net
     * @param engineTaskID the task id assigned by the engine
     * @param datalist a list of data entries to log with this event
     */
    public void logSubNetCreated(final YSpecificationID ySpecID,
                                 final YNetRunner runner, final String engineTaskID, final YLogDataItemList datalist) {
        if (loggingEnabled()) {
            _executor.execute(new Runnable() {
                @Override
                public void run() {
                    // get the required foreign key values
                    YIdentifier subnetID = runner.getCaseID();
                    long netID = YEventLogger.this.getNetID(ySpecID, runner.getNet().getID());
                    long taskID = YEventLogger.this.getTaskID(ySpecID, engineTaskID, netID);
                    long rootNetInstanceID = YEventLogger.this.getRootNetInstanceID(subnetID);

                    // log the composite task enablement first
                    long parentTaskInstanceID = YEventLogger.this.getTaskInstanceID(subnetID, taskID);
                    if (parentTaskInstanceID < 0) {
                        parentTaskInstanceID = YEventLogger.this.insertTaskInstance(subnetID.toString(),
                                taskID, -1, YEventLogger.this.getNetInstanceID(subnetID.getParent()));
                    }
                    YEventLogger.this.logEvent(parentTaskInstanceID, NET_UNFOLD, null, -1, rootNetInstanceID);

                    // now log the subnet launch
                    long netInstanceID = YEventLogger.this.insertNetInstance(subnetID, netID,
                            parentTaskInstanceID);
                    YEventLogger.this.logEvent(netInstanceID, NET_START, datalist, -1, rootNetInstanceID);
                }
            });
        }
    }


    /**
     * Logs a case cancellation.
     * @param caseID the id of the case
     * @param datalist a list of data entries to log with this event
     * @param serviceRef a reference to the client service that launched the case
     */
    public void logCaseCancelled(final YIdentifier caseID,
                                 final YLogDataItemList datalist, final String serviceRef) {
        if (loggingEnabled()) {
            _executor.execute(new Runnable() {
                @Override
                public void run() {
                    long netInstanceID = YEventLogger.this.getNetInstanceID(caseID);
                    long serviceID = YEventLogger.this.getServiceID(serviceRef);
                    YEventLogger.this.logEvent(netInstanceID, CASE_CANCEL, datalist, serviceID,
                            YEventLogger.this.getRootNetInstanceID(caseID));
                    _keyCache.removeCase(caseID);
                }
            });
        }
    }


    /**
     * Logs the normal completion of a net. If the net is a root net, it means the
     * case has completed.
     * @param engineNetID the id of the net
     * @param datalist a list of data entries to log with this event
     */
    public void logNetCompleted(final YIdentifier engineNetID,
                                final YLogDataItemList datalist) {
        if (loggingEnabled()) {
            _executor.execute(new Runnable() {
                @Override
                public void run() {
                    String event;
                    long rootNetInstanceID = YEventLogger.this.getRootNetInstanceID(engineNetID);
                    long netInstanceID = YEventLogger.this.getNetInstanceID(engineNetID);

                    // a root net has no parent
                    if (engineNetID.getParent() != null) {
                        event = NET_COMPLETE;
                    }
                    else {
                        event = CASE_COMPLETE;
                        _keyCache.removeCase(engineNetID);
                    }
                    YEventLogger.this.logEvent(netInstanceID, event, datalist, -1, rootNetInstanceID);
                }
            });
        }
    }


     public void logNetCancelled(final YSpecificationID ySpecID, final YNetRunner runner,
                                 final String engineTaskID, final YLogDataItemList datalist) {
        if (loggingEnabled()) {
            _executor.execute(new Runnable() {
                @Override
                public void run() {
                    // get the required foreign key values
                    YIdentifier subnetID = runner.getCaseID();
                    long netID = YEventLogger.this.getNetID(ySpecID, runner.getNet().getID());
                    long taskID = YEventLogger.this.getTaskID(ySpecID, engineTaskID, netID);
                    long rootNetInstanceID = YEventLogger.this.getRootNetInstanceID(subnetID);

                    // log the composite task cancellation
                    long parentTaskInstanceID = YEventLogger.this.getTaskInstanceID(subnetID, taskID);
                    YEventLogger.this.logEvent(parentTaskInstanceID, NET_CANCEL, datalist, -1,
                            rootNetInstanceID);
                }
            });
        }
    }


    /**
     * Logs a workitem event (change of status).
     * @param workItem the workitem that triggered the event
     * @param eventName the event that has occurred
     * @param datalist a list of data entries to log with this event
     */
    public void logWorkItemEvent(final YWorkItem workItem, final String eventName,
                                 final YLogDataItemList datalist) {
        if (loggingEnabled()) {
            _executor.execute(() -> {
                long taskInstanceID = YEventLogger.this.getOrCreateTaskInstanceID(workItem);
                YEventLogger.this.logEvent(taskInstanceID, eventName, datalist,
                        YEventLogger.this.getServiceID(workItem),
                        YEventLogger.this.getRootNetInstanceID(workItem.getCaseID()));
            });
        }
    }


    /**
     * Logs a workitem event (change of status).
     * @param workItem the workitem that triggered the event
     * @param event the event that has occurred
     * @param datalist a list of data entries to log with this event
     */
    public void logWorkItemEvent(final YWorkItem workItem,
                                 final YWorkItemStatus event, final YLogDataItemList datalist) {
//        _executor.execute(new Runnable() {
//            @Override
//            public void run() {
                String eventName = event.equals(statusIsParent) ? "Decompose" : event.toString();
                logWorkItemEvent(workItem, eventName, datalist);
//            }
//        });
    }


    /**
     * Logs data variables and values when mapped between net and workitem
     * @param workitem the workitem starting or completing
     * @param descriptor a label for the kind of data it is
     * @param datalist a list of data entries to log with this event
     */
    public void logDataEvent(final YWorkItem workitem, final String descriptor,
                             final YLogDataItemList datalist) {
        if (loggingEnabled() && (datalist.size() > 0)) {
            _executor.execute(new Runnable() {
                @Override
                public void run() {
                    long instanceID = YEventLogger.this.getOrCreateTaskInstanceID(workitem);
                    YEventLogger.this.populateDataListSchemas(workitem.getSpecificationID(), datalist);
                    YEventLogger.this.logEvent(instanceID, descriptor, datalist, -1,
                            YEventLogger.this.getRootNetInstanceID(workitem.getCaseID()));
                }
            });
        }
    }


    /**
     * Gets the last allocated case number from the logs (called as secondary
     * source on engine startup)
     * @return the last allocated case number
     */
    public int getMaxCaseNbr() {
        Query query = getDb().createQuery(
                "select max(engineInstanceID) from YLogNetInstance");
        if (query != null && !query.list().isEmpty()) {
            String engineID = (String) query.iterate().next();
            try {
                // only want integral case numbers
                return new Double(engineID).intValue();
            }
            catch (Exception e) {
                // ignore - fallthrough
            }
        }
        return 0;    // will increment to one on first case start
    }


    //*****************************************************************************//

    //PRIVATE (IMPLEMENTATION) METHODS //

    /** @return the current system time */
    private long now() { return System.currentTimeMillis(); }


    private boolean loggingEnabled() {
        return _enabled;
    }

    private String getWarnMsg(String value) {
        return String.format("WARNING: Exception writing %s to the process logs.", value);
    }


    /**
     * Gets the row of the YLogSpecification table matching the spec data passed
     * @param ySpecID the identifiers of the specification
     * @return the matching table row as a YLogSpecification object, or null if no match
     */
    private YLogSpecification getSpecificationEntry(YSpecificationID ySpecID) {

        // pre-2.0 specs don't have an identifier field
        String field = (ySpecID.getIdentifier() != null) ? "identifier" : "uri";

        YLogSpecification specEntry = _keyCache.specEntries.get(ySpecID);
        if (specEntry == null) {
            String where = String.format("%s='%s' AND tbl.version='%s'", field,
                    ySpecID.getKey(), ySpecID.getVersionAsString());
            specEntry = (YLogSpecification) selectScalarWhere(
                    "YLogSpecification", where);
            if (specEntry != null) _keyCache.specEntries.put(ySpecID, specEntry);
        }
        return specEntry;
    }


    /**
     * Gets the primary key for a specification record.
     * @param ySpecID the identifiers of the specification
     * @return the primary key for the specification
     */
    private long getSpecificationKey(YSpecificationID ySpecID) {
        YLogSpecification specEntry = getSpecificationEntry(ySpecID);
        return (specEntry != null) ? specEntry.getRowKey() : -1;
    }


    /**
     * Gets the (primary key) id for the root net of a specification, creating new
     * specification and root net records if they have never been logged.
     * @param ySpecID the identifiers of the specification
     * @return the primary key for the root net entry for the specification
     */
    private long getRootNetID(YSpecificationID ySpecID) {
        Long result = _keyCache.rootNets.get(ySpecID);
        if (result == null) {
            YLogSpecification specEntry = getSpecificationEntry(ySpecID);
            if (specEntry != null) {
                result = specEntry.getRootNetID();
            }
            else {

                // insert new spec & root net entries
                specEntry = insertSpecification(ySpecID);
                result = insertNet(getRootNetName(ySpecID), specEntry.getRowKey());

                // now update spec entry with cross-ref to net entry
                specEntry.setRootNetID(result);
                updateRow(specEntry);
            }
            _keyCache.rootNets.put(ySpecID, result);
        }
        return result;
    }


    /**
     * Gets the (primary key) id for the net of the specification with the name specified,
     * and creates a new net record if the net has not yet been recorded.
     * Assumption: all (sub-)net names are unique within a specification.
     * @param ySpecID the identifiers of the specification
     * @param netName the name of the net or sub-net
     * @return the primary key for the net entry
     */
    private long getNetID(YSpecificationID ySpecID, String netName) {
        long specKey = getSpecificationKey(ySpecID);
        long netID = _keyCache.getNetID(ySpecID, netName);
        if (netID < 0) {
            String where = String.format("name='%s' AND tbl.specKey=%d", netName, specKey);
            YLogNet net = (YLogNet) selectScalarWhere("YLogNet", where);
            netID = (net != null) ? net.getNetID() : insertNet(netName, specKey);
            _keyCache.putNetID(ySpecID, netName, netID);
        }
        return netID;
    }


    /**
     * Gets the (primary key) id for a task
     * @param workItem a workitem instantiated from the task
     * @return the primary key for the task record
     * @see this.getTaskID(YSpecificationID, String, long)
     */
    private long getTaskID(YWorkItem workItem) {
        return getTaskID(workItem.getSpecificationID(), workItem.getTaskID(), -1);
    }


    /**
     * Gets the (primary key) id for a task, and creates a new task record if the task
     * has not yet been recorded.
     * @param ySpecID the identifiers of the specification
     * @param engineTaskID the task id assigned by the engine
     * @param childNetID the FK to the unfolded net of this task (composite tasks only)
     * @return the primary key for the task record
     */
    private long getTaskID(YSpecificationID ySpecID,
                           String engineTaskID, long childNetID) {
        YTask task = _engine.getTaskDefinition(ySpecID, engineTaskID);
        long netID = getNetID(ySpecID, task._net.getID());
        long taskID = _keyCache.getTaskID(netID, engineTaskID);
        if (taskID < 0) {
            String where = String.format("name='%s' AND tbl.parentNetID=%d",
                    engineTaskID, netID);
            YLogTask logTask = (YLogTask) selectScalarWhere("YLogTask", where);
            taskID = (logTask != null) ? logTask.getTaskID() :
                    insertTask(engineTaskID, netID, childNetID);
            _keyCache.putTaskID(netID, engineTaskID, taskID);
        }
        return taskID;
    }


    /**
     * Gets the primary key for the root net of a process instance
     * @param caseID the case id
     * @return the primary key for the root net record
     */
    private long getRootNetInstanceID(YIdentifier caseID) {
        return getNetInstanceID(caseID.getRootAncestor());
    }


    /**
     * Gets the primary key of a net instance
     * @param engineID the 'case id' of the net instance
     * @return the primary key of a net instance record
     */
    private long getNetInstanceID(YIdentifier engineID) {
        Long result = _keyCache.netInstances.get(engineID);
        if (result == null) {
            String where = String.format("engineInstanceID='%s'", engineID.toString());
            YLogNetInstance instance =
                    (YLogNetInstance) selectScalarWhere("YLogNetInstance", where);
            result = (instance != null) ? instance.getNetInstanceID() : -1;
            _keyCache.netInstances.put(engineID, result);
        }
        return result;
    }


    private long getOrCreateTaskInstanceID(YWorkItem workItem) {
        synchronized (TASK_INST_MUTEX) {
            long taskInstanceID = getTaskInstanceID(workItem);
            if (taskInstanceID < 0) {
                taskInstanceID = insertTaskInstance(workItem);
            }
            return taskInstanceID;
        }
    }

    /**
     * Gets the (primary key) id for a task instance
     * @param workItem a workitem instantiated from the task
     * @return the primary key for the task record
     * @see this.getTaskInstanceID(YIdentifier, long)
     */
    private long getTaskInstanceID(YWorkItem workItem) {
        return getTaskInstanceID(workItem.getCaseID(), getTaskID(workItem));
    }


    /**
     * Gets the primary key of a net instance
     * @param engineID the 'case id' of the task instance
     * @param taskID a foreign key to the corresponding YLogTask record
     * @return the primary key of a task instance record
     */
    private long getTaskInstanceID(YIdentifier engineID, long taskID) {
        long taskInstanceID = _keyCache.getTaskInstanceID(engineID, taskID);
        if (taskInstanceID < 0) {
            String where = String.format("engineInstanceID='%s' AND tbl.taskID=%d",
                                        engineID.toString(), taskID);
            YLogTaskInstance instance =
                    (YLogTaskInstance) selectScalarWhere("YLogTaskInstance", where);
            taskInstanceID = (instance != null) ? instance.getTaskInstanceID() : -1;
            _keyCache.putTaskInstanceID(engineID, taskID, taskInstanceID);
        }
        return taskInstanceID;
    }


    private long getServiceID(YWorkItem item) {
        return getServiceID(item.getExternalClient());
    }


    private long getServiceID(String serviceHandle) {
        YSession session = _engine.getSessionCache().getSession(serviceHandle);
        return (session != null) ? getServiceID(session.getClient()) : -1;
    }


    private long getServiceID(YClient client) {
        long result = -1;
        if (client != null) {
            String uri = (client instanceof YAWLServiceReference) ?
                         ((YAWLServiceReference) client).getURI() : null;
            result = getServiceID(client.getUserName(), uri) ;
        }
        return result;
    }


    /**
     * Gets the primary key of a service record
     * @param name the name of the service
     * @param url the url of the service
     * @return the primary key
     */
    private long getServiceID(String name, String url) {
        Long serviceID = _keyCache.services.get((url != null) ? url : name);
        if (serviceID == null) {
            String template = (url != null) ? "name='%s' AND tbl.url='%s'" : "name='%s'";
            String where = String.format(template, name, url);
            YLogService instance = (YLogService) selectScalarWhere("YLogService", where);
            serviceID = (instance != null) ? instance.getServiceID() : insertService(name, url);
            _keyCache.services.put((url != null) ? url : name, serviceID);
        }
        return serviceID;
    }


    /**
     * Gets the primary key of a data type definition record
     * @param item the data item with the data type to get the id for
     * @return the primary key of a dataType record
     */
    private long getDataTypeID(YLogDataItem item) {
        String name = item.getDataTypeName();
        String def = item.getDataTypeDefinition();
        long dataTypeID = _keyCache.getDataTypeID(name, def);
        if (dataTypeID == -1) {
            List list = getDb().createQuery(
                    "from YLogDataType where dataTypeName=:name")
                            .setString("name", name).list();
            if (! list.isEmpty()) {
                for (Object o : list) {
                    YLogDataType logDataType = (YLogDataType) o;
                    if (logDataType.getDefinition().equals(def)) {
                        dataTypeID = logDataType.getDataTypeID();
                        break;
                    }
                }
            }
            if (dataTypeID == -1) {                 // empty list or no match on defn.
                dataTypeID = insertDataType(item);
            }
            _keyCache.putDataTypeID(name, def, dataTypeID);
        }
        return dataTypeID;
    }


    /**
     * Gets the name of the root net for the given specification
     * @param ySpecID the id of the specification
     * @return the name of its root net
     */
    private String getRootNetName(YSpecificationID ySpecID) {
        YSpecification spec = _engine.getSpecification(ySpecID);
        return (spec != null) ? spec.getRootNet().getID() : "";
    }


    /**
     * Inserts a new specification record
     * @param ySpecID the id of the specification to insert
     * @return the inserted record
     */
    private YLogSpecification insertSpecification(YSpecificationID ySpecID) {
        YLogSpecification specEntry = new YLogSpecification(ySpecID) ;
        insertRow(specEntry);
        return specEntry;
    }


    /**
     * Inserts a new net record
     * @param netName the name of the net
     * @param specKey a foreign key to the parent specification
     * @return the primary key of the inserted record
     */
    private long insertNet(String netName, long specKey) {
        YLogNet netEntry = new YLogNet(netName, specKey);
        insertRow(netEntry);
        return netEntry.getNetID();
    }


    /**
     * Inserts a new net instance record
     * @param engineID the 'case id' of the net instance
     * @param netID a foreign key to the net record of which this is an instance
     * @param parentTaskInstanceID a foreign key to the parent task record that
     * 'unfolded' to this net instance (composite tasks only) - atomic tasks should have
     * a value of -1 for this parameter
     * @return the primary key of the inserted record
     */
    private long insertNetInstance(YIdentifier engineID, long netID,
                                   long parentTaskInstanceID) {
        YLogNetInstance netInstance =
                    new YLogNetInstance(engineID.toString(), netID, parentTaskInstanceID);
        insertRow(netInstance);
        return netInstance.getNetInstanceID();
    }


    /**
     * Inserts a new task record
     * @param taskName the name of the task
     * @param netID a foreign key to the encapsulating net
     * @param childNetID a foreign key to the child net this task unfolds to (composite
     * tasks only) - atomic tasks should have a value of -1 for this parameter
     * @return the primary key of the inserted record
     */
    private long insertTask(String taskName, long netID, long childNetID) {
        YLogTask taskEntry = new YLogTask(taskName, netID, childNetID) ;
        insertRow(taskEntry);
        return taskEntry.getTaskID();
    }


    /**
     * Inserts a new task instance record
     * @param workItem the task instance
     * @return the primary key of the inserted record
     * @see this.insertTaskInstance(String, long, long, long)
     */
    private long insertTaskInstance(YWorkItem workItem) {

        // make the workitem give up the required parameters
        long taskID = getTaskID(workItem);
        String engineInstanceID = workItem.getCaseID().toString();

        // only a child workitem will have a parent task instance
        YWorkItem parent = workItem.getParent();
        YNetRunner runner = workItem.getNetRunner();
        long parentTaskInstanceID = -1;
        if (parent != null) {
            parentTaskInstanceID = getTaskInstanceID(parent);
            runner = parent.getNetRunner();
        }
        long parentNetInstanceID = (runner != null) ?
                getNetInstanceID(runner.getCaseID()) : -1L;

        return insertTaskInstance(engineInstanceID, taskID,
                parentTaskInstanceID, parentNetInstanceID);
    }


    /**
     * Inserts a new task instance record
     * @param engineInstanceID the 'case id' of this task instance
     * @param taskID a foreign key to the task of which this is an instance
     * @param parentTaskInstanceID a foreign key to the parent task instance (child
     * workitems only, composite and parent workitem instances should have -1 for this
     * parameter)
     * @param parentNetInstanceID a foreign key to the encapsulating net instance
     * @return the primary key of the inserted record
     */
    private long insertTaskInstance(String engineInstanceID,
                                    long taskID, long parentTaskInstanceID,
                                    long parentNetInstanceID) {
        YLogTaskInstance taskInstance = new YLogTaskInstance(engineInstanceID, taskID,
                parentTaskInstanceID, parentNetInstanceID);
        insertRow(taskInstance);
        return taskInstance.getTaskInstanceID();
    }


    /**
     * Logs an event and its associated data items
     * @param instanceID a foreign key to either the net instance or the task instance
     * that generated the event
     * @param descriptor a label that describes the kind of event
     * @param datalist the list of data items to insert
     * @param serviceID a foreign key to the client service initiating the event
     * @param rootNetInstanceID a foreign key to the root net instance that (eventually)
     * encapsulates this event
     */
    private void logEvent(long instanceID, String descriptor,
                          YLogDataItemList datalist, long serviceID, long rootNetInstanceID) {
        long eventID = insertEvent(instanceID, descriptor, serviceID, rootNetInstanceID) ;
        insertDataItems(eventID, datalist);
    }


    /**
     * Inserts a new event record
     * @param instanceID a foreign key to either the net instance or the task instance
     * that generated the event
     * @param descriptor a label that describes the kind of event
     * @param serviceID a foreign key to the service that triggered the event
     * @param rootNetInstanceID a foreign key to the root net instance that (eventually)
     * encapsulates this event
     * @return the primary key of the inserted record
     */
    private long insertEvent(long instanceID, String descriptor,
                             long serviceID, long rootNetInstanceID) {
        YLogEvent logEvent = new YLogEvent(instanceID, descriptor, now(), serviceID,
                                           rootNetInstanceID);
        insertRow(logEvent);
        return logEvent.getEventID();        
    }


    /**
     * Inserts a new service record
     * @param name the name of the service
     * @param url the url of the service
     * @return the primary key of the inserted record
     */
    private long insertService(String name, String url) {
        YLogService serviceEntry = new YLogService(name, url);
        insertRow(serviceEntry);
        return serviceEntry.getServiceID();
    }


    /**
     * Inserts a list of data items records for an event
     * @param eventID a foreign key to the event record associated with the data items
     * @param datalist the list of data items to insert
     */
    private void insertDataItems(long eventID, YLogDataItemList datalist) {
        if (datalist != null) {
            for (YLogDataItem item : datalist) {
                long dataTypeID = getDataTypeID(item);
                YLogDataItemInstance itemInstance =
                        new YLogDataItemInstance(eventID, item, dataTypeID);
                insertRow(itemInstance);
            }
        }
    }


    /**
     * Inserts a new data type record
     * @param item the data item that contains the datatype to insert
     * @return the primary key of the inserted record
     */
    private long insertDataType(YLogDataItem item) {
        YLogDataType dataType =
                new YLogDataType(item.getDataTypeName(), item.getDataTypeDefinition());
        insertRow(dataType);
        return dataType.getDataTypeID();
    }


    /**
     * Populates the data definition for each data item in the list, for data items
     * originating from net and task instances (as opposed to externally supplied
     * data items which are assumed to come with their own data type definitions)
     * @param specID the specification identifier from which to get the data schemas
     * @param datalist the list of data items
     */
    private void populateDataListSchemas(YSpecificationID specID,
                                         YLogDataItemList datalist) {
        if (datalist != null) {

            // make sure this spec is mapped
            if (! _keyCache.dataSchema.contains(specID)) {
                _keyCache.dataSchema.add(specID);
            }
            for (YLogDataItem item : datalist) {
                String dataTypeName = item.getDataTypeName();
                item.setDataTypeDefinition(getDataSchema(specID, dataTypeName));
            }
        }
    }


    /**
     * Performs a scalar selection for a specified table and constraint
     * @param objName the name of the object table to select from
     * @param whereClause the constraint clause
     * @return the first or only matching record, or null if there's no match
     */
    private Object selectScalarWhere(String objName, String whereClause) {
        List list = getDb().getObjectsForClassWhere(objName, whereClause);
        return (! list.isEmpty()) ? list.get(0) : null;
    }


    /**
     * Inserts a row in the appropriate log table
     * @param o the object representing the contents of the row to insert
     */
    private void insertRow(Object o) {
        getDb().exec(o, HibernateEngine.DB_INSERT, true);
    }


    /**
     * Updates a row in the appropriate log table
     * @param o the object representing the contents of the row to update
     */
    private void updateRow(Object o) {
        getDb().exec(o, HibernateEngine.DB_UPDATE, true);
    }


    protected HibernateEngine getDb() {
        if (_db == null) {
            Set<Class> classSet = new HashSet<Class>(Arrays.asList(LOG_CLASSES));
            _db = new HibernateEngine(true, classSet);
        }
        return _db;
    }
    
}
