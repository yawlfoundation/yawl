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
 
package au.edu.qut.yawl.editor.thirdparty.engine;

import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YInputCondition;
import org.yawlfoundation.yawl.elements.YOutputCondition;
import org.yawlfoundation.yawl.elements.YCondition;
import org.yawlfoundation.yawl.elements.YAtomicTask;
import org.yawlfoundation.yawl.elements.YCompositeTask;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class EngineNetElementSummary {
  private YNet engineNet;
  
  private YInputCondition inputCondition;
  private YOutputCondition outputCondition;

  private HashSet flows = new HashSet();
  private HashSet conditions = new HashSet();
  private HashSet atomicTasks = new HashSet();
  private HashSet compositeTasks = new HashSet();
  private HashSet tasksWithCancellationSets = new HashSet();
  
  public EngineNetElementSummary(YNet engineNet) {
    assert engineNet != null : "No NetGraphModel specificed in constructor";
    this.engineNet = engineNet;
    parseEngineNet();
  }

  private void parseEngineNet() {
    
    Iterator engineNetElementIterator = engineNet.getNetElements().keySet().iterator();

    while(engineNetElementIterator.hasNext()) {
      Object engineNetElementKey = engineNetElementIterator.next();
      Object engineNetElement = engineNet.getNetElement((String) engineNetElementKey);
      
      if (engineNetElement instanceof YInputCondition) {
        inputCondition = (YInputCondition) engineNetElement;
        flows.addAll(((YCondition)engineNetElement).getPostsetFlows());
      }
      if (engineNetElement instanceof YOutputCondition) {
        outputCondition = (YOutputCondition) engineNetElement;
      }
      if (engineNetElement instanceof YCondition &&
          !(engineNetElement instanceof YInputCondition) &&
          !(engineNetElement instanceof YOutputCondition)) {
        conditions.add(engineNetElement);
        flows.addAll(((YCondition)engineNetElement).getPostsetFlows());
      }
      if (engineNetElement instanceof YAtomicTask) {
        atomicTasks.add(engineNetElement);
        YAtomicTask engineTask = (YAtomicTask) engineNetElement;
        flows.addAll(engineTask.getPostsetFlows());
        if (engineTask.getRemoveSet().size() > 0) {
          tasksWithCancellationSets.add(engineTask);
        }
      }
      if (engineNetElement instanceof YCompositeTask) {
        compositeTasks.add(engineNetElement);
        YCompositeTask engineTask = (YCompositeTask) engineNetElement;
        flows.addAll(engineTask.getPostsetFlows());
        if (engineTask.getRemoveSet().size() > 0) {
          tasksWithCancellationSets.add(engineTask);
        }
      }
    }
  }
    
  public YNet getEngineNet() {
    return this.engineNet;
  }

  public YInputCondition getInputCondition() {
    return this.inputCondition;
  }

  public YOutputCondition getOutputCondition() {
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
