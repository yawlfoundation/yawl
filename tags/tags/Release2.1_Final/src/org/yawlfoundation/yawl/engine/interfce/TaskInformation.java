/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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
