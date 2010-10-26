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

package org.yawlfoundation.yawl.resourcing.resource;

public class NonHumanResourceCategory {

    private long _key;
    private String _category;
    private long _parentKey;

    public NonHumanResourceCategory() { }

    public NonHumanResourceCategory(String category, long parentKey) {
         _category = category;
        _parentKey = parentKey;
    }

    public long getKey() { return _key; }

    public String getCategory() { return _category; }

    public long getParentKey() { return _parentKey; }
}