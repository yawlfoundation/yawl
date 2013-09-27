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

package org.yawlfoundation.yawl.editor.ui.actions.view;

import org.yawlfoundation.yawl.editor.ui.actions.YAWLBaseAction;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class ToolbarTipToggleAction extends YAWLBaseAction {

    private boolean selected;

    {
        putValue(Action.SHORT_DESCRIPTION, "Show Tooltips");
        putValue(Action.NAME, "Show Tooltips");
        putValue(Action.SMALL_ICON, getPNGIcon("balloon"));
        putValue(Action.LONG_DESCRIPTION, "Show Tooltips");
        putValue(Action.MNEMONIC_KEY, KeyEvent.VK_T);
    }

    public ToolbarTipToggleAction() {
        selected = UserSettings.getShowToolTips();
        ToolTipManager.sharedInstance().setEnabled(selected);
    }

    public void actionPerformed(ActionEvent event) {
        selected = !selected;
        ToolTipManager.sharedInstance().setEnabled(selected);
        UserSettings.setShowToolTips(selected);
    }

    public boolean isSelected() {
        return selected;
    }
}
