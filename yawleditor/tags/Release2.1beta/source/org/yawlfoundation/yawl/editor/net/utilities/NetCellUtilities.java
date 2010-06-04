/*
 * Created on 12/11/2004
 * YAWLEditor v1.01 
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
 */
package org.yawlfoundation.yawl.editor.net.utilities;

import org.jgraph.graph.*;
import org.yawlfoundation.yawl.editor.data.DataVariable;
import org.yawlfoundation.yawl.editor.data.WebServiceDecomposition;
import org.yawlfoundation.yawl.editor.elements.model.*;
import org.yawlfoundation.yawl.editor.foundations.XMLUtilities;
import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

public class NetCellUtilities {

  public static void propogateFontChangeAcrossNet(NetGraph net, Font font) {
    
    if (net.getNetModel() == null) {
     return; 
    }
    
    net.getNetModel().beginUpdate();
      
    propogateFontChangeAcrossNetNoUndo(net,font);
    
    net.getNetModel().endUpdate();
  }

  public static void propogateFontChangeAcrossNetNoUndo(NetGraph net, Font font) {

    net.setFont(font);

    List cells = NetGraphModel.getDescendants(
        net.getNetModel(), 
        NetGraphModel.getRoots(net.getNetModel())
    );
    
    Iterator vertexContainerIterator = cells.iterator();
    while(vertexContainerIterator.hasNext()) {
      GraphCell cell = (GraphCell) vertexContainerIterator.next();
      if (cell instanceof VertexContainer) {
        VertexContainer container = (VertexContainer) cell;
        if (container.getLabel() != null) {
          String label = container.getLabel().getLabel();
          
          // cheap and nasty way of getting the label to position nicely
          // with the vertex is to delete, and set it again, letting 
          // the method do the math for positioning them in relation 
          // to each other.
          
          net.setElementLabelInsideUpdate(container.getVertex(),"");
          net.setElementLabelInsideUpdate(container.getVertex(),label);
        }
      }
    }

  }

  public static void translateView(NetGraph net, CellView view, double x, double y) {
    translateViews(net, new CellView[] { view }, x, y);
  }
  
  public static void translateViews(NetGraph net, CellView[] views, double x, double y) {
    for(CellView view: views) {
      Rectangle2D oldBounds = view.getBounds();
      Rectangle2D.Double newBounds = 
        new Rectangle2D.Double(
            oldBounds.getX() + x,
            oldBounds.getY() + y,
            oldBounds.getWidth(),
            oldBounds.getHeight()
        );
      if (newBounds.getX() < 0 || newBounds.getY() < 0) {
        return;
      }
      if (view instanceof VertexView) {
        ((VertexView)view).setBounds(newBounds);
      }
      if (view instanceof EdgeView) {
        EdgeView edgeView = (EdgeView) view;
        
        for (int j = 0; j < edgeView.getPointCount(); j++) {
          Point2D oldPoint = edgeView.getPoint(j);
          Point2D.Double newPoint = new Point2D.Double(
              oldPoint.getX() + x,
              oldPoint.getY() + y
          );
          edgeView.setPoint(j, newPoint);
        }
      }
    }
    
    net.setGridEnabled(false);
    applyViewChange(net, views);
    net.setGridEnabled(true);
  }
  
  public static void moveViewToLocation(NetGraph net, CellView view, double x, double y) {
    moveViewsToLocation(net, new CellView[] { view }, x, y);
  }
  
  public static void moveViewsToLocation(NetGraph net, CellView[] views, double x, double y) {
    for(CellView view: views) {
      Rectangle2D oldBounds = view.getBounds();

      Rectangle2D.Double newBounds = 
        new Rectangle2D.Double(
            x,
            y,
            oldBounds.getWidth(),
            oldBounds.getHeight()
      );

      if (view instanceof VertexView) {
        ((VertexView)view).setBounds(newBounds);
      }
    }
    
    net.setGridEnabled(false);
    applyViewChange(net, views);
    net.setGridEnabled(true);
  }
  
  public static void resizeViews(NetGraph net, CellView[] views, double width, double height) {
    for(int i = 0; i < views.length; i++) {
      resizeView(net,views[i],width,height);
    }
  }

  public static void resizeView(NetGraph net, CellView view, double width, double height) {
    Rectangle2D oldBounds = view.getBounds();

    Rectangle2D.Double newBounds = 
      new Rectangle2D.Double(
          oldBounds.getX(),
          oldBounds.getY(),
          oldBounds.getWidth() + width,
          oldBounds.getHeight() + height
      );
    
    net.setGridEnabled(false);

    HashMap map = new HashMap();
    GraphConstants.setBounds(map,newBounds);
    
    net.getGraphLayoutCache().editCell(view.getCell(),map);
    net.setGridEnabled(true);
  }
  
  public static void applyViewChange(NetGraph net, CellView view) {
    applyViewChange(net, new CellView[] { view } );
  }
  
  public static void applyViewChange(NetGraph net, CellView[] views) {
    CellView[] allViews =
      VertexView.getDescendantViews(views);
    Map attributes = GraphConstants.createAttributes(allViews, null);
    net.getNetModel().edit(attributes, null, null, null);
    scrollNetToShowCells(net, getCellsOfViews(views));
  }
  
  public static void alignCellsAlongTop(NetGraph net, Object[] cells) {
    // a retrofit of source from jgraphpad.
    if (cells != null) {
      Rectangle2D r = net.getCellBounds(cells);
      net.getNetModel().beginUpdate();
      
      for(Object cell: cells) {
        Rectangle2D bounds = net.getCellBounds(cell);
        net.moveElementBy(
            (GraphCell) cell, 
            0, 
            (-1 * bounds.getY()) + r.getY()
        );
      }

      net.getNetModel().endUpdate();
      net.requestFocus();
    }
  }

  public static void alignCellsAlongHorizontalCentre(NetGraph net, Object[] cells) {
    // a retrofit of source from jgraphpad.
    if (cells != null) {
      Rectangle2D r = net.getCellBounds(cells);
      double cy = r.getHeight() / 2;
      net.getNetModel().beginUpdate();
      
      for(Object cell: cells) {
        Rectangle2D bounds = net.getCellBounds(cell);
        net.moveElementBy(
            (GraphCell) cell, 
            0, 
            (-1 * bounds.getY()) + r.getY() + cy - bounds.getHeight() / 2
        );
      }
      
      net.getNetModel().endUpdate();
      net.requestFocus();
    }
  }
  
  public static void alignCellsAlongBottom(NetGraph net, Object[] cells) {
    // a retrofit of source from jgraphpad.
    if (cells != null) {
      Rectangle2D r = net.getCellBounds(cells);
      net.getNetModel().beginUpdate();
      
      for(Object cell: cells) {
        Rectangle2D bounds = net.getCellBounds(cell);
        net.moveElementBy(
            (GraphCell) cell, 
            0, 
            (-1 * bounds.getY()) + r.getY() + r.getHeight() - bounds.getHeight()
        );
      }

      net.getNetModel().endUpdate();
      net.requestFocus();
    }
  }

  public static void alignCellsAlongLeft(NetGraph net, Object[] cells) {
    // a retrofit of source from jgraphpad.
    if (cells != null) {
      Rectangle2D r = net.getCellBounds(cells);
      net.getNetModel().beginUpdate();
      
      for(Object cell: cells) {
        Rectangle2D bounds = net.getCellBounds(cell);
        net.moveElementBy(
            (GraphCell) cell, 
            (-1 * bounds.getX())+ r.getX(), 
            0
        );
      }
      
      net.getNetModel().endUpdate();
      net.requestFocus();
    }
  }

  public static void alignCellsAlongVerticalCentre(NetGraph net, Object[] cells) {
    // a retrofit of source from jgraphpad.
    if (cells != null) {
      Rectangle2D r = net.getCellBounds(cells);
      double cx = r.getWidth() / 2;
      net.getNetModel().beginUpdate();
      
      for(Object cell: cells) {
        Rectangle2D bounds = net.getCellBounds(cell);
        net.moveElementBy(
            (GraphCell) cell, 
            (-1 * bounds.getX()) + r.getX() + cx - bounds.getWidth() / 2, 
            0
        );
      }

      net.getNetModel().endUpdate();
      net.requestFocus();
    }
  }
  
  public static void alignCellsAlongRight(NetGraph net, Object[] cells) {
    // a retrofit of source from jgraphpad.
    if (cells != null) {
      Rectangle2D r = net.getCellBounds(cells);
      net.getNetModel().beginUpdate();
      
      for(Object cell: cells) {
        Rectangle2D bounds = net.getCellBounds(cell);
        net.moveElementBy(
            (GraphCell) cell, 
            (-1 * bounds.getX())
            + r.getX() + r.getWidth() - bounds.getWidth(), 
            0
        );
      }

      net.getNetModel().endUpdate();
      net.requestFocus();
    }
  }

  public static void setFlowStyle(NetGraph net, YAWLFlowRelation flow, int flowStyle) {
    Map flowMap = GraphConstants.createAttributes(
        flow, 
        GraphConstants.LINESTYLE, 
        flowStyle
    );
    
    HashMap nestedMap = new HashMap();
    nestedMap.put(flow, flowMap);

    net.getNetModel().edit(
        flowMap, 
        null, 
        null, 
        null
    );
  }
  
  public static int getFlowLineStyle(NetGraph net, YAWLFlowRelation flow) {
    return GraphConstants.getLineStyle(
        net.getViewFor(flow).getAllAttributes()
    );
  }
  
  public static void togglePointOnFlow(NetGraph net, YAWLFlowRelation flow, Point point) {
    
    Point2D pointFoundForDeletion = null;
    
    List flowPoints = GraphConstants.getPoints(
        net.getViewFor(flow).getAllAttributes()
    );
 
    for(int i = 0; i < flowPoints.size(); i++) {
      // Because we have ports in the list too, focus just on the extra points.
      if (flowPoints.get(i) instanceof Point2D) {  
        Point2D thisPoint = (Point2D) flowPoints.get(i);
        
        // if it's pretty close (within 8 pixels), we're trying to remove a point.
        if ((Math.abs(thisPoint.getX() - point.getX()) <= 8) &&
            (Math.abs(thisPoint.getY() - point.getY()) <= 8)) {
          pointFoundForDeletion = thisPoint;
        }
      }
    }
    
    if (pointFoundForDeletion != null) {
      flowPoints.remove(pointFoundForDeletion);
      updateFlowPoints(net, flow, flowPoints);
      return;
    } 
    
    // Dealing with addition of a point below.
    
    if (flowPoints.size() == 2) {
      flowPoints.add(1,point);    
    } else {

      Line2D thisSegment = null;
      int containedSegmentIndex = 0;  // default to the first segment.
      
      for(int i = 0; i < flowPoints.size() - 1; i++) {
        Point2D currentPoint = getPointFor(net, flowPoints.get(i));
        Point2D nextPoint = getPointFor(net, flowPoints.get(i + 1));
        thisSegment = new Line2D.Double(currentPoint, nextPoint);

        // if the point is within 4 pixels of the nearest point in thisSegment, we
        // want this segment  to insert the new point within.
        
        if (thisSegment.ptSegDist(point) <= 4) {
          containedSegmentIndex = i;
        }
      }
      flowPoints.add(containedSegmentIndex + 1,point);    
    }
    
    updateFlowPoints(net, flow, flowPoints);
  }
  
  private static Point2D getPointFor(NetGraph net, Object object) {
    if (object instanceof PortView) {
      return getCenterOfPort(net, (PortView)object);
    }
    if (object instanceof Point2D) {
      return (Point2D) object;
    }
    return null;
  }
  
  public static Point2D getCenterOfPort(NetGraph net, PortView portView) {
    return new Point2D.Double(
        portView.getBounds().getCenterX(),
        portView.getBounds().getCenterY()
    );
  }
  
  private static void updateFlowPoints(NetGraph net, YAWLFlowRelation flow, List flowPoints) {
    Map flowMap = GraphConstants.createAttributes(
        flow, 
        GraphConstants.POINTS, 
        flowPoints
    );
    
    HashMap nestedMap = new HashMap();
    nestedMap.put(flow, flowMap);

    net.getNetModel().edit(
        flowMap, 
        null, 
        null, 
        null
    );
  }


  public static void scrollNetToShowCells(NetGraph net, Object[] cells) {
    final int ELEMENT_BUFFER = 10;
    
    if (net.getFrame() == null) {
     return;
    }
      
    Rectangle bufferedCellBounds = (Rectangle) net.getCellBounds(cells).getBounds().clone();

    bufferedCellBounds.grow(
        ELEMENT_BUFFER,
        ELEMENT_BUFFER
    );
    
    ((JViewport) net.getParent()).scrollRectToVisible(
        bufferedCellBounds
    );
  }

  /**
   * A convenience method that takes all the supplied views and 
   * returns their underlying cells in an object array.
   * @param views
   * @return
   */
  public static Object[] getCellsOfViews(CellView[] views) {
    Object[] cells = new Object[views.length];
    
    int i = 0;
    for(CellView view: views) {
      cells[i++] = view.getCell();
    }
    return cells;
  }
  
  
  /**
   * Creates a decomposition of the specifiied name for the givem 
   * task in the specified net.The input and output paramaters of the task 
   * match type and names of the supplied net variables, and the parameter 
   * queries default to direct type-compatible data transfer.
   * @param net
   * @param task
   */
  public static void creatDirectTransferDecompAndParams(
                            NetGraph net,
                            YAWLAtomicTask task,
                            String decompName,
                            List<DataVariable> inputNetVars,
                            List<DataVariable> outputNetVars) {
    
      List<DataVariable> inputOutputNetVars = new ArrayList<DataVariable>();
      for (DataVariable netVariable : inputNetVars) {
          if (outputNetVars.contains(netVariable)) {
              inputOutputNetVars.add(netVariable);
          }
      }
      for (DataVariable netVariable : inputOutputNetVars) {
          inputNetVars.remove(netVariable);
          outputNetVars.remove(netVariable);
      }

    createTaskDecompParamsToMatchNetParams(
        createDecompositionForAtomicTask(
            net, 
            task, 
            decompName
        ),
        inputNetVars,
        inputOutputNetVars,
        outputNetVars
    );

    inputNetVars.addAll(inputOutputNetVars);
    for (DataVariable inputNetVar : inputNetVars) {
      DataVariable matchingTaskVar = task.getWSDecomposition().getVariableWithName(inputNetVar.getName());
      ((YAWLTask) task).getParameterLists().getInputParameters().addParameterPair(
          matchingTaskVar, 
          XMLUtilities.getTagEnclosedVariableContentXQuery(inputNetVar)
      );
    }

    outputNetVars.addAll(inputOutputNetVars);  
    for(DataVariable outputNetVar : outputNetVars) {
      DataVariable matchingTaskVar = task.getWSDecomposition().getVariableWithName(outputNetVar.getName());
      ((YAWLTask) task).getParameterLists().getOutputParameters().addParameterPair(
          outputNetVar, 
          XMLUtilities.getTagEnclosedVariableContentXQuery(matchingTaskVar)
      );
    }
  }
  
  public static WebServiceDecomposition createDecompositionForAtomicTask(NetGraph net, YAWLAtomicTask task, String label) {
    WebServiceDecomposition taskDecomp = new WebServiceDecomposition();
    taskDecomp.setLabel(label);
    SpecificationModel.getInstance().addWebServiceDecomposition(taskDecomp);
    net.setTaskDecomposition(
          (YAWLTask) task,
          taskDecomp
    );
    return taskDecomp;
  }
  
  public static void createTaskDecompParamsToMatchNetParams(
                           WebServiceDecomposition taskDecomp, 
                           List<DataVariable> inputNetVars,
                           List<DataVariable> inputOutputNetVars,
                           List<DataVariable> outputNetVars) {

    for (DataVariable netVariable : inputNetVars) {
        taskDecomp.addVariable(
          createMatchingTaskVarForNetVar(
              netVariable,
              DataVariable.USAGE_INPUT_ONLY
          )
        );
    }
    for (DataVariable netVariable : inputOutputNetVars) {
        taskDecomp.addVariable(
          createMatchingTaskVarForNetVar(
              netVariable,
              DataVariable.USAGE_INPUT_AND_OUTPUT
          )
        );
    }

    for (DataVariable netVariable : outputNetVars) {
      taskDecomp.addVariable(
        createMatchingTaskVarForNetVar(
            netVariable, 
            DataVariable.USAGE_OUTPUT_ONLY
        )    
      );
    }
    
//    taskDecomp.getVariables().consolidateInputAndOutputVariables();
  }

  public static DataVariable createMatchingTaskVarForNetVar(DataVariable netVar, int taskUsage) {
    DataVariable taskVariable = new DataVariable();
    
    taskVariable.setName(netVar.getName());
    taskVariable.setDataType(netVar.getDataType());
    taskVariable.setUsage(taskUsage);

    return taskVariable;
  }
  
  public static YAWLVertex getVertexFromCell(Object cell) {
    if (cell instanceof VertexContainer) {
      cell = ((VertexContainer) cell).getVertex();
    }
    if (cell instanceof YAWLVertex) {
      return (YAWLVertex) cell;
    }
    if (cell instanceof Decorator) {
      return ((Decorator) cell).getTask();
    }
    return null;
  }

  public static YAWLCondition getConditionFromCell(Object cell) {
    YAWLVertex vertex = getVertexFromCell(cell);
    if (vertex != null && vertex instanceof YAWLCondition) {
      return (YAWLCondition) vertex;
    }
    return null;
  }

  public static InputCondition getInputConditionFromCell(Object cell) {
    YAWLCondition condition = getConditionFromCell(cell);
    if (condition != null && condition instanceof InputCondition) {
      return (InputCondition) condition;
    }
    return null;
  }

  public static OutputCondition getOutputConditionFromCell(Object cell) {
    YAWLCondition condition = getConditionFromCell(cell);
    if (condition != null && condition instanceof OutputCondition) {
      return (OutputCondition) condition;
    }
    return null;
  }
  
  public static YAWLTask getTaskFromCell(Object cell) {
    YAWLVertex vertex = getVertexFromCell(cell);
    if (vertex != null && vertex instanceof YAWLTask) {
      return (YAWLTask) vertex;
    }
    return null;
  }
  
  public static YAWLAtomicTask getAtomicTaskFromCell(Object cell) {
    YAWLTask task = getTaskFromCell(cell);
    if (task != null && task instanceof YAWLAtomicTask) {
      return (YAWLAtomicTask) task;
    }
    return null;
  }

  public static YAWLCompositeTask getCompositeTaskFromCell(Object cell) {
    YAWLTask task = getTaskFromCell(cell);
    if (task != null && task instanceof YAWLCompositeTask) {
      return (YAWLCompositeTask) task;
    }
    return null;
  }
  
  public static YAWLFlowRelation getFlowRelationFromCell(Object cell) {
    if (cell != null && cell instanceof YAWLFlowRelation) {
      return (YAWLFlowRelation) cell;
    }
    return null;
  }
  
}
