

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
import org.yawlfoundation.yawl.analyser.elements.RPlace;
import org.yawlfoundation.yawl.analyser.elements.RTransition;
import org.yawlfoundation.yawl.analyser.elements.ResetWFNet;

import java.util.Set;

/**
 * Reduction rule for RWF-net: FESR rule
 */
public class FESRrule extends ResetReductionRule{

    /**
     * Innermost method for a reduction rule.
     * Implementation of the abstract method from superclass.
     * @param net ResetWFNet to perform reduction
     * @param netElement an  for consideration.
     * returns a reduced ResetWFNet or null if a given net cannot be reduced.
     */
    public ResetWFNet reduceElement(ResetWFNet net, RElement netElement) {
        boolean isReducible = false;
        if (netElement instanceof RPlace) {
            RPlace p = (RPlace) netElement;
            Set<RElement> postSet = p.getPostsetElements();
            Set<RElement> preSet  = p.getPresetElements();

            //check if more than one && preset and postset transitions have exactly one
            if (preSet.size() > 1 && postSet.size() >1 &&
                    checkTransitionsOnePrePost(preSet) &&
                    checkTransitionsOnePrePost(postSet)) {

                // potential candidate exits so now try and find
                // one or more other ps
                for (RElement p2 : net.getNetElements().values()) {
                    if ((p != p2) && (p2 instanceof RPlace)) {
                        Set<RElement> postSet2 = p2.getPostsetElements();
                        Set<RElement> preSet2  = p2.getPresetElements();

                        // found another place
                        if (preSet2.size() > 1 && postSet2.size() > 1 &&
                                checkTransitionsOnePrePost(preSet2) &&
                                checkTransitionsOnePrePost(postSet2) &&
                                p2.getCancelledBySet().equals(p.getCancelledBySet()) &&
                                checkForEquivalence(preSet, preSet2) &&
                                checkForEquivalence(postSet, postSet2)) {

                            isReducible = true;
                            net.removeNetElement(p2);
                            removeFromNet(net, preSet2);
                            removeFromNet(net, postSet2);
                        }
                    }
                }
            }
        }
        return isReducible ? net : null;
    }

    private boolean checkTransitionsOnePrePost(Set<RElement> elements) {
        for (RElement element : elements) {
            if (! (element.getPresetElements().size() == 1 &&
                   element.getPostsetElements().size() ==1)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkForEquivalence(Set<RElement> transitions1,
                                        Set<RElement> transitions2) {
        if (transitions1.size() == transitions2.size()) {
            Set<RElement> preSet = ResetWFNet.getPreset(transitions1);
            Set<RElement> preSet2 = ResetWFNet.getPreset(transitions2);
            Set<RElement> postSet = ResetWFNet.getPostset(transitions1);
            Set<RElement> postSet2 = ResetWFNet.getPostset(transitions2);

            if (preSet.equals(preSet2) && postSet.equals(postSet2)) {

                //now consider individual transition and compare removeset
                for (RElement re : transitions1) {
                     if (! hasEquivalentTransition((RTransition) re, transitions2)) {
                        return false;
                    }
                }
            }
            else return false;
        }
        return true;
    }


    private boolean hasEquivalentTransition(RTransition t, Set<RElement> transitions) {
        Set<RElement> removeSet = t.getRemoveSet();
        Set<RElement> preSetOft = t.getPresetElements();
        Set<RElement> postSetOft = t.getPostsetElements();

        for (RElement t2 : transitions) {
            Set<RElement> removeSet2 = ((RTransition) t2).getRemoveSet();
            Set<RElement> preSetOft2 = t2.getPresetElements();
            Set<RElement> postSetOft2 = t2.getPostsetElements();

            if (preSetOft.equals(preSetOft2) && postSetOft.equals(postSetOft2) &&
                    removeSet.equals(removeSet2)) {
                return true;
            }
        }
        return false;
    }


    private void removeFromNet(ResetWFNet net, Set<RElement> elements) {
        for (RElement element : elements) {
            net.removeNetElement(element);
        }
    }

}