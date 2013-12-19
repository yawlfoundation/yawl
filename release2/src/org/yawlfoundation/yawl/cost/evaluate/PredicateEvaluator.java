package org.yawlfoundation.yawl.cost.evaluate;

import org.yawlfoundation.yawl.cost.data.*;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.ResourceEvent;

import java.util.*;

/**
 * @author Michael Adams
 * @date 3/12/12
 */
public class PredicateEvaluator {

    private Map<String, ResourceTaskTimings> _timings;   // timestamp grouping per task
    private Map<String, TaskCost> _taskCosts;

    public PredicateEvaluator() { }


    public boolean evaluate(Predicate predicate, List<ResourceEvent> events,
                            DriverMatrix matrix) {
        _timings = new Hashtable<String, ResourceTaskTimings>();
        _taskCosts = new Hashtable<String, TaskCost>();
        return evaluate(predicate, groupAndCalculate(events, matrix));
    }


    private boolean evaluate(Predicate predicate, int caseCount) {
        double cost = 0;
        for (TaskCost taskCost : _taskCosts.values()) {
            if (meetsCriteria(taskCost, predicate)) {
                cost += taskCost.getCost();
            }
        }
        if (predicate.average() && cost > 0 && caseCount > 1) {
            cost /= caseCount;
        }
        return predicate.evaluate(cost);
    }


    private int groupAndCalculate(List<ResourceEvent> events, DriverMatrix matrix) {
        Set<String> uniqueCases = new HashSet<String>();
        for (ResourceEvent event : events) {
            if (event.get_taskID() == null || event.get_taskID().length() == 0) continue;
            if (event.get_event().equals("unknown")) continue;

            ResourceTaskTimings timings = getTaskTimings(event);
            timings.update(event);

            // "completed" or "cancelled" events are candidates for cost annotation
            if (isConcludingEvent(event) && (!timings.isProcessed())) {
                processDrivers(matrix, event, timings);
            }
            uniqueCases.add(getRootCaseID(event.get_caseID()));
        }
        return uniqueCases.size();
    }


    private boolean meetsCriteria(TaskCost taskCost, Predicate predicate) {
        Set<String> taskList = predicate.getTaskList();
        Set<String> resourceList = predicate.getResourceList();

        return taskCost != null &&
               (taskList == null || taskList.contains(taskCost.getTaskID())) &&
               (resourceList == null || resourceList.contains(taskCost.getResourceID()));
    }


    /**
     * Find the cost drivers corresponding to this event and use them to calculate the
     * relevant cost data
     *
     * @param driverMatrix a matrix of all cost drivers for the log's specification
     *                     mapped by task, resource and data
     * @param event        the completed or cancelled event to process
     * @param timings      the set of timestamps for the task referenced by this event
     */
    private void processDrivers(DriverMatrix driverMatrix, ResourceEvent event,
                                ResourceTaskTimings timings) {
        Set<CostDriver> drivers = new HashSet<CostDriver>();

        // get all of the drivers for the task referenced by this event
        String taskID = event.get_taskID();
        if ((taskID != null) && driverMatrix.hasDriversForTask(taskID)) {
            drivers.addAll(driverMatrix.getTaskDrivers(taskID));
        }

        // get all of the drivers for the resource referenced by this event
        String resource = event.get_resourceID();
        if ((resource != null) && driverMatrix.hasDriversForResource(resource)) {
            drivers.addAll(driverMatrix.getResourceDrivers(resource));
        }

        // get all of the drivers for each data variable referenced by this event
        //        for (String variable : event.getDataMap().keySet()) {
        //            if (driverMatrix.hasDriversForVariable(variable)) {
        //                drivers.addAll(driverMatrix.getVariableDrivers(variable));
        //            }
        //        }

        // now that we've collected all possibly affected drivers, we can evaluate them
        evaluateDrivers(event, drivers, timings, driverMatrix);

        // since there are two 'completed' events for each task (one for the child,
        // one for the parent), we need to mark this task's evaluation as done
        timings.setProcessed(true);
    }


    /**
     * For each driver in a Set, checks that all of its entities are satisfied in an event,
     * and if so, evaluates and saves the cost data to the event
     *
     * @param event        the event to annotate
     * @param drivers      the set of drivers that relate to this event
     * @param timings      the set of timestamps for the task this event relates to
     * @param driverMatrix a matrix of all cost drivers for the log's specification
     *                     resource entity
     */
    private void evaluateDrivers(ResourceEvent event, Set<CostDriver> drivers,
                                 ResourceTaskTimings timings, DriverMatrix driverMatrix) {
        for (CostDriver driver : drivers) {

            // check that all the driver's entities appear in this event
            boolean satisfied = true;
            for (DriverFacet facet : driver.getFacets()) {
                satisfied = evaluateEntity(facet, event, driverMatrix);
                if (!satisfied) break;
            }

            // if all are satisfied, calculate and save
            if (satisfied) {
                double cost = calcCost(event, driver, timings);
                TaskCost taskCost = new TaskCost(event.get_taskID(), cost);
                _taskCosts.put(event.get_taskID(), taskCost);
                if (driver.hasFacetAspect(FacetAspect.resource)) {
                    taskCost.setResourceID(event.get_resourceID());
                }
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
    private double calcCost(ResourceEvent event, CostDriver driver,
                            ResourceTaskTimings timings) {
        UnitCost unitCost = driver.getUnitCost();
        //        Map<String, String> dataMap = event.getDataMap();
        Map<String, String> dataMap = new Hashtable<String, String>();

        // if the unit cost duration is 'fixed', simply return the amount
        if (unitCost.getUnit().equals("fixed")) {
            return unitCost.getCostValue().getAmount(dataMap);  // duration ignored for fixed
        }

        // get the duration in msecs for the duration type defined in the cost driver
        long period;
        String instance = event.get_itemID();
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
                return 0;  // incl. nil
        }

        // -1 denotes calculation error, in which case return the simple amount
        return period > -1 ? period * unitCost.getCostPerMSec(dataMap) :
                unitCost.getCostValue().getAmount(dataMap);
    }


    /**
     * Checks whether an facet is referenced by an event
     *
     * @param facet        the facet to check
     * @param event        the event in question
     * @param driverMatrix a matrix of all cost drivers for the log's specification
     * @return true if the facet matches a value in the event
     */
    private boolean evaluateEntity(DriverFacet facet, ResourceEvent event,
                                   DriverMatrix driverMatrix) {
        switch (facet.getFacetAspect()) {
            case task:
                return facet.getName().equals(event.get_taskID());
            case resource:
                return driverMatrix.hasResourceMatch(
                        facet.getName(), event.get_resourceID());
            //            case data     : return event.hasData() &&
            //                                event.hasDataMatch(facet.getName(), facet.getValue());
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
    private ResourceTaskTimings getTaskTimings(ResourceEvent event) {
        String key = event.get_taskID();
        ResourceTaskTimings timings = _timings.get(key);
        if (timings == null) {                    // new task
            timings = new ResourceTaskTimings(event);
            _timings.put(key, timings);
        }
        return timings;
    }


    private boolean isConcludingEvent(ResourceEvent event) {
        return event.get_event().equals("complete") ||
                event.get_event().equals("cancelled");
    }


    private String getRootCaseID(String caseID) {
        if (caseID == null) return null;
        int period = caseID.indexOf('.');
        return (period > -1) ? caseID.substring(0, period) : caseID;
    }

}


