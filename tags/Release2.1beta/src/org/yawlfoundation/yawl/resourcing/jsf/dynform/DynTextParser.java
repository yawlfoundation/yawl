package org.yawlfoundation.yawl.resourcing.jsf.dynform;

import org.jdom.Element;
import org.yawlfoundation.yawl.util.YPredicateParser;
import org.yawlfoundation.yawl.util.JDOMUtil;

/**
 * Author: Michael Adams
 * Creation Date: 1/04/2010
 */
public class DynTextParser extends YPredicateParser {

    Element _data;

    public DynTextParser(Element data) {
        super();
        _data = data;
    }

    public DynTextParser(String data) {
        this(JDOMUtil.stringToElement(data));
    }


    protected String valueOf(String s) {
        return evaluateQuery(s, _data);
    }
}
