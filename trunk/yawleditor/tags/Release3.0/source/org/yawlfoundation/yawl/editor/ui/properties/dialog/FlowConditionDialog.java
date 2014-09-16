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
import javax.swing.border.EmptyBorder;
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
    private JButton _btnOK;

    public FlowConditionDialog(Window parent, YAWLTask task, NetGraph graph) {
        super(parent);
        setModal(true);
        setTitle(makeTitle(task));
        setResizable(true);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        add(getContent(task, graph));
        this.setMinimumSize(new Dimension(420, 180));

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
        JPanel content = new JPanel(new BorderLayout());
        _tablePanel = new FlowConditionTablePanel(this, task, graph);
        _btnOK = new JButton("OK");
        _btnOK.setActionCommand("OK");
        _btnOK.setPreferredSize(new Dimension(75, 25));
        _btnOK.addActionListener(this);
        JPanel btnPanel = new JPanel();
        btnPanel.setBorder(new EmptyBorder(0,0,5,0));
        btnPanel.add(_btnOK);
        content.add(_tablePanel, BorderLayout.CENTER);
        content.add(btnPanel, BorderLayout.SOUTH);
        return content;
    }

    public void actionPerformed(ActionEvent event) {
        _tablePanel.stopEdits();
        _tablePanel.resetFlowColours();
        setVisible(false);
    }


    public void enableOK(boolean enable) { _btnOK.setEnabled(enable);}


    private String makeTitle(YAWLTask task) {
        return String.format("Split Predicates for Task '%s'", task.getID());
    }

}
