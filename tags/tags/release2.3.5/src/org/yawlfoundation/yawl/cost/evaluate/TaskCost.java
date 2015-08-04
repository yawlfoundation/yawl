package org.yawlfoundation.yawl.cost.evaluate;

/**
 * @author Michael Adams
 * @date 5/12/12
 */
public class TaskCost {

    private String _taskID;
    private String _resourceID;
    private double _cost;

    public TaskCost(String taskID, double cost) {
        _taskID = taskID;
        _cost = cost;
    }

    public String getTaskID() {
        return _taskID;
    }

    public void setTaskID(String taskID) {
        _taskID = taskID;
    }

    public String getResourceID() {
        return _resourceID;
    }

    public void setResourceID(String resourceID) {
        _resourceID = resourceID;
    }

    public double getCost() {
        return _cost;
    }

    public void setCost(double cost) {
        _cost = cost;
    }

}
