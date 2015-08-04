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

import org.yawlfoundation.yawl.elements.YAWLServiceReference;

import java.util.Comparator;

/**
 * Allows YAWL Services to be sorted on name for display in 'Service Mgt' form
 *
 * Author: Michael Adams
 * Creation Date: 24/01/2008
 */
public class YAWLServiceComparator implements Comparator<YAWLServiceReference> {

    /*
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(YAWLServiceReference service1, YAWLServiceReference service2)	{

        // if one object is null, ignore it and return the other as having precedence
        if (service1 == null) return -1;
        if (service2 == null) return 1;

        String id1 = service1.getServiceName();
        String id2 = service2.getServiceName();

        // compare id strings
        return id1.compareTo(id2);
	}

}