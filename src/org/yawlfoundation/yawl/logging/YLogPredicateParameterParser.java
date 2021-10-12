/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

    protected String valueOf(String predicate) {
        String resolved = "n/a";
        if (predicate.equals("${parameter:name}")) {
            resolved = _param.getPreferredName();
        }
        else if (predicate.equals("${parameter:datatype}")) {
            resolved = _param.getDataTypeName();
        }
        else if (predicate.equals("${parameter:namespace}")) {
            resolved = _param.getDataTypeNameSpace();
        }
        else if (predicate.equals("${parameter:doco}")) {
            resolved = _param.getDocumentation();
        }
        else if (predicate.equals("${parameter:usage}")) {
            resolved = _param.getDirection();
        }
        else if (predicate.equals("${parameter:ordering}")) {
            resolved = String.valueOf(_param.getOrdering());
        }
        else if (predicate.equals("${parameter:decomposition}")) {
            YDecomposition decomposition = _param.getParentDecomposition();
            if (decomposition != null) {
                resolved = decomposition.getID();
            }
        }
        else if (predicate.equals("${parameter:initialvalue}")) {
            resolved = _param.getInitialValue();
        }
        else if (predicate.equals("${parameter:defaultvalue}")) {
            resolved = _param.getDefaultValue();
        }
        else if (predicate.startsWith("${parameter:attribute:")) {
            resolved = getAttributeValue(_param.getAttributes(), predicate);
        }
        else {
            resolved = super.valueOf(predicate);
        }
        if (resolved == null || "null".equals(resolved) || predicate.equals(resolved)) {
            resolved = "n/a";
        }
        return resolved;
    }

}
