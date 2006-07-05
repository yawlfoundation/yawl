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
import au.edu.qut.yawl.editor.net.utilities.NetCellUtilities;
import au.edu.qut.yawl.editor.swing.TooltipTogglingWidget;

public class AlignTopAction extends YAWLSelectedNetAction implements TooltipTogglingWidget {

  private static final AlignTopAction INSTANCE = new AlignTopAction();
    
  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Align along Top Edges");
    putValue(Action.LONG_DESCRIPTION, "Align the selected elements their top edges.");
    putValue(Action.SMALL_ICON, getIconByName("AlignTop"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_T));
  }
  
  private AlignTopAction() {};  
  
  public static AlignTopAction getInstance() {
    return INSTANCE; 
  }

  public void actionPerformed(ActionEvent event) {
    final NetGraph graph = getGraph();
    if (graph != null) {
      NetCellUtilities.alignCellsAlongTop(graph, graph.getSelectionCells());
    }
  }
  
  public String getEnabledTooltipText() {
    return " Align the selected elements along their top edges ";
  }
  
  public String getDisabledTooltipText() {
    return " You must have a number of net elements selected" + 
           " to align them ";
  }
}