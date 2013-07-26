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

package org.yawlfoundation.yawl.editor.ui.properties.dialog;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.swing.AbstractDoneDialog;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.component.CodeletSelectTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CodeletDialog extends AbstractDoneDialog {

    private CodeletSelectTable codeletTable;


    public CodeletDialog() {
        super("Set Codelet for Automated Decomposition", true);
        setContentPanel(getCodeletPanel());
        getDoneButton().setText("OK");
        getRootPane().setDefaultButton(getCancelButton());
        setLocationRelativeTo(YAWLEditor.getInstance());
    }

    protected void makeLastAdjustments() {
        setSize(600, 400);
        setResizable(false);
    }


    public String getSelection() {
        return cancelButtonSelected() ? null : codeletTable.getSelectedCodeletName();
    }


    public void setSelection(String codeletName) {
        if (codeletName != null) {
            int lastDot = codeletName.lastIndexOf('.');
            if (lastDot > -1) codeletName = codeletName.substring(lastDot + 1);
            codeletTable.setSelectedRowWithName(codeletName);
        }
    }


    private JPanel getCodeletPanel() {
        codeletTable = new CodeletSelectTable(CodeletSelectTable.CODELET);
        JScrollPane jspane = new JScrollPane(codeletTable);
        JPanel codeletTablePanel = new JPanel();
        codeletTablePanel.setBorder(new EmptyBorder(12,12,0,11));
        codeletTablePanel.add(jspane, BorderLayout.CENTER);
        return codeletTablePanel;
    }

}
