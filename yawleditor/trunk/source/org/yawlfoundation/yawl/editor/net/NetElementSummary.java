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
 
package org.yawlfoundation.yawl.editor.net;

import org.yawlfoundation.yawl.editor.elements.model.InputCondition;
import org.yawlfoundation.yawl.editor.elements.model.OutputCondition;
import org.yawlfoundation.yawl.editor.elements.model.Condition;
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.elements.model.AtomicTask;
import org.yawlfoundation.yawl.editor.elements.model.MultipleAtomicTask;
import org.yawlfoundation.yawl.editor.elements.model.CompositeTask;
import org.yawlfoundation.yawl.editor.elements.model.MultipleCompositeTask;
import org.yawlfoundation.yawl.editor.elements.model.YAWLFlowRelation;
import org.yawlfoundation.yawl.editor.elements.model.VertexContainer;

import java.util.HashSet;
import java.util.Set;

public class NetElementSummary {
  private NetGraphModel model;
  
  private InputCondition inputCondition;
  private OutputCondition outputCondition;

  private HashSet flows = new HashSet();
  private HashSet conditions = new HashSet();
  private HashSet atomicTasks = new HashSet();
  private HashSet compositeTasks = new HashSet();
  private HashSet tasksWithCancellationSets = new HashSet();
  
  public NetElementSummary(NetGraphModel model) {
    assert model != null : "No NetGraphModel specified in constructor";
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
        conditions.add(cells[i]);
      }
      if (cells[i] instanceof AtomicTask ||
          cells[i] instanceof MultipleAtomicTask ) {
        atomicTasks.add(cells[i]);
        if (((YAWLTask) cells[i]).getCancellationSet().getSetMembers().size() > 0) {
          tasksWithCancellationSets.add(cells[i]);
        }
      }
      if (cells[i] instanceof CompositeTask ||
          cells[i] instanceof MultipleCompositeTask ) {
        compositeTasks.add(cells[i]);
        if (((YAWLTask) cells[i]).getCancellationSet().getSetMembers().size() > 0) {
          tasksWithCancellationSets.add(cells[i]);
        }
      }
      if (cells[i] instanceof YAWLFlowRelation) {
        flows.add(cells[i]);        
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
  
  public Set getConditions() {
    return this.conditions;
  }
  
  public Set getAtomicTasks() {
    return this.atomicTasks;
  }

  public Set getCompositeTasks() {
    return this.compositeTasks;
  }
  
  public Set getFlows() {
    return this.flows;
  }
  
  public Set getTasksWithCancellationSets() {
    return this.tasksWithCancellationSets;
  }
}
