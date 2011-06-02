/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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
 * The base class of all codelets.
 *
 * @author Michael Adams
 * @date 17/06/2008
 */
public abstract class AbstractCodelet {

    protected String _description ;                          // what does it do?
    private WorkItemRecord _wir;                             // the 'calling' work item
    private Element _inData;                                 // the item's starting data
    private List<YParameter> _inParams;                      // its input parameters
    private List<YParameter> _outParams;                     // its output parameters
    private Element _outData;                                // the codelet's result
    private boolean _persist = true;                         // persists it by default

    protected static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema";

    protected AbstractCodelet() {}

    protected AbstractCodelet(String desc) { _description = desc; }

    public String getDescription() { return _description; }
    
    public void setDescription(String desc) { _description = desc; }

    public WorkItemRecord getWorkItem() { return _wir; }

    public void setWorkItem(WorkItemRecord wir) { _wir = wir; }

    public boolean getPersist() { return _persist; }

    protected void setPersist(boolean persist) { _persist = persist; }


    protected void setInputs(Element inData, List<YParameter> inParams,
                             List<YParameter> outParams) {
        _inData = inData ;
        _inParams = inParams ;
        _outParams = outParams ;
    }


    /**
     * Converts the string representation of a parameter value to its specified data
     * type, for the most frequent data types. Implementers may override this method to
     * handle other data types if required.
     * @param varName the name of the parameter
     * @return the appropriately typed value
     * @throws CodeletExecutionException if the string value can't be converted to the
     * specified type.
     */
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


    /**
     * Adds a new value to an output parameter, and creates the parameter if it doesn't
     * already exist
     * @param varName the name of the parameter
     * @param value the value to assign to it
     */
    protected void setParameterValue(String varName, String value) {
        if (_outData == null) _outData = new Element("codelet_output");
        Element content =
              (Element) JDOMUtil.stringToElement(StringUtil.wrap(value, varName)).clone();
        _outData.removeChild(varName);
        _outData.addContent(content);
    }

    
    protected Element getOutputData() { return _outData; }


    protected YParameter getInputParameter(String paramName)
            throws CodeletExecutionException {
        return getParameter(paramName, _inParams) ;
    }


    protected YParameter getOutputParameter(String paramName)
            throws CodeletExecutionException {
        return getParameter(paramName, _outParams) ;
    }


    /**
     * Extracts a parameter from a List of parameters
     * @param paramName the parameter to extract
     * @param params the list of parameters containing the parameter
     * @return the specified parameters
     * @throws CodeletExecutionException if the requested parameter isn't in the list
     */
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


    /**
     * Gets the value of a parameter
     * @param varName the name of the parameter
     * @return its value
     * @throws CodeletExecutionException if the parameter doesn't exist in the input
     * data
     */
    protected String getValue(String varName) throws CodeletExecutionException {
        String result = null ;
        if (_inData != null) {
            Element varElem = _inData.getChild(varName);          // may be complex type
            if (varElem != null) {
               result = StringUtil.unwrap(JDOMUtil.elementToString(varElem));
            }
        }
        if (result != null)
            return result;
        else
            throw new CodeletExecutionException("A value for parameter '" + varName +
                    "' is required by the specified codelet but could not be found in " +
                    "the workitem's data.");
    }


    public String getClassName() { return this.getClass().getSimpleName(); }


    public String getCanonicalClassName() {
        return this.getClass().getCanonicalName();
    }

    public String toXML() {
        StringBuilder xml = new StringBuilder("<codelet>");
        xml.append(StringUtil.wrap(getClassName(), "name"))
           .append(StringUtil.wrap(getCanonicalClassName(), "canonicalname"))     
           .append(StringUtil.wrap(JDOMUtil.encodeEscapes(_description), "description"))
           .append(getRequiredParamsToXML())
           .append("</codelet>");
        return xml.toString();
    }

    public String getRequiredParamsToXML() {
        StringBuilder xml = new StringBuilder("<requiredparams>");
        List<YParameter> requiredParams = getRequiredParams();
        if (requiredParams != null) {
            for (YParameter param : requiredParams) {
                String paramType = param.getParamType();
                xml.append("<").append(paramType).append(">")
                   .append(StringUtil.wrap(param.getPreferredName(), "name"))
                   .append(StringUtil.wrap(param.getDataTypeName(), "datatype"))
                   .append(StringUtil.wrap(param.getDataTypeNameSpace(), "namespace"))
                   .append(StringUtil.wrap(param.getDocumentation(), "documentation"))
                   .append("</").append(paramType).append(">");
            }
        }
        xml.append("</requiredparams>");
        return xml.toString();
    }


    /********************************************************************************/

    /**
     * This method is called when a codelet first starts. Override to include any
     * initialisations required.
     */
    public void init() { }

    /**
     * This method is called when the server is shutting down. Override to include any
     * state saving required.
     */
    public void shutdown() { }

    /**
     * This method is called when the codelet is resumed after a server shutdown.
     * Override to include any state restoring required.
     */
    public void resume() { }

    /**
     * This method is called when the work item running this codelet is cancelled.
     * Override to cancel the codelet as required.
     */
    public void cancel() { }

    /**
     * This method is called when an external entity (such as the YAWL editor) requests
     * the list of parameters required by the codelet. Override to populate the list.
     * Each parameter should include a name, data type and description as a minimum.
     * @return the List of parameters required by the codelet.
     */
    public List<YParameter> getRequiredParams() { return null; }

    /*********************************************************************************/

    /**
     * This method must be implemented to do the work of the codelet.
     * @param inData the work item's input data
     * @param inParams the work item's input parameters
     * @param outParams the work item's output parameters
     * @return a JDOM Element containing the output data generated by this method
     * @throws CodeletExecutionException if there's any problem getting the required
     * input data, composing the required output data, or performing the execution.
     */
    public abstract Element execute(Element inData,
                                    List<YParameter> inParams,
                                    List<YParameter> outParams)
                                    throws CodeletExecutionException;


}
