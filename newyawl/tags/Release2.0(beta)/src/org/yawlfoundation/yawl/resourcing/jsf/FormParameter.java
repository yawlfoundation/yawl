package org.yawlfoundation.yawl.resourcing.jsf;

import org.yawlfoundation.yawl.elements.data.YParameter;

import java.io.Serializable;

/**
 *  Adds a few extra members to a YParameter object, so it ca be used to populate
 *  JSF pages.
 *
 *  @author: Michael Adams
 *  Date: 9/01/2008
 */
public class FormParameter extends YParameter implements Serializable {

    private String _value ;
    private boolean _inputOnly ;
    private boolean _required ;

    public FormParameter() {}

    // Constructor - casts a YParameter UP
    public FormParameter(YParameter param) {
        super(param.getParentDecomposition(), param.getParamType()) ;
        setInitialValue(param.getInitialValue());
        setDataTypeAndName(param.getDataTypeName(), param.getName(),
                           param.getDataTypeNameSpace());
        setDocumentation(param.getDocumentation());
        setOrdering(param.getOrdering());
        setElementName(param.getElementName());
        setAttributes(param.getAttributes());
    }

    public String getValue() { return _value; }

    public void setValue(String value) { _value = value; }

    public boolean isInputOnly() { return _inputOnly; }

    public void setInputOnly(boolean inputOnly) { _inputOnly = inputOnly; }

    public boolean isRequired() { return _required; }

    public void setRequired(boolean required) { _required = required; }
}
