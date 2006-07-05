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

package au.edu.qut.yawl.editor.actions.net;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import au.edu.qut.yawl.editor.net.NetGraph;
import au.edu.qut.yawl.editor.swing.TooltipTogglingWidget;

import au.edu.qut.yawl.editor.net.utilities.NetCellUtilities;

public class AlignCentreAction extends YAWLSelectedNetAction implements TooltipTogglingWidget {

  private static final AlignCentreAction INSTANCE = new AlignCentreAction();
  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Align along Vertical Centre");
    putValue(Action.LONG_DESCRIPTION, "Vertically align the selected elements along their centre.");
    putValue(Action.SMALL_ICON, getIconByName("AlignCentre"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_V));
  }
  
  private AlignCentreAction() {}
  
  public static AlignCentreAction getInstance() {
    return INSTANCE; 
  }

  public void actionPerformed(ActionEvent event) {
    final NetGraph graph = getGraph();
    if (graph != null) {
      NetCellUtilities.alignCellsAlongVerticalCentre(graph, graph.getSelectionCells());
    }
  }
  
  public String getEnabledTooltipText() {
    return " Vertically align the selected elements along their centre ";
  }
  
  public String getDisabledTooltipText() {
    return " You must have a number of net elements selected" + 
           " to align them ";
  }
}
