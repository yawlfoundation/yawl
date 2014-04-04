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

import org.yawlfoundation.yawl.analyser.elements.RElement;
import org.yawlfoundation.yawl.analyser.elements.RTransition;
import org.yawlfoundation.yawl.analyser.elements.ResetWFNet;

import java.util.Set;

/**
 * Reduction rule for YAWL net: FPTY rule
 */
public class FPTRrule extends ResetReductionRule {

    /**
     * Innermost method for a reduction rule.
     * Implementation of the abstract method from superclass.
     * @param net ResetWFNet to perform reduction
     * @param netElement an  for consideration.
     * returns a reduced ResetWFNet or null if a given net cannot be reduced.
     */

    public ResetWFNet reduceElement(ResetWFNet net, RElement netElement) {
        boolean isReducible = false;
        if (netElement instanceof RTransition) {
            RTransition task = (RTransition) netElement;
            Set<RElement> postSet = task.getPostsetElements();
            Set<RElement> preSet  = task.getPresetElements();

            if (task.getRemoveSet().isEmpty()) {

                // potential candidate exists so now try and find
                // one or more other tasks
                for (RElement element : net.getNetElements().values()) {
                    if (element instanceof RTransition) {
                        Set<RElement> postSet2 = element.getPostsetElements();
                        Set<RElement> preSet2  = element.getPresetElements();
                        RTransition elementTask = (RTransition) element;

                        //two tasks are identical
                        if (postSet.equals(postSet2) && preSet.equals(preSet2) &&
                                elementTask.getRemoveSet().isEmpty() &&
                                task.getCancelledBySet().isEmpty() &&
                                element.getCancelledBySet().isEmpty() &&
                                elementTask.getRemoveSet().isEmpty() &&
                                !element.equals(task)) {

                            isReducible = true;
                            net.removeNetElement(element);
                            task.addToYawlMappings(element.getYawlMappings());
                        }
                    }
                }
            }
        }
        return isReducible ? net : null;
    }

}