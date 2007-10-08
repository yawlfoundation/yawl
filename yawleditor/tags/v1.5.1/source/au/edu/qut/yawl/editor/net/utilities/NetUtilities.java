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

package au.edu.qut.yawl.editor.net.utilities;

import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;

import au.edu.qut.yawl.editor.elements.model.VertexContainer;
import au.edu.qut.yawl.editor.elements.model.YAWLAtomicTask;
import au.edu.qut.yawl.editor.elements.model.YAWLCompositeTask;
import au.edu.qut.yawl.editor.elements.model.YAWLTask;
import au.edu.qut.yawl.editor.elements.model.YAWLVertex;
import au.edu.qut.yawl.editor.elements.model.YAWLFlowRelation;
import au.edu.qut.yawl.editor.elements.model.InputCondition;
import au.edu.qut.yawl.editor.elements.model.OutputCondition;
import au.edu.qut.yawl.editor.elements.model.YAWLCell;
import au.edu.qut.yawl.editor.foundations.ResourceLoader;

import au.edu.qut.yawl.editor.net.NetGraphModel;

/**
 * A library of standard utilities that return information on, or manipulate 
 * selected nets. 
 * 
 * @author Lindsay Bradford
 */

public final class NetUtilities {
  
  /**
   * Scans the current net looking for the largest Engine ID number registered
   * against the vertex entries of that net.
   * @param net
   * @return largest Engine Id Number used in the net.
   */
  
  public static long getLargestEngineIdNumberWithin(NetGraphModel net) {
    long largestIDSoFar = 0;
    for (Object netRoot : NetGraphModel.getRoots(net)) {
      if (netRoot instanceof VertexContainer) {
        netRoot = ((VertexContainer) netRoot).getVertex();
      }
      if (netRoot instanceof YAWLVertex) {
        long vertexId = Long.parseLong(((YAWLVertex) netRoot).getEngineIdNumber());
        if (vertexId > largestIDSoFar) {
          largestIDSoFar = vertexId;
        }
      }
    }
    return largestIDSoFar;
  }

  
  /**
   * Returns all Vertexes in the selected net. Specifically, 
   * all those verticies in the net that conform to the <code>YAWLVertex</code> interface.
   * @param net The net to search within.
   * @return The set of <code>YAWLVertex</code> objects within the selected net.
   * @see au.edu.qut.yawl.editor.elements.model.YAWLVertex
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
   * @see au.edu.qut.yawl.editor.elements.model.YAWLTask
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
   * @see au.edu.qut.yawl.editor.elements.model.YAWLAtomicTask
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
   * Returns a boolean indicating whether given net 
   * contains an atomic task with the given label. 
   * @param net   The net to search within.
   * @param label The label to searh for.
   * @return <code>true</code> when a net has a task with the given label, <code>false</code> otherwise.
   * @see au.edu.qut.yawl.editor.elements.model.YAWLAtomicTask
   */
  public static boolean hasAtomicTaskWithLabel(NetGraphModel net, String label) {
    for (Object netRoot : NetGraphModel.getRoots(net)) {
      YAWLAtomicTask task = NetCellUtilities.getAtomicTaskFromCell(netRoot);
      if (task != null) {
        if (label.equals(task.getLabel())) {
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
   * @see au.edu.qut.yawl.editor.net.CancellationSet
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
   * Returns those tasks in the selected net with split decorators.
   * @param net The net to search within.
   * @return The set of tasks with split decorators.
   * @see au.edu.qut.yawl.editor.elements.model.SplitDecorator
   */
  public static Set<YAWLTask> getTasksWitSplitDecorators(NetGraphModel net) {
    HashSet<YAWLTask> tasks = new HashSet<YAWLTask>();
    for (Object netRoot :  NetGraphModel.getRoots(net)) {
      YAWLTask task = NetCellUtilities.getTaskFromCell(netRoot);
      if (task != null && task.hasSplitDecorator() ) {
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
   * Returns all flows in the selected net.
   * @param net The net to search within.
   * @return The set of flows within that net.
   * @see au.edu.qut.yawl.editor.elements.model.YAWLFlowRelation
   */

  public static Set<YAWLFlowRelation> getAllFlows(NetGraphModel net) {
    HashSet<YAWLFlowRelation> flows = new HashSet<YAWLFlowRelation>();
    for(Object netRoot : NetGraphModel.getRoots(net)) {
      if (netRoot instanceof YAWLFlowRelation) {
        flows.add((YAWLFlowRelation) netRoot);
      }
    }
    return flows;
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

    HashSet flows = new HashSet();
    flows.addAll(vertex.getOutgoingFlows());
    return flows;
  }

  /**
   * Returns all the incomming flows to the specified cell.
   * @param cell
   * @return A set of incomming flows to the cell
   */

  public static Set<YAWLFlowRelation> getIncomingFlowsFrom(YAWLCell cell) {
    YAWLVertex vertex = NetCellUtilities.getVertexFromCell(cell);
    if (vertex == null) { // not interested in anything else
      return null;
    }

    HashSet flows = new HashSet();
    flows.addAll(vertex.getIncomingFlows());
    return flows;
  }

  public static ImageIcon getStartingNetIcon() {
    return ResourceLoader.getImageAsIcon(
        getStartingNetIconPath()    
    );  
  }
  
  public static String getStartingNetIconPath() {
    return "/au/edu/qut/yawl/editor/resources/menuicons/StartingNetInternalFrame.gif";
  }

  public static ImageIcon getSubNetIcon() {
    return ResourceLoader.getImageAsIcon(
        getSubNetIconPath()    
    );  
  }
  
  public static String getSubNetIconPath() {
    return "/au/edu/qut/yawl/editor/resources/menuicons/SubNetInternalFrame.gif";
  }

  public static ImageIcon getIconForNetModel(NetGraphModel model) {
    if (model.isStartingNet()) {
      return getStartingNetIcon();
    }
    return getSubNetIcon();
  }
  
  public static void setNetIconFromModel(NetGraphModel model) {
    if (model.getGraph().getFrame() != null) {
      if (model.isStartingNet()) {
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
