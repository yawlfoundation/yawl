/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */

package org.yawlfoundation.yawl.engine.interfce;

import org.yawlfoundation.yawl.elements.YAttributeMap;
import org.yawlfoundation.yawl.engine.YSpecificationID;

import java.util.Hashtable;

/**
 * 
 * @author Lachlan Aldred
 * Date: 26/02/2004
 * Time: 14:54:10
 *
 * @author Michael Adams - reworked for v2.1 Apr-09
 */
public class TaskInformation {
    private YParametersSchema _paramSchema;
    private String _taskID;
    private YSpecificationID _specificationID;
    private String _taskDocumentation;
    private String _taskName;
    private String _decompositionID;
    private YAttributeMap _attributes;

    public TaskInformation(YParametersSchema paramSchema, String taskID,
                           YSpecificationID specificationID, String taskName,
                           String taskDocumentation, String decompositionID) {
        _paramSchema = paramSchema;
        _taskID = taskID;
        _taskName = taskName;
        _specificationID = specificationID;
        _taskDocumentation = taskDocumentation;
        _decompositionID = decompositionID;
	      _attributes = new YAttributeMap();
    }


    public void setAttributes(Hashtable<String, String> map) {
	      _attributes.set(map);
    }

    public void addAttribute(String key, String value) {
	      _attributes.put(key,value);
    }

    public Hashtable<String, String> getAttributes() {
	      return _attributes;
    }

    public String getAttribute(String key) {
	     return _attributes.get(key);
    }

    public YParametersSchema getParamSchema() {
        return _paramSchema;
    }

    public String getTaskID() {
        return _taskID;
    }

    public YSpecificationID getSpecificationID() {
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
