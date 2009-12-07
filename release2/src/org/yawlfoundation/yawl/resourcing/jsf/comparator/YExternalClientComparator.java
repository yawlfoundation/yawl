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

        String id1 = client1.getUserID();
        String id2 = client2.getUserID();

        // compare id strings
        return id1.compareTo(id2);
	}

}