/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.resource.AbstractResource;
import org.yawlfoundation.yawl.resourcing.resource.Capability;
import org.yawlfoundation.yawl.resourcing.resource.Participant;

import java.io.IOException;
import java.util.*;

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


    public Set<Participant> performFilter(Set<Participant> distSet, WorkItemRecord wir) {
        if ((distSet == null) || distSet.isEmpty()) return distSet;
        Set<Participant> result = new HashSet<Participant>();
        Set<AbstractResource> capableSet = parse(getParamValue("Capability"), wir);
        for (Participant p : distSet) if (capableSet.contains(p)) result.add(p);
        return result;
    }


    private Set<AbstractResource> parse(String expression, WorkItemRecord wir) {
        if (expression != null) {
            List<Set<AbstractResource>> pSets = new ArrayList<Set<AbstractResource>>();
            for (String capName : expression.split("[&|]")) {
                capName = capName.trim();
                if (capName.startsWith("$")) {
                    capName = getRuntimeValue(capName, wir);
                }
                Capability c = ResourceManager.getInstance().
                        getOrgDataSet().getCapabilityByLabel(capName);
                if (c == null) c = new Capability();
                pSets.add(c.getResources());
            }
            return evaluate(pSets, expression);
        }
        return Collections.emptySet();
    }

    
    private String getRuntimeValue(String s, WorkItemRecord wir) {
        String varName = s.substring(2, s.indexOf('}'));       // extract varname
        try {
            String varValue = ResourceManager.getInstance().getNetParamValue(wir.getCaseID(), varName);
            return varValue != null ? varValue : "";
        }
        catch (IOException e) {
            return "";
        }
    }
    

}
