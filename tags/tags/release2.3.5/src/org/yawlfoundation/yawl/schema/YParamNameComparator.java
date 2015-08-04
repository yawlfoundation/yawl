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

package org.yawlfoundation.yawl.schema;

import org.yawlfoundation.yawl.elements.data.YParameter;

import java.util.Comparator;

/**
 * Allows YParameters to be sorted on name for schema validation. Used by
 * YDataValidator.validate()
 *
 * Author: Michael Adams
 * Creation Date: 07/05/2008
 */
public class YParamNameComparator implements Comparator {

	public int compare(Object obj1, Object obj2)	{

        // if one object is null, ignore it and return the other as having precedence
        if (obj1 == null) return -1;
        if (obj2 == null) return 1;

        String name1 = ((YParameter) obj1).getName();
        String name2 = ((YParameter) obj2).getName();

        // compare id strings
        return name1.compareTo(name2);
	}

}