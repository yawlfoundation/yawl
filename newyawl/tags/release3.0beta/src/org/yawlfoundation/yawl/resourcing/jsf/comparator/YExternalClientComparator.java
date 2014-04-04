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

import org.yawlfoundation.yawl.authentication.YExternalClient;

import java.util.Comparator;

/**
 * Allows External Client Application to be sorted on name
 *
 * Author: Michael Adams
 * Creation Date: 24/01/2008
 */
public class YExternalClientComparator implements Comparator<YExternalClient> {

    /*
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(YExternalClient client1, YExternalClient client2)	{

        // if one object is null, ignore it and return the other as having precedence
        if (client1 == null) return -1;
        if (client2 == null) return 1;

        String id1 = client1.getUserName();
        String id2 = client2.getUserName();

        // compare id strings
        return id1.compareTo(id2);
	}

}