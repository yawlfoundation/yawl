/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */

package org.yawlfoundation.yawl.engine.interfce.interfaceB;

import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.TaskInformation;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;

import java.io.IOException;
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
        return _taskInfoCache.get(specID.getKey() + taskID);
    }


    public void setTaskInformation(YSpecificationID specID, String taskID,
                                   TaskInformation taskInfo) {
        _taskInfoCache.put(specID.getKey() + taskID, taskInfo);
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


    public void setSpecificationData(SpecificationData specData) {
        if (! _specDataCache.containsKey(specData.getID().getKey())) {
            _specDataCache.put(specData.getID().getKey(), specData);
        }
    }


    /**
     * Gets a data object describing the specification.  This is cached in
     * the worklist application to reduce unecessary communication
     * between messaging tiers, and to improve performance.
     * @param specID
     * @return the specification data
     */
    public SpecificationData getSpecificationData(YSpecificationID specID) throws IOException {
        return _specDataCache.get(specID.getKey());
    }

    public void unloadSpecificationData(YSpecificationID specID) throws IOException {
	      _specDataCache.remove(specID.getKey());
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
