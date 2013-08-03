/*
 * Created on 11/02/2005
 * YAWLEditor v1.1 
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

package org.yawlfoundation.yawl.editor.ui.net.utilities;

import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

/**
 * A library of standard utilities that return information on, or manipulate 
 * selected nets. 
 * 
 * @author Lindsay Bradford
 */

public final class NetUtilities {

  
  /**
   * Returns all Vertexes in the selected net. Specifically, 
   * all those verticies in the net that conform to the <code>YAWLVertex</code> interface.
   * @param net The net to search within.
   * @return The set of <code>YAWLVertex</code> objects within the selected net.
   * @see org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex
   */
  public static Set<YAWLVertex> getVertexes(NetGraphModel net) {
    HashSet<YAWLVertex> vertexList = new HashSet<YAWLVertex>();
    for(Object netRoot: NetGraphModel.getRoots(net)) {
      YAWLVertex vertex = NetCellUtilities.getVertexFromCell(netRoot);
      if (vertex != null) {
        vertexList.add(vertex);
      }
    }
    return vertexList;
  }

  /**
   * Returns all tasks in the selected net. Specifically, 
   * all those verticies in the net that conform to the <code>YAWLTask</code> interface.
   * @param net The net to search within.
   * @return The set of <code>YAWLTask</code> objects within the selected net.
   * @see org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask
   */
  
  public static Set<YAWLTask> getAllTasks(NetGraphModel net) {
    HashSet<YAWLTask> tasks = new HashSet<YAWLTask>();
    
    for(Object netRoot: NetGraphModel.getRoots(net)) {
      YAWLTask task = NetCellUtilities.getTaskFromCell(netRoot);
      if (task != null) {
        tasks.add(task);
      }
    }
    return tasks;
  }
  
  /**
   * Returns all atomic tasks in the selected net. Specifically, 
   * all those verticies in the net that conform to the <code>YAWLAtomicTask</code> interface.
   * @param net ThSe net to search within.
   * @return The set of <code>YAWLAtomicTask</code> objects within the selected net.
   * @see org.yawlfoundation.yawl.editor.ui.elements.model.YAWLAtomicTask
   */
  public static Set<YAWLAtomicTask> getAtomicTasks(NetGraphModel net) {
    HashSet<YAWLAtomicTask> atomicTasks = new HashSet<YAWLAtomicTask>();
    for (Object netRoot: NetGraphModel.getRoots(net)) {
      YAWLAtomicTask task = NetCellUtilities.getAtomicTaskFromCell(netRoot);
      if (task != null) {
        atomicTasks.add(task);
      }
    }
    return atomicTasks;
  }


  /**
   * Returns all composite tasks in the selected net. Specifically, 
   * all those verticies in the net that conform to the <code>YAWLCompositeTask</code> interface.
   * @param net The net to search within.
   * @return The set of <code>YAWLCompositeTask</code> objects within the selected net.
   * @see org.yawlfoundation.yawl.editor.ui.elements.model.YAWLCompositeTask
   */
  public static Set<YAWLCompositeTask> getCompositeTasks(NetGraphModel net) {
    HashSet<YAWLCompositeTask> compositeTasks = new HashSet<YAWLCompositeTask>();
    for (Object netRoot : NetGraphModel.getRoots(net)) {
      YAWLCompositeTask task = NetCellUtilities.getCompositeTaskFromCell(netRoot);
      if (task != null) {
        compositeTasks.add(task);
      }
    }
    return compositeTasks;
  }
  
  /**
   * Returns those tasks in the selected net that trigger cancellation set behaviour. 
   * @param net The net to search within.
   * @return The set of tasks that trigger cancellation set behaviour.
   * @see org.yawlfoundation.yawl.editor.ui.net.CancellationSet
   */
  public static Set<YAWLTask> getTasksWithCancellationSets(NetGraphModel net) {
    HashSet<YAWLTask> tasks = new HashSet<YAWLTask>();
    for (Object netRoot : NetGraphModel.getRoots(net)) {
      YAWLTask task = NetCellUtilities.getTaskFromCell(netRoot);
      if (task != null && task.getCancellationSet().size() > 0) {
        tasks.add(task);
      }
    }
    return tasks;
  }
  
  /**
   * Returns all the tasks that have flows requiring predicates from the set of net elements supplied
   * @param cells
   * @return
   */
  public static Set<YAWLTask> getTasksRequiringFlowPredicates(Set cells) {
    HashSet<YAWLTask> tasks = new HashSet<YAWLTask>();
    for(Object cell: cells) {
      if (cell instanceof YAWLFlowRelation) {
        YAWLFlowRelation flow = (YAWLFlowRelation) cell;
        if (flow.requiresPredicate()) {
          tasks.add(flow.getSourceTask());
        }
      }
    }
    return tasks;
  }

  /**
   * This should only be called by the PasteAction class as a cleanup. Trying to 
   * pre-emptively stop invalid flows from being copied/cut/deleted is simply too hard to implement
   * without significant changes to JGraph.  Instead, we allow all flows to be copied/cut, and
   * then trim out those that no longer have a valid source or target port on the paste action.
   * @param net
   * @return Object[] a set of objects that are not copyable.
   */
 
  public static Set<YAWLFlowRelation> getIllegallyCopiedFlows(NetGraphModel net) {
    HashSet<YAWLFlowRelation> illegalFlows = new HashSet<YAWLFlowRelation>();
    for (Object netRoot : NetGraphModel.getRoots(net)) {
      if (netRoot instanceof YAWLFlowRelation) {
        YAWLFlowRelation flow = (YAWLFlowRelation) netRoot;
        if (!net.contains(flow.getSource()) || !net.contains(flow.getTarget())) {
          illegalFlows.add(flow);
        }
      }
    }
    return illegalFlows;
  }
  
  /**
   * Returns the input condition of the net specified.
   * @param net
   * @return the input condition of the net specified
   */
  public static InputCondition getInputCondition(NetGraphModel net) {
    for(Object cell: NetGraphModel.getRoots(net)) {
      InputCondition condition = NetCellUtilities.getInputConditionFromCell(cell);
      if (condition != null) {
        return condition;
      }
    }
    return null;
  }

  /**
   * Returns the output condition of the net specified.
   * @param net
   * @return the output condition of the net specified
   */
  public static OutputCondition getOutputCondition(NetGraphModel net) {
    for(Object cell : NetGraphModel.getRoots(net)) {
      OutputCondition condition = NetCellUtilities.getOutputConditionFromCell(cell);
      if (condition != null) {
        return condition;
      }
    }
    return null;
  }

  /**
   * Returns all the outgoing flows from the specified cell.
   * @param cell
   * @return A set of outgoing flows from the cell
   */
  public static Set<YAWLFlowRelation> getOutgoingFlowsFrom(YAWLCell cell) {
    YAWLVertex vertex = NetCellUtilities.getVertexFromCell(cell);
    if (vertex == null) { // not interested in anything else
      return null;
    }

    HashSet<YAWLFlowRelation> flows = new HashSet<YAWLFlowRelation>();
    flows.addAll(vertex.getOutgoingFlows());
    return flows;
  }

  /**
   * Returns all the incoming flows to the specified cell.
   * @param cell
   * @return A set of incoming flows to the cell
   */

  public static Set<YAWLFlowRelation> getIncomingFlowsTo(YAWLCell cell) {
    YAWLVertex vertex = NetCellUtilities.getVertexFromCell(cell);
    if (vertex == null) { // not interested in anything else
      return null;
    }

    HashSet<YAWLFlowRelation> flows = new HashSet<YAWLFlowRelation>();
    flows.addAll(vertex.getIncomingFlows());
    return flows;
  }
  
  
  /**
   * Returns the image icon used for the starting net of a specification
   * @return
   */
  public static ImageIcon getStartingNetIcon() {
    return ResourceLoader.getImageAsIcon(
        getStartingNetIconPath()    
    );  
  }

  /**
   * Returns the Net that a composite task unfolds to. If the composite
   * task does not yet unfold to a net, it reutrns null.
   * @param task
   * @return
   */
  
  public static NetGraphModel getNetOfCompositeTask(CompositeTask task) {
    if (task.getDecomposition() == null) {
      return null;
    }

    for(NetGraphModel net : SpecificationModel.getInstance().getNets()) {
      if (net.getDecomposition() == task.getDecomposition()) {
        return net;
      }
    }
    
    return null;
  }
  
  public static String getStartingNetIconPath() {
    return "/org/yawlfoundation/yawl/editor/ui/resources/menuicons/StartingNetInternalFrame.gif";
  }

  public static ImageIcon getSubNetIcon() {
    return ResourceLoader.getImageAsIcon(
        getSubNetIconPath()    
    );  
  }
  
  public static String getSubNetIconPath() {
    return "/org/yawlfoundation/yawl/editor/ui/resources/menuicons/SubNetInternalFrame.gif";
  }

  public static ImageIcon getIconForNetModel(NetGraphModel model) {
    if (model.isRootNet()) {
      return getStartingNetIcon();
    }
    return getSubNetIcon();
  }
  
  public static void setNetIconFromModel(NetGraphModel model) {
    if (model.getGraph().getFrame() != null) {
      if (model.isRootNet()) {
        model.getGraph().getFrame().setClosable(false);
        model.getGraph().getFrame().setFrameIcon(
            NetUtilities.getStartingNetIcon()
        );
      } else {
        model.getGraph().getFrame().setClosable(true);
        model.getGraph().getFrame().setFrameIcon(
            NetUtilities.getSubNetIcon()
        );
      }
    }
  }
}
