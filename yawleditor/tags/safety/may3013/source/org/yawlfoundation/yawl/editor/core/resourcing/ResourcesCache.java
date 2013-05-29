package org.yawlfoundation.yawl.editor.core.resourcing;

import org.yawlfoundation.yawl.elements.YAtomicTask;

import java.util.Hashtable;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 27/06/12
 */
public class ResourcesCache {

    Map<String, Map<String, TaskResources>> _cache;

    public ResourcesCache() {
        _cache = new Hashtable<String, Map<String, TaskResources>>();
    }


    public void add(String netID, String taskID, TaskResources resources) {
        if (! (netID == null || taskID == null || resources == null)) {
            getNetMap(netID).put(taskID, resources);
        }
    }


    public void add(TaskResources resources) {
        YAtomicTask task = resources.getTask();
        add(task.getNet().getID(), task.getID(), resources);
    }


    public TaskResources get(String netID, String taskID) {
        return (netID == null || taskID == null) ? null : getNetMap(netID).get(taskID);
    }


    public TaskResources remove(String netID, String taskID) {
        return (netID == null || taskID == null) ? null : getNetMap(netID).remove(taskID);
    }

    public void clear() { _cache.clear(); }


    public void primeTasks() {
        for (Map<String, TaskResources> map : _cache.values()) {
            for (TaskResources resources : map.values()) {
                resources.primeTask();
            }
        }
    }


    private Map<String, TaskResources> getNetMap(String netID) {
        Map<String, TaskResources> map = _cache.get(netID);
        if (map == null) {
            map = new Hashtable<String, TaskResources>();
            _cache.put(netID, map);
        }
        return map;
    }

}
