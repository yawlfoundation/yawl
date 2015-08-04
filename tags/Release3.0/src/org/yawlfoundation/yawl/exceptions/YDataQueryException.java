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

package org.yawlfoundation.yawl.exceptions;

import org.jdom2.Element;

/**
 * 
 * @author Lachlan Aldred
 * Date: 1/09/2005
 * Time: 08:39:16
 * 
 */
public class YDataQueryException extends YDataStateException {


    public YDataQueryException(String queryString, Element data, String source, String message) {
        super(queryString, data, null, null, null, source, message);
    }

    public String getMessage() {
        String msg = "The MI data accessing query (" + getQueryString() + ") " +
                "for the task (" + getSource() + ") was applied over some data. " +
                "It failed to execute as expected";
        if (super.getMessage() != null) msg += ": " + super.getMessage();
        return msg;
    }


    public String getQueryString() {
        return _queryString;
    }

    public Element getData() {
        return _queriedData;
    }
}
