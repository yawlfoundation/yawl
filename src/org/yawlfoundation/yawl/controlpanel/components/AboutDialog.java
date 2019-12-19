/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.controlpanel.components;

import org.yawlfoundation.yawl.controlpanel.YControlPanel;
import org.yawlfoundation.yawl.controlpanel.icons.IconLoader;
import org.yawlfoundation.yawl.controlpanel.update.ChecksumsReader;
import org.yawlfoundation.yawl.controlpanel.util.FileUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Calendar;

/**
 * @author Michael Adams
 * @date 26/08/2014
 */
public class AboutDialog extends JDialog {

    private static final Color BACK_COLOUR = new Color(70,100,100);
    private static final String DEFAULT_YEAR =
           String.valueOf(Calendar.getInstance().get(Calendar.YEAR));


    public AboutDialog(JFrame frame) {
        super();
        init(frame);
    }


    private void init(JFrame frame) {
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setContentPane(createContent());
        addKeyListener(this);
        pack();
        setLocationRelativeTo(frame);
    }


    private JPanel createContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new CompoundBorder(new LineBorder(Color.GRAY),
                new EmptyBorder(12,12,12,12)));
        panel.setBackground(BACK_COLOUR);
        panel.add(new JLabel(IconLoader.get("YawlRunning")), BorderLayout.WEST);
        panel.add(getLabel(), BorderLayout.CENTER);
        addMouseListener(panel);
        return panel;
    }


    private JLabel getLabel() {
        ChecksumsReader props = new ChecksumsReader(FileUtil.getLocalCheckSumFile());
        StringBuilder s = new StringBuilder();
        s.append("<b>YAWL Control Panel ");
        s.append(getVersion(props));

        String build = getBuild(props);
        if (build != null) s.append(" (build ").append(build).append(')');

        s.append("</b><br><br>");
        s.append(getCopyrightText(props));

        JLabel label = new JLabel(StringUtil.wrap(s.toString(), "HTML"));
        label.setFont(label.getFont().deriveFont(18.0f));
        label.setForeground(new Color(240,240,240));
        addMouseListener(label);
        return label;
    }


    private String getVersion(ChecksumsReader props) {
        String version = props.getVersion();
        return version != null ? version : YControlPanel.VERSION;
    }


    private String getBuild(ChecksumsReader props) {
        XNode node = props.getControlPanelNode();
        return node != null ? node.getChildText("build") : null;
    }


    private String getCopyrightText(ChecksumsReader props) {
        String buildDate = null;
        XNode node = props.getControlPanelFileNode();
        if (node != null) {
            buildDate = node.getAttributeValue("timestamp");
        }
        String year = buildDate != null ? buildDate.substring(0,4) : DEFAULT_YEAR;
        return "\u00a9 " + year + " The YAWL Foundation";
    }



    private void addMouseListener(Component c) {
        c.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                setVisible(false);
            }
        });
    }


    private void addKeyListener(Component c) {
        c.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent keyEvent) {
                setVisible(false);
            }
        });
    }

}
