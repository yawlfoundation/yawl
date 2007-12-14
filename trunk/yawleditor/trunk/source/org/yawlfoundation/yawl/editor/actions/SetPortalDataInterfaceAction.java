/*
 * Created on 14/11/2005
 * YAWLEditor v1.0 
 *
 * @author Mike Fowler
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

package au.edu.qut.yawl.editor.actions;

import au.edu.qut.yawl.editor.YAWLEditor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;

public class SetPortalDataInterfaceAction extends YAWLBaseAction
{

    private boolean invokedAtLeastOnce = false;

    {
        putValue(Action.SHORT_DESCRIPTION, " Specify portal data interface URL. ");
        putValue(Action.NAME, "Set Portal Data Interface");
        putValue(Action.LONG_DESCRIPTION, "Specify portal data interface URL.");
//    putValue(Action.SMALL_ICON, getIconByName("Blank"));
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_P));
    }

    public SetPortalDataInterfaceAction()
    {}

    public void actionPerformed(ActionEvent event)
    {
        if (!invokedAtLeastOnce)
        {
            invokedAtLeastOnce = true;
        }

        Preferences prefs = Preferences.userNodeForPackage(YAWLEditor.class);

        Object result = JOptionPane.showInputDialog(YAWLEditor.getInstance(),
                                                    "Portal Data Interface URI",
                                                    "Specify Portal Data Interface Detail",
                                                    JOptionPane.QUESTION_MESSAGE,
                                                    null,
                                                    null,
                                                    prefs.get("portalDataInterfaceURI", ""));

        if(result != null)
        {
            prefs.put("portalDataInterfaceURI", result.toString());
        }
    }
}

