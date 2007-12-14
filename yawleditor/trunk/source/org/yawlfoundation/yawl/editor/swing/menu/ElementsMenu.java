/*
 * Created on 05/10/2003
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

package au.edu.qut.yawl.editor.swing.menu;

import java.awt.event.KeyEvent;
import javax.swing.JMenu;

import au.edu.qut.yawl.editor.actions.net.DecreaseSizeAction;
import au.edu.qut.yawl.editor.actions.net.IncreaseSizeAction;

import au.edu.qut.yawl.editor.actions.net.AlignLeftAction;
import au.edu.qut.yawl.editor.actions.net.AlignRightAction;
import au.edu.qut.yawl.editor.actions.net.AlignCentreAction;
import au.edu.qut.yawl.editor.actions.net.AlignTopAction;
import au.edu.qut.yawl.editor.actions.net.AlignMiddleAction;
import au.edu.qut.yawl.editor.actions.net.AlignBottomAction;

import au.edu.qut.yawl.editor.actions.net.AddToVisibleCancellationSetAction;
import au.edu.qut.yawl.editor.actions.net.RemoveFromVisibleCancellationSetAction;


class ElementsMenu extends YAWLOpenSpecificationMenu {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public ElementsMenu() {
    super("Elements",KeyEvent.VK_L);
  }
  
  protected void buildInterface() {
    add(getAlignmentMenu());
    addSeparator();
		add(new YAWLMenuItem(IncreaseSizeAction.getInstance()));
		add(new YAWLMenuItem(DecreaseSizeAction.getInstance()));
		addSeparator();
    add(new YAWLMenuItem(AddToVisibleCancellationSetAction.getInstance()));
    add(new YAWLMenuItem(RemoveFromVisibleCancellationSetAction.getInstance()));
  }
  
  private JMenu getAlignmentMenu() {
    JMenu alignmentMenu = new JMenu("Alignment");
    alignmentMenu.setMnemonic(KeyEvent.VK_L);
    
    alignmentMenu.add(new YAWLMenuItem(AlignTopAction.getInstance()));
    alignmentMenu.add(new YAWLMenuItem(AlignMiddleAction.getInstance()));
    alignmentMenu.add(new YAWLMenuItem(AlignBottomAction.getInstance()));
    alignmentMenu.addSeparator();
    alignmentMenu.add(new YAWLMenuItem(AlignLeftAction.getInstance()));
    alignmentMenu.add(new YAWLMenuItem(AlignCentreAction.getInstance()));
    alignmentMenu.add(new YAWLMenuItem(AlignRightAction.getInstance()));

    return alignmentMenu;
  }
}
