/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.engine.interfce;

import org.yawlfoundation.yawl.elements.data.YParameter;

import java.util.*;

/**
 * 
 * @author Lachlan Aldred
 * Date: 5/11/2003
 * Time: 12:18:37
 *
 * updated for v2.0 by Michael Adams
 * last date: 14/02/2008
 * 
 */
public class YParametersSchema {
    private Map<String, YParameter> _inputParams = new HashMap<String, YParameter>();
    private Map<String, YParameter> _outputParams = new HashMap<String, YParameter>();
    private String _formalInputParam;


    public List<YParameter> getInputParams() {
        List<YParameter> list = new ArrayList<YParameter>(_inputParams.values());
        Collections.sort(list);
        return list;
    }


    public List<YParameter> getOutputParams() {
        List<YParameter> list = new ArrayList<YParameter>(_outputParams.values());
        Collections.sort(list);
        return list;
    }


    public List<YParameter> getInputOnlyParams() {
        List<YParameter> inputOnlyList = new ArrayList<YParameter>();
        for (YParameter param : getInputParams()) {
             if (! _outputParams.containsKey(param.getName())) {
                inputOnlyList.add(param);
            }
        }
        return inputOnlyList;
    }


    public List<YParameter> getOutputOnlyParams() {
        List<YParameter> outputOnlyList = new ArrayList<YParameter>();
        for (YParameter param : getOutputParams()) {
             if (! _inputParams.containsKey(param.getName())) {
                outputOnlyList.add(param);
            }
        }
        return outputOnlyList;
    }


    public List<YParameter> getInputOutputParams() {
        List<YParameter> inputOutputList = new ArrayList<YParameter>();
        for (YParameter param : getInputParams()) {
             if (_outputParams.containsKey(param.getName())) {
                inputOutputList.add(param);
            }
        }
        return inputOutputList;
    }


    public List<YParameter> getCombinedParams() {
        List<YParameter> result = getInputParams();              // includes I&O params
        List<YParameter> outputOnlyList = getOutputOnlyParams();

        // combine and return
        result.addAll(getOutputOnlyParams());
        Collections.sort(result);
        return result;
    }


    public void setInputParam(YParameter parameter) {
        if (YParameter.getTypeForInput().equals(parameter.getDirection()))
            _inputParams.put(parameter.getName(), parameter);
        else
            throw new IllegalArgumentException();
    }


    public void setOutputParam(YParameter parameter) {
        if (YParameter.getTypeForOutput().equals(parameter.getDirection()))
            _outputParams.put(parameter.getName(), parameter);
        else
            throw new IllegalArgumentException();
    }


    public String toString() {
        StringBuilder result = new StringBuilder();
        for (YParameter parameter : _inputParams.values())
            result.append(parameter.toSummaryXML());
        for (YParameter parameter : _outputParams.values())
            result.append(parameter.toSummaryXML());
       
        return result.toString();
    }


    public void setFormalInputParam(String formalInputParam) {
        _formalInputParam = formalInputParam;
    }


    public YParameter getFormalInputParam() {
        return _formalInputParam != null ? _inputParams.get(_formalInputParam) : null;
    }
}
