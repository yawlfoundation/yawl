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

package org.yawlfoundation.yawl.editor.ui.engine;

import org.yawlfoundation.yawl.editor.core.controlflow.YCompoundFlow;
import org.yawlfoundation.yawl.elements.*;

import java.util.HashSet;
import java.util.Set;

public class EngineNetElementSummary {

    private YNet engineNet;
    private YInputCondition inputCondition;
    private YOutputCondition outputCondition;
    private Set<YFlow> flows = new HashSet<YFlow>();
    private Set<YCompoundFlow> compoundFlows = new HashSet<YCompoundFlow>();
    private Set<YCondition> conditions = new HashSet<YCondition>();
    private Set<YAtomicTask> atomicTasks = new HashSet<YAtomicTask>();
    private Set<YCompositeTask> compositeTasks = new HashSet<YCompositeTask>();
    private Set<YTask> tasksWithCancellationSets = new HashSet<YTask>();

    public EngineNetElementSummary(YNet engineNet) {
        assert engineNet != null : "No NetGraphModel specified in constructor";
        this.engineNet = engineNet;
        parseEngineNet();
    }

    private void parseEngineNet() {
        for (String id : engineNet.getNetElements().keySet()) {
            YExternalNetElement engineNetElement = engineNet.getNetElement(id);

            if (engineNetElement instanceof YInputCondition) {
                inputCondition = (YInputCondition) engineNetElement;
            }
            else if (engineNetElement instanceof YOutputCondition) {
                outputCondition = (YOutputCondition) engineNetElement;
            }
            else if (engineNetElement instanceof YCondition) {
                conditions.add((YCondition) engineNetElement);
            }
            else if (engineNetElement instanceof YAtomicTask) {
                YAtomicTask engineTask = (YAtomicTask) engineNetElement;
                atomicTasks.add(engineTask);
                addCancellations(engineTask);
            }
            else if (engineNetElement instanceof YCompositeTask) {
                YCompositeTask engineTask = (YCompositeTask) engineNetElement;
                compositeTasks.add(engineTask);
                addCancellations(engineTask);
            }
            flows.addAll(engineNetElement.getPostsetFlows());
        }
        rationaliseFlows();
    }


    private void rationaliseFlows() {
        Set<YCondition> implicitConditions = new HashSet<YCondition>();
        for (YCondition condition : conditions) {
            if (condition.isImplicit()) {
                YFlow flowFromSource = condition.getPresetFlows().iterator().next();
                YFlow flowIntoTarget = condition.getPostsetFlows().iterator().next();
                compoundFlows.add(
                        new YCompoundFlow(flowFromSource, condition, flowIntoTarget));
                flows.remove(flowFromSource);
                flows.remove(flowIntoTarget);
                implicitConditions.add(condition);
            }
        }
        conditions.removeAll(implicitConditions);
        for (YFlow flow : flows) {
            compoundFlows.add(new YCompoundFlow(flow));
        }
    }

    private void addCancellations(YTask engineTask) {
        if (engineTask.getRemoveSet().size() > 0) {
            tasksWithCancellationSets.add(engineTask);
        }
    }

    public YNet getEngineNet() {
        return engineNet;
    }

    public YInputCondition getInputCondition() {
        return inputCondition;
    }

    public YOutputCondition getOutputCondition() {
        return outputCondition;
    }

    public Set<YCondition> getConditions() {
        return conditions;
    }

    public Set<YAtomicTask> getAtomicTasks() {
        return atomicTasks;
    }

    public Set<YCompositeTask> getCompositeTasks() {
        return compositeTasks;
    }

    public Set<YCompoundFlow> getFlows() {
        return compoundFlows;
    }

    public Set<YTask> getTasksWithCancellationSets() {
        return tasksWithCancellationSets;
    }
}
