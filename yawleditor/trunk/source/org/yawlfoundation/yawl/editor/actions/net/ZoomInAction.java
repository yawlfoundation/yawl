/*
 * Created on 09/10/2003
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

package org.yawlfoundation.yawl.editor.actions.net;

import org.yawlfoundation.yawl.editor.net.NetGraph;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ZoomInAction extends YAWLSelectedNetAction {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final ZoomInAction INSTANCE = new ZoomInAction();
  {
    putValue(Action.SHORT_DESCRIPTION, " Zoom in ");
    putValue(Action.NAME, "Zoom In");
    putValue(Action.LONG_DESCRIPTION, "Zoom in on this net.");
    putValue(Action.SMALL_ICON, getPNGIcon("magnifier_zoom_in"));
  }
  
  private ZoomInAction() {};  
  
  public static ZoomInAction getInstance() {
    return INSTANCE; 
  }

  public void actionPerformed(ActionEvent event) {
    final NetGraph graph = getGraph();
    if (graph != null) {
     graph.setScale(graph.getScale()*1.5);
    }
  }
}
