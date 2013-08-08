/*
 * Created on 11/06/2003
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
 */

package org.yawlfoundation.yawl.editor.ui.actions.specification;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.DataDefinitionDialog;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DataTypeDefinitionsAction extends YAWLOpenSpecificationAction {


    {
        putValue(Action.SHORT_DESCRIPTION, " Update Data Type Definitions. ");
        putValue(Action.NAME, "Data Types...");
        putValue(Action.LONG_DESCRIPTION, "Update Data Type Definitions.");
        putValue(Action.SMALL_ICON, getPNGIcon("page_white_code"));
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_D));
        putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("D"));
    }

    public void actionPerformed(ActionEvent event) {
        String currentSchema = SpecificationModel.getHandler().getSchema();
        DataDefinitionDialog dialog = new DataDefinitionDialog();
        dialog.setContent(currentSchema);
        dialog.setVisible(true);
        String newContent = dialog.getContent();
        if (! (newContent == null || newContent.equals(currentSchema))) {
            try {
                SpecificationModel.getHandler().setSchema(newContent);
            }
            catch (YSyntaxException yse) {
                YAWLEditor.getStatusBar().setText(
                        "Failed to update Data Definition: Invalid syntax");
            }
        }

    }
}
