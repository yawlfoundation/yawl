/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.core.resourcing;

import org.yawlfoundation.yawl.editor.core.resourcing.validation.InvalidReference;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.resourcing.constraints.AbstractConstraint;

import java.util.*;

/**
 * Caches all resource references for each task of the loaded specification
 * @author Michael Adams
 * @date 27/06/12
 */
public class ResourcesCache {

    // [netID, [taskID, set of task resources]]
    private final Map<String, Map<String, TaskResourceSet>> _cache;


    public ResourcesCache() {
        _cache = new HashMap<String, Map<String, TaskResourceSet>>();
    }


    public void add(String netID, String taskID, TaskResourceSet resources) {
        if (! (netID == null || taskID == null || resources == null)) {
            getNetMap(netID).put(taskID, resources);
        }
    }


    public void add(TaskResourceSet resources) {
        YTask task = resources.getTask();
        if (task != null) {
            add(task.getNet().getID(), task.getID(), resources);
        }
    }


    public TaskResourceSet get(String netID, String taskID) {
        if (! (netID == null || taskID == null)) {
            Map<String, TaskResourceSet> netMap = getNetMap(netID);
            if (netMap != null) {
                return netMap.get(taskID);
            }
        }
        return null;
    }


    public TaskResourceSet remove(String netID, String taskID) {
        if (! (netID == null || taskID == null)) {
            Map<String, TaskResourceSet> netMap = getNetMap(netID);
            if (netMap != null) {
                return netMap.remove(taskID);
            }
        }
        return null;
    }


    public boolean hasLoadedResources() {
        for (Map<String, TaskResourceSet> map : _cache.values()) {    // net in spec
            for (TaskResourceSet resources : map.values()) {          // task in spec
                if (resources.hasAnyResourceReferences()) return true;
            }
        }
        return false;
    }


    public void updateRationalisedReferences(Map<String, String> updateMap) {
        for (Map<String, TaskResourceSet> map : _cache.values()) {    // net in spec
            for (TaskResourceSet resources : map.values()) {          // task in spec
                BasicOfferInteraction offer = resources.getOffer();
                String famTaskID = offer.getFamiliarParticipantTask();
                if (famTaskID != null && updateMap.containsKey(famTaskID)) {
                    offer.setFamiliarParticipantTask(updateMap.get(famTaskID));
                }
                for (AbstractConstraint constraint : offer.getConstraintSet().getAll()) {
                    if (constraint.getName().equals("SeparationOfDuties")) {
                        famTaskID = constraint.getParamValue("familiarTask");
                        if (famTaskID != null && updateMap.containsKey(famTaskID)) {
                            constraint.setKeyValue("SeparationOfDuties",
                                    updateMap.get(famTaskID));
                        }
                    }
                }
            }
            for (String oldID : updateMap.keySet()) {
                 if (map.containsKey(oldID)) {
                     TaskResourceSet resources = map.remove(oldID);
                     map.put(updateMap.get(oldID), resources);
                 }
            }
        }
    }


    public void clear() { _cache.clear(); }


    public void generateXML() {
        for (Map<String, TaskResourceSet> map : _cache.values()) {    // net in spec
            for (TaskResourceSet resources : map.values()) {          // task in spec
                resources.setTaskXML();
            }
        }
    }


    public Set<InvalidReference> getAllInvalidReferences() {
        Set<InvalidReference> invalids = new HashSet<InvalidReference>();
        for (String netID : _cache.keySet()) {
            Map<String, TaskResourceSet> map = _cache.get(netID);
            for (String taskID : map.keySet()) {
                TaskResourceSet resourceSet = map.get(taskID);
                for (InvalidReference invalid : resourceSet.getInvalidReferences()) {
                    invalid.setNetID(netID);
                    invalid.setTaskID(taskID);
                    invalids.add(invalid);
                }
            }
        }
        return invalids;
    }


    private Map<String, TaskResourceSet> getNetMap(String netID) {
        Map<String, TaskResourceSet> map = _cache.get(netID);
        if (map == null) {
            map = new HashMap<String, TaskResourceSet>();
            _cache.put(netID, map);
        }
        return map;
    }

}
