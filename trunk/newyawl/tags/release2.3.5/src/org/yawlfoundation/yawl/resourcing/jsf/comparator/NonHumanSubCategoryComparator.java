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

package org.yawlfoundation.yawl.resourcing.jsf.comparator;

import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanSubCategory;

import java.util.Comparator;

/**
 * Allows NonHumanSubCategory objects to be sorted on name for display in
 * 'Asset Mgt' form
 *
 * Author: Michael Adams
 * Creation Date: 28/03/2011
 */
public class NonHumanSubCategoryComparator implements Comparator<NonHumanSubCategory> {


	public int compare(NonHumanSubCategory sc1, NonHumanSubCategory sc2)	{

        // if one object is null, ignore it and return the other as having precedence
        if (sc1 == null) return -1;
        if (sc2 == null) return 1;

        // compare id strings
        return sc1.getName().compareTo(sc2.getName());
    }

}