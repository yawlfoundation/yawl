/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
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
