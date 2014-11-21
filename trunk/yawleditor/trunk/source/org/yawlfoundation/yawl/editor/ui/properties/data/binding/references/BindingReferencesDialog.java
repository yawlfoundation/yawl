/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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

/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.properties.data.binding.references;

import org.yawlfoundation.yawl.editor.core.data.BindingReference;
import org.yawlfoundation.yawl.editor.ui.properties.data.DataVariableDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Michael Adams
 * @date 21/11/2014
 */
public class BindingReferencesDialog extends JDialog implements ActionListener {

    private BindingReferencesTable table;

    public BindingReferencesDialog(DataVariableDialog dataDialog,
                                   java.util.List<BindingReference> references,
                                   String varName) {
        super(dataDialog);
        add(getContent(references));
        setModal(true);
        setTitle("References to/from net variable: " + varName);
        setMinimumSize(new Dimension(600, 320));
        setLocationRelativeTo(dataDialog);
        pack();
    }


    public BindingReferencesTable getTable() { return table; }


    public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();
        if (cmd.equals("Close")) {
            setVisible(false);
        }
    }


    private JPanel getContent(java.util.List<BindingReference> references) {
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(7, 7, 7, 7));
        JPanel subContent = new JPanel(new GridLayout(0, 1, 10, 10));
        BindingReferencesTablePanel panel =
                new BindingReferencesTablePanel(references);
        table = panel.getTable();
        table.setDefaultEditor(String.class, new BindingViewer(this));
        subContent.add(panel);
        content.add(subContent, BorderLayout.CENTER);
        content.add(createButtonBar(), BorderLayout.SOUTH);
        return content;
    }


    private JPanel createButtonBar() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10,0,0,0));
        JButton btnClose = createButton("Close");
        getRootPane().setDefaultButton(btnClose);
        panel.add(btnClose);
        return panel;
    }


    private JButton createButton(String label) {
        JButton button = new JButton(label);
        button.setPreferredSize(new Dimension(70,25));
        button.setActionCommand(label);
        button.setMnemonic(label.charAt(0));
        button.addActionListener(this);
        return button;
    }

}
