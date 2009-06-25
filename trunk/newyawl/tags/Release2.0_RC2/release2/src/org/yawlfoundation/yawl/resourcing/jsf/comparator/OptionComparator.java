package org.yawlfoundation.yawl.resourcing.jsf.comparator;

import com.sun.rave.web.ui.model.Option;

import java.util.Comparator;

/**
 * Allows Option objects (for jsf listboxes) to be sorted case-insensitively for display
 *
 * Author: Michael Adams
 * Creation Date: 08/02/2008
 */
public class OptionComparator implements Comparator<Option> {

    /*
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Option opt1, Option opt2)	{

        // if one object is null, ignore it and return the other as having precedence
        if (opt1 == null) return -1;
        if (opt2 == null) return 1;

        String label1 = opt1.getLabel();
        String label2 = opt2.getLabel();

        // compare label strings
        return label1.compareToIgnoreCase(label2);
    }

}