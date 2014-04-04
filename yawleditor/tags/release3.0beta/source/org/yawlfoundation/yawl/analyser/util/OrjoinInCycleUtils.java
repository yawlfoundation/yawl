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

package org.yawlfoundation.yawl.analyser.util;

import org.yawlfoundation.yawl.elements.YExternalNetElement;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.util.StringUtil;

import java.util.*;

public class OrjoinInCycleUtils {

    /**
     * checks whether two or more OR-joins are in a cycle, resulting in a potential 
     * vicious circle.
     */
    public String checkORjoinsInCycle(YNet net) {
        String msg = checkForCycles(net);
        if (msg.length() == 0) {
            msg = formatXMLMessage("The net " + net.getID() +
                    " has no OR-joins in a cycle.", true);
        }
        return msg;
    }


    public boolean hasORjoinsInCycle(YNet net) {
        return checkForCycles(net).length() > 0;                  // has error messages
    }

    /*****************************************************************************/

    private String checkForCycles(YNet net) {

        //First, get all the OR-joins in the net
        Set<YTask> ORJoins = getOrJoins(net);

        // if at least 2 or joins, check predecessors for each OR-join
        if (ORJoins.size() > 1) {
            return checkPredecessors(getPredecessors(ORJoins));
        }
        return "";
    }


    private Set<YTask> getOrJoins(YNet net) {
        Set<YTask> ORJoins = new HashSet<YTask>();
        for (YTask task : net.getNetTasks()) {
            if (task.getJoinType() == YTask._OR) ORJoins.add(task);
        }
        return ORJoins;
    }


    private Map<YTask, Set<YExternalNetElement>> getPredecessors(Set<YTask> ORJoins) {
        Map<YTask, Set<YExternalNetElement>> ojPresets =
                new HashMap<YTask, Set<YExternalNetElement>>();
        for (YTask task : ORJoins) {
            Set<YExternalNetElement> preSet = task.getPresetElements();
            Set<YExternalNetElement> predecessors = new HashSet<YExternalNetElement>(preSet);
            Set<YExternalNetElement> predTemp = new HashSet<YExternalNetElement>();
            do {
                predTemp.clear();
                predTemp.addAll(predecessors);
                predecessors.addAll(YNet.getPreset(predTemp));
            } while(! predTemp.containsAll(predecessors));

            //keep all OR-joins in presets
            predecessors.retainAll(ORJoins);

            //if the OR-join in question is in its own preset, remove it.
            predecessors.remove(task);
            ojPresets.put(task, predecessors);
        }
        return ojPresets;
    }


    private String checkPredecessors(Map<YTask, Set<YExternalNetElement>> ojPresets) {
        String msg = "";
        for (YTask firstJoin : ojPresets.keySet()) {
            Set<YExternalNetElement> firstPreSet = ojPresets.get(firstJoin);
            if (firstPreSet != null) {
                for (YExternalNetElement elem : firstPreSet) {
                    if (elem instanceof YTask) {
                        YTask secondJoin = (YTask) elem;
                        Set<YExternalNetElement> secondPreSet = ojPresets.get(secondJoin);
                        if (secondPreSet !=null && secondPreSet.contains(firstJoin)) {

                            //remove firstJoin so that there is no duplicate error msgs.
                            secondPreSet.remove(firstJoin);
                            ojPresets.put(secondJoin, secondPreSet);
                            String ms = "OR-joins " + firstJoin.getID() + " and " +
                                    secondJoin.getID() + " are on a cycle.";
                            msg += formatXMLMessage(ms, false);
                        }
                    }
                }
            }
        }
        return msg;
    }


    /**
     * used for formatting xml messages.
     * Message could be a warning or observation.
     */
    private String formatXMLMessage(String msg, boolean isObservation) {
        return StringUtil.wrap(msg, isObservation ? "observation" : "warning");
    }

}
