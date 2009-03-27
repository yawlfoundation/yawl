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

package org.yawlfoundation.yawl.editor.swing.menu;

import org.yawlfoundation.yawl.editor.swing.TooltipTogglingWidget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class YAWLToolBarButton extends JButton {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final Insets margin = new Insets(4,4,4,4);

  public YAWLToolBarButton(Action a) {
    super(a);    
    setText(null);
    setMnemonic(0);   
    setMargin(margin);
    setMaximumSize(getPreferredSize());
  }

  public Point getToolTipLocation(MouseEvent e) {
    return new Point(0,getSize().height);
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
}
