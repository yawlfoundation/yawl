package org.yawlfoundation.yawl.resourcing.jsf.comparator;

import com.sun.rave.web.ui.model.Option;

import java.util.Comparator;

/**
 * Allows Option objects (for jsf listboxes) to be sorted case-insensitively for display
 *
 * Author: Michael Adams
 * Creation Date: 08/02/2008
 */
public class OptionComparator implements Comparator {

    /*
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object obj1, Object obj2)	{

        // if one object is null, ignore it and return the other as having precedence
        if (obj1 == null) return -1;
        if (obj2 == null) return 1;

        String label1 = ((Option) obj1).getLabel().toUpperCase();
        String label2 = ((Option) obj2).getLabel().toUpperCase();

        // compare label strings
        return label1.compareTo(label2);
    }

}