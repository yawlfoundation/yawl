/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.cost.log;

import org.yawlfoundation.yawl.cost.CostService;
import org.yawlfoundation.yawl.cost.data.*;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * Annotates the events an XES formatted log with corresponding cost information
 * where applicable.
 * @author Michael Adams
 * @date 27/10/11
 */
public class Annotator {
    
    private String _unannotatedLog;              // raw engine of resource xes log
    private YSpecificationID _specID;            // specification id for the log
    private Map<String, TaskTimings> _timings;   // timestamp grouping per task

    private static final String ERROR_PRETEXT = "Unannotated (input) log has ";

    /**
     * Constructs a new Annotator
     */
    public Annotator() {
        _timings = new Hashtable<String, TaskTimings>();
    }


    /**
     * Constructs a new Annotator with the log specified
     * @param unannotatedLog an XES log to annotate with cost data
     */
    public Annotator(String unannotatedLog) {
        this();
        setUnannotatedLog(unannotatedLog);
    }


    /**
     * Sets the unannotated log
     * @param unannotatedLog an XES log to annotate with cost data
     */
    public void setUnannotatedLog(String unannotatedLog) {
        _unannotatedLog = unannotatedLog;
    }


    /**
     * Sets the specification id for this log
     * @param id the specification id
     */
    public void setSpecID(YSpecificationID id) { _specID = id; }


    /**
     * Annotates a log with applicable cost data
     * @param unannotatedLog an XES log to annotate with cost data
     * @return the cost-data annotated log
     * @throws IllegalStateException if the unannotatedLog is null or has
     * an invalid format
     */
    public String annotate(String unannotatedLog) throws IllegalStateException {
        setUnannotatedLog(unannotatedLog); 
        return annotate();
    }


    /**
     * Annotates a log with applicable cost data
     * @return the cost-data annotated log
     * @throws IllegalStateException if the unannotatedLog is null (i.e. has
     * not been previously set via the relevant constructor or mutator) or has
     * an invalid format
     */
    public String annotate() throws IllegalStateException {
        if (_unannotatedLog == null) {
            throw new IllegalStateException(ERROR_PRETEXT + "a null value");
        }
        XNode head = new XNodeParser().parse(_unannotatedLog);
        if (head == null) {
            throw new IllegalStateException(ERROR_PRETEXT + "an invalid structure");
        }
        return annotate(head);
    }
    
    
    /***************************************************************************************/

    /**
     * Annotates a log with applicable cost data
     * @param head a root node (hierarchically) containing the entire unannotated log
     * @return the node with (hierarchically) annotated cost data
     */
    private String annotate(XNode head) {
        Set<CostModel> models = getModelsForLog(head);
        if (models != null) {
            DriverMatrix driverMatrix = new DriverMatrix(models);
            for (XNode traceNode : head.getChildren("trace")) {   // each trace = one case
                annotate(traceNode, driverMatrix);
            }
            addCostHeaders(head);           // augment the header defs with cost elements
        }
        return head.toPrettyString(true);
    }


    /**
     * Annotates a log trace (i.e. a single case) with applicable cost data
     * @param trace a node containing all the events for a single case
     * @param driverMatrix a matrix of all cost drivers for the log's specification
     * mapped by task, resource and data
     */
    private void annotate(XNode trace, DriverMatrix driverMatrix) {
        for (XNode eventNode : trace.getChildren("event")) {
            UnbundledEvent unbundled = new UnbundledEvent(eventNode);

            // ignore any events with a transition of "unknown"
            if (unbundled.transition.equals("unknown")) continue;

            // update the timestamps record for this task
            TaskTimings timings = getTaskTimings(unbundled);
            timings.update(unbundled);

            // "completed" or "cancelled" events are candidates for cost annotation
            if (unbundled.isCompletedTransition() && (! timings.processed)) {
                processDrivers(driverMatrix, unbundled, timings);
            }
        }
    }


    /**
     * Find the cost drivers corresponding to this event and use them to calculate the
     * relevant cost data, then annotate the data to the logged event
     * @param driverMatrix a matrix of all cost drivers for the log's specification
     * mapped by task, resource and data
     * @param event the (unbundled) completed or cancelled event to process
     * @param timings the set of timestamps for the task referenced by this event
     */
    private void processDrivers(DriverMatrix driverMatrix, UnbundledEvent event,
                                TaskTimings timings) {
        Set<CostDriver> drivers = new HashSet<CostDriver>();

        // get all of the drivers for the task referenced by this event
        String name = event.name;
        if ((name != null) && driverMatrix.taskMap.containsKey(name)) {
            drivers.addAll(driverMatrix.taskMap.get(name));
        }

        // get all of the drivers for the resource referenced by this event
        String resource = event.resource;
        if ((resource != null) && driverMatrix.resourceMap.containsKey(event.resource)) {
            drivers.addAll(driverMatrix.resourceMap.get(event.resource));
        }

        // get all of the drivers for each data variable referenced by this event
        for (String dataName : event.getDataMap().keySet()) {
            Set<CostDriver> dataDrivers = driverMatrix.dataMap.get(dataName);
            if (dataDrivers != null) drivers.addAll(dataDrivers);
        }

        // now that we've collected all possibly affected drivers, we can evaluate them
        evaluateDrivers(event, drivers, timings, driverMatrix);

        // since there are two 'completed' events for each task (one for the child,
        // one for the parent), we need to mark this task's evaluation as done
        timings.processed = true;
    }


    /**
     * Gets the cost models for the log's specification
     * @param head a root node (hierarchically) containing the entire unannotated log
     * @return the Set of CostModels applicable to this log's process specification
     */
    private Set<CostModel> getModelsForLog(XNode head) {

        // if the spec id has been set already, return that
        if (_specID != null) return CostService.getInstance().getModels(_specID);

        // no specID set - get spec details from log
        for (XNode stringNode : head.getChildren("string")) {  // should only be one
            if (stringNode.getAttributeValue("key").equals("concept:name")) {
                String[] specIDString = stringNode.getAttributeValue("value").split(" - version ");
                return CostService.getInstance().getModels(specIDString[0], specIDString[1]);
            }
        }
        return null;
    }


    /**
     * For each driver in a Set, checks that all of its entities are satisfied in an event,
     * and if so, evaluates and annotates the cost data to the event
     * @param event the event to annotate
     * @param drivers the set of drivers that relate to this event
     * @param timings the set of timestamps for the task this event relates to
     * @param driverMatrix a matrix of all cost drivers for the log's specification
     *                  resource entity
     */
    private void evaluateDrivers(UnbundledEvent event, Set<CostDriver> drivers,
                                 TaskTimings timings, DriverMatrix driverMatrix) {
        for (CostDriver driver : drivers) {

            // check that all the driver's entities appear in this event
            boolean satisfied = true;
            for (DriverEntity entity : driver.getEntities()) {
                satisfied = evaluateEntity(entity, event, driverMatrix);
                if (! satisfied) break;
            }

            // if all are satisfied, calculate and annotate
            if (satisfied) {
                double cost = calcCost(event, driver, timings);
                event.xnode.addChild(createCostNode(driver, cost));
            }
        }
    }


    /**
     * Calculate and return the costs associated with this event
     * @param event the event to calculate the costs for
     * @param driver the cost driver to use for the calculation
     * @param timings the set of timestamps for the task this event relates to
     * @return the calculated cost
     */
    private double calcCost(UnbundledEvent event, CostDriver driver,
                            TaskTimings timings) {
        UnitCost unitCost = driver.getUnitCost();

        // if the unit cost duration is 'fixed', simply return the amount
        if (unitCost.getUnit().equals("fixed")) {
            return unitCost.getCostValue().getAmount();  // duration ignored for fixed
        }

        // get the duration in msecs for the duration type defined in the cost driver
        long period;
        String instance = event.instance;
        switch (unitCost.getDuration()) {
            case assigned  : period = timings.getAssignedTime(); break;
            case allocated : period = timings.getAllocatedTime(instance); break;
            case busy      : period = timings.getBusyTime(instance); break;
            case active    : period = timings.getWorkingTime(instance); break;
            case inactive  : period = timings.getWaitingTime(instance); break;
            case suspended : period = timings.getSuspendedTime(instance); break;
            default        : return unitCost.getCostValue().getAmount();  // incl. nil
        }

        // -1 denotes calculation error, in which case return the simple amount
        return period > -1 ? period * unitCost.getCostPerMSec() :
                unitCost.getCostValue().getAmount();
    }


    /**
     * Creates a properly formatted cost node for insertion into the log
     * @param driver the driver which produced the cost annotation
     * @param cost the calculated cost amount
     * @return the cost node for insertion into the log
     */
    private XNode createCostNode(CostDriver driver, double cost) {
        XNode costNode = stringNode("cost:driver", driver.getID());
        costNode.addChild(floatNode("cost:amount", cost));
        for (CostType costType : driver.getCostTypes()) {
            costNode.addChild(stringNode("cost:type", costType.getType()));
        }
        CostValue costValue = driver.getUnitCost().getCostValue();
        costNode.addChild(stringNode("cost:currency", costValue.getCurrency()));
        return costNode;
    }


    /**
     * Creates a float node to insert into an event log
     * @param key the value of the 'key' attribute
     * @param value the value of the 'value' attribute
     * @return the constructed node
     */
    private XNode floatNode(String key, double value) {
        XNode floatNode = new XNode("float");
        addAttributes(floatNode, key, String.valueOf(value));
        return floatNode;
    }


    /**
     * Creates a string node to insert into an event log
     * @param key the value of the 'key' attribute
     * @param value the value of the 'value' attribute
     * @return the constructed node
     */
    private XNode stringNode(String key, String value) {
        XNode stringNode = new XNode("string");
        addAttributes(stringNode, key, value);
        return stringNode;
    }

    
    /**
     * Adds attributes to a node
     * @param node the node to add the attributes to
     * @param key the value of the 'key' attribute
     * @param value the value of the 'value' attribute
     */
    private void addAttributes(XNode node, String key, String value) {
        node.addAttribute("key", key);
        node.addAttribute("value", value);
    }


    /**
     * Adds the extra header information for cost annotations to the log headers
     * @param head a root node (hierarchically) containing the entire unannotated log
     */
    private void addCostHeaders(XNode head) {

        // add cost extension
        XNode extNode = new XNode("extension");
        extNode.addAttribute("name", "Cost");
        extNode.addAttribute("prefix", "cost");
        extNode.addAttribute("uri", "http://www.yawlfoundation.org/yawlschema/xes/cost.xesext");
        int pos = head.posChildWithAttribute("name", "Organizational");     // insert after
        head.insertChild(pos + 1, extNode);

        // add definition to global event reference
        for (XNode globalNode : head.getChildren("global")) {
            if (globalNode.getAttributeValue("scope").equals("event")) {
                XNode costNode = globalNode.addChild(stringNode("cost:driver", "UNKNOWN"));
                costNode.addChild(floatNode("cost:amount", 0.0));
                costNode.addChild(stringNode("cost:type", "UNKNOWN"));
                costNode.addChild((stringNode("cost:currency", "UNKNOWN")));
                break;
            }
        }

        head.insertComment(2, "and then annotated with cost information by the Cost Service");
    }


    /**
     * Checks whether an entity is referenced by an event
     * @param entity the entity to check
     * @param event the event in question
     * @param driverMatrix a matrix of all cost drivers for the log's specification
     * @return true if the entity matches a value in the event
     */
    private boolean evaluateEntity(DriverEntity entity, UnbundledEvent event,
                                   DriverMatrix driverMatrix) {
        switch (entity.getEntityType()) {
            case task     : return entity.getName().equals(event.name);
            case resource : return driverMatrix.hasResourceMatch(
                                entity.getName(), event.resource);
            case data     : return event.hasData &&
                                event.hasDataMatch(entity.getName(), entity.getValue());
            default       : return false;
        }
    }


    /**
     * Gets the grouping of task timestamps for an event
     * @param event the event to get the timings object for
     * @return the timings object
     */
    private TaskTimings getTaskTimings(UnbundledEvent event) {
        String key = event.getKey();
        TaskTimings timings = _timings.get(key);
        if (timings == null) {                    // new task
            timings = new TaskTimings(event);
            _timings.put(key, timings);
        }
        return timings;
    }


    /**********************************************************************************/

    /**
     * Unbundles the information in a log event node into its composite parts
     */
    class UnbundledEvent {
        XNode xnode;                                         // the orig. event node
        String timestamp;
        String name;                                         // task name
        String transition;                                   // lifecycle change
        String instance;                                     // work item's case id
        String resource;                                     // resource id

        // we're only interested in the data in 'complete' events, so to save time
        // we won't unravel the data elements whe nthey are unneeded
        boolean hasData = false;
        Map<String, String> dataMap;


        /**
         * Constructs a new UnbundledEvent
         * @param eventNode the node this object will represent
         */
        UnbundledEvent(XNode eventNode) {
            xnode = eventNode;                               // store orig. node

            // read values from event node elements
            for (XNode child : xnode.getChildren()) {
                String key = child.getAttributeValue("key");
                if (key.equals("time:timestamp")) {
                    timestamp = child.getAttributeValue("value");
                }
                else if (key.equals("concept:name")) {
                    name = child.getAttributeValue("value");
                }
                else if (key.equals("lifecycle:transition")) {
                    transition = child.getAttributeValue("value");
                }
                else if (key.equals("lifecycle:instance")) {
                    instance = child.getAttributeValue("value");
                }
                else if (key.equals("org:resource")) {
                    resource = child.getAttributeValue("value");
                }
                else hasData = true;                    // no match means a data element
            }
        }


        /**
         * Checks if there is a matching data variable AND value in this event
         * @param key the name of the variable
         * @param value its value
         * @return true iff a data variable of the same name as 'key' exists in this
         * event, and its 'value' matches the value logged for it
         */
        boolean hasDataMatch(String key, String value) {
            return getDataMap().containsKey(key) && value.equals(getDataMap().get(key));
        }


        /**
         * Checks if this event's transition come before the 'start' event
         * @return true if this event precedes the starting of the work item
         */
        boolean isPreStart() {
            return transition.equals("schedule") || transition.equals("reassign") ||
                    transition.equals("allocate");
        }


        /**
         * Checks if this event's transition is a completion
         * @return true if this event represents the completion of the work item
         */
        boolean isCompletedTransition() {
            return transition.equals("complete") || transition.endsWith("abort");
        }


        /**
         * Gets the map of data variables and values for this event, if any. (Note:
         * only 'start' and 'complete' events contain data values)
         * @return the data map (which will be empty if there are no data values
         * associated with this event)
         */
        Map<String, String> getDataMap() {
            if (dataMap == null) {
                dataMap = new Hashtable<String, String>();
                for (XNode child : xnode.getChildren()) {
                    if (! child.getAttributeValue("key").contains(":")) {
                        dataMap.put(child.getAttributeValue("key"),
                                child.getAttributeValue("value"));
                    }
                }
            }
            return dataMap;
        }


        /**
         * Gets the key used to index this event in higher level maps
         * @return the appropriate key
         */
        String getKey() {
            String id = instance;

            // if this event is of a child work item, we need the key of its parent
            if (! isPreStart()) {
                int pos = instance.lastIndexOf('.');
                if (pos > -1) id = instance.substring(0, pos);
            }
            return id + ":" + name;
        }

    }

    /**********************************************************************************/

    /**
     * Stores a matrix of all the referenced task, resource and data entities of a
     * set of cost drivers to the driver(s) that reference them.
     */
    class DriverMatrix {
        Map<String, Set<CostDriver>> taskMap;             // [task name, drivers]
        Map<String, Set<CostDriver>> resourceMap;         // [resource id, drivers]
        Map<String, Set<CostDriver>> dataMap;             // [var name, drivers]
        Map<String, Set<String>> resolvedResources;       // [group id, participant ids]



        /**
         * Constructs a new DriverMatrix
         * @param models the set of models to use to create the matrix
         */
        DriverMatrix(Set<CostModel> models) {
            taskMap = new Hashtable<String, Set<CostDriver>>();
            resourceMap = new Hashtable<String, Set<CostDriver>>();
            dataMap = new Hashtable<String, Set<CostDriver>>();
            resolvedResources = new Hashtable<String, Set<String>>();
            unbundleModels(models);
        }


        /**
         * Creates the matrix of cost models
         * @param models the models to use
         */
        void unbundleModels(Set<CostModel> models) {
            for (CostModel model : models) {
                for (CostDriver driver : model.getDrivers()) {
                    for (DriverEntity entity : driver.getEntities()) {
                        if (entity.getEntityType() == EntityType.task) {
                            addToMap(taskMap, driver, entity.getName());
                        }
                        else if (entity.getEntityType() == EntityType.resource) {

                            // model's entity name may reference a set of resources
                            for (String resourceID : resolveResource(entity.getName())) {
                                addToMap(resourceMap, driver, resourceID);
                            }
                        }
                        else if (entity.getEntityType() == EntityType.data) {
                            addToMap(dataMap, driver, entity.getName());
                        }
                    }
                }
            }
        }


        /**
         * Adds a cost driver to a map
         * @param map the map to add the driver to
         * @param driver the cost driver to add
         * @param name the name to use as the key
         */
        void addToMap(Map<String, Set<CostDriver>> map, CostDriver driver, String name) {
            Set<CostDriver> drivers = map.get(name);
            if (drivers == null) {
                drivers = new HashSet<CostDriver>();
                map.put(name, drivers);
            }
            drivers.add(driver);
        }


        /**
         * Resolves a resource id from the entities of a cost driver into a set of
         * participant ids (as stored in the log).
         * @param resourceID A cost driver's resource entity name, which may refer to
         * a participant, role, capability, position or org group
         * @return the ids of the set of participants referenced by the entity id
         */
        Set<String> resolveResource(String resourceID) {
            if (resolvedResources.containsKey(resourceID)) {
                return resolvedResources.get(resourceID);
            }
            Set<String> resolved = CostService.getInstance().resolveResources(resourceID);
            resolvedResources.put(resourceID, resolved);
            return resolved;
        }


        /**
         * checks whether the resource id in a driver entity tag (which may identify a
         * participant, role, capability, position or org group) can be resolved to
         * the participant id contained in the event
         * @param driverResource the resource id in the driver
         * @param eventResource the resource id in the log
         * @return true if there's a match
         */
        boolean hasResourceMatch(String driverResource, String eventResource) {
            Set<String> participantIDs = resolvedResources.get(driverResource);
            return (participantIDs != null) && participantIDs.contains(eventResource);
        }

    }

    /*************************************************************************************/

    /**
     * Stores the set of timestamps for a task (i.e. across a number of event nodes)
     */
    class TaskTimings {
        
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        String taskName;
        String instanceID;
        String scheduled;
        String allocated;

        // Since one task may start more than one child work item, we need a map
        // to store multiple started, suspended and completed timestamps
        Map<String, String> started;
        Map<String, String> completed;
        Map<String, String> suspended;
        Map<String, Long> totalSuspended;
  
        // Each task has at least 2 complete events (one for the parent, one for each
        // child) - this flag ensures we process the correct event (and only once per
        // task instance)
        boolean processed = false;


        /**
         * Constructs a new TaskTimings object
         * @param event the initial event for a particular task instance
         */
        TaskTimings(UnbundledEvent event) {
            taskName = event.name;
            instanceID = event.instance;
        }


        /**
         * Adds the timestamp for the transition found in an event
         * @param event the event to store the timestamp for
         */
        void update(UnbundledEvent event) {
            processIfSuspended(event);
            if (event.transition.equals("schedule")) {
                scheduled = event.timestamp;
            }
            else if (event.transition.equals("allocate")) {
                allocated = event.timestamp;
            }
            else if (event.transition.equals("start")) {
                if (started == null) started = new Hashtable<String, String>();
                started.put(event.instance, event.timestamp);
                
                // it may have gone straight from schedule to start
                if (allocated == null) allocated = event.timestamp;
            }
            else if (event.isCompletedTransition()) {
                if (completed == null) completed = new Hashtable<String, String>();
                completed.put(event.instance, event.timestamp);
            }
            else if (event.transition.equals("suspend")) {
                if (suspended == null) suspended = new Hashtable<String, String>();
                suspended.put(event.instance, event.timestamp);
            }
            else if (event.transition.equals("withdraw")) {

                // withdraw means deallocate - i.e. revert from allocated to offered
                allocated = null;
            }

            // other possible events (no action required):
            // 'resume' - handled by #processIfSuspended
            // 'reassign' - (i) if delegated (i.e. when allocated), it remains allocated;
            //              (ii) if reallocated (i.e. when started), it remains started
            // 'manualskip' - skipped so can be ignored
            // 'unknown' - ignored
        }


        /**
         * If the last event processed was a suspend event, calculate and store
         * how long the work item was suspended for
         * @param event the current event
         */
        void processIfSuspended(UnbundledEvent event) {

            // if it has a suspended timestamp, any later event is effectively a resume
            if (isSuspended(event.instance) && (! event.transition.equals("suspend"))) {
                String suspTimestamp = suspended.remove(event.instance);
                if (suspTimestamp != null) {
                    long suspTime = timeDiff(event.timestamp, suspTimestamp);
                    if (suspTime > -1) {
                        addSuspendedTime(event.instance, suspTime);
                    }
                }
            }
        }


        /**
         * Calculate and store a period of suspension for a work item
         * @param instance the work item instance id
         * @param time the amount of time to store
         */
        void addSuspendedTime(String instance, long time) {
            if (totalSuspended == null) totalSuspended = new Hashtable<String, Long>();
            Long prevTime = totalSuspended.get(instance);
            if (prevTime != null) {
                time += prevTime;
            }
            totalSuspended.put(instance, time);
        }


        /**
         * Checks whether the last processed event was a suspend event
         * @param instance the work item instance
         * @return true if the last event for this work item was a suspend event
         */
        boolean isSuspended(String instance) {
            return (suspended != null) && suspended.containsKey(instance);
        }


        /**
         * @return the number of msecs that expired from when the task was offered
         * until when it was allocated to a resource
         */
        long getAssignedTime() {
            return timeDiff(allocated, scheduled);
        }


        /**
         * @param instance the instance id of the work item
         * @return the number of msecs that expired from when the work item was 
         * allocated until when it was started by a resource
         */
        long getAllocatedTime(String instance) {
            return timeDiff(started.get(instance), allocated);
        }


        /**
         * @param instance the instance id of the work item
         * @return the number of msecs that expired from when the work item was started
         * until when it was completed or cancelled
         */
        long getBusyTime(String instance) {
            return timeDiff(completed.get(instance), started.get(instance));
        }


        /**
         * @param instance the instance id of the work item
         * @return the number of msecs that expired from when the work item was enabled
         * until when it was started
         */
        long getWaitingTime(String instance) {
            return timeDiff(started.get(instance), scheduled);
        }


        /**
         * @param instance the instance id of the work item
         * @return the number of msecs that expired from when the work item was started
         * until when it was completed or cancelled, less any time spent suspended
         */
        long getWorkingTime(String instance) {
            return getBusyTime(instance) - getSuspendedTime(instance);
        }


        /**
         * @param instance the instance id of the work item
         * @return the number of msecs that expired while the work item was suspended
         */
        long getSuspendedTime(String instance) {
            if (totalSuspended != null) {
                Long time = totalSuspended.get(instance);
                if (time != null) {
                    return time;
                }
            }
            return 0;
        }


        /**
         * Converts a timestamp string to a msec representation of it
         * @param timestamp the timestamp to convert
         * @return its msec equivalent, or -1 if the timestamp can't be parsed
         */
        private long timestampToLong(String timestamp) {
            if (timestamp == null) return -1;
            try {
                return sdf.parse(timestamp).getTime();
            }
            catch (ParseException pe) {
                return -1;
            }
        }


        /**
         * Calculates the number of msecs difference between two timestamp strings
         * @param later the later timestamp
         * @param earlier the earlier timestamp
         * @return the number of msecs difference
         */
        private long timeDiff(String later, String earlier) {
            return timeDiff(timestampToLong(later), timestampToLong(earlier));
        }


        /**
         * Calculates the number of msecs difference between two times
         * @param later the later time
         * @param earlier the earlier time
         * @return later-earlier, iff each has a value > -1 and later > earlier,
         * otherwise -1 is returned
         */
        private long timeDiff(long later, long earlier) {
            return (later > -1) && (earlier > -1) && (later > earlier) ?
                   (later - earlier) : -1;
        }
                

    }

}
