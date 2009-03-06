package org.yawlfoundation.yawl.editor.net;

import org.jgraph.graph.BasicMarqueeHandler;
import org.jgraph.graph.CellView;
import org.jgraph.graph.Port;
import org.jgraph.graph.PortView;
import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.elements.model.*;
import org.yawlfoundation.yawl.editor.net.utilities.NetCellFactory;
import org.yawlfoundation.yawl.editor.swing.CursorFactory;
import org.yawlfoundation.yawl.editor.swing.menu.ControlFlowPalette;
import org.yawlfoundation.yawl.editor.swing.menu.Palette;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;


public class NetMarqueeHandler extends BasicMarqueeHandler {
  private NetGraph net;
  
  private static final int PORT_BUFFER = 2;
  
  private PortView sourcePort, targetPort = null;
  
  private enum State {
    ABOVE_CANVAS,
    ABOVE_VERTEX,
    ABOVE_FLOW_RELATION,
    ABOVE_OUTGOING_PORT,
    DRAGGING_VERTEX,
    DRAWING_FLOW_RELATION
  }
  
  private State state = State.ABOVE_CANVAS;
  
  public NetMarqueeHandler(NetGraph net) {
    this.net = net;
  }
  
  /**
   * Determines whether the default marquee behaviour is sufficient, or whether
   * our own response to mouse events are more appropriate. Specifically, we rely on the
   * functionality of {@link BasicMarqueeHandler} in the following scenarios:
   * <ul>
   *   <li> When the Palette is in Marquee mode
   *   <li> When the  Palette is not in Marquee mode, but the mouse is hovering above a Vertex,
   *   <li> When the Palette is not in Marquee mode, but the mouse if hovering above a flow relation.
   * <li>
   * For the last two, if the mouse hovers above these elements, we want the default drag behaviour
   * of {@link BasicMarqueeHandler} to be used on them. What remains in this class is the drawing of flows
   * between net elements.
   */
  public boolean isForceMarqueeEvent(MouseEvent event) {
    if (Palette.getInstance().getControlFlowPaletteState() == 
        ControlFlowPalette.SelectionState.MARQUEE) {
      return false;
    }
    if (Palette.getInstance().getControlFlowPaletteState() != ControlFlowPalette.SelectionState.MARQUEE && 
        state == State.ABOVE_VERTEX) {
      return false;
    }
    if (Palette.getInstance().getControlFlowPaletteState() != ControlFlowPalette.SelectionState.MARQUEE && 
        state == State.ABOVE_FLOW_RELATION) {
      return false;
    }
    return true;
  }

  public NetGraph getNet() {
    return this.net;
  }
  
  
  /**
   * Defaults to typical marquee behaviour if the Palette is in Marquee mode,
   * Otherwise, the state of this marquee is determined through what lies underneat
   * the mouse.
   * @param point
   */

  public void mouseMoved(MouseEvent event) {
    if (Palette.getInstance().getControlFlowPaletteState() == 
        ControlFlowPalette.SelectionState.MARQUEE ) {
      super.mouseMoved(event);
      return;
    }
    
     State oldState = state;

     determineStateFromPoint(event.getPoint());
     
     if (state == State.ABOVE_CANVAS) {
       setCursorFromPalette();
     }

     doStateTransitionProcessing(event.getPoint(), oldState, state);   
     
     event.consume();
  }
  
  private void setCursorFromPalette() {
    switch (Palette.getInstance().getControlFlowPaletteState()) {
      case MARQUEE : {
        matchCursorTo(CursorFactory.SELECTION);
        break;
      }
      case DRAG: {
        matchCursorTo(CursorFactory.DRAG);
        break;
      }
      case CONDITION: {
        matchCursorTo(CursorFactory.CONDITION);
        break;
      }
      case ATOMIC_TASK: {
        matchCursorTo(CursorFactory.ATOMIC_TASK);
        break;
      }
      case COMPOSITE_TASK: {
        matchCursorTo(CursorFactory.COMPOSITE_TASK);
        break;
      }
      case MULTIPLE_ATOMIC_TASK: {
        matchCursorTo(CursorFactory.MULTIPLE_ATOMIC_TASK);
        break;
      }
      case MULTIPLE_COMPOSITE_TASK: {
        matchCursorTo(CursorFactory.MULTIPLE_COMPOSITE_TASK);
        break;
      }
    }
  }
  
  private void matchCursorTo(int cursorType) {
    getNet().setCursor(CursorFactory.getCustomCursor(cursorType));
    Palette.getInstance().refreshSelected();
  }
  
  /**
   * Sets the marquee state based on the net element currently underneath
   * the point specified.
   * @param point
   */
  
  private void determineStateFromPoint(Point point) {
    if (isPointOverOutgoingPort(point)) {
      state = State.ABOVE_OUTGOING_PORT;
    } else if (isPointOverVertex(point)) {
      state = State.ABOVE_VERTEX;
    } else if (isPointOverFlow(point)){
      state = State.ABOVE_FLOW_RELATION;
    } else {
      state = State.ABOVE_CANVAS;
    }
  }
  
  /**
   * Defaults to typical marquee behaviour when the marquee palette button
   * is selected. Otherwise, if there is nothing under the mouse upon a mouse press
   * a new net element will be added. If there is a vertex or condition under the mouse
   * this method will not be invoked (see {@link isForceMarqueeEvent(MouseEvent event)}, 
   * relying instead on the default behaviour of {@link BasicMarqueeHandler}, 
   * which is typically to begin a select/drag operation on the element. If there is
   * a valid outgoing flow under the mouse, this is a trigger to begin drawing a flow relation.
   */
  
  public void mousePressed(MouseEvent event) {
    if (Palette.getInstance().getControlFlowPaletteState() == 
        ControlFlowPalette.SelectionState.MARQUEE) {
      super.mousePressed(event);
      return;
    }

    if (SwingUtilities.isRightMouseButton(event)) {
      super.mousePressed(event);
      return;
    }
    
    State oldState = state;
    
    switch(oldState) {
      case ABOVE_VERTEX: {
        state = State.DRAGGING_VERTEX;
        break;
      }
      case ABOVE_OUTGOING_PORT: {
        state = State.DRAWING_FLOW_RELATION;
        break;
      }
      case ABOVE_CANVAS: {
        switch (Palette.getInstance().getControlFlowPaletteState()) {
          case CONDITION: {
              NetCellFactory.insertCondition(
                  getNet(), 
                  getNearestSnapPoint(
                      event.getPoint()
                  )
            );
            state = State.ABOVE_VERTEX;
            break;        
          }
          case ATOMIC_TASK: {
                NetCellFactory.insertAtomicTask(
                    getNet(), 
                    getNearestSnapPoint(
                        event.getPoint()
                    ),
                    Palette.getInstance().getSelectedAtomicTaskIconPath()
            );
            state = State.ABOVE_VERTEX;
            break;        
          }
          case MULTIPLE_ATOMIC_TASK: {
                NetCellFactory.insertMultipleAtomicTask(
                    getNet(), 
                    getNearestSnapPoint(
                        event.getPoint()
                    ),
                    Palette.getInstance().getSelectedAtomicTaskIconPath()
            );
            state = State.ABOVE_VERTEX;
            break;        
          }
          case COMPOSITE_TASK: {
                NetCellFactory.insertCompositeTask(
                    getNet(),
                    getNearestSnapPoint(
                        event.getPoint()
                    )
            );
            state = State.ABOVE_VERTEX;
            break;        
          }
          case MULTIPLE_COMPOSITE_TASK: {
                NetCellFactory.insertMultipleCompositeTask(
                    getNet(), 
                    getNearestSnapPoint(
                        event.getPoint()
                    )
            );
            state = State.ABOVE_VERTEX;
            break;        
          }
        }  // switch Palette.getInstance().getSelected();
        break;
      }
    }
    doStateTransitionProcessing(event.getPoint(), oldState, state);
  }

  /**
   * Defaults to typical marquee behaviour unless a flow relation is being drawn.
   * In that case, each drag event will redraw a line from the mouse cursor to the
   * source port. If there is a valid incoming port under the mouse, this will also
   * be hilighted to identify that a valid flow may be drawn upon a mouse 
   * release event.
   */
  
  public void mouseDragged(MouseEvent event) {
    if (Palette.getInstance().getControlFlowPaletteState() == 
        ControlFlowPalette.SelectionState.MARQUEE) {
      super.mouseDragged(event);
      return;
    }
    
    if (state == State.DRAWING_FLOW_RELATION) {
      if (sourcePort != null) {
        hidePotentialFlow();
        setCurrentPoint(getNet().snap(event.getPoint()));
        showPotentialFlow();
        PortView portUnderMouse = getPortViewAt(event.getPoint());
        if (portUnderMouse != null && portUnderMouse != sourcePort &&
            connectionAllowable(sourcePort, portUnderMouse) &&
            acceptsIncomingFlows(portUnderMouse)) {
          hidePort(targetPort);         
          targetPort = portUnderMouse;    
          showPort(targetPort);
        }
        if (portUnderMouse == null) {
          hidePort(targetPort);
          targetPort = null; 
        }
      }
      event.consume();
    }
  }

  /**
   *  Defaults to typical marquee behaviour unless a flow relation is being
   *  drawn. In this case, the mouse release event checks to see if there is a
   *  valid incoming port under the mouse. If so, a flow relation will be 
   *  established between the source and target ports, otherwise the flow is
   *  ignrored.
   */
  
  public void mouseReleased(MouseEvent event) {
    if (Palette.getInstance().getControlFlowPaletteState() == 
        ControlFlowPalette.SelectionState.MARQUEE) {
      super.mouseReleased(event);
      return;
    }
    
    if (state == State.DRAWING_FLOW_RELATION) {
      connectElementsOrIgnoreFlow();
      determineStateFromPoint(event.getPoint());
    }
    setCursorFromPalette();
    event.consume();
  }

  /**
   * A convenience method to determine whether
   * the current point is above a flow relation.
   * @param point
   * @return Returns true if above a flow relation, false otherwise.
   */
  
  private boolean isPointOverFlow(Point point) {
    if (getElementAt(point) == null) {
      return false;
    }
    if (getElementAt(point) instanceof YAWLFlowRelation) {
      return true;
    }
    return false;
  }

  /**
   * A convenience method to determine whether the curent
   * point is above a YAWL vertex (task or condition).
   * @param point
   * @return Returns true if above a vertex, false otherwise.
   */
  
  private boolean isPointOverVertex(Point point) {
    if (getElementAt(point) == null) {
      return false;
    }
    if (getElementAt(point) instanceof VertexContainer ||
        getElementAt(point) instanceof YAWLVertex) {
      return true;
    }
    return false;
  }
  
  /**
   * A concenienve method to determine whether the current
   * point is above a valid outgoing port of a YAWL Vertex.
   * @param point
   * @return true if above a valid outgoing port, false otherwise.
   */
  
  private boolean isPointOverOutgoingPort(Point point) {
    PortView portUnderMouse = getPortViewAt(point);
    if (portUnderMouse == null) {
      return false;
    }
    
    // Selected flows attached to a port take precedence. We consider the mouse
    // to be over the flow, not the port.

    for(Object flowAsObject : ((YAWLPort) portUnderMouse.getCell()).getEdges()) {
      YAWLFlowRelation flow = (YAWLFlowRelation) flowAsObject;
      for(Object selectedCell : getNet().getSelectionCells()) {
        if (flow == selectedCell) {
          return false;
        }
      }
    }

    if (generatesOutgoingFlows(portUnderMouse)) {
      return true;
    }
    return false;
  }
  
  /**
   * This method is responsible for implementing the necessary side-effects 
   * of state transitions in this marquee handler.
   * @param point
   * @param oldState
   * @param newState
   */
  
  private void doStateTransitionProcessing(Point point, State oldState, State newState) {
    switch(oldState) {
      case ABOVE_CANVAS: {
        switch(newState) {
          case ABOVE_OUTGOING_PORT:  {
            doMouseMovedOverPortProcessing(
              getPortViewAt(point)                  
            );
            break;
          }
        }  
        break;
      }  // case OVER_NOTHING
      case ABOVE_OUTGOING_PORT: {
        switch(newState) {
          case ABOVE_OUTGOING_PORT: {
            if (getPortViewAt(point) != sourcePort) {
              doMouseMovedOverPortProcessing(
                  getPortViewAt(point)                  
              );
            }
            break;
          }
          case DRAWING_FLOW_RELATION: {
            // we deliberately do nothing. mouseDragged() event handling takes care of what needs to be done.
            break;
          }
          default: {
            hidePort(sourcePort);
            sourcePort = null;
            setCursorFromPalette();
            break;
          }
        }
        break;
      }  // case OVER_OUTGOING_PORT
    }
  }
  
  /**
   * A convenience method to hide a previous source port if necessary, 
   * to set the source port the the one specified, and then to make the 
   * source port visible.
   * @param portView
   */
  
  private void doMouseMovedOverPortProcessing(PortView portView) {
    if (sourcePort == null || sourcePort != portView) {
      hidePort(sourcePort);
    }
    sourcePort = portView;
    showPort(sourcePort);
    matchCursorTo(CursorFactory.FLOW_RELATION);
    YAWLEditor.setStatusBarText(
        "Left-click on this connection point, drag the flow to another " +
        "valid connection point and release the mouse button to create a flow."
    );
  }

  private PortView getPortViewAt(Point point) {
    return getNet().getPortViewAt(point.getX(), point.getY());
  }
  
  private Object getElementAt(Point point) {
    return getNet().getFirstCellForLocation(point.getX(), point.getY());
  }
  
  /**
   * Hides the specified port. 
   * @param thisPort
   */
  
  protected void hidePort(PortView thisPort) {
    if (thisPort == null) {
      return;
    }
    
    final Graphics graphics = getNet().getGraphics();
    
    graphics.setColor(getNet().getBackground());
    graphics.setXORMode(getNet().getMarqueeColor());
    
    showPort(thisPort);
  }
  
  /**
   * Makes the specified port visible.
   * @param port
   */
  
  protected void showPort(PortView port) {
    if (port != null) {
      Rectangle2D portBounds = getNet().toScreen(port.getBounds());
      
      Rectangle2D.Double portViewbox = new Rectangle2D.Double(
        portBounds.getX() - PORT_BUFFER/2, 
        portBounds.getY() - PORT_BUFFER/2,
        portBounds.getWidth() + (2 * PORT_BUFFER), 
        portBounds.getHeight() + (2 * PORT_BUFFER)        
      );
      
      getNet().getUI().paintCell(
        getNet().getGraphics(),
        port, 
        portViewbox, 
        true
      );
    }
  }
  
  /**
   * This method checks to determine whether the specified port
   * can have outgoing flows drawn from it.
   * @param portView
   * @return true if flows can start from this port, false otherwise.
   */
  
  private boolean generatesOutgoingFlows(PortView portView) {
    CellView parentView = portView.getParentView();
    YAWLPort yawlPort  = (YAWLPort) portView.getCell();
    YAWLCell vertex = (YAWLCell) parentView.getCell();  
    return yawlPort.generatesOutgoingFlows() && 
           vertex.generatesOutgoingFlows()   && 
           getNet().generatesOutgoingFlows(vertex);
  }
  
  /**
   * This method checks to determine whether the specified port
   * is allowed to have incoming flows attached to it.
   * @param portView
   * @return true if flows can end at this port, false otherwise.
   */
  
  private boolean acceptsIncomingFlows(PortView portView) {
    CellView parentView = portView.getParentView();
    YAWLPort yawlPort  = (YAWLPort) portView.getCell();
    YAWLCell vertex = (YAWLCell) parentView.getCell();  
    return yawlPort.acceptsIncomingFlows() && 
           vertex.acceptsIncomingFlows()   &&
           getNet().acceptsIncomingFlows(vertex);
  }

  
  private Point2D getNearestSnapPoint(Point2D point) {
    return getNet().snap(getNet().fromScreen(point));
  }
 
  /**
   * Returns whether the net considers the connection of the two ports
   * specified to be valid or not.
   * @param source
   * @param target
   * @return true if a connection between the ports is valid, false otherwise.
   */
  
  private boolean connectionAllowable(PortView source, PortView target) {
    return getNet().connectionAllowable(
        (Port) source.getCell(), 
        (Port) target.getCell()
    );
  }
  
  /**
   * Hides a potential flow relation. 
   * See {@link paintPotentialFlow(Color fg, Color bg, Graphics g)}.
   */
  
  private void hidePotentialFlow() {
    paintPotentialFlow(
        Color.black, 
        getNet().getBackground(), 
        getNet().getGraphics()
    );
  }

  /**
   * Shows a potential flow relation. 
   * See {@link paintPotentialFlow(Color fg, Color bg, Graphics g)}.
   */

  private void showPotentialFlow() {
    paintPotentialFlow(
        getNet().getBackground(), 
        Color.black, 
        getNet().getGraphics()
    );
  }

  /**
   * Paints a 'potential' flow from a source port to the current point
   * being tracked by the marquee handler (the point under the mouse).
   * @param fg
   * @param bg
   * @param g
   */
  
  protected void paintPotentialFlow(Color fg, Color bg, Graphics g) {
    g.setColor(fg);
    g.setXORMode(bg);
    if (sourcePort != null && getCurrentPoint() != null) {
      g.drawLine(
          (int) getNet().toScreen(sourcePort.getLocation()).getX(), 
          (int) getNet().toScreen(sourcePort.getLocation()).getY(), 
          (int) getCurrentPoint().getX(), 
          (int) getCurrentPoint().getY()
      );
    }
  }
  
  /**
   * If there are valid source and target ports specified as a result
   * of a flow relation being drawn, this method will draw a flow connecting
   * these two ports, resulting in a flow relation between the parent elements
   * of the ports. If the flow woule be invalid, the flow being drawn will 
   * be ignored.
   */
  public void connectElementsOrIgnoreFlow() {
    if (targetPort != null && sourcePort != null &&
        connectionAllowable(sourcePort, targetPort) &&
        acceptsIncomingFlows(targetPort)) {
      getNet().connect(
        (YAWLPort) sourcePort.getCell(), 
        (YAWLPort) targetPort.getCell()
      );
    }
    hidePotentialFlow();

    hidePort(sourcePort);

    sourcePort = targetPort = null;

    setStartPoint(null); 
    
    setCurrentPoint(null);
  }
}
