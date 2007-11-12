/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.swingWorklist.util;

import au.edu.qut.yawl.worklist.model.YParametersSchema;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Lachlan Aldred
 * Date: 5/11/2003
 * Time: 11:46:06
 * 
 */
public class ParamsDefinitions {
    Map _nameNDecompositionIDMapsToParameterTuple = new HashMap();


    public YParametersSchema getParamsForTask(String taskID) {
        return (YParametersSchema) _nameNDecompositionIDMapsToParameterTuple.get(taskID);
    }


    public void setParamsForTask(String taskID, YParametersSchema parameterTuple) {
        _nameNDecompositionIDMapsToParameterTuple.put(taskID, parameterTuple);
    }
}
