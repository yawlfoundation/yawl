package org.yawlfoundation.yawl.editor.ui.actions.specification;

import org.yawlfoundation.yawl.editor.ui.swing.AbstractDoneDialog;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * Author: Michael Adams
 * Creation Date: 15/10/2008
 */
class SaveOptionsDialog extends AbstractDoneDialog {

    private JCheckBox verificationCheckBox;
    private JCheckBox analysisCheckBox;
    private JCheckBox autoIncVersionCheckBox;
    private JCheckBox backupCheckBox;
    private JCheckBox versionCopyCheckBox;
    private JCheckBox showDialogCheckBox;

    public SaveOptionsDialog() {
        super("File Save Options", true);

        setContentPanel(getContent());
        getDoneButton().setText("OK");
        getDoneButton().addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        UserSettings.setVerifyOnSave(verificationCheckBox.isSelected());
                        UserSettings.setAnalyseOnSave(analysisCheckBox.isSelected());
                        UserSettings.setAutoIncrementVersionOnSave(
                                autoIncVersionCheckBox.isSelected());
                        UserSettings.setFileBackupOnSave(backupCheckBox.isSelected());
                        UserSettings.setFileVersioningOnSave(versionCopyCheckBox.isSelected());
                        UserSettings.setShowFileOptionsDialogOnSave(showDialogCheckBox.isSelected());
                    }
                }
        );
    }

    protected void makeLastAdjustments() {
        pack();
        setResizable(false);
    }

    private JPanel getContent() {

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();

        JPanel panel = new JPanel(gbl);
        panel.setBorder(new EmptyBorder(12,12,0,11));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0,5,5,5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        panel.add(getVerifyCheckBox(), gbc);
        gbc.gridy++;
        panel.add(getAnalysisCheckBox(), gbc);
        gbc.gridy++;
        panel.add(getAutoIncVersionCheckBox(), gbc);
        gbc.gridy++;
        panel.add(getBackupCheckBox(), gbc);
        gbc.gridy++;
        panel.add(getVersionCopyCheckBox(), gbc);
        gbc.gridy++;
        panel.add(getShowDialogCheckBox(), gbc);
        return panel;
    }


    private JCheckBox getVerifyCheckBox() {
        verificationCheckBox = new JCheckBox();
        verificationCheckBox.setText("Verify on save");
        verificationCheckBox.setToolTipText(" Check the model for errors ");
        verificationCheckBox.setMnemonic(KeyEvent.VK_V);
        verificationCheckBox.setSelected(UserSettings.getVerifyOnSave());
        return verificationCheckBox;
    }

    private JCheckBox getAnalysisCheckBox() {
        analysisCheckBox = new JCheckBox();
        analysisCheckBox.setText("Analyse on save");
        analysisCheckBox.setToolTipText(" Perform a full analysis of the model ");
        analysisCheckBox.setMnemonic(KeyEvent.VK_A);
        analysisCheckBox.setSelected(UserSettings.getAnalyseOnSave());
        return analysisCheckBox;
    }

    private JCheckBox getAutoIncVersionCheckBox() {
        autoIncVersionCheckBox = new JCheckBox();
        autoIncVersionCheckBox.setText("Auto Increment Minor Version Number");
        autoIncVersionCheckBox.setToolTipText(" Increment version number for each save ");
        autoIncVersionCheckBox.setMnemonic(KeyEvent.VK_I);
        autoIncVersionCheckBox.setSelected(UserSettings.getAutoIncrementVersionOnSave());
        return autoIncVersionCheckBox;
    }

    private JCheckBox getBackupCheckBox() {
        backupCheckBox = new JCheckBox();
        backupCheckBox.setText("Create backup");
        backupCheckBox.setToolTipText(" Keep the previous copy of this file ");
        backupCheckBox.setMnemonic(KeyEvent.VK_B);
        backupCheckBox.setSelected(UserSettings.getFileBackupOnSave());
        return backupCheckBox;
    }

    private JCheckBox getVersionCopyCheckBox() {
        versionCopyCheckBox = new JCheckBox();
        versionCopyCheckBox.setText("File Versioning");
        versionCopyCheckBox.setToolTipText(" Save all previous versions of this file ");
        versionCopyCheckBox.setMnemonic(KeyEvent.VK_P);
        versionCopyCheckBox.setSelected(UserSettings.getFileVersioningOnSave());
        return versionCopyCheckBox;
    }

    private JCheckBox getShowDialogCheckBox() {
        showDialogCheckBox = new JCheckBox();
        showDialogCheckBox.setText("Show this dialog for each save");
        showDialogCheckBox.setToolTipText(" Show this dialog every time you save a file ");
        showDialogCheckBox.setMnemonic(KeyEvent.VK_D);
        showDialogCheckBox.setSelected(UserSettings.getShowFileOptionsDialogOnSave());
        return showDialogCheckBox;
    }

}
