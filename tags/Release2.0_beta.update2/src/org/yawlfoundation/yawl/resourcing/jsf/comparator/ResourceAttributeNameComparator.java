package org.yawlfoundation.yawl.resourcing.jsf.comparator;

import org.yawlfoundation.yawl.resourcing.resource.Capability;
import org.yawlfoundation.yawl.resourcing.resource.OrgGroup;
import org.yawlfoundation.yawl.resourcing.resource.Position;
import org.yawlfoundation.yawl.resourcing.resource.Role;

import java.util.Comparator;

/**
 * Allows ResourceAttribute objects to be sorted  on name for display in
 * 'Edit Org Data' form
 *
 * Author: Michael Adams
 * Creation Date: 08/02/2008
 */
public class ResourceAttributeNameComparator implements Comparator {

    /*
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object obj1, Object obj2)	{

        String name1 = "";
        String name2 = "";

        // if one object is null, ignore it and return the other as having precedence
        if (obj1 == null) return -1;
        if (obj2 == null) return 1;

        if (obj1 instanceof Role) {
            name1 = ((Role) obj1).getName();
            name2 = ((Role) obj2).getName();
        }
        else if (obj1 instanceof Position) {
            name1 = ((Position) obj1).getTitle();
            name2 = ((Position) obj2).getTitle();
        }
        else if (obj1 instanceof Capability) {
            name1 = ((Capability) obj1).getCapability();
            name2 = ((Capability) obj2).getCapability();
        }
        else if (obj1 instanceof OrgGroup) {
            name1 = ((OrgGroup) obj1).getGroupName();
            name2 = ((OrgGroup) obj2).getGroupName();       
        }


        // compare id strings
        return name1.compareTo(name2);
    }

}