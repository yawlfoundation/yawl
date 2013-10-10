/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.editor.ui.net;

import org.jgraph.JGraph;
import org.jgraph.graph.*;
import org.yawlfoundation.yawl.editor.ui.actions.net.*;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetCellFactory;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetCellUtilities;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.ui.swing.net.YAWLEditorNetPanel;
import org.yawlfoundation.yawl.editor.ui.swing.undo.UndoableTaskDecompositionChange;
import org.yawlfoundation.yawl.editor.ui.swing.undo.UndoableTaskIconChange;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;
import org.yawlfoundation.yawl.elements.YDecomposition;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NetGraph extends JGraph {

  public static final int DEFAULT_MARGIN  = 50;
  
  // Default margin size of whitespace to appear around elements being added to a net
  public static final int WHITESPACE_MARGIN  = 20;

  private YAWLEditorNetPanel frame;
  private NetSelectionListener selectionListener;
  private CancellationSetModel cancellationSetModel;

  /**
   * The following fields are added by Jingxin XU
   */
  //stores all editor settings of a net tp steer configuration opportunities
  private ConfigurationSettingInfo configurationSettings;
  private ServiceAutomatonTree serviceAutomaton = null;

  /**********/  

  public NetGraph() {
    super();
    initialize();
  }


  public NetGraph(YDecomposition decomposition) {
    super();
    initialize();
    getNetModel().setDecomposition(decomposition);
  }

  
  private void initialize() {
    buildBasicGraphContent();
    this.configurationSettings = new ConfigurationSettingInfo();
  }

  public void createServiceAutonomous() {
	  this.serviceAutomaton = new ServiceAutomatonTree(this);
  }


    @Override
    public void updateUI() {
        setUI(new NetGraphUI());
        invalidate();
    }

    private void buildBasicGraphContent() {
        setGridMode(JGraph.DOT_GRID_MODE);
        setGridVisible(UserSettings.getShowGrid());
        setGridEnabled(true);
        setDoubleBuffered(true);
        setGridSize(4);
        setMinimumMove(4);
        setBackground(UserSettings.getNetBackgroundColour());
        setFont(UserSettings.getDefaultFont());
        setForeground(UserSettings.getDefaultTextColour());
        setAntiAliased(UserSettings.getShowAntiAliasing());
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
  
  public ConfigurationSettingInfo getConfigurationSettings(){
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
            UserSettings.setInternalFrameWidth((int) (getWidth() * getScale()));
            UserSettings.setInternalFrameHeight((int) (getHeight() * getScale()));
        } 
    });
  }
  
  private void bindCancellationModel() {
      cancellationSetModel = new CancellationSetModel(this);
      cancellationSetModel.subscribe(AddToVisibleCancellationSetAction.getInstance());
      cancellationSetModel.subscribe(RemoveFromVisibleCancellationSetAction.getInstance());
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
    return new Dimension(UserSettings.getInternalFrameWidth(),
                         UserSettings.getInternalFrameHeight());
  }
  
  public NetSelectionListener getSelectionListener() {
    return selectionListener; 
  }
  
  public void addElement(YAWLVertex element) {
    getModel().insert(new Object[] {element}, null, null, null, null);
    NetCellUtilities.scrollNetToShowCells(this, new Object[] { element });

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
    return ResourceLoader.getImageAsIcon("/org/yawlfoundation/yawl/editor/ui/resources/yawlElements/"
            + iconName + ".gif");
  }
  
  public YAWLFlowRelation connect(YAWLPort source, YAWLPort target) {
      YAWLFlowRelation flow = NetCellFactory.insertFlow(this,
              source.getVertexID(), target.getVertexID());
      connect(flow, source, target);
      return flow;
  }

    public void connect(YAWLFlowRelation flow, YAWLVertex source, YAWLVertex target) {
        connect(flow, source.getDefaultSourcePort(), target.getDefaultTargetPort());
    }

  public void connect(YAWLFlowRelation flow, YAWLPort source, YAWLPort target) {
      ConnectionSet cs = new ConnectionSet();
      cs.connect(flow, source, target);

      getNetModel().beginUpdate();
      getModel().insert(new Object[] {flow}, null, cs, null, null);
 //     setFlowPriorityIfNecessary(flow);
      if (flow.connectsTaskToItself()) {
          NetCellUtilities.prettifyLoopingFlow(this, flow,
                  (EdgeView) getViewFor(flow), getViewFor(flow.getSourceTask()));
      }
      updateCPorts(source, target);
      getNetModel().endUpdate();
   }
  
  private void setFlowPriorityIfNecessary(YAWLFlowRelation flow) {
    YAWLCell sourceCell = getSourceOf(flow);
    if (sourceCell instanceof SplitDecorator) {
      Set edges = this.getEdges(new YAWLCell[] { sourceCell} );
      flow.setPriority(edges.size() - 1);
    }
  }

    private void updateCPorts(YAWLPort source, YAWLPort target) {
        updateCPort(source);
        updateCPort(target);
    }
  
    private void updateCPort(YAWLPort port) {
        YAWLTask task = port.getTask();
        if (task != null && task.isConfigurable()) {
            task.configureReset();
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
      return view != null && !view.isLeaf();
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
        if (task.getJoinDecorator() == null &&
                (type == Decorator.NO_TYPE || position == YAWLTask.NOWHERE)) {
            return;
        }

        getNetModel().beginUpdate();
        String label = getElementLabel(task);
        setElementLabelInsideUpdate(task, null);
        getNetModel().setJoinDecorator(task, type, position);
        setElementLabelInsideUpdate(task, label);
        getNetModel().endUpdate();
        NetCellUtilities.scrollNetToShowCells(this, new Object[] { task });
        getGraphLayoutCache().reload();
    }

    public void setSplitDecorator(YAWLTask task, int type, int position) {
        if(task.getSplitDecorator() == null &&
                (type == Decorator.NO_TYPE || position == YAWLTask.NOWHERE)) {
            return;
        }

        getNetModel().beginUpdate();
        String label = getElementLabel(task);
        setElementLabelInsideUpdate(task, null);
        getNetModel().setSplitDecorator(task, type, position);
        setElementLabelInsideUpdate(task, label);
        getNetModel().endUpdate();
        NetCellUtilities.scrollNetToShowCells(this, new Object[]{task });
        getGraphLayoutCache().reload();
    }

    public Set<Object> removeCellsAndTheirEdges(Object[] cells) {
        return getNetModel().removeCells(cells);
    }


    public Set<Object> removeSelectedCellsAndTheirEdges() {
        return getNetModel().removeCells(getSelectionCells());
    }

    public void increaseSelectedVertexSize() {
        changeSelectedVertexSize(getGridSize());
    }

    public void decreaseSelectedVertexSize() {
        changeSelectedVertexSize(-getGridSize());
    }

    private void changeSelectedVertexSize(double baseSize) {
        getNetModel().beginUpdate();
        for (Object cell : getSelectionCells()) {
            try {
                VertexView view = getVertexViewFor((GraphCell) cell);
                if (view.getCell() instanceof VertexContainer) {
                    changeDecoratedVertexViewSize(view, baseSize);
                    translateLabelIfNecessary((VertexContainer) cell, baseSize / 2, baseSize);
                }
                else {
                    NetCellUtilities.resizeView(this, view, baseSize, baseSize);
                }
            }
            catch (Exception e) {
                //
            }
        }
        getNetModel().endUpdate();
    }

    private void translateLabelIfNecessary(VertexContainer container, double x, double y) {
        if (container.getLabel() != null) {
            NetCellUtilities.translateView(this,
                    getVertexViewFor(container.getLabel()),x, y);
        }
    }

    private void changeDecoratedVertexViewSize(VertexView view, double baseSize) {
        VertexContainer vertexContainer = (VertexContainer) view.getCell();
        YAWLVertex vertex = vertexContainer.getVertex();
        if (vertex instanceof YAWLTask) {
            changeDecoratedTaskViewSize(view, baseSize);
        }
        else {
            NetCellUtilities.resizeView(this, getVertexViewFor(vertex), baseSize, baseSize);
        }
    }

    private void changeDecoratedTaskViewSize(VertexView view, double baseSize) {
        VertexContainer decoratedTask = (VertexContainer) view.getCell();
        YAWLTask task = (YAWLTask) decoratedTask.getVertex();
        VertexView taskView = getVertexViewFor(task);
        HashSet verticalViews = new HashSet();
        HashSet horizontalViews = new HashSet();

        JoinDecorator join = task.getJoinDecorator();
        VertexView joinView = null;
        if (join != null) {
            joinView = getVertexViewFor(join);
            if (join.getCardinalPosition() == YAWLTask.LEFT ||
                    join.getCardinalPosition() == YAWLTask.RIGHT) {
                verticalViews.add(joinView);
            }
            if (join.getCardinalPosition() == YAWLTask.TOP ||
                    join.getCardinalPosition() == YAWLTask.BOTTOM) {
                horizontalViews.add(joinView);
            }
        }

        SplitDecorator split  = task.getSplitDecorator();
        VertexView splitView = null;
        if (split != null) {
            splitView = getVertexViewFor(split);
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
            NetCellUtilities.translateView(this, taskView, baseSize / 4, 0);

            if (join != null) {
                if(join.getCardinalPosition() == YAWLTask.TOP ||
                        join.getCardinalPosition() == YAWLTask.BOTTOM) {
                    NetCellUtilities.translateView(this, joinView, baseSize / 4, 0);
                }
                if (join.getCardinalPosition() == YAWLTask.RIGHT) {
                    NetCellUtilities.translateView(this, joinView, baseSize + baseSize / 4, 0);
                }
            }
            if (split != null) {
                if (split.getCardinalPosition() == YAWLTask.TOP ||
                        split.getCardinalPosition() == YAWLTask.BOTTOM) {
                    NetCellUtilities.translateView(this, splitView, baseSize / 4, 0); }
                if(split.getCardinalPosition() == YAWLTask.RIGHT) {
                    NetCellUtilities.translateView(this, splitView, baseSize + baseSize / 4, 0); }
            }
        }
        else { // no decorator on left
            if (join != null && join.getCardinalPosition() == YAWLTask.RIGHT) {
                NetCellUtilities.translateView(this, joinView, baseSize, 0);
            }
            if (split != null && split.getCardinalPosition() == YAWLTask.RIGHT) {
                NetCellUtilities.translateView(this, splitView, baseSize, 0);
            }
        }
        if (task.hasDecoratorAtPosition(YAWLTask.TOP)) {
            NetCellUtilities.translateView(this, taskView, 0, baseSize / 4);

            if (join != null) {
                if(join.getCardinalPosition() == YAWLTask.LEFT ||
                        join.getCardinalPosition() == YAWLTask.RIGHT) {
                    NetCellUtilities.translateView(this, joinView, 0, baseSize / 4);
                }
                if (join.getCardinalPosition() == YAWLTask.BOTTOM) {
                    NetCellUtilities.translateView(this, joinView, 0, baseSize + baseSize / 4);
                }
            }
            if (split != null) {
                if (split.getCardinalPosition() == YAWLTask.LEFT ||
                        split.getCardinalPosition() == YAWLTask.RIGHT) {
                    NetCellUtilities.translateView(this, splitView, 0, baseSize / 4);
                }
                if (split.getCardinalPosition() == YAWLTask.BOTTOM) {
                    NetCellUtilities.translateView(this, splitView, 0, baseSize + baseSize / 4);
                }
            }
        }
        else { // no decorator on top
            if (join != null && join.getCardinalPosition() == YAWLTask.BOTTOM) {
                NetCellUtilities.translateView(this, joinView, 0, baseSize);
            }
            if(split != null && split.getCardinalPosition() == YAWLTask.BOTTOM) {
                NetCellUtilities.translateView(this, splitView, 0, baseSize);
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
        return container.getLabel().getText();
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

        VertexLabel oldLabel = container.getLabel();
      if(oldLabel != null) {
        getNetModel().removeCells(new Object[]{oldLabel});
      }
      
      if(labelString == null || labelString.equals("") || labelString.equals("null")) {
        return;
      }

      VertexLabel newLabel = new VertexLabel(element, labelString);
        if (oldLabel != null) {
            newLabel.setFont(oldLabel.getFont());
            newLabel.setForeground(oldLabel.getForeground());
        }
      
      getNetModel().insert(objectsToInsert.toArray(),null, null, parentMap, null); 
      
      getGraphLayoutCache().insert(newLabel);
      
      // The ordering of the following code is very important for getting the label
      // to align correctly with the element (JGraph 5.7.3.1 exhibits this behaviour). 

      // Moving after the insert causes the label to resize to its maximum bounding box.
      
      NetCellUtilities.moveViewToLocation(
          this, 
          getVertexViewFor(newLabel),
          getVertexViewFor(container).getBounds().getCenterX(),
          getVertexViewFor(container).getBounds().getMaxY()
      );

      // Adding the label as a child to the container adds a couple pixels to the
      // label width, so we remember the correct label width now.

      double labelWidth = getVertexViewFor(newLabel).getBounds().getWidth();

      parentMap.addEntry(newLabel, container);
      
      getNetModel().edit(null,null, parentMap, null); 

      // Center the label under the element. Rounding was necessary to get
      // good placement of the label.
      
      NetCellUtilities.translateView(
          this, 
          getVertexViewFor(newLabel),
          Math.round(-1.0*(labelWidth/2.0)),
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
    for (YAWLCell cell : triggeringTask.getCancellationSet().getMembers()) {
       showCellAsNotInCurrentCancellationSet(cell);
    }
  }
  
  private void showCurrentCancellationSet() {
    YAWLTask triggeringTask = cancellationSetModel.getTriggeringTask();
    changeVertexBackground(triggeringTask, 
                           CancellationSetModel.CANCELLATION_SET_TRIGGER_BACKGROUND);
    for (YAWLCell cell : triggeringTask.getCancellationSet().getMembers()) {
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
      for (Object validSelectedCell : validSelectedCells) {
          removeCellFromCancellationSetInsideUpdate((YAWLCell) validSelectedCell);
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

  public void removeFrame() {
      frame.setVisible(false);
      frame = null;
  }
  
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
      if (! (vertex instanceof YAWLTask)) return;

      YAWLTask task = (YAWLTask) vertex;
      if (task.getIconPath() != null && task.getIconPath().equals(iconPath)) {
          return;
      }

      getNetModel().beginUpdate();
      getNetModel().postEdit(
          new UndoableTaskIconChange(this, vertex, task.getIconPath(), iconPath));

      task.setIconPath(iconPath);
      repaint();
      getNetModel().endUpdate();
  }
  
  public void setTaskDecomposition(YAWLTask task, YDecomposition decomposition) {
      YDecomposition oldDecomposition = task.getDecomposition();
    if ((decomposition != null) && decomposition.equals(oldDecomposition)) {
      return;
    }

    getNetModel().beginUpdate();
    
    if (decomposition != null) {
        if (task.getLabel() == null) {
          setElementLabelInsideUpdate(task, decomposition.getID());
        }
    }
    else {

        // if decomp is null, its being dropped, so if the decomp's name == the task's label
        // remove the label also
        if ((oldDecomposition != null) &&
            (oldDecomposition.getID().equals(task.getLabel()))) {
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
//      net.getNetMarqueeHandler().connectElementsOrIgnoreFlow();
    } catch (Exception e) {}
  }
}
}