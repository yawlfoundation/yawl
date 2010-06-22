package org.yawlfoundation.yawl.logging;

import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.util.YPredicateParser;

/**
 * Author: Michael Adams
 * Creation Date: 1/03/2010
 */
public class YLogPredicateDecompositionParser extends YPredicateParser {

    private YDecomposition _decomp;

    public YLogPredicateDecompositionParser(YDecomposition decomp) {
        super();
        _decomp = decomp;
    }

    protected String valueOf(String s) {
        if (s.equals("${decomp:name}")) {
            s = _decomp.getName();
        }
        else if (s.equals("${decomp:spec:name}")) {
            s = _decomp.getSpecification().getName();
        }
        else if (s.equals("${decomp:inputs}")) {
            s = namesToCSV(_decomp.getInputParameterNames());
        }
        else if (s.equals("${decomp:outputs}")) {
            s = namesToCSV(_decomp.getOutputParameterNames());           
        }
        else if (s.equals("${decomp:doco}")) {
            s = _decomp.getDocumentation();
        }
        else if (s.startsWith("${decomp:attribute:")) {
            String value = getAttributeValue(_decomp.getAttributes(), s);
            s = (value != null) ? value : "n/a";
        }
        else {
            s = super.valueOf(s);
        }
        return s;
    }

}
