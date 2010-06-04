/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.logging;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.authentication.*;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.*;
import static org.yawlfoundation.yawl.engine.YWorkItemStatus.statusIsParent;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.logging.table.*;
import org.yawlfoundation.yawl.schema.XSDType;
import org.yawlfoundation.yawl.schema.YDataSchemaCache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * and again for v2.1 04-12/2009
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

    private final String WARN_MSG = "WARNING: Exception writing %s to the process logs.";

    // caches caseIDs <--> rootNetInstanceIDs for writing to event table rows
    private Map<String, Long> _caseIDtoRootNetMap = new HashMap<String, Long>() ;

    private Logger _log = Logger.getLogger(YEventLogger.class);
    private boolean _enabled = true;
    private static YEventLogger _me = null;
    private YPersistenceManager _pmgr;
    private YEngine _engine;

    private YDataSchemaCache _dataSchemaCache = new YDataSchemaCache();


    // PUBLIC INTERFACE METHODS //


    /**
     * Get a reference to the event logger object. This one is called by the engine on
     * startup.
     * @param engine a reference to the running engine
     * @return an instantiated event logger
     */
    public static YEventLogger getInstance(YEngine engine) {
        if (_me == null) _me = new YEventLogger();
        _me._engine = engine;
        if (YEngine.isPersisting()) {
            try {
                _me._pmgr = getPersistenceSession();
            }
            catch (YPersistenceException ype) {
                _me.disable();
                _me._log.error("Exception initialising the Process Logging Engine. " +
                               " Process logging disabled");
            }
        }
        else {
            _me.disable();
            _me._log.warn("Process logging disabled because Engine persistence is disabled");            
        }
        return _me;
    }


    /** @return an instantiated event logger */
    public static YEventLogger getInstance() {
        if (_me == null) _me = new YEventLogger();
        return _me;
    }


    /* enables event logging (the default) */
    public void enable() { _enabled = true; }

    /* disables event logging */
    public void disable() { _enabled = false; }


    public String getDataSchema(YSpecificationID specID, String dataTypeName) {
        return XSDType.getInstance().isBuiltInType(dataTypeName) ? dataTypeName :
                _dataSchemaCache.getSchemaTypeAsString(specID, dataTypeName);
    }

    public void removeSpecificationDataSchemas(YSpecificationID specID) {
        _dataSchemaCache.remove(specID);
    }


    /********************************************************************************/

    /**
     * Logs the launching of a new process instance.
     * @param ySpecID the id of the process specification
     * @param caseID the id of the case
     * @param datalist a list of data entries to log with this event
     */
    public void logCaseCreated(YSpecificationID ySpecID, YIdentifier caseID,
                               YLogDataItemList datalist, String serviceRef) {
        if (loggingEnabled()) {
            try {
                long netInstanceID = insertNetInstance(caseID, getRootNetID(ySpecID), -1);
                long serviceID = getServiceID(serviceRef);
                logEvent(netInstanceID, CASE_START, datalist, serviceID, netInstanceID);
                _caseIDtoRootNetMap.put(caseID.toString(), netInstanceID);
            }
            catch (YPersistenceException ype) {
                _log.error(getWarnMsg("case creation"), ype);
            }
        }
    }


    /**
     * Logs the launching of a sub-net (ie. the enablement of a composite task).
     * @param ySpecID the id of the process specification
     * @param runner the net runner launching this sub-net
     * @param engineTaskID the task id assigned by the engine
     * @param datalist a list of data entries to log with this event
     */
    public void logSubNetCreated(YSpecificationID ySpecID, YNetRunner runner,
                                 String engineTaskID, YLogDataItemList datalist) {
        if (loggingEnabled()) {
            try {
                // get the required foreign key values
                YIdentifier subnetID = runner.getCaseID();
                long netID = getNetID(ySpecID, runner.getNet().getID());
                long taskID = getTaskID(ySpecID, engineTaskID, netID);
                long rootNetInstanceID = getRootNetInstanceID(subnetID);

                // log the composite task enablement first
                long parentTaskInstanceID = getTaskInstanceID(subnetID, taskID);
                if (parentTaskInstanceID < 0) {
                    parentTaskInstanceID = insertTaskInstance(subnetID.toString(), taskID,
                            -1, getNetInstanceID(subnetID.getParent()));
                }
                logEvent(parentTaskInstanceID, NET_UNFOLD, null, -1, rootNetInstanceID);

                // now log the subnet launch
                long netInstanceID = insertNetInstance(subnetID, netID, parentTaskInstanceID);
                logEvent(netInstanceID, NET_START, datalist, -1, rootNetInstanceID);
            }
            catch (YPersistenceException ype) {
                _log.error(getWarnMsg("sub-net creation"), ype);
            }
        }
    }


    /**
     * Logs a case cancellation.
     * @param caseID the id of the case
     * @param datalist a list of data entries to log with this event
     */
    public void logCaseCancelled(YIdentifier caseID, YLogDataItemList datalist,
                                 String serviceRef) {
        if (loggingEnabled()) {
            try {
                long netInstanceID = getNetInstanceID(caseID);
                long serviceID = getServiceID(serviceRef);
                logEvent(netInstanceID, CASE_CANCEL, datalist, serviceID,
                         getRootNetInstanceID(caseID));
            }
            catch (YPersistenceException ype) {
                _log.error(getWarnMsg("case cancellation"), ype);
            }
        }
    }


    /**
     * Logs the normal completion of a net. If the net is a root net, it means the
     * case has completed.
     * @param engineNetID the id of the net
     * @param datalist a list of data entries to log with this event
     */
    public void logNetCompleted(YIdentifier engineNetID, YLogDataItemList datalist) {
        if (loggingEnabled()) {
            try {
                String event;
                long rootNetInstanceID = getRootNetInstanceID(engineNetID);
                long netInstanceID = getNetInstanceID(engineNetID);

                // a root net has no parent
                if (engineNetID.getParent() != null) {
                    event = NET_COMPLETE ;
                }
                else {
                    event = CASE_COMPLETE;
                    _caseIDtoRootNetMap.remove(engineNetID.toString()) ;
                }
                logEvent(netInstanceID, event, datalist, -1, rootNetInstanceID);
            }
            catch (YPersistenceException ype) {
                _log.error(getWarnMsg("net completion"), ype);
            }
        }
    }


     public void logNetCancelled(YSpecificationID ySpecID, YNetRunner runner,
                                 String engineTaskID, YLogDataItemList datalist) {
        if (loggingEnabled()) {
            try {
                // get the required foreign key values
                YIdentifier subnetID = runner.getCaseID();
                long netID = getNetID(ySpecID, runner.getNet().getID());
                long taskID = getTaskID(ySpecID, engineTaskID, netID);
                long rootNetInstanceID = getRootNetInstanceID(subnetID);

                // log the composite task cancellation
                long parentTaskInstanceID = getTaskInstanceID(subnetID, taskID);
                logEvent(parentTaskInstanceID, NET_CANCEL, datalist, -1, rootNetInstanceID);
            }
            catch (YPersistenceException ype) {
                _log.error(getWarnMsg("sub-net creation"), ype);
            }
        }
    }


    /**
     * Logs a workitem event (change of status).
     * @param workItem the workitem that triggered the event
     * @param eventName the event that has occurred
     * @param datalist a list of data entries to log with this event
     */
    public void logWorkItemEvent(YWorkItem workItem, String eventName,
                                 YLogDataItemList datalist) {
        if (loggingEnabled()) {
            try {
                long taskInstanceID = getTaskInstanceID(workItem);
                if (taskInstanceID < 0) {
                    taskInstanceID = insertTaskInstance(workItem);
                }
                logEvent(taskInstanceID, eventName, datalist, getServiceID(workItem),
                        getRootNetInstanceID(workItem.getCaseID()));
            }
            catch (YPersistenceException ype) {
                _log.error(getWarnMsg("workitem event"), ype);
            }
        }
    }


    /**
     * Logs a workitem event (change of status).
     * @param workItem the workitem that triggered the event
     * @param event the event that has occurred
     * @param datalist a list of data entries to log with this event
     */
    public void logWorkItemEvent(YWorkItem workItem, YWorkItemStatus event,
                                 YLogDataItemList datalist) {
        String eventName = event.equals(statusIsParent) ? "Decompose" : event.toString();
        logWorkItemEvent(workItem, eventName, datalist);
    }


    /**
     * Logs data variables and values when mapped between net and workitem
     * @param workitem the workitem starting or completing
     * @param descriptor a label for the kind of data it is
     * @param datalist a list of data entries to log with this event
     */
    public void logDataEvent(YWorkItem workitem, String descriptor,
                             YLogDataItemList datalist) {
        if (loggingEnabled() && (datalist.size() > 0)) {
            try {
                long instanceID = getTaskInstanceID(workitem);
                populateDataListSchemas(workitem.getSpecificationID(), datalist);
                logEvent(instanceID, descriptor, datalist, -1, 
                        getRootNetInstanceID(workitem.getCaseID()));
            }
            catch (YPersistenceException ype) {
                _log.error(getWarnMsg("data event"), ype);
            }
        }
    }


    /*****************************************************************************/

    //PRIVATE (IMPLEMENTATION) METHODS //

    /** @return the current system time */
    private long now() { return System.currentTimeMillis(); }


    private boolean loggingEnabled() {
        return (_pmgr != null) && _enabled;
    }

    private String getWarnMsg(String value) {
        return String.format(WARN_MSG, value);
    }

    /**
     * Starts a persistence session (so that records can be written to the database)
     * @return the instantiated session
     * @throws YPersistenceException if there's a problem with the persistence layer
     */
    private static YPersistenceManager getPersistenceSession() throws YPersistenceException {
        YPersistenceManager pmgr = new YPersistenceManager(YEngine.getPMSessionFactory());
        pmgr.startTransactionalSession();
        return pmgr ;
    }


    /**
     * Gets the row of the YLogSpecification table matching the spec data passed
     * @param ySpecID the identifiers of the specification
     * @return the matching table row as a YLogSpecification object, or null if no match
     * @throws YPersistenceException if there's a problem with the persistence layer
     */
    private YLogSpecification getSpecificationEntry(YSpecificationID ySpecID)
            throws YPersistenceException {

        // pre-2.0 specs don't have an identifier field
        String field = (ySpecID.getIdentifier() != null) ? "identifier" : "uri";
        String where = String.format("%s='%s' AND version='%s'", field, ySpecID.getKey(),
                                      ySpecID.getVersionAsString());
        return (YLogSpecification) selectScalarWhere("YLogSpecification", where);
    }


    /**
     * Gets the primary key for a specification record.
     * @param ySpecID the identifiers of the specification
     * @return the primary key for the specification
     * @throws YPersistenceException if there's a problem with the persistence layer
     */
    private long getSpecificationKey(YSpecificationID ySpecID)
            throws YPersistenceException {
        long result = -1;
        YLogSpecification specEntry = getSpecificationEntry(ySpecID);
        if (specEntry != null) {
            result = specEntry.getRowKey();
        }
        return result;
    }


    /**
     * Gets the (primary key) id for the root net of a specification, creating new
     * specification and root net records if they have never been logged.
     * @param ySpecID the identifiers of the specification
     * @return the primary key for the root net entry for the specification
     * @throws YPersistenceException if there's a problem with the persistence layer
     */
    private long getRootNetID(YSpecificationID ySpecID) throws YPersistenceException {
        long result;
        YLogSpecification specEntry = getSpecificationEntry(ySpecID);
        if (specEntry != null) {
            result = specEntry.getRootNetID();                          // entry exists
        }
        else {

            // insert new spec & root net entries
            specEntry = insertSpecification(ySpecID);
            result = insertNet(getRootNetName(ySpecID), specEntry.getRowKey());

            // now update spec entry with cross-ref to net entry
            specEntry.setRootNetID(result);
            updateRow(specEntry);
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
     * @throws YPersistenceException if there's a problem with the persistence layer
     */
    private long getNetID(YSpecificationID ySpecID, String netName)
            throws YPersistenceException {
        long result;
        long specKey = getSpecificationKey(ySpecID);
        String where = String.format("name='%s' AND specKey=%d", netName, specKey);
        YLogNet net = (YLogNet) selectScalarWhere("YLogNet", where);
        if (net != null) {
            result = net.getNetID();
        }
        else {
            result = insertNet(netName, specKey);
        }
        return result;
    }


    /**
     * Gets the (primary key) id for a task
     * @param workItem a workitem instantiated from the task
     * @return the primary key for the task record
     * @throws YPersistenceException if there's a problem with the persistence layer
     * @see this.getTaskID(YSpecificationID, String, long)
     */
    private long getTaskID(YWorkItem workItem) throws YPersistenceException {
        return getTaskID(workItem.getSpecificationID(), workItem.getTaskID(), -1);
    }


    /**
     * Gets the (primary key) id for a task, and creates a new task record if the task
     * has not yet been recorded.
     * @param ySpecID the identifiers of the specification
     * @param engineTaskID the task id assigned by the engine
     * @param childNetID the FK to the unfolded net of this task (composite tasks only)
     * @return the primary key for the task record
     * @throws YPersistenceException if there's a problem with the persistence layer
     */
    private long getTaskID(YSpecificationID ySpecID, String engineTaskID, long childNetID)
            throws YPersistenceException {
        long result ;
        YTask task = _engine.getTaskDefinition(ySpecID, engineTaskID);
        long netID = getNetID(ySpecID, task._net.getID()) ;
        String where = String.format("name='%s' AND parentNetID=%d", task.getName(), netID);
        YLogTask logTask = (YLogTask) selectScalarWhere("YLogTask", where);

        if (logTask != null) {
            result = logTask.getTaskID();
        }
        else {
            result = insertTask(task.getName(), netID, childNetID);
        }
        return result;
    }


    /**
     * Gets the primary key for the root net of a process instance
     * @param caseID the case id
     * @return the primary key for the root net record
     */
    private long getRootNetInstanceID(YIdentifier caseID) throws YPersistenceException {
        long result = -1;
        String rootCaseID = caseID.getRootAncestor().toString();
        if (_caseIDtoRootNetMap.containsKey(rootCaseID)) {
            result = _caseIDtoRootNetMap.get(rootCaseID);
        }
        else {
            Long key = getNetInstanceID(caseID.getRootAncestor());
            if (key != null) result = key;
        }
        return result;
    }


    /**
     * Gets the primary key of a net instance
     * @param engineID the 'case id' of the net instance
     * @return the primary key of a net instance record
     * @throws YPersistenceException if there's a problem with the persistence layer
     */
    private long getNetInstanceID(YIdentifier engineID)
            throws YPersistenceException {
        long result = -1;
        String where = String.format("engineInstanceID='%s'", engineID.toString());
        YLogNetInstance instance =
                (YLogNetInstance) selectScalarWhere("YLogNetInstance", where);
        if (instance != null) {
            result = instance.getNetInstanceID();
        }
        return result;
    }


    /**
     * Gets the (primary key) id for a task instance
     * @param workItem a workitem instantiated from the task
     * @return the primary key for the task record
     * @throws YPersistenceException if there's a problem with the persistence layer
     * @see this.getTaskInstanceID(YIdentifier, long)
     */
    private long getTaskInstanceID(YWorkItem workItem)
            throws YPersistenceException {
        return getTaskInstanceID(workItem.getCaseID(), getTaskID(workItem));
    }


    /**
     * Gets the primary key of a net instance
     * @param engineID the 'case id' of the task instance
     * @param taskID a foreign key to the corresponding YLogTask record
     * @return the primary key of a task instance record
     * @throws YPersistenceException if there's a problem with the persistence layer
     */
    private long getTaskInstanceID(YIdentifier engineID, long taskID)
            throws YPersistenceException {
        long result = -1;
        String where = String.format("engineInstanceID='%s' AND taskID=%d",
                                    engineID.toString(), taskID);
        YLogTaskInstance instance =
                (YLogTaskInstance) selectScalarWhere("YLogTaskInstance", where);
        if (instance != null) {
            result = instance.getTaskInstanceID();
        }
        return result;
    }


    private long getServiceID(YWorkItem item) throws YPersistenceException {
        return getServiceID(item.getExternalClient());
    }


    private long getServiceID(String serviceHandle) throws YPersistenceException {
        long result = -1;
        YSession session = _engine.getSessionCache().getSession(serviceHandle);
        if (session != null) {
            result = getServiceID(session.getClient());
        }
        return result;
    }


    private long getServiceID(YClient client) throws YPersistenceException {
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
     * @throws YPersistenceException if there's a problem with the persistence layer
     */
    private long getServiceID(String name, String url) throws YPersistenceException {
        String template = (url != null) ? "name='%s' AND url='%s'" : "name='%s'";
        String where = String.format(template, name, url);
        YLogService instance = (YLogService) selectScalarWhere("YLogService", where);
        return (instance != null) ? instance.getServiceID() : insertService(name, url);
    }


    /**
     * Gets the primary key of a data type definition record
     * @param item the data item with the data type to get the id for
     * @return the primary key of a dataType record
     * @throws YPersistenceException if there's a problem with the persistence layer
     */
    private long getDataTypeID(YLogDataItem item) throws YPersistenceException {
        long result;
        String where = String.format("definition='%s'", item.getDataTypeDefinition());
        YLogDataType dataType = (YLogDataType) selectScalarWhere("YLogDataType", where);

        if (dataType != null) {
            result = dataType.getDataTypeID();
        }
        else {   // no match
            result = insertDataType(item);
        }
        return result;
    }


    /**
     * Gets the name of the root net for the given specification
     * @param ySpecID the id of the specification
     * @return the name of its root net
     */
    private String getRootNetName(YSpecificationID ySpecID) {
        String result = "";
        YSpecification spec = _engine.getSpecification(ySpecID);
        if (spec != null) result = spec.getRootNet().getID();
        return result;
    }


    /**
     * Inserts a new specification record
     * @param ySpecID the id of the specification to insert
     * @return the inserted record
     * @throws YPersistenceException if there's a problem with the persistence layer
     */
    private YLogSpecification insertSpecification(YSpecificationID ySpecID)
            throws YPersistenceException {
        YLogSpecification specEntry = new YLogSpecification(ySpecID) ;
        insertRow(specEntry);
        return specEntry;
    }


    /**
     * Inserts a new net record
     * @param netName the name of the net
     * @param specKey a foreign key to the parent specification
     * @return the primary key of the inserted record
     * @throws YPersistenceException if there's a problem with the persistence layer
     */
    private long insertNet(String netName, long specKey) throws YPersistenceException {
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
     * @throws YPersistenceException if there's a problem with the persistence layer
     */
    private long insertNetInstance(YIdentifier engineID, long netID,
                               long parentTaskInstanceID) throws YPersistenceException {
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
     * @throws YPersistenceException if there's a problem with the persistence layer
     */
    private long insertTask(String taskName, long netID, long childNetID)
            throws YPersistenceException {
        YLogTask taskEntry = new YLogTask(taskName, netID, childNetID) ;
        insertRow(taskEntry);
        return taskEntry.getTaskID();
    }


    /**
     * Inserts a new task instance record
     * @param workItem the task instance
     * @return the primary key of the inserted record
     * @throws YPersistenceException if there's a problem with the persistence layer
     * @see this.insertTaskInstance(String, long, long, long)
     */
    private long insertTaskInstance(YWorkItem workItem) throws YPersistenceException {

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
        long parentNetInstanceID = getNetInstanceID(runner.getCaseID());

        return insertTaskInstance(engineInstanceID, taskID, parentTaskInstanceID,
                                  parentNetInstanceID);
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
     * @throws YPersistenceException if there's a problem with the persistence layer
     */
    private long insertTaskInstance(String engineInstanceID, long taskID,
                                    long parentTaskInstanceID, long parentNetInstanceID)
            throws YPersistenceException {

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
     * @param rootNetInstanceID a foreign key to the root net instance that (eventually)
     * encapsulates this event
     * @throws YPersistenceException if there's a problem with the persistence layer
     */
    private void logEvent(long instanceID, String descriptor, YLogDataItemList datalist,
                          long serviceID, long rootNetInstanceID)
            throws YPersistenceException {
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
     * @throws YPersistenceException if there's a problem with the persistence layer
     */
    private long insertEvent(long instanceID, String descriptor, long serviceID,
                             long rootNetInstanceID) throws YPersistenceException {
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
     * @throws YPersistenceException if there's a problem with the persistence layer
     */
    private long insertService(String name, String url) throws YPersistenceException {
        YLogService serviceEntry = new YLogService(name, url);
        insertRow(serviceEntry);
        return serviceEntry.getServiceID();
    }


    /**
     * Inserts a list of data items records for an event
     * @param eventID a foreign key to the event record associated with the data items
     * @param datalist the list of data items to insert
     * @throws YPersistenceException if there's a problem with the persistence layer
     */
    private void insertDataItems(long eventID, YLogDataItemList datalist)
            throws YPersistenceException {
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
     * @throws YPersistenceException if there's a problem with the persistence layer
     */
    private long insertDataType(YLogDataItem item) throws YPersistenceException {
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
            if (! _dataSchemaCache.contains(specID)) {
                _dataSchemaCache.add(specID);
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
     * @throws YPersistenceException if there's a problem with the persistence layer
     */
    private Object selectScalarWhere(String objName, String whereClause)
            throws YPersistenceException {
        Object result = null;
        List list = _pmgr.getObjectsForClassWhere(objName, whereClause);
        if (! list.isEmpty()) result = list.get(0);
        return result;
    }


    /**
     * Inserts a row in the appropriate log table
     * @param o the object representing the contents of the row to insert
     * @throws YPersistenceException if there's a problem with the persistence layer
     */
    private void insertRow(Object o) throws YPersistenceException {
        _engine.storeObject(o);
    }


    /**
     * Updates a row in the appropriate log table
     * @param o the object representing the contents of the row to update
     * @throws YPersistenceException if there's a problem with the persistence layer
     */
    private void updateRow(Object o) throws YPersistenceException {
        _engine.updateObject(o);
    }


}
