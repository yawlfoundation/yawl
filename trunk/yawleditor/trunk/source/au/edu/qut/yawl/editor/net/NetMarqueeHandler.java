package au.edu.qut.yawl.editor.net;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jgraph.graph.BasicMarqueeHandler;
import org.jgraph.graph.CellView;
import org.jgraph.graph.Port;
import org.jgraph.graph.PortView;

import au.edu.qut.yawl.editor.elements.model.VertexContainer;
import au.edu.qut.yawl.editor.elements.model.YAWLCell;
import au.edu.qut.yawl.editor.elements.model.YAWLFlowRelation;
import au.edu.qut.yawl.editor.elements.model.YAWLPort;
import au.edu.qut.yawl.editor.elements.model.YAWLVertex;
import au.edu.qut.yawl.editor.swing.CursorFactory;
import au.edu.qut.yawl.editor.swing.menu.Palette;

public class NetMarqueeHandler extends BasicMarqueeHandler {
  private NetGraph net;
  
  private static final int PORT_BUFFER = 2;
  
  private PortView sourcePort, targetPort = null;
  
  private enum State {
    OVER_NOTHING,
    OVER_VERTEX,
    OVER_FLOW,
    OVER_OUTGOING_PORT,
    DRAGGING_VERTEX,
    DRAWING_FLOW
  }
  
  private State state = State.OVER_NOTHING;
  
  public NetMarqueeHandler(NetGraph net) {
    this.net = net;
  }
  
  public boolean isForceMarqueeEvent(MouseEvent event) {
    if (Palette.getInstance().getSelected() == Palette.MARQUEE) {
      return false;
    }
    if (Palette.getInstance().getSelected() != Palette.MARQUEE && state == State.OVER_VERTEX) {
      return false;
    }
    if (Palette.getInstance().getSelected() != Palette.MARQUEE && state == State.OVER_FLOW) {
      return false;
    }
    return true;
  }

  public NetGraph getNet() {
    return this.net;
  }
  
  public void mouseMoved(MouseEvent event) {
    if (Palette.getInstance().getSelected() == Palette.MARQUEE) {
      super.mouseMoved(event);
      return;
    }
    
     State oldState = state;

     setCursorFromPalette();

     determineStateFromPoint(event.getPoint());
     
     doStateTransitionProcessing(event.getPoint(), oldState, state);   
     
     event.consume();
  }
  
  private void setCursorFromPalette() {
    switch (Palette.getInstance().getSelected()) {
      case Palette.FLOW_RELATION: {
        matchCursorToPalette(CursorFactory.FLOW_RELATION);
        break;
      }
      case Palette.MARQUEE: {
        matchCursorToPalette(CursorFactory.SELECTION);
        break;
      }
      case Palette.DRAG: {
        matchCursorToPalette(CursorFactory.DRAG);
        break;
      }
      case Palette.CONDITION: {
        matchCursorToPalette(CursorFactory.CONDITION);
        break;
      }
      case Palette.ATOMIC_TASK: {
        matchCursorToPalette(CursorFactory.ATOMIC_TASK);
        break;
      }
      case Palette.COMPOSITE_TASK: {
        matchCursorToPalette(CursorFactory.COMPOSITE_TASK);
        break;
      }
      case Palette.MULTIPLE_ATOMIC_TASK: {
        matchCursorToPalette(CursorFactory.MULTIPLE_ATOMIC_TASK);
        break;
      }
      case Palette.MULTIPLE_COMPOSITE_TASK: {
        matchCursorToPalette(CursorFactory.MULTIPLE_COMPOSITE_TASK);
        break;
      }
    }
  }
  
  private void matchCursorToPalette(int cursorType) {
    getNet().setCursor(CursorFactory.getCustomCursor(cursorType));
  }
  
  private void determineStateFromPoint(Point point) {
    if (isPointOverOutgoingPort(point)) {
      state = State.OVER_OUTGOING_PORT;
    } else if (isPointOverVertex(point)) {
      state = State.OVER_VERTEX;
    } else if (isPointOverFlow(point)){
      state = State.OVER_FLOW;
    } else {
      state = State.OVER_NOTHING;
    }
  }
  
  public void mousePressed(MouseEvent event) {
    if (Palette.getInstance().getSelected() == Palette.MARQUEE) {
      super.mousePressed(event);
      return;
    }
    
    State oldState = state;
    
    switch(oldState) {
      case OVER_VERTEX: {
        state = State.DRAGGING_VERTEX;
        break;
      }
      case OVER_OUTGOING_PORT: {
        state = State.DRAWING_FLOW;
        break;
      }
      case OVER_NOTHING: {
        switch (Palette.getInstance().getSelected()) {
          case Palette.CONDITION: {
            getNet().addCondition(getNearestSnapPoint(event.getPoint()));
            state = State.OVER_VERTEX;
            break;        
          }
          case Palette.ATOMIC_TASK: {
            getNet().addAtomicTask(getNearestSnapPoint(event.getPoint()));
            state = State.OVER_VERTEX;
            break;        
          }
          case Palette.COMPOSITE_TASK: {
            getNet().addCompositeTask(getNearestSnapPoint(event.getPoint()));
            state = State.OVER_VERTEX;
            break;        
          }
          case Palette.MULTIPLE_ATOMIC_TASK: {
            getNet().addMultipleAtomicTask(getNearestSnapPoint(event.getPoint()));
            state = State.OVER_VERTEX;
            break;        
          }
          case Palette.MULTIPLE_COMPOSITE_TASK: {
            getNet().addMultipleCompositeTask(getNearestSnapPoint(event.getPoint()));
            state = State.OVER_VERTEX;
            break;        
          }
        }  // switch Palette.getInstance().getSelected();
        break;
      }
    }
    doStateTransitionProcessing(event.getPoint(), oldState, state);
  }

  public void mouseDragged(MouseEvent event) {
    if (Palette.getInstance().getSelected() == Palette.MARQUEE) {
      super.mouseDragged(event);
      return;
    }
    
    switch(state) {
      case DRAWING_FLOW: {
        if (sourcePort != null) {
          hideConnector();
          setCurrentPoint(getNet().snap(event.getPoint()));
          showConnector();
          PortView portUnderMouse = getPortViewAt(event.getPoint());
          if (portUnderMouse != null && portUnderMouse != sourcePort &&
              connectionAllowable(sourcePort, portUnderMouse) &&
              acceptsIncommingFlows(portUnderMouse)) {
            hidePort(targetPort);         
            targetPort = portUnderMouse;    
            showPort(portUnderMouse);
          }
          if (portUnderMouse == null) {
            hidePort(targetPort);
            targetPort = null; 
          }
        }
        break;
      }
    }
  }
  
  public void mouseReleased(MouseEvent event) {
    if (Palette.getInstance().getSelected() == Palette.MARQUEE) {
      super.mouseReleased(event);
      return;
    }
    
    State oldState = state;
    switch(oldState) {
      case DRAWING_FLOW: {
        connectElementsOrIgnoreFlow();
        break;
      }
    }
    event.consume();
  }

  private boolean isPointOverFlow(Point point) {
    if (getElementAt(point) == null) {
      return false;
    }
    if (getElementAt(point) instanceof YAWLFlowRelation) {
      return true;
    }
    return false;
  }

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
  
  private boolean isPointOverOutgoingPort(Point point) {
    PortView portUnderMouse = getPortViewAt(point);
    if (portUnderMouse == null) {
      return false;
    }
    if (generatesOutgoingFlows(portUnderMouse)) {
      return true;
    }
    return false;
  }
  
  private void doStateTransitionProcessing(Point point, State oldState, State newState) {
    switch(oldState) {
      case OVER_NOTHING: {
        switch(newState) {
          case OVER_VERTEX: {
          
             break;
          }
          case OVER_OUTGOING_PORT:  {
            doMouseMovedOverPortProcessing(
              getPortViewAt(point)                  
            );
            break;
          }
        }  
        break;
      }  // case OVER_NOTHING
      case OVER_OUTGOING_PORT: {
        switch(newState) {
          case OVER_OUTGOING_PORT: {
            if (getPortViewAt(point) != sourcePort) {
              doMouseMovedOverPortProcessing(
                  getPortViewAt(point)                  
              );
            }
            break;
          }
          case DRAWING_FLOW: {
            break;
          }
          default: {
            hidePort(sourcePort);
            sourcePort = null;
            break;
          }
        }
        break;
      }  // case OVER_OUTGOING_PORT
    }
  }
  
  private void doMouseMovedOverPortProcessing(PortView portView) {
    if (sourcePort == null || sourcePort != portView) {
      hidePort(sourcePort);
    }      
    sourcePort = portView;
    showPort(sourcePort);
  }

  private PortView getPortViewAt(Point point) {
    return getNet().getPortViewAt(point.getX(), point.getY());
  }
  
  private Object getElementAt(Point point) {
    return getNet().getFirstCellForLocation(point.getX(), point.getY());
  }
  
  protected void hidePort(PortView thisPort) {
    if (thisPort == null) {
      return;
    }
    
    final Graphics graphics = getNet().getGraphics();
    
    graphics.setColor(getNet().getBackground());
    graphics.setXORMode(getNet().getMarqueeColor());
    
    showPort(thisPort);
  }
  
  protected void showPort(PortView thisPort) {
    if (thisPort != null) {
      Rectangle2D portBounds = getNet().toScreen(thisPort.getBounds());
      
      Rectangle2D.Double portViewbox = new Rectangle2D.Double(
        portBounds.getX() - PORT_BUFFER/2, 
        portBounds.getY() - PORT_BUFFER/2,
        portBounds.getWidth() + (2 * PORT_BUFFER), 
        portBounds.getHeight() + (2 * PORT_BUFFER)        
      );
      
      getNet().getUI().paintCell(
        getNet().getGraphics(),
        thisPort, 
        portViewbox, 
        true
      );
    }
  }
  
  private boolean generatesOutgoingFlows(PortView portView) {
    CellView parentView = portView.getParentView();
    YAWLPort yawlPort  = (YAWLPort) portView.getCell();
    YAWLCell vertex = (YAWLCell) parentView.getCell();  
    return yawlPort.generatesOutgoingFlows() && 
           vertex.generatesOutgoingFlows()   && 
           getNet().generatesOutgoingFlows(vertex);
  }
  
  private boolean acceptsIncommingFlows(PortView portView) {
    CellView parentView = portView.getParentView();
    YAWLPort yawlPort  = (YAWLPort) portView.getCell();
    YAWLCell vertex = (YAWLCell) parentView.getCell();  
    return yawlPort.acceptsIncomingFlows() && 
           vertex.acceptsIncommingFlows()   && 
           getNet().acceptsIncommingFlows(vertex);
  }

  
  private Point2D getNearestSnapPoint(Point2D point) {
    return getNet().snap(getNet().fromScreen(point));
  }
  
  private boolean connectionAllowable(PortView source, PortView target) {
    return getNet().connectionAllowable((Port) source.getCell(), (Port) target.getCell());
  }
  
  private void hideConnector() {
    paintConnector(
        Color.black, 
        getNet().getBackground(), 
        getNet().getGraphics()
    );
  }

  private void showConnector() {
    paintConnector(
        getNet().getBackground(), 
        Color.black, 
        getNet().getGraphics()
    );
  }

  protected void paintConnector(Color fg, Color bg, Graphics g) {
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
  
  public void connectElementsOrIgnoreFlow() {
    if (targetPort != null && sourcePort != null &&
        connectionAllowable(sourcePort, targetPort) &&
        acceptsIncommingFlows(targetPort)) {
      getNet().connect(
        (YAWLPort) sourcePort.getCell(), 
        (YAWLPort) targetPort.getCell()
      );
    }
    hideConnector();
    sourcePort = targetPort = null;
    setStartPoint(null); 
    setCurrentPoint(null);
  }
}
