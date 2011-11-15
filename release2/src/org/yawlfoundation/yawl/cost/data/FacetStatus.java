/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.cost.data;

/**
* @author Michael Adams
* @date 25/10/11
*/
public enum FacetStatus {
    nil,                         // not specified
    assigned,                    // for a resource, period between offered and allocated
    allocated,                   // for a resource, period between allocated and started
    busy,                        // for a resource, period between started and completed
    inactive,                    // for a task, period between enabled and executing
    active,                      // for a task, period between executing and completed...
                                 // ...less any time spent suspended
    suspended                    // for a task, period spent suspended
}
