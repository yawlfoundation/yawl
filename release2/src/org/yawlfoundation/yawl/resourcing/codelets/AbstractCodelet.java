package org.yawlfoundation.yawl.resourcing.codelets;

import org.jdom.Element;
import org.yawlfoundation.yawl.elements.data.YParameter;

import javax.xml.datatype.DatatypeFactory;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

/**
 * Author: Michael Adams
 * Creation Date: 17/06/2008
 */
public abstract class AbstractCodelet {

    protected String _description ;                // what does it do?
    private Element _inData;
    private List _inParams;
    private List _outParams;
    private Element _outData;

    protected AbstractCodelet() {}

    protected AbstractCodelet(String desc) { _description = desc; }

    public String getDescription() { return _description; }
    
    public void setDescription(String desc) { _description = desc; }

    protected void setInputs(Element inData, List inParams, List outParams) {
        _inData = inData ;
        _inParams = inParams ;
        _outParams = outParams ;
    }

    protected Object getParameterValue(String varName) throws CodeletExecutionException {
        Object result = null ;
        YParameter param = getInputParameter(varName);
        String dataType = param.getDataTypeName();
        String value = getValue(varName);
        try {
            if (dataType.endsWith("boolean")) 
                result = new Boolean(value);
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


    private YParameter getParameter(String paramName, List params)
                                                      throws CodeletExecutionException {
        YParameter result = null;
        if (params != null) {
            Iterator itr = params.iterator();
            while (itr.hasNext()) {
                YParameter param = (YParameter) itr.next();
                if (param.getName().equals(paramName)) {
                    result = param ;
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


    /********************************************************************************/

    public abstract Element execute(Element inData, List inParams, List outParams)
                                                 throws CodeletExecutionException;

}
