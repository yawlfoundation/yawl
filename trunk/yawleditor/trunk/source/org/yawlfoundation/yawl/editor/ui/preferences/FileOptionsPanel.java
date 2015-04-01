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

import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * @author Michael Adams
 * @date 4/10/13
 */
public class FileOptionsPanel extends JPanel implements PreferencePanel {

    private JCheckBox verifyCheckBox;
    private JCheckBox analysisCheckBox;
    private JCheckBox autoIncVersionCheckBox;
    private JCheckBox backupCheckBox;
    private JCheckBox versionCopyCheckBox;
    private JCheckBox showDialogCheckBox;
    private JCheckBox reloadOnStartCheckBox;


    public FileOptionsPanel(ActionListener listener) {
        super();
        getContent(listener);
    }


    public void applyChanges() {
        UserSettings.setVerifyOnSave(verifyCheckBox.isSelected());
        UserSettings.setAnalyseOnSave(analysisCheckBox.isSelected());
        UserSettings.setAutoIncrementVersionOnSave(autoIncVersionCheckBox.isSelected());
        UserSettings.setFileBackupOnSave(backupCheckBox.isSelected());
        UserSettings.setFileVersioningOnSave(versionCopyCheckBox.isSelected());
        UserSettings.setShowFileOptionsDialogOnSave(showDialogCheckBox.isSelected());
        UserSettings.setReloadLastSpecOnStartup(reloadOnStartCheckBox.isSelected());
    }


    private void getContent(ActionListener listener) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        add(makeVerifyCheckBox(listener));
        add(makeAnalysisCheckBox(listener));
        add(makeAutoIncVersionCheckBox(listener));
        add(makeBackupCheckBox(listener));
        add(makeVersionCopyCheckBox(listener));
        add(makeShowDialogCheckBox(listener));
        add(makeReloadOnStartupCheckBox(listener));
    }


    private JCheckBox makeVerifyCheckBox(ActionListener listener) {
        verifyCheckBox = makeCheckBox("Verify specification",
                KeyEvent.VK_V, UserSettings.getVerifyOnSave(), listener);
        return verifyCheckBox;
    }

    private JCheckBox makeAnalysisCheckBox(ActionListener listener) {
        analysisCheckBox = makeCheckBox("Analyse specification",
                KeyEvent.VK_A, UserSettings.getAnalyseOnSave(), listener);
        return analysisCheckBox;
    }

    private JCheckBox makeAutoIncVersionCheckBox(ActionListener listener) {
        autoIncVersionCheckBox = makeCheckBox("Auto increment minor version number",
                KeyEvent.VK_I, UserSettings.getAutoIncrementVersionOnSave(), listener);
        return autoIncVersionCheckBox;
    }

    private JCheckBox makeBackupCheckBox(ActionListener listener) {
        backupCheckBox = makeCheckBox("Create backup file of specification",
                KeyEvent.VK_B, UserSettings.getFileBackupOnSave(), listener);
        return backupCheckBox;
    }

    private JCheckBox makeVersionCopyCheckBox(ActionListener listener) {
        versionCopyCheckBox = makeCheckBox("Save each previous version of specification",
                KeyEvent.VK_P, UserSettings.getFileVersioningOnSave(), listener);
        return versionCopyCheckBox;
    }

    private JCheckBox makeShowDialogCheckBox(ActionListener listener) {
        showDialogCheckBox = makeCheckBox("Show save option dialog when saving a file",
                KeyEvent.VK_S, UserSettings.getShowFileOptionsDialogOnSave(), listener);
        return showDialogCheckBox;
    }

    private JCheckBox makeReloadOnStartupCheckBox(ActionListener listener) {
        reloadOnStartCheckBox = makeCheckBox("Reload most recent specification on startup",
                KeyEvent.VK_R, UserSettings.getReloadLastSpecOnStartup(), listener);
        return reloadOnStartCheckBox;
    }


    private JCheckBox makeCheckBox(String caption, int mnemonic, boolean selected,
                                   ActionListener listener) {
        JCheckBox checkBox = new JCheckBox(caption);
        checkBox.setBorder(new EmptyBorder(5, 5, 5, 0));
        checkBox.setMnemonic(mnemonic);
        checkBox.setSelected(selected);
        checkBox.setAlignmentX(LEFT_ALIGNMENT);
        checkBox.addActionListener(listener);
        return checkBox;
    }

}

