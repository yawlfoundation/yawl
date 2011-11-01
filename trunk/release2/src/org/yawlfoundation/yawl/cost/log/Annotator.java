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
 * @author Michael Adams
 * @date 27/10/11
 */
public class Annotator {
    
    private String _unannotatedLog;
    private YSpecificationID _specID;
    private Map<String, TaskTimings> _timings;
    
    public Annotator() {
        _timings = new Hashtable<String, TaskTimings>();
    }
    
    public Annotator(String unannotatedLog) {
        this();
        setUnannotatedLog(unannotatedLog);
    }

    
    public void setUnannotatedLog(String unannotatedLog) {
        _unannotatedLog = unannotatedLog;
    }


    public void setSpecID(YSpecificationID id) { _specID = id; }
    
    
    public String annotate(String unannotatedLog) throws IllegalStateException {
        setUnannotatedLog(unannotatedLog); 
        return annotate();
    }


    public String annotate() throws IllegalStateException {
        if (_unannotatedLog == null) {
            throw new IllegalStateException("Unannotated (input) log has a null value");
        }
        XNode head = new XNodeParser().parse(_unannotatedLog);
        if (head == null) {
            throw new IllegalStateException("Unannotated (input) log has an invalid structure");
        }
        return annotate(head);
    }
    
    
    /***************************************************************************************/
    
    private String annotate(XNode head) {
        Set<CostModel> models = getModelsForLog(head);
        if (models != null) {
            DriverMatrix driverMatrix = new DriverMatrix(models);
            for (XNode traceNode : head.getChildren("trace")) {   // each trace = one case
                annotate(traceNode, driverMatrix);
            }
            head.insertComment(2, "and then annotated with cost information by the Cost Service");
        }
        return head.toPrettyString(true);
    }
    
    
    private void annotate(XNode trace, DriverMatrix driverMatrix) {
        for (XNode eventNode : trace.getChildren("event")) {
            UnbundledEvent unbundled = new UnbundledEvent(eventNode);
            if (unbundled.transition.equals("unknown")) continue;         // ignore

            TaskTimings timings = getTaskTimings(unbundled);
            timings.update(unbundled);

            if (unbundled.isCompletedTransition() && (! timings.processed)) {
                processDrivers(driverMatrix, unbundled, timings);
            }
        }
    }


    private void processDrivers(DriverMatrix driverMatrix, UnbundledEvent unbundled,
                                TaskTimings timings) {
        Set<CostDriver> drivers = new HashSet<CostDriver>();
        if (driverMatrix.taskMap.containsKey(unbundled.name)) {
            drivers.addAll(driverMatrix.taskMap.get(unbundled.name));
        }
        if (driverMatrix.resourceMap.containsKey(unbundled.resource)) {
            drivers.addAll(driverMatrix.resourceMap.get(unbundled.resource));
        }
        for (String dataName : unbundled.getDataMap().keySet()) {
            Set<CostDriver> dataDrivers = driverMatrix.dataMap.get(dataName);
            if (dataDrivers != null) drivers.addAll(dataDrivers);
        }
        evaluateDrivers(unbundled, drivers, timings);
        timings.processed = true;
    }
    
    
    private Set<CostModel> getModelsForLog(XNode head) {
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
    
    
    private void evaluateDrivers(UnbundledEvent unbundled, Set<CostDriver> drivers,
                                 TaskTimings timings) {
        for (CostDriver driver : drivers) {
            boolean satisfied = true;
            for (DriverEntity entity : driver.getEntities()) {
                satisfied = evaluateEntity(entity, unbundled);
                if (! satisfied) break;
            }
            if (satisfied) {
                double cost = calcCost(unbundled, driver, timings);
                unbundled.xnode.addChild(createCostNode(driver, cost));
            }
        }
    }


    private double calcCost(UnbundledEvent unbundled, CostDriver driver,
                            TaskTimings timings) {
        long period;
        UnitCost unitCost = driver.getUnitCost();
        switch (unitCost.getDuration()) {
            case assigned  : period = timings.getAssignedTime(); break;
            case allocated : period = timings.getAllocatedTime(unbundled); break;
            case busy      : period = timings.getBusyTime(unbundled); break;
            case active    : period = timings.getWorkingTime(unbundled); break;
            case inactive  : period = timings.getWaitingTime(unbundled); break;
            case suspended : period = timings.getSuspendedTime(unbundled); break;
            default        : return unitCost.getCostValue().getAmount();  // incl. nil
        }

        // period is msec duration (-1 denotes calc error)
        return period > -1 ? period * unitCost.getCostPerMSec() :
                unitCost.getCostValue().getAmount();
    }


    private XNode createCostNode(CostDriver driver, double cost) {
        CostValue costValue = driver.getUnitCost().getCostValue();
        XNode costNode = floatNode("cost:driver:" + driver.getID(), cost);
        int i = 1;
        for (CostType costType : driver.getCostTypes()) {
            costNode.addChild(stringNode("cost:type:" + i++, costType.getType()));
        }
        costNode.addChild(stringNode("cost:currency", costValue.getCurrency()));
        return costNode;
    }
    

    private XNode floatNode(String key, double value) {
        XNode floatNode = new XNode("float");
        addAttributes(floatNode, key, String.valueOf(value));
        return floatNode;
    }


    private XNode stringNode(String key, String value) {
        XNode stringNode = new XNode("string");
        addAttributes(stringNode, key, value);
        return stringNode;
    }

    
    private void addAttributes(XNode node, String key, String value) {
        node.addAttribute("key", key);
        node.addAttribute("value", value);
    }


    private boolean evaluateEntity(DriverEntity entity, UnbundledEvent unbundled) {
        switch (entity.getEntityType()) {
            case task     : return entity.getName().equals(unbundled.name);
            case resource : return entity.getName().equals(unbundled.resource);
            case data     : return unbundled.hasData &&
                                unbundled.hasDataMatch(entity.getName(), entity.getValue());
            default       : return false;
        }
    }
    
    
    private TaskTimings getTaskTimings(UnbundledEvent event) {
        String key = event.getKey();
        TaskTimings timings = _timings.get(key);
        if (timings == null) {
            timings = new TaskTimings(event);
            _timings.put(key, timings);
        }
        return timings;
    }


    private void removeTaskTimings(UnbundledEvent event) {
        _timings.remove(event.getKey());
    }


    /**********************************************************************************/

    class UnbundledEvent {
        XNode xnode;
        String timestamp;
        String name;
        String transition;
        String instance;
        String resource;
        boolean hasData = false;
        Map<String, String> dataMap;

        
        UnbundledEvent(XNode eventNode) {
            xnode = eventNode;
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
        
        
        boolean hasDataMatch(String key, String value) {
            return getDataMap().containsKey(key) && value.equals(getDataMap().get(key));
        }
        
        
        boolean isPreStart() {
            return transition.equals("schedule") || transition.equals("reassign") ||
                    transition.equals("allocate");
        }


        boolean isCompletedTransition() {
            return transition.equals("complete") || transition.endsWith("abort");
        }
        
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
        
        String getKey() {
            String id = instance;
            if (! isPreStart()) {
                int pos = instance.lastIndexOf('.');
                if (pos > -1) id = instance.substring(0, pos);
            }
            return id + ":" + name;
        }

    }

    /**********************************************************************************/

    class DriverMatrix {
        Map<String, Set<CostDriver>> taskMap;
        Map<String, Set<CostDriver>> resourceMap;
        Map<String, Set<CostDriver>> dataMap;

        DriverMatrix(Set<CostModel> models) {
            taskMap = new Hashtable<String, Set<CostDriver>>();
            resourceMap = new Hashtable<String, Set<CostDriver>>();
            dataMap = new Hashtable<String, Set<CostDriver>>();
            unbundleModels(models);
        }

        void unbundleModels(Set<CostModel> models) {
            for (CostModel model : models) {
                for (CostDriver driver : model.getDrivers()) {
                    for (DriverEntity entity : driver.getEntities()) {
                        if (entity.getEntityType() == EntityType.task) {
                            addToMap(taskMap, driver, entity.getName());
                        }
                        else if (entity.getEntityType() == EntityType.resource) {
                            addToMap(resourceMap, driver, entity.getName());
                        }
                        else if (entity.getEntityType() == EntityType.data) {
                            addToMap(dataMap, driver, entity.getName());
                        }
                    }
                }
            }
        }


        void addToMap(Map<String, Set<CostDriver>> map, CostDriver driver, String name) {
            Set<CostDriver> drivers = map.get(name);
            if (drivers == null) {
                drivers = new HashSet<CostDriver>();
                map.put(name, drivers);
            }
            drivers.add(driver);
        }

    }

    /*************************************************************************************/

    class TaskTimings {
        
        String taskName;
        String instanceID;
        String scheduled;
        String allocated;
        Map<String, String> started;
        Map<String, String> completed;

        boolean processed = false;

        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        
        TaskTimings(String name, String id) {
            taskName = name;
            instanceID = id;
        }
        
        TaskTimings(UnbundledEvent event) {
            this(event.name, event.instance);
        }


        void update(UnbundledEvent event) {
            if (event.transition.equals("schedule")) {
                scheduled = event.timestamp;
            }
            else if (event.transition.equals("allocate")) {
                allocated = event.timestamp;
            }
            else if (event.transition.equals("start")) {
                if (started == null) started = new Hashtable<String, String>();
                started.put(event.instance, event.timestamp);
            }
            else if (event.isCompletedTransition()) {
                if (completed == null) completed = new Hashtable<String, String>();
                completed.put(event.instance, event.timestamp);
            }
        }


        // Resource Timings

        long getAssignedTime() {
            return timeDiff(allocated, scheduled);
        }

        long getAllocatedTime(UnbundledEvent event) {
            return timeDiff(started.get(event.instance), allocated);
        }

        long getBusyTime(UnbundledEvent event) {
            return timeDiff(completed.get(event.instance), started.get(event.instance));
        }


        // Task Timings

        long getWaitingTime(UnbundledEvent event) {
            return timeDiff(started.get(event.instance), scheduled);
        }

        long getWorkingTime(UnbundledEvent event) {     // busy - suspended
            return getBusyTime(event);   // less any time spent suspended
        }

        long getSuspendedTime(UnbundledEvent event) {
            // todo
            return -1;
        }
        
        
        private long timestampToLong(String timestamp) {
            if (timestamp == null) return -1;
            try {
                return sdf.parse(timestamp).getTime();
            }
            catch (ParseException pe) {
                return -1;
            }
        }

        
        private long timeDiff(String later, String earlier) {
            return timeDiff(timestampToLong(later), timestampToLong(earlier));
        }
        
        private long timeDiff(long later, long earlier) {
            return (later > -1) && (earlier > -1) && (later > earlier) ?
                   (later - earlier) : -1;
        }
                

    }

}
