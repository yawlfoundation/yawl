/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */

package org.yawlfoundation.yawl.engine.interfce;

import org.yawlfoundation.yawl.engine.YSpecificationID;

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
 * @author Michael Adams (for v2.0 02/07/2008)
 * 
 */
public class WorklistModel {
    private Map<String, WorkItemRecord> _workItemCache =
            new HashMap<String, WorkItemRecord>();
    private Map<String, TaskInformation> _taskInfoCache =
            new HashMap<String, TaskInformation>();
    private Map<String, String> _itemIDToDataMap =
            new HashMap<String, String>();
    private Map<String, SpecificationData> _specDataCache =
            new HashMap<String, SpecificationData>();


    public WorklistModel() { }


    public void updateWorkItems(List<WorkItemRecord> items) {
        for (WorkItemRecord wir : items) {
            _workItemCache.put(wir.getID(), wir);
        }
    }


    public WorkItemRecord getWorkItem(String workItemID) {
        return _workItemCache.get(workItemID);
    }


    public TaskInformation getTaskInformation(YSpecificationID specID, String taskID) {
        return _taskInfoCache.get(specID.getSpecName() + specID.getVersionAsString() + taskID);
    }


    public void setTaskInformation(YSpecificationID specID, String taskID,
                                   TaskInformation taskInfo) {
        _taskInfoCache.put(specID.getSpecName() + specID.getVersionAsString() + taskID, taskInfo);
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
        if (! _specDataCache.containsKey(specData.getName() + specData.getSpecVersion())) {
            _specDataCache.put(specData.getName() + specData.getSpecVersion(), specData);
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
        return _specDataCache.get(specID.getSpecName() + specID.getVersionAsString());
    }

    public void unloadSpecification(YSpecificationID specID) throws IOException {
	      _specDataCache.remove(specID.getSpecName() + specID.getVersionAsString());
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

    public String getDataRootElementName(String specID, String taskID) {
        SpecificationData sdata = _specDataCache.get(specID + "0.1");
        if (sdata.usesSimpleRootData()) {
            return "data";
        } else {
            TaskInformation taskInf = getTaskInformation(new YSpecificationID(specID), taskID);
            return taskInf.getDecompositionID();
        }
    }
}
