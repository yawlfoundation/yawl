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

package org.yawlfoundation.yawl.editor.ui.actions.element;

import org.yawlfoundation.yawl.editor.ui.actions.net.YAWLSelectedNetAction;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;
import org.yawlfoundation.yawl.editor.ui.swing.element.LabelElementDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class LabelElementAction extends YAWLSelectedNetAction 
                                implements TooltipTogglingWidget {

  private static final long serialVersionUID = 1L;

  private NetGraph net;
  private YAWLVertex vertex;
  
  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Set Label...");
    putValue(Action.LONG_DESCRIPTION, "Labels this element.");
    putValue(Action.SMALL_ICON, getPNGIcon("page_edit"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_L));
  }
  
  public LabelElementAction(YAWLVertex vertex, NetGraph net) {
    super();
    this.vertex = vertex;
    this.net = net;
  }  

  public void actionPerformed(ActionEvent event) {
    LabelElementDialog labelElementDialog = new LabelElementDialog();
    labelElementDialog.setVertex(vertex, net);
    labelElementDialog.setVisible(true);
    net.clearSelection();
  }
  
  public String getEnabledTooltipText() {
    return " Label this element ";
  }
  
  public String getDisabledTooltipText() {
    return " You must have an element selected" + 
           " to label it ";
  }
}