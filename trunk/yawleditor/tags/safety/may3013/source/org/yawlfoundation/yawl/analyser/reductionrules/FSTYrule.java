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
public class FSTYrule extends YAWLReductionRule {

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

            //\pre{p} = 1, \post{p} = 1
            if (hasOneInAndOutFlow(condition)) {
                YTask t = (YTask) condition.getPresetElements().iterator().next();
                YTask u = (YTask) condition.getPostsetElements().iterator().next();
                Set<YExternalNetElement> postSetOfu = u.getPostsetElements();

                // t,u and p are not reset
                // u does not have reset arcs
                Set<YExternalNetElement> postSetOft =
                        new HashSet<YExternalNetElement>(t.getPostsetElements());
                postSetOft.retainAll(postSetOfu);

                if (hasAndSplit(t) && hasAndSplit(u) &&
                        hasOneInFlow(u) && postSetOft.isEmpty() &&
                        !(canReset(u) || isInACancelSet(t) || isInACancelSet(u) ||
                          isInACancelSet(condition) || checkReset(postSetOfu))) {

                    // set postflows from u to t
                    for (YExternalNetElement next : postSetOfu) {
                        t.addPostset(new YFlow(t, next));
                    }

                    //remove condition from postset of t
                    t.removePostsetFlow(new YFlow(condition, t));
                    net.removeNetElement(condition);
                    net.removeNetElement(u);
                    t.addToYawlMappings(condition);
                    t.addToYawlMappings(condition.getYawlMappings());
                    t.addToYawlMappings(u);
                    t.addToYawlMappings(u.getYawlMappings());
                    return net;
                }
            }
        }
        return null;
    }

    private boolean checkReset(Set<YExternalNetElement> elements) {
        for (YExternalNetElement element : elements) {
            if (!isInACancelSet(element)) {
                return true;
            }
        }
        return false;
    }
}