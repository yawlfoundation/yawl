/*
 * Created on 18/10/2003
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.SwingUtilities;

import org.jgraph.graph.BasicMarqueeHandler;
import org.jgraph.graph.CellView;

import org.jgraph.graph.Port;
import org.jgraph.graph.PortView;

import au.edu.qut.yawl.editor.elements.model.YAWLPort;
import au.edu.qut.yawl.editor.elements.model.YAWLCell;

import au.edu.qut.yawl.editor.swing.CursorFactory;
import au.edu.qut.yawl.editor.swing.menu.Palette;

public class NetMarqueeHandler extends BasicMarqueeHandler {
  private NetGraph graph;
  private PortView startPort, currentPort;
  
  private static final int PORT_BUFFER = 2;
  
  public NetMarqueeHandler(NetGraph graph) {
    this.graph = graph;
    startPort = null;
    currentPort = null;
  }


  public boolean isForceMarqueeEvent(MouseEvent event) {
    return (Palette.getInstance().getSelected() != Palette.MARQUEE
            || super.isForceMarqueeEvent(event));  
  }
  
  public void mouseMoved(MouseEvent event) {
    switch (Palette.getInstance().getSelected()) {
      case Palette.FLOW_RELATION: {
        if (startPort == null) {
          graph.setCursor(CursorFactory.getCustomCursor(CursorFactory.FLOW_RELATION));
          PortView newPort = getSourcePortAt(event.getPoint());
          if (newPort == null) {
            hidePort(currentPort);
            currentPort = null;  
          }
          if (newPort != null && generatesOutgoingFlows(newPort)) {
            if(newPort != currentPort) {
              hidePort(currentPort);
              currentPort = newPort;
              showPort(currentPort); 
            }
          }
          event.consume();
        }
        break;
      }
      case Palette.MARQUEE: {
        graph.setCursor(CursorFactory.getCustomCursor(CursorFactory.SELECTION));
        event.consume();
        break;
      }
      case Palette.DRAG: {
        graph.setCursor(CursorFactory.getCustomCursor(CursorFactory.DRAG));
        event.consume();
        break;
      }
      case Palette.CONDITION: {
        graph.setCursor(CursorFactory.getCustomCursor(CursorFactory.CONDITION));
        event.consume();
        break;
      }
      case Palette.ATOMIC_TASK: {
        graph.setCursor(CursorFactory.getCustomCursor(CursorFactory.ATOMIC_TASK));
        event.consume();
        break;
      }
      case Palette.COMPOSITE_TASK: {
        graph.setCursor(CursorFactory.getCustomCursor(CursorFactory.COMPOSITE_TASK));
        event.consume();
        break;
      }
      case Palette.MULTIPLE_ATOMIC_TASK: {
        graph.setCursor(CursorFactory.getCustomCursor(CursorFactory.MULTIPLE_ATOMIC_TASK));
        event.consume();
        break;
      }
      case Palette.MULTIPLE_COMPOSITE_TASK: {
        graph.setCursor(CursorFactory.getCustomCursor(CursorFactory.MULTIPLE_COMPOSITE_TASK));
        event.consume();
        break;
      }
      default: {
        super.mouseMoved(event);
        event.consume();        
      } 
    }   
  }

  public void mousePressed(final MouseEvent event) {

    if(SwingUtilities.isRightMouseButton(event)) {
      super.mousePressed(event);
      return;
    }

    switch (Palette.getInstance().getSelected()) {
      case Palette.FLOW_RELATION: {
        if (currentPort != null && generatesOutgoingFlows(currentPort)) {
          setStartPoint(getNearestSnapPoint(graph.toScreen(currentPort.getLocation())));
          startPort = currentPort;
        }
        event.consume();
        break;
      }
      case Palette.CONDITION: {
        graph.addCondition(getNearestSnapPoint(event.getPoint()));
        break;        
      }
      case Palette.ATOMIC_TASK: {
        graph.addAtomicTask(getNearestSnapPoint(event.getPoint()));
        break;        
      }
      case Palette.COMPOSITE_TASK: {
        graph.addCompositeTask(getNearestSnapPoint(event.getPoint()));
        break;        
      }
      case Palette.MULTIPLE_ATOMIC_TASK: {
        graph.addMultipleAtomicTask(getNearestSnapPoint(event.getPoint()));
        break;        
      }
      case Palette.MULTIPLE_COMPOSITE_TASK: {
        graph.addMultipleCompositeTask(getNearestSnapPoint(event.getPoint()));
        break;        
      }
      case Palette.DRAG: {
        setStartPoint(graph.toScreen(event.getPoint()));
        event.consume();
        break;
      }
      default: {
        super.mousePressed(event);
        break;
      }
    }
  }
  
  private Point2D getNearestSnapPoint(Point2D point) {
    return graph.snap(graph.fromScreen(point));
  }

  public void mouseDragged(MouseEvent event) {
    switch (Palette.getInstance().getSelected()) {
      case Palette.FLOW_RELATION: {
        if (!event.isConsumed() && startPort != null) {
          hideConnector();
          setCurrentPoint(graph.snap(event.getPoint()));
          showConnector();
          PortView thisPort = getSourcePortAt(event.getPoint());
          if (thisPort != null && thisPort != startPort &&
              connectionAllowable(startPort, thisPort) &&
              acceptsIncommingFlows(thisPort)) {
            hidePort(currentPort);         
            currentPort = thisPort;    
            showPort(currentPort);
          }
          if (thisPort == null) {
            hidePort(currentPort);
            currentPort = null; 
          }
          event.consume();
        }
        break;
      }
      case Palette.DRAG: {
        if (!event.isConsumed() && startPoint != null) {
          
          Point2D delta = new Point2D.Double(
              startPoint.getX() - graph.toScreen(event.getPoint()).getX(),
              startPoint.getY() - graph.toScreen(event.getPoint()).getY()
          );
          
          graph.scrollRectToVisible(
              new Rectangle(
                  (int) (graph.getVisibleRect().getX() + delta.getX()),
                  (int) (graph.getVisibleRect().getY() + delta.getY()),
                  (int) graph.getVisibleRect().getWidth(),
                  (int) graph.getVisibleRect().getHeight()
              )
          );
        }
        event.consume();
        break;
      }
      default: {
        super.mouseDragged(event);
        break;
      }
    }
  }

  public void mouseReleased(MouseEvent e) {
    switch (Palette.getInstance().getSelected()) {
      case Palette.FLOW_RELATION: {
        connectElementsOrIgnoreFlow();
        e.consume();
        break;
      }
      case Palette.MARQUEE: {
        super.mouseReleased(e);
        break;
      }
      case Palette.DRAG: {
        setStartPoint(null);
        e.consume();
        break;
      }
      default: {
        e.consume();
        break;
      }
    }
  }
  
  public void connectElementsOrIgnoreFlow() {
    if (currentPort != null && startPort != null &&
        connectionAllowable(startPort, currentPort) &&
        acceptsIncommingFlows(currentPort)) {
      graph.connect(
        (YAWLPort) startPort.getCell(), 
        (YAWLPort) currentPort.getCell()
      );
    }
    hideConnector();
    startPort = currentPort = null;
    setStartPoint(null); 
    setCurrentPoint(null);
  }

  public PortView getSourcePortAt(Point point) {
    final Point2D tmp = graph.fromScreen(point);
    return graph.getPortViewAt(tmp.getX(), tmp.getY());
  }
  
  private void hideConnector() {
    paintConnector(Color.black, graph.getBackground(), graph.getGraphics());
  }

  private void showConnector() {
    paintConnector(graph.getBackground(), Color.black, graph.getGraphics());
  }

  protected void paintConnector(Color fg, Color bg, Graphics g) {
    g.setColor(fg);
    g.setXORMode(bg);
    if (startPort != null && getStartPoint() != null && getCurrentPoint() != null)
      g.drawLine(
          (int) getStartPoint().getX(), 
          (int) getStartPoint().getY(), 
          (int) getCurrentPoint().getX(), 
          (int) getCurrentPoint().getY()
      );
  }
 
  protected void hidePort(PortView thisPort) {
    final Graphics g = graph.getGraphics();
    g.setColor(graph.getBackground());
    g.setXORMode(graph.getMarqueeColor());
    showPort(thisPort);
  }
  
  protected void showPort(PortView thisPort) {
    if (thisPort != null) {
      Rectangle2D portBounds = thisPort.getBounds();
      
      Rectangle2D.Double portViewbox = new Rectangle2D.Double(
        portBounds.getX() - PORT_BUFFER/2, 
        portBounds.getY() - PORT_BUFFER/2,
        portBounds.getWidth() + (2 * PORT_BUFFER), 
        portBounds.getHeight() + (2 * PORT_BUFFER)        
      );
      
      graph.getUI().paintCell(graph.getGraphics(), 
                              thisPort, 
                              portViewbox, true);
    }
  }
  
  public PortView getPortViewAt(int x, int y) {
    PortView port = graph.getPortViewAt(x, y);
    return port;
  }
  
  private boolean acceptsIncommingFlows(PortView portView) {
    CellView parentView = portView.getParentView();
    YAWLPort yawlPort  = (YAWLPort) portView.getCell();
    YAWLCell vertex = (YAWLCell) parentView.getCell();  
    return yawlPort.acceptsIncomingFlows() && 
           vertex.acceptsIncommingFlows()   && 
           graph.acceptsIncommingFlows(vertex);
  }

  private boolean generatesOutgoingFlows(PortView portView) {
    CellView parentView = portView.getParentView();
    YAWLPort yawlPort  = (YAWLPort) portView.getCell();
    YAWLCell vertex = (YAWLCell) parentView.getCell();  
    return yawlPort.generatesOutgoingFlows() && 
           vertex.generatesOutgoingFlows()   && 
           graph.generatesOutgoingFlows(vertex);
  }

  private boolean connectionAllowable(PortView source, PortView target) {
    return graph.connectionAllowable((Port) source.getCell(), (Port) target.getCell());
  }
}