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
