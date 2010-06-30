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
import org.yawlfoundation.yawl.resourcing.resource.Capability;
import org.yawlfoundation.yawl.resourcing.ResourceManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Filters a distribution set based on participant Capabilities
 *
 *  Create Date: 23/08/2007. Last Date: 12/11/2007
 *
 *  @author Michael Adams (BPM Group, QUT Australia)
 *  @version 2.0
 */

public class CapabilityFilter extends AbstractFilter {

    public CapabilityFilter() {
        super() ;
        setName(this.getClass().getSimpleName());
        addKey("Capability") ;
        setDisplayName("Filter by Capability") ;
        setFilterType(AbstractFilter.CAPABILITY_FILTER) ;
        setDescription("This Capability Filter will filter a distribution set " +
                       "based on whether each Participant in the set has a " +
                       "the Capability specified.");
    }


    public Set<Participant> performFilter(Set<Participant> distSet) {

        if ((distSet == null) || distSet.isEmpty()) return distSet;

        String capability = getParamValue("Capability") ;
        ResourceManager rm = ResourceManager.getInstance() ;
        Set<Participant> result = new HashSet<Participant>() ;

        if (capability != null ) {
            Capability cap = rm.getOrgDataSet().getCapabilityByLabel(capability) ;
            for (Participant p : distSet)
                if (cap.hasResource(p)) result.add(p) ;
        }

        return result ;
    }

}
