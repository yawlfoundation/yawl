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

package org.yawlfoundation.yawl.resourcing.jsf.comparator;

import org.yawlfoundation.yawl.resourcing.resource.Participant;

import java.util.Comparator;

/**
 * Allows Participant objects to be sorted on userid for display in
 * 'Edit Org Data' form
 *
 * Author: Michael Adams
 * Creation Date: 08/02/2008
 */
public class ParticipantUserIDComparator implements Comparator<Participant> {

    /*
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Participant p1, Participant p2)	{

        // if one object is null, ignore it and return the other as having precedence
        if (p1 == null) return -1;
        if (p2 == null) return 1;

        String userID1 = p1.getUserID();
        String userID2 = p2.getUserID();

        // compare id strings
        return userID1.compareTo(userID2);
    }

}