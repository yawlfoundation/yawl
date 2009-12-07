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

        String id1 = sd1.getID().getKey();
        String id2 = sd2.getID().getKey();

        // compare case-insensitive id strings
        return id1.compareToIgnoreCase(id2);
	}

}