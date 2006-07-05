/*
 * Created on 17/10/2003
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

package au.edu.qut.yawl.editor.net;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Point;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Iterator;

import javax.swing.ImageIcon;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.util.prefs.Preferences;

import org.jgraph.JGraph;
import org.jgraph.graph.CellView;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.VertexView;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.Edge;
import org.jgraph.graph.CellMapper;
import org.jgraph.graph.ParentMap;
import org.jgraph.graph.GraphConstants;

import org.jgraph.graph.Port;

import au.edu.qut.yawl.editor.YAWLEditor;
import au.edu.qut.yawl.editor.data.Decomposition;
import au.edu.qut.yawl.editor.elements.model.AtomicTask;
import au.edu.qut.yawl.editor.elements.model.CompositeTask;
import au.edu.qut.yawl.editor.elements.model.Condition;
import au.edu.qut.yawl.editor.elements.model.InputCondition;
import au.edu.qut.yawl.editor.elements.model.MultipleAtomicTask;
import au.edu.qut.yawl.editor.elements.model.MultipleCompositeTask;
import au.edu.qut.yawl.editor.elements.model.YAWLCompositeTask;
import au.edu.qut.yawl.editor.elements.model.OutputCondition;

import au.edu.qut.yawl.editor.elements.model.VertexContainer;
import au.edu.qut.yawl.editor.elements.model.YAWLFlowRelation;
import au.edu.qut.yawl.editor.elements.model.YAWLCell;
import au.edu.qut.yawl.editor.elements.model.YAWLPort;
import au.edu.qut.yawl.editor.elements.model.YAWLVertex;
import au.edu.qut.yawl.editor.elements.model.VertexLabel;
import au.edu.qut.yawl.editor.elements.model.YAWLTask;
import au.edu.qut.yawl.editor.elements.model.Decorator;
import au.edu.qut.yawl.editor.elements.model.JoinDecorator;
import au.edu.qut.yawl.editor.elements.model.SplitDecorator;

import au.edu.qut.yawl.editor.elements.view.DecoratorView;
import au.edu.qut.yawl.editor.elements.view.ConditionView;
import au.edu.qut.yawl.editor.elements.view.InputConditionView;
import au.edu.qut.yawl.editor.elements.view.OutputConditionView;
import au.edu.qut.yawl.editor.elements.view.AtomicTaskView;
import au.edu.qut.yawl.editor.elements.view.MultipleAtomicTaskView;
import au.edu.qut.yawl.editor.elements.view.CompositeTaskView;
import au.edu.qut.yawl.editor.elements.view.MultipleCompositeTaskView;

import au.edu.qut.yawl.editor.foundations.ResourceLoader;

import au.edu.qut.yawl.editor.specification.*;
import au.edu.qut.yawl.editor.swing.YAWLEditorDesktop;
import au.edu.qut.yawl.editor.swing.net.YAWLEditorNetFrame;
import au.edu.qut.yawl.editor.swing.undo.UndoableTaskDecompositionChange;

import au.edu.qut.yawl.editor.actions.net.MoveElementsDownAction;
import au.edu.qut.yawl.editor.actions.net.MoveElementsLeftAction;
import au.edu.qut.yawl.editor.actions.net.MoveElementsRightAction;
import au.edu.qut.yawl.editor.actions.net.MoveElementsUpAction;

import au.edu.qut.yawl.editor.net.utilities.NetCellUtilities;
import au.edu.qut.yawl.editor.net.utilities.NetUtilities;

import java.awt.event.KeyEvent;
import java.awt.Color;

import javax.swing.ActionMap;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;

import au.edu.qut.yawl.editor.actions.net.AddToVisibleCancellationSetAction;
import au.edu.qut.yawl.editor.actions.net.RemoveFromVisibleCancellationSetAction;
import au.edu.qut.yawl.editor.actions.net.SelectAllNetElementsAction;

public class NetGraph extends JGraph {
  
  private static final int FLOW_SPACER = 20;
  
  public static final int DEFAULT_MARGIN  = 50;

  protected transient final Preferences prefs = Preferences.userNodeForPackage(YAWLEditor.class);

  private YAWLEditorNetFrame frame;
  
  private NetSelectionListener selectionListener;
  private CancellationSetModel cancellationSetModel;

  public NetGraph() {
    super();
    initialize();
  }
  
  
  public NetGraph(Decomposition decomposition) {
    super();
    initialize();
    getNetModel().setDecomposition(decomposition);
  }

  
  public NetGraph(YAWLEditorNetFrame frame) {
    super();
    initialize();
    setFrame(frame);
  }
  
  private void initialize() {
    buildBasicGraphContent();
  }

  private void buildBasicGraphContent() {
    setGridMode(JGraph.DOT_GRID_MODE);
    setGridVisible(prefs.getBoolean("showNetGrid", true));
    setGridEnabled(true);
    setGridSize(4);
    setMinimumMove(4);
    setAntiAliased(prefs.getBoolean("showAntiAliasing", true));
    setPortsVisible(true);
    setCloneable(false);
    setTolerance(5);  
    getSelectionModel().setChildrenSelectable(false);
    bindComponentListener();
    bindKeyMappings();
    addMouseListener(new NetPopupListener(this));
    bindCancellationModel();
    setModel(new NetGraphModel(this));
    ToolTipManager.sharedInstance().registerComponent(this);
    selectionListener = new NetSelectionListener(this.getSelectionModel());
    addGraphSelectionListener(selectionListener);
    addFocusListener(new NetFocusListener(this));
    setMarqueeHandler(new NetMarqueeHandler(this));
    getModel().addUndoableEditListener(SpecificationUndoManager.getInstance());
    startUndoableEdits();
  }
  
  public void buildNewGraphContent(){
    stopUndoableEdits();
    setSize(getDefaultSize());
    addInputCondition();
    addOutputCondition();
    startUndoableEdits();
  }
  
  public void startUndoableEdits() {
    SpecificationUndoManager.getInstance().acceptEdits(true);
  }

  public void stopUndoableEdits() {
    SpecificationUndoManager.getInstance().acceptEdits(false);
  }
  
  private void bindComponentListener() {
    addComponentListener(
      new ComponentAdapter() {
        public void componentResized(ComponentEvent event) {
          prefs.putInt("internalFrameWidth", getWidth());
          prefs.putInt("internalFrameHeight", getHeight());
        } 
    });
  }
  
  private void bindCancellationModel() {
    cancellationSetModel = new CancellationSetModel(this);
    this.getCancellationSetModel().subscribe(AddToVisibleCancellationSetAction.getInstance());
    this.getCancellationSetModel().subscribe(RemoveFromVisibleCancellationSetAction.getInstance());
  }

  private void bindKeyMappings() {
    ActionMap map = new ActionMap();
    InputMap  inputMap = new InputMap();

    addKeyMapping(
        map, inputMap,
        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,0),
        MoveElementsLeftAction.getInstance()
    ); 

    addKeyMapping(
        map, inputMap,
        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,0),
        MoveElementsRightAction.getInstance()
    ); 

    addKeyMapping(
        map, inputMap,
        KeyStroke.getKeyStroke(KeyEvent.VK_UP,0),
        MoveElementsUpAction.getInstance()
    ); 

    addKeyMapping(
        map, inputMap,
        KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,0),
        MoveElementsDownAction.getInstance()
    ); 

    addKeyMapping(
        map, inputMap,
        KeyStroke.getKeyStroke(KeyEvent.VK_A,KeyEvent.CTRL_DOWN_MASK),
        SelectAllNetElementsAction.getInstance()
    ); 

    
    setActionMap(map); 
    setInputMap(JComponent.WHEN_FOCUSED, inputMap);
    setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, inputMap);
  }

  private void addKeyMapping(ActionMap actionMap, InputMap inputMap, 
                             KeyStroke keystroke, Action action) {
    actionMap.put(action.getValue(Action.NAME), action);
    inputMap.put(keystroke,action.getValue(Action.NAME));
  }


  private Dimension getDefaultSize() {
    return new Dimension(prefs.getInt("internalFrameWidth", 500), 
                         prefs.getInt("internalFrameHeight", 300));
  }
  
  public NetSelectionListener getSelectionListener() {
    return selectionListener; 
  }
  
  public Condition addCondition(Point point) {
    Condition condition = new Condition(point);
    addElement(condition);
    return condition;
  }

  public AtomicTask addAtomicTask(Point point) {
    AtomicTask task = new AtomicTask(point);
    addElement(task);
    return task;
  }

  public MultipleAtomicTask addMultipleAtomicTask(Point point) {
    MultipleAtomicTask task = new MultipleAtomicTask(point);
    addElement(task);
    return task;
  }

  public CompositeTask addCompositeTask(Point point) {
    CompositeTask task = new CompositeTask(point);
    addElement(task);
    return task;
  }

	public MultipleCompositeTask addMultipleCompositeTask(Point point) {
		MultipleCompositeTask task = new MultipleCompositeTask(point);
		addElement(task);
		return task;
	}
  
  private void addElement(YAWLVertex element) {
    getModel().insert(new Object[] {element}, 
                      null, null, null, null);
  }

  public String getToolTipText(MouseEvent event) {
    Object cell = getFirstCellForLocation(event.getX(), event.getY());
    if (cell instanceof YAWLVertex) {
      return ((YAWLVertex) cell).getToolTipText();
    }
    if (cell instanceof VertexContainer) {
      return ((VertexContainer) cell).getToolTipText();
      
    }
    return null;
  }
  
  
  private InputCondition addInputCondition() {
    Point startPoint = getInputConditionDefaultPoint(InputCondition.getIconSize());
    InputCondition inputCondition = new InputCondition(startPoint);
    addElement(inputCondition);
    return inputCondition;
  }
  
  private Point getInputConditionDefaultPoint(Dimension size) {
   return snap(new Point((DEFAULT_MARGIN)  - (size.width/2), 
                         (getHeight()/2) - (size.height/2)));
  }
  
  private OutputCondition addOutputCondition() {
    Point startPoint = getOutputConditionDefaultPoint(OutputCondition.getIconSize());
    OutputCondition outputCondition = new OutputCondition(startPoint);
    addElement(outputCondition);
    return outputCondition;
  }

  private Point getOutputConditionDefaultPoint(Dimension size) {
    return snap(new Point((getWidth()-DEFAULT_MARGIN) - (size.width/2), 
                          (getHeight()/2)  - (size.height/2)));
  }
  
  public Dimension getBufferedPreferredSize() {
    Dimension preferredSize = getPreferredSize();
    preferredSize.width += YAWLEditorDesktop.X_OFFSET;
    preferredSize.height += YAWLEditorDesktop.Y_OFFSET;    
    return preferredSize;
  }

  protected ImageIcon getIconByName(String iconName) {
    return ResourceLoader.getImageAsIcon("/au/edu/qut/yawl/editor/resources/yawlElements/" 
           + iconName + ".gif");
  }
  
  public void connect(YAWLPort source, YAWLPort target) {
    ConnectionSet cs = new ConnectionSet();
    YAWLFlowRelation flow = new YAWLFlowRelation();
    cs.connect(flow, source, target);

    getNetModel().beginUpdate();

    getModel().insert(new Object[] {flow},
                      null, cs, null, null);

    setFlowPriorityIfNecessary(flow);
    makeSameTaskFlowPrettyIfNecessary(flow);

    getNetModel().endUpdate();
  }
  
  private void setFlowPriorityIfNecessary(YAWLFlowRelation flow) {
    YAWLCell sourceCell = getSourceOf(flow);
    if (sourceCell instanceof SplitDecorator) {
      Set edges = this.getEdges(new YAWLCell[] { sourceCell} );
      flow.setPriority(edges.size() - 1);
    }
  }
  
  private void makeSameTaskFlowPrettyIfNecessary(YAWLFlowRelation flow) {
     
    if (flow.connectsTaskToItself()) {

      /* We can make some assumptions here for fast-tracking the calculation of
         where to put points.  A flow that connects a task to itself MUST have both a 
         join and split decorator. We build the extra points around the decorators.
      */
      
      EdgeView flowView = (EdgeView) getViewFor(flow);
      Point awaySourcePoint = 
        new Point(flowView.getPoint(0).x,
                  flowView.getPoint(0).y 
        );

      Point awayTargetPoint = 
        new Point(flowView.getPoint(1).x,
                  flowView.getPoint(1).y 
        );
      
      switch(flow.getSourceTask().getSplitDecorator().getCardinalPosition()) {
        case Decorator.TOP: {
          awaySourcePoint.translate(0,-FLOW_SPACER);
          break;
        }
        case Decorator.BOTTOM: {
          awaySourcePoint.translate(0,FLOW_SPACER);
          break;
        }
        case Decorator.LEFT: {
          awaySourcePoint.translate(-FLOW_SPACER,0);
          break;
        }
        case Decorator.RIGHT: {
          awaySourcePoint.translate(FLOW_SPACER,0);
          break;
        }
      }

      switch(flow.getTargetTask().getJoinDecorator().getCardinalPosition()) {
        case Decorator.TOP: {
          awayTargetPoint.translate(0,-FLOW_SPACER);
          break;
        }
        case Decorator.BOTTOM: {
          awayTargetPoint.translate(0,FLOW_SPACER);
          break;
        }
        case Decorator.LEFT: {
          awayTargetPoint.translate(-FLOW_SPACER,0);
          break;
        }
        case Decorator.RIGHT: {
          awayTargetPoint.translate(FLOW_SPACER,0);
          break;
        }
      }
      
      flowView.addPoint(1, awaySourcePoint);
      flowView.addPoint(2, awayTargetPoint);
      
      if (flow.getSourceTask().hasTopLeftAdjacentDecorators()) {
        Point cornerPoint = new Point(
          Math.min(awaySourcePoint.x, awayTargetPoint.x),
          Math.min(awaySourcePoint.y, awayTargetPoint.y)
        );
        flowView.addPoint(2, cornerPoint);
      }

      if (flow.getSourceTask().hasTopRightAdjacentDecorators()) {
        Point cornerPoint = new Point(
          Math.max(awaySourcePoint.x, awayTargetPoint.x),
          Math.min(awaySourcePoint.y, awayTargetPoint.y)
        );
        flowView.addPoint(2, cornerPoint);
      }

      if (flow.getSourceTask().hasBottomRightAdjacentDecorators()) {
        Point cornerPoint = new Point(
          Math.max(awaySourcePoint.x, awayTargetPoint.x),
          Math.max(awaySourcePoint.y, awayTargetPoint.y)
        );
        flowView.addPoint(2, cornerPoint);
      }

      if (flow.getSourceTask().hasBottomLeftAdjacentDecorators()) {
        Point cornerPoint = new Point(
          Math.min(awaySourcePoint.x, awayTargetPoint.x),
          Math.max(awaySourcePoint.y, awayTargetPoint.y)
        );
        flowView.addPoint(2, cornerPoint);
      }
      
      if (flow.getSourceTask().hasHorizontallyAlignedDecorators()) {
        if (awaySourcePoint.x < awayTargetPoint.x) {
          Point sourceCornerPoint = new Point(
              awaySourcePoint.x,
              getViewFor(flow.getSourceTask()).getBounds().y - FLOW_SPACER
            );
          
          Point targetCornerPoint = new Point(
              awayTargetPoint.x,
              getViewFor(flow.getSourceTask()).getBounds().y - FLOW_SPACER
            );

          flowView.addPoint(2, sourceCornerPoint);
          flowView.addPoint(3, targetCornerPoint);
        } else {
          Point sourceCornerPoint = new Point(
              awaySourcePoint.x,
              getViewFor(flow.getSourceTask()).getBounds().y - FLOW_SPACER
            );
          
          Point targetCornerPoint = new Point(
              awayTargetPoint.x,
              getViewFor(flow.getSourceTask()).getBounds().y - FLOW_SPACER
            );

          flowView.addPoint(2, sourceCornerPoint);
          flowView.addPoint(3, targetCornerPoint);
        }
      }
      if (flow.getSourceTask().hasVerticallyAlignedDecorators()) {
        if (awaySourcePoint.y < awayTargetPoint.y) {
          Point sourceCornerPoint = new Point(
              getViewFor(flow.getSourceTask()).getBounds().x - FLOW_SPACER,
              awaySourcePoint.y
            );
          
          Point targetCornerPoint = new Point(
              getViewFor(flow.getSourceTask()).getBounds().x - FLOW_SPACER,
              awayTargetPoint.y
          );

          flowView.addPoint(2, sourceCornerPoint);
          flowView.addPoint(3, targetCornerPoint);
        } else {
          Point sourceCornerPoint = new Point(
              getViewFor(flow.getSourceTask()).getBounds().x - FLOW_SPACER,
              awaySourcePoint.y
          );
          
          Point targetCornerPoint = new Point(
              getViewFor(flow.getSourceTask()).getBounds().x - FLOW_SPACER,
              awayTargetPoint.y
          );

          flowView.addPoint(2, sourceCornerPoint);
          flowView.addPoint(3, targetCornerPoint);
        }
      }
      NetCellUtilities.applyViewChange(this, flowView);
    }
  }

  /**
   * Returns true if <code>object</code> is a vertex, that is, if it
   * is not an instance of Port or Edge, and all of its children are
   * ports, or it has no children.
   */
  public boolean isGroup(Object cell) {
    // Map the Cell to its View
    CellView view = getGraphLayoutCache().getMapping(cell, false);
    if (view != null)
      return !view.isLeaf();
    return false;
  }

  public boolean connectionAllowable(Port source, Port target) {
    return getNetModel().connectionAllowable(source, target);
  }
  
  public Set getEdges(YAWLCell [] cells) {
    return NetGraphModel.getEdges(getModel(), cells );
  }
  
  public YAWLCell getTargetOf(Edge edge) {
    return getNetModel().getTargetOf(edge);
  }

  public YAWLCell getSourceOf(Edge edge) {
    return getNetModel().getSourceOf(edge);
  }
  
  public boolean areConnected(YAWLCell sourceCell, YAWLCell targetCell) {
    return getNetModel().areConnected(sourceCell, targetCell);
  }
  
  public boolean acceptsIncommingFlows(YAWLCell cell) {
    return getNetModel().acceptsIncommingFlows(cell);
  }
  
  public boolean hasIncommingFlow(YAWLCell cell) {
    return getNetModel().hasIncommingFlow(cell);
  }

  public boolean generatesOutgoingFlows(YAWLCell cell) {
    return getNetModel().generatesOutgoingFlows(cell);
  }

  public boolean hasOutgoingFlow(YAWLCell cell) {
    return getNetModel().hasOutgoingFlow(cell);
  }
  
  protected VertexView createVertexView(Object vertex, CellMapper cm) {
    if (vertex instanceof AtomicTask) {
      return new AtomicTaskView(vertex, this, cm);
    }
    if (vertex instanceof MultipleAtomicTask) {
      return new MultipleAtomicTaskView(vertex, this, cm);
    }
    if (vertex instanceof CompositeTask) {
      return new CompositeTaskView(vertex, this, cm);
    }
    if (vertex instanceof MultipleCompositeTask) {
      return new MultipleCompositeTaskView(vertex, this, cm);
    }
    if (vertex instanceof InputCondition) {
      return new InputConditionView(vertex, this, cm);
    }
    if (vertex instanceof OutputCondition) {
      return new OutputConditionView(vertex, this, cm);
    }
    if (vertex instanceof Condition) {
      return new ConditionView(vertex, this, cm);
    }
    if (vertex instanceof Decorator) {
      return new DecoratorView((Decorator) vertex, this, cm);
    }
    if (vertex instanceof VertexContainer) {
      return new VertexView(vertex, this, cm);
    }
    return super.createVertexView(vertex, cm);
  }
  
  public void setJoinDecorator(YAWLTask task, int type, int position) {
    if(task.getJoinDecorator() == null && 
        (type == Decorator.NO_TYPE || position == YAWLTask.NOWHERE)) {
      return;
    }
    
    getNetModel().beginUpdate();
    
    String label = this.getElementLabel(task);
    this.setElementLabelInsideUpdate(task, null);
    
    getNetModel().setJoinDecorator(task, type, position);

    this.setElementLabelInsideUpdate(task, label);
    
    getNetModel().endUpdate();

    this.getGraphLayoutCache().reload();
  }

  public void setSplitDecorator(YAWLTask task, int type, int position) {
    if(task.getSplitDecorator() == null && 
        (type == Decorator.NO_TYPE || position == YAWLTask.NOWHERE)) {
      return;
    }

    getNetModel().beginUpdate();
    
    String label = this.getElementLabel(task);
    this.setElementLabelInsideUpdate(task, null);

    getNetModel().setSplitDecorator(task, type, position);

    this.setElementLabelInsideUpdate(task, label);
    
    getNetModel().endUpdate();

    this.getGraphLayoutCache().reload();
  }
  
  public void removeCellsAndTheirEdges(Object[] cells) {
    getNetModel().remove(cells);
  }
  
  public void increaseSelectedVertexSize() {
    changeSelectedVertexSize(getGridSize());
  }

  public void decreaseSelectedVertexSize() {
    changeSelectedVertexSize(-getGridSize());
  }

  private void changeSelectedVertexSize(int baseSize) {
    getNetModel().beginUpdate();
    Object[] cells = getSelectionCells();
    for(int i = 0; i < cells.length; i++) {
      try {
        VertexView view = 
          getVertexViewFor((GraphCell) cells[i]);
        if (view.getCell() instanceof VertexContainer) {
          changeDecoratedVertexViewSize(view, baseSize);
          translateLabelIfNecessary((VertexContainer) cells[i], baseSize/2, baseSize);
        } else { 
          changeViewSize(new VertexView[] { view }, 
                         baseSize, baseSize);          
        }
      } catch (Exception e) {}
    }
    getNetModel().endUpdate();
  }
  
  private void translateLabelIfNecessary(VertexContainer container, int x, int y) {
    if (container.getLabel() != null) {
      NetCellUtilities.translateView(this, getVertexViewFor(container.getLabel()),x, y);
    }
  }
  
  private void changeDecoratedVertexViewSize(VertexView view, int baseSize) {
    VertexContainer vertexContainer = (VertexContainer) view.getCell();
    
    YAWLVertex vertex = vertexContainer.getVertex();
    if (vertex instanceof YAWLTask) {
      changeDecoratedTaskViewSize(view, baseSize);
    } else {
      changeViewSize( new VertexView[] { getVertexViewFor(vertex) } , baseSize, baseSize);
    }
  }

  private void changeDecoratedTaskViewSize(VertexView view, int baseSize) {
    VertexContainer decoratedTask = (VertexContainer) view.getCell();
    
    YAWLTask task        = (YAWLTask) decoratedTask.getVertex();
    VertexView taskView  = getVertexViewFor(task);
    
    JoinDecorator join   = task.getJoinDecorator();
    VertexView joinView  = null;
    if (join != null) {
      joinView = getVertexViewFor(join);
    }
    
    SplitDecorator split  = task.getSplitDecorator();
    VertexView splitView = null;
    if (split != null) {
      splitView = (VertexView) getViewFor(split);
    }
    
    HashSet verticalViews = new HashSet();
    HashSet horizontalViews = new HashSet();
    if (join != null) {
      if (join.getCardinalPosition() == YAWLTask.LEFT ||
          join.getCardinalPosition() == YAWLTask.RIGHT) {
       verticalViews.add(joinView);
      }
      if (join.getCardinalPosition() == YAWLTask.TOP ||
          join.getCardinalPosition() == YAWLTask.BOTTOM) {
       horizontalViews.add(joinView);
      }
    }
    if (split != null) {
      if (split.getCardinalPosition() == YAWLTask.LEFT ||
          split.getCardinalPosition() == YAWLTask.RIGHT) {
       verticalViews.add(splitView);
      }
      if (split.getCardinalPosition() == YAWLTask.TOP ||
          split.getCardinalPosition() == YAWLTask.BOTTOM) {
       horizontalViews.add(splitView);
      }
    }
    
    changeViewSize(new VertexView[] { taskView }, baseSize, baseSize);
    changeViewSize(verticalViews.toArray(), baseSize/4, baseSize);
    changeViewSize(horizontalViews.toArray(), baseSize, baseSize/4);
    
    if (task.hasDecoratorAtPosition(YAWLTask.LEFT)) {
      NetCellUtilities.translateView(
          this,
          taskView,
          baseSize/4,
          0);
      
      if (join != null) {
        if(join.getCardinalPosition() == YAWLTask.TOP ||
           join.getCardinalPosition() == YAWLTask.BOTTOM) {
          NetCellUtilities.translateView(
              this, 
              joinView,
              baseSize/4,
              0);  
        }
        if(join.getCardinalPosition() == YAWLTask.RIGHT) {
          NetCellUtilities.translateView(
              this,
              joinView,
              baseSize + baseSize/4,
              0);  
        }
      }
      if (split != null) {
        if(split.getCardinalPosition() == YAWLTask.TOP ||
           split.getCardinalPosition() == YAWLTask.BOTTOM) {
          NetCellUtilities.translateView(
              this,
              splitView,
              baseSize/4,
              0);  
        }
        if(split.getCardinalPosition() == YAWLTask.RIGHT) {
          NetCellUtilities.translateView(
              this,
              splitView,
              baseSize + baseSize/4,
              0);  
        }
      }
    } else { // no decorator on left
      if (join != null && join.getCardinalPosition() == YAWLTask.RIGHT) {
          NetCellUtilities.translateView(
              this,
              joinView,
              baseSize,
              0);  
        }
      if(split != null && split.getCardinalPosition() == YAWLTask.RIGHT) {
        NetCellUtilities.translateView(
            this,
            splitView,
            baseSize,
            0);  
      }
    }
    if(task.hasDecoratorAtPosition(YAWLTask.TOP)) {
      NetCellUtilities.translateView(
          this,
          taskView,
          0,
          baseSize/4);
      
      if (join != null) {
        if(join.getCardinalPosition() == YAWLTask.LEFT ||
           join.getCardinalPosition() == YAWLTask.RIGHT) {
          NetCellUtilities.translateView(
              this,
              joinView,
              0,
              baseSize/4);  
        }
        if(join.getCardinalPosition() == YAWLTask.BOTTOM) {
          NetCellUtilities.translateView(
              this,
              joinView,
              0,
              baseSize + baseSize/4);  
        }
      }
      if (split != null) {
        if(split.getCardinalPosition() == YAWLTask.LEFT ||
           split.getCardinalPosition() == YAWLTask.RIGHT) {
          NetCellUtilities.translateView(
              this,
              splitView,
              0,
              baseSize/4);  
        }
        if(split.getCardinalPosition() == YAWLTask.BOTTOM) {
          NetCellUtilities.translateView(
              this,
              splitView,
              0,
              baseSize + baseSize/4);  
        } 
      }
    } else { // no decorator on top
        if(join != null && join.getCardinalPosition() == YAWLTask.BOTTOM) {
          NetCellUtilities.translateView(
              this,
              joinView,
              0,
              baseSize);  
        }
      if(split != null && split.getCardinalPosition() == YAWLTask.BOTTOM) {
        NetCellUtilities.translateView(
            this,
            splitView,
            0,
            baseSize);  
      } 
    }
  }

  private void changeViewSize(Object[] views, int width, int height) {
    for(int i = 0; i < views.length; i++) {
      
      VertexView view = (VertexView) views[i];
      
      Rectangle bounds = view.getBounds();
      bounds.width += width;
      bounds.height += height;
      view.setBounds(bounds);
      
      NetCellUtilities.applyViewChange(this,view);
    }
  }
  
  
  public void moveSelectedElementsLeft() {
    moveSelectedElementsBy(-getGridSize(), 0);
  }

  public void moveSelectedElementsRight() {
    moveSelectedElementsBy(getGridSize(), 0);
  }

  public void moveSelectedElementsUp() {
    moveSelectedElementsBy(0, -getGridSize());
  }

  public void moveSelectedElementsDown() {
    moveSelectedElementsBy(0, getGridSize());
  }


  public void moveElementBy(GraphCell cell, int x, int y) {
		CellView view = getViewFor(cell);
		if (view.getCell() instanceof VertexContainer) {
			translateDecoratedVertexView((VertexView) view, x, y);
			translateLabelIfNecessary((VertexContainer) cell, x, y);
		} else { 
			NetCellUtilities.translateView(this, view, x, y);          
		}
  }
  
  private void moveSelectedElementsBy(int x, int y) {
    getNetModel().beginUpdate();
    Object[] cells = getSelectionCells();
    for(int i = 0; i < cells.length; i++) {
      moveElementBy((GraphCell) cells[i], x, y);
    }
    getNetModel().endUpdate();
  }
  
  private void translateDecoratedVertexView(VertexView view, int x, int y) {
    VertexContainer vertexContainer = (VertexContainer) view.getCell();
    
    YAWLVertex vertex = vertexContainer.getVertex();
    if (vertex instanceof YAWLTask) {
      translateDecoratedTaskView(view, x, y);
    } else {
      NetCellUtilities.translateView(this,getVertexViewFor(vertex), x, y);
    }
  }
  
  private void translateDecoratedTaskView(VertexView view, int x, int y) {
    VertexContainer decoratedTask = (VertexContainer) view.getCell();
    
    YAWLTask task        = (YAWLTask) decoratedTask.getVertex();
    VertexView taskView  = (VertexView) getViewFor(task);
    
    JoinDecorator join   = task.getJoinDecorator();
    VertexView joinView  = null;
    if (join != null) {
      joinView = (VertexView) getViewFor(join);
    }
    
    SplitDecorator split  = task.getSplitDecorator();
    VertexView splitView = null;
    if (split != null) {
      splitView = (VertexView) getViewFor(split);
    }

    NetCellUtilities.translateView(this,taskView,x,y);  
    if (join != null) {
      NetCellUtilities.translateView(this,joinView,x,y);  
    }
    if (split != null) {
      NetCellUtilities.translateView(this,splitView,x,y);  
    }
  }
  
  public CellView getViewFor(GraphCell cell) {
    return graphLayoutCache.getMapping(cell, false);
  }

  public VertexView getVertexViewFor(GraphCell cell) {
    return (VertexView) graphLayoutCache.getMapping(cell, false);
  }
  
  public String getElementLabel(GraphCell vertex) {
    VertexContainer container = null;
    if (vertex instanceof VertexContainer) {
      container = (VertexContainer) vertex;
    }
    if (vertex instanceof YAWLVertex) {
      YAWLVertex element = (YAWLVertex) vertex;
      if (element.getParent() != null) {
        container = (VertexContainer) element.getParent();
      }
    }
    if (container != null) {
      VertexLabel label = container.getLabel();
      if (label != null) {
        return container.getLabel().toString();
      }
    }
    return null;
  }
  
  public void setElementLabel(GraphCell vertex, String labelString) {
    getNetModel().beginUpdate();
    setElementLabelInsideUpdate(vertex, labelString);
    getNetModel().endUpdate();
  }

  public void setElementLabelInsideUpdate(GraphCell vertex, String labelString) {
    HashSet objectsToInsert = new HashSet();
    ParentMap parentMap = new ParentMap();
    YAWLVertex element = null;
    
    try {
      VertexContainer container = null;
      if (vertex instanceof VertexContainer) {
        container = (VertexContainer) vertex;
        element = container.getVertex();
      } else {
        element = (YAWLVertex) vertex;
        container = getNetModel().getVertexContainer(element, objectsToInsert, parentMap);
      }
       
      if(container.getLabel() != null) {
        getNetModel().remove(new Object[] { container.getLabel() } ); 
      }
      
      if(labelString == null || labelString.equals("")) {
        return;
      }

      VertexLabel label = new VertexLabel(element, labelString);

      getNetModel().insert(objectsToInsert.toArray(),null, null, parentMap, null); 
      getNetModel().insert(new Object[] { label } ,null, null, null, null); 
      
      VertexView labelView = getVertexViewFor(label);
      VertexView containerView = getVertexViewFor(container);
      
      NetCellUtilities.translateView(
          this,
          labelView, 
          (int) (containerView.getBounds().getX() + containerView.getBounds().getWidth()/2 - 10 - labelView.getBounds().getWidth()/2), 
          (int) (containerView.getBounds().getY() + containerView.getBounds().getHeight() - 10));

      parentMap.addEntry(label, container);
      
      getNetModel().edit(null,null, parentMap, null); 
    } catch (Exception e) {}
  }


  public NetGraphModel getNetModel() {
   return (NetGraphModel) getModel();
  }
  
  public CancellationSetModel getCancellationSetModel() {
    return this.cancellationSetModel;
  }
  
  public void changeCancellationSet(YAWLTask task) {
    getNetModel().beginUpdate();

    if (cancellationSetModel.getTriggeringTask() != null) {
      hideOldCancellationSet();
    }

    cancellationSetModel.changeCancellationSet(task);

    if (task != null) {
      showCurrentCancellationSet();
    }
    getNetModel().endUpdate();
  }
  
  public YAWLTask viewingCancellationSetOf() {
    return cancellationSetModel.getTriggeringTask();
    
  }
  
  private void hideOldCancellationSet() {
    YAWLTask triggeringTask = cancellationSetModel.getTriggeringTask();

    changeVertexBackground(triggeringTask, 
                           YAWLVertex.DEFAULT_VERTEX_BACKGROUND);

    Object[] setMembers = triggeringTask.getCancellationSet().getSetMembers().toArray();
    for(int i = 0; i < setMembers.length ; i++) {
      YAWLCell cell = (YAWLCell) setMembers[i];
       this.showCellAsNotInCurrentCancellationSet(cell);
    }
  }
  
  private void showCurrentCancellationSet() {
    YAWLTask triggeringTask = cancellationSetModel.getTriggeringTask();

    changeVertexBackground(triggeringTask, 
                           CancellationSetModel.CANCELLATION_SET_TRIGGER_BACKGROUND);

    Object[] setMembers = triggeringTask.getCancellationSet().getSetMembers().toArray();
    for(int i = 0; i < setMembers.length ; i++) {
      YAWLCell cell = (YAWLCell) setMembers[i];
       this.showCellAsInCurrentCancellationSet(cell);
    }
  }
  
  public YAWLTask getTriggeringTaskOfCurrentCancellationSet() {
  	return cancellationSetModel.getTriggeringTask();
  }
  
  public void addSelectedCellsToVisibleCancellationSet() {
    Object[] validSelectedCells = 
      getCancellationSetModel().getValidSelectedCellsForInclusion();

    if(validSelectedCells.length == 0) {
      return;
    }

    getNetModel().beginUpdate();
    for(int i = 0; i < validSelectedCells.length; i++) {
      addCellToCancellationSetInsideUpdate((YAWLCell) validSelectedCells[i]);
    }
    getCancellationSetModel().refresh();
    getNetModel().endUpdate();
  }

  public void removeSelectedCellsFromVisibleCancellationSet() {
    Object[] validSelectedCells = 
      getCancellationSetModel().getValidSelectedCellsForExclusion();

    if(validSelectedCells.length == 0) {
      return;
    }

    getNetModel().beginUpdate();
    for(int i = 0; i < validSelectedCells.length; i++) {
      removeCellFromCancellationSetInsideUpdate((YAWLCell) validSelectedCells[i]);
    }
    getCancellationSetModel().refresh();
    getNetModel().endUpdate();
  }

  
  public void addCellToCancellationSet(YAWLCell cell) {
    getNetModel().beginUpdate();
    addCellToCancellationSetInsideUpdate(cell);
    getNetModel().endUpdate();
  }
  
  private void addCellToCancellationSetInsideUpdate(YAWLCell cell) {
    if (cancellationSetModel.addCellToCancellationSet(cell)) {
      showCellAsInCurrentCancellationSet(cell);
    }
  }

  public void removeCellFromCancellationSet(YAWLCell cell) {
    getNetModel().beginUpdate();
    removeCellFromCancellationSetInsideUpdate(cell);
    getNetModel().endUpdate();
  }
  
  public void removeCellFromCancellationSetInsideUpdate(YAWLCell cell) {
    if (cancellationSetModel.removeCellFromCancellationSet(cell)) {
      showCellAsNotInCurrentCancellationSet(cell);
    }
  }


  private void showCellAsInCurrentCancellationSet(YAWLCell cell) {
    changeNetCellForeground(cell, CancellationSetModel.CANCELLATION_SET_MEMBER_FOREGROUND);
  }

  private void showCellAsNotInCurrentCancellationSet(YAWLCell cell) {
    changeNetCellForeground(cell, CancellationSetModel.NOT_CANCELLATION_SET_MEMBER_FOREGROUND);
  }
  
  public void changeCellForeground(GraphCell cell, Color color) {
    Map attributes;
    if(cell instanceof Edge) {
      attributes = GraphConstants.createAttributes(cell, 
                                                   GraphConstants.LINECOLOR,
                                                   color);
    } else {
      attributes = GraphConstants.createAttributes(cell, 
                                                   GraphConstants.FOREGROUND,
                                                   color);
    }
    getModel().edit(attributes, null, null, null);
  }
  
  private void changeNetCellForeground(YAWLCell cell, Color color) {
    changeCellForeground((GraphCell) cell,color);
    if (cell instanceof YAWLTask && ((YAWLTask)cell).getParent() != null) {
      YAWLTask task = (YAWLTask) cell;
      if (task.getJoinDecorator() != null) {
        changeCellForeground(task.getJoinDecorator(), 
                             color);
      }
      if (task.getSplitDecorator() != null) {
        changeCellForeground(task.getSplitDecorator(), 
                             color);
      }
    }
  }

  private void changeVertexBackground(YAWLVertex vertex, Color color) {
    changeCellBackground(vertex,color);
    if (vertex.getParent() != null && vertex instanceof YAWLTask) {
      YAWLTask task = (YAWLTask) vertex;
      if (task.getJoinDecorator() != null) {
        changeCellBackground(task.getJoinDecorator(), 
                             color);
      }
      if (task.getSplitDecorator() != null) {
        changeCellBackground(task.getSplitDecorator(), 
                             color);
      }
    }
  }

  private  void changeCellBackground(GraphCell cell, Color color) {
    Map attributes = GraphConstants.createAttributes(cell, 
                                                     GraphConstants.BACKGROUND,
                                                     color);
    getModel().edit(attributes, null, null, null);
  }
  
  public YAWLEditorNetFrame getFrame() {
    return frame;
  }
  
  public void setFrame(YAWLEditorNetFrame frame) {
    this.frame = frame;
  }
  
  public String getName() {
    return getNetModel().getName();
  }
  
  public void setName(String name) {
    getNetModel().setName(name);
  }
  
  
  public void setUnfoldingNet(YAWLCompositeTask task, NetGraph graph) {
    if (graph != null) {
      String name = graph.getNetModel().getName();
      ((YAWLTask)task).setDecomposition(graph.getNetModel().getDecomposition());
      setElementLabel((GraphCell) task, name);
    } else {
      ((YAWLTask)task).setDecomposition(null);
      setElementLabel((GraphCell) task, null);
    }
  }
  
  public void setTaskDecomposition(YAWLTask task, Decomposition decomposition) {
    Decomposition oldDecomposition = task.getDecomposition();
    if (decomposition.equals(oldDecomposition)) {
      return;
    }
    task.setDecomposition(decomposition);
    task.getParameterLists().reset();

    getNetModel().beginUpdate();
    
    this.setElementLabelInsideUpdate(task, decomposition.getLabel());
    getNetModel().postEdit(
        new UndoableTaskDecompositionChange(
              task, 
              oldDecomposition, 
              task.getDecomposition()
        )
      );    
    
    getNetModel().endUpdate();
  }
  
  public void refreshTaskDecompositionLabels(Decomposition decomposition) {
    Iterator taskIterator = 
      NetUtilities.getAllTasks(getNetModel()).iterator();
    while(taskIterator.hasNext()) {
      YAWLTask task = (YAWLTask) taskIterator.next();
      Decomposition taskDecomposition = 
        task.getDecomposition();
      if (decomposition.equals(taskDecomposition)) {
        setElementLabel(task, decomposition.getLabel());
      }
    }
  }
  
  public NetMarqueeHandler getNetMarqueeHandler() {
    return (NetMarqueeHandler) getMarqueeHandler();
  }
}


class NetFocusListener implements FocusListener {

  /* 
   * This jiggery-pokery has been made necessary because 
   * ALT-TABbing out of the application without forcing cleanup of 
   * Marquee state (as we now do in the focusLost() method below) 
   * would result in unconnected ghost flows occasionally appearing 
   * on the net when users next tried connecting two net elements with 
   * a flow.
   */
  
  private NetGraph net;
  
  public NetFocusListener(NetGraph net) {
    this.net = net;
  }
  
  public void focusGained(FocusEvent event) {
    // deliberately does nothing. 
  }

  public void focusLost(FocusEvent event) {
    try {
      net.getNetMarqueeHandler().connectElementsOrIgnoreFlow();
    } catch (Exception e) {}
  }
}