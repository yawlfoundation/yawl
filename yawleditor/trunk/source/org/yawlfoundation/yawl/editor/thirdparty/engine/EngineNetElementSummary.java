/*
 * Created on 01/04/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * 
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

package org.yawlfoundation.yawl.editor.thirdparty.engine;

import org.yawlfoundation.yawl.elements.*;

import java.util.HashSet;
import java.util.Set;

public class EngineNetElementSummary {

    private YNet engineNet;
    private YInputCondition inputCondition;
    private YOutputCondition outputCondition;
    private Set<YFlow> flows = new HashSet<YFlow>();
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

    public Set<YFlow> getFlows() {
        return flows;
    }

    public Set<YTask> getTasksWithCancellationSets() {
        return tasksWithCancellationSets;
    }
}
