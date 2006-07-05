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

package au.edu.qut.yawl.editor.actions.element;

import au.edu.qut.yawl.editor.net.NetGraph;
import au.edu.qut.yawl.editor.actions.net.YAWLSelectedNetAction;
import au.edu.qut.yawl.editor.elements.model.YAWLCondition;

import au.edu.qut.yawl.editor.swing.TooltipTogglingWidget;
import au.edu.qut.yawl.editor.swing.element.LabelConditionDialog;

import java.awt.event.ActionEvent;

import javax.swing.Action;

public class LabelConditionAction extends YAWLSelectedNetAction 
                                implements TooltipTogglingWidget {

  private static final LabelConditionDialog labelConditionDialog = new LabelConditionDialog();

  private NetGraph net;
  private YAWLCondition condition;
  
  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Set Label...");
    putValue(Action.LONG_DESCRIPTION, "Labels this condition.");
    putValue(Action.SMALL_ICON, getIconByName("LabelCondition"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_L));
  }
  
  public LabelConditionAction(YAWLCondition condition, NetGraph net) {
    super();
    this.condition = condition;
    this.net = net;
  }  

  public void actionPerformed(ActionEvent event) {
    labelConditionDialog.setCondition(net, condition);
    labelConditionDialog.setVisible(true);

    net.clearSelection();
  }
  
  public String getEnabledTooltipText() {
    return " Label this condition ";
  }
  
  public String getDisabledTooltipText() {
    return " You must have a condition selected" + 
           " to label it ";
  }
}