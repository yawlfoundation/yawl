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
 
package org.yawlfoundation.yawl.editor.ui.net;

import org.yawlfoundation.yawl.editor.ui.elements.model.*;

import java.util.HashSet;
import java.util.Set;

public class NetElementSummary {
  private final NetGraphModel model;
  
  private InputCondition inputCondition;
  private OutputCondition outputCondition;

  private final Set<YAWLFlowRelation> flows = new HashSet<YAWLFlowRelation>();
  private final Set<Condition> conditions = new HashSet<Condition>();
  private final Set<YAWLAtomicTask> atomicTasks = new HashSet<YAWLAtomicTask>();
  private final Set<YAWLCompositeTask> compositeTasks = new HashSet<YAWLCompositeTask>();
    private final Set<YAWLTask> tasks = new HashSet<YAWLTask>();
  private final Set<YAWLTask> tasksWithCancellationSets = new HashSet<YAWLTask>();
  
  public NetElementSummary(NetGraphModel model) {
    this.model = model;
    parseModel();
  }

  private void parseModel() {
    Object[] cells = NetGraphModel.getRoots(model);    
    for (int i=0; i < cells.length; i++) {
      if (cells[i] instanceof VertexContainer) {
         cells[i] = ((VertexContainer) cells[i]).getVertex();        
      }
      if (cells[i] instanceof InputCondition) {
        inputCondition = (InputCondition) cells[i];
      }
      if (cells[i] instanceof OutputCondition) {
        outputCondition = (OutputCondition) cells[i];
      }
      if (cells[i] instanceof Condition) {
        conditions.add((Condition) cells[i]);
      }
      if (cells[i] instanceof AtomicTask ||
          cells[i] instanceof MultipleAtomicTask ) {
        atomicTasks.add((YAWLAtomicTask) cells[i]);
          tasks.add((YAWLTask) cells[i]);
        if (((YAWLTask) cells[i]).getCancellationSet().getMembers().size() > 0) {
          tasksWithCancellationSets.add((YAWLTask) cells[i]);
        }
      }
      if (cells[i] instanceof CompositeTask ||
          cells[i] instanceof MultipleCompositeTask ) {
        compositeTasks.add((YAWLCompositeTask) cells[i]);
          tasks.add((YAWLTask) cells[i]);
        if (((YAWLTask) cells[i]).getCancellationSet().getMembers().size() > 0) {
          tasksWithCancellationSets.add((YAWLTask) cells[i]);
        }
      }
      if (cells[i] instanceof YAWLFlowRelation) {
        flows.add((YAWLFlowRelation) cells[i]);
      }
    }
  }

  public NetGraphModel getModel() {
    return this.model;
  }

  public InputCondition getInputCondition() {
    return this.inputCondition;
  }

  public OutputCondition getOutputCondition() {
    return this.outputCondition;
  }
  
  public Set<Condition> getConditions() {
    return this.conditions;
  }
  
  public Set<YAWLAtomicTask> getAtomicTasks() {
    return this.atomicTasks;
  }

  public Set<YAWLCompositeTask> getCompositeTasks() {
    return this.compositeTasks;
  }

    public Set<YAWLTask> getTasks() { return tasks; }

  public Set<YAWLFlowRelation> getFlows() {
    return this.flows;
  }
  
  public Set<YAWLTask> getTasksWithCancellationSets() {
    return this.tasksWithCancellationSets;
  }
}
