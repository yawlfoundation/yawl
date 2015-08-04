/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.logging;

import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.util.YPredicateParser;

/**
 * Author: Michael Adams
 * Creation Date: 1/03/2010
 */
public class YLogPredicateParameterParser extends YPredicateParser {

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
        else if (s.equals("${parameter:usage}")) {
            s = _param.getDirection();
        }
        else if (s.equals("${parameter:ordering}")) {
            s = String.valueOf(_param.getOrdering());
        }
        else if (s.equals("${parameter:decomposition}")) {
            YDecomposition decomposition = _param.getParentDecomposition();
            s = (decomposition != null) ? decomposition.getName() : "n/a";
        }
        else if (s.equals("${parameter:initialvalue}")) {
            String value = _param.getInitialValue();
            s = (value != null) ? value : "n/a" ;
        }
        else if (s.equals("${parameter:defaultvalue}")) {
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
