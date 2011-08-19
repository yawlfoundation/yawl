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

package org.yawlfoundation.yawl.logging;

import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.logging.table.YLogSpecification;
import org.yawlfoundation.yawl.schema.YDataSchemaCache;

import java.util.Hashtable;
import java.util.Map;

/**
 * A cache of foreign keys for use in event log tables.
 * @author Michael Adams
 * @date 16/06/11
 */
public class YEventKeyCache {

    protected final YDataSchemaCache dataSchema;

    // [service url or client name, YLogServiceID] - always active
    protected final Map<String, Long> services;

    // [datatype name, [datatype schema, dataTypeID]] - always active
    protected final Map<String, Map<String, Long>> dataDefn;

    // [specID, YLogSpecification] - removed when spec unloaded
    protected final Map<YSpecificationID, YLogSpecification> specEntries;

    // [specID, rootNetID] (rootNetID is a netID) - removed when spec unloaded
    protected final Map<YSpecificationID, Long> rootNets;

    // [specID, [netName, netID]] - removed when spec unloaded
    protected final Map<YSpecificationID, Map<String, Long>> nets;

    // [netID, [taskName, taskID]] - removed when spec unloaded (via netID relation)
    protected final Map<Long, Map<String, Long>> tasks;

    // [caseID, netInstanceID] - removed when case completes or cancelled
    protected final Map<YIdentifier, Long> netInstances;

    // [caseID, [taskID, taskInstanceID]] - removed when case completes or cancelled
    protected final Map<YIdentifier, Map<Long, Long>> taskInstances;


    protected YEventKeyCache() {
        dataSchema = new YDataSchemaCache();
        services = new Hashtable<String, Long>();
        rootNets = new Hashtable<YSpecificationID, Long>();
        nets = new Hashtable<YSpecificationID, Map<String, Long>>();
        tasks = new Hashtable<Long, Map<String, Long>>();
        specEntries = new Hashtable<YSpecificationID, YLogSpecification>();
        netInstances = new Hashtable<YIdentifier, Long>();
        taskInstances = new Hashtable<YIdentifier, Map<Long, Long>>();
        dataDefn = new Hashtable<String, Map<String, Long>>();
    }


    protected long getNetID(YSpecificationID specID, String netName) {
        Long id = getID(nets, specID, netName);
        return (id != null) ? id : -1;
    }

    protected long putNetID(YSpecificationID specID, String netName, long key) {
        Long id = putID(nets, specID, netName, key);
        return (id != null) ? id : -1;
    }


    protected long getTaskID(Long netID, String taskName) {
        Long id = getID(tasks, netID, taskName);
        return (id != null) ? id : -1;
    }

    protected long putTaskID(Long netID, String taskName, long key) {
        Long id = putID(tasks, netID, taskName, key);
        return (id != null) ? id : -1;
    }


    protected long getTaskInstanceID(YIdentifier caseID, Long taskID) {
        Long id = getID(taskInstances, caseID, taskID);
        return (id != null) ? id : -1;
    }

    protected long putTaskInstanceID(YIdentifier caseID, Long taskID, long key) {
        Long id = putID(taskInstances, caseID, taskID, key);
        return (id != null) ? id : -1;
    }


    protected long getDataTypeID(String name, String definition) {
        Long id = getID(dataDefn, name, definition);
        return (id != null) ? id : -1;
    }

    protected long putDataTypeID(String name, String definition, long key) {
        Long id = putID(dataDefn, name, definition, key);
        return (id != null) ? id : -1;
    }


    protected void removeCase(YIdentifier caseID) {
        for (YIdentifier descendant : caseID.getDescendants()) {  // includes parent
            netInstances.remove(descendant);
            taskInstances.remove(descendant);
        }
    }


    protected void removeSpecification(YSpecificationID specID) {
        dataSchema.remove(specID);
        specEntries.remove(specID);
        rootNets.remove(specID);
        Map<String, Long> netIDMap = nets.remove(specID);
        if (netIDMap != null) {
            for (Long netID : netIDMap.values()) {
                tasks.remove(netID);
            }
        }
    }


    /****************************************************************************/

    private <K, S, V> V getID(Map<K, Map<S, V>> map, K key, S subKey) {
        Map<S, V> subMap = map.get(key);
        return (subMap != null) ? subMap.get(subKey) : null;
    }


    private <K, S, V> V putID(Map<K, Map<S, V>> map, K key, S subKey, V value) {
        Map<S, V> subMap = map.get(key);
        if (subMap == null) {
            subMap = new Hashtable<S, V>();
            map.put(key, subMap);
        }
        return subMap.put(subKey, value);
    }

}
