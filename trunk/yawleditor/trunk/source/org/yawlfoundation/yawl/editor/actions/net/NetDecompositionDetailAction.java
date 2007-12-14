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

import java.util.HashMap;

import au.edu.qut.yawl.editor.YAWLEditor;
import au.edu.qut.yawl.editor.actions.net.YAWLSelectedNetAction;
import au.edu.qut.yawl.editor.swing.data.NetDecompositionUpdateDialog;

import au.edu.qut.yawl.editor.net.NetGraph;

import java.awt.event.ActionEvent;

import javax.swing.Action;

public class NetDecompositionDetailAction extends YAWLSelectedNetAction {

   /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final HashMap netDialogs = new HashMap();
  
   {
    putValue(Action.SHORT_DESCRIPTION, " Manage decomposition detail of this Net  ");
    putValue(Action.NAME, "Update Net Detail...");
    putValue(Action.LONG_DESCRIPTION, "Manage the decomposition of this net.");
    putValue(Action.SMALL_ICON, getIconByName("DecompositionDetail"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_D));
  }
  
  public void actionPerformed(ActionEvent event) {
    NetDecompositionUpdateDialog dialog;
    if (!invokedAtLeastOnce(getGraph())) {
      dialog = new NetDecompositionUpdateDialog(
          getGraph().getNetModel().getDecomposition()
      );

      dialog.setLocationRelativeTo(YAWLEditor.getInstance());
      netDialogs.put(getGraph(), dialog);
      dialog.setVisible(true);
    } else {
      ((NetDecompositionUpdateDialog) netDialogs.get(getGraph())).setVisible(true);
    }
  }
  
  private boolean invokedAtLeastOnce(NetGraph net) {
    if (netDialogs.containsKey(net)) {
      return true;
    }
    return false;
  }
}