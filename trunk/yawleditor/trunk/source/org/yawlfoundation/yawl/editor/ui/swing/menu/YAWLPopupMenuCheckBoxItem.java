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

package org.yawlfoundation.yawl.editor.ui.swing.menu;

import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class YAWLPopupMenuCheckBoxItem extends JCheckBoxMenuItem {

    public YAWLPopupMenuCheckBoxItem(Action a) {
        super(a);
    }

    public Point getToolTipLocation(MouseEvent e) {
        return new Point(2, getSize().height + 2);
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
