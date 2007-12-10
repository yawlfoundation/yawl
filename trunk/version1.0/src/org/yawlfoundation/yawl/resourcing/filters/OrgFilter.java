/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
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
 *  Create Date: 23/08/2007. Last Date: 12/11/2007
 *
 *  @author Michael Adams (BPM Group, QUT Australia)
 *  @version 1.0
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
     * @param pSet the distribution set to filter
     * @return the filtered distribution set
     */
    public Set<Participant> performFilter(Set<Participant> pSet) {
        String positionID = getParamValue("Position") ;
        String orgGroupID = getParamValue("OrgGroup") ;
        ResourceManager rm = ResourceManager.getInstance() ;
        Set<Participant> result = new HashSet<Participant>() ;

        // do posn first as it will usually result in a smaller set
        if (positionID != null ) {
            Position pos = rm.getPosition(positionID) ;
            for (Participant p : pSet)
                if (pos.hasResource(p)) result.add(p) ;
            pSet = result ;
        }

        if (orgGroupID != null) {
            OrgGroup og = rm.getOrgGroup(orgGroupID) ;
            result = new HashSet<Participant>() ;
            for (Participant p : pSet) {
                if (og.hasResourceInHierarchy(p)) result.add(p) ;
            }
        }
        
        if (result.isEmpty()) return null ;
        else return result ;
    }

}
