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

package org.yawlfoundation.yawl.resourcing.codelets;

import org.jdom.Element;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.xml.datatype.DatatypeFactory;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Author: Michael Adams
 * Creation Date: 17/06/2008
 */
public abstract class AbstractCodelet {

    protected String _description ;                // what does it do?
    private WorkItemRecord _wir;
    private Element _inData;
    private List<YParameter> _inParams;
    private List<YParameter> _outParams;
    private Element _outData;

    protected AbstractCodelet() {}

    protected AbstractCodelet(String desc) { _description = desc; }

    public String getDescription() { return _description; }
    
    public void setDescription(String desc) { _description = desc; }

    public WorkItemRecord getWorkItem() { return _wir; }

    public void setWorkItem(WorkItemRecord wir) { _wir = wir; }


    protected void setInputs(Element inData, List<YParameter> inParams,
                             List<YParameter> outParams) {
        _inData = inData ;
        _inParams = inParams ;
        _outParams = outParams ;
    }

    protected Object getParameterValue(String varName) throws CodeletExecutionException {
        Object result ;
        YParameter param = getInputParameter(varName);
        String dataType = param.getDataTypeName();
        String value = getValue(varName);
        try {
            if (dataType.endsWith("boolean")) 
                result = value.equalsIgnoreCase("true");
            else if (dataType.endsWith("date"))
                result = new SimpleDateFormat().parse(value);    
            else if (dataType.endsWith("double"))
                result = new Double(value);
            else if (dataType.endsWith("integer"))
                result = new Integer(value);
            else if (dataType.endsWith("long"))
                result = new Long(value);
            else if (dataType.endsWith("duration"))
                result = DatatypeFactory.newInstance().newDuration(value);
            else
                result = value ;
        }
        catch (Exception e) {
            throw new CodeletExecutionException("Invalid value '" + value + "' for " +
                         "datatype '" + dataType + "' of parameter '" + varName + "'.");
        }
        return result ;
    }

    protected void setParameterValue(String varName, String value) {
        if (_outData == null) _outData = new Element("codelet_output") ;
        Element eParam = _outData.getChild(varName) ;
        if (eParam != null)
            eParam.setText(value);
        else {
            eParam = new Element(varName) ;
            eParam.setText(value);
            _outData.addContent(eParam);
        }
    }

    
    protected Element getOutputData() { return _outData; }


    private YParameter getInputParameter(String paramName)
                                                      throws CodeletExecutionException {
        return getParameter(paramName, _inParams) ;
    }


    private YParameter getOutputParameter(String paramName)
                                                      throws CodeletExecutionException {
        return getParameter(paramName, _outParams) ;
    }


    private YParameter getParameter(String paramName, List<YParameter> params)
                                                      throws CodeletExecutionException {
        YParameter result = null;
        if (params != null) {
            for (YParameter param : params) {
                if (param.getName().equals(paramName)) {
                    result = param;
                    break;
                }
            }
        }
        if (result != null)
            return result;
        else
            throw new CodeletExecutionException("Parameter '" + paramName +
                    "' is required by the specified codelet but could not be found in " +
                    "the workitem's parameters.");
    }


    private String getValue(String varName) throws CodeletExecutionException {
        String result = null ;
        if (_inData != null) {
            result = _inData.getChildText(varName);
        }
        if (result != null)
            return result;
        else
            throw new CodeletExecutionException("A value for parameter '" + varName +
                    "' is required by the specified codelet but could not be found in " +
                    "the workitem's data.");
    }

    public String getClassName() { return this.getClass().getSimpleName(); }
    

    public String toXML() {
        StringBuilder xml = new StringBuilder("<codelet>");
        xml.append(StringUtil.wrap(getClassName(), "name"))
           .append(StringUtil.wrap(JDOMUtil.encodeEscapes(_description), "description"))
           .append("</codelet>");
        return xml.toString();
    }


    /********************************************************************************/

    public void init() { }

    public void shutdown() { }

    public void resume() { }

    public void cancel() { }

    /*********************************************************************************/

    public abstract Element execute(Element inData,
                                    List<YParameter> inParams,
                                    List<YParameter> outParams)
                                    throws CodeletExecutionException;


}
