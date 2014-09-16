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

import org.yawlfoundation.yawl.engine.interfce.SpecificationData;

import java.util.Comparator;

/**
 * Allows SpecificationData objects to be sorted on id for display in 'Case Mgt' form
 *
 * Author: Michael Adams
 * Creation Date: 26/01/2008
 */
public class SpecificationDataComparator implements Comparator<SpecificationData> {

    /*
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(SpecificationData sd1, SpecificationData sd2)	{

        // if one object is null, ignore it and return the other as having precedence
        if (sd1 == null) return -1;
        if (sd2 == null) return 1;

        String uri1 = sd1.getID().getUri();
        String uri2 = sd2.getID().getUri();

        // compare case-insensitive uri strings
        int specCompare = uri1.compareToIgnoreCase(uri2);

        if (specCompare == 0) {
            String version1 = sd1.getID().getVersionAsString();
            String version2 = sd2.getID().getVersionAsString();
            specCompare = version1.compareTo(version2);
        }
        
        return specCompare;
	}

}