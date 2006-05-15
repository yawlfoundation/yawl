/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.worklist.model;


/**
 * 
 * @author Lachlan Aldred
 * Date: 26/02/2004
 * Time: 14:54:10
 * 
 */
public class TaskInformation {
    private YParametersSchema _paramSchema;
    private String _taskID;
    private String _specificationID;
    private String _taskDocumentation;
    private String _taskName;
    private String _decompositionID;


    public TaskInformation(YParametersSchema paramSchema, String taskID,
                           String specificationID, String taskName,
                           String taskDocumentation, String decompositionID) {
        this._paramSchema = paramSchema;
        this._taskID = taskID;
        this._taskName = taskName;
        this._specificationID = specificationID;
        this._taskDocumentation = taskDocumentation;
        this._decompositionID = decompositionID;
    }


    public YParametersSchema getParamSchema() {
        return _paramSchema;
    }


    public String getTaskID() {
        return _taskID;
    }


    public String getSpecificationID() {
        return _specificationID;
    }


    public String getTaskDocumentation() {
        return _taskDocumentation;
    }


    public String getTaskName() {
        return _taskName;
    }


    public String getDecompositionID() {
        return _decompositionID;
    }
}
