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

package org.yawlfoundation.yawl.engine.interfce.interfaceB;

import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.engine.interfce.TaskInformation;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Lachlan Aldred
 * Date: 27/01/2004
 * Time: 18:56:15
 *
 * @author Michael Adams (renamed/refactored/relocated for v2.0 07/2008 & 04/2009)
 * 
 */
public class IBControllerCache {
    private Map<String, WorkItemRecord> _workItemCache =
            new HashMap<String, WorkItemRecord>();
    private Map<String, TaskInformation> _taskInfoCache =
            new HashMap<String, TaskInformation>();
    private Map<String, String> _itemIDToDataMap =
            new HashMap<String, String>();
    private Map<String, SpecificationData> _specDataCache =
            new HashMap<String, SpecificationData>();


    public IBControllerCache() { }


    public void updateWorkItems(List<WorkItemRecord> items) {
        for (WorkItemRecord wir : items) {
            _workItemCache.put(wir.getID(), wir);
        }
    }


    public WorkItemRecord getWorkItem(String workItemID) {
        return _workItemCache.get(workItemID);
    }


    public TaskInformation getTaskInformation(YSpecificationID specID, String taskID) {
        return _taskInfoCache.get(specID.toKeyString() + ":" + taskID);
    }


    public void setTaskInformation(YSpecificationID specID, String taskID,
                                   TaskInformation taskInfo) {
        _taskInfoCache.put(specID.toKeyString() + ":" + taskID, taskInfo);
    }


    public void unloadTaskInformation(YSpecificationID specID) {
        String specKey = specID.toKeyString() + ":";
        List<String> toRemove = new ArrayList<String>();
        for (String key : _taskInfoCache.keySet()) {
            if (key.startsWith(specKey)) {
                toRemove.add(key);            // avoid concurrency exceptions
            }
        }
        for (String key : toRemove) {
            _taskInfoCache.remove(key);
        }
    }


    public void setDataForWorkItemID(String workItemID, String data) {
        _itemIDToDataMap.put(workItemID, data);
    }


    public String getDataForWorkItemID(String workItemID) {
        return _itemIDToDataMap.get(workItemID);
    }


    public void unsaveWorkItem(String workItemID) {
        _itemIDToDataMap.remove(workItemID);
    }


    public void addSpecificationData(SpecificationData specData) {
        String key = specData.getID().toKeyString();
        if (! _specDataCache.containsKey(key)) {
            _specDataCache.put(key, specData);
        }
    }


    /**
     * Gets a data object describing the specification.  This is cached in
     * the worklist application to reduce unnecessary communication
     * between messaging tiers, and to improve performance.
     * @param specID
     * @return the specification data
     */
    public SpecificationData getSpecificationData(YSpecificationID specID) throws IOException {
        return _specDataCache.get(specID.toKeyString());
    }

    public void unloadSpecificationData(YSpecificationID specID) throws IOException {
	      _specDataCache.remove(specID.toKeyString());
        unloadTaskInformation(specID);
    }


    public void addWorkItem(WorkItemRecord itemRecord) {
        _workItemCache.put(itemRecord.getID(), itemRecord);
    }


    /**
     * Removes any cached copy of a work item from the custom yawl service.
     * @param workItemID the work item id.
     */
    public void removeRemotelyCachedWorkItem(String workItemID) {
        _workItemCache.remove(workItemID);
    }

}
