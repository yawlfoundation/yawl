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

package org.yawlfoundation.yawl.cost.data;

import org.yawlfoundation.yawl.cost.CostService;
import org.yawlfoundation.yawl.util.XNode;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * Stores a matrix of all the referenced task, resource and data entities of a
 * set of cost drivers to the driver(s) that reference them.
 */
public class DriverMatrix {
    
    Map<String, Set<CostDriver>> taskMap;             // [task name, drivers]
    Map<String, Set<CostDriver>> resourceMap;         // [resource id, drivers]
    Map<String, Set<CostDriver>> dataMap;             // [var name, drivers]
    Map<String, Set<String>> resolvedResources;       // [group id, participant ids]



    /**
     * Constructs a new DriverMatrix
     * @param models the set of models to use to create the matrix
     */
    public DriverMatrix(Set<CostModel> models) {
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
    private void unbundleModels(Set<CostModel> models) {
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
    private void addToMap(Map<String, Set<CostDriver>> map, CostDriver driver, String name) {
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
    private Set<String> resolveResource(String resourceID) {
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
     * the participant id
     * @param driverResource the resource id in the driver
     * @param participantID the participant id
     * @return true if the participant id equals or is a member of the driverResource
     */
    public boolean hasResourceMatch(String driverResource, String participantID) {
        Set<String> participantIDs = resolvedResources.get(driverResource);
        return (participantIDs != null) && participantIDs.contains(participantID);
    }
    
    
    public boolean hasDriversForTask(String taskName) {
        return taskMap.containsKey(taskName);
    }
    
    
    public boolean hasDriversForResource(String resource) {
        return resourceMap.containsKey(resource);
    }


    public boolean hasDriversForVariable(String variable) {
        return dataMap.containsKey(variable);
    }


    public Set<CostDriver> getTaskDrivers(String taskName) {
        return taskMap.get(taskName);
    }


    public Set<CostDriver> getResourceDrivers(String resource) {
        return resourceMap.get(resource);
    }


    public Set<CostDriver> getVariableDrivers(String variable) {
        return dataMap.get(variable);
    }
    
    
    public Map<String, XNode> getCostMap(String taskName, Set<String> resourceSet) {

        // resource is assumed to be a participant id
        Map<String, XNode> costMap = new Hashtable<String, XNode>();
        for (String resource : resourceSet) {
            costMap.put(resource, getResourceCost(resource, taskName));
        }
        return costMap;
    }


    public String getCostMapAsXML(String taskName, Set<String> resourceSet) {
        Map<String, XNode> costMap = getCostMap(taskName, resourceSet);
        XNode node = new XNode("costs");
        node.addAttribute("task", taskName);
        node.addChildren(costMap.values());
        return node.toString();
    }


    /**
     * For each driver in a Set, checks that all of its entities are satisfied in an event,
     * and if so, evaluates and annotates the cost data to the event
     */
    private XNode getResourceCost(String resource, String taskName) {
        XNode costs = new XNode("resource");
        costs.addAttribute("id", resource);
        costs.addAttribute("task", taskName);
        Set<CostDriver> drivers = resourceMap.get(resource);
        if (drivers != null) {
            for (CostDriver driver : drivers) {

                // we already know the entity contains the resource, and we don't care
                // about data, but only if it contains a task that matches the one passed
                boolean satisfied = true;
                for (DriverEntity entity : driver.getEntities()) {
                    if ((entity.getEntityType() == EntityType.task) &&
                        (! entity.getName().equals(taskName))) {
                        satisfied = false;
                        break;
                    }
                }
                if (satisfied) {
                    UnitCost unitCost = driver.getUnitCost();
                    XNode driverNode = costs.addChild("driver");
                    driverNode.addAttribute("id", driver.getID());
                    driverNode.addChild("amount", unitCost.getCostPerMSec());
                    driverNode.addChild("currency", unitCost.getCostValue().getCurrency());
                    driverNode.addChild("duration", unitCost.getDuration().name());
                }
            }
        }
        return costs;
    }



}
