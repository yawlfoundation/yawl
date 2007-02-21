/*
 * Created on 25/02/2005
 * YAWLEditor v1.1-1
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

package au.edu.qut.yawl.editor.actions.view;

import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;

import au.edu.qut.yawl.editor.YAWLEditor;
import au.edu.qut.yawl.editor.actions.YAWLBaseAction;
import au.edu.qut.yawl.editor.specification.SpecificationModel;

public class ShowGridToggleAction extends YAWLBaseAction {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private boolean selected;
  private Preferences prefs = Preferences.userNodeForPackage(YAWLEditor.class);

  {
    putValue(Action.SHORT_DESCRIPTION, " Toggle the display of grids on nets. ");
    putValue(Action.NAME, "Show grids in diagrams");
    putValue(Action.LONG_DESCRIPTION, "Toggle the display of grids on nets.");
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_G));
  }

  public ShowGridToggleAction() {
    selected = prefs.getBoolean("showNetGrid", true);
  }
 
  public void actionPerformed(ActionEvent event) {
    selected = !selected;
    
    JCheckBoxMenuItem menuItem = 
      (JCheckBoxMenuItem) event.getSource();
    menuItem.setSelected(selected);
    prefs.putBoolean("showNetGrid",selected);
    SpecificationModel.getInstance().showNetGrid(selected);
  }
  
  public boolean isSelected() {
    return selected; 
  }
}
