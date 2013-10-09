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

package org.yawlfoundation.yawl.editor.ui.actions.element;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.actions.net.ProcessConfigurationAction;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.swing.TooltipTogglingWidget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class CancellationRegionConfigurationAction extends ProcessConfigurationAction
        implements TooltipTogglingWidget {

    {
        putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
        putValue(Action.NAME, "Cancellation Region...");
        putValue(Action.LONG_DESCRIPTION, "Configure the cancellation region for this task.");
        putValue(Action.SMALL_ICON, getPNGIcon("comment_delete"));

    }


    public CancellationRegionConfigurationAction() {
        super();
    }

    public void actionPerformed(ActionEvent event) {
        final YAWLTask task = this.task;
        final NetGraph net = this.net;
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ConfigureCancellationSetJDialog dialog =
                        new ConfigureCancellationSetJDialog(
                                new javax.swing.JFrame(), true, task);
                dialog.setLocationRelativeTo(YAWLEditor.getInstance());
                dialog.setPreferredSize(new Dimension(200, 100));
                dialog.setResizable(false);
                dialog.setVisible(true);
            }
        });
    }


    public void setEnabled(boolean enable) {
        super.setEnabled(enable && task != null && task.isConfigurable() &&
                task.hasCancellationSetMembers());
    }

    public String getDisabledTooltipText() {
        return null;
    }


    public String getEnabledTooltipText() {
        return "Configure the cancellation region for this task.";
    }


    private class ConfigureCancellationSetJDialog extends JDialog implements ActionListener {

        private YAWLTask task;

        /**
         * Creates new form ConfigureCancellationSetJDialog
         */
        public ConfigureCancellationSetJDialog(java.awt.Frame parent, boolean modal,
                                               YAWLTask task) {
            super(parent, modal);
            this.task = task;
            this.setTitle("");
            initComponents();
        }

        private void initComponents() {
            JRadioButton rbActivate = new JRadioButton("Activate");
            rbActivate.setMnemonic(KeyEvent.VK_A);
            rbActivate.setActionCommand("activate");
            rbActivate.addActionListener(this);
            rbActivate.setSelected(true);

            JRadioButton rbBlock = new JRadioButton("Block");
            rbBlock.setMnemonic(KeyEvent.VK_B);
            rbBlock.setActionCommand("block");
            rbBlock.addActionListener(this);

            ButtonGroup group = new ButtonGroup();
            group.add(rbActivate);
            group.add(rbBlock);

            JPanel panel = new JPanel(new GridLayout(0, 1));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            panel.add(rbActivate);
            panel.add(rbBlock);

            getContentPane().setLayout(new BorderLayout());
            add(panel, BorderLayout.CENTER);
            setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            pack();
        }

        public void actionPerformed(ActionEvent e) {
            task.setCancellationSetEnable(e.getActionCommand().equals("enable"));
        }

    }

}
