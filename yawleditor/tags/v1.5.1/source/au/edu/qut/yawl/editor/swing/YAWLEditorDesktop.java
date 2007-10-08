/*
 * Created on 10/10/2003
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

package au.edu.qut.yawl.editor.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.LinkedList;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import au.edu.qut.yawl.editor.net.*;
import au.edu.qut.yawl.editor.specification.SpecificationModel;
import au.edu.qut.yawl.editor.swing.net.YAWLEditorNetFrame;

public class YAWLEditorDesktop extends JDesktopPane 
                               implements InternalFrameListener {
                                 
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final SpecificationModel model = SpecificationModel.getInstance();

  public final static int X_OFFSET = 20;
  public final static int Y_OFFSET = 20;
  
  private static final YAWLEditorDesktop INSTANCE = new YAWLEditorDesktop();
  
  private static JScrollPane scrollPane;

   private YAWLEditorDesktop() {
     super();
     setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);    
     setBackground(Color.WHITE);
   }

   public static YAWLEditorDesktop getInstance( ) {
     return INSTANCE;
   }  
   
   public void setScrollPane(JScrollPane inputScrollPane) {
    scrollPane = inputScrollPane;
   }

  public YAWLEditorNetFrame newNet() {
    YAWLEditorNetFrame frame = new YAWLEditorNetFrame(getNewLocation());
    bindFrame(frame);
    return frame;
  }
  
  public void openNet(NetGraph graph) {
    openNet(
       new Rectangle(10,10,200,200),
       false,
       null,
       false,
       graph
    );
  }
  
  public void openNet(Rectangle bounds, 
                      boolean iconified, 
                      Rectangle iconBounds,
                      boolean maximised,
                      NetGraph graph) {
    YAWLEditorNetFrame frame = new YAWLEditorNetFrame(bounds, graph.getName());
    bindFrame(frame);
    graph.setSize(frame.getContentPane().getSize());
    frame.setNet(graph);
    try {
      frame.setIcon(iconified);
      if (iconified) {
				frame.getDesktopIcon().setBounds(iconBounds);
      }
      frame.setMaximum(maximised);
    } catch (Exception e) {};
  }
  
  private void bindFrame(final YAWLEditorNetFrame frame) {
    frame.addInternalFrameListener(this);
    add(frame);
    frame.setVisible(true); //necessary as of 1.3
    try {
      frame.setSelected(true);
    } catch (java.beans.PropertyVetoException e) {}
    
    frame.addComponentListener(
      new ComponentAdapter() {
        public void componentMoved(ComponentEvent event) {
          frame.setLocation(Math.max(0,frame.getX()),Math.max(0, frame.getY()));
          resizeIfNecessary();
          repositionViewportIfNecessary(frame);
        }
          
        public void componentResized(ComponentEvent event) {
          if (!frame.isMaximum()) {
            resizeIfNecessary();
          }
        }
    });
  }
  
  public void removeActiveNet() {
    YAWLEditorNetFrame f = (YAWLEditorNetFrame) getSelectedFrame();
    if (f != null && !f.getNet().getNetModel().isStartingNet()) {
      f.doDefaultCloseAction();
    }
  }
  
  public void closeAllNets() {
    JInternalFrame[] frames = getAllFrames();
    for(int i = 0; i < frames.length; i++) {
      ((YAWLEditorNetFrame)frames[i]).dispose();
    }
    setPreferredSize(new Dimension(0,0));
  }
  
  public void iconifyAllNets() {
    JInternalFrame[] frames = getAllFrames();
    for(int i = 0; i < frames.length; i++) {
      try {
        frames[i].setIcon(true);
      } catch (Exception e) {}
    }
  }
  
  public void showAllNets() {
    JInternalFrame[] frames = getAllFrames();
    for(int i = frames.length - 1; i >= 0; i--){
      try {
        frames[i].setIcon(false);
      } catch (Exception e) {}
    }
  }
 
  public void internalFrameActivated(InternalFrameEvent e) {
    updateState();   
  }

  public void internalFrameDeactivated(InternalFrameEvent e) {
    updateState();   
  }

  public void internalFrameClosing(InternalFrameEvent e) {}

  public void internalFrameClosed(InternalFrameEvent e) {
    resizeIfNecessary();   
    updateState();   
  }
  
  public void internalFrameDeiconified(InternalFrameEvent e) {
    resizeIfNecessary();
    repositionViewportIfNecessary(e.getInternalFrame());
    updateState();   
  }

  public void internalFrameIconified(InternalFrameEvent e) {
    resizeIfNecessary();
    repositionViewportIfNecessary(e.getInternalFrame());
    updateState();   
  }

  public void internalFrameOpened(InternalFrameEvent e) {
    resizeIfNecessary();
    repositionViewportIfNecessary(e.getInternalFrame());
    updateState();   
  }
  
  private void repositionViewportIfNecessary(JInternalFrame frame) {
    
    // TODO: reposition to work on iconified frames as well.

    if (!this.isVisible() || scrollPane == null) {
  		return;
  	}
    final JViewport viewport  = scrollPane.getViewport();
    final Rectangle trimmedViewport = removeOffset(viewport.getViewRect());
    if (!trimmedViewport.contains(frame.getBounds())) {
      viewport.setVisible(false);  // for faster redraw
      viewport.scrollRectToVisible(addOffset(frame.getBounds()));
      if (!trimmedViewport.contains(frame.getBounds())) {  // must be too big...
        viewport.setViewPosition(new Point(Math.max(0,frame.getX() - X_OFFSET), 
                                           Math.max(0,frame.getY() - Y_OFFSET)));
      }
      viewport.setVisible(true);
    }
  }
  
  private Rectangle removeOffset(Rectangle rectangle) {
    return new Rectangle((int) rectangle.getX() + X_OFFSET,
                         (int) rectangle.getY() + Y_OFFSET,
                         (int) rectangle.getWidth()  - (2 *  X_OFFSET),
                         (int) rectangle.getHeight() - (2 * Y_OFFSET));
  }

  private Rectangle addOffset(Rectangle rectangle) {
    return new Rectangle((int) rectangle.getX() - X_OFFSET,
                         (int) rectangle.getY() - Y_OFFSET,
                         (int) rectangle.getWidth()  + (2 *  X_OFFSET),
                         (int) rectangle.getHeight() + (2 * Y_OFFSET));
  }
  
  private void updateState() {
    JInternalFrame frame = getSelectedFrame();
    if (frame == null) {
      model.nothingSelected();
      return;
    }
    model.somethingSelected();    
    try {
      getSelectedGraph().getSelectionListener().forceActionUpdate();
      getSelectedGraph().getCancellationSetModel().refresh();
    } catch (Exception e) {}
  }
  
  private void resizeIfNecessary() {
    if (!this.isVisible()) {
      return;
    }

    int maxX = 0;
    int maxY = 0;

    JInternalFrame currentFrame = null;
    JInternalFrame[] frames = getAllFrames();

    for (int i=0; i < frames.length; i++) {
      currentFrame = frames[i];
      
      Rectangle currentFrameBounds = currentFrame.getNormalBounds();
      
      if ((currentFrameBounds.getX() + currentFrameBounds.getWidth()) > maxX) {
        maxX = (int) (currentFrameBounds.getX() + currentFrameBounds.getWidth());
      }
      if ((currentFrameBounds.getY() + currentFrameBounds.getHeight()) > maxY) {
        maxY = (int) (currentFrameBounds.getY() + currentFrameBounds.getHeight());
      }
    }
    maxX += X_OFFSET;
    maxY += Y_OFFSET;
    
    setPreferredSize(new Dimension(maxX,maxY));
   }
  
  private Point getNewLocation() {
    Point point = new Point();
    YAWLEditorNetFrame frame = (YAWLEditorNetFrame) getSelectedFrame();
    if (frame == null) {
      point.x = X_OFFSET;
      point.y = Y_OFFSET;
    } else {
      point.x = frame.getX() + X_OFFSET;
      point.y = frame.getY() + Y_OFFSET;      
    }
    return point;  
  }
  
  public NetGraph getSelectedGraph() {
    final YAWLEditorNetFrame frame = (YAWLEditorNetFrame) getSelectedFrame();
    if (frame != null) {
      return frame.getNet();  
    } 
    return null; 
  }
}