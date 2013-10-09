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

import org.yawlfoundation.yawl.elements.YExternalNetElement;
import org.yawlfoundation.yawl.elements.YFlow;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YTask;

import java.util.Set;

/**
 * Reduction rule for YAWL net with OR-joins: FOR rule
 */
public class FORrule extends YAWLReductionRule {

    /**
     * Innermost method for a reduction rule.
     * Implementation of the abstract method from superclass.
     * @param net YNet to perform reduction
     * @param netElement an  for consideration.
     * returns a reduced YNet or null if a given net cannot be reduced.
     */
    public YNet reduceElement(YNet net, YExternalNetElement netElement) {
        if (netElement instanceof YTask) {
            YTask task = (YTask) netElement;

            //join is OR and no cancellation
            if (task.getJoinType() == YTask._OR && task.getRemoveSet().isEmpty() &&
                    task.getCancelledBySet().isEmpty()) {
                Set<YExternalNetElement> preSet = task.getPresetElements();
                Set<YExternalNetElement> preSetTasks = YNet.getPreset(preSet);

                //join paths come from one task
                if (preSetTasks.size() == 1) {
                    YTask t = (YTask) preSetTasks.iterator().next();
                    Set<YExternalNetElement> postSetOft = t.getPostsetElements();

                    // \post{t} = \pre{u} and t cannot cancel &&
                    // all are cancelled by the same task
                    // Not sure - should be no cancel as with XOR and AND combinations
                    if (t.getRemoveSet().isEmpty() && t.getCancelledBySet().isEmpty() &&
                            postSetOft.equals(preSet) && checkEqualConditions(preSet)) {

                        // set postflows
                        for (YExternalNetElement next : task.getPostsetElements()) {
                            YFlow postflow = new YFlow(t, next);
                            t.addPostset(postflow);
                        }

                        // modify the reducedNet
                        t.setSplitType(task.getSplitType());
                        net.removeNetElement(task);
                        t.addToYawlMappings(task);
                        t.addToYawlMappings(task.getYawlMappings());

                        for (YExternalNetElement next : preSet) {
                            net.removeNetElement(next);
                            t.addToYawlMappings(next);
                            t.addToYawlMappings(next.getYawlMappings());
                        }
                        return net;
                    }
                }
            }
        }
        return null;
    }

}