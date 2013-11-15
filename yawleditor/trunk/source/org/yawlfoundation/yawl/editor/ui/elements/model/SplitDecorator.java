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

package org.yawlfoundation.yawl.editor.ui.elements.model;

import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class SplitDecorator extends Decorator {

    /**
     * This constructor is to be invoked whenever we are creating a new decorator
     * from scratch. It also creates the correct ports needed for the decorator
     * as an intended side-effect.
     */
    public SplitDecorator(YAWLTask task, int type, int position) {
        super(task, type, position);
    }

    public boolean generatesOutgoingFlows() {
        return true;
    }

    public boolean acceptsIncomingFlows() {
        return false;
    }

    public static int getDefaultPosition() {
        return Decorator.RIGHT;
    }

    public void compressFlowPriorities() {
        compressFlowPriorities(getFlowsInPriorityOrder());
    }

    public void compressFlowPriorities(Set flows) {
        Object[] flowsAsArray = flows.toArray();

        // Convert to array (also sorted) and compress the priority range
        // to no longer have gaps.

        for (int j = 0; j < flowsAsArray.length; j++) {
            ((YAWLFlowRelation) flowsAsArray[j]).setPriority(j);
        }
    }


    public SortedSet getFlowsInPriorityOrder() {
        TreeSet flows = new TreeSet(new FlowPriorityComparator());
        for (DecoratorPort port : getPorts()) {
            for (Object flow : port.getEdges()) {
                flows.add(flow);
            }
        }
        compressFlowPriorities(flows);
        return flows;
    }

    public String toString() {
        switch (getType()) {
            case Decorator.NO_TYPE:
            default: {
                return null;
            }
            case Decorator.AND_TYPE: {
                return "AND split";
            }
            case Decorator.OR_TYPE: {
                return "OR split";
            }
            case Decorator.XOR_TYPE: {
                return "XOR split";
            }
        }
    }

    class FlowPriorityComparator implements Comparator {

        /*
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Object obj1, Object obj2) {
            YAWLFlowRelation flow1 = (YAWLFlowRelation) obj1;
            YAWLFlowRelation flow2 = (YAWLFlowRelation) obj2;

            if (flow1.getPriority() < flow2.getPriority()) {
                return -1;                                   // 1 comes before 2
            } else {
                return 1;                                   // 1 comes after or is same as 2
            }
        }
    }
}
