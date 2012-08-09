

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

import org.yawlfoundation.yawl.elements.YExternalNetElement;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YTask;

import java.util.Set;

/**
 * Reduction rule for YAWL net with OR-joins: FOR rule
 */
public class FIErule extends YAWLReductionRule {

    /**
     * Innermost method for a reduction rule.
     * Implementation of the abstract method from superclass.
     * @param net YNet to perform reduction
     * @param netElement an  for consideration.
     * returns a reduced YNet or null if a given net cannot be reduced.
     */
    public YNet reduceElement(YNet net, YExternalNetElement netElement) {
        boolean isReducible = false;

        if (netElement instanceof YTask) {
            YTask task = (YTask) netElement;
            if (task.getJoinType() == YTask._OR) {     // each task that has an or join

                // get all the input places to this OR join
                Set<YExternalNetElement> preSet = task.getPresetElements();
                if (preSet.size() > 1) {

                    // get all the input tasks to these places
                    for (YExternalNetElement element : YNet.getPreset(preSet)) {
                        YTask t = (YTask) element;
                        Set<YExternalNetElement> postSetOft = t.getPostsetElements();
                        preSet.retainAll(postSetOft);

                        //task has more than one input to ORjoin
                        //remove common places except 2
                        if (preSet.size() > 2 && checkEqualConditions(preSet)) {
                            Object[] preSets = preSet.toArray();
                            for (int i=2; i<preSets.length; i++) {
                                YExternalNetElement commonPlace =
                                        (YExternalNetElement) preSets[i];
                                net.removeNetElement(commonPlace);
                                task.addToYawlMappings(commonPlace);
                                task.addToYawlMappings(commonPlace.getYawlMappings());
                                isReducible = true;
                            }
                        }
                        if (isReducible) return net;
                    }
                }
            }
        }
        return null;
    }

}