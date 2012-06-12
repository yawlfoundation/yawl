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

package org.yawlfoundation.yawl.editor.ui.actions;

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.swing.YAWLEditorDesktop;

import javax.swing.*;
import java.awt.event.ActionEvent;

public abstract class YAWLBaseAction extends AbstractAction {

    protected static final String iconPath = "/org/yawlfoundation/yawl/editor/ui/resources/menuicons/";

    protected ImageIcon getIconByName(String iconName) {
        return ResourceLoader.getImageAsIcon(iconPath + iconName + "16.gif");
    }

    protected ImageIcon getPNGIcon(String iconName) {
        return ResourceLoader.getImageAsIcon(iconPath + iconName + ".png");
    }


    public void actionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(YAWLEditor.getInstance(),
                "The action labelled '" + getValue(Action.NAME) +
                        "' is not yet implemented.\n\n",
                "No Action",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public NetGraph getGraph() {
        return YAWLEditorDesktop.getInstance().getSelectedGraph();
    }

    public boolean shouldBeEnabled() {
        return true;
    }

    public boolean shouldBeVisible() {
        return true;
    }
}
