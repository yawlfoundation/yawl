/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.worklist.model;


import java.io.IOException;
import java.util.*;

/**
 * 
 * @author Lachlan Aldred
 * Date: 27/01/2004
 * Time: 18:56:15
 * 
 */
public class WorklistModel {
    private Map _workItems = new HashMap();
    private Map _taskInformations = new HashMap();
    private Map _itemIDToDataMap = new HashMap();
    private Map _specificationData = new HashMap();



    public WorklistModel() {

    }


    public void updateWorkItems(List items) {
        for (int i = 0; i < items.size(); i++) {
            WorkItemRecord workItemRecord = (WorkItemRecord) items.get(i);
            _workItems.put(workItemRecord.getID(), workItemRecord);
        }
    }


    public WorkItemRecord getWorkItem(String workItemID) {
        return (WorkItemRecord) _workItems.get(workItemID);
    }


    public TaskInformation getTaskInformation(String specificationID, String taskID) {
        return (TaskInformation) _taskInformations.get(specificationID + taskID);
    }


    public void setTaskInformation(String specificationID, String taskID, TaskInformation taskInfo) {
        _taskInformations.put(specificationID + taskID, taskInfo);
    }


    public void setDataForWorkItemID(String workItemID, String data) {
        _itemIDToDataMap.put(workItemID, data);
    }


    public String getDataForWorkItemID(String workItemID) {
        return (String) _itemIDToDataMap.get(workItemID);
    }

    public void unsaveWorkItem(String workItemID) {
        _itemIDToDataMap.remove(workItemID);
    }


    /**
     * @param specData
     */
    public void setSpecificationData(SpecificationData specData) {
        if (!_specificationData.containsKey(specData.getID())) {
            _specificationData.put(specData.getID(), specData);
        }
    }


    /**
     * Gets a data object describing the specification.  This is cached in
     * the worklist application to reduce unecessary communication
     * between messaging teirs, and to improve performance.
     * @param specID
     * @return the specification data
     */
    public SpecificationData getSpecificationData(String specID) throws IOException {
        return (SpecificationData) _specificationData.get(specID);
    }

    public void unloadSpecification(String specID) throws IOException {
    	_specificationData.remove(specID);
    }	


    public void addWorkItem(WorkItemRecord itemRecord) {
        _workItems.put(itemRecord.getID(), itemRecord);
    }


    /**
     * Removes any cached copy of a work item from the custom yawl service.
     * @param workItemID the work item id.
     */
    public void removeRemotelyCachedWorkItem(String workItemID) {
        _workItems.remove(workItemID);
    }

    public String getDataRootElementName(String specificationID, String taskID, String sessionHandle) {
        SpecificationData sdata = (SpecificationData) _specificationData.get(specificationID);
        if (sdata.usesSimpleRootData()) {
            return "data";
        } else {
            TaskInformation taskInf = getTaskInformation(specificationID, taskID);
            return taskInf.getDecompositionID();
        }
    }
}
