package org.yawlfoundation.yawl.editor.ui.preferences;

import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
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


    public FileOptionsPanel(ActionListener listener) {
        super();
        getContent(listener);
        setPreferredSize(new Dimension(500, 400));
    }


    public void applyChanges() {
        UserSettings.setVerifyOnSave(verifyCheckBox.isSelected());
        UserSettings.setAnalyseOnSave(analysisCheckBox.isSelected());
        UserSettings.setAutoIncrementVersionOnSave(autoIncVersionCheckBox.isSelected());
        UserSettings.setFileBackupOnSave(backupCheckBox.isSelected());
        UserSettings.setFileVersioningOnSave(versionCopyCheckBox.isSelected());
        UserSettings.setShowFileOptionsDialogOnSave(showDialogCheckBox.isSelected());
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
        autoIncVersionCheckBox = makeCheckBox("Auto Increment minor version number",
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

