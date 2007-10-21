/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are commited to improving
 * workflow technology.
 */

package au.edu.qut.yawl.resourcing.filters;

import au.edu.qut.yawl.resourcing.resource.Participant;
import au.edu.qut.yawl.resourcing.resource.Capability;
import au.edu.qut.yawl.resourcing.ResourceManager;

import java.util.*;

/**
 * Filters a distribution set based on participant Capabilities
 *
 *  @author Michael Adams
 *  BPM Group, QUT Australia
 *  m3.adams@qut.edu.au
 *  v0.1, 23/08/2007
 */

public class CapabilityFilter extends AbstractFilter {

    public CapabilityFilter(String name) {
        this();
        setName(name) ;
    }

    public CapabilityFilter(String name, HashMap<String,String> params) {
        this() ;
        setName(name) ;
        setParams(params) ;
     }

    public CapabilityFilter() {
        super();
        setDescription("This Capability Filter will filter a distribution set " +
                       "based on whether each Participant in the set has a " +
                       "the Capability passed as a parameter" +
                       "    PARAMS: Capability");
        setDisplayName("Capability Filter") ;
        setFilterType(AbstractFilter.CAPABILITY_FILTER) ;
        addParam("Capability", null) ;
    }


    public Set<Participant> performFilter(Set<Participant> pSet) {
        String capabilityID = getParamValue("Capability") ;
        ResourceManager rm = ResourceManager.getInstance() ;
        Set<Participant> result = new HashSet<Participant>() ;

        if (capabilityID != null ) {
            Capability cap = rm.getCapability(capabilityID) ;
            for (Participant p : pSet)
                if (cap.hasResource(p)) result.add(p) ;
        }

        if (result.isEmpty()) return null ;
        else return result ;
    }

}
