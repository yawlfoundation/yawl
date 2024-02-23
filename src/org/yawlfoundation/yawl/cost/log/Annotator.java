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

package org.yawlfoundation.yawl.cost.log;

import org.yawlfoundation.yawl.cost.CostService;
import org.yawlfoundation.yawl.cost.data.DriverMatrix;
import org.yawlfoundation.yawl.cost.data.*;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * Annotates the events an XES formatted log with corresponding cost information
 * where applicable.
 *
 * @author Michael Adams
 * @date 27/10/11
 */
public class Annotator {

    private String _unannotatedLog;              // raw engine or resource xes log
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
     *
     * @param unannotatedLog an XES log to annotate with cost data
     */
    public Annotator(String unannotatedLog) {
        this();
        setUnannotatedLog(unannotatedLog);
    }


    /**
     * Sets the unannotated log
     *
     * @param unannotatedLog an XES log to annotate with cost data
     */
    public void setUnannotatedLog(String unannotatedLog) {
        _unannotatedLog = unannotatedLog;
    }


    /**
     * Sets the specification id for this log
     *
     * @param id the specification id
     */
    public void setSpecID(YSpecificationID id) { _specID = id; }


    /**
     * Annotates a log with applicable cost data
     *
     * @param unannotatedLog an XES log to annotate with cost data
     * @return the cost-data annotated log
     * @throws IllegalStateException if the unannotatedLog is null or has
     *                               an invalid format
     */
    public String annotate(String unannotatedLog) throws IllegalStateException {
        setUnannotatedLog(unannotatedLog);
        return annotate();
    }


    /**
     * Annotates a log with applicable cost data
     *
     * @return the cost-data annotated log
     * @throws IllegalStateException if the unannotatedLog is null (i.e. has
     *                               not been previously set via the relevant constructor
     *                               or mutator) or has an invalid format
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
     *
     * @param head a root node (hierarchically) containing the entire unannotated log
     * @return the node with (hierarchically) annotated cost data
     */
    private String annotate(XNode head) {
        CostModelCache cache = getModelCacheForLog(head);
        if (cache != null) {
            for (XNode traceNode : head.getChildren("trace")) {   // each trace = one case
                annotate(traceNode, cache.getDriverMatrix());
            }
            addCostHeaders(head);           // augment the header defs with cost elements
        }
        return head.toPrettyString(true);
    }


    /**
     * Annotates a log trace (i.e. a single case) with applicable cost data
     *
     * @param trace        a node containing all the events for a single case
     * @param driverMatrix a matrix of all cost drivers for the log's specification
     *                     mapped by task, resource and data
     */
    private void annotate(XNode trace, DriverMatrix driverMatrix) {
        for (XNode eventNode : trace.getChildren("event")) {
            UnbundledEvent unbundled = new UnbundledEvent(eventNode);

            // ignore any events with a transition of "unknown"
            if (unbundled.hasTransition("unknown")) continue;

            // update the timestamps record for this task
            TaskTimings timings = getTaskTimings(unbundled);
            timings.update(unbundled);

            // "completed" or "cancelled" events are candidates for cost annotation
            if (unbundled.isCompletedTransition() && (!timings.isProcessed())) {
                processDrivers(driverMatrix, unbundled, timings);
            }
        }
    }


    /**
     * Find the cost drivers corresponding to this event and use them to calculate the
     * relevant cost data, then annotate the data to the logged event
     *
     * @param driverMatrix a matrix of all cost drivers for the log's specification
     *                     mapped by task, resource and data
     * @param event        the (unbundled) completed or cancelled event to process
     * @param timings      the set of timestamps for the task referenced by this event
     */
    private void processDrivers(DriverMatrix driverMatrix, UnbundledEvent event,
                                TaskTimings timings) {
        Set<CostDriver> drivers = new HashSet<CostDriver>();

        // get all of the drivers for the task referenced by this event
        String name = event.getName();
        if ((name != null) && driverMatrix.hasDriversForTask(name)) {
            drivers.addAll(driverMatrix.getTaskDrivers(name));
        }

        // get all of the drivers for the resource referenced by this event
        String resource = event.getResource();
        if ((resource != null) && driverMatrix.hasDriversForResource(resource)) {
            drivers.addAll(driverMatrix.getResourceDrivers(resource));
        }

        // get all of the drivers for each data variable referenced by this event
        for (String variable : event.getDataMap().keySet()) {
            if (driverMatrix.hasDriversForVariable(variable)) {
                drivers.addAll(driverMatrix.getVariableDrivers(variable));
            }
        }

        // now that we've collected all possibly affected drivers, we can evaluate them
        evaluateDrivers(event, drivers, timings, driverMatrix);

        // since there are two 'completed' events for each task (one for the child,
        // one for the parent), we need to mark this task's evaluation as done
        timings.setProcessed(true);
    }


    /**
     * Gets the cost models for the log's specification
     *
     * @param head a root node (hierarchically) containing the entire unannotated log
     * @return the Set of CostModels applicable to this log's process specification
     */
    private CostModelCache getModelCacheForLog(XNode head) {

        // if the spec id has been set already, return that
        if (_specID != null) return CostService.getInstance().getModelCache(_specID);

        // no specID set - get spec details from log
        for (XNode stringNode : head.getChildren("string")) {  // should only be one
            if (stringNode.getAttributeValue("key").equals("concept:name")) {
                String[] specIDString = stringNode.getAttributeValue("value").split(" - version ");
                return CostService.getInstance().getModelCache(specIDString[0], specIDString[1]);
            }
        }
        return null;
    }


    /**
     * For each driver in a Set, checks that all of its entities are satisfied in an event,
     * and if so, evaluates and annotates the cost data to the event
     *
     * @param event        the event to annotate
     * @param drivers      the set of drivers that relate to this event
     * @param timings      the set of timestamps for the task this event relates to
     * @param driverMatrix a matrix of all cost drivers for the log's specification
     *                     resource entity
     */
    private void evaluateDrivers(UnbundledEvent event, Set<CostDriver> drivers,
                                 TaskTimings timings, DriverMatrix driverMatrix) {
        for (CostDriver driver : drivers) {

            // check that all the driver's entities appear in this event
            boolean satisfied = true;
            for (DriverFacet facet : driver.getFacets()) {
                satisfied = evaluateEntity(facet, event, driverMatrix);
                if (!satisfied) break;
            }

            // if all are satisfied, calculate and annotate
            if (satisfied) {
                double cost = calcCost(event, driver, timings);
                event.getNode().addChild(createCostNode(driver, cost));
            }
        }
    }


    /**
     * Calculate and return the costs associated with this event
     *
     * @param event   the event to calculate the costs for
     * @param driver  the cost driver to use for the calculation
     * @param timings the set of timestamps for the task this event relates to
     * @return the calculated cost
     */
    private double calcCost(UnbundledEvent event, CostDriver driver,
                            TaskTimings timings) {
        UnitCost unitCost = driver.getUnitCost();
        Map<String, String> dataMap = event.getDataMap();

        // if the unit cost duration is 'fixed', simply return the amount
        if (unitCost.getUnit().equals("fixed")) {
            return unitCost.getCostValue().getAmount(dataMap);  // duration ignored for fixed
        }

        // get the duration in msecs for the duration type defined in the cost driver
        long period;
        String instance = event.getInstance();
        switch (unitCost.getDuration()) {
            case assigned:
                period = timings.getAssignedTime();
                break;
            case allocated:
                period = timings.getAllocatedTime(instance);
                break;
            case busy:
                period = timings.getBusyTime(instance);
                break;
            case active:
                period = timings.getActiveTime(instance);
                break;
            case inactive:
                period = timings.getInactiveTime(instance);
                break;
            case suspended:
                period = timings.getSuspendedTime(instance);
                break;
            default:
                return unitCost.getCostValue().getAmount(dataMap);  // incl. nil
        }

        // -1 denotes calculation error, in which case return the simple amount
        return period > -1 ? period * unitCost.getCostPerMSec(dataMap) :
                unitCost.getCostValue().getAmount(dataMap);
    }


    /**
     * Creates a properly formatted cost node for insertion into the log
     *
     * @param driver the driver which produced the cost annotation
     * @param cost   the calculated cost amount
     * @return the cost node for insertion into the log
     */
    private XNode createCostNode(CostDriver driver, double cost) {
        XNode costNode = floatNode("cost:driver:" + driver.getID(), cost);
        costNode.addChild(stringNode("cost:type", driver.getCostTypesAsCSV()));
        CostValue costValue = driver.getUnitCost().getCostValue();
        costNode.addChild(stringNode("cost:currency", costValue.getCurrency()));
        return costNode;
    }


    /**
     * Creates a float node to insert into an event log
     *
     * @param key   the value of the 'key' attribute
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
     *
     * @param key   the value of the 'key' attribute
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
     *
     * @param node  the node to add the attributes to
     * @param key   the value of the 'key' attribute
     * @param value the value of the 'value' attribute
     */
    private void addAttributes(XNode node, String key, String value) {
        node.addAttribute("key", key);
        node.addAttribute("value", value);
    }


    /**
     * Adds the extra header information for cost annotations to the log headers
     *
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
     * Checks whether an facet is referenced by an event
     *
     * @param facet        the facet to check
     * @param event        the event in question
     * @param driverMatrix a matrix of all cost drivers for the log's specification
     * @return true if the facet matches a value in the event
     */
    private boolean evaluateEntity(DriverFacet facet, UnbundledEvent event,
                                   DriverMatrix driverMatrix) {
        switch (facet.getFacetAspect()) {
            case task:
                return facet.getName().equals(event.getName());
            case resource:
                return driverMatrix.hasResourceMatch(
                        facet.getName(), event.getResource());
            case data:
                return event.hasData() &&
                        event.hasDataMatch(facet.getName(), facet.getValue());
            default:
                return false;
        }
    }


    /**
     * Gets the grouping of task timestamps for an event
     *
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


}
