/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

import org.yawlfoundation.yawl.resourcing.resource.*;
import org.yawlfoundation.yawl.resourcing.ResourceManager;

import java.util.*;

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
        Set<Participant> result = new HashSet<Participant>();
        String posValue = getParamValue("Position");
        String ogValue = getParamValue("OrgGroup");

        // do posn first as it will usually result in a smaller set
        if (posValue != null) {
            Set<AbstractResource> positionSet = parsePositionValue(posValue);
            for (Participant p : distSet) if (positionSet.contains(p)) result.add(p);
            distSet = result;
        }

        if (ogValue != null) {
            result = new HashSet<Participant>() ;
            Set<AbstractResource> orgGroupSet = parseOrgGroupValue(ogValue, distSet);
            for (Participant p : distSet) if (orgGroupSet.contains(p)) result.add(p) ;
        }
        return result;
    }


    private Set<AbstractResource> parsePositionValue(String expression) {
        if (expression != null) {
            List<Set<AbstractResource>> pSets = new ArrayList<Set<AbstractResource>>();
            for (String posName : expression.split("[&|]")) {
                Position p = ResourceManager.getInstance().
                        getOrgDataSet().getPositionByLabel(posName.trim());
                if (p == null) p = new Position();
                pSets.add(p.getResources());
            }
            return evaluate(pSets, expression);
        }
        return Collections.emptySet();
    }

    private Set<AbstractResource> parseOrgGroupValue(String expression,
                                                     Set<Participant> distSet) {
        if (expression != null) {
            List<Set<AbstractResource>> pSets = new ArrayList<Set<AbstractResource>>();
            for (String ogName : expression.split("[&|]")) {
                OrgGroup og = ResourceManager.getInstance().
                        getOrgDataSet().getOrgGroupByLabel(ogName.trim());
                if (og == null) og = new OrgGroup();
                Set<AbstractResource> resources = new HashSet<AbstractResource>();
                for (Participant p : distSet) {
                    if (p.isOrgGroupMember(og)) resources.add(p);  // hierarchical
                }
                pSets.add(resources);
            }
            return evaluate(pSets, expression);
        }
        return Collections.emptySet();
    }

}
