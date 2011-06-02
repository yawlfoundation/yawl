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

package org.yawlfoundation.yawl.resourcing.util;

import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.resourcing.ResourceMap;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;


/**
 * @author Michael Adams 
 *         Date: 01/09/2008
 */
public class ResourceMapCache extends Hashtable<String, VersionToMapTable> {

    public ResourceMapCache() {}


    public VersionToMapTable add(YSpecificationID specID, String taskID,
                                                  ResourceMap map) {
        VersionToMapTable entry = this.get(specID.getKey());
        if (entry != null) {
            entry.add(specID, taskID, map) ;
        }
        else {
            entry = new VersionToMapTable(specID, taskID, map);
            this.put(specID.getKey(), entry);
        }
        return entry;
    }


    public ResourceMap get(YSpecificationID specID, String taskID) {
        VersionToMapTable entry = this.get(specID.getKey());
        if (entry != null) {
            Hashtable<String, ResourceMap> table = entry.get(specID.getVersionAsString());
            if (table != null) {
                return table.get(taskID);
            }
        }
        return null;
    }


    public VersionToMapTable remove(YSpecificationID specID) {
        return this.remove(specID.getKey());
    }


    public boolean contains(YSpecificationID specID) {
        VersionToMapTable entry = this.get(specID.getKey());
        return entry.contains(specID.getVersionAsString());
    }

    public Set<ResourceMap> getAll() {
        Set<ResourceMap> result = new HashSet<ResourceMap>();
        for (VersionToMapTable entry : this.values()) {
            for (Hashtable<String, ResourceMap> table : entry.values()) {
                result.addAll(table.values());
            }
        }
        return result;
    }

    public Set<ResourceMap> getAll(String key, String taskID) {
        Set<ResourceMap> result = new HashSet<ResourceMap>();
        VersionToMapTable entry = this.get(key);
        if (entry != null) {
            for (Hashtable<String, ResourceMap> table : entry.values()) {
                for (String id : table.keySet()) {
                    if (id.equals(taskID)) {
                        result.add(table.get(id));
                    }
                }
            }
        }
        return result;
    }


}


/********************************************************************************/

class VersionToMapTable extends Hashtable<String, Hashtable<String, ResourceMap>> {

    // Constructors //
    public VersionToMapTable(YSpecificationID specID, String taskID, ResourceMap map) {
        super() ;
        this.add(specID, taskID, map);
    }


    public void add(YSpecificationID specID, String taskID, ResourceMap map) {
        Hashtable<String, ResourceMap> table = this.get(specID.getVersionAsString());
        if (table != null) {
            table.put(taskID, map);
        }
        else {
            table = new Hashtable<String, ResourceMap>();
            table.put(taskID, map);
            this.put(specID.getVersionAsString(), table);
        }
    }
}





