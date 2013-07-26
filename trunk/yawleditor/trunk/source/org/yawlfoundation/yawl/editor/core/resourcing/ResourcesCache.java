package org.yawlfoundation.yawl.editor.core.resourcing;

import org.yawlfoundation.yawl.editor.core.resourcing.validation.InvalidReference;
import org.yawlfoundation.yawl.elements.YTask;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * Caches all resource references for each task of the loaded specification
 * @author Michael Adams
 * @date 27/06/12
 */
public class ResourcesCache {

    // [netID, [taskID, set of task resources]]
    Map<String, Map<String, TaskResourceSet>> _cache;

    public ResourcesCache() {
        _cache = new Hashtable<String, Map<String, TaskResourceSet>>();
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

    public void clear() { _cache.clear(); }


    public void generateXML() {
        for (Map<String, TaskResourceSet> map : _cache.values()) {    // net in spec
            for (TaskResourceSet resources : map.values()) {          // task in spec
                resources.finaliseUpdate();
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
            map = new Hashtable<String, TaskResourceSet>();
            _cache.put(netID, map);
        }
        return map;
    }

}
