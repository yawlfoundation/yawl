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

package org.yawlfoundation.yawl.resourcing.filters;

import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Position;
import org.yawlfoundation.yawl.resourcing.resource.OrgGroup;
import org.yawlfoundation.yawl.resourcing.ResourceManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Filters a distribution set based on organisational data
 *
 *  Create Date: 23/08/2007. Last Date: 08/08/2008
 *
 *  @author Michael Adams
 *  @version 2.0
 */

public class OrgFilter extends AbstractFilter {

    public OrgFilter() {
        super();
        setName(this.getClass().getSimpleName());
        setDescription("This Organisational Filter will filter a distribution set " +
                       "based on whether each Participant in the set is a member of " +
                       "the OrgGroup and/or Position passed as parameters");
        setDisplayName("Filter by Organisational Data");
        setFilterType(AbstractFilter.ORGANISATIONAL_FILTER) ;
        addKey("OrgGroup") ;
        addKey("Position") ;
    }

    
    /**
     * Filters the distribution set passed based on position and/or org group
     * values specified
     * @param distSet the distribution set to filter
     * @return the filtered distribution set
     */
    public Set<Participant> performFilter(Set<Participant> distSet) {

        if ((distSet == null) || distSet.isEmpty()) return distSet;

        String positionTitle = getParamValue("Position") ;
        String orgGroupName = getParamValue("OrgGroup") ;
        ResourceManager rm = ResourceManager.getInstance() ;
        Set<Participant> result = new HashSet<Participant>();

        // do posn first as it will usually result in a smaller set
        if (positionTitle != null ) {
            Position pos = rm.getOrgDataSet().getPositionByLabel(positionTitle) ;
            if (pos != null) {
               for (Participant p : distSet) {
                   if (pos.hasResource(p)) result.add(p) ;
               }
               distSet = result ;
            }    
        }

        if (orgGroupName != null) {
            OrgGroup og = rm.getOrgDataSet().getOrgGroupByLabel(orgGroupName) ;
            if (og != null) {
                result = new HashSet<Participant>() ;
                for (Participant p : distSet) {
                    if (p.isOrgGroupMember(og)) result.add(p) ;
                }
            }
        }
        
        return result ;
    }

}
