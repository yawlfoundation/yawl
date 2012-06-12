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

package org.yawlfoundation.yawl.editor.ui.actions.element;

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.actions.net.YAWLSelectedNetAction;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class SetElementFillColourAction extends YAWLSelectedNetAction
                                implements TooltipTogglingWidget {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

    private NetGraph net;
    private YAWLVertex vertex;

  {
    putValue(Action.SHORT_DESCRIPTION, " Set the element's background colour. ");
    putValue(Action.NAME, "Set Fill Colour...");
    putValue(Action.LONG_DESCRIPTION, "Set the element's background colour.");
    putValue(Action.SMALL_ICON, getPNGIcon("color_swatch"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_F));
  }

  public SetElementFillColourAction(YAWLVertex vertex, NetGraph net) {
    super();
    this.vertex = vertex;
    this.net = net;
  }

    public void actionPerformed(ActionEvent event) {
    Color newColor = JColorChooser.showDialog(
        YAWLEditor.getInstance(),
        "Set Element Background Color",
        vertex.getBackgroundColor()
    );
    if (newColor != null) {
      vertex.setBackgroundColor(newColor);
      net.changeVertexBackground(vertex, newColor);
      net.resetCancellationSet();
      net.clearSelection();
    }
  }

    public String getEnabledTooltipText() {
      return " Change the background colour of the selected element ";
    }

    public String getDisabledTooltipText() {
      return "";
    }

    public boolean shouldBeEnabled() {
        return true;
    }

}