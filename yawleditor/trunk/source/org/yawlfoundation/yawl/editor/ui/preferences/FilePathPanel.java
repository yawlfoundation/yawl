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

package org.yawlfoundation.yawl.editor.ui.preferences;

import org.yawlfoundation.yawl.editor.ui.util.FileUtilities;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * @author Michael Adams
 * @date 26/09/13
 */
public class FilePathPanel extends JPanel implements PreferencePanel {

    private JTextField _fldDecomposition;
    private JTextField _fldVariable;
    private JTextField _fldIcons;
    private JTextField _fldWofyawl;
    private JTextField _fldWendy;

    private static final String PROPERTY_LOCATION =
            FileUtilities.getDecompositionAttributePath();


    public FilePathPanel(CaretListener listener) {
        super();
        addContent(listener);
        setPreferredSize(new Dimension(500, 400));
    }


    public void applyChanges() {
        UserSettings.setDecompositionAttributesFilePath(_fldDecomposition.getText());
        UserSettings.setVariableAttributesFilePath(_fldVariable.getText());
        UserSettings.setTaskIconsFilePath(checkPath(_fldIcons.getText()));
        UserSettings.setWofyawlFilePath(_fldWofyawl.getText());
        UserSettings.setWendyFilePath(checkPath(_fldWendy.getText()));
    }


    private void addContent(CaretListener listener) {
        _fldVariable = new JTextField(getVariablePath());
        _fldDecomposition = new JTextField(getDecompositionPath());
        _fldIcons = new JTextField(getTaskIconsPath());
        _fldWofyawl = new JTextField(getWofyawlPath());
        _fldWendy = new JTextField(getWendyPath());

        JPanel contentPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPanel.add(buildFileEntryPanel(_fldDecomposition,
                "Decomposition Extended Attributes File", listener));
        contentPanel.add(buildFileEntryPanel(_fldVariable,
                "Variable Extended Attributes File", listener));
        contentPanel.add(buildFileEntryPanel(_fldIcons,
                "Task Icons Folder", listener));
        contentPanel.add(buildFileEntryPanel(_fldWofyawl,
                "WofYAWL Analysis File", listener));
        contentPanel.add(buildFileEntryPanel(_fldWendy,
                "Wendy (Process Configuration) Folder", listener));
        add(contentPanel);
    }

    private JPanel buildFileEntryPanel(JTextField textField, String title,
                                       CaretListener listener) {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new TitledBorder(title));
        panel.add(buildFileButton(title), BorderLayout.EAST);
        textField.setPreferredSize(new Dimension(440, 25));
        textField.addCaretListener(listener);
        panel.add(textField, BorderLayout.CENTER);
        return panel;
    }


    private JButton buildFileButton(String title) {
        JButton button = new JButton("...");
        button.setPreferredSize(new Dimension(25, 15));
        button.setToolTipText(" Select File Dialog ");
        button.setActionCommand(title.substring(0, title.indexOf(' ')));

        final JPanel thisPanel = this;
        button.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                String cmd = e.getActionCommand();
                JFileChooser fileChooser = new JFileChooser(getInitialDir(cmd));
                fileChooser.setDialogTitle("Select " + getDialogTitle(cmd));
                if (cmd.equals("Task") || cmd.equals("Wendy")) {
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                }
                if (fileChooser.showOpenDialog(thisPanel) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    if (file != null) {
                        setPath(cmd, file);
                    }
                }
            }
        });
        return button;
    }

    private String getDialogTitle(String cmd) {
        if (cmd.equals("Wendy")) {
            return "Wendy (Process Configuration) Folder";
        }
        else if (cmd.equals("WofYAWL")) {
            return "WofYAWL File";
        }
        else return cmd + (cmd.equals("Task") ? " Icons Folder" : " Extended Attributes File");
    }

    private void setPath(String cmd, File file) {
        try {
            getField(cmd).setText(file.getCanonicalPath());
        }
        catch (IOException ioe) {
            getField(cmd).setText(file.getAbsolutePath());
        }
    }

    private String checkPath(String path) {
        if (path.endsWith("/") || path.endsWith("\\")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }


    private String getDecompositionPath() {
        String path = UserSettings.getDecompositionAttributesFilePath();
        return path != null ? path : PROPERTY_LOCATION;
    }

    private String getVariablePath() {
        return UserSettings.getVariableAttributesFilePath();
    }

    private String getTaskIconsPath() {
        return FileUtilities.getAbsoluteTaskIconPath();
    }

    private String getWofyawlPath() {
        String path = UserSettings.getWofyawlFilePath();
        return path != null ? path : FileUtilities.getHomeDir();
    }

    private String getWendyPath() {
        String path = UserSettings.getWendyFilePath();
        return path != null ? path : FileUtilities.getHomeDir() + "wendy";
    }

    private String getInitialDir(String cmd) {
        String path = getField(cmd).getText();
        return (path != null) ? path.substring(0, path.lastIndexOf(File.separator)) : null;
    }

    private JTextField getField(String cmd) {
        if (cmd.equals("Variable")) {
            return _fldVariable;
        }
        else if (cmd.equals("Decomposition")) {
            return _fldDecomposition;
        }
        else if (cmd.equals("Task")) {
            return _fldIcons;
        }
        else if (cmd.equals("WofYAWL")) {
            return _fldWofyawl;
        }
        else if (cmd.equals("Wendy")) {
            return _fldWendy;
        }
        return null;
    }



}
