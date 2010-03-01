package org.yawlfoundation.yawl.logging;

import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.data.YParameter;

/**
 * Author: Michael Adams
 * Creation Date: 1/03/2010
 */
public class YLogPredicateParameterParser extends YLogPredicateParser {

    private YParameter _param;

    public YLogPredicateParameterParser(YParameter param) {
        super();
        _param = param;
    }

    protected String valueOf(String s) {
        if (s.equals("${parameter:name}")) {
            s = _param.getPreferredName();
        }
        else if (s.equals("${parameter:datatype}")) {
            s = _param.getDataTypeName();
        }
        else if (s.equals("${parameter:namespace}")) {
            s = _param.getDataTypeNameSpace();
        }
        else if (s.equals("${parameter:doco}")) {
            s = _param.getDocumentation();
        }
        else if (s.equals("${parameter:type}")) {
            s = _param.getDirection();
        }
        else if (s.equals("${parameter:ordering}")) {
            s = String.valueOf(_param.getOrdering());
        }
        else if (s.equals("${parameter:decomposition}")) {
            YDecomposition decomposition = _param.getParentDecomposition();
            s = (decomposition != null) ? decomposition.getName() : "n/a";
        }
        else if (s.equals("${parameter:initialValue}")) {
            String value = _param.getInitialValue();
            s = (value != null) ? value : "n/a" ;
        }
        else if (s.equals("${parameter:defaultValue}")) {
            String value = _param.getDefaultValue();
            s = (value != null) ? value : "n/a" ;
        }
        else if (s.startsWith("${parameter:attribute:")) {
            String value = getAttributeValue(_param.getAttributes(), s);
            s = (value != null) ? value : "n/a";
        }
        else {
            s = super.valueOf(s);
        }
        return s;        
    }

}
