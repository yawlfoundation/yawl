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

import org.yawlfoundation.yawl.editor.ui.swing.menu.DataTypeDialogToolBarMenu;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CopyDataTypeDialogAction extends YAWLBaseAction {

    {
        putValue(Action.SHORT_DESCRIPTION, " Copy the selected text to the Clipboard");
        putValue(Action.NAME, "Copy");
        putValue(Action.LONG_DESCRIPTION, "Copy the selected text");
        putValue(Action.SMALL_ICON, getPNGIcon("page_copy"));
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_C));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control C"));
    }


    public void actionPerformed(ActionEvent event) {
        DataTypeDialogToolBarMenu.getEditorPane().getEditor().copy();
        DataTypeDialogToolBarMenu.getInstance().getButton("paste").setEnabled(true);
    }
}