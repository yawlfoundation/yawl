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

    protected String valueOf(String predicate) {
        String resolved = "n/a";
        if (predicate.equals("${decomp:name}")) {
            resolved = _decomp.getID();
        }
        else if (predicate.equals("${decomp:spec:name}")) {
            resolved = _decomp.getSpecification().getName();
        }
        else if (predicate.equals("${decomp:inputs}")) {
            resolved = namesToCSV(_decomp.getInputParameterNames());
        }
        else if (predicate.equals("${decomp:outputs}")) {
            resolved = namesToCSV(_decomp.getOutputParameterNames());
        }
        else if (predicate.startsWith("${decomp:attribute:")) {
            resolved = getAttributeValue(_decomp.getAttributes(), predicate);
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
