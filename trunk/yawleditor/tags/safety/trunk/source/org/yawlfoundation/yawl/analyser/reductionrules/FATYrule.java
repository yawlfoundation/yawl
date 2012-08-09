

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
 * Reduction rule for YAWL net: FATY rule
 */
public class FATYrule extends YAWLReductionRule{

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
            Set<YExternalNetElement> postSet = task.getPostsetElements();
            Set<YExternalNetElement> preSet  = task.getPresetElements();

            //check if task is xor-split and xor-join
            if (preSet.size() > 1 && postSet.size()>1 &&
                    task.getSplitType() == YTask._XOR &&
                    task.getJoinType() == YTask._XOR ) {

                // potential candidate exists so now try and find
                // one or more other tasks
                for (YExternalNetElement element : net.getNetElements().values()) {
                    if (element instanceof YTask) {
                        Set<YExternalNetElement> postSet2 = element.getPostsetElements();
                        Set<YExternalNetElement> preSet2  = element.getPresetElements();
                        YTask elementTask = (YTask) element;
                        if (postSet.equals(postSet2) && preSet.equals(preSet2) &&
                                task.getRemoveSet().isEmpty() &&
                                task.getSplitType() == elementTask.getSplitType() &&
                                task.getJoinType() == elementTask.getJoinType() &&
                                task.getCancelledBySet().isEmpty() &&
                                element.getCancelledBySet().isEmpty() &&
                                elementTask.getRemoveSet().isEmpty() &&
                                !element.equals(task)) {

                            isReducible = true;
                            net.removeNetElement(element);
                            task.addToYawlMappings(element);
                            task.addToYawlMappings(element.getYawlMappings());
                        }
                    }
                }
                if (isReducible) {
                    return net;
                }
            }
        }
        return null;
    }

}