/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.resourcing.jsf.comparator;

import org.yawlfoundation.yawl.resourcing.resource.*;

import java.util.Comparator;

/**
 * Allows ResourceAttribute objects to be sorted  on name for display in
 * 'Edit Org Data' form
 *
 * Author: Michael Adams
 * Creation Date: 08/02/2008
 */
public class ResourceAttributeNameComparator implements Comparator<AbstractResourceAttribute> {

	public int compare(AbstractResourceAttribute one, AbstractResourceAttribute two)	{

        String name1 = "";
        String name2 = "";

        // if one object is null, ignore it and return the other as having precedence
        if (one == null) return -1;
        if (two == null) return 1;

        if (one instanceof Role) {
            name1 = ((Role) one).getName();
            name2 = ((Role) two).getName();
        }
        else if (one instanceof Position) {
            name1 = ((Position) one).getTitle();
            name2 = ((Position) two).getTitle();
        }
        else if (one instanceof Capability) {
            name1 = ((Capability) one).getCapability();
            name2 = ((Capability) two).getCapability();
        }
        else if (one instanceof OrgGroup) {
            name1 = ((OrgGroup) one).getGroupName();
            name2 = ((OrgGroup) two).getGroupName();
        }


        // compare id strings
        return name1.compareTo(name2);
    }

}