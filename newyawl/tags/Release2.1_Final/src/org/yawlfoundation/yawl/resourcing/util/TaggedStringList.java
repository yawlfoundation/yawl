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

package org.yawlfoundation.yawl.resourcing.util;

import org.yawlfoundation.yawl.util.StringUtil;

import java.util.ArrayList;

/**
 * Provides the storage of multiple String values for a single (tag) attribute
 *
 * @author Michael Adams
 * Date: 05/03/2008
 */

public class TaggedStringList extends ArrayList<String> {

    private String _tag ;

    // Constructors //
    public TaggedStringList(String tag) {
        super() ;
        _tag = tag ;

    }

    public TaggedStringList(String tag, String value) {
        this(tag);
        add(value);
    }

    public String getTag() { return _tag ; }

    public String toXML() {
        StringBuilder result = new StringBuilder() ;
        for (String value : this)
            result.append(StringUtil.wrap(value, _tag));
        return result.toString() ;
    }
}
