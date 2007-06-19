/*
 * Created on 17/06/2005
 * YAWLEditor v1.3 
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

import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.JMenuItem;


import au.edu.qut.yawl.editor.swing.TooltipTogglingWidget;
import au.edu.qut.yawl.editor.actions.YAWLBaseAction;

public class YAWLPopupMenuItem extends JMenuItem {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final Insets margin = new Insets(0,0,0,0);

  public YAWLPopupMenuItem(YAWLBaseAction a) {
    super(a);    
    setMargin(margin);
  }
  
  public Point getToolTipLocation(MouseEvent e) {
    return new Point(0 + 2,getSize().height + 2);
  }
  
  public void setEnabled(boolean enabled) {
    if (getAction() instanceof TooltipTogglingWidget) {
      TooltipTogglingWidget action = (TooltipTogglingWidget) this.getAction();
      if (enabled) {
        setToolTipText(action.getEnabledTooltipText());
      } else {
        setToolTipText(action.getDisabledTooltipText());
      }
    }
    super.setEnabled(enabled);
  }
  
  public boolean shouldBeEnabled() {
    return ((YAWLBaseAction) getAction()).shouldBeEnabled();
  }
  
  public boolean shouldBeVisible() {
    return ((YAWLBaseAction) getAction()).shouldBeVisible();
  }
}
