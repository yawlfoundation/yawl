/*
 * Created on 11/02/2005
 * YAWLEditor v1.1 
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2003 Queensland University of Technology
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

package au.edu.qut.yawl.editor.net.utilities;

import java.util.HashSet;
import java.util.Set;

import au.edu.qut.yawl.editor.elements.model.AtomicTask;
import au.edu.qut.yawl.editor.elements.model.MultipleAtomicTask;
import au.edu.qut.yawl.editor.elements.model.VertexContainer;
import au.edu.qut.yawl.editor.elements.model.YAWLAtomicTask;
import au.edu.qut.yawl.editor.elements.model.YAWLCompositeTask;
import au.edu.qut.yawl.editor.elements.model.YAWLTask;
import au.edu.qut.yawl.editor.elements.model.YAWLFlowRelation;

import au.edu.qut.yawl.editor.net.NetGraphModel;

/**
 * A library of standard utilities that return information in 
 * selected nets. 
 * 
 * @author Lindsay Bradford
 */

public final class NetUtilities {
  
  /**
   * Returns all tasks in the selected net. Specifically, 
   * all those verticies in the net that conform to the <code>YAWLTask</code> interface.
   * @param net The net to search within.
   * @return The set of <code>YAWLTask</code> objects within the selected net.
   * @see au.edu.qut.yawl.editor.elements.model.YAWLTask
   */
  
  public static Set getAllTasks(NetGraphModel net) {
    HashSet tasks = new HashSet();
    Object[] cells = NetGraphModel.getRoots(net);
    for(int i = 0; i < cells.length; i++) {
      if (cells[i] instanceof VertexContainer) {
        cells[i] = ((VertexContainer) cells[i]).getVertex();
      }
      if (cells[i] instanceof YAWLTask) {
        tasks.add(cells[i]);
      }
    }
    return tasks;
  }
  
  /**
   * Returns all atomic tasks in the selected net. Specifically, 
   * all those verticies in the net that conform to the <code>YAWLAtomicTask</code> interface.
   * @param net The net to search within.
   * @return The set of <code>YAWLAtomicTask</code> objects within the selected net.
   * @see au.edu.qut.yawl.editor.elements.model.YAWLAtomicTask
   */
  public static Set getAtomicTasks(NetGraphModel net) {
    HashSet atomicTasks = new HashSet();
    Object[] cells = NetGraphModel.getRoots(net);
    for(int i = 0; i < cells.length; i++) {
      if (cells[i] instanceof VertexContainer) {
        cells[i] = ((VertexContainer) cells[i]).getVertex();
      }
      if (cells[i] instanceof YAWLAtomicTask) {
        atomicTasks.add(cells[i]);
      }
    }
    return atomicTasks;
  }

  /**
   * Returns a boolean indicating whether given net 
   * contains an atomic task with the given label. 
   * @param net   The net to search within.
   * @param label The label to searh for.
   * @return <code>true</code> when a net has a task with the given label, <code>false</code> otherwise.
   * @see au.edu.qut.yawl.editor.elements.model.YAWLAtomicTask
   */
  public static boolean hasAtomicTaskWithLabel(NetGraphModel net, String label) {
    Object[] cells = NetGraphModel.getRoots(net);
    for(int i = 0; i < cells.length; i++) {
      if (cells[i] instanceof VertexContainer) {
        cells[i] = ((VertexContainer) cells[i]).getVertex();
      }
      if (cells[i] instanceof AtomicTask ||
          cells[i] instanceof MultipleAtomicTask) {
          String cellLabel = ((YAWLTask) cells[i]).getLabel();
        if (label.equals(cellLabel)) {
          return true;
        }
      }
    }
    return false; 
  }
  
  /**
   * Returns all composite tasks in the selected net. Specifically, 
   * all those verticies in the net that conform to the <code>YAWLCompositeTask</code> interface.
   * @param net The net to search within.
   * @return The set of <code>YAWLCompositeTask</code> objects within the selected net.
   * @see au.edu.qut.yawl.editor.elements.model.YAWLCompositeTask
   */
  public static Set getCompositeTasks(NetGraphModel net) {
    HashSet compositeTasks = new HashSet();
    Object[] cells = NetGraphModel.getRoots(net);
    for(int i = 0; i < cells.length; i++) {
      if (cells[i] instanceof VertexContainer) {
        cells[i] = ((VertexContainer) cells[i]).getVertex();
      }
      if (cells[i] instanceof YAWLCompositeTask) {
        compositeTasks.add(cells[i]);
      }
    }
    return compositeTasks;
  }
  
  /**
   * Returns those tasks in the selected net that trigger cancellation set behaviour. 
   * @param net The net to search within.
   * @return The set of tasks that trigger cancellation set behaviour.
   * @see au.edu.qut.yawl.editor.net.CancellationSet
   */
  public static Set getTasksWithCancellationSets(NetGraphModel net) {
    HashSet tasks = new HashSet();
    Object[] cells = NetGraphModel.getRoots(net);
    for(int i = 0; i < cells.length; i++) {
      if (cells[i] instanceof VertexContainer) {
        cells[i] = ((VertexContainer) cells[i]).getVertex();
      }
      if (cells[i] instanceof YAWLTask && ((YAWLTask)cells[i]).getCancellationSet().size() > 0) {
        tasks.add(cells[i]);
      }
    }
    return tasks;
  }
  
  /**
   * Returns those tasks in the selected net with split decorators.
   * @param net The net to search within.
   * @return The set of tasks with split decorators.
   * @see au.edu.qut.yawl.editor.elements.model.SplitDecorator
   */
  public static Set getTasksWitSplitDecorators(NetGraphModel net) {
    HashSet tasks = new HashSet();
    Object[] cells = NetGraphModel.getRoots(net);
    for(int i = 0; i < cells.length; i++) {
      if (cells[i] instanceof VertexContainer) {
        cells[i] = ((VertexContainer) cells[i]).getVertex();
      }
      if (cells[i] instanceof YAWLTask && ((YAWLTask)cells[i]).hasSplitDecorator()) {
        tasks.add(cells[i]);
      }
    }
    return tasks;
  }

  /**
   * Returns all flows in the selected net.
   * @param net The net to search within.
   * @return The set of flows within that net.
   * @see au.edu.qut.yawl.editor.elements.model.YAWLFlowRelation
   */

  public static Set getAllFlows(NetGraphModel net) {
    HashSet flows = new HashSet();
    Object[] cells = NetGraphModel.getRoots(net);
    for(int i = 0; i < cells.length; i++) {
      if (cells[i] instanceof YAWLFlowRelation) {
        flows.add(cells[i]);
      }
    }
    return flows;
  }
  
  /**
   * This should only be called by the PasteAction class as a cleanup. Trying to 
   * pre-emptively stop invalid flows from being copied/cut/deleted is simply too hard to implement
   * without significant changes to JGraph.  Instead, we allow all flows to be copied/cut, and
   * then trim out those that no longer have a source or target port on the paste action.
   * @param net
   * @return Object[] a set of objects that are not copyable.
   */
 
  public static Object[] getIllegallyCopiedFlows(NetGraphModel net) {
    HashSet illegalFlows = new HashSet();
    Object[] flows = NetGraphModel.getRoots(net);
    for(int i = 0; i < flows.length; i++) {
      if (flows[i] instanceof YAWLFlowRelation) {
        YAWLFlowRelation flow = (YAWLFlowRelation) flows[i];
        if (!flow.isCopyable()) {
          illegalFlows.add(flow);
        }
      }
    }
    return illegalFlows.toArray();
  }
}
