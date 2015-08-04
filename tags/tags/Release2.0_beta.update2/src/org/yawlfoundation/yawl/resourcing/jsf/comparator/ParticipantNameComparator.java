package org.yawlfoundation.yawl.resourcing.jsf.comparator;

import org.yawlfoundation.yawl.elements.YAWLServiceReference;
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
public class ParticipantNameComparator implements Comparator {

    /*
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object obj1, Object obj2)	{

        // if one object is null, ignore it and return the other as having precedence
        if (obj1 == null) return -1;
        if (obj2 == null) return 1;

        String lastName1 = getCorrectedSurname(obj1);
        String lastName2 = getCorrectedSurname(obj2);

        // compare id strings
        int result = lastName1.compareTo(lastName2);

        // if last names are the same, secondary sort on firstnames
        if (result == 0) {
            String firstName1 = ((Participant) obj1).getFirstName().trim();
            String firstName2 = ((Participant) obj2).getFirstName().trim();
            result = firstName1.compareTo(firstName2);
        }
        return result;
    }
    

    private String getCorrectedSurname(Object obj) {
        String name = ((Participant) obj).getLastName().trim();
        int lastSpace = name.lastIndexOf(" ");
        if (lastSpace > -1) name = name.substring(lastSpace + 1);
        return name.toUpperCase();
    }

}