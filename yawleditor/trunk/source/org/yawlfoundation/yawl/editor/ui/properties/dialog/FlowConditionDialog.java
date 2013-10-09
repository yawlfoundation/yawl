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

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.component.FlowConditionTablePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Author: Michael Adams
 * Creation Date: 8/04/2013
 */
public class FlowConditionDialog extends JDialog implements ActionListener {

    private FlowConditionTablePanel _tablePanel;

    public FlowConditionDialog(Window parent, YAWLTask task, NetGraph graph) {
        super(parent);
        setModal(true);
        setTitle(makeTitle(task));
        setResizable(false);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        add(getContent(task, graph));
        this.setPreferredSize(new Dimension(420, 270));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                _tablePanel.resetFlowColours();
                super.windowClosing(windowEvent);
            }
        });

        pack();
    }


    private JPanel getContent(YAWLTask task, NetGraph graph) {
        JPanel content = new JPanel();
        _tablePanel = new FlowConditionTablePanel(this, task, graph);
        JButton btnOK = new JButton("OK");
        btnOK.setActionCommand("OK");
        btnOK.setPreferredSize(new Dimension(75, 25));
        btnOK.addActionListener(this);
        content.add(_tablePanel);
        content.add(btnOK);
        return content;
    }

    public void actionPerformed(ActionEvent event) {
        _tablePanel.resetFlowColours();
        setVisible(false);
    }


    private String makeTitle(YAWLTask task) {
        return String.format("Split Conditions for Task '%s'", task.getID());
    }

}
