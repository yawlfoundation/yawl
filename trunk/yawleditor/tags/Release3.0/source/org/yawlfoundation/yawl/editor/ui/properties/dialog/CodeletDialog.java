/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.editor.ui.properties.dialog;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.component.CodeletData;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.component.CodeletTable;
import org.yawlfoundation.yawl.editor.ui.swing.AbstractDoneDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CodeletDialog extends AbstractDoneDialog {

    private CodeletTable codeletTable;


    public CodeletDialog() {
        super("Set Codelet for Automated Decomposition");
        setContentPanel(getCodeletPanel());
        getDoneButton().setText("OK");
        getRootPane().setDefaultButton(getCancelButton());
        setLocationRelativeTo(YAWLEditor.getInstance());
    }

    protected void makeLastAdjustments() {
        setSize(630, 400);
        setMinimumSize(new Dimension(300,200));
        setResizable(true);
    }


    public CodeletData getSelection() {
        return cancelButtonSelected() ? null : codeletTable.getSelectedCodelet();
    }


    public void setSelection(CodeletData codelet) {
        if (codelet != null) {
            codeletTable.setSelectedCodelet(codelet);
        }
    }


    private JPanel getCodeletPanel() {
        codeletTable = new CodeletTable();
        JScrollPane jspane = new JScrollPane(codeletTable);
        JPanel codeletTablePanel = new JPanel(new BorderLayout());
        codeletTablePanel.setBorder(new EmptyBorder(5,5,0,5));
        codeletTablePanel.add(jspane, BorderLayout.CENTER);
        return codeletTablePanel;
    }

}
