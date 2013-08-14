/*
 * Created on 9/10/2003
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

package org.yawlfoundation.yawl.editor.ui.actions.specification;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;
import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SaveSpecificationAction extends YAWLOpenSpecificationAction implements TooltipTogglingWidget {

    {
        putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
        putValue(Action.NAME, "Save");
        putValue(Action.LONG_DESCRIPTION, "Save this specification");
        putValue(Action.SMALL_ICON, getPNGIcon("disk"));
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_S));
        putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("S"));
    }

    public void actionPerformed(ActionEvent event) {
        SaveOptionsDialog dialog = new SaveOptionsDialog();
        dialog.setLocationRelativeTo(YAWLEditor.getInstance());
        dialog.showOrHideSpecIDField();
        dialog.setVisible(true);
    }

    public String getEnabledTooltipText() {
        return " Save this specification ";
    }

    public String getDisabledTooltipText() {
        return " You must have an open specification" +
                " to save it ";
    }
}