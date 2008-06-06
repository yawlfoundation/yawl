package org.yawlfoundation.yawl.resourcing.jsf.comparator;

import org.yawlfoundation.yawl.elements.YAWLServiceReference;

import java.util.Comparator;

/**
 * Allows YAWL Services to be sorted on name for display in 'Service Mgt' form
 *
 * Author: Michael Adams
 * Creation Date: 24/01/2008
 */
public class YAWLServiceComparator implements Comparator {

    /*
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object obj1, Object obj2)	{

        // if one object is null, ignore it and return the other as having precedence
        if (obj1 == null) return -1;
        if (obj2 == null) return 1;

        String id1 = ((YAWLServiceReference) obj1).get_serviceName();
        String id2 = ((YAWLServiceReference) obj2).get_serviceName();

        // compare id strings
        return id1.compareTo(id2);
	}

}