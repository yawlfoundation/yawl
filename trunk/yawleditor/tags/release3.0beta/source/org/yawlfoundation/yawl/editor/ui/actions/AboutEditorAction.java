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

package org.yawlfoundation.yawl.editor.ui.actions;

import org.yawlfoundation.yawl.editor.ui.swing.JUtilities;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;
import org.yawlfoundation.yawl.util.YBuildProperties;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;


public class AboutEditorAction extends YAWLBaseAction {


    {
        putValue(Action.SHORT_DESCRIPTION, "About the YAWL Editor");
        putValue(Action.NAME, "About...");
        putValue(Action.LONG_DESCRIPTION, "About the YAWL Editor");
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_A));
    }

    public void actionPerformed(ActionEvent event) {
        new AboutEditorDialog().setVisible(true);
    }
}

class AboutEditorDialog extends JDialog {

    private static final String DEFAULT_VERSION = "3.0 (beta)";
    private static final String DEFAULT_YEAR = "2014";
    private static final Color BACK_COLOUR = new Color(254,254,240);

    public AboutEditorDialog() {
        super();
        setUndecorated(true);
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setContentPane(getAboutPanel());
        setSize(430, 160);
        addKeyListener(this);
        JUtilities.centerWindow(this);
    }

    private JPanel getAboutPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new CompoundBorder(new LineBorder(Color.GRAY),
                new EmptyBorder(7,7,7,7)));
        panel.setBackground(BACK_COLOUR);
        panel.add(getLogoPanel(), BorderLayout.WEST);
        panel.add(getTextPanel(), BorderLayout.CENTER);
        return panel;
    }


    private JPanel getLogoPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(5,5,5,5));
        panel.setBackground(BACK_COLOUR);
        JLabel logo = new JLabel(ResourceLoader.getImage("yawlLogo.png"));
        panel.add(logo);
        addMouseListener(logo);
        addMouseListener(panel);
        return panel;
    }

    private JPanel getTextPanel() {
        JPanel panel = new JPanel(new GridLayout(0,1,3,3));
        panel.setBorder(new EmptyBorder(5,10,20,5));
        panel.setBackground(BACK_COLOUR);

        JLabel nameLabel = new JLabel("The YAWL Process Editor");
        Font font = nameLabel.getFont();
        font = font.deriveFont(Font.BOLD);
        nameLabel.setFont(font.deriveFont(18f));
        nameLabel.setForeground(Color.DARK_GRAY);
        addMouseListener(nameLabel);
        panel.add(nameLabel);

        YBuildProperties buildProperties = loadBuildProperties();

        JLabel versionLabel = new JLabel(getVersionText(buildProperties));
        versionLabel.setForeground(Color.DARK_GRAY);
        addMouseListener(versionLabel);
        panel.add(versionLabel);

        JLabel buildLabel = new JLabel(getBuildDateText(buildProperties));
        buildLabel.setForeground(Color.DARK_GRAY);
        addMouseListener(buildLabel);
        panel.add(buildLabel);

        JLabel copyLabel = new JLabel(getCopyrightText(buildProperties));
        copyLabel.setForeground(Color.DARK_GRAY);
        addMouseListener(copyLabel);
        panel.add(copyLabel);

        addMouseListener(panel);
        return panel;
    }


    private YBuildProperties loadBuildProperties() {
        YBuildProperties buildProperties = null;
        InputStream is = getClass().getResourceAsStream("/version.properties");
        if (is != null) {
            buildProperties = new YBuildProperties();
            buildProperties.load(is);
        }
        return buildProperties;
    }


    private String getVersionText(YBuildProperties buildProperties) {
        String version = buildProperties.getVersion();
        String buildNumber = buildProperties.getBuildNumber();
        String result = "Version " + (version != null ? version : DEFAULT_VERSION);
        if (buildNumber != null) {
            result += " (build " + buildNumber + ")";
        }
        return result;
    }


    private String getBuildDateText(YBuildProperties buildProperties) {
        String buildDate = buildProperties.getBuildDate();
        return "Build Date: " + (buildDate != null ? buildDate : "");
    }


    private String getCopyrightText(YBuildProperties buildProperties) {
        String buildDate = buildProperties.getBuildDate();
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

