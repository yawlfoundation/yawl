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

import org.yawlfoundation.yawl.elements.*;

import java.util.Set;

/**
 * Reduction rule for YAWL net: FXOR rule
 */
public class FXORrule extends YAWLReductionRule {

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
            Set<YExternalNetElement> preSet  = condition.getPresetElements();
            if (preSet.size() == 1 && postSet.size() == 1) {
                YTask t = (YTask) preSet.iterator().next();
                YTask u = (YTask) postSet.iterator().next();

                if (t.getSplitType() == YTask._XOR && u.getJoinType() == YTask._XOR &&
                        t.getRemoveSet().isEmpty() && u.getRemoveSet().isEmpty() &&
                        t.getCancelledBySet().isEmpty() &&
                        u.getCancelledBySet().isEmpty()) {

                    Set<YExternalNetElement> postSetOft = t.getPostsetElements();
                    Set<YExternalNetElement> preSetOfu  = u.getPresetElements();

                    //N>1 and \pre{p} = t and \post{p} = u and p not cancel
                    if (preSetOfu.equals(postSetOft) && checkEqualConditions(preSetOfu)) {
                        for (YExternalNetElement c : preSetOfu) {
                            net.removeNetElement(c);     //remove conditions
                        }
                        for (YExternalNetElement next : u.getPostsetElements()) {
                            t.addPostset(new YFlow(t,next)); // set postflows from u to t
                        }
                        t.setSplitType(u.getSplitType());
                        net.removeNetElement(u);
                        return net;
                    }
                }
            }
        }
        return null;
    }

}