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