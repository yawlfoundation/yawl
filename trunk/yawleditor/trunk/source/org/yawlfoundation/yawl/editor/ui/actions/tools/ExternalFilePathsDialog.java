package org.yawlfoundation.yawl.editor.ui.actions.tools;

import org.yawlfoundation.yawl.editor.ui.swing.AbstractDoneDialog;
import org.yawlfoundation.yawl.editor.ui.util.FileUtilities;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * Author: Michael Adams
 * Creation Date: 20/04/2010
 */
public class ExternalFilePathsDialog extends AbstractDoneDialog {

    private JTextField _fldDecomposition;
    private JTextField _fldVariable;
    private JTextField _fldIcons;
    private JTextField _fldWofyawl;
    private JTextField _fldWendy;

    public static final String PROPERTY_LOCATION =
            FileUtilities.getDecompositionAttributePath();


    public ExternalFilePathsDialog() {
        super("External File Paths Settings", true);
        setContentPanel(buildContentPanel());
        setSize(new Dimension(600, 385));
        setResizable(false);

        getDoneButton().addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               UserSettings.setDecompositionAttributesFilePath(_fldDecomposition.getText());
               UserSettings.setVariableAttributesFilePath(_fldVariable.getText());
               UserSettings.setTaskIconsFilePath(checkPath(_fldIcons.getText()));
               UserSettings.setWofyawlFilePath(_fldWofyawl.getText());
               UserSettings.setWendyFilePath(checkPath(_fldWendy.getText()));
      //         PaletteBar.getInstance().updatePluginIcons();
           }

           private String checkPath(String path) {
               if (path.endsWith("/") || path.endsWith("\\")) {
                   path = path.substring(0, path.length() - 1);
               }
               return path;
           }
        });
    }


    private JPanel buildContentPanel() {
        _fldVariable = new JTextField(getVariablePath());
        _fldDecomposition = new JTextField(getDecompositionPath());
        _fldIcons = new JTextField(getTaskIconsPath());
        _fldWofyawl = new JTextField(getWofyawlPath());
        _fldWendy = new JTextField(getWendyPath());

        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPanel.add(buildFileEntryPanel(_fldDecomposition,
                "Decomposition Extended Attributes File"));
        contentPanel.add(buildFileEntryPanel(_fldVariable, "Variable Extended Attributes File"));
        contentPanel.add(buildFileEntryPanel(_fldIcons, "Task Icons Folder"));
        contentPanel.add(buildFileEntryPanel(_fldWofyawl, "WofYAWL Analysis File"));
        contentPanel.add(buildFileEntryPanel(_fldWendy, "Wendy (Process Configuration) Folder"));
        return contentPanel;
    }


    private JPanel buildFileEntryPanel(JTextField textField, String title) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new TitledBorder(title));
        panel.add(buildFileButton(title), BorderLayout.EAST);
        textField.setPreferredSize(new Dimension(540, 20));
        panel.add(textField, BorderLayout.CENTER);
        return panel;
    }

    private JButton buildFileButton(String title) {
        JButton button = new JButton("...");
        button.setPreferredSize(new Dimension(25, 15));
        button.setToolTipText(" Select File Dialog ");
        button.setActionCommand(title.substring(0, title.indexOf(' ')));

        final JDialog thisDialog = this;
        button.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e) {
               String cmd = e.getActionCommand();
               JFileChooser fileChooser = new JFileChooser(getInitialDir(cmd));
               fileChooser.setDialogTitle("Select " + getDialogTitle(cmd));
               if (cmd.equals("Task") || cmd.equals("Wendy")) {
                   fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
               }
               if (fileChooser.showOpenDialog(thisDialog) == JFileChooser.APPROVE_OPTION) {
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
