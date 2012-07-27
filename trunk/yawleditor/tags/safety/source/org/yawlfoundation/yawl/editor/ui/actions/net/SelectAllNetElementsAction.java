/*
 * Created on 19/05/2005
 * YAWLEditor v1.1-2 
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

package org.yawlfoundation.yawl.editor.ui.actions.net;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.yawlfoundation.yawl.editor.ui.net.NetGraph;

public class SelectAllNetElementsAction extends YAWLSelectedNetAction {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final SelectAllNetElementsAction INSTANCE 
    = new SelectAllNetElementsAction();
  {
    putValue(Action.SHORT_DESCRIPTION, " Select all net elements ");
    putValue(Action.NAME, "Select all");
    putValue(Action.LONG_DESCRIPTION, "Select all net elements.");
  }
  
  private SelectAllNetElementsAction() {};  
  
  public static SelectAllNetElementsAction getInstance() {
    return INSTANCE; 
  }

  public void actionPerformed(ActionEvent event) {
    final NetGraph graph = getGraph();
    if (graph != null) {
      graph.setSelectionCells(graph.getRoots());
    }
  }
}
