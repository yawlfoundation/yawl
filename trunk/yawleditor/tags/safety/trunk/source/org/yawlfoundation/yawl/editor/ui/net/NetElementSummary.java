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
 
package org.yawlfoundation.yawl.editor.ui.net;

import org.yawlfoundation.yawl.editor.ui.elements.model.*;

import java.util.HashSet;
import java.util.Set;

public class NetElementSummary {
  private NetGraphModel model;
  
  private InputCondition inputCondition;
  private OutputCondition outputCondition;

  private Set<YAWLFlowRelation> flows = new HashSet<YAWLFlowRelation>();
  private Set<Condition> conditions = new HashSet<Condition>();
  private Set<YAWLAtomicTask> atomicTasks = new HashSet<YAWLAtomicTask>();
  private Set<YAWLCompositeTask> compositeTasks = new HashSet<YAWLCompositeTask>();
  private Set<YAWLTask> tasksWithCancellationSets = new HashSet<YAWLTask>();
  
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
        if (((YAWLTask) cells[i]).getCancellationSet().getSetMembers().size() > 0) {
          tasksWithCancellationSets.add((YAWLTask) cells[i]);
        }
      }
      if (cells[i] instanceof CompositeTask ||
          cells[i] instanceof MultipleCompositeTask ) {
        compositeTasks.add((YAWLCompositeTask) cells[i]);
        if (((YAWLTask) cells[i]).getCancellationSet().getSetMembers().size() > 0) {
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
  
  public Set<YAWLFlowRelation> getFlows() {
    return this.flows;
  }
  
  public Set<YAWLTask> getTasksWithCancellationSets() {
    return this.tasksWithCancellationSets;
  }
}
