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

package org.yawlfoundation.yawl.editor.net;

import org.jgraph.JGraph;
import org.jgraph.graph.*;
import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.actions.net.*;
import org.yawlfoundation.yawl.editor.data.Decomposition;
import org.yawlfoundation.yawl.editor.elements.model.*;
import org.yawlfoundation.yawl.editor.foundations.ResourceLoader;
import org.yawlfoundation.yawl.editor.net.utilities.NetCellUtilities;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.swing.net.YAWLEditorNetPanel;
import org.yawlfoundation.yawl.editor.swing.undo.UndoableTaskDecompositionChange;
import org.yawlfoundation.yawl.editor.swing.undo.UndoableTaskIconChange;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

public class NetGraph extends JGraph {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final int FLOW_SPACER = 20;
  
  public static final int DEFAULT_MARGIN  = 50;
  
  /**
   * Default margin size of whitespace to appear around elements
   * being added to a net.
   */
  public static final int WHITESPACE_MARGIN  = 20;


  protected transient final Preferences prefs = Preferences.userNodeForPackage(YAWLEditor.class);

  private YAWLEditorNetPanel frame;
  
  private NetSelectionListener selectionListener;
  private CancellationSetModel cancellationSetModel;

  /**
   * The following fields are added by Jingxin XU
   */
  private ConfigurationSettingInfor configurationSettings; //This variable stores all editor settings of a net tp steer configuration opportunities

  private ServiceAutomatonTree serviceAutomaton = null;

  /**********/  

  public NetGraph() {
    super();
    initialize();
  }
  
  public NetGraph(Decomposition decomposition) {
    super();
    initialize();
    getNetModel().setDecomposition(decomposition);
  }

  public NetGraph(YAWLEditorNetPanel frame) {
    super();
    initialize();
    setFrame(frame);
  }
  
  private void initialize() {
    buildBasicGraphContent();
    this.configurationSettings = new ConfigurationSettingInfor();
  }

  public void createServiceAutonomous() {
	  this.serviceAutomaton = new ServiceAutomatonTree(this);
  }

  private void buildBasicGraphContent() {
    setGridMode(JGraph.DOT_GRID_MODE);
    setGridVisible(prefs.getBoolean("showNetGrid", true));
    setGridEnabled(true);
    setDoubleBuffered(true);
    setGridSize(4);
    setMinimumMove(4);
    setBackground(new Color(SpecificationModel.getInstance().getDefaultNetBackgroundColor()));
    setAntiAliased(prefs.getBoolean("showAntiAliasing", true));
    setPortsVisible(false);
    setCloneable(false);
    setTolerance(5);
    setAutoResizeGraph(true);
    setAutoscrolls(true);
    getSelectionModel().setChildrenSelectable(false);
    bindComponentListener();
    bindKeyMappings();

    addMouseListener(new NetPopupListener(this));
    addMouseListener(new ElementDoubleClickListener(this));
    addMouseListener(new ElementControlClickListener(this));

    bindCancellationModel();
    setModel(new NetGraphModel(this));
    ToolTipManager.sharedInstance().registerComponent(this);
    selectionListener = new NetSelectionListener(this.getSelectionModel());
    addGraphSelectionListener(selectionListener);
    addFocusListener(new NetFocusListener(this));
    setMarqueeHandler(new NetMarqueeHandler(this));
    getModel().addUndoableEditListener(SpecificationUndoManager.getInstance());
    
    getGraphLayoutCache().setFactory(new NetCellViewFactory());
    getGraphLayoutCache().setSelectsAllInsertedCells(false);
    
    startUndoableEdits();
  }
  
  public ConfigurationSettingInfor getConfigurationSettings(){
	  return this.configurationSettings;
  }

  public void buildNewGraphContent(){
    buildNewGraphContent(getDefaultSize());
  }

  public void buildNewGraphContent(Rectangle bounds) {
    buildNewGraphContent(new Dimension(bounds.width, bounds.height));
  }

  private void buildNewGraphContent(Dimension dimension) {
    stopUndoableEdits();
    setSize(dimension);
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
          prefs.putInt("internalFrameWidth", (int) (getWidth() * getScale()));
          prefs.putInt("internalFrameHeight", (int) (getHeight() * getScale()));
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
  
  public void addElement(YAWLVertex element) {
    getModel().insert(new Object[] {element}, 
                      null, null, null, null);
    NetCellUtilities.scrollNetToShowCells(
        this, 
        new Object[] { element }
    );

    if(element instanceof YAWLTask){
    	YAWLTask task = (YAWLTask)element;
    	if(this.configurationSettings.isNewElementsConfigurable()){
          task.setConfigurable(! task.isConfigurable());
  }
    }

  }

  public Dimension getPreferredSize() {
    Dimension currentPrefferedSize = super.getPreferredSize();

    // Put a little whitespace padding around the default preferred size.
    
    return new Dimension(
      (int) currentPrefferedSize.getWidth() + WHITESPACE_MARGIN,
      (int) currentPrefferedSize.getHeight() + WHITESPACE_MARGIN
    );
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
    Point2D startPoint = getInputConditionDefaultPoint(InputCondition.getVertexSize());
    InputCondition inputCondition = new InputCondition(startPoint);
    addElement(inputCondition);
    return inputCondition;
  }
  
  private Point2D getInputConditionDefaultPoint(Dimension size) {
   return snap(new Point((DEFAULT_MARGIN)  - (size.width/2), 
                         (getHeight()/2) - (size.height/2)));
  }
  
  private OutputCondition addOutputCondition() {
    Point2D startPoint = getOutputConditionDefaultPoint(OutputCondition.getVertexSize());
    OutputCondition outputCondition = new OutputCondition(startPoint);
    addElement(outputCondition);
    return outputCondition;
  }

  private Point2D getOutputConditionDefaultPoint(Dimension size) {
    return snap(new Point((getWidth()-DEFAULT_MARGIN) - (size.width/2), 
                          (getHeight()/2)  - (size.height/2)));
  }

  protected ImageIcon getIconByName(String iconName) {
    return ResourceLoader.getImageAsIcon("/org/yawlfoundation/yawl/editor/resources/yawlElements/" 
           + iconName + ".gif");
  }
  
  public YAWLFlowRelation connect(YAWLVertex sourceVertex, YAWLVertex targetVertex) {
    return connect(sourceVertex.getDefaultSourcePort(), targetVertex.getDefaultTargetPort());
  }
  
  public YAWLFlowRelation connect(YAWLPort source, YAWLPort target) {
    ConnectionSet cs = new ConnectionSet();
    YAWLFlowRelation flow = new YAWLFlowRelation();
    
    cs.connect(flow, source, target);

    getNetModel().beginUpdate();

    getModel().insert(new Object[] {flow},
                      null, cs, null, null);

    setFlowPriorityIfNecessary(flow);
    makeSameTaskFlowPrettyIfNecessary(flow);

    getNetModel().endUpdate();
    return flow;
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
      Point2D.Double awaySourcePoint = 
        new Point2D.Double(flowView.getPoint(0).getX(),
                           flowView.getPoint(0).getY() 
        );

      Point2D.Double awayTargetPoint = 
        new Point2D.Double(flowView.getPoint(1).getX(),
                           flowView.getPoint(1).getY()
        );
      
      switch(flow.getSourceTask().getSplitDecorator().getCardinalPosition()) {
        case Decorator.TOP: {
          awaySourcePoint.setLocation(
              awaySourcePoint.getX(),
              awaySourcePoint.getY() - FLOW_SPACER
          );
          break;
        }
        case Decorator.BOTTOM: {
          awaySourcePoint.setLocation(
              awaySourcePoint.getX(),
              awaySourcePoint.getY() + FLOW_SPACER
          );
          break;
        }
        case Decorator.LEFT: {
          awaySourcePoint.setLocation(
              awaySourcePoint.getX() - FLOW_SPACER,
              awaySourcePoint.getY()
          );
          break;
        }
        case Decorator.RIGHT: {
          awaySourcePoint.setLocation(
              awaySourcePoint.getX() + FLOW_SPACER,
              awaySourcePoint.getY()
          );
          break;
        }
      }

      switch(flow.getTargetTask().getJoinDecorator().getCardinalPosition()) {
        case Decorator.TOP: {
          awayTargetPoint.setLocation(
              awayTargetPoint.getX(),
              awayTargetPoint.getY() - FLOW_SPACER
          );
          break;
        }
        case Decorator.BOTTOM: {
          awayTargetPoint.setLocation(
              awayTargetPoint.getX(),
              awayTargetPoint.getY() + FLOW_SPACER
          );
          break;
        }
        case Decorator.LEFT: {
          awayTargetPoint.setLocation(
              awayTargetPoint.getX() - FLOW_SPACER,
              awayTargetPoint.getY()
          );
          break;
        }
        case Decorator.RIGHT: {
          awayTargetPoint.setLocation(
              awayTargetPoint.getX() + FLOW_SPACER,
              awayTargetPoint.getY()
          );
          break;
        }
      }
      
      flowView.addPoint(1, awaySourcePoint);
      flowView.addPoint(2, awayTargetPoint);
      
      if (flow.getSourceTask().hasTopLeftAdjacentDecorators()) {
        Point2D.Double cornerPoint = new Point2D.Double(
          Math.min(awaySourcePoint.x, awayTargetPoint.x),
          Math.min(awaySourcePoint.y, awayTargetPoint.y)
        );
        flowView.addPoint(2, cornerPoint);
      }

      if (flow.getSourceTask().hasTopRightAdjacentDecorators()) {
        Point2D.Double cornerPoint = new Point2D.Double(
          Math.max(awaySourcePoint.x, awayTargetPoint.x),
          Math.min(awaySourcePoint.y, awayTargetPoint.y)
        );
        flowView.addPoint(2, cornerPoint);
      }

      if (flow.getSourceTask().hasBottomRightAdjacentDecorators()) {
        Point2D.Double cornerPoint = new Point2D.Double(
          Math.max(awaySourcePoint.x, awayTargetPoint.x),
          Math.max(awaySourcePoint.y, awayTargetPoint.y)
        );
        flowView.addPoint(2, cornerPoint);
      }

      if (flow.getSourceTask().hasBottomLeftAdjacentDecorators()) {
        Point2D.Double cornerPoint = new Point2D.Double(
          Math.min(awaySourcePoint.x, awayTargetPoint.x),
          Math.max(awaySourcePoint.y, awayTargetPoint.y)
        );
        flowView.addPoint(2, cornerPoint);
      }
      
      if (flow.getSourceTask().hasHorizontallyAlignedDecorators()) {
        if (awaySourcePoint.x < awayTargetPoint.x) {
          Point2D.Double sourceCornerPoint = new Point2D.Double(
              awaySourcePoint.getX(),
              getViewFor(flow.getSourceTask()).getBounds().getY() - FLOW_SPACER
          );
          
          Point2D.Double targetCornerPoint = new Point2D.Double(
              awayTargetPoint.getX(),
              getViewFor(flow.getSourceTask()).getBounds().getY() - FLOW_SPACER
          );

          flowView.addPoint(2, sourceCornerPoint);
          flowView.addPoint(3, targetCornerPoint);
        } else {
          Point2D.Double sourceCornerPoint = new Point2D.Double(
              awaySourcePoint.getX(),
              getViewFor(flow.getSourceTask()).getBounds().getY() - FLOW_SPACER
          );
          
          Point2D.Double targetCornerPoint = new Point2D.Double(
              awayTargetPoint.getX(),
              getViewFor(flow.getSourceTask()).getBounds().getY() - FLOW_SPACER
          );

          flowView.addPoint(2, sourceCornerPoint);
          flowView.addPoint(3, targetCornerPoint);
        }
      }
      if (flow.getSourceTask().hasVerticallyAlignedDecorators()) {
        if (awaySourcePoint.y < awayTargetPoint.y) {
          Point2D.Double sourceCornerPoint = new Point2D.Double(
              getViewFor(flow.getSourceTask()).getBounds().getX() - FLOW_SPACER,
              awaySourcePoint.getY()
          );
          
          Point2D.Double targetCornerPoint = new Point2D.Double(
              getViewFor(flow.getSourceTask()).getBounds().getX() - FLOW_SPACER,
              awayTargetPoint.getY()
          );

          flowView.addPoint(2, sourceCornerPoint);
          flowView.addPoint(3, targetCornerPoint);
        } else {
          Point2D.Double sourceCornerPoint = new Point2D.Double(
              getViewFor(flow.getSourceTask()).getBounds().getX() - FLOW_SPACER,
              awaySourcePoint.getY()
          );
          
          Point2D.Double targetCornerPoint = new Point2D.Double(
              getViewFor(flow.getSourceTask()).getBounds().getX() - FLOW_SPACER,
              awayTargetPoint.getY()
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
  
  public boolean acceptsIncomingFlows(YAWLCell cell) {
    return getNetModel().acceptsIncomingFlows(cell);
  }
  
  public boolean hasIncomingFlow(YAWLCell cell) {
    return getNetModel().hasIncomingFlow(cell);
  }

  public boolean generatesOutgoingFlows(YAWLCell cell) {
    return getNetModel().generatesOutgoingFlows(cell);
  }

  public boolean hasOutgoingFlow(YAWLCell cell) {
    return getNetModel().hasOutgoingFlow(cell);
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
    
    NetCellUtilities.scrollNetToShowCells(
        this, 
        new Object[] { task }
    );
    
    getGraphLayoutCache().reload();
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
    
    NetCellUtilities.scrollNetToShowCells(
        this, 
        new Object[] { task }
    );

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
  
  private void changeSelectedVertexSize(double baseSize) {
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
          NetCellUtilities.resizeView(this, view, baseSize, baseSize);
        }
      } catch (Exception e) {}
    }
    getNetModel().endUpdate();
  }
  
  private void translateLabelIfNecessary(VertexContainer container, double x, double y) {
    if (container.getLabel() != null) {
      NetCellUtilities.translateView(this, getVertexViewFor(container.getLabel()),x, y);
    }
  }
  
  private void changeDecoratedVertexViewSize(VertexView view, double baseSize) {
    VertexContainer vertexContainer = (VertexContainer) view.getCell();
    
    YAWLVertex vertex = vertexContainer.getVertex();
    if (vertex instanceof YAWLTask) {
      changeDecoratedTaskViewSize(view, baseSize);
    } else {
      NetCellUtilities.resizeView(this, getVertexViewFor(vertex), baseSize, baseSize);
    }
  }

  private void changeDecoratedTaskViewSize(VertexView view, double baseSize) {
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
      splitView = getVertexViewFor(split);
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
    
    if (task.hasDecoratorAtPosition(YAWLTask.LEFT)) {
      NetCellUtilities.translateView(
          this,
          taskView,
          baseSize/4,
          0
      );
      
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
    
    NetCellUtilities.resizeView(this, taskView, baseSize, baseSize);
    if (join != null) {
      if (join.getCardinalPosition() == YAWLTask.BOTTOM ||
          join.getCardinalPosition() == YAWLTask.TOP) {
        NetCellUtilities.resizeView(this, joinView, baseSize, baseSize/4);
      } else {
        NetCellUtilities.resizeView(this, joinView, baseSize/4, baseSize);
      }
    }
    if (split != null) {
      if (split.getCardinalPosition() == YAWLTask.BOTTOM ||
          split.getCardinalPosition() == YAWLTask.TOP) {
        NetCellUtilities.resizeView(this, splitView, baseSize, baseSize/4);
      } else {
        NetCellUtilities.resizeView(this, splitView, baseSize/4, baseSize);
      }
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
  
  public void moveElementsBy(Object[] cells, double x, double y) {
    CellView[] views = new CellView[cells.length];
    for(int i = 0; i < cells.length; i++) {
      views[i] = getViewFor((GraphCell) cells[i]);
    }
    NetCellUtilities.translateViews(this, views, x, y);
  }

  public void moveElementBy(GraphCell cell, double x, double y) {
    NetCellUtilities.translateViews(this, new CellView[] { getViewFor(cell) }, x, y);
  }

  public void moveElementTo(GraphCell cell, double x, double y) {
    double deltaX = x - getCellBounds(cell).getX();
    double deltaY = y - getCellBounds(cell).getY();
    
    moveElementBy(cell, deltaX, deltaY);
  }

  
  private void moveSelectedElementsBy(double x, double y) {
    moveElementsBy(getSelectionCells(), x, y);
  }
  
  public CellView getViewFor(GraphCell cell) {
    return graphLayoutCache.getMapping(cell, false);
  }

  public VertexView getVertexViewFor(GraphCell cell) {
    return (VertexView) getViewFor(cell);
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
        return container.getLabel().getLabel();
      }
    }
    return null;
  }
  
  public void setElementLabel(GraphCell vertex, String labelString) {
    getNetModel().beginUpdate();
    setElementLabelInsideUpdate(vertex, labelString);
    getNetModel().endUpdate();
    
    NetCellUtilities.scrollNetToShowCells(
        this, 
        new Object[] { vertex }
    );

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
      
      if(labelString == null || labelString.equals("") || labelString.equals("null")) {
        return;
      }

      VertexLabel label = new VertexLabel(element, labelString);
      
      getNetModel().insert(objectsToInsert.toArray(),null, null, parentMap, null); 
      
      getGraphLayoutCache().insert(label);
      
      // The ordering of the following code is very important for getting the label
      // to align correctly with the element (JGraph 5.7.3.1 exhibits this behaviour). 

      // Moving after the insert causes the label to resize to its maximum bounding box.
      
      NetCellUtilities.moveViewToLocation(
          this, 
          getVertexViewFor(label),
          getVertexViewFor(container).getBounds().getCenterX(),
          getVertexViewFor(container).getBounds().getMaxY()
      );

      // Adding the label as a child to the container adds a couple pixels to the
      // label width, so we remember the correct label width now.

      double labelWidth = getVertexViewFor(label).getBounds().getWidth();
      
      parentMap.addEntry(label, container);
      
      getNetModel().edit(null,null, parentMap, null); 

      // Center the label under the element. Rounding was necessary to get
      // good placement of the label.
      
      NetCellUtilities.translateView(
          this, 
          getVertexViewFor(label),
          Math.round(-1.0*(labelWidth/2.0)),
          0
      );
        
    } catch (Exception e) {}
  }

  /**
   * Created by Jingxin XU for testing purpose only
   * @param vertex
   * @param configureInfor
   */
  public void setConfigureInforUpdate(GraphCell vertex, String configureInfor) {
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



	      if(configureInfor == null || configureInfor.equals("")) {
	        return;
	      }

	      ConfigureInformation config = new ConfigureInformation(element, configureInfor);

	      getNetModel().insert(objectsToInsert.toArray(),null, null, parentMap, null);

	      getGraphLayoutCache().insert(config);

	      // The ordering of the following code is very important for getting the label
	      // to align correctly with the element (JGraph 5.7.3.1 exhibits this behaviour).

	      // Moving after the insert causes the label to resize to its maximum bounding box.

	      NetCellUtilities.moveViewToLocation(
	          this,
	          getVertexViewFor(config),
	          getVertexViewFor(container).getBounds().getCenterX(),
	          getVertexViewFor(container).getBounds().getMaxY()
	      );

	      // Adding the label as a child to the container adds a couple pixels to the
	      // label width, so we remember the correct label width now.

	      double configWidth = getVertexViewFor(config).getBounds().getWidth();

	      parentMap.addEntry(config, container);

	      getNetModel().edit(null,null, parentMap, null);

	      // Center the label under the element. Rounding was necessary to get
	      // good placement of the label.

	      NetCellUtilities.translateView(
	          this,
	          getVertexViewFor(config),
	          Math.round(-1.0*(configWidth/2.0)),
	          0
	      );

	    } catch (Exception e) {}
	  }


  public NetGraphModel getNetModel() {
   return (NetGraphModel) getModel();
  }
  
  public CancellationSetModel getCancellationSetModel() {
    return this.cancellationSetModel;
  }

  public void resetCancellationSet() {
      YAWLTask task = cancellationSetModel.getTriggeringTask();
      if (task != null) changeCancellationSet(task);       
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
    changeVertexBackground(triggeringTask, triggeringTask.getBackgroundColor());
    for (YAWLCell cell : triggeringTask.getCancellationSet().getSetMembers()) {
       showCellAsNotInCurrentCancellationSet(cell);
    }
  }
  
  private void showCurrentCancellationSet() {
    YAWLTask triggeringTask = cancellationSetModel.getTriggeringTask();
    changeVertexBackground(triggeringTask, 
                           CancellationSetModel.CANCELLATION_SET_TRIGGER_BACKGROUND);
    for (YAWLCell cell : triggeringTask.getCancellationSet().getSetMembers()) {
       showCellAsInCurrentCancellationSet(cell);
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

  public Color getCellForeground(GraphCell cell) {
      String colourKey = (cell instanceof Edge) ? GraphConstants.LINECOLOR :
                                                  GraphConstants.FOREGROUND;
      return (Color) getModel().getAttributes(cell).get(colourKey);
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

  public void changeVertexBackground(YAWLVertex vertex, Color color) {
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
  
  public  void changeLineWidth(YAWLVertex cell) {
	    Map attributes = GraphConstants.createAttributes(cell,
	                                                     GraphConstants.LINEWIDTH,
	                                                     (float)20.0);
	    getModel().edit(attributes, null, null, null);
	  }

  public YAWLEditorNetPanel getFrame() {
    return frame;
  }
  
  public void setFrame(YAWLEditorNetPanel frame) {
    this.frame = frame;
  }

  public void removeFrame() { frame = null; }  
  
  public String getName() {
    return getNetModel().getName();
  }
  
  public void setName(String name) {
    getNetModel().setName(name);
  }
  
  
  public void setUnfoldingNet(YAWLCompositeTask task, NetGraph graph) {
    if (graph != null) {
      ((YAWLTask)task).setDecomposition(graph.getNetModel().getDecomposition());
    } else {
      ((YAWLTask)task).setDecomposition(null);
    }
    if (!((YAWLTask) task).hasLabel()) {
      String name = graph.getNetModel().getName();
      setElementLabel((YAWLTask) task, name);
    }
  }
  
  /**
   * Sets an icon on a vertex that becomes an undoable action for the graph.
   * @param vertex The vertex to add the icon to.
   * @param iconPath The file path to the icon needed.
   */
  
  public void setVertexIcon(YAWLVertex vertex, String iconPath) {
    if (vertex.getIconPath() != null && vertex.getIconPath().equals(iconPath)) {
      return;
    }
    getNetModel().beginUpdate();
    
    getNetModel().postEdit(
        new UndoableTaskIconChange(
              this,
              vertex,
              vertex.getIconPath(), 
              iconPath
        )
    );
    
    vertex.setIconPath(iconPath);
    repaint();
    
    getNetModel().endUpdate();
  }
  
  public void setTaskDecomposition(YAWLTask task, Decomposition decomposition) {
    Decomposition oldDecomposition = task.getDecomposition();
    if ((decomposition != null) && decomposition.equals(oldDecomposition)) {
      return;
    }
    task.setDecomposition(decomposition);
    task.getParameterLists().reset();

    getNetModel().beginUpdate();
    
    if (decomposition != null) {
        if (task.getLabel() == null) {
          setElementLabelInsideUpdate(task, decomposition.getLabel());
        }
    }
    else {

        // if decomp is null, its being dropped, so if the decomp's name == the task's label
        // remove the label also
        if ((oldDecomposition != null) &&
            (oldDecomposition.getLabel().equals(task.getLabel()))) {
            setElementLabelInsideUpdate(task, null);
        }
    }

    
    getNetModel().postEdit(
        new UndoableTaskDecompositionChange(
              task, 
              oldDecomposition, 
              task.getDecomposition()
        )
      );    
    
    getNetModel().endUpdate();
  }
  
  public NetMarqueeHandler getNetMarqueeHandler() {
    return (NetMarqueeHandler) getMarqueeHandler();
  }

public ServiceAutomatonTree getServiceAutonomous() {
	return serviceAutomaton;
}

public void setServiceAutonomous(ServiceAutomatonTree serviceAutonomous) {
	this.serviceAutomaton = serviceAutonomous;
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
}