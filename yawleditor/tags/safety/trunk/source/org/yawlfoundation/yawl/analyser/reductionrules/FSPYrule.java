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

import org.yawlfoundation.yawl.elements.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Reduction rule for YAWL net: FSPY rule
 */
public class FSPYrule extends YAWLReductionRule {

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
            Set<YExternalNetElement> postSet = condition.getPostsetElements();
            Set<YExternalNetElement> postSetConditions = YNet.getPostset(postSet);

            //\post{p} = t and \post{t} = q and p is not input place
            if (postSet.size() == 1 && postSetConditions.size() == 1 &&
                    !(condition instanceof YInputCondition)) {

                YTask t = (YTask) postSet.iterator().next();
                Set preSetOft = t.getPresetElements();

                //pre{t} = p (just one)
                if (preSetOft.size() == 1) {
                    YCondition q = (YCondition) postSetConditions.toArray()[0];
                    Set<YExternalNetElement> preSetOfq =
                            new HashSet<YExternalNetElement>(q.getPresetElements());
                    Set<YExternalNetElement> preSetOfp  =
                            new HashSet<YExternalNetElement>(condition.getPresetElements());
                    preSetOfp.retainAll(preSetOfq);

                    //q is not output condition
                    //check for direct conditions \pre{p} \cap \pre{q} = empty
                    //rem-1(t) = rem-1(p) = rem-1(q)
                    //rem(t) = emptyset
                    if (!(q instanceof YOutputCondition) && preSetOfp.isEmpty() &&
                            t.getRemoveSet().isEmpty() &&
                            condition.getCancelledBySet().equals(q.getCancelledBySet()) &&
                            condition.getCancelledBySet().equals(t.getCancelledBySet())) {

                        // set postflows from q to p
                        Set<YExternalNetElement> postSetOfq =
                                new HashSet<YExternalNetElement>(q.getPostsetElements());
                        for (YExternalNetElement next : postSetOfq) {
                            condition.addPostset(new YFlow(condition, next));
                        }

                        // set preflows from q to p - t
                        preSetOfq.remove(t);
                        for (YExternalNetElement next : preSetOfq) {
                            condition.addPreset(new YFlow(next, condition));
                        }

                        //remove t from postset of condition
                        condition.removePostsetFlow(new YFlow(condition, t));
                        condition.addToYawlMappings(q);
                        condition.addToYawlMappings(t);
                        condition.addToYawlMappings(q.getYawlMappings());
                        condition.addToYawlMappings(t.getYawlMappings());
                        setLabel(condition);
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