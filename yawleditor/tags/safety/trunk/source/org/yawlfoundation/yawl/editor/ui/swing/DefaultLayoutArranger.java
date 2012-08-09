/*
 * Created on 21/02/2006
 * YAWLEditor v1.4 
 *
 * @author Lindsay Bradford
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

package org.yawlfoundation.yawl.editor.ui.swing;

import org.jgraph.graph.GraphCell;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetCellUtilities;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetUtilities;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 * This is a dirty, dirty hack-job at automated graph layout.
 * I'll do something pretty in my spare time.
 * @author Lindsay Bradford
 */

public class DefaultLayoutArranger {
  private static final int X_BUFFER = 32;
  private static final int Y_BUFFER = 32;
  
  private static HashSet breadthTraversedNodes;
  private static LinkedList nodeBreadthsList;
  private static int largestNumberOfNodesAtSingleBreadth = 0;
  
  public static void layoutSpecification() {
    for(NetGraphModel net : SpecificationModel.getInstance().getNets()) {
      layoutNet(net);
    }
  }

  private static void initialise() {
    breadthTraversedNodes = new HashSet();
    nodeBreadthsList = new LinkedList();
  }
  
  private static void layoutNet(NetGraphModel netModel) {
    initialise();

    traverseGraphToDetermineNodeBredth(netModel);
    ensureOutputConditionIsLast(netModel);
    doGraphLayout(netModel);
  }
  
  private static void traverseGraphToDetermineNodeBredth(NetGraphModel netModel) {
    InputCondition inputCondition = NetUtilities.getInputCondition(netModel);

    LinkedList startNodeList = new LinkedList();
    startNodeList.add(inputCondition);
    
    nodeBreadthsList.add(startNodeList);
    breadthTraversedNodes.add(inputCondition);
    largestNumberOfNodesAtSingleBreadth = 1;

    while (getUntraversedNodesFrom((LinkedList) nodeBreadthsList.getLast()).size() > 0) {
      nodeBreadthsList.add(new LinkedList(getUntraversedNodesFrom((LinkedList) nodeBreadthsList.getLast())));
      if (((LinkedList) nodeBreadthsList.getLast()).size() > largestNumberOfNodesAtSingleBreadth) {
        largestNumberOfNodesAtSingleBreadth = ((LinkedList) nodeBreadthsList.getLast()).size();
      }
      breadthTraversedNodes.addAll((LinkedList) nodeBreadthsList.getLast());
    }
  }
  
  private static Set getUntraversedNodesFrom(LinkedList nodesAtCurrentDepth) {
    HashSet nodeSet = new HashSet();
    Iterator nodeIterator = nodesAtCurrentDepth.iterator();
    while (nodeIterator.hasNext()) {
      YAWLCell cell = (YAWLCell) nodeIterator.next();
      
      for(YAWLFlowRelation flow : NetUtilities.getOutgoingFlowsFrom(cell)) {
        YAWLCell targetCell = NetCellUtilities.getVertexFromCell(flow.getTargetVertex());
        
        if (!breadthTraversedNodes.contains(targetCell)) {
          nodeSet.add(targetCell);
        }
      }
    }
    return nodeSet;
  }
  
  private static void ensureOutputConditionIsLast(NetGraphModel netModel) {
    OutputCondition outputCondition = NetUtilities.getOutputCondition(netModel);
    
    if (!((LinkedList) nodeBreadthsList.getLast()).contains(outputCondition)) {
      
      // It's somewhere earlier in the list. Locate, and remove from there.
      
      LinkedList depthToRemove = null;
      Iterator nodeListIterator = nodeBreadthsList.iterator();
      while(nodeListIterator.hasNext()) {
        LinkedList nodes = (LinkedList) nodeListIterator.next();
        if (nodes.contains(outputCondition)) {
          nodes.remove(outputCondition);
          if (nodes.size() == 0) {
            depthToRemove = nodes;
          }
        }
      }
      if (depthToRemove != null) {
        nodeBreadthsList.remove(depthToRemove);
      }
      
      // Put output condition last.
      
      LinkedList endNodeList = new LinkedList();
      endNodeList.add(outputCondition);
      nodeBreadthsList.add(endNodeList);
    }
  }
  
  private static void doGraphLayout(NetGraphModel netModel) {
    int graphNodeXPosn = X_BUFFER;
    Iterator nodeDepthIterator = nodeBreadthsList.iterator();
    while (nodeDepthIterator.hasNext()) {
      LinkedList nodesAtCurrentDepth = (LinkedList) nodeDepthIterator.next();
      Iterator nodeIterator = nodesAtCurrentDepth.iterator();

      double largestXwidth = 0;

      int graphNodeYPosn = Y_BUFFER;

      while (nodeIterator.hasNext()) {
        GraphCell cell  = (GraphCell) nodeIterator.next();
        
        if (cell instanceof YAWLVertex  && ((YAWLVertex) cell).getParent() != null) {
          cell = (GraphCell) ((YAWLVertex)cell).getParent();
        }
        
        netModel.getGraph().moveElementTo(
            cell,
            graphNodeXPosn,
            graphNodeYPosn
        );

        
        if (netModel.getGraph().getCellBounds(cell).getWidth() > largestXwidth) {
          largestXwidth = netModel.getGraph().getCellBounds(cell).getWidth();
        }
        graphNodeYPosn = graphNodeYPosn + (int) netModel.getGraph().getCellBounds(cell).getHeight() + Y_BUFFER;

      }
      NetCellUtilities.alignCellsAlongVerticalCentre(netModel.getGraph(),nodesAtCurrentDepth.toArray());
      
      graphNodeXPosn = graphNodeXPosn + (int) largestXwidth + + X_BUFFER;
    }
  }
}