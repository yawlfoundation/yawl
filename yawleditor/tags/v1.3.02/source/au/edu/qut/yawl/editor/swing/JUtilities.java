/*
 * Created on 05/10/2003
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


import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import java.util.List;

import java.awt.Component;

import au.edu.qut.yawl.editor.net.NetGraph;
import au.edu.qut.yawl.editor.elements.model.YAWLVertex;

import javax.swing.JComponent;

/**
 *  A non-instantiable class, colleting together a loosely related set of stateless swing utility methods.
 *  @author Lindsay Bradford
 */

public class JUtilities {
  
  private static final Point TOP_LEFT = new Point(0,0);

  /**
   *  Centres the supplied Window in the middle of the screen.  
	 *  @param window  the window to centre on-screen
	 */

	public static void centerWindow(Window window) {
    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    final Dimension windowSize = window.getSize();
    final int centerX = screenSize.width  / 2;
    final int centerY = screenSize.height / 2;
    window.setLocation(centerX - (windowSize.width  / 2),
                       centerY - (windowSize.height / 2));
  }
  
  /**
   * Makes the supplied list of Component objects the same size (width and height) as the largest 
   * component in the list.
   * @param components  the list of components that are to be resized.
   */

  public static void equalizeComponentSizes(List components) {
    Dimension  maxComponentSize;
  	maxComponentSize = getMaxDimension(components);
    for (int i = 0; i < components.size(); ++i) {
			resizeComponent((JComponent) components.get(i), maxComponentSize);
    }
  }

	private static void resizeComponent(JComponent currentComponent, 
	                                    Dimension maxComponentSize) {
		currentComponent.setPreferredSize(maxComponentSize);
		currentComponent.setMaximumSize(maxComponentSize);
		currentComponent.setMinimumSize(maxComponentSize);
	}

	private static Dimension getMaxDimension(List components) {
		Dimension maxComponentSize = new Dimension(0,0);
		JComponent currentComponent;
		Dimension currentComponentSize;
		for (int i = 0; i < components.size(); ++i) {
		  currentComponent   = (JComponent) components.get(i);
		  currentComponentSize   = currentComponent.getPreferredSize();
		  maxComponentSize.width  = Math.max(maxComponentSize.width,  (int)currentComponentSize.getWidth());
		  maxComponentSize.height = Math.max(maxComponentSize.height, (int)currentComponentSize.getHeight());
		}
		return maxComponentSize;
	} 
  
  public static void centreWindowUnderVertex(NetGraph graph, 
                                             Window window, 
                                             YAWLVertex vertex,
                                             int distance){
    Rectangle viewBounds = null;
    if (vertex.getParent() != null) {
      viewBounds = graph.getCellBounds(vertex.getParent()); 
    } else {
      viewBounds = graph.getCellBounds(vertex);
    }
 
    Point newLocation = graph.getLocationOnScreen();

    centreWindowUnderRectangle(window, viewBounds, newLocation, distance);
  }
  
  public static void centreWindowUnderRectangle(Window window,
                                                Rectangle rectangle,
                                                int distance) {
                                                  
    centreWindowUnderRectangle(window, rectangle, TOP_LEFT, distance);
  }
  
  public static void centreWindowUnderRectangle(Window window,
                                                Rectangle rectangle,
                                                Point offset,
                                                int distance) {

    offset.translate((rectangle.x + (rectangle.width/2) - window.getWidth()/2), 
                      rectangle.y + rectangle.height + distance);
    
    // pushing window back onto screen if it off the edge.
    
    final int screenWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    final int screenHeight = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();

    // We do the RHS first, JIC the window is wider than the screen.
    // The LHS side of the window will be visible at least in this case.
    
    if (offset.x + window.getWidth() > screenWidth) {
      offset.x = screenWidth - window.getWidth() - 5;
    }
    
    if (offset.x < 0) {
      offset.translate((offset.x * -1) + 5,0);
    }

    // Same again for the vertical.

    if (offset.y + window.getHeight() > screenHeight) {
      offset.y = screenHeight - window.getHeight() - 5;
    }
    
    if (offset.y < 0) {
      offset.translate(0,(offset.y * -1) + 5);
    }
    
    window.setLocation(offset);
  }
  
  public static void setMinSizeToCurrent(final Component component) {

    final Dimension minimumSize = component.getSize();

    component.addComponentListener(
      new ComponentAdapter() {
        public void componentResized(ComponentEvent ce) {
          component.setSize(
            Math.max(minimumSize.width,  component.getSize().width ), 
            Math.max(minimumSize.height, component.getSize().height )
          );
        }
      }
    );
  }
  
}
