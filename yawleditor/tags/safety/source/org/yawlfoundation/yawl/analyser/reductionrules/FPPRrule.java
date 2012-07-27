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
import org.yawlfoundation.yawl.analyser.elements.ResetWFNet;

import java.util.Set;

/**
 * Reduction rule for YAWL net: FSPY rule
 */
public class FPPRrule extends ResetReductionRule {

    /**
     * Innermost method for a reduction rule.
     * Implementation of the abstract method from superclass.
     * @param net ResetWFNet to perform reduction
     * @param netElement an  for consideration.
     * returns a reduced ResetWFNet or null if a given net cannot be reduced.
     */
    public ResetWFNet reduceElement(ResetWFNet net, RElement netElement) {
        boolean isReducible = false;
        if (netElement instanceof RPlace) {
            RPlace place = (RPlace) netElement;
            Set<RElement> postSet = place.getPostsetElements();
            Set<RElement> preSet  = place.getPresetElements();

            //check for $i$ and $o$
            if (! (preSet.isEmpty() || postSet.isEmpty())) {

                // potential candidate exits so now try and find
                // one or more other places
                for (RElement element : net.getNetElements().values()) {
                    if (element instanceof RPlace) {
                        Set<RElement> postSet2 = element.getPostsetElements();
                        Set<RElement> preSet2  = element.getPresetElements();

                        //two places with same presets and postsets
                        //in same cancellation regions
                        if (postSet.equals(postSet2) && preSet.equals(preSet2) &&
                                !element.equals(place)
                                && element.getCancelledBySet().equals(
                                place.getCancelledBySet())) {

                            isReducible = true;
                            net.removeNetElement(element);
                            place.addToResetMappings(element.getResetMappings());
                        }
                    }
                }
            }
        }
        return isReducible ? net : null;
    }

}