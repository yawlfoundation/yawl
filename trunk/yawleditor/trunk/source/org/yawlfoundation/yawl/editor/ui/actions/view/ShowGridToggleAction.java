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

package org.yawlfoundation.yawl.editor.ui.actions.view;

import org.yawlfoundation.yawl.editor.ui.actions.YAWLBaseAction;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ShowGridToggleAction extends YAWLBaseAction {

    private boolean selected;

    {
        putValue(Action.SHORT_DESCRIPTION, " Toggle the display of grids on nets. ");
        putValue(Action.NAME, "Show Grid");
        putValue(Action.LONG_DESCRIPTION, "Toggle the display of grids on nets.");
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_G));
    }

    public ShowGridToggleAction() {
        selected = UserSettings.getShowGrid();
    }

    public void actionPerformed(ActionEvent event) {
        selected = !selected;
        JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) event.getSource();
        menuItem.setSelected(selected);
        UserSettings.setShowGrid(selected);
        for (NetGraphModel net : SpecificationModel.getInstance().getNets()) {
            net.getGraph().setGridVisible(selected);
        }

    }

    public boolean isSelected() {
        return selected;
    }
}
