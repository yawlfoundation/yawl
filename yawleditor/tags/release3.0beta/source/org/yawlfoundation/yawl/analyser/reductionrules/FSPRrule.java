/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

/*
 * 
 * @author Moe Thandar Wynn
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.yawlfoundation.yawl.analyser.reductionrules;

import org.yawlfoundation.yawl.analyser.elements.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Reduction rule for RWF-net: FSPR rule
 */
public class FSPRrule extends ResetReductionRule {

    /**
     * Innermost method for a reduction rule.
     * @param net ResetWFNet to perform reduction
     * @param netElement one element for consideration.
     * returns a reduced ResetWFNet or null if a given net cannot be reduced.
     * Must be implemented by individual reduction rule
     */
    public ResetWFNet reduceElement(ResetWFNet net, RElement netElement) {
        if (netElement instanceof RPlace){
            RPlace place = (RPlace) netElement;
            Set<RElement> postSet = place.getPostsetElements();
            Set<RElement> postSetPlaces = ResetWFNet.getPostset(postSet);
            Set<RElement> preSet = place.getPresetElements();

            //\post{p} = 1 and \post{t} = q (just one) and p is not input place
            if (postSet.size() == 1 && postSetPlaces.size() == 1 && !preSet.isEmpty()) {
                RTransition t = (RTransition) postSet.toArray()[0];
                Set<RElement> preSetOft = t.getPresetElements();

                //pre{t} = p (just one)
                if (preSetOft.size() == 1) {

                    RPlace q = (RPlace) postSetPlaces.iterator().next();
                    Set<RElement> preSetOfq = new HashSet<RElement>(q.getPresetElements());
                    Set<RElement> preSetOfp  = new HashSet<RElement>(place.getPresetElements());
                    preSetOfp.retainAll(preSetOfq);

                    //q is not output place
                    //check for direct places \pre{p} \cap \pre{q} = empty
                    //R-1(p) = R-1(q)
                    //rem(t) = emptyset
                    if (!(q.equals(net.getOutputPlace())) && preSetOfp.isEmpty() &&
                            t.getRemoveSet().isEmpty() &&
                            place.getCancelledBySet().equals(q.getCancelledBySet())) {

                        // set postflows from q to p
                        Set<RElement> postSetOfq = new HashSet<RElement>(q.getPostsetElements());
                        for (RElement next : postSetOfq) {
                            place.setPostset(new RFlow(place, next));
                        }

                        // set preflows from q to p - t
                        preSetOfq.remove(t);
                        for (RElement next : preSetOfq) {
                            place.setPreset(new RFlow(next, place));
                        }

                        //remove t from postset of place
                        place.removePostsetFlow(new RFlow(place, t));
                        place.addToResetMappings(q.getResetMappings());
                        place.addToResetMappings(t.getResetMappings());
                        net.removeNetElement(q);
                        net.removeNetElement(t);

                        return net;
                    }
                }
            }
        }
        return null;
    }
}