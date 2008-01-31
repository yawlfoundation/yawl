package org.yawlfoundation.yawl.resourcing.jsf.comparator;

import org.yawlfoundation.yawl.engine.interfce.SpecificationData;

import java.util.Comparator;

/**
 * Allows SpecificationData objects to be sorted on id for display in 'Case Mgt' form
 *
 * Author: Michael Adams
 * Creation Date: 26/01/2008
 */
public class SpecificationDataComparator implements Comparator {

    /*
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object obj1, Object obj2)	{

        // if one object is null, ignore it and return the other as having precedence
        if (obj1 == null) return -1;
        if (obj2 == null) return 1;

        String id1 = ((SpecificationData) obj1).getID();
        String id2 = ((SpecificationData) obj2).getID();

        // compare id strings
        return id1.compareTo(id2);
	}

}