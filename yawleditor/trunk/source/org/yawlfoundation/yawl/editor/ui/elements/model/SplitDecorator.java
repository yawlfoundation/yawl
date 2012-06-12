/*
 * Created on 28/12/2003, 17:20:52
 * YAWLEditor v1.0 
 * 
 * Copyright (C) 2003 Lindsay Bradford
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

package org.yawlfoundation.yawl.editor.ui.elements.model;

import org.yawlfoundation.yawl.editor.ui.util.XMLUtilities;

import java.util.*;

public class SplitDecorator extends Decorator {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * This constructor is ONLY to be invoked when we are reconstructing a decorator
     * from saved state. Ports will not be created with this constructor, as they
     * are already part of the JGraph state-space.
     */

    public SplitDecorator() {
        super();
    }

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

    public void changeDecompositionInPredicates(String oldLabel, String newLabel) {
        String oldLabelAsElement = XMLUtilities.toValidXMLName(oldLabel);
        String newLabelAsElement = XMLUtilities.toValidXMLName(newLabel);

        Iterator i = getFlowsInPriorityOrder().iterator();
        while (i.hasNext()) {
            YAWLFlowRelation flow = (YAWLFlowRelation) i.next();
            // guard against NullPointerException
            if (flow.getPredicate() == null)
                continue;
            String updatedPredicate =
                    flow.getPredicate().replaceAll(
                            "/" + oldLabelAsElement + "/",
                            "/" + newLabelAsElement + "/");
            flow.setPredicate(updatedPredicate);
        }
    }

    public void changeVariableNameInPredicates(String oldVariableName, String newVariableName) {

        Iterator i = getFlowsInPriorityOrder().iterator();
        while (i.hasNext()) {
            YAWLFlowRelation flow = (YAWLFlowRelation) i.next();
            // guard against NullPointerException
            if (flow.getPredicate() == null)
                continue;
            String updatedPredicate =
                    flow.getPredicate().replaceAll(
                            "/" + oldVariableName + "/",
                            "/" + newVariableName + "/");
            flow.setPredicate(updatedPredicate);
        }
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
