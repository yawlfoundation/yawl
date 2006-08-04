/*
 * Created on 9/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
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

import java.awt.Color;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JColorChooser;

import au.edu.qut.yawl.editor.YAWLEditor;

import au.edu.qut.yawl.editor.specification.SpecificationModel;
import au.edu.qut.yawl.editor.actions.YAWLBaseAction;

public class NetBackgroundColourAction extends YAWLSelectedNetAction {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  {
    putValue(Action.SHORT_DESCRIPTION, " Set the net background colour. ");
    putValue(Action.NAME, "Net background colour...");
    putValue(Action.LONG_DESCRIPTION, "Set the net background colour.");
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_B));
    putValue(Action.SMALL_ICON, getIconByName("Blank"));

  }

  public NetBackgroundColourAction() {}
 
  public void actionPerformed(ActionEvent event) {
    Color newColor = JColorChooser.showDialog(
        getGraph(),
        "Select Net Background Color",
        getGraph().getBackground()
    );
    if (newColor != null) {
      getGraph().setBackground(newColor);
    }
  }
}