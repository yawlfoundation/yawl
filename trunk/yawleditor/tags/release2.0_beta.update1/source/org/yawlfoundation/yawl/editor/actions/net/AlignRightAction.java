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

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.jgraph.event.GraphSelectionEvent;

import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.net.utilities.NetCellUtilities;
import org.yawlfoundation.yawl.editor.specification.SpecificationSelectionListener;
import org.yawlfoundation.yawl.editor.specification.SpecificationSelectionSubscriber;
import org.yawlfoundation.yawl.editor.swing.TooltipTogglingWidget;

public class AlignRightAction extends YAWLSelectedNetAction implements TooltipTogglingWidget, SpecificationSelectionSubscriber {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final AlignRightAction INSTANCE = new AlignRightAction();
  
  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Align along Right Edges");
    putValue(Action.LONG_DESCRIPTION, "Align the selected elements along their right edges.");
    putValue(Action.SMALL_ICON, getIconByName("AlignRight"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_R));
  }
  
  private AlignRightAction() {
    SpecificationSelectionListener.getInstance().subscribe(
        this,
        new int[] { 
          SpecificationSelectionListener.STATE_NO_ELEMENTS_SELECTED,
          SpecificationSelectionListener.STATE_ONE_OR_MORE_ELEMENTS_SELECTED,
          SpecificationSelectionListener.STATE_MORE_THAN_ONE_VERTEX_SELECTED
        }
    );
  };  
  
  public static AlignRightAction getInstance() {
    return INSTANCE; 
  }

  public void actionPerformed(ActionEvent event) {
    final NetGraph graph = getGraph();
    if (graph != null) {
      NetCellUtilities.alignCellsAlongRight(graph, graph.getSelectionCells());
    }
  }
  
  public String getEnabledTooltipText() {
    return " Align the selected elements along their right edges ";
  }
  
  public String getDisabledTooltipText() {
    return " You must have a number of net elements selected" + 
           " to align them ";
  }
  
  public void receiveGraphSelectionNotification(int state,GraphSelectionEvent event) {
    switch(state) {
      case SpecificationSelectionListener.STATE_MORE_THAN_ONE_VERTEX_SELECTED: {
        setEnabled(true);
        break;
      }
      default: {
        setEnabled(false);
        break;
      }
    }
  }
}
