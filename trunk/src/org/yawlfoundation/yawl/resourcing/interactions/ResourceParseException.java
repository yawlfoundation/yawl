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

package org.yawlfoundation.yawl.resourcing.interactions;


/**
 *  ResourceParseException Class.
 *
 *  An ResourceParseException is thrown when an attempt is made to parse
 *  the resourcing specification for a task and the spec does not match the
 *  expected content.
 *
 *  @author Michael Adams
 *  v2.0, 05/02/2008
 */

public class ResourceParseException extends Exception {
    
    public ResourceParseException() {
        super();
    }

    public ResourceParseException(String message) {
        super(message);
    }

}