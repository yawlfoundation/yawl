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

import org.yawlfoundation.yawl.elements.YCondition;
import org.yawlfoundation.yawl.elements.YExternalNetElement;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.util.StringUtil;

import java.util.Set;

public abstract class YAWLReductionRule {

    /**
     * Main method for calling reduction rule, apply a reduction rule recursively
     * to a given net until it cannot be reduced further.
     * @param net YNet to perform reduction
     * returns a reduced YNet or null if a given net cannot be reduced.
     */
    public YNet reduce(YNet net) {
        YNet reducedNet = reduceANet(net);
        while (reducedNet != null) {
            YNet temp = reduceANet(reducedNet);
            if (temp == null) break;
            reducedNet = temp;
        }
        return reducedNet;
    }


    /**
     * Inner method for a reduction rule.
     * Go through all elements in a YNet
     * @param net YNet to perform reduction
     * returns a reduced YNet or null if a given net cannot be reduced.
     */
    private YNet reduceANet(YNet net) {
        for (YExternalNetElement nextElement : net.getNetElements().values()) {
            YNet reducedNet = reduceElement(net, nextElement);
            if (reducedNet != null) return reducedNet;
        }
        return null;
    }


    /**
     * Innermost method for a reduction rule.
     * @param net YNet to perform reduction
     * @param element one element for consideration.
     * returns a reduced YNet or null if a given net cannot be reduced.
     * Must be implemented by individual reduction rule
     */
    public abstract YNet reduceElement(YNet net, YExternalNetElement element);


    /**
     * Returns true if every condition in the set has one input and
     * one output and are not part of cancellation regions.
     *
     */
    public boolean checkEqualConditions(Set<YExternalNetElement> conditions) {
        for (YExternalNetElement element : conditions) {
            if (! (hasOneInAndOutFlow(element) || isInACancelSet(element))) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method add labels for YConditions without a name
     * by setting the name to be the same as the ID.
     *
     */
    public void setLabel(YExternalNetElement e) {
        if (e instanceof YCondition && (StringUtil.isNullOrEmpty(e.getName()))) {
            e.setName(e.getID());
        }
    }


    protected boolean hasOneInAndOutFlow(YExternalNetElement e) {
        return hasOneInFlow(e) && hasOneOutFlow(e);
    }


    protected boolean hasOneInFlow(YExternalNetElement e) {
        return e.getPresetElements().size() == 1;
    }


    protected boolean hasOneOutFlow(YExternalNetElement e) {
        return e.getPostsetElements().size() == 1;
    }


    protected boolean hasMultiInAndOutFlows(YExternalNetElement e) {
        return hasMultiInFlows(e) && hasMultiOutFlows(e);
    }


    protected boolean hasMultiInFlows(YExternalNetElement e) {
        return e.getPresetElements().size() > 1;
    }


    protected boolean hasMultiOutFlows(YExternalNetElement e) {
        return e.getPostsetElements().size() > 1;
    }


    protected boolean isInACancelSet(YExternalNetElement e) {
        return ! e.getCancelledBySet().isEmpty();
    }


    protected boolean presetEqualsPostset(YExternalNetElement e) {
        return e.getPresetElements().equals(e.getPostsetElements());
    }


    protected boolean canReset(YTask e) {
        return ! e.getRemoveSet().isEmpty();
    }


    protected boolean hasAndSplit(YTask task) {
        return task.getSplitType() == YTask._AND;
    }


    protected boolean hasAndJoin(YTask task) {
        return task.getJoinType() == YTask._AND;
    }




}
	