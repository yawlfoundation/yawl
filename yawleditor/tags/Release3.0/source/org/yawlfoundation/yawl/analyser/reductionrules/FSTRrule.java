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
 * Reduction rule for YAWL net: FSPY rule
 */
public class FSTRrule extends ResetReductionRule {

    /**
     * Innermost method for a reduction rule.
     * Implementation of the abstract method from superclass.
     * @param net ResetWFNet to perform reduction
     * @param netElement an  for consideration.
     * returns a reduced ResetWFNet or null if a given net cannot be reduced.
     */
    public ResetWFNet reduceElement(ResetWFNet net, RElement netElement) {
        if (netElement instanceof RPlace){
            RPlace place = (RPlace) netElement;
            Set<RElement> postSet = place.getPostsetElements();
            Set<RElement> preSet  = place.getPresetElements();

            if (preSet.size() == 1 && postSet.size() == 1) {
                RTransition t = (RTransition) preSet.iterator().next();
                RTransition u = (RTransition) postSet.iterator().next();
                Set<RElement> preSetOfu = u.getPresetElements();
                Set<RElement> postSetOfu = u.getPostsetElements();
                Set<RElement> postSetOft = new HashSet<RElement>(t.getPostsetElements());
                postSetOft.retainAll(postSetOfu);

                // u, p are not reset
                // u does not have reset arcs
                // output of u are not reset
                // postset u is not postset t
                if (preSetOfu.size() == 1 && u.getRemoveSet().isEmpty() &&
                        place.getCancelledBySet().isEmpty() && (!checkReset(postSetOfu))
                        && postSetOft.isEmpty()) {

                    // set postflows from u to t
                    for (RElement next : postSetOfu) {
                        t.setPostset(new RFlow(t,next));
                    }

                    //remove place from postset of t
                    t.removePostsetFlow(new RFlow(place, t));
                    t.addToResetMappings(place.getResetMappings());
                    t.addToResetMappings(u.getResetMappings());
                    net.removeNetElement(place);
                    net.removeNetElement(u);

                    return net;
                }
            }
        }
        return null;
    }

    private boolean checkReset(Set<RElement> elements) {
        for (RElement next : elements) {
            if (! next.getCancelledBySet().isEmpty()) return true;
        }
        return false;
    }
}