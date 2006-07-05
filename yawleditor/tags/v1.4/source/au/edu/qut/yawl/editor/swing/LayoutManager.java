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

package au.edu.qut.yawl.editor.swing;

import java.util.Set;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.HashSet;

import au.edu.qut.yawl.editor.elements.model.InputCondition;
import au.edu.qut.yawl.editor.elements.model.OutputCondition;
import au.edu.qut.yawl.editor.elements.model.YAWLCell;
import au.edu.qut.yawl.editor.elements.model.YAWLVertex;
import au.edu.qut.yawl.editor.elements.model.YAWLFlowRelation;
import au.edu.qut.yawl.editor.elements.model.Decorator;

import au.edu.qut.yawl.editor.specification.SpecificationModel;

import au.edu.qut.yawl.editor.net.NetGraphModel;

import au.edu.qut.yawl.editor.net.utilities.NetUtilities;
import au.edu.qut.yawl.editor.net.utilities.NetCellUtilities;

import org.jgraph.graph.GraphCell;

/**
 * This is a dirty, dirty hack-job at automated graph layout.
 * I'll do something pretty in my spare time.
 * @author Lindsay Bradford
 */

public class LayoutManager {
  private static final int X_BUFFER = 32;
  private static final int Y_BUFFER = 32;
  
  private static HashSet bredthTraversedNodes;
  private static LinkedList nodeBredthsList;
  private static int        largestNumberOfNodesAtSingleBredth = 0;
  
  public static void layoutSpecification() {
    
    Set nets = SpecificationModel.getInstance().getNets();

    Iterator netIterator = nets.iterator();
    while (netIterator.hasNext()) {
      layoutNet((NetGraphModel) netIterator.next());
    }
  }
  
  private static void initialise() {
    bredthTraversedNodes = new HashSet();
    nodeBredthsList = new LinkedList();
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
    
    nodeBredthsList.add(startNodeList);
    bredthTraversedNodes.add(inputCondition);
    largestNumberOfNodesAtSingleBredth = 1;

    while (getUntraversedNodesFrom((LinkedList) nodeBredthsList.getLast()).size() > 0) {
      nodeBredthsList.add(new LinkedList(getUntraversedNodesFrom((LinkedList) nodeBredthsList.getLast())));
      if (((LinkedList) nodeBredthsList.getLast()).size() > largestNumberOfNodesAtSingleBredth) {
        largestNumberOfNodesAtSingleBredth = ((LinkedList) nodeBredthsList.getLast()).size();
      }
      bredthTraversedNodes.addAll((LinkedList) nodeBredthsList.getLast());
    }
  }
  
  private static Set getUntraversedNodesFrom(LinkedList nodesAtCurrentDepth) {
    HashSet nodeSet = new HashSet();
    Iterator nodeIterator = nodesAtCurrentDepth.iterator();
    while (nodeIterator.hasNext()) {
      YAWLCell cell = (YAWLCell) nodeIterator.next();
      
      Set outgoingFlows = NetUtilities.getOutgoingFlowsFrom(cell);
      Iterator outgoingFlowIterator = outgoingFlows.iterator();
      
      while(outgoingFlowIterator.hasNext()) {
        YAWLFlowRelation flow = (YAWLFlowRelation) outgoingFlowIterator.next();
        YAWLCell targetCell = flow.getTargetVertex();
        
        if (targetCell instanceof Decorator) {
          targetCell = (YAWLCell) ((Decorator) targetCell).getParent();
        }
        if (targetCell instanceof YAWLVertex && 
            ((YAWLVertex) targetCell).getParent() != null) {
          targetCell = (YAWLCell) ((YAWLVertex) targetCell).getParent();
        }
        
        if (!bredthTraversedNodes.contains(targetCell)) {
          nodeSet.add(targetCell);
        }
      }
    }
    return nodeSet;
  }
  
  private static void ensureOutputConditionIsLast(NetGraphModel netModel) {
    OutputCondition outputCondition = NetUtilities.getOutputCondition(netModel);
    
    if (!((LinkedList) nodeBredthsList.getLast()).contains(outputCondition)) {
      
      // It's somewhere earlier in the list. Locate, and remove from there.
      
      LinkedList depthToRemove = null;
      Iterator nodeListIterator = nodeBredthsList.iterator();
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
        nodeBredthsList.remove(depthToRemove);
      }
      
      // Put output condition last.
      
      LinkedList endNodeList = new LinkedList();
      endNodeList.add(outputCondition);
      nodeBredthsList.add(endNodeList);
    }
  }
  
  private static void doGraphLayout(NetGraphModel netModel) {
    int graphNodeXPosn = X_BUFFER;
    Iterator nodeDepthIterator = nodeBredthsList.iterator();
    while (nodeDepthIterator.hasNext()) {
      LinkedList nodesAtCurrentDepth = (LinkedList) nodeDepthIterator.next();
      Iterator nodeIterator = nodesAtCurrentDepth.iterator();

      double largestXwidth = 0;

      int graphNodeYPosn = Y_BUFFER;

      while (nodeIterator.hasNext()) {
        YAWLCell cell = (YAWLCell) nodeIterator.next();
        
        netModel.getGraph().moveElementTo(
            (GraphCell) cell,
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


