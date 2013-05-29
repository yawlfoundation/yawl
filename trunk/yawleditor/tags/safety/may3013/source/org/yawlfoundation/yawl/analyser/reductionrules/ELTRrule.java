

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

import org.yawlfoundation.yawl.analyser.elements.RElement;
import org.yawlfoundation.yawl.analyser.elements.RPlace;
import org.yawlfoundation.yawl.analyser.elements.RTransition;
import org.yawlfoundation.yawl.analyser.elements.ResetWFNet;

import java.util.Set;

/**
 * Reduction rule for YAWL net with OR-joins: FOR rule
 */
public class ELTRrule extends ResetReductionRule {

    /**
     * Innermost method for a reduction rule.
     * Implementation of the abstract method from superclass.
     * @param net ResetWFNet to perform reduction
     * @param netElement an  for consideration.
     * returns a reduced ResetWFNet or null if a given net cannot be reduced.
     */
    public ResetWFNet reduceElement(ResetWFNet net, RElement netElement) {
        if (netElement instanceof RTransition){
            RTransition task = (RTransition) netElement;
            Set<RElement> preSet = task.getPresetElements();
            Set<RElement> postSet = task.getPostsetElements();

            // t has only one input and output
            // t cannot reset and t is not part of cancellation region
            if (preSet.equals(postSet) && preSet.size() == 1 &&
                    task.getRemoveSet().isEmpty()) {
                RPlace place = (RPlace) preSet.iterator().next();

                //added to mappings
                place.addToResetMappings(task.getResetMappings());
                net.removeNetElement(task);
                return net;
            }
        }
        return null;
    }
}