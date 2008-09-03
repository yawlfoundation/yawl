package org.yawlfoundation.yawl.schema;

import org.yawlfoundation.yawl.elements.data.YParameter;

import java.util.Comparator;

/**
 * Allows YParameters to be sorted on name for schema validation. Used by
 * YDataValidator.validate()
 *
 * Author: Michael Adams
 * Creation Date: 07/05/2008
 */
public class YParamNameComparator implements Comparator {

	public int compare(Object obj1, Object obj2)	{

        // if one object is null, ignore it and return the other as having precedence
        if (obj1 == null) return -1;
        if (obj2 == null) return 1;

        String name1 = ((YParameter) obj1).getName();
        String name2 = ((YParameter) obj2).getName();

        // compare id strings
        return name1.compareTo(name2);
	}

}