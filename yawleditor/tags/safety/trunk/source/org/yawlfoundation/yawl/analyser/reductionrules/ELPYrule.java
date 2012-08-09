

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

import org.yawlfoundation.yawl.elements.YCondition;
import org.yawlfoundation.yawl.elements.YExternalNetElement;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YTask;

import java.util.Set;

/**
 * Reduction rule for YAWL net: ELPY
 */
public class ELPYrule extends YAWLReductionRule {

    /**
    * Innermost method for a reduction rule.
    * Implementation of the abstract method from superclass.
    * @param net YNet to perform reduction
    * @param netElement an  for consideration.
    * returns a reduced YNet or null if a given net cannot be reduced.
    */
    public YNet reduceElement(YNet net, YExternalNetElement netElement) {
        if (netElement instanceof YCondition) {
            YCondition condition = (YCondition) netElement;
            Set<YExternalNetElement> preSet = condition.getPresetElements();
            Set<YExternalNetElement> postSet = condition.getPostsetElements();

            // x has only one input and output - t
            // t cannot reset and t is not part of cancellation region
            if (preSet.equals(postSet) && preSet.size() == 1) {
                YTask task = (YTask) preSet.toArray()[0];

                //t does not reset
                // split and join type of t is XOR
                if (task.getRemoveSet().isEmpty() &&
                        task.getJoinType()==YTask._XOR &&
                        task.getSplitType() == YTask._XOR) {

                    //Get all pre and post conditions of t
                    Set<YExternalNetElement> prepostConditions = task.getPresetElements();
                    prepostConditions.addAll(task.getPostsetElements());
                    Set<YExternalNetElement> cancelledBySet = condition.getCancelledBySet();

                    //has the same reset arcs or empty
                    if (hasSameCancelledBySet(prepostConditions, cancelledBySet)) {
                        net.removeNetElement(condition);
                        task.addToYawlMappings(condition);
                        task.addToYawlMappings(condition.getYawlMappings());
                        setLabel(task);
                        return net;
                    }
                }
            }
        }
        return null;
    }


    private boolean hasSameCancelledBySet(Set<YExternalNetElement> conditions,
                                          Set<YExternalNetElement> cancelledBySet) {
        for (YExternalNetElement element : conditions) {
            if (! element.getCancelledBySet().equals(cancelledBySet)) {
                return false;
            }
        }
        return true;
    }
}