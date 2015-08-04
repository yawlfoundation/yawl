/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package au.edu.qut.yawl.worklist.model;

import au.edu.qut.yawl.elements.data.YParameter;

import java.util.*;

/**
 * 
 * @author Lachlan Aldred
 * Date: 5/11/2003
 * Time: 12:18:37
 * 
 */
public class YParametersSchema {
    private Map _inputParams = new HashMap();
    private Map _outputParams = new HashMap();
    private String _formalInputParam;


    public List getInputParams() {
        List r = new ArrayList(_inputParams.values());
        Collections.sort(r);
        return r;
    }


    public List getOutputParams() {
        List r = new ArrayList(_outputParams.values());
        Collections.sort(r);
        return r;
    }


    public void setInputParam(YParameter parameter) {
        if (!YParameter.getTypeForInput().
                equals(parameter.getDirection())) {
            throw new IllegalArgumentException();
        }
        _inputParams.put(parameter.getName(), parameter);
    }


    public void setOutputParam(YParameter parameter) {
        if (!YParameter.getTypeForOutput().
                equals(parameter.getDirection())) {
            throw new IllegalArgumentException();
        }
        _outputParams.put(parameter.getName(), parameter);
    }


    public String toString() {
        StringBuffer result = new StringBuffer();
        for (Iterator iterator = _inputParams.values().iterator(); iterator.hasNext();) {
            YParameter parameter = (YParameter) iterator.next();
            result.append(parameter.toSummaryXML());
        }
        for (Iterator iterator = _outputParams.values().iterator(); iterator.hasNext();) {
            YParameter parameter = (YParameter) iterator.next();
            result.append(parameter.toSummaryXML());
        }
        return result.toString();
    }


    public void setFormalInputParam(String formalInputParam) {
        _formalInputParam = formalInputParam;
    }


    public YParameter getFormalInputParam() {
        return _formalInputParam != null ?
                (YParameter) _inputParams.get(_formalInputParam) : null;
    }
}
