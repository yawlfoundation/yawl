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