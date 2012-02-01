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

import org.yawlfoundation.yawl.resourcing.resource.Participant;

import java.util.Comparator;

/**
 * Allows Participant objects to be sorted on surname, firstname for display in
 * 'Edit Org Data' form.
 *
 * Sorts correctly on surname for qualified names (eg. 'D' in 'van Dyke'); case
 * insensitive for unqualified surnames
 *
 *
 * Author: Michael Adams
 * Creation Date: 08/02/2008
 */
public class ParticipantNameComparator implements Comparator<Participant> {

    /*
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Participant p1, Participant p2)	{

        // if one object is null, ignore it and return the other as having precedence
        if (p1 == null) return -1;
        if (p2 == null) return 1;

        String lastName1 = getCorrectedSurname(p1);
        String lastName2 = getCorrectedSurname(p2);

        // compare id strings
        int result = lastName1.compareTo(lastName2);

        // if last names are the same, secondary sort on firstnames
        if (result == 0) {
            String firstName1 = p1.getFirstName().trim();
            String firstName2 = p2.getFirstName().trim();
            result = firstName1.compareTo(firstName2);
        }
        return result;
    }
    

    private String getCorrectedSurname(Participant p) {
        String name = p.getLastName().trim();
        int lastSpace = name.lastIndexOf(" ");
        if (lastSpace > -1) name = name.substring(lastSpace + 1);
        return name.toUpperCase();
    }

}