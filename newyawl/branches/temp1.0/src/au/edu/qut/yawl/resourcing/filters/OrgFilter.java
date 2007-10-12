/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing.filters;

import au.edu.qut.yawl.resourcing.resource.Participant;
import au.edu.qut.yawl.resourcing.resource.Position;
import au.edu.qut.yawl.resourcing.resource.OrgGroup;
import au.edu.qut.yawl.resourcing.ResourceManager;

import java.util.*;

/**
 * Filters a distribution set based on organisational data
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.1, 23/08/2007
 */

public class OrgFilter extends AbstractFilter {

    public OrgFilter(String name) {
        this();
        setName(name) ;
    }

    public OrgFilter(String name, HashMap<String,String> params) {
        this() ;
        setName(name) ;
        setParams(params) ;
    }

    public OrgFilter() {
        super();
        setDescription("This Organisational Filter will filter a distribution set " +
                       "based on whether each Participant in the set is a member of " +
                       "the OrgGroup and/or Position passed as parameters" +
                       "    PARAMS: OrgGroup, Position");
        setDisplayName("Organisational Filter");
        setFilterType(AbstractFilter.ORGANISATIONAL_FILTER) ;
        addParam("OrgGroup", null) ;
        addParam("Position", null) ;
    }


    public Set<Participant> performFilter(Set<Participant> pSet) {
        String positionID = getParamValue("Position") ;
        String orgGroupID = getParamValue("OrgGroup") ;
        ResourceManager rm = ResourceManager.getInstance() ;
        Set<Participant> result = new HashSet<Participant>() ;

        // do posn first as it will result in a smaller set
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
